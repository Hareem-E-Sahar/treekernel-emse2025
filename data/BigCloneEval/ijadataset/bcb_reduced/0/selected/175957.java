package com.cubusmail.server.mail.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encrypt und decrypt and passwords for mailboxes.
 * 
 * @author Juergen Schlierf
 */
public class MailPasswordEncryptor implements IMailPasswordEncryptor {

    private final Log log = LogFactory.getLog(getClass());

    private String algorithm;

    private KeyPair keyPair;

    /**
	 * 
	 */
    public void init() {
        try {
            this.keyPair = KeyPairGenerator.getInstance(this.algorithm).generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] encryptPassowrd(String password) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(this.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, this.keyPair.getPublic());
            ByteArrayOutputStream baosEncryptedData = new ByteArrayOutputStream();
            CipherOutputStream cos = new CipherOutputStream(baosEncryptedData, cipher);
            cos.write(password.getBytes("UTF-8"));
            cos.flush();
            cos.close();
            return baosEncryptedData.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String decryptPassword(byte[] encryptedPassword) {
        Cipher cipher;
        try {
            log.debug("decrypt...");
            cipher = Cipher.getInstance(this.algorithm);
            cipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
            CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(encryptedPassword), cipher);
            ByteArrayOutputStream baosDecryptedData = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len = 0;
            while ((len = cis.read(buffer)) > 0) {
                baosDecryptedData.write(buffer, 0, len);
            }
            baosDecryptedData.flush();
            cis.close();
            log.debug("...finish");
            return new String(baosDecryptedData.toByteArray());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
	 * @param keyPair
	 *            The keyPair to set.
	 */
    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
	 * @param algorithm
	 *            The algorithm to set.
	 */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
