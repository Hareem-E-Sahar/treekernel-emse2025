package com.tiani.prnscp.print;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

abstract class Curve {

    private static class InterpPoint {

        public float x, y;

        InterpPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private Comparator sortComparator = new Comparator() {

        public int compare(Object a, Object b) {
            return (((InterpPoint) a).x == ((InterpPoint) b).x) ? 0 : (((InterpPoint) a).x - ((InterpPoint) b).x < 0) ? -1 : 1;
        }
    };

    private List pts;

    /** Flags if the set of points have been changed since last call to refreshSamples() */
    protected boolean needRefresh;

    /** List of independant x[1..n] values and their corresponding known y[1..n] values */
    protected float[] x, y;

    protected int n;

    protected Curve() {
        needRefresh = false;
        pts = new Vector(10, 10);
    }

    protected void refreshSamples() {
        InterpPoint[] interpPts = (InterpPoint[]) pts.toArray(new InterpPoint[0]);
        Arrays.sort(interpPts, sortComparator);
        n = interpPts.length;
        x = new float[n];
        y = new float[n];
        for (int i = 0; i < n; i++) {
            x[i] = interpPts[i].x;
            y[i] = interpPts[i].y;
        }
        interpPts = null;
        needRefresh = false;
    }

    public void add(float x, float y) {
        Iterator i = pts.iterator();
        int ind = 0;
        while (i.hasNext() && x > ((InterpPoint) i.next()).x) ind++;
        pts.add(ind, new InterpPoint(x, y));
        needRefresh = true;
    }

    public void addAll(float[] xa, float[] ya) {
        int size = xa.length;
        if (ya.length != size) throw new IllegalArgumentException("xa[] and ya[] arrays length must be equal");
        for (int i = 0; i < size; i++) {
            add(xa[i], ya[i]);
        }
    }

    public void addAll(float[] ya) {
        int size = ya.length;
        for (int i = 0; i < size; i++) {
            add(i, ya[i]);
        }
    }

    public abstract float evaluate(float x0);

    private static void swap(float[][] a, int i, int j, float[][] b, int k, int l) {
        float tmp = a[i][j];
        a[i][j] = b[k][l];
        b[k][l] = tmp;
    }

    protected static void gj(float[][] a, int n, float[][] b, int m) throws Exception {
        int[] indxc = new int[n];
        int[] indxr = new int[n];
        int[] ipiv = new int[n];
        int i, j, k, l, ll, icol, irow;
        float big, dum, pivinv, temp;
        irow = icol = 0;
        for (i = 0; i < n; i++) ipiv[i] = 0;
        for (i = 0; i < n; i++) {
            big = 0;
            for (j = 0; j < n; j++) {
                if (ipiv[j] != 1) {
                    for (k = 0; k < n; k++) {
                        if (ipiv[k] == 0) {
                            if (Math.abs(a[j][k]) >= big) {
                                big = Math.abs(a[j][k]);
                                irow = j;
                                icol = k;
                            }
                        }
                    }
                }
            }
            ++ipiv[icol];
            if (irow != icol) {
                for (l = 0; l < n; l++) swap(a, irow, l, a, icol, l);
                for (l = 0; l < m; l++) swap(b, irow, l, b, icol, l);
            }
            indxr[i] = irow;
            indxc[i] = icol;
            if (a[icol][icol] == 0) throw new Exception("singular matrix");
            pivinv = 1 / a[icol][icol];
            a[icol][icol] = 1;
            for (l = 0; l < n; l++) a[icol][l] *= pivinv;
            for (l = 0; l < m; l++) b[icol][l] *= pivinv;
            for (ll = 0; ll < n; ll++) {
                if (ll != icol) {
                    dum = a[ll][icol];
                    a[ll][icol] = 0;
                    for (l = 0; l < n; l++) a[ll][l] -= a[icol][l] * dum;
                    for (l = 0; l < m; l++) b[ll][l] -= b[icol][l] * dum;
                }
            }
        }
        for (l = n - 1; l >= 0; l--) {
            if (indxr[l] != indxc[l]) {
                for (k = 0; k < n; k++) {
                    swap(a, k, indxr[l], a, k, indxc[l]);
                }
            }
        }
    }

    protected static float[][] mult(float[][] a, float[][] b) {
        int am = a.length;
        int an = a[0].length;
        int bm = b.length;
        int bn = b[0].length;
        int rm = am;
        int rn = bn;
        if (an != bm) {
            return null;
        }
        float[][] r = new float[am][bn];
        int i, j, k;
        float dot;
        for (i = 0; i < rm; i++) {
            for (j = 0; j < rn; j++) {
                dot = 0;
                for (k = 0; k < an; k++) {
                    dot += a[i][k] * b[k][j];
                }
                r[i][j] = dot;
            }
        }
        return r;
    }

    protected static float[][] trans(float[][] a) {
        int am = a.length;
        int an = a[0].length;
        float[][] t = new float[an][am];
        for (int i = 0; i < am; i++) {
            for (int j = 0; j < an; j++) {
                t[j][i] = a[i][j];
            }
        }
        return t;
    }
}
