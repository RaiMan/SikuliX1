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
