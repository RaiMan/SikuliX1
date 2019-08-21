/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.model;

import py4Java.reflection.TypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Model of a Java class used to create a help page.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class Py4JClass extends Py4JMember {

	public final static Py4JClass buildClass(Class<?> clazz) {
		return buildClass(clazz, true);
	}

	public final static Py4JClass buildClass(Class<?> clazz, boolean sort) {
        List<Py4JClass> classes = new ArrayList<>();
        List<Py4JMethod> methods = new ArrayList<>();
        List<Py4JField> fields = new ArrayList<>();

		for (Class<?> memberClass : clazz.getDeclaredClasses()) {
			if (Modifier.isPublic(memberClass.getModifiers())) {
				classes.add(Py4JClass.buildClass(memberClass, sort));
			}
		}

		for (Method method : clazz.getDeclaredMethods()) {
			if (Modifier.isPublic(method.getModifiers())) {
				methods.add(Py4JMethod.buildMethod(method));
			}
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isPublic(field.getModifiers())) {
				fields.add(Py4JField.buildField(field));
			}
		}

		Class<?> superClass = clazz.getSuperclass();
		String extend = null;
		if (superClass != null && superClass != Object.class) {
			extend = superClass.getCanonicalName();
		}

		Class<?>[] interfaces = clazz.getInterfaces();
		List<String> implementTypes = interfaces != null && interfaces.length > 0 ? TypeUtil.getNames(interfaces)
				: null;

		if (sort) {
			Collections.sort(classes);
			Collections.sort(methods);
			Collections.sort(fields);
		}

		return new Py4JClass(clazz.getCanonicalName(), null, extend, implementTypes,
				Collections.unmodifiableList(methods), Collections.unmodifiableList(fields),
				Collections.unmodifiableList(classes));
	}

	private final String extendType;

	private final List<String> implementTypes;

	private final List<Py4JMethod> methods;

	private final List<Py4JField> fields;

	private final List<Py4JClass> classes;

	public Py4JClass(String name, String javadoc, String extendType, List<String> implementTypes,
			List<Py4JMethod> methods, List<Py4JField> fields, List<Py4JClass> classes) {
		super(name, javadoc);
		this.extendType = extendType;
		this.implementTypes = implementTypes;
		this.methods = methods;
		this.fields = fields;
		this.classes = classes;
	}

	public List<Py4JClass> getClasses() {
		return classes;
	}

	public String getExtendType() {
		return extendType;
	}

	public List<Py4JField> getFields() {
		return fields;
	}

	public List<String> getImplementTypes() {
		return implementTypes;
	}

	public List<Py4JMethod> getMethods() {
		return methods;
	}

	@Override
	public String getSignature(boolean shortName) {
		StringBuilder builder = new StringBuilder();

		builder.append(TypeUtil.getName(getName(), shortName));
		if (extendType != null) {
			builder.append(" extends ");
			builder.append(extendType);
		}

		if (implementTypes != null) {
			builder.append(" implements ");
			int length = implementTypes.size();
			for (int i = 0; i < length - 1; i++) {
				builder.append(implementTypes.get(i));
				builder.append(", ");
			}
			builder.append(implementTypes.get(length - 1));
		}
		return builder.toString();
	}
}
