/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

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
