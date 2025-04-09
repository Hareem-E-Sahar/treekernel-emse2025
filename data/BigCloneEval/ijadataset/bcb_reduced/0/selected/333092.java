package org.apps.butler.util.encypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apps.butler.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * This class provides APIs to encrypt and decrypt object with cryptographic
 * algorithm DES/ECB/PKCS5Padding. And use base64 to serialize the encoded data
 * to String. <br>
 * <br>
 * Example: <br>
 *
 * <pre>
 * HashMap&lt;String, Object&gt; map = new HashMap&lt;String, Object&gt;();
 * map.put(&quot;testkey1&quot;, &quot;my value1&quot;);
 * map.put(&quot;testkey2&quot;, &quot;my value2&quot;);
 * String encryptedStr = DesEncryptUtil.encryptBase64(key, map);
 * .....
 * HashMap&lt;String, Object&gt; map = DesEncryptUtil.decryptBase64(key, encryptedStr);
 * </pre>
 */
public class DesEncryptUtil {

    private static final Logger logger = LoggerFactory.getLogger(DesEncryptUtil.class);

    private static final String SECURE_FOLDRE = ".encrypt";

    private static final String SECURE_KEY_FILE_NAME = "secure";

    private static final String SECURE_PASSWORD_FILE_NAME = "passwd";

    private DesEncryptUtil() {
    }

    /**
	 * Encrypt an object to byte array with default secure key file and use
	 * base64 to encode this byte array to String.
	 *
	 * @param object
	 *            the object to be encrypted
	 * @return encrypted string
	 * @throws Exception
	 */
    public static String encrypt(Object object) throws Exception {
        return encrypt(object, null);
    }

    /**
	 * Encrypt an object to byte array with secure key stored in keyFile and use
	 * base64 to encode this byte array to String.
	 *
	 * @param object
	 *            the object to be encrypted
	 * @param keyFile
	 *            the secure key file.
	 * @return encrypted string
	 * @throws Exception
	 */
    public static String encrypt(Object object, File keyFile) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("object: " + object);
        }
        if (keyFile == null) {
            keyFile = getDefaultFile(SECURE_KEY_FILE_NAME);
        }
        byte[] buf = encryptToByteArray(object, keyFile);
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encodeBuffer(buf);
    }

    /**
	 * Decrypt encrypted string to object with default secure key.
	 *
	 * @param encryptedStr
	 *            the encrypted string to be descrypted
	 * @return The original object.
	 * @throws Exception
	 */
    public static Object decrypt(String encryptedStr) throws Exception {
        return decrypt(encryptedStr, null);
    }

    /**
	 * Decrypt encrypted string to object with secure key stored in keyFile.
	 *
	 * @param encryptedStr
	 *            the encrypted string to be descrypted
	 * @param keyFile
	 *            the secure key file
	 * @return The original object.
	 * @throws Exception
	 */
    public static Object decrypt(String encryptedStr, File keyFile) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("encryptedStr: " + encryptedStr);
        }
        if (keyFile == null) {
            keyFile = getDefaultFile(SECURE_KEY_FILE_NAME);
        }
        byte[] buf = null;
        BASE64Decoder decoder = new BASE64Decoder();
        buf = decoder.decodeBuffer(encryptedStr);
        Object obj = decryptFromByteArray(buf, keyFile);
        return obj;
    }

    /**
	 * Encrypt an object to byte array with secure key stored in keyFile
	 *
	 * @param obj
	 *            the object to be encrypted
	 * @return encrypted byte array
	 * @throws Exception
	 */
    private static byte[] encryptToByteArray(Object obj, File keyFile) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, keyFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CipherOutputStream cos = new CipherOutputStream(bos, cipher);
        ObjectOutputStream oos = new ObjectOutputStream(cos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        byte[] buf = bos.toByteArray();
        return buf;
    }

    /**
	 * Decrypt the byte array to original object with key stored in keyFile.
	 *
	 * @param buf
	 *            byte array.
	 * @return The original object.
	 * @throws Exception
	 */
    private static Object decryptFromByteArray(byte[] buf, File keyFile) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, keyFile);
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        CipherInputStream cis = new CipherInputStream(bis, cipher);
        ObjectInputStream ois = new ObjectInputStream(cis);
        Object obj = (Object) ois.readObject();
        ois.close();
        return obj;
    }

    /**
	 * Get the cipher with specified key and mode.
	 *
	 * @param mode
	 *            Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param keyFile
	 *            The secure file
	 * @return The cipher object.
	 * @throws Exception
	 */
    private static Cipher getCipher(int mode, File keyFile) throws Exception {
        byte keyArr[] = getSecureKey(keyFile);
        DESKeySpec desKeySpec = new DESKeySpec(keyArr);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(mode, secretKey);
        return cipher;
    }

    /**
	 * Read secure key from file. If the key file doesn't exist, new secure key
	 * file is generated.
	 *
	 * @param keyFile
	 *            the secure key file
	 * @return The byte array of secure key.
	 * @throws Exception
	 */
    private static byte[] getSecureKey(File keyFile) throws Exception {
        if (!keyFile.exists()) {
            SecureRandom secRandom = new SecureRandom();
            KeyGenerator keyGen = KeyGenerator.getInstance("DES");
            keyGen.init(secRandom);
            SecretKey secKey = keyGen.generateKey();
            byte[] secKeyArray = secKey.getEncoded();
            FileUtil.writeFile(keyFile, secKeyArray);
        }
        return FileUtil.readFile(keyFile);
    }

    /**
	 * Get file in default secure folder
	 *
	 * @param fileName
	 *            The file name
	 * @return The file in default secure folder.
	 */
    private static File getDefaultFile(String fileName) {
        File secFile = new File(SECURE_FOLDRE + File.separator + fileName);
        return secFile;
    }

    /**
	 * Encrypt user password and store in default password file.
	 *
	 * @param userName
	 *            The user name which need to encrypt password
	 * @param password
	 *            The password to be encrypted
	 * @throws Exception
	 */
    public static void encryptUserPassword(String userName, String password) throws Exception {
        encryptUserPassword(userName, password, null);
    }

    /**
	 * Encrypt user password and store in password file.
	 *
	 * @param userName
	 *            The user name which need to encrypt password
	 * @param password
	 *            The password to be encrypted
	 * @param passwordFile
	 *            The file to store encrypted password.
	 * @throws Exception
	 */
    public static void encryptUserPassword(String userName, String password, File passwordFile) throws Exception {
        encryptUserPassword(userName, password, passwordFile, null);
    }

    /**
	 * Encrypt user password and store in password file.
	 *
	 * @param userName
	 *            The user name which need to encrypt password
	 * @param password
	 *            The password to be encrypted
	 * @param passwordFile
	 *            The file to store encrypted password.
	 * @param secKeyFile
	 *            The secure key file
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public static void encryptUserPassword(String userName, String password, File passwordFile, File secKeyFile) throws Exception {
        File encryptFile = passwordFile == null ? getDefaultFile(SECURE_PASSWORD_FILE_NAME) : passwordFile;
        Map<String, String> map = null;
        if (encryptFile.exists()) {
            map = (Map<String, String>) decrypt(new String(FileUtil.readFile(encryptFile)));
        }
        if (map == null) {
            map = new HashMap<String, String>();
        }
        map.put(userName, password);
        FileUtil.writeFile(encryptFile, encrypt(map, secKeyFile));
    }

    /**
	 * Decrypt user password from default password file
	 *
	 * @param userName
	 *            The user name which need to decrypt password
	 * @return The descrypted password of the user
	 * @throws Exception
	 */
    public static String decryptUserPassword(String userName) throws Exception {
        return decryptUserPassword(userName, null);
    }

    /**
	 * Decrypt user password from password file
	 *
	 * @param userName
	 *            The user name which need to decrypt password
	 * @param passwordFile
	 *            The file to store encrypted password.
	 * @return The descrypted password of the user
	 * @throws Exception
	 */
    public static String decryptUserPassword(String userName, File passwordFile) throws Exception {
        return decryptUserPassword(userName, passwordFile, null);
    }

    /**
	 * Decrypt user password from password file
	 *
	 * @param userName
	 *            The user name which need to decrypt password
	 * @param passwordFile
	 *            The file to store encrypted password.
	 * @param secKeyFile
	 *            The secure key file
	 * @return The descrypted password of the user
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public static String decryptUserPassword(String userName, File passwordFile, File secKeyFile) throws Exception {
        File encryptFile = passwordFile == null ? getDefaultFile(SECURE_PASSWORD_FILE_NAME) : passwordFile;
        Map<String, String> map = (Map<String, String>) decrypt(new String(FileUtil.readFile(encryptFile)), secKeyFile);
        String password = null;
        if (map == null || (password = map.get(userName)) == null) {
            throw new Exception("Can't find user: " + userName);
        }
        return password;
    }

    public static void main(String[] args) {
        try {
            String password = "haijun pass3";
            System.out.println("haijun:" + password);
            encryptUserPassword("haijun", password);
            System.out.println(decryptUserPassword("haijun"));
            password = "haijun pass2";
            System.out.println("test1:" + password);
            encryptUserPassword("test1", password);
            System.out.println(decryptUserPassword("test1"));
            password = "haijun pass1";
            System.out.println("haijun1:" + password);
            encryptUserPassword("haijun1", password);
            System.out.println(decryptUserPassword("haijun1"));
            System.out.println(decryptUserPassword("haijun3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
