package uk.ac.ebi.intact.application.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides utility to build ZIP/GZIP files.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id: ZipBuilder.java 4575 2006-02-03 09:43:22Z skerrien $
 * @since <pre>01-Feb-2006</pre>
 */
public class ZipBuilder {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Build a GZIP file.
     *
     * @param gzipFile output file.
     * @param file     the file to gzip.
     */
    public static void createGZipFile(File gzipFile, File file) throws IOException {
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipFile));
        if (!file.canRead()) {
            System.err.println("ZipBuilder (GZip): Could not read " + file.getAbsolutePath());
            return;
        }
        String inFilename = "infilename";
        FileInputStream in = new FileInputStream(inFilename);
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.finish();
        out.close();
    }

    /**
     * Compress all given file into a ZIP file.
     *
     * @param zipFile      the output ZIP file.
     * @param includeFiles all File to be compressed
     */
    public static void createZipFile(File zipFile, Collection includeFiles) throws IOException {
        createZipFile(zipFile, includeFiles, false);
    }

    /**
     * Compress all given file into a ZIP file.
     *
     * @param zipFile      the output ZIP file.
     * @param includeFiles all File to be compressed
     * @param verbose      if true, display verbose output on System.out
     */
    public static void createZipFile(File zipFile, Collection includeFiles, final boolean verbose) throws IOException {
        if (verbose) {
            System.out.println("ZIP: " + zipFile.getAbsolutePath());
        }
        byte[] buf = new byte[BUFFER_SIZE];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        for (Iterator iterator = includeFiles.iterator(); iterator.hasNext(); ) {
            File entryFile = (File) iterator.next();
            if (verbose) {
                System.out.println("Adding: " + entryFile.getAbsolutePath());
            }
            if (!entryFile.canRead()) {
                System.err.println("ZipBuilder: Could not read " + entryFile.getAbsolutePath());
                continue;
            }
            FileInputStream in = new FileInputStream(entryFile);
            out.putNextEntry(new ZipEntry(entryFile.getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
    }
}
