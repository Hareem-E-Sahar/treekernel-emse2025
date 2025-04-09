package lv.odylab.evemanage.security;

import lv.odylab.appengine.repackaged.Base64;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class KeyGenerator {

    public static void main(String args[]) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        KeyPair keyPair = generateKeyPair("RSA", 2048);
        saveToFile("public.key", keyPair.getPublic().getEncoded());
        saveToFile("private.key", keyPair.getPrivate().getEncoded());
    }

    public static KeyPair generateKeyPair(String algorithm, Integer keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
            keyPairGenerator.initialize(keyLength);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToFile(String fileName, byte[] bytes) throws IOException {
        OutputStream outputStream = new Base64.OutputStream(new FileOutputStream(fileName), Base64.ENCODE | Base64.DO_BREAK_LINES);
        try {
            outputStream.write(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            outputStream.close();
        }
    }
}
