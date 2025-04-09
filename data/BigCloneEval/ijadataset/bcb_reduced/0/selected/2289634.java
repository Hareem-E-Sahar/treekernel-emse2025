package org.ccnx.ccn.test.io.content;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.impl.security.crypto.jce.CCNCryptoProvider;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.content.WrappedKey;
import org.ccnx.ccn.io.content.WrappedKey.WrappedKeyObject;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.profiles.security.access.group.GroupAccessControlProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.PublisherID;
import org.ccnx.ccn.test.CCNTestHelper;
import org.ccnx.ccn.test.Flosser;
import org.ccnx.ccn.test.impl.encoding.XMLEncodableTester;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test both encoding/decoding of WrappedKey data structures and writing them
 * to CCN using WrappedKeyObjects. Move tests that require either unlimited-strength
 * crypto or algorithms that BouncyCastle does not support on all platforms/versions
 * to the expanded tests. See apps/examples/ExpandedCryptoTests.
 */
public class WrappedKeyTest {

    /**
	 * Handle naming for the test
	 */
    static CCNTestHelper testHelper = new CCNTestHelper(PublicKeyObjectTestRepo.class);

    public static boolean setupDone = false;

    public static KeyPair wrappingKeyPair = null;

    public static KeyPair wrappedKeyPair = null;

    public static KeyPair wrappedDHKeyPair = null;

    public static KeyPair wrappedDSAKeyPair = null;

    public static SecretKeySpec wrappingAESKey = null;

    public static SecretKeySpec wrappedAESKey = null;

    public static String aLabel = "FileEncryptionKeys";

    public static byte[] wrappingKeyID = null;

    public static ContentName wrappingKeyName = null;

    public static ContentName storedKeyName = null;

    public static byte[] dummyWrappedKey = new byte[64];

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new CCNCryptoProvider());
    }

    /**
	 * Do this in the first test. Were doing it in setupBeforeClass, but I think
	 * it was failing sometimes, possibly because it was too slow.
	 * @throws Exception
	 */
    public void setupTest() throws Exception {
        if (setupDone) {
            return;
        }
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(dummyWrappedKey);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        wrappingKeyPair = kpg.generateKeyPair();
        wrappedKeyPair = kpg.generateKeyPair();
        wrappingKeyID = PublisherID.generatePublicKeyDigest(wrappingKeyPair.getPublic());
        wrappingKeyName = VersioningProfile.addVersion(ContentName.fromNative("/parc/Users/briggs/KEY"));
        kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        wrappedDSAKeyPair = kpg.genKeyPair();
        kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(576);
        wrappedDHKeyPair = kpg.genKeyPair();
        byte[] key = new byte[16];
        sr.nextBytes(key);
        wrappingAESKey = new SecretKeySpec(key, "AES");
        sr.nextBytes(key);
        wrappedAESKey = new SecretKeySpec(key, "AES");
        ContentName nodeName = testHelper.getClassNamespace().append(ContentName.fromNative("/test/content/File1.txt"));
        storedKeyName = GroupAccessControlProfile.nodeKeyName(nodeName);
        setupDone = true;
        Log.info("Initialized keys for WrappedKeyTest");
    }

    @Test
    public void testWrapUnwrapKey() throws Exception {
        setupTest();
        Log.info("Entering testWrapUnwrapKey");
        Log.info("Wrap secret key in secret key.");
        WrappedKey wks = WrappedKey.wrapKey(wrappedAESKey, null, aLabel, wrappingAESKey);
        Key unwrappedKey = wks.unwrapKey(wrappingAESKey);
        Assert.assertArrayEquals(wrappedAESKey.getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap secret key in public key.");
        WrappedKey wksp = WrappedKey.wrapKey(wrappedAESKey, null, aLabel, wrappingKeyPair.getPublic());
        unwrappedKey = wksp.unwrapKey(wrappingKeyPair.getPrivate());
        Assert.assertArrayEquals(wrappedAESKey.getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap private key in public key.");
        WrappedKey wkpp = WrappedKey.wrapKey(wrappingKeyPair.getPrivate(), null, aLabel, wrappingKeyPair.getPublic());
        unwrappedKey = wkpp.unwrapKey(wrappingKeyPair.getPrivate());
        Assert.assertArrayEquals(wrappingKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap private key in secret key.");
        Log.info("wpk length " + wrappingKeyPair.getPrivate().getEncoded().length);
        WrappedKey wkp = WrappedKey.wrapKey(wrappingKeyPair.getPrivate(), null, aLabel, wrappingAESKey);
        unwrappedKey = wkp.unwrapKey(wrappingAESKey);
        Assert.assertArrayEquals(wrappingKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap DSA private in private.");
        wkpp = WrappedKey.wrapKey(wrappedDSAKeyPair.getPrivate(), null, aLabel, wrappingKeyPair.getPublic());
        unwrappedKey = wkpp.unwrapKey(wrappingKeyPair.getPrivate());
        Assert.assertArrayEquals(wrappedDSAKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap DSA private in secret.");
        wkp = WrappedKey.wrapKey(wrappedDSAKeyPair.getPrivate(), null, aLabel, wrappingAESKey);
        unwrappedKey = wkp.unwrapKey(wrappingAESKey);
        Assert.assertArrayEquals(wrappedDSAKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap DH private in private.");
        wkpp = WrappedKey.wrapKey(wrappedDHKeyPair.getPrivate(), null, aLabel, wrappingKeyPair.getPublic());
        unwrappedKey = wkpp.unwrapKey(wrappingKeyPair.getPrivate());
        Assert.assertArrayEquals(wrappedDHKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Wrap DH private in secret.");
        wkp = WrappedKey.wrapKey(wrappedDHKeyPair.getPrivate(), null, aLabel, wrappingAESKey);
        unwrappedKey = wkp.unwrapKey(wrappingAESKey);
        Assert.assertArrayEquals(wrappedDHKeyPair.getPrivate().getEncoded(), unwrappedKey.getEncoded());
        Log.info("Leaving testWrapUnwrapKey");
    }

    @Test
    public void testWrappedKeyByteArrayStringStringStringByteArrayByteArray() throws Exception {
        setupTest();
        Log.info("Entering testWrappedKeyByteArrayStringStringStringByteArrayByteArray");
        WrappedKey wka = null;
        wka = WrappedKey.wrapKey(wrappedAESKey, null, aLabel, wrappingKeyPair.getPublic());
        WrappedKey wk2 = new WrappedKey(wrappingKeyID, WrappedKey.wrapAlgorithmForKey(wrappingKeyPair.getPublic().getAlgorithm()), wrappedAESKey.getAlgorithm(), aLabel, wka.encryptedNonceKey(), wka.encryptedKey());
        WrappedKey dwk = new WrappedKey();
        WrappedKey bdwk = new WrappedKey();
        XMLEncodableTester.encodeDecodeTest("WrappedKey(full)", wk2, dwk, bdwk);
        wka.setWrappingKeyIdentifier(wrappingKeyID);
        Log.info("Leaving testWrappedKeyByteArrayStringStringStringByteArrayByteArray");
    }

    @Test
    public void testDecodeInputStream() throws Exception {
        setupTest();
        Log.info("Entering testDecodeInputStream");
        WrappedKey wk = new WrappedKey(wrappingKeyID, dummyWrappedKey);
        WrappedKey dwk = new WrappedKey();
        WrappedKey bdwk = new WrappedKey();
        XMLEncodableTester.encodeDecodeTest("WrappedKey(dummy)", wk, dwk, bdwk);
        WrappedKey wks = WrappedKey.wrapKey(wrappedAESKey, null, aLabel, wrappingAESKey);
        WrappedKey dwks = new WrappedKey();
        WrappedKey bdwks = new WrappedKey();
        XMLEncodableTester.encodeDecodeTest("WrappedKey(symmetric, real)", wks, dwks, bdwks);
        WrappedKey wka = WrappedKey.wrapKey(wrappedAESKey, NISTObjectIdentifiers.id_aes128_CBC.toString(), aLabel, wrappingKeyPair.getPublic());
        wka.setWrappingKeyIdentifier(wrappingKeyID);
        wka.setWrappingKeyName(wrappingKeyName);
        WrappedKey dwka = new WrappedKey();
        WrappedKey bdwka = new WrappedKey();
        XMLEncodableTester.encodeDecodeTest("WrappedKey(assymmetric wrap symmetric, with id and name)", wka, dwka, bdwka);
        Assert.assertArrayEquals(dwka.wrappingKeyIdentifier(), wrappingKeyID);
        Log.info("Leaving testDecodeInputStream");
    }

    @Test
    public void testWrappedKeyObject() throws Exception {
        setupTest();
        Log.info("Entering testWrappedKeyObject");
        WrappedKey wks = WrappedKey.wrapKey(wrappedAESKey, null, aLabel, wrappingAESKey);
        WrappedKey wka = WrappedKey.wrapKey(wrappedAESKey, NISTObjectIdentifiers.id_aes128_CBC.toString(), aLabel, wrappingKeyPair.getPublic());
        wka.setWrappingKeyIdentifier(wrappingKeyID);
        wka.setWrappingKeyName(wrappingKeyName);
        CCNHandle thandle = CCNHandle.open();
        CCNHandle thandle2 = CCNHandle.open();
        Flosser flosser = null;
        try {
            flosser = new Flosser();
            flosser.handleNamespace(storedKeyName);
            WrappedKeyObject wko = new WrappedKeyObject(storedKeyName, wks, SaveType.RAW, thandle);
            wko.save();
            Assert.assertTrue(VersioningProfile.hasTerminalVersion(wko.getVersionedName()));
            WrappedKeyObject wkoread = new WrappedKeyObject(storedKeyName, thandle2);
            Assert.assertTrue(wkoread.available());
            Assert.assertEquals(wkoread.getVersionedName(), wko.getVersionedName());
            Assert.assertEquals(wkoread.wrappedKey(), wko.wrappedKey());
            wko.save(wka);
            Assert.assertTrue(VersioningProfile.isLaterVersionOf(wko.getVersionedName(), wkoread.getVersionedName()));
            wkoread.update();
            Assert.assertEquals(wkoread.getVersionedName(), wko.getVersionedName());
            Assert.assertEquals(wkoread.wrappedKey(), wko.wrappedKey());
            Assert.assertEquals(wko.wrappedKey(), wka);
        } finally {
            if (null != flosser) {
                Log.info("WrappedKeyTest: Stopping flosser.");
                flosser.stop();
                Log.info("WrappedKeyTest: flosser stopped.");
            }
            thandle.close();
            thandle2.close();
        }
        Log.info("Leaving testWrappedKeyObject");
    }
}
