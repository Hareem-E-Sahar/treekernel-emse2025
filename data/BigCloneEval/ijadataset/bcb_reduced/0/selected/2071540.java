package mx4j.tools.security.cerbero;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import mx4j.util.Utils;

/**
 *
 * @author <a href="mailto:biorn_steedom@users.sourceforge.net">Simone Bordet</a>
 * @version $Revision: 607 $
 */
public final class PasswordEncryptedObject implements Serializable {

    private byte[] encrypted;

    private static final byte[] salt = { (byte) 0xc8, (byte) 0x74, (byte) 0x22, (byte) 0x8d, (byte) 0x7f, (byte) 0xc9, (byte) 0xef, (byte) 0xa0 };

    private static final int iterations = 1000;

    private static final String algorithm = "PBEWithMD5AndDES";

    /**
	 * Encrypts the given serializable object with the given password.
	 * @throws GeneralSecurityException is the object is not encryptable
	 * @throws IOException if the object is not serializable
	 */
    public PasswordEncryptedObject(Object object, char[] password) throws GeneralSecurityException, IOException {
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey key = keyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        this.encrypted = cipher.doFinal(baos.toByteArray());
    }

    /**
	 * Decrypts the held object with the given password.
	 * @throws GeneralSecurityException is the object is not decryptable
	 * @throws IOException if serialized object cannot be deserialized
	 * @throws ClassNotFoundException if the serialized object class cannot be found
	 */
    public Object decrypt(char[] password) throws GeneralSecurityException, IOException, ClassNotFoundException {
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
        SecretKey key = keyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        ByteArrayInputStream bais = new ByteArrayInputStream(cipher.doFinal(encrypted));
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object = ois.readObject();
        ois.close();
        return object;
    }

    public int hashCode() {
        return Utils.arrayHashCode(encrypted);
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        try {
            PasswordEncryptedObject other = (PasswordEncryptedObject) obj;
            return Utils.arrayEquals(encrypted, other.encrypted);
        } catch (ClassCastException x) {
        }
        return false;
    }
}
