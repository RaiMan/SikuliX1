/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

/**
 * <p>Strategy interface to load a class from a fully qualified name.</p>
 */
public interface ClassLoadingStrategy {

	Class<?> classForName(String className) throws ClassNotFoundException;

	ClassLoader getClassLoader();
}
