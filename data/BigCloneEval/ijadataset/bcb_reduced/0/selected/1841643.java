package com.wuala.loader2.loader.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import com.wuala.loader2.copied.Util;

public class LocalCompilation extends AbstractLocalCompilation {

    private File file;

    private int version;

    private ZipFile zipFile;

    public LocalCompilation(File file, byte type) throws IOException {
        super(type);
        this.file = file;
        this.version = extractVersion(file.getName());
        this.zipFile = new ZipFile(file);
    }

    private int extractVersion(String name) {
        return Integer.parseInt(name.substring(PREFIX.length()));
    }

    public int getVersion() {
        return version;
    }

    public synchronized int getVersion(String key) {
        ZipEntry entry = zipFile == null ? null : zipFile.getEntry(key);
        if (entry == null) {
            return 0;
        } else {
            return ItemData.extractVersion(entry);
        }
    }

    public synchronized ItemData getOrNull(String name) {
        try {
            ZipEntry entry = zipFile.getEntry(name);
            return entry == null ? null : new ItemData(zipFile, entry);
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized ItemData get(String name, short version) throws IOException {
        ZipEntry entry = zipFile.getEntry(name);
        if (entry != null && ItemData.extractVersion(entry) == version) {
            return new ItemData(name, version, (int) entry.getSize(), zipFile.getInputStream(entry));
        } else {
            return null;
        }
    }

    public File getPath() {
        return file.getParentFile();
    }

    public long getSize(String name) {
        ZipEntry entry = zipFile.getEntry(name);
        if (entry == null) {
            return 0;
        } else {
            return entry.getSize();
        }
    }

    public synchronized String getLibrary(String name) throws IOException {
        File libPath = new File(file.getParentFile(), name);
        if (libPath.exists() && libPath.length() == getSize(name)) {
            return libPath.getAbsolutePath();
        } else {
            ItemData data = get(name);
            if (data == null) {
                return name;
            } else {
                data.write(libPath);
                return libPath.getAbsolutePath();
            }
        }
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    protected void writeExistings(HashSet<String> obsolete, ZipOutputStream zipOut) throws IOException {
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zEntry = (ZipEntry) entries.nextElement();
            if (!obsolete.contains(zEntry.getName())) {
                ZipEntry newEntry = new ZipEntry(zEntry);
                InputStream is = zipFile.getInputStream(zEntry);
                try {
                    zipOut.putNextEntry(newEntry);
                    Util.copy(is, zipOut);
                    zipOut.closeEntry();
                } finally {
                    is.close();
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
