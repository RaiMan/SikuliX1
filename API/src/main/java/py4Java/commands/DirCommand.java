/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import py4Java.*;
import py4Java.reflection.ReflectionEngine;
import py4Java.reflection.TypeUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static py4Java.NetworkUtil.safeReadLine;

public class DirCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(DirCommand.class.getName());

	private ReflectionEngine reflectionEngine;

	public static final String DIR_COMMAND_NAME = "d";
	public static final String DIR_FIELDS_SUBCOMMAND_NAME = "f";
	public static final String DIR_METHODS_SUBCOMMAND_NAME = "m";
	public static final String DIR_STATIC_SUBCOMMAND_NAME = "s";
	public static final String DIR_JVMVIEW_SUBCOMMAND_NAME = "v";

	public DirCommand() {
		this.commandName = DIR_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String subCommand = safeReadLine(reader);

		boolean unknownSubCommand = false;
		String param = reader.readLine();
        String returnCommand;
		try {
			final String[] names;
			if (subCommand.equals(DIR_FIELDS_SUBCOMMAND_NAME)) {
				Object targetObject = gateway.getObject(param);
				names = reflectionEngine.getPublicFieldNames(targetObject);
			} else if (subCommand.equals(DIR_METHODS_SUBCOMMAND_NAME)) {
				Object targetObject = gateway.getObject(param);
				names = reflectionEngine.getPublicMethodNames(targetObject);
			} else if (subCommand.equals(DIR_STATIC_SUBCOMMAND_NAME)) {
				Class<?> clazz = TypeUtil.forName(param);
				names = reflectionEngine.getPublicStaticNames(clazz);
			} else if (subCommand.equals(DIR_JVMVIEW_SUBCOMMAND_NAME)) {
				names = getJvmViewNames(param, reader);
			} else {
				names = null;
				unknownSubCommand = true;
			}

			// Read and discard end of command
			reader.readLine();

			if (unknownSubCommand) {
				returnCommand = Protocol.getOutputErrorCommand("Unknown Array SubCommand Name: " + subCommand);
			} else if (names == null) {
				ReturnObject returnObject = gateway.getReturnObject(null);
				returnCommand = Protocol.getOutputCommand(returnObject);
			} else {
				StringBuilder namesJoinedBuilder = new StringBuilder();
				for (String name : names) {
					namesJoinedBuilder.append(name);
					namesJoinedBuilder.append("\n");
				}
				final String namesJoined;
				if (namesJoinedBuilder.length() > 0) {
					namesJoined = namesJoinedBuilder.substring(0, namesJoinedBuilder.length() - 1);
				} else {
					namesJoined = "";
				}

				ReturnObject returnObject = gateway.getReturnObject(namesJoined);
				returnCommand = Protocol.getOutputCommand(returnObject);
			}
		} catch (Exception e) {
			logger.log(Level.FINEST, "Error in a dir subcommand", e);
			returnCommand = Protocol.getOutputErrorCommand();
		}

		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	private String[] getJvmViewNames(String jvmId, BufferedReader reader) throws IOException {
		String lastSequenceIdString = (String) Protocol.getObject(reader.readLine(), gateway);
		final int lastSequenceId;
		if (lastSequenceIdString == null) {
			lastSequenceId = 0;
		} else {
			lastSequenceId = Integer.parseInt(lastSequenceIdString);
		}

		JVMView view = (JVMView) Protocol.getObject(jvmId, gateway);
		int sequenceId = view.getSequenceId();
		if (lastSequenceId == sequenceId) {
			return null;
		}

		String[] importedNames = view.getImportedNames();
		String[] returnValue = new String[importedNames.length + 1];
		returnValue[0] = Integer.toString(sequenceId);
		System.arraycopy(importedNames, 0, returnValue, 1, importedNames.length);
		return returnValue;
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		super.init(gateway, connection);
		reflectionEngine = gateway.getReflectionEngine();
	}

}
