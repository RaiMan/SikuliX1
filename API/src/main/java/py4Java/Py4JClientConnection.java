/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

import java.io.IOException;

public interface Py4JClientConnection {

	String sendCommand(String command);

	String sendCommand(String command, boolean blocking);

	void shutdown();

	void shutdown(boolean reset);

	void start() throws IOException;

	void setUsed(boolean used);

	boolean wasUsed();

}
