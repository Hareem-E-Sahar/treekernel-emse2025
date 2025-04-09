package org.ujac.util.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Name: FileUtils<br>
 * Description: Class providing common utility methods for file handling.
 * 
 * @author lauerc
 */
public class FileUtils {

    /** The I/O buffer size. */
    public static final int IO_BUFFER_SIZE = 2048;

    /**
   * Loads the contents of the given file.
   * @param file The file to load.
   * @return The loaded data from the file.
   * @exception IOException In case the loading of the file failed.
   */
    public static final byte[] loadFile(File file) throws IOException {
        if (file == null) {
            throw new IOException("The given file must not be null.");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(fis, IO_BUFFER_SIZE);
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            while (numRead > 0) {
                bos.write(buffer, 0, numRead);
                numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return bos.toByteArray();
    }

    /**
   * Copies the contents of the given file <code>src</code> into 
   * the given file <code>dst</code>.
   * @param src The file to copy from.
   * @param dst The file to copy to.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyFile(File src, File dst) throws IOException {
        if (src == null) {
            throw new IOException("The given source file must not be null.");
        }
        if (dst == null) {
            throw new IOException("The given destination file must not be null.");
        }
        FileOutputStream os = new FileOutputStream(dst);
        try {
            return copyToStream(src, os);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    /**
   * Copies the contents of the given file <code>src</code> into 
   * the given output stream <code>os</code>.
   * @param src The file to copy from.
   * @param os The stream to write to.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyToStream(File src, OutputStream os) throws IOException {
        if (src == null) {
            throw new IOException("The given source file must not be null.");
        }
        if (os == null) {
            throw new IOException("The given output stream must not be null.");
        }
        int numCopied = 0;
        FileInputStream is = new FileInputStream(src);
        try {
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            while (numRead != -1) {
                os.write(buffer, 0, numRead);
                numCopied += numRead;
                numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return numCopied;
    }

    /**
   * Copies the contents of the given byte array <code>bytes</code> into 
   * the given output file <code>file</code>.
   * @param bytes The bytes to copy.
   * @param dest The file to write to.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyFromBytes(byte[] bytes, File dest) throws IOException {
        if (dest == null) {
            throw new IOException("The given destination file must not be null.");
        }
        if (bytes == null) {
            throw new IOException("The given byte array must not be null.");
        }
        int numCopied = 0;
        OutputStream os = new FileOutputStream(dest);
        try {
            os.write(bytes, 0, bytes.length);
            numCopied = bytes.length;
        } finally {
            os.close();
        }
        return numCopied;
    }

    /**
   * Copies the contents of the given input stream <code>is</code> into 
   * the given output file <code>file</code>.
   * @param is The input stream to copy from.
   * @param dest The file to write to.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyFromStream(InputStream is, File dest) throws IOException {
        if (dest == null) {
            throw new IOException("The given destination file must not be null.");
        }
        if (is == null) {
            throw new IOException("The given input stream must not be null.");
        }
        int numCopied = 0;
        OutputStream os = new FileOutputStream(dest);
        try {
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            while (numRead != -1) {
                os.write(buffer, 0, numRead);
                numCopied += numRead;
                numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            os.close();
        }
        return numCopied;
    }

    /**
   * Copies the contents of the given input stream <code>is</code> into 
   * the given output file <code>file</code>.
   * @param is The input stream to copy from.
   * @param os The output stream to write to.
   * @param closeOs Tells, whether or not to close the output stream, false keeps it open.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyStreamToStream(InputStream is, OutputStream os, boolean closeOs) throws IOException {
        if (os == null) {
            throw new IOException("The given destination stream must not be null.");
        }
        if (is == null) {
            throw new IOException("The given input stream must not be null.");
        }
        int numCopied = 0;
        try {
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            while (numRead != -1) {
                os.write(buffer, 0, numRead);
                numCopied += numRead;
                numRead = is.read(buffer, 0, IO_BUFFER_SIZE);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (closeOs) {
                os.close();
            }
        }
        return numCopied;
    }

    /**
   * Copies the contents of the given reader <code>reader</code> into 
   * the given output file <code>file</code>.
   * @param reader The reader to copy from.
   * @param dest The file to write to.
   * @return The number of bytes copied.
   * @exception IOException In case the loading of the file failed.
   */
    public static final int copyFromReader(Reader reader, File dest) throws IOException {
        if (dest == null) {
            throw new IOException("The given destination file must not be null.");
        }
        if (reader == null) {
            throw new IOException("The given reader must not be null.");
        }
        int numCopied = 0;
        Writer out = new FileWriter(dest);
        try {
            char[] buffer = new char[IO_BUFFER_SIZE];
            int numRead = reader.read(buffer, 0, IO_BUFFER_SIZE);
            while (numRead != -1) {
                out.write(buffer, 0, numRead);
                numCopied += numRead;
                numRead = reader.read(buffer, 0, IO_BUFFER_SIZE);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            out.close();
        }
        return numCopied;
    }
}
