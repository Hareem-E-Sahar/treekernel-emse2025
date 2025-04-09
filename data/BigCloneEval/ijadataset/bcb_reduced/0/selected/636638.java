package net.mjrz.fm.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import net.mjrz.fm.Main;

public class BackupProfileUtil {

    String profileName = null;

    File destinationDirectory = null;

    private static Logger logger = Logger.getLogger(BackupProfileUtil.class.getName());

    public BackupProfileUtil(String profileName, File destinationDir) {
        this.profileName = profileName;
        this.destinationDirectory = destinationDir;
    }

    public void createBackup() throws Exception {
        backupProfile();
    }

    private void backupProfile() throws Exception {
        String homedir = Profiles.getInstance().getPathForProfile(profileName);
        logger.info("Backing up profile : " + homedir + " to directory " + destinationDirectory);
        File src = new File(homedir.toString());
        if (!src.exists() || !src.isDirectory()) {
            return;
        }
        String zipFileName = destinationDirectory.getAbsoluteFile() + Main.PATH_SEPARATOR + profileName + ".zip";
        ArrayList<String> fileList = new ArrayList<String>();
        buildDirs(src, fileList);
        createZipFile(profileName, fileList, zipFileName);
    }

    private void createZipFile(String profileName, ArrayList<String> fileList, String zipFileName) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        FileInputStream in = null;
        try {
            for (String s : fileList) {
                byte[] tmpBuf = new byte[1024];
                in = new FileInputStream(s);
                String zipEntryName = cleanZipEntry(profileName, s);
                out.putNextEntry(new ZipEntry(zipEntryName));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            MiscUtils.stackTrace2String(e);
            throw e;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    private void buildDirs(File dirObj, ArrayList<String> fileList) throws IOException {
        File[] files = dirObj.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                buildDirs(files[i], fileList);
                continue;
            }
            fileList.add(files[i].getAbsolutePath());
        }
    }

    private String cleanZipEntry(String profileName, String zipEntryName) {
        int pos = zipEntryName.lastIndexOf(profileName);
        if (pos >= 0 && pos < zipEntryName.length()) {
            String replacement = zipEntryName.substring(pos);
            return replacement;
        }
        return null;
    }
}
