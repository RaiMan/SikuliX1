package se.vidstige.jadb;

import java.io.IOException;
import java.io.OutputStream;

public class AdbFilterOutputStream extends LookBackFilteringOutputStream {
    public AdbFilterOutputStream(OutputStream inner) {
        super(inner, 1);
    }

    @Override
    public void write(int c) throws IOException {
        if (!lookback().isEmpty()) {
            Byte last = lookback().getFirst();
            if (last != null && last == 0x0d && c == 0x0a) {
                unwrite();
            }
        }
        super.write(c);
    }
}
