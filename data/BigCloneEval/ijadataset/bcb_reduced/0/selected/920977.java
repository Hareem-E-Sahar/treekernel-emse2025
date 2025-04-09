package ncr.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import ncr.exception.NcrException;
import ncr.utils.Helper;
import ncr.utils.NcrConstants;

/**
 *
 * @author nnatarajan
 */
public class KeyHandler {

    private File objFile = null;

    private byte iv[] = { 14, -12, 87, 37, 117, -114, 11, 70 };

    private ObjectOutputStream keyStream;

    private SecretKey fileKey;

    private HashMap<String, String> fileMap;

    public KeyHandler(String folderPath) {
        objFile = new File((new StringBuilder()).append(folderPath).append(File.separator + NcrConstants.OBJ_FILE).toString());
        fileMap = new HashMap<String, String>();
    }

    public HashMap<String, String> getNameValues() {
        return fileMap;
    }

    public ObjectOutputStream getKeyStream() {
        return keyStream;
    }

    public SecretKey getKey() {
        return fileKey;
    }

    public void createKeyFile(String password) throws NcrException {
        PBEParameterSpec parameterSpec;
        SecretKey key;
        OutputStream out = null;
        try {
            Cipher ecipher = Cipher.getInstance(NcrConstants.PBE_ALGO);
            parameterSpec = new PBEParameterSpec(iv, 100);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(NcrConstants.PBE_ALGO);
            key = keyFactory.generateSecret(keySpec);
            ecipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            out = new FileOutputStream(objFile);
            out = new CipherOutputStream(out, ecipher);
            keyStream = new ObjectOutputStream(out);
            KeyGenerator keygen = KeyGenerator.getInstance(NcrConstants.FILE_ALGO);
            fileKey = keygen.generateKey();
            keyStream.writeObject(fileKey);
            keyStream.flush();
            this.writeKeyData(keyStream, "version", NcrConstants.VERSION);
            this.writeKeyData(keyStream, "pbeAlgo", NcrConstants.PBE_ALGO);
            this.writeKeyData(keyStream, "fileAlgo", NcrConstants.FILE_ALGO);
        } catch (Exception ex) {
            Helper.close(out);
            throw new NcrException("Error while creating key file.");
        }
    }

    public void deleteKeyfile() {
        if (objFile != null) {
            objFile.delete();
        }
    }

    public void closeKeyHandler() {
        Helper.close(keyStream);
    }

    public void loadKey(String password) throws NcrException {
        InputStream in = null;
        ObjectInputStream s = null;
        try {
            if (!objFile.exists()) {
                throw new NcrException("Key file does not exist.");
            }
            Cipher dcipher = Cipher.getInstance(NcrConstants.PBE_ALGO);
            PBEParameterSpec parameterSpec = new PBEParameterSpec(iv, 100);
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(NcrConstants.PBE_ALGO);
            SecretKey key = keyFactory.generateSecret(keySpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            in = new FileInputStream(objFile);
            in = new CipherInputStream(in, dcipher);
            s = new ObjectInputStream(in);
            fileKey = (SecretKey) s.readObject();
            loadNameValues(s);
        } catch (Exception e) {
            throw new NcrException("Incorrect password.", e);
        } finally {
            Helper.close(in);
            Helper.close(s);
        }
    }

    private void loadNameValues(ObjectInputStream s) {
        try {
            while (true) {
                NameValue data = (NameValue) s.readObject();
                fileMap.put(data.getName(), data.getValue());
            }
        } catch (Exception e) {
        }
    }

    private void writeKeyData(ObjectOutputStream keyStream, String name, String value) throws IOException {
        NameValue val = new NameValue(name, value);
        keyStream.writeObject(val);
        keyStream.flush();
    }
}
