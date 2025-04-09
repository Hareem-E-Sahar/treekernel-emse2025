package lo.local.dreamrec.graphtools;

import java.util.logging.Logger;

/**
 * ������� �������� x, ��� ������� f(x) = 0;
 * ������������ ����� ������� �������.
 */
public class EquationResolver {

    private static final Logger LOG = Logger.getLogger(EquationResolver.class.getName());

    private double x1, x2;

    private int iterationCount = 100;

    private Function f;

    public EquationResolver(final Function f, double x1, double x2) {
        this.f = f;
        this.x1 = x1;
        this.x2 = x2;
        LOG.info("x1 = " + x1 + " f(x1) = " + f(x1));
        LOG.info("x2 = " + x2 + " f(x2) = " + f(x2));
        if (f(x2) < f(x1)) {
            LOG.info("f(x1) should be < f(x2). Invert function.");
            this.f = new Function() {

                public double getValue(double x) {
                    return -f.getValue(x);
                }
            };
        }
        if (f(x1) > 0 || f(x2) < 0) {
            String message = "f(x1) should be < 0 and f(x2) > 0";
            LOG.severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    private double f(double x) {
        return f.getValue(x);
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
        }
        LOG.info("Resulting value x = " + x + " f(x) = " + f(x));
        return x;
    }
}
