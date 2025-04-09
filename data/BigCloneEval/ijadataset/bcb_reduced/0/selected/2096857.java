package org.shapelogic.euler;

import java.util.Map;
import java.util.TreeMap;
import org.shapelogic.calculation.Accumulator;
import org.shapelogic.calculation.BaseAccumulator;
import org.shapelogic.calculation.AdvanceWhile;
import org.shapelogic.mathematics.ArrayOperations;
import org.shapelogic.mathematics.MaxAccumulator;
import org.shapelogic.mathematics.NaturalNumberStream;
import org.shapelogic.mathematics.PrimeNumberStream;
import org.shapelogic.mathematics.SumAccumulator;
import org.shapelogic.streams.BaseListStream0;
import org.shapelogic.streams.BaseListIndexedStream1;
import org.shapelogic.streams.BaseListStream2;
import org.shapelogic.streams.BaseListFilterStream;
import org.shapelogic.streams.ListFilterStream;
import junit.framework.TestCase;

/** First part of implementation of Project Euler. <br />
 * 
 * See: http://projecteuler.net. <br />
 * 
 * @author Sami Badawi
 *
 */
public class ProjectEuler1Test extends TestCase {

    private boolean skipSlowTest = true;

    /** Problem 1.
	 * Add all the natural numbers below 1000 that are multiples of 3 or 5.
	 * 
	 */
    public void testProblem1() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(1, 999);
        ListFilterStream<Integer> filter = new BaseListFilterStream<Integer>(naturalNumberStream) {

            public boolean evaluate(Integer object) {
                return object % 3 == 0 || object % 5 == 0;
            }
        };
        SumAccumulator accumulator = new SumAccumulator(filter);
        System.out.println(accumulator.getValue());
        assertEquals(new Long(233168), accumulator.getValue());
    }

    /** Problem 2.
	 * Find the sum of all the even-valued terms in the Fibonacci sequence 
	 * which do not exceed one million.
	 * 
	 */
    public void testProblem2() {
        BaseListIndexedStream1<Object, Integer> fibonacci = new BaseListIndexedStream1<Object, Integer>() {

            {
                _list.add(1);
                _list.add(2);
            }

            public Integer invoke(Object input, int index) {
                return get(index - 2) + get(index - 1);
            }
        };
        ListFilterStream<Integer> filter = new BaseListFilterStream<Integer>(fibonacci) {

            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        SumAccumulator accumulator = new SumAccumulator(filter) {

            {
                _inputElement = 0;
            }

            public boolean hasNext() {
                return _inputElement < 1000000;
            }
        };
        System.out.println(accumulator.getPreviousValue());
        assertEquals(new Long(1089154), accumulator.getPreviousValue());
    }

    /** Problem 3.
	 * Find the largest prime factor of 317584931803.
	 */
    public void testProblem3() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(2, null);
        BaseAccumulator<Integer, Long> accumulator = new BaseAccumulator<Integer, Long>(naturalNumberStream) {

            long theNumber = 317584931803l;

            {
                _inputElement = 0;
            }

            public Long accumulate(Integer element, Long out) {
                while (theNumber % element == 0l) {
                    theNumber /= element;
                }
                return theNumber;
            }

            public boolean hasNext() {
                return _inputElement <= theNumber;
            }
        };
        System.out.println(accumulator.getValue());
        assertEquals(new Long(3919), accumulator.getPreviousValue());
    }

    /** Problem 4.
	 * Find the largest palindrome made from the product of two 3-digit numbers.
	 */
    public void testProblem4() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(1, 999);
        BaseListStream2<Integer, Integer, Integer> cartesianProduct = new BaseListStream2<Integer, Integer, Integer>(naturalNumberStream, naturalNumberStream) {

            public Integer invoke(Integer input0, Integer input1, int index) {
                return input0 * input1;
            }
        };
        ListFilterStream<Integer> filter = new BaseListFilterStream<Integer>(cartesianProduct) {

            public boolean evaluate(Integer object) {
                return palindrome(object);
            }
        };
        MaxAccumulator accumulator = new MaxAccumulator(filter);
        System.out.println(accumulator.getValue());
        assertEquals(new Integer(906609), accumulator.getValue());
    }

    public static boolean palindrome(int input) {
        String inputAsStreing = "" + input;
        int stringLength = inputAsStreing.length();
        for (int i = 0; i < stringLength / 2; i++) {
            if (inputAsStreing.charAt(i) != inputAsStreing.charAt(stringLength - 1 - i)) return false;
        }
        return true;
    }

    public void testPalindrome() {
        assertTrue(palindrome(1001));
        assertFalse(palindrome(1011));
    }

    /** Calculated the product of factors uplifted to a power.
	 * 
	 * @param factorsWithPower the key is the factor and the value is the power.
	 */
    public static long productWithPower(Map<Integer, Integer> factorsWithPower) {
        long product = 1;
        for (Integer element : factorsWithPower.keySet()) {
            int rank = factorsWithPower.get(element);
            for (int i = 0; i < rank; i++) {
                product *= element;
            }
        }
        return product;
    }

    /** Number of this the divisor is dividable into the input.
	 * 
	 * @param input number to test
	 * @param divisor
	 * @return number of this the divisor is dividable into the input. 
	 */
    public static int rankOfDivisor(int input, int divisor) {
        int rank = 0;
        while (input % divisor == 0) {
            input /= divisor;
            rank++;
        }
        return rank;
    }

    /** Problem 5.
	 * What is the smallest number divisible by each of the numbers 1 to 20?
	 * Decompose each number from 2 to 20 into prime factors with rank. 
	 * Multiply them to get the number. 
	 */
    public void testProblem5() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(2, 20);
        final PrimeNumberStream primeNumberStream = new PrimeNumberStream();
        final AdvanceWhile<Integer> primesUnder20 = new AdvanceWhile<Integer>(primeNumberStream) {

            @Override
            public boolean evaluate(Integer input) {
                return input <= 20;
            }
        };
        Accumulator<Integer, Map<Integer, Integer>> accumulator = new BaseAccumulator<Integer, Map<Integer, Integer>>(naturalNumberStream) {

            {
                _value = new TreeMap<Integer, Integer>();
            }

            @Override
            public Map<Integer, Integer> accumulate(Integer element, Map<Integer, Integer> out) {
                for (Integer prime : primeNumberStream.getList()) {
                    if (element < prime) break;
                    int rank = rankOfDivisor(element, prime);
                    Integer currentRank = out.get(prime);
                    if (0 < rank && (currentRank == null || currentRank < rank)) out.put(prime, rank);
                }
                return out;
            }
        };
        System.out.println(productWithPower(accumulator.getValue()));
        long result = productWithPower(accumulator.getValue());
        assertEquals(232792560L, result);
    }

    /** Problem 6.
	 * What is the difference between the sum of the squares and the square of the sums?
	 */
    public void testProblem6() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(1, 100);
        BaseListIndexedStream1<Integer, Integer> squaredStream = new BaseListIndexedStream1<Integer, Integer>(naturalNumberStream) {

            public Integer invoke(Integer input, int index) {
                if (input == null) return null;
                return input * input;
            }
        };
        SumAccumulator sumOfSquares = new SumAccumulator(squaredStream);
        SumAccumulator sumOfNumbers = new SumAccumulator(naturalNumberStream);
        long result = sumOfNumbers.getValue() * sumOfNumbers.getValue() - sumOfSquares.getValue();
        System.out.println(result);
        assertEquals(25164150L, result);
    }

    /** Problem 7.
	 * Find the 10001st prime.
	 */
    public void testProblem7() {
        PrimeNumberStream primeNumberStream = new PrimeNumberStream();
        long result = primeNumberStream.get(10000);
        System.out.println(result);
        assertEquals(104743L, result);
    }

    /** Problem 8.
	 * Discover the largest product of five consecutive digits in the 1000-digit number.
	 * This is simple generate a stream of Integer.
	 * Accumulate through it looking 5 number back.
	 */
    public void testProblem8() {
        final String inputNumbers = "73167176531330624919225119674426574742355349194934" + "96983520312774506326239578318016984801869478851843" + "85861560789112949495459501737958331952853208805511" + "12540698747158523863050715693290963295227443043557" + "66896648950445244523161731856403098711121722383113" + "62229893423380308135336276614282806444486645238749" + "30358907296290491560440772390713810515859307960866" + "70172427121883998797908792274921901699720888093776" + "65727333001053367881220235421809751254540594752243" + "52584907711670556013604839586446706324415722155397" + "53697817977846174064955149290862569321978468622482" + "83972241375657056057490261407972968652414535100474" + "82166370484403199890008895243450658541227588666881" + "16427171479924442928230863465674813919123162824586" + "17866458359124566529476545682848912883142607690042" + "24219022671055626321111109370544217506941658960408" + "07198403850962455444362981230987879927244284909188" + "84580156166097919133875499200524063689912560717606" + "05886116467109405077541002256983155200055935729725" + "71636269561882670428252483600823257530420752963450";
        final int numberLength = inputNumbers.length() - 1;
        BaseListStream0<Integer> inputNumberStream = new BaseListStream0<Integer>(numberLength) {

            @Override
            public Integer invoke(int index) {
                char c = inputNumbers.charAt(index);
                return Integer.parseInt("" + c);
            }
        };
        BaseListIndexedStream1<Integer, Integer> productStream = new BaseListIndexedStream1<Integer, Integer>(inputNumberStream, numberLength) {

            public Integer invoke(Integer input, int index) {
                if (index < 4) return 0;
                int result = 1;
                for (int i = index - 4; i <= index; i++) {
                    result *= getInput(i);
                }
                ;
                return result;
            }
        };
        MaxAccumulator maxAccu = new MaxAccumulator(productStream);
        System.out.println(maxAccu.getValue());
        assertEquals(new Integer(40824), maxAccu.getValue());
    }

    /** Problem 9.
	 * Find the only Pythagorean triplet, {a, b, c}, for which a + b + c = 1000.
	 * So create a sequence for B from 1 to 500.
	 * For each A you could use a sequence from 1 to b.
	 * But this is just doing the full Cartesian product.
	 *
	 * Contraints:
	 * a < b
	 * c = 1000 - a - b
	 * c*c = a*a + b*b
	 */
    public void testProblem9() {
        NaturalNumberStream naturalNumberStream = new NaturalNumberStream(1, 500);
        final int[] EMPTY_ARRAY = new int[0];
        BaseListStream2<Integer, Integer, int[]> cartesianProduct = new BaseListStream2<Integer, Integer, int[]>(naturalNumberStream, naturalNumberStream) {

            public int[] invoke(Integer a, Integer b, int index) {
                int c = 1000 - a - b;
                if (b <= a) return EMPTY_ARRAY;
                if (c * c == a * a + b * b) return new int[] { a, b, c }; else return EMPTY_ARRAY;
            }
        };
        ListFilterStream<int[]> filter = new BaseListFilterStream<int[]>(cartesianProduct) {

            public boolean evaluate(int[] object) {
                return EMPTY_ARRAY != object;
            }
        };
        int[] result = filter.next();
        long resultNumber = ArrayOperations.product(result);
        System.out.println("result: " + ArrayOperations.product(result));
        System.out.println("a: " + result[0] + ", b: " + result[1] + ", c: " + result[2]);
        assertEquals(31875000L, resultNumber);
    }

    /** Problem 10.
	 * Calculate the sum of all the primes below one million.
	 * This takes minutes to run.
	 */
    public void testProblem10() {
        if (skipSlowTest) return;
        PrimeNumberStream primeNumberStream = new PrimeNumberStream();
        final AdvanceWhile<Integer> primesUnder1000000 = new AdvanceWhile<Integer>(primeNumberStream) {

            public boolean evaluate(Integer input) {
                return input <= 1000000;
            }
        };
        primeNumberStream.setMaxLast(primeNumberStream.getList().size() - 1);
        System.out.println("Number of primes under 1000000: " + (primeNumberStream.getList().size() - 1));
        SumAccumulator sumAccu = new SumAccumulator(primeNumberStream.getList().iterator());
        long result = sumAccu.getPreviousValue();
        System.out.println(result);
        assertEquals(37550402023L, result);
    }
}
