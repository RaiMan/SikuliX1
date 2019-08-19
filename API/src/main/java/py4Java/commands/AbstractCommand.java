/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4Java.Gateway;
import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.Py4JServerConnection;
import py4Java.ReturnObject;

/**
 * <p>
 * Abstract base class for commands. Provides useful methods allowing the
 * parsing of command arguments.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public abstract class AbstractCommand implements Command {

	protected Gateway gateway;

	protected String commandName;

	private final Logger logger = Logger.getLogger(AbstractCommand.class.getName());

	protected Py4JServerConnection connection;

	@Override
	public abstract void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException;

	/**
	 *
	 * @param reader
	 * @return A list of the remaining arguments (converted using
	 *         Protocol.getObject) in the reader. Consumes the end of command
	 *         part.
	 * @throws IOException
	 */
	protected List<Object> getArguments(BufferedReader reader) throws IOException {
		List<Object> arguments = new ArrayList<Object>();
		List<String> stringArguments = getStringArguments(reader);

		for (String stringArgument : stringArguments) {
			arguments.add(Protocol.getObject(stringArgument, this.gateway));
		}

		return arguments;
	}

	@Override
	public String getCommandName() {
		return commandName;
	}

	/**
	 *
	 * @param reader
	 * @return A list of the remaining arguments (as strings) in the reader.
	 *         Consumes the end of command part.
	 * @throws IOException
	 */
	protected List<String> getStringArguments(BufferedReader reader) throws IOException {
		List<String> arguments = new ArrayList<String>();
		String line = reader.readLine();

		while (!Protocol.isEmpty(line) && !Protocol.isEnd(line)) {
			logger.finest("Raw String Argument: " + line);
			arguments.add(line);
			line = reader.readLine();
		}

		return arguments;
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		this.gateway = gateway;
		this.connection = connection;
	}

	/**
	 * <p>
	 * Convenient shortcut to invoke a method dynamically.
	 * </p>
	 *
	 * @param methodName
	 * @param targetObjectId
	 * @param arguments
	 * @return
	 */
	protected ReturnObject invokeMethod(String methodName, String targetObjectId, List<Object> arguments) {
		ReturnObject returnObject = null;
		try {
			returnObject = gateway.invoke(methodName, targetObjectId, arguments);
		} catch (Exception e) {
			logger.log(Level.FINE, "Received exception while executing this command: " + methodName, e);
			returnObject = ReturnObject.getErrorReturnObject(e);
		}
		return returnObject;
	}

}
