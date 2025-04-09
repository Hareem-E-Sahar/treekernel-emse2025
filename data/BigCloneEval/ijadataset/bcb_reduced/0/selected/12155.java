package wsl.licence;

import java.util.Random;
import java.util.zip.CRC32;

public class LicenceKey extends KeyBase implements java.io.Serializable {

    private int _random;

    private int _prodCode;

    private int _checksum;

    public static final int ENCODED_LENGTH = 17;

    public LicenceKey(int prodCode) {
        _prodCode = prodCode;
        Random rand = new Random(System.currentTimeMillis());
        _random = rand.nextInt();
        _checksum = genChecksum();
    }

    public LicenceKey(String data) throws Exception {
        loadKey(KeyManager.instance().decrypt(data));
    }

    public LicenceKey(int prodCode, int random) {
        _prodCode = prodCode;
        _random = random;
        _checksum = genChecksum();
    }

    private int genChecksum() {
        CRC32 crc = new CRC32();
        crc.update(_prodCode);
        crc.update(_random);
        return (int) crc.getValue();
    }

    private void loadKey(byte keyBytes[]) throws Exception {
        if (keyBytes == null || keyBytes.length < 12) throw new RuntimeException("Invalid number of bytes in keyBytes.");
        _prodCode = bytesToInt(keyBytes, 0);
        _random = bytesToInt(keyBytes, 4);
        _checksum = bytesToInt(keyBytes, 8);
    }

    public boolean verifyChecksum() {
        return _checksum == genChecksum();
    }

    protected byte[] getBytes() throws Exception {
        byte bytes[] = new byte[12];
        intToBytes(bytes, 0, _prodCode);
        intToBytes(bytes, 4, _random);
        intToBytes(bytes, 8, _checksum);
        return bytes;
    }

    public int getProductCode() {
        return _prodCode;
    }

    public boolean equals(LicenceKey l) {
        return (l._checksum == _checksum && l._prodCode == _prodCode && l._random == _random);
    }

    /**
     * Overload to force byte array structuring
     */
    public String toString() {
        String ret = "";
        try {
            byte data[] = getBytes();
            if (data != null) ret = KeyManager.instance().encrypt(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
