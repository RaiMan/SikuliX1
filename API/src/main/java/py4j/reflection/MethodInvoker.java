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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.Py4JException;
import py4j.Py4JJavaException;

/**
 * <p>
 * A MethodInvoker translates a call made in a Python Program into a call to a
 * Java method.
 * </p>
 * <p>
 * A MethodInvoker is tailored to a particular set of actual parameters and
 * indicates how far the calling context is from the method signature.
 * </p>
 * <p>
 * For example, a call to method1(String) from Python can be translated to a
 * call to method1(char) in Java, with a cost of 1.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class MethodInvoker {

	public final static int INVALID_INVOKER_COST = -1;

	public final static int MAX_DISTANCE = 100000000;

	private static boolean allNoConverter(List<TypeConverter> converters) {
		boolean allNo = true;

		for (TypeConverter converter : converters) {
			if (converter != TypeConverter.NO_CONVERTER) {
				allNo = false;
				break;
			}
		}

		return allNo;
	}

	/**
	 * <p>Builds a list of converters used to convert the arguments into the parameters.</p>
	 * @param converters
	 * @param parameters
	 * @param arguments
	 * @return
	 */
	public static int buildConverters(List<TypeConverter> converters, Class<?>[] parameters, Class<?>[] arguments) {
		int cost = 0;
		int tempCost = -1;
		int size = arguments.length;
		for (int i = 0; i < size; i++) {
			if (arguments[i] == null) {
				if (parameters[i].isPrimitive()) {
					tempCost = -1;
				} else {
					int distance = TypeUtil.computeDistance(Object.class, parameters[i]);
					tempCost = Math.abs(MAX_DISTANCE - distance);
					converters.add(TypeConverter.NO_CONVERTER);
				}
			} else if (parameters[i].isAssignableFrom(arguments[i])) {
				tempCost = TypeUtil.computeDistance(parameters[i], arguments[i]);
				converters.add(TypeConverter.NO_CONVERTER);
			} else if (TypeUtil.isNumeric(parameters[i]) && TypeUtil.isNumeric(arguments[i])) {
				tempCost = TypeUtil.computeNumericConversion(parameters[i], arguments[i], converters);
			} else if (TypeUtil.isCharacter(parameters[i])) {
				tempCost = TypeUtil.computeCharacterConversion(parameters[i], arguments[i], converters);
			} else if (TypeUtil.isBoolean(parameters[i]) && TypeUtil.isBoolean(arguments[i])) {
				tempCost = 0;
				converters.add(TypeConverter.NO_CONVERTER);
			}

			if (tempCost != -1) {
				cost += tempCost;
				tempCost = -1;
			} else {
				cost = -1;
				break;
			}
		}
		return cost;
	}

	public static MethodInvoker buildInvoker(Constructor<?> constructor, Class<?>[] arguments) {
		MethodInvoker invoker = null;
		int size = 0;
		int cost = 0;

		if (arguments != null) {
			size = arguments.length;
		}

		List<TypeConverter> converters = new ArrayList<TypeConverter>();
		if (arguments != null && size > 0) {
			cost = buildConverters(converters, constructor.getParameterTypes(), arguments);
		}
		if (cost == -1) {
			invoker = INVALID_INVOKER;
		} else {
			TypeConverter[] convertersArray = null;
			if (!allNoConverter(converters)) {
				convertersArray = converters.toArray(new TypeConverter[0]);
			}
			invoker = new MethodInvoker(constructor, convertersArray, cost);
		}

		return invoker;
	}

	public static MethodInvoker buildInvoker(Method method, Class<?>[] arguments) {
		MethodInvoker invoker = null;
		int size = 0;
		int cost = 0;

		if (arguments != null) {
			size = arguments.length;
		}

		List<TypeConverter> converters = new ArrayList<TypeConverter>();
		if (arguments != null && size > 0) {
			cost = buildConverters(converters, method.getParameterTypes(), arguments);
		}
		if (cost == -1) {
			invoker = INVALID_INVOKER;
		} else {
			TypeConverter[] convertersArray = null;
			if (!allNoConverter(converters)) {
				convertersArray = converters.toArray(new TypeConverter[0]);
			}
			invoker = new MethodInvoker(method, convertersArray, cost);
		}

		return invoker;
	}

	private int cost;

	private List<TypeConverter> converters;

	private Method method;

	private Constructor<?> constructor;

	private final Logger logger = Logger.getLogger(MethodInvoker.class.getName());

	public static final MethodInvoker INVALID_INVOKER = new MethodInvoker((Method) null, null, INVALID_INVOKER_COST);

	public MethodInvoker(Constructor<?> constructor, TypeConverter[] converters, int cost) {
		super();
		this.constructor = constructor;
		if (converters != null) {
			this.converters = Collections.unmodifiableList(Arrays.asList(converters));
		}
		this.cost = cost;
	}

	public MethodInvoker(Method method, TypeConverter[] converters, int cost) {
		super();
		this.method = method;
		if (converters != null) {
			this.converters = Collections.unmodifiableList(Arrays.asList(converters));
		}
		this.cost = cost;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public List<TypeConverter> getConverters() {
		return converters;
	}

	public int getCost() {
		return cost;
	}

	public Method getMethod() {
		return method;
	}

	public Object invoke(Object obj, Object[] arguments) {
		Object returnObject = null;

		try {
			Object[] newArguments = arguments;

			if (converters != null) {
				int size = arguments.length;
				newArguments = new Object[size];
				for (int i = 0; i < size; i++) {
					newArguments[i] = converters.get(i).convert(arguments[i]);
				}
			}
			if (method != null) {
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						method.setAccessible(true);
						return null;
					}
				});
				returnObject = method.invoke(obj, newArguments);
			} else if (constructor != null) {
				constructor.setAccessible(true);
				returnObject = constructor.newInstance(newArguments);
			}
		} catch (InvocationTargetException ie) {
			logger.log(Level.WARNING, "Exception occurred in client code.", ie);
			throw new Py4JJavaException(ie.getCause());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Could not invoke method or received an exception while invoking.", e);
			throw new Py4JException(e);
		}

		return returnObject;
	}

	public boolean isVoid() {
		if (constructor != null) {
			return false;
		} else if (method != null) {
			return method.getReturnType().equals(void.class);
		} else {
			throw new Py4JException("Null method or constructor");
		}
	}

}
