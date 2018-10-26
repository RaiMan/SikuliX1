package se.vidstige.jadb.server;

import java.net.Socket;

/**
 * Created by vidstige on 2014-03-20
 */
public class AdbServer extends SocketServer {

    public static final int DEFAULT_PORT = 15037;
    private final AdbResponder responder;

    public AdbServer(AdbResponder responder) {
        this(responder, DEFAULT_PORT);
    }

    public AdbServer(AdbResponder responder, int port) {
        super(port);
        this.responder = responder;
    }

    @Override
    protected Runnable createResponder(Socket socket) {
        return new AdbProtocolHandler(socket, responder);
    }
}
