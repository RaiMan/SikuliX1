package se.vidstige.jadb;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;

public class LookBackFilteringOutputStream extends FilterOutputStream {
    private final ArrayDeque<Byte> buffer;
    private final int lookBackBufferSize;

    protected LookBackFilteringOutputStream(OutputStream inner, int lookBackBufferSize)
    {
        super(inner);
        this.lookBackBufferSize = lookBackBufferSize;
        this.buffer = new ArrayDeque<>(lookBackBufferSize);
    }

    protected void unwrite() {
        buffer.removeFirst();
    }

    protected ArrayDeque<Byte> lookback() {
        return buffer;
    }

    @Override
    public void write(int c) throws IOException {
        buffer.addLast((byte) c);
        flushBuffer(lookBackBufferSize);
    }

    @Override
    public void flush() throws IOException {
        flushBuffer(0);
        out.flush();
    }

    private void flushBuffer(int size) throws IOException {
        while (buffer.size() > size) {
            Byte b = buffer.removeFirst();
            out.write(b);
        }
    }
}
