package general;

import java.security.*;
import javax.swing.JOptionPane;
import java.io.UnsupportedEncodingException;

/**
 * 
 * MD5's the user's banlist.nl and forum password for storage
 * @author JPD
 */
public class MD5Password {

    /**
	 * Encrypts a string using md5
	 * @param pwd --a String password
	 * @return : a text representation of the md5 hash (in hex)
	 */
    public String encrypt(String pwd) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
        try {
            md5.update(pwd.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "That is not a valid encrpytion type");
        }
        byte raw[] = md5.digest();
        String empty = "";
        String hash = "";
        for (byte b : raw) {
            String tmp = empty + Integer.toHexString(b & 0xff);
            if (tmp.length() == 1) {
                tmp = 0 + tmp;
            }
            hash += tmp;
        }
        return hash;
    }
}
