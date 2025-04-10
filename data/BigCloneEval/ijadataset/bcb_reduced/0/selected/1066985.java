package org.apache.sanselan.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import org.apache.sanselan.SanselanConstants;

public class IOUtils implements SanselanConstants {

    /**
     * This class should never be instantiated.
     */
    private IOUtils() {
    }

    /**
     * Reads an InputStream to the end.
     * <p>
     *
     * @param is
     *            The InputStream to read.
     * @return A byte array containing the contents of the InputStream
     * @see InputStream
     */
    public static byte[] getInputStreamBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(4096);
            is = new BufferedInputStream(is);
            int count;
            byte[] buffer = new byte[4096];
            while ((count = is.read(buffer, 0, 4096)) > 0) {
                os.write(buffer, 0, count);
            }
            os.flush();
            return os.toByteArray();
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                Debug.debug(e);
            }
        }
    }

    /**
     * Reads a File into memory.
     * <p>
     *
     * @param file
     *            The File to read.
     * @return A byte array containing the contents of the File
     * @see InputStream
     */
    public static byte[] getFileBytes(File file) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getInputStreamBytes(is);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                Debug.debug(e);
            }
        }
    }

    public static void writeToFile(byte[] src, File file) throws IOException {
        ByteArrayInputStream stream = null;
        try {
            stream = new ByteArrayInputStream(src);
            putInputStreamToFile(stream, file);
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (Exception e) {
                Debug.debug(e);
            }
        }
    }

    public static void putInputStreamToFile(InputStream src, File file) throws IOException {
        FileOutputStream stream = null;
        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            stream = new FileOutputStream(file);
            copyStreamToStream(src, stream);
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (Exception e) {
                Debug.debug(e);
            }
        }
    }

    public static void copyStreamToStream(InputStream src, OutputStream dst) throws IOException {
        copyStreamToStream(src, dst, true);
    }

    public static void copyStreamToStream(InputStream src, OutputStream dst, boolean close_streams) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(src);
            bos = new BufferedOutputStream(dst);
            int count;
            byte[] buffer = new byte[4096];
            while ((count = bis.read(buffer, 0, buffer.length)) > 0) dst.write(buffer, 0, count);
            bos.flush();
        } finally {
            if (close_streams) {
                try {
                    if (bis != null) bis.close();
                } catch (IOException e) {
                    Debug.debug(e);
                }
                try {
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    Debug.debug(e);
                }
            }
        }
    }

    public static final boolean copyFileNio(File src, File dst) throws IOException {
        FileChannel srcChannel = null, dstChannel = null;
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            {
                int safe_max = (64 * 1024 * 1024) / 4;
                long size = srcChannel.size();
                long position = 0;
                while (position < size) {
                    position += srcChannel.transferTo(position, safe_max, dstChannel);
                }
            }
            srcChannel.close();
            srcChannel = null;
            dstChannel.close();
            dstChannel = null;
            return true;
        } finally {
            try {
                if (srcChannel != null) srcChannel.close();
            } catch (IOException e) {
                Debug.debug(e);
            }
            try {
                if (dstChannel != null) dstChannel.close();
            } catch (IOException e) {
                Debug.debug(e);
            }
        }
    }
}
