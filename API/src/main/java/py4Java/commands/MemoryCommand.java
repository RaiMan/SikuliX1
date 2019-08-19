/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import static py4Java.NetworkUtil.safeReadLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

import py4Java.Protocol;
import py4Java.Py4JException;

/**
 * <p>
 * The MemoryCommand is responsible for handling garbage collection requests
 * from the Python side, i.e., when a java object is no longer used by the
 * Python program.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class MemoryCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(MemoryCommand.class.getName());

	public final static String MEMORY_COMMAND_NAME = "m";

	public final static String MEMORY_DEL_SUB_COMMAND_NAME = "d";

	public MemoryCommand() {
		super();
		this.commandName = MEMORY_COMMAND_NAME;
	}

	private String deleteObject(BufferedReader reader) throws IOException {
		String objectId = reader.readLine();
		// EoC
		reader.readLine();

		if (objectId != Protocol.ENTRY_POINT_OBJECT_ID && objectId != Protocol.DEFAULT_JVM_OBJECT_ID
				&& objectId != Protocol.GATEWAY_SERVER_ID) {
			gateway.deleteObject(objectId);
		}

		return Protocol.getOutputVoidCommand();
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String returnCommand = null;
		String subCommand = safeReadLine(reader);

		if (subCommand.equals(MEMORY_DEL_SUB_COMMAND_NAME)) {
			returnCommand = deleteObject(reader);
		} else {
			returnCommand = Protocol.getOutputErrorCommand("Unknown Memory SubCommand Name: " + subCommand);
		}
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

}
