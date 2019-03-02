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

import py4j.Gateway;
import py4j.JVMView;
import py4j.Protocol;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.ReturnObject;
import py4j.StringUtil;
import py4j.reflection.ReflectionEngine;

/**
 * <p>
 * A JVMViewCommand is responsible for managing JVM views: creating views,
 * adding imports, searching for fully qualified names.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class JVMViewCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(JVMViewCommand.class.getName());

	public final static char CREATE_VIEW_SUB_COMMAND_NAME = 'c';

	public final static char IMPORT_SUB_COMMAND_NAME = 'i';

	public final static char REMOVE_IMPORT_SUB_COMMAND_NAME = 'r';

	public final static char SEARCH_SUB_COMMAND_NAME = 's';

	public static final String JVMVIEW_COMMAND_NAME = "j";

	protected ReflectionEngine rEngine;

	public JVMViewCommand() {
		super();
		this.commandName = JVMVIEW_COMMAND_NAME;
	}

	private String createJVMView(BufferedReader reader) throws IOException {
		String name = StringUtil.unescape(reader.readLine());
		reader.readLine();

		JVMView newView = new JVMView(name, null);
		ReturnObject rObject = gateway.getReturnObject(newView);
		newView.setId(rObject.getName());

		return Protocol.getOutputCommand(rObject);
	}

	private String doImport(BufferedReader reader) throws IOException {
		String jvmId = reader.readLine();
		String importString = StringUtil.unescape(reader.readLine());
		reader.readLine();

		JVMView view = (JVMView) Protocol.getObject(jvmId, gateway);
		if (importString.endsWith("*")) {
			view.addStarImport(importString);
		} else {
			view.addSingleImport(importString);
		}

		return Protocol.getOutputVoidCommand();
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		char subCommand = safeReadLine(reader).charAt(0);
		String returnCommand = null;

		if (subCommand == CREATE_VIEW_SUB_COMMAND_NAME) {
			returnCommand = createJVMView(reader);
		} else if (subCommand == IMPORT_SUB_COMMAND_NAME) {
			returnCommand = doImport(reader);
		} else if (subCommand == REMOVE_IMPORT_SUB_COMMAND_NAME) {
			returnCommand = removeImport(reader);
		} else if (subCommand == SEARCH_SUB_COMMAND_NAME) {
			returnCommand = search(reader);
		} else {
			returnCommand = Protocol.getOutputErrorCommand("Unknown JVM View SubCommand Name: " + subCommand);
		}
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		super.init(gateway, connection);
		rEngine = gateway.getReflectionEngine();
	}

	private String removeImport(BufferedReader reader) throws IOException {
		String jvmId = reader.readLine();
		String importString = StringUtil.unescape(reader.readLine());

		reader.readLine();

		JVMView view = (JVMView) Protocol.getObject(jvmId, gateway);
		boolean removed = false;
		if (importString.endsWith("*")) {
			removed = view.removeStarImport(importString);
		} else {
			removed = view.removeSingleImport(importString);
		}

		return Protocol.getOutputCommand(ReturnObject.getPrimitiveReturnObject(removed));
	}

	private String search(BufferedReader reader) {
		return null;
	}

}
