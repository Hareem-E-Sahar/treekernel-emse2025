package com.duroty.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DOCUMENT ME!
 *
 * @author jordi marques
 */
public final class CopyIO {

    /**
     * Creates a new CopyInOut object.
     */
    private CopyIO() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileIn DOCUMENT ME!
     * @param fileOut DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyInOut(String fileIn, String fileOut) throws IOException {
        copyInOut(new FileInputStream(fileIn), new FileOutputStream(fileOut));
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileIn DOCUMENT ME!
     * @param fileOut DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyInOut(File fileIn, File fileOut) throws IOException {
        copyInOut(new FileInputStream(fileIn), new FileOutputStream(fileOut));
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param fileOut DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyInOut(InputStream in, File fileOut) throws IOException {
        copyInOut(in, new FileOutputStream(fileOut));
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param fileOut DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyInOut(InputStream in, String fileOut) throws IOException {
        copyInOut(in, new FileOutputStream(fileOut));
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static void copyInOut(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param srcDir DOCUMENT ME!
     * @param dstDir DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copyInOut(srcDir, dstDir);
        }
    }
}
