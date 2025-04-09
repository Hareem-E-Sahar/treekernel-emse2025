package org.apache.commons.codec.binary.yenc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class YEncEncoder {

    private YEncHeader header;

    private YEncTrailer trailer;

    private int line;

    private ByteArrayOutputStream baos;

    private BufferedInputStream input;

    public YEncEncoder(File input, int line) throws YEncException {
        baos = new ByteArrayOutputStream();
        try {
            this.input = new BufferedInputStream(new FileInputStream(input));
        } catch (FileNotFoundException e) {
            throw new YEncException(e);
        }
        this.line = line;
        header = new YEncHeader();
        header.setName(input.getName());
        header.setLine(line);
        trailer = new YEncTrailer();
    }

    public void encode(String path) throws YEncException {
        int c, w;
        long size = 0;
        CRC32 crc32 = new CRC32();
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path + File.separator + header.getName() + ".yenc")));
            while ((c = input.read()) != -1) {
                crc32.update(c);
                if (size % line == 0 && size != 0) {
                    baos.write((int) '\r');
                    baos.write((int) '\n');
                }
                w = (c + 42) % 256;
                if (w == 0x00 || w == 0x0A || w == 0x0D || w == 0x3D) {
                    baos.write((int) '=');
                    baos.write((w + 64) % 256);
                } else {
                    baos.write(w);
                }
                size++;
            }
            header.setSize(size);
            out.write(header.toString().getBytes());
            out.write((int) '\r');
            out.write((int) '\n');
            out.write(baos.toByteArray());
            out.write((int) '\r');
            out.write((int) '\n');
            trailer.setSize(size);
            trailer.setCrc32(Long.toHexString(crc32.getValue()).toUpperCase());
            out.write(trailer.toString().getBytes());
            out.write((int) '\r');
            out.write((int) '\n');
            baos.flush();
            baos.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new YEncException(e);
        }
    }
}
