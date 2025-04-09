package com.nhncorp.cubridqa.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A zip utility for zip a directory.
 * 
 * @author Li Fei Peng
 * @version 1.0, 2009-09-12
 * 
 */
public class ZipUtil {

    public static void zipDir(String zipFileName, String dir) throws Exception {
        File dirObj = new File(dir);
        File rootDir = dirObj.getParentFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        System.out.println("Creating : " + zipFileName);
        addDir(rootDir, dirObj, out);
        out.close();
    }

    private static void addDir(File rootDir, File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(rootDir, files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            System.out.println(" Adding: " + files[i].getAbsolutePath());
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1)));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

    public static void main(String[] args) throws Exception {
        zipDir("E:\\mysql_test.zip", "E:\\vs_projects\\mysql_test");
    }
}
