package org.jaffa.security;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import javax.crypto.KeyGenerator;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.NotSerializableException;

/** This class has some utility functions for encrypting objects using the
 * JCE Security Package.
 *
 * Its main purpose is to be able to take a Object/String and encrypt it, and then
 * convert the encrypted data into a HexString, so that it can be passed arround as
 * a String, and hence used in URL's.
 *
 * A good exmple of this is if you have an Object that you want to pass to a servlet,
 * then you can use this routine to get a HexString version of that object and pass
 * it accross in the URL as a paramater "data=1234567890ABC...", Data will not only be a serialization
 * of the object, it will also be encrypted with a SecretKey, that the recievoing servlet must use
 * when converting it back to an object.
 *
 * The String version of this process is optimized to convert the String in to a UTF-8 byte array.
 * This results in a much smaller string then regular obejct serialization.
 *
 * @author  paule
 * @version 1.0
 */
public class EncryptionHelper {

    /** This is the encryption policy that will be used */
    public static final String ENCRYPT_POLICY = "DES/ECB/PKCS5Padding";

    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final String HEX_STR = "0123456789ABCDEF";

    /** This method can be used from the command line for creating a Secret Key.
     * @param args the command line arguments
     * Requires one mandatory parameter, which is the file name to use to write out the SecretKey
     */
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Missing Parameter. Please supply the filename for writing out the SecretKey");
            return;
        }
        File f = new File(args[0]);
        if (f.exists()) System.out.println("Warning: Existing File Will Be Replaced.");
        try {
            KeyGenerator kg = KeyGenerator.getInstance("DES");
            SecretKey secretKey = kg.generateKey();
            byte[] rawsecretKey = secretKey.getEncoded();
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(secretKey);
            oos.flush();
            oos.close();
            fos.close();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Invalid Algorithm : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error Writing Out Key : " + e.getMessage());
        }
    }

    /** This method can be used from the command line for creating a Secret Key.
     * @return Returns the newley generated key, or null if there was an error.
     */
    public static SecretKey createKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("DES");
            SecretKey secretKey = kg.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Invalid Algorithm : " + e.getMessage());
            return null;
        }
    }

    /** Read a file that should contain a serialized Secret key
     *
     * @return The secret key object
     * @param file The file object that points to the key file
     * @throws ClassNotFoundException If the SecretKey class is not available
     * @throws IOException If the specfied file can't be loaded
     */
    public static SecretKey readKey(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        SecretKey secretKey = (SecretKey) ois.readObject();
        ois.close();
        fis.close();
        return secretKey;
    }

    /** Read a file that should contain a serialized Secret key, the file
     * is read as a resource on the classpath
     *
     * @return The secret key object
     * @param name The resource name that points to the key file
     * @throws ClassNotFoundException If the SecretKey class is not available
     * @throws IOException If the specfied file can't be loaded
     */
    public static SecretKey readKeyClassPath(String name) throws IOException, ClassNotFoundException {
        InputStream is = EncryptionHelper.class.getClassLoader().getResourceAsStream(name);
        if (is == null) is = ClassLoader.getSystemResourceAsStream(name);
        if (is == null) throw new FileNotFoundException(name);
        ObjectInputStream ois = new ObjectInputStream(is);
        SecretKey secretKey = (SecretKey) ois.readObject();
        ois.close();
        is.close();
        return secretKey;
    }

    /** Creates an encrypted and encode string from the source string.
     * This string can be used directly in a URL without encoding.
     * @param source The source string to encrypt/encode
     * @param key The secret key to use for encryption
     * @throws NoSuchAlgorithmException May be thrown by the Cypher module
     * @throws InvalidKeyException May be thrown by the Cypher module
     * @throws NoSuchPaddingException May be thrown by the Cypher module
     * @throws UnsupportedEncodingException May be thrown by the Cypher module
     * @throws IllegalBlockSizeException May be thrown by the Cypher module
     * @throws BadPaddingException May be thrown by the Cypher module
     * @return The encoded/encrypted string
     */
    public static String encryptStringForURL(String source, SecretKey key) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, javax.crypto.NoSuchPaddingException, java.io.UnsupportedEncodingException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException {
        Cipher desCipher = Cipher.getInstance(ENCRYPT_POLICY);
        desCipher.init(Cipher.ENCRYPT_MODE, key);
        return intoHexString(desCipher.doFinal(intoBytes(source)));
    }

    /** Get a String from an Encoded and Encrypted String.
     * @param data The encoded/encrypted string to process
     * @param key The secret key used needed to decrypt the string
     * @throws NoSuchAlgorithmException May be thrown by the Cypher module
     * @throws InvalidKeyException May be thrown by the Cypher module
     * @throws NoSuchPaddingException May be thrown by the Cypher module
     * @throws IllegalBlockSizeException May be thrown by the Cypher module
     * @throws BadPaddingException May be thrown by the Cypher module
     * @return  The real string that the data represents
     */
    public static String getStringFromEncryptedURL(String data, SecretKey key) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, javax.crypto.NoSuchPaddingException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException {
        Cipher desCipher = Cipher.getInstance(ENCRYPT_POLICY);
        desCipher.init(Cipher.DECRYPT_MODE, key);
        return intoString(desCipher.doFinal(fromHexString(data)));
    }

    /** Creates an encrypted and encode string from the source object.
     * This string can be used directly in a URL without encoding.
     * This assumes that the object passed in can be serialized.
     * @param source The source Object to encrypt/encode
     * @param key The secret key to use for encryption
     * @throws NoSuchAlgorithmException May be thrown by the Cypher module
     * @throws InvalidKeyException May be thrown by the Cypher module
     * @throws NoSuchPaddingException May be thrown by the Cypher module
     * @throws UnsupportedEncodingException May be thrown by the Cypher module
     * @throws IllegalBlockSizeException May be thrown by the Cypher module
     * @throws BadPaddingException May be thrown by the Cypher module
     * @throws NotSerializableException if the source object is not Serializable
     * @return The encoded/encrypted string
     */
    public static String encryptObjectForURL(Object source, SecretKey key) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, javax.crypto.NoSuchPaddingException, java.io.UnsupportedEncodingException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException, java.io.NotSerializableException {
        Cipher desCipher = Cipher.getInstance(ENCRYPT_POLICY);
        desCipher.init(Cipher.ENCRYPT_MODE, key);
        if (!(source instanceof Serializable)) throw new NotSerializableException();
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(source);
            o.flush();
            o.close();
            return intoHexString(desCipher.doFinal(b.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("No IO Exception should occur, this is all in-memory!!");
        }
    }

    /** Get an Object from an Encoded and Encrypted String. This assumes that the
     * object can be recreated by de-serialization, and that the original class for the
     * object is accessable.
     *
     * @param data The encoded/encrypted string to process
     * @param key The secret key used needed to decrypt the string
     * @throws NoSuchAlgorithmException May be thrown by the Cypher module
     * @throws InvalidKeyException May be thrown by the Cypher module
     * @throws NoSuchPaddingException May be thrown by the Cypher module
     * @throws IllegalBlockSizeException May be thrown by the Cypher module
     * @throws BadPaddingException May be thrown by the Cypher module
     * @return  The real object that the data represents
     */
    public static Object getObjectFromEncryptedURL(String data, SecretKey key) throws java.security.NoSuchAlgorithmException, java.security.InvalidKeyException, javax.crypto.NoSuchPaddingException, javax.crypto.IllegalBlockSizeException, javax.crypto.BadPaddingException {
        Cipher desCipher = Cipher.getInstance(ENCRYPT_POLICY);
        desCipher.init(Cipher.DECRYPT_MODE, key);
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(desCipher.doFinal(fromHexString(data)));
            ObjectInputStream o = new ObjectInputStream(b);
            Object out = o.readObject();
            o.close();
            b.close();
            return out;
        } catch (IOException e) {
            throw new RuntimeException("No IO Exception should occur, this is all in-memory!!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can find class of encrypted object!!!");
        }
    }

    /** Converts a String (based on an 8-bit character set) into an byte array.
     * There will be one byte per charater in the string.
     * @param in The string to be converted
     * @throws UnsupportedEncodingException Is thrown if there are any unsupported characters in the string (ie. greater that 8-bits)
     * @return  The byte[] for the string
     */
    public static byte[] intoBytes(String in) throws UnsupportedEncodingException {
        byte[] data = new byte[in.length()];
        char[] a = in.toCharArray();
        for (int i = 0, j = 0; i < a.length; i++) {
            char c = a[i];
            byte hi = (byte) (c >> 8);
            if (hi != 0) throw new UnsupportedEncodingException("Non UTF-8 Characters In String, Use 16-Bit version!");
            byte lo = (byte) (c & 0xFF);
            data[j++] = lo;
        }
        return data;
    }

    /** Converts a byte array into a string. It assumes that 8-bits represents a byte.
     * There should there for be one character per byte.
     * @param in byte[] to be converted
     * @return  Converted string
     */
    public static String intoString(byte[] in) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < in.length; ) {
            byte hi = 0;
            byte lo = in[i++];
            char c = (char) (hi * 0xFF + lo);
            b.append(c);
        }
        return b.toString();
    }

    /** Converts a String into an byte array.
     * There will be two bytes per charater in the string.
     * @param in The string to be converted
     * @return  The byte[] for the string
     */
    public static byte[] intoBytes16(String in) {
        byte[] data = new byte[in.length() * 2];
        char[] a = in.toCharArray();
        for (int i = 0, j = 0; i < a.length; i++) {
            char c = a[i];
            byte hi = (byte) (c >> 8);
            byte lo = (byte) (c & 0xFF);
            data[j++] = hi;
            data[j++] = lo;
        }
        return data;
    }

    /** Converts a byte array into a string. It assumes that 16-bits represents a byte.
     * @param in byte[] to be converted
     * @return  Converted string
     */
    public static String intoString16(byte[] in) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < in.length; ) {
            byte hi = in[i++];
            byte lo = in[i++];
            char c = (char) (hi * 256 + lo + (lo < 0 ? 256 : 0));
            b.append(c);
        }
        return b.toString();
    }

    /** Converts a byte[] into a hex string representation. Each byte will be represented
     * by a 2-digit hex number (00-FF).
     * @param in The byte[] to convert
     * @return  The string containing the Hex representation
     */
    public static String intoHexString(byte[] in) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < in.length; i++) {
            byte bt = in[i];
            b.append(toHex((byte) (bt >> 4)));
            b.append(toHex(bt));
        }
        return b.toString();
    }

    /** Convert a String of hex values into a byte[]. Each two characters in the string
     * represent 1 byte.
     * @param in The hex string to be converted
     * @return  A byte[] of the real data
     */
    public static byte[] fromHexString(String in) {
        byte[] data = new byte[in.length() / 2];
        for (int i = 0, j = 0; i < in.length(); ) {
            byte hi = fromHex(in.charAt(i++));
            byte lo = fromHex(in.charAt(i++));
            data[j++] = (byte) ((hi << 4) + lo);
        }
        return data;
    }

    /** Utility function to convert a number into a hex character.
     * Takes the lowest 4 bits and converts it to a character '0'..'F'
     * @param b The byte to convert
     * @return  The Hex character
     */
    public static char toHex(byte b) {
        return HEX[(int) (b & 0x0F)];
    }

    /** Utility function to convert a hex character to a number.
     * The character must be '0'..'F', the byte will be 0-15.
     * @param c The character to convert
     * @return  The number as a byte
     */
    public static byte fromHex(char c) {
        return (byte) HEX_STR.indexOf(c);
    }
}
