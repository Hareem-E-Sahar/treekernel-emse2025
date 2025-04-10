package nakayo.loginserver.utils;

import com.aionemu.commons.utils.Base64;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class with usefull methods to use with accounts
 *
 * @author SoulKeeper
 */
public class AccountUtils {

    /**
     * Logger :)
     */
    private static final Logger log = Logger.getLogger(AccountUtils.class);

    /**
     * Encodes password. SHA-1 is used to encode password bytes, Base64 wraps SHA1-hash to string.
     *
     * @param password password to encode
     * @return retunrs encoded password.
     */
    public static String encodePassword(String password) {
        try {
            MessageDigest messageDiegest = MessageDigest.getInstance("SHA-1");
            messageDiegest.update(password.getBytes("UTF-8"));
            return Base64.encodeToString(messageDiegest.digest(), false);
        } catch (NoSuchAlgorithmException e) {
            log.error("Exception while encoding password");
            throw new Error(e);
        } catch (UnsupportedEncodingException e) {
            log.error("Exception while encoding password");
            throw new Error(e);
        }
    }
}
