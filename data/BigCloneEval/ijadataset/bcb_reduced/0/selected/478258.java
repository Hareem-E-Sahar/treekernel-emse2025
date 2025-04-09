package com.util;

import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * This class implements an output stream filter for writing files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.
 *
 * @author	David Connelly
 * @version	1.24, 12/03/01
 */
public class ZipOutputStreamEx extends DeflaterOutputStream {

    static long LOCSIG = 0x04034b50L;

    static long EXTSIG = 0x08074b50L;

    static long CENSIG = 0x02014b50L;

    static long ENDSIG = 0x06054b50L;

    static final int LOCHDR = 30;

    static final int EXTHDR = 16;

    static final int CENHDR = 46;

    static final int ENDHDR = 22;

    static final int LOCVER = 4;

    static final int LOCFLG = 6;

    static final int LOCHOW = 8;

    static final int LOCTIM = 10;

    static final int LOCCRC = 14;

    static final int LOCSIZ = 18;

    static final int LOCLEN = 22;

    static final int LOCNAM = 26;

    static final int LOCEXT = 28;

    static final int EXTCRC = 4;

    static final int EXTSIZ = 8;

    static final int EXTLEN = 12;

    static final int CENVEM = 4;

    static final int CENVER = 6;

    static final int CENFLG = 8;

    static final int CENHOW = 10;

    static final int CENTIM = 12;

    static final int CENCRC = 16;

    static final int CENSIZ = 20;

    static final int CENLEN = 24;

    static final int CENNAM = 28;

    static final int CENEXT = 30;

    static final int CENCOM = 32;

    static final int CENDSK = 34;

    static final int CENATT = 36;

    static final int CENATX = 38;

    static final int CENOFF = 42;

    static final int ENDSUB = 8;

    static final int ENDTOT = 10;

    static final int ENDSIZ = 12;

    static final int ENDOFF = 16;

    static final int ENDCOM = 20;

    private ZipEntryEx entry;

    private Vector entries = new Vector();

    private Hashtable names = new Hashtable();

    private CRC32 crc = new CRC32();

    private long written;

    private long locoff = 0;

    private String comment;

    private int method = DEFLATED;

    private boolean finished;

    private boolean closed = false;

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Compression method for uncompressed (STORED) entries.
     */
    public static final int STORED = ZipEntryEx.STORED;

    /**
     * Compression method for compressed (DEFLATED) entries.
     */
    public static final int DEFLATED = ZipEntryEx.DEFLATED;

    /**
     * Creates a new ZIP output stream.
     * @param out the actual output stream
     */
    public ZipOutputStreamEx(OutputStream out) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    }

    /**
     * Sets the ZIP file comment.
     * @param comment the comment string
     * @exception IllegalArgumentException if the length of the specified
     *		  ZIP file comment is greater than 0xFFFF bytes
     */
    public void setComment(String comment) {
        if (comment != null && comment.length() > 0xffff / 3 && getUTF8Length(comment) > 0xffff) {
            throw new IllegalArgumentException("ZIP file comment too long.");
        }
        this.comment = comment;
    }

    /**
     * Sets the default compression method for subsequent entries. This
     * default will be used whenever the compression method is not specified
     * for an individual ZIP file entry, and is initially set to DEFLATED.
     * @param method the default compression method
     * @exception IllegalArgumentException if the specified compression method
     *		  is invalid
     */
    public void setMethod(int method) {
        if (method != DEFLATED && method != STORED) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }

    /**
     * Sets the compression level for subsequent entries which are DEFLATED.
     * The default setting is DEFAULT_COMPRESSION.
     * @param level the compression level (0-9)
     * @exception IllegalArgumentException if the compression level is invalid
     */
    public void setLevel(int level) {
        def.setLevel(level);
    }

    /**
     * Begins writing a new ZIP file entry and positions the stream to the
     * start of the entry data. Closes the current entry if still active.
     * The default compression method will be used if no compression method
     * was specified for the entry, and the current time will be used if
     * the entry has no set modification time.
     * @param e the ZIP entry to be written
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void putNextEntry(ZipEntryEx e) throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        if (e.getTime() == -1) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.getMethod() == -1) {
            e.setMethod(method);
        }
        switch(e.getMethod()) {
            case DEFLATED:
                if (e.getSize() == -1 || e.getCompressedSize() == -1 || e.getCrc() == -1) {
                    e.flag = 8;
                } else if (e.getSize() != -1 && e.getCompressedSize() != -1 && e.getCrc() != -1) {
                    e.flag = 0;
                } else {
                    throw new ZipException("DEFLATED entry missing size, compressed size, or crc-32");
                }
                e.version = 20;
                break;
            case STORED:
                if (e.getSize() == -1) {
                    e.setSize(e.getCompressedSize());
                } else if (e.getCompressedSize() == -1) {
                    e.setCompressedSize(e.getSize());
                } else if (e.getSize() != e.getCompressedSize()) {
                    throw new ZipException("STORED entry where compressed != uncompressed size");
                }
                if (e.getSize() == -1 || e.getCrc() == -1) {
                    throw new ZipException("STORED entry missing size, compressed size, or crc-32");
                }
                e.version = 10;
                e.flag = 0;
                break;
            default:
                throw new ZipException("unsupported compression method");
        }
        e.offset = written;
        if (names.put(e.getName(), e) != null) {
            throw new ZipException("duplicate entry: " + e.getName());
        }
        writeLOC(e);
        entries.addElement(e);
        entry = e;
    }

    /**
     * Closes the current ZIP entry and positions the stream for writing
     * the next entry.
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        ZipEntryEx e = entry;
        if (e != null) {
            switch(e.getMethod()) {
                case DEFLATED:
                    def.finish();
                    while (!def.finished()) {
                        deflate();
                    }
                    if ((e.flag & 8) == 0) {
                        if (e.getSize() != def.getTotalIn()) {
                            throw new ZipException("invalid entry size (expected " + e.getSize() + " but got " + def.getTotalIn() + " bytes)");
                        }
                        if (e.getCompressedSize() != def.getTotalOut()) {
                            throw new ZipException("invalid entry compressed size (expected " + e.getCompressedSize() + " but got " + def.getTotalOut() + " bytes)");
                        }
                        if (e.getCrc() != crc.getValue()) {
                            throw new ZipException("invalid entry CRC-32 (expected 0x" + Long.toHexString(e.getCrc()) + " but got 0x" + Long.toHexString(crc.getValue()) + ")");
                        }
                    } else {
                        e.setSize(def.getTotalIn());
                        e.setCompressedSize(def.getTotalOut());
                        e.setCrc(crc.getValue());
                        writeEXT(e);
                    }
                    def.reset();
                    written += e.getCompressedSize();
                    break;
                case STORED:
                    if (e.getSize() != written - locoff) {
                        throw new ZipException("invalid entry size (expected " + e.getSize() + " but got " + (written - locoff) + " bytes)");
                    }
                    if (e.getCrc() != crc.getValue()) {
                        throw new ZipException("invalid entry crc-32 (expected 0x" + Long.toHexString(e.getCrc()) + " but got 0x" + Long.toHexString(crc.getValue()) + ")");
                    }
                    break;
                default:
                    throw new InternalError("invalid compression method");
            }
            crc.reset();
            entry = null;
        }
    }

    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) (b & 0xff);
        write(buf, 0, 1);
    }

    /**
     * Writes an array of bytes to the current ZIP entry data. This method
     * will block until all the bytes are written.
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        if (entry == null) {
            throw new ZipException("no current ZIP entry");
        }
        switch(entry.getMethod()) {
            case DEFLATED:
                super.write(b, off, len);
                break;
            case STORED:
                written += len;
                if (written - locoff > entry.getSize()) {
                    throw new ZipException("attempt to write past end of STORED entry");
                }
                out.write(b, off, len);
                break;
            default:
                throw new InternalError("invalid compression method");
        }
        crc.update(b, off, len);
    }

    /**
     * Finishes writing the contents of the ZIP output stream without closing
     * the underlying stream. Use this method when applying multiple filters
     * in succession to the same output stream.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (finished) {
            return;
        }
        if (entry != null) {
            closeEntry();
        }
        if (entries.size() < 1) {
            throw new ZipException("ZIP file must have at least one entry");
        }
        long off = written;
        Enumeration e = entries.elements();
        while (e.hasMoreElements()) {
            writeCEN((ZipEntryEx) e.nextElement());
        }
        writeEND(off, written - off);
        finished = true;
    }

    /**
     * Closes the ZIP output stream as well as the stream being filtered.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        super.close();
        closed = true;
    }

    private void writeLOC(ZipEntryEx e) throws IOException {
        writeInt(LOCSIG);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.getMethod());
        writeInt(e.getTime());
        if ((e.flag & 8) == 8) {
            writeInt(0);
            writeInt(0);
            writeInt(0);
        } else {
            writeInt(e.getCrc());
            writeInt(e.getCompressedSize());
            writeInt(e.getSize());
        }
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        writeShort(e.getExtra() != null ? e.getExtra().length : 0);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (e.getExtra() != null) {
            writeBytes(e.getExtra(), 0, e.getExtra().length);
        }
        locoff = written;
    }

    private void writeEXT(ZipEntryEx e) throws IOException {
        writeInt(EXTSIG);
        writeInt(e.getCrc());
        writeInt(e.getCompressedSize());
        writeInt(e.getSize());
    }

    private void writeCEN(ZipEntryEx e) throws IOException {
        writeInt(CENSIG);
        writeShort(e.version);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.getMethod());
        writeInt(e.getTime());
        writeInt(e.getCrc());
        writeInt(e.getCompressedSize());
        writeInt(e.getSize());
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        writeShort(e.getExtra() != null ? e.getExtra().length : 0);
        byte[] commentBytes;
        if (e.getComment() != null) {
            commentBytes = getUTF8Bytes(e.getComment());
            writeShort(commentBytes.length);
        } else {
            commentBytes = null;
            writeShort(0);
        }
        writeShort(0);
        writeShort(0);
        writeInt(0);
        writeInt(e.offset);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (e.getExtra() != null) {
            writeBytes(e.getExtra(), 0, e.getExtra().length);
        }
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, commentBytes.length);
        }
    }

    private void writeEND(long off, long len) throws IOException {
        writeInt(ENDSIG);
        writeShort(0);
        writeShort(0);
        writeShort(entries.size());
        writeShort(entries.size());
        writeInt(len);
        writeInt(off);
        if (comment != null) {
            byte[] b = getUTF8Bytes(comment);
            writeShort(b.length);
            writeBytes(b, 0, b.length);
        } else {
            writeShort(0);
        }
    }

    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) ((v >>> 0) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        written += 4;
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        written += len;
    }

    static int getUTF8Length(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 0x7f) {
                count++;
            } else if (ch <= 0x7ff) {
                count += 2;
            } else {
                count += 3;
            }
        }
        return count;
    }

    private static byte[] getUTF8Bytes(String s) {
        return s.getBytes();
    }
}
