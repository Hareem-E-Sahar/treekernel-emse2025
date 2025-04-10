package jacky.lanlan.song.io;

import jacky.lanlan.song.closure.Handler;
import jacky.lanlan.song.closure.ReturnableHandler;
import jacky.lanlan.song.resource.ResourceUtils;
import jacky.lanlan.song.util.Assert;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import org.apache.log4j.Logger;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 * 
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author Matthew Hawthorne
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @author Stephen Colebourne
 * @author Ian Springer
 * @author Chris Eldredge
 * @author Jim Harrington
 * @author Niall Pemberton
 * @author Sandy McArthur
 * @version $Id: FileUtils.java 507684 2007-02-14 20:38:25Z bayard $
 */
public abstract class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class);

    /**
   * The number of bytes in a kilobyte.
   */
    public static final long ONE_KB = 1024;

    /**
   * The number of bytes in a megabyte.
   */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
   * The number of bytes in a gigabyte.
   */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
   * An empty array of type <code>File</code>.
   */
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
   * Opens a {@link FileInputStream} for the specified file, providing better
   * error messages than simply calling <code>new FileInputStream(file)</code>.
   * <p>
   * At the end of the method either the stream will be successfully opened, or
   * an exception will have been thrown.
   * <p>
   * An exception is thrown if the file does not exist. An exception is thrown
   * if the file object exists but is a directory. An exception is thrown if the
   * file exists but cannot be read.
   * 
   * @param file
   *          the file to open for input, must not be <code>null</code>
   * @return a new {@link FileInputStream} for the specified file
   * @throws FileNotFoundException
   *           if the file does not exist
   * @throws IOException
   *           if the file object is a directory
   * @throws IOException
   *           if the file cannot be read
   * @since Commons IO 1.3
   */
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
   * Opens a {@link FileOutputStream} for the specified file, checking and
   * creating the parent directory if it does not exist.
   * <p>
   * At the end of the method either the stream will be successfully opened, or
   * an exception will have been thrown.
   * <p>
   * The parent directory will be created if it does not exist. The file will be
   * created if it does not exist. An exception is thrown if the file object
   * exists but is a directory. An exception is thrown if the file exists but
   * cannot be written to. An exception is thrown if the parent directory cannot
   * be created.
   * 
   * @param file
   *          the file to open for output, must not be <code>null</code>
   * @return a new {@link FileOutputStream} for the specified file
   * @throws IOException
   *           if the file object is a directory
   * @throws IOException
   *           if the file cannot be written to
   * @throws IOException
   *           if a parent directory needs creating but that fails
   * @since Commons IO 1.3
   */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.exists() == false) {
                if (parent.mkdirs() == false) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

    /**
   * Returns a human-readable version of the file size, where the input
   * represents a specific number of bytes.
   * 
   * @param size
   *          the number of bytes
   * @return a human-readable display value (includes units)
   */
    public static String byteCountToDisplaySize(long size) {
        String displaySize;
        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    /**
   * Implements the same behaviour as the "touch" utility on Unix. It creates a
   * new file with size 0 or, if the file exists already, it is opened and
   * closed without modifying it, but updating the file date and time.
   * <p>
   * NOTE: As from v1.3, this method throws an IOException if the last modified
   * date of the file cannot be set. Also, as from v1.3 this method creates
   * parent directories if they do not exist.
   * 
   * @param file
   *          the File to touch
   * @throws IOException
   *           If an I/O problem occurs
   */
    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            OutputStream out = openOutputStream(file);
            IOUtils.close(out);
        }
        boolean success = file.setLastModified(System.currentTimeMillis());
        if (!success) {
            throw new IOException("Unable to set the last modification time for " + file);
        }
    }

    /**
   * Converts a Collection containing java.io.File instanced into array
   * representation. This is to account for the difference between
   * File.listFiles() and FileUtils.listFiles().
   * 
   * @param files
   *          a Collection containing java.io.File instances
   * @return an array of java.io.File
   */
    public static File[] convertFileCollectionToFileArray(Collection<File> files) {
        return files.toArray(new File[files.size()]);
    }

    /**
   * Compare the contents of two files to determine if they are equal or not.
   * <p>
   * This method checks to see if the two files are different lengths or if they
   * point to the same file, before resorting to byte-by-byte comparison of the
   * contents.
   * <p>
   * Code origin: Avalon
   * 
   * @param file1
   *          the first file
   * @param file2
   *          the second file
   * @return true if the content of the files are equal or they both don't
   *         exist, false otherwise
   * @throws IOException
   *           in case of an I/O error
   */
    public static boolean contentEquals(File file1, File file2) throws IOException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }
        if (!file1Exists) {
            return true;
        }
        if (file1.isDirectory() || file2.isDirectory()) {
            throw new IOException("Can't compare directories, only files");
        }
        if (file1.length() != file2.length()) {
            return false;
        }
        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            return true;
        }
        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = new FileInputStream(file1);
            input2 = new FileInputStream(file2);
            return IOUtils.contentEquals(input1, input2);
        } finally {
            IOUtils.close(input1);
            IOUtils.close(input2);
        }
    }

    /**
   * Convert from a <code>URL</code> to a <code>File</code>.
   * <p>
   * From version 1.1 this method will decode the URL. Syntax such as
   * <code>file:///my%20docs/file.txt</code> will be correctly decoded to
   * <code>/my docs/file.txt</code>.
   * 
   * @param url
   *          the file URL to convert, <code>null</code> returns
   *          <code>null</code>
   * @return the equivalent <code>File</code> object, or <code>null</code>
   *         if the URL's protocol is not <code>file</code>
   * @throws IllegalArgumentException
   *           if the file is incorrectly encoded
   */
    public static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        }
        String filename = url.getFile().replace('/', File.separatorChar);
        int pos = 0;
        while ((pos = filename.indexOf('%', pos)) >= 0) {
            if (pos + 2 < filename.length()) {
                String hexStr = filename.substring(pos + 1, pos + 3);
                char ch = (char) Integer.parseInt(hexStr, 16);
                filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
            }
        }
        return new File(filename);
    }

    /**
   * Converts each of an array of <code>URL</code> to a <code>File</code>.
   * <p>
   * Returns an array of the same size as the input. If the input is
   * <code>null</code>, an empty array is returned. If the input contains
   * <code>null</code>, the output array contains <code>null</code> at the
   * same index.
   * <p>
   * This method will decode the URL. Syntax such as
   * <code>file:///my%20docs/file.txt</code> will be correctly decoded to
   * <code>/my docs/file.txt</code>.
   * 
   * @param urls
   *          the file URLs to convert, <code>null</code> returns empty array
   * @return a non-<code>null</code> array of Files matching the input, with
   *         a <code>null</code> item if there was a <code>null</code> at
   *         that index in the input array
   * @throws IllegalArgumentException
   *           if any file is not a URL file
   * @throws IllegalArgumentException
   *           if any file is incorrectly encoded
   * @since Commons IO 1.1
   */
    public static File[] toFiles(URL[] urls) {
        if (urls == null || urls.length == 0) {
            return EMPTY_FILE_ARRAY;
        }
        File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if (url != null) {
                if (url.getProtocol().equals("file") == false) {
                    throw new IllegalArgumentException("URL could not be converted to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    /**
   * Converts each of an array of <code>File</code> to a <code>URL</code>.
   * <p>
   * Returns an array of the same size as the input.
   * 
   * @param files
   *          the files to convert
   * @return an array of URLs matching the input
   * @throws IOException
   *           if a file cannot be converted
   */
    public static URL[] toURLs(File[] files) throws IOException {
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    /**
   * Copies a file to a directory preserving the file date.
   * <p>
   * This method copies the contents of the specified source file to a file of
   * the same name in the specified destination directory. The destination
   * directory is created if it does not exist. If the destination file exists,
   * then this method will overwrite it.
   * 
   * @param srcFile
   *          an existing file to copy, must not be <code>null</code>
   * @param destDir
   *          the directory to place the copy in, must not be <code>null</code>
   * 
   * @throws NullPointerException
   *           if source or destination is null
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @see #copyFile(File, File, boolean)
   */
    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    /**
   * Copies a file to a directory optionally preserving the file date.
   * <p>
   * This method copies the contents of the specified source file to a file of
   * the same name in the specified destination directory. The destination
   * directory is created if it does not exist. If the destination file exists,
   * then this method will overwrite it.
   * 
   * @param srcFile
   *          an existing file to copy, must not be <code>null</code>
   * @param destDir
   *          the directory to place the copy in, must not be <code>null</code>
   * @param preserveFileDate
   *          true if the file date of the copy should be the same as the
   *          original
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @see #copyFile(File, File, boolean)
   * @since Commons IO 1.3
   */
    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyFile(srcFile, new File(destDir, srcFile.getName()), preserveFileDate);
    }

    /**
   * Copies a file to a new location preserving the file date.
   * <p>
   * This method copies the contents of the specified source file to the
   * specified destination file. The directory holding the destination file is
   * created if it does not exist. If the destination file exists, then this
   * method will overwrite it.
   * 
   * @param srcFile
   *          an existing file to copy, must not be <code>null</code>
   * @param destFile
   *          the new file, must not be <code>null</code>
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @see #copyFileToDirectory(File, File)
   */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
   * Copies a file to a new location.
   * <p>
   * This method copies the contents of the specified source file to the
   * specified destination file. The directory holding the destination file is
   * created if it does not exist. If the destination file exists, then this
   * method will overwrite it.
   * 
   * @param srcFile
   *          an existing file to copy, must not be <code>null</code>
   * @param destFile
   *          the new file, must not be <code>null</code>
   * @param preserveFileDate
   *          true if the file date of the copy should be the same as the
   *          original
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @see #copyFileToDirectory(File, File, boolean)
   */
    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcFile.exists() == false) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        if (destFile.getParentFile() != null && destFile.getParentFile().exists() == false) {
            if (destFile.getParentFile().mkdirs() == false) {
                throw new IOException("Destination '" + destFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
   * Internal copy file method.
   * 
   * @param srcFile
   *          the validated source file, must not be <code>null</code>
   * @param destFile
   *          the validated destination file, must not be <code>null</code>
   * @param preserveFileDate
   *          whether to preserve the file date
   * @throws IOException
   *           if an error occurs
   */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.close(output);
            }
        } finally {
            IOUtils.close(input);
        }
        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
   * Copies a directory to within another directory preserving the file dates.
   * <p>
   * This method copies the source directory and all its contents to a directory
   * of the same name in the specified destination directory.
   * <p>
   * The destination directory is created if it does not exist. If the
   * destination directory did exist, then this method merges the source with
   * the destination, with the source taking precedence.
   * 
   * @param srcDir
   *          an existing directory to copy, must not be <code>null</code>
   * @param destDir
   *          the directory to place the copy in, must not be <code>null</code>
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @since Commons IO 1.2
   */
    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (srcDir.exists() && srcDir.isDirectory() == false) {
            throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
    }

    /**
   * Copies a whole directory to a new location preserving the file dates.
   * <p>
   * This method copies the specified directory and all its child directories
   * and files to the specified destination. The destination is the new location
   * and name of the directory.
   * <p>
   * The destination directory is created if it does not exist. If the
   * destination directory did exist, then this method merges the source with
   * the destination, with the source taking precedence.
   * 
   * @param srcDir
   *          an existing directory to copy, must not be <code>null</code>
   * @param destDir
   *          the new directory, must not be <code>null</code>
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @since Commons IO 1.1
   */
    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    /**
   * Copies a whole directory to a new location.
   * <p>
   * This method copies the contents of the specified source directory to within
   * the specified destination directory.
   * <p>
   * The destination directory is created if it does not exist. If the
   * destination directory did exist, then this method merges the source with
   * the destination, with the source taking precedence.
   * 
   * @param srcDir
   *          an existing directory to copy, must not be <code>null</code>
   * @param destDir
   *          the new directory, must not be <code>null</code>
   * @param preserveFileDate
   *          true if the file date of the copy should be the same as the
   *          original
   * 
   * @throws NullPointerException
   *           if source or destination is <code>null</code>
   * @throws IOException
   *           if source or destination is invalid
   * @throws IOException
   *           if an IO error occurs during copying
   * @since Commons IO 1.1
   */
    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcDir.exists() == false) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (srcDir.isDirectory() == false) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }
        doCopyDirectory(srcDir, destDir, preserveFileDate);
    }

    /**
   * Internal copy directory method.
   * 
   * @param srcDir
   *          the validated source directory, must not be <code>null</code>
   * @param destDir
   *          the validated destination directory, must not be <code>null</code>
   * @param preserveFileDate
   *          whether to preserve the file date
   * @throws IOException
   *           if an error occurs
   * @since Commons IO 1.1
   */
    private static void doCopyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (destDir.mkdirs() == false) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            if (preserveFileDate) {
                destDir.setLastModified(srcDir.lastModified());
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        File[] files = srcDir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (int i = 0; i < files.length; i++) {
            File copiedFile = new File(destDir, files[i].getName());
            if (files[i].isDirectory()) {
                doCopyDirectory(files[i], copiedFile, preserveFileDate);
            } else {
                doCopyFile(files[i], copiedFile, preserveFileDate);
            }
        }
    }

    /**
   * Copies bytes from the URL <code>source</code> to a file
   * <code>destination</code>. The directories up to <code>destination</code>
   * will be created if they don't already exist. <code>destination</code>
   * will be overwritten if it already exists.
   * 
   * @param source
   *          the <code>URL</code> to copy bytes from, must not be
   *          <code>null</code>
   * @param destination
   *          the non-directory <code>File</code> to write bytes to (possibly
   *          overwriting), must not be <code>null</code>
   * @throws IOException
   *           if <code>source</code> URL cannot be opened
   * @throws IOException
   *           if <code>destination</code> is a directory
   * @throws IOException
   *           if <code>destination</code> cannot be written
   * @throws IOException
   *           if <code>destination</code> needs creating but can't be
   * @throws IOException
   *           if an IO error occurs during copying
   */
    public static void copyURLToFile(URL source, File destination) throws IOException {
        InputStream input = source.openStream();
        try {
            FileOutputStream output = openOutputStream(destination);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.close(output);
            }
        } finally {
            IOUtils.close(input);
        }
    }

    /**
   * Recursively delete a directory.
   * 
   * @param directory
   *          directory to delete
   * @throws IOException
   *           in case deletion is unsuccessful
   */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        cleanDirectory(directory);
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
   * Clean a directory without deleting it.
   * 
   * @param directory
   *          directory to clean
   * @throws IOException
   *           in case cleaning is unsuccessful
   */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + directory);
        }
        IOException exception = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (null != exception) {
            throw exception;
        }
    }

    /**
   * Waits for NFS to propagate a file creation, imposing a timeout.
   * <p>
   * This method repeatedly tests {@link File#exists()} until it returns true up
   * to the maximum time specified in seconds.
   * 
   * @param file
   *          the file to check, must not be <code>null</code>
   * @param seconds
   *          the maximum time in seconds to wait
   * @return true if file exists
   * @throws NullPointerException
   *           if the file is <code>null</code>
   */
    public static boolean waitFor(File file, int seconds) {
        int timeout = 0;
        int tick = 0;
        while (!file.exists()) {
            if (tick++ >= 10) {
                tick = 0;
                if (timeout++ > seconds) {
                    return false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            } catch (Exception ex) {
                break;
            }
        }
        return true;
    }

    /**
   * Reads the contents of a file into a String. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @return the file contents, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   */
    public static String readFileToString(File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.toString(in, encoding);
        } finally {
            IOUtils.close(in);
        }
    }

    /**
   * Reads the contents of a file into a String using the default encoding for
   * the VM. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @return the file contents, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3.1
   */
    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, null);
    }

    /**
   * Reads the contents of a file into a byte array. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @return the file contents, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.1
   */
    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.toByteArray(in);
        } finally {
            IOUtils.close(in);
        }
    }

    /**
   * Reads the contents of a file line by line to a List of Strings. The file is
   * always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @return the list of Strings representing each line in the file, never
   *         <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   * @since Commons IO 1.1
   */
    public static List<String> readLines(File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.readLines(in, encoding);
        } finally {
            IOUtils.close(in);
        }
    }

    /**
   * Reads the contents of a file line by line to a List of Strings using the
   * default encoding for the VM. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @return the list of Strings representing each line in the file, never
   *         <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3
   */
    public static List<String> readLines(File file) throws IOException {
        return readLines(file, null);
    }

    /**
   * Writes a String to a file creating the file if it does not exist.
   * 
   * NOTE: As from v1.3, the parent directories of the file will be created if
   * they do not exist.
   * 
   * @param file
   *          the file to write
   * @param data
   *          the content to write to the file
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   */
    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file);
            IOUtils.write(data, out, encoding);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
   * Writes a String to a file creating the file if it does not exist using the
   * default encoding for the VM.
   * 
   * @param file
   *          the file to write
   * @param data
   *          the content to write to the file
   * @throws IOException
   *           in case of an I/O error
   */
    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, null);
    }

    /**
   * Writes a byte array to a file creating the file if it does not exist.
   * <p>
   * NOTE: As from v1.3, the parent directories of the file will be created if
   * they do not exist.
   * 
   * @param file
   *          the file to write to
   * @param data
   *          the content to write to the file
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.1
   */
    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file);
            out.write(data);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
   * Writes the <code>toString()</code> value of each item in a collection to
   * the specified <code>File</code> line by line. The specified character
   * encoding and the default line ending will be used.
   * <p>
   * NOTE: As from v1.3, the parent directories of the file will be created if
   * they do not exist.
   * 
   * @param file
   *          the file to write to
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @param lines
   *          the lines to write, <code>null</code> entries produce blank
   *          lines
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   * @since Commons IO 1.1
   */
    public static void writeLines(File file, String encoding, Collection<?> lines) throws IOException {
        writeLines(file, encoding, lines, null);
    }

    /**
   * Writes the <code>toString()</code> value of each item in a collection to
   * the specified <code>File</code> line by line. The default VM encoding and
   * the default line ending will be used.
   * 
   * @param file
   *          the file to write to
   * @param lines
   *          the lines to write, <code>null</code> entries produce blank
   *          lines
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3
   */
    public static void writeLines(File file, Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null);
    }

    /**
   * Writes the <code>toString()</code> value of each item in a collection to
   * the specified <code>File</code> line by line. The specified character
   * encoding and the line ending will be used.
   * <p>
   * NOTE: As from v1.3, the parent directories of the file will be created if
   * they do not exist.
   * 
   * @param file
   *          the file to write to
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @param lines
   *          the lines to write, <code>null</code> entries produce blank
   *          lines
   * @param lineEnding
   *          the line separator to use, <code>null</code> is system default
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   * @since Commons IO 1.1
   */
    public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file);
            IOUtils.writeLines(lines, lineEnding, out, encoding);
        } finally {
            IOUtils.close(out);
        }
    }

    /**
   * Writes the <code>toString()</code> value of each item in a collection to
   * the specified <code>File</code> line by line. The default VM encoding and
   * the specified line ending will be used.
   * 
   * @param file
   *          the file to write to
   * @param lines
   *          the lines to write, <code>null</code> entries produce blank
   *          lines
   * @param lineEnding
   *          the line separator to use, <code>null</code> is system default
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3
   */
    public static void writeLines(File file, Collection<?> lines, String lineEnding) throws IOException {
        writeLines(file, null, lines, lineEnding);
    }

    /**
   * Delete a file. If file is a directory, delete it and all sub-directories.
   * <p>
   * The difference between File.delete() and this method are:
   * <ul>
   * <li>A directory to be deleted does not have to be empty.</li>
   * <li>You get exceptions when a file or directory cannot be deleted.
   * (java.io.File methods returns a boolean)</li>
   * </ul>
   * 
   * @param file
   *          file or directory to delete, must not be <code>null</code>
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   * @throws IOException
   *           in case deletion is unsuccessful
   */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            if (!file.delete()) {
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
   * Schedule a file to be deleted when JVM exits. If file is directory delete
   * it and all sub-directories.
   * 
   * @param file
   *          file or directory to delete, must not be <code>null</code>
   * @throws NullPointerException
   *           if the file is <code>null</code>
   * @throws IOException
   *           in case deletion is unsuccessful
   */
    public static void forceDeleteOnExit(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    /**
   * Recursively schedule directory for deletion on JVM exit.
   * 
   * @param directory
   *          directory to delete, must not be <code>null</code>
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   * @throws IOException
   *           in case deletion is unsuccessful
   */
    private static void deleteDirectoryOnExit(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        cleanDirectoryOnExit(directory);
        directory.deleteOnExit();
    }

    /**
   * Clean a directory without deleting it.
   * 
   * @param directory
   *          directory to clean, must not be <code>null</code>
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   * @throws IOException
   *           in case cleaning is unsuccessful
   */
    private static void cleanDirectoryOnExit(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of " + directory);
        }
        IOException exception = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (null != exception) {
            throw exception;
        }
    }

    /**
   * Make a directory, including any necessary but nonexistent parent
   * directories. If there already exists a file with specified name or the
   * directory cannot be created then an exception is thrown.
   * 
   * @param directory
   *          directory to create, must not be <code>null</code>
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   * @throws IOException
   *           if the directory cannot be created
   */
    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (directory.isFile()) {
                String message = "File " + directory + " exists and is " + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                String message = "Unable to create directory " + directory;
                throw new IOException(message);
            }
        }
    }

    /**
   * Recursively count size of a directory (sum of the length of all files).
   * 
   * @param directory
   *          directory to inspect, must not be <code>null</code>
   * @return size of directory in bytes, 0 if directory is security restricted
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   */
    public static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }
        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }
        long size = 0;
        File[] files = directory.listFiles();
        if (files == null) {
            return 0L;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }

    /**
   * Tests if the specified <code>File</code> is newer than the reference
   * <code>File</code>.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param reference
   *          the <code>File</code> of which the modification date is used,
   *          must not be <code>null</code>
   * @return true if the <code>File</code> exists and has been modified more
   *         recently than the reference <code>File</code>
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   * @throws IllegalArgumentException
   *           if the reference file is <code>null</code> or doesn't exist
   */
    public static boolean isFileNewer(File file, File reference) {
        if (reference == null) {
            throw new IllegalArgumentException("No specified reference file");
        }
        if (!reference.exists()) {
            throw new IllegalArgumentException("The reference file '" + file + "' doesn't exist");
        }
        return isFileNewer(file, reference.lastModified());
    }

    /**
   * Tests if the specified <code>File</code> is newer than the specified
   * <code>Date</code>.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param date
   *          the date reference, must not be <code>null</code>
   * @return true if the <code>File</code> exists and has been modified after
   *         the given <code>Date</code>.
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   * @throws IllegalArgumentException
   *           if the date is <code>null</code>
   */
    public static boolean isFileNewer(File file, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("No specified date");
        }
        return isFileNewer(file, date.getTime());
    }

    /**
   * Tests if the specified <code>File</code> is newer than the specified time
   * reference.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param timeMillis
   *          the time reference measured in milliseconds since the epoch
   *          (00:00:00 GMT, January 1, 1970)
   * @return true if the <code>File</code> exists and has been modified after
   *         the given time reference.
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   */
    public static boolean isFileNewer(File file, long timeMillis) {
        if (file == null) {
            throw new IllegalArgumentException("No specified file");
        }
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() > timeMillis;
    }

    /**
   * Tests if the specified <code>File</code> is older than the reference
   * <code>File</code>.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param reference
   *          the <code>File</code> of which the modification date is used,
   *          must not be <code>null</code>
   * @return true if the <code>File</code> exists and has been modified before
   *         the reference <code>File</code>
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   * @throws IllegalArgumentException
   *           if the reference file is <code>null</code> or doesn't exist
   */
    public static boolean isFileOlder(File file, File reference) {
        if (reference == null) {
            throw new IllegalArgumentException("No specified reference file");
        }
        if (!reference.exists()) {
            throw new IllegalArgumentException("The reference file '" + file + "' doesn't exist");
        }
        return isFileOlder(file, reference.lastModified());
    }

    /**
   * Tests if the specified <code>File</code> is older than the specified
   * <code>Date</code>.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param date
   *          the date reference, must not be <code>null</code>
   * @return true if the <code>File</code> exists and has been modified before
   *         the given <code>Date</code>.
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   * @throws IllegalArgumentException
   *           if the date is <code>null</code>
   */
    public static boolean isFileOlder(File file, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("No specified date");
        }
        return isFileOlder(file, date.getTime());
    }

    /**
   * Tests if the specified <code>File</code> is older than the specified time
   * reference.
   * 
   * @param file
   *          the <code>File</code> of which the modification date must be
   *          compared, must not be <code>null</code>
   * @param timeMillis
   *          the time reference measured in milliseconds since the epoch
   *          (00:00:00 GMT, January 1, 1970)
   * @return true if the <code>File</code> exists and has been modified before
   *         the given time reference.
   * @throws IllegalArgumentException
   *           if the file is <code>null</code>
   */
    public static boolean isFileOlder(File file, long timeMillis) {
        if (file == null) {
            throw new IllegalArgumentException("No specified file");
        }
        if (!file.exists()) {
            return false;
        }
        return file.lastModified() < timeMillis;
    }

    /**
   * Computes the checksum of a file using the CRC32 checksum routine. The value
   * of the checksum is returned.
   * 
   * @param file
   *          the file to checksum, must not be <code>null</code>
   * @return the checksum value
   * @throws NullPointerException
   *           if the file or checksum is <code>null</code>
   * @throws IllegalArgumentException
   *           if the file is a directory
   * @throws IOException
   *           if an IO error occurs reading the file
   * @since Commons IO 1.3
   */
    public static long checksumCRC32(File file) throws IOException {
        CRC32 crc = new CRC32();
        checksum(file, crc);
        return crc.getValue();
    }

    /**
   * Computes the checksum of a file using the specified checksum object.
   * Multiple files may be checked using one <code>Checksum</code> instance if
   * desired simply by reusing the same checksum object. For example:
   * 
   * <pre>
   * long csum = FileUtils.checksum(file, new CRC32()).getValue();
   * </pre>
   * 
   * @param file
   *          the file to checksum, must not be <code>null</code>
   * @param checksum
   *          the checksum object to be used, must not be <code>null</code>
   * @return the checksum specified, updated with the content of the file
   * @throws NullPointerException
   *           if the file or checksum is <code>null</code>
   * @throws IllegalArgumentException
   *           if the file is a directory
   * @throws IOException
   *           if an IO error occurs reading the file
   * @since Commons IO 1.3
   */
    public static Checksum checksum(File file, Checksum checksum) throws IOException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        InputStream in = null;
        try {
            in = new CheckedInputStream(new FileInputStream(file), checksum);
            IOUtils.copy(in, NULL_OUTPUT_STREAM);
        } finally {
            IOUtils.close(in);
        }
        return checksum;
    }

    /**
   * OutputStream的“Null Object”设计模式实现。
   */
    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {

        @Override
        @SuppressWarnings("unused")
        public void write(int b) throws IOException {
        }
    };

    /**
	 * 递归得到指定文件夹下的指定文件，保留文件夹结构。
	 * @param dir 要遍历的文件夹
	 * @param map 匹配文件夹结构的Map，key=文件夹名，value=该文件夹下的文件列表
	 * @param fileSuffix 文件扩展名(格式：.xyz)，如果为null，则代表得到所有的文件
	 */
    public static void listFiles(File dir, Map<File, Collection<File>> map, String fileSuffix) {
        checkDir(dir);
        Assert.notNull(map, "文件夹映射Map不能为Null");
        Collection<File> fileSet = new ArrayList<File>();
        map.put(dir, fileSet);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                listFiles(file, map, fileSuffix);
            } else if (file.isFile()) {
                if (fileSuffix != null && !file.getName().endsWith(fileSuffix)) {
                    continue;
                }
                fileSet.add(file);
            }
        }
    }

    /**
	 * 递归得到指定文件夹下的文件，不保留文件夹结构，仅仅是得到指定文件夹下的文件。
	 * @param dir 要遍历的文件夹
	 * @param files 指定文件夹下的文件列表
	 * @param fileSuffix 文件扩展名，如果为null，则代表得到所有的文件
	 */
    public static void listFiles(File dir, Collection<File> files, String fileSuffix) {
        checkDir(dir);
        Assert.notNull(files, "文件列表不能为Null");
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                listFiles(file, files, fileSuffix);
            } else if (file.isFile()) {
                if (fileSuffix != null && !file.getName().endsWith(fileSuffix)) {
                    continue;
                }
                files.add(file);
            }
        }
    }

    private static JarFile makeJar(String jarPath) {
        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
        } catch (IOException e) {
            logger.error("无法创建jar文件" + jarPath, e);
            throw new RuntimeException("无法创建jar文件" + jarPath, e);
        }
        return jar;
    }

    /**
	 * 遍历指定jar文件内的指定文件。
	 * @param jarPath 要遍历的jar文件路径
	 * @param fileSuffix 文件扩展名(格式：.xyz)，如果为null，则代表得到所有的文件
	 * @return 找到的文件结构映射，key=文件路径，value=该文件的内容
	 * @throws RuntimeException 如果在遍历指定文件时出错
	 */
    public static Map<String, byte[]> listFiles(String jarPath, String fileSuffix) {
        JarFile jar = makeJar(jarPath);
        String fileName = null;
        Map<String, byte[]> map = new HashMap<String, byte[]>();
        for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            JarEntry entry = entries.nextElement();
            fileName = entry.getName();
            if (fileSuffix != null && !fileName.endsWith(fileSuffix)) {
                continue;
            }
            try {
                byte[] content = IOUtils.toByteArray(jar.getInputStream(entry));
                map.put(entry.getName(), content);
            } catch (Exception e) {
                logger.error("提取 [" + fileName + "] 时出错", e);
                throw new RuntimeException("提取 [" + fileName + "] 时出错: " + e);
            }
        }
        return map;
    }

    /**
	 * 从指定jar文件中装载类。
	 * <p style="color:red">
	 * 这个方法只是尽可能的装载Class，如果某个类无法装载，则可能失败。
	 * @param jarPath 指定的jar文件路径
	 * @param basePackName 要装载的类的包名，该方法将装载该包及其子包下面所有的类
	 * @return 符合条件的Class列表
	 */
    public static List<Class<?>> loadClassFromJar(String jarPath, String basePackName) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        Map<String, byte[]> map = listFiles(jarPath, ".class");
        StringBuilder name = new StringBuilder();
        for (String className : map.keySet()) {
            name.delete(0, name.length());
            name.append(className.replace('/', '.')).delete(name.length() - 6, name.length());
            if (name.toString().startsWith(basePackName)) {
                try {
                    classes.add(Class.forName(name.toString()));
                } catch (Exception e) {
                    logger.error("载入类 [" + name + "] 时出错", e);
                    throw new RuntimeException("载入类 [" + name + "] 时出错", e);
                }
            }
        }
        return classes;
    }

    /**
	 * 递归得到指定文件夹下的子文件夹，不保留文件夹结构，仅仅是得到指定文件夹下的子文件夹。
	 * @param dir 要遍历的文件夹
	 * @param subDirs 指定文件夹下的子文件夹列表
	 */
    public static void listDirs(File dir, Collection<File> subDirs) {
        checkDir(dir);
        Assert.notNull(subDirs, "子文件夹列表不能为Null");
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                subDirs.add(file);
                listDirs(file, subDirs);
            }
        }
    }

    /**
	 * 递归得到指定文件夹下的所有文件夹，保留子文件夹结构。
	 * @param dir 要遍历的文件夹
	 * @param subDirs 匹配文件夹结构的Map，key=文件夹，value=该文件夹下的子文件夹列表
	 */
    public static void listDirs(File dir, Map<File, Collection<File>> subDirs) {
        checkDir(dir);
        Assert.notNull(subDirs, "子文件夹列表不能为Null");
        List<File> fileSet = new ArrayList<File>();
        subDirs.put(dir, fileSet);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                fileSet.add(file);
                listDirs(file, subDirs);
            }
        }
    }

    private static void checkDir(File dir) {
        Assert.notNull(dir, "指定文件夹不能为Null");
        Assert.isTrue(dir.exists() && dir.isDirectory(), "指定文件夹不是一个真实目录");
    }

    /**
	 * 把数据写入磁盘。写入过程中发生的任何异常都将包装为RuntimeException抛出。
	 * @param data 要写入磁盘的数据
	 * @param path 目标路径
	 * @param name 保存的文件名
	 * @param suffix 文件后缀，格式<i>.xyz</i>
	 */
    public static void writeDataToDisk(final byte[] data, String path, String name, String suffix) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        final String fullPath = path + name + suffix;
        handleFileWrite(fullPath, new Handler<FileChannel>() {

            public void doWith(FileChannel fc) {
                ByteBuffer bb = ByteBuffer.wrap(data);
                try {
                    fc.write(bb);
                } catch (IOException e) {
                    logger.error("写入 [" + fullPath + "] 时出错:", e);
                    throw new RuntimeException("写入 [" + fullPath + "] 时出错: " + e);
                }
            }
        });
    }

    /**
	 * 从磁盘指定位置读取文件。读取过程中发生的任何异常都将包装为RuntimeException抛出。
	 * @param path 目标文件路径
	 * @param name 目标文件名
	 * @param suffix 目标文件后缀名
	 * @return 文件的二进制内容数组
	 */
    public static byte[] readFileFromDisk(String path, String name, String suffix) {
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        final String fullPath = path + name + suffix;
        byte[] fileContent = null;
        fileContent = handleFileRead(fullPath, new ReturnableHandler<FileChannel, byte[]>() {

            public byte[] doWith(FileChannel fc) {
                try {
                    ByteBuffer bb = ByteBuffer.allocate((int) fc.size() + 1);
                    fc.read(bb);
                    byte[] content = new byte[bb.flip().limit()];
                    bb.get(content);
                    return content;
                } catch (IOException e) {
                    logger.error("读取 [" + fullPath + "] 时出错:", e);
                    throw new RuntimeException("读取 [" + fullPath + "] 时出错:" + e);
                }
            }
        });
        return fileContent;
    }

    /**
	 * 关闭文件通道。
	 */
    public static void closeFileChannel(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                logger.error("关闭文件通道时出错", e);
            }
        }
    }

    /**
	 * 使用类似Ruby处理文件的方式来处理文件读操作。
	 * <p>
	 * 调用者只需要提供文件路径和handler即可，方法会自动处理资源释放工作。
	 * @param <T> 需要从文件中得到的数据的类型
	 * @param path 文件路径，支持 file: classpath: 前缀
	 * @param handler 处理文件的闭包，根据传入的文件通道，返回需要的数据
	 * @return 需要的返回值
	 */
    public static <T> T handleFileRead(String path, ReturnableHandler<FileChannel, T> handler) {
        FileInputStream is = null;
        T reValue = null;
        try {
            File file = ResourceUtils.getFile(path);
            Assert.isTrue(file.exists(), "文件 [" + path + "] 不存在");
            is = new FileInputStream(file);
            FileChannel channel = null;
            try {
                channel = is.getChannel();
                reValue = handler.doWith(channel);
            } finally {
                closeFileChannel(channel);
            }
        } catch (Exception e) {
            logger.error("读取文件 [" + path + "] 时出错", e);
            throw new RuntimeException("读取文件 [" + path + "] 时出错，嵌套异常为 " + e);
        } finally {
            IOUtils.close(is);
        }
        return reValue;
    }

    /**
	 * 使用类似Ruby处理文件的方式来处理文件写操作。
	 * <p>
	 * 调用者只需要提供文件路径和handler即可，方法会自动处理资源释放工作。
	 * @param <T> 需要从文件中得到的数据的类型
	 * @param path 文件路径，支持 file: classpath: 前缀
	 * @param handler 处理文件的闭包，根据传入的文件通道，处理文件的写入
	 * @throws RuntimeException 如果在handler处理通道时出错
	 */
    public static void handleFileWrite(String path, Handler<FileChannel> handler) {
        FileOutputStream os = null;
        File file = null;
        try {
            file = ResourceUtils.getFile(path);
            os = new FileOutputStream(file);
            FileChannel channel = null;
            try {
                channel = os.getChannel();
                handler.doWith(channel);
            } finally {
                closeFileChannel(channel);
            }
        } catch (Exception e) {
            if (file.exists()) {
                file.delete();
            }
            logger.error("写文件 [" + path + "] 时出错", e);
            throw new RuntimeException("写文件 [" + path + "] 时出错，嵌套异常为 " + e);
        } finally {
            IOUtils.close(os);
        }
    }
}
