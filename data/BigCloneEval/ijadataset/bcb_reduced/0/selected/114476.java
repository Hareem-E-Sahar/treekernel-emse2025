package pl.umk.webclient.impl.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
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
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import com.intel.gpe.security.CertificateUtility;

/**
 * This class manages an instance of a keystore described by provider, type,
 * file name.
 *
 * @author Thomas Kentemich
 * @version $Id: WebKeyStoreManager.java,v 1.1.1.1 2008/10/26 11:14:59 bartosz_pieslak Exp $
 */
public class WebKeyStoreManager {

    private static final Logger logger = Logger.getLogger("com.intel.gpe");

    private File f;

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
    public static WebKeyStoreManager getInstance(String provider, String type, String name, File keystoreFile) {
        return new WebKeyStoreManager(provider, type, name, keystoreFile);
    }

    private Vector cachedKeyEntries = new Vector();

    private Vector cachedTrustedCertificates = new Vector();

    private Vector rejectedEntries = new Vector();

    private String defaultAlias = "";

    private KeyStore keyStore;

    private String keyStoreName = null;

    private String keyStoreType = null;

    private Cipher passwdCipher = null;

    private String provider = null;

    private byte[] rawKey;

    private byte[] rawStore;

    private KeyStore sslKeyStore;

    private Hashtable sslKeystores = new Hashtable();

    private Key tempKey = null;

    /**
     * Constructor for the KeyStoreManager object
     */
    private WebKeyStoreManager() {
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
    private WebKeyStoreManager(String provider, String type, String name, File keyStoreFile) {
        logger.info("New KeystoreManager for : " + name + " provider: [" + provider + "] type: " + type);
        this.f = keyStoreFile;
        this.keyStoreName = name;
        this.provider = provider;
        this.keyStoreType = type;
        Provider bouncy = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(bouncy);
        logger.info("Adding new security provider: " + bouncy.getName());
    }

    /**
     * Setup the relevant system properties for client authenticated https
     * connections.
     */
    public void setupSSLSecurity() throws SecurityException {
        System.setProperty("javax.net.ssl.keyStore", getKeyStoreName());
        System.setProperty("javax.net.ssl.keyStoreType", getKeyStoreType());
        System.setProperty("javax.net.ssl.keyStorePassword", new String(unwrapStorePassword()));
        System.setProperty("javax.net.ssl.trustStore", getKeyStoreName());
        System.setProperty("javax.net.ssl.trustStoreType", getKeyStoreType());
        System.setProperty("javax.net.ssl.trustStorePassword", new String(unwrapStorePassword()));
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

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception Exception
     *                Description of the Exception
     */
    public final String reloadKeystore() throws Exception {
        this.keyStore = readKeyStore(unwrapStorePassword());
        return checkKeyStore(unwrapKeyPassword());
    }

    /**
     * Check, if we have a keystore available
     *
     * @return true, if we have a keystore
     */
    public boolean hasOpenKeystore() {
        return keyStore != null;
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
     */
    public void addKeyEntry(PrivateKey key, Certificate[] certificateChain, String alias) throws Exception {
        keyStore.setKeyEntry(alias, key, unwrapKeyPassword(), certificateChain);
        logger.info("Added new key entry for: " + alias);
        createSSLKeystore(true, true, unwrapStorePassword(), unwrapKeyPassword());
        rehashKeystoreEntries();
    }

    /**
     * Adds a trusted certificate to the KeyStore
     *
     * @param cert
     *            Trusted certificate entry to be added
     */
    public void addTrustedCertificate(String alias, Certificate cert) throws Exception {
        keyStore.setCertificateEntry(alias, cert);
        logger.info("Added new trusted certificate for: " + alias);
        createSSLKeystore(true, true, unwrapStorePassword(), unwrapKeyPassword());
        rehashKeystoreEntries();
    }

    public void addTrustedCertificate(Certificate cert) throws Exception {
        String alias = CertificateUtility.getCommonName((X509Certificate) cert);
        if (keyStore.containsAlias(alias)) {
            int i = 1;
            while (true) {
                if (!keyStore.containsAlias(alias + "(" + i + ")")) {
                    alias = alias + "(" + i + ")";
                    break;
                }
                i++;
            }
        }
        addTrustedCertificate(alias, cert);
    }

    /**
     * Generate an empty keystore
     */
    public void buildEmptyKeystore(String name, char[] storePassword, char[] keyPassword) throws Exception {
        this.startPasswordEncryption(storePassword, keyPassword);
        keyStore = KeyStore.getInstance(keyStoreType);
        this.keyStoreName = name;
        keyStore.load(null, storePassword);
        rehashKeystoreEntries();
    }

    /**
     * Change the keystore password
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
                logger.severe("Bad encryption for passwd: ");
                e.printStackTrace();
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Key key = keyStore.getKey(alias, oldPassword);
                if (key instanceof PrivateKey) {
                    Certificate[] chain = keyStore.getCertificateChain(alias);
                    keyStore.setKeyEntry(alias, keyStore.getKey(alias, oldPassword), newPassword, chain);
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
     */
    public String checkKeyStore(char[] keyPassword) throws Exception {
        logger.info("Checking keystore: " + keyStoreName + " type " + keyStore.getType());
        if (keyStore.size() == 0) {
            return "Keystore is emtpy.";
        }
        cachedKeyEntries = new Vector();
        cachedTrustedCertificates = new Vector();
        rejectedEntries = new Vector();
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
        return "OK";
    }

    public void createSSLKeystore(boolean writeToDisc, boolean forceWrite, char[] storePassword, char[] keyPassword) throws Exception {
        logger.info("Start constructing cached SSL Keystores...");
        if (storePassword == null) {
            storePassword = unwrapStorePassword();
        }
        if (defaultAlias == null) {
            return;
        }
        if (!keyStore.containsAlias(defaultAlias)) {
            Enumeration aliases = keyStore.aliases();
            if (!aliases.hasMoreElements()) {
                defaultAlias = null;
                throw new KeyStoreException("No default identity in keystore");
            }
            defaultAlias = (String) aliases.nextElement();
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
                    if (tmpSslKeyStore.containsAlias(defaultAlias)) {
                        logger.warning("SSL Keystore: " + tmpName + " contains default alias");
                        sslKeyStore = tmpSslKeyStore;
                    }
                    logger.info("SSL Keystore: " + tmpName + " successfully loaded");
                    sslKeystores.put(cert, tmpSslKeyStore);
                    continue;
                } catch (Exception e) {
                    logger.warning("SSL Keystore: " + tmpName + " cannot be loaded");
                }
            } else {
                logger.info("SSL Keystore: " + tmpName + " does not exist");
            }
            tmpSslKeyStore.load(null, storePassword);
            Certificate[] chain = keyStore.getCertificateChain(alias);
            if (keyPassword == null) {
                keyPassword = unwrapKeyPassword();
            }
            tmpSslKeyStore.setKeyEntry(alias, keyStore.getKey(alias, keyPassword), keyPassword, chain);
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
            if (alias.equals(defaultAlias)) {
                sslKeyStore = tmpSslKeyStore;
            }
        }
    }

    /**
     * Filter the complete keystore to obtain one with the alias given and all
     * trusted entries
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
            CertificateUtility.writeCertToPEM(fos, cert[target]);
            fos.close();
        } catch (CertificateEncodingException e) {
            try {
                fos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            throw new KeyStoreException("Cannot encode certificate:" + e.getLocalizedMessage());
        } catch (IOException e) {
            throw new KeyStoreException(e.getMessage());
        }
    }

    public PKCS10CertificationRequest generateCSR(String dnString, int keyLength) throws Exception {
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
        addKeyEntry(kp.getPrivate(), cert, CertificateUtility.getCommonName(cert[0]));
        rehashKeystoreEntries();
        PKCS10CertificationRequest req1 = new PKCS10CertificationRequest("SHA1withRSA", subject, kp.getPublic(), null, kp.getPrivate());
        req1.verify();
        return req1;
    }

    /**
     * Gets the alias for a given certificate; returns nulll if not found
     *
     * @param cert
     *            The certificate to test
     * @return Alias found.
     */
    public String getAliasFromCertificate(Certificate cert) throws Exception {
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
     */
    public X509Certificate[] getCertificateByAlias(String alias) throws KeyStoreException {
        X509Certificate[] returnedChain = CertificateUtility.toX509(keyStore.getCertificateChain(alias));
        if (returnedChain == null) {
            X509Certificate trustedCertificate = (X509Certificate) keyStore.getCertificate(alias);
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

    public String getDefaultAlias() {
        return this.defaultAlias;
    }

    public void setDefaultAlias(String defaultAlias) {
        this.defaultAlias = defaultAlias;
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

    public final Key getKeyEntry(String alias) throws Exception {
        return keyStore.getKey(alias, unwrapKeyPassword());
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
     * @return The signature
     * @exception Exception
     *                Exception
     */
    public final Signature getSignature(X509Certificate cert) throws Exception {
        Signature signature = Signature.getInstance("MD5withRSA");
        String alias = getKeyAliasFromCertificate(cert);
        signature.initSign((PrivateKey) (keyStore.getKey(alias, unwrapKeyPassword())));
        return signature;
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
     */
    public boolean isKeyEntry(String alias) throws Exception {
        return keyStore.isKeyEntry(alias);
    }

    public final boolean keyStoreContainsAlias(String alias) throws Exception {
        return keyStore.containsAlias(alias);
    }

    /**
     * Load a keystore or define a new keystore and check it.
     */
    public final String loadKeyStore(char[] storePassword, char[] keyPassword) throws Exception {
        startPasswordEncryption(storePassword, keyPassword);
        this.keyStore = readKeyStore(storePassword);
        if (defaultAlias == null || !keyStore.containsAlias(defaultAlias)) {
            for (Enumeration aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String nextAlias = (String) aliases.nextElement();
                if (keyStore.isKeyEntry(nextAlias)) {
                    defaultAlias = nextAlias;
                    break;
                }
            }
        }
        return checkKeyStore(keyPassword);
    }

    /**
     * Merges the contents of a second keystore with the current one
     *
     * @param manager
     *            The manager of the keystore to merge
     * @param aliases
     *            Alias list to include
     */
    public void mergeKeystore(WebKeyStoreManager manager, Vector aliases, char[] keyPassword) throws Exception {
        if (aliases == null) {
            return;
        }
        for (int i = 0; i < aliases.size(); i++) {
            String next = (String) aliases.elementAt(i);
            PrivateKey key = (PrivateKey) manager.getKeyEntry(next, keyPassword);
            X509Certificate[] chain = manager.getCertificateByAlias(next);
            if (key == null) {
                addTrustedCertificate(next, chain[0]);
            } else {
                addKeyEntry(key, chain, next);
            }
            key = null;
        }
    }

    /**
     * Private helper that reads a keystore from storage.
     */
    private KeyStore readKeyStore(char[] storePassword) throws Exception {
        logger.info("Open keystores: " + keyStoreName + " type: " + keyStoreType + " provider: " + provider);
        if (provider == null) {
            keyStore = KeyStore.getInstance(keyStoreType);
        } else {
            keyStore = KeyStore.getInstance(keyStoreType, provider);
        }
        keyStore.load(new FileInputStream(f), storePassword);
        logger.info("Keystore: " + keyStoreName + " successfully loaded");
        return keyStore;
    }

    /**
     * Cache key entries and trusted certificates
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

    public void removeEntry(String alias) throws Exception {
        if (keyStore.containsAlias(alias)) {
            if (keyStore.isKeyEntry(alias)) {
                removeKeyEntry(alias);
            } else {
                removeTrustedCertificateEntry(alias);
            }
            createSSLKeystore(true, true, unwrapStorePassword(), unwrapKeyPassword());
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
     * @param alias
     *            Description of the Parameter
     * @exception KeyStoreException
     *                Description of the Exception
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
     */
    public void replaceKeyCertificate(String alias, Certificate cert) throws Exception {
        if (keyStore.isKeyEntry(alias)) {
            Certificate[] chain = { cert };
            addKeyEntry((PrivateKey) keyStore.getKey(alias, unwrapKeyPassword()), chain, alias);
            createSSLKeystore(true, true, unwrapStorePassword(), unwrapKeyPassword());
        }
    }

    /**
     * Writes a jks type keystore to permanent storage.
     */
    public void reWriteKeyStore() throws Exception {
        File defaultFile = new File(keyStoreName);
        reWriteKeyStore(defaultFile);
    }

    /**
     * Writes a jks type keystore to permanent storage.
     */
    public void reWriteKeyStore(File fileName) throws Exception {
        String check = checkKeyStore(unwrapStorePassword());
        logger.info("Writing keystore: " + fileName.getCanonicalPath() + " type " + keyStore.getType());
        fileName.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(fileName);
        keyStore.store(fos, unwrapStorePassword());
        if (!check.equals("OK")) {
            logger.info("Incomplete keystore: " + check);
        }
    }

    public final X509Certificate signCertificate(org.bouncycastle.jce.X509V3CertificateGenerator generator, X509Certificate original) throws Exception {
        String alias = getKeyAliasFromCertificate(original);
        logger.info("Signing new certificate with alias: " + alias);
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
                    e.printStackTrace();
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
     * @param chain
     *            Description of the Parameter
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
                e.printStackTrace();
            }
            if (issuerStored != null) {
                X509Certificate issuer;
                if (i + 1 < chain.length) {
                    issuer = chain[i + 1];
                    if (!issuer.equals(issuerStored)) {
                        logger.warning("Stored issuer certificate  not identical to issuer certificate in chain:" + "\nStored: " + issuerStored.getSubjectDN() + "\nChain: " + issuer.getSubjectDN());
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
                    e.printStackTrace();
                    return false;
                }
                logger.info("Plugin signer certificate verified by: " + issuerDN);
                return true;
            }
        }
        return false;
    }
}
