/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A MethodDescriptor wraps the signature of a method (name, container,
 * parameters).
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
@SuppressWarnings("rawtypes")
public class MethodDescriptor {

	private String internalRepresentation;

	private String name;

	private Class container;

	private List<Class> parameters;

	private final static char DOT = '.';

	public MethodDescriptor(String name, Class container, Class[] parameters) {
		super();
		this.name = name;
		this.container = container;
		this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
		this.internalRepresentation = buildInternalRepresentation(container, name, this.parameters);
	}

	private String buildInternalRepresentation(Class container, String name, List<Class> params) {
		StringBuilder builder = new StringBuilder();

		builder.append(container.getName());
		builder.append(DOT);
		builder.append(name);
		builder.append('(');
		for (Class param : params) {
			String paramName = "null";
			if (param != null) {
				paramName = param.getName();
			}
			builder.append(paramName);
			builder.append(DOT);
		}
		builder.append(')');

		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MethodDescriptor)) {
			return false;
		}

		return internalRepresentation.equals(((MethodDescriptor) obj).internalRepresentation);
	}

	public Class getContainer() {
		return container;
	}

	public String getInternalRepresentation() {
		return internalRepresentation;
	}

	public String getName() {
		return name;
	}

	public List<Class> getParameters() {
		return parameters;
	}

	@Override
	public int hashCode() {
		return internalRepresentation.hashCode();
	}

	@Override
	public String toString() {
		return internalRepresentation;
	}

}
