package com.showdown.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.showdown.log.ShowDownLog;

/**
 * Utility specializing in working with zip files
 * @author Mat DeLong
 */
public final class ZipUtil {

    /**
    * Zips the given input files into a zip file saved at the given output location.
    * If a zip file existed already at the given output location, it will be deleted.
    * @param output the zip file to output to
    * @param input array of files to zip
    * @return true if the zip passed, false if it failed
    */
    public static boolean zipFiles(File output, File... input) {
        if (output == null || input == null || input.length == 0) {
            return false;
        }
        if (!deleteFile(output)) {
            return false;
        }
        byte[] buf = new byte[1024];
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(output));
            for (int i = 0; i < input.length; i++) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(input[i]);
                    out.putNextEntry(new ZipEntry(input[i].getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                } finally {
                    FileUtil.close(in);
                }
            }
        } catch (IOException ex) {
            ShowDownLog.getInstance().logError(ex.getLocalizedMessage(), ex);
            return false;
        } finally {
            FileUtil.close(out);
        }
        return true;
    }

    /**
    * Unzips the specified zip file and puts the contents into the given directory.
    * If the directory doesn't exist, it is created. If the directory already exists,
    * and files with the same name as the ones in the zip file will be replaced.
    * @param zipFile the zip file to unzip
    * @param outputDir the directory to unzip into
    * @return true if unzip passed, false otherwise
    */
    public static boolean unzipFiles(File zipFile, File outputDir) {
        if (zipFile == null || !zipFile.exists() || zipFile.isDirectory() || !makeDir(outputDir)) {
            return false;
        }
        ZipInputStream in = null;
        try {
            BufferedOutputStream out = null;
            in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                try {
                    int count;
                    byte data[] = new byte[1000];
                    File outputFile = new File(outputDir, entry.getName());
                    out = new BufferedOutputStream(new FileOutputStream(outputFile), 1000);
                    while ((count = in.read(data, 0, 1000)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(in);
        }
        return true;
    }

    private static boolean makeDir(File dir) {
        if (dir == null || (dir.exists() && !dir.isDirectory())) {
            return false;
        }
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception ex) {
                ShowDownLog.getInstance().logError(ex.getLocalizedMessage(), ex);
                return false;
            }
        }
        return dir.exists();
    }

    private static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception ex) {
                ShowDownLog.getInstance().logError(ex.getLocalizedMessage(), ex);
            }
        }
        return !file.exists();
    }
}
