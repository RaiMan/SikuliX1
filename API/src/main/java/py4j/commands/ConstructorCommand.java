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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import py4j.Protocol;
import py4j.Py4JException;
import py4j.ReturnObject;

/**
 * <p>
 * A ConstructorCommand is responsible for calling the constructors of a Java
 * class. It provides similar services to the CallCommand.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class ConstructorCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(CallCommand.class.getName());

	public final static String CONSTRUCTOR_COMMAND_NAME = "i";

	public ConstructorCommand() {
		super();
		this.commandName = CONSTRUCTOR_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		String fqn = reader.readLine();
		List<Object> arguments = getArguments(reader);

		ReturnObject returnObject = invokeConstructor(fqn, arguments);

		String returnCommand = Protocol.getOutputCommand(returnObject);
		logger.finest("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	protected ReturnObject invokeConstructor(String fqn, List<Object> arguments) {
		ReturnObject returnObject = null;
		try {
			returnObject = gateway.invoke(fqn, arguments);
		} catch (Exception e) {
			logger.log(Level.FINE, "Received exception while executing this command: " + fqn, e);
			returnObject = ReturnObject.getErrorReturnObject(e);
		}
		return returnObject;
	}

}
