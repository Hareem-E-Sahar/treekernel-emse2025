package edu.unibi.agbi.dawismd.administration;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import edu.unibi.agbi.dawismd.config.logging.Log;

/**
 * @author Benny
 * @version 2.00 23.02.2006
 */
public class Coder {

    private final String PHRASE = new String("c07wZxYb");

    private final Key KEY = new SecretKeySpec(PHRASE.getBytes(), "DES");

    private Cipher ecipher;

    private Cipher dcipher;

    public Coder() {
        try {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
        } catch (Exception e) {
            Log.writeErrorLog(Coder.class, e.getMessage(), e);
        }
    }

    /**
     * @param text
     * @return encrypt
     */
    public String encrypt(String text) {
        String encrypt = new String();
        try {
            ecipher.init(Cipher.ENCRYPT_MODE, KEY);
            encrypt = new BASE64Encoder().encode(ecipher.doFinal(text.getBytes()));
        } catch (Exception e) {
            Log.writeWarningLog(Coder.class, e.getMessage(), e);
        }
        return encrypt;
    }

    /**
     * @param text
     * @return decrypt
     */
    public String decrypt(String text) {
        String decrypt = new String();
        try {
            dcipher.init(Cipher.DECRYPT_MODE, KEY);
            byte[] b64 = new BASE64Decoder().decodeBuffer(text);
            decrypt = new String(dcipher.doFinal(b64));
        } catch (Exception e) {
            Log.writeWarningLog(Coder.class, e.getMessage(), e);
        }
        return decrypt;
    }
}
