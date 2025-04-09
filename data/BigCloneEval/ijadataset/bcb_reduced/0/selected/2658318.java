package com.pjsofts.eurobudget.crypt;

import java.io.*;
import java.security.Key;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Arrays;

public class CryptPassword {

    /** Creates a new instance of Crypt */
    public CryptPassword() {
    }

    /**
     * decode method
     */
    public void decodePassword(File fromFile, File toFile, char[] password) {
        PBEKeySpec pbeKeySpec;
        PBEParameterSpec pbeParamSpec;
        SecretKeyFactory keyFac;
        SecretKey pbeKey;
        Cipher pbeCipher = null;
        byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
        int count = 20;
        pbeParamSpec = new PBEParameterSpec(salt, count);
        pbeKeySpec = new PBEKeySpec(password);
        try {
            keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            pbeKey = keyFac.generateSecret(pbeKeySpec);
            pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new CipherInputStream(new BufferedInputStream(new FileInputStream(fromFile)), pbeCipher);
            out = new BufferedOutputStream(new FileOutputStream(toFile));
            byte[] rbuffer = new byte[2056];
            int rcount = in.read(rbuffer);
            while (rcount > 0) {
                out.write(rbuffer, 0, rcount);
                rcount = in.read(rbuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
            }
        }
        System.out.println("Success: " + toFile.getName() + " generated.");
    }

    /**
     * encode method
     */
    public void encodePassword(File fromFile, File toFile, char[] password) {
        PBEKeySpec pbeKeySpec;
        PBEParameterSpec pbeParamSpec;
        SecretKeyFactory keyFac;
        SecretKey pbeKey;
        Cipher pbeCipher = null;
        byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
        int count = 20;
        pbeParamSpec = new PBEParameterSpec(salt, count);
        pbeKeySpec = new PBEKeySpec(password);
        try {
            keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            pbeKey = keyFac.generateSecret(pbeKeySpec);
            pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(fromFile));
            out = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(toFile)), pbeCipher);
            byte[] rbuffer = new byte[2056];
            int rcount = in.read(rbuffer);
            while (rcount > 0) {
                out.write(rbuffer, 0, rcount);
                rcount = in.read(rbuffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
            }
        }
        System.out.println("Success: " + toFile.getName() + " generated.");
    }

    /**
     * Reads user password from given input stream.
     * It would seem logical to collect and store the password in an object of type java.lang.String. However, here's the caveat: Objects of type String are immutable, i.e., there are no methods defined that allow you to change (overwrite) or zero out the contents of a String after usage. This feature makes String objects unsuitable for storing security sensitive information such as user passwords. You should always collect and store security sensitive information in a char array instead.
     * For that reason, the javax.crypto.spec.PBEKeySpec class takes (and returns) a password as a char array.
     * The following method is an example of how to collect a user password as a char array:
     */
    public static char[] readPasswd(InputStream in) throws IOException {
        char[] lineBuffer;
        char[] buf;
        int i;
        buf = lineBuffer = new char[128];
        int room = buf.length;
        int offset = 0;
        int c;
        loop: while (true) {
            switch(c = in.read()) {
                case -1:
                case '\n':
                    break loop;
                case '\r':
                    int c2 = in.read();
                    if ((c2 != '\n') && (c2 != -1)) {
                        if (!(in instanceof PushbackInputStream)) {
                            in = new PushbackInputStream(in);
                        }
                        ((PushbackInputStream) in).unread(c2);
                    } else break loop;
                default:
                    if (--room < 0) {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        Arrays.fill(lineBuffer, ' ');
                        lineBuffer = buf;
                    }
                    buf[offset++] = (char) c;
                    break;
            }
        }
        if (offset == 0) {
            return null;
        }
        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');
        return ret;
    }
}
