package recursion;

public class Arithmetic {

    public static void main(String[] args) {
        for (int i = 1; i <= 100; ++i) {
            System.out.printf("f(%s) = %s\n", i, f(i));
        }
    }

    public static int S(int n) {
        assert n >= 0;
        if (n == 0) {
            return 0;
        } else {
            return n + S(n - 1);
        }
    }

    public static int f(int n) {
        if (n == 1) {
            return 1;
        } else if (n == 2) {
            return 1;
        } else {
            return f(n - 1) + f(n - 2);
        }
    }
}
