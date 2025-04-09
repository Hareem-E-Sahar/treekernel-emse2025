package hanasu.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @author Marc Miltenberger
 * Implements the RSA algorithm.
 */
public class RSA {

    public static final int KEYSIZE = 5120;

    private static final long newKeyGenerationEveryMs = 1000 * 3600 * 24 * 3;

    private static String path = getKeyFileLocation() + ".tmpkey.rsa";

    private KeyPair keyPair;

    private Signature signature;

    /**
	 * Generates a new RSA instance.
	 * @throws NoSuchAlgorithmException
	 */
    public RSA() throws NoSuchAlgorithmException {
        signature = Signature.getInstance("SHA1withRSA");
    }

    /**
	 * Signs a message
	 * 
	 * @param toSign the message to sign
	 * @return signed message
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 */
    public byte[] signMessage(byte[] toSign) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        signature.initSign(keyPair.getPrivate(), new SecureRandom());
        signature.update(toSign);
        return signature.sign();
    }

    /**
	 * Verifies a signed message. Returns true if the verification was valid.
	 * @param input          the message to check
	 * @param signature      the corresponding signature
	 * @param otherPublicKey the public key of the sender
	 * @return true if the verification was valid 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws SignatureException
	 */
    public boolean verifySignature(byte[] input, byte[] signature, PublicKey otherPublicKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(otherPublicKey);
        sig.update(input);
        return sig.verify(signature);
    }

    /**
	 * Encrypts a message
	 * @param input the message
	 * @param otherPublicKey the other's public key
	 * @return the encrypted message
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException 
	 * @throws SignatureException 
	 */
    public byte[] encrypt(byte[] input, PublicKey otherPublicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, NoSuchProviderException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
        cipher.update(input);
        byte[] encrypted = cipher.doFinal();
        byte[] signed = signMessage(input);
        byte[] output = new byte[encrypted.length + signed.length];
        System.arraycopy(encrypted, 0, output, 0, encrypted.length);
        System.arraycopy(signed, 0, output, encrypted.length, signed.length);
        return output;
    }

    /**
	 * Decrypts a message
	 * @param input the encrypted message
	 * @return the decrypted Message
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws SignatureException 
	 * @throws NoSuchProviderException 
	 */
    public byte[] decrypt(byte[] input, PublicKey otherPublicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, SignatureException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        byte[] encrypted = new byte[input.length / 2];
        byte[] signed = new byte[input.length / 2];
        System.arraycopy(input, 0, encrypted, 0, encrypted.length);
        System.arraycopy(input, encrypted.length, signed, 0, signed.length);
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        cipher.update(encrypted);
        byte[] decrypted = cipher.doFinal();
        if (otherPublicKey != null && !verifySignature(decrypted, signed, otherPublicKey)) throw new SignatureException();
        return decrypted;
    }

    /**
	 * Returns the key pair
	 * @return the key pair
	 */
    public KeyPair getKeyPair() {
        return keyPair;
    }

    /**
	 * Serializes the key to a byte array.
	 * @param includePrivateKey whether to include the private key
	 * @return the serialized byte array
	 * @throws IOException should not occur
	 */
    public byte[] toByteArray(boolean includePrivateKey) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(output);
        out.writeObject(keyPair.getPublic());
        if (includePrivateKey) {
            out.writeObject(keyPair.getPrivate());
        }
        out.close();
        return output.toByteArray();
    }

    /**
	 * Loads the serialized key from a byte array
	 * @param bytArray the byte array to load
	 * @throws IOException should not occur
	 * @throws ClassNotFoundException should not occur
	 */
    public void loadKey(byte[] bytArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream output = new ByteArrayInputStream(bytArray);
        ObjectInputStream out = new ObjectInputStream(output);
        PublicKey publickey = (PublicKey) out.readObject();
        PrivateKey privatekey = null;
        try {
            privatekey = (PrivateKey) out.readObject();
        } catch (Exception e) {
        }
        keyPair = new KeyPair(publickey, privatekey);
        out.close();
    }

    /**
	 * Generates a key pair
	 * @param useCache whether to use cache
	 * @throws NoSuchAlgorithmException
	 */
    public void generateKey(boolean useCache) throws NoSuchAlgorithmException {
        File file = new File(path);
        if (useCache && !needKeyUpdate()) {
            System.out.println("RSA key location: " + path);
            System.out.println("Loading RSA key");
            FileInputStream input;
            try {
                input = new FileInputStream(path);
                byte[] bytInput = new byte[(int) file.length()];
                input.read(bytInput);
                input.close();
                loadKey(bytInput);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        pairgen.initialize(KEYSIZE, random);
        keyPair = pairgen.generateKeyPair();
        if (!path.equals("")) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(path);
                fos.write(toByteArray(true));
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Returns the path to the key file directory
	 * @return the path to the key file directory
	 */
    private static String getKeyFileLocation() {
        return "";
    }

    /**
	 * Converts the given byte array to a public key 
	 * @param key the key byte array
	 * @return the public key
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
    public static PublicKey getPublicKey(byte[] key) throws IOException, ClassNotFoundException {
        ByteArrayInputStream output = new ByteArrayInputStream(key);
        ObjectInputStream out = new ObjectInputStream(output);
        PublicKey publickey = (PublicKey) out.readObject();
        return publickey;
    }

    /**
	 * Returns true if a key is cached
	 * @return true if a key is cached
	 */
    public boolean hasKeyInCache() {
        return new File(path).exists();
    }

    /**
	 * Returns true if the key is out of date or not cached
	 * @return true if the key is out of date or not cached
	 */
    public boolean needKeyUpdate() {
        File file = new File(path);
        long age = System.currentTimeMillis() - file.lastModified();
        if (file.exists() && age < newKeyGenerationEveryMs) return false;
        return true;
    }
}
