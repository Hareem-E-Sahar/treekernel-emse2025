package se.biobanksregistersyd.vienti.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A subclass of Compression used for ZIP compression.
 * 
 * @author ANDREAS
 * 
 */
public class CompressionZIP extends Compression {

    /**
     * Sets the default extension for this compression method.
     * 
     */
    public CompressionZIP() {
        setExtension("zip");
    }

    /**
     * Builds a filename from <code>filenameTemplate</code> and replaces
     * variable strings found inside it with values from the class. <p/>
     * Implementations in subclasses should call super.setFilename() last. <p/>
     * The following macro variables is handled in the template filename: <p/>
     * <dl>
     * <dt> ${extension}
     * <dd> The default extension for the compression method.
     * </dl>
     * 
     * @param filenameTemplate
     *            a <code>String</code> describes how the filename (and path)
     *            should look.
     * @return a <code>String</code> with the transformed filename.
     */
    public String transformFilename(final String filenameTemplate) {
        String tmpFilename = filenameTemplate;
        tmpFilename = tmpFilename.replaceAll("\\$\\{extension\\}", getExtension());
        return super.transformFilename(tmpFilename);
    }

    /**
     * Sets up the <code>ZipOutputStream</code>.
     * 
     * @param fileIn
     *            the <code>File</code> where the data is to be stored
     * @return OutputStream an compression <code>OutputStream</code>.
     * @throws IOException
     *             if something goes wrong when opening the stream.
     */
    public OutputStream getCompressionOutputStream(final File fileIn) throws IOException {
        setFileOutStream(new FileOutputStream(fileIn));
        ZipOutputStream zipOut = new ZipOutputStream(getFileOutStream());
        ZipEntry entry = new ZipEntry(fileIn.getName().replaceAll(getExtension() + "$", ""));
        zipOut.putNextEntry(entry);
        return zipOut;
    }

    /**
     * Sets up the <code>ZipInputStream</code>.
     * 
     * @param fileIn
     *            the <code>File</code> where the data is to be stored
     * @return InputStream the compression <code>InputStream</code>.
     * @throws IOException
     *             if something goes wrong when opening the stream.
     */
    public InputStream getCompressionInputStream(final File fileIn) throws IOException {
        ZipInputStream zIn = new ZipInputStream(new FileInputStream(fileIn));
        zIn.getNextEntry();
        return zIn;
    }
}
