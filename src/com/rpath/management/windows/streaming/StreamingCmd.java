/**
 * 
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
		}
	}
}