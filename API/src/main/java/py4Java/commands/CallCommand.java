/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.ReturnObject;

/**
 * <p>
 * A CallCommand is responsible for parsing a call command and calling the
 * method on the target object.
 * </p>
 * <p>
 * Currently, the call command assumes that a command is well-formed and that
 * there is no communication problem (e.g., the source virtual machine
 * disconnected in the middle of a command). This is a reasonable assumption
 * because the two virtual machines are assumed to be on the same host.
 * </p>
 * <p>
 * <b>TODO:</b> Make the call command more robust to communication errors and
 * ill-formed protocol.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class CallCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(CallCommand.class.getName());

	public final static String CALL_COMMAND_NAME = "c";

	public CallCommand() {
		super();
		this.commandName = CALL_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String targetObjectId = reader.readLine();
		String methodName = reader.readLine();
		List<Object> arguments = getArguments(reader);

		ReturnObject returnObject = invokeMethod(methodName, targetObjectId, arguments);

		String returnCommand = Protocol.getOutputCommand(returnObject);
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

}
