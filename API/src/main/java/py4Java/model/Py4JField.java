/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.model;

import java.lang.reflect.Field;

import py4Java.reflection.TypeUtil;

/**
 * <p>
 * Model of a Java field used to create a help page.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class Py4JField extends Py4JMember {

	public final static Py4JField buildField(Field field) {
		return new Py4JField(field.getName(), null, field.getType().getCanonicalName(),
				field.getDeclaringClass().getCanonicalName());
	}

	private final String type;

	private final String container;

	public Py4JField(String name, String javadoc, String type, String container) {
		super(name, javadoc);
		this.type = type;
		this.container = container;
	}

	public String getContainer() {
		return container;
	}

	@Override
	public String getSignature(boolean shortName) {
		StringBuilder builder = new StringBuilder();

		builder.append(getName());
		builder.append(" : ");
		builder.append(TypeUtil.getName(type, shortName));

		return builder.toString();
	}

	public String getType() {
		return type;
	}

}
