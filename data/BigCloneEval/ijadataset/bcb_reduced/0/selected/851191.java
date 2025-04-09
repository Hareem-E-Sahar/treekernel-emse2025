package org.rt.author;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;

/**
 * Capable of generating 1024 DSA key pairs.
 */
public class KeyGenerator {

    /**
     * Generates a 1024 DSA key pair using the current date and supplied info.
     * @param identity the identity of the Principal for whom this key pair
     *      is being generated
     * @param passPhrase the passPhrase to protect use of the private key
     * @param keyPairNumber what number of key pair this is for the Profile,
     *      ie the first, second, etc
     * @throws java.lang.Exception if any of many possible problems arise
     */
    public static PGPPublicKeyRing generateKeyPair(String userName, String identity, char[] passPhrase, int keyPairNumber, File profileDir) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator dsaKpg = KeyPairGenerator.getInstance("DSA", "BC");
        dsaKpg.initialize(1024);
        KeyPair dsaKp = dsaKpg.generateKeyPair();
        String path = profileDir.getPath() + File.separator + userName + File.separator + "keys" + File.separator;
        String keyNames = "key" + keyPairNumber;
        FileOutputStream secretOut = new FileOutputStream(path + keyNames + ".pri");
        FileOutputStream publicOut = new FileOutputStream(path + keyNames + ".pub");
        PGPKeyPair dsaKeyPair = new PGPKeyPair(PGPPublicKey.DSA, dsaKp, new Date());
        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, dsaKeyPair, identity, PGPEncryptedData.CAST5, passPhrase, true, null, null, new SecureRandom(), "BC");
        keyRingGen.generateSecretKeyRing().encode(secretOut);
        secretOut.close();
        PGPPublicKeyRing publicRing = keyRingGen.generatePublicKeyRing();
        publicRing.encode(publicOut);
        publicOut.close();
        return publicRing;
    }
}
