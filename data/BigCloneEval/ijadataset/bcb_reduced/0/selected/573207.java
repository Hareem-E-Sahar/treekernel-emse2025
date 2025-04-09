package hotpotato.acceptance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class Factor implements Serializable, Runnable, Callable<Factor> {

    private static final long serialVersionUID = 1L;

    private long number;

    private List<Long> factors;

    private long x;

    public Factor(long number) {
        if (number == Long.MIN_VALUE) {
            throw new IllegalArgumentException("Long.MIN_VALUE");
        }
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    public List<Long> getFactors() {
        if (factors == null) {
            throw new IllegalStateException();
        }
        return factors;
    }

    public void run() {
        this.factors = internalFactor();
    }

    public Factor call() {
        run();
        return this;
    }

    public List<Long> factor() {
        return call().getFactors();
    }

    private List<Long> internalFactor() {
        if (number == 0) {
            return Collections.singletonList(0l);
        }
        x = number;
        List<Long> factors1 = new ArrayList<Long>();
        if (x < 0) {
            factors1.add(-1l);
            x = -x;
        }
        long limit = (long) (Math.sqrt(x) + 1);
        factorOut(2, factors1);
        for (long i = 3; i <= limit && x != 1; i += 2) {
            factorOut(i, factors1);
        }
        if (factors1.isEmpty() || (factors1.size() == 1 && factors1.contains(-1l))) {
            factors1.add(x);
        }
        return factors1;
    }

    void factorOut(long i, List<Long> factors1) {
        while (x % i == 0) {
            factors1.add(i);
            x = x / i;
        }
    }
}
