package com.ub.jcrypto.utils;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/** 
 * @author  Umesh Batra 
 * @version encrypt or decrypt the data string with the key passed as arguments 
 *          The program utilizes implementation blowfish algorithm 
 **/
public class CryptoUtil {

    /** 
	 * Default character set to be used e.g. ISO-8859-1 or UTF-16BE 
	 */
    public static final String DEFAULT_CHARSET = "UTF-16BE";

    /** 
	 * Generate a encrypted key that can be used for strong encryption 
	 * @param   key, user's cypher key that can be used for encryption 
	 * @return  returns the encrypted key  
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 
	 **/
    public static byte[] generateKey(String key) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalStateException, IllegalBlockSizeException, BadPaddingException {
        if (key != null) return key.getBytes(); else return null;
    }

    /** 
	 * Encrypt the data string with the key passed as arguments 
	 * @param   source, data string to be encrypted 
	 * @param   encKey, cypher key to be used for encryption 
	 * @return  returns the encrypted string 
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 
	 **/
    public static String encrypt(String source, byte[] encKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec skeySpec = new SecretKeySpec(encKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] sourceBytes = source.getBytes(DEFAULT_CHARSET);
        byte[] encryptedSource = cipher.doFinal(sourceBytes);
        return new String(encryptedSource, DEFAULT_CHARSET);
    }

    /** 
	 * Decrypt the encrypted data string 
	 * @param   encryptedSource, data string to be decrypted 
	 * @param   encKey, cypher key that was used for encryption 
	 * @return  returns the decrypted string 
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 	 
	 **/
    public static String decrypt(String encryptedSource, byte[] encKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec skeySpec = new SecretKeySpec(encKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] encryptedSourceBytes = encryptedSource.getBytes(DEFAULT_CHARSET);
        byte[] source = cipher.doFinal(encryptedSourceBytes);
        return new String(source, DEFAULT_CHARSET);
    }

    /** 
	 * Encrypt the data string 
	 * @param   source, path to the source file to be encrypted 
	 * @param   destination, path to the destination file where encrypted data would go 
	 * @param   encKey, cypher key to be used 
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 	 
	 **/
    public static void encrypt(String source, String destination, byte[] encKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException {
        File inputFile = new File(source);
        File outputFile = new File(destination);
        encrypt(inputFile, outputFile, encKey);
    }

    /** 
	 * Encrypt the data string 
	 * @param   source, source file to be encrypted 
	 * @param   destination, destination file to where encrypted data would go 
	 * @param   encKey, cypher key to be used 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws IllegalStateException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidKeyException 
	 **/
    public static void encrypt(File inputFile, File outputFile, byte[] encKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(encKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        if (inputFile.isFile()) {
            if (outputFile.exists()) outputFile.delete();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile);
            CipherOutputStream cout = new CipherOutputStream(out, cipher);
            int length = 0;
            byte[] buffer = new byte[8];
            while ((length = in.read(buffer)) != -1) {
                cout.write(buffer, 0, length);
            }
            cout.flush();
            cout.close();
            out.close();
            in.close();
        }
    }

    /** 
	 * Decrypt the encrypted file 
	 * @param   source, path to the source file to be decrypted  
	 * @param   destination, path to the destination file where decrypted data would go 
	 * @param   encKey, cypher key that was used for encryption 
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 	 
	 **/
    public static void decrypt(String source, String destination, byte[] encKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException {
        File inputFile = new File(source);
        File outputFile = new File(destination);
        decrypt(inputFile, outputFile, encKey);
    }

    /** 
	 * Decrypt the encrypted file 
	 * @param   inputFile, file to be encrypted 
	 * @param   outputFile, file to which decrypted data would go 
	 * @param   encKey, cypher key that was used for encryption 
	 * @throws  BadPaddingException 
	 * @throws  IllegalBlockSizeException 
	 * @throws  IllegalStateException 
	 * @throws  NoSuchPaddingException 
	 * @throws  NoSuchAlgorithmException 
	 * @throws  UnsupportedEncodingException 
	 * @throws  InvalidKeyException 
	 **/
    public static void decrypt(File inputFile, File outputFile, byte[] encKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException {
        SecretKeySpec skeySpec = new SecretKeySpec(encKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        if (inputFile.isFile()) {
            if (outputFile.exists()) outputFile.delete();
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile);
            CipherInputStream cin = new CipherInputStream(in, cipher);
            int length = 0;
            byte[] buffer = new byte[8];
            while ((length = cin.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
            out.close();
            cin.close();
            in.close();
        }
    }
}
