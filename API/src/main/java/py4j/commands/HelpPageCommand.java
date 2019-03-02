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
import java.util.logging.Logger;

import py4j.Protocol;
import py4j.Py4JException;
import py4j.ReturnObject;
import py4j.model.HelpPageGenerator;
import py4j.model.Py4JClass;
import py4j.reflection.ReflectionUtil;

/**
 * <p>
 * A HelpPageCommand is responsible for generating a help page for a Java object
 * or Java class. The help page typically list the signature of the members
 * declared in the object/class.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class HelpPageCommand extends AbstractCommand {
	private final Logger logger = Logger.getLogger(HelpPageCommand.class.getName());

	public final static String HELP_COMMAND_NAME = "h";

	public final static String HELP_OBJECT_SUB_COMMAND_NAME = "o";

	public final static String HELP_CLASS_SUB_COMMAND_NAME = "c";

	public HelpPageCommand() {
		super();
		this.commandName = HELP_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String returnCommand = null;
		String subCommand = safeReadLine(reader, false);

		if (subCommand.equals(HELP_OBJECT_SUB_COMMAND_NAME)) {
			returnCommand = getHelpObject(reader);
		} else if (subCommand.equals(HELP_CLASS_SUB_COMMAND_NAME)) {
			returnCommand = getHelpClass(reader);
		} else {
			returnCommand = Protocol.getOutputErrorCommand("Unknown Help SubCommand Name: " + subCommand);
		}
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	private String getHelpClass(BufferedReader reader) throws IOException {
		String className = reader.readLine();
		String pattern = (String) Protocol.getObject(reader.readLine(), this.gateway);
		String shortName = safeReadLine(reader, false);
		// EoC
		reader.readLine();
		String returnCommand;

		try {
			Py4JClass clazz = Py4JClass.buildClass(ReflectionUtil.classForName(className), true);
			boolean isShortName = Protocol.getBoolean(shortName);
			String helpPage = HelpPageGenerator.getHelpPage(clazz, pattern, isShortName);
			ReturnObject rObject = gateway.getReturnObject(helpPage);
			returnCommand = Protocol.getOutputCommand(rObject);
		} catch (Exception e) {
			returnCommand = Protocol.getOutputErrorCommand(e);
		}

		return returnCommand;
	}

	private String getHelpObject(BufferedReader reader) throws IOException {
		String objectId = reader.readLine();
		String pattern = (String) Protocol.getObject(reader.readLine(), this.gateway);
		String shortName = safeReadLine(reader, false);
		// EoC
		reader.readLine();
		String returnCommand;

		try {
			Object obj = gateway.getObject(objectId);
			Py4JClass clazz = Py4JClass.buildClass(obj.getClass(), true);
			boolean isShortName = Protocol.getBoolean(shortName);
			String helpPage = HelpPageGenerator.getHelpPage(clazz, pattern, isShortName);
			ReturnObject rObject = gateway.getReturnObject(helpPage);
			returnCommand = Protocol.getOutputCommand(rObject);
		} catch (Exception e) {
			returnCommand = Protocol.getOutputErrorCommand(e);
		}

		return returnCommand;
	}

}
