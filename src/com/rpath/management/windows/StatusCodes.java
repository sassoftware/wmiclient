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
