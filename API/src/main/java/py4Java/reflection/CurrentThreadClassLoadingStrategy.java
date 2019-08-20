/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

/**
 * <p>This class loading strategy uses the current thread's ClassLoader to
 * load a class from a fully qualified name.</p>
 */
public class CurrentThreadClassLoadingStrategy implements ClassLoadingStrategy {

	@Override
	public Class<?> classForName(String className) throws ClassNotFoundException {
		return Class.forName(className, true, this.getClassLoader());
	}

	@Override
	public ClassLoader getClassLoader() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader;
	}
}
