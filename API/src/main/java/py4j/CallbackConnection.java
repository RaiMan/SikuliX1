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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;

/**
 * <p>
 * Default implementation of the CommunicationChannel interface using TCP
 * sockets.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class CallbackConnection implements Py4JClientConnection {

	private boolean used;

	public final static int DEFAULT_NONBLOCKING_SO_TIMEOUT = 1000;
	private final int port;

	private final InetAddress address;

	private final SocketFactory socketFactory;

	private Socket socket;

	private BufferedReader reader;

	private BufferedWriter writer;

	private final Logger logger = Logger.getLogger(CallbackConnection.class.getName());

	private final int blockingReadTimeout;

	private final int nonBlockingReadTimeout;

	private final String authToken;

	public CallbackConnection(int port, InetAddress address) {
		this(port, address, SocketFactory.getDefault());
	}

	public CallbackConnection(int port, InetAddress address, SocketFactory socketFactory) {
		this(port, address, socketFactory, GatewayServer.DEFAULT_READ_TIMEOUT);
	}

	/**
	 *
	 * @param port The port used to connect to the Python side.
	 * @param address The address used to connect to the Java side.
	 * @param socketFactory The socket factory used to create a socket (connection) to the Python side.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once connected to the Python side,
	 *            if the Java side does not receive a response after this time, the connection with the Python
	 *            program is closed. If readTimeout = 0, a default readTimeout of 1000 is used for operations that
	 *            must absolutely be non-blocking.
	 */
	public CallbackConnection(int port, InetAddress address, SocketFactory socketFactory, int readTimeout) {
		this(port, address, socketFactory, readTimeout, null);
	}

	/**
	 *
	 * @param port The port used to connect to the Python side.
	 * @param address The address used to connect to the Java side.
	 * @param socketFactory The socket factory used to create a socket (connection) to the Python side.
	 * @param readTimeout
	 *            Time in milliseconds (0 = infinite). Once connected to the Python side,
	 *            if the Java side does not receive a response after this time, the connection with the Python
	 *            program is closed. If readTimeout = 0, a default readTimeout of 1000 is used for operations that
	 *            must absolutely be non-blocking.
	 * @param authToken Token for authenticating with the callback server.
	 */
	public CallbackConnection(int port, InetAddress address, SocketFactory socketFactory, int readTimeout,
			String authToken) {
		super();
		this.port = port;
		this.address = address;
		this.socketFactory = socketFactory;
		this.blockingReadTimeout = readTimeout;
		if (readTimeout > 0) {
			this.nonBlockingReadTimeout = readTimeout;
		} else {
			this.nonBlockingReadTimeout = DEFAULT_NONBLOCKING_SO_TIMEOUT;
		}
		this.authToken = authToken;
	}

	public String sendCommand(String command) {
		return this.sendCommand(command, true);
	}

	public String sendCommand(String command, boolean blocking) {
		logger.log(Level.INFO, "Sending CB command: " + command);
		String returnCommand = null;
		try {
			this.used = true;
			// XXX write will never fail for small commands because the payload is below the socket's buffer.
			writer.write(command);
			writer.flush();
		} catch (Exception e) {
			throw new Py4JNetworkException("Error while sending a command: null response: " + command, e,
					Py4JNetworkException.ErrorTime.ERROR_ON_SEND);
		}

		try {
			if (blocking) {
				returnCommand = this.readBlockingResponse(this.reader);
			} else {
				returnCommand = this.readNonBlockingResponse(this.socket, this.reader);
			}
		} catch (Exception e) {
			throw new Py4JNetworkException("Error while sending a command: " + command, e,
					Py4JNetworkException.ErrorTime.ERROR_ON_RECEIVE);
		}

		if (returnCommand == null) {
			throw new Py4JNetworkException("Error while sending a command: null response: " + command,
					Py4JNetworkException.ErrorTime.ERROR_ON_RECEIVE);
		} else if (Protocol.isReturnMessage(returnCommand)) {
			returnCommand = returnCommand.substring(1);
		}

		logger.log(Level.INFO, "Returning CB command: " + returnCommand);
		return returnCommand;
	}

	protected String readBlockingResponse(BufferedReader reader) throws IOException {
		return reader.readLine();
	}

	protected String readNonBlockingResponse(Socket socket, BufferedReader reader) throws IOException {
		String returnCommand = null;

		socket.setSoTimeout(nonBlockingReadTimeout);

		while (true) {
			try {
				returnCommand = reader.readLine();
				break;
			} finally {
				// Set back blocking timeout (necessary if
				// sockettimeoutexception is raised and propagated)
				socket.setSoTimeout(blockingReadTimeout);
			}
		}

		// Set back blocking timeout
		socket.setSoTimeout(blockingReadTimeout);

		return returnCommand;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	@Override
	public void shutdown() {
		shutdown(false);
	}

	/**
	 * <p>
	 * Shuts down the connection by closing the socket, the writer, and the reader.
	 * </p>
	 *
	 * <p>
	 * Internal: at this point, the connection has not been given back to the connections deque, or the deque is about
	 * to be cleared.
	 * </p>
	 */
	@Override
	public void shutdown(boolean reset) {
		if (reset) {
			NetworkUtil.quietlySetLinger(socket);
		}
		// XXX Close socket first, otherwise, reader.close() will block if stuck on readLine.
		NetworkUtil.quietlyClose(socket);
		NetworkUtil.quietlyClose(reader);
		NetworkUtil.quietlyClose(writer);
	}

	public void start() throws IOException {
		logger.info("Starting Communication Channel on " + address + " at " + port);
		socket = socketFactory.createSocket(address, port);
		socket.setSoTimeout(blockingReadTimeout);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

		if (authToken != null) {
			try {
				// TODO should we receive an AuthException instead of an IOException?
				NetworkUtil.authToServer(reader, writer, authToken);
			} catch (IOException ioe) {
				shutdown(true);
				throw ioe;
			}
		}
	}

	public boolean wasUsed() {
		return used;
	}

}
