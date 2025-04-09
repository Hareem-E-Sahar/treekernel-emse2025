package com.zwl.util.zip.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Write zip entries to Zip-File, encrypted or not encrypted.
 * 
 * @author olaf@merkert.de
 */
public class ExtZipOutputStream implements ZipConstants {

    public ExtZipOutputStream(File file) throws IOException {
        out = new FileOutputStream(file);
    }

    protected ExtZipOutputStream(OutputStream out) {
        this.out = out;
    }

    protected OutputStream out;

    /** number of bytes written to out */
    protected int written;

    public int getWritten() {
        return this.written;
    }

    public void writeBytes(byte[] b) throws IOException {
        out.write(b);
        written += b.length;
    }

    public void writeShort(int v) throws IOException {
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    public void writeInt(long v) throws IOException {
        out.write((int) ((v >>> 0) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        written += 4;
    }

    public void writeBytes(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        written += len;
    }

    protected static final short ZIP_VERSION = 20;

    protected void writeFileInfo(ExtZipEntry entry) throws IOException {
        writeShort(ZIP_VERSION);
        writeShort(entry.getFlag());
        writeShort(entry.getPrimaryCompressionMethod());
        writeInt(entry.getDosTime());
        writeInt(entry.getCrc());
        writeInt((int) entry.getCompressedSize());
        writeInt((int) entry.getSize());
        writeShort(entry.getName().length());
        if (entry.getExtra() != null) {
            writeShort(entry.getExtra().length);
        } else {
            writeShort(0);
        }
    }

    private List<ExtZipEntry> entries = new ArrayList<ExtZipEntry>();

    protected void writeDirEntry(ExtZipEntry entry) throws IOException {
        writeInt(CENSIG);
        writeShort(ZIP_VERSION);
        writeFileInfo(entry);
        writeShort(0x00);
        writeShort(0x00);
        writeShort(0x00);
        writeInt(0x00);
        writeInt(entry.getOffset());
        writeBytes(entry.getName().getBytes("iso-8859-1"));
        writeExtraBytes(entry);
    }

    protected void writeExtraBytes(ZipEntry entry) throws IOException {
        byte[] extraBytes = entry.getExtra();
        if (extraBytes != null) {
            writeBytes(extraBytes);
        }
    }

    public void putNextEntry(ExtZipEntry entry) throws IOException {
        entries.add(entry);
        entry.setOffset(written);
        writeInt(LOCSIG);
        writeFileInfo(entry);
        writeBytes(entry.getName().getBytes("iso-8859-1"));
        writeExtraBytes(entry);
    }

    /**
	 * Finishes writing the contents of the ZIP output stream.
	 */
    public void finish() throws IOException {
        int dirOffset = written;
        int startOfCentralDirectory = written;
        Iterator<ExtZipEntry> it = entries.iterator();
        while (it.hasNext()) {
            ExtZipEntry entry = it.next();
            writeDirEntry(entry);
        }
        int centralDirectorySize = written - startOfCentralDirectory;
        writeInt(ENDSIG);
        writeShort(0x00);
        writeShort(0x00);
        writeShort(entries.size());
        writeShort(entries.size());
        writeInt(centralDirectorySize);
        writeInt(dirOffset);
        writeShort(0x00);
        out.close();
    }
}
