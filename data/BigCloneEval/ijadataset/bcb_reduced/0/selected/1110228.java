package org.rrd4j.core;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;
import sun.nio.ch.DirectBuffer;

/**
 * Backend which is used to store RRD data to ordinary disk files
 * using java.nio.* package. This is the default backend engine. 
 */
public class RrdNioBackend extends RrdFileBackend {

    private static final Timer fileSyncTimer = new Timer(true);

    private MappedByteBuffer byteBuffer;

    private TimerTask syncTask = new TimerTask() {

        public void run() {
            sync();
        }
    };

    /**
	 * Creates RrdFileBackend object for the given file path, backed by java.nio.* classes.
	 *
	 * @param path       Path to a file
	 * @param readOnly   True, if file should be open in a read-only mode. False otherwise
	 * @param syncPeriod See {@link RrdNioBackendFactory#setSyncPeriod(int)} for explanation
	 * @throws IOException Thrown in case of I/O error
	 */
    protected RrdNioBackend(String path, boolean readOnly, int syncPeriod) throws IOException {
        super(path, readOnly);
        try {
            mapFile();
            if (!readOnly) {
                fileSyncTimer.schedule(syncTask, syncPeriod * 1000L, syncPeriod * 1000L);
            }
        } catch (IOException ioe) {
            super.close();
            throw ioe;
        }
    }

    private void mapFile() throws IOException {
        long length = getLength();
        if (length > 0) {
            FileChannel.MapMode mapMode = readOnly ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;
            byteBuffer = file.getChannel().map(mapMode, 0, length);
        }
    }

    private void unmapFile() {
        if (byteBuffer != null) {
            if (byteBuffer instanceof DirectBuffer) {
                ((DirectBuffer) byteBuffer).cleaner().clean();
            }
            byteBuffer = null;
        }
    }

    /**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 *
	 * @param newLength Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
    protected synchronized void setLength(long newLength) throws IOException {
        unmapFile();
        super.setLength(newLength);
        mapFile();
    }

    /**
	 * Writes bytes to the underlying RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b      Bytes to be written.
	 */
    protected synchronized void write(long offset, byte[] b) throws IOException {
        if (byteBuffer != null) {
            byteBuffer.position((int) offset);
            byteBuffer.put(b);
        } else {
            throw new IOException("Write failed, file " + getPath() + " not mapped for I/O");
        }
    }

    /**
	 * Reads a number of bytes from the RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b      Buffer which receives bytes read from the file.
	 */
    protected synchronized void read(long offset, byte[] b) throws IOException {
        if (byteBuffer != null) {
            byteBuffer.position((int) offset);
            byteBuffer.get(b);
        } else {
            throw new IOException("Read failed, file " + getPath() + " not mapped for I/O");
        }
    }

    /**
	 * Closes the underlying RRD file.
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
    public synchronized void close() throws IOException {
        try {
            if (syncTask != null) {
                syncTask.cancel();
            }
            sync();
            unmapFile();
        } finally {
            super.close();
        }
    }

    /**
	 * This method forces all data cached in memory but not yet stored in the file,
	 * to be stored in it.
	 */
    protected synchronized void sync() {
        if (byteBuffer != null) {
            byteBuffer.force();
        }
    }
}
