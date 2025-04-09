package net.sourceforge.epoint.util;

/**
 * Hexadecimal conversion
 *
 * @author <a href="mailto:nagydani@users.sourceforge.net">Daniel A. Nagy</a>
 */
public class Base16 {

    static final byte[] cTable = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    static final byte[] dTable = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15 };

    static final byte value(char c) {
        if ((c < '0') || (c > 'F')) return -1;
        return dTable[c - '0'];
    }

    public static final String encode(byte[] s, int off, int len) {
        int i = len;
        byte[] e = new byte[i * 2];
        while (i-- > 0) {
            e[i * 2 + 1] = cTable[s[off + i] & 0xf];
            e[i * 2] = cTable[(s[off + i] >> 4) & 0xf];
        }
        return new String(e);
    }

    public static final String encode(byte[] s) {
        return encode(s, 0, s.length);
    }

    public static final byte[] decode(String s) {
        int i = s.length(), j = i, k;
        byte b;
        boolean lo = true;
        byte[] d;
        String S = s.toUpperCase();
        while (i-- > 0) if (value(S.charAt(i)) < 0) j--;
        i = s.length();
        if ((j & 1) == 1) {
            k = (j + 1) / 2;
            d = new byte[k];
        } else {
            k = j / 2;
            d = new byte[k];
        }
        while (i-- > 0) if ((b = value(S.charAt(i))) >= 0) {
            if (lo) {
                d[--k] = b;
                lo = false;
            } else {
                d[k] |= b << 4;
                lo = true;
            }
        }
        return d;
    }
}
