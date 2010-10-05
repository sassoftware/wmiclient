/**
 * Copyright (c) 2010 rPath, Inc.
 */
package com.rpath.management.windows;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for static utility methods.
 */
public class Utils {
	/**
	 * Subselect a String array starting and ending at a given point.
	 * 
	 * @param input an array of strings to subselect from
	 * @param start an index into the input array to start copying
	 * @param stop an index into the input to stop copying
	 */
	public static String[] slice(String[] input, int start, int stop) {
		if (stop > input.length - 1) {
			stop = input.length - 1;
		}
		
		String[] output = new String[(stop-start)+1];
		for (int i=0; i<output.length; i++) {
			output[i] = input[i+start];
		}
		
		return output;
	}
	
	/**
	 * Subselect a String array starting at a given point
	 * 
	 * @param input an array if strings to subselect from
	 * @param start an index into the input array to start copying
	 */
	public static String[] slice(String[] input, int start) {
		return slice(input, start, input.length);
	}
	
	/**
	 * Join a string on a separator.
	 * @param input Input string array
	 * @param delemeter String to insert between each element if the input
	 */
	public static String join(String[] input, String delemeter) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<input.length; i++) {
			buff.append(input[i]);
			if (i<input.length-1)
				buff.append(delemeter);
		}
		return buff.toString();
	}
	
	/**
	 * Join a string on space
	 * @param input Input string array
	 */
	public static String join(String[] input) {
		return join(input, " ");
	}
}