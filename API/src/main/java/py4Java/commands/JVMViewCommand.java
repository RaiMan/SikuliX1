/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import py4Java.*;
import py4Java.reflection.ReflectionEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static py4Java.NetworkUtil.safeReadLine;

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
        writer.write(Objects.requireNonNull(returnCommand));
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
        boolean removed;
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
