package de.fhkl.helloWorld.implementation.model.security;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * The Class CryptoPBEMD5. This class offers the java password based encryption
 * for the account of the user.
 * 
 * @author HelloWorld
 */
public class CryptoPBEMD5 {

    private String pbe = "PBEWithMD5AndDES";

    /**
	 * Decrypt the account of the user
	 * 
	 * @param in
	 *            the account of the user
	 * @param password
	 *            the password of the user
	 * @param pbeparams
	 *            the salt and the iteration count
	 * 
	 * @return the encrypted account of the user
	 */
    public InputStream decryptPBE(InputStream in, String password, PBEParameterSpec pbeparams) {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        try {
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(pbe);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            Cipher pbeCipher = Cipher.getInstance(pbe);
            pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeparams);
            CipherInputStream cis = new CipherInputStream(in, pbeCipher);
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            cis.close();
            in.close();
            return CryptoUtil.outToInputStream(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Encrypt the account of the user
	 * 
	 * @param in
	 *            the account of the user
	 * @param password
	 *            the password of the user
	 * @param pbeparams
	 *            the salt and the iteration count
	 * 
	 * @return the encypted
	 */
    public InputStream encryptPBE(InputStream in, String password, PBEParameterSpec pbeparams) {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac;
        try {
            keyFac = SecretKeyFactory.getInstance(pbe);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
            Cipher pbeCipher = Cipher.getInstance(pbe);
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeparams);
            CipherInputStream cis = new CipherInputStream(in, pbeCipher);
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            cis.close();
            in.close();
            return CryptoUtil.outToInputStream(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
