package org.rdv.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A helper threaded class to delete files and folders
 * 
 * @author Moji Soltani
 *
 */
public class DirectoryDeleter extends Thread {

    /** List of directories to delete */
    private ArrayList<File> dirList = new ArrayList<File>();

    public synchronized void add(File dir) {
        dirList.add(dir);
    }

    /** 
   * Thread runner to delete list of directories
   */
    public void run() {
        synchronized (this) {
            Iterator iterator = dirList.iterator();
            while (iterator.hasNext()) {
                File dir = (File) iterator.next();
                deleteDirectory(dir);
                iterator.remove();
            }
        }
    }

    /**
   * Iterates thru list of files in a folder and recursively
   * deletes all files and folders in the list 
   * 
   * @param dir File or Folder to delete
   */
    private void deleteDirectory(File dir) {
        File[] fileArray = dir.listFiles();
        if (fileArray != null) {
            for (int i = 0; i < fileArray.length; i++) {
                if (fileArray[i].isDirectory()) deleteDirectory(fileArray[i]); else fileArray[i].delete();
            }
        }
        dir.delete();
    }
}
