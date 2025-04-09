package org.parallelj.tools.maven.ui.workarounds;

import java.io.File;
import org.eclipse.core.runtime.IPath;

/**
 * This class is a workaround to a maven-archetype-plugin bug.
 * 
 * Actually, it is not possible to generate empty folders from an Archetype. So,
 * this class parses the file contained under a container folder, and remove all
 * the Archetype ".touch" files.
 */
public class FilesRemover {

    private static final String TOUCH_FILE_NAME = ".touch";

    /**
	 * Remove the .touch file comin form the archetypes.
	 * 
	 * @param container
	 */
    public static void removeTouchFiles(File container) {
        if (container.exists() && container.isDirectory()) for (File file : container.listFiles()) {
            if (file.isDirectory()) {
                removeTouchFiles(file);
            } else if (file.isFile()) {
                if (file.getName().equals(TOUCH_FILE_NAME)) file.delete();
            }
        }
    }

    public static void removeTouchFiles(IPath path) {
        removeTouchFiles(new File(path.toOSString()));
    }
}
