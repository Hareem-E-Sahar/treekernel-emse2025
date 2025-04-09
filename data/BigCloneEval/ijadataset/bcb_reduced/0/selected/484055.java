package de.cowabuja.pawotag.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Encryptor {

    private static PBEKeySpec pbeKeySpec;

    private static PBEParameterSpec pbeParamSpec;

    private static SecretKeyFactory keyFactory;

    private static Cipher pbeCipher;

    private static SecretKey pbeKey;

    private static byte[] salt = { (byte) 0x50, (byte) 0x61, (byte) 0x77, (byte) 0x6F, (byte) 0x74, (byte) 0x61, (byte) 0x67, (byte) 0x22 };

    static int count = 1024;

    public Encryptor(char[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        init(password);
    }

    public String encodeText(String clearText) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        byte[] cipherText = pbeCipher.doFinal(clearText.getBytes());
        return new BASE64Encoder().encode(cipherText);
    }

    public String decodeText(String cipherText) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] textToDecode = new BASE64Decoder().decodeBuffer(cipherText);
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        byte[] ciphertext = pbeCipher.doFinal(textToDecode);
        return new String(ciphertext);
    }

    public void encodeFile(File source, File dest, boolean deleteSourceAfterEncrypt) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        FileInputStream fis = new FileInputStream(source);
        BufferedInputStream bis = new BufferedInputStream(fis);
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        FileOutputStream fos = new FileOutputStream(dest);
        CipherOutputStream cos = new CipherOutputStream(fos, pbeCipher);
        for (int c; (c = bis.read()) != -1; ) {
            cos.write(c);
        }
        cos.close();
        if (deleteSourceAfterEncrypt == true) {
            FileManager.deleteFilesRecursive(source);
        }
    }

    public void decodeFile(File source, File dest, boolean deleteSourceAfterDecode) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        FileInputStream fis = new FileInputStream(source);
        CipherInputStream cis = new CipherInputStream(fis, pbeCipher);
        BufferedInputStream bis = new BufferedInputStream(cis);
        FileOutputStream fos = new FileOutputStream(dest);
        for (int c; (c = bis.read()) != -1; ) {
            fos.write(c);
        }
        fos.close();
        if (deleteSourceAfterDecode == true) {
            FileManager.deleteFilesRecursive(source);
        }
    }

    private void init(char[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());
        keyFactory = SecretKeyFactory.getInstance("PBEWITHMD5ANDDES", Security.getProvider("BC"));
        pbeCipher = Cipher.getInstance("PBEWITHMD5ANDDES");
        pbeParamSpec = new PBEParameterSpec(salt, count);
        setPassword(password);
    }

    private void setPassword(char[] password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        pbeKeySpec = new PBEKeySpec(password);
        pbeKey = keyFactory.generateSecret(pbeKeySpec);
    }
}
