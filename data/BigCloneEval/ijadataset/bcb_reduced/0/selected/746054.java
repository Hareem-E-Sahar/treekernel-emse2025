package uipp.ejb.support;

import java.io.File;

/**
 * Utils for file mnipulation
 * 
 * @author Jindrich Basek (basekjin@fel.cvut.cz, CTU Prague, FEE)
 */
public class FileUtils {

    /**
     * Deletes non empty directory
     * 
     * @param path directory for delete
     * @return true if deleted
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
