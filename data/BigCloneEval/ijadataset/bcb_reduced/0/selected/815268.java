package org.rt.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.io.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.util.encoders.Base64;
import org.rt.credential.*;

/** Capable of generating 1024 DSA key pairs. */
public class KeyUtil {

    /**
	* Encode bytes array to BASE64 string
	* @param bytes
	* @return Encoded string
	*/
    public static String encodeBase64(byte[] bytes) {
        byte[] result = Base64.encode(bytes);
        return new String(result);
    }

    public static String encodeBase64(PGPPublicKeyRing ring) throws IOException {
        byte[] result = Base64.encode(ring.getEncoded());
        return new String(result);
    }

    /**
	* Decode BASE64 encoded string to bytes array
	* @param text The string
	* @return Bytes array
	*/
    public static byte[] decodeBase64(String text) {
        return Base64.decode(text);
    }

    /** Generates a test key pair. */
    public static PGPPublicKeyRing generateKeyPair(String identity, char[] passPhrase) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator dsaKpg = KeyPairGenerator.getInstance("DSA", "BC");
        System.out.println("About to initialize");
        dsaKpg.initialize(1024);
        System.out.println("Done initializing");
        KeyPair dsaKp = dsaKpg.generateKeyPair();
        System.out.println("Done genning key");
        PGPKeyPair dsaKeyPair = new PGPKeyPair(PGPPublicKey.DSA, dsaKp, new Date());
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, dsaKeyPair, identity, PGPEncryptedData.CAST5, passPhrase, true, null, null, new SecureRandom(), "BC");
        keyRingGen.generateSecretKeyRing();
        PGPPublicKeyRing publicRing = keyRingGen.generatePublicKeyRing();
        return publicRing;
    }

    /** Generates keys for BTG in the CredentialAuthor/keys directory. */
    public static PGPPublicKeyRing generateKeyPairPlus(String identity, char[] passPhrase) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator dsaKpg = KeyPairGenerator.getInstance("DSA", "BC");
        dsaKpg.initialize(1024);
        KeyPair dsaKp = dsaKpg.generateKeyPair();
        String path = "./keys/";
        String keyNames = identity;
        FileOutputStream secretOut = new FileOutputStream(path + keyNames + ".pri");
        FileOutputStream publicOut = new FileOutputStream(path + keyNames + ".pub");
        PGPKeyPair dsaKeyPair = new PGPKeyPair(PGPPublicKey.DSA, dsaKp, new Date());
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, dsaKeyPair, identity, PGPEncryptedData.CAST5, passPhrase, true, null, null, new SecureRandom(), "BC");
        PGPSecretKeyRing privateRing = keyRingGen.generateSecretKeyRing();
        privateRing.encode(secretOut);
        secretOut.close();
        PGPPublicKeyRing publicRing = keyRingGen.generatePublicKeyRing();
        publicRing.encode(publicOut);
        publicOut.close();
        return publicRing;
    }

    public static Principal makePrincipal(String base64Key) {
        byte bytes[] = decodeBase64(base64Key);
        Principal newPrincipal = null;
        try {
            newPrincipal = new Principal(new PGPPublicKeyRing(bytes));
            newPrincipal.setName("p");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newPrincipal;
    }
}
