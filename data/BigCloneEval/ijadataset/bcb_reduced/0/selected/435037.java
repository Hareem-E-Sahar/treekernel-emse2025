package at.jku.semwiq.mediator.util;

import java.io.File;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class Filesystem {

    /**
	 * deep-create directory
	 */
    public static void makeDirectory(File dir) {
        if (!dir.exists()) {
            File parent = dir.getParentFile();
            if (parent != null) makeDirectory(parent);
            dir.mkdir();
        }
    }

    /**
	 * deep-delete directory
	 * @param tdbLoc
	 */
    public static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else f.delete();
        }
        dir.delete();
    }
}
