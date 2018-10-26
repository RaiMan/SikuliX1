package se.vidstige.jadb;

/**
 * Created by vidstige on 2014-03-19.
 */
class RemoteFileRecord extends RemoteFile {
    public static final RemoteFileRecord DONE = new RemoteFileRecord(null, 0, 0, 0);

    private final int mode;
    private final int size;
    private final long lastModified;

    public RemoteFileRecord(String name, int mode, int size, long lastModified) {
        super(name);
        this.mode = mode;
        this.size = size;
        this.lastModified = lastModified;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean isDirectory() {
        return (mode & (1 << 14)) == (1 << 14);
    }
}
