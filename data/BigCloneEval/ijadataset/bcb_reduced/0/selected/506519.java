package com.sun.org.apache.xml.internal.security.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A collection of different, general-purpose methods for JAVA-specific things
 * @author Christian Geuer-Pollmann
 *
 */
public class JavaUtils {

    /** {@link java.util.logging} logging facility */
    static java.util.logging.Logger log = java.util.logging.Logger.getLogger(JavaUtils.class.getName());

    private JavaUtils() {
    }

    /**
    * Method getBytesFromFile
    *
    * @param fileName
    * @return the bytes readed from the file
    *
    * @throws FileNotFoundException
    * @throws IOException
    */
    public static byte[] getBytesFromFile(String fileName) throws FileNotFoundException, IOException {
        byte refBytes[] = null;
        {
            FileInputStream fisRef = new FileInputStream(fileName);
            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            while ((len = fisRef.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            refBytes = baos.toByteArray();
        }
        return refBytes;
    }

    /**
    * Method writeBytesToFilename
    *
    * @param filename
    * @param bytes
    */
    public static void writeBytesToFilename(String filename, byte[] bytes) {
        try {
            if (filename != null && bytes != null) {
                File f = new File(filename);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bytes);
                fos.close();
            } else {
                if (log.isLoggable(java.util.logging.Level.FINE)) log.log(java.util.logging.Level.FINE, "writeBytesToFilename got null byte[] pointed");
            }
        } catch (Exception ex) {
        }
    }

    /**
    * This method reads all bytes from the given InputStream till EOF and returns
    * them as a byte array.
    *
    * @param inputStream
    * @return the bytes readed from the stream
    *
    * @throws FileNotFoundException
    * @throws IOException
    */
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        byte refBytes[] = null;
        {
            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            refBytes = baos.toByteArray();
        }
        return refBytes;
    }
}
