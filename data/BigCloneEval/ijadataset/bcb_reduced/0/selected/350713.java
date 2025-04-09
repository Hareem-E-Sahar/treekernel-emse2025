package genomicMap.worker.helper;

import javautil.io.LogUtil;

public class PhysicalMapHelper {

    public static void main(String[] args) {
    }

    public PhysicalMapHelper() {
    }

    public static double[] getR(int ch_length, int clone_length, int probe_count, int clone_count, double[][] aa, int[] probeOrder) {
        double sum = 0;
        double[] R = new double[clone_count];
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            sum = 0.0;
            for (int probeIndex = 0; probeIndex < probe_count - 1; probeIndex++) {
                sum += aa[cloneIndex][probeOrder[probeIndex]] * aa[cloneIndex][probeOrder[probeIndex + 1]];
            }
            R[cloneIndex] = ch_length - probe_count * clone_length + clone_length * sum;
        }
        return R;
    }

    public static double[] Initial_Ys(int[] probeOrder, int probe_count, int clone_count, int ch_length, int clone_length, int gap, int[][] joint) {
        int w, mmax, mmin, msum, j, un, t1, t2;
        double A, eps, t3, t4, eps1, sum;
        double[] y = new double[probe_count + 1];
        eps1 = 0.1;
        msum = 0;
        mmax = 0;
        mmin = clone_count;
        w = 0;
        int[] link = new int[probe_count];
        int[] ulink = new int[probe_count];
        un = 0;
        for (int probeIndex = 1; probeIndex < probe_count; probeIndex++) {
            t1 = joint[probeOrder[probeIndex] - 1][probeOrder[probeIndex - 1] - 1];
            if (t1 > 0) {
                msum += t1;
                if (mmax < t1) {
                    mmax = t1;
                }
                if (mmin > t1) {
                    mmin = t1;
                }
                link[w] = probeIndex;
                w++;
            } else {
                ulink[un] = probeIndex;
                un++;
            }
        }
        A = (double) gap - ((probe_count + 1 - w) * (clone_length - eps1));
        if (A <= 0.0) {
            y[0] = gap / (double) (probe_count + 1 - w);
            for (int probeIndex = 0; probeIndex < un; probeIndex++) {
                y[ulink[probeIndex]] = y[0];
            }
            for (int probeIndex = 0; probeIndex < w; probeIndex++) {
                y[link[probeIndex]] = 0.0;
            }
        } else {
            eps = A / (2 * w);
            y[0] = (gap - A) / (probe_count + 1 - w);
            for (int probeIndex = 0; probeIndex < un; probeIndex++) {
                y[ulink[probeIndex]] = y[0];
            }
            if (((clone_length - eps) * w) <= A) {
                for (int probeIndex = 0; probeIndex < w; probeIndex++) {
                    y[link[probeIndex]] = clone_length - eps;
                }
            } else {
                t1 = (mmax * w) - msum;
                t2 = msum - (mmin * w);
                if (t1 == 0) {
                    for (int probeIndex = 0; probeIndex < w; probeIndex++) {
                        y[link[probeIndex]] = A / w;
                    }
                } else {
                    t3 = (A - (eps * w)) / t1;
                    t4 = ((w * (clone_length - eps)) - A) / t2;
                    if (t3 < t4) {
                        for (int probeIndex = 0; probeIndex < w; probeIndex++) {
                            un = joint[probeOrder[link[probeIndex]] - 1][probeOrder[link[probeIndex] - 1] - 1];
                            y[link[probeIndex]] = ((A * (mmax - un)) + (eps * ((w * un) - msum))) / t1;
                        }
                    } else {
                        for (int probeIndex = 0; probeIndex < w; probeIndex++) {
                            un = joint[probeOrder[link[probeIndex] - 1]][probeOrder[link[probeIndex] - 1] - 1];
                            y[link[probeIndex]] = (((clone_length - eps) * (msum - (w * un))) + (A * (un - mmin))) / t2;
                        }
                    }
                }
            }
        }
        sum = 0.0;
        for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
            sum += y[probeIndex];
        }
        y[probe_count] = gap - sum;
        for (int index = 0; index < y.length; index++) {
            if (y[index] < 0.0) y[index] = -y[index];
        }
        return y;
    }

    /**
     * STEEPEST DESCENT ALGORITHM. GIVEN A VECTOR Y, MOVE TO THE MINIMUM ALONG THE GRADIENT DIRECTION
     */
    public static double Conj(double[] y, int probe_count, int clone_count, double[][] aa, int[] probeOrder, int clone_length, double const1, double[] R) {
        int dir;
        double old_value, new_value, shigh, slow, delta, gg, dgg, gam;
        double CRITERIA_CONJ = 0.00001;
        double[] direction = new double[probe_count + 1];
        double[] d1 = new double[probe_count + 1];
        double[] d2 = new double[probe_count + 1];
        new_value = old_value = Func(const1, R, aa, y, clone_length, probe_count, clone_count, probeOrder);
        direction = Gradient(y, aa, R, probe_count, clone_count, clone_length, probeOrder);
        direction[probe_count] = 0.0;
        for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
            direction[probeIndex] = d1[probeIndex] = d2[probeIndex] = -direction[probeIndex];
            direction[probe_count] -= direction[probeIndex];
        }
        while (1 < 2) {
            direction = Project(direction, y, clone_length, probe_count);
            dir = 0;
            for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                if (!(direction[probeIndex] >= -0.0001 && direction[probeIndex] <= 0.0001)) {
                    dir = 1;
                }
            }
            if (dir == 0) {
                break;
            }
            shigh = BracketSHigh(y, direction, probe_count, clone_length);
            slow = BracketSLow(y, direction, probe_count, clone_length);
            if (shigh > 0) {
                shigh -= 1.0e-10;
            }
            y = Line_Minimize(shigh, slow, y, direction, probe_count, clone_count, aa, probeOrder, clone_length, const1, R);
            old_value = new_value;
            new_value = Func(const1, R, aa, y, clone_length, probe_count, clone_count, probeOrder);
            delta = old_value - new_value;
            if (delta < CRITERIA_CONJ) {
                break;
            }
            Gradient(y, aa, R, probe_count, clone_count, clone_length, probeOrder);
            gg = dgg = 0.0;
            for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                gg += d2[probeIndex] * d2[probeIndex];
                dgg += (direction[probeIndex] + d2[probeIndex]) * direction[probeIndex];
            }
            gam = dgg / gg;
            direction[probe_count] = 0.0;
            for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                d2[probeIndex] = -direction[probeIndex];
                direction[probeIndex] = d1[probeIndex] = d2[probeIndex] + gam * d1[probeIndex];
                direction[probe_count] -= direction[probeIndex];
            }
        }
        return new_value;
    }

    /**
     * FUNCTION TO EVALUATE THE FUNCTION VALUE AT POINT Y
     */
    private static double Func(double const1, double[] R, double[][] aa, double[] y, int clone_length, int probe_count, int clone_count, int[] probeOrder) {
        double ret;
        double[] sum = new double[clone_count];
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            sum[cloneIndex] = 0.0;
            sum[cloneIndex] -= (1 - aa[cloneIndex][probeOrder[0]]) * (1 - aa[cloneIndex][0]) * Min(y[0], (double) clone_length);
            for (int probeIndex = 1; probeIndex < probe_count; probeIndex++) {
                sum[cloneIndex] -= (1 - aa[cloneIndex][probeOrder[probeIndex]]) * (1 - aa[cloneIndex][probeOrder[probeIndex - 1]]) * Min(y[probeIndex], (double) clone_length);
            }
        }
        ret = 0;
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            sum[cloneIndex] += R[cloneIndex];
            if (sum[cloneIndex] <= 0) {
                if (cloneIndex == 0) {
                    sum[cloneIndex] = 1;
                } else {
                    sum[cloneIndex] = sum[cloneIndex - 1];
                }
            }
            ret -= Math.log(sum[cloneIndex]);
        }
        ret += const1;
        return ret;
    }

    private static double Min(double x1, double x2) {
        if (x1 < x2) {
            return x1;
        } else {
            return x2;
        }
    }

    /**
     * RETURNS THE GRADIENT OF THE FUNCTION
     */
    private static double[] Gradient(double[] y, double[][] aa, double[] R, int probe_count, int clone_count, int clone_length, int[] probeOrder) {
        double grad, den, num;
        double[] gradient = new double[probe_count + 1];
        double[] den_vec = new double[clone_count];
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            den_vec[cloneIndex] = 0;
            den_vec[cloneIndex] -= (1 - aa[cloneIndex][probeOrder[0]]) * (1 - aa[cloneIndex][0]) * Min(y[0], (double) clone_length);
            for (int probeIndex = 1; probeIndex < probe_count; probeIndex++) {
                den_vec[cloneIndex] -= (1 - aa[cloneIndex][probeOrder[probeIndex]]) * (1 - aa[cloneIndex][probeOrder[probeIndex - 1]]) * Min(y[probeIndex], (double) clone_length);
            }
            den_vec[cloneIndex] -= (1 - aa[cloneIndex][0]) * (1 - aa[cloneIndex][probeOrder[probe_count - 1]]) * Min(y[probe_count], (double) clone_length);
        }
        for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
            grad = 0.0;
            for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
                den = den_vec[cloneIndex];
                den += R[cloneIndex];
                if (probeIndex == 0) {
                    num = -Indicator(y[probeIndex], (double) clone_length) * (aa[cloneIndex][probeOrder[probeIndex]] - 1) * (aa[cloneIndex][0] - 1) - (aa[cloneIndex][probeOrder[probe_count - 1]] - 1) * Indicator(y[probe_count - 1], (double) clone_length);
                } else {
                    num = -Indicator(y[probeIndex], (double) clone_length) * (aa[cloneIndex][probeOrder[probeIndex]] - 1) * (aa[cloneIndex][probeOrder[probeIndex - 1]] - 1) - (aa[cloneIndex][probeOrder[probe_count - 1]] - 1) * Indicator(y[probe_count - 1], (double) clone_length);
                }
                if (den == 0.0) {
                    den = 0.001;
                }
                grad += num / den;
            }
            gradient[probeIndex] = -grad;
        }
        return gradient;
    }

    private static int Indicator(double x1, double clone_length) {
        if (x1 <= (double) clone_length) {
            return (1);
        } else {
            return (0);
        }
    }

    /**
     * Function to project the direction vector whenever it is necessary
     * in order to satisfy the constraints of the problem.
     * returns the new modified direction 
     */
    private static double[] Project(double[] direction, double[] y, int clone_length, int probe_count) {
        int i, nzero, bool1;
        double last_old_direction;
        int[] flag = new int[probe_count];
        for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
            flag[probeIndex] = 0;
        }
        nzero = 0;
        do {
            bool1 = 1;
            last_old_direction = direction[probe_count];
            for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                if ((y[probeIndex] < 0.0001) && (direction[probeIndex] < 0)) {
                    bool1 = 0;
                    direction[probe_count] += direction[probeIndex];
                    direction[probeIndex] = 0;
                    flag[probeIndex] = 1;
                    nzero++;
                } else if ((Math.abs(y[probeIndex] - (double) clone_length) < 0.0001) && (direction[probeIndex] > 0)) {
                    bool1 = 0;
                    direction[probe_count] += direction[probeIndex];
                    direction[probeIndex] = 0;
                    flag[probeIndex] = 1;
                    nzero++;
                }
            }
            if (((y[probe_count - 1] < 0.0001) && (direction[probe_count] < 0)) || ((Math.abs(y[probe_count] - (double) clone_length) < 0.0001) && (direction[probe_count] > 0))) {
                bool1 = 0;
                direction[probe_count] /= (probe_count - nzero);
                for (int probeIndex = 0; probeIndex < probe_count; probeIndex++) {
                    if (direction[probeIndex] != 0 && flag[probeIndex] != 1) {
                        direction[probeIndex] += direction[probe_count];
                    }
                }
                direction[probe_count] = 0;
            }
        } while (bool1 == 0);
        return direction;
    }

    /**
     * FUNCTION TO BRACKET THE MINIMUM BETWEEN SHIGH AND SLOW and return shigh
     */
    private static double BracketSHigh(double[] y, double[] direction, int probe_count, int clone_length) {
        double t1;
        double shigh = Double.MAX_VALUE;
        for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
            if (direction[probeIndex] > 0) {
                t1 = ((double) clone_length - y[probeIndex]) / direction[probeIndex];
                if (shigh > t1) {
                    shigh = t1;
                }
            } else if (direction[probeIndex] < 0) {
                t1 = -y[probeIndex] / direction[probeIndex];
                if (shigh > t1) {
                    shigh = t1;
                }
            }
        }
        if (shigh < 0) {
            shigh = 0.0;
        }
        return shigh;
    }

    /**
     * FUNCTION TO BRACKET THE MINIMUM BETWEEN SHIGH AND SLOW and return slow
     */
    private static double BracketSLow(double[] y, double[] direction, int probe_count, int clone_length) {
        double t1;
        double slow = 0.0;
        for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
            if (direction[probeIndex] > 0) {
                t1 = -y[probeIndex] / direction[probeIndex];
                if (slow < t1) {
                    slow = t1;
                }
            } else if (direction[probeIndex] < 0) {
                t1 = ((double) clone_length - y[probeIndex]) / direction[probeIndex];
                if (slow < t1) {
                    slow = t1;
                }
            }
        }
        return slow;
    }

    /**
     * Function to do line minimization with respect to s, given the
     * direction, and the bracketing of the minimum.
     */
    private static double[] Line_Minimize(double shigh, double slow, double[] y, double[] direction, int probe_count, int clone_count, double[][] aa, int[] probeOrder, int clone_length, double const1, double[] R) {
        double scalar, old_func, new_func;
        double grad_left, grad_right, grad_middle;
        int caseVble = 0;
        double CRITERIA_BISECTION = 0.0000000001;
        double[] y_temp = new double[probe_count + 1];
        for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
            y_temp[probeIndex] = y[probeIndex] + slow * direction[probeIndex];
        }
        grad_left = Gradient_S(y_temp, direction, probe_count, clone_count, aa, probeOrder, clone_length, R);
        old_func = Func(const1, R, aa, y_temp, clone_length, probe_count, clone_count, probeOrder);
        for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
            y_temp[probeIndex] = y[probeIndex] + shigh * direction[probeIndex];
        }
        grad_right = Gradient_S(y_temp, direction, probe_count, clone_count, aa, probeOrder, clone_length, R);
        while (1 < 2) {
            if (grad_left <= 0 && grad_right <= 0) {
                caseVble = 1;
                for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
                    y[probeIndex] = y[probeIndex] + shigh * direction[probeIndex];
                }
                break;
            }
            if (grad_left >= 0 && grad_right >= 0) {
                caseVble = 2;
                for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
                    y[probeIndex] = y[probeIndex] + slow * direction[probeIndex];
                }
                break;
            }
            if ((grad_left <= 0 && grad_right >= 0) || (grad_left >= 0 && grad_right <= 0)) {
                caseVble = 3;
                scalar = (slow + shigh) / 2;
                for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
                    y_temp[probeIndex] = y[probeIndex] + scalar * direction[probeIndex];
                }
                grad_middle = Gradient_S(y_temp, direction, probe_count, clone_count, aa, probeOrder, clone_length, R);
                new_func = Func(const1, R, aa, y_temp, clone_length, probe_count, clone_count, probeOrder);
                if ((new_func <= old_func) && (old_func - new_func) <= CRITERIA_BISECTION) {
                    caseVble = 4;
                    for (int probeIndex = 0; probeIndex < probe_count + 1; probeIndex++) {
                        y[probeIndex] = y[probeIndex] + scalar * direction[probeIndex];
                    }
                    break;
                }
                if (grad_middle > 0) {
                    caseVble = 5;
                    old_func = new_func;
                    shigh = scalar;
                    grad_right = grad_middle;
                } else {
                    caseVble = 6;
                    old_func = new_func;
                    slow = scalar;
                    grad_left = grad_middle;
                }
                new_func = old_func;
            }
        }
        return y;
    }

    /**
     * Fuction to calculate the 1 dimensional derivative of the 
     * function with respect to s
     */
    private static double Gradient_S(double[] y, double[] direction, int probe_count, int clone_count, double[][] aa, int[] probeOrder, int clone_length, double[] R) {
        double den, num, sum = 0.0;
        for (int cloneIndex = 0; cloneIndex < clone_count; cloneIndex++) {
            den = 0.0;
            den -= (1 - aa[cloneIndex][probeOrder[0]]) * (1 - aa[cloneIndex][0]) * Min(y[0], (double) clone_length);
            for (int probeIndex = 1; probeIndex < probe_count; probeIndex++) {
                den -= (1 - aa[cloneIndex][probeOrder[probeIndex]]) * (1 - aa[cloneIndex][probeOrder[probeIndex - 1]]) * Min(y[probeIndex], (double) clone_length);
            }
            den -= (1 - aa[cloneIndex][0]) * (1 - aa[cloneIndex][probeOrder[probe_count - 1]]) * Min(y[probe_count], (double) clone_length);
            den += R[cloneIndex];
            num = 0.0;
            num += (1 - aa[cloneIndex][probeOrder[0]]) * (1 - aa[cloneIndex][0]) * Indicator(y[0], (double) clone_length) * direction[0];
            for (int probeIndex = 1; probeIndex < probe_count; probeIndex++) {
                num += (1 - aa[cloneIndex][probeOrder[probeIndex]]) * (1 - aa[cloneIndex][probeOrder[probeIndex - 1]]) * Indicator(y[probeIndex], (double) clone_length) * direction[probeIndex];
            }
            num += (1 - aa[cloneIndex][0]) * (1 - aa[cloneIndex][probeOrder[probe_count - 1]]) * Indicator(y[probe_count], (double) clone_length) * direction[probe_count];
            if (den == 0) {
                den = 0.001;
            }
            sum += num / den;
        }
        return sum;
    }
}
