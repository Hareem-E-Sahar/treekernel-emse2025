package org.ivoa.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ivoa.bean.LogSupport;

/**
 * File utility methods : Several utility methods : finds a file in the class path (jar), open files
 * for read or write operation and close file
 *
 * @author Laurent Bourges (voparis) / Gerard Lemson (mpe)
 */
public final class FileUtils extends LogSupport {

    /**
   * default read buffer capacity : DEFAULT_READ_BUFFER_SIZE = 16K
   */
    private static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;

    /**
   * default write buffer capacity : DEFAULT_WRITE_BUFFER_SIZE = 16K
   */
    private static final int DEFAULT_WRITE_BUFFER_SIZE = 16 * 1024;

    /**
   * Forbidden FileUtils constructor
   */
    private FileUtils() {
    }

    /**
   * Find a file in the current classloader (application class Loader)
   *
   * @param fileName file name only no path included
   * @return URL to the file or null
   */
    public static final URL getResource(final String fileName) {
        final URL url = FileUtils.class.getClassLoader().getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Unable to find the file in classpath : " + fileName);
        }
        if (logB.isInfoEnabled()) {
            logB.info("FileUtils.getSystemFileInputStream : URL : " + url);
        }
        return url;
    }

    /**
   * Find a file in the current classloader (application class Loader)
   *
   * @param fileName file name only no path included
   * @return InputStream or RuntimeException if not found
   * @throws RuntimeException if not found
   */
    public static final InputStream getSystemFileInputStream(final String fileName) {
        final URL url = getResource(fileName);
        try {
            return url.openStream();
        } catch (final IOException ioe) {
            throw new RuntimeException("Failure when loading file in classpath : " + fileName, ioe);
        }
    }

    /**
   * Close an inputStream
   *
   * @param in inputStream to close
   */
    public static void closeStream(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (final IOException ioe) {
                logB.error("FileUtils.closeStream : io close failure : ", ioe);
            }
        }
    }

    /**
   * Close an outputStream
   *
   * @param out outputStream to close
   */
    public static void closeStream(final OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (final IOException ioe) {
                logB.error("FileUtils.closeStream : io close failure : ", ioe);
            }
        }
    }

    /**
   * Returns an exisiting File for the given path
   *
   * @param path file path
   * @return File or null
   */
    private static File getExistingFile(final String path) {
        if (!JavaUtils.isEmpty(path)) {
            final File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
   * Returns an existing directory for the given path
   *
   * @param path directory path
   * @return directory or null
   */
    public static File getDirectory(final String path) {
        final File dir = getExistingFile(path);
        if (dir != null && dir.isDirectory()) {
            return dir;
        }
        return null;
    }

    /**
   * Returns an exisiting File for the given path
   *
   * @param path file path
   * @return File or null
   */
    public static File getFile(final String path) {
        final File file = getExistingFile(path);
        if (file != null && file.isFile()) {
            return file;
        }
        return null;
    }

    /**
   * Returns a Writer for the given file path and use the default writer buffer capacity
   *
   * @param absoluteFilePath absolute file path
   * @return Writer (buffered) or null
   */
    public static Writer openFile(final String absoluteFilePath) {
        return openFile(absoluteFilePath, DEFAULT_WRITE_BUFFER_SIZE);
    }

    /**
   * Returns a Writer for the given file path and use the given buffer capacity
   *
   * @param absoluteFilePath absolute file path
   * @param bufferSize write buffer capacity
   * @return Writer (buffered) or null
   */
    public static Writer openFile(final String absoluteFilePath, final int bufferSize) {
        if (!JavaUtils.isEmpty(absoluteFilePath)) {
            return openFile(new File(absoluteFilePath), bufferSize);
        }
        return null;
    }

    /**
   * Returns a Writer for the given file and use the default writer buffer capacity
   *
   * @param file file to write
   * @return Writer (buffered) or null
   */
    public static Writer openFile(final File file) {
        return openFile(file, DEFAULT_WRITE_BUFFER_SIZE);
    }

    /**
   * Returns a Writer for the given file and use the given buffer capacity
   *
   * @param file file to write
   * @param bufferSize write buffer capacity
   * @return Writer (buffered) or null
   */
    public static Writer openFile(final File file, final int bufferSize) {
        try {
            return new BufferedWriter(new FileWriter(file), bufferSize);
        } catch (final IOException ioe) {
            logB.error("FileUtils.openFile : io failure : ", ioe);
        }
        return null;
    }

    /**
   * Close the given writer
   *
   * @param w writer to close
   * @return null
   */
    public static Writer closeFile(final Writer w) {
        if (w != null) {
            try {
                w.close();
            } catch (final IOException ioe) {
                logB.error("FileUtils.closeFile : io close failure : ", ioe);
            }
        }
        return null;
    }

    /**
   * Returns a reader for the given file path and use the default read buffer capacity
   *
   * @param absoluteFilePath absolute file path
   * @return Reader (buffered) or null
   */
    public static Reader readFile(final String absoluteFilePath) {
        return readFile(absoluteFilePath, DEFAULT_READ_BUFFER_SIZE);
    }

    /**
   * Returns a reader for the given file path and use the given read buffer capacity
   *
   * @param absoluteFilePath absolute file path
   * @param bufferSize write buffer capacity
   * @return Reader (buffered) or null
   */
    public static Reader readFile(final String absoluteFilePath, final int bufferSize) {
        return readFile(getFile(absoluteFilePath), bufferSize);
    }

    /**
   * Returns a reader for the given file and use the default read buffer capacity
   *
   * @param file file to read
   * @return Reader (buffered) or null
   */
    public static Reader readFile(final File file) {
        return readFile(file, DEFAULT_READ_BUFFER_SIZE);
    }

    /**
   * Returns a reader for the given file and use the given read buffer capacity
   *
   * @param file file to read
   * @param bufferSize write buffer capacity
   * @return Reader (buffered) or null
   */
    public static Reader readFile(final File file, final int bufferSize) {
        try {
            return new BufferedReader(new FileReader(file), bufferSize);
        } catch (final IOException ioe) {
            logB.error("FileUtils.readFile : io failure : ", ioe);
        }
        return null;
    }

    /**
   * Close the given reader
   *
   * @param r reader to close
   * @return null
   */
    public static Reader closeFile(final Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (final IOException ioe) {
                logB.error("FileUtils.closeFile : io close failure : ", ioe);
            }
        }
        return null;
    }

    public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

    public static void compress(File[] files, File outFile) {
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
            for (int i = 0; i < files.length; i++) {
                FileInputStream in = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(files[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException e) {
        }
    }
}
