package net.sf.jvifm.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtil {

    private byte[] buffer = new byte[10240];

    private String prefix = "";

    public static void main(String[] args) throws Exception {
        ZipUtil app = new ZipUtil();
        String[] paths2 = new String[] { "usb" };
        app.removeFromZip("model.zip", paths2);
    }

    @SuppressWarnings("unchecked")
    public void addToZip(String zipFilePath, String files[]) throws Exception {
        File tmpFile = new File("temp.zip");
        ZipFile zipFile = new ZipFile(new File(zipFilePath));
        ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(tmpFile));
        ZipEntry entry = null;
        ZipEntry outEntry = null;
        java.util.Enumeration e = zipFile.getEntries();
        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            outEntry = new ZipEntry(entry.getName());
            InputStream zi = zipFile.getInputStream(entry);
            zo.putNextEntry(outEntry);
            while (true) {
                int n = zi.read(buffer);
                if (n < 0) break;
                zo.write(buffer, 0, n);
            }
            zi.close();
            zo.closeEntry();
        }
        for (int i = 0; i < files.length; i++) {
            File file = new File(files[i]).getAbsoluteFile();
            prefix = file.getParent();
            if (!prefix.endsWith(File.separator)) prefix = prefix + File.separator;
            doZip(zo, file);
        }
        zo.close();
        zipFile.close();
        File oldZipFile = new File(zipFilePath);
        String zipFileName = oldZipFile.getName();
        oldZipFile.delete();
        tmpFile.renameTo(new File(zipFileName));
    }

    @SuppressWarnings("unchecked")
    public void removeFromZip(String zipFilePath, String entryNames[]) throws Exception {
        File tmpFile = new File("temp1.zip");
        ZipFile zipFile = new ZipFile(new File(zipFilePath));
        ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(tmpFile));
        ZipEntry entry = null;
        ZipEntry outEntry = null;
        boolean needRemove = false;
        java.util.Enumeration e = zipFile.getEntries();
        while (e.hasMoreElements()) {
            needRemove = false;
            entry = (org.apache.tools.zip.ZipEntry) e.nextElement();
            for (int i = 0; i < entryNames.length; i++) {
                String entryName = entry.getName();
                if (entryNames[i].equals(entryName)) {
                    needRemove = true;
                } else {
                    int length = entryNames[i].length();
                    if (entryName.startsWith(entryNames[i]) && (entryName.substring(length, length + 1).equals("/") || entryName.substring(length, length + 1).equals("\\"))) needRemove = true;
                }
            }
            if (needRemove) continue;
            outEntry = new ZipEntry(entry.getName());
            zo.putNextEntry(outEntry);
            InputStream zi = zipFile.getInputStream(entry);
            while (true) {
                int n = zi.read(buffer);
                if (n < 0) break;
                zo.write(buffer, 0, n);
            }
            zo.closeEntry();
            zi.close();
        }
        zipFile.close();
        zo.close();
        File oldZipFile = new File(zipFilePath);
        String zipFileName = oldZipFile.getName();
        oldZipFile.delete();
        tmpFile.renameTo(new File(zipFileName));
    }

    @SuppressWarnings("unchecked")
    public void zip(String filename, String[] paths) throws Exception {
        ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(new File(filename)));
        for (int i = 0; i < paths.length; i++) {
            File file = new File(paths[i]).getAbsoluteFile();
            prefix = file.getParent();
            if (!prefix.endsWith(File.separator)) prefix = prefix + File.separator;
            doZip(zo, file);
        }
        zo.close();
    }

    private void doZip(ZipOutputStream zo, File file) throws Exception {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                if (subFiles[i].isDirectory()) {
                    doZip(zo, subFiles[i]);
                } else {
                    putEntry(zo, subFiles[i]);
                }
            }
        } else {
            putEntry(zo, file);
        }
    }

    private void putEntry(ZipOutputStream zo, File file) throws Exception {
        String name = file.getPath().substring(prefix.length());
        ZipEntry entry = new ZipEntry(name);
        zo.putNextEntry(entry);
        BufferedInputStream bi = new BufferedInputStream(new FileInputStream(file));
        while (true) {
            int n = bi.read(buffer);
            if (n < 0) break;
            zo.write(buffer, 0, n);
        }
        zo.closeEntry();
        bi.close();
    }
}
