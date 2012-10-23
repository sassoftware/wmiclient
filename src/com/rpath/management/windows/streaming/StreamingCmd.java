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


package com.rpath.management.windows.streaming;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.jinterop.dcom.common.JIException;

import com.rpath.management.windows.JILogging;
import com.rpath.management.windows.ManagedSystem;
import com.rpath.management.windows.WMIClientCmd;

/**
 * @author Elliot Peele
 */
public class StreamingCmd {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLine cmdline = WMIClientCmd.parseArguments(args);

		String host = cmdline.getOptionValue("host");
		String user = cmdline.getOptionValue("user");
		String domain = cmdline.getOptionValue("domain");
		String password = cmdline.getOptionValue("password");

		@SuppressWarnings("unused")
		Level level = Level.WARNING;
		if (cmdline.hasOption("debug")) {
			level = Level.ALL;
		} else if (cmdline.hasOption("verbose")) {
			level = Level.INFO;
		}

		try {
			@SuppressWarnings("unused")
			JILogging log = new JILogging(level);
		} catch (SecurityException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		ManagedSystem system = null;
		
		try {
			system = new ManagedSystem(host, domain, user, password);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (JIException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		CommandProcessor processor = new CommandProcessor(system, System.in, System.out, System.err);

		try {
			processor.run();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
