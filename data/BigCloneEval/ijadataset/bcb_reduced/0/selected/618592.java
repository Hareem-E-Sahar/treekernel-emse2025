package org.jscep.transaction;

import org.jscep.util.HexUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class represents the SCEP <code>transactionID</code> attribute.
 * 
 * @author David Grant
 */
public final class TransactionId {

    private static final AtomicLong ID_SOURCE = new AtomicLong();

    private final byte[] id;

    public TransactionId(byte[] id) {
        this.id = copy(id);
    }

    private TransactionId(PublicKey pubKey, String digestAlgorithm) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        id = HexUtil.toHex(digest.digest(pubKey.getEncoded()));
    }

    private TransactionId() {
        id = Long.toHexString(ID_SOURCE.getAndIncrement()).getBytes();
    }

    public byte[] getBytes() {
        return copy(id);
    }

    /**
	 * Creates a new Transaction Id
	 * <p>
	 * Each call to this method will return the same transaction ID for the same parameters.
	 *
     * @param pubKey public key
     * @param digestAlgorithm digest algorithm
	 * @return the new Transaction Id
	 */
    public static TransactionId createTransactionId(PublicKey pubKey, String digestAlgorithm) {
        return new TransactionId(pubKey, digestAlgorithm);
    }

    /**
	 * Creates a new Transaction Id
	 * <p>
	 * Each call to this method will return a different transaction ID.
	 * 
	 * @return the new Transaction Id
	 */
    public static TransactionId createTransactionId() {
        return new TransactionId();
    }

    @Override
    public String toString() {
        return new String(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
        return Arrays.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Arrays.hashCode(id) : 0;
    }

    private static byte[] copy(byte[] source) {
        byte[] dest = new byte[source.length];
        System.arraycopy(source, 0, dest, 0, source.length);
        return dest;
    }
}
