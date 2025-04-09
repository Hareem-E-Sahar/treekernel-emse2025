package euler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrimeFactors extends ArrayList<Double> {

    public PrimeFactors(int entier) {
        calculate(entier);
    }

    private void calculate(int entier) {
        double n = entier;
        for (double i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                add(i);
                n /= i;
            }
        }
        if (n > 1) {
            add(n);
        }
    }

    public List<Double> singlefactors() {
        Set<Double> ens = new HashSet<Double>(this);
        return new ArrayList<Double>(ens);
    }
}
