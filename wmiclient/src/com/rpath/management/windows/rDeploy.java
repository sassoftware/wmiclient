package com.rpath.management.windows;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;

import java.net.UnknownHostException;
import java.util.logging.Level;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.dcom.common.JIDefaultAuthInfoImpl;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.JIUnsignedInteger;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;

public class rDeploy {

	public static enum ServiceState {
		START, STOP
	}
	
	public static enum InstallState {
		INSTALL, UNINSTALL
	}

	private JIComServer comStub = null;
	private IJIComObject comObject = null;
	private IJIDispatch dispatch = null;
	private String address = null;
	private JISession session = null;
	private IJIAuthInfo authInfo = null;

	/**
	 * Static initializer to setup some things we only want to run once
	 */
	static {
		try {
			JISystem.getLogger().setLevel(Level.INFO);
			JISystem.setInBuiltLogHandler(false);
			JISystem.setAutoRegisteration(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for creation of the session and basic setup to get the WBEM
	 * Locator.
	 * 
	 * @param address IP Address or Public DNS Name
	 * @param domain Windows Domain (Machine name if not setup for domains
	 * @param username User authorized to make CIM/WBEM calls
	 * @param password User's password
	 * @throws JIException JInterop exception
	 * @throws UnknownHostException System error if address can't be found
	 */
	public rDeploy(String address, String domain, String username,
			String password) throws JIException, UnknownHostException {

		this.address = address;
		session = JISession.createSession(domain, username, password);
		session.useSessionSecurity(true);
		session.setGlobalSocketTimeout(5000);
		
		// When manipulating the registry use authInfo instead of session
		authInfo = new JIDefaultAuthInfoImpl(domain, username, password);

		// ISWbemLocator
		comStub = new JIComServer(JIProgId
				.valueOf("WbemScripting.SWbemLocator"), address, session);
		IJIComObject unknown = comStub.createInstance();
		// HKLM/SOFTWARE/Classes/Interface
		comObject = (IJIComObject) unknown
				.queryInterface("76A6415B-CB41-11d1-8B02-00600806D9B6");

		// This will obtain the dispatch interface
		dispatch = (IJIDispatch) JIObjectFactory.narrowObject(comObject
				.queryInterface(IJIDispatch.IID));
	}

	public void manageRegistryKey(String keyPath, String valueName, String[] values) {
		try {
			IJIWinReg registry = JIWinRegFactory.getSingleTon().getWinreg(this.authInfo,this.address,true);
			//Open HKLM
			JIPolicyHandle policyHandle = registry.winreg_OpenHKLM();
			//Open a key here
			JIPolicyHandle policyHandle2 = registry.winreg_OpenKey(policyHandle,keyPath,IJIWinReg.KEY_ALL_ACCESS);
			
			byte[][] data = new byte[values.length][];
			for (int i = 0; i < values.length;i++)
			{
				data[i] = values[i].getBytes();
			}
			registry.winreg_SetValue(policyHandle2,valueName,data);
			
			registry.winreg_CloseKey(policyHandle2);
			registry.winreg_CloseKey(policyHandle);
			registry.closeConnection();
			
		} catch (JIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Start or stop a service by its Display Name
	 * 
	 * @param serviceName Display name of the service to manage
	 * @param action START or STOP
	 */
	public void manageService(String serviceName, ServiceState action) {

		try {

			IJIDispatch wbemServices = getDispatch();

			final int RETURN_IMMEDIATE = 0x10;
			final int FORWARD_ONLY = 0x20;
			Object[] params = new Object[] {
					new JIString(
							"SELECT * FROM Win32_Service WHERE Caption = '"
									+ serviceName + "'"),
					JIVariant.OPTIONAL_PARAM(),
					new JIVariant(new Integer(RETURN_IMMEDIATE + FORWARD_ONLY)) };
			
			JIVariant[] servicesSet = wbemServices.callMethodA("ExecQuery",
					params);
			IJIDispatch wbemObjectSet = (IJIDispatch) narrowObject(servicesSet[0]
					.getObjectAsComObject());

			JIVariant newEnumvariant = wbemObjectSet.get("_NewEnum");
			IJIComObject enumComObject = newEnumvariant.getObjectAsComObject();
			IJIEnumVariant enumVariant = (IJIEnumVariant) narrowObject(enumComObject
					.queryInterface(IJIEnumVariant.IID));

			Object[] elements = enumVariant.next(1);
			JIArray aJIArray = (JIArray) elements[0];

			JIVariant[] array = (JIVariant[]) aJIArray.getArrayInstance();
			for (JIVariant variant : array) {
				IJIDispatch wbemObjectDispatch = (IJIDispatch) narrowObject(variant
						.getObjectAsComObject());

				// Start or Stop the service
				String methodToInvoke = (action == ServiceState.START) ? "StartService"
						: "StopService";
				wbemObjectDispatch.callMethodA(methodToInvoke);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void processMSICreation(String msiLocation, String logLocation, InstallState state)
			throws JIException, InterruptedException {

		IJIDispatch wbemServices_dispatch = getDispatch();

		// Get the Win32_Process handle
		JIVariant[] results = wbemServices_dispatch.callMethodA("Get", new Object[] {
				new JIString("Win32_Process"), new Integer(0),
				JIVariant.OPTIONAL_PARAM() });

		// Get the Dispatcher
		IJIDispatch wbemObjectSet_dispatch = (IJIDispatch) JIObjectFactory
				.narrowObject((results[0]).getObjectAsComObject());
		
		// Start or Stop the service
		String argument = (state == InstallState.INSTALL) ? " /i "
				: " /uninstall ";
		
		// Construct the command
		StringBuffer cmd = new StringBuffer("cmd /c msiexec ").append(argument)
				.append(msiLocation).append(" /quiet /l*wvx ").append(
						logLocation);
		
		// Create the process
		// The process Id will be in the second element of the results array
		JIVariant processId = JIVariant.EMPTY_BYREF();
		results = wbemObjectSet_dispatch.callMethodA("Create", new Object[] {
				new JIString(cmd.toString()), JIVariant.OPTIONAL_PARAM(),
				JIVariant.OPTIONAL_PARAM(), processId});
		
		System.out.println("Process return code: " + results[0].getObjectAsInt());
			
		int pid = results[1].getObjectAsVariant().getObjectAsInt();
		System.out.println("Process Id: " + pid);

	}
	
	/**
	 * This method needs attention.  I have not gotten it to return any values.
	 * Most likely there is a race condition and the process is complete before
	 * the call is made.
	 * @param pid
	 * @return
	 */
	private int lookupProcessId(int pid) {
		try {
			IJIDispatch wbemServices = getDispatch();
	
			final int RETURN_IMMEDIATE = 0x10;
			final int FORWARD_ONLY = 0x20;
			Object[] params = new Object[] {
				new JIString(
						"Select * From Win32_ProcessStopTrace where ProcessID=" + pid),
				JIVariant.OPTIONAL_PARAM(),
				new JIVariant(new Integer(RETURN_IMMEDIATE + FORWARD_ONLY)) };
		
			JIVariant[] servicesSet = wbemServices.callMethodA("ExecQuery",
					params);
			IJIDispatch wbemObjectSet = (IJIDispatch) narrowObject(servicesSet[0]
					.getObjectAsComObject());
	
			JIVariant newEnumvariant = wbemObjectSet.get("_NewEnum");
			IJIComObject enumComObject = newEnumvariant.getObjectAsComObject();
			IJIEnumVariant enumVariant = (IJIEnumVariant) narrowObject(enumComObject
					.queryInterface(IJIEnumVariant.IID));
	
			Object[] elements = enumVariant.next(1);
			JIArray aJIArray = (JIArray) elements[0];
	
			JIVariant[] array = (JIVariant[]) aJIArray.getArrayInstance();
			for (JIVariant variant : array) {
				IJIDispatch wbemObjectDispatch = (IJIDispatch) narrowObject(variant
						.getObjectAsComObject());
	
				// Print object as text.
				JIVariant[] v = wbemObjectDispatch.callMethodA(
						"GetObjectText_", new Object[] { 1 });
				System.out.println(v[0].getObjectAsString().getString());
			}
		//System.out.println(variant.getObjectAsInt());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Helper method to get the connection dispatcher for the Windows
	 * Managed System
	 * @return
	 * @throws JIException
	 */
	private IJIDispatch getDispatch() throws JIException {
		System.gc();
		JIVariant results[] = dispatch.callMethodA("ConnectServer",
			new Object[] { new JIString(address),
						JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),
						JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),
						JIVariant.OPTIONAL_PARAM(), new Integer(0),
						JIVariant.OPTIONAL_PARAM() });

		return (IJIDispatch) JIObjectFactory
				.narrowObject((results[0]).getObjectAsComObject());
	}
	
	/**
	 * Clean up the session
	 * @throws JIException
	 */
	private void killme() throws JIException {
		JISession.destroySession(session);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length < 4) {
				System.out
						.println("Please provide address domain username password");
				return;
			}

			rDeploy test = new rDeploy(args[0], args[1], args[2], args[3]);
			test.processMSICreation("C:\\temp\\Setup.msi", "C:\\temp\\setup.log", InstallState.INSTALL);
			//test.manageService("rPath Install Manager",
			//		rDeploy.ServiceState.START);
			//String[] cmds = {"Delay=5", "uri=172.16.175.244", "IgnoreOtherCommands"};
			//test.manageRegistryKey("SYSTEM\\CurrentControlSet\\services\\rPath Install Manager\\Parameters", "RunOnceCommands", cmds);
			test.killme();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
