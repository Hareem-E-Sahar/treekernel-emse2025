package tomPack.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import tomPack.DecoderException;
import tomPack.TomHexUtils;
import tomPack.io.TomIOUtils;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

@SuppressWarnings("nls")
public class TomAesUtils {

    public static final String defaultCharsetName = "UTF-8";

    private static final String algorithm = "AES";

    public static SecretKey createAesKey(String encoded) throws DecoderException {
        return createAesKey(TomHexUtils.decodeHex(encoded));
    }

    public static SecretKey createAesKey(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeySpec keySpec = new SecretKeySpec(encoded, algorithm);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        return factory.generateSecret(keySpec);
    }

    public static SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(algorithm);
        return keygen.generateKey();
    }

    public static String encrypt(String data, SecretKey key) throws AesEncryptionException, UnsupportedEncodingException {
        return encrypt(data.getBytes(defaultCharsetName), key);
    }

    public static String encrypt(byte[] data, SecretKey key) throws AesEncryptionException {
        String result = null;
        byte[] base64Encoded = Base64.encode(data).getBytes();
        InputStream in = new ByteArrayInputStream(base64Encoded);
        OutputStream out = new ByteArrayOutputStream();
        try {
            encrypt(key, in, out);
            result = out.toString();
        } finally {
            TomIOUtils.close(in);
            TomIOUtils.close(out);
        }
        return result;
    }

    private static void encrypt(SecretKey key, InputStream in, OutputStream out) throws AesEncryptionException {
        byte[] buf = new byte[1024];
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            out = new CipherOutputStream(out, cipher);
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0) {
                out.write(buf, 0, numRead);
            }
        } catch (Exception e) {
            throw new AesEncryptionException(e);
        }
    }

    public static String decryptString(SecretKey key, InputStream in) throws Exception {
        byte[] decodedBytes = decrypt(key, in);
        return new String(decodedBytes, defaultCharsetName);
    }

    public static byte[] decrypt(SecretKey key, InputStream in) throws AesDecryptionException, Base64DecodingException {
        String base64Encoded = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            decrypt(key, in, out);
            base64Encoded = out.toString();
        } finally {
            TomIOUtils.close(out);
        }
        return Base64.decode(base64Encoded.getBytes());
    }

    private static void decrypt(SecretKey key, InputStream in, OutputStream out) throws AesDecryptionException {
        CipherInputStream cipherInputStream = null;
        byte[] buf = new byte[1024];
        try {
            Cipher dcipher = Cipher.getInstance(algorithm);
            dcipher.init(Cipher.DECRYPT_MODE, key);
            cipherInputStream = new CipherInputStream(in, dcipher);
            int numRead = 0;
            while ((numRead = cipherInputStream.read(buf)) >= 0) {
                out.write(buf, 0, numRead);
            }
        } catch (Exception e) {
            throw new AesDecryptionException(e);
        } finally {
            TomIOUtils.close(cipherInputStream);
        }
    }
}
