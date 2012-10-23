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

import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

/**
 * @author Elliot Peele <elliot@rpath.com>
 *
 * Class for querying WMI interface
 */
public class Query {
	private Session session = null;
	
	private final int RETURN_IMMEDIATE = 0x10;
	private final int FORWARD_ONLY = 0x20;

	/**
	 * Constructor
	 * @param session WMI Session instance
	 */
	public Query(Session session) {
		this.session = session;
	}
	
	public ArrayList<JIVariant> query(String queryString) throws JIException {
		// Get a dispatcher for communicating with the services interface
		IJIDispatch dispatch = this.session.getDispatch();
		
		// Query options
		Object[] params = new Object[] {
			new JIString(queryString),
			JIVariant.OPTIONAL_PARAM(),
			new JIVariant(new Integer(this.RETURN_IMMEDIATE + this.FORWARD_ONLY)),
		};
		
		JIVariant[] resultSet = dispatch.callMethodA("ExecQuery", params);

		IJIDispatch wbemObjectSet = (IJIDispatch)narrowObject(resultSet[0].getObjectAsComObject());

		JIVariant newEnumVariant = wbemObjectSet.get("_NewEnum");
		IJIComObject enumComObject = newEnumVariant.getObjectAsComObject();
		IJIEnumVariant enumVariant = (IJIEnumVariant)narrowObject(enumComObject.queryInterface(IJIEnumVariant.IID));
		
		ArrayList<JIVariant> ret = new ArrayList<JIVariant>();
		try {
			while(true) {
				Object[] elements = enumVariant.next(1);
				JIArray jiArray = (JIArray)elements[0];
				JIVariant[] array = (JIVariant[])jiArray.getArrayInstance();
				ret.addAll(Arrays.asList(array));
			}
		} catch (JIException e) {
			// ignore this exception, iterator is done.
		}
		return ret;		
	}
	
	public NetworkQueryResults[] queryNetwork() throws JIException {
		ArrayList<JIVariant> queryResults = this.query("SELECT * FROM Win32_NetworkAdapterConfiguration");
		
		NetworkQueryResults[] results = new NetworkQueryResults[queryResults.size()];
		for (int i=0; i<queryResults.size(); i++) {
			IJIComObject obj = queryResults.get(i).getObjectAsComObject();
			IJIDispatch dispatch = (IJIDispatch)narrowObject(obj);

			Boolean IPEnabled = dispatch.get("IPEnabled").getObjectAsBoolean();
			if (IPEnabled==false) {
				continue;
			}

			int index = dispatch.get("InterfaceIndex").getObjectAsInt();
			String hostName = " ";
			try {
				hostName = dispatch.get("DNSHostName").getObjectAsString2();
			} catch (Exception e) {}

			String domain = " ";
			try {
				domain = dispatch.get("DNSDomain").getObjectAsString2();
			} catch (Exception e) {}
			
			JIArray jiAddr = dispatch.get("IPAddress").getObjectAsArray();
			JIVariant[] addr = (JIVariant[])jiAddr.getArrayInstance();

			JIArray jiSubnet = dispatch.get("IPSubnet").getObjectAsArray();
			JIVariant[] subnet = (JIVariant[])jiSubnet.getArrayInstance();

			results[i] = new NetworkQueryResults(index, hostName, domain, addr.length);		
			for (int j=0; j<addr.length; j++) {
				results[i].addAddress(addr[j].getObjectAsString2(), subnet[j].getObjectAsString2());
			}
		}
		return results;
	}
	
	public void displayNetworkQueryResults(NetworkQueryResults[] results, PrintStream out) {
		for (int i=0; i<results.length; i++) {
			NetworkQueryResults result = results[i];
			if (result == null)
				continue;
			String[][] addresses = result.getAddresses();
			for (int j=0; j<addresses.length; j++) {
				out.println(
						result.getIndex() + ", "
						+ addresses[j][0] + ", " 
						+ addresses[j][1] + ", " 
						+ result.getHostname() + ", " 
						+ result.getDomain()
						);
			}
		}
	}
	
	public String queryUUID() throws JIException {
		ArrayList<JIVariant> queryResults = this.query("SELECT * FROM Win32_ComputerSystemProduct");
		
		IJIComObject obj = queryResults.get(0).getObjectAsComObject();
		IJIDispatch dispatch = (IJIDispatch)narrowObject(obj);
			
		String uuid = dispatch.get("UUID").getObjectAsString2();
		return uuid;
	}
	
}
