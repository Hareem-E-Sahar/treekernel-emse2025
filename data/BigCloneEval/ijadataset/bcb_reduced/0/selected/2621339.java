package org.pdfbox.pdmodel.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSString;
import org.pdfbox.encryption.ARCFour;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.pdmodel.PDDocument;

/**
 * 
 * The class implements the standard security handler as decribed
 * in the PDF specifications. This security handler protects document
 * with password.
 * 
 * @see StandardProtectionPolicy to see how to protect document with this security handler.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 *
 * @version $Revision: 1.3 $
 */
public class StandardSecurityHandler extends SecurityHandler {

    /**
     * Type of security handler.
     */
    public static final String FILTER = "Standard";

    private static final int DEFAULT_VERSION = 1;

    private static final int DEFAULT_REVISION = 3;

    private int revision = DEFAULT_REVISION;

    private StandardProtectionPolicy policy;

    private ARCFour rc4 = new ARCFour();

    /**
     * Protection policy class for this handler.
     */
    public static final Class PROTECTION_POLICY_CLASS = StandardProtectionPolicy.class;

    /**
     * Standard padding for encryption.
     */
    public static final byte[] ENCRYPT_PADDING = { (byte) 0x28, (byte) 0xBF, (byte) 0x4E, (byte) 0x5E, (byte) 0x4E, (byte) 0x75, (byte) 0x8A, (byte) 0x41, (byte) 0x64, (byte) 0x00, (byte) 0x4E, (byte) 0x56, (byte) 0xFF, (byte) 0xFA, (byte) 0x01, (byte) 0x08, (byte) 0x2E, (byte) 0x2E, (byte) 0x00, (byte) 0xB6, (byte) 0xD0, (byte) 0x68, (byte) 0x3E, (byte) 0x80, (byte) 0x2F, (byte) 0x0C, (byte) 0xA9, (byte) 0xFE, (byte) 0x64, (byte) 0x53, (byte) 0x69, (byte) 0x7A };

    /**
     * Constructor.
     */
    public StandardSecurityHandler() {
    }

    /**
     * Constructor used for encryption.
     * 
     * @param p The protection policy.
     */
    public StandardSecurityHandler(StandardProtectionPolicy p) {
        policy = p;
        keyLength = policy.getEncryptionKeyLength();
    }

    /**
     * Computes the version number of the StandardSecurityHandler 
     * regarding the encryption key length.
     * See PDF Spec 1.6 p 93
     *  
     * @return The computed cersion number.
     */
    private int computeVersionNumber() {
        if (keyLength == 40) {
            return DEFAULT_VERSION;
        }
        return 2;
    }

    /**
     * Computes the revision version of the StandardSecurityHandler to
     * use regarding the version number and the permissions bits set.
     * See PDF Spec 1.6 p98
     * 
     * @return The computed revision number.
     */
    private int computeRevisionNumber() {
        if (version == 2 && !policy.getPermissions().canFillInForm() && !policy.getPermissions().canExtractForAccessibility() && !policy.getPermissions().canPrintDegraded()) {
            return 2;
        }
        return 3;
    }

    /**
     * Decrypt the document.
     * 
     * @param doc The document to be decrypted.
     * @param decryptionMaterial Information used to decrypt the document.
     * 
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void decryptDocument(PDDocument doc, DecryptionMaterial decryptionMaterial) throws CryptographyException, IOException {
        document = doc;
        PDEncryptionDictionary dictionary = document.getEncryptionDictionary();
        if (!(decryptionMaterial instanceof StandardDecryptionMaterial)) {
            throw new CryptographyException("Provided decryption material is not compatible with the document");
        }
        StandardDecryptionMaterial material = (StandardDecryptionMaterial) decryptionMaterial;
        String password = material.getPassword();
        if (password == null) {
            password = "";
        }
        int dicPermissions = dictionary.getPermissions();
        int dicRevision = dictionary.getRevision();
        int dicLength = dictionary.getLength() / 8;
        COSString id = (COSString) document.getDocument().getDocumentID().getObject(0);
        byte[] u = dictionary.getUserKey();
        byte[] o = dictionary.getOwnerKey();
        boolean isUserPassword = isUserPassword(password.getBytes(), u, o, dicPermissions, id.getBytes(), dicRevision, dicLength);
        boolean isOwnerPassword = isOwnerPassword(password.getBytes(), u, o, dicPermissions, id.getBytes(), dicRevision, dicLength);
        if (isUserPassword) {
            encryptionKey = computeEncryptedKey(password.getBytes(), o, dicPermissions, id.getBytes(), dicRevision, dicLength);
        } else if (isOwnerPassword) {
            byte[] computedUserPassword = getUserPassword(password.getBytes(), o, dicRevision, dicLength);
            encryptionKey = computeEncryptedKey(computedUserPassword, o, dicPermissions, id.getBytes(), dicRevision, dicLength);
        } else {
            throw new CryptographyException("Error: The supplied password does not match either the owner or user password in the document.");
        }
        this.proceedDecryption();
    }

    /**
     * Prepare document for encryption.
     * 
     * @param doc The documeent to encrypt.
     * 
     * @throws IOException If there is an error accessing data.
     * @throws CryptographyException If there is an error with decryption.
     */
    public void prepareDocumentForEncryption(PDDocument doc) throws CryptographyException, IOException {
        document = doc;
        PDEncryptionDictionary encryptionDictionary = document.getEncryptionDictionary();
        if (encryptionDictionary == null) {
            encryptionDictionary = new PDEncryptionDictionary();
        }
        version = computeVersionNumber();
        revision = computeRevisionNumber();
        encryptionDictionary.setFilter(FILTER);
        encryptionDictionary.setVersion(version);
        encryptionDictionary.setRevision(revision);
        encryptionDictionary.setLength(keyLength);
        String ownerPassword = policy.getOwnerPassword();
        String userPassword = policy.getUserPassword();
        if (ownerPassword == null) {
            ownerPassword = "";
        }
        if (userPassword == null) {
            userPassword = "";
        }
        int permissionInt = policy.getPermissions().getPermissionBytes();
        encryptionDictionary.setPermissions(permissionInt);
        int length = keyLength / 8;
        COSArray idArray = document.getDocument().getDocumentID();
        if (idArray == null || idArray.size() < 2) {
            idArray = new COSArray();
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                BigInteger time = BigInteger.valueOf(System.currentTimeMillis());
                md.update(time.toByteArray());
                md.update(ownerPassword.getBytes());
                md.update(userPassword.getBytes());
                md.update(document.getDocument().toString().getBytes());
                byte[] id = md.digest(this.toString().getBytes());
                COSString idString = new COSString();
                idString.append(id);
                idArray.add(idString);
                idArray.add(idString);
                document.getDocument().setDocumentID(idArray);
            } catch (NoSuchAlgorithmException e) {
                throw new CryptographyException(e);
            } catch (IOException e) {
                throw new CryptographyException(e);
            }
        }
        COSString id = (COSString) idArray.getObject(0);
        byte[] o = computeOwnerPassword(ownerPassword.getBytes("ISO-8859-1"), userPassword.getBytes("ISO-8859-1"), revision, length);
        byte[] u = computeUserPassword(userPassword.getBytes("ISO-8859-1"), o, permissionInt, id.getBytes(), revision, length);
        encryptionKey = computeEncryptedKey(userPassword.getBytes("ISO-8859-1"), o, permissionInt, id.getBytes(), revision, length);
        encryptionDictionary.setOwnerKey(o);
        encryptionDictionary.setUserKey(u);
        document.setEncryptionDictionary(encryptionDictionary);
        document.getDocument().setEncryptionDictionary(encryptionDictionary.getCOSDictionary());
    }

    /**
     * Check for owner password.
     * 
     * @param ownerPassword The owner password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The set of permissions on the document.
     * @param id The document id.
     * @param encRevision The encryption algorithm revision.
     * @param length The encryption key length.
     * 
     * @return True If the ownerPassword param is the owner password.
     * 
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isOwnerPassword(byte[] ownerPassword, byte[] u, byte[] o, int permissions, byte[] id, int encRevision, int length) throws CryptographyException, IOException {
        byte[] userPassword = getUserPassword(ownerPassword, o, encRevision, length);
        return isUserPassword(userPassword, u, o, permissions, id, encRevision, length);
    }

    /**
     * Get the user password based on the owner password.
     * 
     * @param ownerPassword The plaintext owner password.
     * @param o The o entry of the encryption dictionary.
     * @param encRevision The encryption revision number.
     * @param length The key length.
     * 
     * @return The u entry of the encryption dictionary.
     * 
     * @throws CryptographyException If there is an error generating the user password.
     * @throws IOException If there is an error accessing data while generating the user password.
     */
    public final byte[] getUserPassword(byte[] ownerPassword, byte[] o, int encRevision, long length) throws CryptographyException, IOException {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] ownerPadded = truncateOrPad(ownerPassword);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(ownerPadded);
            byte[] digest = md.digest();
            if (encRevision == 3 || encRevision == 4) {
                for (int i = 0; i < 50; i++) {
                    md.reset();
                    md.update(digest);
                    digest = md.digest();
                }
            }
            if (encRevision == 2 && length != 5) {
                throw new CryptographyException("Error: Expected length=5 actual=" + length);
            }
            byte[] rc4Key = new byte[(int) length];
            System.arraycopy(digest, 0, rc4Key, 0, (int) length);
            if (encRevision == 2) {
                rc4.setKey(rc4Key);
                rc4.write(o, result);
            } else if (encRevision == 3 || encRevision == 4) {
                byte[] iterationKey = new byte[rc4Key.length];
                byte[] otemp = new byte[o.length];
                System.arraycopy(o, 0, otemp, 0, o.length);
                rc4.write(o, result);
                for (int i = 19; i >= 0; i--) {
                    System.arraycopy(rc4Key, 0, iterationKey, 0, rc4Key.length);
                    for (int j = 0; j < iterationKey.length; j++) {
                        iterationKey[j] = (byte) (iterationKey[j] ^ (byte) i);
                    }
                    rc4.setKey(iterationKey);
                    result.reset();
                    rc4.write(otemp, result);
                    otemp = result.toByteArray();
                }
            }
            return result.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
    }

    /**
     * Compute the encryption key.
     * 
     * @param password The password to compute the encrypted key.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The permissions for the document.
     * @param id The document id.
     * @param encRevision The revision of the encryption algorithm.
     * @param length The length of the encryption key.
     * 
     * @return The encrypted key bytes.
     * 
     * @throws CryptographyException If there is an error with encryption.
     */
    public final byte[] computeEncryptedKey(byte[] password, byte[] o, int permissions, byte[] id, int encRevision, int length) throws CryptographyException {
        byte[] result = new byte[length];
        try {
            byte[] padded = truncateOrPad(password);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(padded);
            md.update(o);
            byte zero = (byte) (permissions >>> 0);
            byte one = (byte) (permissions >>> 8);
            byte two = (byte) (permissions >>> 16);
            byte three = (byte) (permissions >>> 24);
            md.update(zero);
            md.update(one);
            md.update(two);
            md.update(three);
            md.update(id);
            byte[] digest = md.digest();
            if (encRevision == 3 || encRevision == 4) {
                for (int i = 0; i < 50; i++) {
                    md.reset();
                    md.update(digest, 0, length);
                    digest = md.digest();
                }
            }
            if (encRevision == 2 && length != 5) {
                throw new CryptographyException("Error: length should be 5 when revision is two actual=" + length);
            }
            System.arraycopy(digest, 0, result, 0, length);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
        return result;
    }

    /**
     * This will compute the user password hash.
     *
     * @param password The plain text password.
     * @param o The owner password hash.
     * @param permissions The document permissions.
     * @param id The document id.
     * @param encRevision The revision of the encryption.
     * @param length The length of the encryption key.
     *
     * @return The user password.
     *
     * @throws CryptographyException If there is an error computing the user password.
     * @throws IOException If there is an IO error.
     */
    public final byte[] computeUserPassword(byte[] password, byte[] o, int permissions, byte[] id, int encRevision, int length) throws CryptographyException, IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] encryptionKey = computeEncryptedKey(password, o, permissions, id, encRevision, length);
        if (encRevision == 2) {
            rc4.setKey(encryptionKey);
            rc4.write(ENCRYPT_PADDING, result);
        } else if (encRevision == 3 || encRevision == 4) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(ENCRYPT_PADDING);
                md.update(id);
                result.write(md.digest());
                byte[] iterationKey = new byte[encryptionKey.length];
                for (int i = 0; i < 20; i++) {
                    System.arraycopy(encryptionKey, 0, iterationKey, 0, iterationKey.length);
                    for (int j = 0; j < iterationKey.length; j++) {
                        iterationKey[j] = (byte) (iterationKey[j] ^ i);
                    }
                    rc4.setKey(iterationKey);
                    ByteArrayInputStream input = new ByteArrayInputStream(result.toByteArray());
                    result.reset();
                    rc4.write(input, result);
                }
                byte[] finalResult = new byte[32];
                System.arraycopy(result.toByteArray(), 0, finalResult, 0, 16);
                System.arraycopy(ENCRYPT_PADDING, 0, finalResult, 16, 16);
                result.reset();
                result.write(finalResult);
            } catch (NoSuchAlgorithmException e) {
                throw new CryptographyException(e);
            }
        }
        return result.toByteArray();
    }

    /**
     * Compute the owner entry in the encryption dictionary.
     * 
     * @param ownerPassword The plaintext owner password.
     * @param userPassword The plaintext user password.
     * @param encRevision The revision number of the encryption algorithm.
     * @param length The length of the encryption key.
     * 
     * @return The o entry of the encryption dictionary.
     * 
     * @throws CryptographyException If there is an error with encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final byte[] computeOwnerPassword(byte[] ownerPassword, byte[] userPassword, int encRevision, int length) throws CryptographyException, IOException {
        try {
            byte[] ownerPadded = truncateOrPad(ownerPassword);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(ownerPadded);
            byte[] digest = md.digest();
            if (encRevision == 3 || encRevision == 4) {
                for (int i = 0; i < 50; i++) {
                    md.reset();
                    md.update(digest, 0, length);
                    digest = md.digest();
                }
            }
            if (encRevision == 2 && length != 5) {
                throw new CryptographyException("Error: Expected length=5 actual=" + length);
            }
            byte[] rc4Key = new byte[length];
            System.arraycopy(digest, 0, rc4Key, 0, length);
            byte[] paddedUser = truncateOrPad(userPassword);
            rc4.setKey(rc4Key);
            ByteArrayOutputStream crypted = new ByteArrayOutputStream();
            rc4.write(new ByteArrayInputStream(paddedUser), crypted);
            if (encRevision == 3 || encRevision == 4) {
                byte[] iterationKey = new byte[rc4Key.length];
                for (int i = 1; i < 20; i++) {
                    System.arraycopy(rc4Key, 0, iterationKey, 0, rc4Key.length);
                    for (int j = 0; j < iterationKey.length; j++) {
                        iterationKey[j] = (byte) (iterationKey[j] ^ (byte) i);
                    }
                    rc4.setKey(iterationKey);
                    ByteArrayInputStream input = new ByteArrayInputStream(crypted.toByteArray());
                    crypted.reset();
                    rc4.write(input, crypted);
                }
            }
            return crypted.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException(e.getMessage());
        }
    }

    /**
     * This will take the password and truncate or pad it as necessary.
     *
     * @param password The password to pad or truncate.
     *
     * @return The padded or truncated password.
     */
    private final byte[] truncateOrPad(byte[] password) {
        byte[] padded = new byte[ENCRYPT_PADDING.length];
        int bytesBeforePad = Math.min(password.length, padded.length);
        System.arraycopy(password, 0, padded, 0, bytesBeforePad);
        System.arraycopy(ENCRYPT_PADDING, 0, padded, bytesBeforePad, ENCRYPT_PADDING.length - bytesBeforePad);
        return padded;
    }

    /**
     * Check if a plaintext password is the user password.
     * 
     * @param password The plaintext password.
     * @param u The u entry of the encryption dictionary.
     * @param o The o entry of the encryption dictionary.
     * @param permissions The permissions set in the the PDF.
     * @param id The document id used for encryption.
     * @param encRevision The revision of the encryption algorithm.
     * @param length The length of the encryption key.
     * 
     * @return true If the plaintext password is the user password.
     * 
     * @throws CryptographyException If there is an error during encryption.
     * @throws IOException If there is an error accessing data.
     */
    public final boolean isUserPassword(byte[] password, byte[] u, byte[] o, int permissions, byte[] id, int encRevision, int length) throws CryptographyException, IOException {
        boolean matches = false;
        byte[] computedValue = computeUserPassword(password, o, permissions, id, encRevision, length);
        if (encRevision == 2) {
            matches = arraysEqual(u, computedValue);
        } else if (encRevision == 3 || encRevision == 4) {
            matches = arraysEqual(u, computedValue, 16);
        }
        return matches;
    }

    private static final boolean arraysEqual(byte[] first, byte[] second, int count) {
        boolean equal = first.length >= count && second.length >= count;
        for (int i = 0; i < count && equal; i++) {
            equal = first[i] == second[i];
        }
        return equal;
    }

    /**
     * This will compare two byte[] for equality.
     *
     * @param first The first byte array.
     * @param second The second byte array.
     *
     * @return true If the arrays contain the exact same data.
     */
    private static final boolean arraysEqual(byte[] first, byte[] second) {
        boolean equal = first.length == second.length;
        for (int i = 0; i < first.length && equal; i++) {
            equal = first[i] == second[i];
        }
        return equal;
    }
}
