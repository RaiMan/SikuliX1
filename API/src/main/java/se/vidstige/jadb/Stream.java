package se.vidstige.jadb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class Stream {
    private Stream() {
        throw new IllegalStateException("Utility class");
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 10];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static String readAll(InputStream input, Charset charset) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        Stream.copy(input, tmp);
        return new String(tmp.toByteArray(), charset);
    }
}
