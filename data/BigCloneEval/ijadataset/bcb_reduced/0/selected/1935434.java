package de.fhkl.helloWorld.implementation.model.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;

/**
 * This class offers the symmetric encryption with a 256bit long
 * javax.crypto.SecretKey. The algorithm is AES.
 * 
 * Generated Keys from this class are used for the encryption of subprofiles and
 * messages.
 * 
 * @author HelloWorld
 */
public class CryptoAES256 {

    private int symmetricKeyLength = 256;

    private String symmetricAlgorithm = "AES";

    public InputStream encrypt(InputStream in, SecretKey key) throws InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            CipherInputStream cis = new CipherInputStream(in, cipher);
            byte[] b = new byte[1024];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            cis.close();
            in.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CryptoUtil.outToInputStream(fos);
    }

    public InputStream decrypt(InputStream in, SecretKey key) throws IOException {
        Cipher cipher;
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        try {
            cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            CipherInputStream cis = new CipherInputStream(in, cipher);
            byte[] b = new byte[1024];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            cis.close();
            in.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return CryptoUtil.outToInputStream(fos);
    }

    /**
	 * Symmetric encryption of a String.
	 * 
	 * @param data
	 *            the data which should be encrypted
	 * @param secretkey
	 *            the secretkey
	 * 
	 * @return the encrypted string
	 */
    public String encrypt(String data, SecretKey secretkey) {
        Cipher cipher;
        String encryptedData = null;
        try {
            cipher = Cipher.getInstance(secretkey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretkey);
            encryptedData = new String(org.apache.commons.codec.binary.Hex.encodeHex(cipher.doFinal(data.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return encryptedData;
    }

    /**
	 * Symmetric decryption of a String.
	 * 
	 * @param data
	 *            the encrypted data
	 * @param secretkey
	 *            the secretkey
	 * 
	 * @return the string
	 */
    public String decrypt(String data, SecretKey secretkey) {
        Cipher cipher;
        String decrypted = null;
        try {
            cipher = Cipher.getInstance(secretkey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretkey);
            decrypted = new String(cipher.doFinal(org.apache.commons.codec.binary.Hex.decodeHex(data.toCharArray())));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public SecretKey generateKey() {
        SecretKey secretKey = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(symmetricAlgorithm);
            kgen.init(symmetricKeyLength);
            secretKey = kgen.generateKey();
        } catch (Exception e) {
            System.out.println("USE A STATIC LOGGER FOR LOGGING!!!");
        }
        return secretKey;
    }
}
