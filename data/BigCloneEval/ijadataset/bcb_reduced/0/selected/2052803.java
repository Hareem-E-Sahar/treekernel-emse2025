package gnu.javax.crypto.jce.prng;

import gnu.java.security.Configuration;
import gnu.java.security.Registry;
import gnu.java.security.prng.LimitReachedException;
import gnu.javax.crypto.cipher.IBlockCipher;
import gnu.javax.crypto.prng.ICMGenerator;
import java.math.BigInteger;
import java.security.SecureRandomSpi;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
 * An <em>Adapter</em> class around {@link ICMGenerator} to allow using this
 * algorithm as a JCE {@link java.security.SecureRandom}.
 */
public class ICMRandomSpi extends SecureRandomSpi {

    private static final Logger log = Logger.getLogger(ICMRandomSpi.class.getName());

    /** Class-wide prng to generate random material for the underlying prng. */
    private static final ICMGenerator prng;

    static {
        prng = new ICMGenerator();
        resetLocalPRNG();
    }

    private static final String MSG = "Exception while setting up an " + Registry.ICM_PRNG + " SPI: ";

    private static final String RETRY = "Retry...";

    private static final String LIMIT_REACHED_MSG = "Limit reached: ";

    private static final String RESEED = "Re-seed...";

    /** Our underlying prng instance. */
    private ICMGenerator adaptee = new ICMGenerator();

    private static void resetLocalPRNG() {
        if (Configuration.DEBUG) log.entering(ICMRandomSpi.class.getName(), "resetLocalPRNG");
        HashMap attributes = new HashMap();
        attributes.put(ICMGenerator.CIPHER, Registry.AES_CIPHER);
        byte[] key = new byte[128 / 8];
        Random rand = new Random(System.currentTimeMillis());
        rand.nextBytes(key);
        attributes.put(IBlockCipher.KEY_MATERIAL, key);
        int aesBlockSize = 128 / 8;
        byte[] offset = new byte[aesBlockSize];
        rand.nextBytes(offset);
        attributes.put(ICMGenerator.OFFSET, offset);
        int ndxLen = 0;
        int limit = aesBlockSize / 2;
        while (ndxLen < 1 || ndxLen > limit) ndxLen = rand.nextInt(limit + 1);
        attributes.put(ICMGenerator.SEGMENT_INDEX_LENGTH, Integer.valueOf(ndxLen));
        byte[] index = new byte[ndxLen];
        rand.nextBytes(index);
        attributes.put(ICMGenerator.SEGMENT_INDEX, new BigInteger(1, index));
        prng.setup(attributes);
        if (Configuration.DEBUG) log.exiting(ICMRandomSpi.class.getName(), "resetLocalPRNG");
    }

    public byte[] engineGenerateSeed(int numBytes) {
        if (Configuration.DEBUG) log.entering(this.getClass().getName(), "engineGenerateSeed");
        if (numBytes < 1) {
            if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "engineGenerateSeed");
            return new byte[0];
        }
        byte[] result = new byte[numBytes];
        this.engineNextBytes(result);
        if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "engineGenerateSeed");
        return result;
    }

    public void engineNextBytes(byte[] bytes) {
        if (Configuration.DEBUG) log.entering(this.getClass().getName(), "engineNextBytes");
        if (!adaptee.isInitialised()) this.engineSetSeed(new byte[0]);
        while (true) {
            try {
                adaptee.nextBytes(bytes, 0, bytes.length);
                break;
            } catch (LimitReachedException x) {
                if (Configuration.DEBUG) {
                    log.fine(LIMIT_REACHED_MSG + String.valueOf(x));
                    log.fine(RESEED);
                }
                resetLocalPRNG();
            }
        }
        if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "engineNextBytes");
    }

    public void engineSetSeed(byte[] seed) {
        if (Configuration.DEBUG) log.entering(this.getClass().getName(), "engineSetSeed");
        int materialLength = 0;
        materialLength += 16;
        materialLength += 16;
        materialLength += 8;
        byte[] material = new byte[materialLength];
        int materialOffset = 0;
        int materialLeft = material.length;
        if (seed.length > 0) {
            int lenToCopy = Math.min(materialLength, seed.length);
            System.arraycopy(seed, 0, material, 0, lenToCopy);
            materialOffset += lenToCopy;
            materialLeft -= lenToCopy;
        }
        if (materialOffset > 0) {
            while (true) {
                try {
                    prng.nextBytes(material, materialOffset, materialLeft);
                    break;
                } catch (IllegalStateException x) {
                    throw new InternalError(MSG + String.valueOf(x));
                } catch (LimitReachedException x) {
                    if (Configuration.DEBUG) {
                        log.fine(MSG + String.valueOf(x));
                        log.fine(RETRY);
                    }
                }
            }
        }
        HashMap attributes = new HashMap();
        attributes.put(ICMGenerator.CIPHER, Registry.AES_CIPHER);
        attributes.put(ICMGenerator.SEGMENT_INDEX_LENGTH, Integer.valueOf(4));
        byte[] key = new byte[16];
        System.arraycopy(material, 0, key, 0, 16);
        attributes.put(IBlockCipher.KEY_MATERIAL, key);
        byte[] offset = new byte[16];
        System.arraycopy(material, 16, offset, 0, 16);
        attributes.put(ICMGenerator.OFFSET, offset);
        byte[] index = new byte[8];
        System.arraycopy(material, 32, index, 0, 8);
        attributes.put(ICMGenerator.SEGMENT_INDEX, new BigInteger(1, index));
        adaptee.init(attributes);
        if (Configuration.DEBUG) log.exiting(this.getClass().getName(), "engineSetSeed");
    }
}
