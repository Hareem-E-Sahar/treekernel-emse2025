package ivia.thiago.numerosprimos;

public class NumerosPrimos {

    Long n;

    public void setN(Long n) {
        this.n = n;
    }

    public NumerosPrimos(Long number) {
        this.n = number;
    }

    public boolean isPrime() {
        if (n < 0) n *= -1;
        if (n == 0 || n == 1) return false;
        for (int a = 2; ; a++) {
            if (a >= n) break;
            for (int b = 2; ; b++) {
                double p = Math.pow(a, b);
                if (n == p) return false;
                if (p > n) break;
            }
        }
        long r;
        for (r = 2; ; r++) {
            if (getGCD(r, n) == 1) {
                long i = getMultiplicativeOrder(n, r);
                if (i < 0) return false;
                if (i > (Math.log10(n) / Math.log10(2))) break;
            }
        }
        long a = 1;
        while (a <= r) {
            long gcd = getGCD(a, n);
            if (gcd > 1 && gcd < n) return false;
            a++;
        }
        return true;
    }

    private Long getMultiplicativeOrder(Long n, Long mod) {
        for (long i = 1; ; i++) {
            double p = Math.pow(n, i);
            if (p == Double.POSITIVE_INFINITY) return -1L;
            if (p % mod == 1) return i;
        }
    }

    private Long getGCD(Long a, Long b) {
        if (b == 0) return a; else return getGCD(b, a % b);
    }

    public static void main(String[] args) {
        NumerosPrimos n = new NumerosPrimos(0L);
        for (long i = 0; i < 600; i++) {
            n.setN(i);
            if (n.isPrime()) System.out.println(i);
        }
    }
}
