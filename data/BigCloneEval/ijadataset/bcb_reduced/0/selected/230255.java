package org.tcpfile.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tcpfile.gui.settingsmanager.SettingChangeListener;
import org.tcpfile.gui.settingsmanager.SettingsManager;
import org.tcpfile.main.Misc;
import org.tcpfile.net.ByteArray;

/**
 * Handles RSA Encryption now using builtin Methods.
 * @author stivo
 * 
 */
public class RSA implements Serializable {

    private static final long serialVersionUID = 12L;

    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

    private static final int prepareInAdvance = 5;

    public static int RSAKeylength = SettingsManager.settingsManager.findSetting("RSAKeylength").getInteger();

    private static final Vector<RSA> cache = new Vector<RSA>();

    public static final String KEYGENALGORITHM = "RSA";

    public static final BigInteger EXPONENT = new BigInteger("65537");

    private KeyPair keypair;

    private static Logger log = LoggerFactory.getLogger(RSA.class);

    static {
        SettingsManager.settingsManager.findSetting("RSAKeylength").addPropertyChangeListener(new SettingChangeListener(RSA.class, ""));
    }

    private static final Runnable cacheFiller = new Runnable() {

        public void run() {
            while (cache.size() < prepareInAdvance) {
                RSA r = new RSA(RSAKeylength);
                cache.add(r);
            }
        }
    };

    public static RSA getRSA() {
        if (cache.size() < prepareInAdvance / 2) Misc.runRunnableInSingletonThread(cacheFiller, "RSAKeyGen");
        RSA out = null;
        try {
            out = cache.remove(0);
        } catch (RuntimeException e) {
        }
        if (out == null) return new RSA(RSAKeylength);
        return out;
    }

    /**
	 * Only use this directly when you need a non standard Size RSA Key.
	 * Otherwise use getRSA 
	 * @param N The length of the key in bits.
	 */
    public RSA(int N) {
        this.keypair = generateKey(N);
    }

    /**
	 * Generates a new Key, used in the constructor
	 * @param bitlength
	 * @return
	 */
    public KeyPair generateKey(int bitlength) {
        try {
            if (bitlength >= 2000) log.debug("Starting RSA with " + bitlength);
            java.security.KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(bitlength, Misc.random);
            java.security.KeyPair keypair = keyGen.generateKeyPair();
            if (bitlength >= 2000) log.debug("Found RSA with " + bitlength);
            return keypair;
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
        }
        return null;
    }

    /**
	 * Decrypts the input by using the key of this instance
	 * @param input
	 * @return
	 */
    public byte[] decrypt(byte[] input) {
        return encrypt(input, this.keypair.getPrivate(), false);
    }

    /**
	 * Decrypts a signed packet with the public key of this contact
	 * @param input
	 * @param key
	 * @return
	 */
    public static byte[] decrypt(byte[] input, String key) {
        try {
            KeyFactory rsakf = KeyFactory.getInstance(KEYGENALGORITHM);
            PublicKey pub_key = rsakf.generatePublic(new java.security.spec.RSAPublicKeySpec(new BigInteger(key), EXPONENT));
            return encrypt(input, pub_key, false);
        } catch (InvalidKeySpecException e) {
            log.warn("", e);
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
        }
        return null;
    }

    /**
	 * Encrypts input using the public key
	 * @param input
	 * @param key
	 * @return
	 */
    public static byte[] encrypt(byte[] input, String key) {
        try {
            KeyFactory rsakf = KeyFactory.getInstance(KEYGENALGORITHM);
            PublicKey pub_key = rsakf.generatePublic(new java.security.spec.RSAPublicKeySpec(new BigInteger(key), EXPONENT));
            return encrypt(input, pub_key, true);
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
        } catch (InvalidKeySpecException e) {
            log.warn("", e);
        }
        return null;
    }

    /**
	 * Encrypts or Decrypts public key
	 * @param input 
	 * @param key Key for decrypting or Encrypting
	 * @param encrypt True: Encrypt. False: Decrypt
	 * @return
	 */
    public static synchronized byte[] encrypt(byte[] input, Key key, boolean encrypt) {
        int encodinglength = -10;
        try {
            Cipher cip = Cipher.getInstance(ALGORITHM);
            if (encrypt) cip.init(javax.crypto.Cipher.ENCRYPT_MODE, key); else cip.init(javax.crypto.Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream bla = new ByteArrayOutputStream();
            encodinglength = cip.getOutputSize(2);
            if (encrypt) encodinglength = cip.getOutputSize(2) - 20;
            int i = 0;
            boolean finished = false;
            do {
                bla.write(cip.doFinal(ByteArray.copyfromto(input, i * encodinglength, (i + 1) * encodinglength)));
                i++;
                if (input.length <= i * encodinglength) finished = true;
            } while (!finished);
            byte[] encrypted = bla.toByteArray();
            return encrypted;
        } catch (InvalidKeyException e) {
            log.warn("", e);
        } catch (IOException e) {
            log.warn("", e);
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
        } catch (NoSuchPaddingException e) {
            log.warn("", e);
        } catch (IllegalBlockSizeException e) {
            log.warn("", e);
        } catch (BadPaddingException e) {
            log.warn("", e);
        }
        return null;
    }

    /**
	 * Encrypts input using the private key.
	 * @param input byte[] (Unlimited length).
	 * @return Encrypted byte[] so it can be decrypted with this Public Key.
	 */
    public byte[] sign(byte[] input) {
        return encrypt(input, this.keypair.getPrivate(), true);
    }

    /**
	 * Returns the public key of this object as a String
	 * @return
	 */
    public String getPublic() {
        RSAPublicKey pub = (RSAPublicKey) this.keypair.getPublic();
        BigInteger mod_pub_bi = pub.getModulus();
        return mod_pub_bi + "";
    }
}
