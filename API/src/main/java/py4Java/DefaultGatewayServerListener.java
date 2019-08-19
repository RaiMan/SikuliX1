/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 * <p>
 * This class implements a default {@link py4Java.GatewayServerListener
 * GatewayServerListener}. All operations do nothing by default. Clients can
 * extend this class to only override the methods they need.
 * </p>
 * 
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class DefaultGatewayServerListener implements GatewayServerListener {

	@Override
	public void connectionError(Exception e) {
	}

	@Override
	public void connectionStarted(Py4JServerConnection gatewayConnection) {
	}

	@Override
	public void connectionStopped(Py4JServerConnection gatewayConnection) {
	}

	@Override
	public void serverError(Exception e) {
	}

	@Override
	public void serverPostShutdown() {
	}

	@Override
	public void serverPreShutdown() {
	}

	@Override
	public void serverStarted() {
	}

	@Override
	public void serverStopped() {
	}

}
