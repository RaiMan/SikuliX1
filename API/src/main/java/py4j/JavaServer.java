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

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.net.ServerSocketFactory;

import py4j.commands.Command;

/**
 * <p>
 * This class extends GatewayServer by implementing a new threading model:
 * a thread always use the same connection to the other side so callbacks are
 * executed in the calling thread.
 * </p>
 *
 * <p>
 * For example, if Java thread 1 calls Python, and Python calls Java, the
 * callback (from Python to Java) will be executed in Java thread 1.
 * </p>
 *
 */
public class JavaServer extends GatewayServer {

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 * @param connectTimeout
	 *            Time in milliseconds (0 = infinite). If a GatewayServer does
	 *            not receive a connection request after this time, it closes
	 *            the server socket and no other connection is accepted.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once a Python program is
	 *            connected, if a GatewayServer does not receive a request
	 *            (e.g., a method call) after this time, the connection with the
	 *            Python program is closed.
	 * @param customCommands
	 *            A list of custom Command classes to augment the Server
	 *            features. These commands will be accessible from Python
	 *            programs. Can be null.
	 * @param pythonClient
	 *            The Py4JPythonClientPerThread used to call Python.
	 */
	public JavaServer(Object entryPoint, int port, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClientPerThread pythonClient) {
		this(entryPoint, port, connectTimeout, readTimeout, customCommands, pythonClient, null);
	}

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 * @param connectTimeout
	 *            Time in milliseconds (0 = infinite). If a GatewayServer does
	 *            not receive a connection request after this time, it closes
	 *            the server socket and no other connection is accepted.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once a Python program is
	 *            connected, if a GatewayServer does not receive a request
	 *            (e.g., a method call) after this time, the connection with the
	 *            Python program is closed.
	 * @param customCommands
	 *            A list of custom Command classes to augment the Server
	 *            features. These commands will be accessible from Python
	 *            programs. Can be null.
	 * @param pythonClient
	 *            The Py4JPythonClientPerThread used to call Python.
	 * @param authToken
	 *            Token for authenticating with the callback server.
	 */
	public JavaServer(Object entryPoint, int port, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClientPerThread pythonClient, String authToken) {
		super(entryPoint, port, defaultAddress(), connectTimeout, readTimeout, customCommands, pythonClient,
				ServerSocketFactory.getDefault(), authToken);
	}

	@Override
	protected Py4JServerConnection createConnection(Gateway gateway, Socket socket) throws IOException {
		ClientServerConnection connection = new ClientServerConnection(gateway, socket, getCustomCommands(),
				(Py4JPythonClientPerThread) getCallbackClient(), this, getReadTimeout(), authToken);
		connection.startServerConnection();
		return connection;
	}
}
