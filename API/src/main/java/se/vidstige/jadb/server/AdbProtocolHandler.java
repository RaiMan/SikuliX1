/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package se.vidstige.jadb.server;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;
import se.vidstige.jadb.SyncTransport;

import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class AdbProtocolHandler implements Runnable {
    private final Socket socket;
    private final AdbResponder responder;
    private AdbDeviceResponder selected;

    public AdbProtocolHandler(Socket socket, AdbResponder responder) {
        this.socket = socket;
        this.responder = responder;
    }

    private AdbDeviceResponder findDevice(String serial) throws ProtocolException {
        for (AdbDeviceResponder d : responder.getDevices()) {
            if (d.getSerial().equals(serial)) return d;
        }
        throw new ProtocolException("'" + serial + "' not connected");
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (IOException e) {
            if (e.getMessage() != null) // thrown when exiting for some reason
                System.out.println("IO Error: " + e.getMessage());
        }
    }

    private void runServer() throws IOException {
        try (
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            //noinspection StatementWithEmptyBody
            while (processCommand(input, output)) {
                // nothing to do here
            }
        }
    }

    private boolean processCommand(DataInput input, DataOutputStream output) throws IOException {
        String command = readCommand(input);
        responder.onCommand(command);

        try {
            if ("host:version".equals(command)) {
                hostVersion(output);
            } else if ("host:transport-any".equals(command)) {
                hostTransportAny(output);
            } else if ("host:devices".equals(command)) {
                hostDevices(output);
            } else if (command.startsWith("host:transport:")) {
                hostTransport(output, command);
            } else if ("sync:".equals(command)) {
                sync(output, input);
            } else if (command.startsWith("shell:")) {
                shell(input, output, command);
                return false;
            } else if ("host:get-state".equals(command)) {
                hostGetState(output);
            } else if (command.startsWith("host-serial:")) {
                hostSerial(output, command);
            } else {
                throw new ProtocolException("Unknown command: " + command);
            }
        } catch (ProtocolException e) {
            output.writeBytes("FAIL");
            send(output, e.getMessage());
        }
        output.flush();
        return true;
    }

    private void hostSerial(DataOutput output, String command) throws IOException {
        String[] strs = command.split(":",0);
        if (strs.length != 3) {
            throw new ProtocolException("Invalid command: " + command);
        }

        String serial = strs[1];
        boolean found = false;
        output.writeBytes("OKAY");
        for (AdbDeviceResponder d : responder.getDevices()) {
            if (d.getSerial().equals(serial)) {
                send(output, d.getType());
                found = true;
                break;
            }
        }

        if (!found) {
            send(output, "unknown");
        }
    }

    private void hostGetState(DataOutput output) throws IOException {
        // TODO: Check so that exactly one device is selected.
        AdbDeviceResponder device = responder.getDevices().get(0);
        output.writeBytes("OKAY");
        send(output, device.getType());
    }

    private void shell(DataInput input, DataOutputStream output, String command) throws IOException {
        String shellCommand = command.substring("shell:".length());
        output.writeBytes("OKAY");
        shell(shellCommand, output, input);
    }

    private void hostTransport(DataOutput output, String command) throws IOException {
        String serial = command.substring("host:transport:".length());
        selected = findDevice(serial);
        output.writeBytes("OKAY");
    }

    private void hostDevices(DataOutput output) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(tmp);
        for (AdbDeviceResponder d : responder.getDevices()) {
            writer.writeBytes(d.getSerial() + "\t" + d.getType() + "\n");
        }
        output.writeBytes("OKAY");
        send(output, new String(tmp.toByteArray(), StandardCharsets.UTF_8));
    }

    private void hostTransportAny(DataOutput output) throws IOException {
        // TODO: Check so that exactly one device is selected.
        selected = responder.getDevices().get(0);
        output.writeBytes("OKAY");
    }

    private void hostVersion(DataOutput output) throws IOException {
        output.writeBytes("OKAY");
        send(output, String.format("%04x", responder.getVersion()));
    }

    private void shell(String command, DataOutputStream stdout, DataInput stdin) throws IOException {
        selected.shell(command, stdout, stdin);
    }

    private int readInt(DataInput input) throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    private int readHexInt(DataInput input) throws IOException {
        return Integer.parseInt(readString(input, 4), 16);
    }

    private String readString(DataInput input, int length) throws IOException {
        byte[] responseBuffer = new byte[length];
        input.readFully(responseBuffer);
        return new String(responseBuffer, StandardCharsets.UTF_8);
    }

    private String readCommand(DataInput input) throws IOException {
        int length = readHexInt(input);
        return readString(input, length);
    }

    private void sync(DataOutput output, DataInput input) throws IOException {
        output.writeBytes("OKAY");
        try {
            String id = readString(input, 4);
            int length = readInt(input);
            if ("SEND".equals(id)) {
                syncSend(output, input, length);
            } else if ("RECV".equals(id)) {
                syncRecv(output, input, length);
            } else throw new JadbException("Unknown sync id " + id);
        } catch (JadbException e) { // sync response with a different type of fail message
            SyncTransport sync = getSyncTransport(output, input);
            sync.send("FAIL", e.getMessage());
        }
    }

    private void syncRecv(DataOutput output, DataInput input, int length) throws IOException, JadbException {
        String remotePath = readString(input, length);
        SyncTransport transport = getSyncTransport(output, input);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        selected.filePulled(new RemoteFile(remotePath), buffer);
        transport.sendStream(new ByteArrayInputStream(buffer.toByteArray()));
        transport.sendStatus("DONE", 0); // ignored
    }

    private void syncSend(DataOutput output, DataInput input, int length) throws IOException, JadbException {
        String remotePath = readString(input, length);
        int idx = remotePath.lastIndexOf(',');
        String path = remotePath;
        int mode = 0666;
        if (idx > 0) {
            path = remotePath.substring(0, idx);
            mode = Integer.parseInt(remotePath.substring(idx + 1));
        }
        SyncTransport transport = getSyncTransport(output, input);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        transport.readChunksTo(buffer);
        selected.filePushed(new RemoteFile(path), mode, buffer);
        transport.sendStatus("OKAY", 0); // 0 = ignored
    }

    private String getCommandLength(String command) {
        return String.format("%04x", command.length());
    }

    private void send(DataOutput writer, String response) throws IOException {
        writer.writeBytes(getCommandLength(response));
        writer.writeBytes(response);
    }

    private SyncTransport getSyncTransport(DataOutput output, DataInput input) {
        return new SyncTransport(output, input);
    }
}
