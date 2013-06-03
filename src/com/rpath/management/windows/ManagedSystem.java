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
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * This class is meant to represent all WMI interactions with a given system.
 */
public class ManagedSystem {
	private Session session = null;
	
	public Registry registry = null;
	public Services services = null;
	public Processes processes = null;
	public Query query = null;

	/**
	 * Constructor for the creation of system connections.
	 * 
	 * @param address IP address or DNS name of the system to contact
	 * @param domain Windows authentication domain (machine name if not setup for domain auth)
	 * @param username User authorized to make WMI calls (normally administrator)
	 * @param password User's password
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	public ManagedSystem(String address, String domain, String username, String password) throws UnknownHostException, JIException {		
		// AuthInfo is required for registry manipulation.
		IJIAuthInfo authInfo = new JIDefaultAuthInfoImpl(domain, username, password);

		// Session for interacting with WMI
		this.session = new Session(address, authInfo);

		// Registry instance for all registry related interaction
		this.registry = new Registry(address, authInfo);

		// Services instance for interacting with services on the client machine.
		this.services = new Services(this.session);
		
		// Processes instance for interacting with the processes on the client machine.
		this.processes = new Processes(this.session);

		// Query instance for querying the client machine via WQL
		this.query = new Query(this.session);
		
	}
}
