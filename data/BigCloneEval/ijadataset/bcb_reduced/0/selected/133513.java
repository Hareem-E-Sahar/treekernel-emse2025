package net.sf.lightbound.opencms.autointegration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipEntryWriter implements EntryWriter {

    private final ZipOutputStream os;

    public ZipEntryWriter(ZipOutputStream os) {
        this.os = os;
    }

    public OutputStream getEntryStream(String filename) throws IOException {
        os.putNextEntry(new ZipEntry(filename));
        return os;
    }

    public void closeEntryStream(OutputStream out) throws IOException {
        os.closeEntry();
    }
}
