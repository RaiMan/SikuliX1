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
package py4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import py4j.reflection.ReflectionUtil;

/**
 * <p>
 * This class defines the protocol used to communicate between two virtual
 * machines (e.g., Python and Java).
 * </p>
 * <p>
 * Currently, the protocol requires type information (e.g., is this string an
 * integer, an object reference or a boolean?) to be embedded with each command
 * part. The rational is that the source virtual machine is usually better at
 * determining the type of objects it sends.
 * </p>
 * <p>
 * An input command is usually composed of:
 * </p>
 * <ul>
 * <li>A command name (e.g., c for call)</li>
 * <li>Optionally, a sub command name (e.g., 'a' for concatenate in the list
 * command)</li>
 * <li>A list of command parts (e.g., the name of a method, the value of a
 * parameter, etc.)</li>
 * <li>The End of Command marker (e)</li>
 * </ul>
 *
 * <p>
 * The various parts of a command are separated by \n characters. These
 * characters are automatically escaped and unescaped in Strings on both sides
 * (Java and Python).
 * </p>
 *
 * <p>
 * An output command is usually composed of:
 * </p>
 * <ul>
 * <li>A success or error code (y for yes, x for exception)</li>
 * <li>A return value (e.g., n for null, v for void, or any other value like a
 * String)</li>
 * </ul>
 *
 * <p>
 * This class should be used only if the user creates new commands.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class Protocol {

	// TYPES
	public final static char BYTES_TYPE = 'j';
	public final static char INTEGER_TYPE = 'i';
	public final static char LONG_TYPE = 'L';
	public final static char BOOLEAN_TYPE = 'b';
	public final static char DOUBLE_TYPE = 'd';
	public final static char DECIMAL_TYPE = 'D';
	public final static char STRING_TYPE = 's';
	public final static char REFERENCE_TYPE = 'r';
	public final static char LIST_TYPE = 'l';
	public final static char SET_TYPE = 'h';
	public final static char ARRAY_TYPE = 't';
	public final static char MAP_TYPE = 'a';
	public final static char ITERATOR_TYPE = 'g';
	public final static char NULL_TYPE = 'n';
	public final static char PYTHON_PROXY_TYPE = 'f';

	public final static char PACKAGE_TYPE = 'p';
	public final static char CLASS_TYPE = 'c';
	public final static char METHOD_TYPE = 'm';
	public final static char NO_MEMBER = 'o';
	public final static char VOID = 'v';

	public final static char RETURN_MESSAGE = '!';

	// END OF COMMAND MARKER
	public final static char END = 'e';
	public final static char END_OUTPUT = '\n';

	// OUTPUT VALUES
	public final static char ERROR = 'x';
	public final static char FATAL_ERROR = 'z';
	public final static char SUCCESS = 'y';

	// COMMON COMMAND NAME
	public final static String AUTH_COMMAND_NAME = "A";

	// SHORTCUT
	public final static String ERROR_COMMAND = "" + RETURN_MESSAGE + ERROR + END_OUTPUT;
	public final static String VOID_COMMAND = "" + RETURN_MESSAGE + SUCCESS + VOID + END_OUTPUT;
	public final static String NO_SUCH_FIELD = "" + RETURN_MESSAGE + SUCCESS + NO_MEMBER + END_OUTPUT;

	// ENTRY POINT
	public final static String ENTRY_POINT_OBJECT_ID = "t";

	// DEFAULT JVM VIEW
	public final static String DEFAULT_JVM_OBJECT_ID = "j";

	// GATEWAY SERVER
	public final static String GATEWAY_SERVER_ID = "GATEWAY_SERVER";

	// STATIC REFERENCES
	public final static String STATIC_PREFIX = "z:";

	// PYTHON CONSTANTS
	public final static String PYTHON_NAN = "nan";
	public final static String PYTHON_INFINITY = "inf";
	public final static String PYTHON_NEGATIVE_INFINITY = "-inf";

	/**
	 * <p>
	 * Transform the byte array into Base64 characters.
	 * </p>
	 *
	 * @param bytes
	 * @return
	 */
	public static String encodeBytes(byte[] bytes) {
		return Base64.encodeToString(bytes, false);
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The boolean value corresponding to this command part.
	 */
	public final static boolean getBoolean(String commandPart) {
		return Boolean.parseBoolean(commandPart.substring(1, commandPart.length()));
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The byte array corresponding to this command part.
	 */
	public final static byte[] getBytes(String commandPart) {
		return Base64.decode(commandPart.substring(1));
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The decimal value corresponding to this command part.
	 */
	public final static BigDecimal getDecimal(String commandPart) {
		return new BigDecimal(commandPart.substring(1, commandPart.length()));
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The double value corresponding to this command part.
	 */
	public final static double getDouble(String commandPart) {
		String doubleValue = commandPart.substring(1, commandPart.length());
		try {
			return Double.parseDouble(doubleValue);
		} catch (NumberFormatException e) {
			if (doubleValue.equals(PYTHON_INFINITY)) {
				return Double.POSITIVE_INFINITY;
			} else if (doubleValue.equals(PYTHON_NEGATIVE_INFINITY)) {
				return Double.NEGATIVE_INFINITY;
			} else if (doubleValue.equals(PYTHON_NAN)) {
				return Double.NaN;
			} else {
				throw e;
			}
		}
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The integer value corresponding to this command part.
	 */
	public final static int getInteger(String commandPart) {
		return Integer.parseInt(commandPart.substring(1, commandPart.length()));
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The long value corresponding to this command part.
	 */
	public final static long getLong(String commandPart) {
		return Long.parseLong(commandPart.substring(1, commandPart.length()));
	}

	public final static String getMemberOutputCommand(char memberType) {
		StringBuilder builder = new StringBuilder();

		builder.append(RETURN_MESSAGE);
		builder.append(SUCCESS);
		builder.append(memberType);
		builder.append(END_OUTPUT);

		return builder.toString();
	}

	public final static String getMemberOutputCommand(char memberType, String fqn) {
		StringBuilder builder = new StringBuilder();

		builder.append(RETURN_MESSAGE);
		builder.append(SUCCESS);
		builder.append(memberType);
		builder.append(fqn);
		builder.append(END_OUTPUT);

		return builder.toString();
	}

	public static String getNoSuchFieldOutputCommand() {
		return NO_SUCH_FIELD;
	}

	/**
	 * <p>
	 * Method provided for consistency. Just returns null.
	 * </p>
	 *
	 * @param commandPart
	 * @return null.
	 */
	public final static Object getNull(String commandPart) {
		return null;
	}

	public final static Object getObject(String commandPart, Gateway gateway) {
		if (isEmpty(commandPart) || isEnd(commandPart)) {
			throw new Py4JException("Command Part is Empty or is the End of Command Part");
		} else {
			switch (commandPart.charAt(0)) {
			case BOOLEAN_TYPE:
				return getBoolean(commandPart);
			case DOUBLE_TYPE:
				return getDouble(commandPart);
			case LONG_TYPE:
				return getLong(commandPart);
			case INTEGER_TYPE:
				try {
					return getInteger(commandPart);
				} catch (NumberFormatException e) {
					return getLong(commandPart);
				}
			case BYTES_TYPE:
				return getBytes(commandPart);
			case NULL_TYPE:
				return getNull(commandPart);
			case VOID:
				return getNull(commandPart);
			case REFERENCE_TYPE:
				return getReference(commandPart, gateway);
			case STRING_TYPE:
				return getString(commandPart);
			case DECIMAL_TYPE:
				return getDecimal(commandPart);
			case PYTHON_PROXY_TYPE:
				return getPythonProxy(commandPart, gateway);
			default:
				throw new Py4JException("Command Part is unknown: " + commandPart);
			}
		}
	}

	public final static String getOutputCommand(ReturnObject rObject) {
		StringBuilder builder = new StringBuilder();

		// TODO Should be configurable
		// TODO ADD RETURN MESSAGE TO OTHER OUTPUT COMMAND
		builder.append(RETURN_MESSAGE);

		if (rObject.isError()) {
			builder.append(rObject.getCommandPart());
		} else {
			builder.append(SUCCESS);
			builder.append(rObject.getCommandPart());
		}
		builder.append(END_OUTPUT);

		return builder.toString();
	}

	public final static String getOutputErrorCommand() {
		return ERROR_COMMAND;
	}

	public final static String getOutputErrorCommand(String errorMessage) {
		StringBuilder builder = new StringBuilder();
		builder.append(RETURN_MESSAGE);
		builder.append(ERROR);
		builder.append(Protocol.STRING_TYPE);
		builder.append(StringUtil.escape(errorMessage));
		builder.append(END_OUTPUT);
		return builder.toString();
	}

	public final static String getOutputErrorCommand(Throwable throwable) {
		StringBuilder builder = new StringBuilder();
		builder.append(RETURN_MESSAGE);
		builder.append(ERROR);
		builder.append(Protocol.STRING_TYPE);
		builder.append(StringUtil.escape(getThrowableAsString(throwable)));
		builder.append(END_OUTPUT);
		return builder.toString();
	}

	public final static String getOutputFatalErrorCommand(Throwable throwable) {
		StringBuilder builder = new StringBuilder();
		builder.append(RETURN_MESSAGE);
		builder.append(FATAL_ERROR);
		builder.append(Protocol.STRING_TYPE);
		builder.append(StringUtil.escape(getThrowableAsString(throwable)));
		builder.append(END_OUTPUT);
		return builder.toString();
	}

	public final static String getOutputVoidCommand() {
		return VOID_COMMAND;
	}

	public final static String getAuthCommand(String authToken) {
		StringBuilder builder = new StringBuilder();
		builder.append(AUTH_COMMAND_NAME);
		builder.append("\n");
		builder.append(StringUtil.escape(authToken));
		builder.append("\n");
		builder.append(END);
		builder.append("\n");

		return builder.toString();
	}

	public static char getPrimitiveType(Object primitiveObject) {
		char c = INTEGER_TYPE;

		if (primitiveObject instanceof String || primitiveObject instanceof Character) {
			c = STRING_TYPE;
		} else if (primitiveObject instanceof Long) {
			c = LONG_TYPE;
		} else if (primitiveObject instanceof Double || primitiveObject instanceof Float) {
			c = DOUBLE_TYPE;
		} else if (primitiveObject instanceof Boolean) {
			c = BOOLEAN_TYPE;
		} else if (primitiveObject instanceof byte[]) {
			c = BYTES_TYPE;
		}

		return c;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return A Python proxy specified in this command part.
	 */
	public static Object getPythonProxy(String commandPart, Gateway gateway) {
		String proxyString = commandPart.substring(1, commandPart.length());
		String[] parts = proxyString.split(";");
		int length = parts.length;
		Class<?>[] interfaces = new Class<?>[length - 1];
		if (length < 2) {
			throw new Py4JException("Invalid Python Proxy.");
		}

		for (int i = 1; i < length; i++) {
			try {
				interfaces[i - 1] = ReflectionUtil.classForName(parts[i]);
				if (!interfaces[i - 1].isInterface()) {
					throw new Py4JException(
							"This class " + parts[i] + " is not an interface and cannot be used as a Python Proxy.");
				}
			} catch (ClassNotFoundException e) {
				throw new Py4JException("Invalid interface name: " + parts[i]);
			}
		}

		return gateway.createProxy(ReflectionUtil.getClassLoader(), interfaces, parts[0]);
	}

	/**
	 * <p>
	 *     Legacy method. Please use Gateway.createProxy.
	 * </p>
	 * @param classLoader
	 * @param interfacesToImplement
	 * @param objectId
	 * @param gateway
	 * @return
	 * @deprecated
	 */
	public static Object getPythonProxyHandler(ClassLoader classLoader, Class[] interfacesToImplement, String objectId,
			Gateway gateway) {
		return gateway.createProxy(classLoader, interfacesToImplement, objectId);
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The object referenced in this command part.
	 */
	public final static Object getReference(String commandPart, Gateway gateway) {
		String reference = commandPart.substring(1, commandPart.length());

		if (reference.trim().length() == 0) {
			throw new Py4JException("Reference is empty.");
		}

		return gateway.getObject(reference);
	}

	public final static Object getReturnValue(String returnMessage, Gateway gateway) throws Throwable {
		final Object result = getObject(returnMessage.substring(1), gateway);
		if (isError(returnMessage)) {
			if (result instanceof Throwable) {
				throw (Throwable) result;
			} else {
				throw new Py4JException("An exception was raised by the Python Proxy. Return Message: " + result);
			}
		} else {
			return result;
		}
	}

	public final static Throwable getRootThrowable(Throwable throwable, boolean skipInvocation) {
		Throwable child = throwable;
		if (!skipInvocation && child instanceof InvocationTargetException) {
			child = throwable.getCause();
			skipInvocation = true;
		} else if (child instanceof Py4JException || child instanceof Py4JNetworkException) {
			child = throwable.getCause();
		} else {
			return child;
		}

		if (child == null) {
			return throwable;
		} else {
			return getRootThrowable(child, skipInvocation);
		}
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return The reference contained in this command part.
	 */
	public final static String getString(String commandPart) {
		String toReturn = "";
		if (commandPart.length() >= 2) {
			toReturn = StringUtil.unescape(commandPart.substring(1, commandPart.length()));
		}
		return toReturn;
	}

	public final static String getThrowableAsString(Throwable throwable) {
		Throwable root = getRootThrowable(throwable, false);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		root.printStackTrace(printWriter);
		return stringWriter.toString();
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a boolean
	 */
	public final static boolean isBoolean(String commandPart) {
		return commandPart.charAt(0) == BOOLEAN_TYPE;
	}

	/**
	 *
	 * @param commandPart
	 * @return True if the command part is a return message
	 */
	public final static boolean isReturnMessage(String commandPart) {
		return commandPart != null && commandPart.length() > 1 && commandPart.charAt(0) == RETURN_MESSAGE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a byte array
	 */
	public final static boolean isBytes(String commandPart) {
		return commandPart.charAt(0) == BYTES_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a decimal
	 */
	public final static boolean isDecimal(String commandPart) {
		return commandPart.charAt(0) == DECIMAL_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a double
	 */
	public final static boolean isDouble(String commandPart) {
		return commandPart.charAt(0) == DOUBLE_TYPE;
	}

	public final static boolean isEmpty(String commandPart) {
		return commandPart == null || commandPart.trim().length() == 0;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is the end token
	 */
	public final static boolean isEnd(String commandPart) {
		return commandPart.length() == 1 && commandPart.charAt(0) == 'e';
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> null.
	 * </p>
	 *
	 * @param returnMessage
	 * @return True if the return message is an error
	 */
	public final static boolean isError(String returnMessage) {
		return returnMessage == null || returnMessage.length() == 0 || returnMessage.charAt(0) == ERROR;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is an integer
	 */
	public final static boolean isInteger(String commandPart) {
		return commandPart.charAt(0) == INTEGER_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a long
	 */
	public final static boolean isLong(String commandPart) {
		return commandPart.charAt(0) == LONG_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is null
	 */
	public final static boolean isNull(String commandPart) {
		return commandPart.charAt(0) == NULL_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a python proxy
	 */
	public final static boolean isPythonProxy(String commandPart) {
		return commandPart.charAt(0) == PYTHON_PROXY_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a reference
	 */
	public final static boolean isReference(String commandPart) {
		return commandPart.charAt(0) == REFERENCE_TYPE;
	}

	/**
	 * <p>
	 * Assumes that commandPart is <b>not</b> empty.
	 * </p>
	 *
	 * @param commandPart
	 * @return True if the command part is a reference
	 */
	public final static boolean isString(String commandPart) {
		return commandPart.charAt(0) == STRING_TYPE;
	}
}
