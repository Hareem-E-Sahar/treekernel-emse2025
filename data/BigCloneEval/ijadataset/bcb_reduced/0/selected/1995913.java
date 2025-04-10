package com.google.code.p.keytooliui.ktl.util.jarsigner;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;
import com.google.code.p.keytooliui.shared.lang.*;
import com.google.code.p.keytooliui.shared.swing.optionpane.*;
import com.google.code.p.keytooliui.shared.util.jarsigner.*;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.awt.*;
import java.util.*;

public abstract class KTLShkAbs extends KTLAbs {

    protected static SecretKey _s_readKey_(File fleOpen, String strSignatureAlgoCandidate) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        String strMethod = "_s_readKey_(...)";
        String strSignatureAlgo = null;
        for (int i = 0; i < KTLAbs.f_s_strsSigAlgoSKJceks.length; i++) {
            if (!strSignatureAlgoCandidate.equalsIgnoreCase(KTLAbs.f_s_strsSigAlgoSKJceks[i])) continue;
            strSignatureAlgo = KTLAbs.f_s_strsSigAlgoSKJceks[i];
            break;
        }
        if (strSignatureAlgo == null) {
            MySystem.s_printOutExit(strMethod, "uncaught strSignatureAlgoCandidate:" + strSignatureAlgoCandidate);
        }
        DataInputStream dis = new DataInputStream(new FileInputStream(fleOpen));
        byte[] bytsRawKey = new byte[(int) fleOpen.length()];
        dis.readFully(bytsRawKey);
        dis.close();
        SecretKey sky = null;
        if (strSignatureAlgo.equalsIgnoreCase("DES")) {
            DESKeySpec obj = new DESKeySpec(bytsRawKey);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(strSignatureAlgo);
            sky = skf.generateSecret(obj);
        } else if (strSignatureAlgo.equalsIgnoreCase("DESede")) {
            DESedeKeySpec obj = new DESedeKeySpec(bytsRawKey);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(strSignatureAlgo);
            sky = skf.generateSecret(obj);
        } else {
            SecretKeySpec obj = new SecretKeySpec(bytsRawKey, strSignatureAlgo);
            sky = (SecretKey) obj;
        }
        return sky;
    }

    protected static void _writeKey_(SecretKey sky, File fleSave) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, Exception {
        String strMethod = "KTLShkAbs._writeKey_(...)";
        String strAlgoSecretKey = sky.getAlgorithm();
        byte[] bytsRawKey = null;
        if (strAlgoSecretKey.equalsIgnoreCase("DES")) {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(strAlgoSecretKey);
            DESKeySpec spec = (DESKeySpec) skf.getKeySpec(sky, DESKeySpec.class);
            bytsRawKey = spec.getKey();
        } else if (strAlgoSecretKey.equalsIgnoreCase("DESede")) {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(strAlgoSecretKey);
            DESedeKeySpec spec = (DESedeKeySpec) skf.getKeySpec(sky, DESedeKeySpec.class);
            bytsRawKey = spec.getKey();
        } else {
            SecretKeySpec sks = (SecretKeySpec) sky;
            bytsRawKey = sks.getEncoded();
        }
        FileOutputStream fos = new FileOutputStream(fleSave);
        fos.write(bytsRawKey);
        fos.close();
    }

    /** 
     * Use the specified key to decrypt bytes ready from the input 
     * stream and write them to the output stream.  This method 
     * uses Cipher directly to show how it can be done without 
     * CipherInputStream and CipherOutputStream.
     **/
    protected static void _s_decrypt_(SecretKey key, InputStream in, OutputStream out, String strInstanceCipher) throws NoSuchAlgorithmException, InvalidKeyException, IOException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(strInstanceCipher);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(cipher.update(buffer, 0, bytesRead));
        }
        out.write(cipher.doFinal());
        out.flush();
    }

    /** 
     * Use the specified key to encrypt bytes from the input stream
     * and write them to the output stream.  This method uses 
     * CipherOutputStream to perform the encryption and write bytes at the
     * same time.
     **/
    protected static void _s_encrypt_(SecretKey sky, InputStream ism, OutputStream osm, String strInstanceCipher) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        Cipher cip = Cipher.getInstance(strInstanceCipher);
        cip.init(Cipher.ENCRYPT_MODE, sky);
        CipherOutputStream cos = new CipherOutputStream(osm, cip);
        byte[] bytsBuffer = new byte[2048];
        int intBytesRead;
        while ((intBytesRead = ism.read(bytsBuffer)) != -1) {
            cos.write(bytsBuffer, 0, intBytesRead);
        }
        cos.close();
        java.util.Arrays.fill(bytsBuffer, (byte) 0);
    }

    protected KTLShkAbs(Frame frmOwner, String strPathAbsKst, char[] chrsPasswdKst, String strProviderKst) {
        super(frmOwner, strPathAbsKst, chrsPasswdKst, strProviderKst);
    }
}
