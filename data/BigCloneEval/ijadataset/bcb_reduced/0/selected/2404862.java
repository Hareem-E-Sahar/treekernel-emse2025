package org.python.core.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyObject;
import org.python.core.PyString;

/**
 * Base class for text I/O.
 *
 * This class provides a character and line based interface to stream
 * I/O.
 *
 * @author Philip Jenvey
 */
public abstract class TextIOBase extends IOBase {

    /** The size of chunks read for readline */
    public static final int CHUNK_SIZE = 300;

    /** Byte representation of the Carriage Return character */
    protected static final byte CR_BYTE = 13;

    /** The underlying buffered i/o stream */
    protected BufferedIOBase bufferedIO;

    /** The readahead buffer. Though the underlying stream is
     * sometimes buffered, readahead is specific to the TextIO layer
     * to mostly benefit readline processing */
    protected ByteBuffer readahead;

    /** Builds the final String returned from readline */
    protected StringBuilder builder;

    /** An interim buffer for builder; readline loops move bytes into
     * this array to avoid StringBuilder.append method calls */
    protected char[] interimBuilder;

    /**
     * Contruct a TextIOBase wrapping the given BufferedIOBase.
     *
     * @param bufferedIO a BufferedIOBase to wrap
     */
    public TextIOBase(BufferedIOBase bufferedIO) {
        this.bufferedIO = bufferedIO;
        readahead = ByteBuffer.allocate(CHUNK_SIZE);
        readahead.flip();
        builder = new StringBuilder(CHUNK_SIZE);
        interimBuilder = new char[CHUNK_SIZE];
    }

    /**
     * Read and return up to size bytes, contained in a String.
     *
     * Returns an empty String on EOF
     *
     * @param size the number of bytes to read
     * @return a String containing the bytes read
     */
    public String read(int size) {
        unsupported("read");
        return null;
    }

    /**
     * Read until EOF.
     *
     * @return a String containing the bytes read
     */
    public String readall() {
        unsupported("readall");
        return null;
    }

    /**
     * Read until size, newline or EOF.
     *
     * Returns an empty string if EOF is hit immediately.
     *
     * @param size the number of bytes to read
     * @return a String containing the bytes read
     */
    public String readline(int size) {
        unsupported("read");
        return null;
    }

    /**
     * Read into the given PyObject that implements the read-write
     * buffer interface (currently just a PyArray).
     *
     * @param buf a PyObject implementing the read-write buffer interface
     * @return the amount of data read as an int
     */
    public int readinto(PyObject buf) {
        if (!(buf instanceof PyArray)) {
            if (buf instanceof PyString) {
                throw Py.TypeError("Cannot use string as modifiable buffer");
            }
            throw Py.TypeError("argument 1 must be read-write buffer, not " + buf.getType().fastGetName());
        }
        PyArray array = (PyArray) buf;
        String read = read(array.__len__());
        for (int i = 0; i < read.length(); i++) {
            array.set(i, new PyString(read.charAt(i)));
        }
        return read.length();
    }

    /**
     * Write the given String to the IO stream.
     *
     * Returns the number of characters written.
     *
     * @param buf a String value
     * @return the number of characters written as an int
     */
    public int write(String buf) {
        unsupported("write");
        return -1;
    }

    @Override
    public long truncate(long pos) {
        long initialPos = tell();
        flush();
        pos = bufferedIO.truncate(pos);
        if (initialPos > pos) {
            seek(initialPos);
        }
        return pos;
    }

    @Override
    public void flush() {
        bufferedIO.flush();
    }

    @Override
    public void close() {
        bufferedIO.close();
    }

    @Override
    public long seek(long pos, int whence) {
        pos = bufferedIO.seek(pos, whence);
        clearReadahead();
        return pos;
    }

    @Override
    public long tell() {
        return bufferedIO.tell() - readahead.remaining();
    }

    @Override
    public RawIOBase fileno() {
        return bufferedIO.fileno();
    }

    @Override
    public boolean isatty() {
        return bufferedIO.isatty();
    }

    @Override
    public boolean readable() {
        return bufferedIO.readable();
    }

    @Override
    public boolean writable() {
        return bufferedIO.writable();
    }

    @Override
    public boolean closed() {
        return bufferedIO.closed();
    }

    @Override
    public InputStream asInputStream() {
        return bufferedIO.asInputStream();
    }

    @Override
    public OutputStream asOutputStream() {
        return bufferedIO.asOutputStream();
    }

    /**
     * Return the known Newline types, as a PyObject, encountered
     * while reading this file.
     *
     * Returns None for all modes except universal newline mode.
     *
     * @return a PyObject containing all encountered Newlines, or None
     */
    public PyObject getNewlines() {
        return Py.None;
    }

    /**
     * Return true if the file pointer is currently at EOF.
     *
     * If the file pointer is not at EOF, the readahead will contain
     * at least one byte after this method is called.
     *
     * @return true if the file pointer is currently at EOF
     */
    protected boolean atEOF() {
        return readahead.hasRemaining() ? false : readChunk() == 0;
    }

    /**
     * Read a chunk of data of size CHUNK_SIZE into the readahead
     * buffer. Returns the amount of data read.
     *
     * @return the amount of data read
     */
    protected int readChunk() {
        readahead.clear();
        if (readahead.remaining() > CHUNK_SIZE) {
            readahead.limit(readahead.position() + CHUNK_SIZE);
        }
        bufferedIO.read1(readahead);
        readahead.flip();
        return readahead.remaining();
    }

    /**
     * Read a chunk of data of the given size into the readahead
     * buffer. Returns the amount of data read.
     *
     * Enforces a minimum size of CHUNK_SIZE.
     *
     * @param size the desired size of the chunk
     * @return the amount of data read
     */
    protected int readChunk(int size) {
        if (size > CHUNK_SIZE) {
            readahead = ByteBuffer.allocate(size);
        } else {
            size = CHUNK_SIZE;
            readahead.clear().limit(size);
        }
        bufferedIO.readinto(readahead);
        readahead.flip();
        return readahead.remaining();
    }

    /**
     * Restore the readahead to its original size (CHUNK_SIZE) if it
     * was previously resized.
     *
     * The readahead contents are preserved. This method assumes it
     * contains a number of remaining elements less than or equal to
     * CHUNK_SIZE.
     *
     */
    protected void packReadahead() {
        if (readahead.capacity() > CHUNK_SIZE) {
            ByteBuffer old = readahead;
            readahead = ByteBuffer.allocate(CHUNK_SIZE);
            readahead.put(old);
            readahead.flip();
        }
    }

    /**
     * Clear and reset the readahead buffer.
     *
     */
    protected void clearReadahead() {
        readahead.clear().flip();
    }

    /**
     * Return the String result of the builder, and reset it/perform
     * cleanup on it.
     *
     * @return the result of builder.toString()
     */
    protected String drainBuilder() {
        String result = builder.toString();
        if (builder.capacity() > CHUNK_SIZE) {
            builder = new StringBuilder(CHUNK_SIZE);
        } else {
            builder.setLength(0);
        }
        return result;
    }
}
