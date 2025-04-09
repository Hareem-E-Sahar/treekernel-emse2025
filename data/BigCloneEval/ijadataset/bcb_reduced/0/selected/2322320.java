package org.jazzteam.ds;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import javax.crypto.KeyGenerator;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.ws.BindingType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class DigitalSignatureTest {

    private static String signAlg = "SHAwithDSA";

    private static String fileName = "INSERT_PATH_HERE";

    private byte[] sign;

    private boolean signVerify;

    @Test
    public void DSTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        sign = SignUtils.sign(fileName, privateKey, signAlg);
        signVerify = SignUtils.verify(fileName, publicKey, signAlg, sign);
        Assert.assertTrue(signVerify);
    }
}
