package org.kwantu.m2.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;

/**
 * Delete a directory tree with one operation.
 * */
public final class FileUtil {

    /** Logging.*/
    public static final Log LOG = LogFactory.getLog(FileUtil.class);

    private FileUtil() {
    }

    /** Delete a directory tree with one operation.
     *
     * @param path The directory that shall be deleted including it's contents
     * @return true if path was deleted, false otherwise
     */
    public static boolean recursiveDeleteDirectory(final File path) {
        LOG.info(">>recursiveDeleteDirectory");
        if (path.exists()) {
            LOG.info("deleting files in directory " + path);
            for (File file : path.listFiles()) {
                LOG.info("looking at file " + file);
                if (file.isDirectory()) {
                    boolean result = recursiveDeleteDirectory(file);
                    if (!result) {
                        return false;
                    }
                } else {
                    LOG.info("delete file " + file);
                    file.delete();
                }
            }
        }
        LOG.info("<<recursiveDeleteDirectory");
        return path.delete();
    }
}
