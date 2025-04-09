package com.spun.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A static class of convence functions for Files
 **/
public class ZipUtils {

    /***********************************************************************/
    public static File zipDirectory(String directory, String zipFileName) throws IOException {
        return zipDirectory(new File(directory), new File(zipFileName));
    }

    /***********************************************************************/
    public static File zipDirectory(File directory, File zipFileName) throws IOException {
        return doCreateZipFile(FileUtils.getRecursiveFileList(directory), zipFileName);
    }

    /***********************************************************************/
    public static File doCreateZipFile(File[] files, File zipFile) throws IOException {
        byte[] buf = new byte[1024];
        zipFile.getParentFile().mkdirs();
        FileOutputStream fileOut = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(fileOut);
        for (int i = 0; i < files.length; i++) {
            FileInputStream in = new FileInputStream(files[i]);
            out.putNextEntry(new ZipEntry(files[i].getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
        fileOut.close();
        return zipFile;
    }

    /***********************************************************************/
    public static File[] doUnzip(File destination, File zipFile) throws IOException {
        ArrayList<File> list = new ArrayList<File>();
        byte[] buf = new byte[1024];
        FileInputStream fileIn = new FileInputStream(zipFile);
        ZipInputStream in = new ZipInputStream(fileIn);
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            File file = new File(destination, entry.getName());
            list.add(file);
            FileOutputStream out = new FileOutputStream(file);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.closeEntry();
            out.close();
            entry = in.getNextEntry();
        }
        in.close();
        fileIn.close();
        return list.toArray(new File[0]);
    }

    /***********************************************************************/
    public static void main(String args[]) throws IOException {
        zipDirectory("c:\\t", "c:\\t\\t.zip");
    }
}
