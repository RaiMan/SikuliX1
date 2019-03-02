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
package py4j.reflection;

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
