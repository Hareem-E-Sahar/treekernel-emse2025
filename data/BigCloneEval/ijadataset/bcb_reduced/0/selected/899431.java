package com.cntinker.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @autohr: bin_liu
 */
public class ZipHelper {

    private static final String BASE_DIR = "";

    private static final String PATH = "/";

    private static final int BUFFER = 1024;

    /**
     * ѹ��
     * 
     * @param srcFile
     *            Դ·��
     * @param destPath
     *            Ŀ��·��
     * @throws IOException
     * @throws Exception
     * @throws Exception
     */
    public static void compress(File srcFile, File destFile) throws IOException {
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(destFile), new CRC32());
        ZipOutputStream zos = new ZipOutputStream(cos);
        compress(srcFile, zos, BASE_DIR);
        zos.flush();
        zos.close();
    }

    /**
     * ѹ���ļ�
     * 
     * @param srcFile
     * @param destPath
     * @throws IOException
     * @throws Exception
     */
    public static void compress(File srcFile, String destPath) throws IOException {
        compress(srcFile, new File(destPath));
    }

    /**
     * ѹ��
     * 
     * @param srcFile
     *            Դ·��
     * @param zos
     *            ZipOutputStream
     * @param basePath
     *            ѹ���������·��
     * @throws IOException
     * @throws Exception
     */
    private static void compress(File srcFile, ZipOutputStream zos, String basePath) throws IOException {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    /**
     * �ļ�ѹ��
     * 
     * @param srcPath
     *            Դ�ļ�·��
     * @param destPath
     *            Ŀ���ļ�·��
     * @throws IOException
     */
    public static void compress(String srcPath, String destPath) throws IOException {
        File srcFile = new File(srcPath);
        compress(srcFile, destPath);
    }

    /**
     * ѹ��Ŀ¼
     * 
     * @param dir
     * @param zos
     * @param basePath
     * @throws IOException
     * @throws Exception
     */
    private static void compressDir(File dir, ZipOutputStream zos, String basePath) throws IOException {
        File[] files = dir.listFiles();
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            compress(file, zos, basePath + dir.getName() + PATH);
        }
    }

    /**
     * �ļ�ѹ��
     * 
     * @param file
     *            ��ѹ���ļ�
     * @param zos
     *            ZipOutputStream
     * @param dir
     *            ѹ���ļ��еĵ�ǰ·��
     * @throws IOException
     * @throws Exception
     */
    private static void compressFile(File file, ZipOutputStream zos, String dir) throws IOException {
        ZipEntry entry = new ZipEntry(dir + file.getName());
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = bis.read(data, 0, BUFFER)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();
        zos.closeEntry();
    }

    /**
     * ��ѹ�ļ���ָ��Ŀ¼��
     * 
     * @param zipFile
     * @param outPath
     * @throws IOException
     */
    public static void unCompress(String zipFile, String outPath) throws IOException {
        if (!outPath.substring(outPath.length() - 1, outPath.length()).equals("/")) outPath += "/";
        FileHelper.mkdir(outPath);
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            String outName = outPath + entry.getName();
            FileHelper.mkdir(getPath(outName));
            OutputStream out = new FileOutputStream(outName);
            byte[] buf = new byte[BUFFER];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        }
        in.close();
    }

    /**
     * �õ�һ��ֻ���ļ��е�·��
     * 
     * @param file
     * @return String
     */
    private static String getPath(String file) {
        return file.substring(0, file.lastIndexOf("/") + 1);
    }

    public static void main(String[] args) throws Exception {
        String input = "d:/temp/test";
        String output = "d:/temp/out.zip";
        unCompress(output, "d:/temp/source");
    }
}
