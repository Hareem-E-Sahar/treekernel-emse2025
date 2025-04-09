package com.tresys.slide.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class CopyDir {

    /**
	 * Copies One Directory to another directory. If destination directory does
	 * not exist, creates one. No access checks are performed
	 * 
	 * @param Source directory of copy
	 * @param Destination to copy to
	 * @param file filter, can be null
	 * @throws IOException
	 */
    public static void copyDirectory(File srcDir, File dstDir, FilenameFilter filter) {
        if (srcDir.canRead() && srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                if (!dstDir.mkdir()) return;
            }
            File[] children = srcDir.listFiles(filter);
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) copyDirectory(children[i], new File(dstDir, children[i].getName()), filter); else copyFile(children[i], new File(dstDir, children[i].getName()));
            }
        }
    }

    /**
	 * Copies single file
	 * 
	 * @param source
	 *            file to copy
	 * @param destination
	 * @throws IOException
	 * @return none
	 */
    public static void copyFile(File src, File dest) {
        try {
            FileInputStream reader = new FileInputStream(src);
            FileOutputStream writer = new FileOutputStream(dest);
            int len;
            byte[] buf = new byte[(int) Math.min(10240, src.length())];
            while ((len = reader.read(buf)) > 0) {
                writer.write(buf, 0, len);
            }
            reader.close();
            writer.close();
        } catch (IOException ioe) {
        }
    }
}
