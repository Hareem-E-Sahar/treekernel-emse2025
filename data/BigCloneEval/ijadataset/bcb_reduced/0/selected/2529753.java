package name.huliqing.qblog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author huliqing
 */
public class ZipUtils {

    /**
     * 将byte数组压缩成zip byte数组
     * @param bytes
     * @return
     * @throws IOException
     */
    public static final byte[] pack(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry(""));
        zos.write(bytes);
        zos.close();
        return baos.toByteArray();
    }

    /**
     * 将zip bytes解压
     * @param zipBytes
     * @return
     * @throws IOException
     */
    public static final byte[] unpack(byte[] zipBytes) throws IOException {
        ZipInputStream zis = new ZipInputStream(new InputStreamHelper(zipBytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipEntry ze = null;
        int len;
        byte[] buff = new byte[2048];
        while ((ze = zis.getNextEntry()) != null) {
            while ((len = zis.read(buff, 0, buff.length)) != -1) {
                baos.write(buff, 0, len);
            }
        }
        return baos.toByteArray();
    }

    private static final class InputStreamHelper extends InputStream {

        private byte[] bytes;

        private int pos;

        public InputStreamHelper(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int read() throws IOException {
            if (bytes.length <= 0 || pos >= bytes.length) {
                return -1;
            }
            return bytes[pos++] & 0xff;
        }
    }
}
