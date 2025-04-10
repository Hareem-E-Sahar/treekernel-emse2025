package mobac.utilities.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStoreOutputStream extends ZipOutputStream {

    private CRC32 crc = new CRC32();

    public ZipStoreOutputStream(OutputStream out) {
        super(out);
    }

    public ZipStoreOutputStream(File f) throws FileNotFoundException {
        super(new FileOutputStream(f));
    }

    /**
	 * 
	 * Warning this method is not thread safe!
	 * 
	 * @param name file name including path in the zip
	 * @param data
	 * @throws IOException
	 */
    public void writeStoredEntry(String name, byte[] data) throws IOException {
        ZipEntry ze = new ZipEntry(name);
        ze.setMethod(ZipEntry.STORED);
        ze.setCompressedSize(data.length);
        ze.setSize(data.length);
        crc.reset();
        crc.update(data);
        ze.setCrc(crc.getValue());
        putNextEntry(ze);
        write(data);
        closeEntry();
    }
}
