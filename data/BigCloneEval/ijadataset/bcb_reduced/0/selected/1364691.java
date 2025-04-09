package cn.vlabs.clb.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class Compress {

    public Compress(OutputStream out) {
        zip = new ZipOutputStream(out);
        zip.setEncoding("GBK");
    }

    public void addFile(String fname, File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            zip.putNextEntry(new ZipEntry(fname));
            copy(in, zip);
            zip.closeEntry();
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void close() {
        try {
            if (zip != null) zip.close();
        } catch (IOException e) {
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[4096];
        int count;
        while ((count = in.read(buff)) != -1) {
            out.write(buff, 0, count);
        }
    }

    private ZipOutputStream zip;
}
