/**
 * Copyright (c) 2010 rPath, Inc.
 */
package com.rpath.management.windows;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jinterop.dcom.common.JISystem;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for managing the JI log
 */
public class JILogging {

	private Logger jilogger = null;
	
	public JILogging(Level defaultLevel) throws SecurityException, IOException {
		this.jilogger = JISystem.getLogger();
		
		JISystem.setInBuiltLogHandler(true);
		JISystem.setAutoRegisteration(true);
		this.setLevel(defaultLevel);
	}
	
	public void setLevel(Level level) {
		this.jilogger.setLevel(level);
	}
}
