package org.nomadpim.core.ui.startup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.runtime.Platform;

public class Backup {

    static void addFileToZip(ZipOutputStream zipout, File file) throws FileNotFoundException, IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream filein = null;
        try {
            filein = new FileInputStream(file);
            int s;
            while ((s = filein.read()) != -1) {
                bout.write(s);
            }
        } finally {
            filein.close();
        }
        zipout.putNextEntry(new ZipEntry(file.getName()));
        zipout.write(bout.toByteArray());
        zipout.flush();
    }

    public static String backup() throws IOException {
        File directory = new File(Platform.getLocation().append(".").toOSString());
        File[] files = directory.listFiles(new XMLFileNameFilter());
        if (files.length > 0) {
            File zipFile = new File(directory, "backup-" + System.currentTimeMillis() + ".zip");
            FileOutputStream fileout = new FileOutputStream(zipFile);
            ZipOutputStream zipout = new ZipOutputStream(fileout);
            for (File file : files) {
                addFileToZip(zipout, file);
            }
            zipout.close();
            return zipFile.getAbsolutePath();
        }
        return null;
    }
}
