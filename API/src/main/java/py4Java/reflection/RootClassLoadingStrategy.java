/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

/**
 * <p>This class loading strategy just uses the class current class loader
 * to load a class from a fully qualified name.</p>
 */
public class RootClassLoadingStrategy implements ClassLoadingStrategy {

	@Override
	public Class<?> classForName(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}

	@Override
	public ClassLoader getClassLoader() {
		return RootClassLoadingStrategy.class.getClassLoader();
	}
}
