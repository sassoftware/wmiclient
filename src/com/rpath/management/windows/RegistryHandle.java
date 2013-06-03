/*
 * Copyright (c) SAS Institute Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.rpath.management.windows;

import java.net.UnknownHostException;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for handling a registry connection
 */
public class RegistryHandle {
	public IJIWinReg registry = null;
	private JIPolicyHandle hkey = null;
	private JIPolicyHandle key = null;
	
	/**
	 * Constructor for registry interactions.
	 * 
	 * @param address IP address or DNS name of the system to contact
	 * @param authInfo Authentication token for WMI interactions
	 * @throws UnknownHostException 
	 * @throws JIException 
	 */
	public RegistryHandle(String address, IJIAuthInfo authInfo) throws UnknownHostException, JIException {
		// Get a connection to the remote registry
		this.registry = JIWinRegFactory.getSingleTon().getWinreg(authInfo, address, true);

		// Open HKey Local Machine from the registry
		this.hkey = registry.winreg_OpenHKLM();
	}
	
	
	/**
	 * Open a specific key if this handle hasn't been used to open a key already.
	 * 
	 * @param keyPath path to the requested key.
	 * @throws JIException 
	 */
	public JIPolicyHandle openKey(String keyPath) throws JIException {
		// Close the instances key if it is already open.
		if (this.key != null) {
			this.closeKey();
		}

		// Open the requested key from the registry
		this.key = registry.winreg_OpenKey(this.hkey, keyPath, IJIWinReg.KEY_ALL_ACCESS);

		return this.key;
	}
	
	/**
	 * Close the last opened key.
	 * @throws JIException 
	 */
	public void closeKey() throws JIException {
		if (this.key != null) {
			this.registry.winreg_CloseKey(this.key);
			this.key = null;
		}
	}
	
	/**
	 * Tear down the registry handle.
	 * @throws JIException 
	 */
	public void closeConnection() throws JIException {
		// Close connections in the opposite order they were opened.
		
		// Make sure the key is closed first.
		this.closeKey();
		
		// Close the hkey.
		this.registry.winreg_CloseKey(this.hkey);
		
		// Close the registry connection
		this.registry.closeConnection();
	}
}
