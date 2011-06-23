/**
 * Copyright (c) 2010 rPath, Inc.
 */
package com.rpath.management.windows;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for interacting with remote Windows services
 */
public class Services {
	private Session session = null;
	private Query query = null;
	
	private final StatusCodes status_codes = new StatusCodes(new Object[][] {
		{0, "Success"},
		{1, "Not Supported"},
		{2, "Access Denied"},
		{3, "Dependent Services Running"},
		{4, "Invalid Service Control"},
		{5, "Service Cannot Accept Control"},
		{6, "Service Not Active"},
		{7, "Service Request Timeout"},
		{8, "Unknown Failure"},
		{9, "Path Not Found"},
		{10, "Service Already Running"},
		{11, "Service Database Locked"},
		{12, "Service Dependency Deleted"},
		{13, "Service Dependency Failure"},
		{14, "Service Disabled"},
		{15, "Service Logon Failure"}, 
		{16, "Service Marked For Deletion"},
		{17, "Service No Thread"},
		{18, "Status Circular Dependency"},
		{19, "Status Duplicate Name"},
		{20, "Status Invalid Name"},
		{21, "Status Invalid Parameter"},
		{22, "Status Invalid Service Account"},
		{23, "Status Service Exists"},
		{24, "Service Already Paused"},
	});
	
	/**
	 * Constructor for services class
	 * 
	 * @param session j-interop session for communicating via WMI
	 */
	public Services(Session session) {
		this.session = session;
		this.query = new Query(this.session);
	}
	
	/**
	 * Start a service given its name
	 * 
	 * @param serviceName name of the service to start
	 * @throws JIException 
	 */
	public String[] startService(String serviceName) throws JIException {
		return this.status_codes.reportStatus(this.service(serviceName, "StartService"));
	}
	
	/**
	 * Stop a service given its name
	 * 
	 * @param serviceName name of the service to stop
	 * @throws JIException 
	 */
	public String[] stopService(String serviceName) throws JIException {
		return this.status_codes.reportStatus(this.service(serviceName, "StopService"));
	}
	
	/**
	 * Query the status of a service.
	 * 
	 * @param serviceName name of the service to query
	 * @throws JIException 
	 */
	public String[] getStatus(String serviceName) throws JIException {
		return this.status_codes.reportStatus(this.service(serviceName, "InterrogateService"), false);
	}
	
	/**
	 * Perform an action on a set of services.
	 * 
	 * @param seviceName name of the service act on
	 * @param action name of the method to execute for a given service
	 * @throws JIException 
	 */
	private Integer[] service(String serviceName, String action) throws JIException {
		// Query the machine for instances of the given service name
		String queryStr = "SELECT * FROM Win32_Service WHERE Caption='" + serviceName + "'";
		ArrayList<JIVariant> queryList = this.query.query(queryStr);
		
		// Create an array for storing status information
		Integer[] status = new Integer[queryList.size()];
		
		for(int i=0; i<queryList.size();i++) {

			JIVariant service = queryList.get(i);
			
			// Get a dispatcher to control the specific service
			IJIDispatch dispatch = (IJIDispatch)narrowObject(service.getObjectAsComObject());
			
			// Invoke the specified action
			status[i] = dispatch.callMethodA(action).getObjectAsInt();
		}
		return status;
	}
}