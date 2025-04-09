package todopad.controller;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

public class BackupData {

    public String backup() {
        try {
            File backupFolder = new File("backup");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            String backupTo = "backup/" + new SimpleDateFormat("yyyyMMdd_hh_mm_ss").format(new Date()) + "_data.zip";
            FileOutputStream lBackupStream = new FileOutputStream(backupTo);
            ZipOutputStream lZipOutputStream = new ZipOutputStream(lBackupStream);
            File dataFolder = new File(Constants.DATA_FOLDER);
            zipFolder(dataFolder, lZipOutputStream, dataFolder.getName());
            lZipOutputStream.close();
            lBackupStream.close();
            return backupTo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void zipFolder(File aDataFolder, ZipOutputStream aZipOutputStream, String aBasePath) {
        File[] lFiles = aDataFolder.listFiles();
        int lFilesCount = lFiles.length;
        for (int i = 0; i < lFilesCount; i++) {
            File lFile = lFiles[i];
            if (lFile.isDirectory()) {
                zipFolder(lFile, aZipOutputStream, aBasePath + "/" + lFile.getName());
            } else {
                try {
                    aZipOutputStream.putNextEntry(new ZipEntry(aBasePath + "/" + lFile.getName()));
                    byte[] lData = new byte[1024];
                    FileInputStream lFileInputStream = new FileInputStream(lFile);
                    int lReadLength = lFileInputStream.read(lData);
                    while (lReadLength > 0) {
                        aZipOutputStream.write(lData, 0, lReadLength);
                        lReadLength = lFileInputStream.read(lData);
                    }
                    lFileInputStream.close();
                    aZipOutputStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
