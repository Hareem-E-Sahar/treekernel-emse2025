package dk.tiede.oces;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 *
 * @author Kim Tiedemann
 */
public class OcesEncrypter extends OcesCiphering {

    private File x509PublicCertificateFile;

    private File pkcs12File;

    private String pkcs12Password;

    private PublicKey publicKey;

    /** Creates a new instance of OcesEncrypter */
    private OcesEncrypter(File pkcs12File, String pkcs12password) {
        this.pkcs12File = pkcs12File;
        this.pkcs12Password = pkcs12password;
    }

    private OcesEncrypter(File x509PublicCertificateFile) {
        this.x509PublicCertificateFile = x509PublicCertificateFile;
    }

    public static OcesEncrypter newInstance(File pkcs12File, String pkcs12password) throws OcesException {
        OcesEncrypter encrypter = new OcesEncrypter(pkcs12File, pkcs12password);
        encrypter.initialise();
        return encrypter;
    }

    public static OcesEncrypter newInstance(File x509PublicCertificateFile) throws OcesException {
        OcesEncrypter encrypter = new OcesEncrypter(x509PublicCertificateFile);
        encrypter.initialise();
        return encrypter;
    }

    private void initialise() throws OcesException {
        if (pkcs12File != null && pkcs12Password != null) {
            X509Certificate certificate;
            try {
                certificate = PKCS12Util.getCertificateFromPKCS12File(pkcs12File, pkcs12Password);
            } catch (Exception ex) {
                throw new OcesException();
            }
            publicKey = certificate.getPublicKey();
        } else if (x509PublicCertificateFile != null) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(new FileInputStream(x509PublicCertificateFile));
                publicKey = certificate.getPublicKey();
            } catch (FileNotFoundException ex) {
                throw new OcesException();
            } catch (CertificateException ex) {
                throw new OcesException();
            }
        }
    }

    public void encryptFile(File fileToBeEncrypted, File encryptedFile) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom random = new SecureRandom();
            keyGenerator.init(128, random);
            SecretKey secretKey = keyGenerator.generateKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.WRAP_MODE, publicKey);
            byte[] wrappedKey = cipher.wrap(secretKey);
            DataOutputStream out = new DataOutputStream(new FileOutputStream(encryptedFile));
            out.writeInt(wrappedKey.length);
            out.write(wrappedKey);
            InputStream in = new FileInputStream(fileToBeEncrypted);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            crypt(in, out, cipher);
            in.close();
            out.close();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NoSuchPaddingException nspe) {
            nspe.printStackTrace();
        } catch (InvalidKeyException ike) {
            ike.printStackTrace();
        } catch (IllegalBlockSizeException ibse) {
            ibse.printStackTrace();
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        }
    }
}
