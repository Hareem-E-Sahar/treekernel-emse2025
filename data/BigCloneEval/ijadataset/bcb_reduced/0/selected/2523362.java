package net.sf.opengroove.sandbox.ssl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * A test class that will load the certificate and private key stored in
 * selfsigned.jks and create a new certificate signed by the private key in
 * selfsigned.jks, which it will then store in example.jks. The certificate will
 * be issued to example.com.
 * 
 * @author Alexander Boyd
 * 
 */
public class TestSSL003 {

    public static final char[] pass = "pass".toCharArray();

    /**
     * @param args
     */
    public static void main(String[] args) throws Throwable {
        KeyStore signstore = KeyStore.getInstance("JKS");
        signstore.load(new FileInputStream("selfsigned.jks"), pass);
        KeyStore certstore = KeyStore.getInstance("JKS");
        certstore.load(null, pass);
        PrivateKey signkey = (PrivateKey) signstore.getKey("key", pass);
        X509Certificate signcert = (X509Certificate) signstore.getCertificate("key");
        X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
        gen.setIssuerDN(signcert.getSubjectX500Principal());
        gen.setNotBefore(new Date());
        gen.setNotAfter(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(180, TimeUnit.DAYS)));
        gen.setSerialNumber(new BigInteger("" + System.currentTimeMillis()));
        gen.setSignatureAlgorithm("SHA512withRSA");
        gen.setSubjectDN(new X509Name("CN=example.com"));
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(3072);
        System.out.println("generating keys...");
        KeyPair keys = keygen.generateKeyPair();
        System.out.println("keys generated.");
        RSAPublicKey pub = (RSAPublicKey) keys.getPublic();
        RSAPrivateKey prv = (RSAPrivateKey) keys.getPrivate();
        gen.setPublicKey(pub);
        X509Certificate cert = gen.generate(signkey);
        certstore.setKeyEntry("key", prv, pass, new Certificate[] { cert, signcert });
        certstore.store(new FileOutputStream("example.jks"), pass);
    }
}
