/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import py4Java.Protocol;
import py4Java.Py4JException;
import py4Java.ReturnObject;
import py4Java.reflection.MethodInvoker;
import py4Java.reflection.TypeConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static py4Java.NetworkUtil.safeReadLine;

/**
 * <p>
 * A ArrayCommand is responsible for handling operations on arrays.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class ArrayCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(ArrayCommand.class.getName());

	public static final String ARRAY_COMMAND_NAME = "a";

	public static final char ARRAY_GET_SUB_COMMAND_NAME = 'g';
	public static final char ARRAY_SET_SUB_COMMAND_NAME = 's';
	public static final char ARRAY_SLICE_SUB_COMMAND_NAME = 'l';
	public static final char ARRAY_LEN_SUB_COMMAND_NAME = 'e';
	public static final char ARRAY_CREATE_SUB_COMMAND_NAME = 'c';

	public static final String RETURN_VOID = Protocol.RETURN_MESSAGE + "" + Protocol.SUCCESS + "" + Protocol.VOID
			+ Protocol.END_OUTPUT;

	public ArrayCommand() {
		super();
		this.commandName = ARRAY_COMMAND_NAME;
	}

	private String createArray(BufferedReader reader) throws IOException {
		String fqn = (String) Protocol.getObject(reader.readLine(), gateway);
		List<Object> dimensions = getArguments(reader);
		int size = dimensions.size();
		int[] dimensionsInt = new int[size];
		for (int i = 0; i < size; i++) {
			dimensionsInt[i] = (Integer) dimensions.get(i);
		}
		Object newArray = gateway.getReflectionEngine().createArray(fqn, dimensionsInt);
		ReturnObject returnObject = gateway.getReturnObject(newArray);
		return Protocol.getOutputCommand(returnObject);
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		char subCommand = safeReadLine(reader).charAt(0);
		String returnCommand = null;
		if (subCommand == ARRAY_GET_SUB_COMMAND_NAME) {
			returnCommand = getArray(reader);
		} else if (subCommand == ARRAY_SET_SUB_COMMAND_NAME) {
			returnCommand = setArray(reader);
		} else if (subCommand == ARRAY_SLICE_SUB_COMMAND_NAME) {
			returnCommand = sliceArray(reader);
		} else if (subCommand == ARRAY_LEN_SUB_COMMAND_NAME) {
			returnCommand = lenArray(reader);
		} else if (subCommand == ARRAY_CREATE_SUB_COMMAND_NAME) {
			returnCommand = createArray(reader);
		} else {
			returnCommand = Protocol.getOutputErrorCommand("Unknown Array SubCommand Name: " + subCommand);
		}

		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();

	}

	private String getArray(BufferedReader reader) throws IOException {
		Object arrayObject = gateway.getObject(reader.readLine());
		int index = (Integer) Protocol.getObject(reader.readLine(), gateway);
		// Read end
		reader.readLine();

		Object getObject = Array.get(arrayObject, index);
		ReturnObject returnObject = gateway.getReturnObject(getObject);
		return Protocol.getOutputCommand(returnObject);
	}

	private String lenArray(BufferedReader reader) throws IOException {
		Object arrayObject = gateway.getObject(reader.readLine());

		// Read end
		reader.readLine();

		int length = Array.getLength(arrayObject);
		ReturnObject returnObject = gateway.getReturnObject(length);
		return Protocol.getOutputCommand(returnObject);
	}

	private String setArray(BufferedReader reader) throws IOException {
		Object arrayObject = gateway.getObject(reader.readLine());
		int index = (Integer) Protocol.getObject(reader.readLine(), gateway);
		Object objectToSet = Protocol.getObject(reader.readLine(), gateway);

		// Read end
		reader.readLine();

		Object convertedObject = convertArgument(arrayObject.getClass().getComponentType(), objectToSet);

		Array.set(arrayObject, index, convertedObject);
		return RETURN_VOID;
	}

	private Object convertArgument(Class<?> arrayClass, Object objectToSet) {
		Object newObject = null;
        List<TypeConverter> converters = new ArrayList<>();
		Class<?>[] parameterClasses = { arrayClass };
		Class<?>[] argumentClasses = { objectToSet.getClass() };
		int cost = MethodInvoker.buildConverters(converters, parameterClasses, argumentClasses);

		if (cost >= 0) {
			newObject = converters.get(0).convert(objectToSet);
		} else {
			throw new Py4JException("Cannot convert " + argumentClasses[0].getName() + " to " + arrayClass.getName());
		}

		return newObject;
	}

	private String sliceArray(BufferedReader reader) throws IOException {
		Object arrayObject = gateway.getObject(reader.readLine());
		List<Object> indices = getArguments(reader);
		int size = indices.size();
		Object newArray = gateway.getReflectionEngine().createArray(arrayObject.getClass().getComponentType().getName(),
				new int[] { size });
		for (int i = 0; i < size; i++) {
			int index = (Integer) indices.get(i);
			Array.set(newArray, i, Array.get(arrayObject, index));
		}
		ReturnObject returnObject = gateway.getReturnObject(newArray);
		return Protocol.getOutputCommand(returnObject);
	}
}
