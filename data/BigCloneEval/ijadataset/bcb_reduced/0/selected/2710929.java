package sun.security.ssl;

import java.util.Arrays;
import java.security.*;

/**
 * Signature implementation for the SSL/TLS RSA Signature variant with both
 * MD5 and SHA-1 MessageDigests. Used for explicit RSA server authentication
 * (RSA signed server key exchange for RSA_EXPORT and DHE_RSA) and RSA client
 * authentication (RSA signed certificate verify message).
 *
 * It conforms to the standard JCA Signature API. It is registered in the
 * SunJSSE provider to avoid more complicated getInstance() code and
 * negative interaction with the JCA mechanisms for hardware providers.
 *
 * The class should be instantiated via the getInstance() method in this class,
 * which returns the implementation from the prefered provider. The internal
 * implementation allows the hashes to be explicitly set, which is required
 * for RSA client authentication. It can be obtained via the
 * getInternalInstance() method.
 *
 * This class is not thread safe.
 *
 */
public final class RSASignature extends SignatureSpi {

    private final Signature rawRsa;

    private MessageDigest md5, sha;

    private boolean isReset;

    public RSASignature() throws NoSuchAlgorithmException {
        super();
        rawRsa = JsseJce.getSignature(JsseJce.SIGNATURE_RAWRSA);
        isReset = true;
    }

    /**
     * Get an implementation for the RSA signature. Follows the standard
     * JCA getInstance() model, so it return the implementation from the
     * provider with the highest precedence, which may be this class.
     */
    static Signature getInstance() throws NoSuchAlgorithmException {
        return JsseJce.getSignature(JsseJce.SIGNATURE_SSLRSA);
    }

    /**
     * Get an internal implementation for the RSA signature. Used for RSA
     * client authentication, which needs the ability to set the digests
     * to externally provided values via the setHashes() method.
     */
    static Signature getInternalInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance(JsseJce.SIGNATURE_SSLRSA, "SunJSSE");
    }

    /**
     * Set the MD5 and SHA hashes to the provided objects.
     */
    static void setHashes(Signature sig, MessageDigest md5, MessageDigest sha) {
        sig.setParameter("hashes", new MessageDigest[] { md5, sha });
    }

    /**
     * Reset the MessageDigests unless they are already reset.
     */
    private void reset() {
        if (isReset == false) {
            md5.reset();
            sha.reset();
            isReset = true;
        }
    }

    private static void checkNull(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        checkNull(publicKey);
        reset();
        rawRsa.initVerify(publicKey);
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        engineInitSign(privateKey, null);
    }

    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        checkNull(privateKey);
        reset();
        rawRsa.initSign(privateKey, random);
    }

    private void initDigests() {
        if (md5 == null) {
            md5 = JsseJce.getMD5();
            sha = JsseJce.getSHA();
        }
    }

    protected void engineUpdate(byte b) {
        initDigests();
        isReset = false;
        md5.update(b);
        sha.update(b);
    }

    protected void engineUpdate(byte[] b, int off, int len) {
        initDigests();
        isReset = false;
        md5.update(b, off, len);
        sha.update(b, off, len);
    }

    private byte[] getDigest() throws SignatureException {
        try {
            initDigests();
            byte[] data = new byte[36];
            md5.digest(data, 0, 16);
            sha.digest(data, 16, 20);
            isReset = true;
            return data;
        } catch (DigestException e) {
            throw new SignatureException(e);
        }
    }

    protected byte[] engineSign() throws SignatureException {
        rawRsa.update(getDigest());
        return rawRsa.sign();
    }

    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        return engineVerify(sigBytes, 0, sigBytes.length);
    }

    protected boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
        rawRsa.update(getDigest());
        return rawRsa.verify(sigBytes, offset, length);
    }

    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
        if (param.equals("hashes") == false) {
            throw new InvalidParameterException("Parameter not supported: " + param);
        }
        if (value instanceof MessageDigest[] == false) {
            throw new InvalidParameterException("value must be MessageDigest[]");
        }
        MessageDigest[] digests = (MessageDigest[]) value;
        md5 = digests[0];
        sha = digests[1];
    }

    protected Object engineGetParameter(String param) throws InvalidParameterException {
        throw new InvalidParameterException("Parameters not supported");
    }
}
