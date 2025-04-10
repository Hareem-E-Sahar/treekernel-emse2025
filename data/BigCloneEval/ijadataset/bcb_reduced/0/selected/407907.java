package net.sourceforge.cruisecontrol.distributed.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

public final class ZipUtil {

    private static final Logger LOG = Logger.getLogger(ZipUtil.class);

    private ZipUtil() {
    }

    public static void zipFolderContents(final String outFilename, final String folderToZip) {
        validateParams(outFilename, folderToZip);
        BufferedOutputStream bos = null;
        ZipOutputStream zipOut = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(outFilename));
            zipOut = new ZipOutputStream(bos);
            final File folder = new File(folderToZip);
            String message = "Zipping files from: " + folderToZip + " to: " + outFilename;
            LOG.info(message);
            zipFiles(folder, folder, zipOut);
            message = "Finished zipping files";
            LOG.info(message);
        } catch (FileNotFoundException fnfe) {
            final String message = "File not found while zipping files to: " + outFilename;
            LOG.error(message, fnfe);
            throw new RuntimeException(message, fnfe);
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
            } catch (ZipException ze) {
                final File file = new File(outFilename);
                if ((file.length() == 0) && (file.exists())) {
                    final String message = "Empty zip file created: " + outFilename;
                    LOG.debug(message);
                    try {
                        bos.close();
                    } catch (IOException e) {
                        LOG.error(e);
                    }
                    if (!file.delete()) {
                        throw new RuntimeException("Error deleting empty zip file: " + file.getAbsolutePath());
                    }
                    final String message2 = "Deleted empty zip file: " + outFilename;
                    LOG.debug(message2);
                }
            } catch (IOException ioe) {
                final String message = "Error occured while closing zip file: " + outFilename;
                LOG.error(message, ioe);
                throw new RuntimeException(message, ioe);
            }
        }
    }

    private static void zipFiles(final File rootDir, final File folderToZip, final ZipOutputStream zipOutputStream) {
        final byte[] buf = new byte[1024];
        final String relativePath = folderToZip.toString().substring(rootDir.toString().length());
        FileInputStream in;
        final File[] files = folderToZip.listFiles();
        for (int i = 0; i < files.length; i++) {
            final String filename = files[i].getName();
            if (files[i].isDirectory()) {
                final String dirName = relativePath + File.separator + filename;
                LOG.debug("adding dir [" + dirName + "]");
                zipFiles(rootDir, files[i], zipOutputStream);
            } else {
                String filePath = relativePath + File.separator + filename;
                if (filePath.charAt(0) == File.separatorChar) {
                    filePath = filePath.substring(1);
                }
                LOG.debug("adding file [" + filePath + "]");
                try {
                    in = new FileInputStream(new File(folderToZip, filename));
                    zipOutputStream.putNextEntry(new ZipEntry(filePath.replace(File.separatorChar, '/')));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                    zipOutputStream.closeEntry();
                    in.close();
                } catch (IOException ioe) {
                    final String message = "Error occured while zipping file " + filePath;
                    LOG.error(message, ioe);
                    throw new RuntimeException(message, ioe);
                }
            }
        }
    }

    private static void validateParams(final String outFilename, final String folderToZip) {
        if (outFilename == null) {
            final String message = "Missing output zip file name";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (new File(outFilename).isDirectory()) {
            final String message = "Output file already exists as directory";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (folderToZip == null) {
            final String message = "Missing folder to zip";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!(new File(folderToZip).isDirectory())) {
            final String message = "Target folder to zip does not exist or is not a directory";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param zipFilePath
     * @param toDirName
     * @throws IOException
     */
    public static void unzipFileToLocation(final String zipFilePath, final String toDirName) throws IOException {
        final ZipFile zipFile;
        final Enumeration enumr;
        boolean isEmptyFile = false;
        try {
            zipFile = new ZipFile(zipFilePath);
            if (zipFile.size() == 0) {
                isEmptyFile = true;
            } else {
                final String infoMessage = "Unzipping file: " + zipFilePath;
                LOG.info(infoMessage);
                enumr = zipFile.entries();
                while (enumr.hasMoreElements()) {
                    final ZipEntry target = (ZipEntry) enumr.nextElement();
                    final String message = "Exploding: " + target.getName();
                    LOG.debug(message);
                    saveItem(zipFile, toDirName, target);
                }
                zipFile.close();
            }
        } catch (FileNotFoundException fnfe) {
            final String message = "Could not find zip file" + zipFilePath;
            LOG.error(message, fnfe);
            throw new RuntimeException(message, fnfe);
        } catch (ZipException ze) {
            final String message = "Zip error occured while unzipping file " + zipFilePath;
            LOG.error(message, ze);
            throw new RuntimeException(message, ze);
        } catch (IOException ioe) {
            final String message = "Error occured while unzipping file " + zipFilePath;
            LOG.error(message, ioe);
            throw new RuntimeException(message, ioe);
        }
        if (isEmptyFile) {
            final String message = "Zip file has no entries: " + zipFilePath;
            LOG.warn(message);
            throw new IOException(message);
        }
        final String infoMessage = "Unzip complete";
        LOG.info(infoMessage);
    }

    private static void saveItem(final ZipFile zipFile, final String rootDirName, final ZipEntry entry) throws ZipException, IOException {
        final InputStream is;
        BufferedInputStream inStream = null;
        final FileOutputStream outStream;
        BufferedOutputStream bufferedOutStream = null;
        try {
            final File file = new File(rootDirName, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                is = zipFile.getInputStream(entry);
                inStream = new BufferedInputStream(is);
                final File dir = new File(file.getParent());
                dir.mkdirs();
                outStream = new FileOutputStream(file);
                bufferedOutStream = new BufferedOutputStream(outStream);
                int c;
                while ((c = inStream.read()) != -1) {
                    bufferedOutStream.write((byte) c);
                }
            }
        } catch (ZipException ze) {
            final String message = "Zip error unzipping entry: " + entry.getName();
            LOG.error(message, ze);
            throw new RuntimeException(message, ze);
        } catch (IOException ioe) {
            final String message = "I/O error unzipping entry: " + entry.getName();
            LOG.error(message, ioe);
            throw new RuntimeException(message, ioe);
        } finally {
            if (bufferedOutStream != null) {
                bufferedOutStream.close();
            }
            if (inStream != null) {
                inStream.close();
            }
        }
    }
}
