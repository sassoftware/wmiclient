/**
 * Copyright (c) 2010 rPath, Inc.
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
	}
}
