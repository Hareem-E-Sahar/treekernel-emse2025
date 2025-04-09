package com.nimbusinformatics.genomicstransfer;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import com.nimbusinformatics.genomicstransfer.util.Strings;

/**
 * Represents application settings.
 */
public final class Settings implements Cloneable, Serializable {

    private static final long serialVersionUID = 4044222313049893225L;

    /**
   * Algorithm used to encrypt data.
   */
    private static final String DATA_ENCRYPTION_ALGORITHM = "DESede";

    /**
   * Algorithm used to encrypt settings.
   */
    private static final String SETTINGS_ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES";

    private static final String FILE_NAME = ".genomicstransfer/settings";

    private static Settings instance;

    private transient char[] password;

    private String s3AccessKey;

    private String s3BucketName;

    private String s3SecretKey;

    private Key dataEncryptionKey;

    private Settings() {
    }

    /**
   * Generates data encryption key.
   * 
   * @return BASE64-encoded data encryption key
   */
    public static String generateDataEncryptionKeyString() {
        try {
            return generateDataEncryptionKeyStringInternal();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
   * Gets current settings instance.
   * 
   * @return current settings instance
   */
    public static Settings getInstance() {
        return instance;
    }

    /**
   * Validates whether the data encryption key is valid.
   * 
   * @param value
   *          BASE64-encoded data encryption key
   * @return {@code true} if the key is valid
   */
    public static boolean isDataEncryptionKeyStringValid(String value) {
        Base64 base64 = getBase64();
        byte[] bytes = base64.decode(value);
        if (!base64.encodeToString(bytes).equals(value)) {
            return false;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(bytes, DATA_ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (IllegalArgumentException e) {
            return false;
        } catch (InvalidKeyException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
        return true;
    }

    /**
   * Loads settings from file.
   * 
   * @param ui
   *          UI callback
   * @return {@code true} is settings have been loaded successfully
   */
    public static boolean load(SettingsUI ui) {
        try {
            instance = loadInternal(ui);
            return instance != null;
        } catch (Exception e) {
            throw new RuntimeException(Strings.getErrorLoadingSettings(e.getLocalizedMessage()), e);
        }
    }

    /**
   * Creates and initializes cipher to decrypt data.
   * 
   * @return decrypt cipher
   */
    public Cipher createDecryptCipher() {
        try {
            return createDecryptCipherInternal();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
   * Creates and initializes cipher to encrypt data.
   * 
   * @return encrypt cipher
   */
    public Cipher createEncryptCipher() {
        try {
            return createEncryptCipherInternal();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3AccessKey() {
        return s3AccessKey;
    }

    public String getS3SecretKey() {
        return s3SecretKey;
    }

    /**
   * Gets data encryption key.
   * 
   * @return BASE64-encoded key
   */
    public String getDataEncryptionKeyString() {
        if (dataEncryptionKey == null) {
            return null;
        }
        byte[] bytes = dataEncryptionKey.getEncoded();
        return getBase64().encodeToString(bytes);
    }

    /**
   * Edits the settings.
   * 
   * @param ui
   *          UI callback
   */
    public void edit(SettingsUI ui) {
        try {
            editInternal(ui);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public void setPassword(char[] password) {
        this.password = password.clone();
    }

    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public void setS3AccessKey(String s3AccessKey) {
        this.s3AccessKey = s3AccessKey;
    }

    public void setS3SecretKey(String s3SecretKey) {
        this.s3SecretKey = s3SecretKey;
    }

    /**
   * Sets data encryption key.
   * 
   * @param value
   *          BASE64-encoded key
   */
    public void setDataEncryptionKeyString(String value) {
        byte[] bytes = getBase64().decode(value);
        dataEncryptionKey = new SecretKeySpec(bytes, DATA_ENCRYPTION_ALGORITHM);
    }

    /**
   * Creates new settings.
   * 
   * @param ui
   *          UI callback
   * @return new settings, or {@code null} if user canceled settings UI
   * @throws Exception .
   */
    private static Settings createNew(SettingsUI ui) throws Exception {
        Settings settings = new Settings();
        if (!ui.editSettings(settings, true)) {
            return null;
        }
        settings.write();
        return settings;
    }

    private static String generateDataEncryptionKeyStringInternal() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance(DATA_ENCRYPTION_ALGORITHM);
        Key secretKey = generator.generateKey();
        byte[] bytes = secretKey.getEncoded();
        return getBase64().encodeToString(bytes);
    }

    private static Base64 getBase64() {
        return new Base64(Integer.MAX_VALUE, new byte[0]);
    }

    private static File getFile() {
        return new File(System.getProperty("user.home"), FILE_NAME);
    }

    private static Settings loadInternal(SettingsUI ui) throws Exception {
        File file = getFile();
        if (!file.exists()) {
            return createNew(ui);
        }
        while (true) {
            char[] password = ui.askPassword();
            if (password == null) {
                return null;
            }
            try {
                return read(password);
            } catch (InvalidKeyException e) {
                ui.error(Strings.getInvalidPassword());
            }
        }
    }

    /**
   * Reads settings file decrypting it using the specified password.
   * 
   * @param password
   *          password to use to decrypt settings file
   * @return settings
   * @throws Exception .
   */
    private static Settings read(char[] password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SETTINGS_ENCRYPTION_ALGORITHM);
        SecretKey passwordKey = factory.generateSecret(new PBEKeySpec(password));
        ObjectInputStream privateStream = new ObjectInputStream(FileUtils.openInputStream(getFile()));
        try {
            SealedObject sealed = (SealedObject) privateStream.readObject();
            Settings settings = (Settings) sealed.getObject(passwordKey);
            settings.password = password.clone();
            return settings;
        } finally {
            privateStream.close();
        }
    }

    private Cipher createDecryptCipherInternal() throws Exception {
        Cipher cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, dataEncryptionKey);
        return cipher;
    }

    private Cipher createEncryptCipherInternal() throws Exception {
        Cipher cipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, dataEncryptionKey);
        return cipher;
    }

    private void editInternal(SettingsUI ui) throws Exception {
        while (true) {
            char[] password = ui.askPassword();
            if (password == null) {
                return;
            }
            if (Arrays.equals(this.password, password)) {
                break;
            }
            ui.error(Strings.getInvalidPassword());
        }
        Settings clone = (Settings) this.clone();
        if (!ui.editSettings(clone, false)) {
            return;
        }
        setPassword(clone.password);
        setS3AccessKey(clone.getS3AccessKey());
        setS3BucketName(clone.getS3BucketName());
        setS3SecretKey(clone.getS3SecretKey());
        setDataEncryptionKeyString(clone.getDataEncryptionKeyString());
        write();
    }

    /**
   * Writes settings to settings file encrypting it with password.
   * 
   * @throws Exception .
   */
    private void write() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SETTINGS_ENCRYPTION_ALGORITHM);
        SecretKey passwordKey = factory.generateSecret(new PBEKeySpec(password));
        Cipher cipher = Cipher.getInstance(SETTINGS_ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, passwordKey);
        SealedObject object = new SealedObject(this, cipher);
        ObjectOutputStream privateStream = new ObjectOutputStream(FileUtils.openOutputStream(getFile()));
        try {
            privateStream.writeObject(object);
        } finally {
            privateStream.close();
        }
    }
}
