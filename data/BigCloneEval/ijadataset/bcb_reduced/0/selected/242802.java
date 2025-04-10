package alto.sec.pkcs;

import java.io.OutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import alto.sec.util.*;
import alto.sec.x509.AlgorithmId;
import alto.sec.x509.X509Certificate;
import alto.sec.x509.X500Name;
import alto.sec.x509.KeyUsageExtension;
import alto.sec.x509.PKIXExtensions;
import alto.io.u.Hex;

/**
 * A SignerInfo, as defined in PKCS#7's signedData type.
 *
 * @author Benjamin Renaud
 */
public class SignerInfo implements DerEncoder {

    BigInteger version;

    X500Name issuerName;

    BigInteger certificateSerialNumber;

    AlgorithmId digestAlgorithmId;

    AlgorithmId digestEncryptionAlgorithmId;

    byte[] encryptedDigest;

    PKCS9Attributes authenticatedAttributes;

    PKCS9Attributes unauthenticatedAttributes;

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest) {
        this.version = BigInteger.ONE;
        this.issuerName = issuerName;
        this.certificateSerialNumber = serial;
        this.digestAlgorithmId = digestAlgorithmId;
        this.digestEncryptionAlgorithmId = digestEncryptionAlgorithmId;
        this.encryptedDigest = encryptedDigest;
    }

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, PKCS9Attributes authenticatedAttributes, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest, PKCS9Attributes unauthenticatedAttributes) {
        this.version = BigInteger.ONE;
        this.issuerName = issuerName;
        this.certificateSerialNumber = serial;
        this.digestAlgorithmId = digestAlgorithmId;
        this.authenticatedAttributes = authenticatedAttributes;
        this.digestEncryptionAlgorithmId = digestEncryptionAlgorithmId;
        this.encryptedDigest = encryptedDigest;
        this.unauthenticatedAttributes = unauthenticatedAttributes;
    }

    /**
     * Parses a PKCS#7 signer info.
     */
    public SignerInfo(DerInputStream derin) throws IOException, ParsingException {
        this(derin, false);
    }

    /**
     * Parses a PKCS#7 signer info.
     *
     * <p>This constructor is used only for backwards compatibility with
     * PKCS#7 blocks that were generated using JDK1.1.x.
     *
     * @param derin the ASN.1 encoding of the signer info.
     * @param oldStyle flag indicating whether or not the given signer info
     * is encoded according to JDK1.1.x.
     */
    public SignerInfo(DerInputStream derin, boolean oldStyle) throws IOException, ParsingException {
        version = derin.getBigInteger();
        DerValue[] issuerAndSerialNumber = derin.getSequence(2);
        byte[] issuerBytes = issuerAndSerialNumber[0].toByteArray();
        issuerName = new X500Name(new DerValue(DerValue.tag_Sequence, issuerBytes));
        certificateSerialNumber = issuerAndSerialNumber[1].getBigInteger();
        DerValue tmp = derin.getDerValue();
        digestAlgorithmId = AlgorithmId.parse(tmp);
        if (oldStyle) {
            derin.getSet(0);
        } else {
            if ((byte) (derin.peekByte()) == (byte) 0xA0) {
                authenticatedAttributes = new PKCS9Attributes(derin);
            }
        }
        tmp = derin.getDerValue();
        digestEncryptionAlgorithmId = AlgorithmId.parse(tmp);
        encryptedDigest = derin.getOctetString();
        if (oldStyle) {
            derin.getSet(0);
        } else {
            if (derin.available() != 0 && (byte) (derin.peekByte()) == (byte) 0xA1) {
                unauthenticatedAttributes = new PKCS9Attributes(derin, true);
            }
        }
        if (derin.available() != 0) {
            throw new ParsingException("extra data at the end");
        }
    }

    public void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }

    /**
     * DER encode this object onto an output stream.
     * Implements the <code>DerEncoder</code> interface.
     *
     * @param out
     * the output stream on which to write the DER encoding.
     *
     * @exception IOException on encoding error.
     */
    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream seq = new DerOutputStream();
        seq.putInteger(version);
        DerOutputStream issuerAndSerialNumber = new DerOutputStream();
        issuerName.encode(issuerAndSerialNumber);
        issuerAndSerialNumber.putInteger(certificateSerialNumber);
        seq.write(DerValue.tag_Sequence, issuerAndSerialNumber);
        digestAlgorithmId.encode(seq);
        if (authenticatedAttributes != null) authenticatedAttributes.encode((byte) 0xA0, seq);
        digestEncryptionAlgorithmId.encode(seq);
        seq.putOctetString(encryptedDigest);
        if (unauthenticatedAttributes != null) unauthenticatedAttributes.encode((byte) 0xA1, seq);
        DerOutputStream tmp = new DerOutputStream();
        tmp.write(DerValue.tag_Sequence, seq);
        out.write(tmp.toByteArray());
    }

    public X509Certificate getCertificate(PKCS7 block) throws IOException {
        return block.getCertificate(this.certificateSerialNumber, this.issuerName);
    }

    public ArrayList<X509Certificate> getCertificateChain(PKCS7 block) throws IOException {
        X509Certificate userCert;
        userCert = block.getCertificate(certificateSerialNumber, issuerName);
        if (userCert == null) return null;
        ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(userCert);
        X509Certificate[] pkcsCerts = block.getCertificates();
        if (pkcsCerts == null || userCert.getSubjectDN().equals(userCert.getIssuerDN())) {
            return certList;
        }
        Principal issuer = userCert.getIssuerDN();
        int start = 0;
        while (true) {
            boolean match = false;
            int i = start;
            while (i < pkcsCerts.length) {
                if (issuer.equals(pkcsCerts[i].getSubjectDN())) {
                    certList.add(pkcsCerts[i]);
                    if (pkcsCerts[i].getSubjectDN().equals(pkcsCerts[i].getIssuerDN())) {
                        start = pkcsCerts.length;
                    } else {
                        issuer = pkcsCerts[i].getIssuerDN();
                        X509Certificate tmpCert = pkcsCerts[start];
                        pkcsCerts[start] = pkcsCerts[i];
                        pkcsCerts[i] = tmpCert;
                        start++;
                    }
                    match = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!match) break;
        }
        return certList;
    }

    SignerInfo verify(PKCS7 block, byte[] data) throws NoSuchAlgorithmException, SignatureException {
        try {
            ContentInfo content = block.getContentInfo();
            if (data == null) {
                data = content.getContentBytes();
            }
            String digestAlgname = getDigestAlgorithmId().getName();
            if (digestAlgname.equalsIgnoreCase("SHA")) digestAlgname = "SHA1";
            byte[] dataSigned;
            if (authenticatedAttributes == null) {
                dataSigned = data;
            } else {
                ObjectIdentifier contentType = (ObjectIdentifier) authenticatedAttributes.getAttributeValue(PKCS9Attribute.CONTENT_TYPE_OID);
                if (contentType == null || !contentType.equals(content.contentType)) return null;
                byte[] messageDigest = (byte[]) authenticatedAttributes.getAttributeValue(PKCS9Attribute.MESSAGE_DIGEST_OID);
                if (messageDigest == null) return null;
                MessageDigest md = MessageDigest.getInstance(digestAlgname);
                byte[] computedMessageDigest = md.digest(data);
                if (messageDigest.length != computedMessageDigest.length) return null;
                for (int i = 0; i < messageDigest.length; i++) {
                    if (messageDigest[i] != computedMessageDigest[i]) return null;
                }
                dataSigned = authenticatedAttributes.getDerEncoding();
            }
            String encryptionAlgname = getDigestEncryptionAlgorithmId().getName();
            if (encryptionAlgname.equalsIgnoreCase("SHA1withDSA")) encryptionAlgname = "DSA";
            String algname = digestAlgname + "with" + encryptionAlgname;
            Signature sig = Signature.getInstance(algname);
            X509Certificate cert = getCertificate(block);
            if (cert == null) {
                return null;
            }
            if (cert.hasUnsupportedCriticalExtension()) {
                throw new SignatureException("Certificate has unsupported " + "critical extension(s)");
            }
            boolean[] keyUsageBits = cert.getKeyUsage();
            if (keyUsageBits != null) {
                KeyUsageExtension keyUsage;
                try {
                    keyUsage = new KeyUsageExtension(keyUsageBits);
                } catch (IOException ioe) {
                    throw new SignatureException("Failed to parse keyUsage extension");
                }
                boolean digSigAllowed = ((Boolean) keyUsage.get(KeyUsageExtension.DIGITAL_SIGNATURE)).booleanValue();
                boolean nonRepuAllowed = ((Boolean) keyUsage.get(KeyUsageExtension.NON_REPUDIATION)).booleanValue();
                if (!digSigAllowed && !nonRepuAllowed) {
                    throw new SignatureException("Key usage restricted: cannot be used for digital signatures");
                }
            }
            PublicKey key = cert.getPublicKey();
            sig.initVerify(key);
            sig.update(dataSigned);
            if (sig.verify(encryptedDigest)) {
                return this;
            }
        } catch (IOException e) {
            throw new SignatureException("IO error verifying signature:\n" + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new SignatureException("InvalidKey: " + e.getMessage());
        }
        return null;
    }

    SignerInfo verify(PKCS7 block) throws NoSuchAlgorithmException, SignatureException {
        return verify(block, null);
    }

    public BigInteger getVersion() {
        return version;
    }

    public X500Name getIssuerName() {
        return issuerName;
    }

    public BigInteger getCertificateSerialNumber() {
        return certificateSerialNumber;
    }

    public AlgorithmId getDigestAlgorithmId() {
        return digestAlgorithmId;
    }

    public PKCS9Attributes getAuthenticatedAttributes() {
        return authenticatedAttributes;
    }

    public AlgorithmId getDigestEncryptionAlgorithmId() {
        return digestEncryptionAlgorithmId;
    }

    public byte[] getEncryptedDigest() {
        return encryptedDigest;
    }

    public PKCS9Attributes getUnauthenticatedAttributes() {
        return unauthenticatedAttributes;
    }

    public String toString() {
        String out = "";
        out += "Signer Info for (issuer): " + issuerName + "\n";
        out += "\tversion: " + Hex.encode(version) + "\n";
        out += "\tcertificateSerialNumber: " + Hex.encode(certificateSerialNumber) + "\n";
        out += "\tdigestAlgorithmId: " + digestAlgorithmId + "\n";
        if (authenticatedAttributes != null) {
            out += "\tauthenticatedAttributes: " + authenticatedAttributes + "\n";
        }
        out += "\tdigestEncryptionAlgorithmId: " + digestEncryptionAlgorithmId + "\n";
        out += "\tencryptedDigest: " + "\n" + Hex.encode(encryptedDigest) + "\n";
        if (unauthenticatedAttributes != null) {
            out += "\tunauthenticatedAttributes: " + unauthenticatedAttributes + "\n";
        }
        return out;
    }
}
