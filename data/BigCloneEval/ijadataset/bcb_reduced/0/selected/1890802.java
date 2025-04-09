package com.wuala.loader2.loader.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.ZipOutputStream;
import com.wuala.loader2.service.IApplicationInfo;
import com.wuala.loader2.service.ICompilation;

public abstract class AbstractLocalCompilation implements ICompilation {

    public static final String PREFIX = IApplicationInfo.APP_NAME + ".";

    private static final String TEMP_FILE = PREFIX + ".temp";

    public static final int BUFFER = 100000;

    private byte type;

    public AbstractLocalCompilation(byte type) {
        this.type = type;
    }

    public byte getCompilationType() {
        return type;
    }

    public String getName() {
        return NAMES[getCompilationType()];
    }

    public abstract boolean isEmpty();

    public abstract File getPath();

    public abstract ItemData getOrNull(String name);

    public final ItemData get(String name) throws IOException {
        ItemData data = getOrNull(name);
        if (data == null) {
            throw new IOException(name + " not found");
        } else {
            return data;
        }
    }

    public abstract ItemData get(String name, short version) throws IOException;

    public abstract boolean isComplete();

    public static AbstractLocalCompilation createCompilation(File basePath, byte type, int build) {
        basePath.mkdirs();
        File resources = null;
        if (build > 0 && new File(basePath, PREFIX + build).exists()) {
            resources = new File(basePath, PREFIX + build);
        } else {
            resources = findMostRecent(basePath);
        }
        if (resources == null) {
            return new InitialLocalCompilation(basePath, type);
        } else {
            try {
                LocalCompilation comp = new LocalCompilation(resources, type);
                cleanup(basePath, resources.getName());
                return comp;
            } catch (IOException e) {
                if (resources.delete()) {
                    return createCompilation(basePath, type, build);
                } else {
                    return new InitialLocalCompilation(basePath, type);
                }
            }
        }
    }

    private static File findMostRecent(File basePath) {
        File[] files = basePath.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.startsWith(PREFIX);
            }
        });
        File mostRecent = null;
        if (files != null) {
            int mostRecentV = 0;
            for (File file : files) {
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                try {
                    int v = Integer.parseInt(name.substring(dot + 1));
                    if (mostRecentV < v) {
                        mostRecentV = v;
                        mostRecent = file;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return mostRecent;
    }

    private static void cleanup(File basePath, final String skip) {
        File[] files = basePath.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.startsWith(PREFIX) && !name.equals(skip);
            }
        });
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public LocalCompilation writeSuccessor(HashSet<String> obsolete, Collection<ItemData> newItems, int version) throws IOException {
        File path = getPath();
        path.mkdirs();
        File tempFile = new File(path, TEMP_FILE);
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile), BUFFER));
        try {
            if (obsolete != null) {
                writeExistings(obsolete, zipOut);
            }
            for (ItemData item : newItems) {
                if (item.isExisting()) {
                    zipOut.putNextEntry(item.createZipEntry());
                    zipOut.write(item.getBytes());
                    zipOut.closeEntry();
                }
            }
        } finally {
            zipOut.close();
        }
        File next = new File(getPath(), PREFIX + version);
        if (next.exists()) {
            next.delete();
        }
        tempFile.renameTo(next);
        return new LocalCompilation(next, getCompilationType());
    }

    protected abstract void writeExistings(HashSet<String> obsolete, ZipOutputStream zipOut) throws IOException;

    public static byte getByName(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equalsIgnoreCase(name)) {
                return (byte) i;
            }
        }
        throw new IllegalArgumentException(name + " is no valid compilation name");
    }
}
