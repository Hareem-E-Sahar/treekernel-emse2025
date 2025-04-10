package radius.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import radius.RadiusPacket;

/**
 * @author <a href="mailto:zzzhc0508@hotmail.com">zzzhc</a>
 * 
 */
public class AuthenticatorUtil {

    private AuthenticatorUtil() {
    }

    public static byte[] createAuthenticator(ByteBuffer data, String secret) {
        assert data.isDirect() == false : "must not a direct ByteBuffer";
        int pos = data.position();
        if (pos < RadiusPacket.MIN_PACKET_LENGTH) {
            System.err.println("packet too small");
            return null;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] arr = data.array();
            md5.reset();
            md5.update(arr, 0, pos);
            md5.update(secret.getBytes());
            return md5.digest();
        } catch (NoSuchAlgorithmException nsaex) {
            throw new RuntimeException("Could not access MD5 algorithm, fatal error");
        }
    }
}
