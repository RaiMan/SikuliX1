/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.math.BigDecimal;

/**
 * <p>
 * A ReturnObject wraps a value returned by a method. If the value is a
 * primitive, a primitive wrapper object (e.g., Integer) or a String, the value
 * is kept in the primitiveObject field.
 * </p>
 * 
 * <p>
 * If the return value is an object, a key to the reference is kept in the name
 * field. This value can be retrieved by calling
 * {@link py4Java.Gateway#getObject(String)} with the key.
 * </p>
 * 
 * <p>
 * Various methods are defined to determine the type of the return value. For
 * example, if the return value is void, the name and primitiveObject fields are
 * null, but {@link #isVoid()} returns true.
 * </p>
 * 
 * <p>
 * ReturnObject objects can only be constructed through static factory methods
 * such as {@link #getListReturnObject(String, int)}.
 * </p>
 * 
 * @author barthelemy
 * 
 */
public class ReturnObject {

	public static ReturnObject getArrayReturnObject(String name, int size) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.size = size;
		rObject.isArray = true;
		rObject.commandPart = Protocol.ARRAY_TYPE + name;
		return rObject;
	}

	public static ReturnObject getDecimalReturnObject(Object object) {
		BigDecimal decimal = (BigDecimal) object;
		ReturnObject rObject = new ReturnObject();
		rObject.isDecimal = true;
		rObject.commandPart = Protocol.DECIMAL_TYPE + decimal.toPlainString();
		return rObject;
	}

	public static ReturnObject getErrorReferenceReturnObject(String name) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.isError = true;
        rObject.commandPart = String.valueOf(Protocol.ERROR) +
                Protocol.REFERENCE_TYPE +
                name;
		return rObject;
	}

	public static ReturnObject getErrorReturnObject() {
		ReturnObject rObject = new ReturnObject();
		rObject.isError = true;
		rObject.commandPart = String.valueOf(Protocol.ERROR);
		return rObject;
	}

	public static ReturnObject getErrorReturnObject(Throwable throwable) {
		ReturnObject rObject = new ReturnObject();
		rObject.isError = true;
        rObject.commandPart = String.valueOf(Protocol.ERROR) +
                Protocol.STRING_TYPE +
                StringUtil.escape(Protocol.getThrowableAsString(throwable));
		return rObject;
	}

	public static ReturnObject getIteratorReturnObject(String name) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.isIterator = true;
		rObject.commandPart = Protocol.ITERATOR_TYPE + name;
		return rObject;
	}

	public static ReturnObject getListReturnObject(String name, int size) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.size = size;
		rObject.isList = true;
		rObject.commandPart = Protocol.LIST_TYPE + name;
		return rObject;
	}

	public static ReturnObject getMapReturnObject(String name, int size) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.size = size;
		rObject.isMap = true;
		rObject.commandPart = Protocol.MAP_TYPE + name;
		return rObject;
	}

	public static ReturnObject getNullReturnObject() {
		ReturnObject rObject = new ReturnObject();
		rObject.isNull = true;
		rObject.commandPart = String.valueOf(Protocol.NULL_TYPE);
		return rObject;
	}

	public static ReturnObject getPrimitiveReturnObject(Object primitive) {
		ReturnObject rObject = new ReturnObject();
		rObject.primitiveObject = primitive;
		char primitiveType = Protocol.getPrimitiveType(primitive);
		if (primitiveType == Protocol.STRING_TYPE) {
			rObject.commandPart = primitiveType + StringUtil.escape(primitive.toString());
		} else if (primitiveType == Protocol.BYTES_TYPE) {
			rObject.commandPart = primitiveType + Protocol.encodeBytes((byte[]) primitive);
		} else {
			rObject.commandPart = primitiveType + primitive.toString();
		}
		return rObject;
	}

	public static ReturnObject getReferenceReturnObject(String name) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.isReference = true;
		rObject.commandPart = Protocol.REFERENCE_TYPE + name;
		return rObject;
	}

	public static ReturnObject getSetReturnObject(String name, int size) {
		ReturnObject rObject = new ReturnObject();
		rObject.name = name;
		rObject.size = size;
		rObject.isSet = true;
		rObject.commandPart = Protocol.SET_TYPE + name;
		return rObject;
	}

	public static ReturnObject getVoidReturnObject() {
		ReturnObject rObject = new ReturnObject();
		rObject.isVoid = true;
		rObject.commandPart = String.valueOf(Protocol.VOID);
		return rObject;
	}

	private String name;
	private Object primitiveObject;

	private boolean isReference;

	private boolean isMap;

	private boolean isList;

	private boolean isNull;

	private boolean isError;

	private boolean isVoid;

	private boolean isArray;

	private boolean isIterator;

	private boolean isSet;

	private boolean isDecimal;

	private int size;

	private String commandPart;

	private ReturnObject() {
	}

	public String getCommandPart() {
		return commandPart;
	}

	public String getName() {
		return name;
	}

	public Object getPrimitiveObject() {
		return primitiveObject;
	}

	public int getSize() {
		return size;
	}

	public boolean isArray() {
		return isArray;
	}

	public boolean isDecimal() {
		return isDecimal;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isIterator() {
		return isIterator;
	}

	public boolean isList() {
		return isList;
	}

	public boolean isMap() {
		return isMap;
	}

	public boolean isNull() {
		return isNull;
	}

	public boolean isReference() {
		return isReference;
	}

	public boolean isSet() {
		return isSet;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	public void setCommandPart(String commandPart) {
		this.commandPart = commandPart;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public void setIterator(boolean isIterator) {
		this.isIterator = isIterator;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public void setMap(boolean isMap) {
		this.isMap = isMap;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	public void setPrimitiveObject(Object primitiveObject) {
		this.primitiveObject = primitiveObject;
	}

	public void setReference(boolean isReference) {
		this.isReference = isReference;
	}

	public void setSet(boolean isSet) {
		this.isSet = isSet;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}

}
