package org.shakra.common.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * 
 * @author Guillaume Aubert (guillaume.aubert@gmail.com)
 *
 */
public class FileSystem {

    /**
	 * Delete Root Directory and all its sibblings
	 * 
	 * @param path
	 *            Root Dir from where to delete
	 */
    public static void deleteDirs(File path) {
        try {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    deleteDirs(files[i]);
                }
                files[i].delete();
            }
            path.delete();
        } catch (Exception ignored) {
            ignored.printStackTrace(System.err);
        }
    }

    /**
	 * Delete Root Directory and all its sibblings
	 * 
	 * @param path
	 *            Root Dir name from where to delete
	 */
    public static void deleteDirs(String aPathName) {
        try {
            File path = new File(aPathName);
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    deleteDirs(files[i]);
                }
                files[i].delete();
            }
            path.delete();
        } catch (Exception ignored) {
            ignored.printStackTrace(System.err);
        }
    }

    /**
	 * CreateDir if necessary (error if dir cannot be created)
	 * @param aFile
	 * @throws IOException
	 */
    public static void createDirs(File aFile) throws IOException {
        if (aFile == null) throw new IllegalArgumentException("aFile null");
        if (!aFile.exists()) {
            aFile.mkdirs();
        } else {
            if (!aFile.isDirectory()) throw new IOException(aFile.getCanonicalPath() + " isn't a directory");
        }
    }

    /**
	 * CreateDir if necessary (error if dir cannot be created)
	 * @param aPathName
	 * @throws IOException
	 */
    public static void createDirs(String aPathName) throws IOException {
        File file = new File(aPathName);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (!file.isDirectory()) throw new IOException(aPathName + " isn't a directory");
        }
    }

    /**
	 * Return the files names (file or directory names) contained by the Directory
	 * @param _path
	 * @return Array of Files. null IOError or aPath isn't a directory. empty array if no files
	 */
    public static String[] listDirectoryAsString(String aPath) {
        if (aPath == null) return null;
        File dir = new File(aPath);
        return dir.list();
    }

    public static File[] listDirectory(String aPath, FileFilter aFileFilter) {
        if (aPath == null) return null;
        File dir = new File(aPath);
        if (dir == null) return null;
        return ((aFileFilter == null) ? dir.listFiles() : dir.listFiles(aFileFilter));
    }

    /**
	 * 
	 * @param _path
	 * @return Array of Files. null IOError or aPath isn't a directory. empty array if no files
	 */
    public static File[] listDirectory(String aPath) {
        return listDirectory(aPath, null);
    }

    public static File[] listDirectory(File aDir, FileFilter aFileFilter) {
        if ((aDir == null)) return null;
        return ((aFileFilter == null) ? aDir.listFiles() : aDir.listFiles(aFileFilter));
    }

    /**
	 * return the Directory files
	 * @param aDir  Directory to list
	 * @return Array of Files. null IOError or aPath isn't a directory. empty array if no files
	 */
    public static File[] listDirectory(File aDir) {
        return listDirectory(aDir, null);
    }
}
