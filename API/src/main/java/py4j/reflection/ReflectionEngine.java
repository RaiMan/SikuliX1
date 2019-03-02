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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.Py4JException;

/**
 * <p>
 * The reflection engine is responsible for accessing the classes, the instances
 * and members in a JVM.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class ReflectionEngine {

	public final static int cacheSize = 100;

	private final Logger logger = Logger.getLogger(ReflectionEngine.class.getName());

	public final static Object RETURN_VOID = new Object();

	private static ThreadLocal<LRUCache<MethodDescriptor, MethodInvoker>> cacheHolder = new ThreadLocal<LRUCache<MethodDescriptor, MethodInvoker>>() {

		@Override
		protected LRUCache<MethodDescriptor, MethodInvoker> initialValue() {
			return new LRUCache<MethodDescriptor, MethodInvoker>(cacheSize);
		}

	};

	public Object createArray(String fqn, int[] dimensions) {
		Class<?> clazz = null;
		Object returnObject = null;
		try {
			clazz = TypeUtil.forName(fqn);
			returnObject = Array.newInstance(clazz, dimensions);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Class FQN does not exist: " + fqn, e);
			throw new Py4JException(e);
		}
		return returnObject;
	}

	private MethodInvoker getBestConstructor(List<Constructor<?>> acceptableConstructors, Class<?>[] parameters) {
		MethodInvoker lowestCost = null;

		for (Constructor<?> constructor : acceptableConstructors) {
			MethodInvoker temp = MethodInvoker.buildInvoker(constructor, parameters);
			int cost = temp.getCost();
			if (cost == -1) {
				continue;
			} else if (cost == 0) {
				lowestCost = temp;
				break;
			} else if (lowestCost == null || cost < lowestCost.getCost()) {
				lowestCost = temp;
			}
		}

		return lowestCost;
	}

	private MethodInvoker getBestMethod(List<Method> acceptableMethods, Class<?>[] parameters) {
		MethodInvoker lowestCost = null;

		for (Method method : acceptableMethods) {
			MethodInvoker temp = MethodInvoker.buildInvoker(method, parameters);
			int cost = temp.getCost();
			if (cost == -1) {
				continue;
			} else if (cost == 0) {
				lowestCost = temp;
				break;
			} else if (lowestCost == null || cost < lowestCost.getCost()) {
				lowestCost = temp;
			}
		}

		return lowestCost;
	}

	public Class<?> getClass(Class<?> clazz, String name) {
		Class<?> memberClass = null;

		try {
			for (Class<?> tempClass : clazz.getClasses()) {
				if (tempClass.getSimpleName().equals(name)) {
					memberClass = tempClass;
					break;
				}
			}
		} catch (Exception e) {
			memberClass = null;
		}

		return memberClass;
	}

	private Class<?>[] getClassParameters(Object[] parameters) {
		int size = parameters.length;
		Class<?>[] classes = new Class<?>[size];

		for (int i = 0; i < size; i++) {
			if (parameters[i] == null) {
				classes[i] = null;
			} else {
				classes[i] = parameters[i].getClass();
			}
		}

		return classes;
	}

	public MethodInvoker getConstructor(Class<?> clazz, Class<?>[] parameters) {
		MethodDescriptor mDescriptor = new MethodDescriptor(clazz.getName(), clazz, parameters);
		MethodInvoker mInvoker = null;
		List<Constructor<?>> acceptableConstructors = null;
		LRUCache<MethodDescriptor, MethodInvoker> cache = cacheHolder.get();

		mInvoker = cache.get(mDescriptor);

		if (mInvoker == null) {
			acceptableConstructors = getConstructorsByLength(clazz, parameters.length);

			if (acceptableConstructors.size() == 1) {
				mInvoker = MethodInvoker.buildInvoker(acceptableConstructors.get(0), parameters);
			} else {
				mInvoker = getBestConstructor(acceptableConstructors, parameters);
			}

			if (mInvoker != null && mInvoker.getCost() != -1) {
				cache.put(mDescriptor, mInvoker);
			} else {
				String errorMessage = "Constructor " + clazz.getName() + "(" + Arrays.toString(parameters)
						+ ") does not exist";
				logger.log(Level.WARNING, errorMessage);
				throw new Py4JException(errorMessage);
			}
		}

		return mInvoker;
	}

	public MethodInvoker getConstructor(String classFQN, Object[] parameters) {
		Class<?> clazz = null;

		try {
			clazz = ReflectionUtil.classForName(classFQN);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Class FQN does not exist: " + classFQN, e);
			throw new Py4JException(e);
		}

		return getConstructor(clazz, getClassParameters(parameters));
	}

	private List<Constructor<?>> getConstructorsByLength(Class<?> clazz, int length) {
		List<Constructor<?>> methods = new ArrayList<Constructor<?>>();

		for (Constructor<?> constructor : clazz.getConstructors()) {
			if (constructor.getParameterTypes().length == length) {
				methods.add(constructor);
			}
		}

		return methods;
	}

	/**
	 * 
	 * @param clazz
	 * @param name
	 * @return The field or null if a field with this name does not exist in
	 *         this class or in its hierarchy.
	 */
	public Field getField(Class<?> clazz, String name) {
		Field field = null;

		try {
			field = clazz.getField(name);
			if (!Modifier.isPublic(field.getModifiers()) && !field.isAccessible()) {
				field = null;
			}
		} catch (NoSuchFieldException e) {
			field = null;
		} catch (Exception e) {
			field = null;
		}

		return field;
	}

	/**
	 * 
	 * @param obj
	 * @param name
	 * @return The field or null if a field with this name does not exist in the
	 *         class of this object or in its hierarchy.
	 */
	public Field getField(Object obj, String name) {
		return getField(obj.getClass(), name);
	}

	public Field getField(String classFQN, String name) {
		Class<?> clazz = null;

		try {
			clazz = ReflectionUtil.classForName(classFQN);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Class FQN does not exist: " + classFQN, e);
			throw new Py4JException(e);
		}

		return getField(clazz, name);

	}

	/**
	 * <p>
	 * Wrapper around Field.get
	 * </p>
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public Object getFieldValue(Object obj, Field field) {
		Object fieldValue = null;

		try {
			fieldValue = field.get(obj);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while fetching field value of " + field, e);
			throw new Py4JException(e);
		}
		return fieldValue;
	}

	public Method getMethod(Class<?> clazz, String name) {
		Method m = null;
		try {
			for (Method tempMethod : clazz.getMethods()) {
				if (tempMethod.getName().equals(name)) {
					m = tempMethod;
					break;
				}
			}
		} catch (Exception e) {
			m = null;
		}
		return m;
	}

	public MethodInvoker getMethod(Class<?> clazz, String name, Class<?>[] parameters) {
		MethodDescriptor mDescriptor = new MethodDescriptor(name, clazz, parameters);
		MethodInvoker mInvoker = null;
		List<Method> acceptableMethods = null;
		LRUCache<MethodDescriptor, MethodInvoker> cache = cacheHolder.get();

		mInvoker = cache.get(mDescriptor);

		if (mInvoker == null) {
			acceptableMethods = getMethodsByNameAndLength(clazz, name, parameters.length);

			if (acceptableMethods.size() == 1) {
				mInvoker = MethodInvoker.buildInvoker(acceptableMethods.get(0), parameters);
			} else {
				mInvoker = getBestMethod(acceptableMethods, parameters);
			}

			if (mInvoker != null && mInvoker.getCost() != -1) {
				cache.put(mDescriptor, mInvoker);
			} else {
				String errorMessage = "Method " + name + "(" + Arrays.toString(parameters) + ") does not exist";
				logger.log(Level.WARNING, errorMessage);
				throw new Py4JException(errorMessage);
			}
		}

		return mInvoker;
	}

	public MethodInvoker getMethod(Object object, String name, Object[] parameters) {
		return getMethod(object.getClass(), name, getClassParameters(parameters));
	}

	public MethodInvoker getMethod(String classFQN, String name, Object[] parameters) {
		Class<?> clazz = null;

		try {
			clazz = ReflectionUtil.classForName(classFQN);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Class FQN does not exist: " + classFQN, e);
			throw new Py4JException(e);
		}

		return getMethod(clazz, name, getClassParameters(parameters));
	}

	private List<Method> getMethodsByNameAndLength(Class<?> clazz, String name, int length) {
		List<Method> methods = new ArrayList<Method>();

		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(name) && method.getParameterTypes().length == length) {
				methods.add(method);
			}
		}

		return methods;
	}

	public Object invoke(Object object, MethodInvoker invoker, Object[] parameters) {
		Object returnObject = null;

		returnObject = invoker.invoke(object, parameters);
		if (invoker.isVoid()) {
			returnObject = RETURN_VOID;
		}

		return returnObject;
	}

	/**
	 * <p>
	 * Wrapper around Field.set
	 * </p>
	 * 
	 * @param obj
	 * @param field
	 * @param value
	 */
	public void setFieldValue(Object obj, Field field, Object value) {
		try {
			field.set(obj, value);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while setting field value of " + field, e);
			throw new Py4JException(e);
		}
	}

	/**
	 * Retrieve the names of all the public methods in the obj
	 * @param obj the object to inspect
	 * @return list of all the names of public methods in obj
	 */
	public String[] getPublicMethodNames(Object obj) {
		Method[] methods = obj.getClass().getMethods();
		Set<String> methodNames = new HashSet<String>();
		for (Method method : methods) {
			if (Modifier.isPublic(method.getModifiers())) {
				methodNames.add(method.getName());
			}
		}
		return (String[]) methodNames.toArray(new String[methodNames.size()]);
	}

	/**
	 * Retrieve the names of all the public fields in the obj
	 * @param obj the object to inspect
	 * @return list of all the names of public fields in obj
	 */
	public String[] getPublicFieldNames(Object obj) {
		Field[] fields = obj.getClass().getFields();
		Set<String> fieldNames = new HashSet<String>();
		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers())) {
				fieldNames.add(field.getName());
			}
		}
		return (String[]) fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * Retrieve the names of all the public static fields in the clazz
	 *
	 * @param clazz
	 *            the object to inspect
	 * @return list of all the names of public statics
	 */
	public String[] getPublicStaticFieldNames(Class<?> clazz) {
		Field[] fields = clazz.getFields();
		Set<String> fieldNames = new HashSet<String>();
		for (Field field : fields) {
			if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
				fieldNames.add(field.getName());
			}
		}
		return (String[]) fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * Retrieve the names of all the public static methods in the clazz
	 * @param clazz the object to inspect
	 * @return list of all the names of public statics
	 */
	public String[] getPublicStaticMethodNames(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		Set<String> methodNames = new HashSet<String>();
		for (Method method : methods) {
			if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
				methodNames.add(method.getName());
			}
		}
		return (String[]) methodNames.toArray(new String[methodNames.size()]);
	}

	/**
	 * Retrieve the names of all the public static classes in the clazz
	 * @param clazz the object to inspect
	 * @return list of all the names of public statics
	 */
	public String[] getPublicStaticClassNames(Class<?> clazz) {
		Class<?>[] classes = clazz.getClasses();
		Set<String> classNames = new HashSet<String>();
		for (Class<?> clazz2 : classes) {
			if (Modifier.isPublic(clazz2.getModifiers()) && Modifier.isStatic(clazz2.getModifiers())) {
				classNames.add(clazz2.getSimpleName());
			}
		}
		return (String[]) classNames.toArray(new String[classNames.size()]);
	}

	/**
	 * Retrieve the names of all the public static fields, methods and
	 * classes in the clazz
	 * @param clazz the object to inspect
	 * @return list of all the names of public statics
	 */
	public String[] getPublicStaticNames(Class<?> clazz) {
		Set<String> names = new HashSet<String>();
		names.addAll(Arrays.asList(getPublicStaticClassNames(clazz)));
		names.addAll(Arrays.asList(getPublicStaticFieldNames(clazz)));
		names.addAll(Arrays.asList(getPublicStaticMethodNames(clazz)));
		return (String[]) names.toArray(new String[names.size()]);
	}
}
