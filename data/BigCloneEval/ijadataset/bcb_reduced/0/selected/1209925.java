package net.sourceforge.liftoff.builder.zip;

import java.io.*;
import java.util.zip.*;

public class ZipOutputStream extends DeflaterOutputStream {

    /**
     * The entry we are currently writing, or null if we've called
     * closeEntry.
     */
    private ZipEntry current = null;

    /**
     * offset of the start of the current entry.
     */
    private long currentStart = 0;

    /**
     * The chain of entries which have been written to this file.
     */
    private ZipEntry chain;

    private int method = DEFLATED;

    private int level = Deflater.DEFAULT_COMPRESSION;

    private String comment = "";

    private long bytes_written;

    private CRC32 crc;

    /** true if we have an seekable stream */
    private boolean outSeekable = false;

    public static final int STORED = 0;

    public static final int DEFLATED = 8;

    /** Constructor. */
    public ZipOutputStream(OutputStream out) {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        if (out instanceof SeekableOutputStream) {
            outSeekable = true;
            bytes_written = ((SeekableOutputStream) out).tell();
        }
        crc = new CRC32();
    }

    /** */
    public void close() throws IOException {
        finish();
        out.close();
    }

    /**
     * Close the current Entry and prepare for the next.
     */
    public void closeEntry() throws IOException {
        def.finish();
        while (!def.finished()) deflate();
        long uncompressed_size = def.getTotalIn();
        long compressed_size = def.getTotalOut();
        long ecrc = (crc.getValue());
        bytes_written += compressed_size;
        if (outSeekable || current.getCrc() == -1 || current.getCompressedSize() == -1 || current.getSize() == -1) {
            current.setCrc(ecrc);
            current.compressedSize = compressed_size;
            current.setSize(uncompressed_size);
        } else {
            if (current.getCrc() != ecrc || current.getCompressedSize() != compressed_size || current.getSize() != uncompressed_size) throw new ZipException("zip entry field incorrect");
        }
        if (outSeekable) {
            long here = bytes_written;
            ((SeekableOutputStream) out).seek(currentStart + 14);
            put4(ecrc);
            put4(compressed_size);
            put4(uncompressed_size);
            ((SeekableOutputStream) out).seek(here);
        } else bytes_written += current.writeExtHeader(out);
        crc.reset();
        def.reset();
        current.next = chain;
        chain = current;
        current = null;
    }

    /**
     * Prepare for closing this file : close open entriey and 
     * write the central directory.
     */
    public void finish() throws IOException {
        if (current != null) closeEntry();
        long offset = bytes_written;
        int count = 0;
        int bytes = 0;
        while (chain != null) {
            bytes += write_entry(chain, false);
            ++count;
            chain = chain.next;
        }
        put4(0x06054b50);
        put2(0);
        put2(0);
        put2(count);
        put2(count);
        put4(bytes);
        put4(offset);
        byte[] c = comment.getBytes("8859_1");
        put2(c.length);
        if (c.length > 0) {
            out.write(c);
            out.write((byte) 0);
        }
    }

    private long write_entry(ZipEntry entry, boolean is_local) throws IOException {
        return entry.writeHeader(out, is_local, bytes_written);
    }

    public void putNextEntry(ZipEntry entry) throws IOException {
        if (current != null) closeEntry();
        if (entry.method < 0) entry.method = method;
        if (entry.method == STORED) {
            if (entry.getSize() == -1 || entry.getCrc() == -1) throw new ZipException("required entry not set");
            entry.compressedSize = entry.getSize();
        } else if (outSeekable) entry.compressedSize = entry.size = entry.crc = 0;
        currentStart = bytes_written;
        bytes_written += entry.writeHeader(out, true, bytes_written);
        current = entry;
        int compr = (method == STORED) ? Deflater.NO_COMPRESSION : level;
        def.setLevel(compr);
    }

    /**
     * Set the compression level.
     */
    public void setLevel(int level) {
        if (level != Deflater.DEFAULT_COMPRESSION && (level < Deflater.NO_COMPRESSION || level > Deflater.BEST_COMPRESSION)) throw new IllegalArgumentException();
        this.level = level;
    }

    /**
     * set the compression method.
     *
     * @param method one of DEFLATED or STORED.
     */
    public void setMethod(int method) {
        if (method != DEFLATED && method != STORED) throw new IllegalArgumentException();
        this.method = method;
    }

    /**
     * set the zipfile comment.
     *
     * @param comment a String of at most 64k characters.
     */
    public void setComment(String comment) {
        if (comment.length() > 65535) throw new IllegalArgumentException();
        this.comment = comment;
    }

    /**
     * Write to the current Zip entry.
     *
     * @param buf bytes to write.
     * @param off offset in buf.
     * @param len number of bytes to write.
     */
    public synchronized void write(byte[] buf, int off, int len) throws IOException {
        if (current == null) throw new ZipException("no open zip entry");
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }

    private int put2(int i) throws IOException {
        out.write((i) & 0xFF);
        out.write((i >> 8) & 0xFF);
        return 2;
    }

    private int put4(long i) throws IOException {
        out.write((byte) ((i) & 0xFF));
        out.write((byte) ((i >> 8) & 0xFF));
        out.write((byte) ((i >> 16) & 0xFF));
        out.write((byte) ((i >> 24) & 0xFF));
        return 4;
    }

    private int put_version() throws IOException {
        return put2(3 << 8);
    }
}
