package com.ekeyman.securesavelib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;
import com.ekeyman.securesavelib.dto.EncryptionKeys;

public class SecureSaveUtils {

    public static String getTemporaryDirectory() {
        return System.getProperty("jboss.server.temp.dir");
    }

    public static String getUniqueIdentifier() {
        UUID i = UUID.randomUUID();
        String s = i.toString();
        return s.replaceAll("-", "").toLowerCase();
    }

    public static void encrypt(EncryptionKeys encryptionKeys, File uploadedFile, File encryptedFile) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        SecretKeySpec encryptionKey = new SecretKeySpec(encryptionKeys.getKeyBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        InputStream clearTextFis = new FileInputStream(uploadedFile);
        OutputStream cipherTextFos = new FileOutputStream(encryptedFile);
        cipherTextFos = new CipherOutputStream(cipherTextFos, cipher);
        byte[] buf = new byte[4096];
        int numRead = 0;
        while ((numRead = clearTextFis.read(buf)) >= 0) {
            cipherTextFos.write(buf, 0, numRead);
        }
        cipherTextFos.close();
    }

    public static void decrypt(EncryptionKeys encryptionKeys, InputStream s3is, File decryptedFile) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        SecretKeySpec encryptionKey = new SecretKeySpec(encryptionKeys.getKeyBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        OutputStream decryptedFos = new FileOutputStream(decryptedFile);
        s3is = new CipherInputStream(s3is, cipher);
        int numRead = 0;
        byte[] buf = new byte[4096];
        while ((numRead = s3is.read(buf)) >= 0) {
            decryptedFos.write(buf, 0, numRead);
        }
        decryptedFos.close();
    }

    public static void encrypt(EncryptionKeys encryptionKeys, File uploadedFile, File encryptedFile, String passphrase) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecureRandom random = new SecureRandom();
        String secretKeyType = "PBEWITHSHA256AND256BITAES-CBC-BC";
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(secretKeyType);
        PBEKeySpec eKeySpec = new PBEKeySpec(DigestUtils.md5Hex(passphrase).toCharArray(), encryptionKeys.getSaltBytes(), encryptionKeys.getIterationCount(), 1024);
        SecretKey eKey = keyFactory.generateSecret(eKeySpec);
        Cipher eCipher = Cipher.getInstance("AES/CTR/NOPADDING");
        IvParameterSpec eIvParameterSpec = new IvParameterSpec(encryptionKeys.getIvBytes());
        eCipher.init(Cipher.ENCRYPT_MODE, eKey, eIvParameterSpec, random);
        InputStream clearTextFis = new FileInputStream(uploadedFile);
        OutputStream cipherTextFos = new FileOutputStream(encryptedFile);
        cipherTextFos = new CipherOutputStream(cipherTextFos, eCipher);
        byte[] ebuf = new byte[4096];
        int enumRead = 0;
        while ((enumRead = clearTextFis.read(ebuf)) >= 0) {
            cipherTextFos.write(ebuf, 0, enumRead);
        }
        cipherTextFos.close();
    }

    public static void decrypt(EncryptionKeys encryptionKeys, InputStream s3is, File decryptedFile, String passphrase) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        SecureRandom random = new SecureRandom();
        String secretKeyType = "PBEWITHSHA256AND256BITAES-CBC-BC";
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(secretKeyType);
        PBEKeySpec dKeySpec = new PBEKeySpec(DigestUtils.md5Hex(passphrase).toCharArray(), encryptionKeys.getSaltBytes(), encryptionKeys.getIterationCount(), 1024);
        SecretKey dKey = keyFactory.generateSecret(dKeySpec);
        Cipher dCipher = Cipher.getInstance("AES/CTR/NOPADDING");
        IvParameterSpec dIvParameterSpec = new IvParameterSpec(encryptionKeys.getIvBytes());
        dCipher.init(Cipher.DECRYPT_MODE, dKey, dIvParameterSpec, random);
        OutputStream decryptedTextFos = new FileOutputStream(decryptedFile);
        s3is = new CipherInputStream(s3is, dCipher);
        int dnumRead = 0;
        byte[] dbuf = new byte[4096];
        while ((dnumRead = s3is.read(dbuf)) >= 0) {
            decryptedTextFos.write(dbuf, 0, dnumRead);
        }
        decryptedTextFos.close();
    }
}
