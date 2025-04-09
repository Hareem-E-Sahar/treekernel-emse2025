package com.pallas.unicore.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.acl.LastOwnerException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import com.pallas.unicore.resourcemanager.ResourceManager;
import com.pallas.unicore.utility.Base64;
import com.pallas.unicore.utility.UserMessages;

/**
 * This class manages an instance of a keystore described by provider, type,
 * file name.
 * 
 * @author Thomas Kentemich
 * @version $Id: KeyStoreManager.java,v 1.5 2006/11/08 10:07:28 bschuller Exp $
 */
public class KeyStoreManager {

    private static final Logger logger = Logger.getLogger("com.pallas.unicore.security");

    static final ResourceBundle res = ResourceBundle.getBundle("com.pallas.unicore.security.ResourceStrings");

    /**
	 * Gets an instance of the KeystoreManger
	 * 
	 * @param provider
	 *            JCE provider
	 * @param type
	 *            Keystore type
	 * @param name
	 *            Keystore File name
	 * @return Instance of the keystore manager
	 */
    public static KeyStoreManager getInstance(String provider, String type, String name) {
        return new KeyStoreManager(provider, type, name);
    }

    private Vector cachedKeyEntries;

    private Vector cachedTrustedCertificates;

    private KeyStore keyStore;

    private String keyStoreName = null;

    private String keyStoreType = null;

    private Cipher passwdCipher = null;

    private String provider = null;

    private byte[] rawKey;

    private byte[] rawStore;

    private Vector rejectedEntries;

    private KeyStore sslKeyStore;

    private Hashtable sslKeystores = new Hashtable();

    private Key tempKey = null;

    private Vector certificatesAboutToExpire;

    private Date nowPlusTwoWeeks = new Date(System.currentTimeMillis() + 1000 * 86400 * 14);

    private String aliasForLastCSRKeyEntry;

    /**
	 * Constructor for the KeyStoreManager object
	 */
    private KeyStoreManager() {
        this.cachedKeyEntries = new Vector();
        this.cachedTrustedCertificates = new Vector();
        this.rejectedEntries = new Vector();
    }

    /**
	 * Constructor for the KeyStoreManager object
	 * 
	 * @param provider
	 *            JCE provider
	 * @param type
	 *            Keystore type
	 * @param name
	 *            Keystore File name
	 */
    private KeyStoreManager(String provider, String type, String name) {
        this();
        logger.fine("New KeystoreManager for : " + name);
        this.keyStoreName = name;
        this.provider = provider;
        this.keyStoreType = type;
    }

    /**
	 * Adds a feature to the Entry attribute of the KeyStoreManager object
	 * 
	 * @param alias
	 *            The feature to be added to the Entry attribute
	 * @exception Exception
	 *                Description of the Exception
	 */
    private void addEntryToKeystoreManager(String alias) throws Exception {
        if (keyStore.isKeyEntry(alias)) {
            logger.fine("Add key entry: " + alias + " to the keystore manager");
            if (!cachedKeyEntries.contains(alias)) {
                try {
                    if (validateKeyEntry(alias, unwrapKeyPassword())) {
                        cachedKeyEntries.add(alias);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Rejected " + alias, e);
                    rejectedEntries.add(alias);
                }
            }
        } else if (keyStore.isCertificateEntry(alias)) {
            logger.fine("Add trustes certifcate entry: " + alias + " to the keystore manager");
            if (!cachedTrustedCertificates.contains(alias)) {
                try {
                    if (validateCertificateEntry(alias)) {
                        cachedTrustedCertificates.add(alias);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Rejected " + alias, e);
                    rejectedEntries.add(alias);
                }
            }
        }
    }

    /**
	 * Adds a key entry to the KeyStore
	 * 
	 * @param alias
	 *            Alias of the key
	 * @param key
	 *            The private key to store
	 * @param certificateChain
	 *            Certifcate chain (usually obtained by a CSR request)
	 * @param rehashKeystoreEntries
	 *            The feature to be added to the KeyEntry attribute
	 */
    public String addKeyEntry(PrivateKey key, Certificate[] certificateChain, String alias, boolean rehashKeystoreEntries) throws Exception {
        PrivateKey originalKey = (PrivateKey) keyStore.getKey(alias, unwrapKeyPassword());
        if (!key.equals(originalKey)) {
            alias = checkDuplicateAlias(alias);
        }
        keyStore.setKeyEntry(alias, key, unwrapKeyPassword(), certificateChain);
        alias = keyStore.getCertificateAlias(certificateChain[0]);
        logger.info("Added new key entry for: " + alias);
        if (rehashKeystoreEntries) {
            rehashKeystoreEntries();
        } else {
            this.addEntryToKeystoreManager(alias);
        }
        return alias;
    }

    /**
	 * Adds a trusted certificate to the KeyStore
	 * 
	 * @param cert
	 *            Trusted certificate entry to be added
	 * @param rehashKeystoreEntries
	 *            The feature to be added to the TrustedCertificate attribute
	 * @exception Exception
	 *                Description of the Exception
	 */
    public void addTrustedCertificate(Certificate cert, boolean rehashKeystoreEntries) throws Exception {
        String alias = CertificateUtility.getCommonName((X509Certificate) cert);
        Certificate original = keyStore.getCertificate(alias);
        if (original != null && !original.equals(cert)) {
            alias = checkDuplicateAlias(alias);
        }
        keyStore.setCertificateEntry(alias, cert);
        alias = keyStore.getCertificateAlias(cert);
        logger.info("Added new trusted certificate for: " + alias + " to the keystore");
        if (rehashKeystoreEntries) {
            rehashKeystoreEntries();
        } else {
            this.addEntryToKeystoreManager(alias);
        }
    }

    /**
	 * Generate an empty keystore
	 * 
	 *  
	 */
    public void buildEmptyKeystore(String name, char[] storePassword, char[] keyPassword) throws Exception {
        this.startPasswordEncryption(storePassword, keyPassword);
        if (provider == null) {
            keyStore = KeyStore.getInstance(keyStoreType);
        } else {
            keyStore = KeyStore.getInstance(keyStoreType, provider);
        }
        this.keyStoreName = name;
        keyStore.load(null, storePassword);
        rehashKeystoreEntries();
    }

    /**
	 * Change the keystore password
	 * 
	 *  
	 */
    public final void changePassword(char[] oldPassword, char[] newPassword) throws Exception {
        if (new String(oldPassword).equals(new String(unwrapStorePassword()))) {
            try {
                KeyGenerator generator = KeyGenerator.getInstance("DES");
                tempKey = generator.generateKey();
                passwdCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                passwdCipher.init(Cipher.ENCRYPT_MODE, tempKey);
                rawStore = passwdCipher.doFinal(new String(newPassword).getBytes());
                rawKey = passwdCipher.doFinal(new String(newPassword).getBytes());
                IvParameterSpec dps = new IvParameterSpec(passwdCipher.getIV());
                passwdCipher.init(Cipher.DECRYPT_MODE, tempKey, dps);
                tempKey = null;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Bad encryption for password", e);
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Key key = keyStore.getKey(alias, oldPassword);
                if (key instanceof PrivateKey) {
                    Certificate[] chain = keyStore.getCertificateChain(alias);
                    keyStore.setKeyEntry(alias, (PrivateKey) keyStore.getKey(alias, oldPassword), newPassword, chain);
                }
            }
            reWriteKeyStore(new File(keyStoreName));
        } else {
            throw new KeyStoreException("Old password was not correct.");
        }
    }

    /**
	 * Check the validity of the keystore: 1.) that the keystore is not empty
	 * and contains at least one key entry and one trusted certificate 2.) that
	 * the private key can be extracted and contains a valid certificate chain
	 * 3.) that the trusted certificates contain a valid certificate chain 4.)
	 * that the certificates are not expired etc.
	 * 
	 *  
	 */
    public String checkKeyStore(char[] keyPassword) throws Exception {
        logger.info("Checking keystore: " + keyStoreName + " type " + keyStore.getType());
        if (keyStore.size() == 0) {
            return res.getString("EMPTY_KEYSTORE");
        }
        cachedKeyEntries = new Vector();
        cachedTrustedCertificates = new Vector();
        rejectedEntries = new Vector();
        certificatesAboutToExpire = new Vector();
        Enumeration knownAliases = keyStore.aliases();
        while (knownAliases.hasMoreElements()) {
            String next = (String) knownAliases.nextElement();
            if (keyStore.isKeyEntry(next)) {
                logger.fine("Key entry: " + next);
                if (!cachedKeyEntries.contains(next)) {
                    try {
                        if (validateKeyEntry(next, keyPassword)) {
                            cachedKeyEntries.add(next);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Rejected " + next, e);
                        UserMessages.warning("Rejected " + next + e.getLocalizedMessage());
                        rejectedEntries.add(next);
                    }
                }
            } else if (keyStore.isCertificateEntry(next)) {
                Certificate caCert = keyStore.getCertificate(next);
                if (caCert instanceof X509Certificate) {
                    logger.fine("Trusted certifcate entry: " + next);
                    if (!cachedTrustedCertificates.contains(next)) {
                        try {
                            if (validateCertificateEntry(next)) {
                                cachedTrustedCertificates.add(next);
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Rejected " + next, e);
                            UserMessages.warning("Rejected " + next + e.getLocalizedMessage());
                            rejectedEntries.add(next);
                        }
                    }
                }
            }
        }
        if (cachedKeyEntries.size() == 0 || cachedTrustedCertificates.size() == 0) {
            return "Keystore does not contain key entries or trusted certificates";
        }
        logger.info("Keystore: " + keyStoreName + " checked");
        if (certificatesAboutToExpire.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("The following certificates will soon expire.\n");
            sb.append("(check your keystore for details)\n\n");
            for (int i = 0; i < certificatesAboutToExpire.size(); i++) {
                sb.append((String) certificatesAboutToExpire.elementAt(i) + "\n");
            }
            UserMessages.warning(sb.toString());
        }
        return "OK";
    }

    public void createSSLKeystore(boolean writeToDisc, boolean forceWrite, char[] storePassword, char[] keyPassword) throws Exception {
        logger.info("Start constructing cached SSL Keystores...");
        if (storePassword == null) {
            storePassword = unwrapStorePassword();
        }
        if (!keyStore.containsAlias(this.getDefaultAlias())) {
            logger.log(Level.SEVERE, "default identity <" + getDefaultAlias() + "> does not exist");
            return;
        }
        sslKeystores = new Hashtable();
        Enumeration aliases = keyStore.aliases();
        File ssl = null;
        KeyStore tmpSslKeyStore = null;
        String tmpName = null;
        while (aliases.hasMoreElements()) {
            String alias = (String) aliases.nextElement();
            if (!keyStore.isKeyEntry(alias)) {
                continue;
            }
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            tmpSslKeyStore = KeyStore.getInstance(KeyStore.getDefaultType(), "SUN");
            tmpName = keyStoreName + "_" + cert.hashCode() + ".ssl";
            ssl = new File(tmpName);
            if (ssl.exists() && !forceWrite) {
                logger.info("Found cached SSL Keystore: " + tmpName);
                try {
                    tmpSslKeyStore.load(new FileInputStream(ssl), storePassword);
                    if (tmpSslKeyStore.containsAlias(getDefaultAlias())) {
                        logger.info("SSL Keystore: " + tmpName + " contains default alias <" + getDefaultAlias() + ">");
                        sslKeyStore = tmpSslKeyStore;
                    }
                    logger.info("SSL Keystore: " + tmpName + " successfully loaded");
                    sslKeystores.put(cert, tmpSslKeyStore);
                    continue;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "SSL Keystore: " + tmpName + " cannot be loaded", e);
                }
            } else {
                logger.info("SSL Keystore: " + tmpName + " does not exist");
            }
            tmpSslKeyStore.load(null, storePassword);
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (keyPassword == null) {
                keyPassword = unwrapKeyPassword();
            }
            tmpSslKeyStore.setKeyEntry(alias, (PrivateKey) keyStore.getKey(alias, keyPassword), keyPassword, chain);
            Enumeration knownAliases = keyStore.aliases();
            while (knownAliases.hasMoreElements()) {
                String next = (String) knownAliases.nextElement();
                if (keyStore.isCertificateEntry(next)) {
                    Certificate c = keyStore.getCertificate(next);
                    tmpSslKeyStore.setCertificateEntry(next, c);
                }
            }
            sslKeystores.put(cert, tmpSslKeyStore);
            logger.info("Creating sslKeystore done");
            if (writeToDisc) {
                logger.info("Writing cached SSL keystore: " + tmpName + " type" + keyStore.getType());
                FileOutputStream fos = new FileOutputStream(tmpName);
                tmpSslKeyStore.store(fos, storePassword);
                fos.close();
            }
            if (alias.equals(this.getDefaultAlias())) {
                sslKeyStore = tmpSslKeyStore;
            }
        }
    }

    /**
	 * Filter the complete keystore to obtain one with the alias given and all
	 * trusted entries
	 *  
	 */
    public void createSSLKeystore(boolean writeToDisc, char[] storePassword, char[] keyPassword) throws Exception {
        createSSLKeystore(writeToDisc, false, storePassword, keyPassword);
    }

    /**
	 * Exports the public key of the PKCS12 certificate for an alias
	 * 
	 * @param alias
	 *            the name to export
	 * @param filename
	 *            output filenem
	 * @exception Exception
	 */
    public void exportUserCert(String alias, String filename) throws Exception {
        logger.info("Exporting X509 certificate: " + alias);
        X509Certificate[] cert = getCertificateByAlias(alias);
        int target = -1;
        for (int i = 0; i < cert.length; i++) {
            for (int j = 0; j < cert.length; j++) {
                if (cert[i].getSubjectDN().equals(cert[j].getIssuerDN())) {
                    continue;
                }
            }
            target = i;
            break;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            if (filename.endsWith(".der")) {
                fos.write(cert[target].getEncoded());
                fos.close();
            } else {
                String certStart = "-----BEGIN CERTIFICATE-----\n";
                String certEnd = "\n-----END CERTIFICATE-----\n";
                fos.write(certStart.getBytes());
                fos.write(Base64.encode(cert[target].getEncoded(), true));
                fos.write(certEnd.getBytes());
            }
        } catch (CertificateEncodingException e) {
            throw new KeyStoreException("Cannot encode certificate:" + e.getLocalizedMessage());
        } catch (IOException e) {
            throw new KeyStoreException(e.getMessage());
        } finally {
            fos.close();
        }
    }

    public byte[] generateCSR(String dnString, int keyLength) throws Exception {
        X509Name subject = CertificateUtility.stringToBcX509Name(dnString);
        KeyPair kp = CertificateUtility.generateNewKeys("RSA", keyLength);
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        X509Principal x509Principal = new X509Principal(subject);
        v3CertGen.reset();
        v3CertGen.setSerialNumber(new BigInteger(16, new Random()));
        v3CertGen.setIssuerDN(x509Principal);
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (90 * 1000L * 60 * 60 * 24)));
        v3CertGen.setSubjectDN(x509Principal);
        v3CertGen.setPublicKey(kp.getPublic());
        v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");
        X509Certificate[] cert = { v3CertGen.generateX509Certificate(kp.getPrivate()) };
        aliasForLastCSRKeyEntry = addKeyEntry(kp.getPrivate(), cert, CertificateUtility.getCommonName((X509Certificate) cert[0]), false);
        logger.info("Adding new CSR: " + cert[0] + " to the keystore manager");
        PKCS10CertificationRequest req1 = new PKCS10CertificationRequest("SHA1withRSA", subject, kp.getPublic(), null, kp.getPrivate());
        req1.verify();
        return req1.getEncoded();
    }

    /**
	 * cleanup last CSR
	 */
    public void deleteLastCSR() {
        try {
            removeKeyEntry(aliasForLastCSRKeyEntry);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not delete last CSR.", e);
        }
    }

    /**
	 * Gets the alias for a given certificate; returns null if not found
	 * 
	 * @param cert
	 *            The certificate to test
	 * @return Alias found.
	 * @exception Exception
	 */
    public String getAliasFromCertifcate(Certificate cert) throws Exception {
        X509Certificate x509 = null;
        String matchedAlias = null;
        for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
            String nextAlias = (String) aliases.nextElement();
            try {
                x509 = (X509Certificate) keyStore.getCertificate(nextAlias);
            } catch (Exception e) {
                continue;
            }
            if (x509.equals(cert)) {
                matchedAlias = nextAlias;
                break;
            }
        }
        return matchedAlias;
    }

    public Enumeration getAllEntries() {
        Enumeration result = null;
        try {
            result = keyStore.aliases();
        } catch (KeyStoreException kse) {
            logger.log(Level.SEVERE, "", kse);
        }
        return result;
    }

    public Vector getCachedKeyEntries() {
        return this.cachedKeyEntries;
    }

    public Vector getCachedTrustedCertificates() {
        return this.cachedTrustedCertificates;
    }

    /**
	 * Fetches the certificate known as alias
	 * 
	 * @param alias
	 *            Alias to fetch from the keystore
	 * @return X509 certificate chain or null
	 * @exception KeyStoreException
	 */
    public X509Certificate[] getCertificateByAlias(String alias) throws KeyStoreException {
        X509Certificate[] returnedChain = CertificateUtility.toX509(keyStore.getCertificateChain(alias.toLowerCase()));
        if (returnedChain == null) {
            X509Certificate trustedCertificate = (X509Certificate) keyStore.getCertificate(alias.toLowerCase());
            if (trustedCertificate != null) {
                returnedChain = new X509Certificate[1];
                returnedChain[0] = trustedCertificate;
            }
        }
        return returnedChain;
    }

    public X509Certificate getCertificateByKey(PublicKey key) throws Exception {
        X509Certificate x509 = null;
        X509Certificate matchedX509 = null;
        for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
            String nextAlias = (String) aliases.nextElement();
            try {
                x509 = (X509Certificate) keyStore.getCertificate(nextAlias);
            } catch (Exception e) {
                continue;
            }
            if (key.equals(x509.getPublicKey())) {
                matchedX509 = x509;
                break;
            }
        }
        return matchedX509;
    }

    /**
	 * Fetches the certificate by principal DN
	 * 
	 * @param principal
	 *            Principal to fetch from the keystore
	 * @return X509 certificate chain or null
	 * @exception KeyStoreException
	 */
    public X509Certificate getCertificateByPrincipal(Principal principal) throws KeyStoreException {
        X509Certificate x509 = null;
        X509Certificate matchedX509 = null;
        for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
            String nextAlias = (String) aliases.nextElement();
            try {
                x509 = (X509Certificate) keyStore.getCertificate(nextAlias);
            } catch (Exception e) {
                continue;
            }
            if (principal.equals(x509.getSubjectDN())) {
                matchedX509 = x509;
                break;
            }
        }
        return matchedX509;
    }

    /**
	 * Fetches the certificate by subject DN Does possibly not work for key
	 * entries as the Sun JCE inverts the DN string for key entries!!
	 * 
	 * @param subject
	 *            DN to fetch from the keystore
	 * @return X509 certificate chain or null
	 * @exception KeyStoreException
	 */
    public X509Certificate getCertificateBySubjectDN(String subject) throws KeyStoreException {
        X509Certificate x509 = null;
        X509Certificate matchedX509 = null;
        for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
            String nextAlias = (String) aliases.nextElement();
            try {
                x509 = (X509Certificate) keyStore.getCertificate(nextAlias);
            } catch (Exception e) {
                continue;
            }
            if (subject.equals(x509.getSubjectDN().getName())) {
                matchedX509 = x509;
                break;
            }
        }
        return matchedX509;
    }

    private String getDefaultAlias() {
        return ResourceManager.getUserDefaults().getDefaultIdentity();
    }

    /**
	 * Checks if the alias is already included in the keystore and extends the
	 * name by appending a number
	 * 
	 * @param alias
	 *            to check
	 * @return original or extended alias
	 * @throws KeyStoreException
	 */
    private String checkDuplicateAlias(String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            int i = 1;
            String newAlias = alias + " (" + i + ")";
            while (keyStore.containsAlias(newAlias)) {
                ++i;
                newAlias = alias + " (" + i + ")";
            }
            alias = newAlias;
        }
        return alias;
    }

    public String getKeyAliasFromCertificate(Certificate cert) throws Exception {
        X509Certificate x509 = null;
        String matchedAlias = null;
        for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
            String nextAlias = (String) aliases.nextElement();
            if (!keyStore.isKeyEntry(nextAlias)) {
                continue;
            }
            try {
                x509 = (X509Certificate) keyStore.getCertificate(nextAlias);
            } catch (Exception e) {
                continue;
            }
            if (x509.equals(cert)) {
                matchedAlias = nextAlias;
                break;
            }
        }
        return matchedAlias;
    }

    public final Key getKeyEntry(String alias, char[] keyPassword) throws Exception {
        return keyStore.getKey(alias, keyPassword);
    }

    public String getKeyStoreName() {
        return this.keyStoreName;
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }

    public Vector getRejectedEntries() {
        return this.rejectedEntries;
    }

    /**
	 * Returns a newly initialized Signature object
	 * 
	 * @param cert
	 * @return The signature
	 * @exception Exception
	 */
    public final Signature getSignature(X509Certificate cert) throws Exception {
        Signature signature = Signature.getInstance("MD5withRSA");
        String alias = getKeyAliasFromCertificate(cert);
        signature.initSign((PrivateKey) (keyStore.getKey(alias, unwrapKeyPassword())));
        return signature;
    }

    /**
	 * Returns a matching SSL Keystore for the requested Cert or null
	 * 
	 * @param cert
	 * @return Keystore or null
	 */
    public KeyStore getSSLKeystore(X509Certificate cert) {
        return (KeyStore) sslKeystores.get(cert);
    }

    /**
	 * Check, if we have a keystore available
	 * 
	 * @return true, if we have a keystore
	 */
    public boolean hasOpenKeystore() {
        return keyStore != null;
    }

    public SSLContext initializeSSLContext(SecureRandom secureRandom, String alias, char[] passwd) throws Exception {
        logger.info("Initializing SSL factory, default id: " + alias);
        SSLContext sslContext = SSLContext.getInstance("SSL");
        KeyManagerFactory keymanager = KeyManagerFactory.getInstance("SunX509");
        if (passwd == null) {
            passwd = unwrapStorePassword();
        }
        keymanager.init(sslKeyStore, passwd);
        KeyManager[] keymanagers = keymanager.getKeyManagers();
        TrustManagerFactory trustmanager = TrustManagerFactory.getInstance("SunX509");
        trustmanager.init(sslKeyStore);
        TrustManager[] trustmanagers = trustmanager.getTrustManagers();
        sslContext.init(keymanagers, trustmanagers, secureRandom);
        logger.info("Initializing SSL factory: Done.");
        return sslContext;
    }

    public SSLContext initializeSSLContext(SecureRandom secureRandom, X509Certificate cert) throws Exception {
        logger.info("Initializing SSL factory for: " + cert.getSubjectDN());
        SSLContext sslContext = SSLContext.getInstance("SSL");
        KeyManagerFactory keymanager = KeyManagerFactory.getInstance("SunX509");
        char[] passwd = unwrapStorePassword();
        KeyStore kk = (KeyStore) sslKeystores.get(cert);
        if (kk == null) {
            throw new Exception("Error initializing SSL connection: unknown cert: " + cert.getSubjectDN());
        }
        keymanager.init(kk, passwd);
        KeyManager[] keymanagers = keymanager.getKeyManagers();
        TrustManagerFactory trustmanager = TrustManagerFactory.getInstance("SunX509");
        trustmanager.init(kk);
        TrustManager[] trustmanagers = trustmanager.getTrustManagers();
        sslContext.init(keymanagers, trustmanagers, secureRandom);
        logger.info("Initializing SSL factory: Done.");
        return sslContext;
    }

    /**
	 * Check if this alias is a key entry
	 * 
	 * @param alias
	 * @return The keyEntry value
	 * @exception Exception
	 */
    public boolean isKeyEntry(String alias) throws Exception {
        return keyStore.isKeyEntry(alias);
    }

    public final boolean keyStoreContainsAlias(String alias) throws Exception {
        return keyStore.containsAlias(alias);
    }

    /**
	 * Load a keystore or define a new keystore and check it.
	 *  
	 */
    public final String loadKeyStore(char[] storePassword, char[] keyPassword) throws Exception {
        startPasswordEncryption(storePassword, keyPassword);
        this.keyStore = readKeyStore(storePassword);
        return checkKeyStore(keyPassword);
    }

    public void loadTrustedCertificate(File certFile) throws Exception {
        X509Certificate cert = (X509Certificate) CertificateUtility.importTrustedCertifcate(certFile);
        logger.fine("Importing cert for: " + ((X509Certificate) cert).getSubjectDN().getName());
        X509Certificate oriCert = getCertificateByKey(cert.getPublicKey());
        if (oriCert == null) {
            logger.info("Adding new trusted certificate: " + cert);
            addTrustedCertificate(cert, true);
        } else {
            String alias = getAliasFromCertifcate(oriCert);
            if (isKeyEntry(alias)) {
                logger.info("Replacing key certificate: " + alias);
                replaceKeyCertificate(alias, cert, true);
            } else {
                logger.info("Replacing trusted certifcate: " + alias);
                addTrustedCertificate(cert, true);
            }
        }
    }

    private void makeCertRequest(String dn, KeyPair rsaKeys, String reqfile) throws NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidKeyException, SignatureException {
        PKCS10CertificationRequest req = new PKCS10CertificationRequest("SHA1WithRSA", CertificateUtility.stringToBcX509Name(dn), rsaKeys.getPublic(), null, rsaKeys.getPrivate());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(req);
        dOut.close();
        ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
        DERInputStream dIn = new DERInputStream(bIn);
        PKCS10CertificationRequest req2 = new PKCS10CertificationRequest((DERSequence) dIn.readObject());
        boolean verify = req2.verify();
        System.out.println("Verify returned " + verify);
        if (verify == false) {
            System.out.println("Aborting!");
            return;
        }
        FileOutputStream os1 = new FileOutputStream(reqfile);
        os1.write("-----BEGIN CERTIFICATE REQUEST-----\n".getBytes());
        os1.write(new sun.misc.BASE64Encoder().encode(bOut.toByteArray()).getBytes());
        os1.write("\n-----END CERTIFICATE REQUEST-----\n".getBytes());
        os1.close();
        System.out.println("CertificationRequest '" + reqfile + "' generated succefully.");
    }

    /**
	 * Merges the contents of a second keystore with the current one
	 * 
	 * @param manager
	 *            The manager of the keystore to merge
	 * @param aliases
	 *            Alias list to include
	 * @param keyPassword
	 * @exception Exception
	 */
    public void mergeKeystore(KeyStoreManager manager, Vector aliases, char[] keyPassword) throws Exception {
        if (aliases == null) {
            return;
        }
        for (int i = 0; i < aliases.size(); i++) {
            String next = (String) aliases.elementAt(i);
            PrivateKey key = (PrivateKey) manager.getKeyEntry(next, keyPassword);
            X509Certificate[] chain = manager.getCertificateByAlias(next);
            if (key == null) {
                this.addTrustedCertificate(chain[0], false);
            } else {
                this.addKeyEntry(key, chain, next, false);
            }
            key = null;
        }
    }

    /**
	 * Read a keystore from storage.
	 * 
	 * @param storePassword
	 * @return @exception
	 *         Exception
	 */
    private KeyStore readKeyStore(char[] storePassword) throws Exception {
        logger.info("Open keystores: " + keyStoreName);
        if (provider == null) {
            keyStore = KeyStore.getInstance(keyStoreType);
        } else {
            keyStore = KeyStore.getInstance(keyStoreType, provider);
        }
        File f = new File(keyStoreName);
        keyStore.load(new FileInputStream(f), storePassword);
        logger.info("Keystore: " + keyStoreName + " successfully loaded");
        try {
            File chmod = new File("/bin/chmod");
            if (!chmod.exists()) chmod = new File("/usr/bin/chmod");
            if (chmod.exists()) {
                String cmd = chmod.getAbsolutePath() + " 600 " + f.getAbsolutePath();
                Runtime.getRuntime().exec(cmd);
                logger.info("Running " + cmd + ", setting keystore permissions to user rw.");
            }
        } catch (Exception e) {
        }
        return keyStore;
    }

    /**
	 * Cache key entries and trusted certificates
	 * 
	 * @exception Exception
	 */
    public void rehashKeystoreEntries() throws Exception {
        cachedKeyEntries = new Vector();
        cachedTrustedCertificates = new Vector();
        rejectedEntries = new Vector();
        Enumeration knownAliases = keyStore.aliases();
        while (knownAliases.hasMoreElements()) {
            String next = (String) knownAliases.nextElement();
            if (keyStore.isKeyEntry(next)) {
                logger.fine("Rehash key entry: " + next);
                if (!cachedKeyEntries.contains(next)) {
                    try {
                        if (validateKeyEntry(next, unwrapKeyPassword())) {
                            cachedKeyEntries.add(next);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Rejected " + next, e);
                        rejectedEntries.add(next);
                    }
                }
            } else if (keyStore.isCertificateEntry(next)) {
                logger.fine("Rehash trustes certifcate entry: " + next);
                if (!cachedTrustedCertificates.contains(next)) {
                    try {
                        if (validateCertificateEntry(next)) {
                            cachedTrustedCertificates.add(next);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Rejected " + next, e);
                        rejectedEntries.add(next);
                    }
                }
            }
        }
    }

    public final String reloadKeystore() throws Exception {
        this.keyStore = readKeyStore(unwrapStorePassword());
        return checkKeyStore(unwrapKeyPassword());
    }

    /**
	 * Changes the alias name of an entry
	 * 
	 * @param oldAlias --
	 *            the original
	 * @param newAlias --
	 *            the new name
	 * @return old alias
	 */
    public String changeAlias(String oldAlias, String newAlias) throws Exception {
        if (keyStore.containsAlias(oldAlias)) {
            Key key = keyStore.getKey(oldAlias, unwrapKeyPassword());
            Certificate[] chain = keyStore.getCertificateChain(oldAlias);
            keyStore.deleteEntry(oldAlias);
            cachedKeyEntries.removeElement(oldAlias);
            keyStore.setKeyEntry(newAlias, key, unwrapKeyPassword(), chain);
            rehashKeystoreEntries();
            return keyStore.getCertificateAlias(chain[0]);
        }
        return oldAlias;
    }

    public void removeEntry(String alias) throws Exception {
        if (keyStore.containsAlias(alias)) {
            if (keyStore.isKeyEntry(alias)) {
                removeKeyEntry(alias);
            } else {
                removeTrustedCertificateEntry(alias);
            }
            rehashKeystoreEntries();
        }
    }

    /**
	 * Removes a key entry from the keystore
	 * 
	 * @param alias
	 *            the alias of the key entry
	 * @exception KeyStoreException
	 *                Exception thrown
	 */
    public void removeKeyEntry(String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
            cachedKeyEntries.removeElement(alias);
        }
    }

    /**
	 * Removes a trusted certificate from the keystore;
	 * 
	 *  
	 */
    public void removeTrustedCertificateEntry(String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
            cachedTrustedCertificates.removeElement(alias);
        }
    }

    /**
	 * Replaces a certifcate associated with a key entry with a new certficate/
	 * This is done for example when importing a cert after sending a CSR
	 * 
	 *  
	 */
    public String replaceKeyCertificate(String alias, Certificate cert, boolean rehashKeystoreEntries) throws Exception {
        if (keyStore.isKeyEntry(alias)) {
            Certificate[] chain = { cert };
            alias = this.addKeyEntry((PrivateKey) keyStore.getKey(alias, unwrapKeyPassword()), chain, alias, rehashKeystoreEntries);
        }
        return alias;
    }

    public void resetKeystore() {
        keyStore = null;
    }

    /**
	 * Writes a jks type keystore to permanent storage.
	 *  
	 */
    public void reWriteKeyStore() throws Exception {
        File defaultFile = new File(keyStoreName);
        reWriteKeyStore(defaultFile);
        createSSLKeystore(true, true, unwrapStorePassword(), unwrapKeyPassword());
    }

    /**
	 * Writes a jks type keystore to permanent storage.
	 *  
	 */
    public void reWriteKeyStore(File fileName) throws Exception {
        String check = checkKeyStore(unwrapStorePassword());
        logger.info("Writing keystore: " + fileName.getCanonicalPath() + " type " + keyStore.getType());
        FileOutputStream fos = new FileOutputStream(fileName);
        keyStore.store(fos, unwrapStorePassword());
        if (!check.equals("OK")) {
            logger.severe("Incomplete keystore: " + check);
            throw new Exception(check);
        }
    }

    private void setDefaultAlias(String alias) {
        ResourceManager.getUserDefaults().setDefaultIdentity(alias);
    }

    /**
	 * Setup the relevant system properties for client authenticated https
	 * connections. Used by gridservice connections
	 * 
	 * @param cert
	 */
    public void setupAxisKeymanger(X509Certificate cert) throws SecurityException {
        KeyStore ssl = (KeyStore) sslKeystores.get(cert);
        if (ssl == null) {
            throw new SecurityException("Cannot locate ssl keystore for: " + cert.getIssuerDN());
        }
        System.setProperty("keystore", keyStoreName + "_" + cert.hashCode() + ".ssl");
        System.setProperty("keystoreType", getKeyStoreType());
        System.setProperty("keystorePass", new String(unwrapStorePassword()));
        System.setProperty("clientauth", "true");
        System.setProperty("keypass", new String(unwrapStorePassword()));
    }

    public final X509Certificate signCertificate(org.bouncycastle.jce.X509V3CertificateGenerator generator, X509Certificate original) throws Exception {
        String alias = getKeyAliasFromCertificate(original);
        return generator.generateX509Certificate((PrivateKey) keyStore.getKey(alias, unwrapKeyPassword()));
    }

    private void startPasswordEncryption(final char[] storePassword, final char[] keyPassword) {
        Thread thread = new Thread() {

            public void run() {
                try {
                    KeyGenerator generator = KeyGenerator.getInstance("DES");
                    tempKey = generator.generateKey();
                    passwdCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                    passwdCipher.init(Cipher.ENCRYPT_MODE, tempKey);
                    rawStore = passwdCipher.doFinal(new String(storePassword).getBytes());
                    rawKey = passwdCipher.doFinal(new String(keyPassword).getBytes());
                    IvParameterSpec dps = new IvParameterSpec(passwdCipher.getIV());
                    passwdCipher.init(Cipher.DECRYPT_MODE, tempKey, dps);
                    tempKey = null;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Cannot encrypt keystore password", e);
                }
            }
        };
        thread.start();
    }

    private final char[] unwrapKeyPassword() {
        try {
            while (rawKey == null) {
                Thread.sleep(10);
            }
            return new String(passwdCipher.doFinal(rawKey)).toCharArray();
        } catch (Exception e) {
            return null;
        }
    }

    private final char[] unwrapStorePassword() {
        try {
            while (rawStore == null) {
                Thread.sleep(10);
            }
            return new String(passwdCipher.doFinal(rawStore)).toCharArray();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateCertificateEntry(String alias) throws Exception {
        X509Certificate trustedCertificate = (X509Certificate) keyStore.getCertificate(alias.toLowerCase());
        trustedCertificate.checkValidity();
        try {
            trustedCertificate.checkValidity(nowPlusTwoWeeks);
        } catch (Exception e) {
            certificatesAboutToExpire.add(CertificateUtility.getCommonName(trustedCertificate));
        }
        return true;
    }

    /**
	 * Checks if the key entry specified by alias can be extrcated
	 * 
	 * @param alias
	 *            The alias to check.
	 * @param keyPassword
	 *            Description of the Parameter
	 * @return Return true if ok.
	 * @exception Exception
	 *                Various exceptions if key or password is invalid
	 */
    private boolean validateKeyEntry(String alias, char[] keyPassword) throws Exception {
        keyStore.getKey(alias, keyPassword);
        Certificate[] chain = keyStore.getCertificateChain(alias);
        for (int i = 0; i < chain.length; i++) {
            X509Certificate next = (X509Certificate) chain[i];
            Date validTo = next.getNotAfter();
            Date now = new Date(System.currentTimeMillis());
            if (now.after(validTo)) {
                throw new CertificateException("Certificate expired: " + CertificateUtility.getCommonName(next));
            }
        }
        return true;
    }

    /**
	 * Try to verify the user certificate (which is the first certificate in the
	 * chain) by a trusted certificate from the keystore;
	 *  
	 */
    public boolean verify(X509Certificate[] chain) {
        if (!CertificateUtility.checkOrder(chain)) {
            return false;
        }
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            Principal issuerDN = cert.getIssuerDN();
            X509Certificate issuerStored = null;
            try {
                issuerStored = getCertificateByPrincipal(issuerDN);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Cannot locate certificate issuer " + issuerDN + "in the keystore", e);
            }
            if (issuerStored != null) {
                X509Certificate issuer;
                if (i + 1 < chain.length) {
                    issuer = chain[i + 1];
                    if (!issuer.equals(issuerStored)) {
                        UserMessages.error("Stored issuer certificate  not identical to issuer certificate in chain:" + "\nStored: " + issuerStored.getSubjectDN() + "\nChain: " + issuer.getSubjectDN());
                        return false;
                    }
                }
                try {
                    cert.verify(issuerStored.getPublicKey());
                    for (int j = i - 1; j >= 0; j--) {
                        issuer = cert;
                        cert = chain[j];
                        cert.verify(issuer.getPublicKey());
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Cannot verify certificate issuer " + issuerDN, e);
                    return false;
                }
                logger.info("Plugin signer certificate verified by: " + issuerDN);
                return true;
            }
        }
        return false;
    }
}
