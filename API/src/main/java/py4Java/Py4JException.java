/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 * <p>
 * Exception raised when an error is encountered while using Py4J.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class Py4JException extends RuntimeException {

	private static final long serialVersionUID = -2457373060192300387L;

	public Py4JException() {
	}

	public Py4JException(String arg0) {
		super(arg0);
	}

	public Py4JException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public Py4JException(Throwable arg0) {
		super(arg0);
	}

}
