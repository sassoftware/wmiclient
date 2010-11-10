/**
 * Copyright (c) 2010 rPath, Inc.
 */
package com.rpath.management.windows;

import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

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
	 * @param session WMI Sesison instance
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
}
