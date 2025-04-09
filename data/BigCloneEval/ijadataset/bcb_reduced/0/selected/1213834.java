package net.lunglet.svm;

final class NuSolver extends Solver {

    private SolutionInfo si;

    private boolean beShrunken(final int i, final double Gmax1, final double Gmax2, final double Gmax3, final double Gmax4) {
        if (is_upper_bound(i)) {
            if (y[i] == +1) {
                return (-G[i] > Gmax1);
            } else {
                return (-G[i] > Gmax4);
            }
        } else if (is_lower_bound(i)) {
            if (y[i] == +1) {
                return (G[i] > Gmax2);
            } else {
                return (G[i] > Gmax3);
            }
        } else {
            return (false);
        }
    }

    @Override
    double calculateRho() {
        int nrFree1 = 0, nrFree2 = 0;
        double ub1 = INF, ub2 = INF;
        double lb1 = -INF, lb2 = -INF;
        double sumFree1 = 0, sumFree2 = 0;
        for (int i = 0; i < active_size; i++) {
            if (y[i] == +1) {
                if (is_lower_bound(i)) {
                    ub1 = Math.min(ub1, G[i]);
                } else if (is_upper_bound(i)) {
                    lb1 = Math.max(lb1, G[i]);
                } else {
                    ++nrFree1;
                    sumFree1 += G[i];
                }
            } else {
                if (is_lower_bound(i)) {
                    ub2 = Math.min(ub2, G[i]);
                } else if (is_upper_bound(i)) {
                    lb2 = Math.max(lb2, G[i]);
                } else {
                    ++nrFree2;
                    sumFree2 += G[i];
                }
            }
        }
        double r1, r2;
        if (nrFree1 > 0) {
            r1 = sumFree1 / nrFree1;
        } else {
            r1 = (ub1 + lb1) / 2;
        }
        if (nrFree2 > 0) {
            r2 = sumFree2 / nrFree2;
        } else {
            r2 = (ub2 + lb2) / 2;
        }
        si.r = (r1 + r2) / 2;
        return (r1 - r2) / 2;
    }

    @Override
    void doShrinking() {
        double Gmax1 = -INF;
        double Gmax2 = -INF;
        double Gmax3 = -INF;
        double Gmax4 = -INF;
        int i;
        for (i = 0; i < active_size; i++) {
            if (!is_upper_bound(i)) {
                if (y[i] == +1) {
                    if (-G[i] > Gmax1) {
                        Gmax1 = -G[i];
                    }
                } else if (-G[i] > Gmax4) {
                    Gmax4 = -G[i];
                }
            }
            if (!is_lower_bound(i)) {
                if (y[i] == +1) {
                    if (G[i] > Gmax2) {
                        Gmax2 = G[i];
                    }
                } else if (G[i] > Gmax3) {
                    Gmax3 = G[i];
                }
            }
        }
        for (i = 0; i < active_size; i++) {
            if (beShrunken(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
                active_size--;
                while (active_size > i) {
                    if (!beShrunken(active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size--;
                }
            }
        }
        if (unshrinked || Math.max(Gmax1 + Gmax2, Gmax3 + Gmax4) > eps * 10) {
            return;
        }
        unshrinked = true;
        reconstructGradient();
        for (i = l - 1; i >= active_size; i--) {
            if (!beShrunken(i, Gmax1, Gmax2, Gmax3, Gmax4)) {
                while (active_size < i) {
                    if (beShrunken(active_size, Gmax1, Gmax2, Gmax3, Gmax4)) {
                        swap_index(i, active_size);
                        break;
                    }
                    active_size++;
                }
                active_size++;
            }
        }
    }

    @Override
    int selectWorkingSet(final int[] workingSet) {
        double Gmaxp = -INF;
        double Gmaxp2 = -INF;
        int Gmaxp_idx = -1;
        double Gmaxn = -INF;
        double Gmaxn2 = -INF;
        int Gmaxn_idx = -1;
        int Gmin_idx = -1;
        double obj_diff_min = INF;
        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!is_upper_bound(t)) {
                    if (-G[t] >= Gmaxp) {
                        Gmaxp = -G[t];
                        Gmaxp_idx = t;
                    }
                }
            } else {
                if (!is_lower_bound(t)) {
                    if (G[t] >= Gmaxn) {
                        Gmaxn = G[t];
                        Gmaxn_idx = t;
                    }
                }
            }
        }
        int ip = Gmaxp_idx;
        int in = Gmaxn_idx;
        float[] Q_ip = null;
        float[] Q_in = null;
        if (ip != -1) {
            Q_ip = Q.getQ(ip, active_size);
        }
        if (in != -1) {
            Q_in = Q.getQ(in, active_size);
        }
        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!is_lower_bound(j)) {
                    double gradDiff = Gmaxp + G[j];
                    if (G[j] >= Gmaxp2) {
                        Gmaxp2 = G[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = Q_ip[ip] + QD[j] - 2 * Q_ip[j];
                        if (quadCoef > 0) {
                            objDiff = -(gradDiff * gradDiff) / quadCoef;
                        } else {
                            objDiff = -(gradDiff * gradDiff) / 1e-12;
                        }
                        if (objDiff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = objDiff;
                        }
                    }
                }
            } else {
                if (!is_upper_bound(j)) {
                    double gradDiff = Gmaxn - G[j];
                    if (-G[j] >= Gmaxn2) {
                        Gmaxn2 = -G[j];
                    }
                    if (gradDiff > 0) {
                        double objDiff;
                        double quadCoef = Q_in[in] + QD[j] - 2 * Q_in[j];
                        if (quadCoef > 0) {
                            objDiff = -(gradDiff * gradDiff) / quadCoef;
                        } else {
                            objDiff = -(gradDiff * gradDiff) / 1e-12;
                        }
                        if (objDiff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = objDiff;
                        }
                    }
                }
            }
        }
        if (Math.max(Gmaxp + Gmaxp2, Gmaxn + Gmaxn2) < eps) {
            return 1;
        }
        if (y[Gmin_idx] == +1) {
            workingSet[0] = Gmaxp_idx;
        } else {
            workingSet[0] = Gmaxn_idx;
        }
        workingSet[1] = Gmin_idx;
        return 0;
    }

    @Override
    void Solve(final int l, final QMatrix Q, final double[] p, final byte[] y, final double[] alpha, final double Cp, final double Cn, final double eps, final SolutionInfo si, final int shrinking) {
        this.si = si;
        super.Solve(l, Q, p, y, alpha, Cp, Cn, eps, si, shrinking);
    }
}
