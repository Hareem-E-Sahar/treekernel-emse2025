package ua.org.hatu.daos.gwt.server.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Code to generate page ids. IdGenerators are expensive to setup so it is
 * suggested that you share instances wherever possible. This action will also
 * enhance security. Much of this code is adapted from
 * org.apache.catalina.session.ManagerBase. Specifically Revision 1.37 which has
 * been unchanged in the past 18 months. I have taken out the /dev/urandom stuff
 * and simplified things to the point where we can audit it to work out what
 * might be broken.
 * 
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class IdGenerator {

    /**
	 * Seed the random number
	 */
    public IdGenerator() {
        long seed = System.currentTimeMillis();
        char[] entropy = toString().toCharArray();
        for (int i = 0; i < entropy.length; i++) {
            long update = ((byte) entropy[i]) << ((i % 8) * 8);
            seed ^= update;
        }
        random.setSeed(seed);
    }

    /**
	 * Generate and return a new session identifier.
	 * 
	 * @param length
	 *            The number of bytes to generate
	 * @return A new page id string
	 */
    public synchronized String generateId(int length) {
        byte[] buffer = new byte[length];
        StringBuffer reply = new StringBuffer();
        int resultLenBytes = 0;
        while (resultLenBytes < length) {
            random.nextBytes(buffer);
            buffer = getDigest().digest(buffer);
            for (int j = 0; j < buffer.length && resultLenBytes < length; j++) {
                byte b1 = (byte) ((buffer[j] & 0xf0) >> 4);
                if (b1 < 10) {
                    reply.append((char) ('0' + b1));
                } else {
                    reply.append((char) ('A' + (b1 - 10)));
                }
                byte b2 = (byte) (buffer[j] & 0x0f);
                if (b2 < 10) {
                    reply.append((char) ('0' + b2));
                } else {
                    reply.append((char) ('A' + (b2 - 10)));
                }
                resultLenBytes++;
            }
        }
        return reply.toString();
    }

    /**
	 * @return the algorithm
	 */
    public synchronized String getAlgorithm() {
        return algorithm;
    }

    /**
	 * @param algorithm
	 *            the algorithm to set
	 */
    public synchronized void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        digest = null;
    }

    /**
	 * Return the MessageDigest object to be used for calculating session
	 * identifiers. If none has been created yet, initialize one the first time
	 * this method is called.
	 * 
	 * @return The hashing algorithm
	 */
    private MessageDigest getDigest() {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException ex) {
                try {
                    digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException ex2) {
                    digest = null;
                    throw new IllegalStateException("No algorithms for IdGenerator");
                }
            }
            System.out.println("Using MessageDigest: " + digest.getAlgorithm());
        }
        return digest;
    }

    @Override
    public final String toString() {
        return super.toString();
    }

    /**
	 * The default message digest algorithm to use if we cannot use the
	 * requested one.
	 */
    protected static final String DEFAULT_ALGORITHM = "MD5";

    /**
	 * The message digest algorithm to be used when generating session
	 * identifiers. This must be an algorithm supported by the
	 * <code>java.security.MessageDigest</code> class on your platform.
	 */
    private String algorithm = DEFAULT_ALGORITHM;

    /**
	 * A random number generator to use when generating session identifiers.
	 */
    private Random random = new SecureRandom();

    /**
	 * Return the MessageDigest implementation to be used when creating session
	 * identifiers.
	 */
    private MessageDigest digest = null;
}
