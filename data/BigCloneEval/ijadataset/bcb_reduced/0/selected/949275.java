package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Entier {

    private static List<Integer> facteurL;

    public static List<Integer> facteursprem(int entier) {
        if (facteurL == null) {
            facteurL = new Factorisation(entier).factL;
            return new ArrayList<Integer>(new HashSet<Integer>(facteurL));
        } else {
            return new ArrayList<Integer>(new HashSet<Integer>(facteurL));
        }
    }

    public static List<Integer> facteurs(final int entier) {
        facteurL = new Factorisation(entier).factL;
        return facteurL;
    }

    private static class Factorisation extends Entier {

        private final List<Integer> factL;

        private Factorisation(int entier) {
            factL = new ArrayList<Integer>();
            int n = entier;
            for (int i = 2; i <= n / i; i++) {
                while (n % i == 0) {
                    factL.add(i);
                    n /= i;
                }
            }
            if (n > 1) {
                factL.add(n);
            }
        }
    }
}
