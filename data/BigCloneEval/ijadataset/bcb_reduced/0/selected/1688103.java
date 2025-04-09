package net.sf.syncopate.proxy.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.Reader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static void waitForInput(InputStream in) throws IOException {
        if (in.available() <= 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void waitForInput(Reader in) throws IOException {
        if (in.ready()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
   * Reads byte data in chunks of a specified size from one stream and writes
   * it into another stream.
   *
   * @param input  The InputStream from which to read the data
   * @param output The OutputStream to which to write the data
   * @throws IOException
   */
    public static void copy(final InputStream input, final OutputStream output) throws IOException {
        if (input == null) {
            return;
        }
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        waitForInput(input);
        while ((input.available() > 0) && (n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            waitForInput(input);
        }
    }

    /**
   * Reads character data in chunks of a specified size from a Reader and
   * writes it into a Writer.
   *
   * @param input  The Reader from which to read the data
   * @param output The Writer to which to write the data
   * @throws IOException
   */
    public static void copy(final Reader input, final Writer output) throws IOException {
        if (input == null) {
            return;
        }
        final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        waitForInput(input);
        while (input.ready() && (n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            waitForInput(input);
        }
    }

    private static void copy(final File inFile, final File outFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inFile);
            out = new FileOutputStream(outFile);
            copy(in, out);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
   * This method is used to completely delete all contents of a directory
   * including all files and (recursively) subdirectories, but not the
   * (specified) directory itself.
   *
   * @param directory The directory whose contents are to be deleted
   * @return <b>true</b> if the directory contains no more files after this
   *         operation; <b>false</b> otherwise (i.e. if some file could not be
   *         deleted for some reason)
   */
    public static boolean makeEmpty(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return true;
        }
        boolean retVal = true;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                retVal &= makeEmpty(file);
            }
            retVal &= file.delete();
        }
        return retVal;
    }

    /**
   * This method is used to recursively copy the contents of one
   * complete directory tree from one location in the file system
   * to another.
   *
   * @param srcDir The source directory whose contents are to be
   *        copied
   * @param targetDir The target directory (to which the contents
   *        of the source directory are copied)
   * @throws IOException
   */
    public static void copyTree(File srcDir, File targetDir) throws IOException {
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            File outFile = new File(targetDir, file.getName());
            if (file.isFile()) {
                copy(file, outFile);
            } else if (file.isDirectory()) {
                outFile.mkdir();
                copyTree(file, outFile);
            }
        }
    }

    /**
   * This method constructs a <code>Reader</code> object of a <code>File</code>
   * for a given <code>Charset</code>.
   *
   * @param file The file for which the <code>Reader</code> is to be
   *        constructed
   * @param charset The <code>Charset</code> of the <code>Reader</code>
   *        to be constructed
   * @return A <code>Reader</code> object for the specified <code>File</code>
   *         with the specified <code>Charset</code>
   * @throws FileNotFoundException
   */
    public static Reader getReader(File file, Charset charset) throws FileNotFoundException {
        if (charset != null) {
            FileInputStream stream = new FileInputStream(file);
            return new InputStreamReader(stream, charset);
        } else {
            return new FileReader(file);
        }
    }

    /**
   * This method constructs a <code>Reader</code> object of a <code>File</code>
   * for a given <code>Charset</code>.
   *
   * @param filename The name of the file for which the <code>Reader</code>
   *        is to be constructed
   * @param charset The <code>Charset</code> of the <code>Reader</code>
   *        to be constructed
   * @return A <code>Reader</code> object for the specified <code>File</code>
   *         with the specified <code>Charset</code>
   * @throws FileNotFoundException
   */
    public static Reader getReader(String filename, Charset charset) throws FileNotFoundException {
        if (charset != null) {
            FileInputStream stream = new FileInputStream(filename);
            return new InputStreamReader(stream, charset);
        } else {
            return new FileReader(filename);
        }
    }

    /**
   * This method constructs a <code>Writer</code> object of a <code>File</code>
   * for a given <code>Charset</code>.
   *
   * @param file The file for which the <code>Writer</code> is to be
   *        constructed
   * @param charset The <code>Charset</code> of the <code>Writer</code>
   *        to be constructed
   * @return A <code>Writer</code> object for the specified <code>File</code>
   *         with the specified <code>Charset</code>
   * @throws FileNotFoundException
   */
    public static Writer getWriter(File file, Charset charset) throws IOException {
        if (charset != null) {
            FileOutputStream stream = new FileOutputStream(file);
            return new OutputStreamWriter(stream, charset);
        } else {
            return new FileWriter(file);
        }
    }

    /**
   * This method constructs a <code>Writer</code> object of a <code>File</code>
   * for a given <code>Charset</code>.
   *
   * @param filename The name of the file for which the <code>Writer</code>
   *        is to be constructed
   * @param charset The <code>Charset</code> of the <code>Writer</code>
   *        to be constructed
   * @return A <code>Writer</code> object for the specified <code>File</code>
   *         with the specified <code>Charset</code>
   * @throws FileNotFoundException
   */
    public static Writer getWriter(String filename, Charset charset) throws IOException {
        if (charset != null) {
            FileOutputStream stream = new FileOutputStream(filename);
            return new OutputStreamWriter(stream, charset);
        } else {
            return new FileWriter(filename);
        }
    }

    private IOUtils() {
    }
}
