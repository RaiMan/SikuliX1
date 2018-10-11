package se.vidstige.jadb;

import java.io.IOException;

public class DeviceWatcher implements Runnable {
    private Transport transport;
    private final DeviceDetectionListener listener;
    private final JadbConnection connection;

    public DeviceWatcher(Transport transport, DeviceDetectionListener listener, JadbConnection connection) {
        this.transport = transport;
        this.listener = listener;
        this.connection = connection;
    }

    @Override
    public void run() {
        watch();
    }

    @SuppressWarnings("squid:S2189") // watcher is stopped by closing transport
    private void watch() {
        try {
            while (true) {
                listener.onDetect(connection.parseDevices(transport.readString()));
            }
        } catch (IOException ioe) {
            synchronized(this) {
                if (transport != null) {
                    listener.onException(ioe);
                }
            }
        } catch (Exception e) {
            listener.onException(e);
        }
    }

    public void stop() throws IOException {
        synchronized(this) {
            transport.close();
            transport = null;
        }
    }
}
