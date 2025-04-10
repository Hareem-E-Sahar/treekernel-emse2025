package de.juwimm.util;

import java.security.MessageDigest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:eduard.siebert@juwimm.com">Eduard Siebert</a>
 * @version $Id: MD5.java 8 2009-02-15 08:54:54Z skulawik $
 */
public class MD5 {

    private static final Log log = LogFactory.getLog(MD5.class);

    public static final String computeHash(String stringToCompile) {
        String retVal = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(stringToCompile.getBytes());
            byte[] result = md5.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                hexString.append(Integer.toHexString(0xFF & result[i]));
            }
            retVal = hexString.toString();
            if (log.isDebugEnabled()) log.debug("MD5 hash for \"" + stringToCompile + "\" is: " + retVal);
        } catch (Exception exe) {
            log.error(exe.getMessage(), exe);
        }
        return retVal;
    }
}
