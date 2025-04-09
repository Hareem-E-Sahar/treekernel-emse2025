package shu.math.test.svd;

import java.io.*;
import java.text.*;

public final class matrix {

    public static PrintWriter _stdout = new PrintWriter(System.out);

    public static NumberFormat _nf;

    public static NumberFormat _if;

    static {
        _nf = NumberFormat.getInstance();
        _nf.setMinimumFractionDigits(3);
        _nf.setMaximumFractionDigits(3);
        _nf.setGroupingUsed(false);
        _if = NumberFormat.getInstance();
        _if.setMinimumFractionDigits(0);
        _if.setMaximumFractionDigits(0);
    }

    /**
   */
    public static void setFractionDigits(int n) {
        _nf.setMinimumFractionDigits(n);
        _nf.setMaximumFractionDigits(n);
    }

    /**
   */
    public static void print(String msg, int[][] M) {
        print(_stdout, msg, M);
        _stdout.flush();
    }

    /**
   * TODO: does printing int with _if work?
   */
    public static void print(PrintWriter f, String msg, int[][] M) {
        int nr = M.length;
        int nc = M[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 10) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_if.format(M[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_if.format(M[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_if.format(M[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   */
    public static void print(String msg, float[][] M) {
        print(_stdout, msg, M);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, float[][] M) {
        int nr = M.length;
        int nc = M[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 10) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   */
    public static void print(String msg, double[][] M) {
        print(_stdout, msg, M);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, double[][] M) {
        int nr = M.length;
        int nc = M[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 10) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(M[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   */
    public static void print(String msg, short[] v) {
        print(_stdout, msg, v);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, short[] v) {
        int n = v.length;
        f.println(msg);
        f.print("[ ");
        if (n <= 10) {
            for (int ic = 0; ic < n; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
        } else {
            for (int ic = 0; ic < 4; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
            f.print(" ... ");
            for (int ic = n - 2; ic < n; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
        }
        f.println("]");
        f.flush();
    }

    /**
   */
    public static void print(String msg, int[] v) {
        print(_stdout, msg, v);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, int[] v) {
        int n = v.length;
        f.println(msg);
        f.print("[ ");
        if (n <= 10) {
            for (int ic = 0; ic < n; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
        } else {
            for (int ic = 0; ic < 4; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
            f.print(" ... ");
            for (int ic = n - 2; ic < n; ic++) {
                f.print(_if.format(v[ic]) + "  ");
            }
        }
        f.println("]");
        f.flush();
    }

    /**
   */
    public static void print(String msg, float[] v) {
        print(_stdout, msg, v);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, float[] v) {
        int n = v.length;
        f.println(msg);
        f.print("[ ");
        if (n <= 10) {
            for (int ic = 0; ic < n; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
        } else {
            for (int ic = 0; ic < 4; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
            f.print(" ... ");
            for (int ic = n - 2; ic < n; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
        }
        f.println("]");
        f.flush();
    }

    /**
   */
    public static void print(String msg, double[] v) {
        print(_stdout, msg, v);
        _stdout.flush();
    }

    /**
   */
    public static void print(PrintWriter f, String msg, double[] v) {
        int n = v.length;
        f.println(msg);
        f.print("[ ");
        if (n <= 10) {
            for (int ic = 0; ic < n; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
        } else {
            for (int ic = 0; ic < 4; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
            f.print(" ... ");
            for (int ic = n - 2; ic < n; ic++) {
                f.print(_nf.format(v[ic]) + "  ");
            }
        }
        f.println("]");
        f.flush();
    }

    /**
   * print rows of A, B side by side for comparison
   */
    public static void printCompare(PrintWriter f, String msg, double[][] A, double[][] B) {
        int nr = A.length;
        int nc = A[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            }
            f.print("] vs. [");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   * print rows of A, B side by side for comparison
   */
    public static void printCompare(PrintWriter f, String msg, float[][] A, float[][] B) {
        int nr = A.length;
        int nc = A[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            }
            f.print("] vs. [");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   * print rows of A, B side by side for comparison.
   * float vs. double version.
   */
    public static void printCompare(PrintWriter f, String msg, float[][] A, double[][] B) {
        int nr = A.length;
        int nc = A[0].length;
        f.println(msg);
        for (int ir = 0; ir < nr; ir++) {
            f.print("[ ");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(A[ir][ic]) + "  ");
                }
            }
            f.print("] vs. [");
            if (nc <= 5) {
                for (int ic = 0; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            } else {
                for (int ic = 0; ic < 4; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
                f.print(" ... ");
                for (int ic = nc - 2; ic < nc; ic++) {
                    f.print(_nf.format(B[ir][ic]) + "  ");
                }
            }
            f.println("]");
        }
        f.flush();
    }

    /**
   * print the whole thing to a file, for debugging.
   */
    public static void printFull(String msg, float[][] A, String path) throws IOException {
        PrintWriter f = (path.equals("stdout") ? _stdout : new PrintWriter(new BufferedWriter(new FileWriter(path))));
        f.print(msg);
        int yres = A.length;
        int xres = A[0].length;
        for (int y = 0; y < yres; y++) {
            for (int x = 0; x < xres; x++) {
                if (x % 7 == 0) {
                    f.println("");
                    int end = ((x + 6) >= xres) ? (xres - 1) : (x + 6);
                    f.print("row " + y + " " + x + ".." + end + ": ");
                }
                f.print(_nf.format(A[y][x]) + "  ");
            }
            f.println("");
        }
        if (f == _stdout) {
            f.flush();
        } else {
            f.close();
        }
    }

    /**
   * print the whole thing to a file, for debugging.
   */
    public static void printFull(String msg, double[][] A, String path) throws IOException {
        PrintWriter f = (path.equals("stdout") ? _stdout : new PrintWriter(new BufferedWriter(new FileWriter(path))));
        f.print(msg);
        int yres = A.length;
        int xres = A[0].length;
        for (int y = 0; y < yres; y++) {
            for (int x = 0; x < xres; x++) {
                if (x % 7 == 0) {
                    f.println("");
                    int end = ((x + 6) >= xres) ? (xres - 1) : (x + 6);
                    f.print("row " + y + " " + x + ".." + end + ": ");
                }
                f.print(_nf.format(A[y][x]) + "  ");
            }
            f.println("");
        }
        if (f == _stdout) {
            f.flush();
        } else {
            f.close();
        }
    }

    /**
   * print every element of a 1d float array, for detailed debugging.
   */
    public static void printFull(String msg, float[] v, String path) throws IOException {
        PrintWriter f = (path.equals("stdout") ? _stdout : new PrintWriter(new BufferedWriter(new FileWriter(path))));
        f.print(msg);
        int len = v.length;
        for (int x = 0; x < len; x++) {
            if (x % 7 == 0) {
                f.println("");
                int end = ((x + 6) >= len) ? (len - 1) : (x + 6);
                f.print(x + ".." + end + ": ");
            }
            f.print(_nf.format(v[x]) + "  ");
        }
        f.println("");
        if (f == _stdout) {
            f.flush();
        } else {
            f.close();
        }
    }

    /**
   * print every element of a 1d double array, for detailed debugging.
   */
    public static void printFull(String msg, double[] v, String path) throws IOException {
        PrintWriter f = (path.equals("stdout") ? _stdout : new PrintWriter(new BufferedWriter(new FileWriter(path))));
        f.print(msg);
        int len = v.length;
        f.println("--> v[0] = " + v[0]);
        for (int x = 0; x < len; x++) {
            if (x % 7 == 0) {
                f.println("");
                int end = ((x + 6) >= len) ? (len - 1) : (x + 6);
                f.print(x + ".." + end + ": ");
            }
            f.print(_nf.format(v[x]) + "  ");
        }
        f.println("");
        if (f == _stdout) {
            f.flush();
        } else {
            f.close();
        }
    }

    /**
   * data is an array of n-dimensional data, not a matrix
   * depending on how you look at it.
   * Get the min/max bounds for each dimension.
   * Could be called column bounds.
   */
    public static void getNDBounds(double[][] data, double[][] bounds) {
        int nd = data[0].length;
        for (int id = 0; id < nd; id++) {
            bounds[id][0] = Double.POSITIVE_INFINITY;
            bounds[id][1] = Double.NEGATIVE_INFINITY;
        }
        int ndata = data.length;
        for (int i = 0; i < ndata; i++) {
            for (int id = 0; id < nd; id++) {
                double v = data[i][id];
                if (v < bounds[id][0]) {
                    bounds[id][0] = v;
                }
                if (v > bounds[id][1]) {
                    bounds[id][1] = v;
                }
            }
        }
    }

    /** @deprecated moved to zlib.array */
    public static double[][] clone(double[][] m) {
        double[][] mc = (double[][]) m.clone();
        for (int r = 0; r < mc.length; r++) {
            mc[r] = (double[]) m[r].clone();
        }
        return mc;
    }

    /**
   * return a double[][] copy of this float matrix
   * @deprecated moved to zlib.array
   */
    public static double[][] doubleClone(float[][] matrix) {
        int nr = matrix.length;
        int nc = matrix[0].length;
        double[][] cmatrix = new double[nr][nc];
        for (int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                cmatrix[r][c] = matrix[r][c];
            }
        }
        return cmatrix;
    }

    /**
   * return a double[] copy of this float vector
   * @deprecated moved to zlib.array
   */
    public static double[] doubleClone(float[] vec) {
        int nr = vec.length;
        double[] cvec = new double[nr];
        for (int r = 0; r < nr; r++) {
            cvec[r] = vec[r];
        }
        return cvec;
    }

    /**
   * return a float[][] copy of this double matrix
   * @deprecated moved to zlib.array
   */
    public static float[][] floatClone(double[][] matrix) {
        int nr = matrix.length;
        int nc = matrix[0].length;
        float[][] cmatrix = new float[nr][nc];
        for (int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                cmatrix[r][c] = (float) matrix[r][c];
            }
        }
        return cmatrix;
    }

    /**
   * return a float[] copy of this double vector
   * @deprecated moved to zlib.array
   */
    public static float[] floatClone(double[] vec) {
        int nr = vec.length;
        float[] cvec = new float[nr];
        for (int r = 0; r < nr; r++) {
            cvec[r] = (float) vec[r];
        }
        return cvec;
    }

    /**
   */
    public static void zero(float[][] mat) {
        int nr = mat.length;
        int nc = mat[0].length;
        for (int r = 0; r < nr; r++) {
            float[] mr = mat[r];
            for (int c = 0; c < nc; c++) {
                mr[c] = 0.f;
            }
        }
    }

    /**
   */
    public static void zero(double[][] mat) {
        int nr = mat.length;
        int nc = mat[0].length;
        for (int r = 0; r < nr; r++) {
            double[] mr = mat[r];
            for (int c = 0; c < nc; c++) {
                mr[c] = 0.;
            }
        }
    }

    /**
   * colonExtract, like the matlab : operator, but only works with 2d arrays
   * colonex(0,0) = matlab mat(:,1)   result is [nr,1]
   * colonex(0,1) = matlab mat(:,2)
   * colonex(1,0) = matlab mat(1,:)
   * colonex(1,1) = matlab mat(2,:)   result is [1,nc]
   */
    public static int[][] colonEx(int[][] mat, int dim, int el) {
        int nr = mat.length;
        int nc = mat[0].length;
        int[] size = new int[] { nr, nc };
        int[][] v = null;
        if (dim == 0) {
            int vdim = size[dim];
            v = new int[vdim][1];
            for (int i = 0; i < vdim; i++) {
                v[i][0] = mat[i][el];
            }
        } else if (dim == 1) {
            int vdim = size[dim];
            v = new int[1][vdim];
            for (int i = 0; i < vdim; i++) {
                v[0][i] = mat[el][i];
            }
        } else {
        }
        return v;
    }

    /**
   * colonSet(m,0,k,v) = matlab mat(:,k+1) = v
   * colonSet(m,1,k,v) = matlab mat(k+1,:) = v
   */
    public static void colonSet(int[][] mat, int dim, int el, int[][] v) {
        int nr = mat.length;
        int nc = mat[0].length;
        int[] size = new int[] { nr, nc };
        if (dim == 0) {
            int vdim = size[dim];
            for (int i = 0; i < vdim; i++) {
                mat[i][el] = v[i][0];
            }
        } else if (dim == 1) {
            int vdim = size[dim];
            for (int i = 0; i < vdim; i++) {
                mat[el][i] = v[0][i];
            }
        } else {
        }
    }

    /**
   * float version
   */
    public static void setIdentity(float[][] mat) {
        int nr = mat.length;
        int nc = mat[0].length;
        for (int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                if (r == c) {
                    mat[r][c] = 1.f;
                } else {
                    mat[r][c] = 0.f;
                }
            }
        }
    }

    /**
   */
    public static void setIdentity(double[][] mat) {
        int nr = mat.length;
        int nc = mat[0].length;
        for (int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                if (r == c) {
                    mat[r][c] = 1.;
                } else {
                    mat[r][c] = 0.;
                }
            }
        }
    }

    /**
   */
    public static int[][] min(int[][] m1, int[][] m2) {
        int nr = m1.length;
        int nc = m1[0].length;
        int[][] mr = new int[nr][nc];
        min(m1, m2, mr);
        return mr;
    }

    /**
   */
    public static void min(int[][] m1, int[][] m2, int[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            int[] m1row = m1[ir];
            int[] m2row = m2[ir];
            int[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                int m1v = m1row[ic];
                int m2v = m2row[ic];
                mrrow[ic] = (m1v < m2v) ? m1v : m2v;
            }
        }
    }

    /**
   */
    public static int[][] max(int[][] m1, int[][] m2) {
        int nr = m1.length;
        int nc = m1[0].length;
        int[][] mr = new int[nr][nc];
        max(m1, m2, mr);
        return mr;
    }

    /**
   */
    public static void max(int[][] m1, int[][] m2, int[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            int[] m1row = m1[ir];
            int[] m2row = m2[ir];
            int[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                int m1v = m1row[ic];
                int m2v = m2row[ic];
                mrrow[ic] = (m1v > m2v) ? m1v : m2v;
            }
        }
    }

    /**
   * functional
   */
    public static float[] scalefun(float scale, float[] v) {
        int len = v.length;
        float[] sv = new float[len];
        for (int i = 0; i < len; i++) {
            sv[i] = v[i] * scale;
        }
        return sv;
    }

    public static void scale(float scale, float[] v) {
        int len = v.length;
        for (int i = 0; i < len; i++) {
            v[i] *= scale;
        }
    }

    /**
   * functional
   */
    public static double[] scalefun(double scale, double[] v) {
        int len = v.length;
        double[] sv = new double[len];
        for (int i = 0; i < len; i++) {
            sv[i] = v[i] * scale;
        }
        return sv;
    }

    public static void scale(double scale, double[] v) {
        int len = v.length;
        for (int i = 0; i < len; i++) {
            v[i] *= scale;
        }
    }

    /**
   * functional
   */
    public static int[][] addfun(int[][] m1, int[][] m2) {
        int nr = m1.length;
        int nc = m1[0].length;
        int[][] mr = new int[nr][nc];
        add(m1, m2, mr);
        return mr;
    }

    /**
   */
    public static void add(int[][] m1, int[][] m2, int[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            int[] m1row = m1[ir];
            int[] m2row = m2[ir];
            int[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                mrrow[ic] = m1row[ic] + m2row[ic];
            }
        }
    }

    /**
   * mr = s1*v1 + s2*v2
   */
    public static void addscaled(float s1, float[] v1, float s2, float[] v2, float[] vr) {
        int len = v1.length;
        for (int i = 0; i < len; i++) {
            vr[i] = s1 * v1[i] + s2 * v2[i];
        }
    }

    /**
   * mr = s1*m1 + s2*m2
   */
    public static void addscaled(float s1, float[][] m1, float s2, float[][] m2, float[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            float[] m1row = m1[ir];
            float[] m2row = m2[ir];
            float[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                mrrow[ic] = s1 * m1row[ic] + s2 * m2row[ic];
            }
        }
    }

    /**
   * mr = s1*m1 + s2*m2
   */
    public static void addscaled(double s1, double[][] m1, double s2, double[][] m2, double[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            double[] m1row = m1[ir];
            double[] m2row = m2[ir];
            double[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                mrrow[ic] = s1 * m1row[ic] + s2 * m2row[ic];
            }
        }
    }

    /**
   */
    public static float[][] addfun(float[][] m1, float[][] m2) {
        int nr = m1.length;
        int nc = m1[0].length;
        float[][] mr = new float[nr][nc];
        add(m1, m2, mr);
        return mr;
    }

    public static void add(float[][] m1, float[][] m2, float[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            float[] m1row = m1[ir];
            float[] m2row = m2[ir];
            float[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                mrrow[ic] = m1row[ic] + m2row[ic];
            }
        }
    }

    /**
   */
    public static double[][] addfun(double[][] m1, double[][] m2) {
        int nr = m1.length;
        int nc = m1[0].length;
        double[][] mr = new double[nr][nc];
        add(m1, m2, mr);
        return mr;
    }

    public static void add(final double[][] m1, final double[][] m2, double[][] mr) {
        int nr = m1.length;
        int nc = m1[0].length;
        for (int ir = 0; ir < nr; ir++) {
            double[] m1row = m1[ir];
            double[] m2row = m2[ir];
            double[] mrrow = mr[ir];
            for (int ic = 0; ic < nc; ic++) {
                mrrow[ic] = m1row[ic] + m2row[ic];
            }
        }
    }

    public static void add(final double[] v1, final double[] v2, double[] v3) {
        int len = v1.length;
        for (int i = 0; i < len; i++) {
            v3[i] = v1[i] + v2[i];
        }
    }

    public static double[] add(final double[] v1, final double[] v2) {
        int len = v1.length;
        double[] v3 = new double[len];
        add(v1, v2, v3);
        return v3;
    }

    /**
   */
    public static double[][] alloc(int nr, int nc) {
        return new double[nr][nc];
    }

    /**
   */
    public static double[] alloc(int nr) {
        return new double[nr];
    }

    /**
   * todo is there a better algorithm?
   * maybe not, jama uses this algorithm
   */
    public static double[][] transpose(final double[][] A) {
        int N = A.length;
        double[][] B = alloc(N, N);
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                B[r][c] = A[c][r];
            }
        }
        return B;
    }

    /**
   * Conformal matrix multiply M3[n1,n3] = M1[n1,n2] * M2[n2,n3];
   * M3 must be distinct from M1,M2.
   */
    public static void multiply(final double[][] A, final double[][] B, double[][] C) {
        int nr = A.length;
        int nc = B[0].length;
        int ni = A[0].length;
        for (int r = 0; r < nr; r++) {
            for (int c = 0; c < nc; c++) {
                double sum = 0.0;
                for (int i = 0; i < ni; i++) {
                    sum += (A[r][i] * B[i][c]);
                }
                C[r][c] = sum;
            }
        }
    }

    public static double[][] multiply(final double[][] A, final double[][] B) {
        int nr = A.length;
        int nc = B[0].length;
        double[][] C = new double[nr][nc];
        multiply(A, B, C);
        return C;
    }

    /**
   * multiply M,v
   */
    public static double[] multiply(final double[][] M, final double[] v1) {
        int nr = M.length;
        int nc = M[0].length;
        double[] v2 = new double[nr];
        for (int r = 0; r < nr; r++) {
            double sum = 0.0;
            for (int c = 0; c < nc; c++) {
                sum += (M[r][c] * v1[c]);
            }
            v2[r] = sum;
        }
        return v2;
    }

    /**
   * multiply M,v1 -> v2
   */
    public static void multiply(final double[][] M, final double[] v1, double[] v2) {
        int nr = M.length;
        int nc = M[0].length;
        for (int r = 0; r < nr; r++) {
            double sum = 0.0;
            for (int c = 0; c < nc; c++) {
                sum += (M[r][c] * v1[c]);
            }
            v2[r] = sum;
        }
    }
}
