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
package py4j;

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
