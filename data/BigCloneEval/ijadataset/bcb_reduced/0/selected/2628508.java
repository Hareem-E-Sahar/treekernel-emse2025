package de.derbsen.jkangoo;

import java.io.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: niels
 * Date: Oct 4, 2004
 * Time: 12:57:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class BackupManager {

    static FileOutputStream fos = null;

    static ZipOutputStream zos = null;

    public static void backup() {
        String dateName = dateFunc();
        try {
            fos = new FileOutputStream(Config.getBackupDir() + "/kangoobackup_" + dateName + ".zip");
            zos = new ZipOutputStream(fos);
            dirFunc(Config.getDataDir());
            zos.flush();
            zos.close();
            fos.close();
        } catch (IOException e) {
        }
    }

    private static void dirFunc(String dirName) {
        File dirObj = new File(dirName);
        if (dirObj.exists() == true) {
            if (dirObj.isDirectory() == true) {
                File[] fileList = dirObj.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isDirectory()) {
                        dirFunc(fileList[i].getPath());
                    } else if (fileList[i].isFile()) {
                        zipFunc(fileList[i].getPath());
                    }
                }
            } else {
                System.out.println(dirName + " is not a directory.");
            }
        } else {
            System.out.println("Directory " + dirName + " does not exist.");
        }
    }

    private static void zipFunc(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipEntry fileEntry = new ZipEntry(filePath.substring(Config.getDataDir().length() + 1));
            zos.putNextEntry(fileEntry);
            byte[] data = new byte[1024];
            int byteCount;
            while ((byteCount = bis.read(data, 0, 1024)) > -1) {
                zos.write(data, 0, byteCount);
            }
            bis.close();
            fis.close();
        } catch (IOException e) {
        }
        System.out.println(filePath);
    }

    private static String dateFunc() {
        Calendar calendar = Calendar.getInstance();
        String YY = (calendar.get(Calendar.YEAR) + "").substring(2);
        String MM = ((calendar.get(Calendar.MONTH) + 1) + "");
        String DD = (calendar.get(Calendar.DAY_OF_MONTH) + "");
        String HH = (calendar.get(Calendar.HOUR) + "");
        String MI = (calendar.get(Calendar.MINUTE) + "");
        int AMint = (calendar.get(Calendar.AM_PM));
        String AMStr;
        if (AMint == 0) {
            AMStr = "AM";
        } else {
            AMStr = "PM";
        }
        if (MM.length() == 1) {
            MM = "0" + MM;
        }
        if (DD.length() == 1) {
            DD = "0" + DD;
        }
        if (HH.length() == 1) {
            HH = "0" + HH;
        }
        if (MI.length() == 1) {
            MI = "0" + MI;
        }
        return (YY + "-" + MM + "-" + DD + "_" + HH + "-" + MI + "-" + AMStr);
    }
}
