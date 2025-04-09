package org.hsqldb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

/**
 * This class is a wapper for a random access file such as that used for
 * CACHED table storage.
 *
 * The constructor takes a multiplier for positioning.
 * The seek(long position) method multiplies the position by the multiplier to
 * map to the underlying file.
 *
 * @author fredt@users
 * @version  1.7.2
 * @since  1.7.2
 */
class ScaledRAFile {

    static final int DATA_FILE_RAF = 0;

    static final int DATA_FILE_NIO = 1;

    final RandomAccessFile file;

    final int scale;

    final boolean readOnly;

    final String fileName;

    boolean isNio;

    static ScaledRAFile newScaledRAFile(String name, boolean readonly, int multiplier, int type) throws FileNotFoundException, IOException {
        if (type == DATA_FILE_RAF) {
            return new ScaledRAFile(name, readonly, multiplier);
        } else {
            try {
                Class.forName("java.nio.MappedByteBuffer");
                Class c = Class.forName("org.hsqldb.NIOScaledRAFile");
                Constructor constructor = c.getConstructor(new Class[] { String.class, boolean.class, int.class });
                return (ScaledRAFile) constructor.newInstance(new Object[] { name, new Boolean(readonly), new Integer(multiplier) });
            } catch (Exception e) {
                return new ScaledRAFile(name, readonly, multiplier);
            }
        }
    }

    ScaledRAFile(String name, boolean readonly, int multiplier) throws FileNotFoundException, IOException {
        file = new RandomAccessFile(name, readonly ? "r" : "rw");
        this.readOnly = readonly;
        scale = multiplier;
        fileName = name;
    }

    long length() throws IOException {
        return file.length();
    }

    /**
     * Some JVM's do not allow seek beyon end of file, so zeros are written
     * first in that case. Reported by bohgammer@users in Open Disucssion
     * Forum.
     */
    void seek(long position) throws IOException {
        if (file.length() < position) {
            file.seek(file.length());
            for (long ix = file.length(); ix < position; ix++) {
                file.write(0);
            }
        }
        file.seek(position);
    }

    long getFilePointer() throws IOException {
        return (file.getFilePointer() + scale - 1) / scale;
    }

    int read() throws IOException {
        return file.read();
    }

    void read(byte[] b, int offset, int length) throws IOException {
        file.readFully(b, offset, length);
    }

    int readInt() throws IOException {
        return file.readInt();
    }

    void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    void writeInt(int i) throws IOException {
        file.writeInt(i);
    }

    void close() throws IOException {
        file.close();
    }
}
