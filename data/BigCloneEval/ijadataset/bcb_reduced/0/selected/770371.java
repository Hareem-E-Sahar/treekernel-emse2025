package org.restlet.ext.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.restlet.data.Digest;
import org.restlet.engine.util.Base64;

/**
 * Security data manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class DigestUtils {

    /**
     * General regex pattern to extract comma separated name-value components.
     * This pattern captures one name and value per match(), and is repeatedly
     * applied to the input string to extract all components. Must handle both
     * quoted and unquoted values as RFC2617 isn't consistent in this respect.
     * Pattern is immutable and thread-safe so reuse one static instance.
     */
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac(String source, byte[] secretKey) {
        byte[] result = null;
        try {
            final SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            result = mac.doFinal(source.getBytes());
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Could not find the SHA-1 algorithm. HMac conversion failed.", nsae);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException("Invalid key exception detected. HMac conversion failed.", ike);
        }
        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA-1 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac(String source, String secretKey) {
        return toHMac(source, secretKey.getBytes());
    }

    ;

    /**
     * Converts a source string to its HMAC/SHA256 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac256(String source, byte[] secretKey) {
        byte[] result = null;
        try {
            final SecretKeySpec signingKey = new SecretKeySpec(secretKey, "HmacSHA256");
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            result = mac.doFinal(source.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Could not find the SHA256 algorithm. HMac conversion failed.", nsae);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException("Invalid key exception detected. HMac conversion failed.", ike);
        } catch (IllegalStateException ise) {
            throw new RuntimeException("IIllegal state exception detected. HMac conversion failed.", ise);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Unsuported encoding UTF-8. HMac conversion failed.", uee);
        }
        return result;
    }

    /**
     * Converts a source string to its HMAC/SHA256 value.
     * 
     * @param source
     *            The source string to convert.
     * @param secretKey
     *            The secret key to use for conversion.
     * @return The HMac value of the source string.
     */
    public static byte[] toHMac256(String source, String secretKey) {
        return toHMac256(source, secretKey.getBytes());
    }

    /**
     * Return the HTTP DIGEST hashed secret. It concatenates the identifier,
     * realm and secret, separated by a comma and digest them using MD5.
     * 
     * @param identifier
     *            The user identifier to hash.
     * @param secret
     *            The user secret.
     * @param realm
     *            The authentication realm.
     * @return A hash of the user name, realm, and password, specified as A1 in
     *         section 3.2.2.2 of RFC2617, or null if the identifier has no
     *         corresponding secret.
     */
    public static String toHttpDigest(String identifier, char[] secret, String realm) {
        if (secret != null) {
            return toMd5(identifier + ":" + realm + ":" + new String(secret));
        }
        return null;
    }

    /**
     * Returns the MD5 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset. The returned hexadecimal String always
     * contains 32 lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *            The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toMd5(String target) {
        try {
            return toMd5(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("US-ASCII is an unsupported encoding, unable to compute MD5");
        }
    }

    /**
     * Returns the MD5 digest of target string. Target is decoded to bytes using
     * the named charset. The returned hexadecimal String always contains 32
     * lowercase alphanumeric characters. For example, if target is
     * "HelloWorld", this method returns "68e109f0f40ca72a15e05cc22786f8e6".
     * 
     * @param target
     *            The string to encode.
     * @param charsetName
     *            The character set.
     * @return The MD5 digest of the target string.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String toMd5(String target, String charsetName) throws UnsupportedEncodingException {
        try {
            final byte[] md5 = MessageDigest.getInstance("MD5").digest(target.getBytes(charsetName));
            final char[] md5Chars = new char[32];
            int i = 0;
            for (final byte b : md5) {
                md5Chars[i++] = HEXDIGITS[(b >> 4) & 0xF];
                md5Chars[i++] = HEXDIGITS[b & 0xF];
            }
            return new String(md5Chars);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("No MD5 algorithm, unable to compute MD5");
        }
    }

    /**
     * Returns the SHA1 digest of the target string. Target is decoded to bytes
     * using the US-ASCII charset.
     * 
     * @param target
     *            The string to encode.
     * @return The MD5 digest of the target string.
     */
    public static String toSha1(String target) {
        try {
            return toSha1(target, "US-ASCII");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("US-ASCII is an unsupported encoding, unable to compute SHA1");
        }
    }

    /**
     * Returns the digest of the target string. Target is decoded to bytes using
     * the US-ASCII charset. Supports MD5 and SHA-1 algorithms.
     * 
     * @param target
     *            The string to encode.
     * @param algorithm
     *            The digest algorithm to use.
     * @return The digest of the target string.
     */
    public static char[] digest(char[] target, String algorithm) {
        return DigestUtils.digest(new String(target), algorithm).toCharArray();
    }

    /**
     * Returns the digest of the target string. Target is decoded to bytes using
     * the US-ASCII charset. Supports MD5 and SHA-1 algorithms.
     * 
     * @param target
     *            The string to encode.
     * @param algorithm
     *            The digest algorithm to use.
     * @return The digest of the target string.
     */
    public static String digest(String target, String algorithm) {
        if (Digest.ALGORITHM_MD5.equals(algorithm)) {
            return toMd5(target);
        } else if (Digest.ALGORITHM_SHA_1.equals(algorithm)) {
            return toSha1(target);
        }
        throw new IllegalArgumentException("Unsupported algorithm.");
    }

    /**
     * Returns the SHA1 digest of target string. Target is decoded to bytes
     * using the named charset.
     * 
     * @param target
     *            The string to encode.
     * @param charsetName
     *            The character set.
     * @return The SHA1 digest of the target string.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String toSha1(String target, String charsetName) throws UnsupportedEncodingException {
        try {
            return Base64.encode(MessageDigest.getInstance("SHA1").digest(target.getBytes(charsetName)), false);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("No SHA1 algorithm, unable to compute SHA1");
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private DigestUtils() {
    }
}
