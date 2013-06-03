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

import java.io.PrintStream;

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
	
	/**
	 * Display an array of strings, leaving out any null elements
	 * @param output array of strings
	 * @param out output stream to print them to
	 */
	public static void displayStringArray(String[] output, PrintStream out) {
		if (output != null) {
			for (String o : output) {
				if (o != null)
					out.println(o);
			}
		}
	}
}
