package net.sourceforge.xuse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.sourceforge.xuse.log.Logger;

public class ZipUtils {

    private static final ZipUtils LOG_INSTANCE = new ZipUtils();

    private ZipUtils() {
    }

    public static final void unzip(ZipFile zipFile, File unzipDir) throws IOException {
        if (zipFile != null && unzipDir != null) {
            Logger.info(LOG_INSTANCE, "Extracting " + zipFile.getName() + " to " + unzipDir.getAbsolutePath());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File destination = new File(unzipDir, entry.getName());
                if (entry.isDirectory()) {
                    destination.mkdirs();
                } else if (!destination.getParentFile().exists()) {
                    destination.getParentFile().mkdirs();
                }
                Logger.debug(LOG_INSTANCE, "Extracting file: " + entry.getName());
                FileUtils.copy(zipFile.getInputStream(entry), destination);
            }
            zipFile.close();
        } else {
            throw new IOException("Please specify a valid zipfile (" + zipFile + ") and target directory (" + unzipDir + ")");
        }
    }

    public static final void zipDirectory(String zipFileName, File directory) throws IOException {
        zipDirectory(zipFileName, directory, directory);
    }

    public static final void zipDirectory(String zipFileName, File baseDir, File directory) throws IOException {
        if (directory == null || zipFileName == null) {
            throw new IllegalArgumentException("Directory or zip file name was null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }
        List<String> allDescendentFiles = FileUtils.getFilesInDir(directory, null);
        byte[] buffer = new byte[4096];
        int bytesRead;
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        for (String fileEntry : allDescendentFiles) {
            File f = new File(fileEntry);
            if (f.isDirectory()) {
                continue;
            }
            FileInputStream in = new FileInputStream(f);
            String localPath = baseDir != null ? f.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1) : f.getPath();
            Logger.debug(LOG_INSTANCE, "Basedir   : " + baseDir.getPath());
            Logger.debug(LOG_INSTANCE, "File path : " + f.getPath());
            Logger.debug(LOG_INSTANCE, "Local path: " + localPath);
            ZipEntry entry = new ZipEntry(localPath);
            out.putNextEntry(entry);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
        }
        out.close();
    }
}
