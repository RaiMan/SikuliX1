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
package py4j.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.Gateway;
import py4j.Protocol;
import py4j.Py4JException;
import py4j.Py4JServerConnection;
import py4j.ReturnObject;

/**
 * <p>
 * Abstract base class for commands. Provides useful methods allowing the
 * parsing of command arguments.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public abstract class AbstractCommand implements Command {

	protected Gateway gateway;

	protected String commandName;

	private final Logger logger = Logger.getLogger(AbstractCommand.class.getName());

	protected Py4JServerConnection connection;

	@Override
	public abstract void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException;

	/**
	 *
	 * @param reader
	 * @return A list of the remaining arguments (converted using
	 *         Protocol.getObject) in the reader. Consumes the end of command
	 *         part.
	 * @throws IOException
	 */
	protected List<Object> getArguments(BufferedReader reader) throws IOException {
		List<Object> arguments = new ArrayList<Object>();
		List<String> stringArguments = getStringArguments(reader);

		for (String stringArgument : stringArguments) {
			arguments.add(Protocol.getObject(stringArgument, this.gateway));
		}

		return arguments;
	}

	@Override
	public String getCommandName() {
		return commandName;
	}

	/**
	 *
	 * @param reader
	 * @return A list of the remaining arguments (as strings) in the reader.
	 *         Consumes the end of command part.
	 * @throws IOException
	 */
	protected List<String> getStringArguments(BufferedReader reader) throws IOException {
		List<String> arguments = new ArrayList<String>();
		String line = reader.readLine();

		while (!Protocol.isEmpty(line) && !Protocol.isEnd(line)) {
			logger.finest("Raw String Argument: " + line);
			arguments.add(line);
			line = reader.readLine();
		}

		return arguments;
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		this.gateway = gateway;
		this.connection = connection;
	}

	/**
	 * <p>
	 * Convenient shortcut to invoke a method dynamically.
	 * </p>
	 *
	 * @param methodName
	 * @param targetObjectId
	 * @param arguments
	 * @return
	 */
	protected ReturnObject invokeMethod(String methodName, String targetObjectId, List<Object> arguments) {
		ReturnObject returnObject = null;
		try {
			returnObject = gateway.invoke(methodName, targetObjectId, arguments);
		} catch (Exception e) {
			logger.log(Level.FINE, "Received exception while executing this command: " + methodName, e);
			returnObject = ReturnObject.getErrorReturnObject(e);
		}
		return returnObject;
	}

}
