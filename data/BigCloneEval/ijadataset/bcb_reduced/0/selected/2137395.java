package net.java.otr4j.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import net.java.otr4j.io.SerializationUtils;
import org.bouncycastle2.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle2.crypto.BufferedBlockCipher;
import org.bouncycastle2.crypto.engines.AESFastEngine;
import org.bouncycastle2.crypto.generators.DHKeyPairGenerator;
import org.bouncycastle2.crypto.modes.SICBlockCipher;
import org.bouncycastle2.crypto.params.DHKeyGenerationParameters;
import org.bouncycastle2.crypto.params.DHParameters;
import org.bouncycastle2.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle2.crypto.params.DHPublicKeyParameters;
import org.bouncycastle2.crypto.params.DSAParameters;
import org.bouncycastle2.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle2.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle2.crypto.params.KeyParameter;
import org.bouncycastle2.crypto.params.ParametersWithIV;
import org.bouncycastle2.crypto.signers.DSASigner;
import org.bouncycastle2.util.BigIntegers;

/**
 * @author George Politis
 */
public class OtrCryptoEngineImpl implements OtrCryptoEngine {

    public KeyPair generateDHKeyPair() throws OtrCryptoException {
        DHParameters dhParams = new DHParameters(MODULUS, GENERATOR, null, DH_PRIVATE_KEY_MINIMUM_BIT_LENGTH);
        DHKeyGenerationParameters params = new DHKeyGenerationParameters(new SecureRandom(), dhParams);
        DHKeyPairGenerator kpGen = new DHKeyPairGenerator();
        kpGen.init(params);
        AsymmetricCipherKeyPair pair = kpGen.generateKeyPair();
        DHPublicKeyParameters pub = (DHPublicKeyParameters) pair.getPublic();
        DHPrivateKeyParameters priv = (DHPrivateKeyParameters) pair.getPrivate();
        try {
            KeyFactory keyFac = KeyFactory.getInstance("DH");
            DHPublicKeySpec pubKeySpecs = new DHPublicKeySpec(pub.getY(), MODULUS, GENERATOR);
            DHPublicKey pubKey = (DHPublicKey) keyFac.generatePublic(pubKeySpecs);
            DHParameters dhParameters = priv.getParameters();
            DHPrivateKeySpec privKeySpecs = new DHPrivateKeySpec(priv.getX(), dhParameters.getP(), dhParameters.getG());
            DHPrivateKey privKey = (DHPrivateKey) keyFac.generatePrivate(privKeySpecs);
            return new KeyPair(pubKey, privKey);
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public DHPublicKey getDHPublicKey(byte[] mpiBytes) throws OtrCryptoException {
        return getDHPublicKey(new BigInteger(mpiBytes));
    }

    public DHPublicKey getDHPublicKey(BigInteger mpi) throws OtrCryptoException {
        DHPublicKeySpec pubKeySpecs = new DHPublicKeySpec(mpi, MODULUS, GENERATOR);
        try {
            KeyFactory keyFac = KeyFactory.getInstance("DH");
            return (DHPublicKey) keyFac.generatePublic(pubKeySpecs);
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public byte[] sha256Hmac(byte[] b, byte[] key) throws OtrCryptoException {
        return this.sha256Hmac(b, key, 0);
    }

    public byte[] sha256Hmac(byte[] b, byte[] key, int length) throws OtrCryptoException {
        SecretKeySpec keyspec = new SecretKeySpec(key, "HmacSHA256");
        javax.crypto.Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new OtrCryptoException(e);
        }
        try {
            mac.init(keyspec);
        } catch (InvalidKeyException e) {
            throw new OtrCryptoException(e);
        }
        byte[] macBytes = mac.doFinal(b);
        if (length > 0) {
            byte[] bytes = new byte[length];
            ByteBuffer buff = ByteBuffer.wrap(macBytes);
            buff.get(bytes);
            return bytes;
        } else {
            return macBytes;
        }
    }

    public byte[] sha1Hmac(byte[] b, byte[] key, int length) throws OtrCryptoException {
        try {
            SecretKeySpec keyspec = new SecretKeySpec(key, "HmacSHA1");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(keyspec);
            byte[] macBytes = mac.doFinal(b);
            if (length > 0) {
                byte[] bytes = new byte[length];
                ByteBuffer buff = ByteBuffer.wrap(macBytes);
                buff.get(bytes);
                return bytes;
            } else {
                return macBytes;
            }
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public byte[] sha256Hmac160(byte[] b, byte[] key) throws OtrCryptoException {
        return sha256Hmac(b, key, 20);
    }

    public byte[] sha256Hash(byte[] b) throws OtrCryptoException {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(b, 0, b.length);
            return sha256.digest();
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public byte[] sha1Hash(byte[] b) throws OtrCryptoException {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-1");
            sha256.update(b, 0, b.length);
            return sha256.digest();
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public byte[] aesDecrypt(byte[] key, byte[] ctr, byte[] b) throws OtrCryptoException {
        AESFastEngine aesDec = new AESFastEngine();
        SICBlockCipher sicAesDec = new SICBlockCipher(aesDec);
        BufferedBlockCipher bufSicAesDec = new BufferedBlockCipher(sicAesDec);
        if (ctr == null) ctr = ZERO_CTR;
        bufSicAesDec.init(false, new ParametersWithIV(new KeyParameter(key), ctr));
        byte[] aesOutLwDec = new byte[b.length];
        int done = bufSicAesDec.processBytes(b, 0, b.length, aesOutLwDec, 0);
        try {
            bufSicAesDec.doFinal(aesOutLwDec, done);
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
        return aesOutLwDec;
    }

    public byte[] aesEncrypt(byte[] key, byte[] ctr, byte[] b) throws OtrCryptoException {
        AESFastEngine aesEnc = new AESFastEngine();
        SICBlockCipher sicAesEnc = new SICBlockCipher(aesEnc);
        BufferedBlockCipher bufSicAesEnc = new BufferedBlockCipher(sicAesEnc);
        if (ctr == null) ctr = ZERO_CTR;
        bufSicAesEnc.init(true, new ParametersWithIV(new KeyParameter(key), ctr));
        byte[] aesOutLwEnc = new byte[b.length];
        int done = bufSicAesEnc.processBytes(b, 0, b.length, aesOutLwEnc, 0);
        try {
            bufSicAesEnc.doFinal(aesOutLwEnc, done);
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
        return aesOutLwEnc;
    }

    public BigInteger generateSecret(PrivateKey privKey, PublicKey pubKey) throws OtrCryptoException {
        try {
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(privKey);
            ka.doPhase(pubKey, true);
            byte[] sb = ka.generateSecret();
            BigInteger s = new BigInteger(1, sb);
            return s;
        } catch (Exception e) {
            throw new OtrCryptoException(e);
        }
    }

    public byte[] sign(byte[] b, PrivateKey privatekey) throws OtrCryptoException {
        if (!(privatekey instanceof DSAPrivateKey)) throw new IllegalArgumentException();
        DSAParams dsaParams = ((DSAPrivateKey) privatekey).getParams();
        DSAParameters bcDSAParameters = new DSAParameters(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG());
        DSAPrivateKey dsaPrivateKey = (DSAPrivateKey) privatekey;
        DSAPrivateKeyParameters bcDSAPrivateKeyParms = new DSAPrivateKeyParameters(dsaPrivateKey.getX(), bcDSAParameters);
        DSASigner dsaSigner = new DSASigner();
        dsaSigner.init(true, bcDSAPrivateKeyParms);
        BigInteger q = dsaParams.getQ();
        BigInteger bmpi = new BigInteger(1, b);
        BigInteger[] rs = dsaSigner.generateSignature(BigIntegers.asUnsignedByteArray(bmpi.mod(q)));
        int siglen = q.bitLength() / 4;
        int rslen = siglen / 2;
        byte[] rb = BigIntegers.asUnsignedByteArray(rs[0]);
        byte[] sb = BigIntegers.asUnsignedByteArray(rs[1]);
        byte[] sig = new byte[siglen];
        Boolean writeR = false;
        Boolean writeS = false;
        for (int i = 0; i < siglen; i++) {
            if (i < rslen) {
                if (!writeR) writeR = rb.length >= rslen - i;
                sig[i] = (writeR) ? rb[i] : (byte) 0x0;
            } else {
                int j = i - rslen;
                if (!writeS) writeS = sb.length >= rslen - j;
                sig[i] = (writeS) ? sb[j] : (byte) 0x0;
            }
        }
        return sig;
    }

    public boolean verify(byte[] b, PublicKey pubKey, byte[] rs) throws OtrCryptoException {
        if (!(pubKey instanceof DSAPublicKey)) throw new IllegalArgumentException();
        DSAParams dsaParams = ((DSAPublicKey) pubKey).getParams();
        int qlen = dsaParams.getQ().bitLength() / 8;
        ByteBuffer buff = ByteBuffer.wrap(rs);
        byte[] r = new byte[qlen];
        buff.get(r);
        byte[] s = new byte[qlen];
        buff.get(s);
        return verify(b, pubKey, r, s);
    }

    private Boolean verify(byte[] b, PublicKey pubKey, byte[] r, byte[] s) throws OtrCryptoException {
        Boolean result = verify(b, pubKey, new BigInteger(1, r), new BigInteger(1, s));
        return result;
    }

    private Boolean verify(byte[] b, PublicKey pubKey, BigInteger r, BigInteger s) throws OtrCryptoException {
        if (!(pubKey instanceof DSAPublicKey)) throw new IllegalArgumentException();
        DSAParams dsaParams = ((DSAPublicKey) pubKey).getParams();
        BigInteger q = dsaParams.getQ();
        DSAParameters bcDSAParams = new DSAParameters(dsaParams.getP(), q, dsaParams.getG());
        DSAPublicKey dsaPrivateKey = (DSAPublicKey) pubKey;
        DSAPublicKeyParameters dsaPrivParms = new DSAPublicKeyParameters(dsaPrivateKey.getY(), bcDSAParams);
        DSASigner dsaSigner = new DSASigner();
        dsaSigner.init(false, dsaPrivParms);
        BigInteger bmpi = new BigInteger(1, b);
        Boolean result = dsaSigner.verifySignature(BigIntegers.asUnsignedByteArray(bmpi.mod(q)), r, s);
        return result;
    }

    public String getFingerprint(PublicKey pubKey) throws OtrCryptoException {
        byte[] b;
        try {
            byte[] bRemotePubKey = SerializationUtils.writePublicKey(pubKey);
            if (pubKey.getAlgorithm().equals("DSA")) {
                byte[] trimmed = new byte[bRemotePubKey.length - 2];
                System.arraycopy(bRemotePubKey, 2, trimmed, 0, trimmed.length);
                b = new OtrCryptoEngineImpl().sha1Hash(trimmed);
            } else b = new OtrCryptoEngineImpl().sha1Hash(bRemotePubKey);
        } catch (IOException e) {
            throw new OtrCryptoException(e);
        }
        return this.byteArrayToHexString(b);
    }

    private String byteArrayToHexString(byte in[]) {
        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0) return null;
        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
        StringBuffer out = new StringBuffer(in.length * 2);
        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[(int) ch]);
            ch = (byte) (in[i] & 0x0F);
            out.append(pseudo[(int) ch]);
            i++;
        }
        String rslt = new String(out);
        return rslt;
    }
}
