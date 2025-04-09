package iwork.multibrowse;

import java.io.*;
import java.net.*;
import java.util.zip.*;

public class ZipArchiver {

    static final int BUFFER = 2048;

    public static File archiveFolder(File sourceFolder) throws Exception {
        String destName = sourceFolder.getName() + ".zip";
        File destFile = new File(sourceFolder.getParentFile(), destName);
        int i = 0;
        while (destFile.exists()) {
            i++;
            String newDestName = sourceFolder.getName() + i + ".zip";
            destFile = new File(sourceFolder.getParentFile(), newDestName);
        }
        return archiveFolder(sourceFolder, destFile);
    }

    public static File archiveFolder(File sourceFolder, File destFile) throws Exception {
        FileOutputStream dest = new FileOutputStream(destFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        addFolderToArchive("", sourceFolder, out);
        out.close();
        return destFile;
    }

    public static void addFolderToArchive(String prefix, File folder, ZipOutputStream out) throws Exception {
        BufferedInputStream origin = null;
        String files[] = folder.list();
        byte data[] = new byte[BUFFER];
        for (int i = 0; i < files.length; i++) {
            System.out.println("Adding: " + files[i]);
            System.out.println("With prefix: " + prefix + files[i]);
            File fileToAdd = new File(folder, files[i]);
            if (fileToAdd.isFile()) {
                FileInputStream fi = new FileInputStream(fileToAdd);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(prefix + files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            } else if (fileToAdd.isDirectory()) {
                ZipEntry entry = new ZipEntry(files[i] + "/");
                out.putNextEntry(entry);
                addFolderToArchive(prefix + files[i] + "/", fileToAdd, out);
            }
        }
    }
}
