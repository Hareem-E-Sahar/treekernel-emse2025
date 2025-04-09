package tools.keytool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import codec.x509.AlgorithmIdentifier;

public class JKSManager {

    public static KeyStore openJKS(String ksLocation, char[] ksPass) throws FileNotFoundException, SecurityException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        KeyStore keyStore = null;
        FileInputStream fis;
        if (ksPass == null) {
            throw new IllegalArgumentException("Password must not be empty!");
        }
        if (ksPass.equals("")) {
            throw new IllegalArgumentException("Password must not be empty!");
        }
        try {
            keyStore = KeyStore.getInstance("JKS");
            fis = new FileInputStream(ksLocation);
            keyStore.load(fis, ksPass);
            fis.close();
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("The path to the jks file is not valid!");
        } catch (NoSuchAlgorithmException nsae) {
            throw nsae;
        } catch (CertificateException ce) {
            throw new CertificateException("No certificates in the keystore could be loaded");
        } catch (KeyStoreException kse) {
            throw new KeyStoreException("A JKS keystore is not available from the specified provider! " + "Please choose another provider!");
        } catch (SecurityException se) {
            throw new SecurityException("No permission to read the jks file!");
        }
        return keyStore;
    }

    public static HashMap<String, KeystoreEntryDefinition> getJKSContent(String ksLocation, char[] ksPass, Provider provider) throws FileNotFoundException, SecurityException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableEntryException {
        HashMap<String, KeystoreEntryDefinition> content;
        KeyStore.TrustedCertificateEntry tcEntry;
        KeyStore.PasswordProtection params;
        KeyStore.PrivateKeyEntry pkEntry;
        KeyStore.SecretKeyEntry skEntry;
        KeystoreEntryDefinition entry;
        Enumeration<String> en;
        Certificate[] chain;
        String alias;
        KeyStore ks;
        ks = openJKS(ksLocation, ksPass);
        content = new HashMap<String, KeystoreEntryDefinition>();
        en = ks.aliases();
        while (en.hasMoreElements()) {
            alias = en.nextElement();
            if (ks.entryInstanceOf(alias, KeyStore.SecretKeyEntry.class)) {
                params = new KeyStore.PasswordProtection(ksPass);
                try {
                    skEntry = (SecretKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(ksPass));
                    entry = new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, skEntry, params, null, KeystoreEntryDefinition.SECRET_KEY_ENTRY, false);
                    content.put(alias, entry);
                } catch (UnrecoverableEntryException uee) {
                    entry = new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, null, params, null, KeystoreEntryDefinition.SECRET_KEY_ENTRY, true);
                    content.put(alias, entry);
                }
            }
            if (ks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                params = new KeyStore.PasswordProtection(ksPass);
                try {
                    pkEntry = (PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(ksPass));
                    chain = ks.getCertificateChain(alias);
                    entry = new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, pkEntry, params, chain, KeystoreEntryDefinition.PRIVATE_KEY_ENTRY, false);
                    content.put(alias, entry);
                } catch (UnrecoverableEntryException uee) {
                    chain = ks.getCertificateChain(alias);
                    entry = new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, null, params, chain, KeystoreEntryDefinition.PRIVATE_KEY_ENTRY, true);
                    content.put(alias, entry);
                }
            }
            if (ks.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry.class)) {
                tcEntry = (TrustedCertificateEntry) ks.getEntry(alias, null);
                chain = new Certificate[1];
                chain[0] = tcEntry.getTrustedCertificate();
                entry = new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, tcEntry, null, chain, KeystoreEntryDefinition.TRUSTED_CERTIFICATE_ENTRY, false);
                content.put(alias, entry);
            }
        }
        return content;
    }

    public static boolean containsKeyAlias(String ksLocation, char[] ksPass, Provider provider, String alias) throws FileNotFoundException, SecurityException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        KeyStore ks;
        ks = openJKS(ksLocation, ksPass);
        return ks.containsAlias(alias);
    }

    public static KeystoreEntryDefinition getProtectedKeyEntry(String alias, String ksLocation, char[] ksPass, Provider provider, char[] keyPass) throws FileNotFoundException, SecurityException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableEntryException {
        KeyStore.PasswordProtection prot;
        Certificate[] chain;
        KeyStore.Entry ent;
        KeyStore ks;
        ks = openJKS(ksLocation, ksPass);
        prot = new KeyStore.PasswordProtection(keyPass);
        ent = ks.getEntry(alias, prot);
        if (ent instanceof KeyStore.PrivateKeyEntry) {
            chain = ((PrivateKeyEntry) ent).getCertificateChain();
            return new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, ent, prot, chain, KeystoreEntryDefinition.PRIVATE_KEY_ENTRY, false);
        }
        if (ent instanceof KeyStore.SecretKeyEntry) {
            return new KeystoreEntryDefinition(alias, ksLocation, ksPass, provider, ent, prot, null, KeystoreEntryDefinition.SECRET_KEY_ENTRY, false);
        }
        return null;
    }

    public static void generateNewSelfSignedKeystore(String ksLocation, char[] ksPass, Provider provider, Vector<KeyDefinition> keys) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, InvalidKeyException {
        Certificate[] chain;
        KeyDefinition key;
        SignKey signer;
        File newKSFile;
        String alias;
        Iterator it;
        FileOutputStream fos = null;
        X509Certificate cert = null;
        X509Certificate s_cert = null;
        KeyStore newKS = null;
        KeyPair pair = null;
        try {
            newKS = KeyStore.getInstance("JKS");
        } catch (KeyStoreException kse) {
            throw new KeyStoreException("The requested keystore type is not available from the " + "default provider!");
        } catch (IllegalArgumentException iae) {
            throw new KeyStoreException("The chosen provider is empty! Choose another one!");
        }
        newKS.load(null, ksPass);
        it = keys.iterator();
        while (it.hasNext()) {
            key = (KeyDefinition) it.next();
            try {
                pair = generateKeyPair(key.getType(), key.getKeysize(), provider);
                cert = generateCertificate(key.getDNameObject(), key.getDNameObject(), key.getValidity(), key.getSigAlg(), pair.getPublic());
                chain = new Certificate[1];
                chain[0] = cert;
            } catch (Exception e) {
                throw new KeyStoreException("There occurred the following problem while trying to " + "generate key and certificates for the entry with parameters:\n Alias: " + key.getAlias() + "\nKey ID: " + key.getName() + ":\n" + e.getClass().getName() + ": " + e.getMessage());
            }
            signer = new SignKey();
            try {
                s_cert = signer.sign(cert, pair.getPrivate(), cert, key.getSigAlg(), key.getUsageArray(), new Integer(0));
            } catch (Exception e) {
                throw new KeyStoreException("There occurred the following problem while trying to " + "sign the certificate of the entry with parameters: " + "\n Alias: " + key.getAlias() + "\nKey ID: " + key.getName() + ":\n" + e.getClass().getName() + ": " + e.getMessage());
            }
            chain = new Certificate[1];
            chain[0] = s_cert;
            alias = key.getAlias();
            newKS.setKeyEntry(alias, pair.getPrivate(), ksPass, chain);
        }
        try {
            newKSFile = new File(ksLocation);
            newKSFile.createNewFile();
            fos = new FileOutputStream(newKSFile);
            newKS.store(fos, ksPass);
            fos.close();
        } catch (Exception e) {
            throw new KeyStoreException("There occurred the following problem while trying to " + "save the generated keystore in the file: " + ksLocation + ":\n" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void generateNewCASignedKeystore(String ksLocation, char[] ksPass, Provider provider, String caKSLocation, String caKSAlias, char[] caKSPass, Provider caProvider, String caType, String caSigAlg, Vector<KeyDefinition> keys) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        Certificate[] caChain;
        Certificate[] chain;
        KeyDefinition key;
        PrivateKey caKey;
        SignKey signer;
        File newKSFile;
        String alias;
        Iterator it;
        X509Certificate signedCert = null;
        X509Certificate trustedCert = null;
        X509Certificate cert = null;
        FileOutputStream fos = null;
        KeyStore newKS = null;
        KeyStore caKS = null;
        KeyPair pair = null;
        try {
            if (provider == null) {
                newKS = KeyStore.getInstance("JKS", provider);
            } else {
                newKS = KeyStore.getInstance("JKS");
            }
        } catch (KeyStoreException kse) {
            if (provider == null) {
                throw new KeyStoreException("The requested keystore type is not available from the " + "default provider!");
            } else {
                throw new KeyStoreException("The requested keystore type is not available from the " + "chosen provider!");
            }
        } catch (IllegalArgumentException iae) {
            if (provider == null) {
                throw new IllegalArgumentException("The default provider is empty! Choose another one!");
            } else {
                throw new KeyStoreException("The chosen provider is empty! Choose another one!");
            }
        }
        newKS.load(null, ksPass);
        try {
            caKS = openJKS(caKSLocation, caKSPass);
            if (!caKS.isKeyEntry(caKSAlias)) {
                throw new Exception("The certification alias doesn't contain a key entry!");
            }
            caChain = caKS.getCertificateChain(caKSAlias);
            if (caChain == null) {
                throw new Exception("The certificate chain of the certification authority " + "alias cannot be null!");
            }
            trustedCert = (X509Certificate) (caChain[caChain.length - 1]);
            caKey = (PrivateKey) caKS.getKey(caKSAlias, caKSPass);
            if (!caKey.getAlgorithm().equals(caType)) {
                throw new Exception("The private key of the CA certificate is not of type " + caType + ". It is of type " + caKey.getAlgorithm() + ". Please choose as type " + caKey.getAlgorithm() + " and choose a suitable signature!");
            }
            newKS.setCertificateEntry(caKSAlias, trustedCert);
        } catch (Exception e) {
            throw new KeyStoreException("There occurred the following exception while trying " + "to extract the trusted certificate with alias: \"" + caKSAlias + "\" certification keystore: \"" + caKSLocation + "\" and import it into the keystore \"" + ksLocation + "\":\n" + e.getClass().getName() + ": " + e.getMessage());
        }
        it = keys.iterator();
        while (it.hasNext()) {
            key = (KeyDefinition) it.next();
            try {
                pair = generateKeyPair(key.getType(), key.getKeysize(), provider);
                cert = generateCertificate(key.getDNameObject(), key.getDNameObject(), key.getValidity(), caSigAlg, pair.getPublic());
                chain = new Certificate[1];
                chain[0] = cert;
                alias = key.getAlias();
            } catch (Exception e) {
                throw new KeyStoreException("There occurred the following problem while trying to " + "generate the self-signed certificate of the entry with " + "parameters:\n Alias: " + key.getAlias() + "\nKey ID: " + key.getName() + ":\n" + e.getClass().getName() + ": " + e.getMessage());
            }
            signer = new SignKey();
            try {
                signedCert = signer.sign(trustedCert, caKey, cert, caSigAlg, key.getUsageArray(), null);
            } catch (Exception e) {
                throw new KeyStoreException("There occurred the following problem while trying to " + "sign the certificate of the entry with parameters: " + "\n Alias: " + key.getAlias() + "\nKey ID: " + key.getName() + ":\n" + e.getClass().getName() + ": " + e.getMessage());
            }
            chain = new Certificate[2];
            chain[0] = signedCert;
            chain[1] = trustedCert;
            try {
                newKS.setKeyEntry(alias, pair.getPrivate(), ksPass, chain);
            } catch (Exception e) {
                throw new KeyStoreException("There occurred the following problem while trying to " + "import the entry with parameters: " + "\n Alias: " + key.getAlias() + "\nKey ID: " + key.getName() + ":\n" + e.getClass().getName() + ": " + e.getMessage());
            }
        }
        try {
            newKSFile = new File(ksLocation);
            newKSFile.createNewFile();
            fos = new FileOutputStream(newKSFile);
            newKS.store(fos, ksPass);
            fos.close();
        } catch (Exception e) {
            throw new KeyStoreException("There occurred the following problem while trying to " + "save the generated keystore in the file: " + ksLocation + ":\n" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static KeyPair generateKeyPair(String keyalg, int keysize, Provider provider) throws NoSuchAlgorithmException {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance(keyalg, provider);
            generator.initialize(keysize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException nsae) {
            throw new NoSuchAlgorithmException("The algorithm " + keyalg + " is not available");
        }
    }

    private static X509Certificate generateCertificate(Principal subjectDN, Principal issuerDN, int validity, String sigalg, PublicKey pubkey) throws InvalidKeyException, NoSuchAlgorithmException {
        codec.x509.X509Certificate cert;
        Calendar cal;
        Date d;
        cert = new codec.x509.X509Certificate();
        cert.setIssuerDN(subjectDN);
        cert.setSubjectDN(issuerDN);
        cal = Calendar.getInstance();
        d = cal.getTime();
        cert.setNotBefore(d);
        cal.add(Calendar.DAY_OF_YEAR, validity);
        d = cal.getTime();
        cert.setNotAfter(d);
        try {
            cert.setSubjectPublicKey(pubkey);
            cert.setSignatureAlgorithm(new AlgorithmIdentifier(sigalg));
        } catch (InvalidKeyException ike) {
            throw new InvalidKeyException("The provided public key for this certificate is invalid!");
        } catch (NoSuchAlgorithmException nsae) {
            throw nsae;
        }
        return cert;
    }

    public static void testKey(String keyalg, int keysize, Principal subjectDN, int validity, String sigalg, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        KeyPair pair;
        pair = generateKeyPair(keyalg, keysize, provider);
        generateCertificate(subjectDN, subjectDN, validity, sigalg, pair.getPublic());
    }

    public static void saveJKS(String ksLocation, char[] ksPass, Provider provider, HashMap<String, KeystoreEntryDefinition> content) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeystoreEntryDefinition entry;
        FileOutputStream fos;
        Iterator<String> it;
        KeyStore.Entry entr;
        KeyStore ks = null;
        try {
            if (provider != null) {
                ks = KeyStore.getInstance("JKS", provider);
            } else {
                ks = KeyStore.getInstance("JKS");
            }
        } catch (KeyStoreException kse) {
            if (provider == null) {
                throw new KeyStoreException("The requested keystore type is not available from the " + "default provider!");
            } else {
                throw new KeyStoreException("The requested keystore type is not available from the " + "chosen provider!");
            }
        } catch (IllegalArgumentException iae) {
            if (provider == null) {
                throw new IllegalArgumentException("The default provider is empty! Choose another one!");
            } else {
                throw new KeyStoreException("The chosen provider is empty! Choose another one!");
            }
        }
        ks.load(null, ksPass);
        it = content.keySet().iterator();
        while (it.hasNext()) {
            entry = content.get(it.next());
            entr = entry.getEntry();
            if (entr == null || entry.needsCorrectKeyParameters()) {
                throw new IllegalArgumentException("There are keystores entries with unresolved " + "protection parameters. Please give the right " + "password for all entries and try again!");
            }
            if (entry.getAlias() == null) {
                throw new IllegalArgumentException("There are keystores entries with uninitialized " + "aliases. Resolve this problem and try agian!");
            }
            ks.setEntry(entry.getNewAlias(), entr, entry.getProtectionParams());
            entry.setKeyStoreLocation(ksLocation);
            entry.setKeystorePass(ksPass);
            entry.setProvider(provider);
        }
        try {
            fos = new FileOutputStream(ksLocation);
            ks.store(fos, ksPass);
            fos.close();
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("The path to the jks file is not valid!");
        } catch (KeyStoreException kse) {
            throw new KeyStoreException("The specified keystore is not opened!");
        } catch (IOException ioe) {
            throw new FileNotFoundException("There are I/O problems with the specifed location! " + "Please choose another location!");
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static void changeJKSPassword(String ksLocation, char[] newPass, Provider provider, HashMap<String, KeystoreEntryDefinition> content) throws FileNotFoundException, SecurityException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, UnrecoverableEntryException {
        saveJKS(ksLocation, newPass, provider, content);
    }

    public static void isCAEntry(String caKSLocation, char[] caKSPass, String caKSAlias, String caKeyType, String caKeySigAlg) throws KeyStoreException, IllegalArgumentException {
        PrivateKey key = null;
        Certificate[] chain;
        KeyStore ks;
        try {
            ks = openJKS(caKSLocation, caKSPass);
        } catch (Exception e) {
            throw new KeyStoreException("The specified keystore :\n" + caKSLocation + "\n cannot " + "be opened. An exception with the following message " + "occurred:\n" + e.getMessage());
        }
        if (!ks.containsAlias(caKSAlias)) {
            throw new KeyStoreException("The specified keystore :\n" + caKSLocation + "\n doesn't " + "contain the specified alias: " + caKSAlias);
        }
        if (!ks.isKeyEntry(caKSAlias)) {
            throw new KeyStoreException("The specified keystore :\n" + caKSLocation + "\n doesn't " + "contain the specified alias: " + caKSAlias);
        }
        chain = ks.getCertificateChain(caKSAlias);
        if (chain == null) {
            throw new KeyStoreException("The certificate chain of the certification authority " + "alias cannot be null!");
        }
        try {
            key = (PrivateKey) ks.getKey(caKSAlias, caKSPass);
        } catch (UnrecoverableKeyException uee) {
            throw new KeyStoreException("The key password of alias " + caKSAlias + " is false!");
        } catch (Exception e) {
            throw new KeyStoreException("There occurred the following exception while trying to " + "retrieve the private key of the ca entry with alias " + caKSAlias + ":\n" + e.getClass() + ": " + e.getMessage());
        }
        if (caKeySigAlg == null || caKeySigAlg.equals("")) {
            throw new KeyStoreException("The signing algorithm of the CA cannot be empty!");
        }
        if (caKeyType != null && !caKeyType.equals("")) {
            if (!key.getAlgorithm().equals(caKeyType)) {
                throw new KeyStoreException("The algorithm private key of the CA alias is " + key.getAlgorithm() + ". It is different than " + "the specified key type. Please choose the type " + key.getAlgorithm() + " in the field \"CA Private " + "Key Type\" and an appropriate signature " + "algorithm.");
            }
        }
        if (!caKeySigAlg.contains(key.getAlgorithm())) {
            throw new IllegalArgumentException("You haven't specified the CA Type Field. The signing " + "algorithm " + caKeySigAlg + " is not a suitable signature " + "for the CA.");
        }
    }
}
