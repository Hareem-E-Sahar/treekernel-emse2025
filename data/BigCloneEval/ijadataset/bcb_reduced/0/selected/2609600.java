package org.rt.util;

import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.junit.Test;
import org.rt.author.clearFileSigner;
import org.rt.parser.CredentialSignatureVerifier;
import org.rt.util.KeyUtil;

public class PublicKeyTest {

    static final String NL = System.getProperty("line.separator");

    @Test
    public void testEncodeAndDecodeKeyRingToString() throws Exception {
        String identity = "Alice";
        char[] pass = { 'p', 'a', 's', 's' };
        PGPPublicKeyRing ring1 = KeyUtil.generateKeyPair(identity, pass);
        PGPPublicKey key1 = ring1.getPublicKey();
        String output = KeyUtil.encodeBase64(ring1.getEncoded());
        byte[] bytes = KeyUtil.decodeBase64(output);
        PGPPublicKeyRing ring2 = new PGPPublicKeyRing(bytes);
        PGPPublicKey key2 = ring2.getPublicKey();
        assertTrue(key1.getAlgorithm() == key2.getAlgorithm());
        assertTrue(key1.getBitStrength() == key2.getBitStrength());
        assertTrue(key1.getCreationTime().equals(key2.getCreationTime()));
        assertTrue(key1.getKeyID() == key2.getKeyID());
        assertTrue(ring2 != null);
    }

    @Test
    public void testWriteKeyToFileAndCompare() throws Exception {
        File file = new File("test.new");
        if (file.exists()) file.delete();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        String identity = "Alice";
        char[] pass = { 'p', 'a', 's', 's' };
        PGPPublicKeyRing ring1 = KeyUtil.generateKeyPair(identity, pass);
        String output = KeyUtil.encodeBase64(ring1.getEncoded());
        out.write(output);
        out.close();
        BufferedReader input = new BufferedReader(new FileReader("test.new"));
        StringBuffer buff = new StringBuffer();
        String line;
        while ((line = input.readLine()) != null) {
            buff.append(line);
        }
        String result = buff.toString();
        byte[] bytes = KeyUtil.decodeBase64(result);
        PGPPublicKeyRing ring2 = new PGPPublicKeyRing(bytes);
        String output2 = KeyUtil.encodeBase64(ring2.getEncoded());
        assertTrue("Keys did not match", output.equals(output2));
    }

    @Test
    public void testSignAndVerifyFromBase64() throws Exception {
        String identity = "Alice";
        char[] pass = { 'p', 'a', 's', 's' };
        PGPPublicKeyRing ring1 = KeyUtil.generateKeyPairPlus(identity, pass);
        String key64 = KeyUtil.encodeBase64(ring1.getEncoded());
        File toSign = new File("test/util/test.txt");
        File signedFile = new File("test/util/test.asc");
        File keyFile = new File("key.pri");
        BufferedWriter out = new BufferedWriter(new FileWriter(toSign));
        out.write(key64 + NL + "" + NL);
        out.close();
        clearFileSigner.signFile(toSign, keyFile, signedFile, pass);
        BufferedReader input = new BufferedReader(new FileReader(signedFile));
        StringBuffer buff = new StringBuffer();
        String line;
        while ((line = input.readLine()) != null) {
            buff.append(line + NL);
        }
        buff.deleteCharAt(buff.length() - 1);
        String credential = buff.toString();
        PGPPublicKeyRing ring2 = new PGPPublicKeyRing(KeyUtil.decodeBase64(key64));
        assertTrue(CredentialSignatureVerifier.verifySignature(credential, ring2));
    }

    @Test
    public void testLotsOfStuff() throws Exception {
        String identity = "Alice";
        char[] pass = { 'p', 'a', 's', 's' };
        PGPPublicKeyRing ring1 = KeyUtil.generateKeyPairPlus(identity, pass);
        String outputA = KeyUtil.encodeBase64(ring1.getEncoded());
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ring1.encode(bOut);
        byte[] bytes = bOut.toByteArray();
        String outputB = KeyUtil.encodeBase64(bytes);
        assertTrue(outputA.equals(outputB));
        File toSign = new File("test/util/test.txt");
        File signedFile = new File("test/util/test.asc");
        File keyFile = new File("key.pri");
        clearFileSigner.signFile(toSign, keyFile, signedFile, pass);
        BufferedReader input = new BufferedReader(new FileReader(signedFile));
        StringBuffer buff = new StringBuffer();
        String line;
        while ((line = input.readLine()) != null) {
            buff.append(line + NL);
        }
        buff.deleteCharAt(buff.length() - 1);
        String credential = buff.toString();
        assertTrue(CredentialSignatureVerifier.verifySignature(credential, ring1));
        PGPPublicKeyRing ring2 = new PGPPublicKeyRing(ring1.getEncoded());
        assertTrue(ring2.getPublicKey().getKeyID() == ring1.getPublicKey().getKeyID());
        PGPPublicKeyRing ring3 = new PGPPublicKeyRing(KeyUtil.decodeBase64(outputA));
        assertTrue(ring3.getPublicKey().getKeyID() == ring1.getPublicKey().getKeyID());
        assertTrue(CredentialSignatureVerifier.verifySignature(credential, ring3));
    }
}
