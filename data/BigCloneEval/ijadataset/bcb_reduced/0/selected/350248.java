package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.harmony.luni.internal.nls.Messages;

/**
 * UUID is an immutable representation of a 128-bit universally unique
 * identifier (UUID).
 * <p>
 * There are multiple, variant layouts of UUIDs, but this class is based upon
 * variant 2 of <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>, the
 * Leach-Salz variant. This class can be used to model alternate variants, but
 * most of the methods will be unsupported in those cases; see each method for
 * details.
 *
 * @since 1.5
 */
public final class UUID implements Serializable, Comparable<UUID> {

    private static final long serialVersionUID = -4856846361193249489L;

    private static SecureRandom rng;

    private long mostSigBits;

    private long leastSigBits;

    private transient int variant;

    private transient int version;

    private transient long timestamp;

    private transient int clockSequence;

    private transient long node;

    private transient int hash;

    /**
     * <p>
     * Constructs an instance with the specified bits.
     *
     * @param mostSigBits
     *            The 64 most significant bits of the UUID.
     * @param leastSigBits
     *            The 64 least significant bits of the UUID.
     */
    public UUID(long mostSigBits, long leastSigBits) {
        super();
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
        init();
    }

    /**
     * <p>
     * Sets up the transient fields of this instance based on the current values
     * of the {@code mostSigBits} and {@code leastSigBits} fields.
     */
    private void init() {
        int msbHash = (int) (mostSigBits ^ (mostSigBits >>> 32));
        int lsbHash = (int) (leastSigBits ^ (leastSigBits >>> 32));
        hash = msbHash ^ lsbHash;
        if ((leastSigBits & 0x8000000000000000L) == 0) {
            variant = 0;
        } else if ((leastSigBits & 0x4000000000000000L) != 0) {
            variant = (int) ((leastSigBits & 0xE000000000000000L) >>> 61);
        } else {
            variant = 2;
        }
        version = (int) ((mostSigBits & 0x000000000000F000) >>> 12);
        if (variant != 2 && version != 1) {
            return;
        }
        long timeLow = (mostSigBits & 0xFFFFFFFF00000000L) >>> 32;
        long timeMid = (mostSigBits & 0x00000000FFFF0000L) << 16;
        long timeHigh = (mostSigBits & 0x0000000000000FFFL) << 48;
        timestamp = timeLow | timeMid | timeHigh;
        clockSequence = (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
        node = (leastSigBits & 0x0000FFFFFFFFFFFFL);
    }

    /**
     * <p>
     * Generates a variant 2, version 4 (randomly generated number) UUID as per
     * <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return an UUID instance.
     */
    public static UUID randomUUID() {
        byte[] data;
        synchronized (UUID.class) {
            if (rng == null) {
                rng = new SecureRandom();
            }
        }
        rng.nextBytes(data = new byte[16]);
        long msb = (data[0] & 0xFFL) << 56;
        msb |= (data[1] & 0xFFL) << 48;
        msb |= (data[2] & 0xFFL) << 40;
        msb |= (data[3] & 0xFFL) << 32;
        msb |= (data[4] & 0xFFL) << 24;
        msb |= (data[5] & 0xFFL) << 16;
        msb |= (data[6] & 0x0FL) << 8;
        msb |= (0x4L << 12);
        msb |= (data[7] & 0xFFL);
        long lsb = (data[8] & 0x3FL) << 56;
        lsb |= (0x2L << 62);
        lsb |= (data[9] & 0xFFL) << 48;
        lsb |= (data[10] & 0xFFL) << 40;
        lsb |= (data[11] & 0xFFL) << 32;
        lsb |= (data[12] & 0xFFL) << 24;
        lsb |= (data[13] & 0xFFL) << 16;
        lsb |= (data[14] & 0xFFL) << 8;
        lsb |= (data[15] & 0xFFL);
        return new UUID(msb, lsb);
    }

    /**
     * <p>
     * Generates a variant 2, version 3 (name-based, MD5-hashed) UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @param name
     *            the name used as byte array to create an UUID.
     * @return an UUID instance.
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        if (name == null) {
            throw new NullPointerException();
        }
        byte[] hash;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(name);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        long msb = (hash[0] & 0xFFL) << 56;
        msb |= (hash[1] & 0xFFL) << 48;
        msb |= (hash[2] & 0xFFL) << 40;
        msb |= (hash[3] & 0xFFL) << 32;
        msb |= (hash[4] & 0xFFL) << 24;
        msb |= (hash[5] & 0xFFL) << 16;
        msb |= (hash[6] & 0x0FL) << 8;
        msb |= (0x3L << 12);
        msb |= (hash[7] & 0xFFL);
        long lsb = (hash[8] & 0x3FL) << 56;
        lsb |= (0x2L << 62);
        lsb |= (hash[9] & 0xFFL) << 48;
        lsb |= (hash[10] & 0xFFL) << 40;
        lsb |= (hash[11] & 0xFFL) << 32;
        lsb |= (hash[12] & 0xFFL) << 24;
        lsb |= (hash[13] & 0xFFL) << 16;
        lsb |= (hash[14] & 0xFFL) << 8;
        lsb |= (hash[15] & 0xFFL);
        return new UUID(msb, lsb);
    }

    /**
     * <p>
     * Parses a UUID string with the format defined by {@link #toString()}.
     *
     * @param uuid
     *            the UUID string to parse.
     * @return an UUID instance.
     * @throws NullPointerException
     *             if {@code uuid} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code uuid} is not formatted correctly.
     */
    public static UUID fromString(String uuid) {
        if (uuid == null) {
            throw new NullPointerException();
        }
        int[] position = new int[5];
        int lastPosition = 1;
        int startPosition = 0;
        int i = 0;
        for (; i < position.length && lastPosition > 0; i++) {
            position[i] = uuid.indexOf("-", startPosition);
            lastPosition = position[i];
            startPosition = position[i] + 1;
        }
        if (i != position.length || lastPosition != -1) {
            throw new IllegalArgumentException(Messages.getString("luni.47") + uuid);
        }
        long m1 = Long.parseLong(uuid.substring(0, position[0]), 16);
        long m2 = Long.parseLong(uuid.substring(position[0] + 1, position[1]), 16);
        long m3 = Long.parseLong(uuid.substring(position[1] + 1, position[2]), 16);
        long lsb1 = Long.parseLong(uuid.substring(position[2] + 1, position[3]), 16);
        long lsb2 = Long.parseLong(uuid.substring(position[3] + 1), 16);
        long msb = (m1 << 32) | (m2 << 16) | m3;
        long lsb = (lsb1 << 48) | lsb2;
        return new UUID(msb, lsb);
    }

    /**
     * <p>
     * The 64 least significant bits of the UUID.
     *
     * @return the 64 least significant bits.
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * <p>
     * The 64 most significant bits of the UUID.
     *
     * @return the 64 most significant bits.
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * <p>
     * The version of the variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>. If the variant
     * is not 2, then the version will be 0.
     * <ul>
     * <li>1 - Time-based UUID</li>
     * <li>2 - DCE Security UUID</li>
     * <li>3 - Name-based with MD5 hashing UUID ({@link #nameUUIDFromBytes(byte[])})</li>
     * <li>4 - Randomly generated UUID ({@link #randomUUID()})</li>
     * <li>5 - Name-based with SHA-1 hashing UUID</li>
     * </ul>
     * 
     * @return an {@code int} value.
     */
    public int version() {
        return version;
    }

    /**
     * <p>
     * The variant of the UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     * <ul>
     * <li>0 - Reserved for NCS compatibility</li>
     * <li>2 - RFC 4122/Leach-Salz</li>
     * <li>6 - Reserved for Microsoft Corporation compatibility</li>
     * <li>7 - Reserved for future use</li>
     * </ul>
     * 
     * @return an {@code int} value.
     */
    public int variant() {
        return variant;
    }

    /**
     * <p>
     * The timestamp value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public long timestamp() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return timestamp;
    }

    /**
     * <p>
     * The clock sequence value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public int clockSequence() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return clockSequence;
    }

    /**
     * <p>
     * The node value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public long node() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return node;
    }

    /**
     * <p>
     * Compares this UUID to the specified UUID. The natural ordering of UUIDs
     * is based upon the value of the bits from most significant to least
     * significant.
     *
     * @param uuid
     *            the UUID to compare to.
     * @return a value of -1, 0 or 1 if this UUID is less than, equal to or
     *         greater than {@code uuid}.
     */
    public int compareTo(UUID uuid) {
        if (uuid == this) {
            return 0;
        }
        if (this.mostSigBits != uuid.mostSigBits) {
            return this.mostSigBits < uuid.mostSigBits ? -1 : 1;
        }
        assert this.mostSigBits == uuid.mostSigBits;
        if (this.leastSigBits != uuid.leastSigBits) {
            return this.leastSigBits < uuid.leastSigBits ? -1 : 1;
        }
        assert this.leastSigBits == uuid.leastSigBits;
        return 0;
    }

    /**
     * <p>
     * Compares this UUID to another object for equality. If {@code object}
     * is not {@code null}, is a UUID instance, and all bits are equal, then
     * {@code true} is returned.
     *
     * @param object
     *            the {@code Object} to compare to.
     * @return {@code true} if this UUID is equal to {@code object}
     *         or {@code false} if not.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof UUID)) {
            return false;
        }
        UUID that = (UUID) object;
        return (this.leastSigBits == that.leastSigBits) && (this.mostSigBits == that.mostSigBits);
    }

    /**
     * <p>
     * Returns a hash value for this UUID that is consistent with the
     * {@link #equals(Object)} method.
     *
     * @return an {@code int} value.
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * <p>
     * Returns a string representation of this UUID in the following format, as
     * per <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * <pre>
     *            UUID                   = time-low &quot;-&quot; time-mid &quot;-&quot;
     *                                     time-high-and-version &quot;-&quot;
     *                                     clock-seq-and-reserved
     *                                     clock-seq-low &quot;-&quot; node
     *            time-low               = 4hexOctet
     *            time-mid               = 2hexOctet
     *            time-high-and-version  = 2hexOctet
     *            clock-seq-and-reserved = hexOctet
     *            clock-seq-low          = hexOctet
     *            node                   = 6hexOctet
     *            hexOctet               = hexDigit hexDigit
     *            hexDigit =
     *                &quot;0&quot; / &quot;1&quot; / &quot;2&quot; / &quot;3&quot; / &quot;4&quot; / &quot;5&quot; / &quot;6&quot; / &quot;7&quot; / &quot;8&quot; / &quot;9&quot; /
     *                &quot;a&quot; / &quot;b&quot; / &quot;c&quot; / &quot;d&quot; / &quot;e&quot; / &quot;f&quot; /
     *                &quot;A&quot; / &quot;B&quot; / &quot;C&quot; / &quot;D&quot; / &quot;E&quot; / &quot;F&quot;
     * </pre>
     * 
     * @return a String instance.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(36);
        String msbStr = Long.toHexString(mostSigBits);
        if (msbStr.length() < 16) {
            int diff = 16 - msbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(msbStr);
        builder.insert(8, '-');
        builder.insert(13, '-');
        builder.append('-');
        String lsbStr = Long.toHexString(leastSigBits);
        if (lsbStr.length() < 16) {
            int diff = 16 - lsbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(lsbStr);
        builder.insert(23, '-');
        return builder.toString();
    }

    /**
     * <p>
     * Resets the transient fields to match the behavior of the constructor.
     * 
     * @param in
     *            the {@code InputStream} to read from.
     * @throws IOException
     *             if {@code in} throws it.
     * @throws ClassNotFoundException
     *             if {@code in} throws it.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }
}
