package org.jrobin.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JRobin backend which is used to store RRD data to ordinary disk files
 * by using fast java.nio.* package. This is the default backend engine since JRobin 1.4.0.
 */
public class RrdNioByteBufferBackend extends RrdFileBackend {

    private ByteBuffer m_byteBuffer;

    private FileChannel m_ch;

    private static final ReadWriteLock m_readWritelock = new ReentrantReadWriteLock();

    private static final Lock m_readLock = m_readWritelock.readLock();

    private static final Lock m_writeLock = m_readWritelock.writeLock();

    /**
	 * Creates RrdFileBackend object for the given file path, backed by java.nio.* classes.
	 *
	 * @param path	   Path to a file
	 * @param m_readOnly   True, if file should be open in a read-only mode. False otherwise
	 * @param syncPeriod See {@link RrdNioBackendFactory#setSyncPeriod(int)} for explanation
	 * @throws IOException Thrown in case of I/O error
	 */
    protected RrdNioByteBufferBackend(final String path, final boolean readOnly) throws IOException, IllegalStateException {
        super(path, readOnly);
        if (file != null) {
            m_ch = file.getChannel();
            m_byteBuffer = ByteBuffer.allocate((int) m_ch.size());
            m_ch.read(m_byteBuffer, 0);
        } else {
            throw new IllegalStateException("File in base class is null.");
        }
    }

    /**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 *
	 * @param newLength Length of the RRD file
	 * @throws IOException 
	 * @throws IOException Thrown in case of I/O error.
	 */
    @Override
    protected void setLength(final long newLength) throws IOException {
        m_writeLock.lock();
        try {
            super.setLength(newLength);
            m_ch = file.getChannel();
            m_byteBuffer = ByteBuffer.allocate((int) newLength);
            m_ch.read(m_byteBuffer, 0);
            m_byteBuffer.position(0);
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
	 * Writes bytes to the underlying RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Bytes to be written.
	 */
    @Override
    protected void write(final long offset, final byte[] b) {
        m_writeLock.lock();
        try {
            m_byteBuffer.position((int) offset);
            m_byteBuffer.put(b);
        } finally {
            m_writeLock.unlock();
        }
    }

    /**
	 * Reads a number of bytes from the RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Buffer which receives bytes read from the file.
	 */
    @Override
    protected void read(final long offset, final byte[] b) {
        m_readLock.lock();
        try {
            m_byteBuffer.position((int) offset);
            m_byteBuffer.get(b);
        } finally {
            m_readLock.unlock();
        }
    }

    /**
	 * Closes the underlying RRD file.
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
    public void close() throws IOException {
        m_writeLock.lock();
        try {
            m_byteBuffer.position(0);
            if (!isReadOnly()) m_ch.write(m_byteBuffer, 0);
            super.close();
        } finally {
            m_writeLock.unlock();
        }
    }
}
