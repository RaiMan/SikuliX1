/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.model;

import java.util.regex.Pattern;

import py4Java.reflection.TypeUtil;

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
