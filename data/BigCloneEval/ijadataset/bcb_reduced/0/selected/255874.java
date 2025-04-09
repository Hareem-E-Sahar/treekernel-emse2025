package com.limegroup.gnutella.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import com.bitzi.util.Base32;
import com.limegroup.gnutella.settings.SecuritySettings;

public class KeyManager {

    public static void main(String[] args) throws Throwable {
        if (args[0].equals("generate")) generate(args); else if (args[0].equals("version")) createVersionXML(args); else if (args[0].equals("simpp")) ;
    }

    public static void generate(String args[]) throws Throwable {
        if (args.length < 1) throw new IllegalArgumentException("missing second argument specifying output name");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        generator.initialize(1024);
        KeyPair pair = generator.generateKeyPair();
        File privateKey = new File("pri" + args[1] + ".key");
        FileOutputStream fos1 = new FileOutputStream(privateKey);
        PKCS8EncodedKeySpec spec1 = new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded());
        fos1.write(Base32.encode(spec1.getEncoded()).getBytes());
        fos1.close();
        File publicKey = new File("pub" + args[1] + ".key");
        FileOutputStream fos2 = new FileOutputStream(publicKey);
        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(pair.getPublic().getEncoded());
        fos2.write(Base32.encode(spec2.getEncoded()).getBytes());
        fos2.close();
    }

    public static void createVersionXML(String[] args) throws Throwable {
        File privateKeyFile = new File(args[1]);
        File versionFile = new File(args[2]);
        PrivateKey privateKey = SecuritySettings.getPrivateKey(privateKeyFile);
        RandomAccessFile raf = new RandomAccessFile(versionFile, "r");
        byte[] versionBytes = new byte[(int) raf.length()];
        raf.readFully(versionBytes);
        Signature sig = Signature.getInstance("SHA1" + "with" + privateKey.getAlgorithm());
        sig.initSign(privateKey);
        sig.update(versionBytes);
        byte[] sigBytes = sig.sign();
        String sigString = Base32.encode(sigBytes);
        File outFile = new File("version.xml");
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(sigString.getBytes("UTF-8"));
        fos.write("||".getBytes("UTF-8"));
        fos.write(versionBytes);
        fos.close();
    }
}
