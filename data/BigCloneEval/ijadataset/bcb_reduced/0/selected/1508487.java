package br.com.caelum.jambo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe auixiliar para criptografar strings por MD5.
 * 
 * Partes dessa classe foram retiradas de algum lugar da net.
 * 
 * @author Rafael Steil
 * @author Paulo Silveira
 */
public class MD5 {

    private static MessageDigest digester;

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String crypt(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }
        digester.update(str.getBytes());
        byte[] hash = digester.digest();
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return hexString.toString();
    }
}
