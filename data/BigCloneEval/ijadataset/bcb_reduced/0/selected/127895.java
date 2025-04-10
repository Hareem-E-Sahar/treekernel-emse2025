package java.io;

/**
 * Piped character-input streams.
 *
 * @version 	1.12, 00/02/02
 * @author	Mark Reinhold
 * @since	JDK1.1
 */
public class PipedReader extends Reader {

    boolean closedByWriter = false;

    boolean closedByReader = false;

    boolean connected = false;

    Thread readSide;

    Thread writeSide;

    /**
     * The size of the pipe's circular input buffer.
     */
    static final int PIPE_SIZE = 1024;

    /**
     * The circular buffer into which incoming data is placed.
     */
    char buffer[] = new char[PIPE_SIZE];

    /**
     * The index of the position in the circular buffer at which the 
     * next character of data will be stored when received from the connected 
     * piped writer. <code>in&lt;0</code> implies the buffer is empty, 
     * <code>in==out</code> implies the buffer is full
     */
    int in = -1;

    /**
     * The index of the position in the circular buffer at which the next 
     * character of data will be read by this piped reader.
     */
    int out = 0;

    /**
     * Creates a <code>PipedReader</code> so
     * that it is connected to the piped writer
     * <code>src</code>. Data written to <code>src</code> 
     * will then be  available as input from this stream.
     *
     * @param      src   the stream to connect to.
     * @exception  IOException  if an I/O error occurs.
     */
    public PipedReader(PipedWriter src) throws IOException {
        connect(src);
    }

    /**
     * Creates a <code>PipedReader</code> so
     * that it is not  yet connected. It must be
     * connected to a <code>PipedWriter</code>
     * before being used.
     *
     * @see     java.io.PipedReader#connect(java.io.PipedWriter)
     * @see     java.io.PipedWriter#connect(java.io.PipedReader)
     */
    public PipedReader() {
    }

    /**
     * Causes this piped reader to be connected
     * to the piped  writer <code>src</code>.
     * If this object is already connected to some
     * other piped writer, an <code>IOException</code>
     * is thrown.
     * <p>
     * If <code>src</code> is an
     * unconnected piped writer and <code>snk</code>
     * is an unconnected piped reader, they
     * may be connected by either the call:
     * <p>
     * <pre><code>snk.connect(src)</code> </pre> 
     * <p>
     * or the call:
     * <p>
     * <pre><code>src.connect(snk)</code> </pre> 
     * <p>
     * The two
     * calls have the same effect.
     *
     * @param      src   The piped writer to connect to.
     * @exception  IOException  if an I/O error occurs.
     */
    public void connect(PipedWriter src) throws IOException {
        src.connect(this);
    }

    /**
     * Receives a char of data.  This method will block if no input is
     * available.
     */
    synchronized void receive(int c) throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }
        writeSide = Thread.currentThread();
        while (in == out) {
            if ((readSide != null) && !readSide.isAlive()) {
                throw new IOException("Pipe broken");
            }
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (char) c;
        if (in >= buffer.length) {
            in = 0;
        }
    }

    /**
     * Receives data into an array of characters.  This method will
     * block until some input is available. 
     */
    synchronized void receive(char c[], int off, int len) throws IOException {
        while (--len >= 0) {
            receive(c[off++]);
        }
    }

    /**
     * Notifies all waiting threads that the last character of data has been
     * received.
     */
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    /**
     * Reads the next character of data from this piped stream.
     * If no character is available because the end of the stream 
     * has been reached, the value <code>-1</code> is returned. 
     * This method blocks until input data is available, the end of
     * the stream is detected, or an exception is thrown. 
     *
     * If a thread was providing data characters
     * to the connected piped writer, but
     * the  thread is no longer alive, then an
     * <code>IOException</code> is thrown.
     *
     * @return     the next character of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if the pipe is broken.
     */
    public synchronized int read() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive() && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }
        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("Pipe broken");
            }
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++];
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            in = -1;
        }
        return ret;
    }

    /**
     * Reads up to <code>len</code> characters of data from this piped
     * stream into an array of characters. Less than <code>len</code> characters
     * will be read if the end of the data stream is reached. This method 
     * blocks until at least one character of input is available. 
     * If a thread was providing data characters to the connected piped output, 
     * but the thread is no longer alive, then an <code>IOException</code> 
     * is thrown.
     *
     * @param      cbuf     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of characters read.
     * @return     the total number of characters read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized int read(char cbuf[], int off, int len) throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive() && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        int c = read();
        if (c < 0) {
            return -1;
        }
        cbuf[off] = (char) c;
        int rlen = 1;
        while ((in >= 0) && (--len > 0)) {
            cbuf[off + rlen] = buffer[out++];
            rlen++;
            if (out >= buffer.length) {
                out = 0;
            }
            if (in == out) {
                in = -1;
            }
        }
        return rlen;
    }

    /**
     * Tell whether this stream is ready to be read.  A piped character
     * stream is ready if the circular buffer is not empty.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public synchronized boolean ready() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive() && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }
        if (in < 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Closes this piped stream and releases any system resources 
     * associated with the stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        in = -1;
        closedByReader = true;
    }
}
