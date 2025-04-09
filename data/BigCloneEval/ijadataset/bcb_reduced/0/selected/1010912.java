package net.sf.exorcist.core.zip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.exorcist.api.ContentException;
import net.sf.exorcist.api.ContentProcessor;
import net.sf.exorcist.api.ContentTree;

/**
 * Importer plugin for the Exorcist content state file format.
 * This plugin can be used to save content into an Exorcist
 * content state file.
 */
public class ZipImporter extends ZipBase implements ContentProcessor {

    /** Static logger instance. */
    private static final Logger logger = Logger.getLogger(ZipImporter.class.getName());

    /**
     * Writes the contents of the given {@link ContentState} instance
     * to the content state file.
     *
     * @param state the content state to be written to the state file
     * @throws ContentException if the content state file could not be written
     * @see ContentImporter#importContent(ContentState)
     */
    public void processContent(ContentTree tree) throws ContentException {
        try {
            logger.info("Writing content state file " + getZipfile());
            OutputStream output = new FileOutputStream(getZipfile());
            try {
                ZipOutputStream zip = zip = new ZipOutputStream(output);
                try {
                    logger.fine("Writing " + CONTENT_XML + " to " + getZipfile());
                    writeEntry(zip, CONTENT_XML, tree.getContentStream());
                    Iterator iterator = tree.getAttachmentHashes().iterator();
                    while (iterator.hasNext()) {
                        String hash = (String) iterator.next();
                        logger.fine("Writing attachment " + hash + " to " + getZipfile());
                        String part = hash.substring(0, 2) + "/" + hash.substring(2, 4);
                        writeEntry(zip, DATA + "/" + part + "/" + hash, tree.getAttachmentStream(hash));
                    }
                } finally {
                    zip.close();
                }
            } finally {
                logger.fine("Closing content state file " + getZipfile());
                output.close();
            }
        } catch (IOException e) {
            throw new ContentException(e);
        }
    }

    /**
     * Static utility method for writing a single entry to the given
     * zip output stream.
     * <p>
     * Note that the given input stream is fully consumed <em>and closed</em>
     * by this method.
     *
     * @param zip the zip output stream 
     * @param name name of the entry to be written
     * @param in contents of the entry to be written 
     * @throws IOException if the entry could not be written
     */
    private static void writeEntry(ZipOutputStream zip, String name, InputStream in) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        try {
            byte[] buffer = new byte[4096];
            for (int n = in.read(buffer); n != -1; n = in.read(buffer)) {
                zip.write(buffer, 0, n);
            }
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        zip.closeEntry();
    }
}
