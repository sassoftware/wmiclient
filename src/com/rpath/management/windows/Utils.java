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
