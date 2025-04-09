package com.narirelays.ems.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FileUtility {

    public static String readToString(File file, String encoding) {
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
        }
        try {
            if (encoding != null) {
                return new String(filecontent, encoding);
            } else {
                return new String(filecontent);
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readToByteArray(File file) throws IOException {
        assert file.isFile() && file.canRead();
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("The file is too big");
        }
        byte[] buffer = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        BufferedInputStream bis = null;
        try {
            FileInputStream is = new FileInputStream(file);
            bis = new BufferedInputStream(is);
            while (offset < buffer.length && (numRead = bis.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < buffer.length) {
                throw new IOException("The file was not completely read: " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }
        return buffer;
    }
}
