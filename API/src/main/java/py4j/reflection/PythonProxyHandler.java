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
package py4j.reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import py4j.Gateway;
import py4j.Protocol;
import py4j.Py4JException;

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
		Object convertedOutput = convertOutput(method, output);
		return convertedOutput;
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
