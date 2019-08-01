/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import static py4Java.NetworkUtil.safeReadLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import py4Java.Gateway;
import py4Java.JVMView;
import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.Py4JServerConnection;
import py4Java.ReturnObject;
import py4Java.reflection.ReflectionEngine;
import py4Java.reflection.TypeUtil;

/**
 * <p>
 * The ReflectionCommand is responsible for accessing packages, classes, and
 * static members. This is the command invoked when using the jvm property of a
 * JavaGateway on the Python side.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class ReflectionCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(ReflectionCommand.class.getName());

	public final static char GET_UNKNOWN_SUB_COMMAND_NAME = 'u';

	public final static char GET_MEMBER_SUB_COMMAND_NAME = 'm';

	public final static char GET_JAVA_LANG_CLASS_SUB_COMMAND_NAME = 'c';

	public static final String REFLECTION_COMMAND_NAME = "r";

	protected ReflectionEngine rEngine;

	public ReflectionCommand() {
		super();
		this.commandName = REFLECTION_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		char subCommand = safeReadLine(reader).charAt(0);
		String returnCommand = null;

		if (subCommand == GET_UNKNOWN_SUB_COMMAND_NAME) {
			returnCommand = getUnknownMember(reader);
		} else if (subCommand == GET_JAVA_LANG_CLASS_SUB_COMMAND_NAME) {
			returnCommand = getJavaLangClass(reader);
		} else {
			returnCommand = getMember(reader);
		}

		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	private String getJavaLangClass(BufferedReader reader) throws IOException {
		String fqn = reader.readLine();
		reader.readLine();
		String returnCommand = null;
		try {
			Class<?> clazz = TypeUtil.forName(fqn);
			ReturnObject rObject = gateway.getReturnObject(clazz);
			returnCommand = Protocol.getOutputCommand(rObject);
		} catch (ClassNotFoundException ce) {
			returnCommand = Protocol.getOutputErrorCommand("The class " + fqn + " does not exist.");
		} catch (Exception e) {
			returnCommand = Protocol.getOutputErrorCommand();
		}

		return returnCommand;
	}

	/**
	 * 1- Try fields. 2- If no static field, try methods. 3- If method and
	 * static, return method. 4- If method and not static, then class is
	 * impossible so return exception. 5- If no method, try class.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private String getMember(BufferedReader reader) throws IOException {
		String fqn = reader.readLine();
		String member = reader.readLine();
		reader.readLine();
		String returnCommand = null;
		try {
			Class<?> clazz = TypeUtil.forName(fqn);
			Field f = rEngine.getField(clazz, member);
			if (f != null && Modifier.isStatic(f.getModifiers())) {
				Object obj = rEngine.getFieldValue(null, f);
				ReturnObject rObject = gateway.getReturnObject(obj);
				returnCommand = Protocol.getOutputCommand(rObject);
			}

			if (returnCommand == null) {
				Method m = rEngine.getMethod(clazz, member);
				if (m != null) {
					if (Modifier.isStatic(m.getModifiers())) {
						returnCommand = Protocol.getMemberOutputCommand(Protocol.METHOD_TYPE);
					} else {
						returnCommand = Protocol
								.getOutputErrorCommand("Trying to access a non-static member from a static context.");
					}
				}
			}

			if (returnCommand == null) {
				Class<?> c = rEngine.getClass(clazz, member);
				if (c != null) {
					returnCommand = Protocol.getMemberOutputCommand(Protocol.CLASS_TYPE);
				} else {
					returnCommand = Protocol.getOutputErrorCommand();
				}
			}
		} catch (Exception e) {
			returnCommand = Protocol.getOutputErrorCommand();
		}

		return returnCommand;
	}

	private String getUnknownMember(BufferedReader reader) throws IOException {
		String fqn = reader.readLine();
		String jvmId = reader.readLine();
		JVMView view = (JVMView) Protocol.getObject(jvmId, this.gateway);
		reader.readLine();
		String returnCommand = null;
		try {
			// TODO APPEND CLASS NAME, because it might not be the fqn, but a
			// new one because of imports!
			String fullyQualifiedName = TypeUtil.forName(fqn, view).getName();
			returnCommand = Protocol.getMemberOutputCommand(Protocol.CLASS_TYPE, fullyQualifiedName);
		} catch (ClassNotFoundException e) {
			returnCommand = Protocol.getMemberOutputCommand(Protocol.PACKAGE_TYPE);
		} catch (Exception e) {
			returnCommand = Protocol.getOutputErrorCommand(e);
		}
		return returnCommand;
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		super.init(gateway, connection);
		rEngine = gateway.getReflectionEngine();
	}

}
