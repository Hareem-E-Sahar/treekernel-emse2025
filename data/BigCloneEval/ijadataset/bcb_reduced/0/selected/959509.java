package com.jguigen.secure;

import java.security.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.*;
import javax.crypto.spec.*;
import sun.misc.BASE64Encoder;
import java.io.*;

/**
 *  GHP This is taken from the WROX book Professional Java Security
 *	This class encrypts and decrypts a file using CipherStreams
 *	and a 256-bit Rijndael key stored in the filesystem.
 */
public class FileEncryptor {

    private static String KEY_FILENAME = "rijndaelkey.bin";

    private static String KEY_PATH = "";

    private static String fullString = "";

    private static int ITERATIONS = 1000;

    private static final boolean DEBUG = true;

    private static FileEncryptor instance;

    private FileEncryptor() {
    }

    public static void main(String[] args) throws Exception {
        if (DEBUG) {
            String[] types = getServiceTypes();
            System.out.println("Service Types");
            for (int i = 0; i < types.length; i++) {
                System.out.println(types[i]);
            }
            String[] names = getCryptoImpls("Cipher");
            System.out.println("\n\n\n" + "Crypto Implementations available");
            for (int i = 0; i < names.length; i++) {
                System.out.println(names[i]);
            }
        }
        char[] password = new char["testpw".length()];
        "testpw".getChars(0, "testpw".length(), password, 0);
        FileEncryptor fc = FileEncryptor.getInstance();
        fc.decrypt(password, "test.out", "test_decrypt.out");
        String outp = fc.decryptArray(password, "test.out");
        if (DEBUG) {
            System.out.println(outp);
            System.out.println("Finished");
        }
    }

    /**
   *	Creates a 256-bit Rijndael key and stores it to
   *	the filesystem as a KeyStore.
   */
    public synchronized void createKey(char[] password) throws Exception {
        if (DEBUG) {
            System.out.println("Generating a Rijndael key...");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("Rijndael");
        keyGenerator.init(128);
        Key key = keyGenerator.generateKey();
        if (DEBUG) {
            System.out.println("Done generating the key.");
        }
        byte[] salt = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, ITERATIONS);
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        byte[] encryptedKeyBytes = cipher.doFinal(key.getEncoded());
        String keyfile = KEY_FILENAME;
        if (KEY_PATH.trim().length() == 0) {
        } else {
            if (KEY_PATH.endsWith(File.separator)) {
                keyfile = KEY_PATH + KEY_FILENAME;
            } else {
                keyfile = KEY_PATH + File.separator + KEY_FILENAME;
            }
        }
        FileOutputStream fos = new FileOutputStream(keyfile);
        fos.write(salt);
        fos.write(encryptedKeyBytes);
        fos.close();
        BASE64Encoder encoder = new BASE64Encoder();
        String myString = encoder.encode(encryptedKeyBytes);
        if (DEBUG) {
            System.out.println(myString);
        }
    }

    /**
   *	Loads a key from the filesystem
   */
    public synchronized Key loadKey(char[] password) throws Exception {
        String keyfile = KEY_FILENAME;
        if (KEY_PATH.trim().length() == 0) {
        } else {
            if (KEY_PATH.endsWith(File.separator)) {
                keyfile = KEY_PATH + KEY_FILENAME;
            } else {
                keyfile = KEY_PATH + File.separator + KEY_FILENAME;
            }
        }
        FileInputStream fis = new FileInputStream(keyfile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        while ((i = fis.read()) != -1) {
            baos.write(i);
        }
        fis.close();
        byte[] saltAndKeyBytes = baos.toByteArray();
        baos.close();
        byte[] salt = new byte[8];
        System.arraycopy(saltAndKeyBytes, 0, salt, 0, 8);
        int length = saltAndKeyBytes.length - 8;
        byte[] encryptedKeyBytes = new byte[length];
        System.arraycopy(saltAndKeyBytes, 8, encryptedKeyBytes, 0, length);
        BASE64Encoder encoder = new BASE64Encoder();
        String myString = encoder.encode(encryptedKeyBytes);
        if (DEBUG) {
            System.out.println(myString);
        }
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, ITERATIONS);
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedKeyBytes);
        SecretKeySpec key = new SecretKeySpec(decryptedKeyBytes, "Rijndael");
        return key;
    }

    /** Allow us to change the name of the key file */
    public void setKeyFileName(String str) {
        this.KEY_FILENAME = str;
    }

    /** Allow us to change the path to the key file */
    public void setKeyPath(String str) {
        this.KEY_PATH = str;
    }

    /** Allow us to get the string that was encrypted into a file */
    public String getFullString() {
        return fullString;
    }

    /**
   *	Encrypt a file using Rijndael. Load the key
   *	from the filesystem, given a password.
   */
    public synchronized void encrypt(char[] password, String fileInput, String fileOutput) throws Exception {
        if (DEBUG) {
            System.out.println("Loading the key.");
        }
        Key key = loadKey(password);
        if (DEBUG) {
            System.out.println("Loaded the key.");
        }
        Cipher cipher = Cipher.getInstance("Rijndael/CBC/PKCS5Padding");
        if (DEBUG) {
            System.out.println("Initializing SecureRandom...");
        }
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        FileInputStream fis = new FileInputStream(fileInput);
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.write(iv);
        IvParameterSpec spec = new IvParameterSpec(iv);
        if (DEBUG) {
            System.out.println("Initializing the cipher.");
            System.out.println(cipher.getAlgorithm());
            System.out.println("blocksize: " + cipher.getBlockSize());
        }
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        if (DEBUG) {
            System.out.println("Encrypting the file...");
        }
        int theByte = 0;
        while ((theByte = fis.read()) != -1) {
            cos.write(theByte);
        }
        fis.close();
        cos.close();
    }

    /**
   *	Encrypt a String to disk using Rijndael. Load the key
   *	from the filesystem, given a password.
   */
    public synchronized void encryptArray(char[] password, char[] charsToEncrypt, String fileOutput) throws Exception {
        if (DEBUG) {
            System.out.println("Loading the key.");
        }
        Key key = loadKey(password);
        if (DEBUG) {
            System.out.println("Loaded the key.");
        }
        Cipher cipher = Cipher.getInstance("Rijndael/CBC/PKCS5Padding");
        if (DEBUG) {
            System.out.println("Initializing SecureRandom...");
        }
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.write(iv);
        IvParameterSpec spec = new IvParameterSpec(iv);
        if (DEBUG) {
            System.out.println("Initializing the cipher.");
            System.out.println(cipher.getAlgorithm());
            System.out.println("blocksize: " + cipher.getBlockSize());
        }
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        if (DEBUG) {
            System.out.println("Encrypting the Text...");
        }
        int theByte = 0;
        for (int i = 0; i < charsToEncrypt.length; i++) {
            theByte = charsToEncrypt[i];
            cos.write(theByte);
        }
        cos.close();
    }

    /**
   *	Decrypt a file using Rijndael. Load the key
   *	from the filesystem, given a password.
   */
    public synchronized void decrypt(char[] password, String fileInput, String fileOutput) throws Exception {
        if (DEBUG) {
            System.out.println("Loading the key.");
        }
        Key key = loadKey(password);
        if (DEBUG) {
            System.out.println("Loaded the key.");
        }
        Cipher cipher = Cipher.getInstance("Rijndael/CBC/PKCS5Padding");
        FileInputStream fis = new FileInputStream(fileInput);
        FileOutputStream fos = new FileOutputStream(fileOutput);
        byte[] iv = new byte[16];
        fis.read(iv);
        IvParameterSpec spec = new IvParameterSpec(iv);
        if (DEBUG) {
            System.out.println("Initializing the cipher.");
        }
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        if (DEBUG) {
            System.out.println("Decrypting the file...");
        }
        StringBuffer myBuff = new StringBuffer();
        int theByte = 0;
        while ((theByte = cis.read()) != -1) {
            fos.write(theByte);
            myBuff.append(theByte);
        }
        cis.close();
        fos.close();
        fullString = myBuff.toString();
    }

    /**
   *	Decrypt a file using Rijndael. Load the key
   *	from the filesystem, given a password.
   */
    public synchronized String decryptArray(char[] password, String fileInput) throws Exception {
        if (DEBUG) {
            System.out.println("Loading the key.");
        }
        Key key = loadKey(password);
        if (DEBUG) {
            System.out.println("Loaded the key.");
        }
        Cipher cipher = Cipher.getInstance("Rijndael/CBC/PKCS5Padding");
        FileInputStream fis = new FileInputStream(fileInput);
        byte[] iv = new byte[16];
        fis.read(iv);
        IvParameterSpec spec = new IvParameterSpec(iv);
        if (DEBUG) {
            System.out.println("Initializing the cipher.");
        }
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        if (DEBUG) {
            System.out.println("Decrypting the file...");
        }
        StringBuffer myBuff = new StringBuffer();
        int theByte = 0;
        while ((theByte = cis.read()) != -1) {
            myBuff.append(new Character((char) theByte).toString());
        }
        cis.close();
        return myBuff.toString();
    }

    private char BASE64Encode(int theByte) {
        return 0;
    }

    public static synchronized FileEncryptor getInstance() {
        if (instance == null) {
            instance = new FileEncryptor();
        }
        return instance;
    }

    public static String[] getCryptoImpls(String serviceType) {
        Set result = new HashSet();
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            Set keys = providers[i].keySet();
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                key = key.split(" ")[0];
                if (key.startsWith(serviceType + ".")) {
                    result.add(key.substring(serviceType.length() + 1));
                } else if (key.startsWith("Alg.Alias." + serviceType + ".")) {
                    result.add(key.substring(serviceType.length() + 11));
                }
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public static String[] getServiceTypes() {
        Set result = new HashSet();
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            Set keys = providers[i].keySet();
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                String key = (String) it.next();
                key = key.split(" ")[0];
                if (key.startsWith("Alg.Alias.")) {
                    key = key.substring(10);
                }
                int ix = key.indexOf('.');
                result.add(key.substring(0, ix));
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }
}
