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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * <p>
 * This class creates the JavaServer and the PythonClient necessary to
 * communicate with a Python virtual machine with the new threading model.
 * </p>
 */
public class ClientServer {

	protected final int javaPort;

	protected final InetAddress javaAddress;

	protected final int pythonPort;

	protected final InetAddress pythonAddress;

	protected final int connectTimeout;

	protected final int readTimeout;

	protected final ServerSocketFactory sSocketFactory;

	protected final SocketFactory socketFactory;

	protected final Gateway gateway;

	protected final Py4JJavaServer javaServer;

	protected final Py4JPythonClientPerThread pythonClient;

	protected final boolean autoStartJavaServer;

	protected final boolean enableMemoryManagement;

	protected final String authToken;

	protected final Logger logger = Logger.getLogger(ClientServer.class.getName());

	/**
	 *
	 * @param entryPoint
	 */
	public ClientServer(Object entryPoint) {
		this(GatewayServer.DEFAULT_PORT, GatewayServer.defaultAddress(), GatewayServer.DEFAULT_PYTHON_PORT,
				GatewayServer.defaultAddress(), GatewayServer.DEFAULT_CONNECT_TIMEOUT,
				GatewayServer.DEFAULT_READ_TIMEOUT, ServerSocketFactory.getDefault(), SocketFactory.getDefault(),
				entryPoint);
	}

	/**
	 *
	 * @param javaPort
	 * @param javaAddress
	 * @param pythonPort
	 * @param pythonAddress
	 * @param connectTimeout
	 * @param readTimeout
	 * @param sSocketFactory
	 * @param socketFactory
	 * @param entryPoint
	 */
	public ClientServer(int javaPort, InetAddress javaAddress, int pythonPort, InetAddress pythonAddress,
			int connectTimeout, int readTimeout, ServerSocketFactory sSocketFactory, SocketFactory socketFactory,
			Object entryPoint) {
		this(javaPort, javaAddress, pythonPort, pythonAddress, connectTimeout, readTimeout, sSocketFactory,
				socketFactory, entryPoint, true, true);
	}

	/**
	 *
	 * @param javaPort
	 * @param javaAddress
	 * @param pythonPort
	 * @param pythonAddress
	 * @param connectTimeout
	 * @param readTimeout
	 * @param sSocketFactory
	 * @param socketFactory
	 * @param entryPoint
	 * @param autoStartJavaServer
	 * @param enableMemoryManagement
	 */
	public ClientServer(int javaPort, InetAddress javaAddress, int pythonPort, InetAddress pythonAddress,
			int connectTimeout, int readTimeout, ServerSocketFactory sSocketFactory, SocketFactory socketFactory,
			Object entryPoint, boolean autoStartJavaServer, boolean enableMemoryManagement) {
		this(javaPort, javaAddress, pythonPort, pythonAddress, connectTimeout, readTimeout, sSocketFactory,
				socketFactory, entryPoint, autoStartJavaServer, enableMemoryManagement, null);
	}

	private ClientServer(int javaPort, InetAddress javaAddress, int pythonPort, InetAddress pythonAddress,
			int connectTimeout, int readTimeout, ServerSocketFactory sSocketFactory, SocketFactory socketFactory,
			Object entryPoint, boolean autoStartJavaServer, boolean enableMemoryManagement, String authToken) {
		this.javaPort = javaPort;
		this.javaAddress = javaAddress;
		this.pythonPort = pythonPort;
		this.pythonAddress = pythonAddress;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.sSocketFactory = sSocketFactory;
		this.socketFactory = socketFactory;
		this.enableMemoryManagement = enableMemoryManagement;
		this.authToken = authToken;
		this.pythonClient = createPythonClient();
		this.javaServer = createJavaServer(entryPoint, pythonClient);

		this.gateway = javaServer.getGateway();
		pythonClient.setGateway(gateway);
		pythonClient.setJavaServer(javaServer);
		this.autoStartJavaServer = autoStartJavaServer;

		if (autoStartJavaServer) {
			this.javaServer.start();
		} else {
			this.gateway.startup();
		}
	}

	protected Py4JPythonClientPerThread createPythonClient() {
		return new PythonClient(null, null, pythonPort, pythonAddress, CallbackClient.DEFAULT_MIN_CONNECTION_TIME,
				TimeUnit.SECONDS, this.socketFactory, null, enableMemoryManagement, readTimeout, authToken);
	}

	protected Py4JJavaServer createJavaServer(Object entryPoint, Py4JPythonClientPerThread pythonClient) {
		return new JavaServer(entryPoint, javaPort, connectTimeout, readTimeout, null, pythonClient, authToken);
	}

	public Py4JJavaServer getJavaServer() {
		return javaServer;
	}

	public Py4JPythonClient getPythonClient() {
		return pythonClient;
	}

	/**
	 * <p>
	 * Starts the JavaServer on its own thread.
	 * </p>
	 *
	 * <p>
	 * Does nothing if autoStartJavaServer was set to true when constructing the instance.
	 * </p>
	 */
	public void startServer() {
		startServer(true);
	}

	/**
	 * <p>
	 * Starts the JavaServer, which will handle requests from the Python side.
	 * </p>
	 *
	 * <p>
	 * Does nothing if autoStartJavaServer was set to true when constructing the instance.
	 * </p>
	 *
	 * @param fork If the JavaServer is started in this thread or in its own
	 *                thread.
	 */
	public void startServer(boolean fork) {
		if (!autoStartJavaServer) {
			javaServer.start(fork);
		}
	}

	/**
	 * Shuts down the Java Server so that it stops accepting requests and it
	 * closes existing connections.
	 */
	public void shutdown() {
		this.javaServer.shutdown(true);
	}

	/**
	 * <p>
	 * Gets a reference to the entry point on the Python side. This is often
	 * necessary if Java is driving the communication because Java cannot call
	 * static methods, initialize Python objects or load Python modules yet.
	 * </p>
	 *
	 * @param interfacesToImplement
	 * @return
	 */
	public Object getPythonServerEntryPoint(Class[] interfacesToImplement) {
		return pythonClient.getPythonServerEntryPoint(gateway, interfacesToImplement);
	}

	/**
	 * Helper class to make it easier and self-documenting how a
	 * {@link ClientServer} is constructed.
	 */
	public static class ClientServerBuilder {
		private int javaPort;
		private InetAddress javaAddress;
		private int pythonPort;
		private InetAddress pythonAddress;
		private int connectTimeout;
		private int readTimeout;
		private ServerSocketFactory serverSocketFactory;
		private SocketFactory socketFactory;
		private Object entryPoint;
		private boolean autoStartJavaServer;
		private boolean enableMemoryManagement;
		private String authToken;

		public ClientServerBuilder() {
			this(null);
		}

		public ClientServerBuilder(Object entryPoint) {
			javaPort = GatewayServer.DEFAULT_PORT;
			javaAddress = GatewayServer.defaultAddress();
			pythonPort = GatewayServer.DEFAULT_PYTHON_PORT;
			pythonAddress = GatewayServer.defaultAddress();
			connectTimeout = GatewayServer.DEFAULT_CONNECT_TIMEOUT;
			readTimeout = GatewayServer.DEFAULT_READ_TIMEOUT;
			serverSocketFactory = ServerSocketFactory.getDefault();
			socketFactory = SocketFactory.getDefault();
			this.entryPoint = entryPoint;
			autoStartJavaServer = true;
			enableMemoryManagement = true;
		}

		public ClientServer build() {
			return new ClientServer(javaPort, javaAddress, pythonPort, pythonAddress, connectTimeout, readTimeout,
					serverSocketFactory, socketFactory, entryPoint, autoStartJavaServer, enableMemoryManagement,
					authToken);
		}

		public ClientServerBuilder javaPort(int javaPort) {
			this.javaPort = javaPort;
			return this;
		}

		public ClientServerBuilder javaAddress(InetAddress javaAddress) {
			this.javaAddress = javaAddress;
			return this;
		}

		public ClientServerBuilder pythonPort(int pythonPort) {
			this.pythonPort = pythonPort;
			return this;
		}

		public ClientServerBuilder pythonAddress(InetAddress pythonAddress) {
			this.pythonAddress = pythonAddress;
			return this;
		}

		public ClientServerBuilder connectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		public ClientServerBuilder readTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public ClientServerBuilder serverSocketFactory(ServerSocketFactory serverSocketFactory) {
			this.serverSocketFactory = serverSocketFactory;
			return this;
		}

		public ClientServerBuilder socketFactory(SocketFactory socketFactory) {
			this.socketFactory = socketFactory;
			return this;
		}

		public ClientServerBuilder entryPoint(Object entryPoint) {
			this.entryPoint = entryPoint;
			return this;
		}

		public ClientServerBuilder autoStartJavaServer(boolean autoStartJavaServer) {
			this.autoStartJavaServer = autoStartJavaServer;
			return this;
		}

		public ClientServerBuilder enableMemoryManagement(boolean enableMemoryManagement) {
			this.enableMemoryManagement = enableMemoryManagement;
			return this;
		}

		public ClientServerBuilder authToken(String authToken) {
			this.authToken = StringUtil.escape(authToken);
			return this;
		}
	}
}
