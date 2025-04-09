package org.amlfilter.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

public class Zip {

    private static final int BUFFER_SIZE = 2048;

    private static Logger _logger = Logger.getLogger("org.amlfilter.util.Zip");

    /**
     * Compresses a file given the incoming file name and returns the compressed file
     * @param pIncomingFileName The incoming file name
     * @param pCompressedFileName The compressed file name
     */
    public static void compressFile(String pIncomingFileName, String pCompressedFileName) {
        final String methodSignature = "void compressFile(String, String): ";
        try {
            FileOutputStream dest = new FileOutputStream(pCompressedFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER_SIZE];
            File incomingFile = new File(pIncomingFileName);
            FileInputStream fis = new FileInputStream(incomingFile);
            BufferedInputStream origin = new BufferedInputStream(fis, BUFFER_SIZE);
            int pathOffset = pIncomingFileName.lastIndexOf("/");
            if (-1 != pathOffset) {
                pIncomingFileName = pIncomingFileName.substring(pathOffset + 1);
            }
            ZipEntry entry = new ZipEntry(pIncomingFileName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
