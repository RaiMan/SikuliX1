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

import java.util.regex.Pattern;

import py4j.reflection.TypeUtil;

/**
 * <p>
 * The HelpPageGenerator generates a help page (a String) for a class or a
 * method.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class HelpPageGenerator {

	public final static String PREFIX = "|";
	public final static String INDENT = "  ";
	public final static String PREFIX_INDENT = PREFIX + INDENT;
	public final static String DOUBLE_LINES = "\n" + PREFIX_INDENT + "\n";
	public final static String SEPARATOR = "------------------------------------------------------------";
	public final static String PREFIX_SEPARATOR = PREFIX + INDENT + SEPARATOR + "\n";

	/**
	 * 
	 * @param clazz
	 * @param pattern
	 *            Star pattern.
	 * @param shortName
	 * @return
	 */
	public final static String getHelpPage(Py4JClass clazz, String pattern, boolean shortName) {
		Pattern regex = getRegex(pattern);

		StringBuilder builder = new StringBuilder();

		builder.append("Help on ");

		builder.append("class ");
		builder.append(TypeUtil.getName(clazz.getName(), true));
		builder.append(" in package ");
		builder.append(TypeUtil.getPackage(clazz.getName()));
		builder.append(":\n\n");
		builder.append(clazz.getSignature(shortName));
		builder.append(" {");
		builder.append(DOUBLE_LINES);
		builder.append(PREFIX_INDENT);
		builder.append("Methods defined here:");
		builder.append(DOUBLE_LINES);
		for (Py4JMethod method : clazz.getMethods()) {
			String signature = method.getSignature(shortName);
			if (regex.matcher(signature).matches()) {
				builder.append(PREFIX_INDENT);
				builder.append(signature);
				builder.append(DOUBLE_LINES);
			}
		}

		builder.append(PREFIX_SEPARATOR);
		builder.append(PREFIX_INDENT);
		builder.append("Fields defined here:");
		builder.append(DOUBLE_LINES);
		for (Py4JField field : clazz.getFields()) {
			String signature = field.getSignature(shortName);
			if (regex.matcher(signature).matches()) {
				builder.append(PREFIX_INDENT);
				builder.append(signature);
				builder.append(DOUBLE_LINES);
			}
		}

		builder.append(PREFIX_SEPARATOR);
		builder.append(PREFIX_INDENT);
		builder.append("Internal classes defined here:");
		builder.append(DOUBLE_LINES);
		for (Py4JClass internalClass : clazz.getClasses()) {
			builder.append(PREFIX_INDENT);
			builder.append(internalClass.getSignature(shortName));
			builder.append(DOUBLE_LINES);
		}
		builder.append("}");
		builder.append("\n");
		return builder.toString();
	}

	public final static String getHelpPage(Py4JMethod method, boolean shortName) {
		StringBuilder builder = new StringBuilder();
		builder.append("Method \"");
		builder.append(method.getName());
		builder.append("\" of class ");
		builder.append(method.getContainer());
		builder.append("\n{\n");
		builder.append(PREFIX_INDENT);
		builder.append(method.getSignature(shortName));
		builder.append("\n}");
		builder.append("\n");
		return builder.toString();
	}

	public final static Pattern getRegex(String pattern) {
		if (pattern == null) {
			return Pattern.compile(".*");
		} else {
			String newPattern = "^" + pattern.trim().replace(".", "\\.").replace("*", ".*").replace("?", ".?")
					.replace("(", "\\(").replace(")", "\\)");
			return Pattern.compile(newPattern);
		}
	}
}
