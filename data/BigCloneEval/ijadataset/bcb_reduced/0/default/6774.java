public class Imath {

    static double Dot(double[] v1, double[] v2, int l) {
        double res = 0.0F;
        for (int i = 0; i < l; i++) res += v1[i] * v2[i];
        return res;
    }

    static void BuildSqSymmBasisMatrix(double[][] lSVsqss, int lnv, double[][] lelectrodesub, int lnChan) {
        for (int j = 0; j < lnv; j++) {
            for (int k = 0; k <= j; k++) {
                lSVsqss[j][k] = Dot(lelectrodesub[j], lelectrodesub[k], lnChan);
                if (k != j) lSVsqss[k][j] = lSVsqss[j][k];
            }
        }
    }

    static boolean InvertVs(double[] lLf, double[][] lVsqss, int lnv) {
        for (int r = 0; r < lnv; r++) {
            int jw = -1;
            double hvj = -1.0F;
            for (int j = r; j < lnv; j++) {
                if (lVsqss[j][r] != 0.0F) {
                    double lhvj = 0.0F;
                    for (int k = r + 1; k < lnv; k++) lhvj = Math.max(lhvj, Math.abs(lVsqss[j][k] / lVsqss[j][r]));
                    if ((jw == -1) || (lhvj < hvj)) {
                        jw = j;
                        hvj = lhvj;
                    }
                }
            }
            if (jw == -1.0F) return false;
            double scajw = 1.0F / lVsqss[jw][r];
            double t = lLf[jw] * scajw;
            lLf[jw] = lLf[r];
            lLf[r] = t;
            for (int k = r; k < lnv; k++) {
                t = lVsqss[jw][k] * scajw;
                lVsqss[jw][k] = lVsqss[r][k];
                lVsqss[r][k] = (k != r ? t : 1.0);
            }
            for (int j = 0; j < lnv; j++) {
                if (j != r) {
                    double rd = lVsqss[j][r];
                    lLf[j] -= lLf[r] * rd;
                    lVsqss[j][r] = 0.0F;
                    for (int k = r + 1; k < lnv; k++) lVsqss[j][k] -= lVsqss[r][k] * rd;
                }
            }
        }
        return true;
    }

    static boolean FindSubspaceScalars(double[] ielecdiff, double[] ielecsubins, double[] isubspacescalars, double[][] lVsqss, double[][] lSVsqss, int lnv, double[][] lelectrodesub, double ielectrodeins[], int lnChan) {
        for (int j = 0; j < lnv; j++) for (int k = 0; k < lnv; k++) lVsqss[j][k] = lSVsqss[j][k];
        for (int j = 0; j < lnv; j++) isubspacescalars[j] = Dot(lelectrodesub[j], ielectrodeins, lnChan);
        boolean bInvSucc = InvertVs(isubspacescalars, lVsqss, lnv);
        return bInvSucc;
    }

    static double MultSubspaceScalars(double[] ielecdiff, double[] ielecsubins, double[] isubspacescalars, int nvlo, int lnv, double[][] lelectrodesub, double ielectrodeins[], int lnChan) {
        double diffsq = 0.0;
        for (int d = 0; d < lnChan; d++) {
            ielecsubins[d] = 0.0;
            for (int j = nvlo; j < lnv; j++) ielecsubins[d] += lelectrodesub[j][d] * isubspacescalars[j];
            if (ielecdiff != null) {
                ielecdiff[d] = ielectrodeins[d] - ielecsubins[d];
                diffsq += ielecdiff[d] * ielecdiff[d];
            }
        }
        return diffsq;
    }
}

;
