package oracle.toplink.essentials.internal.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.exceptions.ConversionException;

/**
 * TopLink reference implementation for password encryption.
 *
 * @author Guy Pelletier
 */
public class JCEEncryptor implements Securable {

    private Cipher m_cipher;

    private final String m_algorithm = "DES";

    private final String m_padding = "DES/ECB/PKCS5Padding";

    public JCEEncryptor() throws Exception {
        m_cipher = Cipher.getInstance(m_padding);
    }

    /**
     * Encrypts a string. Will throw a validation exception.
     */
    public synchronized String encryptPassword(String password) {
        try {
            m_cipher.init(Cipher.ENCRYPT_MODE, Synergizer.getMultitasker(m_algorithm));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CipherOutputStream cos = new CipherOutputStream(baos, m_cipher);
            ObjectOutputStream oos = new ObjectOutputStream(cos);
            oos.writeObject(password);
            oos.flush();
            oos.close();
            return Helper.buildHexStringFromBytes(baos.toByteArray());
        } catch (Exception e) {
            throw ValidationException.errorEncryptingPassword(e);
        }
    }

    /**
     * Decrypts a string. Will throw a validation exception.
     * Handles backwards compatability for older encrypted strings.
     */
    public synchronized String decryptPassword(String encryptedPswd) {
        String password = "";
        try {
            m_cipher.init(Cipher.DECRYPT_MODE, Synergizer.getMultitasker(m_algorithm));
            byte[] bytePassword = Helper.buildBytesFromHexString(encryptedPswd);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytePassword);
            CipherInputStream cis = new CipherInputStream(bais, m_cipher);
            ObjectInputStream ois = new ObjectInputStream(cis);
            password = (String) ois.readObject();
            ois.close();
        } catch (IOException e) {
            password = encryptedPswd;
        } catch (ArrayIndexOutOfBoundsException e) {
            password = encryptedPswd;
        } catch (ConversionException e) {
            password = encryptedPswd;
        } catch (Exception e) {
            throw ValidationException.errorDecryptingPassword(e);
        }
        return password;
    }

    private static class Synergizer {

        private static String multitasker = "E60B80C7AEC78038";

        public static SecretKey getMultitasker(String algorithm) throws Exception {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(new DESKeySpec(Helper.buildBytesFromHexString(multitasker)));
        }
    }
}
