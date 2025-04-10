package sun.net.www.http;

import java.io.*;
import java.util.*;
import sun.net.www.*;

/**
 * A <code>ChunkedInputStream</code> provides a stream for reading a body of
 * a http message that can be sent as a series of chunks, each with its own
 * size indicator. Optionally the last chunk can be followed by trailers 
 * containing entity-header fields.
 * <p>
 * A <code>ChunkedInputStream</code> is also <code>Hurryable</code> so it
 * can be hurried to the end of the stream if the bytes are available on
 * the underlying stream.
 */
public class ChunkedInputStream extends InputStream implements Hurryable {

    /**
     * The underlying stream
     */
    private InputStream in;

    /**
     * The <code>HttpClient<code> that should be notified when the chunked stream has
     * completed.
     */
    private HttpClient hc;

    /**
     * The <code>MessageHeader</code> that is populated with any optional trailer
     * that appear after the last chunk.
     */
    private MessageHeader responses;

    /**
     * The size, in bytes, of the chunk that is currently being read.
     * This size is only valid if the current position in the underlying
     * input stream is inside a chunk (ie: state == STATE_READING_CHUNK).
     */
    private int chunkSize;

    /**
     * The number of bytes read from the underlying stream for the current
     * chunk. This value is always in the range <code>0</code> through to
     * <code>chunkSize</code>
     */
    private int chunkRead;

    /**
     * The internal buffer array where chunk data is available for the 
     * application to read. 
     */
    private byte chunkData[] = new byte[4096];

    /**
     * The current position in the buffer. It contains the index
     * of the next byte to read from <code>chunkData</code>
     */
    private int chunkPos;

    /**
     * The index one greater than the index of the last valid byte in the
     * buffer. This value is always in the range <code>0</code> through 
     * <code>chunkData.length</code>.
     */
    private int chunkCount;

    /**
     * The internal buffer where bytes from the underlying stream can be
     * read. It may contain bytes representing chunk-size, chunk-data, or 
     * trailer fields. 
     */
    private byte rawData[] = new byte[32];

    /**
     * The current position in the buffer. It contains the index
     * of the next byte to read from <code>rawData</code>
     */
    private int rawPos;

    /**
     * The index one greater than the index of the last valid byte in the
     * buffer. This value is always in the range <code>0</code> through 
     * <code>rawData.length</code>.
     */
    private int rawCount;

    /**
     * Indicates if an error was encountered when processing the chunked
     * stream. 
     */
    private boolean error;

    /**
     * Indicates if the chunked stream has been closed using the 
     * <code>close</close> method.
     */
    private boolean closed;

    /**
     * State to indicate that next field should be :-
     *	chunk-size [ chunk-extension ] CRLF
     */
    static final int STATE_AWAITING_CHUNK_HEADER = 1;

    /**
     * State to indicate that we are currently reading the chunk-data.
     */
    static final int STATE_READING_CHUNK = 2;

    /**
     * Indicates that a chunk has been completely read and the next 
     * fields to be examine should be CRLF
     */
    static final int STATE_AWAITING_CHUNK_EOL = 3;

    /**
     * Indicates that all chunks have been read and the next field
     * should be optional trailers or an indication that the chunked
     * stream is complete.
     */
    static final int STATE_AWAITING_TRAILERS = 4;

    /**
     * State to indicate that the chunked stream is complete and
     * no further bytes should be read from the underlying stream.
     */
    static final int STATE_DONE = 5;

    /**
     * Indicates the current state.
     */
    private int state;

    /**
     * Check to make sure that this stream has not been closed.
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("stream is closed");
        }
    }

    /**
     * Ensures there is <code>size</code> bytes available in
     * <code>rawData<code>. This requires that we either 
     * shift the bytes in use to the begining of the buffer
     * or allocate a large buffer with sufficient space available.
     */
    private void ensureRawAvailable(int size) {
        if (rawCount + size > rawData.length) {
            int used = rawCount - rawPos;
            if (used + size > rawData.length) {
                byte tmp[] = new byte[used + size];
                if (used > 0) {
                    System.arraycopy(rawData, rawPos, tmp, 0, used);
                }
                rawData = tmp;
            } else {
                if (used > 0) {
                    System.arraycopy(rawData, rawPos, rawData, 0, used);
                }
            }
            rawCount = used;
            rawPos = 0;
        }
    }

    /**
     * Close the underlying input stream by either returning it to the
     * keep alive cache or closing the stream.
     * <p>
     * As a chunked stream is inheritly persistent (see HTTP 1.1 RFC) the
     * underlying stream can be returned to the keep alive cache if the
     * stream can be completely read without error.
     */
    private void closeUnderlying() throws IOException {
        if (in == null) {
            return;
        }
        if (!error && state == STATE_DONE) {
            hc.finished();
        } else {
            if (!hurry()) {
                hc.closeServer();
            }
        }
        in = null;
    }

    /**
     * Attempt to read the remainder of a chunk directly into the
     * caller's buffer.
     * <p>
     * Return the number of bytes read.
     */
    private int fastRead(byte[] b, int off, int len) throws IOException {
        int remaining = chunkSize - chunkRead;
        int cnt = (remaining < len) ? remaining : len;
        if (cnt > 0) {
            int nread;
            try {
                nread = in.read(b, off, cnt);
            } catch (IOException e) {
                error = true;
                throw e;
            }
            if (nread > 0) {
                chunkRead += nread;
                if (chunkRead >= chunkSize) {
                    state = STATE_AWAITING_CHUNK_EOL;
                }
                return nread;
            }
            error = true;
            throw new IOException("Premature EOF");
        } else {
            return 0;
        }
    }

    /**
     * Process any outstanding bytes that have already been read into 
     * <code>rawData</code>.
     * <p>
     * The parsing of the chunked stream is performed as a state machine with
     * <code>state</code> representing the current state of the processing.
     * <p>
     * Returns when either all the outstanding bytes in rawData have been
     * processed or there is insufficient bytes available to continue
     * processing. When the latter occurs <code>rawPos</code> will not have
     * been updated and thus the processing can be restarted once further
     * bytes have been read into <code>rawData</code>.
     */
    private void processRaw() throws IOException {
        int pos;
        int i;
        while (state != STATE_DONE) {
            switch(state) {
                case STATE_AWAITING_CHUNK_HEADER:
                    pos = rawPos;
                    while (pos < rawCount) {
                        if (rawData[pos] == '\n') {
                            break;
                        }
                        pos++;
                    }
                    if (pos >= rawCount) {
                        return;
                    }
                    String header = new String(rawData, rawPos, pos - rawPos + 1);
                    for (i = 0; i < header.length(); i++) {
                        if (Character.digit(header.charAt(i), 16) == -1) break;
                    }
                    try {
                        chunkSize = Integer.parseInt(header.substring(0, i), 16);
                    } catch (NumberFormatException e) {
                        error = true;
                        throw new IOException("Bogus chunk size");
                    }
                    rawPos = pos + 1;
                    chunkRead = 0;
                    if (chunkSize > 0) {
                        state = STATE_READING_CHUNK;
                    } else {
                        state = STATE_AWAITING_TRAILERS;
                    }
                    break;
                case STATE_READING_CHUNK:
                    if (rawPos >= rawCount) {
                        return;
                    }
                    int copyLen = Math.min(chunkSize - chunkRead, rawCount - rawPos);
                    if (chunkData.length < chunkCount + copyLen) {
                        int cnt = chunkCount - chunkPos;
                        if (chunkData.length < cnt + copyLen) {
                            byte tmp[] = new byte[cnt + copyLen];
                            System.arraycopy(chunkData, chunkPos, tmp, 0, cnt);
                            chunkData = tmp;
                        } else {
                            System.arraycopy(chunkData, chunkPos, chunkData, 0, cnt);
                        }
                        chunkPos = 0;
                        chunkCount = cnt;
                    }
                    System.arraycopy(rawData, rawPos, chunkData, chunkCount, copyLen);
                    rawPos += copyLen;
                    chunkCount += copyLen;
                    chunkRead += copyLen;
                    if (chunkSize - chunkRead <= 0) {
                        state = STATE_AWAITING_CHUNK_EOL;
                    } else {
                        return;
                    }
                    break;
                case STATE_AWAITING_CHUNK_EOL:
                    if (rawPos + 1 >= rawCount) {
                        return;
                    }
                    if (rawData[rawPos] != '\r') {
                        error = true;
                        throw new IOException("missing CR");
                    }
                    if (rawData[rawPos + 1] != '\n') {
                        error = true;
                        throw new IOException("missing LF");
                    }
                    rawPos += 2;
                    state = STATE_AWAITING_CHUNK_HEADER;
                    break;
                case STATE_AWAITING_TRAILERS:
                    pos = rawPos;
                    while (pos < rawCount) {
                        if (rawData[pos] == '\n') {
                            break;
                        }
                        pos++;
                    }
                    if (pos >= rawCount) {
                        return;
                    }
                    if (pos == rawPos) {
                        error = true;
                        throw new IOException("LF should be proceeded by CR");
                    }
                    if (rawData[pos - 1] != '\r') {
                        error = true;
                        throw new IOException("LF should be proceeded by CR");
                    }
                    if (pos == (rawPos + 1)) {
                        state = STATE_DONE;
                        closeUnderlying();
                        return;
                    }
                    String trailer = new String(rawData, rawPos, pos - rawPos);
                    i = trailer.indexOf(':');
                    if (i == -1) {
                        throw new IOException("Malformed tailer - format should be key:value");
                    }
                    String key = (trailer.substring(0, i)).trim();
                    String value = (trailer.substring(i + 1, trailer.length())).trim();
                    responses.add(key, value);
                    rawPos = pos + 1;
                    break;
            }
        }
    }

    /**
     * Reads any available bytes from the underlying stream into 
     * <code>rawData</code> and returns the number of bytes of
     * chunk data available in <code>chunkData</code> that the
     * application can read.
     */
    private int readAheadNonBlocking() throws IOException {
        int avail = in.available();
        if (avail > 0) {
            ensureRawAvailable(avail);
            int nread;
            try {
                nread = in.read(rawData, rawCount, avail);
            } catch (IOException e) {
                error = true;
                throw e;
            }
            if (nread < 0) {
                error = true;
                return -1;
            }
            rawCount += nread;
            processRaw();
        }
        return chunkCount - chunkPos;
    }

    /**
     * Reads from the underlying stream until there is chunk data
     * available in <code>chunkData</code> for the application to
     * read.
     */
    private int readAheadBlocking() throws IOException {
        do {
            if (state == STATE_DONE) {
                return -1;
            }
            ensureRawAvailable(32);
            int nread;
            try {
                nread = in.read(rawData, rawCount, rawData.length - rawCount);
            } catch (IOException e) {
                error = true;
                throw e;
            }
            if (nread < 0) {
                error = true;
                throw new IOException("Premature EOF");
            }
            rawCount += nread;
            processRaw();
        } while (chunkCount <= 0);
        return chunkCount - chunkPos;
    }

    /**
     * Read ahead in either blocking or non-blocking mode. This method
     * is typically used when we run out of available bytes in 
     * <code>chunkData<code> or we need to determine how many bytes
     * are available on the input stream.
     */
    private int readAhead(boolean allowBlocking) throws IOException {
        if (state == STATE_DONE) {
            return -1;
        }
        if (chunkPos >= chunkCount) {
            chunkCount = 0;
            chunkPos = 0;
        }
        if (allowBlocking) {
            return readAheadBlocking();
        } else {
            return readAheadNonBlocking();
        }
    }

    /**
     * Creates a <code>ChunkedInputStream</code> and saves its  arguments, for 
     * later use.
     *
     * @param   in   the underlying input stream.
     * @param	hc   the HttpClient
     * @param	responses   the MessageHeader that should be populated with optional
     *			    trailers.
     */
    public ChunkedInputStream(InputStream in, HttpClient hc, MessageHeader responses) throws IOException {
        this.in = in;
        this.responses = responses;
        this.hc = hc;
        state = STATE_AWAITING_CHUNK_HEADER;
    }

    /**
     * See
     * the general contract of the <code>read</code>
     * method of <code>InputStream</code>.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public synchronized int read() throws IOException {
        ensureOpen();
        if (chunkPos >= chunkCount) {
            if (readAhead(true) <= 0) {
                return -1;
            }
        }
        return chunkData[chunkPos++] & 0xff;
    }

    /**
     * Reads bytes from this stream into the specified byte array, starting at 
     * the given offset.
     *
     * @param      b     destination buffer.
     * @param      off   offset at which to start storing bytes.
     * @param      len   maximum number of bytes to read.
     * @return     the number of bytes read, or <code>-1</code> if the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized int read(byte b[], int off, int len) throws IOException {
        ensureOpen();
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        int avail = chunkCount - chunkPos;
        if (avail <= 0) {
            if (state == STATE_READING_CHUNK) {
                return fastRead(b, off, len);
            }
            avail = readAhead(true);
            if (avail < 0) {
                return -1;
            }
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(chunkData, chunkPos, b, off, cnt);
        chunkPos += cnt;
        return cnt;
    }

    /**
     * Returns the number of bytes that can be read from this input 
     * stream without blocking. 
     *
     * @return     the number of bytes that can be read from this input
     *             stream without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public synchronized int available() throws IOException {
        ensureOpen();
        int avail = readAhead(false);
        if (avail < 0) {
            return 0;
        } else {
            return avail;
        }
    }

    /**
     * Close the stream by either returning the connection to the
     * keep alive cache or closing the underlying stream. 
     * <p>
     * If the chunked response hasn't been completely read we 
     * try to "hurry" to the end of the response. If this is 
     * possible (without blocking) then the connection can be 
     * returned to the keep alive cache.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closeUnderlying();
        closed = true;
    }

    /** 
     * Hurry the input stream by reading everything from the underlying
     * stream. If the last chunk (and optional trailers) can be read without
     * blocking then the stream is considered hurried.
     * <p>
     * Note that if an error has occured or we can't get to last chunk 
     * without blocking then this stream can't be hurried and should be
     * closed.
     */
    public synchronized boolean hurry() {
        if (in == null || error) {
            return false;
        }
        try {
            readAhead(false);
        } catch (Exception e) {
            return false;
        }
        if (error) {
            return false;
        }
        return (state == STATE_DONE);
    }
}
