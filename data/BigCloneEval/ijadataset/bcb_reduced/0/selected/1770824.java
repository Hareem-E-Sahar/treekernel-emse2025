package de.fzj.pkikits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import de.fzj.pkikits.conf.Config;
import de.fzj.pkikits.util.Util;

public class Auth {

    private KeyStore keyStore;

    private String password;

    private String certPath;

    private String keyStorePath;

    private String alias;

    private String dn;

    private String keyType;

    private String keySize;

    private X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();

    private long maxValidLong;

    private String signatureAlgorithm;

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Auth.class);

    private boolean isCa;

    private boolean isRa;

    private static Auth auth;

    public static String ALIAS;

    public static Auth getAuth() throws Exception {
        if (auth == null) {
            auth = new Auth();
        }
        return auth;
    }

    private Auth() {
        maxValidLong = 1000L * 60 * 60 * 24 * 356 * 2;
        dn = Config.getProperty(Config.LOCAL_DN);
        password = Config.getProperty(Config.PRIVATEKEY_PASSWORD);
        Auth.ALIAS = alias = "0";
        keyType = Config.getProperty(Config.LOCAL_KEY_TYPE);
        keySize = Config.getProperty(Config.LOCAL_KEY_SIZE);
        certPath = Config.getProperty(Config.LOCAL_CERT);
        keyStorePath = Config.getProperty(Config.LOCAL_KEYSTORE);
        signatureAlgorithm = Config.getProperty(Config.SIGNATURE_ALGORITHM);
        isCa = true;
    }

    public void setCaExtensions(KeyPair kp) throws CertificateParsingException {
        v3CertGen.addExtension(X509Extensions.KeyUsage, false, new KeyUsage(KeyUsage.dataEncipherment | KeyUsage.cRLSign | KeyUsage.keyEncipherment | KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.keyAgreement));
        v3CertGen.addExtension(MiscObjectIdentifiers.netscapeCertType, false, new NetscapeCertType(NetscapeCertType.objectSigning | NetscapeCertType.sslServer));
        v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(kp.getPublic()));
        v3CertGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0));
    }

    public void setRaExtensions(KeyPair kp) {
        v3CertGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(-1));
        v3CertGen.addExtension(MiscObjectIdentifiers.netscapeCertType, false, new NetscapeCertType(NetscapeCertType.sslServer));
    }

    public synchronized KeyStore getLocalKeyStore() throws PkiException {
        if (keyStore == null) {
            try {
                keyStore = readLocalStore(keyStorePath, certPath, "PKCS12", "BC");
            } catch (Exception e) {
                logger.error("");
                throw (PkiException) new PkiException().initCause(e);
            }
        }
        return keyStore;
    }

    public String getLocalKeystorePassword() {
        return password;
    }

    private synchronized KeyStore saveLocalStore(X509Certificate cert, PrivateKey pk, String keystorepath, String typ, String provider) throws PkiException {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(typ, provider);
            ks.load(null, null);
            Certificate[] chain = new Certificate[] { cert };
            ks.setKeyEntry(alias, pk, password.toCharArray(), chain);
            ks.store(new FileOutputStream(keystorepath), password.toCharArray());
        } catch (Exception e) {
            logger.error("Saving KeyStore failed.\n *** Maybe you don't have \"Unlimited Strength\" Jurisdiction Policy Files installed?");
            throw (PkiException) new PkiException(e.getMessage()).initCause(e);
        }
        return ks;
    }

    /**
     * TODO: rewrite that method, its ugly.
     * 
     * @param ksp
     * @param cp
     * @param typ
     * @param provider
     * @return
     * @throws PkiException 
     * @throws  
     * @throws GeneralSecurityException 
     */
    private synchronized KeyStore readLocalStore(String ksp, String cp, String typ, String provider) throws IOException, GeneralSecurityException, PkiException {
        KeyPair kp;
        X509Certificate cert = null;
        KeyStore ks;
        ks = KeyStore.getInstance(typ, provider);
        File keyStoreFile = new File(ksp);
        File certFile = new File(cp);
        if (keyStoreFile.exists()) {
            ks.load(new FileInputStream(keyStoreFile), password.toCharArray());
            if (certFile.exists()) {
                PEMReader pr = new PEMReader(new FileReader(certFile));
                cert = (X509Certificate) pr.readObject();
                pr.close();
                cert.checkValidity();
                logger.warn("I`m not yet able to check wether " + cp + " is valid. Nor will i send the full certificate chain during a ssl handshake!");
                PublicKey key1 = ks.getCertificate(alias).getPublicKey();
                PublicKey key2 = cert.getPublicKey();
                if (!key1.equals(key2)) {
                    throw new GeneralSecurityException("Keystore key does`t match cert key!");
                }
                Key key = ks.getKey(alias, password.toCharArray());
                ks = saveLocalStore(cert, (PrivateKey) key, ksp, typ, provider);
            }
        } else {
            logger.info("No Keystore found, Generating new one in " + ksp + "...\n" + "Parameters are:\n" + "Key type " + keyType + "\n" + "Key size " + keySize + "\nIsCa=" + isCa);
            ks.load(null, null);
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyType);
            keyGen.initialize(Integer.parseInt(keySize));
            kp = keyGen.generateKeyPair();
            v3CertGen.reset();
            v3CertGen.setSerialNumber(BigInteger.valueOf(1));
            v3CertGen.setIssuerDN(new X509Name(dn));
            v3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
            v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + maxValidLong));
            try {
                Date notbefore = Util.getAsDate(Config.getProperty(Config.NOT_BEFORE));
                Date notafter = Util.getAsDate(Config.getProperty(Config.NOT_AFTER));
                v3CertGen.setNotBefore(notbefore);
                v3CertGen.setNotAfter(notafter);
            } catch (Exception e) {
                throw new PkiException("Failed to setup certificate: " + e.getMessage(), e);
            }
            v3CertGen.setSubjectDN(new X509Name(dn));
            v3CertGen.setPublicKey(kp.getPublic());
            v3CertGen.setSignatureAlgorithm(signatureAlgorithm);
            if (isCa) {
                setCaExtensions(kp);
            }
            if (isRa) {
                setRaExtensions(kp);
            }
            cert = v3CertGen.generateX509Certificate((PrivateKey) kp.getPrivate());
            ks = saveLocalStore(cert, kp.getPrivate(), ksp, typ, provider);
            if (certFile.createNewFile()) {
                PEMWriter pr = new PEMWriter(new FileWriter(certFile));
                pr.writeObject(cert);
                pr.close();
            } else {
                logger.error("Unable to create to " + cp);
            }
        }
        return ks;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public synchronized X509Certificate getCaCert() throws IOException {
        X509Certificate cert;
        File certFile = new File(certPath);
        PEMReader pr = new PEMReader(new FileReader(certFile));
        cert = (X509Certificate) pr.readObject();
        pr.close();
        return cert;
    }

    public String getLocalKeyStorePath() {
        return keyStorePath;
    }

    public String getLocalAlias() {
        return alias;
    }

    public static X500Principal getLocalDN() throws KeyStoreException, Exception {
        X509Certificate cert = (X509Certificate) Auth.getAuth().getLocalKeyStore().getCertificate(Auth.ALIAS);
        return (X500Principal) cert.getSubjectX500Principal();
    }
}
