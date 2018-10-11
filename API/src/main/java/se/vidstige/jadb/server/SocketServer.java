package se.vidstige.jadb.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// >set ANDROID_ADB_SERVER_PORT=15037
public abstract class SocketServer implements Runnable {

    private final int port;
    private ServerSocket socket;
    private Thread thread;

    private boolean isStarted = false;
    private final Object lockObject = new Object();

    protected SocketServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        thread = new Thread(this, "Fake Adb Server");
        thread.setDaemon(true);
        thread.start();
        waitForServer();
    }

    public int getPort() {
        return port;
    }

    @SuppressWarnings("squid:S2189") // server is stopped by closing SocketServer
    @Override
    public void run() {
        try {
            socket = new ServerSocket(port);
            socket.setReuseAddress(true);

            serverReady();

            while (true) {
                Socket c = socket.accept();
                Thread clientThread = new Thread(createResponder(c), "AdbClientWorker");
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
            // Empty on purpose
        }
    }

    private void serverReady() {
        synchronized (lockObject) {
            isStarted = true;
            lockObject.notifyAll();
        }
    }

    private void waitForServer() throws InterruptedException {
        synchronized (lockObject) {
            while (!isStarted) {
                lockObject.wait();
            }
        }
    }

    protected abstract Runnable createResponder(Socket socket);

    public void stop() throws IOException, InterruptedException {
        socket.close();
        thread.join();
    }
}
