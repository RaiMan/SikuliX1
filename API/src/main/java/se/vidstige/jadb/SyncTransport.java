/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */

package se.vidstige.jadb;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by vidstige on 2014-03-19.
 */
public class SyncTransport {

    private final DataOutput output;
    private final DataInput input;

    public SyncTransport(OutputStream outputStream, InputStream inputStream) {
        output = new DataOutputStream(outputStream);
        input = new DataInputStream(inputStream);
    }

    public SyncTransport(DataOutput outputStream, DataInput inputStream) {
        output = outputStream;
        input = inputStream;
    }

    public void send(String syncCommand, String name) throws IOException {
        if (syncCommand.length() != 4) throw new IllegalArgumentException("sync commands must have length 4");
        output.writeBytes(syncCommand);
        output.writeInt(Integer.reverseBytes(name.length()));
        output.writeBytes(name);
    }

    public void sendStatus(String statusCode, int length) throws IOException {
        output.writeBytes(statusCode);
        output.writeInt(Integer.reverseBytes(length));
    }

    public void verifyStatus() throws IOException, JadbException {
        String status = readString(4);
        int length = readInt();
        if ("FAIL".equals(status)) {
            String error = readString(length);
            throw new JadbException(error);
        }
        if (!"OKAY".equals(status)) {
            throw new JadbException("Unknown error: " + status);
        }
    }

    private int readInt() throws IOException {
        return Integer.reverseBytes(input.readInt());
    }

    private String readString(int length) throws IOException {
        byte[] buffer = new byte[length];
        input.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public RemoteFileRecord readDirectoryEntry() throws IOException {
        String id = readString(4);
        int mode = readInt();
        int size = readInt();
        int time = readInt();
        int nameLength = readInt();
        String name = readString(nameLength);

        if (!"DENT".equals(id)) return RemoteFileRecord.DONE;
        return new RemoteFileRecord(name, mode, size, time);
    }

    private void sendChunk(byte[] buffer, int offset, int length) throws IOException {
        output.writeBytes("DATA");
        output.writeInt(Integer.reverseBytes(length));
        output.write(buffer, offset, length);
    }

    private int readChunk(byte[] buffer) throws IOException, JadbException {
        String id = readString(4);
        int n = readInt();
        if ("FAIL".equals(id)) {
            throw new JadbException(readString(n));
        }
        if (!"DATA".equals(id)) return -1;
        input.readFully(buffer, 0, n);
        return n;
    }

    public void sendStream(InputStream in) throws IOException {
        byte[] buffer = new byte[1024 * 64];
        int n = in.read(buffer);
        while (n != -1) {
            sendChunk(buffer, 0, n);
            n = in.read(buffer);
        }
    }

    public void readChunksTo(OutputStream stream) throws IOException, JadbException {
        byte[] buffer = new byte[1024 * 64];
        int n = readChunk(buffer);
        while (n != -1) {
            stream.write(buffer, 0, n);
            n = readChunk(buffer);
        }
    }
}
