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
import py4Java.ReturnObject;
import py4Java.model.HelpPageGenerator;
import py4Java.model.Py4JClass;
import py4Java.reflection.ReflectionUtil;

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
