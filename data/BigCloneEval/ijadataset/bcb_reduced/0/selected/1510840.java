package net.wimpi.pim.util;

/** * A class implements the MD5 algorithm. * <p> * The MD5 algorithm is described in RFC 1321. * There are numerous implementations out there, * this one was tuned specifically for hashing short * strings (i.e. passwords). * <p> * It is not recommend to use this implementation for * anything else but that. * * @author Dieter Wimberger * @version @version@ (@date@) */
public final class MD5 {

    /**   * Returns the MD5 hash digest transformed into a hex   * representation as <code>String</code>.   *   * @return the MD5 hash of the input as <code>String</code>.   */
    public static final String hash(String str) {
        return MD5.asHex(MD5.digest(str.getBytes()));
    }

    /**   * Returns the MD5 hash of the input data.   * <p>   * Following the the MD5 standard specification, the result   * is returned least significant byte first, however,   * many applications use the most significant byte first (more conventional).   *   * @param msg the <code>byte[]</code> to be digested.   *   * @return the MD5 hash of the data as <code>String</code>.   */
    public static final byte[] digest(byte[] msg) {
        int padsize = (120 - (msg.length % 64)) % 64;
        int i;
        long l = msg.length * 8;
        int a = 0x67452301, aa, b = 0xefcdab89, bb, c = 0x98badcfe, cc, d = 0x10325476, dd;
        byte[] src = new byte[msg.length + padsize + 8];
        try {
            System.arraycopy(msg, 0, src, 0, msg.length);
        } catch (Exception ex) {
        }
        src[msg.length] = (byte) 0x80;
        for (i = msg.length + 1; i < msg.length + padsize; i++) src[i] = 0;
        for (i = src.length - 8; i < src.length; i++) {
            src[i] = (byte) l;
            l >>>= 8;
        }
        int[] x = new int[16];
        for (i = 0; i < src.length; i += 64) {
            for (int j = 0; j < 16; j++) {
                x[j] = 0;
                for (int k = 3; k >= 0; k--) {
                    x[j] <<= 8;
                    x[j] += src[i + j * 4 + k] & 0xff;
                }
            }
            aa = a;
            bb = b;
            cc = c;
            dd = d;
            a = round1(a, b, c, d, 0, 7, 1, x);
            d = round1(d, a, b, c, 1, 12, 2, x);
            c = round1(c, d, a, b, 2, 17, 3, x);
            b = round1(b, c, d, a, 3, 22, 4, x);
            a = round1(a, b, c, d, 4, 7, 5, x);
            d = round1(d, a, b, c, 5, 12, 6, x);
            c = round1(c, d, a, b, 6, 17, 7, x);
            b = round1(b, c, d, a, 7, 22, 8, x);
            a = round1(a, b, c, d, 8, 7, 9, x);
            d = round1(d, a, b, c, 9, 12, 10, x);
            c = round1(c, d, a, b, 10, 17, 11, x);
            b = round1(b, c, d, a, 11, 22, 12, x);
            a = round1(a, b, c, d, 12, 7, 13, x);
            d = round1(d, a, b, c, 13, 12, 14, x);
            c = round1(c, d, a, b, 14, 17, 15, x);
            b = round1(b, c, d, a, 15, 22, 16, x);
            a = round2(a, b, c, d, 1, 5, 17, x);
            d = round2(d, a, b, c, 6, 9, 18, x);
            c = round2(c, d, a, b, 11, 14, 19, x);
            b = round2(b, c, d, a, 0, 20, 20, x);
            a = round2(a, b, c, d, 5, 5, 21, x);
            d = round2(d, a, b, c, 10, 9, 22, x);
            c = round2(c, d, a, b, 15, 14, 23, x);
            b = round2(b, c, d, a, 4, 20, 24, x);
            a = round2(a, b, c, d, 9, 5, 25, x);
            d = round2(d, a, b, c, 14, 9, 26, x);
            c = round2(c, d, a, b, 3, 14, 27, x);
            b = round2(b, c, d, a, 8, 20, 28, x);
            a = round2(a, b, c, d, 13, 5, 29, x);
            d = round2(d, a, b, c, 2, 9, 30, x);
            c = round2(c, d, a, b, 7, 14, 31, x);
            b = round2(b, c, d, a, 12, 20, 32, x);
            a = round3(a, b, c, d, 5, 4, 33, x);
            d = round3(d, a, b, c, 8, 11, 34, x);
            c = round3(c, d, a, b, 11, 16, 35, x);
            b = round3(b, c, d, a, 14, 23, 36, x);
            a = round3(a, b, c, d, 1, 4, 37, x);
            d = round3(d, a, b, c, 4, 11, 38, x);
            c = round3(c, d, a, b, 7, 16, 39, x);
            b = round3(b, c, d, a, 10, 23, 40, x);
            a = round3(a, b, c, d, 13, 4, 41, x);
            d = round3(d, a, b, c, 0, 11, 42, x);
            c = round3(c, d, a, b, 3, 16, 43, x);
            b = round3(b, c, d, a, 6, 23, 44, x);
            a = round3(a, b, c, d, 9, 4, 45, x);
            d = round3(d, a, b, c, 12, 11, 46, x);
            c = round3(c, d, a, b, 15, 16, 47, x);
            b = round3(b, c, d, a, 2, 23, 48, x);
            a = round4(a, b, c, d, 0, 6, 49, x);
            d = round4(d, a, b, c, 7, 10, 50, x);
            c = round4(c, d, a, b, 14, 15, 51, x);
            b = round4(b, c, d, a, 5, 21, 52, x);
            a = round4(a, b, c, d, 12, 6, 53, x);
            d = round4(d, a, b, c, 3, 10, 54, x);
            c = round4(c, d, a, b, 10, 15, 55, x);
            b = round4(b, c, d, a, 1, 21, 56, x);
            a = round4(a, b, c, d, 8, 6, 57, x);
            d = round4(d, a, b, c, 15, 10, 58, x);
            c = round4(c, d, a, b, 6, 15, 59, x);
            b = round4(b, c, d, a, 13, 21, 60, x);
            a = round4(a, b, c, d, 4, 6, 61, x);
            d = round4(d, a, b, c, 11, 10, 62, x);
            c = round4(c, d, a, b, 2, 15, 63, x);
            b = round4(b, c, d, a, 9, 21, 64, x);
            a += aa;
            b += bb;
            c += cc;
            d += dd;
        }
        byte[] ret = new byte[16];
        for (i = 0; i < 4; i++) {
            ret[i] = (byte) a;
            a >>>= 8;
        }
        for (; i < 8; i++) {
            ret[i] = (byte) b;
            b >>>= 8;
        }
        for (; i < 12; i++) {
            ret[i] = (byte) c;
            c >>>= 8;
        }
        for (; i < 16; i++) {
            ret[i] = (byte) d;
            d >>>= 8;
        }
        return ret;
    }

    /** MD5 Transformation routines *********************************************/
    private static final int rot(int x, int s) {
        return x << s | x >>> (32 - s);
    }

    private static final int F(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    private static final int G(int x, int y, int z) {
        return (x & z) | (y & ~z);
    }

    private static final int H(int x, int y, int z) {
        return x ^ y ^ z;
    }

    private static final int I(int x, int y, int z) {
        return y ^ (x | ~z);
    }

    private static final int round1(int a, int b, int c, int d, int k, int s, int i, int[] x) {
        return b + rot((a + F(b, c, d) + x[k] + T[i - 1]), s);
    }

    private static final int round2(int a, int b, int c, int d, int k, int s, int i, int[] x) {
        return b + rot((a + G(b, c, d) + x[k] + T[i - 1]), s);
    }

    private static final int round3(int a, int b, int c, int d, int k, int s, int i, int[] x) {
        return b + rot((a + H(b, c, d) + x[k] + T[i - 1]), s);
    }

    private static final int round4(int a, int b, int c, int d, int k, int s, int i, int[] x) {
        return b + rot((a + I(b, c, d) + x[k] + T[i - 1]), s);
    }

    private static final int T[] = { 0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf, 0x4787c62a, 0xa8304613, 0xfd469501, 0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be, 0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821, 0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8, 0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed, 0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a, 0xfffa3942, 0x8771f681, 0x6d9d6122, 0xfde5380c, 0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70, 0x289b7ec6, 0xeaa127fa, 0xd4ef3085, 0x04881d05, 0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665, 0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039, 0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1, 0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1, 0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391 };

    /**   * Returns a <code>String</code> containing unsigned hexadecimal   * numbers as digits.   * <p>   * Contains two hex digit characters for each byte from the passed in   * <code>byte[]</code>.   *   * @param data the array of bytes to be converted into a hex-string.   * @return	the generated hexadecimal representation as   *          <code>String</code>.   */
    public static final String asHex(byte[] data) {
        StringBuffer buf = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            if (((int) data[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) data[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static final long hashValue(byte[] msg, boolean high) {
        byte[] hash = MD5.digest(msg);
        byte[] val = new byte[8];
        if (high) {
            System.arraycopy(hash, 8, val, 0, 8);
        } else {
            System.arraycopy(hash, 0, val, 0, 8);
        }
        System.out.println("Long from :" + asHex(val));
        return bytes2long(val);
    }

    private static final long bytes2long(byte[] data) {
        long l = 0;
        l = ((data[7] & 0xff) << 56) + ((data[6] & 0xff) << 48) + ((data[5] & 0xff) << 40) + ((data[4] & 0xff) << 32) + ((data[3] & 0xff) << 24) + ((data[2] & 0xff) << 16) + ((data[1] & 0xff) << 8) + ((data[0] & 0xff) << 0);
        return l;
    }

    /**   * A main, to allow using this class from the command line.   */
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                System.out.println("Usage: java MD5 <password>");
            }
            byte[] msg = args[0].getBytes();
            String hash = hash(args[0]);
            System.out.println("Hash=" + hash);
            System.out.println("Hex=" + Long.toHexString(Long.MAX_VALUE));
            System.out.println("Low long=" + MD5.hashValue(msg, false));
            System.out.println("High long=" + MD5.hashValue(msg, true));
            msg = args[1].getBytes();
            hash = hash(args[1]);
            System.out.println("Hash=" + hash);
            System.out.println("Hex=" + Long.toHexString(Long.MAX_VALUE));
            System.out.println("Low long=" + MD5.hashValue(msg, false));
            System.out.println("High long=" + MD5.hashValue(msg, true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
