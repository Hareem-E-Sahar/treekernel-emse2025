package org.matsim.core.utils.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.matsim.core.controler.Controler;

/** A class with some static utility functions for file-I/O. */
public class IOUtils {

    private static final String GZ = ".gz";

    public static final String LOGFILE = "logfile.log";

    public static final String WARNLOGFILE = "logfileWarningsErrors.log";

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF8");

    public static final Charset CHARSET_WINDOWS_ISO88591 = Charset.forName("ISO-8859-1");

    public static final String NATIVE_NEWLINE = System.getProperty("line.separator");

    private static final Logger log = Logger.getLogger(IOUtils.class);

    /**
	 * Call this method to create 2 log4j logfiles in the output directory specified as parameter.
	 * The first logfile contains all messages the second only those above log Level.WARN (Priority.WARN).
	 * After the end of the programm run it is strongly recommended to close the file logger by calling
	 * the method closeOutputDirLogging().
	 *
	 * @param outputDirectory the outputdirectory to create the files, whithout seperator at the end.
	 * @param logEvents List of LoggingEvents, may be null, contains log information which should be written
	 * to the files, e.g. LoggingEvents which occurred before the files can be created.
	 * @throws IOException
	 * @see IOUtils#closeOutputDirLogging()
	 * @author dgrether
	 */
    public static void initOutputDirLogging(final String outputDirectory, final List<LoggingEvent> logEvents) throws IOException {
        IOUtils.initOutputDirLogging(outputDirectory, logEvents, null);
    }

    /**
	 * Can be used to add a prefix (e.g. specifying the runId) to the logfiles
	 * @see IOUtils#initOutputDirLogging(String, List);
	 */
    public static void initOutputDirLogging(final String outputDirectory, final List<LoggingEvent> logEvents, final String runIdPrefix) throws IOException {
        String prefix = runIdPrefix;
        if (prefix == null) {
            prefix = "";
        } else {
            prefix = prefix + ".";
        }
        Logger root = Logger.getRootLogger();
        FileAppender appender = new FileAppender(Controler.DEFAULTLOG4JLAYOUT, outputDirectory + System.getProperty("file.separator") + prefix + LOGFILE);
        appender.setName(LOGFILE);
        root.addAppender(appender);
        FileAppender warnErrorAppender = new FileAppender(Controler.DEFAULTLOG4JLAYOUT, outputDirectory + System.getProperty("file.separator") + prefix + WARNLOGFILE);
        warnErrorAppender.setName(WARNLOGFILE);
        warnErrorAppender.setThreshold(Level.WARN);
        root.addAppender(warnErrorAppender);
        if (logEvents != null) {
            for (LoggingEvent e : logEvents) {
                appender.append(e);
                if (e.getLevel().isGreaterOrEqual(Level.WARN)) {
                    warnErrorAppender.append(e);
                }
            }
        }
    }

    /**
	 * Call this method to close the log file streams opened by a call of IOUtils.initOutputDirLogging().
	 * This avoids problems concerning open streams after the termination of the program.
	 * @see IOUtils#initOutputDirLogging(String, List)
	 */
    public static void closeOutputDirLogging() {
        Logger root = Logger.getRootLogger();
        Appender app = root.getAppender(LOGFILE);
        root.removeAppender(app);
        app.close();
        app = root.getAppender(WARNLOGFILE);
        root.removeAppender(app);
        app.close();
    }

    /**
	 * Tries to open the specified file for reading and returns a BufferedReader for it.
	 * Supports gzip-compressed files, such files are automatically decompressed.
	 * If the file is not found, a gzip-compressed version of the file with the
	 * added ending ".gz" will be searched for and used if found. Assumes that the text
	 * in the file is stored in UTF-8 (without BOM).
	 *
	 * @param filename The file to read, may contain the ending ".gz" to force reading a compressed file.
	 * @return BufferedReader for the specified file.
	 * @throws UncheckedIOException
	 *
	 * @author mrieser
	 */
    public static BufferedReader getBufferedReader(final String filename) throws UncheckedIOException {
        return getBufferedReader(filename, Charset.forName("UTF8"));
    }

    /**
	 * Tries to open the specified file for reading and returns a BufferedReader for it.
	 * Supports gzip-compressed files, such files are automatically decompressed.
	 * If the file is not found, a gzip-compressed version of the file with the
	 * added ending ".gz" will be searched for and used if found.
	 *
	 * @param filename The file to read, may contain the ending ".gz" to force reading a compressed file.
	 * @param charset the Charset of the file to read
	 * @return BufferedReader for the specified file.
	 * @throws UncheckedIOException
	 *
	 * @author mrieser
	 */
    public static BufferedReader getBufferedReader(final String filename, final Charset charset) throws UncheckedIOException {
        BufferedReader infile = null;
        if (filename == null) {
            throw new UncheckedIOException(new FileNotFoundException("No filename given (filename == null)"));
        }
        try {
            if (new File(filename).exists()) {
                if (filename.endsWith(GZ)) {
                    infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), charset));
                } else {
                    infile = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset));
                }
            } else if (new File(filename + GZ).exists()) {
                infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename + GZ)), charset));
            } else {
                InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(filename);
                if (stream != null) {
                    if (filename.endsWith(GZ)) {
                        infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(stream), charset));
                        log.info("loading file from classpath: " + filename);
                    } else {
                        infile = new BufferedReader(new InputStreamReader(stream, charset));
                        log.info("loading file from classpath: " + filename);
                    }
                } else {
                    stream = IOUtils.class.getClassLoader().getResourceAsStream(filename + GZ);
                    if (stream != null) {
                        infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(stream), charset));
                        log.info("loading file from classpath: " + filename + GZ);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (infile == null) {
            throw new UncheckedIOException(new FileNotFoundException(filename));
        }
        return infile;
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * Supports gzip-compression of the written data. The filename may contain the
	 * ending ".gz". If no compression is to be used, the ending will be removed
	 * from the filename. If compression is to be used and the filename does not yet
	 * have the ending ".gz", the ending will be added to it.
	 *
	 * @param filename The filename where to write the data.
	 * @param useCompression whether the file should be gzip-compressed or not.
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getBufferedWriter(final String filename, final boolean useCompression) throws UncheckedIOException {
        if (filename == null) {
            throw new UncheckedIOException(new FileNotFoundException("No filename given (filename == null)"));
        }
        if (useCompression && !filename.endsWith(GZ)) {
            return getBufferedWriter(filename + GZ);
        } else if (!useCompression && filename.endsWith(GZ)) {
            return getBufferedWriter(filename.substring(0, filename.length() - 3));
        } else {
            return getBufferedWriter(filename);
        }
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed.
	 * The data written will be encoded as UTF-8 (only relevant if you use Umlauts or
	 * other characters not used in plain English).
	 *
	 * @param filename The filename where to write the data.
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getBufferedWriter(final String filename) throws UncheckedIOException {
        return getBufferedWriter(filename, Charset.forName("UTF8"));
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed.
	 * The data written will be encoded as UTF-8 (only relevant if you use Umlauts or
	 * other characters not used in plain English). If the file already exists, content
	 * will not be overwritten, but new content be appended to the file.
	 *
	 * @param filename The filename where to write the data.
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getAppendingBufferedWriter(final String filename) throws UncheckedIOException {
        return getBufferedWriter(filename, Charset.forName("UTF8"), true);
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed.
	 *
	 * @param filename The filename where to write the data.
	 * @param charset the encoding to use to write the file.
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getBufferedWriter(final String filename, final Charset charset) throws UncheckedIOException {
        return getBufferedWriter(filename, charset, false);
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed. If
	 * the file already exists, content will not be overwritten, but new content be
	 * appended to the file.
	 *
	 * @param filename The filename where to write the data.
	 * @param charset the encoding to use to write the file.
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getAppendingBufferedWriter(final String filename, final Charset charset) throws UncheckedIOException {
        return getBufferedWriter(filename, charset, true);
    }

    /**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed.
	 *
	 * @param filename The filename where to write the data.
	 * @param charset the encoding to use to write the file.
	 * @param append <code>true</code> if the file should be opened for appending, instead of overwriting
	 * @return BufferedWriter for the specified file.
	 * @throws UncheckedIOException
	 */
    public static BufferedWriter getBufferedWriter(final String filename, final Charset charset, final boolean append) throws UncheckedIOException {
        if (filename == null) {
            throw new UncheckedIOException(new FileNotFoundException("No filename given (filename == null)"));
        }
        try {
            if (filename.toLowerCase(Locale.ROOT).endsWith(GZ)) {
                File f = new File(filename);
                if (append && f.exists() && (f.length() > 0)) {
                    throw new IllegalArgumentException("Appending to an existing gzip-compressed file is not supported.");
                }
                return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename, append)), charset));
            }
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append), charset));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
	 * Attempts to rename a file. The built-in method File.renameTo() often fails
	 * for different reasons (e.g. if the file should be moved across different
	 * file systems). This method first tries the method File.renameTo(),
	 * but tries other possibilities if the first one fails.
	 *
	 * @param fromFilename The file name of an existing file
	 * @param toFilename The new file name
	 * @return <code>true</code> if the file was successfully renamed, <code>false</code> otherwise
	 *
	 * @author mrieser
	 */
    public static boolean renameFile(final String fromFilename, final String toFilename) {
        return renameFile(new File(fromFilename), new File(toFilename));
    }

    /**
	 * Attempts to rename a file. The built-in method File.renameTo() often fails
	 * for different reasons (e.g. if the file should be moved across different
	 * file systems). This method first tries the method File.renameTo(),
	 * but tries other possibilities if the first one fails.
	 *
	 * @param fromFile The existing file.
	 * @param toFile A file object pointing to the new location of the file.
	 * @return <code>true</code> if the file was successfully renamed, <code>false</code> otherwise
	 *
	 * @author mrieser
	 */
    public static boolean renameFile(final File fromFile, final File toFile) {
        File toFile2 = toFile;
        if (fromFile.renameTo(toFile)) {
            return true;
        }
        if (!fromFile.exists()) {
            return false;
        }
        if (!fromFile.canRead()) {
            return false;
        }
        if (toFile.isDirectory()) {
            toFile2 = new File(toFile, fromFile.getName());
        }
        if (toFile2.exists()) {
            return false;
        }
        String parent = toFile2.getParent();
        if (parent == null) parent = System.getProperty("user.dir");
        File dir = new File(parent);
        if (!dir.exists()) {
            return false;
        }
        if (!dir.canWrite()) {
            return false;
        }
        try {
            copyFile(fromFile, toFile2);
        } catch (UncheckedIOException e) {
            if (toFile2.exists()) toFile2.delete();
            return false;
        }
        fromFile.delete();
        return true;
    }

    /**
	 * Copies the file content from one file to the other file.
	 *
	 * @param fromFile The file containing the data to be copied
	 * @param toFile The file the data should be written to
	 * @throws UncheckedIOException
	 *
	 * @author mrieser
	 */
    public static void copyFile(final File fromFile, final File toFile) throws UncheckedIOException {
        InputStream from = null;
        OutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            copyStream(from, to);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
	 * Copies the content from one stream to another stream.
	 *
	 * @param fromStream The stream containing the data to be copied
	 * @param toStream The stream the data should be written to
	 * @throws IOException
	 *
	 * @author mrieser
	 */
    public static void copyStream(final InputStream fromStream, final OutputStream toStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fromStream.read(buffer)) != -1) {
            toStream.write(buffer, 0, bytesRead);
        }
    }

    /**
	 * This method recursively deletes the directory and all its contents. This
	 * method will not follow symbolic links, i.e. if the directory or one of the
	 * sub-directories contains a symbolic link, it will not delete this link. Thus
	 * the directory cannot be deleted, too.
	 *
	 * @param dir
	 *          File which must represent a directory, files or symbolic links are
	 *          not permitted as arguments
	 *
	 * @author dgrether
	 */
    public static void deleteDirectory(final File dir) {
        if (dir.isFile()) {
            throw new IllegalArgumentException("Directory " + dir.getName() + " must not be a file!");
        } else if (isLink(dir)) {
            throw new IllegalArgumentException("Directory " + dir.getName() + " doesn't exist or is a symbolic link!");
        }
        if (dir.exists()) {
            IOUtils.deleteDir(dir);
        } else {
            throw new IllegalArgumentException("Directory " + dir.getName() + " doesn't exist!");
        }
    }

    /**
	 * Recursive helper for {@link #deleteDirectory(File)}. Deletes a directory
	 * recursive. If the directory or one of its sub-directories is a symbolic
	 * neither the link nor its parent directories are deleted.
	 *
	 * @param dir The directory to be recursively deleted.
	 *
	 * @author dgrether
	 */
    private static void deleteDir(final File dir) {
        File[] outDirContents = dir.listFiles();
        for (int i = 0; i < outDirContents.length; i++) {
            if (isLink(outDirContents[i])) {
                continue;
            }
            if (outDirContents[i].isDirectory()) {
                deleteDir(outDirContents[i]);
            }
            if (!outDirContents[i].delete() && outDirContents[i].exists()) {
                log.error("Could not delete " + outDirContents[i].getAbsolutePath());
            }
        }
        if (!dir.delete()) {
            log.error("Could not delete " + dir.getAbsolutePath());
        }
    }

    /**
	 * Checks if the given File Object may be a symbolic link.<br />
	 *
	 * For a link that actually points to something (either a file or a
	 * directory), the absolute path is the path through the link, whereas the
	 * canonical path is the path the link references.<br />
	 *
	 * Dangling links appear as files of size zero, and generate a
	 * FileNotFoundException when you try to open them. Unfortunately non-existent
	 * files have the same behavior (size zero - why did't file.length() throw an
	 * exception if the file doesn't exist, rather than returning length zero?).<br />
	 *
	 * The routine below appears to detect Unix/Linux symbolic links, but it also
	 * returns true for non-existent files. Note that if a directory is a link,
	 * all the contents of this directory will appear as symbolic links as well.<br />
	 *
	 * Note that this method has problems if the file is specified with a relative
	 * path like "./" or "../".<br />
	 *
	 * @see <a href="http://www.idiom.com/~zilla/Xfiles/javasymlinks.html">Javasymlinks on www.idiom.com</a>
	 *
	 * @param file
	 * @return true if the file is a symbolic link or does not even exist. false if not
	 *
	 * @author dgrether
	 */
    public static boolean isLink(final File file) {
        try {
            if (!file.exists()) {
                return true;
            }
            String cnnpath = file.getCanonicalPath();
            String abspath = file.getAbsolutePath();
            return !abspath.equals(cnnpath);
        } catch (IOException ex) {
            System.err.println(ex);
            return true;
        }
    }

    /**
   * Tries to open the specified file for reading and returns an InputStream for it.
   * Supports gzip-compressed files, such files are automatically decompressed.
   * If the file is not found, a gzip-compressed version of the file with the
   * added ending ".gz" will be searched for and used if found.
   *
   * @param filename The file to read, may contain the ending ".gz" to force reading a compressed file.
   * @return InputStream for the specified file.
   * @throws UncheckedIOException
   *
   * @author dgrether
   */
    public static InputStream getInputStream(final String filename) throws UncheckedIOException {
        InputStream inputStream = null;
        if (filename == null) {
            throw new UncheckedIOException(new FileNotFoundException("No filename given (filename == null)"));
        }
        try {
            if (new File(filename).exists()) {
                if (filename.endsWith(GZ)) {
                    inputStream = new GZIPInputStream(new FileInputStream(filename));
                } else {
                    inputStream = new FileInputStream(filename);
                }
            } else if (new File(filename + GZ).exists()) {
                inputStream = new GZIPInputStream(new FileInputStream(filename));
            } else {
                InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(filename);
                if (stream != null) {
                    if (filename.endsWith(GZ)) {
                        inputStream = new GZIPInputStream(stream);
                    } else {
                        inputStream = stream;
                    }
                } else {
                    stream = IOUtils.class.getClassLoader().getResourceAsStream(filename + GZ);
                    if (stream != null) {
                        inputStream = new GZIPInputStream(stream);
                    }
                }
                if (inputStream != null) {
                    log.info("streaming file from classpath: " + filename);
                }
            }
            if (inputStream == null) {
                throw new FileNotFoundException(filename);
            }
            return inputStream;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
