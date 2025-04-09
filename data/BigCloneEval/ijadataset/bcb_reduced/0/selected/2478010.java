package com.tce.math;

import com.tce.lang.IComparable;
import com.tce.lang.UnsupportedOperationException;
import com.tce.lang.TLong;
import java.math.BigInteger;

/**
 * Immutable arbitrary-precision integers.  All operations behave as if
 * BCDs were represented in two's-complement notation (like Java's
 * primitive integer types).  BCDs provide analogues to all of Java's
 * primitive integer operators, and all relevant static methods from
 * java.lang.Math.  Additionally, BCDs provide operations for modular
 * arithmetic, GCD calculation, primality testing, prime generation,
 * single-bit manipulation, and a few other odds and ends.
 * <P>
 * Semantics of arithmetic operations exactly mimic those of java's integer
 * arithmetic operators, as defined in The Java Language Specification.  For
 * example, division by zero throws an ArithmeticException, and division of
 * a negative by a positive yields a negative (or zero) remainder.  All of
 * the details in the Spec concerning overflow are ignored, as BCDs
 * are made as large as necessary to accommodate the results of an operation.
 * <P>
 * Comparison operations perform signed integer comparisons, analogous to
 * those performed by java's relational and equality operators.
 * <P>
 * Modular arithmetic operations are provided to compute residues, perform
 * exponentiation, and compute multiplicative inverses.  These methods always
 * return a non-negative result, between 0 and (modulus - 1), inclusive.
 *
 * @author  <A HREF="http://www.voicenet.com/~hsk0/tce">Howard S. Kapustein</A>
 * @version 0.9.1
 * @since   0.9.1
 * @see com.tce.math.TBigDecimal
 */
public final class BCD extends Number implements IComparable {

    private int m_signum = 0;

    private int m_digits = 0;

    private byte[] m_magnitude = null;

    public BCD(byte[] magnitude) {
        this(magnitude, ((magnitude == null) ? 0 : magnitude.length * 2));
    }

    public BCD(byte[] magnitude, int digits) {
        this(magnitude, digits, 0);
    }

    public BCD(byte[] magnitude, int digits, int signum) {
        if (digits < 0) throw new IllegalArgumentException("BCD Invalid digits parameter");
        if ((magnitude == null) || (magnitude.length == 0) || (digits == 0)) {
            if (signum != 0) throw new IllegalArgumentException("BCD magnitude-signum mismatch");
            return;
        } else {
            m_signum = signum;
            m_digits = digits;
            m_magnitude = new byte[(digits + 1) / 2];
            System.arraycopy(magnitude, 0, m_magnitude, 0, m_magnitude.length);
        }
    }

    public BCD(byte[] magnitude, Options.ByteArrayIsImmutable safeToReferenceTheByteArray) {
        this(magnitude, ((magnitude == null) ? 0 : magnitude.length * 2), safeToReferenceTheByteArray);
    }

    public BCD(byte[] magnitude, int digits, Options.ByteArrayIsImmutable safeToReferenceTheByteArray) {
        this(magnitude, digits, 0, safeToReferenceTheByteArray);
    }

    public BCD(byte[] magnitude, int digits, int signum, Options.ByteArrayIsImmutable safeToReferenceTheByteArray) {
        if (digits < 0) throw new IllegalArgumentException("BCD Invalid digits parameter");
        if ((magnitude == null) || (magnitude.length == 0) || (digits == 0)) {
            if (signum != 0) throw new IllegalArgumentException("BCD magnitude-signum mismatch");
            return;
        } else {
            m_signum = signum;
            m_digits = digits;
            m_magnitude = magnitude;
        }
    }

    public BCD(boolean val) {
        this(val ? 1 : 0);
    }

    public BCD(byte val) {
        this((int) val);
    }

    public BCD(char c) throws NumberFormatException {
        if (!Character.isDigit(c)) throw new NumberFormatException("BCD Invalid digit");
        ctorCharArray(Integer.toString(Character.digit(c, 10)).toCharArray());
    }

    public BCD(char[] val) throws NumberFormatException {
        if (val == null) {
            throw new NumberFormatException("BCD null char array");
        }
        ctorCharArray(val);
    }

    public BCD(char[] val, int nChars) throws NumberFormatException {
        if (val == null) {
            throw new NumberFormatException("BCD null char array");
        }
        if (nChars > val.length) {
            throw new NumberFormatException("BCD Invalid number of char");
        }
        ctorCharArray(val, nChars);
    }

    public BCD(short val) {
        this((int) val);
    }

    /**
     * The BCD magnitude for Byte.MIN_VALUE.
     */
    private static final byte[] BYTES_BYTE_MIN = { 0x28, 0x01 };

    /**
     * The BCD magnitude for Byte.MAX_VALUE.
     */
    private static final byte[] BYTES_BYTE_MAX = { 0x27, 0x01 };

    /**
     * The BCD magnitude for Short.MIN_VALUE.
     */
    private static final byte[] BYTES_SHORT_MIN = { 0x68, 0x27, 0x03 };

    /**
     * The BCD magnitude for Short.MAX_VALUE.
     */
    private static final byte[] BYTES_SHORT_MAX = { 0x67, 0x27, 0x03 };

    /**
     * The BCD magnitude for Integer.MIN_VALUE.
     */
    private static final byte[] BYTES_INT_MIN = { 0x48, 0x36, 0x48, 0x47, 0x21 };

    /**
     * The BCD magnitude for Integer.MAX_VALUE.
     */
    private static final byte[] BYTES_INT_MAX = { 0x47, 0x36, 0x48, 0x47, 0x21 };

    /**
     * The BCD magnitude for Long.MIN_VALUE.
     */
    private static final byte[] BYTES_LONG_MIN = { 0x08, 0x58, 0x77, 0x54, 0x68, 0x03, 0x72, 0x33, 0x22, 0x09 };

    /**
     * The BCD magnitude for Long.MAX_VALUE.
     */
    private static final byte[] BYTES_LONG_MAX = { 0x07, 0x58, 0x77, 0x54, 0x68, 0x03, 0x72, 0x33, 0x22, 0x09 };

    /**
     * The BCD digits for Byte.MIN_VALUE.
     */
    private static final int DIGITS_BYTE_MIN = 3;

    /**
     * The BCD digits for Byte.MAX_VALUE.
     */
    private static final int DIGITS_BYTE_MAX = 3;

    /**
     * The BCD digits for Short.MIN_VALUE.
     */
    private static final int DIGITS_SHORT_MIN = 5;

    /**
     * The BCD digits for Short.MAX_VALUE.
     */
    private static final int DIGITS_SHORT_MAX = 5;

    /**
     * The BCD digits for Integer.MIN_VALUE.
     */
    private static final int DIGITS_INT_MIN = 10;

    /**
     * The BCD digits for Integer.MAX_VALUE.
     */
    private static final int DIGITS_INT_MAX = 10;

    /**
     * The BCD digits for Long.MIN_VALUE.
     */
    private static final int DIGITS_LONG_MIN = 19;

    /**
     * The BCD digits for Long.MAX_VALUE.
     */
    private static final int DIGITS_LONG_MAX = 19;

    /**
     * 10 ** N as integers.
     * array[N] = 10**N
     * Thus:
     *
     *  if (abs(x) >= array[N]) && (abs(x) < array[N+1])
     *  then
     *      digits = N+1
     */
    private static final int[] TEN_POW_N_AS_INT = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000 };

    /**
     * 10 ** N as longs.
     * array[N] = 10**N
     * Thus:
     *
     *  if (abs(x) >= array[N]) && (abs(x) < array[N+1])
     *  then
     *      digits = N+1
     */
    private static final long[] TEN_POW_N_AS_LONG = { 1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L };

    /**
     * The number of digits for a twos-complement number who's highest bit
     * set is the index, e.g. array[n] = digits.
     */
    private static final int[] DIGITS_FOR_N_BITS = { 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13, 13, 13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19, 19, 19, 19, 1 };

    public BCD(int val) {
        if (val == 0) {
            return;
        }
        if (val == Integer.MIN_VALUE) {
            m_signum = -1;
            m_digits = DIGITS_INT_MIN;
            m_magnitude = BYTES_INT_MIN;
            return;
        }
        m_signum = (val < 0) ? -1 : 1;
        if (val < 0) {
            val = -val;
        }
        for (m_digits = 1; m_digits < TEN_POW_N_AS_INT.length; m_digits++) {
            if (val < TEN_POW_N_AS_INT[m_digits]) break;
        }
        m_magnitude = new byte[(m_digits + 1) / 2];
        int index = 0;
        do {
            int lo = val % 10;
            val /= 10;
            int hi = 0;
            if (val > 0) {
                hi = val % 10;
                val /= 10;
            }
            m_magnitude[index++] = (byte) (lo | (hi << 4));
        } while (val > 0);
    }

    public BCD(long val) {
        if (val == 0) {
            return;
        }
        if (val == Long.MIN_VALUE) {
            m_signum = -1;
            m_digits = DIGITS_LONG_MIN;
            m_magnitude = BYTES_LONG_MIN;
            return;
        }
        m_signum = (val < 0) ? -1 : 1;
        if (val < 0) {
            val = -val;
        }
        for (m_digits = 1; m_digits < TEN_POW_N_AS_LONG.length; m_digits++) {
            if (val < TEN_POW_N_AS_LONG[m_digits]) break;
        }
        m_magnitude = new byte[(m_digits + 1) / 2];
        int index = 0;
        do {
            int lo = (int) (val % 10);
            val /= 10;
            int hi = 0;
            if (val > 0) {
                hi = (int) (val % 10);
                val /= 10;
            }
            m_magnitude[index++] = (byte) (lo | (hi << 4));
        } while (val > 0);
    }

    public BCD(BigInteger val) {
        this(val.toString());
    }

    public BCD(BCD val) {
        if (val.m_signum != 0) {
            m_signum = val.m_signum;
            m_digits = val.m_digits;
            m_magnitude = val.m_magnitude;
        }
    }

    public BCD(String s) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("BCD null string");
        }
        ctorCharArray(s.toCharArray());
    }

    /**
     * Constructs the object from a <code>char</code> array. Internal helper
     * function to prevent redundant code.
     *
     * @see #ctorCharArray(char[], int)
     * @see #BCD(char[])
     * @see #BCD(String)
     */
    private void ctorCharArray(char[] ch) throws NumberFormatException {
        ctorCharArray(ch, ch.length);
    }

    /**
     * Constructs the object from a <code>char</code> array. Internal helper
     * function to prevent redundant code.
     *
     * @see #BCD(char[])
     * @see #BCD(String)
     */
    private void ctorCharArray(char[] ch, int chlen) throws NumberFormatException {
        if (chlen <= 0) {
            throw new NumberFormatException(new String(ch));
        }
        m_signum = 1;
        int index = 0;
        if (ch[0] == '-') {
            m_signum = -1;
            index++;
        }
        if (index == chlen) {
            throw new NumberFormatException(new String(ch));
        }
        while ((index < chlen) && (ch[index] == '0')) index++;
        if (index == chlen) {
            m_signum = 0;
            m_digits = 0;
            m_magnitude = null;
            return;
        }
        m_digits = chlen - index;
        m_magnitude = new byte[(m_digits + 1) / 2];
        int magnitudeIndex = 0;
        chlen--;
        while (index <= chlen) {
            int lo = Character.digit(ch[chlen--], 10);
            if (lo < 0) throw new NumberFormatException(new String(ch));
            int hi = 0;
            if (index <= chlen) {
                hi = Character.digit(ch[chlen--], 10);
                if (hi < 0) throw new NumberFormatException(new String(ch));
            }
            m_magnitude[magnitudeIndex++] = (byte) (lo | (hi << 4));
        }
    }

    /**
     * Returns a BCD with the specified value.  This factory is provided
     * in preference to a (long) constructor because it allows for reuse
     * of frequently used BCDs (like 0 and 1), obviating the need for
     * exported constants.
     */
    public static BCD valueOf(long val) {
        return new BCD(val);
    }

    /**
     * Returns a BCD whose value is (this + val).
     */
    public BCD add(BCD val) throws ArithmeticException {
        if (val.m_signum == 0) {
            return this;
        } else if (m_signum == 0) {
            return val;
        } else if (((m_digits > val.m_digits) ? m_digits : val.m_digits) < DIGITS_LONG_MIN - 1) {
            long x = longValue();
            long y = val.longValue();
            return new BCD(x + y);
        } else if (val.m_signum == m_signum) {
            byte[] xVal = m_magnitude;
            int xDigits = m_digits;
            byte[] yVal = val.m_magnitude;
            int yDigits = val.m_digits;
            int cmp = compareTo(val);
            if (((cmp > 0) && (m_signum > 0)) || ((cmp < 0) && (m_signum < 0))) {
                xVal = val.m_magnitude;
                xDigits = val.m_digits;
                yVal = m_magnitude;
                yDigits = m_digits;
            }
            int zDigits = ((xDigits > yDigits) ? xDigits : yDigits) + 1;
            byte[] zVal = new byte[((zDigits + 1) / 2) + 1];
            int carry = 0;
            int index = 0;
            int endIndex = (xDigits + 1) / 2;
            while (index < endIndex) {
                byte hiLo = xVal[index];
                int xHi = (hiLo >>> 4) & 0x0F;
                int xLo = hiLo & 0x0F;
                hiLo = yVal[index];
                int yHi = (hiLo >>> 4) & 0x0F;
                int yLo = hiLo & 0x0F;
                int zLo = xLo + yLo + carry;
                if (zLo > 9) {
                    carry = zLo / 10;
                    zLo %= 10;
                } else {
                    carry = 0;
                }
                int zHi = xHi + yHi + carry;
                if (zHi > 9) {
                    carry = zHi / 10;
                    zHi %= 10;
                } else {
                    carry = 0;
                }
                zVal[index] = (byte) (zLo | (zHi << 4));
                index++;
            }
            if (xVal.length < yVal.length) {
                while (index < yVal.length) {
                    byte hiLo = yVal[index];
                    int yHi = (hiLo >>> 4) & 0x0F;
                    int yLo = hiLo & 0x0F;
                    int zLo = yLo + carry;
                    if (zLo > 9) {
                        carry = zLo / 10;
                        zLo %= 10;
                    } else {
                        carry = 0;
                    }
                    int zHi = yHi + carry;
                    if (zHi > 9) {
                        carry = zHi / 10;
                        zHi %= 10;
                    } else {
                        carry = 0;
                    }
                    zVal[index] = (byte) (zLo | (zHi << 4));
                    index++;
                }
            }
            if (carry != 0) {
                zVal[index] = 0x01;
                zDigits = (index * 2) + 1;
            } else {
                if ((zVal[index - 1] & 0xF0) == 0) {
                    zDigits = (index * 2) - 1;
                } else {
                    zDigits = index * 2;
                }
            }
            return new BCD(zVal, zDigits, m_signum, BYTE_ARRAY_IS_IMMUTABLE);
        } else if (m_signum < 0) {
            return val.subtract(this.negate());
        } else {
            return subtract(val.negate());
        }
    }

    /**
     * Returns a BCD whose value is (this - val).
     */
    public BCD subtract(BCD val) {
        if (val.m_signum == 0) {
            return this;
        } else if (m_signum == 0) {
            return val.negate();
        } else if (((m_digits > val.m_digits) ? m_digits : val.m_digits) < DIGITS_LONG_MIN - 1) {
            long x = longValue();
            long y = val.longValue();
            return new BCD(x - y);
        } else if (val.m_signum == m_signum) {
            byte[] xVal = m_magnitude;
            int xDigits = m_digits;
            byte[] yVal = val.m_magnitude;
            int yDigits = val.m_digits;
            int cmp = compareTo(val);
            if (((cmp > 0) && (m_signum > 0)) || ((cmp < 0) && (m_signum < 0))) {
                xVal = val.m_magnitude;
                xDigits = val.m_digits;
                yVal = m_magnitude;
                yDigits = m_digits;
            }
            int zDigits = yDigits;
            byte[] zVal = new byte[(zDigits / 2) + 1];
            int carry = 0;
            int index = 0;
            int endIndex = (xDigits + 1) / 2;
            while (index < endIndex) {
                byte hiLo = xVal[index];
                int xHi = (hiLo >>> 4) & 0x0F;
                int xLo = hiLo & 0x0F;
                hiLo = yVal[index];
                int yHi = (hiLo >>> 4) & 0x0F;
                int yLo = hiLo & 0x0F;
                int zLo = yLo - xLo - carry;
                if (zLo < 0) {
                    carry = 1;
                    zLo += 10;
                } else {
                    carry = 0;
                }
                int zHi = yHi - xHi - carry;
                if (zHi < 0) {
                    carry = 1;
                    zHi += 10;
                } else {
                    carry = 0;
                }
                zVal[index] = (byte) (zLo | (zHi << 4));
                index++;
            }
            if (xVal.length < yVal.length) {
                while (index < yVal.length) {
                    byte hiLo = yVal[index];
                    int yHi = (hiLo >>> 4) & 0x0F;
                    int yLo = hiLo & 0x0F;
                    int zLo = yLo - carry;
                    if (zLo < 0) {
                        carry = 1;
                        zLo += 10;
                    } else {
                        carry = 0;
                    }
                    int zHi = yHi - carry;
                    if (zHi < 0) {
                        carry = 1;
                        zHi += 10;
                    } else {
                        carry = 0;
                    }
                    zVal[index] = (byte) (zLo | (zHi << 4));
                    index++;
                }
            }
            zDigits = (index * 2);
            if ((zVal[index - 1] & 0xF0) == 0) {
                zDigits--;
            }
            return new BCD(zVal, zDigits, cmp, BYTE_ARRAY_IS_IMMUTABLE);
        } else if (m_signum < 0) {
            return val.add(this.negate()).negate();
        } else {
            return add(val.negate());
        }
    }

    /**
     *
     */
    private int multiplyAndAccumulateEven(byte[] accumulator, int accumulatorDigits, byte[] xValue, int xDigits, int yValue, int accumulatorIndex) {
        if (yValue == 0) {
            return accumulatorDigits;
        }
        int xWholeBytes = (xDigits + 1) / 2;
        int carry = 0;
        for (int xIndex = 0; xIndex < xWholeBytes; xIndex++) {
            byte hiLo = xValue[xIndex];
            int xHi = (hiLo >>> 4) & 0x0F;
            int xLo = hiLo & 0x0F;
            hiLo = accumulator[accumulatorIndex];
            int aHi = (hiLo >>> 4) & 0x0F;
            int aLo = hiLo & 0x0F;
            aLo += xLo * yValue + carry;
            if (aLo > 9) {
                carry = aLo / 10;
                aLo %= 10;
            } else {
                carry = 0;
            }
            aHi += xHi * yValue + carry;
            if (aHi > 9) {
                carry = aHi / 10;
                aHi %= 10;
            } else {
                carry = 0;
            }
            accumulator[accumulatorIndex] = (byte) (aLo | (aHi << 4));
            accumulatorIndex++;
        }
        if (carry != 0) {
            while (carry != 0) {
                byte hiLo = accumulator[accumulatorIndex];
                int aHi = (hiLo >>> 4) & 0x0F;
                int aLo = hiLo & 0x0F;
                aLo += carry;
                if (aLo > 9) {
                    carry = aLo / 10;
                    aLo %= 10;
                } else {
                    carry = 0;
                }
                aHi += carry;
                if (aHi > 9) {
                    carry = aHi / 10;
                    aHi %= 10;
                } else {
                    carry = 0;
                }
                accumulator[accumulatorIndex] = (byte) (aLo | (aHi << 4));
                accumulatorIndex++;
            }
        }
        accumulatorDigits = (accumulatorIndex * 2);
        if ((accumulator[accumulatorIndex - 1] & 0xF0) == 0) {
            accumulatorDigits--;
        }
        return accumulatorDigits;
    }

    /**
     *
     */
    private int multiplyAndAccumulateOdd(byte[] accumulator, int accumulatorDigits, byte[] xValue, int xDigits, int yValue, int accumulatorIndex) {
        if (yValue == 0) {
            return accumulatorDigits;
        }
        int xWholeBytes = (xDigits + 1) / 2;
        int carry = 0;
        int xPreviousHi = 0;
        for (int xIndex = 0; xIndex < xWholeBytes; xIndex++) {
            byte hiLo = xValue[xIndex];
            int xHi = (hiLo >>> 4) & 0x0F;
            int xLo = hiLo & 0x0F;
            hiLo = accumulator[accumulatorIndex];
            int aHi = (hiLo >>> 4) & 0x0F;
            int aLo = hiLo & 0x0F;
            aLo += xPreviousHi * yValue + carry;
            if (aLo > 9) {
                carry = aLo / 10;
                aLo %= 10;
            } else {
                carry = 0;
            }
            aHi += xLo * yValue + carry;
            if (aHi > 9) {
                carry = aHi / 10;
                aHi %= 10;
            } else {
                carry = 0;
            }
            xPreviousHi = xHi;
            accumulator[accumulatorIndex] = (byte) (aLo | (aHi << 4));
            accumulatorIndex++;
        }
        carry += xPreviousHi * yValue;
        if (carry != 0) {
            while (carry != 0) {
                byte hiLo = accumulator[accumulatorIndex];
                int aHi = (hiLo >>> 4) & 0x0F;
                int aLo = hiLo & 0x0F;
                aLo += carry;
                if (aLo > 9) {
                    carry = aLo / 10;
                    aLo %= 10;
                } else {
                    carry = 0;
                }
                aHi += carry;
                if (aHi > 9) {
                    carry = aHi / 10;
                    aHi %= 10;
                } else {
                    carry = 0;
                }
                accumulator[accumulatorIndex] = (byte) (aLo | (aHi << 4));
                accumulatorIndex++;
            }
        }
        accumulatorDigits = (accumulatorIndex * 2);
        if ((accumulator[accumulatorIndex - 1] & 0xF0) == 0) {
            accumulatorDigits--;
        }
        return accumulatorDigits;
    }

    /**
     * Remove leading 'unnecessary' zero digits (if any).
     */
    public BCD removeLeadingZeroes() {
        int theDigits = removeLeadingZeroes(m_magnitude, m_digits);
        if (theDigits < m_digits) {
            return new BCD(m_magnitude, theDigits, m_signum, BYTE_ARRAY_IS_IMMUTABLE);
        }
        return this;
    }

    /**
     * Remove leading 'unnecessary' zero digits (if any). Minimal error checking;
     * this method assumes it's used 'correctly'.
     *
     * @param val       BCD encoded value.
     * @param digits    number of digits in the value.
     */
    private int removeLeadingZeroes(byte[] val, int digits) {
        if (digits <= 0) return 0;
        byte[] theVal = val;
        int theDigits = digits;
        int index = (theDigits - 1) / 2;
        if ((theDigits % 2) != 0) {
            if ((theVal[index] & 0x0F) == 0) {
                if (--theDigits == 0) return theDigits;
            }
            index--;
        }
        while (theDigits > 0) {
            if (theVal[index] == 0) {
                theDigits -= 2;
                if (theDigits == 0) return 0;
                index--;
                continue;
            }
            if ((theVal[index] & 0xF0) != 0) return theDigits;
            if (--theDigits == 0) return 0;
            theDigits--;
            index--;
        }
        return theDigits;
    }

    /**
     * Returns a BCD whose value is (this * val).
     */
    public BCD multiply(BCD val) {
        if ((val.m_signum == 0) || (m_signum == 0)) return ZERO; else {
            if (m_digits + val.m_digits < DIGITS_LONG_MIN - 1) {
                long x = longValue();
                long y = val.longValue();
                return new BCD(x * y);
            } else {
                byte[] xVal = m_magnitude;
                int xDigits = m_digits;
                byte[] yVal = val.m_magnitude;
                int yDigits = val.m_digits;
                if (xDigits < yDigits) {
                    xVal = val.m_magnitude;
                    xDigits = val.m_digits;
                    yVal = m_magnitude;
                    yDigits = m_digits;
                }
                int zDigits = xDigits + yDigits + 1;
                byte[] zVal = new byte[(zDigits / 2) + 1];
                zDigits = 0;
                for (int i = 0; i < (yDigits + 1) / 2; i++) {
                    zDigits = multiplyAndAccumulateEven(zVal, zDigits, xVal, xDigits, yVal[i] & 0x0F, i);
                    zDigits = multiplyAndAccumulateOdd(zVal, zDigits, xVal, xDigits, (yVal[i] >>> 4) & 0x0F, i);
                }
                zDigits = removeLeadingZeroes(zVal, zDigits);
                return new BCD(zVal, zDigits, (val.m_signum * m_signum), BYTE_ARRAY_IS_IMMUTABLE);
            }
        }
    }

    /**
     * Returns a BCD whose value is (this / val).  Throws an
     * ArithmeticException if val == 0.
     */
    public BCD divide(BCD val) throws ArithmeticException {
        if (val.m_signum == 0) throw new ArithmeticException("BCD divide by zero"); else if ((m_signum == 0) || (m_digits < val.m_digits)) return ZERO; else {
            if (m_digits < DIGITS_LONG_MIN - 1) {
                long x = longValue();
                long y = val.longValue();
                return new BCD(x / y);
            } else {
                BigInteger x = toBigInteger();
                BigInteger y = val.toBigInteger();
                return new BCD(x.divide(y));
            }
        }
    }

    /**
     * Returns a BCD whose value is (this % val).  Throws an
     * ArithmeticException if val == 0.
     */
    public BCD remainder(BCD val) throws ArithmeticException {
        if (val.m_signum == 0) throw new ArithmeticException("BCD divide by zero"); else if (m_signum == 0) return ZERO; else if (m_digits < val.m_digits) return this; else {
            if (m_digits < DIGITS_LONG_MIN - 1) {
                long x = longValue();
                long y = val.longValue();
                return new BCD(x % y);
            } else {
                BigInteger x = toBigInteger();
                BigInteger y = val.toBigInteger();
                return new BCD(x.remainder(y));
            }
        }
    }

    /**
     * Returns an array of two BCDs. The first ([0]) element of
     * the return value is the quotient (this / val), and the second ([1])
     * element is the remainder (this % val).  Throws an ArithmeticException
     * if val == 0.
     */
    public BCD[] divideAndRemainder(BCD val) throws ArithmeticException {
        if (val.m_signum == 0) {
            throw new ArithmeticException("BCD divide by zero");
        } else if (m_signum == 0) {
            return new BCD[] { ZERO, ZERO };
        } else if (m_digits < val.m_digits) {
            return new BCD[] { ZERO, this };
        } else {
            if (m_digits < DIGITS_LONG_MIN - 1) {
                long x = longValue();
                long y = val.longValue();
                return new BCD[] { new BCD(x / y), new BCD(x % y) };
            } else {
                BigInteger x = toBigInteger();
                BigInteger y = val.toBigInteger();
                BigInteger[] bi = x.divideAndRemainder(y);
                return new BCD[] { new BCD(bi[0]), new BCD(bi[1]) };
            }
        }
    }

    /**
     * Returns a BCD whose value is (this ** exponent).  Throws
     * an ArithmeticException if exponent < 0 (as the operation would yield
     * a non-integer value). Note that exponent is an integer rather than
     * a BCD.
     */
    public BCD pow(int exponent) throws ArithmeticException {
        if (exponent < 0) throw new ArithmeticException("BCD Negative exponent"); else if (exponent == 0) return ONE; else if (m_signum == 0) return this;
        BCD result = ONE;
        BCD baseToPow2 = this;
        while (exponent != 0) {
            if ((exponent & 1) == 1) result = result.multiply(baseToPow2);
            if ((exponent >>= 1) != 0) baseToPow2 = baseToPow2.multiply(baseToPow2);
        }
        return result;
    }

    /**
     * Returns a BCD whose value is the greatest common denominator
     * of abs(this) and abs(val).  Returns 0 if this == 0 && val == 0.
     */
    public BCD gcd(BCD val) {
        if (val.m_signum == 0) return abs(); else if (m_signum == 0) return val.abs(); else throw new UnsupportedOperationException();
    }

    /**
    * Returns a BCD whose value is the absolute value of this
    * number.
    */
    public BCD abs() {
        return (m_signum >= 0) ? this : negate();
    }

    /**
     * Returns a BCD whose value is (-1 * this).
     */
    public BCD negate() {
        return new BCD(m_magnitude, m_digits, -m_signum, BYTE_ARRAY_IS_IMMUTABLE);
    }

    /**
     * Returns the signum function of this number (i.e., -1, 0 or 1 as
     * the value of this number is negative, zero or positive).
     */
    public int signum() {
        return m_signum;
    }

    /**
     * Returns the digits in this number.
     */
    public int digits() {
        return m_digits;
    }

    /**
     * Returns the N'th digit in this number.
     */
    public int getDigit(int index) {
        if (m_signum == 0) {
            if (index != 0) throw new IndexOutOfBoundsException("BCD Invalid index parameter"); else return 0;
        }
        if ((index < 0) || (index >= m_digits)) throw new IndexOutOfBoundsException("BCD Invalid index parameter");
        int nByte = index / 2;
        byte b = m_magnitude[nByte];
        return ((index % 2) == 0) ? (b & 0x0F) : ((b >>> 4) & 0x0F);
    }

    /**
     * Returns a BCD whose value is (this * 10**N), effectively
     * shifting the value left by N digits.
     *
     * @see #shiftRight(int)
     */
    public BCD shiftLeft(int digits) {
        if (digits < 0) throw new IllegalArgumentException("BCD Invalid digits parameter"); else if (digits == 0) return this;
        if (m_signum == 0) return ZERO;
        int newDigits = m_digits + digits;
        if ((digits % 2) == 0) {
            int nBytesToAdd = digits / 2;
            int len = m_magnitude.length + nBytesToAdd;
            byte[] b = new byte[len];
            System.arraycopy(m_magnitude, 0, b, nBytesToAdd, m_magnitude.length);
            return new BCD(b, newDigits, m_signum, BYTE_ARRAY_IS_IMMUTABLE);
        } else {
            int newLen = (newDigits + 1) / 2;
            byte[] newMagnitude = new byte[newLen];
            int nBytesToShift = digits / 2;
            System.arraycopy(m_magnitude, 0, newMagnitude, nBytesToShift, m_magnitude.length);
            byte prevHi = 0;
            int index = 0;
            while (index < newLen - 1) {
                byte thisHi = newMagnitude[index];
                newMagnitude[index] = (byte) ((newMagnitude[index] << 4) | ((prevHi >>> 4) & 0x0F));
                prevHi = thisHi;
                index++;
            }
            newMagnitude[index] <<= 4;
            newMagnitude[index] |= (byte) ((prevHi >>> 4) & 0x0F);
            return new BCD(newMagnitude, newDigits, m_signum, BYTE_ARRAY_IS_IMMUTABLE);
        }
    }

    /**
     * Returns a BCD whose value is (this / 10**N), effectively
     * shifting the value right by N digits. Shifting the value by equal
     * or more than the current number of digits yields zero, e.g.
     * <code>
     *      BCD b = new BCD("123").shiftRight(3);
     *      // b.equals(ZERO) == true
     * </code>
     *
     * @see #shiftRight(int)
     */
    public BCD shiftRight(int digits) {
        if (digits < 0) throw new IllegalArgumentException("BCD Invalid digits parameter");
        if (digits >= m_digits) {
            return ZERO;
        }
        int newDigits = m_digits - digits;
        if ((digits % 2) == 0) {
            int nBytesToDrop = digits / 2;
            int len = m_magnitude.length - nBytesToDrop;
            byte[] b = new byte[len];
            System.arraycopy(m_magnitude, nBytesToDrop, b, 0, len);
            return new BCD(b, newDigits, m_signum, BYTE_ARRAY_IS_IMMUTABLE);
        } else {
            if (m_signum < 0) newDigits++;
            return new BCD(toString().substring(0, newDigits));
        }
    }

    public int compareTo(Object val) {
        return compareTo((BCD) val);
    }

    /**
     * Returns -1, 0 or 1 as this number is less than, equal to, or
     * greater than val.  This method is provided in preference to
     * individual methods for each of the six boolean comparison operators
     * (<, ==, >, >=, !=, <=).  The suggested idiom for performing these
     * comparisons is:  (x.compareTo(y) <op> 0), where <op> is one of the
     * six comparison operators.
     */
    public int compareTo(BCD val) {
        if (m_signum != val.m_signum) {
            return (m_signum > val.m_signum) ? 1 : -1;
        } else if (m_signum == 0) {
            return 0;
        } else if (m_digits > val.m_digits) {
            return (m_signum > 0) ? 1 : -1;
        } else if (m_digits < val.m_digits) {
            return (m_signum < 0) ? 1 : -1;
        } else {
            for (int index = (m_digits - 1) / 2; index >= 0; index--) {
                int left = m_magnitude[index] & 0x00ff;
                int right = val.m_magnitude[index] & 0x00ff;
                if (left != right) return (left < right) ? -1 : 1;
            }
            return 0;
        }
    }

    /**
     * Returns true iff x is a BCD whose value is equal to this number.
     * This method is provided so that BCDs can be used as hash keys.
     */
    public boolean equals(Object x) {
        if (!(x instanceof BCD)) return false;
        BCD xBCD = (BCD) x;
        if ((xBCD.m_signum != m_signum) || (xBCD.m_digits != m_digits)) return false;
        if (xBCD == this) return true;
        if (m_signum != 0) {
            for (int i = 0; i < m_magnitude.length; i++) {
                if (xBCD.m_magnitude[i] != m_magnitude[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the BCD whose value is the lesser of this and val.
     * If the values are equal, either may be returned.
     */
    public BCD min(BCD val) {
        return (compareTo(val) < 0 ? this : val);
    }

    /**
     * Returns the BCD whose value is the greater of this and val.
     * If the values are equal, either may be returned.
     */
    public BCD max(BCD val) {
        return (compareTo(val) > 0 ? this : val);
    }

    /**
     * Computes a hash code for this object.
     */
    public int hashCode() {
        if (m_signum == 0) {
            return 0;
        }
        int hashCode = 0;
        for (int i = 0; i < m_magnitude.length; i++) hashCode = 37 * hashCode + (m_magnitude[i] & 0xff);
        return hashCode * m_signum;
    }

    /**
     * All possible chars for representing a number as a String
     */
    private static final char[] g_digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    /**
     * Returns the string representation of this number in the given radix.
     * If the radix is outside the range from Character.MIN_RADIX(2) to
     * Character.MAX_RADIX(36) inclusive, it will default to 10 (as is the
     * case for Integer.toString).  The digit-to-character mapping provided
     * by Character.forDigit is used, and a minus sign is prepended if
     * appropriate.  (This representation is compatible with the (String, int)
     * constructor.)
     * <P>
     * toString(10) is functionality equivalent to toString() but the latter
     * is highly optimized for base-10 values. If you're just using base-10
     * values, call toString() instead of toString(10).
     *
     * @see #toString()
     */
    public String toString(int radix) {
        return toStringBuffer(radix).toString();
    }

    /**
     * Returns the string buffer representation of this number in the given radix.
     * If the radix is outside the range from Character.MIN_RADIX(2) to
     * Character.MAX_RADIX(36) inclusive, it will default to 10 (as is the
     * case for Integer.toString).  The digit-to-character mapping provided
     * by Character.forDigit is used, and a minus sign is prepended if
     * appropriate.  (This representation is compatible with the (String, int)
     * constructor.)
     * <P>
     * toString(10) is functionality equivalent to toString() but the latter
     * is highly optimized for base-10 values. If you're just using base-10
     * values, call toStringBuffer() instead of toStringBuffer(10).
     *
     * @see #toStringBuffer()
     */
    public StringBuffer toStringBuffer(int radix) {
        if ((compareTo(LONG_MIN) >= 0) && (compareTo(LONG_MAX) <= 0)) {
            return new StringBuffer(TLong.toString(longValue(), radix));
        } else {
            return new StringBuffer(toBigInteger().toString(radix));
        }
    }

    /**
     * Converts this number to a BigInteger.  Standard narrowing primitive conversion
     * as per The Java Language Specification.
     */
    public BigInteger toBigInteger() {
        return new BigInteger(toString());
    }

    /**
     * Returns the string representation of this number, radix 10.  The
     * digit-to-character mapping provided by Character.forDigit is used,
     * and a minus sign is prepended if appropriate.  (This representation
     * is compatible with the (String) constructor, and allows for string
     * concatenation with Java's + operator.)
     *
     * @see #toString(int)
     */
    public String toString() {
        return toStringBuffer().toString();
    }

    /**
     * Returns the string buffer representation of this number, radix 10.  The
     * digit-to-character mapping provided by Character.forDigit is used,
     * and a minus sign is prepended if appropriate.  (This representation
     * is compatible with the (String) constructor.)
     *
     * @see #toStringBuffer(int)
     */
    public StringBuffer toStringBuffer() {
        if (m_signum == 0) {
            return new StringBuffer("0");
        }
        StringBuffer sb = new StringBuffer(((m_signum < 0) ? 1 : 0) + m_digits);
        if (m_signum < 0) {
            sb.append('-');
        }
        int nbytes = m_digits / 2;
        if ((m_digits % 2) != 0) {
            sb.append(g_digits[m_magnitude[nbytes]]);
        }
        for (int i = nbytes - 1; i >= 0; i--) {
            byte hiLo = m_magnitude[i];
            int hi = (hiLo >>> 4) & 0x0F;
            int lo = hiLo & 0x0F;
            sb.append(g_digits[hi]);
            sb.append(g_digits[lo]);
        }
        return sb;
    }

    /**
     * Returns the binary-coded-decimal representation of this number.
     * The array is little-endian (i.e., the least significant byte
     * is in the [0] position), with each base-10 digit stored as a
     * nibble (4-bits) in big-endian format with each byte (7..4 = MSN,
     * 3..0 = LSN).
     *
     * This representation is compatible with the (byte[]) constructor.
     *
     * @returns     null if the value is zero, otherwise the byte array.
     * @see #BCD(byte[])
     * @see #toByteArrayNeverNull()
     */
    public byte[] toByteArray() {
        if (m_signum == 0) {
            return null;
        } else {
            byte[] b = new byte[m_magnitude.length];
            System.arraycopy(m_magnitude, 0, b, 0, m_magnitude.length);
            return b;
        }
    }

    /**
     * Returns the binary-coded-decimal representation of this number,
     * but guaranteed never to return <code>null</code>. Unlike toByteArray(),
     * a zero-length array is returned if the value is zero.
     *
     * This representation is compatible with the (byte[]) constructor.
     *
     * @returns     the byte array.
     * @see #BCD(byte[])
     * @see #toByteArray()
     */
    public byte[] toByteArrayNeverNull() {
        byte[] b = toByteArray();
        return (b == null) ? new byte[0] : b;
    }

    /**
     * Returns the BCD as a character array of type char[],
     * as though the sequence toString().toCharArray() had been used.
     *
     * @return      this BCD converted to a character array.
     * @see         #toString()
     */
    public char[] toCharArray() {
        return toString().toCharArray();
    }

    /**
     * Returns the BCD as a character array of type char[],
     * as though the sequence toString().toCharArray() had been used.
     *
     * @return      this BCD converted to a character array.
     * @see         #toString()
     */
    public char[] toCharArray(int extraChars) {
        if (m_signum == 0) {
            char[] ch = new char[1 + extraChars];
            ch[0] = '0';
            return ch;
        }
        char[] ch = new char[((m_signum < 0) ? 1 : 0) + m_digits + extraChars];
        int index = 0;
        if (m_signum < 0) {
            ch[index++] = '-';
        }
        int nbytes = m_digits / 2;
        if ((m_digits % 2) != 0) {
            ch[index++] = g_digits[m_magnitude[nbytes]];
        }
        for (int i = nbytes - 1; i >= 0; i--) {
            byte hiLo = m_magnitude[i];
            int hi = (hiLo >>> 4) & 0x0F;
            int lo = hiLo & 0x0F;
            ch[index++] = g_digits[hi];
            ch[index++] = g_digits[lo];
        }
        return ch;
    }

    /**
     * Converts this number to an int.  Standard narrowing primitive conversion
     * as per The Java Language Specification.
     */
    public int intValue() {
        if (m_signum == 0) {
            return 0;
        }
        int result = 0;
        int digitsLeft = m_digits;
        if ((digitsLeft % 2) != 0) {
            result = m_magnitude[digitsLeft / 2] & 0x0F;
            digitsLeft--;
        }
        int index = digitsLeft / 2;
        while (index > 0) {
            byte hiLo = m_magnitude[--index];
            result = (result * 100) + (((hiLo >>> 4) & 0x0F) * 10) + (hiLo & 0x0F);
        }
        return (m_signum < 0) ? -result : result;
    }

    /**
     * Converts this number to a long.  Standard narrowing primitive conversion
     * as per The Java Language Specification.
     */
    public long longValue() {
        if (m_signum == 0) {
            return 0;
        }
        long result = 0;
        int digitsLeft = m_digits;
        if ((digitsLeft % 2) != 0) {
            result = m_magnitude[digitsLeft / 2] & 0x0F;
            digitsLeft--;
        }
        int index = digitsLeft / 2;
        while (index > 0) {
            byte hiLo = m_magnitude[--index];
            result = (result * 100) + (((hiLo >>> 4) & 0x0F) * 10) + (hiLo & 0x0F);
        }
        return (m_signum < 0) ? -result : result;
    }

    /**
     * Converts this number to a float.  Similar to the double-to-float
     * narrowing primitive conversion defined in The Java Language
     * Specification: if the number has too great a magnitude to represent
     * as a float, it will be converted to infinity or negative infinity,
     * as appropriate.
     */
    public float floatValue() {
        return Float.valueOf(toString()).floatValue();
    }

    /**
     * Converts the number to a double.  Similar to the double-to-float
     * narrowing primitive conversion defined in The Java Language
     * Specification: if the number has too great a magnitude to represent
     * as a double, it will be converted to infinity or negative infinity,
     * as appropriate.
     */
    public double doubleValue() {
        return Double.valueOf(toString()).doubleValue();
    }

    /**
     * Converts this BCD to a boolean.
     *
     * @return  <code>true</code> if the value is non-zero,
     *          <code>false</code> if the value is zero.
     */
    public boolean booleanValue() {
        return m_signum != 0;
    }

    /**
     * Converts this number to a byte.  Standard narrowing primitive conversion
     * as per The Java Language Specification.
     */
    public byte byteValue() {
        return (byte) intValue();
    }

    /**
     * Converts this number to a short.  Standard narrowing primitive conversion
     * as per The Java Language Specification.
     */
    public short shortValue() {
        return (short) intValue();
    }

    /**
     * Dump the internal state.
     */
    public String dumpToString() {
        StringBuffer sb = new StringBuffer();
        sb.append("signum=" + m_signum + ", digits=" + m_digits + ", magnitude=");
        if (m_magnitude == null) {
            sb.append("<null>");
        } else {
            for (int i = 0; i < m_magnitude.length; i++) {
                byte b = m_magnitude[i];
                int hi = (b >>> 4) & 0x0F;
                int lo = b & 0x0F;
                if (i > 0) sb.append(" ");
                sb.append(hi);
                sb.append(lo);
            }
        }
        return sb.toString();
    }

    private static final long serialVersionUID = 5690631037247216495L;

    /**
     * Reconstitute the <tt>BCD</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (m_magnitude != null) m_magnitude = (byte[]) m_magnitude.clone();
        if ((m_signum < -1) || (m_signum > 1)) throw new java.io.StreamCorruptedException("BCD: Invalid signum value");
        if ((m_magnitude == null) != (m_signum == 0)) throw new java.io.StreamCorruptedException("BCD: signum-magnitude mismatch");
        if ((m_digits == 0) != (m_signum == 0)) throw new java.io.StreamCorruptedException("BCD: signum-digits mismatch");
        if (m_digits < 0) throw new java.io.StreamCorruptedException("BCD: Invalid digits value");
        if ((m_magnitude == null) != (m_digits == 0)) throw new java.io.StreamCorruptedException("BCD: digits-magnitude mismatch");
        if ((m_magnitude != null) && (m_magnitude.length != ((m_digits + 1) / 2))) throw new java.io.StreamCorruptedException("BCD: digits-magnitude mismatch");
    }

    /**
     * 'Marker' class, solely to create a unique data type for method overloading.
     *
     * @see BYTE_ARRAY_IS_IMMUTABLE
     */
    interface Options {

        final class ByteArrayIsImmutable {

            protected ByteArrayIsImmutable() {
            }
        }

        final ByteArrayIsImmutable BYTE_ARRAY_IS_IMMUTABLE = new ByteArrayIsImmutable();
    }

    /**
     * Indicates a byte array is immutable and can be safely referenced
     * (facilitating fast assignment/reference rather than slower deep
     * copy/new byte[]).
     */
    public static final Options.ByteArrayIsImmutable BYTE_ARRAY_IS_IMMUTABLE = Options.BYTE_ARRAY_IS_IMMUTABLE;

    /**
     * A BCD object whose value is exactly 0.
     */
    public static final BCD ZERO = new BCD(0);

    /**
     * A BCD object whose value is exactly 1.
     */
    public static final BCD ONE = new BCD(1);

    /**
     * A BCD object whose value is exactly 2.
     */
    public static final BCD TWO = new BCD(2);

    /**
     * A BCD object whose value is exactly 10.
     */
    public static final BCD TEN = new BCD(10);

    /**
     * A BCD object whose value is exactly Byte.MIN_VALUE.
     */
    public static final BCD BYTE_MIN = new BCD(BYTES_BYTE_MIN, DIGITS_BYTE_MIN, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Byte.MAX_VALUE.
     */
    public static final BCD BYTE_MAX = new BCD(BYTES_BYTE_MAX, DIGITS_BYTE_MAX, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Short.MIN_VALUE.
     */
    public static final BCD SHORT_MIN = new BCD(BYTES_SHORT_MIN, DIGITS_SHORT_MIN, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Short.MAX_VALUE.
     */
    public static final BCD SHORT_MAX = new BCD(BYTES_SHORT_MAX, DIGITS_SHORT_MAX, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Integer.MIN_VALUE.
     */
    public static final BCD INT_MIN = new BCD(BYTES_INT_MIN, DIGITS_INT_MIN, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Integer.MAX_VALUE.
     */
    public static final BCD INT_MAX = new BCD(BYTES_INT_MAX, DIGITS_INT_MAX, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Long.MIN_VALUE.
     */
    public static final BCD LONG_MIN = new BCD(BYTES_LONG_MIN, DIGITS_LONG_MIN, -1, BYTE_ARRAY_IS_IMMUTABLE);

    /**
     * A BCD object whose value is exactly Long.MAX_VALUE.
     */
    public static final BCD LONG_MAX = new BCD(BYTES_LONG_MAX, DIGITS_LONG_MAX, -1, BYTE_ARRAY_IS_IMMUTABLE);
}
