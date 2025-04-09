package nox.encrypt.testEncryption;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.io.*;
import java.math.BigInteger;

/**
 * RSA 工具类。提供加密，解密，生成密钥对等方法。
 * 需要到http://www.bouncycastle.org下载bcprov-jdk14-123.jar。
 * 
* @author xiaoyusong
*           mail: xiaoyusong@etang.com
*           msn:xiao_yu_song@hotmail.com
* @since 2004-5-20
 */
public class RSAUtil {

    /**
	 * 生成密钥对
	 * 
	 * @return KeyPair
	 * @throws GeneralSecurityException
	 */
    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            final int KEY_SIZE = 1024;
            keyPairGen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = keyPairGen.genKeyPair();
            return keyPair;
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    /**
	 * 生成公钥
	 * 
	 * @param modulus
	 * @param publicExponent
	 * @return RSAPublicKey
	 * @throws GeneralSecurityException
	 */
    public static RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws GeneralSecurityException {
        KeyFactory keyFac = null;
        try {
            keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new GeneralSecurityException(ex.getMessage());
        }
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
        try {
            return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new GeneralSecurityException(ex.getMessage());
        }
    }

    /**
	 * 生成私钥
	 * 
	 * @param modulus
	 * @param privateExponent
	 * @return RSAPrivateKey
	 * @throws GeneralSecurityException
	 */
    public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws GeneralSecurityException {
        KeyFactory keyFac = null;
        try {
            keyFac = KeyFactory.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new GeneralSecurityException(ex.getMessage());
        }
        RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(privateExponent));
        try {
            return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
        } catch (InvalidKeySpecException ex) {
            throw new GeneralSecurityException(ex.getMessage());
        }
    }

    /**
	 * 加密
	 * 
	 * @param key
	 *            加密的密钥
	 * @param data
	 *            待加密的明文数据
	 * @return 加密后的数据
	 * @throws GeneralSecurityException
	 */
    public static byte[] encrypt(Key key, byte[] data) throws GeneralSecurityException {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int blockSize = cipher.getBlockSize();
            int outputSize = cipher.getOutputSize(data.length);
            int leavedSize = data.length % blockSize;
            int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
            byte[] raw = new byte[outputSize * blocksSize];
            int i = 0;
            while (data.length - i * blockSize > 0) {
                if (data.length - i * blockSize > blockSize) cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize); else cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
                i++;
            }
            return raw;
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    /**
	 * 解密
	 * 
	 * @param key
	 *            解密的密钥
	 * @param raw
	 *            已经加密的数据
	 * @return 解密后的明文
	 * @throws GeneralSecurityException
	 */
    public static byte[] decrypt(Key key, byte[] raw) throws GeneralSecurityException {
        try {
            Cipher cipher = Cipher.getInstance("RSA", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, key);
            int blockSize = cipher.getBlockSize();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
            int j = 0;
            while (raw.length - j * blockSize > 0) {
                bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
                j++;
            }
            return bout.toByteArray();
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    /**
	 * 
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        File file = new File("test.html");
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] tmpbuf = new byte[1024];
        int count = 0;
        while ((count = in.read(tmpbuf)) != -1) {
            bout.write(tmpbuf, 0, count);
            tmpbuf = new byte[1024];
        }
        in.close();
        byte[] orgData = bout.toByteArray();
        KeyPair keyPair = RSAUtil.generateKeyPair();
        RSAPublicKey pubKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey priKey = (RSAPrivateKey) keyPair.getPrivate();
        byte[] pubModBytes = pubKey.getModulus().toByteArray();
        byte[] pubPubExpBytes = pubKey.getPublicExponent().toByteArray();
        byte[] priModBytes = priKey.getModulus().toByteArray();
        byte[] priPriExpBytes = priKey.getPrivateExponent().toByteArray();
        RSAPublicKey recoveryPubKey = RSAUtil.generateRSAPublicKey(pubModBytes, pubPubExpBytes);
        RSAPrivateKey recoveryPriKey = RSAUtil.generateRSAPrivateKey(priModBytes, priPriExpBytes);
        byte[] raw = RSAUtil.encrypt(priKey, orgData);
        file = new File("encrypt_result.dat");
        OutputStream out = new FileOutputStream(file);
        out.write(raw);
        out.close();
        byte[] data = RSAUtil.decrypt(recoveryPubKey, raw);
        file = new File("decrypt_result.html");
        out = new FileOutputStream(file);
        out.write(data);
        out.flush();
        out.close();
    }
}
