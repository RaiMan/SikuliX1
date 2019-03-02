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

/**
 * <p>
 * A TypeConverter converts a Python type into a Java Type. For example, a
 * Python String might be converted to a Java character (e.g., when calling a
 * method that only accepts characters).
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class TypeConverter {

	public final static int NO_CONVERSION = -1;
	public final static int DOUBLE_TO_FLOAT = 0;
	public final static int INT_TO_SHORT = 1;
	public final static int INT_TO_BYTE = 2;
	public final static int STRING_TO_CHAR = 3;
	public final static int NUM_TO_LONG = 4;

	private final int conversion;

	public final static TypeConverter NO_CONVERTER = new TypeConverter();
	public final static TypeConverter FLOAT_CONVERTER = new TypeConverter(DOUBLE_TO_FLOAT);
	public final static TypeConverter SHORT_CONVERTER = new TypeConverter(INT_TO_SHORT);
	public final static TypeConverter BYTE_CONVERTER = new TypeConverter(INT_TO_BYTE);
	public final static TypeConverter CHAR_CONVERTER = new TypeConverter(STRING_TO_CHAR);
	public final static TypeConverter LONG_CONVERTER = new TypeConverter(NUM_TO_LONG);

	public TypeConverter() {
		this(NO_CONVERSION);
	}

	public TypeConverter(int conversion) {
		this.conversion = conversion;
	}

	public Object convert(Object obj) {
		Object newObject = null;

		switch (conversion) {
		case NO_CONVERSION:
			newObject = obj;
			break;
		case DOUBLE_TO_FLOAT:
			newObject = ((Double) obj).floatValue();
			break;
		case INT_TO_SHORT:
			newObject = ((Integer) obj).shortValue();
			break;
		case INT_TO_BYTE:
			newObject = ((Integer) obj).byteValue();
			break;
		case STRING_TO_CHAR:
			newObject = ((CharSequence) obj).charAt(0);
			break;
		case NUM_TO_LONG:
			newObject = Long.parseLong(obj.toString());
			break;
		default:
			newObject = null;
		}

		return newObject;
	}

	public int getConversion() {
		return conversion;
	}

}
