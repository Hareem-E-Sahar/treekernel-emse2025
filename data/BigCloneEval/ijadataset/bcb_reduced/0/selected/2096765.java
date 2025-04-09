package prajna.util;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import java.util.Map;
import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Encrypts a property map of strings to a file, or decrypts the property map
 * from the file. This class uses a triple-DES (DES-ede) algorithm for
 * encryption. The key provided for decryption must match the key used for
 * encryption.
 * 
 * @author <a href="http://www.ganae.com/edswing">Edward Swing</a>
 */
public class PropertyEncryptor {

    private Cipher cipher;

    private SecretKey cipherKey;

    /**
     * Create a new propertyEncryptor
     */
    public PropertyEncryptor() {
        try {
            cipher = Cipher.getInstance("DESede");
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Decrypt the property map from the provided file.
     * 
     * @param file the file to read
     * @return the property map
     * @throws IOException if there is a problem reading the file or decrypting
     *             it.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> decryptFromFile(File file) throws IOException {
        if (cipher == null || cipherKey == null) {
            throw new IllegalStateException("Cipher not properly initialized");
        }
        Map<String, String> propMap = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, cipherKey);
            CipherInputStream cis = new CipherInputStream(new FileInputStream(file), cipher);
            ObjectInputStream ois = new ObjectInputStream(cis);
            propMap = (Map<String, String>) ois.readObject();
            ois.close();
        } catch (InvalidKeyException exc) {
            throw new IllegalStateException(exc);
        } catch (ClassNotFoundException exc) {
        }
        return propMap;
    }

    /**
     * Decrypt the property map from the provided file.
     * 
     * @param propMap the property map
     * @param file the file to read
     * @throws IOException if there is a problem reading the file or decrypting
     *             it.
     */
    public void encryptToFile(Map<String, String> propMap, File file) throws IOException {
        if (cipher == null || cipherKey == null) {
            throw new IllegalStateException("Cipher not properly initialized");
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
            CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(file), cipher);
            ObjectOutputStream oos = new ObjectOutputStream(cos);
            oos.writeObject(propMap);
            oos.flush();
            oos.close();
            cos.close();
        } catch (InvalidKeyException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * Set the key. The key string is used to create the underlying secret key
     * 
     * @param key key string for the secret key
     */
    public void setKey(String key) {
        if (cipher == null) {
            throw new IllegalStateException("Cipher not properly initialized");
        }
        try {
            String fullKey = key;
            if (key.length() < 24) {
                fullKey = key + "qwertyuiopasdfghjklzxcvbnm";
            }
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(cipher.getAlgorithm());
            KeySpec spec = new DESedeKeySpec(fullKey.getBytes());
            cipherKey = keyFac.generateSecret(spec);
        } catch (Exception exc) {
            throw new IllegalStateException(exc);
        }
    }
}
