package org.limewire.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides file manipulation methods; ensures a file exists, makes a file 
 * writable, renames, saves and deletes a file. 
 */
public class FileUtils {

    private static final Log LOG = LogFactory.getLog(FileUtils.class);

    private static final CopyOnWriteArrayList<FileLocker> fileLockers = new CopyOnWriteArrayList<FileLocker>();

    public static void writeObject(String fileName, Object obj) throws IOException {
        writeObject(new File(fileName), obj);
    }

    /**
     * Writes the passed Object to corresponding file
     * @param file The file to which to write 
     * @param map The Object to be stored
     */
    public static void writeObject(File f, Object obj) throws IOException {
        try {
            f = getCanonicalFile(f);
        } catch (IOException tryAnyway) {
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            out.writeObject(obj);
            out.flush();
        } finally {
            close(out);
        }
    }

    public static Object readObject(String fileName) throws IOException, ClassNotFoundException {
        return readObject(new File(fileName));
    }

    /**
     * @param file The file from where to read the serialized Object
     * @return The Object that was read
     */
    public static Object readObject(File file) throws IOException, ClassNotFoundException {
        try {
            file = getCanonicalFile(file);
        } catch (IOException tryAnyway) {
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return in.readObject();
        } finally {
            close(in);
        }
    }

    /**
     * Gets the canonical path, catching buggy Windows errors
     */
    public static String getCanonicalPath(File f) throws IOException {
        try {
            return f.getCanonicalPath();
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            if (OSUtils.isWindows() && msg != null && msg.indexOf("There are no more files") != -1) return f.getAbsolutePath(); else throw ioe;
        }
    }

    /** Same as f.getCanonicalFile() in JDK1.3. */
    public static File getCanonicalFile(File f) throws IOException {
        try {
            return f.getCanonicalFile();
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            if (OSUtils.isWindows() && msg != null && msg.indexOf("There are no more files") != -1) return f.getAbsoluteFile(); else throw ioe;
        }
    }

    /**
     * Determines if file 'a' is an ancestor of file 'b'.
     */
    public static final boolean isAncestor(File a, File b) {
        while (b != null) {
            if (b.equals(a)) return true;
            b = b.getParentFile();
        }
        return false;
    }

    /** 
     * Detects attempts at directory traversal by testing if testDirectory 
     * really is the parent of testPath.  This method should be used to make
     * sure directory traversal tricks aren't being used to trick
     * LimeWire into reading or writing to unexpected places.
     * 
     * Directory traversal security problems occur when software doesn't 
     * check if input paths contain characters (such as "../") that cause the
     * OS to go up a directory.  This function will ignore benign cases where
     * the path goes up one directory and then back down into the original directory.
     * 
     * @return false if testParent is not the parent of testChild.
     * @throws IOException if getCanonicalPath throws IOException for either input file
     */
    public static final boolean isReallyParent(File testParent, File testChild) throws IOException {
        String testParentName = getCanonicalPath(testParent);
        String testChildParentName = getCanonicalPath(testChild.getAbsoluteFile().getParentFile());
        if (!testParentName.equals(testChildParentName)) return false;
        return true;
    }

    /**
     * Detects attempts at directory traversal by testing if testDirectory 
     * really is a parent of testPath.
     * @see isReallyParent
     */
    public static final boolean isReallyInParentPath(File testParent, File testChild) throws IOException {
        String testParentName = getCanonicalPath(testParent);
        File testChildParentFile = testChild.getAbsoluteFile().getParentFile();
        if (testChildParentFile == null) testChildParentFile = testChild.getAbsoluteFile();
        String testChildParentName = getCanonicalPath(testChildParentFile);
        return testChildParentName.startsWith(testParentName);
    }

    /**
     * Utility method that returns the file extension of the given file.
     * 
     * @param f the <tt>File</tt> instance from which the extension 
     *   should be extracted
     * @return the file extension string, or <tt>null</tt> if the extension
     *   could not be extracted
     */
    public static String getFileExtension(File f) {
        String name = f.getName();
        return getFileExtension(name);
    }

    /**
     * Utility method that returns the file extension of the given file.
     * 
     * @param name the file name <tt>String</tt> from which the extension
     *  should be extracted
     * @return the file extension string, or <tt>null</tt> if the extension
     *   could not be extracted
     */
    public static String getFileExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index == -1) return null;
        if (index == 0) return null;
        if (index == (name.length() - 1)) return null;
        return name.substring(index + 1);
    }

    /**
     * Utility method to set a file as non read only.
     * If the file is already writable, does nothing.
     *
     * @param f the <tt>File</tt> instance whose read only flag should
     *  be unset.
     * 
     * @return whether or not <tt>f</tt> is writable after trying to make it
     *  writeable -- note that if the file doesn't exist, then this returns
     *  <tt>true</tt> 
     */
    public static boolean setWriteable(File f) {
        if (!f.exists()) return true;
        if (f.canWrite()) {
            if (OSUtils.isWindows()) return true; else if (!f.isDirectory()) return true;
        }
        String fName;
        try {
            fName = f.getCanonicalPath();
        } catch (IOException ioe) {
            fName = f.getPath();
        }
        String cmds[] = null;
        if (OSUtils.isWindows() || OSUtils.isMacOSX()) SystemUtils.setWriteable(fName); else if (OSUtils.isOS2()) ; else {
            if (f.isDirectory()) cmds = new String[] { "chmod", "u+w+x", fName }; else cmds = new String[] { "chmod", "u+w", fName };
        }
        if (cmds != null) {
            try {
                Process p = Runtime.getRuntime().exec(cmds);
                p.waitFor();
            } catch (SecurityException ignored) {
            } catch (IOException ignored) {
            } catch (InterruptedException ignored) {
            }
        }
        return f.canWrite();
    }

    /**
     * Touches a file, to ensure it exists.
     * Note: unlike the unix touch this does not change the modification time.
     */
    public static void touch(File f) throws IOException {
        if (f.exists()) return;
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        try {
            f.createNewFile();
        } catch (IOException failed) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
            } catch (IOException ioe) {
                ioe.initCause(failed);
                throw ioe;
            } finally {
                close(fos);
            }
        }
    }

    /**
     * Adds a new FileLocker to the list of FileLockers
     * that are checked when a lock needs to be released
     * on a file prior to deletion or renaming.
     * 
     * @param locker
     */
    public static void addFileLocker(FileLocker locker) {
        fileLockers.addIfAbsent(locker);
    }

    /**
     * Removes <code>locker</code> from the list of FileLockers.
     * 
     * @see #addFileLocker(FileLocker) 
     */
    public static void removeFileLocker(FileLocker locker) {
        fileLockers.remove(locker);
    }

    /**
     * Forcibly renames a file, removing any locks that may
     * be held from any FileLockers that were added.
     * 
     * @param src
     * @param dst
     * @return true if the rename succeeded
     */
    public static boolean forceRename(File src, File dst) {
        boolean success = src.renameTo(dst);
        if (!success) {
            for (FileLocker locker : fileLockers) {
                if (locker.releaseLock(src)) {
                    success = src.renameTo(dst);
                    if (success) break;
                }
            }
        }
        if (!success) {
            success = copy(src, dst);
            if (success) src.delete();
        }
        return success;
    }

    /**
     * Saves the data iff it was written exactly as we wanted.
     */
    public static boolean verySafeSave(File dir, String name, byte[] data) {
        File tmp;
        try {
            tmp = FileUtils.createTempFile(name, "tmp", dir);
        } catch (IOException hrorible) {
            return false;
        }
        File out = new File(dir, name);
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(tmp));
            os.write(data);
            os.flush();
        } catch (IOException bad) {
            return false;
        } finally {
            close(os);
        }
        byte[] read = readFileFully(tmp);
        if (read == null || !Arrays.equals(read, data)) return false;
        return forceRename(tmp, out);
    }

    /**
     * Reads a file, filling a byte array.
     */
    public static byte[] readFileFully(File source) {
        DataInputStream raf = null;
        int length = (int) source.length();
        if (length <= 0) return null;
        byte[] data = new byte[length];
        try {
            raf = new DataInputStream(new BufferedInputStream(new FileInputStream(source)));
            raf.readFully(data);
        } catch (IOException ioe) {
            return null;
        } finally {
            close(raf);
        }
        return data;
    }

    /**
     * @param directory Gets all files under this directory RECURSIVELY.
     * @param filter If null, then returns all files.  Else, only returns files
     * extensions in the filter array.
     * @return An array of Files recursively obtained from the directory,
     * according to the filter.
     * 
     */
    public static File[] getFilesRecursive(File directory, String[] filter) {
        List<File> dirs = new ArrayList<File>();
        List<File> retFileArray = new ArrayList<File>();
        File[] retArray = new File[0];
        if (directory.exists() && directory.isDirectory()) dirs.add(directory);
        while (dirs.size() > 0) {
            File currDir = dirs.remove(0);
            String[] listedFiles = currDir.list();
            for (int i = 0; (listedFiles != null) && (i < listedFiles.length); i++) {
                File currFile = new File(currDir, listedFiles[i]);
                if (currFile.isDirectory()) dirs.add(currFile); else if (currFile.isFile()) {
                    boolean shouldAdd = false;
                    if (filter == null) shouldAdd = true; else {
                        String ext = FileUtils.getFileExtension(currFile);
                        for (int j = 0; (j < filter.length) && (ext != null); j++) {
                            if (ext.equalsIgnoreCase(filter[j])) {
                                shouldAdd = true;
                                break;
                            }
                        }
                    }
                    if (shouldAdd) retFileArray.add(currFile);
                }
            }
        }
        if (!retFileArray.isEmpty()) {
            retArray = new File[retFileArray.size()];
            for (int i = 0; i < retArray.length; i++) retArray[i] = retFileArray.get(i);
        }
        return retArray;
    }

    /**
     * Deletes the given file or directory, moving it to the trash can or 
     * recycle bin if the platform has one and <code>moveToTrash</code> is
     * true.
     * 
     * @param file The file or directory to trash or delete
     * @param moveToTrash whether the file should be moved to the trash bin 
     * or permanently deleted
     * @return     true on success
     * 
     * @throws IllegalArgumentException if the OS does not support moving files
     * to a trash bin, check with {@link OSUtils#supportsTrash()}.
     */
    public static boolean delete(File file, boolean moveToTrash) {
        if (!file.exists()) {
            return false;
        }
        if (moveToTrash) {
            if (OSUtils.isMacOSX()) {
                return moveToTrashOSX(file);
            } else if (OSUtils.isWindows()) {
                return SystemUtils.recycle(file);
            } else {
                throw new IllegalArgumentException("OS does not support trash");
            }
        } else {
            return deleteRecursive(file);
        }
    }

    /**
     * Moves the given file or directory to Trash.
     * 
     * @param file The file or directory to move to Trash
     * @throws IOException if the canonical path cannot be resolved
     *          or if the move process is interrupted
     * @return true on success
     */
    private static boolean moveToTrashOSX(File file) {
        try {
            String[] command = moveToTrashCommand(file);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream();
            Process process = builder.start();
            ProcessUtils.consumeAllInput(process);
            process.waitFor();
        } catch (InterruptedException err) {
            LOG.error("InterruptedException", err);
        } catch (IOException err) {
            LOG.error("IOException", err);
        }
        return !file.exists();
    }

    /**
     * Creates and returns the the osascript command to move
     * a file or directory to the Trash
     * 
     * @param file The file or directory to move to Trash
     * @throws IOException if the canonical path cannot be resolved
     * @return OSAScript command
     */
    private static String[] moveToTrashCommand(File file) {
        String path = null;
        try {
            path = file.getCanonicalPath();
        } catch (IOException err) {
            LOG.error("IOException", err);
            path = file.getAbsolutePath();
        }
        String fileOrFolder = (file.isFile() ? "file" : "folder");
        String[] command = new String[] { "osascript", "-e", "set unixPath to \"" + path + "\"", "-e", "set hfsPath to POSIX file unixPath", "-e", "tell application \"Finder\"", "-e", "if " + fileOrFolder + " hfsPath exists then", "-e", "move " + fileOrFolder + " hfsPath to trash", "-e", "end if", "-e", "end tell" };
        return command;
    }

    /**
     * Deletes all files in 'directory'.
     * Returns true if this succesfully deleted every file recursively, including itself.
     * 
     * @param directory
     * @return
     */
    public static boolean deleteRecursive(File directory) {
        String canonicalParent;
        try {
            canonicalParent = getCanonicalPath(directory);
        } catch (IOException ioe) {
            return false;
        }
        if (!directory.isDirectory()) return directory.delete();
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                if (!getCanonicalPath(files[i]).startsWith(canonicalParent)) continue;
            } catch (IOException ioe) {
                return false;
            }
            if (!deleteRecursive(files[i])) return false;
        }
        return directory.delete();
    }

    /**
     * @return true if the two files are the same.  If they are both
     * directories returns true if there is at least one file that 
     * conflicts.
     */
    public static boolean conflictsAny(File a, File b) {
        if (a.equals(b)) return true;
        Set<File> unique = new HashSet<File>();
        unique.add(a);
        for (File recursive : getFilesRecursive(a, null)) unique.add(recursive);
        if (unique.contains(b)) return true;
        for (File recursive : getFilesRecursive(b, null)) {
            if (unique.contains(recursive)) return true;
        }
        return false;
    }

    /**
     * Returns total length of all files by going through
     * the given directory (if it's a directory).
     */
    public static long getLengthRecursive(File f) {
        if (!f.isDirectory()) return f.length();
        long ret = 0;
        for (File file : getFilesRecursive(f, null)) ret += file.length();
        return ret;
    }

    /**
     * A utility method to close Closeable objects (Readers, Writers, 
     * Input- and OutputStreams and RandomAccessFiles).
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * A utility method to flush Flushable objects (Readers, Writers, 
     * Input- and OutputStreams and RandomAccessFiles).
     */
    public static void flush(Flushable flushable) {
        if (flushable != null) {
            try {
                flushable.flush();
            } catch (IOException ignored) {
            }
        }
    }

    /** 
     * Attempts to copy the first 'amount' bytes of file 'src' to 'dst',
     * returning the number of bytes actually copied.  If 'dst' already exists,
     * the copy may or may not succeed.
     * 
     * @param src the source file to copy
     * @param amount the amount of src to copy, in bytes
     * @param dst the place to copy the file
     * @return the number of bytes actually copied.  Returns 'amount' if the
     *  entire requested range was copied.
     */
    public static long copy(File src, long amount, File dst) {
        final int BUFFER_SIZE = 1024;
        long amountToRead = amount;
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(src));
            out = new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buf = new byte[BUFFER_SIZE];
            while (amountToRead > 0) {
                int read = in.read(buf, 0, (int) Math.min(BUFFER_SIZE, amountToRead));
                if (read == -1) break;
                amountToRead -= read;
                out.write(buf, 0, read);
            }
        } catch (IOException e) {
        } finally {
            close(in);
            flush(out);
            close(out);
        }
        return amount - amountToRead;
    }

    /** 
     * Copies the file 'src' to 'dst', returning true iff the copy succeeded.
     * If 'dst' already exists, the copy may or may not succeed.  May also
     * fail for VERY large source files.
     */
    public static boolean copy(File src, File dst) {
        long length = src.length();
        return copy(src, (int) length, dst) == length;
    }

    /**
     * Creates a temporary file using
     * {@link File#createTempFile(String, String, File)}, trying a few times.
     * This is a workaround for Sun Bug: 6325169: createTempFile occasionally
     * fails (throwing an IOException).
     */
    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        IOException iox = null;
        for (int i = 0; i < 10; i++) {
            try {
                return File.createTempFile(prefix, suffix, directory);
            } catch (IOException x) {
                iox = x;
            }
        }
        throw iox;
    }

    /**
     * Creates a temporary file using
     * {@link File#createTempFile(String, String)}, trying a few times.
     * This is a workaround for Sun Bug: 6325169: createTempFile occasionally
     * fails (throwing an IOException).
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        IOException iox = null;
        for (int i = 0; i < 10; i++) {
            try {
                return File.createTempFile(prefix, suffix);
            } catch (IOException x) {
                iox = x;
            }
        }
        throw iox;
    }

    public static File getJarFromClasspath(String markerFile) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            classLoader = FileUtils.class.getClassLoader();
        }
        if (classLoader == null) {
            return null;
        }
        return getJarFromClasspath(classLoader, markerFile);
    }

    public static File getJarFromClasspath(ClassLoader classLoader, String markerFile) {
        if (classLoader == null) {
            throw new IllegalArgumentException();
        }
        URL messagesURL = classLoader.getResource(markerFile);
        if (messagesURL != null) {
            String url = CommonUtils.decode(messagesURL.toExternalForm());
            if (url != null && url.startsWith("jar:file:")) {
                url = url.substring("jar:file:".length(), url.length());
                url = url.substring(0, url.length() - markerFile.length() - "!/".length());
                return new File(url);
            }
        }
        return null;
    }

    public static void copyDirectoryRecursively(File srcDir, File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdir();
            targetDir.setWritable(true);
        }
        for (File srcElement : srcDir.listFiles()) {
            if (srcElement.isFile()) {
                FileUtils.copy(srcElement, new File(targetDir + File.separator + srcElement.getName()));
            } else if (srcElement.isDirectory()) {
                FileUtils.copyDirectoryRecursively(srcElement, new File(targetDir, srcElement.getName()));
            }
        }
    }
}
