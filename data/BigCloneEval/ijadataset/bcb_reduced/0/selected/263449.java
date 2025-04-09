package orcajo.azada.jdbc.drivers.handlers.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 
 * Zip utilities
 *
 */
public class ZipUtil {

    private final int BUFFER = 2048;

    private final File zipOut;

    private final File fzipIn;

    ZipUtil(File fzipIn, File zipOut) {
        this.fzipIn = fzipIn;
        this.zipOut = zipOut;
    }

    static void zip(File zipFolde, File zipOut) throws Exception {
        new ZipUtil(null, zipOut).zip(zipFolde);
    }

    static void unZip(File fzipIn, File folderOut) throws Exception {
        new ZipUtil(fzipIn, null).unZip(folderOut);
    }

    public static void unZip(File fzipIn, File fDes, String nameEntry) throws Exception {
        new ZipUtil(fzipIn, null).unZip(fDes, nameEntry);
    }

    static boolean contains(File fzipIn, String nameEntry) throws Exception {
        return new ZipUtil(fzipIn, null).contains(nameEntry);
    }

    static List<String> getPackages(File fzipIn) throws ZipException, IOException {
        return new ZipUtil(fzipIn, null).getPackages();
    }

    private void unZip(File folderOut) throws Exception {
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        try {
            valid(folderOut);
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration<?> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                File fDes = new File(folderOut, entry.getName());
                if (entry.getName().endsWith("/")) {
                    if (!fDes.exists()) {
                        if (!fDes.mkdirs()) {
                            throw new IOException("create folder failed" + fDes.getAbsolutePath());
                        }
                    }
                } else {
                    FileOutputStream foutputStream = new FileOutputStream(fDes);
                    outputStream = new BufferedOutputStream(foutputStream, BUFFER);
                    while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                        outputStream.write(data, 0, count);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void unZip(File fDes, String nameEntry) throws Exception {
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration<?> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().equals(nameEntry)) {
                    inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fOutputStream = new FileOutputStream(fDes);
                    outputStream = new BufferedOutputStream(fOutputStream, BUFFER);
                    while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                        outputStream.write(data, 0, count);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                    return;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void valid(File folderOut) throws IOException {
        if (!folderOut.exists()) {
            if (!folderOut.mkdirs()) {
                throw new IOException("create folder failed" + folderOut.getAbsolutePath());
            }
        } else {
            if (!folderOut.isDirectory()) {
                throw new IOException("no is folder " + folderOut.getAbsolutePath());
            }
        }
    }

    private void zip(File folderToZip) throws Exception {
        BufferedInputStream origin = null;
        FileOutputStream fOutputStream = new FileOutputStream(zipOut);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(fOutputStream));
        byte data[] = new byte[BUFFER];
        List<File> list = getListFiles(folderToZip, new ArrayList<File>());
        for (int i = 0; i < list.size(); i++) {
            FileInputStream fi = new FileInputStream(list.get(i));
            origin = new BufferedInputStream(fi, BUFFER);
            File f = list.get(i);
            String entryName = f.getAbsolutePath().substring(folderToZip.getAbsolutePath().length() + 1);
            entryName = entryName.replaceAll("\\\\", "/");
            ZipEntry entry = new ZipEntry(entryName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
    }

    List<File> getListFiles(File f, List<File> list) {
        if (f.isDirectory()) {
            for (int i = 0; i < f.listFiles().length; i++) {
                getListFiles(f.listFiles()[i], list);
            }
        } else if (f.isFile()) {
            list.add(f);
        }
        return list;
    }

    private boolean contains(String nameEntry) throws Exception {
        BufferedInputStream inputStream = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration<?> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().equals(nameEntry)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return false;
    }

    private List<String> getPackages() throws ZipException, IOException {
        List<String> packages = new ArrayList<String>();
        BufferedInputStream inputStream = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration<?> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 6);
                    int endIndex = name.lastIndexOf('/');
                    if (endIndex > -1) {
                        name = name.substring(0, endIndex);
                    }
                    name = name.replaceAll("/", "\\.");
                    if (!packages.contains(name)) {
                        packages.add(name);
                    }
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return packages;
    }
}
