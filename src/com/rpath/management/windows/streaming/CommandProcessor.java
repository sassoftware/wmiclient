package com.rpath.management.windows.streaming;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;

import org.jinterop.dcom.common.JIException;

import com.rpath.management.windows.ManagedSystem;
import com.rpath.management.windows.Utils;

public class CommandProcessor extends IPC {
	private ManagedSystem system;
	
	public CommandProcessor(ManagedSystem system, InputStream input, PrintStream output, PrintStream error) {
		super(input, output, error);
		this.system = system;
	}

	@Override
	protected void processCommand() {
		String cmd = this.command[0];
		if (cmd.equals("registry"))
			this.handleRegistryCommand();
		else if (cmd.equals("service"))
			this.handleServiceCommand();
		else if (cmd.equals("process"))
			this.handleProcessCommand();
		else if (cmd.equals("query"))
			this.handleQueryCommand();
		else
			this.reportError("invalid command " + cmd);
	}

	private void handleRegistryCommand() {
		String cmd = this.command[1];
		if (cmd.equals("getkey"))
			this.handleRegistryGetKey();
		else if (cmd.equals("setkey"))
			this.handleRegistrySetKey();
		else if (cmd.equals("createkey"))
			this.handleRegistryCreateKey();
		else
			this.reportError("invalid option registry " + cmd);
	}

	private void handleRegistryGetKey() {
		String[] data;
		
		try {
			data = this.system.registry.getKey(this.command[2], this.command[3]);
		} catch (UnknownHostException e) {
			this.reportError("getkey failed");
			return;
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
			return;
		}
		
		this.startOutput();
		this.writeOutput(data);
		this.endOutput();
	}

	private void handleRegistrySetKey() {
		String[] values = Utils.slice(this.command, 4);
		
		try {
			this.system.registry.setKey(this.command[2], this.command[3], values);
		} catch (UnknownHostException e) {
			this.reportError("setkey failed");
			return;
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
			return;
		}
		
		this.startOutput();
		this.writeOutput("done");
		this.endOutput();
	}

	private void handleRegistryCreateKey() {
		try {
			this.system.registry.createKey(this.command[2], this.command[3]);
		} catch (UnknownHostException e) {
			this.reportError("createkey failed");
			return;
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
			return;			
		}
		
		this.startOutput();
		this.writeOutput("done");
		this.endOutput();
	}

	private void handleServiceCommand() {
	}

	private void handleProcessCommand() {
	}
	
	private void handleQueryCommand() {
	}
}