package com.rpath.management.windows.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * Class to implement IPC interface.
 */
public abstract class IPC {
	private static final String MARKER = "= ";
	private static final int COMMAND_SIZE = 10;
	private static final byte QUOTE = (byte)'"';
	private static final byte SPACE = (byte)' ';
	private static final byte NEWLINE = (byte)'\n';
	
	private int pos;
	private boolean inQuotedString;
	
	private StreamTokenizer in;
	protected PrintStream out;
	protected PrintStream error;
	protected String[] command;

	public IPC(InputStream input, PrintStream output, PrintStream error) {
		this.out = output;
		this.error = error;

		this.reset();
		
		Reader r = new BufferedReader(new InputStreamReader(input));
		this.in = new StreamTokenizer(r);
		this.in.resetSyntax();
		this.in.eolIsSignificant(true);
	}
	
	public void run() throws IOException, Exception {
		while (this.in.nextToken() != StreamTokenizer.TT_EOF) {
			//System.err.println("state: " + (char)this.in.ttype + "(" + this.in.ttype + ")");
			switch(this.in.ttype) {
				case IPC.SPACE:
					this.handleSpace();
					break;
				case IPC.QUOTE:
					this.handleQuote();
					break;
				case IPC.NEWLINE:
					//this.printCommand();
					this.processCommand();
					this.reset();
					break;
				default:
					this.append();
			}
		}
		
	}

	private void append() {
		byte b = (byte)this.in.ttype;
		byte [] ba = {b, };
		String s = new String(ba);

		//System.err.println("pos: " + this.pos);
		//System.err.println("s: " + s);
		//System.err.println("foo: " + this.command[this.pos]);
		
		String foo = this.command[this.pos];
		String bar = foo.concat(s);
		this.command[this.pos] = bar;
		//this.command[this.pos] = this.command[this.pos].concat(s); 
	}
	
	private void handleSpace() {
		if (!this.inQuotedString) {
			this.pos++;
			this.command[this.pos] = new String(); 
		} else {
			this.append();
		}
	}
	
	private void handleQuote() {
		if (this.inQuotedString) {
			this.inQuotedString = false;
		} else {
			this.inQuotedString = true;
		}
	}
		
	protected void reset() {
		this.pos = 0;
		this.inQuotedString = false;
		this.command = new String[IPC.COMMAND_SIZE];
		this.command[this.pos] = new String();
	}
	
	public void reportError(String error) {
		this.out.print(IPC.MARKER);
		this.out.print("ERROR ");
		this.out.println(error);
	}
	
	public void reportError(int errorCode) {
		this.reportError(Integer.toString(errorCode));
	}
	
	public void reportException(Exception e) {
		this.out.print(IPC.MARKER);
		this.out.println("START STACKTRACE");
		e.printStackTrace(this.out);
		this.out.print(IPC.MARKER);
		this.out.println("END STACKTRACE");
	}
	
	protected void startOutput() {
		this.startOutput(0);
	}
	
	protected void startOutput(int rc) {
		this.out.print(IPC.MARKER);
		this.out.println("START OUTPUT " + rc);
	}
	
	protected void writeOutput(String output) {
		this.out.println(output);
	}
	
	protected void writeOutput(String output[]) {
		for (int i=0; i<output.length; i++) {
			this.out.println(output[i]);
		}
	}
	
	protected void endOutput() {
		this.out.print(IPC.MARKER);
		this.out.println("END OUTPUT");
	}
	
	protected void printCommand() {
		this.error.println("=================");
		for (int i=0; i<=this.pos; i++) {
			this.error.println(i + ": " + this.command[i]);
		}
		this.error.println("=================");
	}
	
	protected int getCommandLength() {
		return this.pos;
	}
	
	protected abstract void processCommand();
}