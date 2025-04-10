package com.emental.mindraider.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.emental.mindraider.ui.dialogs.ProgressDialogJFrame;

/**
 * ZIP helper.
 * 
 * @author Martin.Dvorak
 * @version $Revision: 1.3 $ ($Author: mindraider $)
 */
public class Zipper {

    /**
	 * Logger for this class.
	 */
    private static final Logger logger = Logger.getLogger(Zipper.class);

    /**
	 * Zip directory to archive.
	 * 
	 * @param zipFile
	 *            the zip file
	 * @param zippedDir
	 *            the zipped directory
	 * @param progress
	 *            the progress dialog JFrame
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             the generic I/O exception
	 */
    public static void zip(String zipFile, String zippedDir, ProgressDialogJFrame progress) throws FileNotFoundException, IOException {
        if (zipFile != null && zippedDir != null) {
            ZipOutputStream zipOutputStream = null;
            try {
                File file = new File(zippedDir);
                zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
                zipDirectory(zipOutputStream, zippedDir, file.getName(), progress);
            } catch (Exception e) {
                logger.error("zip() - Unable to zip directory!", e);
                logger.error("zip(String, String, ProgressDialogJFrame)", e);
            } finally {
                if (zipOutputStream != null) {
                    zipOutputStream.flush();
                    zipOutputStream.close();
                }
            }
        }
    }

    /**
	 * Zip directory.
	 * 
	 * @param zipOutputStream
	 *            the zip output stream
	 * @param zippedDir
	 *            the zipped directory
	 * @param relativeDir
	 *            the relative directory
	 * @param progress
	 *            the progress dialog JFrame
	 */
    public static void zipDirectory(ZipOutputStream zipOutputStream, String zippedDir, String relativeDir, ProgressDialogJFrame progress) {
        if (progress != null) {
            progress.setProgressMessage(relativeDir);
        } else {
            logger.debug("  Zipping " + zippedDir + " # " + relativeDir);
        }
        File zipDir = new File(zippedDir);
        String[] dirList = zipDir.list();
        if (dirList.length == 0) {
            String entry = (relativeDir == null ? zippedDir : relativeDir + File.separator);
            ZipEntry ze = new ZipEntry(entry);
            try {
                zipOutputStream.putNextEntry(ze);
            } catch (IOException e) {
                logger.debug("Unable to zip empty directory " + entry, e);
            }
        } else {
            for (int i = 0; i < dirList.length; i++) {
                File file = new File(zippedDir, dirList[i]);
                if (file.isDirectory()) {
                    zipDirectory(zipOutputStream, file.getPath(), (relativeDir == null ? dirList[i] : relativeDir + File.separator + dirList[i]), progress);
                } else {
                    FileInputStream fis = null;
                    try {
                        byte[] readBuffer = new byte[2048];
                        int bytesReaded = 0;
                        String fileToZip = file.getPath();
                        String entry = (relativeDir == null ? dirList[i] : relativeDir + File.separator + dirList[i]);
                        if (progress != null) {
                            progress.setProgressMessage(entry);
                        } else {
                            logger.debug("  Adding file " + fileToZip + " # " + entry);
                        }
                        fis = new FileInputStream(fileToZip);
                        ZipEntry ze = new ZipEntry(entry);
                        zipOutputStream.putNextEntry(ze);
                        while ((bytesReaded = fis.read(readBuffer)) != -1) {
                            zipOutputStream.write(readBuffer, 0, bytesReaded);
                        }
                    } catch (Exception e) {
                        logger.debug("Unable to zip: " + e.getMessage(), e);
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (Exception ee) {
                                logger.debug("Unable to close stream!", ee);
                            }
                        }
                    }
                }
            }
        }
    }
}
