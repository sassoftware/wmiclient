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
	 * Set a registry key.
	 * 
	 * @param keyPath Path to the key in the registry
	 * @param key Key to set
	 * @param values Array of strings to set as the value of key
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public void setKey(String keyPath, String key, String[] values) throws UnknownHostException, JIException {
		// Convert values to a byte array
		byte[][] data = new byte[values.length][];
		for (int i=0; i<values.length; i++) {
			data[i] = values[i].getBytes();
		}

		// Get a handle for talking to the registry
		RegistryHandle handle = new RegistryHandle(this.address, this.authInfo);
		
		// Get an instance of the key to modify
		JIPolicyHandle regkey = handle.openKey(keyPath);
		
		// Set the value
		handle.registry.winreg_SetValue(regkey, key, data);

		// Tear down connection
		handle.closeConnection();
		
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
		return this.getKey(keyPath, key, 2048);
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
		if (dtype != IJIWinReg.REG_MULTI_SZ) {
			output = new String[]{ new String((byte [])data[1]), };
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
