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

import java.lang.reflect.Method;
import java.util.List;

import py4j.reflection.TypeUtil;

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
