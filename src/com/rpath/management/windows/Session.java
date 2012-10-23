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
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class to track WMI Session
 */
public class Session {
	private String address = null;
	private JISession session = null;
	private IJIDispatch dispatch = null;
	private JIComServer comServer = null;
	private IJIComObject comObject = null;

	// Magic WMI UUID to connect to.
	private final String INTERFACE_UUID = "76A6415B-CB41-11d1-8B02-00600806D9B6";
	
	/**
	 * Constructor for the session class
	 * 
	 * @param address Address of the Windows machine to communicate with
	 * @param authInfo Authentication information for the machine
	 * @throws UnknownHostException
	 * @throws JIException
	 */
	public Session(String address, IJIAuthInfo authInfo) throws UnknownHostException, JIException {
		this.address = address;
		
		// The session is used for everything except talking to the registry
		this.session = JISession.createSession(authInfo);
		this.session.useSessionSecurity(true);
		this.session.setGlobalSocketTimeout(5000);
		
		// ISWbemLocator
		this.comServer = new JIComServer(JIProgId.valueOf("WbemScripting.SWbemLocator"), this.address, this.session);
		
		// HKLM/SOFTWARE/Classes/Interface
		IJIComObject tmp = this.comServer.createInstance();
		this.comObject = (IJIComObject)tmp.queryInterface(this.INTERFACE_UUID);
		
		// Dispatch interface
		this.dispatch = (IJIDispatch)JIObjectFactory.narrowObject(this.comObject.queryInterface(IJIDispatch.IID));
	}
	
	/**
	 * Get a dispatcher to work with
	 * @return com object dispatcher
	 * @throws JIException
	 */
	public IJIDispatch getDispatch() throws JIException {
		Object[] params = new Object[] {
			new JIString(this.address),
			JIVariant.OPTIONAL_PARAM(),
			JIVariant.OPTIONAL_PARAM(),
			JIVariant.OPTIONAL_PARAM(),
			JIVariant.OPTIONAL_PARAM(),
			JIVariant.OPTIONAL_PARAM(),
			new Integer(0),
			JIVariant.OPTIONAL_PARAM(),
		};
		
		JIVariant results[] = this.dispatch.callMethodA("ConnectServer", params);
		return (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
	}
	
	/**
	 * Close the WMI session.
	 * @throws JIException
	 */
	public void closeConnection() throws JIException {
		JISession.destroySession(this.session);
	}
}
