package com.wuala.loader2.loader.data;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.wuala.loader2.copied.Util;

public class InitialLocalCompilation extends AbstractLocalCompilation {

    public static final String SUFFIX = ".init";

    private File path;

    private HashMap<String, ItemData> allData;

    public InitialLocalCompilation(File path, byte type) {
        super(type);
        this.path = path;
        this.allData = new HashMap<String, ItemData>();
        try {
            String name = PREFIX.substring(0, PREFIX.length() - 1) + type + SUFFIX;
            File file = new File(path, name);
            if (!file.exists()) {
                file = new File(path.getName(), name);
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] data = Util.toByteArray(fis);
            InputStream is = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(new InflaterInputStream(new BufferedInputStream(is, 8192)));
            try {
                while (true) {
                    ItemData item = new ItemData(dis);
                    allData.put(item.getName(), item);
                }
            } finally {
                dis.close();
            }
        } catch (IOException e) {
        }
    }

    @Override
    public File getPath() {
        return path;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    public Collection<ItemData> getAllItems() {
        return allData.values();
    }

    public ItemData getOrNull(String name) {
        return allData.get(name);
    }

    @Override
    public ItemData get(String name, short version) throws IOException {
        ItemData data = allData.get(name);
        return data == null || data.getVersion() != version ? null : data;
    }

    public synchronized String getLibrary(String name) throws IOException {
        File libPath = new File(path, name);
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

    private long getSize(String name) {
        ItemData data = allData.get(name);
        return data == null ? 0 : data.getBytes().length;
    }

    public int getVersion(String name) {
        ItemData data = allData.get(name);
        return data == null ? 0 : data.getVersion();
    }

    public int getVersion() {
        return 0;
    }

    @Override
    protected void writeExistings(HashSet<String> obsolete, ZipOutputStream zipOut) throws IOException {
        for (ItemData data : allData.values()) {
            if (!obsolete.contains(data.getName())) {
                ZipEntry newEntry = data.createZipEntry();
                zipOut.putNextEntry(newEntry);
                zipOut.write(data.getBytes());
                zipOut.closeEntry();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return allData.isEmpty();
    }
}
