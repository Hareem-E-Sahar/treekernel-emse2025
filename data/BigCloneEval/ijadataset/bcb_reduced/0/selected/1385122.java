package wsl.licence;

import java.util.Random;
import java.util.zip.CRC32;

public class ActivationKey extends KeyBase implements java.io.Serializable {

    private int _prodCode;

    private short _numUsers;

    private int _expiry;

    private int _checksum;

    private LicenceKey _key;

    public static final int ENCODED_LENGTH = 21;

    public static final int PERM_KEY = 0;

    public ActivationKey(int prodCode, short numUsers, int expiry, LicenceKey encryptKey) {
        _prodCode = prodCode;
        _numUsers = numUsers;
        _expiry = expiry;
        _checksum = genChecksum();
        _key = encryptKey;
    }

    public static void main(String args[]) {
        try {
            ActivationKey ak = new ActivationKey("RE1NRJs7+n7dJTDXTUkB", "dWJ+avlqbFGevMEbA");
            if (ak.verifyChecksum()) System.out.println("Valid key"); else System.out.println("Invalid key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActivationKey(String activationKey, String licenceKey) throws Exception {
        _key = new LicenceKey(licenceKey);
        loadKey(KeyManager.instance().decrypt(activationKey, _key.getBytes()));
    }

    private int genChecksum() {
        CRC32 crc = new CRC32();
        crc.update(_prodCode);
        crc.update((int) _numUsers);
        crc.update(_expiry);
        return (int) crc.getValue();
    }

    private void loadKey(byte keyBytes[]) throws Exception {
        if (keyBytes == null || keyBytes.length < 14) throw new RuntimeException("Invalid number of bytes in keyBytes.");
        _prodCode = bytesToInt(keyBytes, 0);
        _numUsers = bytesToShort(keyBytes, 4);
        _expiry = bytesToInt(keyBytes, 6);
        _checksum = bytesToInt(keyBytes, 10);
    }

    public boolean verifyChecksum() {
        return _checksum == genChecksum();
    }

    protected byte[] getBytes() throws Exception {
        byte bytes[] = new byte[14];
        intToBytes(bytes, 0, _prodCode);
        shortToBytes(bytes, 4, _numUsers);
        intToBytes(bytes, 6, _expiry);
        intToBytes(bytes, 10, _checksum);
        return bytes;
    }

    public int getProductCode() {
        return _prodCode;
    }

    public short getNumUsers() {
        return _numUsers;
    }

    public int getExpiry() {
        return _expiry;
    }

    public boolean equals(ActivationKey key) {
        return (key._checksum == _checksum && key._prodCode == _prodCode && key._numUsers == _numUsers && key._expiry == _expiry);
    }

    public byte[] getKeyBytes() throws Exception {
        if (_key == null) throw new RuntimeException("Invalid licence key in activation key");
        return _key.getBytes();
    }

    /**
     * Overload to force byte array structuring
     */
    public String toString() {
        String ret = "";
        try {
            byte data[] = getBytes();
            byte key[] = getKeyBytes();
            if (data != null && key != null) ret = KeyManager.instance().encrypt(data, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void setNumUsers(short users) {
        _numUsers = users;
    }

    public void setExpiry(int _expiry) {
        this._expiry = _expiry;
    }
}
