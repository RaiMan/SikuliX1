/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 * <p>
 * A client can implement this listener to be notified of Gateway events.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public interface GatewayServerListener {

	void connectionError(Exception e);

	void connectionStarted(Py4JServerConnection gatewayConnection);

	void connectionStopped(Py4JServerConnection gatewayConnection);

	/**
	 * <p>
	 * This method may be called concurrently with serverPostShutdown().
	 * </p>
	 * 
	 * <p>
	 * Typically a one thread calls shutdown() and then, the thread running the
	 * GatewayServer breaks from the connection accept loop.
	 * </p>
	 */
	void serverError(Exception e);

	/**
	 * <p>
	 * This method may be called concurrently with serverStopped() and
	 * serverError().
	 * </p>
	 * 
	 * <p>
	 * Typically a one thread calls shutdown() and then, the thread running the
	 * GatewayServer breaks from the connection accept loop.
	 * </p>
	 */
	void serverPostShutdown();

	void serverPreShutdown();

	void serverStarted();

	/**
	 * <p>
	 * This method may be called concurrently with serverPostShutdown().
	 * </p>
	 * 
	 * <p>
	 * Typically a one thread calls shutdown() and then, the thread running the
	 * GatewayServer breaks from the connection accept loop.
	 * </p>
	 */
	void serverStopped();

}
