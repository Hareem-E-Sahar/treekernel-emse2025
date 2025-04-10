package org.archive.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Utility methods for manipulating files and directories.
 *
 * @author John Erik Halse
 */
public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    public static final File TMPDIR = new File(System.getProperty("java.io.tmpdir", "/tmp"));

    private static final boolean DEFAULT_OVERWRITE = true;

    /**
     * Constructor made private because all methods of this class are static.
     */
    private FileUtils() {
        super();
    }

    public static int copyFiles(final File srcDir, Set srcFile, final File dest) throws IOException {
        int count = 0;
        for (Iterator i = srcFile.iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            File src = new File(srcDir, name);
            File tgt = new File(dest, name);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Before " + src.getAbsolutePath() + " " + src.exists() + ", " + tgt.getAbsolutePath() + " " + tgt.exists());
            }
            copyFiles(src, tgt);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("After " + src.getAbsolutePath() + " " + src.exists() + ", " + tgt.getAbsolutePath() + " " + tgt.exists());
            }
            count++;
        }
        return count;
    }

    /** Recursively copy all files from one directory to another.
     *
     * @param src file or directory to copy from.
     * @param dest file or directory to copy to.
     * @throws IOException
     */
    public static void copyFiles(File src, File dest) throws IOException {
        copyFiles(src, null, dest, false, true);
    }

    /**
     * @param src Directory of files to fetch.
     * @param filter Filter to apply to filenames.
     * @return Files in directory sorted.
     */
    public static String[] getSortedDirContent(final File src, final FilenameFilter filter) {
        if (!src.exists()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(src.getAbsolutePath() + " does not exist");
            }
            return null;
        }
        if (!src.isDirectory()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(src.getAbsolutePath() + " is not directory.");
            }
            return null;
        }
        String[] list = (filter == null) ? src.list() : src.list(filter);
        if (list != null) {
            Arrays.sort(list);
        }
        return list;
    }

    /**
     * Recursively copy all files from one directory to another.
     * 
     * @param src File or directory to copy from.
     * @param filter Filename filter to apply to src. May be null if no
     * filtering wanted.
     * @param dest File or directory to copy to.
     * @param inSortedOrder Copy in order of natural sort.
     * @param overwrite If target file already exits, and this parameter is
     * true, overwrite target file (We do this by first deleting the target
     * file before we begin the copy).
     * @throws IOException
     */
    public static void copyFiles(final File src, final FilenameFilter filter, final File dest, final boolean inSortedOrder, final boolean overwrite) throws IOException {
        if (!src.exists()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(src.getAbsolutePath() + " does not exist");
            }
            return;
        }
        if (src.isDirectory()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(src.getAbsolutePath() + " is a directory.");
            }
            if (!dest.exists()) {
                dest.mkdirs();
            }
            String list[] = (filter == null) ? src.list() : src.list(filter);
            if (inSortedOrder) {
                Arrays.sort(list);
            }
            for (int i = 0; i < list.length; i++) {
                copyFiles(new File(src, list[i]), filter, new File(dest, list[i]), inSortedOrder, overwrite);
            }
        } else {
            copyFile(src, dest, overwrite);
        }
    }

    /**
     * Copy the src file to the destination.
     * 
     * @param src
     * @param dest
     * @return True if the extent was greater than actual bytes copied.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean copyFile(final File src, final File dest) throws FileNotFoundException, IOException {
        return copyFile(src, dest, -1, DEFAULT_OVERWRITE);
    }

    /**
     * Copy the src file to the destination.
     * 
     * @param src
     * @param dest
     * @param overwrite If target file already exits, and this parameter is
     * true, overwrite target file (We do this by first deleting the target
     * file before we begin the copy).
     * @return True if the extent was greater than actual bytes copied.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean copyFile(final File src, final File dest, final boolean overwrite) throws FileNotFoundException, IOException {
        return copyFile(src, dest, -1, overwrite);
    }

    /**
     * Copy up to extent bytes of the source file to the destination
     *
     * @param src
     * @param dest
     * @param extent Maximum number of bytes to copy
     * @return True if the extent was greater than actual bytes copied.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean copyFile(final File src, final File dest, long extent) throws FileNotFoundException, IOException {
        return copyFile(src, dest, extent, DEFAULT_OVERWRITE);
    }

    /**
     * Copy up to extent bytes of the source file to the destination
     *
     * @param src
     * @param dest
     * @param extent Maximum number of bytes to copy
	 * @param overwrite If target file already exits, and this parameter is
     * true, overwrite target file (We do this by first deleting the target
     * file before we begin the copy).
	 * @return True if the extent was greater than actual bytes copied.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean copyFile(final File src, final File dest, long extent, final boolean overwrite) throws FileNotFoundException, IOException {
        boolean result = false;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Copying file " + src + " to " + dest + " extent " + extent + " exists " + dest.exists());
        }
        if (dest.exists()) {
            if (overwrite) {
                dest.delete();
                LOGGER.finer(dest.getAbsolutePath() + " removed before copy.");
            } else {
                return result;
            }
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel fcin = null;
        FileChannel fcout = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            fcin = fis.getChannel();
            fcout = fos.getChannel();
            if (extent < 0) {
                extent = fcin.size();
            }
            long trans = fcin.transferTo(0, extent, fcout);
            if (trans < extent) {
                result = false;
            }
            result = true;
        } catch (IOException e) {
            String message = "Copying " + src.getAbsolutePath() + " to " + dest.getAbsolutePath() + " with extent " + extent + " got IOE: " + e.getMessage();
            if (e.getMessage().equals("Invalid argument")) {
                LOGGER.severe("Failed copy, trying workaround: " + message);
                workaroundCopyFile(src, dest);
            } else {
                IOException newE = new IOException(message);
                newE.setStackTrace(e.getStackTrace());
                throw newE;
            }
        } finally {
            if (fcin != null) {
                fcin.close();
            }
            if (fcout != null) {
                fcout.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return result;
    }

    protected static void workaroundCopyFile(final File src, final File dest) throws IOException {
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(src);
            to = new FileOutputStream(dest);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Deletes all files and subdirectories under dir.
     * @param dir
     * @return true if all deletions were successful. If a deletion fails, the
     *          method stops attempting to delete and returns false.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Utility method to read an entire file as a String.
     *
     * @param file
     * @return File as String.
     * @throws IOException
     */
    public static String readFileAsString(File file) throws IOException {
        StringBuffer sb = new StringBuffer((int) file.length());
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }

    /**
     * Get a list of all files in directory that have passed prefix.
     *
     * @param dir Dir to look in.
     * @param prefix Basename of files to look for. Compare is case insensitive.
     *
     * @return List of files in dir that start w/ passed basename.
     */
    public static File[] getFilesWithPrefix(File dir, final String prefix) {
        FileFilter prefixFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().startsWith(prefix.toLowerCase());
            }
        };
        return dir.listFiles(prefixFilter);
    }

    /** Get a @link java.io.FileFilter that filters files based on a regular
     * expression.
     *
     * @param regexp the regular expression the files must match.
     * @return the newly created filter.
     */
    public static FileFilter getRegexpFileFilter(String regexp) {
        class RegexpFileFilter implements FileFilter {

            Pattern pattern;

            protected RegexpFileFilter(String re) {
                pattern = Pattern.compile(re);
            }

            public boolean accept(File pathname) {
                return pattern.matcher(pathname.getName()).matches();
            }
        }
        return new RegexpFileFilter(regexp);
    }

    /**
     * Use for case where files are being added to src.  Will break off copy
     * when tgt is same as src.
     * @param src Source directory to copy from.
     * @param tgt Target to copy to.
     * @param filter Filter to apply to files to copy.
     * @throws IOException
     */
    public static void syncDirectories(final File src, final FilenameFilter filter, final File tgt) throws IOException {
        Set<String> srcFilenames = null;
        do {
            srcFilenames = new HashSet<String>(Arrays.asList(src.list(filter)));
            List<String> tgtFilenames = Arrays.asList(tgt.list(filter));
            srcFilenames.removeAll(tgtFilenames);
            if (srcFilenames.size() > 0) {
                int count = FileUtils.copyFiles(src, srcFilenames, tgt);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Copied " + count);
                }
            }
        } while (srcFilenames != null && srcFilenames.size() > 0);
    }

    /**
     * Test file exists and is readable.
     * @param f File to test.
     * @exception IOException If file does not exist or is not unreadable.
     */
    public static File isReadable(final File f) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath() + " does not exist.");
        }
        if (!f.canRead()) {
            throw new FileNotFoundException(f.getAbsolutePath() + " is not readable.");
        }
        return f;
    }

    /**
     * @param f File to test.
     * @return True if file is readable, has uncompressed extension,
     * and magic string at file start.
     * @exception IOException If file does not exist or is not readable.
     */
    public static boolean isReadableWithExtensionAndMagic(final File f, final String uncompressedExtension, final String magic) throws IOException {
        boolean result = false;
        FileUtils.isReadable(f);
        if (f.getName().toLowerCase().endsWith(uncompressedExtension)) {
            FileInputStream fis = new FileInputStream(f);
            try {
                byte[] b = new byte[magic.length()];
                int read = fis.read(b, 0, magic.length());
                fis.close();
                if (read == magic.length()) {
                    StringBuffer beginStr = new StringBuffer(magic.length());
                    for (int i = 0; i < magic.length(); i++) {
                        beginStr.append((char) b[i]);
                    }
                    if (beginStr.toString().equalsIgnoreCase(magic)) {
                        result = true;
                    }
                }
            } finally {
                fis.close();
            }
        }
        return result;
    }

    /**
     * Turn path into a File, relative to context (which may be ignored 
     * if path is absolute). 
     * 
     * @param context File context if path is relative
     * @param path String path to make into a File
     * @return File created
     */
    public static File maybeRelative(File context, String path) {
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        }
        return new File(context, path);
    }
}
