package sun.security.provider;

import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.KeyStoreSpi;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import sun.security.pkcs.EncryptedPrivateKeyInfo;

/**
 * This class provides the keystore implementation referred to as "JKS".
 *
 * @author Jan Luehe
 * @author David Brownell
 *
 * @version 1.50, 10/10/06
 *
 * @see KeyProtector
 * @see java.security.KeyStoreSpi
 * @see KeyTool
 *
 * @since JDK1.2
 */
public final class JavaKeyStore extends KeyStoreSpi {

    public static final int MAGIC = 0xfeedfeed;

    public static final int VERSION_1 = 0x01;

    public static final int VERSION_2 = 0x02;

    class KeyEntry {

        Date date;

        byte[] protectedPrivKey;

        Certificate chain[];
    }

    ;

    class TrustedCertEntry {

        Date date;

        Certificate cert;
    }

    ;

    /**
     * Private keys and certificates are stored in a hashtable.
     * Hash entries are keyed by alias names.
     */
    private Hashtable entries = new Hashtable();

    /**
     * Returns the key associated with the given alias, using the given
     * password to recover it.
     *
     * @param alias the alias name
     * @param password the password for recovering the key
     *
     * @return the requested key, or null if the given alias does not exist
     * or does not identify a <i>key entry</i>.
     *
     * @exception NoSuchAlgorithmException if the algorithm for recovering the
     * key cannot be found
     * @exception UnrecoverableKeyException if the key cannot be recovered
     * (e.g., the given password is wrong).
     */
    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        Object entry = entries.get(alias.toLowerCase());
        if (entry == null || !(entry instanceof KeyEntry)) {
            return null;
        }
        KeyProtector keyProtector = new KeyProtector(password);
        byte[] encrBytes = ((KeyEntry) entry).protectedPrivKey;
        EncryptedPrivateKeyInfo encrInfo;
        byte[] plain;
        try {
            encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
        } catch (IOException ioe) {
            throw new UnrecoverableKeyException("Private key not stored as " + "PKCS #8 " + "EncryptedPrivateKeyInfo");
        }
        return keyProtector.recover(encrInfo);
    }

    /**
     * Returns the certificate chain associated with the given alias.
     *
     * @param alias the alias name
     *
     * @return the certificate chain (ordered with the user's certificate first
     * and the root certificate authority last), or null if the given alias
     * does not exist or does not contain a certificate chain (i.e., the given 
     * alias identifies either a <i>trusted certificate entry</i> or a
     * <i>key entry</i> without a certificate chain).
     */
    public Certificate[] engineGetCertificateChain(String alias) {
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null && entry instanceof KeyEntry) {
            if (((KeyEntry) entry).chain == null) {
                return null;
            } else {
                return (Certificate[]) ((KeyEntry) entry).chain.clone();
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the certificate associated with the given alias.
     *
     * <p>If the given alias name identifies a
     * <i>trusted certificate entry</i>, the certificate associated with that
     * entry is returned. If the given alias name identifies a
     * <i>key entry</i>, the first element of the certificate chain of that
     * entry is returned, or null if that entry does not have a certificate
     * chain.
     *
     * @param alias the alias name
     *
     * @return the certificate, or null if the given alias does not exist or
     * does not contain a certificate.
     */
    public Certificate engineGetCertificate(String alias) {
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return ((TrustedCertEntry) entry).cert;
            } else {
                if (((KeyEntry) entry).chain == null) {
                    return null;
                } else {
                    return ((KeyEntry) entry).chain[0];
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the creation date of the entry identified by the given alias.
     *
     * @param alias the alias name
     *
     * @return the creation date of this entry, or null if the given alias does
     * not exist
     */
    public Date engineGetCreationDate(String alias) {
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return new Date(((TrustedCertEntry) entry).date.getTime());
            } else {
                return new Date(((KeyEntry) entry).date.getTime());
            }
        } else {
            return null;
        }
    }

    /**
     * Assigns the given key to the given alias, protecting it with the given
     * password.
     *
     * <p>If the given key is of type <code>java.security.PrivateKey</code>,
     * it must be accompanied by a certificate chain certifying the
     * corresponding public key.
     *
     * <p>If the given alias already exists, the keystore information
     * associated with it is overridden by the given key (and possibly
     * certificate chain).
     *
     * @param alias the alias name
     * @param key the key to be associated with the alias
     * @param password the password to protect the key
     * @param chain the certificate chain for the corresponding public
     * key (only required if the given key is of type
     * <code>java.security.PrivateKey</code>).
     *
     * @exception KeyStoreException if the given key cannot be protected, or
     * this operation fails for some other reason
     */
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        KeyProtector keyProtector = null;
        try {
            synchronized (entries) {
                KeyEntry entry = new KeyEntry();
                entry.date = new Date();
                keyProtector = new KeyProtector(password);
                entry.protectedPrivKey = keyProtector.protect(key);
                if ((chain != null) && (chain.length != 0)) {
                    entry.chain = (Certificate[]) chain.clone();
                } else {
                    entry.chain = null;
                }
                entries.put(alias.toLowerCase(), entry);
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException("Key protection algorithm not found");
        } finally {
            keyProtector = null;
        }
    }

    /**
     * Assigns the given key (that has already been protected) to the given
     * alias.
     * 
     * <p>If the protected key is of type
     * <code>java.security.PrivateKey</code>, it must be accompanied by a
     * certificate chain certifying the corresponding public key. If the
     * underlying keystore implementation is of type <code>jks</code>,
     * <code>key</code> must be encoded as an
     * <code>EncryptedPrivateKeyInfo</code> as defined in the PKCS #8 standard.
     *
     * <p>If the given alias already exists, the keystore information
     * associated with it is overridden by the given key (and possibly
     * certificate chain).
     *
     * @param alias the alias name
     * @param key the key (in protected format) to be associated with the alias
     * @param chain the certificate chain for the corresponding public
     * key (only useful if the protected key is of type
     * <code>java.security.PrivateKey</code>).
     *
     * @exception KeyStoreException if this operation fails.
     */
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        synchronized (entries) {
            try {
                new EncryptedPrivateKeyInfo(key);
            } catch (IOException ioe) {
                throw new KeyStoreException("key is not encoded as " + "EncryptedPrivateKeyInfo");
            }
            KeyEntry entry = new KeyEntry();
            entry.date = new Date();
            entry.protectedPrivKey = (byte[]) key.clone();
            if ((chain != null) && (chain.length != 0)) {
                entry.chain = (Certificate[]) chain.clone();
            } else {
                entry.chain = null;
            }
            entries.put(alias.toLowerCase(), entry);
        }
    }

    /**
     * Assigns the given certificate to the given alias.
     *
     * <p>If the given alias already exists in this keystore and identifies a
     * <i>trusted certificate entry</i>, the certificate associated with it is
     * overridden by the given certificate.
     *
     * @param alias the alias name
     * @param cert the certificate
     *
     * @exception KeyStoreException if the given alias already exists and does
     * not identify a <i>trusted certificate entry</i>, or this operation
     * fails for some other reason.
     */
    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        synchronized (entries) {
            Object entry = entries.get(alias.toLowerCase());
            if ((entry != null) && (entry instanceof KeyEntry)) {
                throw new KeyStoreException("Cannot overwrite own certificate");
            }
            TrustedCertEntry trustedCertEntry = new TrustedCertEntry();
            trustedCertEntry.cert = cert;
            trustedCertEntry.date = new Date();
            entries.put(alias.toLowerCase(), trustedCertEntry);
        }
    }

    /**
     * Deletes the entry identified by the given alias from this keystore.
     *
     * @param alias the alias name
     *
     * @exception KeyStoreException if the entry cannot be removed.
     */
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        synchronized (entries) {
            entries.remove(alias.toLowerCase());
        }
    }

    /**
     * Lists all the alias names of this keystore.
     *
     * @return enumeration of the alias names
     */
    public Enumeration engineAliases() {
        return entries.keys();
    }

    /**
     * Checks if the given alias exists in this keystore.
     *
     * @param alias the alias name
     *
     * @return true if the alias exists, false otherwise
     */
    public boolean engineContainsAlias(String alias) {
        return entries.containsKey(alias.toLowerCase());
    }

    /**
     * Retrieves the number of entries in this keystore.
     *
     * @return the number of entries in this keystore
     */
    public int engineSize() {
        return entries.size();
    }

    /**
     * Returns true if the entry identified by the given alias is a
     * <i>key entry</i>, and false otherwise.
     *
     * @return true if the entry identified by the given alias is a
     * <i>key entry</i>, false otherwise.
     */
    public boolean engineIsKeyEntry(String alias) {
        Object entry = entries.get(alias.toLowerCase());
        if ((entry != null) && (entry instanceof KeyEntry)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the entry identified by the given alias is a
     * <i>trusted certificate entry</i>, and false otherwise.
     *
     * @return true if the entry identified by the given alias is a
     * <i>trusted certificate entry</i>, false otherwise.
     */
    public boolean engineIsCertificateEntry(String alias) {
        Object entry = entries.get(alias.toLowerCase());
        if ((entry != null) && (entry instanceof TrustedCertEntry)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the (alias) name of the first keystore entry whose certificate
     * matches the given certificate.
     *
     * <p>This method attempts to match the given certificate with each
     * keystore entry. If the entry being considered
     * is a <i>trusted certificate entry</i>, the given certificate is
     * compared to that entry's certificate. If the entry being considered is
     * a <i>key entry</i>, the given certificate is compared to the first
     * element of that entry's certificate chain (if a chain exists).
     *
     * @param cert the certificate to match with.
     *
     * @return the (alias) name of the first entry with matching certificate,
     * or null if no such entry exists in this keystore.
     */
    public String engineGetCertificateAlias(Certificate cert) {
        Certificate certElem;
        for (Enumeration e = entries.keys(); e.hasMoreElements(); ) {
            String alias = (String) e.nextElement();
            Object entry = entries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                certElem = ((TrustedCertEntry) entry).cert;
            } else if (((KeyEntry) entry).chain != null) {
                certElem = ((KeyEntry) entry).chain[0];
            } else {
                continue;
            }
            if (certElem.equals(cert)) {
                return alias;
            }
        }
        return null;
    }

    /**
     * Stores this keystore to the given output stream, and protects its
     * integrity with the given password.
     *
     * @param stream the output stream to which this keystore is written.
     * @param password the password to generate the keystore integrity check
     *
     * @exception IOException if there was an I/O problem with data
     * @exception NoSuchAlgorithmException if the appropriate data integrity
     * algorithm could not be found
     * @exception CertificateException if any of the certificates included in
     * the keystore data could not be stored
     */
    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        synchronized (entries) {
            if (password == null) {
                throw new IllegalArgumentException("password can't be null");
            }
            byte[] encoded;
            MessageDigest md = getPreKeyedHash(password);
            DataOutputStream dos = new DataOutputStream(new DigestOutputStream(stream, md));
            dos.writeInt(MAGIC);
            dos.writeInt(VERSION_2);
            dos.writeInt(entries.size());
            for (Enumeration e = entries.keys(); e.hasMoreElements(); ) {
                String alias = (String) e.nextElement();
                Object entry = entries.get(alias);
                if (entry instanceof KeyEntry) {
                    dos.writeInt(1);
                    dos.writeUTF(alias);
                    dos.writeLong(((KeyEntry) entry).date.getTime());
                    dos.writeInt(((KeyEntry) entry).protectedPrivKey.length);
                    dos.write(((KeyEntry) entry).protectedPrivKey);
                    int chainLen;
                    if (((KeyEntry) entry).chain == null) {
                        chainLen = 0;
                    } else {
                        chainLen = ((KeyEntry) entry).chain.length;
                    }
                    dos.writeInt(chainLen);
                    for (int i = 0; i < chainLen; i++) {
                        encoded = ((KeyEntry) entry).chain[i].getEncoded();
                        dos.writeUTF(((KeyEntry) entry).chain[i].getType());
                        dos.writeInt(encoded.length);
                        dos.write(encoded);
                    }
                } else {
                    dos.writeInt(2);
                    dos.writeUTF(alias);
                    dos.writeLong(((TrustedCertEntry) entry).date.getTime());
                    encoded = ((TrustedCertEntry) entry).cert.getEncoded();
                    dos.writeUTF(((TrustedCertEntry) entry).cert.getType());
                    dos.writeInt(encoded.length);
                    dos.write(encoded);
                }
            }
            byte digest[] = md.digest();
            dos.write(digest);
            dos.flush();
        }
    }

    /**
     * Loads the keystore from the given input stream.
     *
     * <p>If a password is given, it is used to check the integrity of the
     * keystore data. Otherwise, the integrity of the keystore is not checked.
     *
     * @param stream the input stream from which the keystore is loaded
     * @param password the (optional) password used to check the integrity of
     * the keystore.
     *
     * @exception IOException if there is an I/O or format problem with the
     * keystore data
     * @exception NoSuchAlgorithmException if the algorithm used to check
     * the integrity of the keystore cannot be found
     * @exception CertificateException if any of the certificates in the
     * keystore could not be loaded
     */
    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        synchronized (entries) {
            DataInputStream dis;
            MessageDigest md = null;
            CertificateFactory cf = null;
            Hashtable cfs = null;
            ByteArrayInputStream bais = null;
            byte[] encoded = null;
            if (stream == null) return;
            if (password != null) {
                md = getPreKeyedHash(password);
                dis = new DataInputStream(new DigestInputStream(stream, md));
            } else {
                dis = new DataInputStream(stream);
            }
            int xMagic = dis.readInt();
            int xVersion = dis.readInt();
            if (xMagic != MAGIC || (xVersion != VERSION_1 && xVersion != VERSION_2)) {
                throw new IOException("Invalid keystore format");
            }
            if (xVersion == VERSION_1) {
                cf = CertificateFactory.getInstance("X509");
            } else {
                cfs = new Hashtable(3);
            }
            entries.clear();
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                int tag;
                String alias;
                tag = dis.readInt();
                if (tag == 1) {
                    KeyEntry entry = new KeyEntry();
                    alias = dis.readUTF();
                    entry.date = new Date(dis.readLong());
                    try {
                        entry.protectedPrivKey = new byte[dis.readInt()];
                    } catch (OutOfMemoryError e) {
                        throw new IOException("Keysize too big");
                    }
                    dis.readFully(entry.protectedPrivKey);
                    int numOfCerts = dis.readInt();
                    try {
                        if (numOfCerts > 0) {
                            entry.chain = new Certificate[numOfCerts];
                        }
                    } catch (OutOfMemoryError e) {
                        throw new IOException("Too many certificates in chain");
                    }
                    for (int j = 0; j < numOfCerts; j++) {
                        if (xVersion == 2) {
                            String certType = dis.readUTF();
                            if (cfs.containsKey(certType)) {
                                cf = (CertificateFactory) cfs.get(certType);
                            } else {
                                cf = CertificateFactory.getInstance(certType);
                                cfs.put(certType, cf);
                            }
                        }
                        try {
                            encoded = new byte[dis.readInt()];
                        } catch (OutOfMemoryError e) {
                            throw new IOException("Certificate too big");
                        }
                        dis.readFully(encoded);
                        bais = new ByteArrayInputStream(encoded);
                        entry.chain[j] = cf.generateCertificate(bais);
                        bais.close();
                    }
                    entries.put(alias, entry);
                } else if (tag == 2) {
                    TrustedCertEntry entry = new TrustedCertEntry();
                    alias = dis.readUTF();
                    entry.date = new Date(dis.readLong());
                    if (xVersion == 2) {
                        String certType = dis.readUTF();
                        if (cfs.containsKey(certType)) {
                            cf = (CertificateFactory) cfs.get(certType);
                        } else {
                            cf = CertificateFactory.getInstance(certType);
                            cfs.put(certType, cf);
                        }
                    }
                    try {
                        encoded = new byte[dis.readInt()];
                    } catch (OutOfMemoryError e) {
                        throw new IOException("Certificate too big");
                    }
                    dis.readFully(encoded);
                    bais = new ByteArrayInputStream(encoded);
                    entry.cert = cf.generateCertificate(bais);
                    bais.close();
                    entries.put(alias, entry);
                } else {
                    throw new IOException("Unrecognized keystore entry");
                }
            }
            if (password != null) {
                byte computed[], actual[];
                computed = md.digest();
                actual = new byte[computed.length];
                dis.readFully(actual);
                for (int i = 0; i < computed.length; i++) {
                    if (computed[i] != actual[i]) {
                        throw new IOException("Keystore was tampered with, or " + "password was incorrect");
                    }
                }
            }
        }
    }

    /**
     * To guard against tampering with the keystore, we append a keyed
     * hash with a bit of whitener.
     */
    private MessageDigest getPreKeyedHash(char[] password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        int i, j;
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte[] passwdBytes = new byte[password.length * 2];
        for (i = 0, j = 0; i < password.length; i++) {
            passwdBytes[j++] = (byte) (password[i] >> 8);
            passwdBytes[j++] = (byte) password[i];
        }
        md.update(passwdBytes);
        for (i = 0; i < passwdBytes.length; i++) passwdBytes[i] = 0;
        md.update("Mighty Aphrodite".getBytes("UTF8"));
        return md;
    }
}
