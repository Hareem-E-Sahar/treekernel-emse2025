package org.tranche.users;

import org.tranche.security.SecurityUtil;
import org.tranche.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.tranche.time.TimeUtil;

/**
 * <p>A helper class for packaging a user's certificate and private key and anything else in a standard ZIP file.</p>
 * @author Jayson Falkner - jfalkner@umich.edu
 * @author James "Augie" Hill - augman85@gmail.com
 */
public class UserZipFile extends User {

    public static final int VERSION_ONE = 1;

    public static final int VERSION_LATEST = VERSION_ONE;

    /**
     * <p>Name of the public certificate file.</p>
     */
    public static final String PUBLIC_CERTIFICATE_NAME = "public.certificate";

    /**
     * <p>Name of the private key file.</p>
     */
    public static final String PRIVATE_KEY_NAME = "private.key";

    private int version = VERSION_LATEST;

    private File file;

    private String passphrase = null, email = null;

    private boolean lazyLoaded = false;

    private PrivateKey privateKey = null;

    /**
     * @param user
     * @param key
     */
    public UserZipFile(User user, PrivateKey key) {
        lazyLoaded = true;
        this.privateKey = key;
        this.setCertificate(user.getCertificate());
        this.setFlags(user.getFlags());
    }

    /**
     * @param file
     */
    public UserZipFile(File file) {
        this.file = file;
    }

    /**
     * 
     */
    private synchronized void lazyLoad() {
        if (lazyLoaded) {
            return;
        }
        lazyLoaded = true;
        if (!file.exists()) {
            throw new RuntimeException("Can't find file " + file + " to read user information.");
        }
        try {
            lazyLoad(IOUtil.getBytes(file));
        } catch (IOException ex) {
            throw new RuntimeException("Can't read the user's file: " + file, ex);
        }
    }

    /**
     * 
     * @param is
     */
    private void lazyLoad(byte[] bytes) {
        if (passphrase == null) {
            throw new RuntimeException("Passphrase required.");
        }
        byte[] decryptedBytes = null;
        try {
            decryptedBytes = SecurityUtil.decryptInMemory(passphrase, bytes);
        } catch (Exception ex) {
            throw new RuntimeException("Incorrect passphrase for user zip file.", ex);
        }
        ByteArrayInputStream fis = null;
        BufferedInputStream bis = null;
        ZipInputStream zis = null;
        try {
            fis = new ByteArrayInputStream(decryptedBytes);
            bis = new BufferedInputStream(fis);
            zis = new ZipInputStream(bis);
            for (ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                if (ze.getName().equals(UserZipFile.PUBLIC_CERTIFICATE_NAME)) {
                    byte[] certBytes = IOUtil.getBytes(zis);
                    X509Certificate cert = SecurityUtil.getCertificate(certBytes);
                    setCertificate(cert);
                    continue;
                }
                if (ze.getName().equals(UserZipFile.PRIVATE_KEY_NAME)) {
                    byte[] keyBytes = IOUtil.getBytes(zis);
                    PrivateKey key = SecurityUtil.getPrivateKey(keyBytes);
                    setPrivateKey(key);
                    continue;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            IOUtil.safeClose(zis);
            IOUtil.safeClose(bis);
            IOUtil.safeClose(fis);
        }
    }

    /**
     * <p>Saves the user zip file to the given file.</p>
     * @param file
     */
    public void saveTo(File file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);
            {
                ZipEntry certEntry = new ZipEntry(UserZipFile.PUBLIC_CERTIFICATE_NAME);
                byte[] certBytes = getCertificate().getEncoded();
                certEntry.setSize(certBytes.length);
                zos.putNextEntry(certEntry);
                zos.write(certBytes);
            }
            {
                ZipEntry keyEntry = new ZipEntry(UserZipFile.PRIVATE_KEY_NAME);
                byte[] keyBytes = getPrivateKey().getEncoded();
                keyEntry.setSize(keyBytes.length);
                zos.putNextEntry(keyEntry);
                zos.write(keyBytes);
            }
            zos.finish();
            bos.flush();
            fos.flush();
            zos.close();
            bos.close();
            fos.close();
            if (getPassphrase() != null) {
                File encrypted = SecurityUtil.encryptDiskBacked(passphrase, file);
                IOUtil.renameFallbackCopy(encrypted, file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't save the user information to " + file.getPath() + ": " + e.getMessage(), e);
        } finally {
            IOUtil.safeClose(zos);
            IOUtil.safeClose(bos);
            IOUtil.safeClose(fos);
        }
    }

    /**
     * <p>Gets the file.</p>
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * <p>Gets the passphrase used to decrypt the encrypted file.</p>
     * @return
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * <p>Sets the passphrase used to decrypt the encrypted file.</p>
     * @param passphrase
     */
    public void setPassphrase(String passphrase) {
        lazyLoaded = false;
        this.passphrase = passphrase;
    }

    /**
     * 
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * <p>Gets the private key.</p>
     * @return
     */
    public PrivateKey getPrivateKey() {
        lazyLoad();
        return privateKey;
    }

    /**
     * <p>Sets the private key.</p>
     * @param privateKey
     */
    public void setPrivateKey(PrivateKey privateKey) {
        lazyLoaded = true;
        this.privateKey = privateKey;
    }

    /**
     * <p>Gets the flags.</p>
     * @return
     */
    @Override
    public int getFlags() {
        lazyLoad();
        return super.getFlags();
    }

    /**
     * <p>Gets the certificate.</p>
     * @return
     */
    @Override
    public X509Certificate getCertificate() {
        lazyLoad();
        return super.getCertificate();
    }

    /**
     * <p>Sets the certificate.</p>
     * @param certificate
     */
    @Override
    public void setCertificate(X509Certificate certificate) {
        lazyLoaded = true;
        super.setCertificate(certificate);
    }

    /**
     * <p>Return true only if certificate not yet valid.</p>
     * @return
     */
    public boolean isNotYetValid() {
        try {
            this.getCertificate().checkValidity(new Date(TimeUtil.getTrancheTimestamp()));
        } catch (CertificateNotYetValidException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * <p>Returns true only if certificate is expired.</p>
     * @return
     */
    public boolean isExpired() {
        try {
            this.getCertificate().checkValidity(new Date(TimeUtil.getTrancheTimestamp()));
        } catch (CertificateExpiredException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * <p>Returns true if certificate is valid (not expired or and is valid.)</p>
     * @return
     */
    public boolean isValid() {
        return !isExpired() && !isNotYetValid();
    }

    /**
     * <p>Gets the user name of the user from the certificate.</p>
     * @return User name, if found, or null.
     */
    public String getUserNameFromCert() {
        return UserCertificateUtil.readUserName(getCertificate());
    }

    /**
     * <p>Compares two UserZipFiles.</p>
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof UserZipFile) {
            UserZipFile uzf = (UserZipFile) o;
            return uzf.getCertificate().equals(getCertificate()) && uzf.getPrivateKey().equals(getPrivateKey());
        }
        return o.equals(this);
    }

    /**
     * 
     * @return
     */
    @Override
    public int hashCode() {
        return getCertificate().hashCode();
    }
}
