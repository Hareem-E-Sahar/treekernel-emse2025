package com.sts.webmeet.server.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.sts.webmeet.common.IOUtil;

public class Zip {

    public static void unzip(String strPath, String strDestinationDir) throws IOException {
        System.out.println("unzip() path: " + strPath + " dest dir: " + strDestinationDir);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(strPath)));
        ZipEntry ze = null;
        while (null != (ze = zis.getNextEntry())) {
            if (ze.isDirectory()) {
            } else {
                File file = new File(strDestinationDir + File.separator + ze.getName());
                file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(file);
                IOUtil.copyStream(zis, fos);
            }
        }
    }

    public static void zipFile(String strFile, String strOut) throws IOException {
        File sourceFile = new File(strFile);
        String strSourceDir = sourceFile.getParent();
        File outFile = new File(strOut);
        String strOutDir = outFile.getParent();
        zipFile(strSourceDir, stripPath(sourceFile.getAbsolutePath()), strOutDir, stripPath(outFile.getAbsolutePath()));
    }

    private static String stripPath(String strPath) {
        String strRet = strPath;
        if (strPath.lastIndexOf("\\") > -1) {
            strRet = strPath.substring(strPath.lastIndexOf("\\"));
        } else if (strPath.lastIndexOf("/") > -1) {
            strRet = strPath.substring(strPath.lastIndexOf("/"));
        }
        return strRet;
    }

    public static void zipFile(String strBaseDir, String strInput, String strOutputDir, String strOutputFile) throws IOException {
        strBaseDir = sanitizeDirectoryPath(strBaseDir);
        strOutputDir = sanitizeDirectoryPath(strOutputDir);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(strOutputDir, strOutputFile)));
        ZipEntry ze = new ZipEntry(stripLeadingSlash(strInput));
        zos.putNextEntry(ze);
        File fileSource = new File(strBaseDir, strInput);
        DataInputStream dis = new DataInputStream(new FileInputStream(fileSource));
        IOUtil.copyStream(dis, zos);
        zos.close();
        dis.close();
    }

    private static String stripLeadingSlash(String str) {
        String strRet = str;
        while (strRet.startsWith("/") || strRet.startsWith("\\")) {
            strRet = strRet.substring(1);
        }
        return strRet;
    }

    public static void zipDirectory(String strDirectory, String strOuputDir, String strOutputFile) throws IOException {
        strDirectory = sanitizeDirectoryPath(strDirectory);
        File[] files = FileUtil.discoverFiles(new File(strDirectory));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(strOuputDir, strOutputFile)));
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
            writeEntry(zos, new ZipEntry(files[i].getPath()).toString(), strDirectory);
        }
        zos.close();
    }

    private static String sanitizeDirectoryPath(String strDir) {
        if (!(strDir.endsWith("\\") || strDir.endsWith("/"))) {
            return strDir += File.separator;
        } else {
            return strDir;
        }
    }

    private static void writeEntry(ZipOutputStream zos, String strFullPath, String strBaseDir) throws IOException {
        String strEntry = strFullPath.substring(strBaseDir.length());
        zos.putNextEntry(new ZipEntry(strEntry));
        FileInputStream fis = new FileInputStream(new File(strFullPath));
        IOUtil.copyStream(fis, zos);
        zos.closeEntry();
    }

    public static void main(String[] args) throws IOException {
        zipFile(args[0], args[1]);
    }
}
