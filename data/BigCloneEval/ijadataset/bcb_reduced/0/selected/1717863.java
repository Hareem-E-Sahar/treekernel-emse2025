package br.com.fc.service.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServiceZipClient {

    public static void zip(File outputFile, File... files) throws IOException {
        if (files != null && files.length > 0) {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            ZipOutputStream out = new ZipOutputStream(outputStream);
            Stack<File> parentDirs = new Stack<File>();
            zipFiles(parentDirs, files, out);
            out.close();
            outputStream.flush();
            outputStream.close();
        }
    }

    private static void zipFiles(Stack<File> parentDirs, File[] files, ZipOutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                parentDirs.push(files[i]);
                zipFiles(parentDirs, files[i].listFiles(), out);
                parentDirs.pop();
            } else {
                FileInputStream in = new FileInputStream(files[i]);
                String path = "";
                for (File parentDir : parentDirs) {
                    path += parentDir.getName() + File.separator;
                }
                out.putNextEntry(new ZipEntry(path + files[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        }
    }
}
