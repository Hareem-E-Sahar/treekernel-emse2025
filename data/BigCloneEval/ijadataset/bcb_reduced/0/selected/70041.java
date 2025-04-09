package p400.srm449;

/**
 * Created by IntelliJ IDEA.
 * User: smalex
 * Date: 23.09.2009
 * Time: 19:49:16
 */
public class OddDivisors {

    long find(long n) {
        if (n == 0) return 0;
        long k = (n + 1) / 2;
        System.out.println(String.format("%d * %d + find(%d/2)", k, k, n));
        return k * k + find(n / 2);
    }

    public long findSum(int N) {
        return find(N);
    }

    public static void main(String[] args) {
        System.out.println(new OddDivisors().findSum(777));
    }
}
