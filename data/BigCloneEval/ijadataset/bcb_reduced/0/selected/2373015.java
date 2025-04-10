package org.pdfbox.pdmodel.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;
import org.pdfbox.encryption.ARCFour;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.pdmodel.PDDocument;

/**
 * This class represents a security handler as described in the PDF specifications.
 * A security handler is responsible of documents protection.  
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Benoit Guillon (benoit.guillon@snv.jussieu.fr)
 * 
 * @version $Revision: 1.4 $
 */
public abstract class SecurityHandler {

    private static final int DEFAULT_KEY_LENGTH = 40;

    /**
     * The value of V field of the Encryption dictionary.
     */
    protected int version;

    /**
     * The length of the secret key used to encrypt the document.
     */
    protected int keyLength = DEFAULT_KEY_LENGTH;

    /**
     * The encryption key that will used to encrypt / decrypt.
     */
    protected byte[] encryptionKey;

    /**
     * The document whose security is handled by this security handler.
     */
    protected PDDocument document;

    /**
     * The RC4 implementation used for cryptographic functions.
     */
    protected ARCFour rc4 = new ARCFour();

    private Set objects = new HashSet();

    private Set potentialSignatures = new HashSet();

    /**
     * The access permission granted to the current user for the document. These
     * permissions are computed during decryption and are in read only mode.
     */
    protected AccessPermission currentAccessPermission = null;

    /**
     * Prepare the document for encryption.
     * 
     * @param doc The document that will be encrypted.
     * 
     * @throws CryptographyException If there is an error while preparing.
     * @throws IOException If there is an error with the document.
     */
    public abstract void prepareDocumentForEncryption(PDDocument doc) throws CryptographyException, IOException;

    /**
     * Prepare the document for decryption.
     * 
     * @param doc The document to decrypt.
     * @param mat Information required to decrypt the document.
     * @throws CryptographyException If there is an error while preparing.
     * @throws IOException If there is an error with the document.
     */
    public abstract void decryptDocument(PDDocument doc, DecryptionMaterial mat) throws CryptographyException, IOException;

    /**
     * This method must be called by an implementation of this class to really proceed 
     * to decryption.
     *
     * @throws IOException If there is an error in the decryption.
     * @throws CryptographyException If there is an error in the decryption.
     */
    protected void proceedDecryption() throws IOException, CryptographyException {
        COSDictionary trailer = document.getDocument().getTrailer();
        COSArray fields = (COSArray) trailer.getObjectFromPath("Root/AcroForm/Fields");
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                COSDictionary field = (COSDictionary) fields.getObject(i);
                addDictionaryAndSubDictionary(potentialSignatures, field);
            }
        }
        List allObjects = document.getDocument().getObjects();
        Iterator objectIter = allObjects.iterator();
        while (objectIter.hasNext()) {
            decryptObject((COSObject) objectIter.next());
        }
        document.setEncryptionDictionary(null);
    }

    private void addDictionaryAndSubDictionary(Set set, COSDictionary dic) {
        set.add(dic);
        COSArray kids = (COSArray) dic.getDictionaryObject("Kids");
        for (int i = 0; kids != null && i < kids.size(); i++) {
            addDictionaryAndSubDictionary(set, (COSDictionary) kids.getObject(i));
        }
        COSBase value = dic.getDictionaryObject("V");
        if (value instanceof COSDictionary) {
            addDictionaryAndSubDictionary(set, (COSDictionary) value);
        }
    }

    /**
     * Encrypt a set of data.
     * 
     * @param objectNumber The data object number.
     * @param genNumber The data generation number.
     * @param data The data to encrypt.
     * @param output The output to write the encrypted data to.
     * 
     * @throws CryptographyException If there is an error during the encryption.
     * @throws IOException If there is an error reading the data.
     */
    public void encryptData(long objectNumber, long genNumber, InputStream data, OutputStream output) throws CryptographyException, IOException {
        byte[] newKey = new byte[encryptionKey.length + 5];
        System.arraycopy(encryptionKey, 0, newKey, 0, encryptionKey.length);
        newKey[newKey.length - 5] = (byte) (objectNumber & 0xff);
        newKey[newKey.length - 4] = (byte) ((objectNumber >> 8) & 0xff);
        newKey[newKey.length - 3] = (byte) ((objectNumber >> 16) & 0xff);
        newKey[newKey.length - 2] = (byte) (genNumber & 0xff);
        newKey[newKey.length - 1] = (byte) ((genNumber >> 8) & 0xff);
        byte[] digestedKey = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digestedKey = md.digest(newKey);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
        int length = Math.min(newKey.length, 16);
        byte[] finalKey = new byte[length];
        System.arraycopy(digestedKey, 0, finalKey, 0, length);
        rc4.setKey(finalKey);
        rc4.write(data, output);
        output.flush();
    }

    /**
     * This will decrypt an object in the document.
     *
     * @param object The object to decrypt.
     *
     * @throws CryptographyException If there is an error decrypting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    private void decryptObject(COSObject object) throws CryptographyException, IOException {
        long objNum = object.getObjectNumber().intValue();
        long genNum = object.getGenerationNumber().intValue();
        COSBase base = object.getObject();
        decrypt(base, objNum, genNum);
    }

    /**
     * This will dispatch to the correct method.
     *
     * @param obj The object to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation Number.
     *
     * @throws CryptographyException If there is an error decrypting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    private void decrypt(Object obj, long objNum, long genNum) throws CryptographyException, IOException {
        if (!objects.contains(obj)) {
            objects.add(obj);
            if (obj instanceof COSString) {
                decryptString((COSString) obj, objNum, genNum);
            } else if (obj instanceof COSStream) {
                decryptStream((COSStream) obj, objNum, genNum);
            } else if (obj instanceof COSDictionary) {
                decryptDictionary((COSDictionary) obj, objNum, genNum);
            } else if (obj instanceof COSArray) {
                decryptArray((COSArray) obj, objNum, genNum);
            }
        }
    }

    /**
     * This will decrypt a stream.
     *
     * @param stream The stream to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If there is an error getting the stream.
     * @throws IOException If there is an error getting the stream data.
     */
    public void decryptStream(COSStream stream, long objNum, long genNum) throws CryptographyException, IOException {
        decryptDictionary(stream, objNum, genNum);
        InputStream encryptedStream = stream.getFilteredStream();
        encryptData(objNum, genNum, encryptedStream, stream.createFilteredStream());
    }

    /**
     * This will decrypt a dictionary.
     *
     * @param dictionary The dictionary to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If there is an error decrypting the document.
     * @throws IOException If there is an error creating a new string.
     */
    private void decryptDictionary(COSDictionary dictionary, long objNum, long genNum) throws CryptographyException, IOException {
        Iterator keys = dictionary.keyList().iterator();
        while (keys.hasNext()) {
            COSName key = (COSName) keys.next();
            Object value = dictionary.getItem(key);
            if (!(key.getName().equals("Contents") && value instanceof COSString && potentialSignatures.contains(dictionary))) {
                decrypt(value, objNum, genNum);
            }
        }
    }

    /**
     * This will decrypt a string.
     *
     * @param string the string to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If an error occurs during decryption.
     * @throws IOException If an error occurs writing the new string.
     */
    public void decryptString(COSString string, long objNum, long genNum) throws CryptographyException, IOException {
        ByteArrayInputStream data = new ByteArrayInputStream(string.getBytes());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        encryptData(objNum, genNum, data, buffer);
        string.reset();
        string.append(buffer.toByteArray());
    }

    /**
     * This will decrypt an array.
     *
     * @param array The array to decrypt.
     * @param objNum The object number.
     * @param genNum The object generation number.
     *
     * @throws CryptographyException If an error occurs during decryption.
     * @throws IOException If there is an error accessing the data.
     */
    private void decryptArray(COSArray array, long objNum, long genNum) throws CryptographyException, IOException {
        for (int i = 0; i < array.size(); i++) {
            decrypt(array.get(i), objNum, genNum);
        }
    }

    /**
     * Getter of the property <tt>keyLength</tt>.
     * @return  Returns the keyLength.
     * @uml.property  name="keyLength"
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * Setter of the property <tt>keyLength</tt>.
     * 
     * @param keyLen  The keyLength to set.
     */
    public void setKeyLength(int keyLen) {
        this.keyLength = keyLen;
    }

    /**
     * Returns the access permissions that were computed during document decryption.
     * The returned object is in read only mode.
     * 
     * @return the access permissions or null if the document was not decrypted.
     */
    public AccessPermission getCurrentAccessPermission() {
        return currentAccessPermission;
    }
}
