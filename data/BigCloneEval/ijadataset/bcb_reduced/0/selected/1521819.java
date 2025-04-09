package de.objectcode.openk.soa.auth.store.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultDataSigner implements IDataSigner {

    private static final Log LOG = LogFactory.getLog(DefaultDataSigner.class);

    RSAPublicKey publicKey;

    RSAPrivateKey privateKey;

    SecureRandom random;

    public byte[] generateKeyData() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        try {
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
                if (new File("/dev/urandom").exists()) {
                    byte[] salt = new byte[8192];
                    new FileInputStream("/dev/urandom").read(salt);
                    random.setSeed(salt);
                }
            } catch (Exception e) {
                LOG.fatal("Exception", e);
                random = new SecureRandom();
            }
            KeyPairGenerator keyGenerator;
            keyGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyGenerator.initialize(1024, random);
            KeyPair keyPair = keyGenerator.generateKeyPair();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            publicKey = (RSAPublicKey) keyPair.getPublic();
            oos.writeObject(publicKey.getModulus());
            oos.writeObject(publicKey.getPublicExponent());
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            oos.writeObject(privateKey.getModulus());
            oos.writeObject(privateKey.getPrivateExponent());
            oos.flush();
            oos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            LOG.error("Exception", e);
            return null;
        }
    }

    public void init(byte[] keyData) {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        try {
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
                if (new File("/dev/urandom").exists()) {
                    byte[] salt = new byte[8192];
                    new FileInputStream("/dev/urandom").read(salt);
                    random.setSeed(salt);
                }
            } catch (Exception e) {
                LOG.fatal("Exception", e);
                random = new SecureRandom();
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(keyData);
            ObjectInputStream ois = new ObjectInputStream(bis);
            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec((BigInteger) ois.readObject(), (BigInteger) ois.readObject());
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec((BigInteger) ois.readObject(), (BigInteger) ois.readObject());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            publicKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
    }

    public String signData(byte[] data) {
        try {
            Signature sign = Signature.getInstance("SHA1WithRSA", "BC");
            sign.initSign(privateKey, random);
            sign.update(data);
            return new String(Base64.encodeBase64(sign.sign()), "UTF-8");
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return null;
    }

    public boolean verifyData(byte[] data, String signature) {
        try {
            Signature sign = Signature.getInstance("SHA1WithRSA", "BC");
            sign.initVerify(publicKey);
            sign.update(data);
            return sign.verify(Base64.decodeBase64(signature.getBytes("UTF-8")));
        } catch (Exception e) {
            LOG.error("Exception", e);
        }
        return false;
    }
}
