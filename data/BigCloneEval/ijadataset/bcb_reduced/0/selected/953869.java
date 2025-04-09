package com.patientis.framework.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import com.patientis.framework.locale.SystemUtil;
import com.patientis.framework.logging.Log;

/**
 * One line class description
 *
 * 
 * <br/>  
 */
public class ZipUtil {

    private static int BUFFER = 4096;

    /**
	 * Zip the files
	 * 
	 * @param zipFileName
	 * @param files
	 * @throws Exception
	 */
    public static void zip(File zipFile, List<File> filesToZip) throws IOException {
        zip(zipFile, filesToZip, false, null);
    }

    /**
	 * Zip the files
	 * 
	 * @param zipFileName
	 * @param files
	 * @throws Exception
	 */
    public static void zip(File zipFile, List<File> filesToZip, boolean useAbsolutePath, String removePrefix) throws IOException {
        byte[] buffer = new byte[BUFFER];
        zipFile.createNewFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        out.setLevel(Deflater.DEFAULT_COMPRESSION);
        for (File file : filesToZip) {
            String name = file.getName();
            if (useAbsolutePath) {
                name = file.getAbsolutePath();
                if (removePrefix != null) {
                    name = name.replace(removePrefix, "");
                }
            }
            FileInputStream in = new FileInputStream(file);
            try {
                out.putNextEntry(new ZipEntry(name));
                System.out.println(name);
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
            } catch (Exception ex) {
                Log.exception(ex);
                try {
                    out.closeEntry();
                } catch (Exception ex2) {
                }
            } finally {
                in.close();
            }
        }
        out.close();
    }

    /**
	 * 
	 * @param sourceZipFile
	 * @param unzipDestinationDirectory
	 */
    public static List<File> unzip(File sourceZipFile, File unzipDestinationDirectory) throws Exception {
        ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        List<File> unzippedFiles = new ArrayList<File>();
        Enumeration zipFileEntries = zipFile.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(unzipDestinationDirectory, currentEntry);
            unzippedFiles.add(destFile);
            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                byte data[] = new byte[BUFFER];
                destFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        zipFile.close();
        return unzippedFiles;
    }

    /**
	 * 
	 * @param files
	 * @return
	 */
    public static List<File> getUniqueFiles(List<File> files) {
        return getUniqueFiles(files, false);
    }

    /**
	 * 
	 * @param files
	 * @return
	 */
    public static List<File> getUniqueFiles(List<File> files, boolean filesNotDirectories) {
        List<File> distinctFiles = new ArrayList<File>(files.size());
        for (File file : files) {
            boolean alreadyAdded = false;
            for (File f : distinctFiles) {
                if (file.getAbsolutePath().equals(f.getAbsolutePath())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                if (filesNotDirectories && file.isFile()) {
                    distinctFiles.add(file);
                } else {
                    distinctFiles.add(file);
                }
            }
        }
        return distinctFiles;
    }

    /**
	 * 
	 * @return
	 */
    public static File zipFiles(boolean errorIfMissing, String... paths) throws Exception {
        List<String> list = new ArrayList<String>();
        for (String p : paths) {
            list.add(p);
        }
        return zipFiles(errorIfMissing, list);
    }

    /**
	 * 
	 * @return
	 */
    public static File zipFiles(boolean errorIfMissing, List<String> paths) throws Exception {
        List<File> clientBinFiles = new ArrayList<File>();
        for (String path : paths) {
            if (errorIfMissing && (!new File(path).exists())) {
                throw new Exception("missing file:" + path);
            }
            clientBinFiles.add(new File(path));
        }
        File clientBinZip = SystemUtil.getTemporaryFile("zip");
        ZipUtil.zip(clientBinZip, clientBinFiles);
        return clientBinZip;
    }
}
