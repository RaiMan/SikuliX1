/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.net.Socket;

/**
 *
 */
public interface Py4JServerConnection {

	/**
	 * @return The socket used by this gateway connection.
	 */
	Socket getSocket();

	void shutdown();

	void shutdown(boolean reset);

}
