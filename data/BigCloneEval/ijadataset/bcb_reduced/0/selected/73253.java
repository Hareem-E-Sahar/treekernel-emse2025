package org.dbe.toolkit.portal.ui.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

/**
 * @author andy-edmonds
 */
public class UIResourcesZipper {

    private static final String NIX_FILEPATH_SEP = "/";

    private static boolean relative = true;

    private static String relativeDir = "";

    /**
     * create a zip file from a list of files it'll recursively zip if a file's
     * a directory
     * 
     * @param logger
     * @param file Name of the zip file to create
     * @param saveTo location to save the created zip file to
     * @throws Exception
     */
    public static void zip(Logger logger, String file, String saveTo) throws IOException {
        ZipOutputStream zos = null;
        File[] f = { new File(file) };
        if (relative) {
            relativeDir = f[0].getParent();
            logger.debug("Relative directory is: " + relativeDir);
        }
        try {
            zos = new ZipOutputStream(new FileOutputStream(saveTo));
            zip(logger, f, zos);
            zos.close();
        } catch (IOException e) {
            logger.error("Unable to zip: " + e);
            throw e;
        }
    }

    private static void zip(Logger logger, File[] files, ZipOutputStream zos) throws IOException {
        byte[] buf = new byte[1024];
        try {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    if (relative) {
                        logger.debug("New dir entry: " + f.getAbsolutePath().substring(relativeDir.length() + 1, f.getAbsolutePath().length()) + NIX_FILEPATH_SEP);
                        zos.putNextEntry(new ZipEntry(f.getAbsolutePath().substring(relativeDir.length() + 1, f.getAbsolutePath().length()) + NIX_FILEPATH_SEP));
                    } else {
                        logger.debug("New dir entry: " + f.getAbsolutePath() + NIX_FILEPATH_SEP);
                        zos.putNextEntry(new ZipEntry(f.getAbsolutePath() + NIX_FILEPATH_SEP));
                    }
                    File[] dirList = f.listFiles();
                    zip(logger, dirList, zos);
                    continue;
                }
                FileInputStream in = new FileInputStream(f.getAbsolutePath());
                if (relative) {
                    logger.debug("New file entry: " + f.getAbsolutePath().substring(relativeDir.length(), f.getAbsolutePath().length()));
                    zos.putNextEntry(new ZipEntry(f.getAbsolutePath().substring(relativeDir.length(), f.getAbsolutePath().length())));
                } else {
                    logger.debug(f.getAbsolutePath());
                    zos.putNextEntry(new ZipEntry(f.getAbsolutePath()));
                }
                int len;
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                in.close();
                zos.closeEntry();
            }
        } catch (IOException e) {
            logger.error("IO Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sets archive creation to 'relative'
     * 
     * @param relative a boolean to set the flag
     */
    public static void setRelative(boolean relative) {
        UIResourcesZipper.relative = relative;
    }
}
