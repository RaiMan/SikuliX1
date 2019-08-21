/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import py4Java.commands.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Manage the connection between a Python program and a Gateway. A
 * GatewayConnection lives in its own thread and is created on demand (e.g., one
 * per concurrent thread).
 * </p>
 *
 * <p>
 * The request to connect to the JVM goes through the {@link py4Java.GatewayServer
 * GatewayServer} first and is then passed to a GatewayConnection.
 * </p>
 *
 * <p>
 * This class is not intended to be directly accessed by users.
 * </p>
 *
 *
 * @author Barthelemy Dagenais
 *
 */
public class GatewayConnection implements Runnable, Py4JServerConnection {

	private final static List<Class<? extends Command>> baseCommands;
	protected final Socket socket;
	protected final String authToken;
	protected final AuthCommand authCommand;
	protected final BufferedWriter writer;
	protected final BufferedReader reader;
	protected final Map<String, Command> commands;
	protected final Logger logger = Logger.getLogger(GatewayConnection.class.getName());
	protected final List<GatewayServerListener> listeners;

	static {
		baseCommands = new ArrayList<>();
		baseCommands.add(ArrayCommand.class);
		baseCommands.add(CallCommand.class);
		baseCommands.add(ConstructorCommand.class);
		baseCommands.add(FieldCommand.class);
		baseCommands.add(HelpPageCommand.class);
		baseCommands.add(ListCommand.class);
		baseCommands.add(MemoryCommand.class);
		baseCommands.add(ReflectionCommand.class);
		baseCommands.add(ShutdownGatewayServerCommand.class);
		baseCommands.add(JVMViewCommand.class);
		baseCommands.add(ExceptionCommand.class);
		baseCommands.add(DirCommand.class);
		baseCommands.add(StreamCommand.class);
	}

	/**
	 *
	 * @return The list of base commands that are provided by default. Can be
	 *         hidden by custom commands with the same command id by passing a
	 *         list of custom commands to the {@link py4Java.GatewayServer
	 *         GatewayServer}.
	 */
	public static List<Class<? extends Command>> getBaseCommands() {
		return baseCommands;
	}

	public GatewayConnection(Gateway gateway, Socket socket) throws IOException {
		this(gateway, socket, null, new ArrayList<>());
	}

	public GatewayConnection(Gateway gateway, Socket socket, List<Class<? extends Command>> customCommands,
			List<GatewayServerListener> listeners) throws IOException {
		this(gateway, socket, null, customCommands, listeners);
	}

	public GatewayConnection(Gateway gateway, Socket socket, String authToken,
			List<Class<? extends Command>> customCommands, List<GatewayServerListener> listeners) throws IOException {
		super();
		this.socket = socket;
		this.authToken = authToken;
		if (authToken != null) {
			this.authCommand = new AuthCommand(authToken);
		} else {
			this.authCommand = null;
		}
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		this.commands = new HashMap<>();
		initCommands(gateway, baseCommands);
		if (customCommands != null) {
			initCommands(gateway, customCommands);
		}
		if (authCommand != null) {
			initCommand(gateway, authCommand);
		}
		this.listeners = listeners;
	}

	/**
	 * <p>Wraps the GatewayConnection in a thread and start the thread.</p>
	 */
	public void startConnection() {
		Thread t = new Thread(this);
		t.start();
	}

	protected void fireConnectionStopped() {
		logger.info("Connection Stopped");

		for (GatewayServerListener listener : listeners) {
			try {
				listener.connectionStopped(this);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "A listener crashed.", e);
			}
		}
	}

	/**
	 *
	 * @return The socket used by this gateway connection.
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * <p>
	 * Override this method to initialize custom commands.
	 * </p>
	 *
	 * @param gateway
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
	public void run() {
		boolean executing = false;
		boolean reset = false;
		Throwable error = null;
		try {
			logger.info("Gateway Connection ready to receive messages");
			String commandLine = null;
			do {
				commandLine = reader.readLine();
				executing = true;
				logger.fine("Received command: " + commandLine);
				if (null != commandLine) {
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
				}
			} while (commandLine != null && !commandLine.equals("q"));
		} catch (SocketTimeoutException ste) {
			logger.log(Level.WARNING, "Timeout occurred while waiting for a command.", ste);
			error = ste;
			reset = true;
		} catch (Py4JAuthenticationException pae) {
			logger.log(Level.SEVERE, "Authentication error.", pae);
			// We do not store the error because we do not want to write
			// a message to the other side.
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

	@Override
	public void shutdown() {
		shutdown(false);
	}

	/**
	 * <p>
	 * Shuts down the connection by closing the socket, the writer, and the reader.
	 * </p>
	 * <p>
	 * Internal: emits a connection stopped signal so GatewayServer can remove the connection from the connections list.
	 * In rare occasions, the shutdown method may be called twice (when the server shuts down at the same time as the
	 * connection fails and shuts down).
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
		fireConnectionStopped();
	}
}
