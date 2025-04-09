package com.cp.vaultclipse.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import com.cp.vaultclipse.diff.FileComparator;
import com.cp.vaultclipse.utils.Logger;

/**
 * Helper functions for working with files.
 * 
 * @author daniel.klco
 * @version 20110420
 */
public class FileHelper {

    protected FilenameFilter filter;

    protected List<IgnorePattern> ignorePatterns = new ArrayList<IgnorePattern>();

    protected Logger log = new Logger(FileHelper.class.getName());

    /**
	 * Construct a new FileHelper.
	 * 
	 * @param ignorePattern
	 *            the ignore pattern for the file helper
	 */
    public FileHelper(String ignorePattern) {
        String[] ignorePatternStrings = ignorePattern.split(",");
        for (int i = 0; i < ignorePatternStrings.length; i++) {
            ignorePatterns.add(new IgnorePattern(ignorePatternStrings[i]));
        }
        filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                for (IgnorePattern pattern : ignorePatterns) {
                    if (pattern.matches(name)) {
                        log.debug("Skipping " + dir.getAbsolutePath() + " as it matches " + pattern.getPattern());
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it
	 * will be created.
	 * 
	 * @param srcDir
	 *            the directory to copy
	 * @param dstDir
	 *            the directory to copy the other directory to
	 * @throws IOException
	 *             a file IO exception occurs
	 */
    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        log.debug("copyDirectory");
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list(filter);
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
        if (srcDir.isDirectory()) {
            log.debug("Finding deleted files");
            File[] srcChildren = srcDir.listFiles();
            Set<String> srcFiles = new HashSet<String>();
            for (int i = 0; i < srcChildren.length; i++) {
                srcFiles.add(srcChildren[i].getName());
            }
            File[] dstChildren = dstDir.listFiles(filter);
            for (int i = 0; i < dstChildren.length; i++) {
                if (!srcFiles.contains(dstChildren[i].getName())) {
                    log.debug("Deleting file: " + dstChildren[i].getAbsolutePath());
                    deleteAll(dstChildren[i]);
                }
            }
        }
    }

    /**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it
	 * will be created.
	 * 
	 * @param srcDir
	 *            the directory to copy
	 * @param dstDir
	 *            the directory to copy the other directory to
	 * @param comparator
	 *            the class used to compare files
	 * @throws IOException
	 *             a file IO exception occurs
	 */
    public void copyDirectory(File srcDir, File dstDir, FileComparator comparator) throws IOException {
        log.debug("copyDirectory");
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list(filter);
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]), comparator);
            }
        } else {
            if (!comparator.areEqual(srcDir, dstDir)) {
                log.debug("Copying files");
                copyFile(srcDir, dstDir);
            } else {
                log.debug("Source and destination are equal");
            }
        }
        if (srcDir.isDirectory()) {
            log.debug("Finding deleted files");
            File[] srcChildren = srcDir.listFiles();
            Set<String> srcFiles = new HashSet<String>();
            for (int i = 0; i < srcChildren.length; i++) {
                srcFiles.add(srcChildren[i].getName());
            }
            File[] dstChildren = dstDir.listFiles(filter);
            for (int i = 0; i < dstChildren.length; i++) {
                if (!srcFiles.contains(dstChildren[i].getName())) {
                    log.debug("Deleting file: " + dstChildren[i].getAbsolutePath());
                    deleteAll(dstChildren[i]);
                }
            }
        }
    }

    /**
	 * Copies src file to dst file. If the dst file does not exist, it is
	 * created.
	 * 
	 * @param src
	 *            the file to copy
	 * @param dst
	 *            the file to copy the first file to
	 * @throws IOException
	 *             a file IO exception occurs
	 */
    public void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
	 * Delete a single file. All of the children of the file should already be
	 * deleted. Will attempt to delete the file three times. If the file is not
	 * deleted it will log a warning.
	 * 
	 * @param file
	 *            the file to delete
	 */
    public void delete(File file) {
        log.debug("delete");
        boolean isDeleted = file.delete();
        for (int i = 0; i < 3 && !isDeleted; i++) {
            isDeleted = file.delete();
        }
        if (!isDeleted) {
            log.warn("File " + file.getAbsolutePath() + " is not deleted");
        }
    }

    /**
	 * Delete all of the files under the specified file and the file itself.
	 * 
	 * @param file
	 *            the file to delete
	 */
    public void deleteAll(File file) {
        log.debug("deleteAll");
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteAll(files[i]);
            }
        }
        delete(file);
    }

    public boolean skip(String name) {
        for (IgnorePattern pattern : ignorePatterns) {
            if (pattern.matches(name)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Writes the input stream to a file.
	 * 
	 * @param resourceAsStream
	 *            the input stream to write
	 * @param file
	 *            the file to write the stream to
	 * @throws IOException
	 *             an exception occurs
	 */
    public void toFile(InputStream resourceAsStream, File file) throws IOException {
        log.debug("toFile");
        OutputStream os = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = resourceAsStream.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        os.close();
        resourceAsStream.close();
    }
}
