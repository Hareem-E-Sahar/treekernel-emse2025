package org.xiph.tightspeex;

/**
 * Line Spectral Pair
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision: 1.1 $
 */
public class Lsp {

    private float[] pw;

    private static float[] cheb_poly_eva_T;

    private static float[] lpc2lsp_Q;

    private static float[] lpc2lsp_P;

    /**
   * Constructor
   */
    public Lsp() {
        pw = new float[42];
    }

    /**
   * This function evaluates a series of Chebyshev polynomials.
   * @param coef - coefficients of the polynomial to be evaluated.
   * @param x    - the point where polynomial is to be evaluated.
   * @param m    - order of the polynomial.
   * @return the value of the polynomial at point x.
   */
    public static final float cheb_poly_eva(final float[] coef, float x, final int m) {
        int i;
        float sum;
        int m2 = m >> 1;
        cheb_poly_eva_T = Bits.newOrZeroFloatArray(cheb_poly_eva_T, m2 + 1);
        cheb_poly_eva_T[0] = 1;
        cheb_poly_eva_T[1] = x;
        sum = coef[m2] + coef[m2 - 1] * x;
        x *= 2;
        for (i = 2; i <= m2; i++) {
            cheb_poly_eva_T[i] = x * cheb_poly_eva_T[i - 1] - cheb_poly_eva_T[i - 2];
            sum += coef[m2 - i] * cheb_poly_eva_T[i];
        }
        return sum;
    }

    /**
   * This function converts LPC coefficients to LSP coefficients.
   * @param a      - LPC coefficients.
   * @param lpcrdr - order of LPC coefficients (10).
   * @param freq   - LSP frequencies in the x domain.
   * @param nb     - number of sub-intervals (4).
   * @param delta  - grid spacing interval (0.02).
   * @return the number of roots (the LSP coefs are returned in the array).
   */
    public static int lpc2lsp(final float[] a, final int lpcrdr, final float[] freq, final int nb, final float delta) {
        float psuml, psumr, psumm, temp_xr, xl, xr, xm = 0;
        float temp_psumr;
        int i, j, m, flag, k;
        int px;
        int qx;
        int p;
        int q;
        float[] pt;
        int roots = 0;
        flag = 1;
        m = lpcrdr / 2;
        Lsp.lpc2lsp_Q = Bits.newOrZeroFloatArray(Lsp.lpc2lsp_Q, m + 1);
        Lsp.lpc2lsp_P = Bits.newOrZeroFloatArray(Lsp.lpc2lsp_P, m + 1);
        px = 0;
        qx = 0;
        p = px;
        q = qx;
        lpc2lsp_P[px++] = 1.0f;
        lpc2lsp_Q[qx++] = 1.0f;
        for (i = 1; i <= m; i++) {
            lpc2lsp_P[px++] = a[i] + a[lpcrdr + 1 - i] - lpc2lsp_P[p++];
            lpc2lsp_Q[qx++] = a[i] - a[lpcrdr + 1 - i] + lpc2lsp_Q[q++];
        }
        px = 0;
        qx = 0;
        for (i = 0; i < m; i++) {
            lpc2lsp_P[px] = 2 * lpc2lsp_P[px];
            lpc2lsp_Q[qx] = 2 * lpc2lsp_Q[qx];
            px++;
            qx++;
        }
        px = 0;
        qx = 0;
        xr = 0;
        xl = 1.0f;
        for (j = 0; j < lpcrdr; j++) {
            if (j % 2 != 0) pt = lpc2lsp_Q; else pt = lpc2lsp_P;
            psuml = cheb_poly_eva(pt, xl, lpcrdr);
            flag = 1;
            while ((flag == 1) && (xr >= -1.0)) {
                float dd;
                dd = (float) (delta * (1 - .9 * xl * xl));
                if (Math.abs(psuml) < .2) dd *= .5;
                xr = xl - dd;
                psumr = cheb_poly_eva(pt, xr, lpcrdr);
                temp_psumr = psumr;
                temp_xr = xr;
                if ((psumr * psuml) < 0.0) {
                    roots++;
                    psumm = psuml;
                    for (k = 0; k <= nb; k++) {
                        xm = (xl + xr) / 2;
                        psumm = cheb_poly_eva(pt, xm, lpcrdr);
                        if (psumm * psuml > 0.) {
                            psuml = psumm;
                            xl = xm;
                        } else {
                            psumr = psumm;
                            xr = xm;
                        }
                    }
                    freq[j] = xm;
                    xl = xm;
                    flag = 0;
                } else {
                    psuml = temp_psumr;
                    xl = temp_xr;
                }
            }
        }
        return roots;
    }

    /**
   * Line Spectral Pair to Linear Prediction Coefficients
   * @param freq
   * @param ak
   * @param lpcrdr
   */
    public void lsp2lpc(final float[] freq, final float[] ak, final int lpcrdr) {
        int i, j;
        float xout1, xout2, xin1, xin2;
        int n1, n2, n3, n4 = 0;
        int m = lpcrdr / 2;
        for (i = 0; i < 4 * m + 2; i++) {
            pw[i] = 0.0f;
        }
        xin1 = 1.0f;
        xin2 = 1.0f;
        for (j = 0; j <= lpcrdr; j++) {
            int i2 = 0;
            for (i = 0; i < m; i++, i2 += 2) {
                n1 = i * 4;
                n2 = n1 + 1;
                n3 = n2 + 1;
                n4 = n3 + 1;
                xout1 = xin1 - 2 * (freq[i2]) * pw[n1] + pw[n2];
                xout2 = xin2 - 2 * (freq[i2 + 1]) * pw[n3] + pw[n4];
                pw[n2] = pw[n1];
                pw[n4] = pw[n3];
                pw[n1] = xin1;
                pw[n3] = xin2;
                xin1 = xout1;
                xin2 = xout2;
            }
            xout1 = xin1 + pw[n4 + 1];
            xout2 = xin2 - pw[n4 + 2];
            ak[j] = (xout1 + xout2) * 0.5f;
            pw[n4 + 1] = xin1;
            pw[n4 + 2] = xin2;
            xin1 = 0.0f;
            xin2 = 0.0f;
        }
    }

    /**
   * Makes sure the LSPs are stable.
   * @param lsp
   * @param len
   * @param margin
   */
    public static void enforce_margin(final float[] lsp, final int len, final float margin) {
        int i;
        if (lsp[0] < margin) lsp[0] = margin;
        if (lsp[len - 1] > (float) Math.PI - margin) lsp[len - 1] = (float) Math.PI - margin;
        for (i = 1; i < len - 1; i++) {
            if (lsp[i] < lsp[i - 1] + margin) lsp[i] = lsp[i - 1] + margin;
            if (lsp[i] > lsp[i + 1] - margin) lsp[i] = .5f * (lsp[i] + lsp[i + 1] - margin);
        }
    }
}
