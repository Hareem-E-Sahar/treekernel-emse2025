package jalgebrava.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** 
 * Only factorizes (java) ints for speed
 */
public class Factorization {

    private static final int NUMBER_OF_PRIMES_WITH_SQUARE_UNDER_2_TO_31 = 4792;

    private static final int[] PRIMES = new int[NUMBER_OF_PRIMES_WITH_SQUARE_UNDER_2_TO_31];

    private static boolean isPrime(int n) {
        int i = 3;
        while (i * i <= n) {
            if (n % i == 0) {
                return false;
            }
            i += 2;
        }
        return true;
    }

    static {
        PRIMES[0] = 2;
        int np = 1;
        int i;
        for (i = 3; i * i > 0; i += 2) {
            if (isPrime(i)) {
                PRIMES[np++] = i;
            }
        }
    }

    private final Map<Integer, Integer> factors = new TreeMap<Integer, Integer>();

    public Factorization(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Can't factorize non-positive numbers");
        }
        for (int p : PRIMES) {
            if (p > n) {
                break;
            }
            int exp = 0;
            while (n % p == 0) {
                exp++;
                n /= p;
            }
            if (exp > 0) {
                factors.put(p, exp);
            }
        }
        if (n != 1) {
            factors.put(n, 1);
        }
    }

    @Override
    public String toString() {
        if (factors.size() == 0) {
            return "1";
        }
        StringBuilder sb = new StringBuilder();
        boolean empty = true;
        int lastp = 0;
        for (Map.Entry<Integer, Integer> p_e : factors.entrySet()) {
            if (!empty) {
                sb.append("*");
            }
            lastp = p_e.getKey();
            if (p_e.getValue() == 1) {
                sb.append(p_e.getKey());
            } else {
                sb.append(lastp + "^" + p_e.getValue());
            }
            empty = false;
        }
        return sb.toString();
    }

    public Set<Integer> getUniquePrimeFactors() {
        return factors.keySet();
    }

    public int occuringPowerOf(int p) {
        Integer result;
        result = factors.get(p);
        if (result == 0) {
            return 0;
        }
        return result;
    }

    public static void main(String[] args) {
        for (int i = 1; ; i++) {
            System.out.println(i + " = " + new Factorization(i));
        }
    }

    public List<Integer> primePowerFactors() {
        List<Integer> result = new LinkedList<Integer>();
        for (int p : factors.keySet()) {
            int exp = factors.get(p);
            int x = p;
            for (int i = 1; i <= exp; i++) {
                result.add(x);
                x *= p;
            }
        }
        Collections.reverse(result);
        return result;
    }
}
