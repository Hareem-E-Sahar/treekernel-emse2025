package stk.web.gae.util.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import com.google.appengine.repackaged.com.google.io.base.IORuntimeException;

public class ZipUtil {

    public static byte[] compress(byte[] data) {
        ZipEntry ze = new ZipEntry("");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.setLevel(9);
        try {
            zos.putNextEntry(ze);
            IOUtils.write(data, zos);
        } catch (IOException e) {
            throw new IORuntimeException("", e);
        }
        return baos.toByteArray();
    }
}
