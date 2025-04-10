package org.pprun.hjpetstore.client;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ideally, we can use commons-codec for the following task, but
 * since glassfish has its own repacked 1.2 commons-codec, but I want the SHA512 in commons-codec-1.4.jar
 * and I did not found the way to workaround.
 *
 * @author <a href="mailto:quest.run@gmail.com">pprun</a>
 */
public class MessageDigestUtil {

    private static final String HMAC_SHA512_ALGORITHM = "HmacSHA512";

    private static final Log log = LogFactory.getLog(MessageDigestUtil.class);

    /**
     * MD5 hash function to be "cryptographically secure", the result is 32 HEX characters.
     * <ol>
     * <li>Given a hash, it is computationally infeasible to find an input that produces that hash</li>
     * <li>Given an input, it is computationally infeasible to find another input that produces the same hash</li>
     * </ol>
     *
     * @param plainText
     * @param salt
     * @return the MD% of the input text
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String md5(byte[] plainText, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(salt);
        String md5 = new BigInteger(1, messageDigest.digest(plainText)).toString(16);
        if (md5.length() < 32) {
            md5 = "0" + md5;
        }
        messageDigest.reset();
        if (log.isDebugEnabled()) {
            log.debug(new String(plainText, "UTF-8") + "[salt=" + new String(salt, "UTF-8") + "] 's MD5: " + md5);
        }
        return md5;
    }

    /**
     * SHA-512 hash function to be "cryptographically secure", the result is 128 HEX characters
     * <ol>
     * <li>Given a hash, it is computationally infeasible to find an input that produces that hash</li>
     * <li>Given an input, it is computationally infeasible to find another input that produces the same hash</li>
     * </ol>
     *
     * @param plainText
     * @param salt
     * @return the SHA-512 of the input text
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String sha512(byte[] plainText) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        String sha512 = new BigInteger(1, messageDigest.digest(plainText)).toString(16);
        if (sha512.length() < 128) {
            sha512 = "0" + sha512;
        }
        messageDigest.reset();
        if (log.isDebugEnabled()) {
            log.debug(new String(plainText, "UTF-8") + "'s SHA512: " + sha512);
        }
        return sha512;
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data The data to be signed
     * @param secretKey the secretKey for signature
     * @return The base64-encoded RFC 2104-compliant HmacSHA512 signature.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String rfc2104HmacSha512(String data, String secretKey) {
        String result;
        try {
            SecretKeySpec signingKeySpec = new SecretKeySpec(secretKey.getBytes(Charset.forName("UTF-8")), HMAC_SHA512_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA512_ALGORITHM);
            mac.init(signingKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(Charset.forName("UTF-8")));
            result = new String(Base64.encodeBase64(rawHmac), Charset.forName("UTF-8"));
            if (log.isDebugEnabled()) {
                log.debug(data + "'s HmacSHA512: " + result);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Calculate HmacSHA512 failed: " + e.getMessage());
        }
        return result;
    }

    public static String calculateSignature(String httpMethod, String date, String resourcePath, String secretKey) {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(httpMethod).append("\n");
        stringToSign.append(date).append("\n");
        stringToSign.append(resourcePath).append("\n");
        if (log.isDebugEnabled()) {
            log.debug("stringToSign = " + stringToSign.toString());
        }
        String calculatedSignature = rfc2104HmacSha512(stringToSign.toString(), secretKey);
        return calculatedSignature;
    }
}
