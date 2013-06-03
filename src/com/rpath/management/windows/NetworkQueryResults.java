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

public class NetworkQueryResults {
	private int index;
	private String hostname;
	private String domain;
	
	private int pos;
	private String[][] addresses;
	
	public NetworkQueryResults(int index, String hostname, String domain, int addressCount) {
		this.index = index;
		this.hostname = hostname;
		this.domain = domain;
		
		this.pos = 0;
		this.addresses = new String[addressCount][];
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	public void addAddress(String ipaddr, String netmask) {
		this.addresses[this.pos] = new String[]{ipaddr, netmask};
		this.pos++;
	}
	
	public String[][] getAddresses() {
		return this.addresses;
	}
}
