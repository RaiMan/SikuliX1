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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

import py4j.commands.Command;

/**
 * <p>
 * This class enables Python programs to access a Java program. When a
 * GatewayServer instance is started, Python programs can connect to the JVM by
 * calling:
 * </p>
 *
 * <p>
 * <code>gateway = JavaGateway()</code>
 * </p>
 *
 * <p>
 * The
 * <code>entryPoint</code> passed to a GatewayServer can be accessed with the <code>entry_point</code>
 * member:
 * </p>
 *
 * <p>
 * <code>gateway.entry_point</code>
 * </p>
 *
 * <p>
 * Technically, a GatewayServer is only responsible for accepting connection.
 * Each connection is then handled by a {@link py4j.GatewayConnection
 * GatewayConnection} instance and the various states (e.g., entryPoint,
 * reference to returned objects) are managed by a {@link py4j.Gateway Gateway}
 * instance.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class GatewayServer extends DefaultGatewayServerListener implements Py4JJavaServer, Runnable {

	public static final String DEFAULT_ADDRESS = "127.0.0.1";

	public static final String DEFAULT_IPv6_ADDRESS = "::1";

	public static final int DEFAULT_PORT = 25333;

	public static final int DEFAULT_PYTHON_PORT = 25334;

	public static final int DEFAULT_CONNECT_TIMEOUT = 0;

	public static final int DEFAULT_READ_TIMEOUT = 0;

	public static final String GATEWAY_SERVER_ID = Protocol.GATEWAY_SERVER_ID;

	public static final Logger PY4J_LOGGER = Logger.getLogger("py4j");

	/**
	 * <p>
	 * Utility method to turn logging on. Logging is turned off by default. All
	 * log messages will be logged.
	 * </p>
	 */
	public static void turnAllLoggingOn() {
		PY4J_LOGGER.setLevel(Level.ALL);
	}

	/**
	 * <p>
	 * Utility method to turn logging off. Logging is turned off by default.
	 * </p>
	 */
	public static void turnLoggingOff() {
		PY4J_LOGGER.setLevel(Level.OFF);
	}

	/**
	 * <p>
	 * Utility method to turn logging on. Logging is turned off by default. Log
	 * messages up to INFO level will be logged.
	 * </p>
	 */
	public static void turnLoggingOn() {
		PY4J_LOGGER.setLevel(Level.INFO);
	}

	private final InetAddress address;

	private final int port;

	private int pythonPort;

	private InetAddress pythonAddress;

	private final Gateway gateway;

	private final int connectTimeout;

	private final int readTimeout;

	private final Logger logger = Logger.getLogger(GatewayServer.class.getName());

	private final List<Py4JServerConnection> connections = new ArrayList<Py4JServerConnection>();

	private final List<Class<? extends Command>> customCommands;

	private final CopyOnWriteArrayList<GatewayServerListener> listeners;

	private final ServerSocketFactory sSocketFactory;

	protected final String authToken;

	private ServerSocket sSocket;

	private boolean isShutdown = false;

	private boolean isShuttingDown = false;

	private final Lock lock = new ReentrantLock(true);

	static {
		GatewayServer.turnLoggingOff();
	}

	public static InetAddress defaultAddress() {
		try {
			return InetAddress.getByName(DEFAULT_ADDRESS);
		} catch (UnknownHostException e) {
			throw new Py4JNetworkException(e);
		}
	}

	public static InetAddress defaultIPv6Address() {
		try {
			return InetAddress.getByName(DEFAULT_IPv6_ADDRESS);
		} catch (UnknownHostException e) {
			throw new Py4JNetworkException(e);
		}
	}

	/**
	 * <p>
	 * Creates a GatewayServer instance with default port (25333), default
	 * address (127.0.0.1), and default timeout value (no timeout).
	 * </p>
	 */
	public GatewayServer() {
		this(null, DEFAULT_PORT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}

	/**
	 * <p>
	 * Creates a GatewayServer instance with default port (25333), default
	 * address (127.0.0.1), and default timeout value (no timeout).
	 * </p>
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 */
	public GatewayServer(Object entryPoint) {
		this(entryPoint, DEFAULT_PORT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 */
	public GatewayServer(Object entryPoint, int port) {
		this(entryPoint, port, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
	}

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 * @param pythonPort
	 *            The port used by a PythonProxyHandler to connect to a Python
	 *            gateway. Essentially the port used for Python callbacks.
	 * @param address
	 *            The address the GatewayServer is listening to.
	 * @param pythonAddress
	 *            The address used by a PythonProxyHandler to connect to a
	 *            Python gateway.
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
	 */
	public GatewayServer(Object entryPoint, int port, int pythonPort, InetAddress address, InetAddress pythonAddress,
			int connectTimeout, int readTimeout, List<Class<? extends Command>> customCommands) {
		this(entryPoint, port, address, connectTimeout, readTimeout, customCommands,
				new CallbackClient(pythonPort, pythonAddress), ServerSocketFactory.getDefault());
	}

	/**
	 * <p>
	 * Replace the callback client with the new one which connects to the given address
	 * and port. This method is useful if for some reason your CallbackServer changes its
	 * address or you come to know of the address after Gateway has already instantiated.
	 * </p>
	 *
	 * <p>
	 * This method <strong>is not thread-safe</strong>! Make sure that only
	 * one thread calls this method.
	 * </p>
	 *
	 * @param pythonAddress
	 *            The address used by a PythonProxyHandler to connect to a
	 *            Python gateway.
	 * @param pythonPort
	 *            The port used by a PythonProxyHandler to connect to a Python
	 *            gateway. Essentially the port used for Python callbacks.
	 */
	public void resetCallbackClient(InetAddress pythonAddress, int pythonPort) {
		gateway.resetCallbackClient(pythonAddress, pythonPort);
		this.pythonPort = pythonPort;
		this.pythonAddress = pythonAddress;
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
	 */
	public GatewayServer(Object entryPoint, int port, int connectTimeout, int readTimeout) {
		this(entryPoint, port, DEFAULT_PYTHON_PORT, connectTimeout, readTimeout, null);
	}

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 * @param pythonPort
	 *            The port used by a PythonProxyHandler to connect to a Python
	 *            gateway. Essentially the port used for Python callbacks.
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
	 */
	public GatewayServer(Object entryPoint, int port, int pythonPort, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands) {
		this(entryPoint, port, defaultAddress(), connectTimeout, readTimeout, customCommands,
				new CallbackClient(pythonPort, defaultAddress()), ServerSocketFactory.getDefault());
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
	 * @param cbClient
	 * 			  An instance of a callback client.
	 */
	public GatewayServer(Object entryPoint, int port, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClient cbClient) {
		this(entryPoint, port, defaultAddress(), connectTimeout, readTimeout, customCommands, cbClient,
				ServerSocketFactory.getDefault());
	}

	/**
	 *
	 * @param entryPoint
	 *            The entry point of this Gateway. Can be null.
	 * @param port
	 *            The port the GatewayServer is listening to.
	 * @param address
	 *            The address the GatewayServer is listening to.
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
	 * @param cbClient
	 * 			  An instance of a callback client.
	 */
	public GatewayServer(Object entryPoint, int port, InetAddress address, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClient cbClient) {
		this(entryPoint, port, address, connectTimeout, readTimeout, customCommands, cbClient,
				ServerSocketFactory.getDefault());
	}

	/**
	 *
	 * @param entryPoint
	 *        The entry point of this Gateway. Can be null.
	 * @param port
	 *        The port the GatewayServer is listening to.
	 * @param address
	 *        The address the GatewayServer is listening to.
	 * @param connectTimeout
	 *        Time in milliseconds (0 = infinite). If a GatewayServer does
	 *        not receive a connection request after this time, it closes
	 *        the server socket and no other connection is accepted.
	 * @param readTimeout
	 *        Time in milliseconds (0 = infinite). Once a Python program is
	 *        connected, if a GatewayServer does not receive a request
	 *        (e.g., a method call) after this time, the connection with the
	 *        Python program is closed.
	 * @param customCommands
	 *        A list of custom Command classes to augment the Server
	 *        features. These commands will be accessible from Python
	 *        programs. Can be null.
	 * @param sSocketFactory
	 *        A factory that creates the server sockets that we listen on.
	 */
	public GatewayServer(Object entryPoint, int port, InetAddress address, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClient cbClient,
			ServerSocketFactory sSocketFactory) {
		this(entryPoint, port, address, connectTimeout, readTimeout, customCommands, cbClient, sSocketFactory, null);
	}

	GatewayServer(Object entryPoint, int port, InetAddress address, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, Py4JPythonClient cbClient,
			ServerSocketFactory sSocketFactory, String authToken) {
		super();
		this.port = port;
		this.address = address;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.gateway = new Gateway(entryPoint, cbClient);
		this.pythonPort = cbClient.getPort();
		this.pythonAddress = cbClient.getAddress();
		this.gateway.putObject(GATEWAY_SERVER_ID, this);
		if (customCommands != null) {
			this.customCommands = customCommands;
		} else {
			this.customCommands = new ArrayList<Class<? extends Command>>();
		}
		this.listeners = new CopyOnWriteArrayList<GatewayServerListener>();
		this.sSocketFactory = sSocketFactory;
		this.authToken = authToken;
	}

	/**
	 * @param gateway gateway instance (or subclass).  Must not be <code>null</code>.
	 * @param port the host port to usu
	 * @param address the host address to use
	 * @param connectTimeout the connect timeout (ms)
	 * @param readTimeout the read timeout (ms)
	 * @param customCommands any customCommands to use.  May be <code>null</code>
	 * @param sSocketFactory socketFactory to use.  Must not be <code>null</code>
	 */
	public GatewayServer(Gateway gateway, int port, InetAddress address, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, ServerSocketFactory sSocketFactory) {
		this(gateway, port, address, connectTimeout, readTimeout, customCommands, sSocketFactory, null);
	}

	private GatewayServer(Gateway gateway, int port, InetAddress address, int connectTimeout, int readTimeout,
			List<Class<? extends Command>> customCommands, ServerSocketFactory sSocketFactory, String authToken) {
		super();
		this.port = port;
		this.address = address;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.gateway = gateway;
		this.pythonPort = gateway.getCallbackClient().getPort();
		this.pythonAddress = gateway.getCallbackClient().getAddress();
		this.gateway.putObject(GATEWAY_SERVER_ID, this);
		if (customCommands != null) {
			this.customCommands = customCommands;
		} else {
			this.customCommands = new ArrayList<Class<? extends Command>>();
		}
		this.listeners = new CopyOnWriteArrayList<GatewayServerListener>();
		this.sSocketFactory = sSocketFactory;
		this.authToken = authToken;
	}

	public void addListener(GatewayServerListener listener) {
		listeners.addIfAbsent(listener);
	}

	public void connectionStopped(Py4JServerConnection gatewayConnection) {
		try {
			lock.lock();
			if (!isShutdown) {
				connections.remove(gatewayConnection);
			}
		} finally {
			lock.unlock();
		}

	}

	/**
	 * <p>
	 * Creates a server connection from a Python call to the Java side.
	 * </p>
	 *
	 * @param gateway
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	protected Py4JServerConnection createConnection(Gateway gateway, Socket socket) throws IOException {
		GatewayConnection connection = new GatewayConnection(gateway, socket, authToken, customCommands, listeners);
		connection.startConnection();
		return connection;
	}

	protected void fireConnectionError(Exception e) {
		logger.log(Level.SEVERE, "Connection Server Error", e);
		for (GatewayServerListener listener : listeners) {
			try {
				listener.connectionError(e);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "A listener crashed.", ex);
			}
		}
	}

	protected void fireConnectionStarted(Py4JServerConnection gatewayConnection) {
		logger.info("Connection Started");
		for (GatewayServerListener listener : listeners) {
			try {
				listener.connectionStarted(gatewayConnection);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	protected void fireServerError(Exception e) {
		boolean sendEvent = false;
		if (e.getMessage().toLowerCase().contains("socket closed")) {
			// This is just an internal error that will always be thrown when
			// closing a server socket that is accepting a connection
			logger.log(Level.FINE, "Gateway Server Error", e);
		} else {
			sendEvent = true;
			logger.log(Level.SEVERE, "Gateway Server Error", e);
		}
		if (sendEvent) {
			for (GatewayServerListener listener : listeners) {
				try {
					listener.serverError(e);
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "A listener crashed.", ex);
				}
			}
		}
	}

	protected void fireServerPostShutdown() {
		logger.fine("Gateway Server Post Shutdown");
		for (GatewayServerListener listener : listeners) {
			try {
				listener.serverPostShutdown();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	protected void fireServerPreShutdown() {
		logger.fine("Gateway Server Pre Shutdown");
		for (GatewayServerListener listener : listeners) {
			try {
				listener.serverPreShutdown();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	protected void fireServerStarted() {
		logger.info("Gateway Server Started");
		for (GatewayServerListener listener : listeners) {
			try {
				listener.serverStarted();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	protected void fireServerStopped() {
		logger.info("Gateway Server Stopped");
		for (GatewayServerListener listener : listeners) {
			try {
				listener.serverStopped();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	@Override
	public InetAddress getAddress() {
		return address;
	}

	public Py4JPythonClient getCallbackClient() {
		return gateway.getCallbackClient();
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public Gateway getGateway() {
		return gateway;
	}

	/**
	 *
	 * @return The port the server socket is listening on. It will be different
	 *         than the specified port if the socket is listening on an
	 *         ephemeral port (specified port = 0). Returns -1 if the server
	 *         socket is not listening on anything.
	 */
	@Override
	public int getListeningPort() {
		int port = -1;
		try {
			if (sSocket.isBound()) {
				port = sSocket.getLocalPort();
			}
		} catch (Exception e) {
			// do nothing
		}
		return port;
	}

	/**
	 *
	 * @return The port specified when the gateway server is initialized. This
	 *         is the port that is passed to the server socket.
	 */
	@Override
	public int getPort() {
		return port;
	}

	@Override
	public InetAddress getPythonAddress() {
		return pythonAddress;
	}

	@Override
	public int getPythonPort() {
		return pythonPort;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	protected void processSocket(Socket socket) {
		try {
			lock.lock();
			if (!isShutdown) {
				socket.setSoTimeout(readTimeout);
				Py4JServerConnection gatewayConnection = createConnection(gateway, socket);
				connections.add(gatewayConnection);
				fireConnectionStarted(gatewayConnection);
			}
		} catch (Exception e) {
			// Error while processing a connection should not be prevent the
			// gateway server from accepting new connections.
			fireConnectionError(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void removeListener(GatewayServerListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void run() {
		try {
			gateway.startup();
			fireServerStarted();
			addListener(this);
			while (!isShutdown) {
				Socket socket = sSocket.accept();
				processSocket(socket);
			}
		} catch (Exception e) {
			fireServerError(e);
		}
		fireServerStopped();
		removeListener(this);
	}

	/**
	 * <p>
	 * Stops accepting connections, closes all current connections, and calls
	 * {@link py4j.Gateway#shutdown() Gateway.shutdown()}
	 * </p>
	 */
	public void shutdown() {
		this.shutdown(true);
	}

	/**
	 * <p>
	 * Stops accepting connections, closes all current connections, and calls
	 * {@link py4j.Gateway#shutdown() Gateway.shutdown()}
	 * </p>
	 *
	 * @param shutdownCallbackClient If True, shuts down the CallbackClient
	 *                                  instance.
	 */
	public void shutdown(boolean shutdownCallbackClient) {
		fireServerPreShutdown();
		try {
			lock.lock();
			if (isShuttingDown) {
				// Prevent call to shutdown by a listener.
				return;
			}
			isShutdown = true;
			isShuttingDown = true;
			NetworkUtil.quietlyClose(sSocket);
			ArrayList<Py4JServerConnection> tempConnections = new ArrayList<Py4JServerConnection>(connections);
			for (Py4JServerConnection connection : tempConnections) {
				connection.shutdown();
			}
			// Clear existing connections
			connections.clear();
			gateway.shutdown(shutdownCallbackClient);
		} finally {
			// If an error occurs, do not prevent the shutdown method from being called again.
			isShuttingDown = false;
			lock.unlock();
		}
		fireServerPostShutdown();
	}

	/**
	 * <p>
	 * Starts to accept connections in a second thread (non-blocking call).
	 * </p>
	 */
	public void start() {
		start(true);
	}

	/**
	 * <p>
	 * Starts to accept connections.
	 * </p>
	 *
	 * @param fork
	 *            If true, the GatewayServer accepts connection in another
	 *            thread and this call is non-blocking. If False, the
	 *            GatewayServer accepts connection in this thread and the call
	 *            is blocking (until the Gateway is shutdown by another thread).
	 * @throws Py4JNetworkException
	 *             If the server socket cannot start.
	 */
	public void start(boolean fork) {
		startSocket();

		if (fork) {
			Thread t = new Thread(this);
			t.start();
		} else {
			run();
		}
	}

	/**
	 * <p>
	 * Starts the ServerSocket.
	 * </p>
	 *
	 * @throws Py4JNetworkException
	 *             If the port is busy.
	 */
	protected void startSocket() throws Py4JNetworkException {
		try {
			sSocket = sSocketFactory.createServerSocket();
			sSocket.setSoTimeout(connectTimeout);
			sSocket.setReuseAddress(true);
			sSocket.bind(new InetSocketAddress(address, port), -1);
		} catch (IOException e) {
			throw new Py4JNetworkException(e);
		}
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
	public Object getPythonServerEntryPoint(@SuppressWarnings("rawtypes") Class[] interfacesToImplement) {
		return getCallbackClient().getPythonServerEntryPoint(gateway, interfacesToImplement);
	}

	/**
	 * <p>
	 * Main method to start a local GatewayServer on a given port.
	 * The listening port is printed to stdout so that clients can start
	 * servers on ephemeral ports.
	 * </p>
	 *
	 * <p>
	 * If authentication is enabled, the server will create an auth secret with 256 bits of entropy
	 * and print it to stdout after the server port. Clients should then provide this secret when
	 * connecting to the server. Note that no second line of output is printed if authentication is
	 * not enabled.
	 * </p>
	 */
	public static void main(String[] args) {
		int port;
		boolean dieOnBrokenPipe = false;
		boolean enableAuth = false;
		String usage = "usage: [--die-on-broken-pipe] [--enable-auth] port";

		if (args.length == 0) {
			System.err.println(usage);
			System.exit(1);
		}

		for (int i = 0; i < args.length - 1; i++) {
			String opt = args[i];
			if (opt.equals("--die-on-broken-pipe")) {
				dieOnBrokenPipe = true;
			} else if (opt.equals("--enable-auth")) {
				enableAuth = true;
			} else {
				System.err.println(usage);
				System.exit(1);
			}
		}

		port = Integer.parseInt(args[args.length - 1]);

		String authToken = null;
		if (enableAuth) {
			SecureRandom rnd = new SecureRandom();
			byte[] token = new byte[256 / Byte.SIZE];
			rnd.nextBytes(token);
			authToken = Base64.encodeToString(token, false);
		}

		GatewayServer gatewayServer = new GatewayServerBuilder().javaPort(port).authToken(authToken).build();
		gatewayServer.start();
		/* Print out the listening port so that clients can discover it. */
		int listening_port = gatewayServer.getListeningPort();
		System.out.println("" + listening_port);

		if (authToken != null) {
			System.out.println(authToken);
		}

		if (dieOnBrokenPipe) {
			/* Exit on EOF or broken pipe.  This ensures that the server dies
			 * if its parent program dies. */
			try {
				BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")));
				stdin.readLine();
				System.exit(0);
			} catch (java.io.IOException e) {
				System.exit(1);
			}
		}
	}

	/**
	 *
	 * @return An unmodifiable list of custom commands
	 */
	public List<Class<? extends Command>> getCustomCommands() {
		return Collections.unmodifiableList(customCommands);
	}

	/**
	 *
	 * @return An unmodifiable list of listeners
	 */
	public List<GatewayServerListener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	/**
	 * Helper class to make it easier and self-documenting how a
	 * {@link GatewayServer} is constructed.
	 */
	public static class GatewayServerBuilder {
		private int javaPort;
		private InetAddress javaAddress;
		private int connectTimeout;
		private int readTimeout;
		private Gateway gateway;
		private ServerSocketFactory serverSocketFactory;
		private Object entryPoint;
		private Py4JPythonClient callbackClient;
		private List<Class<? extends Command>> customCommands;
		private String authToken;

		public GatewayServerBuilder() {
			this(null);
		}

		public GatewayServerBuilder(Object entryPoint) {
			javaPort = GatewayServer.DEFAULT_PORT;
			javaAddress = GatewayServer.defaultAddress();
			connectTimeout = GatewayServer.DEFAULT_CONNECT_TIMEOUT;
			readTimeout = GatewayServer.DEFAULT_READ_TIMEOUT;
			serverSocketFactory = ServerSocketFactory.getDefault();
			this.entryPoint = entryPoint;
		}

		/**
		 * <p>
		 * Builds a GatewayServer instance using the provided parameters. If gateway is provided,
		 * some parameters will be ignored (callbackClient and entryPoint).
		 * </p>
		 *
		 * @return
		 */
		public GatewayServer build() {
			if (gateway == null) {
				if (callbackClient == null) {
					callbackClient = new CallbackClient(GatewayServer.DEFAULT_PYTHON_PORT);
				}
				return new GatewayServer(entryPoint, javaPort, javaAddress, connectTimeout, readTimeout, customCommands,
						callbackClient, serverSocketFactory, authToken);
			} else {
				return new GatewayServer(gateway, javaPort, javaAddress, connectTimeout, readTimeout, customCommands,
						serverSocketFactory, authToken);
			}
		}

		public GatewayServerBuilder gateway(Gateway gateway) {
			this.gateway = gateway;
			return this;
		}

		public GatewayServerBuilder javaPort(int javaPort) {
			this.javaPort = javaPort;
			return this;
		}

		public GatewayServerBuilder javaAddress(InetAddress javaAddress) {
			this.javaAddress = javaAddress;
			return this;
		}

		public GatewayServerBuilder callbackClient(int pythonPort, InetAddress pythonAddress) {
			callbackClient = new CallbackClient(pythonPort, pythonAddress);
			return this;
		}

		/**
		 * Set up the callback client to talk to the server running at the given address and port,
		 * authenticating with the given token. If the token is null, no authentication will be
		 * attempted.
		 */
		public GatewayServerBuilder callbackClient(int pythonPort, InetAddress pythonAddress, String authToken) {
			callbackClient = new CallbackClient(pythonPort, pythonAddress, authToken);
			return this;
		}

		public GatewayServerBuilder callbackClient(CallbackClient callbackClient) {
			this.callbackClient = callbackClient;
			return this;
		}

		public GatewayServerBuilder connectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		public GatewayServerBuilder readTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public GatewayServerBuilder serverSocketFactory(ServerSocketFactory serverSocketFactory) {
			this.serverSocketFactory = serverSocketFactory;
			return this;
		}

		public GatewayServerBuilder entryPoint(Object entryPoint) {
			this.entryPoint = entryPoint;
			return this;
		}

		public GatewayServerBuilder customCommands(List<Class<? extends Command>> customCommands) {
			this.customCommands = customCommands;
			return this;
		}

		/**
		 * Authentication token that clients must provide to the server when connecting. If null,
		 * authentication is disabled.
		 */
		public GatewayServerBuilder authToken(String authToken) {
			this.authToken = StringUtil.escape(authToken);
			return this;
		}
	}
}
