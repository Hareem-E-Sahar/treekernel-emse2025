package lo.local.dreamrec.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Находит значение x, при котором f(x) = 0;
 * Используется метод деления пополам.
 * Предпологается, что f(x1)>0, f(x2)<0;
 */
public abstract class EquationResolver {

    double x1, x2;

    int iterationCount = 100;

    private static final Log log = LogFactory.getLog(EquationResolver.class);

    public EquationResolver(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
        log.debug("f(x1) = " + f(x1));
        log.debug("f(x2) = " + f(x2));
        if (f(x1) > 0) {
            log.debug("f(x1) = " + f(x1));
            throw new IllegalArgumentException("f(x1) should be < 0");
        }
        if (f(x2) < 0) {
            log.debug("f(x2) = " + f(x2));
            throw new IllegalArgumentException("f(x2) should be > 0");
        }
    }

    public double getNullValue() {
        double x = 0;
        for (int i = 0; i < iterationCount; i++) {
            x = (x1 + x2) / 2;
            if (f(x) >= 0) {
                x2 = x;
            } else {
                x1 = x;
            }
            log.debug("x = " + x + ",   f(x1) = " + f(x1));
        }
        return x;
    }

    protected abstract double f(double x);
}
