package com.mkk.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 2011-9-27 20:46:14
 * <p>
 * IO util.
 * 
 * @author mkk
 */
public abstract class IO {

    /**
	 * Create a new File(or directory).<br>
	 * No matter how many parent File exist or not,<br>
	 * it will be created.
	 * 
	 * @param newFile
	 *            New file or directory
	 * @return True it is create successful,otherwise false
	 * @throws IOException
	 */
    public static boolean create(File newFile) throws IOException {
        if (newFile == null) {
            return false;
        }
        if (!newFile.exists()) {
            File parent = newFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (newFile.isFile()) {
                return newFile.createNewFile();
            } else {
                return newFile.mkdir();
            }
        }
        return true;
    }

    /**
	 * Remove File(or directory).<br>
	 * If <i>existFile</i> is a directory, delete the sub folder and file at
	 * first,<br>
	 * delete at last;<br>
	 * If <i>existFile</i> is a file,delete it directly.
	 * 
	 * @param existFile
	 *            Exist File(or directory)
	 * @return True it is create successful,otherwise false
	 */
    public static boolean remove(File existFile) {
        if (existFile == null) {
            return false;
        }
        if (!existFile.exists()) {
            return false;
        }
        if (existFile.isDirectory()) {
            File subFiles[] = existFile.listFiles();
            List<File> needDeleteFiles = new ArrayList<File>();
            for (File sub : subFiles) {
                if (sub.isDirectory()) {
                    needDeleteFiles.add(sub);
                    remove(sub);
                } else {
                    return sub.delete();
                }
            }
            if (!needDeleteFiles.isEmpty()) {
                int len = needDeleteFiles.size();
                for (int i = len - 1; i >= 0; i--) {
                    needDeleteFiles.get(i).delete();
                }
            }
        }
        return existFile.delete();
    }

    /**
	 * Copy file to another file or copy directory to another directory.
	 * 
	 * @param fromFile
	 *            From file or directory
	 * @param toFile
	 *            To file or directory
	 * @throws IOException
	 */
    public static boolean copy(File fromFile, File toFile) throws IOException {
        if (fromFile == null || toFile == null) {
            return false;
        }
        if (!fromFile.exists()) {
            return false;
        }
        if ((fromFile.isDirectory() && toFile.isFile()) || (fromFile.isFile() && toFile.isDirectory())) {
            return false;
        }
        create(toFile);
        if (fromFile.isDirectory()) {
            File subFiles[] = fromFile.listFiles();
            for (File sub : subFiles) {
                String name = sub.getName();
                File goalFile = new File(toFile, name);
                if (sub.isFile()) {
                    copyFile(sub, goalFile);
                } else {
                    copy(sub, goalFile);
                }
            }
        } else {
            copyFile(fromFile, toFile);
        }
        return true;
    }

    /**
	 * Copy file to file.
	 * 
	 * @param from
	 * @param to
	 * @throws IOException
	 */
    private static void copyFile(File from, File to) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(from));
            bos = new BufferedOutputStream(new FileOutputStream(to));
            byte bs[] = new byte[1024];
            int len;
            while ((len = bis.read(bs)) != -1) {
                bos.write(bs, 0, len);
            }
            bos.flush();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
}
