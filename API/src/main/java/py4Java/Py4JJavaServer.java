/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.net.InetAddress;
import java.util.List;

/**
 * <p>
 * Interface that describes the operations a server must support to receive
 * requests from the Python side.
 * </p>
 */
public interface Py4JJavaServer {

	/**
	 *
	 * @return An unmodifiable list of listeners
	 */
	List<GatewayServerListener> getListeners();

	InetAddress getAddress();

	Gateway getGateway();

	int getListeningPort();

	int getPort();

	InetAddress getPythonAddress();

	int getPythonPort();

	void removeListener(GatewayServerListener listener);

	/**
	 * <p>
	 * Stops accepting connections, closes all current connections, and calls
	 * {@link py4Java.Gateway#shutdown() Gateway.shutdown()}
	 * </p>
	 */
	void shutdown();

	/**
	 * <p>
	 * Stops accepting connections, closes all current connections, and calls
	 * {@link py4Java.Gateway#shutdown() Gateway.shutdown()}
	 * </p>
	 *
	 * @param shutdownCallbackClient If True, shuts down the CallbackClient
	 *                                  instance.
	 */
	void shutdown(boolean shutdownCallbackClient);

	void addListener(GatewayServerListener listener);

	/**
	 * <p>
	 * Starts to accept connections in a second thread (non-blocking call).
	 * </p>
	 */
	void start();

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
	void start(boolean fork);
}
