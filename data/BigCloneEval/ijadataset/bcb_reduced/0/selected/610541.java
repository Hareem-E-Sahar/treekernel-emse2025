package lusc.net.sourceforge;

import cern.colt.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class MultiDimensionalScaling extends SeqBlas {

    int n;

    int npcs;

    int augmentedDims = 0;

    float[][] out;

    double[] percentExplained;

    double[] eigenValues;

    double[] sds;

    NonMetricMultiDimensionalScaling nmds = null;

    public MultiDimensionalScaling(float[][] data, int anpcs) {
        n = data.length;
        npcs = anpcs;
        if (npcs > n) {
            npcs = n;
        }
        out = new float[n][npcs];
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                d[i][j] = data[i][j];
                d[j][i] = data[i][j];
            }
        }
        double max = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                for (int k = 0; k < j; k++) {
                    max = Math.max(max, d[j][k] - d[i][j] - d[i][k]);
                }
            }
        }
        if (max > 0) {
            max *= 10;
        } else {
            max = 0;
        }
        System.out.println("Triangle inequality constant: " + max);
        float d_col[] = new float[n];
        float d_row[] = new float[n];
        float d_tot = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    d[i][j] += max;
                    d[i][j] = -0.5f * (d[i][j] * d[i][j]);
                    d_col[i] += d[i][j];
                    d_row[j] += d[i][j];
                    d_tot += d[i][j];
                }
            }
        }
        for (int i = 0; i < n; i++) {
            d_col[i] /= n + 0f;
            d_row[i] /= n + 0f;
            d_col[i] *= d_col[i];
            d_row[i] *= d_row[i];
        }
        d_tot /= n * n + 0f;
        d_tot *= d_tot;
        float d_tot2 = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = d[i][j] - d_col[i] - d_row[j] + d_tot;
            }
        }
        long st = System.currentTimeMillis();
        EigenValueDecomposition evd = new EigenValueDecomposition(d);
        long et = System.currentTimeMillis();
        double[] eig = evd.d;
        double[] eigs = new double[npcs];
        for (int i = 0; i < npcs; i++) {
            eigs[i] = eig[n - 1 - i];
            System.out.println(eigs[i]);
        }
        eigenValues = eigs;
        st = System.currentTimeMillis();
        double[][] d2 = solveEigenvectors(d, eigs);
        System.out.println(d2.length + " " + d2[0].length + " " + eigs.length);
        double[][] d3 = scaleEigenvectors(d2, eigs);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < npcs; j++) {
                int jj = eig.length - j - 1;
                out[i][j] = (float) (d3[j][i]);
            }
        }
    }

    public double[][] scaleEigenvectors(double[][] d, double[] eig) {
        int n = d[0].length;
        int m = eig.length;
        System.out.println(n + " " + m);
        double[][] f = new double[m][n];
        for (int i = 0; i < m; i++) {
            double q = 0;
            for (int j = 0; j < n; j++) {
                q += d[i][j] * d[i][j];
            }
            double r = Math.sqrt(q / Math.abs(eig[i]));
            for (int j = 0; j < n; j++) {
                f[i][j] = d[i][j] / r;
            }
        }
        return f;
    }

    public double[][] solveEigenvectors(double[][] d, double[] val) {
        int n = d.length;
        int m = val.length;
        double r[][] = new double[m][n];
        Algebra alg = new Algebra();
        DoubleFactory1D F1 = DoubleFactory1D.dense;
        DoubleFactory2D F2 = DoubleFactory2D.dense;
        DoubleMatrix1D onemat = F1.make(n, 1);
        for (int i = 0; i < m; i++) {
            DoubleMatrix2D mat = F2.make(d);
            for (int j = 0; j < n; j++) {
                double a = mat.getQuick(j, j) - val[i];
                mat.setQuick(j, j, a);
            }
            DoubleMatrix1D ev = alg.mult(mat, onemat);
            LUDecompositionQuick luq = new LUDecompositionQuick(0);
            luq.decompose(mat);
            luq.solve(ev);
            r[i] = ev.toArray();
            for (int j = 0; j < n; j++) {
                r[i][j]--;
            }
        }
        return r;
    }

    public double[][] inverseIterate(double[][] d, double[] val) {
        int n = d.length;
        int m = val.length;
        double r[][] = new double[m][n];
        double d2[] = new double[n];
        Algebra alg = new Algebra();
        DoubleFactory2D F = DoubleFactory2D.dense;
        DoubleFactory1D F2 = DoubleFactory1D.dense;
        double eps = Math.pow(2.0, -26.0);
        long st = 0;
        long mt1 = 0;
        long mt2 = 0;
        long et = 0;
        for (int i = 0; i < m; i++) {
            st += System.currentTimeMillis();
            double[][] di = new double[n][n];
            for (int j = 0; j < n; j++) {
                d2[j] = Math.random();
                for (int k = 0; k < n; k++) {
                    di[j][k] = d[j][k];
                    if (j == k) {
                        di[j][k] -= val[i];
                    }
                }
            }
            DoubleMatrix2D mat1 = F.make(di);
            DoubleMatrix1D matE = F2.make(d2);
            mt1 += System.currentTimeMillis();
            DoubleMatrix2D mat1a = alg.inverse(mat1);
            mt2 += System.currentTimeMillis();
            double norm = 0;
            double converge = 1;
            double t = 0;
            DoubleMatrix1D mat2, matC;
            int count = 0;
            while (converge > eps) {
                converge = 0;
                if (count == 0) {
                    matE = alg.mult(mat1a, matE);
                } else {
                    matE = alg.mult(mat1, matE);
                }
                d2 = matE.toArray();
                norm = alg.norm1(matE);
                norm = 1 / norm;
                dscal(norm, matE);
                d2 = matE.toArray();
                matC = alg.mult(mat1, matE);
                converge = dnrm2(matC);
                count++;
            }
            r[i] = matE.toArray();
            et += System.currentTimeMillis();
        }
        alg = null;
        return (r);
    }

    public double[] calculateSDs(double[][] d) {
        int n = d.length;
        int m = d[0].length;
        double result[] = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < m; k++) {
                    result[i] += (d[i][j] - d[i][k]) * (d[i][j] - d[i][k]);
                }
            }
        }
        return (result);
    }

    public double[] calculateCorrelation(float[][] d, float[][] input) {
        int n = d.length;
        int m = d[0].length;
        double[] results = new double[m];
        float[][] temp = new float[n][];
        for (int i = 0; i < n; i++) {
            temp[i] = new float[i + 1];
        }
        float meanin = 0;
        float count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                meanin += input[i][j];
                count++;
            }
        }
        meanin /= count;
        float sstot = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                sstot += (input[i][j] - meanin) * (input[i][j] - meanin);
            }
        }
        for (int i = 0; i < m; i++) {
            float meantemp = 0;
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < j; k++) {
                    double p = 0;
                    for (int a = 0; a <= i; a++) {
                        double q = 1;
                        if (eigenValues[a] < 0) {
                            q = -1;
                        }
                        p += q * (d[j][a] - d[k][a]) * (d[j][a] - d[k][a]);
                    }
                    temp[j][k] = (float) Math.sqrt(p);
                    meantemp += temp[j][k];
                }
            }
            meantemp /= count;
            float sserr = 0;
            float numer = 0f;
            float ssreg = 0;
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < j; k++) {
                    sserr += (temp[j][k] - input[j][k]) * (temp[j][k] - input[j][k]);
                    numer += (temp[j][k] - meantemp) * (input[j][k] - meanin);
                    ssreg += (temp[j][k] - meantemp) * (temp[j][k] - meantemp);
                }
            }
            results[i] = numer * numer / (ssreg * sstot);
        }
        return results;
    }

    public void calculateSDs() {
        BasicStatistics bs = new BasicStatistics();
        sds = new double[npcs];
        double[] temp = new double[out.length];
        for (int i = 0; i < npcs; i++) {
            for (int j = 0; j < temp.length; j++) {
                temp[j] = out[j][i];
            }
            sds[i] = bs.calculateSD(temp, true);
        }
    }

    public float[][] getDistanceMatrix() {
        float[][] results = new float[out.length][];
        for (int i = 0; i < results.length; i++) {
            results[i] = new float[i + 1];
            for (int j = 0; j < i; j++) {
                double s = 0;
                for (int k = 0; k < npcs; k++) {
                    s += (out[i][k] - out[j][k]) * (out[i][k] - out[j][k]);
                }
                results[i][j] = (float) Math.sqrt(s);
            }
        }
        return results;
    }

    public void augmentMDS(double[] data) {
        if (data.length == out.length) {
            BasicStatistics bs = new BasicStatistics();
            double sd = bs.calculateSD(data, true);
            float[][] results = new float[out.length][npcs + 1];
            for (int i = 0; i < out.length; i++) {
                for (int j = 0; j < npcs; j++) {
                    results[i][j] = out[i][j];
                }
                results[i][npcs] = (float) (data[i] * sds[0] / sd);
            }
            out = results;
            npcs++;
        } else {
            System.out.println("WARNING: MDS can't be augmented because array sizes don't match");
        }
    }
}
