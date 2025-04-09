package nzdis.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for data de/compression using java.util.zip package.
 * 
 *<br><br>
 * ZipUtil.java<br>
 * Created: Tue Mar 14 11:20:39 2000<br>
 *
 * @author Mariusz Nowostawski
 * @version @version@ $Revision: 1.2 $
 */
public class ZipUtil {

    public static int MAX_BUFFER = 20000;

    public static String uncompressString(InputStream in) throws IOException {
        String out = "";
        final byte[] buffer = new byte[MAX_BUFFER];
        final GZIPInputStream zin = new GZIPInputStream(in);
        int len = zin.read(buffer, 0, MAX_BUFFER);
        while (len > 0) {
            out += new String(buffer, 0, len);
            len = zin.read(buffer, 0, MAX_BUFFER);
        }
        zin.close();
        return out;
    }

    public static void compress(OutputStream out, String s) throws IOException {
        final byte[] in = s.getBytes();
        final GZIPOutputStream zout = new GZIPOutputStream(out);
        zout.write(in, 0, in.length);
        zout.flush();
        zout.finish();
    }

    public static String uncompressZipString(InputStream in) throws IOException {
        String out = "";
        final byte[] buffer = new byte[MAX_BUFFER];
        final ZipInputStream zin = new ZipInputStream(in);
        zin.getNextEntry();
        int len = zin.read(buffer, 0, MAX_BUFFER);
        while (len > 0) {
            out += new String(buffer, 0, len);
            len = zin.read(buffer, 0, MAX_BUFFER);
        }
        zin.close();
        return out;
    }

    public static void compressZip(OutputStream out, String s) throws IOException {
        compressZip(out, s, 5);
    }

    public static void compressZip(OutputStream out, String s, int level) throws IOException {
        final byte[] in = s.getBytes();
        final ZipOutputStream zout = new ZipOutputStream(out);
        zout.setLevel(level);
        zout.setComment("Data transfer");
        zout.putNextEntry(new ZipEntry("1"));
        zout.write(in, 0, in.length);
        zout.closeEntry();
        zout.flush();
        zout.finish();
    }
}
