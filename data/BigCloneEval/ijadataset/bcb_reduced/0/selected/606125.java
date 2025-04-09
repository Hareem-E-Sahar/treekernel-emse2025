package edu.clemson.cs.nestbed.common.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipUtils {

    private static final Log log = LogFactory.getLog(ZipUtils.class);

    private static final int BUFFER_SIZE = 4096;

    public static byte[] zipDirectory(File directory) throws IOException {
        byte[] data;
        log.debug("zipDirectory: " + directory.getName());
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
        ZipOutputStream zos = new ZipOutputStream(baos);
        zipDirectory(directory, directory.getName(), zos);
        data = baos.toByteArray();
        baos.close();
        return data;
    }

    public static void zipDirectory(File directory, File output) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
        zipDirectory(directory, directory.getName(), zos);
        zos.close();
    }

    public static File unzip(byte[] zipData, File directory) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
        ZipInputStream zis = new ZipInputStream(bais);
        ZipEntry entry = zis.getNextEntry();
        File root = null;
        while (entry != null) {
            if (entry.isDirectory()) {
                File f = new File(directory, entry.getName());
                f.mkdir();
                if (root == null) {
                    root = f;
                }
            } else {
                BufferedOutputStream out;
                out = new BufferedOutputStream(new FileOutputStream(new File(directory, entry.toString())), BUFFER_SIZE);
                for (int data = zis.read(); data != -1; data = zis.read()) {
                    out.write(data);
                }
                out.close();
            }
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        zis.close();
        return root;
    }

    private static void zipDirectory(File directory, String name, ZipOutputStream zos) throws IOException {
        name += "/";
        zos.putNextEntry(new ZipEntry(name));
        zos.closeEntry();
        String[] entryList = directory.list();
        for (int i = 0; i < entryList.length; ++i) {
            File f = new File(directory, entryList[i]);
            if (f.isDirectory()) {
                zipDirectory(f, name + f.getName(), zos);
            } else {
                FileInputStream fis = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(name + f.getName());
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesIn = 0;
                zos.putNextEntry(entry);
                while ((bytesIn = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesIn);
                }
                fis.close();
                zos.closeEntry();
            }
        }
    }
}
