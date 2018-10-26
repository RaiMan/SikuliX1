package se.vidstige.jadb.server;

import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by vidstige on 20/03/14.
 */
public interface AdbDeviceResponder {
    String getSerial();
    String getType();

    void filePushed(RemoteFile path, int mode, ByteArrayOutputStream buffer) throws JadbException;
    void filePulled(RemoteFile path, ByteArrayOutputStream buffer) throws JadbException, IOException;

    void shell(String command, DataOutputStream stdout, DataInput stdin) throws IOException;
}
