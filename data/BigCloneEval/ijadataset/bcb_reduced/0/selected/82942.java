package net.sf.odinms.client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginCryptoLegacy {

    private static Logger log = LoggerFactory.getLogger(LoginCryptoLegacy.class);

    /**
	 * Map of 6 bit nibbles to base64 characters.
	 */
    private static char[] iota64 = new char[64];

    static {
        int i = 0;
        iota64[i++] = '.';
        iota64[i++] = '/';
        for (char c = 'A'; c <= 'Z'; c++) iota64[i++] = c;
        for (char c = 'a'; c <= 'z'; c++) iota64[i++] = c;
        for (char c = '0'; c <= '9'; c++) iota64[i++] = c;
    }

    /**
	 * Class Constructor. Static class, so it's a dummy constructor.
	 */
    private LoginCryptoLegacy() {
    }

    /**
	 * Hash the password for first time storage.
	 * 
	 * @param password The password to be hashed.
	 * @return String of the hashed password.
	 */
    public static String hashPassword(String password) {
        byte[] randomBytes = new byte[6];
        java.util.Random randomGenerator = new java.util.Random();
        randomGenerator.nextBytes(randomBytes);
        return myCrypt(password, genSalt(randomBytes));
    }

    /**
	 * Check a password against a hash.
	 * 
	 * @param password The password to validate.
	 * @param hash The hash to validate against.
	 * @return <code>true</code> if the password is correct, <code>false</code> otherwise.
	 */
    public static boolean checkPassword(String password, String hash) {
        return (myCrypt(password, hash).equals(hash));
    }

    public static boolean isLegacyPassword(String hash) {
        return hash.substring(0, 3).equals("$H$");
    }

    /**
	 * Encrypt a string with <code>Seed</code> as a seed code.
	 * 
	 * @param password Password to encrypt.
	 * @param seed Seed to use.
	 * @return The salted SHA1 hash of password.
	 * @throws RuntimeException
	 */
    private static String myCrypt(String password, String seed) throws RuntimeException {
        String out = null;
        int count = 8;
        MessageDigest digester;
        if (!seed.substring(0, 3).equals("$H$")) {
            byte[] randomBytes = new byte[6];
            java.util.Random randomGenerator = new java.util.Random();
            randomGenerator.nextBytes(randomBytes);
            seed = genSalt(randomBytes);
        }
        String salt = seed.substring(4, 12);
        if (salt.length() != 8) {
            throw new RuntimeException("Error hashing password - Invalid seed.");
        }
        byte[] sha1Hash = new byte[40];
        try {
            digester = MessageDigest.getInstance("SHA-1");
            digester.update((salt + password).getBytes("iso-8859-1"), 0, (salt + password).length());
            sha1Hash = digester.digest();
            do {
                byte[] CombinedBytes = new byte[sha1Hash.length + password.length()];
                System.arraycopy(sha1Hash, 0, CombinedBytes, 0, sha1Hash.length);
                System.arraycopy(password.getBytes("iso-8859-1"), 0, CombinedBytes, sha1Hash.length, password.getBytes("iso-8859-1").length);
                digester.update(CombinedBytes, 0, CombinedBytes.length);
                sha1Hash = digester.digest();
            } while (--count > 0);
            out = seed.substring(0, 12);
            out += encode64(sha1Hash);
        } catch (NoSuchAlgorithmException Ex) {
            log.error("Error hashing password.", Ex);
        } catch (UnsupportedEncodingException Ex) {
            log.error("Error hashing password.", Ex);
        }
        if (out == null) {
            throw new RuntimeException("Error hashing password - out = null");
        }
        return out;
    }

    /**
	 * Generates a salt string from random bytes <code>Random</code>
	 * 
	 * @param Random Random bytes to get salt from.
	 * @return Salt string.
	 */
    private static String genSalt(byte[] Random) {
        String Salt = "$H$";
        Salt += iota64[30];
        Salt += encode64(Random);
        return Salt;
    }

    /**
	 * Encodes a byte array into base64.
	 * 
	 * @param Input Array of bytes to put into base64.
	 * @return String of base64.
	 */
    private static String encode64(byte[] Input) {
        int iLen = Input.length;
        int oDataLen = (iLen * 4 + 2) / 3;
        int oLen = ((iLen + 2) / 3) * 4;
        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = Input[ip++] & 0xff;
            int i1 = ip < iLen ? Input[ip++] & 0xff : 0;
            int i2 = ip < iLen ? Input[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = iota64[o0];
            out[op++] = iota64[o1];
            out[op] = op < oDataLen ? iota64[o2] : '=';
            op++;
            out[op] = op < oDataLen ? iota64[o3] : '=';
            op++;
        }
        return new String(out);
    }
}
