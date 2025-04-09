package net.sf.opengroove.sandbox.ssl;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * Generates a self-signed certificate, and places it in a keystore located at
 * selfsigned.jks
 * 
 * @author Alexander Boyd
 * 
 */
public class TestSSL002 {

    /**
     * @param args
     */
    public static void main(String[] args) throws Throwable {
        X509V3CertificateGenerator gen = new X509V3CertificateGenerator();
        gen.setIssuerDN(new X509Name("CN=OpenGroove Root CA"));
        gen.setNotBefore(new Date());
        gen.setNotAfter(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(20 * 365, TimeUnit.DAYS)));
        gen.setSerialNumber(new BigInteger("" + System.currentTimeMillis()));
        gen.setSignatureAlgorithm("SHA512withRSA");
        gen.setSubjectDN(new X509Name("CN=OpenGroove Root CA"));
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(3072);
        System.out.println("generating keys...");
        KeyPair keys = keygen.generateKeyPair();
        System.out.println("keys generated.");
        RSAPublicKey pub = (RSAPublicKey) keys.getPublic();
        RSAPrivateKey prv = (RSAPrivateKey) keys.getPrivate();
        gen.setPublicKey(pub);
        X509Certificate cert = gen.generate(prv);
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, "pass".toCharArray());
        keystore.setKeyEntry("key", prv, "pass".toCharArray(), new Certificate[] { cert });
        keystore.store(new FileOutputStream("C:\\opengroove-ca.jks"), "pass".toCharArray());
    }
}
