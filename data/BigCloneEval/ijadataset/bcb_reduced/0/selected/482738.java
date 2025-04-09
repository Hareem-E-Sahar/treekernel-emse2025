package com.alexmcchesney.poster.utils;

import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

/**
 * Utility class allowing for the encryption and decryption of strings
 * @author amcchesney
 *
 */
public class StringEncrypter {

    /** Type of transformation to use when encrypting/decrypting password data
	 *  In this case - DES: The Digital Encryption Standard as described in FIPS PUB 46-2.
	 *  This algorithm is provided by the sun cryptography provider, which is shipped
	 *  with the cryptography extensions.  */
    private static final String TRANSFORMATION = "DES";

    /**
	   * Encypts a string using the given key
	   * @param sSource	String to encrypt
	   * @param sKey	Key to encrypt with
	   * @return	The encrypted string
	   * @throws EncryptionException thrown if we cannot encrypt the stsring
	   */
    public static String encrypt(String sSource, String sKey) throws EncryptionException {
        return processString(sSource, sKey, true);
    }

    /**
	   * Decrypts a string using the given key
	   * @param sSource	String to decrypt
	   * @param sKey	Key to decrypt with
	   * @return	The decrypted string
	   * @throws EncryptionException thrown if we cannot decrypt the stsring
	   */
    public static String decrypt(String sSource, String sKey) throws EncryptionException {
        return processString(sSource, sKey, false);
    }

    /**
	   * Actually processes the string
	   * @param sSource	Source string to encrypt or descrypt
	   * @param sKey	Key to use
	   * @param bEncrypt	If true, the string is encrypted.  If false, decrypted.
	   * @return The converted string
	   * @throws EncryptionException
	   */
    private static String processString(String sSource, String sKey, boolean bEncrypt) throws EncryptionException {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        String sResult = null;
        Cipher writeCipher = null;
        try {
            writeCipher = Cipher.getInstance(TRANSFORMATION);
            Key passKey = generateKey(sKey);
            int iMode = Cipher.DECRYPT_MODE;
            if (bEncrypt) {
                iMode = Cipher.ENCRYPT_MODE;
            }
            writeCipher.init(iMode, passKey);
            byte[] bytes = writeCipher.doFinal(sSource.getBytes());
            sResult = new String(bytes);
        } catch (Throwable t) {
            if (bEncrypt) {
                throw new EncryptionException("ENCRYPTION_EXCEPTION", t);
            } else {
                throw new EncryptionException("DECRYPTION_EXCEPTION", t);
            }
        }
        return sResult;
    }

    /**
		 * Creates a secret key object based on the key string.
		 * @return	Key object
		 * @throws InvalidKeyException
		 * @throws NoSuchAlgorithmException
		 * @throws NoSuchProviderException
		 * @throws InvalidKeySpecException
		 */
    private static Key generateKey(String sKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        DESKeySpec keySpec;
        keySpec = new DESKeySpec(sKey.getBytes(), 0);
        SecretKeyFactory factory;
        factory = SecretKeyFactory.getInstance(TRANSFORMATION);
        Key newKey = factory.generateSecret(keySpec);
        return newKey;
    }
}
