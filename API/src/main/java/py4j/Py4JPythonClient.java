/******************************************************************************
 * Copyright (c) 2009-2018, Barthelemy Dagenais and individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/
package py4j;

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
