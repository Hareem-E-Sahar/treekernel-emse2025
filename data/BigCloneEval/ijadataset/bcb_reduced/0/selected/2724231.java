package cluster5.shared.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {

    /**
	 * Compresses an Object and returns it as byte array.
	 * 
	 * @param o object to serialize and compress
	 * @return byte array or null if exception was thrown
	 */
    public static byte[] zipObject(Object o) {
        if (o == null) return null;
        byte[] returnValue = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            byte[] binaryData = baos.toByteArray();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos2);
            ZipEntry ze = new ZipEntry("compressed_obj");
            zos.putNextEntry(ze);
            zos.write(binaryData);
            zos.closeEntry();
            zos.flush();
            zos.close();
            returnValue = baos2.toByteArray();
            baos.close();
            baos2.close();
        } catch (IOException ex) {
            returnValue = null;
        }
        return returnValue;
    }

    /**
	 * Decompresses Object stored in byte array and returns it.
	 */
    public static Object unzipObject(byte[] b) {
        if (b == null) return null;
        Object returnValue = null;
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(b));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            zis.getNextEntry();
            byte[] bufik = new byte[4096];
            while (zis.available() != 0) {
                int numRead = zis.read(bufik);
                if (numRead > 0) baos.write(bufik, 0, numRead);
            }
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            returnValue = ois.readObject();
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
        return returnValue;
    }
}
