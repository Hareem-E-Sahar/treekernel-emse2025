package api.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class Zip.java
 * @description Makes a zip archive of a folder
 * @author Sébastien Faure  <sebastien.faure3@gmail.com>
 * @version 2011-07-18
 */
public class Zip {

    private static int BUFFER = 1024;

    /**
     * Zips contents of a folder
     * @param folderSrc
     * @param archiveDest
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public static void zipFolder(String folderSrc, String archiveDest) throws FileNotFoundException, IOException, Exception {
        byte data[] = new byte[BUFFER];
        FileOutputStream dest = new FileOutputStream(archiveDest);
        BufferedOutputStream buff = new BufferedOutputStream(dest);
        ZipOutputStream out = new ZipOutputStream(buff);
        out.setMethod(ZipOutputStream.DEFLATED);
        out.setLevel(9);
        File f = new File(folderSrc);
        if (f.isDirectory()) {
            String files[] = f.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    FileInputStream fi;
                    if (api.utils.getOs.isWindows()) {
                        fi = new FileInputStream(folderSrc + "\\" + files[i]);
                    } else {
                        fi = new FileInputStream(folderSrc + "/" + files[i]);
                    }
                    BufferedInputStream buffi = new BufferedInputStream(fi, BUFFER);
                    if (api.utils.getOs.isWindows()) {
                        ZipEntry entry = new ZipEntry(folderSrc.substring(folderSrc.lastIndexOf("\\") + 1) + "\\" + files[i]);
                        out.putNextEntry(entry);
                    } else {
                        ZipEntry entry = new ZipEntry(folderSrc.substring(folderSrc.lastIndexOf("/") + 1) + "/" + files[i]);
                        out.putNextEntry(entry);
                    }
                    int count;
                    while ((count = buffi.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.closeEntry();
                    buffi.close();
                }
            }
        }
        out.flush();
        out.close();
    }
}
