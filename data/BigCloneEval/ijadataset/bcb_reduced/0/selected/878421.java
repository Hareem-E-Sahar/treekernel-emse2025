package org.eclipse.emf.ecore.resource.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.emf.ecore.resource.URIConverter;

/**
 * <p>EMF implementation for the {@link URIConverter.Cipher} interface using 
 * the AES encryption algorithm.</p>  
 * <p>This shows how this class can be used:</p>
 * <pre>
 * Map options = new HashMap();
 * options.put(Resource.OPTION_CIPHER, 
 *             new AESCipherImpl("12345")); // "That's amazing! I've got the same combination on my luggage!"
 * resource.save(options);
 * resource.load(options);
 * </pre> 
 */
public class AESCipherImpl implements URIConverter.Cipher {

    private static final String ENCRYPTION_ALGORITHM = "AES/CFB8/PKCS5Padding";

    private static final int ENCRYPTION_IV_LENGTH = 16;

    private static final String ENCRYPTION_KEY_ALGORITHM = "AES";

    private static final String PBE_ALGORITHM = "PBEWithMD5AndDES";

    private static final int PBE_IV_LENGTH = 8;

    private static final int PBE_ITERATIONS = 1000;

    private static KeyGenerator keygen;

    private static SecureRandom random;

    private static Key generateKey(int keysize) {
        if (keygen == null) {
            try {
                keygen = KeyGenerator.getInstance(ENCRYPTION_KEY_ALGORITHM);
                keygen.init(keysize);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return keygen.generateKey();
    }

    private static byte[] randomBytes(int length) {
        if (random == null) {
            random = new SecureRandom();
        }
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    private static byte[] readBytes(int length, InputStream in) throws Exception {
        byte[] bytes = new byte[length];
        int read = in.read(bytes);
        if (read != length) {
            throw new Exception("expected length != actual length");
        }
        return bytes;
    }

    private static byte[] transformWithPassword(byte[] bytes, byte[] iv, String password, int mode) throws Exception {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(iv, PBE_ITERATIONS);
        Cipher keyCipher = Cipher.getInstance(PBE_ALGORITHM);
        keyCipher.init(mode, pbeKey, pbeParamSpec);
        return keyCipher.doFinal(bytes);
    }

    private String password;

    private Key key;

    private int keysize = 128;

    private byte[] encryptedKeyBytes;

    private byte[] pbeIV;

    private byte[] encryptionIV;

    public AESCipherImpl(String password) throws Exception {
        this.password = password;
    }

    /**
   * <p>Sets the key size to be used when creating the AES key. Using anything 
   * larger than 128 may make the data file non-portable.</p>
   * <p>The key size cannot be changed after this Cipher is used.</p>
   */
    public void setKeysize(int keysize) {
        if (key == null) {
            this.keysize = keysize;
        }
    }

    public int getKeysize() {
        return keysize;
    }

    public OutputStream encrypt(OutputStream outputStream) throws Exception {
        if (key == null) {
            key = generateKey(getKeysize());
            pbeIV = randomBytes(PBE_IV_LENGTH);
            encryptionIV = randomBytes(ENCRYPTION_IV_LENGTH);
            encryptedKeyBytes = transformWithPassword(key.getEncoded(), pbeIV, password, Cipher.ENCRYPT_MODE);
        }
        outputStream.write(pbeIV);
        outputStream.write(encryptionIV);
        outputStream.write(encryptedKeyBytes.length);
        outputStream.write(encryptedKeyBytes);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(encryptionIV));
        outputStream = new FilterOutputStream(outputStream) {

            @Override
            public void close() throws IOException {
            }
        };
        return new CipherOutputStream(outputStream, cipher);
    }

    public void finish(OutputStream out) throws Exception {
        out.close();
    }

    public InputStream decrypt(InputStream in) throws Exception {
        byte[] pbeIV = readBytes(PBE_IV_LENGTH, in);
        byte[] encryptionIV = readBytes(ENCRYPTION_IV_LENGTH, in);
        int keyLength = in.read();
        byte[] encryptedKeyBytes = readBytes(keyLength, in);
        byte[] decryptedKeyBytes = transformWithPassword(encryptedKeyBytes, pbeIV, password, Cipher.DECRYPT_MODE);
        Key key = new SecretKeySpec(decryptedKeyBytes, ENCRYPTION_KEY_ALGORITHM);
        if (this.key == null) {
            this.pbeIV = pbeIV;
            this.encryptionIV = encryptionIV;
            this.encryptedKeyBytes = encryptedKeyBytes;
            this.key = key;
        }
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(encryptionIV));
        return new CipherInputStream(in, cipher);
    }

    public void finish(InputStream in) throws Exception {
    }
}
