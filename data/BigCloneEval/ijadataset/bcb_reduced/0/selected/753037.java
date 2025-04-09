package utils.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileManager {

    public static void copyFile(String inPath, String outPath) {
        try {
            File inFile = new File(inPath);
            File outFile = new File(outPath);
            InputStream in = new FileInputStream(inFile);
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException e) {
        }
    }

    public static void zipComponent(String inPath, String outPath) {
        try {
            File directory = new File(inPath);
            File[] files = directory.listFiles();
            byte[] buffer = new byte[1024];
            String outFilename = outPath + ".flw";
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < files.length; i++) {
                FileInputStream in = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(files[i].getName()));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unZipComponent(String inPath, String outPath) {
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(inPath);
            Enumeration<?> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.out.println("Extracting: " + entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[1024];
                FileOutputStream fos = new FileOutputStream(outPath + entry.getName());
                dest = new BufferedOutputStream(fos, 1024);
                while ((count = is.read(data, 0, 1024)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            for (File f : list) delete(f.getAbsolutePath());
            file.delete();
        } else file.delete();
    }
}
