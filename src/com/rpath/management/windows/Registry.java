/**
 * Copyright (c) 2010 rPath, Inc.
 */

package com.rpath.management.windows;

import java.net.UnknownHostException;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * A class for interacting with the Windows registry via WMI
 */
public class Registry {

	private String address = null;
	private IJIAuthInfo authInfo = null;
	
	/**
	 * Constructor for registry interactions.
	 * 
	 * @param address IP address or DNS name of the system to contact
	 * @param authInfo Authentication token for WMI interactions
	 */
	public Registry(String address, IJIAuthInfo authInfo) {
		this.address = address;
		this.authInfo = authInfo;
	}
		
	/**
	 * Retrieve a value from the registry.
	 * 
	 * @param keyPath Path to the key in the registry
	 * @param key Key to access
	 * @param expectedSize the expected size of the value in the registry
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public String[] getKey(String keyPath, String key, int expectedSize) throws JIException, UnknownHostException {
		// Get a handle for talking to the registry
		RegistryHandle handle = new RegistryHandle(this.address, this.authInfo);

		// Get the key that we are looking for
		JIPolicyHandle regkey = handle.openKey(keyPath);
		
		// Access the value of that key
		Object[] data = handle.registry.winreg_QueryValue(regkey, key, expectedSize);

		// Format output into a string array.
		String[] output = this.formatOutput(data);
		
		// Close the registry connection
		handle.closeConnection();
		
		return output;
	}
	
	/**
	 * Retrieve a value from the registry.
	 * 
	 * @param keyPath Path to the key in the registry
	 * @param key Key to access
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public String[] getKey(String keyPath, String key) throws UnknownHostException, JIException {
		return this.getKey(keyPath, key, 2*1024*1024);
	}

	/**
	 * Set a registry key.
	 * 
	 * @param keyPath Path to the key in the registry
	 * @param key Key to set
	 * @param values Array of strings to set as the value of key
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public void setKey(String keyPath, String key, String[] values) throws UnknownHostException, JIException {
		// Get a handle for talking to the registry
		RegistryHandle handle = new RegistryHandle(this.address, this.authInfo);
		
		// Get an instance of the key to modify
		JIPolicyHandle regkey = handle.openKey(keyPath);

		// Read the key to get the type
		Object[] oldData = handle.registry.winreg_QueryValue(regkey, key, 2*1024*1024);
		Integer dtype = (Integer)oldData[0];

		// Create and set the new data
		if (dtype == IJIWinReg.REG_DWORD) {
			int regData = Integer.parseInt(values[0]);
			handle.registry.winreg_SetValue(regkey, key, regData);			
		} else if (dtype == IJIWinReg.REG_SZ || dtype == IJIWinReg.REG_EXPAND_SZ) {
			byte[] regData = values[0].getBytes();
			handle.registry.winreg_SetValue(regkey, key, regData, false, dtype == IJIWinReg.REG_EXPAND_SZ);			
		} else if (dtype == IJIWinReg.REG_BINARY) {
			byte[] regData = new byte[values.length];
			for (int i=0; i<values.length; i++) { 
				String bs = values[i].substring(2); // ignore the 0x when we convert it to a byte
				regData[i] =  (byte)(Integer.parseInt(bs, 16));
			}
			handle.registry.winreg_SetValue(regkey, key, regData, true, false);			
		} else {
			// It must be a REG_MULTI_SZ
			byte[][] regData = new byte[values.length][];
			for (int i=0; i<values.length; i++) {
				regData[i] = values[i].getBytes();
			}
			handle.registry.winreg_SetValue(regkey, key, regData);			
		}
		
		// Tear down connection
		handle.closeConnection();
	}

	/**
	 * Create a key that doesn't already exist.
	 * 
	 * @param keyPath Path to the key in the registry
	 * @param key Key to create
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public void createKey(String keyPath, String key) throws JIException, UnknownHostException {
		// Get a handle for talking to the registry
		RegistryHandle handle = new RegistryHandle(this.address, this.authInfo);
		
		// Get an instance of the key to create
		JIPolicyHandle regkey = handle.openKey(keyPath);
		
		// Create the new key
		JIPolicyHandle newKey = handle.registry.winreg_CreateKey(regkey, key, IJIWinReg.REG_OPTION_NON_VOLATILE, IJIWinReg.KEY_ALL_ACCESS);
		handle.registry.winreg_CloseKey(newKey);
		
		// Tear down the connection to the registry
		handle.closeConnection();
	}
	
	/**
	 *  Helper function to convert a byte array to an int using 4 bytes
	 * @param arr the byte array
	 * @param start the index to start at
	 * @return
	 */
	public static int arr2int(byte[] arr, int start) {
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		int accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return accum;
	}

	/**
	 * Format the output from a get request.
	 * 
	 * @param data return from winreg_QueryValue
	 * @return array of strings
	 */
	private String[] formatOutput(Object[] data) {
		String[] output = null;
		
		Integer dtype = (Integer)data[0];

		// Handle simple non-list type
		if (dtype == IJIWinReg.REG_SZ || dtype == IJIWinReg.REG_EXPAND_SZ) {
			String s = new String((byte[])data[1]);
			output = new String[]{s, };
			return output;
		} else if (dtype == IJIWinReg.REG_BINARY) {
			byte[] b = ((byte[])data[1]);
			output = new String[b.length];
			for (int i=0; i<b.length; i++) 
				output[i] = "0x" + Integer.toHexString(b[i]);
			return output;
		} else if (dtype == IJIWinReg.REG_DWORD) {
			int i = arr2int((byte[])(data[1]), 0);
			String s = Integer.toString(i);
			output = new String[]{s, };
			return output;
		}	
		// And now for the more complex results
		byte[][] lines = (byte[][])data[1];
		
		// Find the first null line
		int size = 0;
		while (lines[size] != null)
			size++;
		
		output = new String[size];
		
		for (int i=0; i<output.length; i++) {
			output[i] = new String(lines[i]);
		}
		
		return output;
	}
}
