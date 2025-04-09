package org.vrforcad.controller.online;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class compress/decompress the cad file
 * before/after use database.
 * 
 * @version 1.2 
 * @author Daniel Cioi <dan.cioi@vrforcad.org>
 */
public class ZIPfile {

    final int BUFFER = 2048;

    private File file;

    /**
	 * Construct ZIP archive to decompress. 
	 * @param file the file.
	 */
    public ZIPfile(File file) {
        this.file = file;
    }

    /**
	 * Mehod to decompress the file and save it with the same name.  
	 */
    public void decompress() {
        decompress(null);
    }

    /**
	 * Method to decompress the specified archive file and give it a new name.
	 * If outputFile is null, then the zip file name will be used.
	 */
    public void decompress(File outputFile) {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream((outputFile == null) ? new File(entry.getName()) : outputFile);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Method to compress a file and save it with the name + zip extension.
	 */
    public void compress() {
        compress(null);
    }

    /**
	 * Method to compress the file before it will be sent to database.
	 */
    public void compress(File outputFile) {
        try {
            FileOutputStream dest = new FileOutputStream((outputFile == null) ? new File(file.toString() + ".zip") : outputFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            FileInputStream fi = new FileInputStream(file);
            BufferedInputStream origin = null;
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(file.toString());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
