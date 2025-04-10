package de.fzj.pkikits.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class KeyStoreCreate extends TestCase {

    protected void setUp() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void testBCProv() throws Exception, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        KeyPair kp;
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        ks.load(null, null);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        kp = keyGen.generateKeyPair();
        v3CertGen.reset();
        v3CertGen.setSerialNumber(BigInteger.valueOf(0));
        v3CertGen.setIssuerDN(new X509Name("C=DE"));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 999999L));
        v3CertGen.setSubjectDN(new X509Name("C=DE"));
        v3CertGen.setPublicKey(kp.getPublic());
        v3CertGen.setSignatureAlgorithm("sha1withrsa");
        X509Certificate cert = v3CertGen.generateX509Certificate((PrivateKey) kp.getPrivate());
        System.out.println(cert);
        System.out.println("storetype =  " + "PKCS12");
        System.out.println("provider =  " + "BC");
        System.out.println("ksp =  " + "asdfs");
        Certificate[] chain = new Certificate[] { cert };
        PrivateKey pk = kp.getPrivate();
        System.out.println("pk " + pk.getAlgorithm());
        System.out.println("pk " + pk.getFormat());
        System.out.println("pk " + pk.toString());
        String password = "foobarasdf";
        ks.setKeyEntry("0", pk, "test".toCharArray(), chain);
        assertEquals(pk.getAlgorithm(), "RSA");
    }
}
