/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java;

public interface Py4JPythonClientPerThread extends Py4JPythonClient {
	ClientServerConnection getPerThreadConnection();

	void setPerThreadConnection(ClientServerConnection clientServerConnection);

	Gateway getGateway();

	void setGateway(Gateway gateway);

	Py4JJavaServer getJavaServer();

	void setJavaServer(Py4JJavaServer javaServer);

}
