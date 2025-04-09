package crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * This class is responsible for DESEDE (triple DES) encryption and decryption.
 * 
 * @author Thomas Pedley
 */
public class DESEDECrypto {

    /** The encryption cipher. */
    private Cipher cipher;

    /** The secret key. */
    private SecretKey secretKey;

    /**
	 * Constructor.
	 * 
	 * @throws NoSuchAlgorithmException Thrown if DESede is not supported.
	 * @throws NoSuchPaddingException Thrown if PKCS5 is not supported.
	 */
    public DESEDECrypto() throws NoSuchAlgorithmException, NoSuchPaddingException {
        KeyGenerator keygen = KeyGenerator.getInstance("DESede");
        secretKey = keygen.generateKey();
        cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    }

    /**
	 * Set the key.
	 * 
	 * @param key The key bytes.
	 * @throws InvalidKeySpecException Thrown if the key specification is invalid.
	 * @throws NoSuchAlgorithmException Thrown id DESede is not supported.
	 * @throws InvalidKeyException Thrown if the key is invalid.
	 */
    public void setKey(byte[] key) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        DESedeKeySpec sks = new DESedeKeySpec(key);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
        secretKey = factory.generateSecret(sks);
    }

    /**
	 * Encrypt text.
	 * 
	 * @param plainText The text to encrypt.
	 * @return The encrypted text in blocks of base64 encoded bytes.
	 * @throws InvalidKeyException Thrown if the encryption key is invalid.
	 * @throws NoSuchAlgorithmException Thrown if DESede is not supported.
	 * @throws NoSuchPaddingException Thrown if PKCS5 is not supported.
	 * @throws IllegalBlockSizeException Thrown if the encryption block size is invalid.
	 * @throws BadPaddingException Thrown if the padding is bad.
	 * @throws IOException Thrown if an error occurs whilst writing to the stream.
	 * @throws InvalidAlgorithmParameterException Thrown if the generated IV is invalid. 
	 */
    public synchronized byte[] encrypt(String plainText) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidAlgorithmParameterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(plainText.getBytes("UTF8"));
        byte[] iv = new byte[8];
        Random r = new Random(System.currentTimeMillis());
        r.nextBytes(iv);
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
        baos.write(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec);
        CipherOutputStream cos = new CipherOutputStream(baos, cipher);
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = bais.read(buffer)) != -1) {
            cos.write(buffer, 0, bytesRead);
        }
        cos.close();
        byte[] result = baos.toByteArray();
        return result;
    }

    /**
	 * Decrypt ciphertext.
	 * 
	 * @param data The ciphertext to decrypt.
	 * @return The decrypted plaintext.
	 * @throws InvalidKeyException Thrown if an invalid key is used.
	 * @throws IllegalBlockSizeException Thrown if an illegal blocksize is used.
	 * @throws BadPaddingException Thrown if padding is bad.
	 * @throws NoSuchAlgorithmException Thrown if DESede is not supported.
	 * @throws NoSuchPaddingException Thrown if PKCS5 is not supported.
	 * @throws IOException Thrown if an error occurs whilst writing to the stream.
	 * @throws InvalidAlgorithmParameterException Thrown if the received IV is invalid.
	 */
    public synchronized String decrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        byte[] iv = new byte[8];
        System.arraycopy(data, 0, iv, 0, 8);
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec);
        CipherOutputStream cos = new CipherOutputStream(baos, cipher);
        cos.write(data, 8, data.length - 8);
        cos.close();
        byte[] result = baos.toByteArray();
        return new String(result, "UTF8");
    }
}
