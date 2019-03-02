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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import py4j.JVMView;
import py4j.Py4JException;

/**
 * <p>
 * This class is responsible for the type conversion between Python types and
 * Java types.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class TypeUtil {
	private static Set<String> primitiveTypes;
	private static Map<String, Class<?>> primitiveClasses;

	public final static int DISTANCE_FACTOR = 100;

	static {
		primitiveTypes = new HashSet<String>();
		primitiveTypes.add(long.class.getName());
		primitiveTypes.add(int.class.getName());
		primitiveTypes.add(short.class.getName());
		primitiveTypes.add(byte.class.getName());
		primitiveTypes.add(double.class.getName());
		primitiveTypes.add(float.class.getName());
		primitiveTypes.add(Long.class.getName());
		primitiveTypes.add(Integer.class.getName());
		primitiveTypes.add(Short.class.getName());
		primitiveTypes.add(Byte.class.getName());
		primitiveTypes.add(Double.class.getName());
		primitiveTypes.add(Float.class.getName());

		primitiveClasses = new HashMap<String, Class<?>>();
		primitiveClasses.put("long", long.class);
		primitiveClasses.put("int", int.class);
		primitiveClasses.put("short", short.class);
		primitiveClasses.put("byte", byte.class);
		primitiveClasses.put("double", double.class);
		primitiveClasses.put("float", float.class);
		primitiveClasses.put("boolean", boolean.class);
		primitiveClasses.put("char", char.class);
	}

	public static int computeCharacterConversion(Class<?> parent, Class<?> child, List<TypeConverter> converters) {
		int cost = -1;

		if (isCharacter(child)) {
			cost = 0;
			converters.add(TypeConverter.NO_CONVERTER);
		} else if (CharSequence.class.isAssignableFrom(child)) {
			cost = 1;
			converters.add(TypeConverter.CHAR_CONVERTER);
		}

		return cost;
	}

	public static int computeDistance(Class<?> parent, Class<?> child) {
		int distance = -1;
		if (parent.equals(child)) {
			distance = 0;
		}

		// Search through super classes
		if (distance == -1) {
			distance = computeSuperDistance(parent, child);
		}

		// Search through interfaces (costly)
		if (distance == -1) {
			distance = computeInterfaceDistance(parent, child, new HashSet<String>(),
					Arrays.asList(child.getInterfaces()));
		}

		if (distance != -1) {
			distance *= DISTANCE_FACTOR;
		}

		return distance;
	}

	private static int computeInterfaceDistance(Class<?> parent, Class<?> child, Set<String> visitedInterfaces,
			List<? extends Class<?>> interfacesToVisit) {
		int distance = -1;
		List<Class<?>> nextInterfaces = new ArrayList<Class<?>>();
		for (Class<?> clazz : interfacesToVisit) {
			if (parent.equals(clazz)) {
				distance = 1;
				break;
			} else {
				visitedInterfaces.add(clazz.getName());
				getNextInterfaces(clazz, nextInterfaces, visitedInterfaces);
			}
		}

		if (distance == -1) {
			Class<?> grandChild = null;

			if (child != null) {
				// We still have a superclass, so add its interfaces.
				grandChild = child.getSuperclass();
				getNextInterfaces(grandChild, nextInterfaces, visitedInterfaces);
			}

			if (nextInterfaces.size() > 0 || grandChild != null) {
				int newDistance = computeInterfaceDistance(parent, grandChild, visitedInterfaces, nextInterfaces);
				if (newDistance != -1) {
					distance = newDistance + 1;
				}
			}
		}

		return distance;
	}

	public static int computeNumericConversion(Class<?> parent, Class<?> child, List<TypeConverter> converters) {
		int cost = -1;

		// XXX This is not complete. Certain cases are not considered like from
		// Long to Int, Long to short, and the like. This is not a problem for
		// Py4J. This could be a problem for pure Java. But type conversion is
		// NOT required for pure Java, only for scripting languages with less
		// primitives.

		if (isLong(parent) && (!isFloat(child) && !isDouble(child))) {
			cost = getCost(parent, child);
			if (isLong(child)) {
				converters.add(TypeConverter.NO_CONVERTER);
			} else {
				converters.add(TypeConverter.LONG_CONVERTER);
			}
		} else if (isInteger(parent) && (isInteger(child) || isShort(child) || isByte(child))) {
			cost = getCost(parent, child);
			converters.add(TypeConverter.NO_CONVERTER);
		} else if (isShort(parent)) {
			if (isShort(child) || isByte(child)) {
				cost = getCost(parent, child);
				converters.add(TypeConverter.NO_CONVERTER);
			} else if (isInteger(child)) {
				cost = 1;
				converters.add(TypeConverter.SHORT_CONVERTER);
			}
		} else if (isByte(parent)) {
			if (isByte(child)) {
				cost = 0;
				converters.add(TypeConverter.NO_CONVERTER);
			} else if (isInteger(child)) {
				cost = 2;
				converters.add(TypeConverter.BYTE_CONVERTER);
			}
		} else if (isDouble(parent)) {
			if (isDouble(child)) {
				cost = 0;
				converters.add(TypeConverter.NO_CONVERTER);
			} else if (isFloat(child)) {
				cost = 1;
				converters.add(TypeConverter.NO_CONVERTER);
			}
		} else if (isFloat(parent)) {
			if (isFloat(child)) {
				cost = 0;
				converters.add(TypeConverter.NO_CONVERTER);
			} else if (isDouble(child)) {
				cost = 1;
				converters.add(TypeConverter.FLOAT_CONVERTER);
			}
		}

		return cost;
	}

	private static int computeSuperDistance(Class<?> parent, Class<?> child) {
		Class<?> superChild = child.getSuperclass();
		if (superChild == null) {
			return -1;
		} else if (superChild.equals(parent)) {
			return 1;
		} else {
			int distance = computeSuperDistance(parent, superChild);
			if (distance != -1) {
				return distance + 1;
			} else {
				return distance;
			}
		}
	}

	public static Class<?> forName(String fqn) throws ClassNotFoundException {
		Class<?> clazz = primitiveClasses.get(fqn);
		if (clazz == null) {
			clazz = ReflectionUtil.classForName(fqn);
		}
		return clazz;
	}

	public static Class<?> forName(String fqn, JVMView view) throws ClassNotFoundException {
		Class<?> clazz = primitiveClasses.get(fqn);
		if (clazz == null) {
			if (fqn.indexOf('.') < 0) {
				clazz = getClass(fqn, view);
			} else {
				clazz = ReflectionUtil.classForName(fqn);
			}
		}
		return clazz;
	}

	public static Class<?> getClass(String simpleName, JVMView view) throws ClassNotFoundException {
		Class<?> clazz = null;

		try {
			// First, try the fqn
			clazz = ReflectionUtil.classForName(simpleName);
		} catch (Exception e) {
			// Then try the single import
			Map<String, String> singleImportsMap = view.getSingleImportsMap();
			String newFQN = singleImportsMap.get(simpleName);
			if (newFQN != null) {
				clazz = ReflectionUtil.classForName(newFQN);
			} else {
				// Or try star imports
				for (String starImport : view.getStarImports()) {
					try {
						clazz = ReflectionUtil.classForName(starImport + "." + simpleName);
						break;
					} catch (Exception e2) {
						// Ignore
					}
				}
			}
		}

		if (clazz == null) {
			throw new ClassNotFoundException(simpleName + " not found.");
		}

		return clazz;
	}

	public static int getCost(Class<?> parent, Class<?> child) {
		return getPoint(parent) - getPoint(child);
	}

	public static String getName(String name, boolean shortName) {
		if (!shortName) {
			return name;
		} else {
			int index = name.lastIndexOf(".");
			if (index >= 0 && index < name.length() + 1) {
				return name.substring(index + 1);
			} else {
				return name;
			}
		}
	}

	public static List<String> getNames(Class<?>[] classes) {
		List<String> names = new ArrayList<String>();

		for (int i = 0; i < classes.length; i++) {
			names.add(classes[i].getCanonicalName());
		}

		return Collections.unmodifiableList(names);
	}

	private static void getNextInterfaces(Class<?> clazz, List<Class<?>> nextInterfaces,
			Set<String> visitedInterfaces) {
		if (clazz != null) {
			for (Class<?> nextClazz : clazz.getInterfaces()) {
				if (!visitedInterfaces.contains(nextClazz.getName())) {
					nextInterfaces.add(nextClazz);
				}
			}
		}
	}

	public static String getPackage(String name) {
		int index = name.lastIndexOf(".");
		if (index < 0) {
			return name;
		} else {
			return name.substring(0, index);
		}
	}

	public static int getPoint(Class<?> clazz) {
		int point = -1;
		if (isByte(clazz)) {
			point = 0;
		} else if (isShort(clazz)) {
			point = 1;
		} else if (isInteger(clazz)) {
			point = 2;
		} else if (isLong(clazz)) {
			point = 3;
		}
		return point;
	}

	public static boolean isBoolean(Class<?> clazz) {
		return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
	}

	public static boolean isByte(Class<?> clazz) {
		return clazz.equals(Byte.class) || clazz.equals(byte.class);
	}

	public static boolean isCharacter(Class<?> clazz) {
		return clazz.equals(Character.class) || clazz.equals(char.class);
	}

	public static boolean isDouble(Class<?> clazz) {
		return clazz.equals(Double.class) || clazz.equals(double.class);
	}

	public static boolean isFloat(Class<?> clazz) {
		return clazz.equals(Float.class) || clazz.equals(float.class);
	}

	public static boolean isInteger(Class<?> clazz) {
		return clazz.equals(Integer.class) || clazz.equals(int.class);
	}

	public static boolean isLong(Class<?> clazz) {
		return clazz.equals(Long.class) || clazz.equals(long.class);
	}

	public static boolean isNumeric(Class<?> clazz) {
		return primitiveTypes.contains(clazz.getName());
	}

	public static boolean isShort(Class<?> clazz) {
		return clazz.equals(Short.class) || clazz.equals(short.class);
	}

	/**
	 * <p>
	 * Checks if an object is an instance of a given class.
	 * </p>
	 *
	 * @param clazz
	 *            The class to check
	 * @param object
	 *            The object
	 * @return True if object is an instance of clazz.
	 */
	public static boolean isInstanceOf(Class<?> clazz, Object object) {
		return clazz.isInstance(object);
	}

	/**
	 * <p>
	 * Checks if an object is an instance of a given class.
	 * </p>
	 *
	 * @param classFQN
	 *            The fully qualified name of a class to check
	 * @param object
	 *            The object
	 * @return True if object is an instance of the class.
	 */
	public static boolean isInstanceOf(String classFQN, Object object) {
		Class<?> clazz = null;
		try {
			clazz = ReflectionUtil.classForName(classFQN);
		} catch (Exception e) {
			throw new Py4JException(e);
		}
		return isInstanceOf(clazz, object);
	}

}
