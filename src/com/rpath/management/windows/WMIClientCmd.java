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
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		
		ManagedSystem system = null;
		
		try {
			system = new ManagedSystem(host, domain, user, password);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		} catch (JIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		if (remaining[0].equals("registry")) {
			try {
				registryCmd(system, Utils.slice(remaining, 1));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			} catch (JIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		} else if (remaining[0].equals("service")) {
			try {
				serviceCmd(system, Utils.slice(remaining, 1));
			} catch (JIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			printUsage("Sub command not found: " + remaining[0]);
		}
		
		System.exit(0);
	}
	
	/**
	 * Parse arguments
	 * 
	 * @param args argument array
	 */
	private static CommandLine parseArguments(String[] args) {
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
		
		Option host = OptionBuilder.withLongOpt("host").withArgName("hostname or IP").hasArg().withDescription("hostname or IP address to connect to").isRequired().create();
		Option domain = OptionBuilder.withLongOpt("domain").withArgName("authentication domain").hasArg().withDescription("authentication domain").isRequired().create();
		Option user = OptionBuilder.withLongOpt("user").withArgName("username").hasArg().withDescription("username").isRequired().create();
		Option password = OptionBuilder.withLongOpt("password").withArgName("password").hasArg().withDescription("password").isRequired().create();
		
		options.addOption(help);
		options.addOption(verbose);
		options.addOption(debug);
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
		System.out.println("    service start <serviceName>");
		System.out.println("    service stop <serviceName>");
		System.out.println("    fs get <filePath>");
		System.out.println("    fs set <filePath>");
		
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
			printUsage("registry <getkey|setkey>");
		
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
		} else {
			printUsage("registry <getkey|setkey>");
		}

		// Execute registry command
		if (action.equals("getkey")) {
			String[] data = system.registry.getKey(options[0], options[1]);
			for (int i=0; i<data.length; i++)
				System.out.println(data[i]);
		} else if (action.equals("setkey")) {
			String[] values = Utils.slice(options, 2);
			system.registry.setKey(options[0], options[1], values);
		}
	}
	
	/**
	 * Handle the service sub command.
	 * @throws JIException 
	 */
	private static void serviceCmd(ManagedSystem system, String[] args) throws JIException {
		if (args.length == 0)
			printUsage("service <start|stop> <serviceName>");
		
		// Parse service command line
		String[] options = null;
		String action = args[0].toLowerCase();
		if (action.equals("start") || action.equals("stop")) {
			options = Utils.slice(args, 1);
			if (options.length != 1)
				printUsage("service <start|stop> <serviceName>");
		} else {
			printUsage("service <start|stop> <serviceName>");
		}
		
		// Execute service command
		if (action.equals("start")) {
			system.services.startService(options[0]);
		} else if (action.equals("stop")) {
			system.services.stopService(options[0]);
		}
	}
}
