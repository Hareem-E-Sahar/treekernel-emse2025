package net.sourceforge.jaulp.file.write;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.jaulp.file.FileConst;
import net.sourceforge.jaulp.file.read.ReadFileUtils;
import net.sourceforge.jaulp.io.StreamUtils;

/**
 * The Class WriteFileUtils provides methods for writing in files.
 *
 * @version 1.0
 * @author Asterios Raptis
 */
public class WriteFileUtils {

    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WriteFileUtils.class.getName());

    /**
     * Gets the output stream from a File object.
     *
     * @param file
     *            the file.
     * @return the output stream.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static OutputStream getOutputStream(final File file) throws IOException {
        return getOutputStream(file, false);
    }

    /**
     * Gets the output stream from a File object.
     *
     * @param file the file
     * @param createFile If true and the file does not exist it will be create a new
     * file.
     * @return the output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static OutputStream getOutputStream(final File file, final boolean createFile) throws IOException {
        OutputStream os = null;
        BufferedOutputStream bos = null;
        if (file.exists()) {
            os = new FileOutputStream(file);
        } else {
            if (createFile) {
                file.createNewFile();
                os = new FileOutputStream(file);
            } else {
                throw new FileNotFoundException("File " + file.getName() + "does not exist.");
            }
        }
        bos = new BufferedOutputStream(os);
        return bos;
    }

    /**
     * Gets a Writer from the given file object.
     *
     * @param file
     *            the file.
     * @return the Writer.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Writer getWriter(final File file) throws IOException {
        return getWriter(file, null, false);
    }

    /**
     * Gets a Writer from the given file object.
     *
     * @param file
     *            the file
     * @param encoding
     *            The encoding from the file.
     * @param createFile
     *            the create file
     * @return the Writer.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static Writer getWriter(final File file, final String encoding, final boolean createFile) throws IOException {
        PrintWriter printWriter = null;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        if (file.exists()) {
            fos = new FileOutputStream(file);
        } else {
            if (createFile) {
                file.createNewFile();
                fos = new FileOutputStream(file);
            } else {
                throw new FileNotFoundException("File " + file.getName() + "does not exist.");
            }
        }
        bos = new BufferedOutputStream(fos);
        if (null == encoding) {
            osw = new OutputStreamWriter(bos);
        } else {
            osw = new OutputStreamWriter(bos, encoding);
        }
        printWriter = new PrintWriter(osw);
        return printWriter;
    }

    /**
     * Writes the source file with the best performance to the destination file.
     *
     * @param srcfile
     *            The source file.
     * @param destFile
     *            The destination file.
     */
    public static void readSourceFileAndWriteDestFile(final String srcfile, final String destFile) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcfile);
            fos = new FileOutputStream(destFile);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(fos);
            final int availableLength = bis.available();
            final byte[] totalBytes = new byte[availableLength];
            bis.read(totalBytes, 0, availableLength);
            bos.write(totalBytes, 0, availableLength);
            bos.flush();
            bis.close();
            bos.close();
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "readSourceFileAndWriteDestFile failed...\n" + e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "readSourceFileAndWriteDestFile failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeInputStream(bis);
            StreamUtils.closeOutputStream(bos);
            StreamUtils.closeInputStream(fis);
            StreamUtils.closeOutputStream(fos);
        }
    }

    /**
     * Saves a byte array to the given file.
     *
     * @param data
     *            The byte array to be saved.
     * @param file
     *            The file to save the byte array.
     */
    public static void storeByteArrayToFile(final byte[] data, final File file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.flush();
            bos.close();
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "storeByteArrayToFile failed...\n" + e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "storeByteArrayToFile failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeOutputStream(bos);
            StreamUtils.closeOutputStream(fos);
        }
    }

    /**
     * The Method string2File(File, String) writes the String to the File.
     *
     * @param file
     *            The File to write the String.
     * @param string2write
     *            The String to write into the File.
     * @return The Method return true if the String was write successfull to the
     *         file otherwise false.
     */
    public static boolean string2File(final File file, final String string2write) {
        return writeStringToFile(file, string2write, null);
    }

    /**
     * The Method string2File() writes a String to the file.
     *
     * @param string2write
     *            The String to write into the file.
     * @param nameOfFile
     *            The path to the file and name from the file from where we want
     *            to write the String.
     */
    public static void string2File(final String string2write, final String nameOfFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(nameOfFile));
            bufferedWriter.write(string2write);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "string2File failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeWriter(bufferedWriter);
        }
    }

    /**
     * The Method write2File() reads from an opened Reader and writes it to the
     * opened Writer.
     *
     * @param reader
     *            The opened Reader.
     * @param writer
     *            The opened Writer.
     * @param closeStream
     *            If true then close the outputStream otherwise keep open.
     */
    public static void write2File(final Reader reader, final Writer writer, final boolean closeStream) {
        int byt;
        try {
            while ((byt = reader.read()) != -1) {
                writer.write(byt);
            }
            if (closeStream) {
                reader.close();
                writer.close();
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "write2File failed...\n" + e.getMessage(), e);
        } finally {
            if (closeStream) {
                StreamUtils.closeReader(reader);
                StreamUtils.closeWriter(writer);
            }
        }
    }

    /**
     * The Method write2File(String, String) copys a file from one filename to
     * another.
     *
     * @param inputFile
     *            The Name from the File to read and copy.
     * @param outputFile
     *            The Name from the File to write into.
     */
    public static void write2File(final String inputFile, final String outputFile) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(inputFile);
            fos = new FileOutputStream(outputFile);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(fos);
            StreamUtils.writeInputStreamToOutputStream(bis, bos, true);
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "write2File failed...\n" + e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "write2File failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeInputStream(fis);
            StreamUtils.closeOutputStream(fos);
            StreamUtils.closeInputStream(bis);
            StreamUtils.closeOutputStream(bos);
        }
    }

    /**
     * The Method write2File() writes the File into the PrintWriter.
     *
     * @param inputFile
     *            The Name from the File to read and copy.
     * @param writer
     *            The PrintWriter to write into.
     * @param closeWriter
     *            If true then close the outputStream otherwise keep open.
     */
    public static void write2File(final String inputFile, final Writer writer, final boolean closeWriter) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            write2File(bufferedReader, writer, closeWriter);
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "write2File failed...\n" + e.getMessage(), e);
        } finally {
            if (closeWriter) {
                StreamUtils.closeReader(bufferedReader);
                StreamUtils.closeWriter(writer);
            }
        }
    }

    /**
     * The Method write2FileWithBuffer() copy the content from one file to
     * another. It use a buffer as the name says.
     *
     * @param inputFile
     *            The Path to the File and name from the file from where we
     *            read.
     * @param outputFile
     *            The Path to the File and name from the file from where we want
     *            to write.
     */
    public static void write2FileWithBuffer(final String inputFile, final String outputFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = ReadFileUtils.getInputStream(new File(inputFile));
            outputStream = WriteFileUtils.getOutputStream(new File(outputFile));
            int counter = 0;
            final byte byteArray[] = new byte[FileConst.BLOCKSIZE];
            while ((counter = inputStream.read(byteArray)) != -1) {
                outputStream.write(byteArray, 0, counter);
            }
            inputStream.close();
            outputStream.close();
            inputStream = null;
            outputStream = null;
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "write2FileWithBuffer failed...\n" + e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "write2FileWithBuffer failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeInputStream(inputStream);
            StreamUtils.closeOutputStream(outputStream);
        }
    }

    /**
     * Writes the given byte array to the given file.
     *
     * @param file
     *            The file.
     * @param byteArray
     *            The byte array.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void writeByteArrayToFile(final File file, final byte[] byteArray) throws IOException {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            fos.close();
            bos.close();
            fos = null;
            bos = null;
        } catch (final FileNotFoundException ex) {
            throw ex;
        } catch (final IOException ex) {
            throw ex;
        } finally {
            StreamUtils.closeOutputStream(fos);
            StreamUtils.closeOutputStream(bos);
        }
    }

    /**
     * Writes the given byte array to a file.
     *
     * @param filename
     *            The filename from the file.
     * @param byteArray
     *            The byte array.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void writeByteArrayToFile(final String filename, final byte[] byteArray) throws IOException {
        final File file = new File(filename);
        writeByteArrayToFile(file, byteArray);
    }

    /**
     * Writes the input from the collection into the file.
     *
     * @param collection
     *            The collection to write to file.
     * @param output
     *            The output-file.
     */
    public static void writeLinesToFile(final Collection<String> collection, final File output) {
        final StringBuffer sb = new StringBuffer();
        for (final Iterator<String> iter = collection.iterator(); iter.hasNext(); ) {
            final String element = iter.next();
            sb.append(element);
            sb.append("\n");
        }
        string2File(output, sb.toString());
    }

    /**
     * Writes the input from the collection into the file.
     *
     * @param output
     *            The file to write the lines.
     * @param input
     *            The list with the input data.
     * @param encoding
     *            The encoding.
     */
    public static void writeLinesToFile(final File output, final List<String> input, final String encoding) {
        PrintWriter out = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        final String lineSeparator = System.getProperty("line.separator");
        try {
            fos = new FileOutputStream(output);
            if (null == encoding) {
                osw = new OutputStreamWriter(fos);
            } else {
                osw = new OutputStreamWriter(fos, encoding);
            }
            out = new PrintWriter(osw);
            final int size = input.size();
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < size; i++) {
                final String entry = input.get(i);
                sb.append(entry + lineSeparator);
            }
            out.write(sb.toString());
            out.close();
            osw.close();
            fos.close();
            out = null;
            osw = null;
            fos = null;
        } catch (final UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "writeLinesToFile failed...\n" + e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "writeLinesToFile failed...\n" + e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "writeLinesToFile failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeOutputStream(fos);
            StreamUtils.closeWriter(osw);
            StreamUtils.closeWriter(out);
        }
    }

    /**
     * The Method writeProperties2File(String, Properties) writes the Properties
     * to the file.
     *
     * @param filename
     *            The filename from the file to write the properties.
     * @param properties
     *            The properties.
     */
    public static void writeProperties2File(final String filename, final Properties properties) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            properties.store(fos, null);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "writeProperties2File failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeOutputStream(fos);
        }
    }

    /**
     * The Method writeStringToFile(File, String, String) writes the String to
     * the File.
     *
     * @param file
     *            The File to write the String.
     * @param string2write
     *            The String to write into the File.
     * @param encoding
     *            The encoding from the file.
     * @return The Method return true if the String was write successfull to the
     *         file otherwise false.
     */
    public static boolean writeStringToFile(final File file, final String string2write, final String encoding) {
        boolean iswritten = true;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            if (null == encoding) {
                osw = new OutputStreamWriter(bos);
            } else {
                osw = new OutputStreamWriter(bos, encoding);
            }
            osw.write(string2write);
            osw.close();
            bos.close();
            fos.close();
            osw = null;
            bos = null;
            fos = null;
        } catch (final FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "writeStringToFile failed...\n" + e.getMessage(), e);
            iswritten = false;
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "writeStringToFile failed...\n" + e.getMessage(), e);
        } finally {
            StreamUtils.closeWriter(osw);
            StreamUtils.closeOutputStream(bos);
            StreamUtils.closeOutputStream(fos);
        }
        return iswritten;
    }
}
