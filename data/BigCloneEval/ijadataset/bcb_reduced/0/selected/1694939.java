package org.apache.xml.security.algorithms;

import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Document;

/**
 * Digest Message wrapper & selector class.
 *
 * <pre>
 * MessageDigestAlgorithm.getInstance()
 * </pre>
 */
public class MessageDigestAlgorithm extends Algorithm {

    /** Message Digest - NOT RECOMMENDED MD5*/
    public static final String ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5 = Constants.MoreAlgorithmsSpecNS + "md5";

    /** Digest - Required SHA1*/
    public static final String ALGO_ID_DIGEST_SHA1 = Constants.SignatureSpecNS + "sha1";

    /** Message Digest - RECOMMENDED SHA256*/
    public static final String ALGO_ID_DIGEST_SHA256 = EncryptionConstants.EncryptionSpecNS + "sha256";

    /** Message Digest - OPTIONAL SHA384*/
    public static final String ALGO_ID_DIGEST_SHA384 = Constants.MoreAlgorithmsSpecNS + "sha384";

    /** Message Digest - OPTIONAL SHA512*/
    public static final String ALGO_ID_DIGEST_SHA512 = EncryptionConstants.EncryptionSpecNS + "sha512";

    /** Message Digest - OPTIONAL RIPEMD-160*/
    public static final String ALGO_ID_DIGEST_RIPEMD160 = EncryptionConstants.EncryptionSpecNS + "ripemd160";

    /** Field algorithm stores the actual {@link java.security.MessageDigest} */
    private final MessageDigest algorithm;

    private static ThreadLocal<Map<String, MessageDigest>> instances = new ThreadLocal<Map<String, MessageDigest>>() {

        protected Map<String, MessageDigest> initialValue() {
            return new HashMap<String, MessageDigest>();
        }

        ;
    };

    /**
     * Constructor for the brave who pass their own message digest algorithms and the 
     * corresponding URI.
     * @param doc
     * @param algorithmURI
     */
    private MessageDigestAlgorithm(Document doc, String algorithmURI) throws XMLSignatureException {
        super(doc, algorithmURI);
        algorithm = getDigestInstance(algorithmURI);
    }

    /**
     * Factory method for constructing a message digest algorithm by name.
     *
     * @param doc
     * @param algorithmURI
     * @return The MessageDigestAlgorithm element to attach in document and to digest
     * @throws XMLSignatureException
     */
    public static MessageDigestAlgorithm getInstance(Document doc, String algorithmURI) throws XMLSignatureException {
        return new MessageDigestAlgorithm(doc, algorithmURI);
    }

    private static MessageDigest getDigestInstance(String algorithmURI) throws XMLSignatureException {
        Map<String, MessageDigest> digestMap = instances.get();
        MessageDigest result = digestMap.get(algorithmURI);
        if (result != null) {
            return result;
        }
        String algorithmID = JCEMapper.translateURItoJCEID(algorithmURI);
        if (algorithmID == null) {
            Object[] exArgs = { algorithmURI };
            throw new XMLSignatureException("algorithms.NoSuchMap", exArgs);
        }
        MessageDigest md;
        String provider = JCEMapper.getProviderId();
        try {
            if (provider == null) {
                md = MessageDigest.getInstance(algorithmID);
            } else {
                md = MessageDigest.getInstance(algorithmID, provider);
            }
        } catch (java.security.NoSuchAlgorithmException ex) {
            Object[] exArgs = { algorithmID, ex.getLocalizedMessage() };
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        } catch (NoSuchProviderException ex) {
            Object[] exArgs = { algorithmID, ex.getLocalizedMessage() };
            throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs);
        }
        digestMap.put(algorithmURI, md);
        return md;
    }

    /**
     * Returns the actual {@link java.security.MessageDigest} algorithm object
     *
     * @return the actual {@link java.security.MessageDigest} algorithm object
     */
    public java.security.MessageDigest getAlgorithm() {
        return algorithm;
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#isEqual}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param digesta
     * @param digestb
     * @return the result of the {@link java.security.MessageDigest#isEqual} method
     */
    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        return java.security.MessageDigest.isEqual(digesta, digestb);
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#digest()}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @return the result of the {@link java.security.MessageDigest#digest()} method
     */
    public byte[] digest() {
        return algorithm.digest();
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#digest(byte[])}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param input
     * @return the result of the {@link java.security.MessageDigest#digest(byte[])} method
     */
    public byte[] digest(byte input[]) {
        return algorithm.digest(input);
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#digest(byte[], int, int)}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param buf
     * @param offset
     * @param len
     * @return the result of the {@link java.security.MessageDigest#digest(byte[], int, int)} method
     * @throws java.security.DigestException
     */
    public int digest(byte buf[], int offset, int len) throws java.security.DigestException {
        return algorithm.digest(buf, offset, len);
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#getAlgorithm}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @return the result of the {@link java.security.MessageDigest#getAlgorithm} method
     */
    public String getJCEAlgorithmString() {
        return algorithm.getAlgorithm();
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#getProvider}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @return the result of the {@link java.security.MessageDigest#getProvider} method
     */
    public java.security.Provider getJCEProvider() {
        return algorithm.getProvider();
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#getDigestLength}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @return the result of the {@link java.security.MessageDigest#getDigestLength} method
     */
    public int getDigestLength() {
        return algorithm.getDigestLength();
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#reset}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     */
    public void reset() {
        algorithm.reset();
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#update(byte[])}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param input
     */
    public void update(byte[] input) {
        algorithm.update(input);
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#update(byte)}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param input
     */
    public void update(byte input) {
        algorithm.update(input);
    }

    /**
     * Proxy method for {@link java.security.MessageDigest#update(byte[], int, int)}
     * which is executed on the internal {@link java.security.MessageDigest} object.
     *
     * @param buf
     * @param offset
     * @param len
     */
    public void update(byte buf[], int offset, int len) {
        algorithm.update(buf, offset, len);
    }

    /** @inheritDoc */
    public String getBaseNamespace() {
        return Constants.SignatureSpecNS;
    }

    /** @inheritDoc */
    public String getBaseLocalName() {
        return Constants._TAG_DIGESTMETHOD;
    }
}
