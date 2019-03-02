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
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.commands.AuthCommand;
import py4j.commands.Command;

public class ClientServerConnection implements Py4JServerConnection, Py4JClientConnection, Runnable {

	private boolean used = false;
	private boolean initiatedFromClient = false;
	protected Socket socket;
	protected BufferedWriter writer;
	protected BufferedReader reader;
	protected final Map<String, Command> commands;
	protected final Logger logger = Logger.getLogger(ClientServerConnection.class.getName());
	protected final Py4JJavaServer javaServer;
	protected final Py4JPythonClientPerThread pythonClient;
	protected final int blockingReadTimeout;
	protected final int nonBlockingReadTimeout;
	protected final String authToken;
	protected final AuthCommand authCommand;

	public ClientServerConnection(Gateway gateway, Socket socket, List<Class<? extends Command>> customCommands,
			Py4JPythonClientPerThread pythonClient, Py4JJavaServer javaServer, int readTimeout) throws IOException {
		this(gateway, socket, customCommands, pythonClient, javaServer, readTimeout, null);
	}

	public ClientServerConnection(Gateway gateway, Socket socket, List<Class<? extends Command>> customCommands,
			Py4JPythonClientPerThread pythonClient, Py4JJavaServer javaServer, int readTimeout, String authToken)
					throws IOException {
		super();
		this.socket = socket;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
		this.commands = new HashMap<String, Command>();
		initCommands(gateway, GatewayConnection.getBaseCommands());
		if (customCommands != null) {
			initCommands(gateway, customCommands);
		}
		this.javaServer = javaServer;
		this.pythonClient = pythonClient;
		this.blockingReadTimeout = readTimeout;
		if (readTimeout > 0) {
			this.nonBlockingReadTimeout = readTimeout;
		} else {
			this.nonBlockingReadTimeout = CallbackConnection.DEFAULT_NONBLOCKING_SO_TIMEOUT;
		}
		this.authToken = authToken;
		if (authToken != null) {
			this.authCommand = new AuthCommand(authToken);
			initCommand(gateway, authCommand);
		} else {
			this.authCommand = null;
		}
	}

	public void startServerConnection() throws IOException {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		pythonClient.setPerThreadConnection(this);
		waitForCommands();
	}

	/**
	 * <p>
	 * Override this method to initialize custom commands.
	 * </p>
	 *
	 * @param gateway
	 * @param commandsClazz
	 */
	protected void initCommands(Gateway gateway, List<Class<? extends Command>> commandsClazz) {
		for (Class<? extends Command> clazz : commandsClazz) {
			try {
				Command cmd = clazz.newInstance();
				initCommand(gateway, cmd);
			} catch (Exception e) {
				String name = "null";
				if (clazz != null) {
					name = clazz.getName();
				}
				logger.log(Level.SEVERE, "Could not initialize command " + name, e);
			}
		}
	}

	private void initCommand(Gateway gateway, Command cmd) {
		cmd.init(gateway, this);
		commands.put(cmd.getCommandName(), cmd);
	}

	protected void fireConnectionStopped() {
		logger.info("Connection Stopped");

		for (GatewayServerListener listener : javaServer.getListeners()) {
			try {
				listener.connectionStopped(this);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	protected void quietSendFatalError(BufferedWriter writer, Throwable exception) {
		try {
			String returnCommand = Protocol.getOutputFatalErrorCommand(exception);
			logger.fine("Trying to return error: " + returnCommand);
			writer.write(returnCommand);
			writer.flush();
		} catch (Exception e) {
			logger.log(Level.FINEST, "Error in quiet send.", e);
		}
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	public void waitForCommands() {
		boolean reset = false;
		boolean executing = false;
		Throwable error = null;
		try {
			logger.info("Gateway Connection ready to receive messages");
			String commandLine = null;
			do {
				commandLine = reader.readLine();
				executing = true;
				logger.fine("Received command: " + commandLine);
				Command command = commands.get(commandLine);

				if (command != null) {
					if (authCommand != null && !authCommand.isAuthenticated()) {
						authCommand.execute(commandLine, reader, writer);
					} else {
						command.execute(commandLine, reader, writer);
					}
					executing = false;
				} else {
					reset = true;
					throw new Py4JException("Unknown command received: " + commandLine);
				}
			} while (commandLine != null && !commandLine.equals("q"));
		} catch (SocketTimeoutException ste) {
			logger.log(Level.WARNING, "Timeout occurred while waiting for a command.", ste);
			reset = true;
			error = ste;
		} catch (Py4JAuthenticationException pae) {
			logger.log(Level.SEVERE, "Authentication error.", pae);
			// We do not store the error because we do not want to
			// send a message to the other side.
			reset = true;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error occurred while waiting for a command.", e);
			error = e;
		} finally {
			if (error != null && executing && writer != null) {
				quietSendFatalError(writer, error);
			}
			shutdown(reset);
		}
	}

	public String sendCommand(String command) {
		return this.sendCommand(command, true);
	}

	public String sendCommand(String command, boolean blocking) {
		// TODO REFACTOR so that we use the same code in sendCommand and wait
		logger.log(Level.INFO, "Sending Python command: " + command);
		String returnCommand = null;
		try {
			writer.write(command);
			writer.flush();
		} catch (Exception e) {
			throw new Py4JNetworkException("Error while sending a command: " + command, e,
					Py4JNetworkException.ErrorTime.ERROR_ON_SEND);
		}

		try {
			while (true) {
				if (blocking) {
					returnCommand = this.readBlockingResponse(this.reader);
				} else {
					returnCommand = this.readNonBlockingResponse(this.socket, this.reader);
				}

				if (returnCommand == null || returnCommand.trim().equals("")) {
					// TODO LOG AND DO SOMETHING INTELLIGENT
					throw new Py4JException("Received empty command");
				} else if (Protocol.isReturnMessage(returnCommand)) {
					returnCommand = returnCommand.substring(1);
					logger.log(Level.INFO, "Returning CB command: " + returnCommand);
					return returnCommand;
				} else {
					Command commandObj = commands.get(returnCommand);
					if (commandObj != null) {
						commandObj.execute(returnCommand, reader, writer);
					} else {
						logger.log(Level.WARNING, "Unknown command " + returnCommand);
						// TODO SEND BACK AN ERROR?
					}
				}
			}
		} catch (Exception e) {
			// This will make sure that the connection is shut down and not given back to the connections deque.
			throw new Py4JNetworkException("Error while sending a command: " + command, e,
					Py4JNetworkException.ErrorTime.ERROR_ON_RECEIVE);
		}
	}

	@Override
	public void shutdown() {
		shutdown(false);
	}

	@Override
	public void shutdown(boolean reset) {
		if (reset) {
			NetworkUtil.quietlySetLinger(socket);
		}
		// XXX Close socket first, otherwise, reader.close() will block if stuck on readLine.
		NetworkUtil.quietlyClose(socket);
		NetworkUtil.quietlyClose(reader);
		NetworkUtil.quietlyClose(writer);
		socket = null;
		writer = null;
		reader = null;
		if (!initiatedFromClient) {
			// Only fires this event when the connection is created by the JavaServer to respect the protocol.
			fireConnectionStopped();
		}
	}

	@Override
	public void start() throws IOException {
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

	@Override
	public void setUsed(boolean used) {
		this.used = used;
	}

	@Override
	public boolean wasUsed() {
		return used;
	}

	public boolean isInitiatedFromClient() {
		return initiatedFromClient;
	}

	public void setInitiatedFromClient(boolean initiatedFromClient) {
		this.initiatedFromClient = initiatedFromClient;
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

}
