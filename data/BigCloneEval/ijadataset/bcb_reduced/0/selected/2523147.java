package org.iweb2gps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

/**
 * IO Utilities
 */
public class IOUtil {

    /**
     * Quietly closes the reader. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * reader and catch any {@link IOException} which may be thrown and ignore
     * it.
     * 
     * @param reader -
     *            Reader to close
     */
    public static void quietClose(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (java.io.IOException ex) {
        }
    }

    /**
     * Quietly closes the stream. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * stream and catch any {@link IOException} which may be thrown.
     * 
     * @param stream -
     *            Stream to close
     */
    public static void quietClose(InputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (java.io.IOException ex) {
        }
    }

    /**
     * Quietly closes the Writer. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * Writer and catch any ioexceptions which may be thrown.
     * 
     * @param writer -
     *            Writer to close
     */
    public static void quietClose(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (java.io.IOException ex) {
        }
    }

    /**
     * Quietly closes the stream. This avoids having to handle exceptions, and
     * then inside of the exception handling have a try catch block to close the
     * stream and catch any ioexceptions which may be thrown.
     * 
     * @param stream -
     *            Stream to close
     */
    public static void quietClose(OutputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (java.io.IOException ex) {
        }
    }

    /**
     * Get the path to a resource base on the package of given class.
     * 
     * @param c
     *            Class to get the package path too.
     * @param res
     *            Name of the resource to get the path of.
     * @return Returns the fully quilified path to a resource.
     */
    public static String getResourcePath(Class<?> c, String res) {
        assert c != null && StringUtil.isNotBlank(res);
        String classname = c.getName();
        String pkg = classname.substring(0, classname.lastIndexOf('.'));
        String fqres = pkg.replace('.', '/') + '/' + res;
        return fqres;
    }

    /**
     * Returns an input stream of the resource specified.
     * 
     * @return Returns an InputStream to the resource.
     */
    public static InputStream getResourceAsStream(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        InputStream ret = null;
        ClassLoader classLoader = clazz.getClassLoader();
        String name[] = { res, getResourcePath(clazz, res), "/" + getResourcePath(clazz, res) };
        for (int i = 0; ret == null && i < name.length; i++) {
            ret = classLoader.getResourceAsStream(name[i]);
        }
        return ret;
    }

    /**
     * Get the resource as a byte array.
     */
    public static byte[] getResourceAsBytes(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        InputStream ins = getResourceAsStream(clazz, res);
        if (ins == null) {
            throw new IllegalStateException("Resource not found: " + res);
        }
        return inputStreamToBytes(ins);
    }

    /**
     * Read the entire stream into a String and return it.
     */
    public static String getResourceAsString(Class<?> clazz, String res, Charset charset) {
        assert clazz != null && StringUtil.isNotBlank(res);
        String ret = null;
        InputStream ins = getResourceAsStream(clazz, res);
        if (ins != null) {
            try {
                InputStreamReader rdr = new InputStreamReader(ins, charset);
                ret = readerToString(rdr);
            } finally {
                quietClose(ins);
            }
        }
        return ret;
    }

    /**
     * Read the entire stream into a String and return it.
     */
    public static String getResourceAsString(Class<?> clazz, String res) {
        assert clazz != null && StringUtil.isNotBlank(res);
        return getResourceAsString(clazz, res, Charset.forName("UTF-8"));
    }

    /**
     * Takes a 'InputStream' and returns a byte array.
     */
    public static byte[] inputStreamToBytes(InputStream ins) {
        byte[] ret = null;
        try {
            int len = 0;
            byte[] buf = new byte[2048];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((len = ins.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            ret = out.toByteArray();
        } catch (IOException e) {
        }
        return ret;
    }

    /**
     * Takes a 'Reader' and returns the contents as a string.
     * 
     * @param rdr
     *            Producer for the string data.
     * @return Null if the 'Reader' is broken or empty otherwise the contents as
     *         a string.
     */
    public static String readerToString(Reader rdr) {
        String ret = null;
        try {
            int len = 0;
            char[] buf = new char[2048];
            StringWriter wrt = new StringWriter();
            while ((len = rdr.read(buf)) != -1) {
                wrt.write(buf, 0, len);
            }
            ret = wrt.toString();
        } catch (UnsupportedEncodingException e) {
        } catch (IOException e) {
        }
        return ret;
    }

    /**
     * Copies a file to a destination.
     * 
     * @param src
     *            The source must be a file
     * @param dest
     *            This can be a directory or a file.
     * @return True if succeeded otherwise false.
     */
    public static boolean copyFile(File src, File dest) throws IOException {
        boolean ret = true;
        if (src == null || dest == null || !src.isFile()) {
            throw new FileNotFoundException();
        }
        if (dest.isDirectory()) {
            String name = src.getName();
            dest = new File(dest, name);
        }
        FileInputStream fis = null;
        FileOutputStream fout = null;
        try {
            fis = new FileInputStream(src);
            fout = new FileOutputStream(dest);
            ret = copyFile(fis, fout) > 0;
        } finally {
            quietClose(fis);
            quietClose(fout);
        }
        return ret;
    }

    /**
     * Copies one file to another.
     * 
     * @return total bytes copied.
     */
    public static long copyFile(InputStream fis, OutputStream fos) throws IOException {
        long ret = 0;
        byte[] bytes = new byte[8 * 1024];
        for (int rd = fis.read(bytes); rd != -1; rd = fis.read(bytes)) {
            fos.write(bytes, 0, rd);
            ret += rd;
        }
        return ret;
    }

    /**
     * Calculates the CRC32 checksum of the specified file.
     * 
     * @param fileName -
     *            the path to the file on which to calculate the checksum
     */
    public static long checksum(String fileName) throws IOException, FileNotFoundException {
        return (checksum(new File(fileName)));
    }

    public static long checksum(File file) throws java.io.IOException, FileNotFoundException {
        FileInputStream fis = null;
        byte[] bytes = new byte[16384];
        int len;
        try {
            fis = new FileInputStream(file);
            CRC32 chkSum = new CRC32();
            len = fis.read(bytes);
            while (len != -1) {
                chkSum.update(bytes, 0, len);
                len = fis.read(bytes);
            }
            return chkSum.getValue();
        } finally {
            quietClose(fis);
        }
    }

    /**
     * Reads an entire file and returns the bytes.
     * 
     * @param close
     *            if true, close when finished reading.
     * @return file bytes.
     */
    public static byte[] readInputStreamBytes(InputStream is, boolean close) throws IOException {
        byte[] bytes = null;
        if (is != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            try {
                int bytesRead = 0;
                byte[] buf = new byte[1024];
                while ((bytesRead = is.read(buf)) != -1) {
                    bout.write(buf, 0, bytesRead);
                }
                bytes = bout.toByteArray();
            } finally {
                if (close) {
                    quietClose(is);
                }
            }
        }
        return bytes;
    }

    /**
     * Recursively delete all the files in a directory and the directory.
     */
    public static void delete(File f) {
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                for (int x = 0; x < fs.length; x++) {
                    delete(fs[x]);
                }
                f.delete();
            } else if (f.isFile() && !f.delete()) {
                final String msg = "Failed to delete: " + f;
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * Count the total number of files to convert.
     */
    public static int recursiveCount(File f) {
        int ret = 0;
        if (f.isDirectory()) {
            for (File tmp : f.listFiles()) {
                ret += recursiveCount(tmp);
            }
        } else {
            ret = 1;
        }
        return ret;
    }
}
