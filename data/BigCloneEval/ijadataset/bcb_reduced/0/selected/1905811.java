package edu.idp.shared.compress;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Kristopher T Babic
 */
public class ZipCompress extends Compress {

    public ZipCompress() {
        super();
    }

    public ZipCompress(String data) {
        super(data);
    }

    private String stored;

    public void compress() throws IOException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        String temp = getRaw();
        ZipOutputStream out = new ZipOutputStream(byteArray);
        ZipEntry entry = new ZipEntry("test");
        byte[] b = temp.getBytes();
        out.putNextEntry(entry);
        out.write(b, 0, temp.length());
        out.finish();
        setCompressed(byteArray.toString());
    }

    public void decompress() throws IOException {
        ByteArrayInputStream b2 = new ByteArrayInputStream(getRaw().getBytes());
        String s = new String();
        ZipInputStream in = new ZipInputStream(b2);
        ZipEntry z;
        while ((z = in.getNextEntry()) != null) {
            BufferedReader zin = new BufferedReader(new InputStreamReader(in));
            String t;
            while ((t = zin.readLine()) != null) s += t + '\n';
            in.closeEntry();
        }
        in.close();
        setDecompressed(s);
    }
}
