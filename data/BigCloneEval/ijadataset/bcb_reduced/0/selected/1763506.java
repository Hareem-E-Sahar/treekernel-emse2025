package com.netx.cubigraf.shared;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import com.netx.generics.basic.Checker;
import com.netx.generics.io.Directory;
import com.netx.generics.io.File;
import com.netx.generics.io.Streams;
import com.netx.generics.io.FileSystem;
import com.netx.generics.io.Location;
import com.netx.generics.io.ProgressObserver;
import com.netx.generics.util.Tools;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Zipper {

    public static void zip(Directory src, Directory dest) throws IOException {
        zip(src, dest, null);
    }

    public static void zip(Directory src, Directory dest, ProgressObserver observer) throws IOException {
        Checker.checkNull(src, "src");
        Checker.checkNull(dest, "dest");
        final String filename = src.getName() + ".zip";
        File zip = dest.createFile(filename);
        ZipOutputStream out = new ZipOutputStream(zip.getOutputStream());
        _zipFiles(out, src, "", observer);
        out.close();
    }

    public static void unzip(File src) throws IOException {
        Checker.checkNull(src, "src");
        unzip(src, src.getParent());
    }

    public static void unzip(File src, Directory dest) throws IOException {
        Checker.checkNull(src, "src");
        Checker.checkNull(dest, "dest");
        Directory zipDir = dest.mkdirs(src.getNameWithoutExtension());
        ZipInputStream in = new ZipInputStream(src.getInputStream());
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            File extractedFile = zipDir.createFile(entry.getName().replace("\\", "/"), true);
            Streams.copy(in, extractedFile.getOutputStream());
            entry = in.getNextEntry();
        }
    }

    public static boolean compare(Directory dir, File zip) throws IOException {
        dir = new FileSystem(new Location(dir.getPhysicalPath()));
        Map<String, File> fileMap = new HashMap<String, File>();
        _getAllSubFiles(dir, fileMap);
        ZipFile zipFile = new ZipFile(zip.getPhysicalPath());
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            String filename = entry.getName().replace('\\', '/');
            if (filename.startsWith("/")) {
                filename = filename.substring(1);
            }
            if (filename.endsWith(".DS_Store")) {
                fileMap.remove(filename);
                continue;
            }
            File f = fileMap.get(filename);
            if (f == null) {
                zipFile.close();
                return false;
            }
            if (f.getSize() != entry.getSize()) {
                zipFile.close();
                return false;
            }
            fileMap.remove(filename);
        }
        zipFile.close();
        return fileMap.isEmpty();
    }

    private static void _zipFiles(ZipOutputStream out, Directory src, String path, ProgressObserver observer) throws IOException {
        File[] list = src.getFiles();
        String tmpPath = path.equals("") ? "" : path + "/";
        for (int i = 0; i < list.length; i++) {
            String filename = tmpPath + list[i].getName();
            if (filename.endsWith(".DS_Store")) {
                continue;
            }
            ZipEntry entry = new ZipEntry(Tools.toSafeString(filename));
            entry.setTime(list[i].getLastModified().getTimeInMilliseconds());
            entry.setSize(list[i].getSize());
            out.putNextEntry(entry);
            Streams.copy(list[i].getInputStream(), out, observer);
            out.closeEntry();
        }
        out.flush();
        Directory[] dirs = src.getDirectories();
        for (int i = 0; i < dirs.length; i++) {
            _zipFiles(out, dirs[i], path + "/" + dirs[i].getName(), observer);
        }
    }

    private static void _getAllSubFiles(Directory dir, Map<String, File> map) throws IOException {
        for (File f : dir.getFiles()) {
            map.put(Tools.toSafeString(f.getPath().substring(1)), f);
        }
        for (Directory d : dir.getDirectories()) {
            _getAllSubFiles(d, map);
        }
    }
}
