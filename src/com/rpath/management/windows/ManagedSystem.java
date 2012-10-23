/*
 * Copyright (c) rPath, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
