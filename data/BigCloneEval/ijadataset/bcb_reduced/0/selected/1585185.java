package java.util.jar;

import java.util.zip.*;
import java.io.*;

/**
 * The <code>JarOutputStream</code> class is used to write the contents
 * of a JAR file to any output stream. It extends the class
 * <code>java.util.zip.ZipOutputStream</code> with support
 * for writing an optional <code>Manifest</code> entry. The
 * <code>Manifest</code> can be used to specify meta-information about
 * the JAR file and its entries.
 *
 * @author  David Connelly
 * @version 1.19, 02/02/00
 * @see	    Manifest
 * @see	    java.util.zip.ZipOutputStream
 * @since   1.2
 */
public class JarOutputStream extends ZipOutputStream {

    private static final int JAR_MAGIC = 0xCAFE;

    /**
     * Creates a new <code>JarOutputStream</code> with the specified
     * <code>Manifest</code>. The manifest is written as the first
     * entry to the output stream.
     *
     * @param out the actual output stream
     * @param man the optional <code>Manifest</code>
     * @exception IOException if an I/O error has occurred
     */
    public JarOutputStream(OutputStream out, Manifest man) throws IOException {
        super(out);
        if (man == null) {
            throw new NullPointerException("man");
        }
        ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(e);
        man.write(new BufferedOutputStream(this));
        closeEntry();
    }

    /**
     * Creates a new <code>JarOutputStream</code> with no manifest.
     * @param out the actual output stream
     * @exception IOException if an I/O error has occurred
     */
    public JarOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * Begins writing a new JAR file entry and positions the stream
     * to the start of the entry data. This method will also close
     * any previous entry. The default compression method will be
     * used if no compression method was specified for the entry.
     * The current time will be used if the entry has no set modification
     * time.
     *
     * @param ze the ZIP/JAR entry to be written
     * @exception ZipException if a ZIP error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void putNextEntry(ZipEntry ze) throws IOException {
        if (firstEntry) {
            byte[] edata = ze.getExtra();
            if (edata != null && !hasMagic(edata)) {
                byte[] tmp = new byte[edata.length + 4];
                System.arraycopy(tmp, 4, edata, 0, edata.length);
                edata = tmp;
            } else {
                edata = new byte[4];
            }
            set16(edata, 0, JAR_MAGIC);
            set16(edata, 2, 0);
            ze.setExtra(edata);
            firstEntry = false;
        }
        super.putNextEntry(ze);
    }

    private boolean firstEntry = true;

    private static boolean hasMagic(byte[] edata) {
        try {
            int i = 0;
            while (i < edata.length) {
                if (get16(edata, i) == JAR_MAGIC) {
                    return true;
                }
                i += get16(edata, i + 2) + 4;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return false;
    }

    private static int get16(byte[] b, int off) {
        return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
    }

    private static void set16(byte[] b, int off, int value) {
        b[off + 0] = (byte) value;
        b[off + 1] = (byte) (value >> 8);
    }
}
