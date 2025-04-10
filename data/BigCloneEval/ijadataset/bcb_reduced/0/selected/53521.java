package org.apache.poi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtils {

    private IOUtils() {
    }

    /**
	 * Reads all the data from the input stream, and returns the bytes read.
	 */
    public static byte[] toByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read = 0;
        while (read != -1) {
            read = stream.read(buffer);
            if (read > 0) {
                baos.write(buffer, 0, read);
            }
        }
        return baos.toByteArray();
    }

    /**
	 * Helper method, just calls <tt>readFully(in, b, 0, b.length)</tt>
	 */
    public static int readFully(InputStream in, byte[] b) throws IOException {
        return readFully(in, b, 0, b.length);
    }

    /**
	 * Same as the normal <tt>in.read(b, off, len)</tt>, but tries to ensure
	 * that the entire len number of bytes is read.
	 * <p>
	 * If the end of file is reached before any bytes are read, returns -1. If
	 * the end of the file is reached after some bytes are read, returns the
	 * number of bytes read. If the end of the file isn't reached before len
	 * bytes have been read, will return len bytes.
	 */
    public static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int total = 0;
        while (true) {
            int got = in.read(b, off + total, len - total);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            }
            total += got;
            if (total == len) {
                return total;
            }
        }
    }

    /**
	 * Copies all the data from the given InputStream to the OutputStream. It
	 * leaves both streams open, so you will still need to close them once done.
	 */
    public static void copy(InputStream inp, OutputStream out) throws IOException {
        byte[] buff = new byte[4096];
        int count;
        while ((count = inp.read(buff)) != -1) {
            if (count > 0) {
                out.write(buff, 0, count);
            }
        }
    }
}
