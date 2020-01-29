/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package se.vidstige.jadb;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class Transport {

    private final OutputStream outputStream;
    private final InputStream inputStream;

    private Transport(OutputStream outputStream, InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public Transport(Socket socket) throws IOException {
        this(socket.getOutputStream(), socket.getInputStream());
    }

    public String readString() throws IOException {
        String encodedLength = readString(4);
        int length = Integer.parseInt(encodedLength, 16);
        return readString(length);
    }

    public void readResponseTo(OutputStream output) throws IOException {
        Stream.copy(inputStream, output);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void verifyResponse() throws IOException, JadbException {
        String response = readString(4);
        if (!"OKAY".equals(response)) {
            String error = readString();
            throw new JadbException("command failed: " + error);
        }
    }

    public String readString(int length) throws IOException {
        DataInput reader = new DataInputStream(inputStream);
        byte[] responseBuffer = new byte[length];
        reader.readFully(responseBuffer);
        return new String(responseBuffer, StandardCharsets.UTF_8);
    }

    private String getCommandLength(String command) {
        return String.format("%04x", command.length());
    }

    public void send(String command) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(getCommandLength(command));
        writer.write(command);
        writer.flush();
    }

    public SyncTransport startSync() throws IOException, JadbException {
        send("sync:");
        verifyResponse();
        return new SyncTransport(outputStream, inputStream);
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }
}
