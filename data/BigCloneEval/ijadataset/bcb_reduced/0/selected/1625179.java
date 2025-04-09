package math;

import java.util.ArrayList;

public class Facteur extends ArrayList<Integer> {

    private Facteur(int entier) {
        int n = entier;
        for (int i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                add(i);
                n /= i;
            }
        }
        if (n > 1) {
            add(n);
        }
    }

    public static Facteur facteur(int entier) {
        return new Facteur(entier);
    }
}
