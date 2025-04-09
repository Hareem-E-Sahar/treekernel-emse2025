package com.rapidminer.operator.learner.functions.kernel.jmysvm.optimizer;

/**
 * A quadratic optimization problem.
 * 
 * @author Stefan Rueping
 *          Exp $
 */
public class QuadraticProblemSMO extends QuadraticProblem {

    protected double[] sum;

    protected double is_zero;

    protected int max_iteration;

    public QuadraticProblemSMO() {
        is_zero = 1e-10;
        max_allowed_error = 1e-3;
        max_iteration = 10000;
    }

    ;

    @Override
    public void set_n(int new_n) {
        super.set_n(new_n);
        sum = new double[n];
    }

    ;

    public QuadraticProblemSMO(double is_zero, double max_allowed_error, int max_iteration) {
        this.is_zero = is_zero;
        this.max_allowed_error = max_allowed_error;
        this.max_iteration = max_iteration;
    }

    ;

    protected final double x2tox1(double x2, boolean id, double A1, double b) {
        double x1;
        if (id) {
            x1 = -x2;
        } else {
            x1 = x2;
        }
        ;
        if (A1 > 0) {
            x1 += b;
        } else {
            x1 -= b;
        }
        ;
        return x1;
    }

    ;

    protected final double x1tox2(double x1, boolean id, double A2, double b) {
        double x2;
        if (id) {
            x2 = -x1;
        } else {
            x2 = x1;
        }
        ;
        if (A2 > 0) {
            x2 += b;
        } else {
            x2 -= b;
        }
        ;
        return x2;
    }

    ;

    protected final void simple_solve(int i, int j, double H1, double H2, double c0, double c1, double c2, double A1, double A2, double l1, double l2, double u1, double u2) {
        double x1 = x[i];
        double x2 = x[j];
        double t;
        double den;
        den = H1 + H2;
        if (((A1 > 0) && (A2 > 0)) || ((A1 < 0) && (A2 < 0))) {
            den -= c0;
        } else {
            den += c0;
        }
        ;
        den *= 2;
        if (den != 0) {
            double num;
            num = -2 * H1 * x1 - x2 * c0 - c1;
            if (A1 < 0) {
                num = -num;
            }
            ;
            if (A2 > 0) {
                num += 2 * H2 * x2 + x1 * c0 + c2;
            } else {
                num -= 2 * H2 * x2 + x1 * c0 + c2;
            }
            ;
            t = num / den;
            double up;
            double lo;
            if (A1 > 0) {
                lo = l1 - x1;
                up = u1 - x1;
            } else {
                lo = x1 - u1;
                up = x1 - l1;
            }
            ;
            if (A2 < 0) {
                if (l2 - x2 > lo) lo = l2 - x2;
                if (u2 - x2 < up) up = u2 - x2;
            } else {
                if (x2 - l2 < up) up = x2 - l2;
                if (x2 - u2 > lo) lo = x2 - u2;
            }
            ;
            if (t < lo) {
                t = lo;
            }
            ;
            if (t > up) {
                t = up;
            }
            ;
        } else {
            double factor;
            factor = 2 * H1 * x1 + x2 * c0 + c1;
            if (A1 < 0) {
                factor = -factor;
            }
            ;
            if (A2 > 0) {
                factor -= 2 * H2 * x2 + x1 * c0 + c2;
            } else {
                factor += 2 * H2 * x2 + x1 * c0 + c2;
            }
            ;
            if (factor > 0) {
                if (A1 > 0) {
                    t = l1 - x1;
                } else {
                    t = x1 - u1;
                }
                ;
                if (A2 < 0) {
                    if (l2 - x2 > t) t = l2 - x2;
                } else {
                    if (x2 - u2 > t) t = x2 - u2;
                }
                ;
            } else {
                if (A1 > 0) {
                    t = u1 - x1;
                } else {
                    t = x1 - l1;
                }
                ;
                if (A2 < 0) {
                    if (u2 - x2 < t) t = u2 - x2;
                } else {
                    if (x2 - l2 < t) t = x2 - l2;
                }
                ;
            }
            ;
        }
        ;
        if (A1 > 0) {
            x1 += t;
        } else {
            x1 -= t;
        }
        ;
        if (A2 > 0) {
            x2 -= t;
        } else {
            x2 += t;
        }
        ;
        if (x1 - l1 <= is_zero) {
            x1 = l1;
        } else if (x1 - u1 >= -is_zero) {
            x1 = u1;
        }
        ;
        if (x2 - l2 <= is_zero) {
            x2 = l2;
        } else if (x2 - u2 >= -is_zero) {
            x2 = u2;
        }
        ;
        x[i] = x1;
        x[j] = x2;
    }

    ;

    protected final boolean minimize_ij(int i, int j) {
        double sum_i;
        double sum_j;
        sum_i = sum[i];
        sum_j = sum[j];
        sum_i -= H[i * (n + 1)] * x[i];
        sum_i -= H[i * n + j] * x[j];
        sum_j -= H[j * n + i] * x[i];
        sum_j -= H[j * (n + 1)] * x[j];
        sum_i += c[i];
        sum_j += c[j];
        double old_xi = x[i];
        double old_xj = x[j];
        simple_solve(i, j, H[i * (n + 1)] / 2, H[j * (n + 1)] / 2, H[i * n + j], sum_i, sum_j, A[i], A[j], l[i], l[j], u[i], u[j]);
        boolean ok;
        double target;
        target = (old_xi - x[i]) * (H[i * (n + 1)] / 2 * (old_xi + x[i]) + sum_i) + (old_xj - x[j]) * (H[j * (n + 1)] / 2 * (old_xj + x[j]) + sum_j) + H[i * n + j] * (old_xi * old_xj - x[i] * x[j]);
        if (target < 0) {
            x[i] = old_xi;
            x[j] = old_xj;
            old_xi = 0;
            old_xj = 0;
            ok = false;
        } else {
            old_xi -= x[i];
            old_xj -= x[j];
            int k;
            for (k = 0; k < n; k++) {
                sum[k] -= H[i * n + k] * old_xi;
                sum[k] -= H[j * n + k] * old_xj;
            }
            ;
            ok = true;
        }
        ;
        if ((Math.abs(old_xi) > is_zero) || (Math.abs(old_xj) > is_zero)) {
            ok = true;
        } else {
            ok = false;
        }
        ;
        return ok;
    }

    ;

    @Override
    protected final void calc_lambda_eq() {
        double lambda_eq_sum = 0;
        int count = 0;
        int i;
        for (i = 0; i < n; i++) {
            if ((x[i] > l[i]) && (x[i] < u[i])) {
                if (A[i] > 0) {
                    lambda_eq_sum -= (sum[i] + c[i]);
                } else {
                    lambda_eq_sum += sum[i] + c[i];
                }
                ;
                count++;
            }
            ;
        }
        ;
        if (count > 0) {
            lambda_eq_sum /= count;
        } else {
            double lambda_min = Double.NEGATIVE_INFINITY;
            double lambda_max = Double.POSITIVE_INFINITY;
            double nabla;
            for (i = 0; i < n; i++) {
                nabla = sum[i] + c[i];
                if (x[i] <= l[i]) {
                    if (A[i] > 0) {
                        if (-nabla > lambda_min) {
                            lambda_min = -nabla;
                        }
                        ;
                    } else {
                        if (nabla < lambda_max) {
                            lambda_max = nabla;
                        }
                        ;
                    }
                    ;
                } else {
                    if (A[i] > 0) {
                        if (-nabla < lambda_max) {
                            lambda_max = -nabla;
                        }
                        ;
                    } else {
                        if (nabla > lambda_min) {
                            lambda_min = nabla;
                        }
                        ;
                    }
                    ;
                }
                ;
            }
            ;
            if (lambda_min > Double.NEGATIVE_INFINITY) {
                if (lambda_max < Double.POSITIVE_INFINITY) {
                    lambda_eq_sum = (lambda_max + lambda_min) / 2;
                } else {
                    lambda_eq_sum = lambda_min;
                }
                ;
            } else {
                lambda_eq_sum = lambda_max;
            }
            ;
        }
        ;
        lambda_eq = lambda_eq_sum;
    }

    ;

    @Override
    public final int solve() {
        int error = 0;
        int i;
        int j;
        for (i = 0; i < n; i++) {
            sum[i] = 0;
            for (j = 0; j < n; j++) {
                sum[i] += H[i * n + j] * x[j];
            }
            ;
        }
        ;
        int iteration = 0;
        double this_error;
        double this_lambda_eq;
        double max_lambda_eq = 0;
        double max_error = Double.NEGATIVE_INFINITY;
        double min_error = Double.POSITIVE_INFINITY;
        int max_i = 0;
        int min_i = 1;
        int old_min_i = -1;
        int old_max_i = -1;
        calc_lambda_eq();
        S: while (true) {
            if (0 == error) {
                max_error = Double.NEGATIVE_INFINITY;
                min_error = Double.POSITIVE_INFINITY;
                max_i = 0;
                min_i = 1;
                for (i = 0; i < n; i++) {
                    if (x[i] <= l[i]) {
                        this_error = -sum[i] - c[i];
                        if (A[i] > 0) {
                            this_lambda_eq = this_error;
                            this_error -= lambda_eq;
                        } else {
                            this_lambda_eq = -this_error;
                            this_error += lambda_eq;
                        }
                        ;
                    } else if (x[i] >= u[i]) {
                        this_error = sum[i] + c[i];
                        if (A[i] > 0) {
                            this_lambda_eq = -this_error;
                            this_error += lambda_eq;
                        } else {
                            this_lambda_eq = this_error;
                            this_error -= lambda_eq;
                        }
                        ;
                    } else {
                        this_error = sum[i] + c[i];
                        if (A[i] > 0) {
                            this_lambda_eq = -this_error;
                            this_error += lambda_eq;
                        } else {
                            this_lambda_eq = this_error;
                            this_error -= lambda_eq;
                        }
                        ;
                        if (this_error < 0) this_error = -this_error;
                    }
                    if ((this_error > max_error) && (old_max_i != i)) {
                        max_i = i;
                        max_error = this_error;
                        max_lambda_eq = this_lambda_eq;
                    }
                    ;
                    if ((this_error <= min_error) && (i != old_min_i)) {
                        min_i = i;
                        min_error = this_error;
                    }
                    ;
                }
                ;
                old_max_i = max_i;
                old_min_i = min_i;
            } else {
                max_i = (max_i + 1) % n;
            }
            ;
            if (max_error <= max_allowed_error) {
                error = 0;
                break S;
            }
            ;
            double max_diff = -1;
            double this_diff;
            boolean n_up;
            boolean n_lo;
            if (x[max_i] <= l[max_i]) {
                n_lo = false;
            } else {
                n_lo = true;
            }
            ;
            if (x[max_i] >= u[max_i]) {
                n_up = false;
            } else {
                n_up = true;
            }
            ;
            min_i = (max_i + 1) % n;
            for (i = 0; i < n; i++) {
                if ((i != max_i) && (n_up || (x[i] < u[i])) && (n_lo || (x[i] > l[i]))) {
                    if (x[i] <= l[i]) {
                        this_error = -sum[i] - c[i];
                        if (A[i] < 0) {
                            this_error = -this_error;
                        }
                        ;
                    } else {
                        this_error = sum[i] + c[i];
                        if (A[i] > 0) {
                            this_error = -this_error;
                        }
                        ;
                    }
                    ;
                    this_diff = Math.abs(this_error - max_lambda_eq);
                    if (this_diff > max_diff) {
                        max_diff = this_diff;
                        min_i = i;
                    }
                    ;
                }
                ;
            }
            ;
            int it = 1;
            while ((!minimize_ij(min_i, max_i)) && (it < n)) {
                it++;
                min_i = (min_i + 1) % n;
                if (min_i == max_i) {
                    min_i = (min_i + 1) % n;
                }
                ;
            }
            ;
            if (it == n) {
                error++;
                if (error >= n) {
                    break S;
                }
                ;
            } else {
                error = 0;
            }
            ;
            calc_lambda_eq();
            iteration++;
            if (iteration > max_iteration) {
                error += 1;
                break S;
            }
            ;
        }
        ;
        return error;
    }

    ;
}

;
