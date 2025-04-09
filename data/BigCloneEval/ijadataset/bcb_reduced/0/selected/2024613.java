package org.ourgrid.common.util.packagemanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This class provides the operations of compressing and decompressing 
 * a Zip file.
 * It is important to notice that this class is about three times
 * slower than an optmail zip implementation (e.g. Linux unzip tool).
 * @author Jemerson Damasio - jemersonfd@gmail.com
 */
public class ZipPackageManager {

    /**
	 * This is a standard buffer size for writing bytes on the disk.
	 */
    private static final int BUFFER_SIZE = 4096;

    /**
	 * This method provides an operation of decompressing a standard 
	 * Zip file to the specified directory.
	 * If the specified directory does no exists it will be created
	 * if possible.
	 * @param zipFile the file under Zip format to be decompressed.
	 * @param destDir the destination directory.
	 * @throws IOException if any I/O error occur during the extraction.
	 */
    public void unpackIt(File zipFile, String destDir) throws ZipException, IOException {
        File destinyDirectory = new File(destDir);
        ZipFile zip = new ZipFile(zipFile);
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            if (!destinyDirectory.exists()) {
                destinyDirectory.mkdirs();
            }
            if (!destinyDirectory.exists() || !destinyDirectory.isDirectory()) {
                throw new IOException("This directory is not valid");
            }
            Enumeration e = zip.entries();
            File file;
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                file = new File(destinyDirectory, entry.getName());
                if (entry.isDirectory() && !file.exists()) {
                    file.mkdirs();
                    continue;
                }
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                is = zip.getInputStream(entry);
                os = new FileOutputStream(file);
                int bytesLidos = 0;
                if (is == null) {
                    throw new ZipException("Error on reading: " + entry.getName());
                }
                while ((bytesLidos = is.read(buffer)) > 0) {
                    os.write(buffer, 0, bytesLidos);
                }
                is.close();
                os.close();
            }
        } finally {
            zip.close();
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    /**
	 * This method provides an operation of compressing a file or directory 
	 * recursively into a specified file in standard Zip format.
	 * @param destinyZip the destiny file which will contain the compressed
	 * data.
	 * @param fileToZip the file to be compressed. It can also be a directory.
	 * @throws IOException if any I/O error occur during the compression.
	 * @throws ZipException if any compressing operation occur during compression.
	 */
    public void packIt(File destinyZip, File fileToZip) throws ZipException, IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destinyZip));
        String extractPath = fileToZip.getAbsoluteFile().getParent();
        addToZip(fileToZip, out, extractPath);
        out.close();
    }

    /**
	 * This method really adds the content of files into de zip expected file.
	 * @param fileToZip the file to be compressed.
	 * @param out the writer of the actual file into the specified result zip.
	 * @param extractPath the base directory of the files to be compressed. It
	 * is necessary to avoid full names of the files in the result zip file.
	 * @throws IOException if any I/O error occur during the compression.
	 */
    private void addToZip(File fileToZip, ZipOutputStream out, String extractPath) throws IOException {
        if (fileToZip.isDirectory()) {
            File[] files = fileToZip.listFiles();
            for (File toAdd : files) {
                addToZip(toAdd.getAbsoluteFile(), out, extractPath);
            }
        } else {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileToZip));
            String newPath = fileToZip.getAbsolutePath().substring(extractPath.length() + 1);
            out.putNextEntry(new ZipEntry(newPath));
            int len;
            byte[] buf = new byte[BUFFER_SIZE];
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

    /**
	 * Checks whether a file is under zip format.
	 * @param zipFileName the path to the file.
	 * @return <code>true</code> if this file is under zip format. 
	 * <code>false</code> otherwise.
	 * @throws IOException if any I/O error occur during the compression.
	 */
    public static boolean isZipFile(String zipFileName) throws IOException {
        try {
            File testFile = new File(zipFileName);
            ZipFile zipFile = new ZipFile(testFile);
        } catch (ZipException e) {
            return false;
        }
        return true;
    }
}
