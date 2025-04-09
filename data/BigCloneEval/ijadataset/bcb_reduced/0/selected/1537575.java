package mfb2.tools.obclipse.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mfb2.tools.obclipse.exceptions.ObclipseException;
import mfb2.tools.obclipse.util.Msg;

public class ZipCreator {

    /**
   * creates a zip file including all files and directories of the given directory<br>
   * the paths inside of the zip file are stored relative to the given directory
   * 
   * @param theArchiveName the path and name of the target zip file
   * @param theDirectory the directory to zip
   * @return
   */
    public static boolean createZipFile(String targetArchivePath, File sourceDirectory) throws ObclipseException {
        if (sourceDirectory.isDirectory()) {
            List<File> fileList = new ArrayList<File>();
            processFileList(sourceDirectory, fileList);
            return createZipFile(targetArchivePath, sourceDirectory, fileList);
        } else {
            Msg.error("Cannot zip directory ''{0}''! It's not a directory!", sourceDirectory);
            return false;
        }
    }

    /**
   * creates a zip file from the given file list
   * 
   * @param archivePath the path and name of the target zip file
   * @param baseDir the directory that defines the relative path of the files of the zip file
   * @param files the list of files to included in the zip file
   * @return true if zip file was successfully created
   */
    public static boolean createZipFile(String targetArchivePath, File baseDir, List<File> files) throws ObclipseException {
        boolean success = false;
        byte[] buf = new byte[2048];
        File archive = new File(targetArchivePath);
        if (archive.getParentFile() != null && !archive.getParentFile().exists()) {
            archive.getParentFile().mkdirs();
        }
        Msg.verbose("Creating zip file ''{0}''...", archive);
        if (!baseDir.exists()) {
            Msg.error("The given base directory ''{0}'' does not exist!", baseDir.getAbsolutePath());
            return false;
        }
        try {
            ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(targetArchivePath));
            for (File file : files) {
                if (!file.exists()) {
                    Msg.error("The given file ''{0}'' does not exist! File skipped!", file.getAbsolutePath());
                    continue;
                }
                FileInputStream in = new FileInputStream(file);
                String filePath = file.getAbsolutePath();
                filePath = filePath.replaceAll("\\\\", "/");
                String baseDirPath = baseDir.getAbsolutePath();
                baseDirPath = baseDirPath.replaceAll("\\\\", "/") + "/";
                String archiveFilePath = filePath.replaceAll(baseDirPath, "");
                outZip.putNextEntry(new ZipEntry(archiveFilePath));
                int len;
                while ((len = in.read(buf)) > 0) {
                    outZip.write(buf, 0, len);
                }
                outZip.closeEntry();
                in.close();
            }
            outZip.close();
            success = true;
            Msg.verbose("Zip file SUCCESSFULLY created.");
        } catch (IOException e) {
            Msg.ioException(archive, e);
        }
        return success;
    }

    /**
   * process recusive the subdirectories and put all the files in the fileList
   * 
   * @param fileList the list to store the files
   * @param directory the start directory
   */
    private static void processFileList(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                processFileList(files[i], fileList);
            } else {
                fileList.add(files[i]);
            }
        }
    }
}
