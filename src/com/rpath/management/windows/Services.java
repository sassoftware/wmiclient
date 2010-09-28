/**
 * Copyright (c) 2010 rPath, Inc.
 */
package com.rpath.management.windows;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for interacting with remote Windows services
 */
public class Services {
	private Session session = null;
	
	private final int RETURN_IMMEDIATE = 0x10;
	private final int FORWARD_ONLY = 0x20;

	private final String status_strings[] = new String[] {
		"Success",
		"Not Supported",
		"Access Denied",
		"Dependent Services Running",
		"Invalid Service Control",
		"Service Cannot Accept Control",
		"Service Not Active",
		"Service Request Timeout",
		"Unknown Failure",
		"Path Not Found",
		"Service Already Running",
		"Service Database Locked",
		"Service Dependency Deleted",
		"Service Dependency Failure",
		"Service Disabled",
		"Service Logon Failure", 
		"Service Marked For Deletion",
		"Service No Thread",
		"Status Circular Dependency",
		"Status Duplicate Name",
		"Status Invalid Name",
		"Status Invalid Parameter",
		"Status Invalid Service Account",
		"Status Service Exists",
		"Service Already Paused",
	};
	
	/**
	 * Constructor for services class
	 * 
	 * @param session j-interop session for communicating via WMI
	 */
	public Services(Session session) {
		this.session = session;
	}
	
	/**
	 * Start a service given its name
	 * 
	 * @param serviceName name of the service to start
	 * @throws JIException 
	 */
	public String[] startService(String serviceName) throws JIException {
		return this.reportStatus(this.service(serviceName, "StartService"));
	}
	
	/**
	 * Stop a service given its name
	 * 
	 * @param serviceName name of the service to stop
	 * @throws JIException 
	 */
	public String[] stopService(String serviceName) throws JIException {
		return this.reportStatus(this.service(serviceName, "StopService"));
	}
	
	/**
	 * Query the status of a service.
	 * 
	 * @param serviceName name of the service to query
	 * @throws JIException 
	 */
	public String[] getStatus(String serviceName) throws JIException {
		return this.reportStatus(this.service(serviceName, "InterrogateService"), false);
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
		JIVariant[] queryResults = this.queryServices(serviceName);
		
		// Create an array for storing status information
		Integer[] status = new Integer[queryResults.length];
		
		for (int i=0; i<queryResults.length; i++) {
			JIVariant service = queryResults[i];
			
			// Get a dispatcher to control the specific service
			IJIDispatch dispatch = (IJIDispatch)narrowObject(service.getObjectAsComObject());
			
			// Invoke the specified action
			status[i] = dispatch.callMethodA(action).getObjectAsInt();
		}

		return status;
	}
	
	/**
	 * Get a list of services matching the requested name
	 * 
	 * @param serviceName name of the service to query
	 * @throws JIException 
	 */
	private JIVariant[] queryServices(String serviceName) throws JIException {
		// Get a dispatcher for communicating with the services interface
		IJIDispatch wbemServices = this.session.getDispatch();
		
		// Query options
		Object[] params = this.formatQuery(serviceName);
		
		JIVariant[] servicesSet = wbemServices.callMethodA("ExecQuery", params);
		IJIDispatch wbemObjectSet = (IJIDispatch)narrowObject(servicesSet[0].getObjectAsComObject());

		JIVariant newEnumVariant = wbemObjectSet.get("_NewEnum");
		IJIComObject enumComObject = newEnumVariant.getObjectAsComObject();
		IJIEnumVariant enumVariant = (IJIEnumVariant)narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
		
		Object[] elements = enumVariant.next(1);
		JIArray jiArray = (JIArray)elements[0];
		
		JIVariant[] array = (JIVariant[])jiArray.getArrayInstance();
		return array;
	}
	
	/**
	 * Format WMI query arguments
	 * 
	 * @param serviceName name of the service to interact with
	 */
	private Object[] formatQuery(String serviceName) {
		// Build up query string.
		JIString query = new JIString(
			"SELECT * FROM Win32_Service WHERE Caption='" + serviceName + "'");

		// Construct params array
		Object[] params = new Object[] {
			query,
			JIVariant.OPTIONAL_PARAM(),
			new JIVariant(new Integer(this.RETURN_IMMEDIATE + this.FORWARD_ONLY)),
		};

		return params;
	}
	
	/**
	 * Report status
	 * 
	 * @param onlyErrors only report actions that failed
	 */
	 private String[] reportStatus(Integer[] status, boolean onlyErrors) {
		 String[] results = new String[status.length];
		 for (int i=0; i<status.length; i++) {
			 if (onlyErrors && status[i] == 0) {
				 results[i] = null;
				 continue;
			 }
			 results[i] = this.status_strings[status[i]];
		 }
		 return results;
	 }
	 
	 /**
	  * Report status
	  */
	 private String[] reportStatus(Integer[] status) {
		 return this.reportStatus(status, true);
	 }
}
