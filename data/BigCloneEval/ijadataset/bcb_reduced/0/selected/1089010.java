package net.emotivecloud.vrmm.vtm.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.hadoop.io.compress.bzip2.CBZip2InputStream;
import org.apache.hadoop.io.compress.bzip2.CBZip2OutputStream;

/**
 * Compresses a file.
 * @author goirix
 */
public class Compressor {

    public static String compressFile(String fileName) throws IOException {
        String ret = null;
        File src = new File(fileName);
        File dst = new File(fileName + ".bz2");
        CBZip2OutputStream output = new CBZip2OutputStream(new FileOutputStream(dst));
        FileInputStream input = new FileInputStream(src);
        copy(input, output);
        input.close();
        output.close();
        ret = fileName + ".bz2";
        return ret;
    }

    public static String compressFileGz(String fileName) throws IOException {
        String ret = null;
        Compressor.compressFileGz(fileName, fileName + ".gz");
        ret = fileName + ".gz";
        return ret;
    }

    public static void compressFileGz(String src, String dst) throws IOException {
        GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(new File(dst)));
        FileInputStream input = new FileInputStream(new File(src));
        copy(input, output);
        input.close();
        output.close();
    }

    public static String compressFileZip(String fileName) throws IOException {
        String ret = null;
        Compressor.compressFileGz(fileName, fileName + ".zip");
        ret = fileName + ".zip";
        return ret;
    }

    public static void compressFileZip(String src, String dst) throws IOException {
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(new File(dst)));
        output.setLevel(Deflater.BEST_SPEED);
        FileInputStream input = new FileInputStream(new File(src));
        output.putNextEntry(new ZipEntry(src));
        copy(input, output);
        input.close();
        output.close();
    }

    public static String uncompressFile(String fileName) throws IOException {
        String fileNameDst = null;
        if (fileName.endsWith(".bz2")) fileNameDst = fileName.substring(0, fileName.lastIndexOf(".bz2")); else fileNameDst += ".orig";
        File source = new File(fileName);
        File destination = new File(fileNameDst);
        FileOutputStream output = new FileOutputStream(destination);
        CBZip2InputStream input = new CBZip2InputStream(new FileInputStream(source));
        copy(input, output);
        input.close();
        output.close();
        return fileNameDst;
    }

    public static String uncompressFileGz(String fileName) throws IOException {
        String fileNameDst = null;
        if (fileName.endsWith(".gz")) fileNameDst = fileName.substring(0, fileName.lastIndexOf(".gz")); else fileNameDst += ".orig";
        Compressor.uncompressFileGz(fileName, fileNameDst);
        return fileNameDst;
    }

    public static void uncompressFileGz(String src, String dst) throws FileNotFoundException, IOException {
        GZIPInputStream input = new GZIPInputStream(new FileInputStream(new File(src)));
        FileOutputStream output = new FileOutputStream(new File(dst));
        copy(input, output);
        input.close();
        output.close();
    }

    public static String uncompressFileZip(String fileName) throws IOException {
        String fileNameDst = null;
        if (fileName.endsWith(".zip")) fileNameDst = fileName.substring(0, fileName.lastIndexOf(".zip")); else fileNameDst += ".orig";
        Compressor.uncompressFileZip(fileName, fileNameDst);
        return fileNameDst;
    }

    public static void uncompressFileZip(String src, String dst) throws FileNotFoundException, IOException {
        ZipInputStream input = new ZipInputStream(new FileInputStream(new File(src)));
        ZipEntry entry;
        while ((entry = input.getNextEntry()) != null) {
            System.out.println("Extracting: " + entry + " -> " + dst);
            FileOutputStream fos = new FileOutputStream(dst);
            copy(input, fos);
            fos.flush();
            fos.close();
        }
        input.close();
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        final byte[] buffer = new byte[8024];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }
}
