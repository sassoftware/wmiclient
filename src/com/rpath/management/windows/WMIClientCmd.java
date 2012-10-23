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


 	/**
 * Copyright (c) 2010 rPath, Inc. 
 */
package com.rpath.management.windows;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jinterop.dcom.common.JIException;
import com.rpath.management.windows.streaming.CommandProcessor;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class to provide an command line interface.
 */
public class WMIClientCmd {
	private final static String usage = "wmiclient [options] <action>";

	/**
	 * Main method
	 * 
	 * @param args list of arguments
	 */
	public static void main(String[] args) {
		CommandLine cmdline = parseArguments(args);
		
		String host = cmdline.getOptionValue("host");
		String user = cmdline.getOptionValue("user");
		String domain = cmdline.getOptionValue("domain");
		String password = cmdline.getOptionValue("password");

		String[] remaining = cmdline.getArgs();
		
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
		
		/* 
		 * Exceptions need to be handled differently for the interactive
		 * commands so that status gets reported correctly.
		 */
		if (cmdline.hasOption("interactive")) {
			CommandProcessor processor = new CommandProcessor(system, System.in, System.out, System.err);
			
			try {
				system = new ManagedSystem(host, domain, user, password);
				processor.setSystem(system);
				processor.run();
			} catch (UnknownHostException e) {
				processor.reportException(e);
				System.exit(1);
			} catch (JIException e) {
				processor.reportError(e.getErrorCode());
				processor.reportException(e);
				System.exit(1);
			} catch (ServiceNotFoundError e) {
				processor.reportError(e.getError());
				processor.reportException(e);
				System.exit(1);
			} catch (Exception e) {
				processor.reportException(e);
				System.exit(1);
			}

			System.exit(0);
		}
		
		/*
		 * Handling for non interactive commands.
		 */
		try {
			system = new ManagedSystem(host, domain, user, password);

			if (remaining[0].equals("registry")) {
				registryCmd(system, Utils.slice(remaining, 1));
			} else if (remaining[0].equals("service")) {
				serviceCmd(system, Utils.slice(remaining, 1));
			} else if (remaining[0].equals("process")) {
				processCmd(system, Utils.slice(remaining, 1));
			} else if (remaining[0].equals("query")) {
				queryCmd(system, Utils.slice(remaining, 1));
			} else {
				printUsage("Sub command not found: " + remaining[0]);
			}
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (JIException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(e.getErrorCode());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.exit(0);
	}

	/**
	 * Parse arguments
	 * 
	 * @param args argument array
	 */
	public static CommandLine parseArguments(String[] args) {
		CommandLine cmdline = null;
		CommandLineParser parser = new DefaultParser();
		Options options = getOptions();
		try {
			cmdline = parser.parse(options, args);
		}
		catch (ParseException exp) {
			printUsage("Command line parsing failed: " + exp.getMessage());
		}
		
		if (cmdline.hasOption("help")) {
			printUsage(0);
		}
		return cmdline;
	}

	/**
	 * Build up options structure.
	 * @return populated options structure.
	 */
	@SuppressWarnings("static-access")
	private static Options getOptions() {
		Options options = new Options();

		Option help = OptionBuilder.withLongOpt("help").withDescription("print this message").create();
		Option verbose = OptionBuilder.withLongOpt("verbose").withDescription("be more verbose").create();
		Option debug = OptionBuilder.withLongOpt("debug").withDescription("print debugging information").create();
		Option interactive = OptionBuilder.withLongOpt("interactive").withDescription("run in interactive mode").create();
		
		Option host = OptionBuilder.withLongOpt("host").withArgName("hostname or IP").hasArg().withDescription("hostname or IP address to connect to").isRequired().create();
		Option domain = OptionBuilder.withLongOpt("domain").withArgName("authentication domain").hasArg().withDescription("authentication domain").isRequired().create();
		Option user = OptionBuilder.withLongOpt("user").withArgName("username").hasArg().withDescription("username").isRequired().create();
		Option password = OptionBuilder.withLongOpt("password").withArgName("password").hasArg().withDescription("password").isRequired().create();
		
		options.addOption(help);
		options.addOption(verbose);
		options.addOption(debug);
		options.addOption(interactive);
		options.addOption(host);
		options.addOption(domain);
		options.addOption(user);
		options.addOption(password);
		
		return options;
	}
	
	/**
	 * Print usage string and exit.
	 * 
	 * @param rc code to exit with
	 */
	private static void printUsage(int rc) {
		printUsage(rc, null);
	}
	
	private static void printUsage(String msg) {
		printUsage(1, msg);
	}
	
	private static void printUsage(int rc, String msg) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(usage, getOptions());
		
		System.out.println();
		System.out.println("Actions:");
		System.out.println("    registry getkey <keyPath> <key>");
		System.out.println("    registry setkey <keyPath> <key> <value>");
		System.out.println("    registry createkey <keyPath> <key>");
		System.out.println("    service start <serviceName>");
		System.out.println("    service stop <serviceName>");
		System.out.println("    service getstatus <servicename>");
		System.out.println("    process create <command>");
		System.out.println("    process kill <pid>");
		System.out.println("    process status <pid>");
		System.out.println("    query network");
		System.out.println("    query uuid");
		
		if (msg != null) {
			System.out.println();
			System.out.println(msg);
		}
		System.exit(rc);		
	}
	
	
	/*
	 * Start command functions.
	 */
	
	/**
	 * Handle the registry sub command.
	 * @throws JIException 
	 * @throws UnknownHostException 
	 */
	private static void registryCmd(ManagedSystem system, String[] args) throws UnknownHostException, JIException {
		if (args.length == 0)
			printUsage("registry <getkey|setkey|createkey>");
		
		// Parse registry command line
		String[] options = null;
		String action = args[0];
		if (action.toLowerCase().equals("getkey")) {
			options = Utils.slice(args, 1);
			if (options.length != 2)
				printUsage("registry getkey <keyPath> <key>");
		} else if (action.toLowerCase().equals("setkey")) {
			options = Utils.slice(args, 1);
			if (options.length < 3)
				printUsage("registry setkey <keyPath> <key> <value>");
		} else if (action.toLowerCase().equals("createkey")) {
			options = Utils.slice(args, 1);
			if (options.length != 2)
				printUsage("registry createkey <keyPath> <key>");
		} else {
			printUsage("registry <getkey|setkey|createkey>");
		}

		// Execute registry command
		if (action.equals("getkey")) {
			String[] data = system.registry.getKey(options[0], options[1]);
			for (int i=0; i<data.length; i++)
				System.out.println(data[i]);
		} else if (action.equals("setkey")) {
			String[] values = Utils.slice(options, 2);
			system.registry.setKey(options[0], options[1], values);
		} else if (action.equals("createkey")) {
			system.registry.createKey(options[0], options[1]);
		}
	}
	
	/**
	 * Handle the service sub command.
	 * @throws JIException 
	 * @throws ServiceNotFoundError 
	 */
	private static void serviceCmd(ManagedSystem system, String[] args) throws JIException, ServiceNotFoundError {
		if (args.length == 0)
			printUsage("service <start|stop> <serviceName>");
		
		// Parse service command line
		String[] options = null;
		String action = args[0].toLowerCase();
		if (action.equals("start") || action.equals("stop") || action.equals("getstatus")) {
			options = Utils.slice(args, 1);
			if (options.length != 1)
				printUsage("service <start|stop|getstatus> <serviceName>");
		} else {
			printUsage("service <start|stop|getstatus> <serviceName>");
		}
		
		// Execute service command
		String[] status = null;
		if (action.equals("start")) {
			status = system.services.startService(options[0]);
		} else if (action.equals("stop")) {
			status = system.services.stopService(options[0]);
		} else if (action.equals("getstatus")) {
			status = system.services.getStatus(options[0]);
		}
		
		Utils.displayStringArray(status, System.out);
	}

	/**
	 * Handle the process sub command.
	 * @throws JIException 
	 */
	private static void processCmd(ManagedSystem system, String[] args) throws JIException {
		if (args.length == 0)
			printUsage("process <create|kill|status>");
		
		// Parse process command line
		String[] options = Utils.slice(args, 1);
		String action = args[0].toLowerCase();
		if (action.equals("create")) {
			if (options.length == 0)
				printUsage("process create <command>");
		} else if (action.equals("kill") || action.equals("status")) {
			if (options.length != 1) {
				printUsage("process <kill|status> <pid>");
			}
		} else {
			printUsage("process <create|kill|status>");
		}
		
		// Execute process command
		if (action.equals("create")) {
			int pid = system.processes.create(options);
			System.out.println(pid);
		} else if (action.equals("kill")) {
			system.processes.kill(Integer.parseInt(options[0]));
		} else if (action.equals("status")) {
			String[] status = system.processes.status(Integer.parseInt(options[0]));
			if (status != null) {
				for (String state : status) {
					if (state != null)
						System.out.println(state);
				}
			}
		}
	}

	/**
	 * Handle the query sub command.
	 * @throws JIException 
	 */
	private static void queryCmd(ManagedSystem system, String[] args) throws JIException {
		String usageStr = "query network|uuid";
		if (args.length != 1)
			printUsage(usageStr);
		
		// Execute process command
		if (args[0].equals("network")) {
			NetworkQueryResults[] results = system.query.queryNetwork();
			system.query.displayNetworkQueryResults(results, System.out);
		} else if (args[0].equals("uuid")) {
			String uuid = system.query.queryUUID();
			System.out.println(uuid);
		} else {
			printUsage(usageStr);
		}
	}
}
