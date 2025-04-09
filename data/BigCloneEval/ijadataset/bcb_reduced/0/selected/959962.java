package org.apache.geronimo.crypto;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import org.apache.geronimo.crypto.encoders.Base64;

/**
 * @version $Rev: 653740 $ $Date: 2008-05-06 12:44:18 +0200 (Tue, 06 May 2008) $
 */
public abstract class AbstractEncryption implements Encryption {

    /**
	 * Gets a String which contains the Base64-encoded form of the source,
	 * encrypted with the key from getSecretKeySpec().
	 */
    public String encrypt(Serializable source) {
        SecretKeySpec spec = getSecretKeySpec();
        try {
            Cipher c = Cipher.getInstance(spec.getAlgorithm());
            c.init(Cipher.ENCRYPT_MODE, spec);
            SealedObject so = new SealedObject(source, c);
            ByteArrayOutputStream store = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(store);
            out.writeObject(so);
            out.close();
            byte[] data = store.toByteArray();
            byte[] textData = Base64.encode(data);
            return new String(textData, "US-ASCII");
        } catch (Exception e) {
            return null;
        }
    }

    /**
	 * Given a String which is the Base64-encoded encrypted data, retrieve the
	 * original Object.
	 */
    public Serializable decrypt(String source) {
        SecretKeySpec spec = getSecretKeySpec();
        try {
            byte[] data = Base64.decode(source);
            Cipher c = Cipher.getInstance(spec.getAlgorithm());
            c.init(Cipher.DECRYPT_MODE, spec);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            SealedObject so = (SealedObject) in.readObject();
            return (Serializable) so.getObject(c);
        } catch (Exception e) {
            return null;
        }
    }

    protected abstract SecretKeySpec getSecretKeySpec();
}
