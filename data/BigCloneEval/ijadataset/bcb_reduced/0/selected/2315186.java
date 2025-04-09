package to_do_o.core.pki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKKeyPairGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import to_do_o.core.config.SWTConstants;

/**
 * This class is used e.g. for creating new certificates.
 * 
 * @author Ruediger Gad
 * 
 */
public class CA {

    private static CA instance;

    public static synchronized CA getInstance() {
        if (instance == null) {
            instance = new CA();
        }
        return instance;
    }

    public static String getSystemID() throws UnknownHostException {
        InetAddress localAddr = InetAddress.getLocalHost();
        String systemId = "To-Do-O: " + System.getProperty("user.name") + "@" + localAddr.getHostName() + "_" + System.getProperty("os.name");
        return systemId;
    }

    public static boolean isCreated() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
        File keystoreFile = new File(SWTConstants.KEYSTORE_FILE);
        File truststoreFile = new File(SWTConstants.TRUSTSTORE_FILE);
        if (keystoreFile.exists() && truststoreFile.exists()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystoreFile), "quaerite et invenietis".toCharArray());
            trustStore.load(new FileInputStream(truststoreFile), "quaerite et invenietis".toCharArray());
            String systemId = getSystemID();
            return keyStore.containsAlias(systemId) && trustStore.containsAlias(systemId);
        }
        return false;
    }

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private X509Certificate cert;

    private KeyStore keyStore;

    private KeyStore trustStore;

    private CA() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            if (isCreated()) {
                loadCA();
            } else {
                createCA();
            }
            System.setProperty("javax.net.ssl.keyStore", SWTConstants.KEYSTORE_FILE);
            System.setProperty("javax.net.ssl.keyStorePassword", "quaerite et invenietis");
            System.setProperty("javax.net.ssl.trustStore", SWTConstants.TRUSTSTORE_FILE);
            System.setProperty("javax.net.ssl.trustStorePassword", "quaerite et invenietis");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCA() throws InvalidKeyException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, KeyStoreException, CertificateException, IOException {
        File toDoODir = new File(SWTConstants.TODO_O_HOME);
        if (!toDoODir.exists()) {
            toDoODir.mkdirs();
        } else if (!toDoODir.isDirectory()) {
            toDoODir.delete();
            toDoODir.mkdirs();
        }
        keyStore = KeyStore.getInstance("JKS");
        if (new File(SWTConstants.KEYSTORE_FILE).exists()) {
            keyStore.load(new FileInputStream(SWTConstants.KEYSTORE_FILE), "quaerite et invenietis".toCharArray());
        } else {
            keyStore.load(null);
        }
        trustStore = KeyStore.getInstance("JKS");
        if (new File(SWTConstants.TRUSTSTORE_FILE).exists()) {
            trustStore.load(new FileInputStream(SWTConstants.TRUSTSTORE_FILE), "quaerite et invenietis".toCharArray());
        } else {
            trustStore.load(null);
        }
        String systemId = getSystemID();
        if (!keyStore.containsAlias(systemId) || !trustStore.containsAlias(systemId)) {
            JDKKeyPairGenerator.RSA keyGen = new JDKKeyPairGenerator.RSA();
            keyGen.initialize(SWTConstants.KEY_SIZE);
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
            X500Principal dName = new X500Principal("CN=" + systemId);
            Calendar cal = Calendar.getInstance();
            cal.set(2010, 1, 1);
            Date startDate = cal.getTime();
            cal.set(2020, 12, 31);
            Date endDate = cal.getTime();
            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
            certGen.setSerialNumber(new BigInteger("1"));
            certGen.setIssuerDN(dName);
            certGen.setSubjectDN(dName);
            certGen.setNotBefore(startDate);
            certGen.setNotAfter(endDate);
            certGen.setPublicKey(publicKey);
            certGen.setSignatureAlgorithm("SHA1withRSA");
            cert = certGen.generate(privateKey, "BC");
            keyStore.setKeyEntry(systemId, privateKey, "quaerite et invenietis".toCharArray(), new Certificate[] { cert });
            keyStore.store(new FileOutputStream(SWTConstants.KEYSTORE_FILE), "quaerite et invenietis".toCharArray());
            trustStore.setCertificateEntry(systemId, cert);
            trustStore.store(new FileOutputStream(SWTConstants.TRUSTSTORE_FILE), "quaerite et invenietis".toCharArray());
        }
    }

    public X509Certificate getCertificate() {
        return cert;
    }

    private void loadCA() throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, UnrecoverableKeyException {
        keyStore = KeyStore.getInstance("JKS");
        trustStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(SWTConstants.KEYSTORE_FILE), "quaerite et invenietis".toCharArray());
        trustStore.load(new FileInputStream(SWTConstants.TRUSTSTORE_FILE), "quaerite et invenietis".toCharArray());
        String systemId = getSystemID();
        privateKey = (PrivateKey) keyStore.getKey(systemId, "quaerite et invenietis".toCharArray());
        cert = (X509Certificate) trustStore.getCertificate(systemId);
        publicKey = cert.getPublicKey();
    }
}
