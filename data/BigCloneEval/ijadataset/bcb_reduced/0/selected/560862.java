package sevs.crypto;

import java.io.*;
import java.util.Properties;
import java.security.*;

/**
 * Generates an RSA key pair. The generated key pair
 * will be used by clients to authenticate the server.
 */
public class KeyGenerator {

    /** RSA key length. */
    private static int keyLength = 512;

    public static void main(String[] args) throws Exception {
        java.security.Security.addProvider(new cryptix.jce.provider.CryptixCrypto());
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA", "CryptixCrypto");
        keygen.initialize(keyLength, new SecureRandom());
        KeyPair pair = keygen.generateKeyPair();
        writeToFile("key.pub", pair.getPublic());
        Properties keyStore = new Properties();
        keyStore.put("key.prv", pair.getPrivate());
        KeyStore.save(keyStore);
        System.out.println("key.pub and the server keystore file are generated");
    }

    /**
     * Writes a specified key into the specified file.
     */
    private static void writeToFile(String fileName, Key key) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
        oos.writeObject(key);
        oos.close();
    }
}
