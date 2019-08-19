/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

/**
 *
 * <p>
 * Default application that can be used to quickly test Py4J.
 * </p>
 *
 * @author Barthelemy Dagenais
 *
 */
public class DefaultApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GatewayServer server = new GatewayServer(new Object());
		server.start();
	}

}
