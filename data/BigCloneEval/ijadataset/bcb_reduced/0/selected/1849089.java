package org.owasp.esapi.crypto;

import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.crypto.CipherSpec;
import org.owasp.esapi.crypto.CipherText;
import org.owasp.esapi.crypto.CryptoHelper;
import org.owasp.esapi.errors.EncryptionException;
import org.owasp.esapi.reference.crypto.CryptoPolicy;

public class CipherTextTest {

    private static final boolean POST_CLEANUP = true;

    private CipherSpec cipherSpec = null;

    private Cipher encryptor = null;

    private Cipher decryptor = null;

    private IvParameterSpec ivSpec = null;

    @BeforeClass
    public static void preCleanup() {
        new File("ciphertext.ser").delete();
        new File("ciphertext-portable.ser").delete();
    }

    @Before
    public void setUp() throws Exception {
        encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] ivBytes = null;
        ivBytes = ESAPI.randomizer().getRandomBytes(encryptor.getBlockSize());
        ivSpec = new IvParameterSpec(ivBytes);
    }

    @After
    public void tearDown() throws Exception {
    }

    @AfterClass
    public static void postCleanup() {
        if (POST_CLEANUP) {
            new File("ciphertext.ser").delete();
            new File("ciphertext-portable.ser").delete();
        }
    }

    /** Test the default CTOR */
    @Test
    public final void testCipherText() {
        CipherText ct = new CipherText();
        cipherSpec = new CipherSpec();
        assertTrue(ct.getCipherTransformation().equals(cipherSpec.getCipherTransformation()));
        assertTrue(ct.getBlockSize() == cipherSpec.getBlockSize());
    }

    @Test
    public final void testCipherTextCipherSpec() {
        cipherSpec = new CipherSpec("DESede/OFB8/NoPadding", 112);
        CipherText ct = new CipherText(cipherSpec);
        assertTrue(ct.getRawCipherText() == null);
        assertTrue(ct.getCipherAlgorithm().equals("DESede"));
        assertTrue(ct.getKeySize() == cipherSpec.getKeySize());
    }

    @Test
    public final void testCipherTextCipherSpecByteArray() {
        try {
            CipherSpec cipherSpec = new CipherSpec(encryptor, 128);
            cipherSpec.setIV(ivSpec.getIV());
            SecretKey key = CryptoHelper.generateSecretKey(cipherSpec.getCipherAlgorithm(), 128);
            encryptor.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] raw = encryptor.doFinal("Hello".getBytes("UTF8"));
            CipherText ct = new CipherText(cipherSpec, raw);
            assertTrue(ct != null);
            byte[] ctRaw = ct.getRawCipherText();
            assertTrue(ctRaw != null);
            assertArrayEquals(raw, ctRaw);
            assertTrue(ct.getCipherTransformation().equals(cipherSpec.getCipherTransformation()));
            ;
            assertTrue(ct.getCipherAlgorithm().equals(cipherSpec.getCipherAlgorithm()));
            assertTrue(ct.getPaddingScheme().equals(cipherSpec.getPaddingScheme()));
            assertTrue(ct.getBlockSize() == cipherSpec.getBlockSize());
            assertTrue(ct.getKeySize() == cipherSpec.getKeySize());
            byte[] ctIV = ct.getIV();
            byte[] csIV = cipherSpec.getIV();
            assertArrayEquals(ctIV, csIV);
        } catch (Exception ex) {
            fail("Caught unexpected exception: " + ex.getClass().getName() + "; exception message was: " + ex.getMessage());
        }
    }

    @Test
    public final void testDecryptionUsingCipherText() {
        try {
            CipherSpec cipherSpec = new CipherSpec(encryptor, 128);
            cipherSpec.setIV(ivSpec.getIV());
            assertTrue(cipherSpec.getIV() != null);
            assertTrue(cipherSpec.getIV().length > 0);
            SecretKey key = CryptoHelper.generateSecretKey(cipherSpec.getCipherAlgorithm(), 128);
            encryptor.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ctraw = encryptor.doFinal("Hello".getBytes("UTF8"));
            CipherText ct = new CipherText(cipherSpec, ctraw);
            assertTrue(ct.getCipherMode().equals("CBC"));
            assertTrue(ct.requiresIV());
            String b64ctraw = ct.getBase64EncodedRawCipherText();
            assertTrue(b64ctraw != null);
            assertArrayEquals(ESAPI.encoder().decodeFromBase64(b64ctraw), ctraw);
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ct.getIV()));
            byte[] ptraw = decryptor.doFinal(ESAPI.encoder().decodeFromBase64(b64ctraw));
            assertTrue(ptraw != null);
            assertTrue(ptraw.length > 0);
            String plaintext = new String(ptraw, "UTF-8");
            assertTrue(plaintext.equals("Hello"));
            assertArrayEquals(ct.getRawCipherText(), ctraw);
            byte[] ivAndRaw = ESAPI.encoder().decodeFromBase64(ct.getEncodedIVCipherText());
            assertTrue(ivAndRaw.length > ctraw.length);
            assertTrue(ct.getBlockSize() == (ivAndRaw.length - ctraw.length));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail("Caught unexpected exception: " + ex.getClass().getName() + "; exception message was: " + ex.getMessage());
        }
    }

    @Test
    public final void testMIC() {
        try {
            CipherSpec cipherSpec = new CipherSpec(encryptor, 128);
            cipherSpec.setIV(ivSpec.getIV());
            SecretKey key = CryptoHelper.generateSecretKey(cipherSpec.getCipherAlgorithm(), 128);
            encryptor.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ctraw = encryptor.doFinal("Hello".getBytes("UTF8"));
            CipherText ct = new CipherText(cipherSpec, ctraw);
            assertTrue(ct.getIV() != null && ct.getIV().length > 0);
            SecretKey authKey = CryptoHelper.computeDerivedKey(key, key.getEncoded().length * 8, "authenticity");
            ct.computeAndStoreMAC(authKey);
            try {
                ct.setIVandCiphertext(ivSpec.getIV(), ctraw);
            } catch (Exception ex) {
                assertTrue(ex instanceof EncryptionException);
            }
            try {
                ct.setCiphertext(ctraw);
            } catch (Exception ex) {
                assertTrue(ex instanceof EncryptionException);
            }
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ct.getIV()));
            byte[] ptraw = decryptor.doFinal(ct.getRawCipherText());
            assertTrue(ptraw != null && ptraw.length > 0);
            ct.validateMAC(authKey);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            fail("Caught unexpected exception: " + ex.getClass().getName() + "; exception message was: " + ex.getMessage());
        }
    }

    /** Test <i>portable</i> serialization. */
    @Test
    public final void testPortableSerialization() {
        System.err.println("CipherTextTest.testPortableSerialization()...");
        String filename = "ciphertext-portable.ser";
        File serializedFile = new File(filename);
        serializedFile.delete();
        int keySize = 128;
        if (CryptoPolicy.isUnlimitedStrengthCryptoAvailable()) {
            keySize = 256;
        }
        CipherSpec cipherSpec = new CipherSpec(encryptor, keySize);
        cipherSpec.setIV(ivSpec.getIV());
        SecretKey key;
        try {
            key = CryptoHelper.generateSecretKey(cipherSpec.getCipherAlgorithm(), keySize);
            encryptor.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] raw = encryptor.doFinal("This is my secret message!!!".getBytes("UTF8"));
            CipherText ciphertext = new CipherText(cipherSpec, raw);
            SecretKey authKey = CryptoHelper.computeDerivedKey(key, key.getEncoded().length * 8, "authenticity");
            ciphertext.computeAndStoreMAC(authKey);
            byte[] serializedBytes = ciphertext.asPortableSerializedByteArray();
            FileOutputStream fos = new FileOutputStream(serializedFile);
            fos.write(serializedBytes);
            fos.close();
            FileInputStream fis = new FileInputStream(serializedFile);
            int avail = fis.available();
            byte[] bytes = new byte[avail];
            fis.read(bytes, 0, avail);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
            }
            CipherText restoredCipherText = CipherText.fromPortableSerializedBytes(bytes);
            assertTrue(ciphertext.equals(restoredCipherText));
        } catch (EncryptionException e) {
            Assert.fail("Caught EncryptionException: " + e);
        } catch (FileNotFoundException e) {
            Assert.fail("Caught FileNotFoundException: " + e);
        } catch (IOException e) {
            Assert.fail("Caught IOException: " + e);
        } catch (Exception e) {
            Assert.fail("Caught Exception: " + e);
        } finally {
            serializedFile.delete();
        }
    }

    /** Test Java serialization. */
    @Test
    public final void testJavaSerialization() {
        String filename = "ciphertext.ser";
        File serializedFile = new File(filename);
        try {
            serializedFile.delete();
            CipherSpec cipherSpec = new CipherSpec(encryptor, 128);
            cipherSpec.setIV(ivSpec.getIV());
            SecretKey key = CryptoHelper.generateSecretKey(cipherSpec.getCipherAlgorithm(), 128);
            encryptor.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] raw = encryptor.doFinal("This is my secret message!!!".getBytes("UTF8"));
            CipherText ciphertext = new CipherText(cipherSpec, raw);
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(ciphertext);
            out.close();
            fos.close();
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            CipherText restoredCipherText = (CipherText) in.readObject();
            in.close();
            fis.close();
            assertEquals("1: Serialized restored CipherText differs from saved CipherText", ciphertext.toString(), restoredCipherText.toString());
            assertArrayEquals("2: Serialized restored CipherText differs from saved CipherText", ciphertext.getIV(), restoredCipherText.getIV());
            assertEquals("3: Serialized restored CipherText differs from saved CipherText", ciphertext.getBase64EncodedRawCipherText(), restoredCipherText.getBase64EncodedRawCipherText());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected IOException: " + ex);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected ClassNotFoundException: " + ex);
        } catch (EncryptionException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected EncryptionException: " + ex);
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected IllegalBlockSizeException: " + ex);
        } catch (BadPaddingException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected BadPaddingException: " + ex);
        } catch (InvalidKeyException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected InvalidKeyException: " + ex);
        } catch (InvalidAlgorithmParameterException ex) {
            ex.printStackTrace(System.err);
            fail("testJavaSerialization(): Unexpected InvalidAlgorithmParameterException: " + ex);
        } finally {
            serializedFile.delete();
        }
    }

    /**
	 * Run all the test cases in this suite.
	 * This is to allow running from {@code org.owasp.esapi.AllTests} which
	 * uses a JUnit 3 test runner.
	 */
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CipherTextTest.class);
    }
}
