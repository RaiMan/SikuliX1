/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.model;

import java.lang.reflect.Method;
import java.util.List;

import py4Java.reflection.TypeUtil;

/**
 * <p>
 * Model of a Java method used to create a help page.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class Py4JMethod extends Py4JMember {

	public final static Py4JMethod buildMethod(Method method) {
		return new Py4JMethod(method.getName(), null, TypeUtil.getNames(method.getParameterTypes()), null,
				method.getReturnType().getCanonicalName(), method.getDeclaringClass().getCanonicalName());
	}

	private final List<String> parameterTypes;

	// Currently not supported.
	private final List<String> parameterNames;

	private final String returnType;

	private final String container;

	public Py4JMethod(String name, String javadoc, List<String> parameterTypes, List<String> parameterNames,
			String returnType, String container) {
		super(name, javadoc);

		this.parameterNames = parameterNames;
		this.parameterTypes = parameterTypes;

		this.returnType = returnType;
		this.container = container;
	}

	public String getContainer() {
		return container;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	public String getReturnType() {
		return returnType;
	}

	@Override
	public String getSignature(boolean shortName) {
		StringBuilder builder = new StringBuilder();
		int length = parameterTypes.size();
		builder.append(getName());
		builder.append('(');
		for (int i = 0; i < length - 1; i++) {
			builder.append(TypeUtil.getName(parameterTypes.get(i), shortName));
			builder.append(", ");
		}
		if (length > 0) {
			builder.append(TypeUtil.getName(parameterTypes.get(length - 1), shortName));
		}
		builder.append(") : ");
		builder.append(TypeUtil.getName(returnType, shortName));

		return builder.toString();
	}

}
