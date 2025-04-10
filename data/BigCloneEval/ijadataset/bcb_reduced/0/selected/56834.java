package libsvm;

import java.io.*;
import java.util.*;

class Solver {

    int active_size;

    byte[] y;

    double[] G;

    static final byte LOWER_BOUND = 0;

    static final byte UPPER_BOUND = 1;

    static final byte FREE = 2;

    byte[] alpha_status;

    double[] alpha;

    QMatrix Q;

    float[] QD;

    double eps;

    double Cp, Cn;

    double[] p;

    int[] active_set;

    double[] G_bar;

    int l;

    boolean unshrink;

    static final double INF = java.lang.Double.POSITIVE_INFINITY;

    double get_C(int i) {
        return (y[i] > 0) ? Cp : Cn;
    }

    void update_alpha_status(int i) {
        if (alpha[i] >= get_C(i)) {
            alpha_status[i] = UPPER_BOUND;
        } else if (alpha[i] <= 0) {
            alpha_status[i] = LOWER_BOUND;
        } else {
            alpha_status[i] = FREE;
        }
    }

    boolean is_upper_bound(int i) {
        return alpha_status[i] == UPPER_BOUND;
    }

    boolean is_lower_bound(int i) {
        return alpha_status[i] == LOWER_BOUND;
    }

    boolean is_free(int i) {
        return alpha_status[i] == FREE;
    }

    static class SolutionInfo {

        double obj;

        double rho;

        double upper_bound_p;

        double upper_bound_n;

        double r;

        int iterations;
    }

    void swap_index(int i, int j) {
        Q.swap_index(i, j);
        do {
            byte _ = y[i];
            y[i] = y[j];
            y[j] = _;
        } while (false);
        do {
            double _ = G[i];
            G[i] = G[j];
            G[j] = _;
        } while (false);
        do {
            byte _ = alpha_status[i];
            alpha_status[i] = alpha_status[j];
            alpha_status[j] = _;
        } while (false);
        do {
            double _ = alpha[i];
            alpha[i] = alpha[j];
            alpha[j] = _;
        } while (false);
        do {
            double _ = p[i];
            p[i] = p[j];
            p[j] = _;
        } while (false);
        do {
            int _ = active_set[i];
            active_set[i] = active_set[j];
            active_set[j] = _;
        } while (false);
        do {
            double _ = G_bar[i];
            G_bar[i] = G_bar[j];
            G_bar[j] = _;
        } while (false);
    }

    void reconstruct_gradient() {
        if (active_size == l) {
            return;
        }
        int i, j;
        int nr_free = 0;
        for (j = active_size; j < l; j++) {
            G[j] = G_bar[j] + p[j];
        }
        for (j = 0; j < active_size; j++) {
            if (is_free(j)) {
                nr_free++;
            }
        }
        if (2 * nr_free < active_size) {
            svm.log(0, "\nWarning: using -h 0 may be faster\n");
        }
        if (nr_free * l > 2 * active_size * (l - active_size)) {
            for (i = active_size; i < l; i++) {
                float[] Q_i = Q.get_Q(i, active_size);
                for (j = 0; j < active_size; j++) {
                    if (is_free(j)) {
                        G[i] += alpha[j] * Q_i[j];
                    }
                }
            }
        } else {
            for (i = 0; i < active_size; i++) {
                if (is_free(i)) {
                    float[] Q_i = Q.get_Q(i, l);
                    double alpha_i = alpha[i];
                    for (j = active_size; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                }
            }
        }
    }

    void Solve(int l, QMatrix Q, double[] p_, byte[] y_, double[] alpha_, double Cp, double Cn, double eps, SolutionInfo si, int shrinking, int max_iters) {
        this.l = l;
        this.Q = Q;
        QD = Q.get_QD();
        p = (double[]) p_.clone();
        y = (byte[]) y_.clone();
        alpha = (double[]) alpha_.clone();
        this.Cp = Cp;
        this.Cn = Cn;
        this.eps = eps;
        this.unshrink = false;
        {
            alpha_status = new byte[l];
            for (int i = 0; i < l; i++) {
                update_alpha_status(i);
            }
        }
        {
            active_set = new int[l];
            for (int i = 0; i < l; i++) {
                active_set[i] = i;
            }
            active_size = l;
        }
        {
            G = new double[l];
            G_bar = new double[l];
            int i;
            for (i = 0; i < l; i++) {
                G[i] = p[i];
                G_bar[i] = 0;
            }
            for (i = 0; i < l; i++) {
                if (!is_lower_bound(i)) {
                    float[] Q_i = Q.get_Q(i, l);
                    double alpha_i = alpha[i];
                    int j;
                    for (j = 0; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                    if (is_upper_bound(i)) {
                        for (j = 0; j < l; j++) {
                            G_bar[j] += get_C(i) * Q_i[j];
                        }
                    }
                }
            }
        }
        int iter = 0;
        int counter = Math.min(l, 1000) + 1;
        int[] working_set = new int[2];
        while (max_iters == 0 || iter < max_iters) {
            if (--counter == 0) {
                counter = Math.min(l, 1000);
                if (shrinking != 0) {
                    do_shrinking();
                }
                svm.log(2, ".");
            }
            if (select_working_set(working_set) != 0) {
                reconstruct_gradient();
                active_size = l;
                svm.log(2, "*");
                if (select_working_set(working_set) != 0) {
                    break;
                } else {
                    counter = 1;
                }
            }
            int i = working_set[0];
            int j = working_set[1];
            ++iter;
            float[] Q_i = Q.get_Q(i, active_size);
            float[] Q_j = Q.get_Q(j, active_size);
            double C_i = get_C(i);
            double C_j = get_C(j);
            double old_alpha_i = alpha[i];
            double old_alpha_j = alpha[j];
            if (y[i] != y[j]) {
                double quad_coef = Q_i[i] + Q_j[j] + 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (-G[i] - G[j]) / quad_coef;
                double diff = alpha[i] - alpha[j];
                alpha[i] += delta;
                alpha[j] += delta;
                if (diff > 0) {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = diff;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = -diff;
                    }
                }
                if (diff > C_i - C_j) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = C_i - diff;
                    }
                } else {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = C_j + diff;
                    }
                }
            } else {
                double quad_coef = Q_i[i] + Q_j[j] - 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12;
                }
                double delta = (G[i] - G[j]) / quad_coef;
                double sum = alpha[i] + alpha[j];
                alpha[i] -= delta;
                alpha[j] += delta;
                if (sum > C_i) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = sum - C_i;
                    }
                } else {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = sum;
                    }
                }
                if (sum > C_j) {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = sum - C_j;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = sum;
                    }
                }
            }
            double delta_alpha_i = alpha[i] - old_alpha_i;
            double delta_alpha_j = alpha[j] - old_alpha_j;
            for (int k = 0; k < active_size; k++) {
                G[k] += Q_i[k] * delta_alpha_i + Q_j[k] * delta_alpha_j;
            }
            {
                boolean ui = is_upper_bound(i);
                boolean uj = is_upper_bound(j);
                update_alpha_status(i);
                update_alpha_status(j);
                int k;
                if (ui != is_upper_bound(i)) {
                    Q_i = Q.get_Q(i, l);
                    if (ui) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_i * Q_i[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_i * Q_i[k];
                        }
                    }
                }
                if (uj != is_upper_bound(j)) {
                    Q_j = Q.get_Q(j, l);
                    if (uj) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_j * Q_j[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_j * Q_j[k];
                        }
                    }
                }
            }
        }
        si.rho = calculate_rho();
        {
            double v = 0;
            int i;
            for (i = 0; i < l; i++) {
                v += alpha[i] * (G[i] + p[i]);
            }
            si.obj = v / 2;
        }
        {
            for (int i = 0; i < l; i++) {
                alpha_[active_set[i]] = alpha[i];
            }
        }
        si.upper_bound_p = Cp;
        si.upper_bound_n = Cn;
        svm.log(1, "\noptimization finished, #iter = " + iter + "\n");
        si.iterations = iter;
    }

    int select_working_set(int[] working_set) {
        double Gmax = -INF;
        double Gmax2 = -INF;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = INF;
        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmax) {
                        Gmax = -G[t];
                        Gmax_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmax) {
                        Gmax = G[t];
                        Gmax_idx = t;
                    }
                }
            }
        }
        int i = Gmax_idx;
        float[] Q_i = null;
        if (i != -1) {
            Q_i = Q.get_Q(i, active_size);
        }
        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double grad_diff = Gmax + G[j];
                    if (G[j] >= Gmax2) {
                        Gmax2 = G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = Q_i[i] + QD[j] - 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }
                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!is_upper_bound(j)) {
                    double grad_diff = Gmax - G[j];
                    if (-G[j] >= Gmax2) {
                        Gmax2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        double obj_diff;
                        double quad_coef = Q_i[i] + QD[j] + 2.0 * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12;
                        }
                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }
        if (Gmax + Gmax2 < eps) {
            return 1;
        }
        working_set[0] = Gmax_idx;
        working_set[1] = Gmin_idx;
        return 0;
    }

    private boolean be_shrunk(int i, double Gmax1, double Gmax2) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax2);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax1);
            }
        } else {
            return (false);
        }
    }

    void do_shrinking() {
        int i;
        double Gmax1 = -INF;
        double Gmax2 = -INF;
        for (i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax1) {
                        Gmax1 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax2) {
                        Gmax2 = G[i];
                    }
                }
            } else {
                if (!is_upper_bound(i)) {
                    if (-G[i] >= Gmax2) {
                        Gmax2 = -G[i];
                    }
                }
                if (!is_lower_bound(i)) {
                    if (G[i] >= Gmax1) {
                        Gmax1 = G[i];
                    }
                }
            }
        }
        if (unshrink == false && Gmax1 + Gmax2 <= eps * 10) {
            unshrink = true;
            reconstruct_gradient();
            active_size = l;
        }
        for (i = 0; i < active_size; i++) {
            if (be_shrunk(i, Gmax1, Gmax2)) {
                active_size--;
                while (active_size > i) {
                    if (!be_shrunk(active_size, Gmax1, Gmax2)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
    }

    double calculate_rho() {
        double r;
        int nr_free = 0;
        double ub = INF, lb = -INF, sum_free = 0;
        for (int i = 0; i < active_size; i++) {
            double yG = y[i] * G[i];
            if (is_lower_bound(i)) {
                if (y[i] > 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else if (is_upper_bound(i)) {
                if (y[i] < 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else {
                ++nr_free;
                sum_free += yG;
            }
        }
        if (nr_free > 0) {
            r = sum_free / nr_free;
        } else {
            r = (ub + lb) / 2;
        }
        return r;
    }
}
