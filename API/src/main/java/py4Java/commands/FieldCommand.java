/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import static py4Java.NetworkUtil.safeReadLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import py4Java.Gateway;
import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.Py4JServerConnection;
import py4Java.ReturnObject;
import py4Java.reflection.ReflectionEngine;

/**
 * <p>
 * A FieldCommand is responsible for accessing and setting fields of objects.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class FieldCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(FieldCommand.class.getName());

	public final static String FIELD_COMMAND_NAME = "f";

	public final static String FIELD_GET_SUB_COMMAND_NAME = "g";

	public final static String FIELD_SET_SUB_COMMAND_NAME = "s";

	private ReflectionEngine reflectionEngine;

	public FieldCommand() {
		super();
		this.commandName = FIELD_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String returnCommand = null;
		String subCommand = safeReadLine(reader, false);

		if (subCommand.equals(FIELD_GET_SUB_COMMAND_NAME)) {
			returnCommand = getField(reader);
		} else if (subCommand.equals(FIELD_SET_SUB_COMMAND_NAME)) {
			returnCommand = setField(reader);
		} else {
			returnCommand = Protocol.getOutputErrorCommand("Unknown Field SubCommand Name: " + subCommand);
		}
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	private String getField(BufferedReader reader) throws IOException {
		String targetObjectId = reader.readLine();
		String fieldName = reader.readLine();
		reader.readLine(); // read EndOfCommand.

		Object object = gateway.getObject(targetObjectId);
		Field field = reflectionEngine.getField(object, fieldName);
		logger.finer("Getting field " + fieldName);
		String returnCommand = null;
		if (field == null) {
			returnCommand = Protocol.getNoSuchFieldOutputCommand();
		} else {
			Object fieldObject = reflectionEngine.getFieldValue(object, field);
			ReturnObject rObject = gateway.getReturnObject(fieldObject);
			returnCommand = Protocol.getOutputCommand(rObject);
		}
		return returnCommand;
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		super.init(gateway, connection);
		reflectionEngine = gateway.getReflectionEngine();
	}

	private String setField(BufferedReader reader) throws IOException {
		String targetObjectId = reader.readLine();
		String fieldName = reader.readLine();
		String value = reader.readLine();

		reader.readLine(); // read EndOfCommand;

		Object valueObject = Protocol.getObject(value, this.gateway);
		Object object = gateway.getObject(targetObjectId);
		Field field = reflectionEngine.getField(object, fieldName);
		logger.finer("Setting field " + fieldName);
		String returnCommand = null;
		if (field == null) {
			returnCommand = Protocol.getNoSuchFieldOutputCommand();
		} else {
			reflectionEngine.setFieldValue(object, field, valueObject);
			returnCommand = Protocol.getOutputVoidCommand();
		}
		return returnCommand;
	}

}
