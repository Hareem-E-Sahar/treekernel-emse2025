package net.woodstock.rockapi.security.crypt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;
import net.woodstock.rockapi.security.common.Charset;
import net.woodstock.rockapi.security.crypt.Algorithm;
import net.woodstock.rockapi.utils.Base64Utils;

public class AsyncCrypter extends CrypterBase {

    private PrivateKey privateKey;

    private PublicKey publicKey;

    protected AsyncCrypter(InputStream privateKey, InputStream publicKey, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        super(algorithm, charset);
        if (publicKey == null) {
            throw new InstantiationException("security.crypt.no-public-key");
        }
        if (privateKey != null) {
            this.privateKey = (PrivateKey) Base64Utils.unserializeFrom(privateKey);
            this.publicKey = (PublicKey) Base64Utils.unserializeFrom(publicKey);
        } else {
            this.publicKey = (PublicKey) Base64Utils.unserializeFrom(publicKey);
        }
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String encrypt(String str) throws IOException, GeneralSecurityException {
        if (this.privateKey == null) {
            throw new InvalidKeyException("security.crypt.no-private-key");
        }
        if (this.getEcipher() == null) {
            this.setEcipher(Cipher.getInstance(this.getAlgorithm().algorithm()));
            this.getEcipher().init(Cipher.ENCRYPT_MODE, this.privateKey);
        }
        byte[] bytes = str.getBytes(this.getCharset().charset());
        byte[] enc = this.getEcipher().doFinal(bytes);
        return Base64Utils.toBase64String(enc);
    }

    @Override
    public String decrypt(String str) throws IOException, GeneralSecurityException {
        if (this.getDcipher() == null) {
            this.setDcipher(Cipher.getInstance(this.getAlgorithm().algorithm()));
            this.getDcipher().init(Cipher.DECRYPT_MODE, this.publicKey);
        }
        byte[] dec = Base64Utils.fromBase64(str);
        byte[] bytes = this.getDcipher().doFinal(dec);
        return new String(bytes, this.getCharset().charset());
    }

    public static AsyncCrypter newInstance(String publicKeyFile) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, new File(publicKeyFile), Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(File publicKeyFile) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKeyFile, Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(InputStream publicKey) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKey, Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(String publicKeyFile, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, new File(publicKeyFile), algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(File publicKeyFile, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKeyFile, algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(InputStream publicKey, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKey, algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(String publicKeyFile, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, new File(publicKeyFile), Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(File publicKeyFile, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKeyFile, Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(InputStream publicKey, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKey, Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(String publicKeyFile, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, new File(publicKeyFile), algorithm, charset);
    }

    public static AsyncCrypter newInstance(File publicKeyFile, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKeyFile, algorithm, charset);
    }

    public static AsyncCrypter newInstance(InputStream publicKey, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(null, publicKey, algorithm, charset);
    }

    public static AsyncCrypter newInstance(String privateKeyFile, String publicKeyFile) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(new File(privateKeyFile), new File(publicKeyFile), Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(File privateKeyFile, File publicKeyFile) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKeyFile, publicKeyFile, Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(InputStream privateKey, InputStream publicKey) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKey, publicKey, Algorithm.DEFAULT_ASYNC, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(String privateKeyFile, String publicKeyFile, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(new File(privateKeyFile), new File(publicKeyFile), algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(File privateKeyFile, File publicKeyFile, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKeyFile, publicKeyFile, algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(InputStream privateKey, InputStream publicKey, Algorithm algorithm) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKey, publicKey, algorithm, Charset.DEFAULT);
    }

    public static AsyncCrypter newInstance(String privateKeyFile, String publicKeyFile, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(new File(privateKeyFile), new File(publicKeyFile), Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(File privateKeyFile, File publicKeyFile, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKeyFile, publicKeyFile, Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(InputStream privateKey, InputStream publicKey, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(privateKey, publicKey, Algorithm.DEFAULT_ASYNC, charset);
    }

    public static AsyncCrypter newInstance(String privateKeyFile, String publicKeyFile, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(new File(privateKeyFile), new File(publicKeyFile), algorithm, charset);
    }

    public static AsyncCrypter newInstance(File privateKey, File publicKey, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return AsyncCrypter.newInstance(new FileInputStream(privateKey), new FileInputStream(publicKey), algorithm, charset);
    }

    public static AsyncCrypter newInstance(InputStream privateKey, InputStream publicKey, Algorithm algorithm, Charset charset) throws IOException, InstantiationException, ClassNotFoundException {
        return new AsyncCrypter(privateKey, publicKey, algorithm, charset);
    }

    public static void generateKey(String privateKeyFile, String publicKeyFile) throws IOException, GeneralSecurityException {
        AsyncCrypter.generateKey(new File(privateKeyFile), new File(publicKeyFile), Algorithm.DEFAULT_ASYNC);
    }

    public static void generateKey(File privateKeyFile, File publicKeyFile) throws IOException, GeneralSecurityException {
        AsyncCrypter.generateKey(privateKeyFile, publicKeyFile, Algorithm.DEFAULT_ASYNC);
    }

    public static void generateKey(String privateKeyFile, String publicKeyFile, Algorithm algorithm) throws IOException, GeneralSecurityException {
        AsyncCrypter.generateKey(new File(privateKeyFile), new File(publicKeyFile), algorithm);
    }

    public static void generateKey(File privateKeyFile, File publicKeyFile, Algorithm algorithm) throws IOException, GeneralSecurityException {
        KeyPair key = KeyPairGenerator.getInstance(algorithm.algorithm()).generateKeyPair();
        Base64Utils.serializeTo(key.getPrivate(), privateKeyFile);
        Base64Utils.serializeTo(key.getPublic(), publicKeyFile);
    }

    public static void generateKey(OutputStream privateKeyFile, OutputStream publicKeyFile, Algorithm algorithm) throws IOException, GeneralSecurityException {
        KeyPair key = KeyPairGenerator.getInstance(algorithm.algorithm()).generateKeyPair();
        Base64Utils.serializeTo(key.getPrivate(), privateKeyFile);
        Base64Utils.serializeTo(key.getPublic(), publicKeyFile);
    }
}
