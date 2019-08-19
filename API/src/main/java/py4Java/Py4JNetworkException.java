/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 * <p>
 * Exception raised when a network error is encountered while using Py4J.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class Py4JNetworkException extends Py4JException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3338931855286981212L;

	private final ErrorTime when;

	public enum ErrorTime {
		ERROR_ON_SEND, ERROR_ON_RECEIVE, OTHER
	}

	public Py4JNetworkException() {
		this(null, null, ErrorTime.OTHER);
	}

	public Py4JNetworkException(ErrorTime when) {
		this(null, null, when);
	}

	public Py4JNetworkException(String message) {
		this(message, null, ErrorTime.OTHER);
	}

	public Py4JNetworkException(String message, ErrorTime when) {
		this(message, null, when);
	}

	public Py4JNetworkException(String message, Throwable cause) {
		this(message, cause, ErrorTime.OTHER);
	}

	public Py4JNetworkException(String message, Throwable cause, ErrorTime when) {
		super(message, cause);
		this.when = when;
	}

	public Py4JNetworkException(Throwable cause) {
		this(null, cause, ErrorTime.OTHER);
	}

	public ErrorTime getWhen() {
		return when;
	}

}
