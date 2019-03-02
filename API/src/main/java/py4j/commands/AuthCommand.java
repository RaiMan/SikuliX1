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

import static py4j.Protocol.AUTH_COMMAND_NAME;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import py4j.Protocol;
import py4j.Py4JAuthenticationException;
import py4j.Py4JException;

/**
 * The auth command is responsible for checking that the client knows the server's auth
 * secret.
 */
public class AuthCommand extends AbstractCommand {

	public static final String COMMAND_NAME = AUTH_COMMAND_NAME;

	private final String authToken;
	private volatile boolean hasAuthenticated;

	public AuthCommand(String authToken) {
		this.commandName = COMMAND_NAME;
		this.authToken = authToken;
		this.hasAuthenticated = false;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		// Check the command name since socket handlers will always call this command first when
		// authentication is enabled, regardless of the command actually sent by the client.
		if (!COMMAND_NAME.equals(commandName)) {
			writer.write(Protocol.getOutputErrorCommand("Authentication error: unexpected command."));
			writer.flush();
			throw new Py4JAuthenticationException(
					String.format("Expected %s, got %s instead.", COMMAND_NAME, commandName));
		}

		String clientToken = reader.readLine();
		if (authToken.equals(clientToken)) {
			writer.write(Protocol.getOutputVoidCommand());
			writer.flush();
			hasAuthenticated = true;
		} else {
			writer.write(Protocol.getOutputErrorCommand("Authentication error: bad auth token received."));
			writer.flush();
			throw new Py4JAuthenticationException("Client authentication unsuccessful.");
		}
	}

	public boolean isAuthenticated() {
		return hasAuthenticated;
	}

}
