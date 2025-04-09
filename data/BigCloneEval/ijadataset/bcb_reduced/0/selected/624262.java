package org.mili.core.io;

import java.io.*;
import java.net.*;
import java.util.zip.*;
import org.mili.core.logging.*;

/**
 * This utility class defines some useful methods to operate wih files.
 *
 * @author Michael Lieshoff
 */
public final class FileUtil {

    private static Logger log = DefaultLogger.getLogger(FileUtil.class);

    /**
     * Instantiates a new file util.
     */
    public FileUtil() {
    }

    /**
     * Creates an input stream from a filename. The order of search the file is as first the
     * filesystem then the classloader.
     *
     * @param filename name of file.
     * @return input stream.
     * @throws FileNotFoundException if the file cannot be found.
     * @throws IOException if other io exceptions occurs.
     */
    public static InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return getInputStream(filename, FileAccessOrder.FilesystemThenClassloader);
    }

    /**
     * Creates an input stream from a filename. The order of search the file is defined by the
     * file access order.
     *
     * @param filename name of file.
     * @param fao the file access order.
     * @return input stream.
     * @throws FileNotFoundException if the file cannot be found.
     * @throws IOException if other io exceptions occurs.
     */
    public static InputStream getInputStream(String filename, FileAccessOrder fao) throws FileNotFoundException, IOException {
        return getInputStream(filename, fao, FileUtil.class.getClassLoader());
    }

    /**
     * Creates an input stream from a filename. The order of search the file is defined by the
     * file access order. For classloader search the defines classloader will be used.
     *
     * @param filename name of file.
     * @param fao the file access order.
     * @param cl the classloader.
     * @return input stream.
     * @throws FileNotFoundException if the file cannot be found.
     * @throws IOException if other io exceptions occurs.
     */
    public static InputStream getInputStream(String filename, FileAccessOrder fao, ClassLoader cl) throws FileNotFoundException, IOException {
        switch(fao) {
            case Filesystem:
                log.debug("load[", filename, "] from filesystem.");
                return _readFromFS(filename);
            case Classloader:
                log.debug("load[", filename, "] from classloader.");
                return _readFromCL(filename, cl);
            case ClassloaderThenFilesystem:
                if (findFromCL(filename, cl)) {
                    log.debug("load[", filename, "] from classloader.");
                    return _readFromCL(filename, cl);
                } else if (new File(filename).exists()) {
                    log.debug("load[", filename, "] from filesystem.");
                    return _readFromFS(filename);
                }
            case FilesystemThenClassloader:
            default:
                File f = new File(filename);
                if (f.exists()) {
                    log.debug("load[", filename, "] from filesystem.");
                    return _readFromFS(filename);
                } else if (findFromCL(filename, cl)) {
                    log.debug("load[", filename, "] from classloader.");
                    return _readFromCL(filename, cl);
                }
        }
        throw new FileNotFoundException("File[filename=" + filename + ", file-access-order=" + fao + ", class-loader=" + cl.getClass().getName() + ", file-base=" + (new File("").getAbsolutePath()) + "] not found !");
    }

    /**
     * Zip file.
     *
     * @param source the source
     * @param target the target
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void zipFile(File source, File target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(source);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipEntry entry = new ZipEntry(source.getCanonicalFile().getName());
        zos.putNextEntry(entry);
        byte[] barray = new byte[1024];
        int bytes;
        while ((bytes = bis.read(barray, 0, 1024)) > -1) {
            zos.write(barray, 0, bytes);
        }
        bis.close();
        fis.close();
        zos.flush();
        zos.close();
        fos.close();
    }

    private static boolean findFromCL(String filename, ClassLoader cl) {
        URL u = cl.getResource(getClFilename(filename));
        return u != null;
    }

    private static InputStream _readFromCL(String filename, ClassLoader cl) throws FileNotFoundException, IOException {
        InputStream is = cl.getResourceAsStream(getClFilename(filename));
        if (is == null) {
            throw new FileNotFoundException("File[filename=" + filename + ", class-loader=" + cl.getClass().getName() + ", file-base=" + (new File("").getAbsolutePath()) + "] not found !");
        }
        return is;
    }

    private static InputStream _readFromFS(String filename) throws FileNotFoundException, IOException {
        File f = new File(filename);
        return new FileInputStream(f);
    }

    private static String getClFilename(String filename) {
        int i = filename.indexOf(":");
        String fn = filename.replace("\\", "/");
        if (i > 0) {
            fn = fn.substring(i + 1);
        }
        i = 0;
        if (fn.startsWith("./")) {
            i = 2;
        } else if (fn.startsWith("/") || fn.startsWith("\\")) {
            i = 1;
        }
        if (i == 0) {
            return fn;
        }
        return fn.substring(i);
    }
}
