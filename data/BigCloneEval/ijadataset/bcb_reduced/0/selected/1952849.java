package geovista.readers;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * routines for reading and writing files. Features include transparent use of
 * compression and decompression, stdin/stdout.
 * 
 * 
 * @author Masahiro Takatsuka (masa@psu.edu)
 */
public final class FileIO {

    public static final int NO_ZIP = 0;

    public static final int ZIP = 1;

    public static final int GZIP = 2;

    public static final String STDIN_NAME = "stdin";

    public static final String STDOUT_NAME = "stdout";

    public static final String STDERR_NAME = "stderr";

    private static final int UNKNOWN_MODE = 0x00;

    private static final int READ_MODE = 0x01;

    private static final int WRITE_MODE = 0x02;

    public static final int OK = 0;

    public static final int UNKNOWN = 1;

    public static final int NO_MEMORY = 2;

    public static final int FILEMODE = 3;

    public static final int NO_PIPES = 4;

    public static final int OPENFILE = 5;

    public static final int COMMAND = 6;

    public static final int REWINDFILE = 7;

    public static final int REWINDPIPE = 8;

    public static final int LINETOOLONG = 9;

    public static final int FILEERR = 10;

    public static final int HEADER = 11;

    public static final int FILEFORMAT = 12;

    public static final int EOF = 13;

    private final String originalname;

    private final String originalmode;

    private String name;

    private int mode;

    private geovista.readers.Reader ir;

    private PrintWriter pw;

    private int compressed;

    private boolean pipe = false;

    private int lineno;

    protected static final Logger logger = Logger.getLogger(FileIO.class.getName());

    /**
	 * returns a input reader associated with this file I/O.
	 * 
	 * @return InputStreamReader
	 * @see #getWriter()
	 */
    public final geovista.readers.Reader getReader() {
        return ir;
    }

    /**
	 * returns a writer associated with this file I/O.
	 * 
	 * @return PrintWriter
	 * @see #getReader()
	 */
    public final PrintWriter getWriter() {
        return pw;
    }

    /**
	 * Returns the file mode.
	 * 
	 * @return int
	 */
    public final int getMode() {
        return mode;
    }

    /**
	 * Returns the original file name.
	 * 
	 * @return String
	 */
    public final String getOriginalFilename() {
        return originalname;
    }

    /**
	 * Returns the original mode string.
	 * 
	 * @return String
	 */
    public final String getOriginalMode() {
        return originalmode;
    }

    /**
	 * Returns true if the file has reached EOF.
	 * 
	 * @return boolean
	 */
    public final boolean hasReachedEOF() {
        return ir.isEof();
    }

    /**
	 * Returns true if the file is piped.
	 * 
	 * @return boolean
	 */
    public final boolean getPipeFlag() {
        return pipe;
    }

    /**
	 * Returns current line number.
	 * 
	 * @return int
	 */
    public final int getLineNo() {
        return lineno;
    }

    /**
	 * Opens a file for reading or writing. If name is 'null', standard input
	 * (System.in) or standard output (System.out) will be used. There are two
	 * basic mode "r" for reading and "w" for writing. The mode can have
	 * optional characters, which are "z" for "ZIP" compression and "g" for
	 * "GZIP" compression. The pipe is not supported yet. If optional characters
	 * are not specified, they will be guessed from the file name. The suffix
	 * ".zip" implies "ZIP" mode and ".gz", ".z" or ".Z" imply "GZIP" mode.
	 * 
	 * <PRE>
	 *  try {
	 * 		FileIO fio0 = new FileIO(&quot;foo0&quot;, &quot;r&quot;);
	 * 		FileIO fio1 = new FileIO(&quot;foo1.gz&quot;, &quot;r&quot;);
	 * 		FileIO fio2 = new FileIO(&quot;foo2.zip&quot;, &quot;rz&quot;);
	 * 		FileIO fio3 = new FileIO(&quot;foo3.gz&quot;, &quot;rg&quot;);
	 * 			...
	 * 	fio0.close();
	 * 	fio1.close();
	 * 	fio2.close();
	 * 	fio3.close();
	 *  } catch (InvalidFileModeException ifme) {
	 *  	System.err.println(ifme);
	 * } catch (FileIOException fioe) {
	 *  	System.err.println(fioe);
	 *  } catch (IOException ioe) {
	 *  	System.err.println(ioe);
	 *  } finally {
	 *  }
	 * </PRE>
	 * 
	 * @param name
	 *            The name of the filename to be opened.
	 * @param mode
	 *            The file mode.
	 * @return A newly created FileIO instance.
	 * @exception (full-classname) (description)
	 * @see #close()
	 */
    public FileIO(String filename, String filemode) throws InvalidFileModeException, FileIOException, IOException {
        ir = null;
        pw = null;
        compressed = NO_ZIP;
        pipe = false;
        lineno = 0;
        originalname = (filename != null) ? filename.intern() : null;
        originalmode = (filemode != null) ? filemode.intern() : null;
        mode = UNKNOWN_MODE;
        StringBuffer buf = new StringBuffer();
        int idx;
        if (filemode == null) {
            buf.append("FileIO: incorrect file mode: ").append(filemode).append(" for file ").append(filename);
            throw new InvalidFileModeException(buf.toString());
        }
        if (filemode.indexOf('r') >= 0) {
            mode = READ_MODE;
        } else if (filemode.indexOf('w') >= 0) {
            mode = WRITE_MODE;
        } else {
            buf.append("FileIO: incorrect file mode: ").append(filemode).append(" for file ").append(filename);
            throw new InvalidFileModeException(buf.toString());
        }
        if ((idx = filemode.indexOf('z')) >= 0 || (idx = filemode.indexOf('g')) >= 0) {
            compressed = ZIP;
            pipe = false;
            buf.setLength(0);
            filemode = buf.append(filemode.substring(0, idx)).append(filemode.substring(idx + 1)).toString();
        }
        if ((idx = filemode.indexOf('p')) >= 0) {
            pipe = true;
            compressed = NO_ZIP;
            buf.setLength(0);
            filemode = buf.append(filemode.substring(0, idx)).append(filemode.substring(idx + 1)).toString();
            logger.finest(filemode);
        }
        if (filename != null && (filename.charAt(0) == '|')) {
            pipe = true;
            compressed = NO_ZIP;
            filename = filename.substring(1);
        }
        if (filename != null) {
            filename = filename.trim();
            if (!pipe) {
                compressed = checkCompressionType(filename);
            }
            if (filename.compareTo("-") == 0) {
                filename = null;
            }
        }
        if (filename == null) {
            if (mode == READ_MODE) {
                ir = new geovista.readers.Reader(System.in);
                pw = null;
            } else {
                ir = null;
                pw = new PrintWriter(System.out);
            }
            filename = (mode == READ_MODE) ? STDIN_NAME : STDOUT_NAME;
        } else if (compressed > NO_ZIP) {
            if (mode == READ_MODE) {
                switch(compressed) {
                    case ZIP:
                        ZipFile zf = new ZipFile(filename);
                        Enumeration enumeration = zf.entries();
                        ZipEntry target = (ZipEntry) enumeration.nextElement();
                        ir = new geovista.readers.Reader(zf.getInputStream(target));
                        break;
                    case GZIP:
                        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(filename));
                        ir = new geovista.readers.Reader(gzis);
                        break;
                }
                pw = null;
            } else {
                File zf = new File(filename);
                switch(compressed) {
                    case ZIP:
                        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zf));
                        String fullpath = zf.getPath();
                        String path = fullpath.substring(0, fullpath.lastIndexOf("."));
                        ZipEntry target = new ZipEntry(path);
                        zos.putNextEntry(target);
                        ir = null;
                        pw = new PrintWriter(zos, true);
                        break;
                    case GZIP:
                        GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(zf));
                        ir = null;
                        pw = new PrintWriter(gos, true);
                        break;
                }
            }
        } else if (pipe) {
        } else {
            if (mode == READ_MODE) {
                ir = new geovista.readers.Reader(new FileInputStream(filename));
                pw = null;
            } else {
                ir = null;
                pw = new PrintWriter(new FileOutputStream(filename), true);
            }
        }
        name = filename.intern();
    }

    /**
	 * Closes FileIO instance.
	 * 
	 * @see #FileIO(java.lang.String, java.lang.String)
	 */
    public final void close() throws IOException {
        if (name != null) {
            name = null;
        }
        if (ir != null) {
            ir.close();
        }
        if (pw != null) {
            pw.flush();
            pw.close();
        }
    }

    /**
	 * Returns a filename associated with this FileIO object.
	 * 
	 * @return a filename
	 */
    public final String getFilename() {
        return name;
    }

    /**
	 * check_for_compression - check if name indicates compression, i.e. the
	 * ending is one of .gz, .z, .Z. If suffix is found, returns an index of the
	 * dot, otherwise returns -1
	 * 
	 * @param name
	 *            The name of file.
	 * @return integer value indicating the type of compression (NO_ZIP : no
	 *         compression, ZIP : zip, GZIP : gnuzip
	 */
    private int checkCompressionType(String name) {
        String s;
        int idx = -1;
        if (name == null) {
            return NO_ZIP;
        }
        if ((idx = name.lastIndexOf('.')) < 0) {
            return NO_ZIP;
        }
        s = name.substring(idx);
        if (s.compareTo(".gz") == 0 || s.compareTo(".z") == 0 || s.compareTo(".Z") == 0) {
            return GZIP;
        } else if (s.compareTo(".zip") == 0) {
            return ZIP;
        }
        return NO_ZIP;
    }

    /**
	 * Reads a line from file. Returns a String object containing a line, null
	 * on error.
	 * 
	 * @return String containing a line.
	 */
    public final String getLine() throws EOFException, IOException {
        String tstr = null;
        lineno += 1;
        try {
            tstr = ir.readLine();
            if (tstr == null) {
                throw new EOFException();
            }
        } catch (EOFException eofe) {
            throw eofe;
        } catch (IOException ioe) {
            throw ioe;
        }
        return tstr;
    }

    /**
	 * Read a single character.
	 * 
	 * @return The character read, or -1 if the end of the stream has been
	 *         reached
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final int read() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.read();
    }

    /**
	 * Read characters into a portion of an array.
	 * 
	 * @param cbuf
	 *            Destination buffer
	 * @param off
	 *            Offset at which to start storing characters
	 * @param len
	 *            Maximum number of characters to read
	 * 
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final int read(char cbuf[], int off, int len) throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.read(cbuf, off, len);
    }

    /**
	 * Read a line of text. A line is considered to be terminated by any one of
	 * a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 * @return A String containing the contents of the line, not including any
	 *         line-termination characters, or null if the end of the stream has
	 *         been reached
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final String readLine() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readLine();
    }

    /**
	 * Read a line of text. A line is considered to be terminated by any one of
	 * a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 * @return A String containing the contents of the line, not including any
	 *         line-termination characters, or null if the end of the stream has
	 *         been reached
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final String readLine(String commentchars) throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readLine(commentchars);
    }

    /**
	 * Read a line of text. A line is considered to be terminated by any one of
	 * a line feed ('\n'), a carriage return ('\r'), or a carriage return
	 * followed immediately by a linefeed.
	 * 
	 * @return A String containing the contents of the line, not including any
	 *         line-termination characters, or null if the end of the stream has
	 *         been reached
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final String readLine(char commentchar) throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readLine(commentchar);
    }

    /**
	 * Read one token as a string.
	 * 
	 * @return A String containing the string, or null if the end of the stream
	 *         has been reached.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs
	 */
    public final String readToken() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readToken();
    }

    /**
	 * Reads a <code>boolean</code> from this data input stream. This method
	 * reads a single byte from the underlying input stream. A value of
	 * <code>0</code> represents <code>false</code>. Any other value represents
	 * <code>true</code>. This method blocks until either the byte is read, the
	 * end of the stream is detected, or an exception is thrown.
	 * 
	 * @return the <code>boolean</code> value read.
	 * @exception EOFException
	 *                if this input stream has reached the end.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final boolean readBoolean() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readBoolean();
    }

    /**
	 * Reads a signed 8-bit value from this data input stream. This method reads
	 * a byte from the underlying input stream. If the byte read is
	 * <code>b</code>, where 0&nbsp;&lt;=&nbsp;<code>b</code>
	 * &nbsp;&lt;=&nbsp;255, then the result is:
	 * <ul>
	 * <code>
	 *     (byte)(b)
	 * </code>
	 * </ul>
	 * <p>
	 * This method blocks until either the byte is read, the end of the stream
	 * is detected, or an exception is thrown.
	 * 
	 * @return the next byte of this input stream as a signed 8-bit
	 *         <code>byte</code>.
	 * @exception EOFException
	 *                if this input stream has reached the end.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final byte readByte() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readByte();
    }

    /**
	 * Reads a signed 16-bit number from this data input stream. The method
	 * reads two bytes from the underlying input stream. If the two bytes read,
	 * in order, are <code>b1</code> and <code>b2</code>, where each of the two
	 * values is between <code>0</code> and <code>255</code>, inclusive, then
	 * the result is equal to:
	 * <ul>
	 * <code> (short)((b1 &lt;&lt; 8) | b2) </code>
	 * </ul>
	 * <p>
	 * This method blocks until the two bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next two bytes of this input stream, interpreted as a signed
	 *         16-bit number.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading two
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final short readShort() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readShort();
    }

    /**
	 * Reads a Unicode character from this data input stream. This method reads
	 * two bytes from the underlying input stream. If the bytes read, in order,
	 * are <code>b1</code> and <code>b2</code>, where 0&nbsp;&lt;=&nbsp;
	 * <code>b1</code>, <code>b1</code>&nbsp;&lt;=&nbsp;255, then the result is
	 * equal to:
	 * <ul>
	 * <code> (char)((b1 &lt;&lt; 8) | b2) </code>
	 * </ul>
	 * <p>
	 * This method blocks until either the two bytes are read, the end of the
	 * stream is detected, or an exception is thrown.
	 * 
	 * @return the next two bytes of this input stream as a Unicode character.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading two
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final char readChar() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readChar();
    }

    /**
	 * Reads a signed 32-bit integer from this data input stream. This method
	 * reads four bytes from the underlying input stream. If the bytes read, in
	 * order, are <code>b1</code>, <code>b2</code>, <code>b3</code>, and
	 * <code>b4</code>, where 0&nbsp;&lt;=&nbsp;<code>b1</code>, <code>b2</code>
	 * , <code>b3</code>, <code>b4</code>&nbsp;&lt;=&nbsp;255, then the result
	 * is equal to:
	 * <ul>
	 * <code> (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) +b4 </code>
	 * </ul>
	 * <p>
	 * This method blocks until the four bytes are read, the end of the stream
	 * is detected, or an exception is thrown.
	 * 
	 * @return the next four bytes of this input stream, interpreted as an
	 *         <code>int</code>.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading four
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final int readInt() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readInt();
    }

    /**
	 * Reads a signed 64-bit integer from this data input stream. This method
	 * reads eight bytes from the underlying input stream. If the bytes read, in
	 * order, are <code>b1</code>, <code>b2</code>, <code>b3</code>,
	 * <code>b4</code>, <code>b5</code>, <code>b6</code>, <code>b7</code>, and
	 * <code>b8</code>, where
	 * <ul>
	 * <code> 0 &lt;= b1, b2, b3, b4, b5, b6, b7, b8 &lt;= 255, </code>
	 * </ul>
	 * <p>
	 * then the result is equal to:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * ((long) b1 &lt;&lt; 56) + ((long) b2 &lt;&lt; 48) + ((long) b3 &lt;&lt; 40) + ((long) b4 &lt;&lt; 32)
	 * 		+ ((long) b5 &lt;&lt; 24) + (b6 &lt;&lt; 16) + (b7 &lt;&lt; 8) + b8
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * This method blocks until the eight bytes are read, the end of the stream
	 * is detected, or an exception is thrown.
	 * 
	 * @return the next eight bytes of this input stream, interpreted as a
	 *         <code>long</code>.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading eight
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final long readLong() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readLong();
    }

    /**
	 * Reads a <code>float</code> from this data input stream. This method reads
	 * an <code>int</code> value as if by the <code>readInt</code> method and
	 * then converts that <code>int</code> to a <code>float</code> using the
	 * <code>intBitsToFloat</code> method in class <code>Float</code>. This
	 * method blocks until the four bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 * 
	 * @return the next four bytes of this input stream, interpreted as a
	 *         <code>float</code>.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading four
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final float readFloat() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readFloat();
    }

    /**
	 * Reads a <code>double</code> from this data input stream. This method
	 * reads a <code>long</code> value as if by the <code>readLong</code> method
	 * and then converts that <code>long</code> to a <code>double</code> using
	 * the <code>longBitsToDouble</code> method in class <code>Double</code>.
	 * <p>
	 * This method blocks until the eight bytes are read, the end of the stream
	 * is detected, or an exception is thrown.
	 * 
	 * @return the next eight bytes of this input stream, interpreted as a
	 *         <code>double</code>.
	 * @exception EOFException
	 *                if this input stream reaches the end before reading eight
	 *                bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
    public final double readDouble() throws IOException {
        if (ir == null) {
            throw new IOException("FileIO: _ir == null");
        }
        return ir.readDouble();
    }

    /** Flush the stream. */
    public final void flush() {
        if (pw != null) {
            pw.flush();
        }
    }

    /**
	 * Flush the stream and check its error state. Errors are cumulative; once
	 * the stream encounters an error, this routine will return true on all
	 * successive calls.
	 * 
	 * @return True if the print stream has encountered an error, either on the
	 *         underlying output stream or during a format conversion.
	 */
    public final boolean checkError() {
        if (pw != null) {
            return pw.checkError();
        }
        return true;
    }

    /** Write a single character. */
    public final void write(int c) {
        if (pw != null) {
            pw.write(c);
        }
    }

    /** Write a portion of an array of characters. */
    public final void write(char buf[], int off, int len) {
        if (pw != null) {
            pw.write(buf, off, len);
        }
    }

    /**
	 * Write an array of characters. This method cannot be inherited from the
	 * Writer class because it must suppress I/O exceptions.
	 */
    public final void write(char buf[]) {
        if (pw != null) {
            pw.write(buf);
        }
    }

    /** Write a portion of a string. */
    public final void write(String s, int off, int len) {
        if (pw != null) {
            pw.write(s, off, len);
        }
    }

    /**
	 * Write a string. This method cannot be inherited from the Writer class
	 * because it must suppress I/O exceptions.
	 */
    public final void write(String s) {
        if (pw != null) {
            pw.write(s);
        }
    }

    /**
	 * Print a boolean value. The string produced by <code>
	 * {@link java.lang.String#valueOf(boolean)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param b
	 *            The <code>boolean</code> to be printed
	 */
    public final void print(boolean b) {
        if (pw != null) {
            pw.print(b);
        }
    }

    /**
	 * Print a character. The character is translated into one or more bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param c
	 *            The <code>char</code> to be printed
	 */
    public final void print(char c) {
        if (pw != null) {
            pw.print(c);
        }
    }

    /**
	 * Print an integer. The string produced by <code>
	 * {@link java.lang.String#valueOf(int)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param i
	 *            The <code>int</code> to be printed
	 * @see java.lang.Integer#toString(int)
	 */
    public final void print(int i) {
        if (pw != null) {
            pw.print(i);
        }
    }

    /**
	 * Print a long integer. The string produced by <code>
	 * {@link java.lang.String#valueOf(long)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param l
	 *            The <code>long</code> to be printed
	 * @see java.lang.Long#toString(long)
	 */
    public final void print(long l) {
        if (pw != null) {
            pw.print(l);
        }
    }

    /**
	 * Print a floating-point number. The string produced by <code>
	 * {@link java.lang.String#valueOf(float)}</code> is translated into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param f
	 *            The <code>float</code> to be printed
	 * @see java.lang.Float#toString(float)
	 */
    public final void print(float f) {
        if (pw != null) {
            pw.print(f);
        }
    }

    /**
	 * Print a double-precision floating-point number. The string produced by
	 * <code>{@link java.lang.String#valueOf(double)}</code> is translated into
	 * bytes according to the platform's default character encoding, and these
	 * bytes are written in exactly the manner of the <code>{@link #write(int)}
	 * </code> method.
	 * 
	 * @param d
	 *            The <code>double</code> to be printed
	 * @see java.lang.Double#toString(double)
	 */
    public final void print(double d) {
        if (pw != null) {
            pw.print(d);
        }
    }

    /**
	 * Print an array of characters. The characters are converted into bytes
	 * according to the platform's default character encoding, and these bytes
	 * are written in exactly the manner of the <code>{@link #write(int)}</code>
	 * method.
	 * 
	 * @param s
	 *            The array of chars to be printed
	 * 
	 * @throws NullPointerException
	 *             If <code>s</code> is <code>null</code>
	 */
    public final void print(char s[]) {
        if (pw != null) {
            pw.print(s);
        }
    }

    /**
	 * Print a string. If the argument is <code>null</code> then the string
	 * <code>"null"</code> is printed. Otherwise, the string's characters are
	 * converted into bytes according to the platform's default character
	 * encoding, and these bytes are written in exactly the manner of the <code>
	 * {@link #write(int)}</code> method.
	 * 
	 * @param s
	 *            The <code>String</code> to be printed
	 */
    public final void print(String s) {
        if (pw != null) {
            pw.print(s);
        }
    }

    /**
	 * Print an object. The string produced by the <code>
	 * {@link java.lang.String#valueOf(Object)}</code> method is translated into
	 * bytes according to the platform's default character encoding, and these
	 * bytes are written in exactly the manner of the <code>{@link #write(int)}
	 * </code> method.
	 * 
	 * @param obj
	 *            The <code>Object</code> to be printed
	 * @see java.lang.Object#toString()
	 */
    public final void print(Object obj) {
        if (pw != null) {
            pw.print(obj);
        }
    }

    /**
	 * Terminate the current line by writing the line separator string. The line
	 * separator string is defined by the system property
	 * <code>line.separator</code>, and is not necessarily a single newline
	 * character (<code>'\n'</code>).
	 */
    public final void println() {
        if (pw != null) {
            pw.println();
        }
    }

    /**
	 * Print a boolean value and then terminate the line. This method behaves as
	 * though it invokes <code>{@link #print(boolean)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(boolean x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print a character and then terminate the line. This method behaves as
	 * though it invokes <code>{@link #print(char)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(char x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print an integer and then terminate the line. This method behaves as
	 * though it invokes <code>{@link #print(int)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(int x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print a long integer and then terminate the line. This method behaves as
	 * though it invokes <code>{@link #print(long)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(long x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print a floating-point number and then terminate the line. This method
	 * behaves as though it invokes <code>{@link #print(float)}</code> and then
	 * <code>{@link #println()}</code>.
	 */
    public final void println(float x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print a double-precision floating-point number and then terminate the
	 * line. This method behaves as though it invokes <code>
	 * {@link #print(double)}</code> and then <code>{@link #println()}</code>.
	 */
    public final void println(double x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print an array of characters and then terminate the line. This method
	 * behaves as though it invokes <code>{@link #print(char[])}</code> and then
	 * <code>{@link #println()}</code>.
	 */
    public final void println(char x[]) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print a String and then terminate the line. This method behaves as though
	 * it invokes <code>{@link #print(String)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(String x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Print an Object and then terminate the line. This method behaves as
	 * though it invokes <code>{@link #print(Object)}</code> and then <code>
	 * {@link #println()}</code>.
	 */
    public final void println(Object x) {
        if (pw != null) {
            pw.println(x);
        }
    }

    /**
	 * Tests FileIO class. It reads from args[0] and write to args[1].
	 * 
	 * @param args
	 *            An array of input and output filename.
	 */
    public static final void main(String[] args) {
        try {
            FileIO fi = new FileIO(args[0], "r");
            FileIO fw = new FileIO(args[1], "w");
            int c;
            while ((c = fi.read()) != -1) {
                fw.write((byte) c);
            }
            fi.close();
            fw.close();
        } catch (InvalidFileModeException ifme) {
            System.err.println(ifme);
        } catch (FileIOException fioe) {
            System.err.println(fioe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
}
