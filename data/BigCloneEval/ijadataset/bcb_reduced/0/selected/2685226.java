package org.apache.xml.security.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A collection of different, general-purpose methods for JAVA-specific things
 * @author Christian Geuer-Pollmann
 */
public class JavaUtils {

    /** {@link org.apache.commons.logging} logging facility */
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(JavaUtils.class);

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
        FileInputStream fisRef = new FileInputStream(fileName);
        try {
            UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            while ((len = fisRef.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            refBytes = baos.toByteArray();
        } finally {
            fisRef.close();
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
        FileOutputStream fos = null;
        try {
            if (filename != null && bytes != null) {
                File f = new File(filename);
                fos = new FileOutputStream(f);
                fos.write(bytes);
                fos.close();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("writeBytesToFilename got null byte[] pointed");
                }
            }
        } catch (IOException ex) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    if (log.isDebugEnabled()) {
                        log.debug(ioe);
                    }
                }
            }
        }
    }

    /**
     * This method reads all bytes from the given InputStream till EOF and 
     * returns them as a byte array.
     *
     * @param inputStream
     * @return the bytes read from the stream
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
        byte buf[] = new byte[4 * 1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }
}
