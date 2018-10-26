package se.vidstige.jadb;

import se.vidstige.jadb.managers.Bash;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JadbDevice {
    @SuppressWarnings("squid:S00115")
    public enum State {
        Unknown,
        Offline,
        Device,
        Recovery,
        BootLoader
    }

    //noinspection OctalInteger
    private static final int DEFAULT_MODE = 0664;
    private final String serial;
    private final ITransportFactory transportFactory;

    JadbDevice(String serial, ITransportFactory tFactory) {
        this.serial = serial;
        this.transportFactory = tFactory;
    }

    static JadbDevice createAny(JadbConnection connection) {
        return new JadbDevice(connection);
    }

    private JadbDevice(ITransportFactory tFactory) {
        serial = null;
        this.transportFactory = tFactory;
    }

    private State convertState(String type) {
        switch (type) {
            case "device":     return State.Device;
            case "offline":    return State.Offline;
            case "bootloader": return State.BootLoader;
            case "recovery":   return State.Recovery;
            default:           return State.Unknown;
        }
    }

    private Transport getTransport() throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        send(transport, serial == null ? "host:transport-any" : "host:transport:" + serial );
        return transport;
    }

    public String getSerial() {
        return serial;
    }

    public State getState() throws IOException, JadbException {
        Transport transport = transportFactory.createTransport();
        send(transport, serial == null ? "host:get-state" : "host-serial:" + serial + ":get-state");

        State state = convertState(transport.readString());
        transport.close();
        return state;
    }

    /** <p>Execute a shell command.</p>
     *
     * <p>For Lollipop and later see: {@link #execute(String, String...)}</p>
     *
     * @param command main command to run. E.g. "ls"
     * @param args arguments to the command.
     * @return combined stdout/stderr stream.
     * @throws IOException
     * @throws JadbException
     */
    public InputStream executeShell(String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "shell:" + shellLine.toString());
        return new AdbFilterInputStream(new BufferedInputStream(transport.getInputStream()));
    }

    /**
     *
     * @deprecated Use InputStream executeShell(String command, String... args) method instead. Together with
     * Stream.copy(in, out), it is possible to achieve the same effect.
     */
    @Deprecated
    public void executeShell(OutputStream output, String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "shell:" + shellLine.toString());
        if (output != null) {
        	AdbFilterOutputStream out = new AdbFilterOutputStream(output);
        	try {
        		transport.readResponseTo(out);
        	} finally {
        		out.close();
        	}
        }
    }

    /** <p>Execute a command with raw binary output.</p>
     *
     * <p>Support for this command was added in Lollipop (Android 5.0), and is the recommended way to transmit binary
     * data with that version or later. For earlier versions of Android, use
     * {@link #executeShell(String, String...)}.</p>
     *
     * @param command main command to run, e.g. "screencap"
     * @param args arguments to the command, e.g. "-p".
     * @return combined stdout/stderr stream.
     * @throws IOException
     * @throws JadbException
     */
    public InputStream execute(String command, String... args) throws IOException, JadbException {
        Transport transport = getTransport();
        StringBuilder shellLine = buildCmdLine(command, args);
        send(transport, "exec:" + shellLine.toString());
        return new BufferedInputStream(transport.getInputStream());
    }

    /**
     * Builds a command line string from the command and its arguments.
     *
     * @param command the command.
     * @param args the list of arguments.
     * @return the command line.
     */
    private StringBuilder buildCmdLine(String command, String... args) {
        StringBuilder shellLine = new StringBuilder(command);
        for (String arg : args) {
            shellLine.append(" ");
            shellLine.append(Bash.quote(arg));
        }
        return shellLine;
    }

    public List<RemoteFile> list(String remotePath) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("LIST", remotePath);

        List<RemoteFile> result = new ArrayList<>();
        for (RemoteFileRecord dent = sync.readDirectoryEntry(); dent != RemoteFileRecord.DONE; dent = sync.readDirectoryEntry()) {
            result.add(dent);
        }
        return result;
    }

    public void push(InputStream source, long lastModified, int mode, RemoteFile remote) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("SEND", remote.getPath() + "," + Integer.toString(mode));

        sync.sendStream(source);

        sync.sendStatus("DONE", (int) lastModified);
        sync.verifyStatus();
    }

    public void push(File local, RemoteFile remote) throws IOException, JadbException {
        try (FileInputStream fileStream = new FileInputStream(local)) {
            push(fileStream, local.lastModified(), DEFAULT_MODE, remote);
        }
    }

    public void pull(RemoteFile remote, OutputStream destination) throws IOException, JadbException {
        Transport transport = getTransport();
        SyncTransport sync = transport.startSync();
        sync.send("RECV", remote.getPath());

        sync.readChunksTo(destination);
    }

    public void pull(RemoteFile remote, File local) throws IOException, JadbException {
        try (FileOutputStream fileStream = new FileOutputStream(local)) {
            pull(remote, fileStream);
        }
    }

    private void send(Transport transport, String command) throws IOException, JadbException {
        transport.send(command);
        transport.verifyResponse();
    }

    @Override
    public String toString() {
        return "Android Device with serial " + serial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JadbDevice other = (JadbDevice) obj;
        if (serial == null) {
            return other.serial == null;
        }
        return serial.equals(other.serial);
    }
}
