package gnu.math;

/** This contains various low-level routines for unsigned bigints.
 * The interfaces match the mpn interfaces in gmp,
 * so it should be easy to replace them with fast native functions
 * that are trivial wrappers around the mpn_ functions in gmp
 * (at least on platforms that use 32-bit "limbs").
 */
class MPN {

    /** Add x[0:size-1] and y, and write the size least
   * significant words of the result to dest.
   * Return carry, either 0 or 1.
   * All values are unsigned.
   * This is basically the same as gmp's mpn_add_1. */
    public static int add_1(int[] dest, int[] x, int size, int y) {
        long carry = (long) y & 0xffffffffL;
        for (int i = 0; i < size; i++) {
            carry += ((long) x[i] & 0xffffffffL);
            dest[i] = (int) carry;
            carry >>= 32;
        }
        return (int) carry;
    }

    /** Add x[0:len-1] and y[0:len-1] and write the len least
   * significant words of the result to dest[0:len-1].
   * All words are treated as unsigned.
   * @return the carry, either 0 or 1
   * This function is basically the same as gmp's mpn_add_n.
   */
    public static int add_n(int dest[], int[] x, int[] y, int len) {
        long carry = 0;
        for (int i = 0; i < len; i++) {
            carry += ((long) x[i] & 0xffffffffL) + ((long) y[i] & 0xffffffffL);
            dest[i] = (int) carry;
            carry >>>= 32;
        }
        return (int) carry;
    }

    /** Subtract Y[0:size-1] from X[0:size-1], and write
   * the size least significant words of the result to dest[0:size-1].
   * Return borrow, either 0 or 1.
   * This is basically the same as gmp's mpn_sub_n function.
   */
    public static int sub_n(int[] dest, int[] X, int[] Y, int size) {
        int cy = 0;
        for (int i = 0; i < size; i++) {
            int y = Y[i];
            int x = X[i];
            y += cy;
            cy = (y ^ 0x80000000) < (cy ^ 0x80000000) ? 1 : 0;
            y = x - y;
            cy += (y ^ 0x80000000) > (x ^ 0x80000000) ? 1 : 0;
            dest[i] = y;
        }
        return cy;
    }

    /** Multiply x[0:len-1] by y, and write the len least
   * significant words of the product to dest[0:len-1].
   * Return the most significant word of the product.
   * All values are treated as if they were unsigned
   * (i.e. masked with 0xffffffffL).
   * OK if dest==x (not sure if this is guaranteed for mpn_mul_1).
   * This function is basically the same as gmp's mpn_mul_1.
   */
    public static int mul_1(int[] dest, int[] x, int len, int y) {
        long yword = (long) y & 0xffffffffL;
        long carry = 0;
        for (int j = 0; j < len; j++) {
            carry += ((long) x[j] & 0xffffffffL) * yword;
            dest[j] = (int) carry;
            carry >>>= 32;
        }
        return (int) carry;
    }

    /**
   * Multiply x[0:xlen-1] and y[0:ylen-1], and
   * write the result to dest[0:xlen+ylen-1].
   * The destination has to have space for xlen+ylen words,
   * even if the result might be one limb smaller.
   * This function requires that xlen >= ylen.
   * The destination must be distinct from either input operands.
   * All operands are unsigned.
   * This function is basically the same gmp's mpn_mul. */
    public static void mul(int[] dest, int[] x, int xlen, int[] y, int ylen) {
        dest[xlen] = MPN.mul_1(dest, x, xlen, y[0]);
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

    public static long udiv_qrnnd(long N, int D) {
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

    /** Divide divident[0:len-1] by (unsigned int)divisor.
     * Write result into quotient[0:len-1].
     * Return the one-word (unsigned) remainder.
     * OK for quotient==dividend.
     */
    public static int divmod_1(int[] quotient, int[] dividend, int len, int divisor) {
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

    public static int submul_1(int[] dest, int offset, int[] x, int len, int y) {
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

    /** Divide zds[0:nx] by y[0:ny-1].
   * The remainder ends up in zds[0:ny-1].
   * The quotient ends up in zds[ny:nx].
   * Assumes:  nx>ny.
   * (int)y[ny-1] < 0  (i.e. most significant bit set)
   */
    public static void divide(int[] zds, int nx, int[] y, int ny) {
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

    /** Number of digits in the conversion base that always fits in a word.
   * For example, for base 10 this is 9, since 10**9 is the
   * largest number that fits into a words (assuming 32-bit words).
   * This is the same as gmp's __mp_bases[radix].chars_per_limb.
   * @param radix the base
   * @return number of digits */
    public static int chars_per_word(int radix) {
        if (radix < 10) {
            if (radix < 8) {
                if (radix <= 2) return 32; else if (radix == 3) return 20; else if (radix == 4) return 16; else return 18 - radix;
            } else return 10;
        } else if (radix < 12) return 9; else if (radix <= 16) return 8; else if (radix <= 23) return 7; else if (radix <= 40) return 6; else if (radix <= 256) return 4; else return 1;
    }

    /** Count the number of leading zero bits in an int. */
    public static int count_leading_zeros(int i) {
        if (i == 0) return 32;
        int count = 0;
        for (int k = 16; k > 0; k = k >> 1) {
            int j = i >>> k;
            if (j == 0) count += k; else i = j;
        }
        return count;
    }

    public static int set_str(int dest[], byte[] str, int str_len, int base) {
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
            int indigits_per_limb = MPN.chars_per_word(base);
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
                    cy_limb = MPN.mul_1(dest, dest, size, big_base);
                    cy_limb += MPN.add_1(dest, dest, size, res_digit);
                }
                if (cy_limb != 0) dest[size++] = cy_limb;
            }
        }
        return size;
    }

    /** Compare {@code x[0:size-1]} with {@code y[0:size-1]}, treating them as unsigned integers.
   * @return -1, 0, or 1 depending on if {@code x<y}, {@code x==y}, or {@code x>y}.
   * This is basically the same as gmp's {@code mpn_cmp} function.
   */
    public static int cmp(int[] x, int[] y, int size) {
        while (--size >= 0) {
            int x_word = x[size];
            int y_word = y[size];
            if (x_word != y_word) {
                return (x_word ^ 0x80000000) > (y_word ^ 0x80000000) ? 1 : -1;
            }
        }
        return 0;
    }

    /** Compare {@code x[0:xlen-1]} with {@code y[0:ylen-1]}, treating them as unsigned integers.
   * @return -1, 0, or 1 depending on
   * whether {@code x<y}, {@code x==y}, or {@code x>y}.
   */
    public static int cmp(int[] x, int xlen, int[] y, int ylen) {
        return xlen > ylen ? 1 : xlen < ylen ? -1 : cmp(x, y, xlen);
    }

    public static int rshift(int[] dest, int[] x, int x_start, int len, int count) {
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

    public static void rshift0(int[] dest, int[] x, int x_start, int len, int count) {
        if (count > 0) rshift(dest, x, x_start, len, count); else for (int i = 0; i < len; i++) dest[i] = x[i + x_start];
    }

    /** Return the long-truncated value of right shifting.
  * @param x a two's-complement "bignum"
  * @param len the number of significant words in x
  * @param count the shift count
  * @return (long)(x[0..len-1] >> count).
  */
    public static long rshift_long(int[] x, int len, int count) {
        int wordno = count >> 5;
        count &= 31;
        int sign = x[len - 1] < 0 ? -1 : 0;
        int w0 = wordno >= len ? sign : x[wordno];
        wordno++;
        int w1 = wordno >= len ? sign : x[wordno];
        if (count != 0) {
            wordno++;
            int w2 = wordno >= len ? sign : x[wordno];
            w0 = (w0 >>> count) | (w1 << (32 - count));
            w1 = (w1 >>> count) | (w2 << (32 - count));
        }
        return ((long) w1 << 32) | ((long) w0 & 0xffffffffL);
    }

    public static int lshift(int[] dest, int d_offset, int[] x, int len, int count) {
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

    /** Return least i such that word&(1<<i). Assumes word!=0. */
    static int findLowestBit(int word) {
        int i = 0;
        while ((word & 0xF) == 0) {
            word >>= 4;
            i += 4;
        }
        if ((word & 3) == 0) {
            word >>= 2;
            i += 2;
        }
        if ((word & 1) == 0) i += 1;
        return i;
    }

    /** Return least i such that words & (1<<i). Assumes there is such an i. */
    static int findLowestBit(int[] words) {
        for (int i = 0; ; i++) {
            if (words[i] != 0) return 32 * i + findLowestBit(words[i]);
        }
    }

    /** Calculate Greatest Common Divisior of x[0:len-1] and y[0:len-1].
    * Assumes both arguments are non-zero.
    * Leaves result in x, and returns len of result.
    * Also destroys y (actually sets it to a copy of the result). */
    public static int gcd(int[] x, int[] y, int len) {
        int i, word;
        for (i = 0; ; i++) {
            word = x[i] | y[i];
            if (word != 0) {
                break;
            }
        }
        int initShiftWords = i;
        int initShiftBits = findLowestBit(word);
        len -= initShiftWords;
        MPN.rshift0(x, x, initShiftWords, len, initShiftBits);
        MPN.rshift0(y, y, initShiftWords, len, initShiftBits);
        int[] odd_arg;
        int[] other_arg;
        if ((x[0] & 1) != 0) {
            odd_arg = x;
            other_arg = y;
        } else {
            odd_arg = y;
            other_arg = x;
        }
        for (; ; ) {
            for (i = 0; other_arg[i] == 0; ) i++;
            if (i > 0) {
                int j;
                for (j = 0; j < len - i; j++) other_arg[j] = other_arg[j + i];
                for (; j < len; j++) other_arg[j] = 0;
            }
            i = findLowestBit(other_arg[0]);
            if (i > 0) MPN.rshift(other_arg, other_arg, 0, len, i);
            i = MPN.cmp(odd_arg, other_arg, len);
            if (i == 0) break;
            if (i > 0) {
                MPN.sub_n(odd_arg, odd_arg, other_arg, len);
                int[] tmp = odd_arg;
                odd_arg = other_arg;
                other_arg = tmp;
            } else {
                MPN.sub_n(other_arg, other_arg, odd_arg, len);
            }
            while (odd_arg[len - 1] == 0 && other_arg[len - 1] == 0) len--;
        }
        if (initShiftWords + initShiftBits > 0) {
            if (initShiftBits > 0) {
                int sh_out = MPN.lshift(x, initShiftWords, x, len, initShiftBits);
                if (sh_out != 0) x[(len++) + initShiftWords] = sh_out;
            } else {
                for (i = len; --i >= 0; ) x[i + initShiftWords] = x[i];
            }
            for (i = initShiftWords; --i >= 0; ) x[i] = 0;
            len += initShiftWords;
        }
        return len;
    }

    public static int intLength(int i) {
        return 32 - count_leading_zeros(i < 0 ? ~i : i);
    }

    /** Calcaulte the Common Lisp "integer-length" function.
   * Assumes input is canonicalized:  len==IntNum.wordsNeeded(words,len) */
    public static int intLength(int[] words, int len) {
        len--;
        return intLength(words[len]) + 32 * len;
    }
}
