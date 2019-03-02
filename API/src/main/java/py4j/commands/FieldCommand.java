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
package py4j.commands;

import static py4j.NetworkUtil.safeReadLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import py4j.Gateway;
import py4j.Protocol;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.ReturnObject;
import py4j.reflection.ReflectionEngine;

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
