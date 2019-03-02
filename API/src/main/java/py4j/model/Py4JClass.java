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
package py4j.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import py4j.reflection.TypeUtil;

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
		List<Py4JClass> classes = new ArrayList<Py4JClass>();
		List<Py4JMethod> methods = new ArrayList<Py4JMethod>();
		List<Py4JField> fields = new ArrayList<Py4JField>();

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
