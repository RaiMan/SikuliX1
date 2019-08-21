/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.ReturnObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A ConstructorCommand is responsible for calling the constructors of a Java
 * class. It provides similar services to the CallCommand.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class ConstructorCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(CallCommand.class.getName());

	public final static String CONSTRUCTOR_COMMAND_NAME = "i";

	public ConstructorCommand() {
		super();
		this.commandName = CONSTRUCTOR_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String fqn = reader.readLine();
		List<Object> arguments = getArguments(reader);

		ReturnObject returnObject = invokeConstructor(fqn, arguments);

		String returnCommand = Protocol.getOutputCommand(returnObject);
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	protected ReturnObject invokeConstructor(String fqn, List<Object> arguments) {
        ReturnObject returnObject;
		try {
			returnObject = gateway.invoke(fqn, arguments);
		} catch (Exception e) {
			logger.log(Level.FINE, "Received exception while executing this command: " + fqn, e);
			returnObject = ReturnObject.getErrorReturnObject(e);
		}
		return returnObject;
	}

}
