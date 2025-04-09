package net.sf.daileon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The <code>DirectoryDuplicator</code> class provides a single method, which
 * copies the content of a directory to another directory. This is a utility
 * class that is used before performing the actual transformations of the domain
 * annotations, since the original code is kept with the domain annotations, and
 * the actual domain annotations are only translated in the copied classes.
 * 
 * @author Roberto Perillo
 * @version 1.0 01/01/10
 */
class DirectoryDuplicator {

    /**
     * Copies the content of a directory to another location.
     * 
     * @param sourceLocation
     *            A {@link File} representation of the directory whose content
     *            will be copied.
     * @param targetLocation
     *            A <code>File</code> representation of the directory that will
     *            receive the content of the source directory.
     * @throws RuntimeException
     *             If any problem occurs when trying to create a directory with
     *             the same name as the source location in the target location,
     *             or if any {@link IOException} occurs.
     */
    static void copyDirectory(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                if (!targetLocation.mkdir()) {
                    String message = "It was not possible to create directory " + targetLocation.getAbsolutePath() + ". This application cannot continue.";
                    throw new RuntimeException(message);
                }
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            try {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException exception) {
                String message = "It was not possible to copy the source directory " + "to the chosen destiny due to the following cause: " + exception.getMessage();
                throw new RuntimeException(message, exception);
            }
        }
    }
}
