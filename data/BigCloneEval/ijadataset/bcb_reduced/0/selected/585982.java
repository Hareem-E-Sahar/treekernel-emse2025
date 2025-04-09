package com.adobe.dp.epub.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OCFContainerWriter extends ContainerWriter {

    ZipOutputStream zip;

    class CompressedEntryStream extends OutputStream {

        CompressedEntryStream() {
        }

        public void write(int b) throws IOException {
            zip.write(b);
        }

        public void close() throws IOException {
            zip.closeEntry();
        }

        public void flush() throws IOException {
            zip.flush();
        }

        public void write(byte[] arg0, int arg1, int arg2) throws IOException {
            zip.write(arg0, arg1, arg2);
        }
    }

    class StoredEntryStream extends OutputStream {

        String name;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        StoredEntryStream(String name) {
            this.name = name;
        }

        public void write(int b) throws IOException {
            buffer.write(b);
        }

        public void close() throws IOException {
            byte[] bytes = buffer.toByteArray();
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipOutputStream.STORED);
            entry.setSize(bytes.length);
            entry.setCompressedSize(bytes.length);
            CRC32 crc = new CRC32();
            crc.update(bytes);
            entry.setCrc(crc.getValue());
            zip.putNextEntry(entry);
            zip.write(bytes);
            zip.closeEntry();
        }

        public void flush() throws IOException {
        }

        public void write(byte[] buf, int arg1, int arg2) throws IOException {
            buffer.write(buf, arg1, arg2);
        }
    }

    public OCFContainerWriter(OutputStream out) throws IOException {
        this(out, "application/epub+zip");
    }

    public OCFContainerWriter(OutputStream out, String mime) throws IOException {
        zip = new ZipOutputStream(out);
        try {
            byte[] bytes = mime.getBytes("UTF-8");
            ZipEntry mimetype = new ZipEntry("mimetype");
            mimetype.setMethod(ZipOutputStream.STORED);
            mimetype.setSize(bytes.length);
            mimetype.setCompressedSize(bytes.length);
            CRC32 crc = new CRC32();
            crc.update(bytes);
            mimetype.setCrc(crc.getValue());
            zip.putNextEntry(mimetype);
            zip.write(bytes);
            zip.closeEntry();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public OutputStream getOutputStream(String name, boolean eligibleForCompression) throws IOException {
        if (eligibleForCompression) {
            zip.putNextEntry(new ZipEntry(name));
            return new CompressedEntryStream();
        } else {
            return new StoredEntryStream(name);
        }
    }

    public void close() throws IOException {
        zip.close();
    }
}
