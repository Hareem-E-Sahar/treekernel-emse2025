package org.openremote.modeler.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openremote.modeler.exception.FileOperationException;

/**
 * Util class for compressing or unzipping files.
 *  
 * @author Allen, Handy
 */
public class ZipUtils {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ZipUtils.class);

    /**
    * Instantiates a new zip utils.
    */
    private ZipUtils() {
    }

    /**
    * Compress a list of files into the output file.
    * 
    * @param outputFilePath the output file path
    * @param files the files
    * 
    * @return the file
    */
    public static File compress(String outputFilePath, List<File> files) {
        final int buffer = 2048;
        BufferedInputStream bufferedInputStream;
        File outputFile = new File(outputFilePath);
        try {
            FileUtils.touch(outputFile);
        } catch (IOException e) {
            LOGGER.error("create zip file fail.", e);
            throw new FileOperationException("create zip file fail.", e);
        }
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileInputStream fileInputStream;
        try {
            fileOutputStream = new FileOutputStream(outputFilePath);
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
            byte[] data = new byte[buffer];
            for (File file : files) {
                if (!file.exists()) {
                    continue;
                }
                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream, buffer);
                ZipEntry entry = new ZipEntry(file.getName());
                entry.setSize(file.length());
                entry.setTime(file.lastModified());
                zipOutputStream.putNextEntry(entry);
                int count;
                while ((count = bufferedInputStream.read(data, 0, buffer)) != -1) {
                    zipOutputStream.write(data, 0, count);
                }
                zipOutputStream.closeEntry();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Can't find the output file.", e);
            throw new FileOperationException("Can't find the output file.", e);
        } catch (IOException e) {
            LOGGER.error("Can't compress file to zip archive, occured IOException", e);
            throw new FileOperationException("Can't compress file to zip archive, occured IOException", e);
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("Close zipOutputStream and fileOutputStream occur IOException", e);
            }
        }
        return outputFile;
    }

    /**
    * Unzip a zip.
    * 
    * @param inputStream the input stream
    * @param targetDir the target dir
    * 
    * @return true, if success
    */
    public static boolean unzip(InputStream inputStream, String targetDir) {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        FileOutputStream fileOutputStream = null;
        try {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    targetDir = targetDir.endsWith("/") || targetDir.endsWith("\\") ? targetDir : targetDir + "/";
                    File zippedFile = new File(targetDir + zipEntry.getName());
                    FileUtilsExt.deleteQuietly(zippedFile);
                    fileOutputStream = new FileOutputStream(zippedFile);
                    int b;
                    while ((b = zipInputStream.read()) != -1) {
                        fileOutputStream.write(b);
                    }
                    fileOutputStream.close();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Can't unzip to " + targetDir, e);
            return false;
        } finally {
            try {
                zipInputStream.closeEntry();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("Error while closing stream.", e);
            }
        }
        return true;
    }

    public static boolean unzip(File zipFile, String targetDir) {
        try {
            InputStream inputStream = new FileInputStream(zipFile);
            return unzip(inputStream, targetDir);
        } catch (Exception e) {
            throw new FileOperationException("Failed to unzip file" + zipFile.getName(), e);
        }
    }
}
