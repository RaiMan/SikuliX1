/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import py4Java.Gateway;
import py4Java.GatewayServer;
import py4Java.Py4JException;
import py4Java.Py4JJavaServer;
import py4Java.Py4JServerConnection;

/**
 * <p>
 * The ShutdownGatewayServerCommand is responsible for shutting down the
 * GatewayServer. This command is useful to shut down the server remotely, i.e.,
 * from the Python side.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class ShutdownGatewayServerCommand extends AbstractCommand {

	private Py4JJavaServer gatewayServer;

	public static final String SHUTDOWN_GATEWAY_SERVER_COMMAND_NAME = "s";

	public ShutdownGatewayServerCommand() {
		super();
		this.commandName = SHUTDOWN_GATEWAY_SERVER_COMMAND_NAME;
	}

	@Override
	public void execute(String commandName, BufferedReader reader, BufferedWriter writer)
			throws Py4JException, IOException {
		this.gatewayServer.shutdown();
	}

	@Override
	public void init(Gateway gateway, Py4JServerConnection connection) {
		super.init(gateway, connection);
		this.gatewayServer = (Py4JJavaServer) gateway.getObject(GatewayServer.GATEWAY_SERVER_ID);
	}

}
