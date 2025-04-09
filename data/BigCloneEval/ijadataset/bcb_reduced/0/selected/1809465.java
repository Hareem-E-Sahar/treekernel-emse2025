package note;

import java.util.ArrayList;

class Factor extends ArrayList<Integer> {

    Factor(long n) {
        for (int i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                add(i);
                n = n / i;
            }
        }
        if (n > 1) {
            add((int) n);
        }
    }
}
