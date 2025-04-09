package org.andnav2.osm.mtp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FolderZipper {

    public static void zipFolderToFile(final File pDestinationFile, final File pFolderToZip) {
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pDestinationFile));
            final int len = pDestinationFile.getAbsolutePath().lastIndexOf(File.separator);
            String baseName = pFolderToZip.getAbsolutePath().substring(0, len + 1);
            addFolderToZip(pFolderToZip, out, baseName);
            StreamUtils.closeStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException {
        final File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToZip(file, zip, baseName);
            } else {
                final String name = file.getAbsolutePath().substring(baseName.length());
                final ZipEntry zipEntry = new ZipEntry(name);
                zip.putNextEntry(zipEntry);
                final FileInputStream fileIn = new FileInputStream(file);
                StreamUtils.copy(fileIn, zip);
                StreamUtils.closeStream(fileIn);
                zip.closeEntry();
            }
        }
    }
}
