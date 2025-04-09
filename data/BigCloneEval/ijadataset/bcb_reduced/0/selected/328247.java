package com.pjsofts.eurobudget.crypt;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;

/**
 * test of cypher and cryptographic package of jdk (jce)
 * will encode a text file , sign it , save this a new file
 * then decode it in another new file too.
 * then compare original and new one.
 *
 *
 * @author  pjourdan
 */
public class Crypt {

    /** key used both in encoding and decoding */
    private SecretKey key = null;

    private Cipher cipher = null;

    /** Creates a new instance of Crypt */
    public Crypt() {
        String algorithm = "DES";
        String mode = "CBC";
        String padding = "PKCS5Padding";
        String transformation = algorithm + "/" + mode + "/" + padding;
        String provider = "SUN";
        String keyAgreementAlgorithm = "DiffieHellman";
        String keyGeneratorAlgorithm = "DES";
        String keyPairGeneratorAlgorithm = "DiffieHellman";
        String secretKeyGeneratorAlgorithm = "DiffieHellman";
        String secretKeyAlgorithm = "DES";
        String keyFactoryAlgorithm = "DiffieHellman";
        String algorithmParameterGenerator = "DiffieHellman";
        String algorithmParameters = "DES";
        String macAlgorithm = "HmacSHA1";
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(keyGeneratorAlgorithm);
            this.key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException nse) {
            nse.printStackTrace();
        }
        try {
            this.cipher = Cipher.getInstance(keyGeneratorAlgorithm);
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        } catch (NoSuchPaddingException nspad) {
            nspad.printStackTrace();
        }
    }

    /** encode a file */
    public void encode(File fromFile, File toFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            in = new BufferedInputStream(new FileInputStream(fromFile));
            out = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(toFile)), this.cipher);
            byte[] rbuffer = new byte[2056];
            int rcount = in.read(rbuffer);
            while (rcount > 0) {
                out.write(rbuffer, 0, rcount);
                rcount = in.read(rbuffer);
            }
        } catch (InvalidKeyException ke) {
            ke.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /** decode a file */
    public void decode(File fromFile, File toFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            in = new CipherInputStream(new BufferedInputStream(new FileInputStream(fromFile)), this.cipher);
            out = new BufferedOutputStream(new FileOutputStream(toFile));
            byte[] rbuffer = new byte[2056];
            int rcount = in.read(rbuffer);
            while (rcount > 0) {
                out.write(rbuffer, 0, rcount);
                rcount = in.read(rbuffer);
            }
        } catch (InvalidKeyException ke) {
            ke.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
}
