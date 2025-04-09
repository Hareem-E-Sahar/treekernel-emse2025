import java.util.StringTokenizer;

class P10814_39643 {

    private static BigInt mdc(BigInt m, BigInt n) {
        do {
            m = m.remainder(n);
            if (m.equals(BigInt.ZERO)) return n;
            n = n.remainder(m);
        } while (!n.equals(BigInt.ZERO));
        return m;
    }

    public static void main(String[] args) {
        int cases = Integer.parseInt(readLn());
        for (int k = 0; k < cases; k++) {
            StringTokenizer st = new StringTokenizer(readLn(), "/");
            BigInt m = new BigInt(st.nextToken().trim());
            BigInt n = new BigInt(st.nextToken().trim());
            BigInt value = mdc(m, n);
            System.out.println(m.divide(value) + " / " + n.divide(value));
        }
    }

    static String readLn() {
        String newLine = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
        int car = -1;
        try {
            car = System.in.read();
            while ((car > 0) && (car != newLine.charAt(0))) {
                buffer.append((char) car);
                car = System.in.read();
            }
            if (car == newLine.charAt(0)) System.in.skip(newLine.length() - 1);
        } catch (java.io.IOException e) {
            return (null);
        }
        if ((car < 0) && (buffer.length() == 0)) return (null);
        return (buffer.toString()).trim();
    }
}

class BigInt implements Comparable {

    private transient int ival;

    private transient int[] words;

    private static final int minFixNum = -100;

    private static final int maxFixNum = 1024;

    private static final int numFixNum = maxFixNum - minFixNum + 1;

    private static final BigInt[] smallFixNums = new BigInt[numFixNum];

    static {
        for (int i = numFixNum; --i >= 0; ) smallFixNums[i] = new BigInt(i + minFixNum);
    }

    static final BigInt ZERO = smallFixNums[-minFixNum];

    private static final int FLOOR = 1;

    private static final int CEILING = 2;

    private static final int TRUNCATE = 3;

    private static final int ROUND = 4;

    private BigInt() {
    }

    private BigInt(int value) {
        ival = value;
    }

    private BigInt(String val, int radix) {
        BigInt result = valueOf(val, radix);
        this.ival = result.ival;
        this.words = result.words;
    }

    BigInt(String val) {
        this(val, 10);
    }

    private static BigInt valueOf(long val) {
        if (val >= minFixNum && val <= maxFixNum) return smallFixNums[(int) val - minFixNum];
        int i = (int) val;
        if ((long) i == val) return new BigInt(i);
        BigInt result = alloc(2);
        result.ival = 2;
        result.words[0] = i;
        result.words[1] = (int) (val >> 32);
        return result;
    }

    private static BigInt make(int[] words, int len) {
        if (words == null) return valueOf(len);
        len = BigInt.wordsNeeded(words, len);
        if (len <= 1) return len == 0 ? ZERO : valueOf(words[0]);
        BigInt num = new BigInt();
        num.words = words;
        num.ival = len;
        return num;
    }

    private static BigInt alloc(int nwords) {
        BigInt result = new BigInt();
        if (nwords > 1) result.words = new int[nwords];
        return result;
    }

    private void realloc(int nwords) {
        if (nwords == 0) {
            if (words != null) {
                if (ival > 0) ival = words[0];
                words = null;
            }
        } else if (words == null || words.length < nwords || words.length > nwords + 2) {
            int[] new_words = new int[nwords];
            if (words == null) {
                new_words[0] = ival;
                ival = 1;
            } else {
                if (nwords < ival) ival = nwords;
                System.arraycopy(words, 0, new_words, 0, ival);
            }
            words = new_words;
        }
    }

    private boolean isNegative() {
        return (words == null ? ival : words[ival - 1]) < 0;
    }

    private static int compareTo(BigInt x, BigInt y) {
        if (x.words == null && y.words == null) return x.ival < y.ival ? -1 : x.ival > y.ival ? 1 : 0;
        boolean x_negative = x.isNegative();
        boolean y_negative = y.isNegative();
        if (x_negative != y_negative) return x_negative ? -1 : 1;
        int x_len = x.words == null ? 1 : x.ival;
        int y_len = y.words == null ? 1 : y.ival;
        if (x_len != y_len) return (x_len > y_len) != x_negative ? 1 : -1;
        return cmp(x.words, y.words, x_len);
    }

    public int compareTo(Object obj) {
        if (obj instanceof BigInt) return compareTo(this, (BigInt) obj);
        throw new ClassCastException();
    }

    private boolean isZero() {
        return words == null && ival == 0;
    }

    private static int wordsNeeded(int[] words, int len) {
        int i = len;
        if (i > 0) {
            int word = words[--i];
            if (word == -1) {
                while (i > 0 && (word = words[i - 1]) < 0) {
                    i--;
                    if (word != -1) break;
                }
            } else {
                while (word == 0 && i > 0 && (word = words[i - 1]) >= 0) i--;
            }
        }
        return i + 1;
    }

    private BigInt canonicalize() {
        if (words != null && (ival = BigInt.wordsNeeded(words, ival)) <= 1) {
            if (ival == 1) ival = words[0];
            words = null;
        }
        if (words == null && ival >= minFixNum && ival <= maxFixNum) return smallFixNums[ival - minFixNum];
        return this;
    }

    private static BigInt add(int x, int y) {
        return valueOf((long) x + (long) y);
    }

    private static BigInt add(BigInt x, int y) {
        if (x.words == null) return BigInt.add(x.ival, y);
        BigInt result = new BigInt(0);
        result.setAdd(x, y);
        return result.canonicalize();
    }

    private void setAdd(BigInt x, int y) {
        if (x.words == null) {
            set((long) x.ival + (long) y);
            return;
        }
        int len = x.ival;
        realloc(len + 1);
        long carry = y;
        for (int i = 0; i < len; i++) {
            carry += ((long) x.words[i] & 0xffffffffL);
            words[i] = (int) carry;
            carry >>= 32;
        }
        if (x.words[len - 1] < 0) carry--;
        words[len] = (int) carry;
        ival = wordsNeeded(words, len + 1);
    }

    private void setAdd(int y) {
        setAdd(this, y);
    }

    private void set(long y) {
        int i = (int) y;
        if ((long) i == y) {
            ival = i;
            words = null;
        } else {
            realloc(2);
            words[0] = i;
            words[1] = (int) (y >> 32);
            ival = 2;
        }
    }

    private void set(int[] words, int length) {
        this.ival = length;
        this.words = words;
    }

    private void set(BigInt y) {
        if (y.words == null) set(y.ival); else if (this != y) {
            realloc(y.ival);
            System.arraycopy(y.words, 0, words, 0, y.ival);
            ival = y.ival;
        }
    }

    private static BigInt add(BigInt x, BigInt y, int k) {
        if (x.words == null && y.words == null) return valueOf((long) k * (long) y.ival + (long) x.ival);
        if (k != 1) {
            if (k == -1) y = BigInt.neg(y); else y = BigInt.times(y, valueOf(k));
        }
        if (x.words == null) return BigInt.add(y, x.ival);
        if (y.words == null) return BigInt.add(x, y.ival);
        if (y.ival > x.ival) {
            BigInt tmp = x;
            x = y;
            y = tmp;
        }
        BigInt result = alloc(x.ival + 1);
        int i = y.ival;
        long carry = add_n(result.words, x.words, y.words, i);
        long y_ext = y.words[i - 1] < 0 ? 0xffffffffL : 0;
        for (; i < x.ival; i++) {
            carry += ((long) x.words[i] & 0xffffffffL) + y_ext;
            ;
            result.words[i] = (int) carry;
            carry >>>= 32;
        }
        if (x.words[i - 1] < 0) y_ext--;
        result.words[i] = (int) (carry + y_ext);
        result.ival = i + 1;
        return result.canonicalize();
    }

    private static BigInt times(BigInt x, int y) {
        if (y == 0) return ZERO;
        if (y == 1) return x;
        int[] xwords = x.words;
        int xlen = x.ival;
        if (xwords == null) return valueOf((long) xlen * (long) y);
        boolean negative;
        BigInt result = BigInt.alloc(xlen + 1);
        if (xwords[xlen - 1] < 0) {
            negative = true;
            negate(result.words, xwords, xlen);
            xwords = result.words;
        } else negative = false;
        if (y < 0) {
            negative = !negative;
            y = -y;
        }
        result.words[xlen] = mul_1(result.words, xwords, xlen, y);
        result.ival = xlen + 1;
        if (negative) result.setNegative();
        return result.canonicalize();
    }

    private static BigInt times(BigInt x, BigInt y) {
        if (y.words == null) return times(x, y.ival);
        if (x.words == null) return times(y, x.ival);
        boolean negative = false;
        int[] xwords;
        int[] ywords;
        int xlen = x.ival;
        int ylen = y.ival;
        if (x.isNegative()) {
            negative = true;
            xwords = new int[xlen];
            negate(xwords, x.words, xlen);
        } else {
            negative = false;
            xwords = x.words;
        }
        if (y.isNegative()) {
            negative = !negative;
            ywords = new int[ylen];
            negate(ywords, y.words, ylen);
        } else ywords = y.words;
        if (xlen < ylen) {
            int[] twords = xwords;
            xwords = ywords;
            ywords = twords;
            int tlen = xlen;
            xlen = ylen;
            ylen = tlen;
        }
        BigInt result = BigInt.alloc(xlen + ylen);
        mul(result.words, xwords, xlen, ywords, ylen);
        result.ival = xlen + ylen;
        if (negative) result.setNegative();
        return result.canonicalize();
    }

    private static void divide(long x, long y, BigInt quotient, BigInt remainder, int rounding_mode) {
        boolean xNegative, yNegative;
        if (x < 0) {
            xNegative = true;
            if (x == Long.MIN_VALUE) {
                divide(valueOf(x), valueOf(y), quotient, remainder, rounding_mode);
                return;
            }
            x = -x;
        } else xNegative = false;
        if (y < 0) {
            yNegative = true;
            if (y == Long.MIN_VALUE) {
                if (rounding_mode == TRUNCATE) {
                    if (quotient != null) quotient.set(0);
                    if (remainder != null) remainder.set(x);
                } else divide(valueOf(x), valueOf(y), quotient, remainder, rounding_mode);
                return;
            }
            y = -y;
        } else yNegative = false;
        long q = x / y;
        long r = x % y;
        boolean qNegative = xNegative ^ yNegative;
        boolean add_one = false;
        if (r != 0) {
            switch(rounding_mode) {
                case TRUNCATE:
                    break;
                case CEILING:
                case FLOOR:
                    if (qNegative == (rounding_mode == FLOOR)) add_one = true;
                    break;
                case ROUND:
                    add_one = r > ((y - (q & 1)) >> 1);
                    break;
            }
        }
        if (quotient != null) {
            if (add_one) q++;
            if (qNegative) q = -q;
            quotient.set(q);
        }
        if (remainder != null) {
            if (add_one) {
                r = y - r;
                xNegative = !xNegative;
            } else {
            }
            if (xNegative) r = -r;
            remainder.set(r);
        }
    }

    public long longValue() {
        if (words == null) return ival;
        if (ival == 1) return words[0];
        return ((long) words[1] << 32) + ((long) words[0] & 0xffffffffL);
    }

    private static void divide(BigInt x, BigInt y, BigInt quotient, BigInt remainder, int rounding_mode) {
        if ((x.words == null || x.ival <= 2) && (y.words == null || y.ival <= 2)) {
            long x_l = x.longValue();
            long y_l = y.longValue();
            if (x_l != Long.MIN_VALUE && y_l != Long.MIN_VALUE) {
                divide(x_l, y_l, quotient, remainder, rounding_mode);
                return;
            }
        }
        boolean xNegative = x.isNegative();
        boolean yNegative = y.isNegative();
        boolean qNegative = xNegative ^ yNegative;
        int ylen = y.words == null ? 1 : y.ival;
        int[] ywords = new int[ylen];
        y.getAbsolute(ywords);
        while (ylen > 1 && ywords[ylen - 1] == 0) ylen--;
        int xlen = x.words == null ? 1 : x.ival;
        int[] xwords = new int[xlen + 2];
        x.getAbsolute(xwords);
        while (xlen > 1 && xwords[xlen - 1] == 0) xlen--;
        int qlen, rlen;
        int cmpval = cmp(xwords, xlen, ywords, ylen);
        if (cmpval < 0) {
            int[] rwords = xwords;
            xwords = ywords;
            ywords = rwords;
            rlen = xlen;
            qlen = 1;
            xwords[0] = 0;
        } else if (cmpval == 0) {
            xwords[0] = 1;
            qlen = 1;
            ywords[0] = 0;
            rlen = 1;
        } else if (ylen == 1) {
            qlen = xlen;
            if (ywords[0] == 1 && xwords[xlen - 1] < 0) qlen++;
            rlen = 1;
            ywords[0] = divmod_1(xwords, xwords, xlen, ywords[0]);
        } else {
            int nshift = count_leading_zeros(ywords[ylen - 1]);
            if (nshift != 0) {
                lshift(ywords, 0, ywords, ylen, nshift);
                int x_high = lshift(xwords, 0, xwords, xlen, nshift);
                xwords[xlen++] = x_high;
            }
            if (xlen == ylen) xwords[xlen++] = 0;
            divide(xwords, xlen, ywords, ylen);
            rlen = ylen;
            rshift0(ywords, xwords, 0, rlen, nshift);
            qlen = xlen + 1 - ylen;
            if (quotient != null) {
                for (int i = 0; i < qlen; i++) xwords[i] = xwords[i + ylen];
            }
        }
        if (ywords[rlen - 1] < 0) {
            ywords[rlen] = 0;
            rlen++;
        }
        boolean add_one = false;
        if (rlen > 1 || ywords[0] != 0) {
            switch(rounding_mode) {
                case TRUNCATE:
                    break;
                case CEILING:
                case FLOOR:
                    if (qNegative == (rounding_mode == FLOOR)) add_one = true;
                    break;
                case ROUND:
                    BigInt tmp = remainder == null ? new BigInt() : remainder;
                    tmp.set(ywords, rlen);
                    tmp = shift(tmp, 1);
                    if (yNegative) tmp.setNegative();
                    int cmp = compareTo(tmp, y);
                    if (yNegative) cmp = -cmp;
                    add_one = (cmp == 1) || (cmp == 0 && (xwords[0] & 1) != 0);
            }
        }
        if (quotient != null) {
            quotient.set(xwords, qlen);
            if (qNegative) {
                if (add_one) quotient.setInvert(); else quotient.setNegative();
            } else if (add_one) quotient.setAdd(1);
        }
        if (remainder != null) {
            remainder.set(ywords, rlen);
            if (add_one) {
                BigInt tmp;
                if (y.words == null) {
                    tmp = remainder;
                    tmp.set(yNegative ? ywords[0] + y.ival : ywords[0] - y.ival);
                } else tmp = BigInt.add(remainder, y, yNegative ? 1 : -1);
                if (xNegative) remainder.setNegative(tmp); else remainder.set(tmp);
            } else {
                if (xNegative) remainder.setNegative();
            }
        }
    }

    BigInt divide(BigInt val) {
        if (val.isZero()) throw new ArithmeticException("divisor is zero");
        BigInt quot = new BigInt();
        divide(this, val, quot, null, TRUNCATE);
        return quot.canonicalize();
    }

    BigInt remainder(BigInt val) {
        if (val.isZero()) throw new ArithmeticException("divisor is zero");
        BigInt rem = new BigInt();
        divide(this, val, null, rem, TRUNCATE);
        return rem.canonicalize();
    }

    private void setInvert() {
        if (words == null) ival = ~ival; else {
            for (int i = ival; --i >= 0; ) words[i] = ~words[i];
        }
    }

    private void setShiftLeft(BigInt x, int count) {
        int[] xwords;
        int xlen;
        if (x.words == null) {
            if (count < 32) {
                set((long) x.ival << count);
                return;
            }
            xwords = new int[1];
            xwords[0] = x.ival;
            xlen = 1;
        } else {
            xwords = x.words;
            xlen = x.ival;
        }
        int word_count = count >> 5;
        count &= 31;
        int new_len = xlen + word_count;
        if (count == 0) {
            realloc(new_len);
            for (int i = xlen; --i >= 0; ) words[i + word_count] = xwords[i];
        } else {
            new_len++;
            realloc(new_len);
            int shift_out = lshift(words, word_count, xwords, xlen, count);
            count = 32 - count;
            words[new_len - 1] = (shift_out << count) >> count;
        }
        ival = new_len;
        for (int i = word_count; --i >= 0; ) words[i] = 0;
    }

    private void setShiftRight(BigInt x, int count) {
        if (x.words == null) set(count < 32 ? x.ival >> count : x.ival < 0 ? -1 : 0); else if (count == 0) set(x); else {
            boolean neg = x.isNegative();
            int word_count = count >> 5;
            count &= 31;
            int d_len = x.ival - word_count;
            if (d_len <= 0) set(neg ? -1 : 0); else {
                if (words == null || words.length < d_len) realloc(d_len);
                rshift0(words, x.words, word_count, d_len, count);
                ival = d_len;
                if (neg) words[d_len - 1] |= -2 << (31 - count);
            }
        }
    }

    private void setShift(BigInt x, int count) {
        if (count > 0) setShiftLeft(x, count); else setShiftRight(x, -count);
    }

    private static BigInt shift(BigInt x, int count) {
        if (x.words == null) {
            if (count <= 0) return valueOf(count > -32 ? x.ival >> (-count) : x.ival < 0 ? -1 : 0);
            if (count < 32) return valueOf((long) x.ival << count);
        }
        if (count == 0) return x;
        BigInt result = new BigInt(0);
        result.setShift(x, count);
        return result.canonicalize();
    }

    private void format(int radix, StringBuffer buffer) {
        if (words == null) buffer.append(Integer.toString(ival, radix)); else if (ival <= 2) buffer.append(Long.toString(longValue(), radix)); else {
            boolean neg = isNegative();
            int[] work;
            if (neg || radix != 16) {
                work = new int[ival];
                getAbsolute(work);
            } else work = words;
            int len = ival;
            if (radix == 16) {
                if (neg) buffer.append('-');
                int buf_start = buffer.length();
                for (int i = len; --i >= 0; ) {
                    int word = work[i];
                    for (int j = 8; --j >= 0; ) {
                        int hex_digit = (word >> (4 * j)) & 0xF;
                        if (hex_digit > 0 || buffer.length() > buf_start) buffer.append(Character.forDigit(hex_digit, 16));
                    }
                }
            } else {
                int i = buffer.length();
                for (; ; ) {
                    int digit = divmod_1(work, work, len, radix);
                    buffer.append(Character.forDigit(digit, radix));
                    while (len > 0 && work[len - 1] == 0) len--;
                    if (len == 0) break;
                }
                if (neg) buffer.append('-');
                int j = buffer.length() - 1;
                while (i < j) {
                    char tmp = buffer.charAt(i);
                    buffer.setCharAt(i, buffer.charAt(j));
                    buffer.setCharAt(j, tmp);
                    i++;
                    j--;
                }
            }
        }
    }

    public String toString() {
        return toString(10);
    }

    private String toString(int radix) {
        if (words == null) return Integer.toString(ival, radix);
        if (ival <= 2) return Long.toString(longValue(), radix);
        int buf_size = ival * (chars_per_word(radix) + 1);
        StringBuffer buffer = new StringBuffer(buf_size);
        format(radix, buffer);
        return buffer.toString();
    }

    public int hashCode() {
        return words == null ? ival : (words[0] + words[ival - 1]);
    }

    private static boolean equals(BigInt x, BigInt y) {
        if (x.words == null && y.words == null) return x.ival == y.ival;
        if (x.words == null || y.words == null || x.ival != y.ival) return false;
        for (int i = x.ival; --i >= 0; ) {
            if (x.words[i] != y.words[i]) return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BigInt)) return false;
        return equals(this, (BigInt) obj);
    }

    private static BigInt valueOf(String s, int radix) throws NumberFormatException {
        int len = s.length();
        if (len <= 15 && radix <= 16) return valueOf(Long.parseLong(s, radix));
        int byte_len = 0;
        byte[] bytes = new byte[len];
        boolean negative = false;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch == '-') negative = true; else if (ch == '_' || (byte_len == 0 && (ch == ' ' || ch == '\t'))) continue; else {
                int digit = Character.digit(ch, radix);
                if (digit < 0) break;
                bytes[byte_len++] = (byte) digit;
            }
        }
        return valueOf(bytes, byte_len, negative, radix);
    }

    private static BigInt valueOf(byte[] digits, int byte_len, boolean negative, int radix) {
        int chars_per_word = chars_per_word(radix);
        int[] words = new int[byte_len / chars_per_word + 1];
        int size = set_str(words, digits, byte_len, radix);
        if (size == 0) return ZERO;
        if (words[size - 1] < 0) words[size++] = 0;
        if (negative) negate(words, words, size);
        return make(words, size);
    }

    private void getAbsolute(int[] words) {
        int len;
        if (this.words == null) {
            len = 1;
            words[0] = this.ival;
        } else {
            len = this.ival;
            for (int i = len; --i >= 0; ) words[i] = this.words[i];
        }
        if (words[len - 1] < 0) negate(words, words, len);
        for (int i = words.length; --i > len; ) words[i] = 0;
    }

    private static boolean negate(int[] dest, int[] src, int len) {
        long carry = 1;
        boolean negative = src[len - 1] < 0;
        for (int i = 0; i < len; i++) {
            carry += ((long) (~src[i]) & 0xffffffffL);
            dest[i] = (int) carry;
            carry >>= 32;
        }
        return (negative && dest[len - 1] < 0);
    }

    private void setNegative(BigInt x) {
        int len = x.ival;
        if (x.words == null) {
            if (len == Integer.MIN_VALUE) set(-(long) len); else set(-len);
            return;
        }
        realloc(len + 1);
        if (negate(words, x.words, len)) words[len++] = 0;
        ival = len;
    }

    private void setNegative() {
        setNegative(this);
    }

    private static BigInt neg(BigInt x) {
        if (x.words == null && x.ival != Integer.MIN_VALUE) return valueOf(-x.ival);
        BigInt result = new BigInt(0);
        result.setNegative(x);
        return result.canonicalize();
    }

    private static int add_1(int[] dest, int[] x, int size, int y) {
        long carry = (long) y & 0xffffffffL;
        for (int i = 0; i < size; i++) {
            carry += ((long) x[i] & 0xffffffffL);
            dest[i] = (int) carry;
            carry >>= 32;
        }
        return (int) carry;
    }

    private static int add_n(int dest[], int[] x, int[] y, int len) {
        long carry = 0;
        for (int i = 0; i < len; i++) {
            carry += ((long) x[i] & 0xffffffffL) + ((long) y[i] & 0xffffffffL);
            dest[i] = (int) carry;
            carry >>>= 32;
        }
        return (int) carry;
    }

    private static int mul_1(int[] dest, int[] x, int len, int y) {
        long yword = (long) y & 0xffffffffL;
        long carry = 0;
        for (int j = 0; j < len; j++) {
            carry += ((long) x[j] & 0xffffffffL) * yword;
            dest[j] = (int) carry;
            carry >>>= 32;
        }
        return (int) carry;
    }

    private static void mul(int[] dest, int[] x, int xlen, int[] y, int ylen) {
        dest[xlen] = mul_1(dest, x, xlen, y[0]);
        for (int i = 1; i < ylen; i++) {
            long yword = (long) y[i] & 0xffffffffL;
            long carry = 0;
            for (int j = 0; j < xlen; j++) {
                carry += ((long) x[j] & 0xffffffffL) * yword + ((long) dest[i + j] & 0xffffffffL);
                dest[i + j] = (int) carry;
                carry >>>= 32;
            }
            dest[i + xlen] = (int) carry;
        }
    }

    private static long udiv_qrnnd(long N, int D) {
        long q, r;
        long a1 = N >>> 32;
        long a0 = N & 0xffffffffL;
        if (D >= 0) {
            if (a1 < ((D - a1 - (a0 >>> 31)) & 0xffffffffL)) {
                q = N / D;
                r = N % D;
            } else {
                long c = N - ((long) D << 31);
                q = c / D;
                r = c % D;
                q += 1 << 31;
            }
        } else {
            long b1 = D >>> 1;
            long c = N >>> 1;
            if (a1 < b1 || (a1 >> 1) < b1) {
                if (a1 < b1) {
                    q = c / b1;
                    r = c % b1;
                } else {
                    c = ~(c - (b1 << 32));
                    q = c / b1;
                    r = c % b1;
                    q = (~q) & 0xffffffffL;
                    r = (b1 - 1) - r;
                }
                r = 2 * r + (a0 & 1);
                if ((D & 1) != 0) {
                    if (r >= q) {
                        r = r - q;
                    } else if (q - r <= ((long) D & 0xffffffffL)) {
                        r = r - q + D;
                        q -= 1;
                    } else {
                        r = r - q + D + D;
                        q -= 2;
                    }
                }
            } else {
                if (a0 >= ((long) (-D) & 0xffffffffL)) {
                    q = -1;
                    r = a0 + D;
                } else {
                    q = -2;
                    r = a0 + D + D;
                }
            }
        }
        return (r << 32) | (q & 0xFFFFFFFFl);
    }

    private static int divmod_1(int[] quotient, int[] dividend, int len, int divisor) {
        int i = len - 1;
        long r = dividend[i];
        if ((r & 0xffffffffL) >= ((long) divisor & 0xffffffffL)) r = 0; else {
            quotient[i--] = 0;
            r <<= 32;
        }
        for (; i >= 0; i--) {
            int n0 = dividend[i];
            r = (r & ~0xffffffffL) | (n0 & 0xffffffffL);
            r = udiv_qrnnd(r, divisor);
            quotient[i] = (int) r;
        }
        return (int) (r >> 32);
    }

    private static int submul_1(int[] dest, int offset, int[] x, int len, int y) {
        long yl = (long) y & 0xffffffffL;
        int carry = 0;
        int j = 0;
        do {
            long prod = ((long) x[j] & 0xffffffffL) * yl;
            int prod_low = (int) prod;
            int prod_high = (int) (prod >> 32);
            prod_low += carry;
            carry = ((prod_low ^ 0x80000000) < (carry ^ 0x80000000) ? 1 : 0) + prod_high;
            int x_j = dest[offset + j];
            prod_low = x_j - prod_low;
            if ((prod_low ^ 0x80000000) > (x_j ^ 0x80000000)) carry++;
            dest[offset + j] = prod_low;
        } while (++j < len);
        return carry;
    }

    private static void divide(int[] zds, int nx, int[] y, int ny) {
        int j = nx;
        do {
            int qhat;
            if (zds[j] == y[ny - 1]) qhat = -1; else {
                long w = (((long) (zds[j])) << 32) + ((long) zds[j - 1] & 0xffffffffL);
                qhat = (int) udiv_qrnnd(w, y[ny - 1]);
            }
            if (qhat != 0) {
                int borrow = submul_1(zds, j - ny, y, ny, qhat);
                int save = zds[j];
                long num = ((long) save & 0xffffffffL) - ((long) borrow & 0xffffffffL);
                while (num != 0) {
                    qhat--;
                    long carry = 0;
                    for (int i = 0; i < ny; i++) {
                        carry += ((long) zds[j - ny + i] & 0xffffffffL) + ((long) y[i] & 0xffffffffL);
                        zds[j - ny + i] = (int) carry;
                        carry >>>= 32;
                    }
                    zds[j] += carry;
                    num = carry - 1;
                }
            }
            zds[j] = qhat;
        } while (--j >= ny);
    }

    private static int chars_per_word(int radix) {
        if (radix < 10) {
            if (radix < 8) {
                if (radix <= 2) return 32; else if (radix == 3) return 20; else if (radix == 4) return 16; else return 18 - radix;
            } else return 10;
        } else if (radix < 12) return 9; else if (radix <= 16) return 8; else if (radix <= 23) return 7; else if (radix <= 40) return 6; else if (radix <= 256) return 4; else return 1;
    }

    private static int count_leading_zeros(int i) {
        if (i == 0) return 32;
        int count = 0;
        for (int k = 16; k > 0; k = k >> 1) {
            int j = i >>> k;
            if (j == 0) count += k; else i = j;
        }
        return count;
    }

    private static int set_str(int dest[], byte[] str, int str_len, int base) {
        int size = 0;
        if ((base & (base - 1)) == 0) {
            int next_bitpos = 0;
            int bits_per_indigit = 0;
            for (int i = base; (i >>= 1) != 0; ) bits_per_indigit++;
            int res_digit = 0;
            for (int i = str_len; --i >= 0; ) {
                int inp_digit = str[i];
                res_digit |= inp_digit << next_bitpos;
                next_bitpos += bits_per_indigit;
                if (next_bitpos >= 32) {
                    dest[size++] = res_digit;
                    next_bitpos -= 32;
                    res_digit = inp_digit >> (bits_per_indigit - next_bitpos);
                }
            }
            if (res_digit != 0) dest[size++] = res_digit;
        } else {
            int indigits_per_limb = chars_per_word(base);
            int str_pos = 0;
            while (str_pos < str_len) {
                int chunk = str_len - str_pos;
                if (chunk > indigits_per_limb) chunk = indigits_per_limb;
                int res_digit = str[str_pos++];
                int big_base = base;
                while (--chunk > 0) {
                    res_digit = res_digit * base + str[str_pos++];
                    big_base *= base;
                }
                int cy_limb;
                if (size == 0) cy_limb = res_digit; else {
                    cy_limb = mul_1(dest, dest, size, big_base);
                    cy_limb += add_1(dest, dest, size, res_digit);
                }
                if (cy_limb != 0) dest[size++] = cy_limb;
            }
        }
        return size;
    }

    private static int cmp(int[] x, int[] y, int size) {
        while (--size >= 0) {
            int x_word = x[size];
            int y_word = y[size];
            if (x_word != y_word) {
                return (x_word ^ 0x80000000) > (y_word ^ 0x80000000) ? 1 : -1;
            }
        }
        return 0;
    }

    private static int cmp(int[] x, int xlen, int[] y, int ylen) {
        return xlen > ylen ? 1 : xlen < ylen ? -1 : cmp(x, y, xlen);
    }

    private static int rshift(int[] dest, int[] x, int x_start, int len, int count) {
        int count_2 = 32 - count;
        int low_word = x[x_start];
        int retval = low_word << count_2;
        int i = 1;
        for (; i < len; i++) {
            int high_word = x[x_start + i];
            dest[i - 1] = (low_word >>> count) | (high_word << count_2);
            low_word = high_word;
        }
        dest[i - 1] = low_word >>> count;
        return retval;
    }

    private static void rshift0(int[] dest, int[] x, int x_start, int len, int count) {
        if (count > 0) rshift(dest, x, x_start, len, count); else for (int i = 0; i < len; i++) dest[i] = x[i + x_start];
    }

    private static int lshift(int[] dest, int d_offset, int[] x, int len, int count) {
        int count_2 = 32 - count;
        int i = len - 1;
        int high_word = x[i];
        int retval = high_word >>> count_2;
        d_offset++;
        while (--i >= 0) {
            int low_word = x[i];
            dest[d_offset + i] = (high_word << count) | (low_word >>> count_2);
            high_word = low_word;
        }
        dest[d_offset + i] = high_word << count;
        return retval;
    }
}
