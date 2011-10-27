package com.rpath.management.windows;

public class ServiceNotFoundError extends Exception {

	public String error = "No such service: ";
	public String serviceName = null;
	
	public ServiceNotFoundError(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getError() {
		return this.error + this.serviceName;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
