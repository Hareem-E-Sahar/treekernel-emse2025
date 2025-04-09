package com.jspx.io.zip;

import com.jspx.utils.ArrayUtil;
import com.jspx.utils.FileUtil;
import com.jspx.utils.StringUtil;
import java.io.*;
import java.util.zip.*;

public class Zip {

    static final int BUFFER = 2048;

    public Zip() {
    }

    public boolean doZipFile(String fileName, String outfile, String zipentry) {
        BufferedInputStream origin = null;
        try {
            if (!(new File(fileName)).isFile()) return false;
            FileOutputStream dest = new FileOutputStream(outfile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
            byte data[] = new byte[BUFFER];
            FileInputStream fi = new FileInputStream(fileName);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(FileUtil.getFileName(zipentry));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean doZipDir(String dirName, String type, boolean child, String outfile) {
        try {
            String fen = "@!#";
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(outfile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
            byte data[] = new byte[BUFFER];
            String file = FileUtil.getFileList(dirName, fen, type, child);
            if (StringUtil.isNULL(file)) return false;
            String files[] = StringUtil.split(file, fen);
            if (files == null || files.length < 1) return false;
            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String[] doUnZip(String fileName) {
        String[] result = null;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(fileName);
            CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                result = ArrayUtil.append(result, entry.toString());
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
