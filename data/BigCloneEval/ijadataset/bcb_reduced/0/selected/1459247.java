package org.apache.log4j.rolling.helper;

import org.apache.log4j.helpers.LogLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compresses a file using Zip compression.
 *
 * @author Curt Arnold
 */
public final class ZipCompressAction extends ActionBase {

    /**
   * Source file.
   */
    private final File source;

    /**
   * Destination file.
   */
    private final File destination;

    /**
   * If true, attempt to delete file on completion.
   */
    private final boolean deleteSource;

    /**
   * Create new instance of GZCompressAction.
   *
   * @param source file to compress, may not be null.
   * @param destination compressed file, may not be null.
   * @param deleteSource if true, attempt to delete file on completion.  Failure to delete
   * does not cause an exception to be thrown or affect return value.
   */
    public ZipCompressAction(final File source, final File destination, final boolean deleteSource) {
        if (source == null) {
            throw new NullPointerException("source");
        }
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        this.source = source;
        this.destination = destination;
        this.deleteSource = deleteSource;
    }

    /**
   * Compress.
   * @return true if successfully compressed.
   * @throws IOException on IO exception.
   */
    public boolean execute() throws IOException {
        return execute(source, destination, deleteSource);
    }

    /**
   * Compress a file.
   *
   * @param source file to compress, may not be null.
   * @param destination compressed file, may not be null.
   * @param deleteSource if true, attempt to delete file on completion.  Failure to delete
   * does not cause an exception to be thrown or affect return value.
   * @return true if source file compressed.
   * @throws IOException on IO exception.
   */
    public static boolean execute(final File source, final File destination, final boolean deleteSource) throws IOException {
        if (source.exists()) {
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry zipEntry = new ZipEntry(source.getName());
            zos.putNextEntry(zipEntry);
            byte[] inbuf = new byte[8102];
            int n;
            while ((n = fis.read(inbuf)) != -1) {
                zos.write(inbuf, 0, n);
            }
            zos.close();
            fis.close();
            if (deleteSource && !source.delete()) {
                LogLog.warn("Unable to delete " + source.toString() + ".");
            }
            return true;
        }
        return false;
    }

    /**
     * Capture exception.
     *
     * @param ex exception.
     */
    protected void reportException(final Exception ex) {
        LogLog.warn("Exception during compression of '" + source.toString() + "'.", ex);
    }
}
