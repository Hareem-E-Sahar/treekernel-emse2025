package com.crowdsourcing.framework.util;

import java.io.*;
import java.util.Vector;
import java.util.zip.*;
import java.util.jar.*;

/**
 * INTERNAL:
 *
 * <b>Purpose</b>: Provide common file I/O utilites
 * @author Steven Vo
 * @since TopLink 4.5
 */
public class FileUtil {

    public static void copy(String inputPath, String outputPath, String[] filteredExtensions) throws IOException {
        File inputPathFile = new File(inputPath);
        if (!inputPathFile.exists()) {
            return;
        }
        File outputPathFile = new File(outputPath);
        if (!outputPathFile.exists()) {
            if (outputPathFile.isDirectory()) {
                outputPathFile.mkdirs();
            } else {
                new File(outputPathFile.getParent()).mkdirs();
            }
        }
        Vector files = findFiles(inputPath, filteredExtensions);
        for (int i = 0; i < files.size(); i++) {
            File in = (File) files.elementAt(i);
            String outFilePath = in.getAbsolutePath().substring(inputPath.length());
            outFilePath = outputPath + File.separator + outFilePath;
            File out = new File(outFilePath);
            File parent = new File(out.getParent());
            if (!parent.exists()) {
                parent.mkdirs();
            }
            copy(new FileInputStream(in), new FileOutputStream(out));
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[512];
        while (true) {
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {
                break;
            }
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
    }

    public static void createJarFromDirectory(String jarFileName, String jarDirectory, String[] filtertedExtensions) throws IOException {
        File directory = new File(jarDirectory);
        if (!directory.exists()) {
            return;
        }
        File jar = new File(jarFileName);
        if (!jar.exists()) {
            new File(jar.getParent()).mkdirs();
        }
        JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(jar), new Manifest());
        Vector files = findFiles(jarDirectory, filtertedExtensions);
        for (int i = 0; i < files.size(); i++) {
            File file = (File) files.elementAt(i);
            String relativePathToDirectory = file.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1);
            String entryName = relativePathToDirectory.replace('\\', '/');
            FileInputStream inStream = new FileInputStream(file);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = inStream.read(buffer)) > 0) {
                byteStream.write(buffer, 0, length);
            }
            byte[] arr = byteStream.toByteArray();
            byteStream.close();
            JarEntry meta = new JarEntry(entryName);
            jarOut.putNextEntry(meta);
            meta.setSize(arr.length);
            meta.setCompressedSize(arr.length);
            CRC32 crc = new CRC32();
            crc.update(arr);
            meta.setCrc(crc.getValue());
            meta.setMethod(ZipEntry.STORED);
            jarOut.write(arr, 0, arr.length);
            jarOut.closeEntry();
        }
        jarOut.close();
    }

    public static Vector findFiles(String path, String[] filteredExtensions) {
        Vector files = new Vector();
        findFilesHelper(new File(path), filteredExtensions, files);
        return files;
    }

    private static void findFilesHelper(File file, String[] filteredExtensions, Vector result) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            String[] entries = file.list();
            for (int i = 0; i < entries.length; i++) {
                findFilesHelper(new File(file, entries[i]), filteredExtensions, result);
            }
        } else {
            if ((filteredExtensions == null) || (filteredExtensions.length == 0)) {
                result.addElement(file);
                return;
            }
            for (int i = 0; i < filteredExtensions.length; i++) {
                if (file.getName().endsWith(filteredExtensions[i])) {
                    result.addElement(file);
                    return;
                }
            }
        }
    }

    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            String[] entries = file.list();
            if (entries.length == 0) {
                file.delete();
            } else {
                for (int i = 0; i < entries.length; i++) {
                    delete(new File(file, entries[i]));
                }
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    public static String trimFileName(String pFileName) {
        final char[] fileSeparators = new char[] { '\\', '/' };
        int lastSeparatorIndex = -1;
        for (int i = 0; i < fileSeparators.length; i++) {
            int index = pFileName.lastIndexOf(fileSeparators[i]);
            if (index > lastSeparatorIndex) {
                lastSeparatorIndex = index;
            }
        }
        return pFileName.substring(lastSeparatorIndex + 1);
    }
}
