package com.htdsoft.generic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Archive {

    static final int BUFFER = 4096;

    static byte data[] = new byte[BUFFER];

    /**
	 * Compresser une archive
	 * @throws IOException
	 *
	 */
    public static FileOutputStream Compress(Object[] files) throws IOException {
        FileOutputStream dest = new FileOutputStream("fichier.zip");
        CheckedOutputStream checksum = new CheckedOutputStream(dest, new CRC32());
        BufferedOutputStream buff = new BufferedOutputStream(checksum);
        ZipOutputStream out = new ZipOutputStream(buff);
        out.setMethod(ZipOutputStream.DEFLATED);
        out.setLevel(9);
        for (int i = 0; i < files.length; i++) {
            FileInputStream fi = new FileInputStream((File) files[i]);
            BufferedInputStream buffi = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(((File) files[i]).getName());
            out.putNextEntry(entry);
            int count;
            while ((count = buffi.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            out.closeEntry();
            buffi.close();
        }
        out.close();
        return dest;
    }

    /**
	 * D�compresser une archive zip
	 * @throws IOException
	 */
    public static void Decompress(File src, File dest) throws IOException {
        FileInputStream in = new FileInputStream(src);
        ZipInputStream zin = new ZipInputStream(in);
        int lu = -1;
        FileOutputStream fout = new FileOutputStream(dest);
        do {
            lu = zin.read(data);
            if (lu > 0) fout.write(data, 0, lu);
        } while (lu > 0);
        fout.flush();
        zin.closeEntry();
        zin.close();
        fout.close();
        in.close();
    }
}
