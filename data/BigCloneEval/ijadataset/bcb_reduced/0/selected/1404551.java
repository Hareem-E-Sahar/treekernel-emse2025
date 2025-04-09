package net.sourceforge.purrpackage.reporting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.purrpackage.recording.Copier;

/**
 * Some utilities for managing files.
 */
public class FileHelper extends Copier {

    /**
     * A bit whacky. Looks for a file at path or a file with the defaultFileName
     * in the path directory. If found, copies to destdir under the
     * defaultFileName. otherwise does nothing.
     */
    public void copyFileOrFileInDirectoryIfExists(String path, String defaultFileName, File destdir) throws IOException {
        if (!isPathPossiblyValid(path)) {
            return;
        }
        File x = new File(path);
        if (x.exists()) {
            if (x.isDirectory()) {
                x = new File(x, defaultFileName);
            }
            if (x.exists()) {
                copyFile(x, new File(destdir, defaultFileName));
            }
        }
    }

    /** Trivial test of a proposed path */
    boolean isPathPossiblyValid(String path) {
        return path != null && path.trim().length() > 0;
    }

    /**
     * Recursively removes a directory or file. On failure, the exception names
     * the file that could not be removed.
     */
    public RmRecResult rmRec(File x) {
        return rmRec(x, new RmRecResult());
    }

    public RmRecResult rmRec(File x, RmRecResult result) {
        if (x.isDirectory()) {
            for (File f : x.listFiles()) {
                rmRec(f, result);
            }
        }
        if (!x.delete()) {
            result.failures.add(x);
        }
        return result;
    }

    static class RmRecResult {

        List<File> failures = new ArrayList<File>();

        List<File> getFailures() {
            return failures;
        }
    }
}
