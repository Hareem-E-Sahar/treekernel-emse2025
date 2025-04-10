package org.tranche.util;

import SevenZip.LzmaAlone;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;

/**
 * <p>Helper methods for using various compression encodings. Includes methods that use temporary files and methods that don't.</p>
 * @author Jayson Falkner - jfalkner@umich.edu
 * @author Bryan Smith - bryanesmith@gmail.com
 * @author James "Augie" Hill - augman85@gmail.com
 */
public class CompressionUtil {

    /**
     * 
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static final File tgzCompress(File directory) throws IOException {
        File file = tarCompress(directory);
        file = gzipCompress(file);
        File renameTo = new File(file.getAbsolutePath().replace(".gzip", ".tgz"));
        IOUtil.renameFallbackCopy(file, renameTo);
        return renameTo;
    }

    /**
     * 
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static final File tbzCompress(File directory) throws IOException {
        File file = tarCompress(directory);
        file = bzip2Compress(file);
        File renameTo = new File(file.getAbsolutePath().replace(".bzip2", ".tbz"));
        IOUtil.renameFallbackCopy(file, renameTo);
        return renameTo;
    }

    /**
     * 
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static final File tarCompress(File directory) throws IOException {
        File file = null;
        FileOutputStream fos = null;
        TarOutputStream tos = null;
        try {
            file = TempFileUtil.createTemporaryFile(".tar");
            fos = new FileOutputStream(file);
            tos = new TarOutputStream(fos);
            for (File subFile : directory.listFiles()) {
                TarEntry ze = new TarEntry(subFile.getName());
                byte[] bytes = IOUtil.getBytes(subFile);
                ze.setSize(bytes.length);
                tos.putNextEntry(ze);
                tos.write(bytes);
                tos.closeEntry();
            }
        } finally {
            IOUtil.safeClose(tos);
            IOUtil.safeClose(fos);
        }
        return file;
    }

    /**
     * 
     * @param zipFile
     * @param directoryName
     * @return
     * @throws java.io.IOException
     */
    public static final File tarDecompress(File file, String directoryName) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        TarInputStream tis = null;
        File directory = null;
        try {
            directory = TempFileUtil.createTemporaryDirectory(directoryName);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            tis = new TarInputStream(bis);
            for (TarEntry te = tis.getNextEntry(); te != null; te = tis.getNextEntry()) {
                if (te.isDirectory()) {
                    continue;
                }
                File tempFile = new File(directory, te.getName());
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(tempFile);
                    bos = new BufferedOutputStream(fos);
                    IOUtil.getBytes(tis, bos);
                } finally {
                    IOUtil.safeClose(bos);
                    IOUtil.safeClose(fos);
                }
            }
        } finally {
            IOUtil.safeClose(tis);
            IOUtil.safeClose(bis);
            IOUtil.safeClose(fis);
        }
        return directory;
    }

    /**
     * <p>Zip compress/archive a single zipFile or directory.</p>
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static final File zipCompress(File file) throws IOException {
        File zipFile = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            String name = file.getName() + ".zip";
            zipFile = TempFileUtil.createTempFileWithName(name);
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    ZipEntry ze = new ZipEntry(subFile.getName());
                    zos.putNextEntry(ze);
                    zos.write(IOUtil.getBytes(subFile));
                    zos.flush();
                }
            } else {
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                zos.write(IOUtil.getBytes(file));
                zos.flush();
            }
        } finally {
            IOUtil.safeClose(zos);
            IOUtil.safeClose(fos);
        }
        return zipFile;
    }

    /**
     * 
     * @param zipFile
     * @return
     * @throws java.io.IOException
     */
    public static final File zipDecompress(File file, String directoryName) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ZipInputStream zis = null;
        File directory = null;
        try {
            directory = TempFileUtil.createTemporaryDirectory(directoryName);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            zis = new ZipInputStream(bis);
            for (ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                if (ze.isDirectory()) {
                    continue;
                }
                File tempFile = new File(directory, ze.getName());
                tempFile.createNewFile();
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(tempFile);
                    bos = new BufferedOutputStream(fos);
                    IOUtil.getBytes(zis, bos);
                } finally {
                    IOUtil.safeClose(bos);
                    IOUtil.safeClose(fos);
                }
            }
        } finally {
            IOUtil.safeClose(zis);
            IOUtil.safeClose(bis);
            IOUtil.safeClose(fis);
        }
        return directory;
    }

    /**
     * <p>GZIPs the in memory and returns the compressed bytes. Avoids any time penalties associated with making/using files.</p>
     * @param dataBytes
     * @param padding
     * @return
     * @throws java.io.IOException
     */
    public static final byte[] gzipCompress(byte[] dataBytes, byte[] padding) throws IOException {
        ByteArrayInputStream fis = null;
        ByteArrayOutputStream fos = null;
        GZIPOutputStream gos = null;
        try {
            fis = new ByteArrayInputStream(dataBytes);
            fos = new ByteArrayOutputStream();
            gos = new GZIPOutputStream(fos);
            IOUtil.getBytes(fis, gos);
            gos.write(padding);
        } finally {
            IOUtil.safeClose(fis);
            IOUtil.safeClose(gos);
            IOUtil.safeClose(fos);
        }
        return fos.toByteArray();
    }

    /**
     * <p>Decompresses the input bytes assuming that it is GZIP'd. Completely avoids the use of temporary files, which saves associated time.</p>
     * @param dataBytes
     * @return
     * @throws java.io.IOException
     */
    public static final byte[] gzipDecompress(byte[] dataBytes) throws IOException {
        ByteArrayInputStream fis = null;
        ByteArrayOutputStream fos = null;
        GZIPInputStream gis = null;
        try {
            fis = new ByteArrayInputStream(dataBytes);
            fos = new ByteArrayOutputStream();
            gis = new GZIPInputStream(fis);
            IOUtil.getBytes(gis, fos);
        } finally {
            IOUtil.safeClose(gis);
            IOUtil.safeClose(fis);
            IOUtil.safeClose(fos);
        }
        return fos.toByteArray();
    }

    /**
     * <p>GZIPs the input zipFile and returns a pointer to a zipFile that is GZIP compressed.</p>
     * @param input
     * @return
     * @throws java.io.IOException
     */
    public static final File gzipCompress(File input) throws IOException {
        File gzip = TempFileUtil.createTemporaryFile(".gzip");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPOutputStream gos = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(gzip);
            gos = new GZIPOutputStream(fos);
            IOUtil.getBytes(fis, gos);
        } finally {
            IOUtil.safeClose(fis);
            IOUtil.safeClose(gos);
            IOUtil.safeClose(fos);
        }
        return gzip;
    }

    /**
     * <p>Decompresses the input zipFile assuming that it is GZIP'd.</p>
     * @param input
     * @return
     * @throws java.io.IOException
     */
    public static final File gzipDecompress(File input) throws IOException {
        File gzip = TempFileUtil.createTemporaryFile();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPInputStream gis = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(gzip);
            gis = new GZIPInputStream(fis);
            IOUtil.getBytes(gis, fos);
        } finally {
            IOUtil.safeClose(gis);
            IOUtil.safeClose(fis);
            IOUtil.safeClose(fos);
        }
        return gzip;
    }

    /**
     * <p>bzip2 compresses the input zipFile and returns a reference to the compressed zipFile.</p>
     * @param input
     * @return
     * @throws java.io.IOException
     */
    public static final File bzip2Compress(File input) throws IOException {
        File compressed = TempFileUtil.createTemporaryFile(".bzip2");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        CBZip2OutputStream gos = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(compressed);
            gos = new CBZip2OutputStream(fos);
            IOUtil.getBytes(fis, gos);
        } finally {
            IOUtil.safeClose(fis);
            IOUtil.safeClose(gos);
            IOUtil.safeClose(fos);
        }
        return compressed;
    }

    /**
     * <p>Decompresses the input zipFile assuming that it is bzip2 compressed.</p>
     * @param input
     * @return
     * @throws java.io.IOException
     */
    public static final File bzip2Decompress(File input) throws IOException {
        File decompressed = TempFileUtil.createTemporaryFile();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        CBZip2InputStream gis = null;
        try {
            fis = new FileInputStream(input);
            fos = new FileOutputStream(decompressed);
            gis = new CBZip2InputStream(fis);
            IOUtil.getBytes(gis, fos);
        } finally {
            IOUtil.safeClose(gis);
            IOUtil.safeClose(fis);
            IOUtil.safeClose(fos);
        }
        return decompressed;
    }

    /**
     * <p>Decompresses the input zipFile assuming that it is LZMA compressed.</p>
     * @param input
     * @return
     * @throws java.lang.Exception
     */
    public static final File lzmaDecompress(File input) throws Exception {
        File decompressed = TempFileUtil.createTemporaryFile();
        LzmaAlone.main(new String[] { "d", input.getAbsolutePath(), decompressed.getAbsolutePath() });
        return decompressed;
    }

    /**
     * <p>Decompresses the input bytes assuming that it is LZMA compressed.</p>
     * @param bytes
     * @return
     * @throws java.lang.Exception
     */
    public static final byte[] lzmaDecompress(byte[] bytes) throws Exception {
        File tempFile = TempFileUtil.createTemporaryFile();
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            try {
                fos.write(bytes);
            } finally {
                IOUtil.safeClose(fos);
            }
            return IOUtil.getBytes(lzmaDecompress(tempFile));
        } finally {
            IOUtil.safeDelete(tempFile);
        }
    }

    /**
     * <p>Compresses the input zipFile using LZMA.</p>
     * @param input
     * @return
     * @throws java.lang.Exception
     */
    public static final File lzmaCompress(File input) throws Exception {
        File compressed = TempFileUtil.createTemporaryFile(".lzma");
        LzmaAlone.main(new String[] { "e", input.getCanonicalPath(), compressed.getCanonicalPath() });
        return compressed;
    }
}
