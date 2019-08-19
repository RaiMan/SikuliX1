/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import static py4Java.Protocol.AUTH_COMMAND_NAME;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import py4Java.Protocol;
import py4Java.Py4JAuthenticationException;
import py4Java.Py4JException;

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
