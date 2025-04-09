package com.myJava.file.archive.zip64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import com.myJava.file.archive.AbstractArchiveAdapter;
import com.myJava.file.archive.ArchiveAdapter;
import com.myJava.file.multivolumes.VolumeInputStream;
import com.myJava.file.multivolumes.VolumeStrategy;

public class ZipArchiveAdapter extends AbstractArchiveAdapter implements ArchiveAdapter {

    public ZipArchiveAdapter(InputStream in) {
        this(in, 1);
    }

    public ZipArchiveAdapter(InputStream in, long streamSize) {
        super(ACCESS_READ, streamSize);
        if (streamSize != 0) {
            this.zin = new ZipInputStream(in);
        }
    }

    public ZipArchiveAdapter(VolumeStrategy strategy) {
        this(strategy, 1);
    }

    public ZipArchiveAdapter(VolumeStrategy strategy, long streamSize) {
        super(ACCESS_READ, 0);
        if (streamSize != 0) {
            this.zin = new ZipInputStream(new VolumeInputStream(strategy));
        }
    }

    public ZipArchiveAdapter(OutputStream out, boolean useZip64, int level) {
        super(ACCESS_WRITE, 0);
        this.zout = new ZipOutputStream(out, useZip64);
        if (level >= 0) {
            ((ZipOutputStream) zout).setLevel(level);
        } else {
            ((ZipOutputStream) zout).setLevel(9);
        }
    }

    public ZipArchiveAdapter(VolumeStrategy strategy, long volumeSize, boolean useZip64, int level) {
        super(ACCESS_WRITE, 0);
        this.zout = new ZipOutputStream(strategy, volumeSize, useZip64);
        if (level >= 0) {
            ((ZipOutputStream) zout).setLevel(level);
        } else {
            ((ZipOutputStream) zout).setLevel(9);
        }
    }

    public void addEntry(String entryName, long size) throws IOException {
        super.addEntry(entryName, size);
        ZipEntry entry = new ZipEntry(entryName);
        ((ZipOutputStream) zout).putNextEntry(entry);
    }

    public void close() throws IOException {
        super.close();
    }

    public void setCharset(Charset charset) {
        if (zout != null) {
            ((ZipOutputStream) zout).setCharset(charset);
        }
        if (zin != null) {
            ((ZipInputStream) zin).setCharset(charset);
        }
    }

    public void setArchiveComment(String comment) {
        if (zout != null) {
            ((ZipOutputStream) zout).setComment(comment);
        }
    }

    public void closeEntry() throws IOException {
        if (zin != null) {
            ((ZipInputStream) zin).closeEntry();
        }
        if (zout != null) {
            ((ZipOutputStream) zout).closeEntry();
        }
    }

    public String getNextEntry() throws IOException {
        if (zin == null) {
            return null;
        }
        ZipEntry entry = ((ZipInputStream) zin).getNextEntry();
        if (entry == null) {
            return null;
        } else {
            return entry.getName();
        }
    }
}
