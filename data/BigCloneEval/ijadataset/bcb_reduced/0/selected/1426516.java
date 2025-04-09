package org.jp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class KZipUtils {

    /**
	 * ファイルまたはディレクトリをzipに圧縮すること
	 * 
	 * @param fileOrDirectory
	 *            ファイルまたはディレクトリを圧縮しています
	 * @param zipFile圧縮したファイル
	 */
    public static void zip(String fileOrDirectory, String zipFile) {
        FileOutputStream fileOut;
        ZipOutputStream zipOut = null;
        try {
            fileOut = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(fileOut);
            zipOut.setEncoding("gbk");
            File file = new File(fileOrDirectory);
            if (file.isFile()) {
                zipFileOrDirectory(zipOut, file, "");
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        zipFileOrDirectory(zipOut, f, "");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
	 * zipファイルを指定したパスーに解凍すること
	 * @param unzipFile　解凍したzipファイル
	 * @param unFilePath　解凍したパスー
	 */
    @SuppressWarnings("unchecked")
    public static void unzip(String unzipFile, String unFilePath) {
        ZipFile zipFile;
        File file = null;
        try {
            File unFile = new File(unFilePath);
            if (!unFile.exists()) {
                unFile.mkdirs();
            }
            zipFile = new ZipFile(unzipFile);
            Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.getEntries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = enu.nextElement();
                String fileName = zipEntry.getName();
                file = new File(unFilePath + File.separator + fileName);
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                    continue;
                } else {
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                }
                InputStream in = zipFile.getInputStream(zipEntry);
                FileOutputStream out = new FileOutputStream(new File(unFilePath + File.separator + fileName));
                int bytes;
                byte[] b = new byte[1024];
                while ((bytes = in.read(b)) != -1) {
                    out.write(b, 0, bytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipFileOrDirectory(ZipOutputStream zipOut, File file, String curPath) {
        if (file == null || zipOut == null) {
            return;
        }
        String fileName = file.getName();
        FileInputStream in = null;
        try {
            if (file.isFile()) {
                ZipEntry zipEntry = new ZipEntry(curPath + fileName);
                zipOut.putNextEntry(zipEntry);
                in = new FileInputStream(file);
                int bytes;
                byte[] b = new byte[1024];
                while ((bytes = in.read(b)) != -1) {
                    zipOut.write(b, 0, bytes);
                }
                zipOut.closeEntry();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        zipFileOrDirectory(zipOut, f, curPath + file.getName() + "/");
                    }
                } else if (files != null && files.length == 0) {
                    ZipEntry zipEntry = new ZipEntry(curPath + fileName + "/");
                    zipOut.putNextEntry(zipEntry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
