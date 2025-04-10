package cn.ac.ntarl.umt.utils.pwd;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

/**
 * <a href="Digester.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class Digester {

    public static final String ENCODING = "UTF-8";

    public static final String DIGEST_ALGORITHM = "SHA";

    public static String digest(String text) {
        return digest(DIGEST_ALGORITHM, text);
    }

    public static String digest(String algorithm, String text) {
        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance(algorithm);
            mDigest.update(text.getBytes(ENCODING));
        } catch (NoSuchAlgorithmException nsae) {
            _log.error(nsae, nsae);
        } catch (UnsupportedEncodingException uee) {
            _log.error(uee, uee);
        }
        byte[] raw = mDigest.digest();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(raw);
    }

    private static Logger _log = Logger.getLogger(Digester.class);
}
