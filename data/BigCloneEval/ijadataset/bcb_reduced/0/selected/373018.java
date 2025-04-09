package org.nestframework.commons.utils;

import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Hex;
import sun.security.rsa.RSAPublicKeyImpl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * RSA encrypt
 * @author wanghai
 * @version 1.0.0
 */
public class RSA_Encrypt {

    /** assign to RSA */
    private static String ALGORITHM = "RSA";

    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /** assign key size */
    private static int KEYSIZE = 1024;

    /** assign publicKey filename */
    private static String PUBLIC_KEY_FILE = "PublicKey";

    /** assign privateKey filename */
    private static String PRIVATE_KEY_FILE = "PrivateKey";

    private static PublicKey publicKey;

    private static PrivateKey privateKey;

    /**
	 * generate keypair(publicKey and privateKey)
	 */
    private static void generateKeyPair() throws Exception {
        SecureRandom sr = new SecureRandom();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
        kpg.initialize(KEYSIZE, sr);
        KeyPair kp = kpg.generateKeyPair();
        publicKey = kp.getPublic();
        privateKey = kp.getPrivate();
        writePublicKey(PUBLIC_KEY_FILE);
        writePrivateKey(PRIVATE_KEY_FILE);
    }

    /**
	 * get privateKey from default path
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
    public static void getPrivateKey() throws FileNotFoundException, IOException, ClassNotFoundException {
        if (privateKey == null) {
            String f = RSA_Encrypt.class.getResource("/").getFile();
            getPrivateKey(f + PRIVATE_KEY_FILE);
        }
    }

    /**
	 * get privateKey from assigned path
	 * @param privateKeyPath
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
    public static void getPrivateKey(String privateKeyPath) throws FileNotFoundException, IOException, ClassNotFoundException {
        String s = ReadFile(privateKeyPath);
        String mod = GetValue("m=", s);
        String priExp = GetValue("privateExponent=", s);
        BigInteger m = new BigInteger(mod, 16);
        BigInteger e = new BigInteger(priExp, 16);
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }
    }

    /**
	 * get publicKey from default path
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
    public static void getPublicKey() throws FileNotFoundException, IOException, ClassNotFoundException {
        if (publicKey == null) {
            String f = RSA_Encrypt.class.getResource("/").getFile();
            getPublicKey(f + PUBLIC_KEY_FILE);
        }
    }

    /**
	 * get publicKey from assigned path
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
    public static void getPublicKey(String publicKeyPath) throws FileNotFoundException, IOException, ClassNotFoundException {
        String s = ReadFile(publicKeyPath);
        String mod = GetValue("m=", s);
        String pubExp = GetValue("e=", s);
        BigInteger m = new BigInteger(mod, 16);
        BigInteger e = new BigInteger(pubExp, 16);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }
    }

    public static String ReadFile(String FileName) {
        String s = "";
        try {
            FileInputStream f = new FileInputStream(FileName);
            byte b[] = new byte[f.available()];
            f.read(b);
            s = new String(b);
        } catch (Exception exception) {
        }
        return s;
    }

    public static String GetValue(String n, String src) {
        String s = "";
        int i1 = src.indexOf(n);
        i1 += n.length();
        int i2 = src.indexOf(";", i1);
        s = src.substring(i1, i2);
        return s;
    }

    /**
	 * write publicKey to Txt file
	 * @param publicKeyPath
	 */
    public static void writePublicKey(String publicKeyPath) {
        try {
            PrintWriter pw;
            pw = new PrintWriter(new FileWriter(publicKeyPath));
            pw.println("-------------PUBLIC_KEY-------------");
            BigInteger m = ((RSAPublicKeyImpl) publicKey).getModulus();
            BigInteger e = ((RSAPublicKeyImpl) publicKey).getPublicExponent();
            pw.println("bitlen=" + m.bitLength() + ";");
            String mStr = m.toString(16);
            if ((mStr.length() % 2) == 1) mStr = "0" + mStr;
            pw.println("m=" + mStr + ";");
            String eStr = e.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("e=" + eStr + ";");
            pw.println("-------------PUBLIC_KEY-------------");
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * write publicKey to Txt file
	 * @param publicKeyPath
	 */
    public static void writePrivateKey(String privateKeyPath) {
        try {
            PrintWriter pw;
            pw = new PrintWriter(new FileWriter(privateKeyPath));
            pw.println("-------------PRIVATE_KEY-------------");
            BigInteger m = ((RSAPrivateCrtKey) privateKey).getModulus();
            BigInteger e = ((RSAPrivateCrtKey) privateKey).getPublicExponent();
            BigInteger privateExponent = ((RSAPrivateCrtKey) privateKey).getPrivateExponent();
            BigInteger p = ((RSAPrivateCrtKey) privateKey).getPrimeP();
            BigInteger q = ((RSAPrivateCrtKey) privateKey).getPrimeQ();
            BigInteger dP = ((RSAPrivateCrtKey) privateKey).getPrimeExponentP();
            BigInteger dQ = ((RSAPrivateCrtKey) privateKey).getPrimeExponentQ();
            BigInteger qInv = ((RSAPrivateCrtKey) privateKey).getCrtCoefficient();
            pw.println("bitlen=" + m.bitLength() + ";");
            String mStr = m.toString(16);
            if ((mStr.length() % 2) == 1) mStr = "0" + mStr;
            pw.println("m=" + mStr + ";");
            String eStr = e.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("e=" + eStr + ";");
            eStr = privateExponent.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("privateExponent=" + eStr + ";");
            eStr = p.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("p=" + eStr + ";");
            eStr = q.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("q=" + eStr + ";");
            eStr = dQ.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("dQ=" + eStr + ";");
            eStr = dP.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("dP=" + eStr + ";");
            eStr = qInv.toString(16);
            if ((eStr.length() % 2) == 1) eStr = "0" + eStr;
            pw.println("qInv=" + eStr + ";");
            pw.println("-------------PRIVATE_KEY-------------");
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * encrypt source data use publicKey
	 * @param source
	 * @return
	 * @throws Exception
	 */
    public static String encrypt(String source) throws Exception {
        getPublicKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b = source.getBytes();
        byte[] b1 = cipher.doFinal(b);
        return new String(Hex.encodeHex(b1));
    }

    /**
	 * decrypt cryptograph use privateKey
	 */
    public static String decrypt(String cryptograph) throws Exception {
        getPrivateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] b1 = Hex.decodeHex(cryptograph.toCharArray());
        byte[] b = cipher.doFinal(b1);
        return new String(b);
    }

    /**
	 * 
	 * 用私钥对信息生成数字签名
	 * @param data
	 *            加密数据
	 * @param privateKey
	 *            私钥
	 * @return
	 * @throws Exception
	 */
    public static String sign(String data) {
        try {
            getPrivateKey();
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            byte[] b1 = signature.sign();
            return Hex.encodeHexString(b1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**  
     * 校验数字签名  
     * @param data  
     *            加密数据  
     * @param sign  
     *            数字签名  
     * @return 校验成功返回true 失败返回false  
     * @throws Exception  
     */
    public static boolean verify(String data, String sign) {
        try {
            getPublicKey();
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            byte[] b1 = Hex.decodeHex(sign.toCharArray());
            signature.update(data.getBytes());
            return signature.verify(b1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        getPrivateKey(PRIVATE_KEY_FILE);
        getPublicKey(PUBLIC_KEY_FILE);
        String source = "296502429874592438576248524admin";
        String cryptograph = encrypt(source);
        System.out.println(cryptograph);
        String target = decrypt(cryptograph);
        System.out.println(target);
        String s = sign(source);
        System.out.println(s);
        System.out.println(verify(source, s));
    }
}
