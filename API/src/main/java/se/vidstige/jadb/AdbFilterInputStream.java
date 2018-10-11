package se.vidstige.jadb;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdbFilterInputStream extends FilterInputStream {
    public AdbFilterInputStream(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public int read() throws IOException {
        int b1 = in.read();
        if (b1 == 0x0d) {
            in.mark(1);
            int b2 = in.read();
            if (b2 == 0x0a) {
                return b2;
            }
            in.reset();
        }
        return b1;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int n = 0;
        for (int i = 0; i < length; i++) {
            int b = read();
            if (b == -1) return n == 0 ? -1 : n;
            buffer[offset + n] = (byte) b;
            n++;

            // Return as soon as no more data is available (and at least one byte was read)
            if (in.available() <= 0) {
                return n;
            }
        }
        return n;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
}
