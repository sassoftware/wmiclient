package com.rpath.management.windows.streaming;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;

import org.jinterop.dcom.common.JIException;

import com.rpath.management.windows.ManagedSystem;
import com.rpath.management.windows.NetworkQueryResults;
import com.rpath.management.windows.Utils;

public class CommandProcessor extends IPC {
	private ManagedSystem system;
	
	public CommandProcessor(ManagedSystem system, InputStream input, PrintStream output, PrintStream error) {
		super(input, output, error);
		this.system = system;
	}

	public void run() throws Exception {
		try {
			super.run();
		} catch (Exception e) {
			this.reportError(1);
			throw e;
		}
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
		String[] values = Utils.slice(this.command, 4, this.getCommandLength());
		
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
		String cmd = this.command[1];
		if (cmd.equals("start"))
			this.handleServiceStart();
		else if (cmd.equals("stop"))
			this.handleServiceStop();
		else if (cmd.equals("getstatus"))
			this.handleServiceGetStatus();
		else
			this.reportError("invalid command: " + cmd);
	}

	private void handleServiceStart() {
		String[] status = null;
		try {
			status = this.system.services.startService(this.command[2]);
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}

		this.startOutput();
		Utils.displayStringArray(status, this.out);
		this.endOutput();
	}

	private void handleServiceStop() {
		String[] status = null;
		try {
			status = this.system.services.stopService(this.command[2]);
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}
		
		this.startOutput();
		Utils.displayStringArray(status, this.out);
		this.endOutput();
	}

	private void handleServiceGetStatus() {
		String[] status = null;
		try {
			status = this.system.services.getStatus(this.command[2]);
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}
		
		this.startOutput();
		Utils.displayStringArray(status, this.out);
		this.endOutput();
	}
	
	private void handleProcessCommand() {
		String cmd = this.command[1];
		if (cmd.equals("create"))
			this.handleProcessCreate();
		else if (cmd.equals("kill"))
			this.handleProcessKill();
		else if (cmd.equals("status"))
			this.handleProcessStatus();
		else
			this.reportError("invalid command: " + cmd);
	}
	
	private void handleProcessCreate() {
		int pid = 0;
		try {
			pid = this.system.processes.create(Utils.slice(this.command, 2, this.getCommandLength()));
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}
		
		this.startOutput();
		this.writeOutput(Integer.toString(pid));
		this.endOutput();
	}

	private void handleProcessKill() {
		this.reportError("command not implemented");
		
		int pid = Integer.parseInt(this.command[2]);
		this.system.processes.kill(pid);
		
		this.startOutput();
		this.endOutput();
	}

	private void handleProcessStatus() {
		int pid = Integer.parseInt(this.command[2]);
		String[] status = null;
		try {
			status = this.system.processes.status(pid);
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}
		
		this.startOutput();
		Utils.displayStringArray(status, this.out);
		this.endOutput();
	}

	private void handleQueryCommand() {
		String cmd = this.command[1];
		if (cmd.equals("uuid"))
			this.handleQueryUUID();
		else if (cmd.equals("network"))
			this.handleQueryNetwork();
		else
			this.reportError("invalid command: " + cmd);
	}
	
	private void handleQueryUUID() {
		String uuid = null;
		try {
			uuid = this.system.query.queryUUID();
		} catch (JIException e) {
			this.reportError("query uuid failed");
			return;
		}
		
		this.startOutput();
		this.writeOutput(uuid);
		this.endOutput();
	}

	private void handleQueryNetwork() {
		NetworkQueryResults[] results = null;
		try {
			results = system.query.queryNetwork();
		} catch (JIException e) {
			this.reportError(e.getErrorCode());
		}

		this.startOutput();
		this.system.query.displayNetworkQueryResults(results, this.out);
		this.endOutput();
	}
}