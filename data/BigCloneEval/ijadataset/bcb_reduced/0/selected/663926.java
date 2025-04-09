package ca.ucalgary.cpsc.ebe.fitClipse.util;

import java.io.File;

/**
 * The Class FileUtils.
 */
public class FileUtils {

    /**
	 * Delete directory.
	 * 
	 * @param path
	 *            the path
	 * 
	 * @return true, if successful
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

    /**
	 * Gets the slash notation.
	 * 
	 * @param dotNotation
	 *            the dot notation
	 * 
	 * @return the slash notation
	 */
    public static String getSlashNotation(String dotNotation) {
        if (dotNotation.charAt(0) == '.') return "." + File.separatorChar + dotNotation.substring(1); else return dotNotation.replace('.', File.separator.charAt(0));
    }
}
