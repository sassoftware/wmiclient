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
	private int pos = 0;
	
	private StreamTokenizer in;
	private PrintStream out;
	private PrintStream error;
	protected String[] command;

	public IPC(InputStream input, PrintStream output, PrintStream error) {
		this.out = output;
		this.error = error;

		this.reset();
		
		Reader r = new BufferedReader(new InputStreamReader(input));
		this.in = new StreamTokenizer(r);
		this.in.eolIsSignificant(true);
	}
	
	public void run() throws IOException {
		while (this.in.nextToken() != StreamTokenizer.TT_EOF) {
			switch(this.in.ttype) {
				case StreamTokenizer.TT_EOL: this.processCommand();
				case StreamTokenizer.TT_NUMBER: this.appendNumber();
				case StreamTokenizer.TT_WORD: this.appendWord();
			}
		}
	}

	private void appendNumber() {
		this.command[this.pos++] = Integer.toString((int)this.in.nval);
	}
	
	private void appendWord() {
		this.command[this.pos++] = new String(this.in.sval);
	}
	
	protected void reset() {
		this.pos = 0;
		this.command = new String[IPC.COMMAND_SIZE];
	}
	
	protected void reportError(String error) {
		this.out.print(IPC.MARKER);
		this.out.print("ERROR ");
		this.out.println(error);
	}
	
	protected void startOutput() {
		this.out.print(IPC.MARKER);
		this.out.println("START OUTPUT");
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
	
	protected abstract void processCommand();
}