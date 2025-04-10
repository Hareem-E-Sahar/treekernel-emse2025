package org.apache.harmony.crypto.tests.javax.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import tests.support.resource.Support_Resources;
import org.apache.harmony.crypto.tests.support.MyCipher;

public class CipherTest extends junit.framework.TestCase {

    static Key cipherKey;

    static final String algorithm = "DESede";

    static final int keyLen = 168;

    static {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            kg.init(keyLen, new SecureRandom());
            cipherKey = kg.generateKey();
        } catch (Exception e) {
            fail("No key " + e);
        }
    }

    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String)
     */
    public void test_getInstanceLjava_lang_String() throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        assertNotNull("Received a null Cipher instance", cipher);
    }

    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String,
     *        java.lang.String)
     */
    public void test_getInstanceLjava_lang_StringLjava_lang_String() throws Exception {
        Provider[] providers = Security.getProviders("Cipher.DES");
        assertNotNull("No installed providers support Cipher.DES", providers);
        for (int i = 0; i < providers.length; i++) {
            Cipher cipher = Cipher.getInstance("DES", providers[i].getName());
            assertNotNull("Cipher.getInstance() returned a null value", cipher);
            try {
                cipher = Cipher.getInstance("DoBeDoBeDo", providers[i]);
                fail("Should have thrown an NoSuchAlgorithmException");
            } catch (NoSuchAlgorithmException e) {
            }
        }
        try {
            Cipher.getInstance("DES", (String) null);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Cipher.getInstance("DES", "IHaveNotBeenConfigured");
            fail("Should have thrown an NoSuchProviderException");
        } catch (NoSuchProviderException e) {
        }
    }

    /**
     * @tests javax.crypto.Cipher#getInstance(java.lang.String,
     *        java.security.Provider)
     */
    public void test_getInstanceLjava_lang_StringLjava_security_Provider() throws Exception {
        Provider[] providers = Security.getProviders("Cipher.DES");
        assertNotNull("No installed providers support Cipher.DES", providers);
        for (int i = 0; i < providers.length; i++) {
            Cipher cipher = Cipher.getInstance("DES", providers[i]);
            assertNotNull("Cipher.getInstance() returned a null value", cipher);
        }
    }

    /**
     * @tests javax.crypto.Cipher#getProvider()
     */
    public void test_getProvider() throws Exception {
        Provider[] providers = Security.getProviders("Cipher.AES");
        assertNotNull("No providers support Cipher.AES", providers);
        for (int i = 0; i < providers.length; i++) {
            Provider provider = providers[i];
            Cipher cipher = Cipher.getInstance("AES", provider.getName());
            Provider cipherProvider = cipher.getProvider();
            assertTrue("Cipher provider is not the same as that " + "provided as parameter to getInstance()", cipherProvider.equals(provider));
        }
    }

    /**
     * @tests javax.crypto.Cipher#getAlgorithm()
     */
    public void test_getAlgorithm() throws Exception {
        final String algorithm = "DESede/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        assertTrue("Cipher algorithm does not match", cipher.getAlgorithm().equals(algorithm));
    }

    /**
     * @tests javax.crypto.Cipher#getBlockSize()
     */
    public void test_getBlockSize() throws Exception {
        final String algorithm = "DESede/CBC/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(algorithm);
        assertEquals("Block size does not match", 8, cipher.getBlockSize());
    }

    /**
     * @tests javax.crypto.Cipher#getOutputSize(int)
     */
    public void test_getOutputSizeI() throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);
        int result = cipher.getOutputSize(25);
        assertTrue("Output size too small", result > 31);
        result = cipher.getOutputSize(8);
        assertTrue("Output size too small", result > 15);
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key)
     */
    public void test_initILjava_security_Key() throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.SecureRandom)
     */
    public void test_initILjava_security_KeyLjava_security_SecureRandom() throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, sr);
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.spec.AlgorithmParameterSpec)
     */
    public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpec() throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = null;
        byte[] iv = null;
        AlgorithmParameterSpec ivAVP = null;
        iv = new byte[8];
        sr.nextBytes(iv);
        ivAVP = new IvParameterSpec(iv);
        cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivAVP);
        byte[] cipherIV = cipher.getIV();
        assertTrue("IVs differ", Arrays.equals(cipherIV, iv));
    }

    /**
     * @tests javax.crypto.Cipher#init(int, java.security.Key,
     *        java.security.spec.AlgorithmParameterSpec,
     *        java.security.SecureRandom)
     */
    public void test_initILjava_security_KeyLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom() throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = null;
        byte[] iv = null;
        AlgorithmParameterSpec ivAVP = null;
        iv = new byte[8];
        sr.nextBytes(iv);
        ivAVP = new IvParameterSpec(iv);
        cipher = Cipher.getInstance(algorithm + "/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivAVP, sr);
        byte[] cipherIV = cipher.getIV();
        assertTrue("IVs differ", Arrays.equals(cipherIV, iv));
    }

    /**
     * @tests javax.crypto.Cipher#update(byte[], int, int)
     */
    public void test_update$BII() throws Exception {
        for (int index = 1; index < 4; index++) {
            Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");
            byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".key");
            DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
            Key k = skf.generateSecret(keySpec);
            byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".iv");
            IvParameterSpec iv = new IvParameterSpec(ivMaterial);
            c.init(Cipher.DECRYPT_MODE, k, iv);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] input = new byte[256];
            String resPath = "hyts_" + "des-ede3-cbc.test" + index + ".ciphertext";
            InputStream is = Support_Resources.getResourceStream(resPath);
            int bytesRead = is.read(input, 0, 256);
            while (bytesRead > 0) {
                byte[] output = c.update(input, 0, bytesRead);
                if (output != null) {
                    baos.write(output);
                }
                bytesRead = is.read(input, 0, 256);
            }
            byte[] output = c.doFinal();
            if (output != null) {
                baos.write(output);
            }
            byte[] decipheredCipherText = baos.toByteArray();
            is.close();
            byte[] plaintextBytes = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".plaintext");
            assertTrue("Operation produced incorrect results", Arrays.equals(plaintextBytes, decipheredCipherText));
        }
    }

    /**
     * @tests javax.crypto.Cipher#doFinal()
     */
    public void test_doFinal() throws Exception {
        for (int index = 1; index < 4; index++) {
            Cipher c = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");
            byte[] keyMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".key");
            DESedeKeySpec keySpec = new DESedeKeySpec(keyMaterial);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESEDE");
            Key k = skf.generateSecret(keySpec);
            byte[] ivMaterial = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".iv");
            IvParameterSpec iv = new IvParameterSpec(ivMaterial);
            c.init(Cipher.ENCRYPT_MODE, k, iv);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] input = new byte[256];
            String resPath = "hyts_" + "des-ede3-cbc.test" + index + ".plaintext";
            InputStream is = Support_Resources.getResourceStream(resPath);
            int bytesRead = is.read(input, 0, 256);
            while (bytesRead > 0) {
                byte[] output = c.update(input, 0, bytesRead);
                if (output != null) {
                    baos.write(output);
                }
                bytesRead = is.read(input, 0, 256);
            }
            byte[] output = c.doFinal();
            if (output != null) {
                baos.write(output);
            }
            byte[] encryptedPlaintext = baos.toByteArray();
            is.close();
            byte[] cipherText = loadBytes("hyts_" + "des-ede3-cbc.test" + index + ".ciphertext");
            assertTrue("Operation produced incorrect results", Arrays.equals(encryptedPlaintext, cipherText));
        }
    }

    private byte[] loadBytes(String resPath) {
        try {
            InputStream is = Support_Resources.getResourceStream(resPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int readlen;
            while ((readlen = is.read(buff)) > 0) {
                out.write(buff, 0, readlen);
            }
            is.close();
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public void testGetParameters() throws Exception {
        Cipher c = Cipher.getInstance("DES");
        assertNull(c.getParameters());
    }

    public void testUpdatebyteArrayintintbyteArrayint() throws Exception {
        Cipher c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        byte[] b = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] b1 = new byte[6];
        try {
            c.update(b, 0, 10, b1, 5);
            fail("No expected ShortBufferException");
        } catch (ShortBufferException e) {
        }
    }

    public void testDoFinalbyteArrayintintbyteArrayint() throws Exception {
        Cipher c = Cipher.getInstance("DESede");
        c.init(Cipher.ENCRYPT_MODE, cipherKey);
        byte[] b = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        byte[] b1 = new byte[6];
    }

    public void testGetMaxAllowedKeyLength() throws NoSuchAlgorithmException {
        try {
            Cipher.getMaxAllowedKeyLength(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Cipher.getMaxAllowedKeyLength("//CBC/PKCS5Paddin");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Cipher.getMaxAllowedKeyLength("/DES/CBC/PKCS5Paddin/1");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public void testGetMaxAllowedParameterSpec() throws NoSuchAlgorithmException {
        try {
            Cipher.getMaxAllowedParameterSpec(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Cipher.getMaxAllowedParameterSpec("/DES//PKCS5Paddin");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
        try {
            Cipher.getMaxAllowedParameterSpec("/DES/CBC/ /1");
            fail("No expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        }
    }

    /**
     * @tests javax.crypto.Cipher#Cipher(CipherSpi cipherSpi, Provider provider,
     *        String transformation)
     */
    public void test_Ctor() throws Exception {
        try {
            new testCipher(null, null, "s");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            new testCipher(new MyCipher(), null, "s");
            fail("NullPointerException expected for 'null' provider");
        } catch (NullPointerException e) {
        }
        try {
            new testCipher(null, new Provider("qwerty", 1.0, "qwerty") {
            }, "s");
            fail("NullPointerException expected for 'null' cipherSpi");
        } catch (NullPointerException e) {
        }
    }

    class testCipher extends Cipher {

        testCipher(CipherSpi c, Provider p, String s) {
            super(c, p, s);
        }
    }
}
