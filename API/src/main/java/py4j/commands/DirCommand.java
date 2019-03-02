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
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.Gateway;
import py4j.JVMView;
import py4j.Protocol;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.ReturnObject;
import py4j.reflection.ReflectionEngine;
import py4j.reflection.TypeUtil;

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
		String returnCommand = null;
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
