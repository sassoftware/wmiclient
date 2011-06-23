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
