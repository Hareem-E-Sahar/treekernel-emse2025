package org.barred.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *  Description of the Class
 *
 */
public class ZipHandler {

    /**
     *  Helper method for replacing the file seperator
     *  char based on the OS
     *
     *@param  tsize  Description of the Parameter
     *@return        The block value
     */
    private static ZipOutputStream out = null;

    private static FileOutputStream fout = null;

    public static void initO(FileOutputStream _out) {
        fout = _out;
        out = new ZipOutputStream(fout);
    }

    public static void closeOut() {
        try {
            out.finish();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String fname, String entry) {
        byte[] buf = new byte[1024];
        try {
            FileInputStream in = new FileInputStream(entry);
            if (fname == null) {
                out.putNextEntry(new ZipEntry(entry));
            } else {
                out.putNextEntry(new ZipEntry(fname));
            }
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            out.closeEntry();
            in.close();
        } catch (Exception e) {
            System.out.println("Error while writing to ZIP Stream!" + e);
        }
    }

    public static void viewArc(String in) {
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(in);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                System.out.println(" " + entry.getName() + " [" + entry.getSize() + "] -> [" + entry.getCompressedSize() + "]");
            }
        } catch (Exception e) {
            System.out.println("Error while reading ZIP Stream!");
        }
    }

    public static void copyFile(String in, String ofile) {
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(in);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File art = new File(ofile, entry.getName());
                File parent = new File(art.getParent());
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(art);
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(fos));
            }
            zipFile.close();
        } catch (Exception e) {
            System.out.println("Error while reading ZIP Stream!");
        }
    }

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }
}
