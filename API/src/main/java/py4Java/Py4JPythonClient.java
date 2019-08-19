/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.net.InetAddress;

/**
 * <p>
 * Interface that describes the operations a client must support to make
 * requests to the Python side.
 * </p>
 */
public interface Py4JPythonClient {

	/**
	 * <p>
	 * Sends a command to the Python side. This method is typically used by
	 * Python proxies to call Python methods or to request the garbage
	 * collection of a proxy.
	 * </p>
	 *
	 * @param command
	 *            The command to send.
	 * @return The response.
	 */
	String sendCommand(String command);

	/**
	 * <p>
	 * Sends a command to the Python side. This method is typically used by
	 * Python proxies to call Python methods or to request the garbage
	 * collection of a proxy.
	 * </p>
	 *
	 * @param command
	 *            The command to send.
	 * @param blocking
	 * 			  If the CallbackClient should wait for an answer (default
	 * 			  should be True, except for critical cases such as a
	 * 			  finalizer sending a command).
	 * @return The response.
	 */
	String sendCommand(String command, boolean blocking);

	/**
	 * <p>
	 * Closes all active channels, stops the periodic cleanup of channels and
	 * mark the client as shutting down.
	 *
	 * No more commands can be sent after this method has been called,
	 * <em>except</em> commands that were initiated before the shutdown method
	 * was called..
	 * </p>
	 */
	void shutdown();

	/**
	 * <p>
	 * Creates a callback client which connects to the given address and port,
	 * but retains all the other settings (like the minConnectionTime
	 * and the socketFactory). This method is useful if for some reason
	 * your CallbackServer changes its address or you come to know of the
	 * address after Gateway has already instantiated.
	 * </p>
	 *
	 * @param pythonAddress
	 *            The address used by a PythonProxyHandler to connect to a
	 *            Python gateway.
	 * @param pythonPort
	 *            The port used by a PythonProxyHandler to connect to a Python
	 *            gateway. Essentially the port used for Python callbacks.
	 */
	Py4JPythonClient copyWith(InetAddress pythonAddress, int pythonPort);

	boolean isMemoryManagementEnabled();

	int getPort();

	int getReadTimeout();

	InetAddress getAddress();

	/**
	 * <p>
	 * Gets a reference to the entry point on the Python side. This is often
	 * necessary if Java is driving the communication because Java cannot call
	 * static methods, initialize Python objects or load Python modules yet.
	 * </p>
	 *
	 * @param gateway
	 * @param interfacesToImplement
	 * @return
	 */
	public Object getPythonServerEntryPoint(Gateway gateway, Class[] interfacesToImplement);
}
