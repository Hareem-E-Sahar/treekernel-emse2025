package sun.security.krb5.internal.crypto;

import sun.security.krb5.Checksum;
import sun.security.krb5.Confounder;
import sun.security.krb5.KrbCryptoException;
import sun.security.krb5.internal.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.InvalidKeyException;

public final class RsaMd5DesCksumType extends CksumType {

    public RsaMd5DesCksumType() {
    }

    public int confounderSize() {
        return 8;
    }

    public int cksumType() {
        return Checksum.CKSUMTYPE_RSA_MD5_DES;
    }

    public boolean isSafe() {
        return true;
    }

    public int cksumSize() {
        return 24;
    }

    public int keyType() {
        return Krb5.KEYTYPE_DES;
    }

    public int keySize() {
        return 8;
    }

    /**
     * Calculates keyed checksum.
     * @param data the data used to generate the checksum.
     * @param size length of the data.
     * @param key the key used to encrypt the checksum.
     * @return keyed checksum.
     *
     * @modified by Yanni Zhang, 12/08/99.
     */
    public byte[] calculateKeyedChecksum(byte[] data, int size, byte[] key, int usage) throws KrbCryptoException {
        byte[] new_data = new byte[size + confounderSize()];
        byte[] conf = Confounder.bytes(confounderSize());
        System.arraycopy(conf, 0, new_data, 0, confounderSize());
        System.arraycopy(data, 0, new_data, confounderSize(), size);
        byte[] mdc_cksum = calculateChecksum(new_data, new_data.length);
        byte[] cksum = new byte[cksumSize()];
        System.arraycopy(conf, 0, cksum, 0, confounderSize());
        System.arraycopy(mdc_cksum, 0, cksum, confounderSize(), cksumSize() - confounderSize());
        byte[] new_key = new byte[keySize()];
        System.arraycopy(key, 0, new_key, 0, key.length);
        for (int i = 0; i < new_key.length; i++) new_key[i] = (byte) (new_key[i] ^ 0xf0);
        try {
            if (DESKeySpec.isWeak(new_key, 0)) {
                new_key[7] = (byte) (new_key[7] ^ 0xF0);
            }
        } catch (InvalidKeyException ex) {
        }
        byte[] ivec = new byte[new_key.length];
        byte[] enc_cksum = new byte[cksum.length];
        Des.cbc_encrypt(cksum, enc_cksum, new_key, ivec, true);
        return enc_cksum;
    }

    /**
     * Verifies keyed checksum.
     * @param data the data.
     * @param size the length of data.
     * @param key the key used to encrypt the checksum.
     * @param checksum
     * @return true if verification is successful.
     *
     * @modified by Yanni Zhang, 12/08/99.
     */
    public boolean verifyKeyedChecksum(byte[] data, int size, byte[] key, byte[] checksum, int usage) throws KrbCryptoException {
        byte[] cksum = decryptKeyedChecksum(checksum, key);
        byte[] new_data = new byte[size + confounderSize()];
        System.arraycopy(cksum, 0, new_data, 0, confounderSize());
        System.arraycopy(data, 0, new_data, confounderSize(), size);
        byte[] new_cksum = calculateChecksum(new_data, new_data.length);
        byte[] orig_cksum = new byte[cksumSize() - confounderSize()];
        System.arraycopy(cksum, confounderSize(), orig_cksum, 0, cksumSize() - confounderSize());
        return isChecksumEqual(orig_cksum, new_cksum);
    }

    /**
     * Decrypts keyed checksum.
     * @param enc_cksum the buffer for encrypted checksum.
     * @param key the key.
     * @return the checksum.
     *
     * @modified by Yanni Zhang, 12/08/99.
     */
    private byte[] decryptKeyedChecksum(byte[] enc_cksum, byte[] key) throws KrbCryptoException {
        byte[] new_key = new byte[keySize()];
        System.arraycopy(key, 0, new_key, 0, key.length);
        for (int i = 0; i < new_key.length; i++) new_key[i] = (byte) (new_key[i] ^ 0xf0);
        try {
            if (DESKeySpec.isWeak(new_key, 0)) {
                new_key[7] = (byte) (new_key[7] ^ 0xF0);
            }
        } catch (InvalidKeyException ex) {
        }
        byte[] ivec = new byte[new_key.length];
        byte[] cksum = new byte[enc_cksum.length];
        Des.cbc_encrypt(enc_cksum, cksum, new_key, ivec, false);
        return cksum;
    }

    /**
     * Calculates checksum using MD5.
     * @param data the data used to generate the checksum.
     * @param size length of the data.
     * @return the checksum.
     *
     * @modified by Yanni Zhang, 12/08/99.
     */
    public byte[] calculateChecksum(byte[] data, int size) throws KrbCryptoException {
        MessageDigest md5;
        byte[] result = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new KrbCryptoException("JCE provider may not be installed. " + e.getMessage());
        }
        try {
            md5.update(data);
            result = md5.digest();
        } catch (Exception e) {
            throw new KrbCryptoException(e.getMessage());
        }
        return result;
    }
}
