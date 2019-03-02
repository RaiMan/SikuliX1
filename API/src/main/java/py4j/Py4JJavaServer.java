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
	 * {@link py4j.Gateway#shutdown() Gateway.shutdown()}
	 * </p>
	 */
	void shutdown();

	/**
	 * <p>
	 * Stops accepting connections, closes all current connections, and calls
	 * {@link py4j.Gateway#shutdown() Gateway.shutdown()}
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
