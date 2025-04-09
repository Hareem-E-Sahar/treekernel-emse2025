package es.aeat.eett.rubik.drivers;

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
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 
 * Zip utilities
 *
 */
class ZipUtil {

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

    static void unZip(File fzipIn, File fDes, String nameEntry) throws Exception {
        new ZipUtil(fzipIn, null).unZip(fDes, nameEntry);
    }

    static boolean contains(File fzipIn, String nameEntry) throws Exception {
        return new ZipUtil(fzipIn, null).contains(nameEntry);
    }

    private void unZip(File folderOut) throws Exception {
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        try {
            valid(folderOut);
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                File fDes = new File(folderOut, entry.getName());
                if (entry.getName().endsWith("/")) {
                    if (!fDes.exists()) {
                        if (!fDes.mkdirs()) {
                            throw new IOException("failed create folder " + fDes.getAbsolutePath());
                        }
                    }
                } else {
                    FileOutputStream fos = new FileOutputStream(fDes);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (dest != null) {
                dest.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    private void unZip(File fDes, String nameEntry) throws Exception {
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().equals(nameEntry)) {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(fDes);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                    return;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (dest != null) {
                dest.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    private void valid(File folderOut) throws IOException {
        if (!folderOut.exists()) {
            if (!folderOut.mkdirs()) {
                throw new IOException("failed create folder " + folderOut.getAbsolutePath());
            }
        } else {
            if (!folderOut.isDirectory()) {
                throw new IOException("no is folder " + folderOut.getAbsolutePath());
            }
        }
    }

    private void zip(File folderToZip) throws Exception {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(zipOut);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte data[] = new byte[BUFFER];
        List list = getListFiles(folderToZip, new ArrayList());
        for (int i = 0; i < list.size(); i++) {
            FileInputStream fi = new FileInputStream((File) list.get(i));
            origin = new BufferedInputStream(fi, BUFFER);
            File f = (File) list.get(i);
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

    List getListFiles(File f, List list) {
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
        BufferedInputStream is = null;
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(fzipIn);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.getName().equals(nameEntry)) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return false;
    }
}
