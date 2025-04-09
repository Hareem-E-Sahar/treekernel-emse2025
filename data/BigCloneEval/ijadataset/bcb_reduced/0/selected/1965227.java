package org.sss.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.IOUtils;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 加密工具
 * @author Jason.Hoo (latest modification by $Author: hujianxin78728 $)
 * @version $Revision: 406 $ $Date: 2009-06-10 08:09:48 -0400 (Wed, 10 Jun 2009) $
 */
public class EncryptUtils {

    public static final String encodeDES(byte[] key, String input, String charset) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        BASE64Encoder encoder = new BASE64Encoder();
        KeyGenerator gen = KeyGenerator.getInstance("DES");
        gen.init(new SecureRandom(key));
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, gen.generateKey());
        return encoder.encode(cipher.doFinal(input.getBytes(charset)));
    }

    public static final String decodeDES(byte[] key, String input, String charset) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        KeyGenerator gen = KeyGenerator.getInstance("DES");
        gen.init(new SecureRandom(key));
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, gen.generateKey());
        return new String(cipher.doFinal(decoder.decodeBuffer(input)), charset);
    }

    public static final String decodeAES(byte[] key, String input, String charset) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(new SecureRandom(key));
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, gen.generateKey());
        return new String(cipher.doFinal(decoder.decodeBuffer(input)), charset);
    }

    public static final String encodeAES(String input) throws KeyczarException {
        String mask = "                 ";
        if (input.length() < 16) input += mask.substring(1, 17 - input.length());
        return new Crypter("aeskeys").encrypt(input);
    }

    public static final String decodeAES(String input) throws KeyczarException {
        return new Crypter("aeskeys").decrypt(input).trim();
    }

    public static final void main(String[] argv) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(new SecureRandom("656F43C3E8F28DE517EF20BA3742424F".getBytes("ISO8859-1")));
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, gen.generateKey());
            IOUtils.write(cipher.doFinal("1234567890ABCDEF".getBytes("ISO8859-1")), new FileOutputStream("C:\\aes\\target.aes"));
            cipher.init(Cipher.DECRYPT_MODE, gen.generateKey());
            IOUtils.write(cipher.doFinal("1234567890ABCDEF".getBytes("ISO8859-1")), new FileOutputStream("C:\\aes\\target.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
