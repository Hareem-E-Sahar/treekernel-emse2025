public class Test {    public Spectrum fft() {
        final Spectrum ret = new Spectrum(this.copy(), ((double) this.getSamplerate()) / ((double) this.size()));
        int mit2, iter, irem, it, it2, nxp, nxp2, m, mxp, j1, j2, k, n, i, j;
        double wre, wim;
        ComplexD t;
        n = ret.size();
        for (iter = 0, irem = n / 2; irem != 0; irem /= 2, iter++) ;
        if (SIN_TABLE.dim != n) SIN_TABLE.ini_fft(n);
        for (it = 0, nxp2 = n, it2 = 1; it < iter; it++, it2 *= 2) {
            nxp = nxp2;
            nxp2 = nxp / 2;
            for (m = 0, mit2 = 0; m < nxp2; m++, mit2 += it2) {
                wre = SIN_TABLE.cosinus[mit2];
                wim = -SIN_TABLE.sinus[mit2];
                for (mxp = nxp, j1 = m; mxp <= n; mxp += nxp, j1 += nxp) {
                    j2 = j1 + nxp2;
                    t = ret.get(j1).copy();
                    t.sub(ret.get(j2));
                    ret.get(j1).addi(ret.get(j2));
                    t.mul(new NotComplexD(wre, wim));
                    ret.set(j2, t);
                }
            }
        }
        for (i = 0, j = 0; i < n - 1; i++) {
            if (i < j) ret.change(i, j);
            k = n / 2;
            while (k <= j) {
                j -= k;
                k = (k + 1) / 2;
            }
            j += k;
        }
        for (i = 0; i < n; i++) ret.get(i).setIsComplex();
        return ret;
    }
}