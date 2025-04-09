package org.jasen.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jasen.error.ErrorHandlerBroker;
import org.jasen.error.JasenException;

/**
 * <P>
 * 	General file utility methods.
 * </P>
 * @author Jason Polites
 */
public class FileUtils {

    static Logger logger = Logger.getLogger(FileUtils.class);

    /**
     *
     */
    public FileUtils() {
        super();
    }

    /**
     * Removes (deletes) all duplicate files found in the given folder.
     * @param folder
     * @param filename If true, files with the same root name are considered duplicates irrespective of their fingerprint
     * @throws IOException
     */
    public static void dedupe(File folder, boolean filename) throws IOException {
        dedupe(folder, null, null, filename);
    }

    /**
     * Removes all duplicate files found in the given folder and moves them to the deposit folder
     * @param folder
     * @param deposit
     * @param filename If true, files with the same root name are considered duplicates irrespective of their fingerprint
     * @throws IOException
     */
    public static void dedupe(File folder, File deposit, boolean filename) throws IOException {
        dedupe(folder, null, deposit, filename);
    }

    /**
     * Removes (deletes) all duplicate files found in the given folder with the given filter
     * @param folder
     * @param filter
     * @param filename If true, files with the same root name are considered duplicates irrespective of their fingerprint
     * @throws IOException
     */
    public static void dedupe(File folder, FileFilter filter, boolean filename) throws IOException {
        dedupe(folder, filter, null, filename);
    }

    /**
     * Removes duplicate files from the given folder by renaming them to the given extension.
     * <p>
     * If extension is null, the duplicate files are deleted.
     * </p>
     * <p>
     * If more than one duplicate of the same file is found, an integer count is appended
     * to the renamed file.
     * </p>
     * @param folder The folder in which to look for duplicates
     * @param filter The file filter to use when listing files
     * @param deposit The path to which duplicates are moved (must be a directory)
     * @param filename If true, files with the same root name are considered duplicates irrespective of their fingerprint
     */
    public static void dedupe(File folder, FileFilter filter, File deposit, boolean filename) throws IOException {
        File[] files = null;
        if (!folder.isDirectory()) {
            throw new IOException("folder parameter must be a directory");
        }
        if (deposit != null && !deposit.isDirectory()) {
            throw new IOException("deposit parameter must be a directory");
        }
        if (filter != null) {
            files = folder.listFiles(filter);
        } else {
            files = folder.listFiles();
        }
        if (files != null) {
            Map fingerprints = new Hashtable();
            String strFingerprint = null;
            String strFilename = null;
            String rootName = null;
            File moveTo = null;
            boolean dupe = false;
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    dupe = false;
                    rootName = FileUtils.getFilenameWithoutExtension(files[i].getName());
                    if (filename) {
                        strFilename = (String) fingerprints.get(rootName);
                        if (strFilename == null) {
                            fingerprints.put(rootName, files[i].getName());
                        } else {
                            dupe = true;
                        }
                    }
                    if (!dupe) {
                        strFingerprint = fingerPrintFile(files[i], 64);
                        strFilename = (String) fingerprints.get(strFingerprint);
                        if (strFilename == null) {
                            fingerprints.put(strFingerprint, files[i].getName());
                        } else {
                            dupe = true;
                        }
                    }
                    if (dupe) {
                        logger.info(files[i].getName() + " is a duplicate of " + strFilename);
                        if (deposit != null) {
                            moveTo = new File(deposit.getAbsolutePath() + System.getProperty("file.separator") + files[i].getName());
                            files[i].renameTo(moveTo);
                        } else {
                            if (!files[i].delete()) {
                                System.err.println("ERROR:  Could not delete " + files[i].getName());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a distinct "fingerprint" of the given file such that two files
     * with the same content will have the same fingerprint.
     * @param file The file to fingerprint
     * @param length The length of the fingerprint.  The longer the length, the more accurate the fingerprint.  NOTE: The size of the actual string returned will be greater than "length" bytes
     * @return A String representing a non-unique representation of the file
     * @throws IOException
     */
    public static String fingerPrintFile(File file, int length) throws IOException {
        long size = file.length();
        int space = 0;
        FileInputStream fin = null;
        RandomAccessFile raf = null;
        try {
            if (size <= length) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                fin = new FileInputStream(file);
                IOUtils.pipe(fin, bout, 1024);
                return new String(bout.toByteArray());
            } else {
                space = (int) Math.floor((size / length));
                raf = new RandomAccessFile(file, "r");
                StringBuffer buffer = new StringBuffer();
                for (long i = 0; i < size; i += space) {
                    try {
                        buffer.append(raf.readByte());
                        raf.skipBytes(space);
                    } catch (EOFException e) {
                        i = size;
                    }
                }
                buffer.append(size);
                return buffer.toString();
            }
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ignore) {
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
	 * Gets the absolute path without the file (root path).
	 * @param pathname
	 * @return The absolute path to the file excluding the filename
	 */
    public static String getAbsolutePathWithoutFile(File pathname) {
        if (pathname.isDirectory()) {
            return getSafePath(pathname.getAbsolutePath());
        } else {
            return getSafePath(pathname.getParentFile().getAbsolutePath());
        }
    }

    /**
	 * Ensures the path is terminated with a file separator
	 * @param path
	 * @return
	 */
    public static String getSafePath(String path) {
        if (path != null) {
            int fs = path.indexOf('/');
            int bs = path.indexOf('\\');
            if (fs > -1 && !path.endsWith("/")) {
                path += '/';
            } else if (bs > -1 && !path.endsWith("\\")) {
                path += '\\';
            } else if (fs <= -1 && bs <= -1) {
                path += System.getProperty("file.separator");
            }
        }
        return path;
    }

    /**
	 * Gets the name of a file without the extension (text after last dot)
	 * @param filename
	 * @return The root filename without its extension
	 */
    public static String getFilenameWithoutExtension(String filename) {
        if (filename.indexOf('.') > -1) {
            return filename.substring(0, filename.lastIndexOf("."));
        } else {
            return filename;
        }
    }

    /**
	 * Returns the String that occurs after the last "dot" in the filename
	 * @param pathname
	 * @return The extension of the file
	 */
    public static String getFileExtension(File pathname) {
        return getFileExtension(pathname.getName());
    }

    public static String getFileExtension(String filename) {
        if (filename.indexOf('.') > -1) {
            return filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        } else {
            return null;
        }
    }

    /**
	 * Convenience file copy method
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
    public static final void copy(File source, File destination) throws IOException {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(source);
            fout = new FileOutputStream(destination);
            IOUtils.pipe(fin, fout, 1024);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ignore) {
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Lists all the .jar files in the given folder as URL references
     * @param folder The folder in which to look
     * @return An array of java.net.URL objects
     * @throws MalformedURLException
     */
    public static URL[] listJars(File folder) throws MalformedURLException {
        return listFiles(folder, ".jar");
    }

    /**
     * Lists all the files in the given folder with the given extension
     * @param folder The folder in which to look
     * @param extension The file extension (case sensitive)
     * @return An array of java.net.URL objects
     * @throws MalformedURLException
     */
    public static URL[] listFiles(File folder, String extension) throws MalformedURLException {
        File[] files = folder.listFiles(new ExtensionFileFilter(extension));
        URL[] urls = null;
        if (files != null) {
            urls = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURL();
            }
        }
        return urls;
    }

    /**
     * <p>
     * Inner file filter class for listing files of known extension.
     * </p>
     */
    public static final class ExtensionFileFilter implements FileFilter {

        String extension;

        public ExtensionFileFilter(String extension) {
            this.extension = extension;
        }

        public boolean accept(File pathname) {
            return pathname.getName().endsWith(extension);
        }
    }

    /**
     * Resolves an absolute OR classpath-relative path to an absolute path.
     * @param strPath The path to resolve.
     * @return The absolute file path.
     * @throws JasenException
     */
    public static String getAbsolutePath(ClassLoader classLoader, String strPath) throws JasenException {
        File file = new File(strPath);
        if (!file.exists()) {
            try {
                if (logger.isDebugEnabled()) logger.debug("Loading resource from: " + strPath);
                URI uri = new URI(classLoader.getResource(strPath).toExternalForm());
                file = new File(uri);
                strPath = file.getAbsolutePath();
            } catch (URISyntaxException e) {
                throw new JasenException(e);
            }
        }
        return strPath;
    }

    public static Properties getPropertiesFile(ClassLoader classLoader, String path) throws JasenException {
        path = FileUtils.getAbsolutePath(classLoader, path);
        Properties props = null;
        File file = new File(path);
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            props = new Properties();
            props.load(fin);
        } catch (IOException e) {
            throw new JasenException(e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    ErrorHandlerBroker.getInstance().getErrorHandler().handleException(e);
                }
            }
        }
        return props;
    }
}
