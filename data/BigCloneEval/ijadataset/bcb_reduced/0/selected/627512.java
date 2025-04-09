package com.xith3d.utility.general;

import java.io.*;
import java.util.zip.*;

/**
 * <p> </p>
 * <p> </p>
 * <p> Copyright (c) 2000-2003, David J. Yazel</p>
 * <p> Teseract Software, LLP</p>
 * @author David Yazel
 *
 */
public class ZipUtility {

    public ZipUtility() {
    }

    public static void zipFiles(String filename, String[] files) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        byte[] buffer = new byte[10000];
        for (int i = 0; i < files.length; i++) {
            File f = new File(files[i]);
            if (f.exists()) {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(files[i]));
                zip.putNextEntry(new ZipEntry(files[i]));
                boolean done = false;
                while (!done) {
                    int num = in.read(buffer);
                    if (num > 0) {
                        zip.write(buffer, 0, num);
                    }
                    done = (num < buffer.length);
                }
                in.close();
            }
        }
        zip.flush();
        zip.close();
    }

    public static void main(String[] args) {
        ZipUtility zipUtility1 = new ZipUtility();
    }
}
