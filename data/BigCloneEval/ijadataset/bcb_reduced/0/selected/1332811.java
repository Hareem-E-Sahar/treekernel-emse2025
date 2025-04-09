package es.caib.signatura.provider.impl.mscryptoapi.mscrypto;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import es.caib.signatura.impl.SigDebug;

public final class MSRSACipherFactoryImpl extends CipherSpi {

    static String PaddingAlgorithm = "PKCS1";

    static int ciphermode = 0;

    static MSCryptoFunctions MSF = new MSCryptoFunctions();

    static int KeySize = MSF.MSrsaGetKeysize() / 8;

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineDoFinal entered");
        byte[] outputData = null;
        buffer.write(input, inputOffset, inputLen);
        byte[] inputData = buffer.toByteArray();
        if (ciphermode == Cipher.DECRYPT_MODE) {
            if (KeySize != inputData.length) throw new IllegalBlockSizeException("MSRSA length of data to be decrypted must equal keysize " + KeySize + "  " + inputData.length);
            outputData = MSF.MSrsaDecrypt(PaddingAlgorithm, inputData);
        }
        if (ciphermode == Cipher.ENCRYPT_MODE) {
            if (KeySize < inputData.length) throw new IllegalBlockSizeException("MSRSA length of data to be decrypted must be <= keysize " + KeySize + "  " + inputData.length);
            outputData = MSF.MSrsaEncrypt(PaddingAlgorithm, inputData);
        }
        buffer.reset();
        return outputData;
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineDoFinal entered");
        byte[] outputData = engineDoFinal(input, inputOffset, inputLen);
        System.arraycopy(outputData, 0, output, outputOffset, outputData.length);
        return outputData.length;
    }

    protected int engineGetBlockSize() {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineGetBlockSize entered");
        return KeySize;
    }

    protected byte[] engineGetIV() {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineGetIV entered");
        return null;
    }

    protected int engineGetKeySize(Key key) {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineGetKeySize entered");
        return KeySize;
    }

    protected int engineGetOutputSize(int inputLen) {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineOutputSize entered");
        return KeySize;
    }

    protected AlgorithmParameters engineGetParameters() {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineGetParameters entered");
        return null;
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException, InvalidKeyException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineInit entered");
        engineInit(opmode, key, random);
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineInit entered");
        buffer.reset();
        throw new InvalidAlgorithmParameterException("MSRSA does not accept AlgorithmParameterSpec");
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineInit entered");
        buffer.reset();
        if (opmode != Cipher.ENCRYPT_MODE && opmode != Cipher.DECRYPT_MODE) throw new InvalidKeyException("MSRSA opmode must be either encrypt or decrypt");
        ciphermode = opmode;
    }

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineSetMode entered");
        if (!mode.equalsIgnoreCase("ECB")) {
            throw new NoSuchAlgorithmException("MSRSA supports only ECB mode");
        }
    }

    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineSetPadding entered");
        if (padding.substring(0, 5).equalsIgnoreCase("PKCS1")) {
            PaddingAlgorithm = "PKCS1";
        } else {
            throw new NoSuchPaddingException("MSRSA only supports PKCS1 Padding (" + padding + ")");
        }
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineUpdate entered");
        buffer.write(input, inputOffset, inputLen);
        return null;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        if (SigDebug.isActive()) SigDebug.write("MSRSACipherFactoryImpl: engineUpdate entered");
        return 0;
    }
}
