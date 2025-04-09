package org.progeeks.util;

import java.io.*;
import java.util.zip.CRC32;

/**
 *  Utility methods for dumping streams to disk and copying
 *  files, etc..
 *
 *  @version   $Revision: 1.3 $
 *  @author    Paul Speed
 */
public class FileUtils {

    /**
     *  Copies the specified file to the specified location.
     *  Returns the number of bytes copied.
     */
    public static long copyFile(File source, File destination) throws IOException {
        FileInputStream fIn = new FileInputStream(source);
        BufferedInputStream in = new BufferedInputStream(fIn, 65536);
        try {
            return (saveStream(destination, in));
        } finally {
            in.close();
        }
    }

    /**
     *  Copies the specified file to the specified location.
     *  Returns the number of bytes copied and reports its status
     *  to the specified progress reporter.
     */
    public static long copyFile(File source, File destination, ProgressReporter pr) throws IOException {
        FileInputStream fIn = new FileInputStream(source);
        BufferedInputStream bIn = new BufferedInputStream(fIn, 65536);
        InputStream in = new ProgressReporterInputStream(bIn, pr, source.length());
        try {
            return (saveStream(destination, in));
        } finally {
            in.close();
        }
    }

    /**
     *  Saves the specified stream to a file and returns the
     *  number of bytes written.
     */
    public static long saveStream(File f, InputStream in) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        BufferedOutputStream out = new BufferedOutputStream(fOut, 65536);
        byte[] transferBuff = new byte[65536];
        try {
            int count = 0;
            int total = 0;
            while ((count = in.read(transferBuff)) >= 0) {
                out.write(transferBuff, 0, count);
                total += count;
            }
            return (total);
        } finally {
            out.close();
        }
    }

    /**
     *  Returns the CRC of the specified InputStream.  The supplied stream
     *  must be closed by the caller.
     */
    public static long getCrc(InputStream in) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buff = new byte[65536];
        int count = 0;
        while ((count = in.read(buff)) >= 0) {
            crc.update(buff, 0, count);
        }
        return (crc.getValue());
    }

    /**
     *  Returns the CRC of the specified file.
     */
    public static long getCrc(File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        try {
            return (getCrc(fIn));
        } finally {
            fIn.close();
        }
    }
}
