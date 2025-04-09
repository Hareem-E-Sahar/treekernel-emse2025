package jgcp.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import jgcp.common.Task;
import jgcp.common.TaskDescription;

/**
 * 
 * @Date 28/05/2009
 * @author Jie Zhao (288654)
 * @version 1.0
 */
public class Packer {

    private Packer() {
    }

    private static void output(String filename, ZipOutputStream os) {
        if (filename != null && !filename.equals("")) {
            FileInputStream fis = null;
            try {
                File f = new File(filename);
                if (!f.exists()) return;
                os.putNextEntry(new ZipEntry(f.getName()));
                fis = new FileInputStream(filename);
                int t;
                while ((t = fis.read()) != -1) {
                    os.write(t);
                }
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static Checksum packTask(Task t, String targetFile) {
        ZipOutputStream out = null;
        CheckedOutputStream cos = null;
        try {
            File file = new File(targetFile);
            FileOutputStream fos = new FileOutputStream(file);
            cos = new CheckedOutputStream(fos, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(cos));
            TaskDescription td = t.getDescription();
            if (td != null) {
                out.putNextEntry(new ZipEntry("description.ini"));
                out.write(td.toString().getBytes());
                output(td.getExefile(), out);
                output(td.getDataFile(), out);
            }
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return cos.getChecksum();
    }

    ;

    public static void unpack(String packfile, String tp) {
        try {
            File dir = new File(tp);
            dir.mkdirs();
            ZipFile zip = new ZipFile(packfile);
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream is = zip.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(tp + entry.getName());
                int t;
                while ((t = is.read()) != -1) {
                    fos.write(t);
                }
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ;

    public static void main(String[] args) {
        Packer.unpack("d:/temp/worker/task4.zip", "d:/temp/worker/task0/");
    }
}
