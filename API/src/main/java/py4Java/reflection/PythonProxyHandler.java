/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

import py4Java.Gateway;
import py4Java.Protocol;
import py4Java.Py4JException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 * A PythonProxyHandler is used in place of a Python object. Python programs can
 * send Python objects that implements a Java interface to the JVM: these Python
 * objects are represented by dynamic proxies with a PythonProxyHandler.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class PythonProxyHandler implements InvocationHandler {

	private final String id;

	private final Gateway gateway;

	private final Logger logger = Logger.getLogger(PythonProxyHandler.class.getName());

	private final String finalizeCommand;

	public final static String CALL_PROXY_COMMAND_NAME = "c\n";

	public final static String GARBAGE_COLLECT_PROXY_COMMAND_NAME = "g\n";

	public PythonProxyHandler(String id, Gateway gateway) {
		super();
		this.id = id;
		this.gateway = gateway;
		this.finalizeCommand = GARBAGE_COLLECT_PROXY_COMMAND_NAME + id + "\ne\n";
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (gateway.getCallbackClient().isMemoryManagementEnabled() && this.id != Protocol.ENTRY_POINT_OBJECT_ID) {
				logger.fine("Finalizing python proxy id " + this.id);
				gateway.getCallbackClient().sendCommand(finalizeCommand, false);
			}
		} catch (Exception e) {
			logger.warning("Python Proxy ID could not send a finalize message: " + this.id);
		} finally {
			super.finalize();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		logger.fine("Method " + method.getName() + " called on Python object " + id);
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(CALL_PROXY_COMMAND_NAME);
		sBuilder.append(id);
		sBuilder.append("\n");
		sBuilder.append(method.getName());
		sBuilder.append("\n");

		if (args != null) {
			for (Object arg : args) {
				sBuilder.append(gateway.getReturnObject(arg).getCommandPart());
				sBuilder.append("\n");
			}
		}

		sBuilder.append("e\n");

		String returnCommand = gateway.getCallbackClient().sendCommand(sBuilder.toString());

		Object output = Protocol.getReturnValue(returnCommand, gateway);
        return convertOutput(method, output);
	}

	private Object convertOutput(Method method, Object output) {
		Class<?> returnType = method.getReturnType();
		// If output is None/null or expected return type is 
		// Void then return output with no conversion
		if (output == null || returnType.equals(Void.TYPE)) {
			// Do not convert void
			return output;
		}
		Class<?> outputType = output.getClass();
		Class<?>[] parameters = { returnType };
		Class<?>[] arguments = { outputType };
		List<TypeConverter> converters = new ArrayList<TypeConverter>();
		int cost = MethodInvoker.buildConverters(converters, parameters, arguments);
		if (cost == -1) {
			// This will be wrapped into Py4JJavaException if the Java code is being called by Python.
			throw new Py4JException(
					"Incompatible output type. Expected: " + returnType.getName() + " Actual: " + outputType.getName());
		}
		return converters.get(0).convert(output);
	}

}
