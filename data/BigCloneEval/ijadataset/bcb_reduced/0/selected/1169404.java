package org.ccnx.ccn.impl.security.crypto.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidParameterSpecException;
import java.util.logging.Level;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERTags;
import org.bouncycastle.asn1.DERUnknownTag;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.ccnx.ccn.config.PlatformConfiguration;
import org.ccnx.ccn.impl.security.crypto.SignatureLocks;
import org.ccnx.ccn.impl.security.crypto.gingerbreadfix.JDKDigestSignature;
import org.ccnx.ccn.impl.support.Log;

/** 
 * Helper class for generating signatures.
 */
public class SignatureHelper {

    /**
	 * Signs an array of bytes with a private signing key and specified digest algorithm. 
	 * @param digestAlgorithm the digest algorithm. if null uses DEFAULT_DIGEST_ALGORITHM
	 * @param toBeSigned the array of bytes to be signed.
	 * @param signingKey the signing key.
	 * @return the signature.
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
    public static byte[] sign(String digestAlgorithm, byte[] toBeSigned, PrivateKey signingKey) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        if (null == toBeSigned) {
            Log.info("sign: null content to be signed!");
            throw new SignatureException("Cannot sign null content!");
        }
        if (null == signingKey) {
            Log.info("sign: Signing key cannot be null.");
            Log.info("Temporarily generating fake signature.");
            return DigestHelper.digest(digestAlgorithm, toBeSigned);
        }
        String sigAlgName = getSignatureAlgorithmName(((null == digestAlgorithm) || (digestAlgorithm.length() == 0)) ? DigestHelper.DEFAULT_DIGEST_ALGORITHM : digestAlgorithm, signingKey);
        Signature sig = Signature.getInstance(sigAlgName);
        SignatureLocks.signingLock();
        try {
            sig.initSign(signingKey);
            sig.update(toBeSigned);
            return sig.sign();
        } finally {
            SignatureLocks.signingUnock();
        }
    }

    /**
	 * Sign concatenation of the toBeSigneds.
	 * @param digestAlgorithm the digest algorithm. if null uses DEFAULT_DIGEST_ALGORITHM
	 * @param toBeSigneds the content to be signed.
	 * @param signingKey the signing key.
	 * @return the signature.
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
    public static byte[] sign(String digestAlgorithm, byte[][] toBeSigneds, PrivateKey signingKey) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        if (null == toBeSigneds) {
            Log.info("sign: null content to be signed!");
            throw new SignatureException("Cannot sign null content!");
        }
        if (null == signingKey) {
            Log.info("sign: Signing key cannot be null.");
            Log.info("Temporarily generating fake signature.");
            return DigestHelper.digest(digestAlgorithm, toBeSigneds);
        }
        String sigAlgName = getSignatureAlgorithmName(((null == digestAlgorithm) || (digestAlgorithm.length() == 0)) ? DigestHelper.DEFAULT_DIGEST_ALGORITHM : digestAlgorithm, signingKey);
        Signature sig = Signature.getInstance(sigAlgName);
        SignatureLocks.signingLock();
        try {
            sig.initSign(signingKey);
            for (int i = 0; i < toBeSigneds.length; ++i) {
                sig.update(toBeSigneds[i]);
            }
            return sig.sign();
        } finally {
            SignatureLocks.signingUnock();
        }
    }

    /**
	 * Verifies the signature on the concatenation of a set of individual
	 * data items, given the verification key and digest algorithm. 
	 * @param data the data; which are expected to have been concatenated before 
	 * 	signing. Any null arrays are skipped.
	 * @param signature the signature.
	 * @param digestAlgorithm the digest algorithm. if null uses DEFAULT_DIGEST_ALGORITHM
	 * @param verificationKey the public verification key.
	 * @return the correctness of the signature as a boolean.
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
    public static boolean verify(final byte[][] data, final byte[] signature, String digestAlgorithm, final PublicKey verificationKey) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        if (null == verificationKey) {
            Log.info("verify: Verifying key cannot be null.");
            throw new IllegalArgumentException("verify: Verifying key cannot be null.");
        }
        String sigAlgName = getSignatureAlgorithmName(((null == digestAlgorithm) || (digestAlgorithm.length() == 0)) ? DigestHelper.DEFAULT_DIGEST_ALGORITHM : digestAlgorithm, verificationKey);
        if (PlatformConfiguration.workaroundGingerbreadBug) {
            return new JDKDigestSignature.SHA256WithRSAEncryption() {

                boolean verify() throws InvalidKeyException, SignatureException {
                    SignatureLocks.signingLock();
                    try {
                        engineInitVerify(verificationKey);
                        if (null != data) {
                            for (int i = 0; i < data.length; ++i) {
                                if (data[i] != null) engineUpdate(data[i], 0, data[i].length);
                            }
                        }
                        return engineVerify(signature);
                    } finally {
                        SignatureLocks.signingUnock();
                    }
                }
            }.verify();
        } else {
            Signature sig = Signature.getInstance(sigAlgName);
            SignatureLocks.signingLock();
            try {
                sig.initVerify(verificationKey);
                if (null != data) {
                    for (int i = 0; i < data.length; ++i) {
                        if (data[i] != null) sig.update(data[i]);
                    }
                }
                return sig.verify(signature);
            } finally {
                SignatureLocks.signingUnock();
            }
        }
    }

    /**
	 * Verify a standalone signature.
	 * @param data the data whose signature we want to verify
	 * @param signature the signature itself
	 * @param digestAlgorithm the digest algorithm used to generate the signature,
	 * 		if null uses DEFAULT_DIGEST_ALGORITHM
	 * @param verificationKey the public key to verify the signature with
	 * @return true if signature valid, false otherwise
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 */
    public static boolean verify(byte[] data, byte[] signature, String digestAlgorithm, PublicKey verificationKey) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        return verify(new byte[][] { data }, signature, digestAlgorithm, verificationKey);
    }

    /**
	 * Gets an AlgorithmIdentifier incorporating a given digest and
	 * encryption algorithm, and containing any necessary parameters for
	 * the signing key.
	 * 
	 * @param hashAlgorithm the JCA standard name of the digest algorithm
	 * (e.g. "SHA1")
	 * @param signingKey the private key that will be used to compute the
	 * signature
	 * @return the algorithm identifier.
	 * @throws NoSuchAlgorithmException if the algorithm identifier can't
	 * be formed
	 * @throws InvalidParameterSpecException
	 * @throws InvalidAlgorithmParameterException
	 */
    public static AlgorithmIdentifier getSignatureAlgorithm(String hashAlgorithm, PrivateKey signingKey) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        String signatureAlgorithmOID = getSignatureAlgorithmOID(hashAlgorithm, signingKey.getAlgorithm());
        if (signatureAlgorithmOID == null) {
            if (Log.isLoggable(Level.WARNING)) {
                Log.warning("Error: got no signature algorithm!");
            }
            throw new NoSuchAlgorithmException("Cannot determine OID for hash algorithm " + hashAlgorithm + " and encryption alg " + signingKey.getAlgorithm());
        }
        AlgorithmIdentifier thisSignatureAlgorithm = null;
        try {
            DEREncodable paramData = null;
            AlgorithmParameters params = OIDLookup.getParametersFromKey(signingKey);
            if (params == null) {
                paramData = new DERUnknownTag(DERTags.NULL, new byte[0]);
            } else {
                ByteArrayInputStream bais = new ByteArrayInputStream(params.getEncoded());
                ASN1InputStream dis = new ASN1InputStream(bais);
                paramData = dis.readObject();
            }
            thisSignatureAlgorithm = new AlgorithmIdentifier(new DERObjectIdentifier(signatureAlgorithmOID), paramData);
        } catch (IOException ex) {
            System.out.println("This should not happen: getSignatureAlgorithm -- ");
            System.out.println("    IOException thrown when decoding a key");
            ex.getMessage();
            ex.printStackTrace();
            throw new InvalidParameterSpecException(ex.getMessage());
        }
        return thisSignatureAlgorithm;
    }

    /**
	 * Gets the JCA string name of a signature algorithm, to be used with
	 * a Signature object.
	 *
	 * @param hashAlgorithm the JCA standard name of the digest algorithm
	 * (e.g. "SHA1").
	 * @param signingKey the private key that will be used to compute the
	 * signature.
	 *
	 * @returns the JCA string alias for the signature algorithm.
	 */
    public static String getSignatureAlgorithmName(String hashAlgorithm, PrivateKey signingKey) {
        return getSignatureAlgorithmName(hashAlgorithm, signingKey.getAlgorithm());
    }

    public static String getSignatureAlgorithmName(String hashAlgorithm, PublicKey publicKey) {
        return getSignatureAlgorithmName(hashAlgorithm, publicKey.getAlgorithm());
    }

    /**
	 * Gets the JCA string name of a signature algorithm, to be used with
	 * a Signature object.
	 * @param hashAlgorithm the JCA standard name of the digest algorithm
	 * (e.g. "SHA1").
	 * @param keyAlgorithm the key algorithm.
	 * @return the JCA string alias for the signature algorithm.
	 */
    public static String getSignatureAlgorithmName(String hashAlgorithm, String keyAlgorithm) {
        String signatureAlgorithm = OIDLookup.getSignatureAlgorithm(hashAlgorithm, keyAlgorithm);
        return signatureAlgorithm;
    }

    /**
	 * Gets the OID of a signature algorithm, to be used with
	 * a Signature object.
	 * @param hashAlgorithm the JCA standard name of the digest algorithm
	 * (e.g. "SHA1").
	 * @param keyAlgorithm the key algorithm.
	 * @return the JCA string alias for the signature algorithm.
	 */
    public static String getSignatureAlgorithmOID(String hashAlgorithm, String keyAlgorithm) {
        String signatureAlgorithm = OIDLookup.getSignatureAlgorithmOID(hashAlgorithm, keyAlgorithm);
        return signatureAlgorithm;
    }
}
