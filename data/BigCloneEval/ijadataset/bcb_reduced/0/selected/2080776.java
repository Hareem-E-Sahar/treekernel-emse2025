package variosEjemplos;

/**
 * Generador de Secuencia de Fibonacci
 * Formula de fibonacci  (suma de dos numeros anteriores)
 */
public class Fibonacci {

    public static long fib(int n) {
        if (n <= 1) return n; else return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        int N = Integer.parseInt(args[0]);
        for (int i = 1; i <= N; i++) System.out.println(i + ": " + fib(i));
    }
}
