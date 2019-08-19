/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

/**
 * <p>
 * Global utility to load classes and perform general reflection operations
 * that can be customized by Strategy classes.
 * </p>
 */
public class ReflectionUtil {

	private static ClassLoadingStrategy classLoadingStrategy = new CurrentThreadClassLoadingStrategy();

	public static ClassLoadingStrategy getClassLoadingStrategy() {
		return classLoadingStrategy;
	}

	public static void setClassLoadingStrategy(ClassLoadingStrategy classLoadingStrategy) {
		ReflectionUtil.classLoadingStrategy = classLoadingStrategy;
	}

	public static Class<?> classForName(String className) throws ClassNotFoundException {
		return classLoadingStrategy.classForName(className);
	}

	public static ClassLoader getClassLoader() {
		return classLoadingStrategy.getClassLoader();
	}
}
