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
