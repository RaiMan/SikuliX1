/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 * <p>
 * String utility class providing operations to escape and unescape new lines.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class StringUtil {

	public final static char ESCAPE_CHAR = '\\';

	public static String escape(String original) {
		if (original != null) {
			return original.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
		} else {
			return null;
		}
	}

	public static String unescape(String escaped) {
		boolean escaping = false;
		StringBuilder newString = new StringBuilder();

		for (char c : escaped.toCharArray()) {
			if (!escaping) {
				if (c == ESCAPE_CHAR) {
					escaping = true;
				} else {
					newString.append(c);
				}
			} else {
				if (c == 'n') {
					newString.append('\n');
				} else if (c == 'r') {
					newString.append('\r');
				} else {
					newString.append(c);
				}
				escaping = false;
			}
		}

		return newString.toString();

	}
}
