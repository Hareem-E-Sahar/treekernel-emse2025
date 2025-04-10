package lpr.minikazaa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 *
 * @author Andrea Di Grazia, Massimiliano Giovine
 * @date 17-nov-2008
 * @file md5.java
 */
public class md5 {

    public static String getMD5(File file) {
        final int MAX_BYTE = 1024;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[Constants.MAX_BYTE];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception ex) {
            return null;
        }
    }
}
