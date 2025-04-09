package net.woodstock.rockapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriter {

    public static final String SEPARATOR = "/";

    public static final String BLANK = "";

    private Set<File> files;

    public ZipWriter() {
        this.files = new LinkedHashSet<File>(0);
    }

    public void add(File... files) {
        for (File f : files) {
            this.files.add(f);
        }
    }

    public void add(String... files) {
        for (String s : files) {
            File f = new File(s);
            this.files.add(f);
        }
    }

    public void del(File... files) {
        for (File f : files) {
            this.files.remove(f);
        }
    }

    public void del(String... files) {
        for (String s : files) {
            for (File d : this.files) {
                if (d.getName().equals(s)) {
                    this.files.remove(d);
                }
            }
        }
    }

    public Collection<File> list() {
        Collection<File> list = new LinkedHashSet<File>();
        list.addAll(this.files);
        return list;
    }

    public void save(String fileName) throws IOException {
        this.save(new File(fileName));
    }

    public void save(File file) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        for (File f : this.files) {
            if (f.isDirectory()) {
                ZipEntry entry = new ZipEntry(f.getName() + ZipWriter.SEPARATOR);
                out.putNextEntry(entry);
                out.closeEntry();
                ZipWriter.addDir(out, f);
            } else if (f.isFile()) {
                ZipWriter.addFile(out, f);
            }
        }
        out.close();
    }

    private static void addDir(ZipOutputStream out, File dir) throws IOException {
        ZipWriter.addDir(out, null, dir);
    }

    private static void addFile(ZipOutputStream out, File file) throws IOException {
        ZipWriter.addFile(out, null, file);
    }

    private static void addDir(ZipOutputStream out, File parent, File dir) throws IOException {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                String name = (parent != null ? parent.getName() + ZipWriter.SEPARATOR : ZipWriter.BLANK) + dir.getName() + ZipWriter.SEPARATOR;
                out.putNextEntry(new ZipEntry(name));
                out.closeEntry();
                ZipWriter.addDir(out, dir, f);
            } else if (f.isFile()) {
                ZipWriter.addFile(out, dir, f);
            }
        }
    }

    private static void addFile(ZipOutputStream out, File parent, File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        String name = (parent != null ? parent.getName() + ZipWriter.SEPARATOR : ZipWriter.BLANK) + file.getName();
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
