package cloudspace.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private ZipUtil() {
    }

    public static File zipFile(File toZip) throws IOException {
        File zipped = File.createTempFile("zippedItem", ".zip");
        zipped.deleteOnExit();
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipped));
        if (toZip.isDirectory()) {
            addDirectory(zipOut, toZip, true);
        } else {
            addFile(zipOut, toZip);
        }
        zipOut.close();
        return zipped;
    }

    private static void addDirectory(ZipOutputStream zipOut, File toZip, boolean isRoot) throws IOException {
        if (toZip.getName().length() > 0 && !toZip.getName().equals("/") && !isRoot) {
            ZipEntry entry = new ZipEntry(toZip.getName() + "/");
            zipOut.putNextEntry(entry);
            zipOut.closeEntry();
        }
        for (File internalDir : toZip.listFiles()) {
            if (internalDir.isFile()) addFile(zipOut, internalDir); else addDirectory(zipOut, internalDir, false);
        }
    }

    private static void addFile(ZipOutputStream zipOut, File toZip) throws IOException {
        FileInputStream toZipOut = new FileInputStream(toZip);
        ByteArrayOutputStream memStream = new ByteArrayOutputStream();
        copy(toZipOut, memStream);
        toZipOut.close();
        long length = memStream.size();
        ZipEntry entry = new ZipEntry(toZip.getName());
        entry.setSize(length);
        zipOut.putNextEntry(entry);
        ByteArrayInputStream memInputStream = new ByteArrayInputStream(memStream.toByteArray());
        copy(memInputStream, zipOut);
        zipOut.closeEntry();
    }

    /**
     * Copy the entire contents of an input stream to an output stream, using
     * the specified intermediate buffer.
     * 
     * @param inputStream
     *            the input stream to copy from
     * @param outputStream
     *            the output stream to copy to
     * @param buffer
     *            the buffer that will hold data while it is being copied
     * @throws IOException
     *             if an I/O error occurred
     */
    private static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[65536];
        int bytesRead = inputStream.read(buffer);
        while (bytesRead > 0) {
            outputStream.write(buffer, 0, bytesRead);
            bytesRead = inputStream.read(buffer);
        }
    }
}
