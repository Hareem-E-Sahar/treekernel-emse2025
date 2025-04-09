package net.sf.securejdms.keymanagement.core.extensions;

import java.util.zip.CRC32;
import net.sf.securejdms.common.core.extensionpoints.CryptographicException;
import net.sf.securejdms.common.core.extensionpoints.ISecurityProvider;
import net.sf.securejdms.keymanagement.core.CSPManager;
import net.sf.securejdms.keymanagement.core.KeyCache;
import net.sf.securejdms.keymanagement.core.entities.BlockCipherBL;
import net.sf.securejdms.keymanagement.core.entities.KeyBL;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link ISecurityProvider}
 * 
 * @author Boris Brodski
 */
public class SecurityProvider implements ISecurityProvider {

    private static final Logger log = Logger.getLogger(SecurityProvider.class);

    public SecurityProvider() {
    }

    byte[] tmpData;

    /**
	 * {@inheritDoc}
	 */
    @Override
    public byte[] encrypt(byte[] data, byte[] keySignature) throws CryptographicException {
        tmpData = new byte[data.length];
        System.arraycopy(data, 0, tmpData, 0, data.length);
        KeyBL key = KeyCache.searchKey(keySignature);
        if (key == null) {
            return null;
        }
        int dataSize = data.length;
        if (log.isTraceEnabled()) {
            log.trace("Encrypting " + dataSize + " bytes with CSP " + key.getKeyCSP());
        }
        BlockCipherBL blockCipher = CSPManager.getBlockCipher(key.getKeyCSP());
        if (blockCipher == null) {
            throw new CryptographicException("Cryptographic service provider " + key.getKeyCSP() + " couldn't be found");
        }
        int blockSize = blockCipher.getBlockSize();
        int cryptedDataSize = dataSize + 8;
        int cryptedDataBlockCount = (cryptedDataSize + blockSize - 1) / blockSize;
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        int crcValue = (int) crc32.getValue();
        byte[] cryptedData = new byte[blockSize * cryptedDataBlockCount];
        System.arraycopy(data, 0, cryptedData, 0, dataSize);
        intToBytes(cryptedData, cryptedData.length - 8, dataSize);
        intToBytes(cryptedData, cryptedData.length - 4, crcValue);
        blockCipher.getBlockCipher().encrypt(cryptedData, 0, blockSize, cryptedDataBlockCount, key.getKey());
        return cryptedData;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public byte[] decrypt(byte[] cryptedData, byte[] keySignature) throws CryptographicException {
        KeyBL key = KeyCache.searchKey(keySignature);
        if (key == null) {
            return null;
        }
        BlockCipherBL blockCipher = CSPManager.getBlockCipher(key.getKeyCSP());
        int blockSize = blockCipher.getBlockSize();
        int cryptedDataSize = cryptedData.length;
        if (cryptedDataSize == 0 || cryptedDataSize % blockSize != 0) {
            throw new CryptographicException("Incompatible crypted data size (" + cryptedDataSize + ") or block cipher block size (" + blockSize + ")");
        }
        if (blockSize <= 8) {
            throw new CryptographicException("Unsupported block type: " + blockSize + " <= 8");
        }
        byte[] lastBlocks;
        if (cryptedData.length == blockSize) {
            lastBlocks = new byte[blockSize];
        } else {
            lastBlocks = new byte[2 * blockSize];
        }
        System.arraycopy(cryptedData, cryptedData.length - lastBlocks.length, lastBlocks, 0, lastBlocks.length);
        blockCipher.getBlockCipher().decrypt(lastBlocks, 0, blockSize, lastBlocks.length / blockSize, key.getKey());
        int dataSize = bytesToInt(lastBlocks, lastBlocks.length - 8);
        int crcValue = bytesToInt(lastBlocks, lastBlocks.length - 4);
        if (log.isTraceEnabled()) {
            log.trace("Decrypting " + dataSize + " bytes with CSP " + key.getKeyCSP());
        }
        if (dataSize > cryptedDataSize) {
            log.error("Decryption error");
            throw new RuntimeException("Decryption error");
        }
        byte[] data = new byte[dataSize];
        int fullDataBlockCount = data.length / blockSize;
        int dataInLastBlock = data.length % blockSize;
        if (cryptedData.length != blockSize && fullDataBlockCount == cryptedDataSize / blockSize - 1) {
            fullDataBlockCount--;
            dataInLastBlock += blockSize;
        }
        System.arraycopy(cryptedData, 0, data, 0, fullDataBlockCount * blockSize);
        blockCipher.getBlockCipher().decrypt(data, 0, blockSize, fullDataBlockCount, key.getKey());
        System.arraycopy(lastBlocks, 0, data, fullDataBlockCount * blockSize, dataInLastBlock);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        if ((int) crc32.getValue() != crcValue) {
            throw new CryptographicException("CRC error after decryption. (should: " + crcValue + ", is: " + (int) crc32.getValue() + ")");
        }
        return data;
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public boolean possessKey(byte[] keySignature) {
        return KeyCache.searchKey(keySignature) != null;
    }

    private void intToBytes(byte[] byteArray, int position, int value) {
        byteArray[position] = (byte) (value & 0xFF);
        byteArray[position + 1] = (byte) ((value >> 8) & 0xFF);
        byteArray[position + 2] = (byte) ((value >> 16) & 0xFF);
        byteArray[position + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private int bytesToInt(byte[] byteArray, int position) {
        return (0xFF & (int) byteArray[position]) | (0xFF00 & (((int) byteArray[position + 1]) << 8)) | (0xFF0000 & (((int) byteArray[position + 2]) << 16)) | (0xFF000000 & (((int) byteArray[position + 3]) << 24));
    }
}
