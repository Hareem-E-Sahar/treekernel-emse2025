package com.xinsdd.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZIPUtil {

    /** 解压文件夹加后缀 */
    public static String DIR_SUFFIX = "DIRYXL" + File.separator;

    private ZIPUtil() {
    }

    /**
	 * 创建压缩文件
	 * 
	 * @param filePath
	 *            文件目录
	 * @param zipFilePath
	 *            压缩后的文件目录
	 * @throws IOException
	 */
    public void createZipFile(String filePath, String zipFilePath) throws IOException {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        fos = new FileOutputStream(zipFilePath);
        zos = new ZipOutputStream(fos);
        writeZipFile(new File(filePath), zos, "");
        if (zos != null) zos.close();
        if (fos != null) fos.close();
    }

    /**
	 * 将文件写入压缩文件，并压缩
	 * 
	 * @param f
	 * @param zos
	 * @param hiberarchy
	 * @throws IOException
	 */
    private void writeZipFile(File f, ZipOutputStream zos, String hiberarchy) throws IOException {
        if (f.exists()) {
            if (f.isDirectory()) {
                hiberarchy += f.getName() + "/";
                File[] fif = f.listFiles();
                for (int i = 0; i < fif.length; i++) {
                    writeZipFile(fif[i], zos, hiberarchy);
                }
            } else {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                ZipEntry ze = new ZipEntry(hiberarchy + f.getName());
                zos.putNextEntry(ze);
                byte[] b = new byte[1024];
                while (fis.read(b) != -1) {
                    zos.write(b);
                    b = new byte[1024];
                }
                if (fis != null) fis.close();
            }
        }
    }

    private static ZIPUtil zu = null;

    public static ZIPUtil getInstance() {
        if (zu == null) zu = new ZIPUtil();
        return zu;
    }

    static Random rnd = new Random();

    public static String zipToTempFile(File file) {
        String rootPath = null;
        try {
            byte buffer[] = new byte[1024];
            rootPath = file.getParentFile().getAbsolutePath() + File.separator;
            rootPath = getUrl(rootPath);
            ZipFile zipFile = new ZipFile(file.getPath());
            Enumeration enumeration = zipFile.entries();
            ZipEntry zipEntry = null;
            InputStream inputStream = null;
            while (enumeration.hasMoreElements()) {
                zipEntry = (ZipEntry) enumeration.nextElement();
                if (zipEntry.isDirectory()) {
                    File f = new File(rootPath + zipEntry.getName() + "/");
                    f.mkdirs();
                    f.setWritable(false);
                    f.setReadable(false);
                    f.deleteOnExit();
                } else {
                    String fileName = rnd.nextInt(9999999) + ".tmp";
                    File tempDir = new File(rootPath + "/system/");
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                        new HiddenFile().hidden(tempDir.getPath());
                    }
                    File f = new File(rootPath + "/system/" + fileName);
                    f.deleteOnExit();
                    File parentDir = f.getParentFile();
                    if (!parentDir.exists()) parentDir.mkdirs();
                    OutputStream out = new FileOutputStream(f);
                    inputStream = zipFile.getInputStream(zipEntry);
                    int length = 0;
                    while ((length = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    out.close();
                    rootPath = f.getAbsolutePath();
                    System.out.println(rootPath);
                }
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootPath;
    }

    /**
	 * 转换成标准URL
	 */
    public static String getUrl(String path) {
        return getReplace(path, "\\", "/");
    }

    /**
	 * 强制替换
	 */
    public static String getReplace(String str, String oldStr, String newStr) {
        while (str.indexOf(oldStr) != -1) {
            str = str.substring(0, str.indexOf(oldStr)) + newStr + str.substring(str.indexOf(oldStr) + oldStr.length(), str.length());
        }
        return str;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("D:/temp/12.res");
        System.out.println(ZIPUtil.zipToTempFile(file));
    }
}
