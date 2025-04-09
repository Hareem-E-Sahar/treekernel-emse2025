package ru.mcfr.oxygen.updater.utils;

import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: ws
 * Date: 28.04.11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class Zipper {

    private static Logger logger = Logger.getLogger(Zipper.class);

    public static String unzip(File zipSrc, String destination) {
        try {
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(zipSrc);
            Enumeration e = zipfile.entries();
            (new File(destination)).mkdirs();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) (new File(destination + File.separator + entry.getName())).mkdirs(); else {
                    File entryFile = new File(destination + File.separator + entry.getName());
                    entryFile.getParentFile().mkdirs();
                    entryFile.createNewFile();
                    Streamer.bufferedStreamCopy(zipfile.getInputStream(entry), new FileOutputStream(entryFile));
                }
            }
            return zipfile.entries().nextElement().getName();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getLocalizedMessage());
            return "";
        }
    }

    private static void rsAddInZip(ZipOutputStream zos, File baseDir, File entryDir) {
        try {
            for (File entry : entryDir.listFiles()) {
                System.out.println(entry.getAbsolutePath());
                if (entry.isFile()) {
                    ZipEntry zipEntry = new ZipEntry(FileCommander.getRelativePath(baseDir, entry));
                    zipEntry.setComment("packed by me");
                    zos.putNextEntry(zipEntry);
                    Streamer.bufferedStreamCopy_noCloseOut(new FileInputStream(entry), zos);
                    zos.closeEntry();
                }
                rsAddInZip(zos, baseDir, entry);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public static boolean zip(File src, String destination) {
        try {
            File destFile = new File(destination);
            if (!destFile.exists()) {
                destFile.getParentFile().mkdirs();
                destFile.createNewFile();
            }
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile));
            rsAddInZip(zos, src.getParentFile(), src);
            zos.finish();
            zos.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }
}
