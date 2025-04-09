package com.setec.commonutils;

import java.io.*;

public class FileUtils {

    public static void streamData(InputStream input, OutputStream output) throws IOException {
        InputStream inputBuffer = null;
        OutputStream outputBuffer = null;
        try {
            inputBuffer = new BufferedInputStream(input);
            outputBuffer = new BufferedOutputStream(output);
            while (true) {
                int data = inputBuffer.read();
                if (data == -1) {
                    break;
                }
                outputBuffer.write(data);
            }
        } finally {
            if (inputBuffer != null) {
                inputBuffer.close();
            }
            if (outputBuffer != null) {
                outputBuffer.close();
            }
        }
    }

    public static void writeToFile(String data, String fileName) throws IOException {
        File file = new File(fileName);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data.getBytes());
        stream.close();
    }

    public static String readFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1000);
        streamData(fileInputStream, byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

    public static void moveDirectory(String from, String to) throws IOException {
        File srcDir = new File(from);
        File destBaseDir = new File(to);
        File destDir = new File(to + System.getProperty("file.separator") + srcDir.getName());
        if (srcDir.exists() && srcDir.isDirectory()) {
            destBaseDir.mkdir();
            srcDir.renameTo(destDir);
        }
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void copyDirectory(File srcPath, File dstPath) throws IOException {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        } else {
            if (srcPath.exists()) {
                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
    }

    public static void prepareEmptyDir(String dirName) {
        deleteDirectory(new File(dirName));
        new File(dirName).mkdirs();
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }
}
