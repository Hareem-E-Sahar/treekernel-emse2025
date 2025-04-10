package org.xiph.tightspeex;

/**
 * Filters
 *
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision: 1.1 $
 */
public class Filters {

    private int last_pitch;

    private float[] last_pitch_gain;

    private float smooth_gain;

    private float[] xx;

    private static float[] qmf_decomp_a;

    private static float[] qmf_decomp_x;

    private static float[] syn_percep_zero_mem;

    private static float[] residue_percep_zero_mem;

    /**
   * Constructor
   */
    public Filters() {
        last_pitch_gain = new float[3];
        xx = new float[1024];
    }

    /**
   * Initialisation
   */
    public void init() {
        last_pitch = 0;
        last_pitch_gain[0] = last_pitch_gain[1] = last_pitch_gain[2] = 0;
        smooth_gain = 1;
    }

    /**
   * bw_lpc
   * @param gamma
   * @param lpc_in
   * @param lpc_out
   * @param order
   */
    public static final void bw_lpc(final float gamma, final float[] lpc_in, final float[] lpc_out, final int order) {
        float tmp = 1;
        for (int i = 0; i < order + 1; i++) {
            lpc_out[i] = tmp * lpc_in[i];
            tmp *= gamma;
        }
    }

    /**
   * filter_mem2
   * @param x
   * @param xs
   * @param num
   * @param den
   * @param N
   * @param ord
   * @param mem
   * @param ms
   */
    public static final void filter_mem2(final float[] x, final int xs, final float[] num, final float[] den, final int N, final int ord, final float[] mem, final int ms) {
        int i, j;
        float xi, yi;
        for (i = 0; i < N; i++) {
            xi = x[xs + i];
            x[xs + i] = num[0] * xi + mem[ms + 0];
            yi = x[xs + i];
            for (j = 0; j < ord - 1; j++) {
                mem[ms + j] = mem[ms + j + 1] + num[j + 1] * xi - den[j + 1] * yi;
            }
            mem[ms + ord - 1] = num[ord] * xi - den[ord] * yi;
        }
    }

    /**
   * filter_mem2
   * @param x
   * @param xs
   * @param num
   * @param den
   * @param y
   * @param ys
   * @param N
   * @param ord
   * @param mem
   * @param ms
   */
    public static final void filter_mem2(final float[] x, final int xs, final float[] num, final float[] den, final float[] y, final int ys, final int N, final int ord, final float[] mem, final int ms) {
        int i, j;
        float xi, yi;
        for (i = 0; i < N; i++) {
            xi = x[xs + i];
            y[ys + i] = num[0] * xi + mem[0];
            yi = y[ys + i];
            for (j = 0; j < ord - 1; j++) {
                mem[ms + j] = mem[ms + j + 1] + num[j + 1] * xi - den[j + 1] * yi;
            }
            mem[ms + ord - 1] = num[ord] * xi - den[ord] * yi;
        }
    }

    /**
   * iir_mem2
   * @param x
   * @param xs
   * @param den
   * @param y
   * @param ys
   * @param N
   * @param ord
   * @param mem
   */
    public static final void iir_mem2(final float[] x, final int xs, final float[] den, final float[] y, final int ys, final int N, final int ord, final float[] mem) {
        int i, j;
        for (i = 0; i < N; i++) {
            y[ys + i] = x[xs + i] + mem[0];
            for (j = 0; j < ord - 1; j++) {
                mem[j] = mem[j + 1] - den[j + 1] * y[ys + i];
            }
            mem[ord - 1] = -den[ord] * y[ys + i];
        }
    }

    /**
   * fir_mem2
   * @param x
   * @param xs
   * @param num
   * @param y
   * @param ys
   * @param N
   * @param ord
   * @param mem
   */
    public static final void fir_mem2(final float[] x, final int xs, final float[] num, final float[] y, final int ys, final int N, final int ord, final float[] mem) {
        int i, j;
        float xi;
        for (i = 0; i < N; i++) {
            xi = x[xs + i];
            y[ys + i] = num[0] * xi + mem[0];
            for (j = 0; j < ord - 1; j++) {
                mem[j] = mem[j + 1] + num[j + 1] * xi;
            }
            mem[ord - 1] = num[ord] * xi;
        }
    }

    /**
   * syn_percep_zero
   * @param xx
   * @param xxs
   * @param ak
   * @param awk1
   * @param awk2
   * @param y
   * @param N
   * @param ord
   */
    public static final void syn_percep_zero(final float[] xx, final int xxs, final float[] ak, final float[] awk1, final float[] awk2, final float[] y, final int N, final int ord) {
        int i;
        syn_percep_zero_mem = Bits.newOrZeroFloatArray(syn_percep_zero_mem, ord);
        filter_mem2(xx, xxs, awk1, ak, y, 0, N, ord, syn_percep_zero_mem, 0);
        for (i = 0; i < ord; i++) syn_percep_zero_mem[i] = 0;
        iir_mem2(y, 0, awk2, y, 0, N, ord, syn_percep_zero_mem);
    }

    /**
   * residue_percep_zero
   * @param xx
   * @param xxs
   * @param ak
   * @param awk1
   * @param awk2
   * @param y
   * @param N
   * @param ord
   */
    public static final void residue_percep_zero(final float[] xx, final int xxs, final float[] ak, final float[] awk1, final float[] awk2, final float[] y, final int N, final int ord) {
        int i;
        residue_percep_zero_mem = Bits.newOrZeroFloatArray(residue_percep_zero_mem, ord);
        filter_mem2(xx, xxs, ak, awk1, y, 0, N, ord, residue_percep_zero_mem, 0);
        for (i = 0; i < ord; i++) residue_percep_zero_mem[i] = 0;
        fir_mem2(y, 0, awk2, y, 0, N, ord, residue_percep_zero_mem);
    }

    /**
   * fir_mem_up
   * @param x
   * @param a
   * @param y
   * @param N
   * @param M
   * @param mem
   */
    public void fir_mem_up(final float[] x, final float[] a, final float[] y, final int N, final int M, final float[] mem) {
        int i, j;
        for (i = 0; i < N / 2; i++) xx[2 * i] = x[N / 2 - 1 - i];
        for (i = 0; i < M - 1; i += 2) xx[N + i] = mem[i + 1];
        for (i = 0; i < N; i += 4) {
            float y0, y1, y2, y3, x0;
            y0 = y1 = y2 = y3 = 0.f;
            x0 = xx[N - 4 - i];
            for (j = 0; j < M; j += 4) {
                float x1, a0, a1;
                a0 = a[j];
                a1 = a[j + 1];
                x1 = xx[N - 2 + j - i];
                y0 += a0 * x1;
                y1 += a1 * x1;
                y2 += a0 * x0;
                y3 += a1 * x0;
                a0 = a[j + 2];
                a1 = a[j + 3];
                x0 = xx[N + j - i];
                y0 += a0 * x0;
                y1 += a1 * x0;
                y2 += a0 * x1;
                y3 += a1 * x1;
            }
            y[i] = y0;
            y[i + 1] = y1;
            y[i + 2] = y2;
            y[i + 3] = y3;
        }
        for (i = 0; i < M - 1; i += 2) mem[i + 1] = xx[i];
    }

    /**
   * Comb Filter
   * @param exc - decoded excitation
   * @param esi
   * @param new_exc - enhanced excitation
   * @param nsi
   * @param nsf - sub-frame size
   * @param pitch - pitch period
   * @param pitch_gain - pitch gain (3-tap)
   * @param comb_gain - gain of comb filter
   */
    public void comb_filter(final float[] exc, final int esi, final float[] new_exc, final int nsi, final int nsf, final int pitch, final float[] pitch_gain, float comb_gain) {
        int i, j;
        float exc_energy = 0.0f, new_exc_energy = 0.0f;
        float gain, step, fact, g = 0.0f;
        for (i = esi; i < esi + nsf; i++) {
            exc_energy += exc[i] * exc[i];
        }
        g = .5f * Math.abs(pitch_gain[0] + pitch_gain[1] + pitch_gain[2] + last_pitch_gain[0] + last_pitch_gain[1] + last_pitch_gain[2]);
        if (g > 1.3f) comb_gain *= 1.3f / g;
        if (g < .5f) comb_gain *= 2.0f * g;
        step = 1.0f / nsf;
        fact = 0;
        for (i = 0, j = esi; i < nsf; i++, j++) {
            fact += step;
            new_exc[nsi + i] = exc[j] + comb_gain * fact * (pitch_gain[0] * exc[j - pitch + 1] + pitch_gain[1] * exc[j - pitch] + pitch_gain[2] * exc[j - pitch - 1]) + comb_gain * (1.0f - fact) * (last_pitch_gain[0] * exc[j - last_pitch + 1] + last_pitch_gain[1] * exc[j - last_pitch] + last_pitch_gain[2] * exc[j - last_pitch - 1]);
        }
        last_pitch_gain[0] = pitch_gain[0];
        last_pitch_gain[1] = pitch_gain[1];
        last_pitch_gain[2] = pitch_gain[2];
        last_pitch = pitch;
        for (i = nsi; i < nsi + nsf; i++) new_exc_energy += new_exc[i] * new_exc[i];
        gain = (float) (Math.sqrt(exc_energy / (.1f + new_exc_energy)));
        if (gain < .5f) {
            gain = .5f;
        }
        if (gain > 1.0f) {
            gain = 1.0f;
        }
        for (i = nsi; i < nsi + nsf; i++) {
            smooth_gain = .96f * smooth_gain + .04f * gain;
            new_exc[i] *= smooth_gain;
        }
    }

    /**
   * Quadrature Mirror Filter to Split the band in two.
   * A 16kHz signal is thus divided into two 8kHz signals representing the low and high bands.
   * (used by wideband encoder)
   * @param xx
   * @param aa
   * @param y1
   * @param y2
   * @param N
   * @param M
   * @param mem
   */
    public static final void qmf_decomp(final float[] xx, final float[] aa, final float[] y1, final float[] y2, final int N, final int M, final float[] mem) {
        int i, j, k, M2;
        int x2;
        qmf_decomp_a = Bits.newOrZeroFloatArray(qmf_decomp_a, M);
        qmf_decomp_x = Bits.newOrZeroFloatArray(qmf_decomp_x, N + M - 1);
        x2 = M - 1;
        M2 = M >> 1;
        for (i = 0; i < M; i++) qmf_decomp_a[M - i - 1] = aa[i];
        for (i = 0; i < M - 1; i++) qmf_decomp_x[i] = mem[M - i - 2];
        for (i = 0; i < N; i++) qmf_decomp_x[i + M - 1] = xx[i];
        for (i = 0, k = 0; i < N; i += 2, k++) {
            y1[k] = 0;
            y2[k] = 0;
            for (j = 0; j < M2; j++) {
                y1[k] += qmf_decomp_a[j] * (qmf_decomp_x[i + j] + qmf_decomp_x[x2 + i - j]);
                y2[k] -= qmf_decomp_a[j] * (qmf_decomp_x[i + j] - qmf_decomp_x[x2 + i - j]);
                j++;
                y1[k] += qmf_decomp_a[j] * (qmf_decomp_x[i + j] + qmf_decomp_x[x2 + i - j]);
                y2[k] += qmf_decomp_a[j] * (qmf_decomp_x[i + j] - qmf_decomp_x[x2 + i - j]);
            }
        }
        for (i = 0; i < M - 1; i++) mem[i] = xx[N - i - 1];
    }
}
