package org.fao.fenix.communication.compression;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.fao.fenix.communication.settings.ResourceSettings;

public class Zipper {

    public void zip(String resourceName) {
        String[] filenames = new String[2];
        if (new File(ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.DATASET_EXTENSION_1).exists()) filenames[0] = ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.DATASET_EXTENSION_1; else if (new File(ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.DATASET_EXTENSION_2).exists()) filenames[0] = ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.DATASET_EXTENSION_2;
        if (new File(ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.METADATA_EXTENSION_1).exists()) filenames[1] = ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.METADATA_EXTENSION_1; else if (new File(ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.METADATA_EXTENSION_2).exists()) filenames[1] = ResourceSettings.UPLOAD_PATH + resourceName + ResourceSettings.METADATA_EXTENSION_2;
        byte[] buf = new byte[1024];
        try {
            String outFilename = ResourceSettings.DOWNLOAD_PATH + resourceName + ResourceSettings.ARCHIVE_EXTENSION_1;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < filenames.length; i++) {
                File tmpFile = new File(filenames[i]);
                FileInputStream in = new FileInputStream(tmpFile);
                out.putNextEntry(new ZipEntry(tmpFile.getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException e) {
            System.out.println("IO EXCEPTION: " + e.getMessage());
        }
    }

    public List list(String resourceName) {
        ArrayList list = new ArrayList();
        try {
            ZipFile zipfile = new ZipFile(ResourceSettings.DOWNLOAD_PATH + resourceName + ResourceSettings.ARCHIVE_EXTENSION_1);
            for (Enumeration entries = zipfile.entries(); entries.hasMoreElements(); ) {
                list.add(((ZipEntry) entries.nextElement()).getName());
            }
        } catch (IOException e) {
            System.out.println("IO EXCEPTION: " + e.getMessage());
        }
        return list;
    }

    public void unzip(String resourceName) {
        try {
            String inFilename = ResourceSettings.DOWNLOAD_PATH + resourceName + ResourceSettings.ARCHIVE_EXTENSION_1;
            ZipInputStream in = new ZipInputStream(new FileInputStream(inFilename));
            List list = list(resourceName);
            for (int i = 0; i < list.size(); i++) {
                ZipEntry entry = in.getNextEntry();
                String outFilename = ResourceSettings.DOWNLOAD_PATH + (String) list.get(i);
                OutputStream out = new FileOutputStream(outFilename);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
            }
            in.close();
        } catch (IOException e) {
        }
    }
}
