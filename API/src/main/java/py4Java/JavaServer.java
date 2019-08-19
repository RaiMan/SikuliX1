/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.net.ServerSocketFactory;

import py4Java.commands.Command;

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
