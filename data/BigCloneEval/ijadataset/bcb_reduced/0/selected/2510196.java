package net.laubenberger.bogatyr.service.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.laubenberger.bogatyr.helper.HelperArray;
import net.laubenberger.bogatyr.helper.HelperCrypto;
import net.laubenberger.bogatyr.helper.HelperEnvironment;
import net.laubenberger.bogatyr.helper.HelperLog;
import net.laubenberger.bogatyr.helper.HelperObject;
import net.laubenberger.bogatyr.misc.Constants;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionExceedsVmMemory;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsEmpty;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsEquals;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsNull;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionMustBeGreater;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionMustBeSmaller;
import net.laubenberger.bogatyr.model.crypto.CryptoSymmetricAlgo;
import net.laubenberger.bogatyr.model.crypto.HashCodeAlgo;
import net.laubenberger.bogatyr.service.ServiceAbstract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for symmetric cryptology via AES.
  *
 * @author Stefan Laubenberger
 * @version 0.9.6 (20110601)
 * @since 0.1.0
 */
public class CryptoSymmetricImpl extends ServiceAbstract implements CryptoSymmetric {

    private static final Logger log = LoggerFactory.getLogger(CryptoSymmetricImpl.class);

    private final CryptoSymmetricAlgo algorithm;

    private final Cipher cipher;

    private final KeyGenerator kg;

    private final HashCodeGenerator hcg;

    public CryptoSymmetricImpl(final Provider provider, final CryptoSymmetricAlgo algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
        super();
        if (log.isTraceEnabled()) log.trace(HelperLog.constructor(provider, algorithm));
        if (null == provider) {
            throw new RuntimeExceptionIsNull("provider");
        }
        if (null == algorithm) {
            throw new RuntimeExceptionIsNull("algorithm");
        }
        this.algorithm = algorithm;
        cipher = Cipher.getInstance(algorithm.getXform(), provider);
        kg = KeyGenerator.getInstance(algorithm.getAlgorithm(), provider);
        hcg = new HashCodeGeneratorImpl(HashCodeAlgo.SHA512);
    }

    public CryptoSymmetricImpl(final CryptoSymmetricAlgo algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this(HelperCrypto.DEFAULT_PROVIDER, algorithm);
    }

    private AlgorithmParameterSpec prepareIv() {
        if (log.isTraceEnabled()) log.trace(HelperLog.methodStart());
        final byte[] ivBytes = new byte[algorithm.getIvSize()];
        for (int ii = 0; algorithm.getIvSize() > ii; ii++) {
            ivBytes[ii] = (byte) 0x5a;
        }
        final AlgorithmParameterSpec result = new IvParameterSpec(ivBytes);
        if (log.isTraceEnabled()) log.trace(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Generates a {@link SecretKey} with the {@link CryptoSymmetricAlgo} standard key size.
	 *
	 * @return generated secret key
	 * @see SecretKey
	 * @see CryptoSymmetricAlgo
	 * @since 0.1.0
	 */
    @Override
    public SecretKey generateKey() {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart());
        final SecretKey result = generateKey(algorithm.getDefaultKeysize());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public SecretKey generateKey(final int keySize) {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(keySize));
        if (0 >= keySize) {
            throw new RuntimeExceptionMustBeGreater("keySize", keySize, 0);
        }
        if (0 != keySize % 8) {
            throw new IllegalArgumentException("keySize is not a multiple of 8");
        }
        kg.init(keySize);
        final SecretKey result = kg.generateKey();
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public SecretKey generateKey(final byte... password) {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(password));
        final SecretKey result = generateKey(password, algorithm.getDefaultKeysize());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public SecretKey generateKey(final byte[] password, final int keySize) {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(password, keySize));
        if (null == password) {
            throw new RuntimeExceptionIsNull("password");
        }
        if (!HelperArray.isValid(password)) {
            throw new RuntimeExceptionIsEmpty("password");
        }
        if (0 >= keySize) {
            throw new RuntimeExceptionMustBeGreater("keySize", keySize, 0);
        }
        if (512 < keySize) {
            throw new RuntimeExceptionMustBeSmaller("keySize", keySize, 512);
        }
        if (0 != keySize % 8) {
            throw new IllegalArgumentException("keySize is not a multiple of 8");
        }
        final SecretKey result = new SecretKeySpec(Arrays.copyOfRange(hcg.getHash(password), 0, keySize / 8), algorithm.getAlgorithm());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public byte[] encrypt(final byte[] input, final Key key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, key));
        if (null == input) {
            throw new RuntimeExceptionIsNull("input");
        }
        if (!HelperArray.isValid(input)) {
            throw new RuntimeExceptionIsEmpty("input");
        }
        if (null == key) {
            throw new RuntimeExceptionIsNull("key");
        }
        if (input.length * 2 > HelperEnvironment.getMemoryFree()) {
            throw new RuntimeExceptionExceedsVmMemory("input", input.length * 2);
        }
        cipher.init(Cipher.ENCRYPT_MODE, key, prepareIv());
        final byte[] result = cipher.doFinal(input);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public byte[] decrypt(final byte[] input, final Key key) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, key));
        if (null == input) {
            throw new RuntimeExceptionIsNull("input");
        }
        if (!HelperArray.isValid(input)) {
            throw new RuntimeExceptionIsEmpty("input");
        }
        if (null == key) {
            throw new RuntimeExceptionIsNull("key");
        }
        if (input.length * 2 > HelperEnvironment.getMemoryFree()) {
            throw new RuntimeExceptionExceedsVmMemory("input", input.length * 2);
        }
        cipher.init(Cipher.DECRYPT_MODE, key, prepareIv());
        final byte[] result = cipher.doFinal(input);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    @Override
    public void encrypt(final InputStream is, final OutputStream os, final Key key) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, os, key));
        encrypt(is, os, key, Constants.DEFAULT_FILE_BUFFER_SIZE);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void encrypt(final InputStream is, OutputStream os, final Key key, final int bufferSize) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, os, key, bufferSize));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        if (null == os) {
            throw new RuntimeExceptionIsNull("os");
        }
        if (null == key) {
            throw new RuntimeExceptionIsNull("key");
        }
        if (1 > bufferSize) {
            throw new RuntimeExceptionMustBeGreater("bufferSize", bufferSize, 1);
        }
        if (bufferSize > HelperEnvironment.getMemoryFree()) {
            throw new RuntimeExceptionExceedsVmMemory("bufferSize", bufferSize);
        }
        final byte[] buffer = new byte[bufferSize];
        cipher.init(Cipher.ENCRYPT_MODE, key, prepareIv());
        os = new CipherOutputStream(os, cipher);
        try {
            int offset;
            while (0 <= (offset = is.read(buffer))) {
                os.write(buffer, 0, offset);
            }
        } finally {
            os.close();
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void decrypt(final InputStream is, final OutputStream os, final Key key) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, os, key));
        decrypt(is, os, key, Constants.DEFAULT_FILE_BUFFER_SIZE);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void decrypt(final InputStream is, final OutputStream os, final Key key, final int bufferSize) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, os, key, bufferSize));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        if (null == os) {
            throw new RuntimeExceptionIsNull("os");
        }
        if (null == key) {
            throw new RuntimeExceptionIsNull("key");
        }
        if (1 > bufferSize) {
            throw new RuntimeExceptionMustBeGreater("bufferSize", bufferSize, 1);
        }
        if (bufferSize > HelperEnvironment.getMemoryFree()) {
            throw new RuntimeExceptionExceedsVmMemory("bufferSize", bufferSize);
        }
        final byte[] buffer = new byte[bufferSize];
        CipherInputStream cis = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, prepareIv());
            cis = new CipherInputStream(is, cipher);
            int offset;
            while (0 <= (offset = cis.read(buffer))) {
                os.write(buffer, 0, offset);
            }
        } finally {
            os.close();
            if (null != cis) {
                cis.close();
            }
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void encrypt(final File input, final File output, final Key key) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, output, key));
        encrypt(input, output, key, Constants.DEFAULT_FILE_BUFFER_SIZE);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void encrypt(final File input, final File output, final Key key, final int bufferSize) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, output, key, bufferSize));
        if (null == input) {
            throw new RuntimeExceptionIsNull("input");
        }
        if (null == output) {
            throw new RuntimeExceptionIsNull("output");
        }
        if (HelperObject.isEquals(input, output)) {
            throw new RuntimeExceptionIsEquals("input", "output");
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(input));
            bos = new BufferedOutputStream(new FileOutputStream(output));
            encrypt(bis, bos, key, bufferSize);
        } finally {
            if (null != bos) {
                bos.close();
            }
            if (null != bis) {
                bis.close();
            }
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void decrypt(final File input, final File output, final Key key) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, output, key));
        decrypt(input, output, key, Constants.DEFAULT_FILE_BUFFER_SIZE);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    @Override
    public void decrypt(final File input, final File output, final Key key, final int bufferSize) throws InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(input, output, key, bufferSize));
        if (null == input) {
            throw new RuntimeExceptionIsNull("input");
        }
        if (null == output) {
            throw new RuntimeExceptionIsNull("output");
        }
        if (HelperObject.isEquals(input, output)) {
            throw new RuntimeExceptionIsEquals("input", "output");
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(input));
            bos = new BufferedOutputStream(new FileOutputStream(output));
            decrypt(bis, bos, key, bufferSize);
        } finally {
            if (null != bos) {
                bos.close();
            }
            if (null != bis) {
                bis.close();
            }
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }
}
