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

import java.util.Hashtable;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for storing status and common formatting methods.
 */
public class StatusCodes {
	@SuppressWarnings("rawtypes")
	private Hashtable hash = null;
	
	/**
	 * Constructor
	 */
	@SuppressWarnings("rawtypes")
	public StatusCodes() {
		this.hash = new Hashtable();
	}
	
	/**
	 * Constructor
	 * @param input A two dimensional object array of return code/status string pairs.
	 */
	@SuppressWarnings("rawtypes")
	public StatusCodes(Object[][] input) {
		this.hash = new Hashtable();
		
		Integer rc = null;
		String status = null;
		for (int i=0; i<input.length; i++) {
			rc = (Integer)input[i][0];
			status = (String)input[i][1];
			
			this.set(rc, status);
		}
	}
	
	/**
	 * Get a status string given a code.
	 * @param code
	 */
	public String get(int code) {
		if (!this.has_status(code))
			return null;
		return (String)this.hash.get((Integer)code);
	}
	
	/**
	 * Set a return code status string pair.
	 * @param code
	 * @param status
	 */
	@SuppressWarnings("unchecked")
	public void set(int code, String status) {
		this.hash.put((Integer)code, status);
	}
	
	/**
	 * Check if a return code is defined.
	 * @param code
	 */
	public boolean has_status(int code) {
		return this.hash.containsKey((Integer)code);
	}
	
	/**
	 * Check if a return code is an error.
	 * @param code
	 */
	public boolean is_error(int code) {
		if (code != 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Report status
	 * 
	 * @param onlyErrors only report actions that failed
	 */
	 public String[] reportStatus(Integer[] status, boolean onlyErrors) {
		 String[] results = new String[status.length];
		 for (int i=0; i<status.length; i++) {
			 if (onlyErrors && !this.is_error(status[i])) {
				 results[i] = null;
				 continue;
			 }
			 results[i] = this.get(status[i]);
		 }
		 return results;
	 }
	 
	 /**
	  * Report status
	  */
	 public String[] reportStatus(Integer[] status) {
		 return this.reportStatus(status, true);
	 }
}
