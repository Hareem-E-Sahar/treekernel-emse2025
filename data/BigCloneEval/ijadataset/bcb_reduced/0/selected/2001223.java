package pipe4j.pipe.archive;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import pipe4j.pipe.SimpleStreamPipe;

/**
 * Zips stream into one entry.
 * 
 * @author bbennett
 */
public class ZipPipe extends SimpleStreamPipe {

    private final String entryName;

    private int level = Deflater.DEFAULT_COMPRESSION;

    public ZipPipe(String entryName) {
        super();
        this.entryName = entryName;
    }

    public ZipPipe(String entryName, int level) {
        super();
        this.entryName = entryName;
        this.level = level;
    }

    @Override
    protected void run(InputStream inputStream, OutputStream outputStream) throws Exception {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        zipOutputStream.setLevel(level);
        transfer(inputStream, zipOutputStream);
        zipOutputStream.closeEntry();
        zipOutputStream.finish();
    }
}
