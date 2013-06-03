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
