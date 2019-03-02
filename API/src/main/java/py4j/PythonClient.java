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
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.net.SocketFactory;

import py4j.commands.Command;

/**
 * <p>
 * Subclass of CallbackClient that implements the new threading model,
 * ensuring that each thread uses its own connection.
 * </p>
 */
public class PythonClient extends CallbackClient implements Py4JPythonClientPerThread, GatewayServerListener {

	protected Gateway gateway;

	protected List<Class<? extends Command>> customCommands;

	protected final Logger logger = Logger.getLogger(PythonClient.class.getName());

	protected Py4JJavaServer javaServer;

	protected ThreadLocal<WeakReference<ClientServerConnection>> threadConnection;

	protected final int readTimeout;

	/**
	 *
	 * @param gateway The gateway used to pool Java instances created on the Python side.
	 * @param customCommands Optional list of custom commands that can be invoked by the Python side.
	 * @param pythonPort Port the PythonClient should connect to.
	 * @param pythonAddress Address (IP) the PythonClient should connect to.
	 * @param minConnectionTime Minimum time to wait before closing unused connections. Not used with PythonClient.
	 * @param minConnectionTimeUnit Time unit of minConnectionTime
	 * @param socketFactory SocketFactory used to create a socket.
	 * @param javaServer The JavaServer used to receive commands from the Python side.
	 */
	public PythonClient(Gateway gateway, List<Class<? extends Command>> customCommands, int pythonPort,
			InetAddress pythonAddress, long minConnectionTime, TimeUnit minConnectionTimeUnit,
			SocketFactory socketFactory, Py4JJavaServer javaServer) {
		this(gateway, customCommands, pythonPort, pythonAddress, minConnectionTime, minConnectionTimeUnit,
				socketFactory, javaServer, true, GatewayServer.DEFAULT_READ_TIMEOUT);
	}

	/**
	 *
	 * @param gateway The gateway used to pool Java instances created on the Python side.
	 * @param customCommands Optional list of custom commands that can be invoked by the Python side.
	 * @param pythonPort Port the PythonClient should connect to.
	 * @param pythonAddress Address (IP) the PythonClient should connect to.
	 * @param minConnectionTime Minimum time to wait before closing unused connections. Not used with PythonClient.
	 * @param minConnectionTimeUnit Time unit of minConnectionTime
	 * @param socketFactory SocketFactory used to create a socket.
	 * @param javaServer The JavaServer used to receive commands from the Python side.
	 * @param enableMemoryManagement If false, the Java side does not tell the Python side when a Python proxy is
	 *      			garbage collected.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once connected to the Python side,
	 *            if the Java side does not receive a response after this time, the connection with the Python
	 *            program is closed. If readTimeout = 0, a default readTimeout of 1000 is used for operations that
	 *            must absolutely be non-blocking.
	 */
	public PythonClient(Gateway gateway, List<Class<? extends Command>> customCommands, int pythonPort,
			InetAddress pythonAddress, long minConnectionTime, TimeUnit minConnectionTimeUnit,
			SocketFactory socketFactory, Py4JJavaServer javaServer, boolean enableMemoryManagement, int readTimeout) {
		this(gateway, customCommands, pythonPort, pythonAddress, minConnectionTime, minConnectionTimeUnit,
				socketFactory, javaServer, enableMemoryManagement, readTimeout, null);
	}

	/**
	 *
	 * @param gateway The gateway used to pool Java instances created on the Python side.
	 * @param customCommands Optional list of custom commands that can be invoked by the Python side.
	 * @param pythonPort Port the PythonClient should connect to.
	 * @param pythonAddress Address (IP) the PythonClient should connect to.
	 * @param minConnectionTime Minimum time to wait before closing unused connections. Not used with PythonClient.
	 * @param minConnectionTimeUnit Time unit of minConnectionTime
	 * @param socketFactory SocketFactory used to create a socket.
	 * @param javaServer The JavaServer used to receive commands from the Python side.
	 * @param enableMemoryManagement If false, the Java side does not tell the Python side when a Python proxy is
	 *      			garbage collected.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once connected to the Python side,
	 *            if the Java side does not receive a response after this time, the connection with the Python
	 *            program is closed. If readTimeout = 0, a default readTimeout of 1000 is used for operations that
	 *            must absolutely be non-blocking.
	 * @param authToken
	 *            Token for authenticating with the callback server.
	 */
	public PythonClient(Gateway gateway, List<Class<? extends Command>> customCommands, int pythonPort,
			InetAddress pythonAddress, long minConnectionTime, TimeUnit minConnectionTimeUnit,
			SocketFactory socketFactory, Py4JJavaServer javaServer, boolean enableMemoryManagement, int readTimeout,
			String authToken) {
		super(pythonPort, pythonAddress, authToken, minConnectionTime, minConnectionTimeUnit, socketFactory,
				enableMemoryManagement, readTimeout);
		this.gateway = gateway;
		this.javaServer = javaServer;
		this.customCommands = customCommands;
		this.threadConnection = new ThreadLocal<WeakReference<ClientServerConnection>>();
		this.readTimeout = readTimeout;
		setSelfListener();
	}

	private void setSelfListener() {
		// Used to know when a connection is closed so we can remove it from our connections list
		if (javaServer != null) {
			javaServer.addListener(this);
		}
	}

	@Override
	public ClientServerConnection getPerThreadConnection() {
		ClientServerConnection connection = null;
		WeakReference<ClientServerConnection> weakConnection = this.threadConnection.get();
		if (weakConnection != null) {
			connection = weakConnection.get();
		}
		return connection;
	}

	@Override
	public void setPerThreadConnection(ClientServerConnection clientServerConnection) {
		threadConnection.set(new WeakReference<ClientServerConnection>(clientServerConnection));
	}

	public Gateway getGateway() {
		return gateway;
	}

	public void setGateway(Gateway gateway) {
		this.gateway = gateway;
	}

	public Py4JJavaServer getJavaServer() {
		return javaServer;
	}

	public void setJavaServer(Py4JJavaServer javaServer) {
		this.javaServer = javaServer;
		setSelfListener();
	}

	@Override
	public int getReadTimeout() {
		return readTimeout;
	}

	@Override
	protected void setupCleaner() {
		// Do nothing, we don't need a cleaner.
	}

	protected Socket startClientSocket() throws IOException {
		logger.info("Starting Python Client connection on " + address + " at " + port);
		Socket socket = socketFactory.createSocket(address, port);
		socket.setSoTimeout(readTimeout);
		return socket;
	}

	@Override
	protected Py4JClientConnection getConnection() throws IOException {
		ClientServerConnection connection = null;

		connection = getPerThreadConnection();

		if (connection != null) {
			try {
				lock.lock();
				connections.remove(connection);
			} finally {
				lock.unlock();
			}
		}

		if (connection == null || connection.getSocket() == null) {
			Socket socket = startClientSocket();
			connection = new ClientServerConnection(gateway, socket, customCommands, this, javaServer, readTimeout,
					authToken);
			connection.setInitiatedFromClient(true);
			connection.start();
			setPerThreadConnection(connection);
		}

		return connection;
	}

	@Override
	protected boolean shouldRetrySendCommand(Py4JClientConnection cc, Py4JNetworkException pne) {
		boolean shouldRetry = super.shouldRetrySendCommand(cc, pne);

		if (shouldRetry && cc instanceof ClientServerConnection) {
			ClientServerConnection csc = (ClientServerConnection) cc;
			shouldRetry = csc.isInitiatedFromClient();
		}

		return shouldRetry;
	}

	@Override
	protected void giveBackConnection(Py4JClientConnection cc) {
		try {
			lock.lock();
			connections.addLast(cc);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Py4JPythonClient copyWith(InetAddress pythonAddress, int pythonPort) {
		return new PythonClient(gateway, customCommands, pythonPort, pythonAddress, minConnectionTime,
				minConnectionTimeUnit, socketFactory, javaServer);
	}

	@Override
	public void connectionError(Exception e) {

	}

	@Override
	public void connectionStarted(Py4JServerConnection gatewayConnection) {

	}

	@Override
	public void connectionStopped(Py4JServerConnection gatewayConnection) {
		try {
			// Best effort to remove connections from deque
			// In an ideal world, we should use a lock around connections, but it could be tricky (potential deadlock?)
			lock.lock();
			connections.remove(gatewayConnection);
		} catch (Exception e) {

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void serverError(Exception e) {

	}

	@Override
	public void serverPostShutdown() {

	}

	@Override
	public void serverPreShutdown() {

	}

	@Override
	public void serverStarted() {

	}

	@Override
	public void serverStopped() {

	}
}
