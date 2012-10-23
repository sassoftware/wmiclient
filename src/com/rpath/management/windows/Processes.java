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

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for managing processes on a remote machine over WMI.
 */
public class Processes {
	private Session session = null;
	private Query query = null;
	
	private final StatusCodes status_codes = new StatusCodes(new Object[][] {
		{0, "Successful Completion"},
		{2, "Access Denied"},
		{3, "Insufficient Privilege"},
		{8, "Unknown failure"},
		{9, "Path Not Found"},
		{21, "Invalid Parameter"},
	});
	
	/**
	 * Constructor
	 * @param session
	 */
	public Processes(Session session) {
		this.session = session;
		this.query = new Query(this.session);
	}
	
	/**
	 * Create a process by running a specified command.
	 * @param cmd Command to execute
	 * @return process ID of the running process
	 * @throws JIException 
	 */
	public int create(String[] cmd) throws JIException {
		// Get a process handle
		IJIDispatch handle = this.getHandle();
		
		// Create the process
		// The process Id will be in the second element of the results array
		JIVariant processId = JIVariant.EMPTY_BYREF();
		
		Object[] params = new Object[] {
				new JIString(Utils.join(cmd)),
				JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(),
				processId,
		};
		
		JIVariant[] results = handle.callMethodA("Create", params);
		int pid = results[1].getObjectAsVariant().getObjectAsInt();

		return pid;
	}

	/**
	 * Kill a process
	 * @param pid Process ID to kill
	 */
	public void kill(int pid) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Query the status of a process.
	 * @param pid Process ID to query
	 * @return status string of the requested process
	 * @throws JIException 
	 */
	public String[] status(int pid) throws JIException {
		// Query results
		JIVariant[] queryResults = this.queryPid(pid);
		
		Integer[] results = new Integer[queryResults.length];
		for (int i=0; i<queryResults.length; i++)
			results[i] = queryResults[i].getObjectAsInt();
			
		return this.status_codes.reportStatus(results);
	}

	/**
	 * Create a new Win32 Process handle
	 * @throws JIException 
	 */
	private IJIDispatch getHandle() throws JIException {
		IJIDispatch comDispatch = this.session.getDispatch();

		// Build query params
		Object[] params = new Object[] {
				new JIString("Win32_Process"),
				new Integer(0),
				JIVariant.OPTIONAL_PARAM()
		};
		
		// Get the Win32_Process handle
		JIVariant[] processHandle = comDispatch.callMethodA("Get", params);
		
		// Get the Dispatcher
		IJIDispatch dispatch = (IJIDispatch)JIObjectFactory.narrowObject(processHandle[0].getObjectAsComObject());

		return dispatch;
	}
	
	/**
	 * Query process by process id
	 * @param pid Process ID to search for
	 * @throws JIException 
	 */
	private JIVariant[] queryPid(int pid) throws JIException {
		String queryStr = "Select * From Win32_ProcessStopTrace where ProcessID=" + pid;
		return (JIVariant[])this.query.query(queryStr).toArray();
	}
}
