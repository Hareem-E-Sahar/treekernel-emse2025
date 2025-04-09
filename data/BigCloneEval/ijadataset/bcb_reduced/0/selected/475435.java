package org.nightlabs.editor2d.iofilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.nightlabs.io.AbstractIOFilter;

public abstract class AbstractZipFilter extends AbstractIOFilter {

    public AbstractZipFilter() {
        super();
    }

    /**
	 * determines if the Stream should be zipped or not
	 */
    protected boolean useZip = true;

    public boolean isUseZip() {
        return useZip;
    }

    public void setUseZip(boolean useZip) {
        this.useZip = useZip;
    }

    /**
	 * the Compression Level of the ZipOutputStream
	 * valid values are 0-9 (0=no compression, 9=strongest compression)
	 */
    protected int compressLevel = 9;

    public int getCompressLevel() {
        return compressLevel;
    }

    public void setCompressLevel(int compressLevel) {
        if (compressLevel > 9) compressLevel = 9; else if (compressLevel < 0) compressLevel = 0;
        this.compressLevel = compressLevel;
    }

    /**
	 * @see org.nightlabs.io.IOFilter
	 */
    public Object read(InputStream in) throws IOException {
        if (isUseZip()) return readZip(in); else return readStream(in);
    }

    /**
	 * This Methods wraps the readStream(InputStream in) - Method into a ZipInpuStream
	 * 
	 * @param in The InpuStream to read from
	 * @return the Object returned from readStream(InputStream in)
	 */
    protected Object readZip(InputStream in) {
        try {
            ZipInputStream zipStream = new ZipInputStream(in);
            zipStream.getNextEntry();
            Object o = readStream(zipStream);
            zipStream.close();
            closeReadStream();
            return o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * Implement in this Method what would normally be implemented in IOFilter.read(InputStream in)
	 * 
	 * @param in The InpuStream to read from
	 * @return The Object to read
	 */
    protected abstract Object readStream(InputStream in);

    /**
	 * @see org.nightlabs.io.IOFilter
	 */
    public void write(Object o, OutputStream out) throws IOException {
        if (isUseZip()) writeZip(o, out, getEntryName()); else writeStream(o, out);
    }

    /**
	 * This Method wraps the content of the writeStream(Object o, OutputStream out)-Method into
	 * a ZipOutputStream with only one entry
	 * 
	 * @param o The Object to write
	 * @param out The OutputStream to which will be written
	 */
    protected void writeZip(Object o, OutputStream out, String entryName) {
        try {
            ZipOutputStream zipStream = new ZipOutputStream(out);
            zipStream.setLevel(compressLevel);
            ZipEntry entry = new ZipEntry(entryName);
            zipStream.putNextEntry(entry);
            writeStream(o, zipStream);
            zipStream.closeEntry();
            zipStream.finish();
            zipStream.close();
            closeWriteStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void closeReadStream();

    protected abstract void closeWriteStream();

    /**
	 * Implement in this Method what would normally be implemented in IOFilter.write(Object o, OutputStream out)
	 * 
	 * @param o the Object to write
	 * @param out the OutputStream to write to
	 */
    protected abstract void writeStream(Object o, OutputStream out);

    /**
	 * determines the name of the Entry in the ZipStream
	 */
    public abstract String getEntryName();
}
