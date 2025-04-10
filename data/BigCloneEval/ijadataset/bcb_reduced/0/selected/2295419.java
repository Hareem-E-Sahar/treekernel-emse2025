package fr.cnes.sitools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import junit.framework.TestCase;
import org.junit.Test;
import org.restlet.ext.crypto.DigestUtils;

/**
 * Test the different crypting algorithms.
 * 
 * @author jp.boignard (AKKA Technologies)
 */
public class SecurityEncryptionTestCase extends TestCase {

    /**
   * Test Digest SSHA crypting algorithm shared with many LDAPs
   */
    @Test
    public void testDigestSSHACryptingAlgorithm() {
        String password = "ulisse2010";
        String salt = "salt";
        String expected = "{MD5}0c0MW1lbDoe0rqrYxc30Rw==";
        String encoded = "{MD5}" + DigestUtils.toMd5(password);
        String bencoded = digestMd5(password);
        assertEquals(expected, bencoded);
    }

    /**
   * DigestMD5 used by LDAP is a combination of javax.security instead of Restlet DigestUtils.toMd5
   * and base64.
   * @param password password to encode
   * @return encoded password
   */
    private String digestMd5(final String password) {
        String base64;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            base64 = fr.cnes.sitools.util.Base64.encodeBytes(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return "{MD5}" + base64;
    }
}
