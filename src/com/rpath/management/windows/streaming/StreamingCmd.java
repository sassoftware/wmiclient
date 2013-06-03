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
