package com.koozi.backup;

import com.koozi.Settings;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Sven-Ove Bjerkan
 */
public class Zip {

    private Settings settings;

    private String backupPath;

    private static final int BUFFER = 2048;

    public Zip() {
        settings = Settings.getInstance();
        backupPath = settings.getProjectPath() + "/Backup/";
        File f = new File(backupPath);
        if (!f.isDirectory()) f.mkdir();
    }

    public boolean compress(String filename, boolean incSubDirs) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(backupPath + filename);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            File f = new File(settings.getProjectPath());
            String files[] = f.list();
            if (files.length == 0) return false;
            for (int i = 0; i < files.length; i++) {
                if (new File(settings.getProjectPath() + "/" + files[i]).isDirectory() && !incSubDirs) {
                    continue;
                }
                FileInputStream fi = new FileInputStream(settings.getProjectPath() + "/" + files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            return true;
        } catch (ZipException ze) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
