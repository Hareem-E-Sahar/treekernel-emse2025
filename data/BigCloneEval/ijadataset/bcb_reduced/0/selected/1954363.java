package br.net.woodstock.rockframework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import br.net.woodstock.rockframework.collection.ImmutableSet;

public class ZipWriter {

    private static final char DIR_SEPARATOR = '/';

    private Set<File> files;

    public ZipWriter() {
        this.files = new LinkedHashSet<File>(0);
    }

    public void add(final File... files) {
        for (File f : files) {
            this.files.add(f);
        }
    }

    public Collection<File> getFiles() {
        return new ImmutableSet<File>(this.files);
    }

    public void save(final File file) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        for (File f : this.files) {
            if (f.isDirectory()) {
                this.addDir(out, f);
            } else if (f.isFile()) {
                this.addFile(out, f);
            }
        }
        out.close();
    }

    private void addDir(final ZipOutputStream out, final File dir) throws IOException {
        this.addDir(out, null, dir);
    }

    private void addFile(final ZipOutputStream out, final File file) throws IOException {
        this.addFile(out, null, file);
    }

    private void addDir(final ZipOutputStream out, final File parent, final File dir) throws IOException {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                String name = null;
                if (parent != null) {
                    name = parent.getName() + ZipWriter.DIR_SEPARATOR + dir.getName();
                } else {
                    name = dir.getName();
                }
                ZipEntry entry = new ZipEntry(name);
                out.putNextEntry(entry);
                this.addDir(out, dir, f);
            } else if (f.isFile()) {
                this.addFile(out, dir, f);
            }
        }
    }

    private void addFile(final ZipOutputStream out, final File parent, final File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        String name = null;
        if (parent != null) {
            name = parent.getName() + ZipWriter.DIR_SEPARATOR + file.getName();
        } else {
            name = file.getName();
        }
        out.putNextEntry(new ZipEntry(name));
        byte[] buf = new byte[1024];
        int len = input.read(buf);
        while (len > 0) {
            out.write(buf, 0, len);
            len = input.read(buf);
        }
        out.closeEntry();
        input.close();
    }
}
