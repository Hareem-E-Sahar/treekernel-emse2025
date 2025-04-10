package welo.utility.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void jar(File folder, String outputname) {
        jar(folder, outputname, null);
    }

    public static void jar(File folder, String outputname, FileFilter filter) {
        File files[] = null;
        if (filter != null) {
            files = folder.listFiles(filter);
        } else {
            files = folder.listFiles();
        }
        byte[] buf = new byte[1024];
        try {
            String outFilename = outputname;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
            for (int i = 0; i < files.length; i++) {
                FileInputStream in = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(files[i].getCanonicalPath()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException e) {
        }
    }

    public static void unjar(File jarFile, boolean useprocess) {
        String jarCmd = "jar xf " + jarFile.getPath();
        if (useprocess == true) {
            Process pr = null;
            try {
                pr = Runtime.getRuntime().exec(jarCmd);
                pr.waitFor();
                pr.destroy();
            } catch (Throwable t) {
                System.out.println("Process exception: " + t.getMessage());
                if (pr != null) {
                    pr.destroy();
                    pr = null;
                }
            }
        } else {
            System.out.println("Jar not implemented yet with useprocess=false");
        }
    }
}
