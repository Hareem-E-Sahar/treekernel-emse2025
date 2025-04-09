package org.gaea.common.util;

import java.io.File;
import java.util.Vector;

/**
 * Does all the job that java.io.File doesn't do.
 * 
 * @author bdevost
 */
public class FileSystemHelper {

    /**
	 * Operates a recursive delete on a folder.
	 * 
	 * @param dir
	 */
    public static void recursiveDelete(File dir) {
        if (dir.isDirectory()) {
            for (File subfiles : dir.listFiles()) {
                recursiveDelete(subfiles);
            }
        }
        dir.delete();
    }

    /**
	 * Lists all files recursively in a folder.
	 * 
	 * @param list
	 * @param folder
	 */
    public static void listFiles(Vector<File> list, File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                listFiles(list, file);
            } else {
                list.add(file);
            }
        }
    }
}
