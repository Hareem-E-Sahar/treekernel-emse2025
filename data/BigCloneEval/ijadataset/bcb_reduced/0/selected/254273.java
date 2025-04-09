package org.neblipedia.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.neblipedia.archivos.LeedorFileChannel;

public class ZipMaker {

    private int BUFFER_SIZE = 2048;

    private ZipOutputStream out;

    public ZipMaker(File path) throws IOException {
        FileOutputStream dest = new FileOutputStream(path);
        out = new ZipOutputStream(new BufferedOutputStream(dest, 2048));
        out.setLevel(9);
    }

    public ZipMaker(String path) throws IOException {
        this(new File(path));
    }

    public void close() throws IOException {
        out.close();
    }

    public void putNextEntry(String nombre, File f) throws IOException {
        byte[] data = new byte[BUFFER_SIZE];
        LeedorFileChannel origin = new LeedorFileChannel(f);
        ZipEntry entry = new ZipEntry(nombre);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data)) != -1) {
            out.write(data, 0, count);
        }
        origin.close();
    }

    public void putNextEntry(String nombre, String datos) throws IOException, ZipException {
        byte[] data = datos.getBytes("UTF-8");
        this.putNextEntry(new ZipEntry(nombre.trim()), data);
    }

    public void putNextEntry(ZipEntry entry, byte[] data) throws IOException {
        out.putNextEntry(entry);
        out.write(data, 0, data.length);
        out.flush();
    }
}
