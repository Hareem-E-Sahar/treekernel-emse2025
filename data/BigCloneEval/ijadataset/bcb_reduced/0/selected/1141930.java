package com.dreamlizard.miles.text;

import com.dreamlizard.miles.text.exception.CryptoException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayOutputStream;
import java.security.Security;
import java.util.StringTokenizer;

/**
 * A class to encrypt/decrypt a string using the "PBEWithMD5AndDES" cipher.
 *
 * @author Thomas Bohmbach, Jr.
 */
public class StringCipher {

    private final byte[] DEFAULT_SALT = { (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0x22, (byte) 0x44, (byte) 0xab, (byte) 0x12 };

    private final int DEFAULT_ITERATIONS = 10;

    private final String CIPHER_NAME = "PBEWithMD5AndDES";

    private static boolean isInitialized = false;

    private Cipher cipher = null;

    private PBEParameterSpec pbeParamSpec = null;

    private SecretKey key = null;

    /**
     * Construct a StringCipher with a given password and default salt and iteration values.
     *
     * @param inPassword The password to encrypt/decrypt with
     * @throws CryptoException
     */
    public StringCipher(String inPassword) throws CryptoException {
        initCipher(inPassword, DEFAULT_SALT, DEFAULT_ITERATIONS);
    }

    /**
     * Construct a StringCipher with given password and salt and a default iteration value.
     *
     * @param inPassword The password to encrypt/decrypt with
     * @param inSalt     The byte array containing salt values
     * @throws CryptoException
     */
    public StringCipher(String inPassword, byte[] inSalt) throws CryptoException {
        initCipher(inPassword, inSalt, DEFAULT_ITERATIONS);
    }

    /**
     * Construct a StringCipher with given password, salt, and iteration values.
     *
     * @param inPassword   The password to encrypt/decrypt with
     * @param inSalt       The byte array containing salt values
     * @param inIterations The number of DES iterations to use
     * @throws CryptoException
     */
    public StringCipher(String inPassword, byte[] inSalt, int inIterations) throws CryptoException {
        initCipher(inPassword, inSalt, inIterations);
    }

    /**
     * Initialize the underlying Cipher object.
     *
     * @param inPassword   The password to encrypt/decrypt with
     * @param inSalt       The byte array containing salt values
     * @param inIterations The number of DES iterations to use
     * @throws CryptoException
     */
    private void initCipher(String inPassword, byte[] inSalt, int inIterations) throws CryptoException {
        try {
            if (!isInitialized) {
                synchronized (StringCipher.class) {
                    if (!isInitialized) {
                        Security.addProvider(new com.sun.crypto.provider.SunJCE());
                        isInitialized = true;
                    }
                }
            }
            pbeParamSpec = new PBEParameterSpec(inSalt, inIterations);
            String thePassword = inPassword;
            if (inPassword == null) {
                thePassword = "";
            }
            PBEKeySpec thePbeKeySpec = new PBEKeySpec(thePassword.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(CIPHER_NAME);
            key = keyFac.generateSecret(thePbeKeySpec);
            cipher = Cipher.getInstance(CIPHER_NAME);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Encrypt the given <code>String</code> into a byte array.
     *
     * @param inString The <code>String</code> to encrypt
     * @return The encrypted string represented as a byte array (<code>byte[]</code>)
     * @throws com.dreamlizard.miles.text.exception.CryptoException
     *
     */
    public byte[] encrypt(String inString) throws CryptoException {
        byte[] theEncryptedBytes = null;
        if (inString != null) {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
                ByteArrayOutputStream theBAOS = new ByteArrayOutputStream();
                CipherOutputStream theCOS = new CipherOutputStream(theBAOS, cipher);
                theCOS.write(inString.getBytes());
                theCOS.close();
                theBAOS.close();
                theEncryptedBytes = theBAOS.toByteArray();
            } catch (Exception e) {
                throw new CryptoException(e);
            }
        }
        return theEncryptedBytes;
    }

    /**
     * Encrypt the given <code>String</code> and return a <code>String</code>
     * representing the encrypted byte array.
     *
     * @param inString The <code>String</code> to encrypt
     * @return A <code>String</code> representing the encrypted byte array
     * @throws com.dreamlizard.miles.text.exception.CryptoException
     *
     */
    public String encryptToByteArrayString(String inString) throws CryptoException {
        byte[] theEncryptedByteArray = encrypt(inString);
        return getStringFromByteArray(theEncryptedByteArray);
    }

    /**
     * Decrypt the given byte array into a <code>String</code>.
     *
     * @param inByteArray The byte array (<code>byte[]</code>) to decrypt
     * @return The decrypted <code>String</code>
     * @throws CryptoException
     */
    public String decrypt(byte[] inByteArray) throws CryptoException {
        String theDecryptedString = null;
        if ((inByteArray != null) && (inByteArray.length != 0)) {
            try {
                cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
                ByteArrayOutputStream theBAOS = new ByteArrayOutputStream();
                CipherOutputStream theCOS = new CipherOutputStream(theBAOS, cipher);
                theCOS.write(inByteArray);
                theCOS.close();
                theBAOS.close();
                theDecryptedString = theBAOS.toString();
            } catch (Exception e) {
                throw new CryptoException(e);
            }
        }
        return theDecryptedString;
    }

    /**
     * Decrypt the given string representation of an encrypted byte array (as returned by
     * <code>encryptToByteArrayString</code> and return the decrypted <code>String</code>.
     *
     * @param inByteArrayString The <code>String</code> representing the encrypted byte array
     * @return The decrypted <code>String</code>
     * @throws CryptoException
     */
    public String decryptFromByteArrayString(String inByteArrayString) throws CryptoException {
        byte[] theByteArray = getByteArrayFromString(inByteArrayString);
        return decrypt(theByteArray);
    }

    /**
     * A static utility method to return a string representation of a given byte array (<code>byte[]</code>).
     * Returns a string with the following format: <code>{ x1 x2 x3 ... xn }</code>
     *
     * @param inByteArray The byte array (<code>byte[]</code>)
     * @return The <code>String</code> representing the byte array
     */
    public static String getStringFromByteArray(byte[] inByteArray) {
        StringBuffer theStringBuffer = new StringBuffer();
        theStringBuffer.append("{ ");
        if (inByteArray != null) {
            int theLength = inByteArray.length;
            for (int i = 0; i < theLength; i++) {
                theStringBuffer.append(Byte.toString(inByteArray[i]));
                theStringBuffer.append(" ");
            }
        }
        theStringBuffer.append("}");
        return theStringBuffer.toString();
    }

    /**
     * A static utility method to return a byte array (<code>byte[]</code>) from a given string representation.
     * Accepts a string with the following format: <code>{ x1 x2 x3 ... xn }</code>
     * (this string can be created by <code>getStringFromByteArray(byte[] inByteArray)).
     *
     * @param inByteArrayString The <code>String</code> representing the byte array
     * @return The byte array (<code>byte[]</code>)
     */
    public static byte[] getByteArrayFromString(String inByteArrayString) {
        String theTokenString;
        byte[] theByteArray = null;
        StringTokenizer theST = new StringTokenizer(inByteArrayString, "{} ");
        int theLength = theST.countTokens();
        if (theLength > 0) {
            int i = 0;
            theByteArray = new byte[theLength];
            while (theST.hasMoreTokens()) {
                theTokenString = theST.nextToken();
                theByteArray[i++] = Byte.parseByte(theTokenString);
            }
        }
        return theByteArray;
    }

    /**
     * The main method<br>
     * Usage: java com.uhg.uht.services.crypto.StringCipher string password [encrypt|decrypt]<br>
     * string: The string to encrypt or decrypt<br>
     * password: The password used to encrypt or decrypt the string<br>
     * [encrypt|decrypt]: Whether to encrypt or decrypt the string (Default: encrypt)<br>
     * If decrypt is selected, the input string should be a string<br>
     * representation of a byte array ( for example: <code>{ 22 -126 12 99 }</code> )<br>
     * Output: The encrypted byte array string or the decrypted string
     *
     * @param args The array of <code>String</code> arguments to the main method
     */
    public static void main(String[] args) {
        try {
            if ((args.length == 2) || ((args.length == 3) && (args[2].equalsIgnoreCase("encrypt")))) {
                String theStringToEncrypt = args[0];
                String thePassword = args[1];
                StringCipher theSC = new StringCipher(thePassword);
                String theEncryptedByteArray = theSC.encryptToByteArrayString(theStringToEncrypt);
                System.out.println(theEncryptedByteArray);
            } else if ((args.length == 3) && (args[2].equalsIgnoreCase("decrypt"))) {
                String theStringToDecrypt = args[0];
                String thePassword = args[1];
                StringCipher theSC = new StringCipher(thePassword);
                String theDecryptedString = theSC.decryptFromByteArrayString(theStringToDecrypt);
                System.out.println(theDecryptedString);
            } else {
                System.out.println("Usage: com.dreamlizard.miles.text.StringCipher s p [encrypt|decrypt]");
                System.out.println("    s: The string to encrypt or decrypt");
                System.out.println("    p: The password used to encrypt or decrypt the string");
                System.out.println("    [encrypt|decrypt]: Whether to encrypt or decrypt (Default: encrypt)");
                System.out.println("        If decrypt is selected, the input string should be a string");
                System.out.println("        representation of a byte array ( for example: { 22 -126 12 99 } )");
                System.out.println("Output: The encrypted byte array string or the decrypted string");
            }
        } catch (CryptoException ce) {
            ce.printStackTrace();
        }
    }
}
