package org.mortbay.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Temporary buffer for bytes to be used in situations where bytes need to be buffered 
 * but total size of data is not known in advance and may potentially be very large.
 * Provides easy way to access small buffered data as byte[] or String.
 * Enables efficient memory-only handling of small data while automatically switching
 * to temporary file storage when data gets too big to fit in memory buffer.
 * It is highly efficient for both byte-per-byte and block I/O.
 * This class is not a FIFO - you can't mix reading and writing infinitely as all data
 * keep being buffered, not just unread data.
 * Mixing reads and writes may be inefficient in some situations but is fully supported.
 * <br>
 * Overall usage strategy: You first write data to the buffer using OutputStream 
 * returned by getOutputStream(), then examine data size using getLength() 
 * and isLarge() and either call getBytes() to get byte[],
 * getString() to get data as String or getInputStream() to read data using stream.
 * Instance of TempByteHolder can be safely and efficiently reused by calling clear().
 * When TempByteHolder is no longer needed you must call close() to ensure underlying
 * temporary file is closed and deleted.
 * <br><br>
 * <i>NOTE:</i> For performance, this class is not synchronized. If you need thread safety, 
 * use synchronized wrapper.<br>
 * This class can hold up to 2GB of data.
 * <br><br>
 * <i>SECURITY NOTE:</i> As data may be written to disk, don't use this for sensitive information.
 * @author  Jan Hlavat� &lt;hlavac AT code.cz&gt;
 */
public class TempByteHolder {

    byte[] _memory_buffer = null;

    /** buffer to use */
    boolean _file_mode = false;

    /** false: memory buffer mode (small data)
                                    true: temp file mode (large data) */
    int _window_size = 0;

    /** size of memory buffer */
    int _window_low = 0;

    /** offset of first byte in memory buffer */
    int _window_high = 0;

    /** offset of first byte after memory buffer */
    int _file_high = 0;

    /** offset of fist byte not yet written to temp file */
    int _write_pos = 0;

    /** offset of next byte to be writen; number of bytes written */
    int _read_pos = 0;

    /** offset of fist byte to be read */
    int _file_pos = -1;

    /** current temp file seek offset; -1 = unknown */
    int _mark_pos = 0;

    /** Instance of OutputStream is cached and reused. */
    TempByteHolder.OutputStream _output_stream = new TempByteHolder.OutputStream();

    /** Instance of InputStream is cached and reused. */
    TempByteHolder.InputStream _input_stream = null;

    /** Temporary directory to be used, or null for system default */
    File _temp_directory = null;

    /** File object representing temporary file. */
    File _tempfilef = null;

    /** Temporary file or null when none is used yet */
    RandomAccessFile _tempfile = null;

    /**
     * Creates a new instance of TempByteHolder allocating memory buffer of given capacity.
     * You should use reasonably large buffer for potentionally large data to improve
     * effect of caching for file operations (about 512 bytes).
     * @param in_memory_capacity Size in bytes of memory buffer to allocate.
     */
    public TempByteHolder(int in_memory_capacity) {
        this(new byte[in_memory_capacity], 0, 0);
    }

    /**
     * Creates a new instance of TempByteHolder using passed byte[] as memory buffer.
     * @param byte_array byte array to be used as memory buffer.
     */
    public TempByteHolder(byte[] byte_array) {
        this(byte_array, 0, 0);
    }

    /**
     * Creates a new instance of TempByteHolder using passed byte[] which
     * contains prefilled data as memory buffer.
     * @param byte_array byte array to be used as memory buffer.
     * @param offset offset of prefilled data in buffer.
     * @param prefilled_data_size number of bytes that contain valid data.
     */
    public TempByteHolder(byte[] byte_array, int offset, int prefilled_data_size) {
        if (byte_array == null) throw new NullPointerException();
        _window_size = byte_array.length;
        if ((offset < 0) || (offset > _window_size)) throw new IllegalArgumentException("Bad prefilled data offset");
        if ((offset + prefilled_data_size > _window_size) || (prefilled_data_size < 0)) throw new IllegalArgumentException("Bad prefilled data size");
        _memory_buffer = byte_array;
        _write_pos = prefilled_data_size;
        _window_low = -offset;
        _window_high = _window_size - offset;
    }

    protected void finalize() {
        try {
            close();
        } catch (IOException e) {
        }
    }

    /**
     * Erases all unread buffered data and prepares for next use cycle.
     * If temporary file was used, it is not closed/deleted yet as it may be needed again.
     */
    public void clear() {
        _file_mode = false;
        _write_pos = 0;
        _read_pos = 0;
        _window_low = 0;
        _window_high = _window_size;
        _file_high = 0;
        _mark_pos = 0;
    }

    /**
     * Clears all data and closes/deletes backing temporary file if used.
     * @throws IOException when something goes wrong.
     */
    public void close() throws IOException {
        clear();
        if (_tempfile != null) {
            _tempfile.close();
            _tempfile = null;
            _tempfilef.delete();
            _tempfilef = null;
        }
    }

    /**
     * Repositions InputStream at given offset within buffered data.
     * @throws IOException when something goes wrong.
     */
    public void seek(int offset) throws IOException {
        if ((offset <= _write_pos) && (offset >= 0)) {
            _read_pos = offset;
        } else throw new IOException("bad seek offset");
    }

    /**
     * Truncates buffered data to specified size. Can not be used to extend data.
     * Repositions OutputStream at the end of truncated data.
     * If current read offset or mark is past the new end of data, it is moved at the new end.
     */
    public void truncate(int offset) throws IOException {
        if ((offset < 0) || (offset > _write_pos)) throw new IOException("bad truncate offset");
        if (_read_pos > offset) _read_pos = offset;
        if (_mark_pos > offset) _mark_pos = offset;
        _write_pos = offset;
        if (_file_high > offset) _file_high = offset;
        moveWindow(_write_pos);
    }

    /**
     * Override directory to create temporary file in.
     * Does not affect already open temp file.
     * @param dir File object representing temporary directory.
     * May be null which means that system default
     * (java.io.tmpdir system property) should be used.
     * @throws IOException
     */
    public void setTempDirectory(File dir) throws IOException {
        File td = dir.getCanonicalFile();
        if (td.isDirectory()) {
            _temp_directory = td;
        }
    }

    /**
     * Returns number of bytes buffered so far.
     * @return total number of bytes buffered. If you need number of bytes
     * to be read, use InputStream.available() .
     */
    public int getLength() {
        return _write_pos;
    }

    /**
     * Tells whether buffered data is small enough to fit in memory buffer
     * so that it can be returned as byte[]. Data is considered large 
     * when it will not fit into backing memory buffer.
     * @return true when data is only accessible through InputStream interface;
     * false when data can be also retrieved directly as byte[] or String.
     * @see #getBytes()
     * @see #getString(String)
     */
    public boolean isLarge() {
        return _file_mode;
    }

    /**
     * Returns byte[] that holds all buffered data in its first getLength() bytes.
     * If this instance was created using (byte[]) constructor, this is the same
     * array that has been passed to the constructor. If buffered data don't fit into
     * memory buffer, IllegalStateException is thrown.
     * @return byte[] with data as its first getLength() bytes.
     * @throws IllegalStateException when data is too big to be read this way.
     * @see #isLarge()
     * @see #getLength()
     * @see #getString(String)
     * @see #getInputStream()
     */
    public byte[] getBytes() {
        if (_file_mode) throw new IllegalStateException("data too large");
        return _memory_buffer;
    }

    /**
     * Returns buffered data as String using given character encoding.
     * @param character_encoding Name of character encoding to use for
     * converting bytes to String.
     * @return Buffered data as String.
     * @throws IllegalStateException when data is too large to be read this way.
     * @throws java.io.UnsupportedEncodingException when this encoding is not supported.
     */
    public String getString(String character_encoding) throws java.io.UnsupportedEncodingException {
        if (_file_mode) throw new IllegalStateException("data too large");
        return new String(_memory_buffer, 0, _write_pos, character_encoding);
    }

    /**
     * Returns OutputStream filling this buffer.
     * @return OutputStream for writing in the buffer.
     */
    public java.io.OutputStream getOutputStream() {
        return _output_stream;
    }

    /**
     * Returns InputSream for reading buffered data.
     * @return InputSream for reading buffered data.
     */
    public java.io.InputStream getInputStream() {
        if (_input_stream == null) {
            _input_stream = new TempByteHolder.InputStream();
        }
        return _input_stream;
    }

    /**
     * Writes efficiently whole content to output stream.
     * @param os OutputStream to write to
     * @throws IOException
     */
    public void writeTo(java.io.OutputStream os) throws IOException {
        writeTo(os, 0, getLength());
    }

    /**
     * Writes efficiently part of the content to output stream.
     * @param os OutputStream to write to
     * @param start_offset  Offset of data fragment to be written
     * @param length        Length of data fragment to be written
     * @throws IOException
     */
    public void writeTo(java.io.OutputStream os, int start_offset, int length) throws IOException {
        int towrite = min(length, _write_pos - start_offset);
        int writeoff = start_offset;
        if (towrite > 0) {
            while (towrite >= _window_size) {
                moveWindow(writeoff);
                os.write(_memory_buffer, 0, _window_size);
                towrite -= _window_size;
                writeoff += _window_size;
            }
            if (towrite > 0) {
                moveWindow(writeoff);
                os.write(_memory_buffer, 0, towrite);
            }
        }
    }

    /**
     * Reads all available data from input stream.
     * @param is
     * @throws IOException
     */
    public void readFrom(java.io.InputStream is) throws IOException {
        int howmuch = 0;
        do {
            _write_pos += howmuch;
            moveWindow(_write_pos);
            howmuch = is.read(_memory_buffer);
        } while (howmuch != -1);
    }

    /**
     * Create tempfile if it does not already exist
     */
    private void createTempFile() throws IOException {
        _tempfilef = File.createTempFile("org.mortbay.util.TempByteHolder-", ".tmp", _temp_directory).getCanonicalFile();
        _tempfilef.deleteOnExit();
        _tempfile = new RandomAccessFile(_tempfilef, "rw");
    }

    /**
     * Write chunk of data at specified offset in temp file.
     * Marks data as big.
     * Updates high water mark on tempfile content.
     */
    private void writeToTempFile(int at_offset, byte[] data, int offset, int len) throws IOException {
        if (_tempfile == null) {
            createTempFile();
            _file_pos = -1;
        }
        _file_mode = true;
        if (at_offset != _file_pos) {
            _tempfile.seek((long) at_offset);
        }
        _tempfile.write(data, offset, len);
        _file_pos = at_offset + len;
        _file_high = max(_file_high, _file_pos);
    }

    /**
     * Read chunk of data from specified offset in tempfile
     */
    private void readFromTempFile(int at_offset, byte[] data, int offset, int len) throws IOException {
        if (_file_pos != at_offset) {
            _tempfile.seek((long) at_offset);
        }
        _tempfile.readFully(data, offset, len);
        _file_pos = at_offset + len;
    }

    /**
     * Move file window, synchronizing data with file.
     * Works somewhat like memory-mapping a file.
     * This one was nightmare to write :-)
     */
    private void moveWindow(int start_offset) throws IOException {
        if (start_offset != _window_low) {
            int end_offset = start_offset + _window_size;
            int dirty_low = _file_high;
            int dirty_high = _write_pos;
            int dirty_len = _write_pos - _file_high;
            if (dirty_len > 0) {
                if ((dirty_low < start_offset) || (dirty_high > end_offset)) {
                    writeToTempFile(dirty_low, _memory_buffer, dirty_low - _window_low, dirty_len);
                }
            }
            int stay_low = max(start_offset, _window_low);
            int stay_high = min(_write_pos, _window_high, end_offset);
            int stay_size = stay_high - stay_low;
            if (stay_size > 0) {
                System.arraycopy(_memory_buffer, stay_low - _window_low, _memory_buffer, stay_low - start_offset, stay_size);
            }
            if (stay_low > start_offset) {
                int toread_low = start_offset;
                int toread_high = min(stay_low, end_offset);
                int toread_size = toread_high - toread_low;
                if (toread_size > 0) {
                    readFromTempFile(toread_low, _memory_buffer, toread_low - start_offset, toread_size);
                }
            }
            if (stay_high < end_offset) {
                int toread_low = max(stay_high, start_offset);
                int toread_high = min(end_offset, _file_high);
                int toread_size = toread_high - toread_low;
                if (toread_size > 0) {
                    readFromTempFile(toread_low, _memory_buffer, toread_low - start_offset, toread_size);
                }
            }
            _window_low = start_offset;
            _window_high = end_offset;
        }
    }

    /** Simple minimum for 2 ints */
    private static int min(int a, int b) {
        return (a < b ? a : b);
    }

    /** Simple maximum for 2 ints */
    private static int max(int a, int b) {
        return (a > b ? a : b);
    }

    /** Simple minimum for 3 ints */
    private static int min(int a, int b, int c) {
        int r = a;
        if (r > b) r = b;
        if (r > c) r = c;
        return r;
    }

    /**
     * @return true when range 1 is fully contained in range 2
     */
    private static boolean contained(int range1_low, int range1_high, int range2_low, int range2_high) {
        return ((range1_low >= range2_low) && (range1_high <= range2_high));
    }

    /**
     * Internal implementation of java.io.OutputStream used to fill the byte buffer.
     */
    class OutputStream extends java.io.OutputStream {

        /**
         * Write whole byte array into buffer.
         * @param data byte[] to be written
         * @throws IOException when something goes wrong.
         */
        public void write(byte[] data) throws IOException {
            write(data, 0, data.length);
        }

        /**
         * Write segment of byte array to the buffer.
         * @param data Byte array with data
         * @param off Starting offset within the array.
         * @param len Number of bytes to write
         * @throws IOException when something goes wrong.
         */
        public void write(byte[] data, int off, int len) throws IOException {
            int new_write_pos = _write_pos + len;
            boolean write_pos_in_window = (_write_pos >= _window_low) && (_write_pos < _window_high);
            if (!write_pos_in_window) {
                moveWindow(_write_pos);
            }
            boolean end_of_data_in_window = (new_write_pos <= _window_high);
            if (end_of_data_in_window) {
                System.arraycopy(data, off, _memory_buffer, _write_pos - _window_low, len);
                _write_pos = new_write_pos;
            } else {
                int out_of_window = new_write_pos - _window_high;
                if (out_of_window < _window_size) {
                    int part1_len = _window_high - _write_pos;
                    int part2_len = len - part1_len;
                    System.arraycopy(data, off, _memory_buffer, _write_pos - _window_low, part1_len);
                    _write_pos = _window_high;
                    moveWindow(_write_pos);
                    System.arraycopy(data, off + part1_len, _memory_buffer, 0, part2_len);
                    _write_pos = new_write_pos;
                } else {
                    int part1_size = _window_high - _write_pos;
                    int part2_size = len - part1_size;
                    if (part1_size == _window_size) {
                        writeToTempFile(_write_pos, data, off, len);
                        _write_pos = new_write_pos;
                        moveWindow(_write_pos);
                    } else {
                        if (part1_size > 0) {
                            System.arraycopy(data, off, _memory_buffer, _write_pos - _window_low, part1_size);
                            _write_pos += part1_size;
                            moveWindow(_write_pos);
                        }
                        writeToTempFile(_write_pos, data, off + part1_size, part2_size);
                        _write_pos = new_write_pos;
                        moveWindow(_write_pos);
                    }
                }
            }
        }

        /**
         * Write single byte to the buffer.
         * @param b
         * @throws IOException
         */
        public void write(int b) throws IOException {
            if ((_write_pos >= _window_high) || (_write_pos < _window_low)) {
                moveWindow(_write_pos);
            }
            _memory_buffer[_write_pos - _window_low] = (byte) (b & 0xFF);
            _write_pos++;
        }

        public void flush() throws IOException {
            moveWindow(_write_pos);
        }

        public void close() throws IOException {
        }
    }

    /**
     * Internal implementation of InputStream used to read buffered data.
     */
    class InputStream extends java.io.InputStream {

        public int read() throws IOException {
            int ret = -1;
            if (!contained(_read_pos, _read_pos + 1, _window_low, _window_high)) {
                moveWindow(_read_pos);
            }
            if (_write_pos > _read_pos) {
                ret = (_memory_buffer[_read_pos - _window_low]) & 0xFF;
                _read_pos++;
            }
            return ret;
        }

        public int read(byte[] buff) throws IOException {
            return read(buff, 0, buff.length);
        }

        public int read(byte[] buff, int off, int len) throws IOException {
            int read_size = min(len, _write_pos - _read_pos);
            if (read_size > 0) {
                if (read_size >= _window_size) {
                    moveWindow(_write_pos);
                    readFromTempFile(_read_pos, buff, off, read_size);
                } else {
                    int read_low = _read_pos;
                    int read_high = read_low + read_size;
                    if (!contained(read_low, read_high, _window_low, _window_high)) {
                        moveWindow(_read_pos);
                    }
                    System.arraycopy(_memory_buffer, _read_pos - _window_low, buff, off, read_size);
                }
                _read_pos += read_size;
            }
            return read_size;
        }

        public long skip(long bytes) throws IOException {
            if (bytes < 0 || bytes > Integer.MAX_VALUE) throw new IllegalArgumentException();
            int len = (int) bytes;
            if ((len + _read_pos) > _write_pos) len = _write_pos - _read_pos;
            _read_pos += len;
            moveWindow(_write_pos);
            return (long) len;
        }

        public int available() throws IOException {
            return _write_pos - _read_pos;
        }

        public void mark(int readlimit) {
            _mark_pos = _read_pos;
        }

        public void reset() throws IOException {
            _read_pos = _mark_pos;
        }

        public boolean markSupported() {
            return true;
        }
    }
}
