package cryptix.sasl.srp;

import java.math.BigInteger;
import java.security.MessageDigest;
import cryptix.sasl.SaslUtil;
import org.apache.log4j.Category;

/**
 * The SRP protocol does not disallow the use of digest algorithms other than
 * SHA. This object encapsulates the concrete implementation of the Message
 * Digest Algorithm instance used by an incarnation of an SRP agreement.
 *
 * @version $Revision: 1.3 $
 * @since draft-burdis-cat-sasl-srp-04
 */
final class SRPDigest {

    private static Category cat = Category.getInstance(SRPDigest.class);

    /**
	 * The size of the output (in bytes) of the underlying SRP message digest
	 * algorithm.
	 */
    private int digestLength;

    /**
	 * The root of all instances of the underlying SRP message digest
	 * algorithm.<p>
	 *
	 * In this code of the SRP protocol we use Sun's SHA implementation.
	 * In later version this will change to use any provider's
	 * implementation but we shall add some verification tests (known
	 * test vectors) to ensure it is reliable.
	 */
    private MessageDigest md;

    /**
	 * Trivial private constructor to enforce Singleton pattern.
	 */
    private SRPDigest(MessageDigest md) {
        super();
        this.md = md;
        digestLength = md.digest().length;
    }

    /**
    * Returns an instance of this object for the designated message digest
    * algorithm.
    */
    static SRPDigest getInstance(String mdName) {
        MessageDigest result = null;
        try {
            result = MessageDigest.getInstance(mdName);
        } catch (Exception x) {
            cat.error("getInstance(\"" + String.valueOf(mdName) + "\")", x);
            throw new RuntimeException(String.valueOf(x));
        }
        MessageDigest sha;
        try {
            sha = (MessageDigest) result.clone();
        } catch (CloneNotSupportedException x) {
            cat.error("getInstance(\"" + String.valueOf(mdName) + "\")", x);
            throw new RuntimeException("Message digest implementation not cloneable");
        }
        return new SRPDigest(result);
    }

    /**
    * Returns the size of the output, in bytes, of the message digest
    * algorithm.
    *
    * @return the size in bytes of the digest output.
    */
    int getDigestLength() {
        return digestLength;
    }

    /**
	 * Returns a new instance of the SRP message digest algorithm --which is
	 * SHA-1 by default, but could be anything else provided the proper
	 * conditions as specified in the SRP specifications.
	 *
	 * @return a new instance of the underlying SRP message digest algorithm.
	 * @exception RuntimeException if the implementation of the message digest
	 * algorithm does not support cloning.
	 */
    MessageDigest newDigest() {
        MessageDigest result = null;
        try {
            result = (MessageDigest) md.clone();
        } catch (CloneNotSupportedException x) {
            cat.error("Message Digest algorithm implementation does not support cloning");
            throw new RuntimeException("Message Digest implementation not cloneable");
        }
        return result;
    }

    /**
	 * Performs an interleaved even-odd hash on the designated byte string.
	 *
	 * @param ba a byte array to hash-interleave.
	 * @return the interleaved hash result.
	 */
    byte[] digestInterleaved(byte[] ba) {
        int limit = ba.length;
        int i, offset;
        for (offset = 0; offset < limit && ba[offset] == 0x00; offset++) ;
        int klen = (limit - offset) / 2;
        byte[] result = new byte[2 * digestLength];
        MessageDigest mdEven = newDigest();
        MessageDigest mdOdd = newDigest();
        int j = limit - 1;
        for (i = 0; i < klen; i++) {
            mdEven.update(ba[j--]);
            mdOdd.update(ba[j--]);
        }
        byte[] outEven = mdEven.digest();
        byte[] outOdd = mdOdd.digest();
        for (i = 0, j = 0; i < digestLength; i++) {
            result[j++] = outEven[i];
            result[j++] = outOdd[i];
        }
        return result;
    }
}
