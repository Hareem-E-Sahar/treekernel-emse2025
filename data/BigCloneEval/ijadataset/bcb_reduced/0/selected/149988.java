package org.josef.science.math;

import static org.josef.annotations.Status.Stage.PRODUCTION;
import static org.josef.annotations.Status.UnitTests.COMPLETE;
import static org.josef.annotations.ThreadSafety.ThreadSafetyLevel.IMMUTABLE;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.josef.annotations.Review;
import org.josef.annotations.Status;
import org.josef.annotations.ThreadSafety;
import org.josef.util.CDebug;

/**
 * A prime factor.
 * <br>A prime factor is the result of prime factorization and is described by a
 * prime number and an exponent. For example: The prime factors of 100 are: 2^2
 * and 5^2. How do you compute the prime factors of a number? "Simply" divide by
 * all prime numbers up to the number to factorize. The problem is that it is
 * computationally expensive to generate large prime numbers in sequence. This
 * is why the factorize(...) methods in this class all use some optimization.
 * They first determine whether 2 is a factor of the supplied value and then in
 * a loop determine whether 3,5,7,9,11,13,... are factors. The loop ends when
 * the factor is larger than the square root of the supplied value.<br>
 * Note: Calculating prime factors of large numbers is very time consuming and
 * calculating with BigIntegers is much more expensive than calculating with
 * long values!<br>
 * For a technical background on WikiPedia, click
 * &lt;a href="http://en.wikipedia.org/wiki/Prime_factor"&gt;here&lt;/a&gt;.
 * @author Kees Schotanus
 * @version 1.0 $Revision: 2840 $
 */
@Status(stage = PRODUCTION, unitTests = COMPLETE)
@Review(by = "Kees Schotanus", at = "2009-10-16", reason = "Initial review")
@ThreadSafety(level = IMMUTABLE)
public final class PrimeFactor {

    /**
     * The prime number of this PrimeFactor.
     */
    private final BigInteger primeNumber;

    /**
     * The exponent of this PrimeFactor.
     */
    private final int exponent;

    /**
     * Constructs this PrimeNumber from the supplied primeNumber and exponent.
     * @param primeNumber The prime number.
     *  <br>Note: No check is made whether the supplied value is actually a
     *  prime number.
     * @param exponent The exponent.
     *  <br>Should be a positive number.
     * @throws NullPointerException When the supplied primeNumber is null.
     * @throws IllegalArgumentException When the supplied exponent is less than
     *  one.
     */
    private PrimeFactor(final BigInteger primeNumber, final int exponent) {
        CDebug.checkParameterNotNull(primeNumber, "primeNumber");
        CDebug.checkParameterTrue(exponent > 0, "Exponent must be a positive number, but is:" + exponent);
        this.primeNumber = primeNumber;
        this.exponent = exponent;
    }

    /**
     * Gets the prime number of this PrimeFactor.
     * @return The prime number of this PrimeFactor.
     */
    public BigInteger getPrimeNumber() {
        return primeNumber;
    }

    /**
     * Gets the exponent of this PrimeFactor.
     * @return The exponent of this PrimeFactor.
     */
    public int getExponent() {
        return exponent;
    }

    /**
     * Factorizes the supplied BigInteger value into its prime factors.
     * <br>For numbers smaller than {@link Long#MAX_VALUE} you are better of
     * using {@link #factorize(long)}.
     * @param value The value to factorize.
     * @return A list of prime factors.
     * @throws ArithmeticException When the supplied value is &lt; 2.
     * @throws NullPointerException When the supplied value is null.
     */
    public static List<PrimeFactor> factorize(final BigInteger value) {
        CDebug.checkParameterNotNull(value, "value");
        if (value.compareTo(BigInteger.ONE) <= 0) {
            throw new ArithmeticException("Factorization needs a value larger than one, but is:" + value);
        }
        final List<PrimeFactor> primeFactors = new ArrayList<PrimeFactor>();
        final BigInteger lastFactor = new CBigInteger(value).squareRoot();
        BigInteger factor = CBigInteger.TWO;
        BigInteger remainder = factorize(primeFactors, value, factor);
        for (factor = CBigInteger.THREE; factor.compareTo(lastFactor) < 0 && remainder.compareTo(BigInteger.ONE) != 0; factor = factor.add(CBigInteger.TWO)) {
            remainder = factorize(primeFactors, remainder, factor);
        }
        if (remainder.compareTo(BigInteger.ONE) != 0) {
            primeFactors.add(new PrimeFactor(remainder, 1));
        }
        return primeFactors;
    }

    /**
     * Determines whether the supplied BigInteger factor is a prime factor of
     * the supplied value.
     * <br>When the supplied factor is a factor of the supplied value, a prime
     * factor is added to the supplied primeFactors.
     * @param primeFactors The list of prime factors found so far.
     * @param value The value to check.
     * @param factor The factor to check whether it is a prime factor of the
     *  supplied value.
     * @return When the supplied factor is no prime factor of the supplied
     *  value then the value is returned as is, otherwise the value that remains
     *  after factoring out the supplied factor is returned.
     *   <br>For example: value=45 and factor=2. Since 2 is no factor of 45, the
     *   value 45 is returned. When the value=45 and the factor=3 then the value
     *   45 / 3 / 3 =&gt; 5 is returned.
     */
    private static BigInteger factorize(final List<PrimeFactor> primeFactors, final BigInteger value, final BigInteger factor) {
        int exponent = 0;
        BigInteger remainder = value;
        BigInteger[] divAndMod = remainder.divideAndRemainder(factor);
        while (divAndMod[1].compareTo(BigInteger.ZERO) == 0) {
            ++exponent;
            remainder = divAndMod[0];
            divAndMod = remainder.divideAndRemainder(factor);
        }
        if (exponent != 0) {
            primeFactors.add(new PrimeFactor(factor, exponent));
        }
        return remainder;
    }

    /**
     * Factorizes the supplied long value into its prime factors.
     * @param value The value to factorize.
     * @return A list of prime factors.
     * @throws ArithmeticException When the supplied value is &lt; 2.
     * @throws NullPointerException When the supplied value is null.
     */
    public static List<PrimeFactor> factorize(final long value) {
        CDebug.checkParameterNotNull(value, "value");
        if (value <= 1) {
            throw new ArithmeticException("Factorization needs a value larger than one, but is:" + value);
        }
        final List<PrimeFactor> primeFactors = new ArrayList<PrimeFactor>();
        final long lastFactor = (long) Math.sqrt(value);
        long factor = 2L;
        long remainder = factorize(primeFactors, value, factor);
        for (factor += 1L; factor < lastFactor && remainder != 1L; factor += 2L) {
            remainder = factorize(primeFactors, remainder, factor);
        }
        if (remainder != 1L) {
            primeFactors.add(new PrimeFactor(BigInteger.valueOf(remainder), 1));
        }
        return primeFactors;
    }

    /**
     * Determines whether the supplied long factor is a prime factor of the
     * supplied value.
     * <br>When the supplied factor is a factor of the supplied value, a prime
     * factor is added to the supplied primeFactors.
     * @param primeFactors The list of prime factors found so far.
     * @param value The value to check.
     * @param factor The factor to check whether it is a prime factor of the
     *  supplied value.
     * @return When the supplied factor is no prime factor of the supplied
     *  value then the value is returned as is, otherwise the value that remains
     *  after factoring out the supplied factor is returned.
     *   <br>For example: value=45 and factor=2. Since 2 is no factor of 45, the
     *   value 45 is returned. When the value=45 and the factor=3 then the value
     *   45 / 3 / 3 =&gt; 5 is returned.
     */
    private static long factorize(final List<PrimeFactor> primeFactors, final long value, final long factor) {
        int exponent = 0;
        long remainder = value;
        while (remainder % factor == 0) {
            ++exponent;
            remainder /= factor;
        }
        if (exponent != 0) {
            primeFactors.add(new PrimeFactor(BigInteger.valueOf(factor), exponent));
        }
        return remainder;
    }

    /**
     * Creates a String representation of this PrimeFactor.
     * @return A String representation of this PrimeFactor.
     */
    @Override
    public String toString() {
        if (exponent == 1) {
            return primeNumber.toString();
        }
        return primeNumber.toString() + "^" + exponent;
    }
}
