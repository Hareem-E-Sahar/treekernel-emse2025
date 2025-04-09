package archlib.ziplib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import archlib.AbstractArchiveOutputStream;
import archlib.ArchiveEntry;

/**
 * OutputStreams which handles Zip format. It encapsulates {@link ZipOutputStream}
 * @author Vaman Kulkarni
 */
public class ZipFileOutputStream extends AbstractArchiveOutputStream {

    private ZipOutputStream zos = null;

    public ZipFileOutputStream(OutputStream os) {
        zos = new ZipOutputStream(os);
    }

    @Override
    public void putNextEntry(ArchiveEntry entry) throws IOException {
        zos.putNextEntry(new ZipEntry(entry.getFileName()));
    }

    @Override
    public <X extends InputStream> X getNativeOutputStream(Class<X> streamClass) {
        return streamClass.cast(zos);
    }

    public void write(byte[] dataBuffer, int offset, int len) throws IOException {
        zos.write(dataBuffer, offset, len);
    }

    public void flush() throws IOException {
        zos.flush();
    }

    public void close() throws IOException {
        zos.close();
    }

    public void closeEntry() throws IOException {
        zos.closeEntry();
    }
}
