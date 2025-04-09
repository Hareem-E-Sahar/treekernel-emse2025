package lusc.net.sourceforge;

import java.util.*;

public class CalculateHopkinsStatistic {

    int type = 0;

    Random random = new Random(System.currentTimeMillis());

    int resamples = 100;

    String resultString[] = null;

    double[][] results = null;

    int[] picks = { 1, 2, 5, 10, 20 };

    double[][] silhouetteMax = null;

    public CalculateHopkinsStatistic(float[][] data, int resamples, int type) {
        this.resamples = resamples;
        this.type = type;
        int dims = data[0].length;
        int n = data.length;
        float[][] inputData = new float[dims][n];
        BasicStatistics bs = new BasicStatistics();
        for (int i = 0; i < dims; i++) {
            for (int j = 0; j < n; j++) {
                inputData[i][j] = data[j][i];
            }
            float av = (float) bs.calculateMean(inputData[i]);
            for (int j = 0; j < n; j++) {
                inputData[i][j] -= av;
            }
        }
        double[] sds = new double[dims];
        for (int i = 0; i < dims; i++) {
            sds[i] = bs.calculateSD(inputData[i], true);
        }
        for (int i = 0; i < picks.length; i++) {
            if (picks[i] >= n - 1) {
                picks[i] = n - 2;
            }
        }
        double realscore[] = calculateSumNNearestNeighbour(inputData, picks);
        float[][] simData = new float[dims][n];
        double[][] simresults = new double[picks.length][resamples];
        silhouetteMax = new double[n][resamples];
        for (int i = 0; i < resamples; i++) {
            for (int j = 0; j < dims; j++) {
                for (int k = 0; k < n; k++) {
                    simData[j][k] = (float) (random.nextGaussian() * sds[j]);
                }
            }
            double[] score = calculateSumNNearestNeighbour(simData, inputData, picks);
            for (int j = 0; j < picks.length; j++) {
                simresults[j][i] = score[j] / (score[j] + realscore[j]);
            }
        }
        for (int i = 0; i < n; i++) {
            Arrays.sort(silhouetteMax[i]);
        }
        resultString = new String[picks.length];
        results = new double[picks.length][4];
        for (int i = 0; i < picks.length; i++) {
            double meanscore = bs.calculateMean(simresults[i]);
            double sdscore = bs.calculateSD(simresults[i], true);
            double upper2point5 = bs.calculatePercentile(simresults[i], 2.5, true);
            double lower2point5 = bs.calculatePercentile(simresults[i], 2.5, false);
            results[i][0] = meanscore;
            results[i][1] = sdscore;
            results[i][2] = upper2point5;
            results[i][3] = lower2point5;
            resultString[i] = picks[i] + " MEAN: " + meanscore + " SD: " + sdscore + " UPPER 2.5: " + upper2point5 + " LOWER 2.5: " + lower2point5;
        }
    }

    public double calculateSumNearestNeighbour(float[][] data) {
        double sum = 0;
        double suma = 0;
        double a;
        int n = data.length;
        int m = data[0].length;
        for (int i = 0; i < m; i++) {
            double min = 100000;
            for (int j = 0; j < m; j++) {
                if (i != j) {
                    suma = 0;
                    for (int k = 0; k < n; k++) {
                        a = data[k][i] - data[k][j];
                        suma += a * a;
                    }
                    if (suma < min) {
                        min = suma;
                    }
                }
            }
            sum += Math.sqrt(min);
        }
        return sum;
    }

    public double calculateSumNearestNeighbour(float[][] data1, float[][] data2) {
        double sum = 0;
        double suma = 0;
        double a;
        int n = data1.length;
        int m = data1[0].length;
        for (int i = 0; i < m; i++) {
            double min = 100000;
            for (int j = 0; j < m; j++) {
                suma = 0;
                for (int k = 0; k < n; k++) {
                    a = data1[k][i] - data2[k][j];
                    suma += a * a;
                }
                if (suma < min) {
                    min = suma;
                }
            }
            sum += Math.sqrt(min);
        }
        return sum;
    }

    public double calculateSumNearestNeighbourGreaterThanX(float[][] data, double X) {
        double sum = 0;
        double suma = 0;
        double a;
        int n = data.length;
        int m = data[0].length;
        for (int i = 0; i < m; i++) {
            double min = 100000;
            for (int j = 0; j < m; j++) {
                if (i != j) {
                    suma = 0;
                    for (int k = 0; k < n; k++) {
                        a = data[k][i] - data[k][j];
                        suma += a * a;
                    }
                    if ((suma > X) && (suma < min)) {
                        min = suma;
                    }
                }
            }
            sum += min;
        }
        return sum;
    }

    public double calculateSumNNearestNeighbour(float[][] data, int N) {
        double sum = 0;
        double suma = 0;
        double a;
        int n = data.length;
        int N2 = N - 1;
        int m = data[0].length;
        for (int i = 0; i < m; i++) {
            double[] nearest = new double[N];
            for (int j = 0; j < N; j++) {
                nearest[j] = 1000000000;
            }
            for (int j = 0; j < m; j++) {
                if (i != j) {
                    suma = 0;
                    for (int k = 0; k < n; k++) {
                        a = data[k][i] - data[k][j];
                        suma += a * a;
                    }
                    if (suma < nearest[N2]) {
                        nearest[N2] = suma;
                        Arrays.sort(nearest);
                    }
                }
            }
            sum += nearest[N2];
        }
        return sum;
    }

    public double[] calculateSumNNearestNeighbour(float[][] data, int[] N) {
        double suma = 0;
        double a;
        int n = data.length;
        int m = data[0].length;
        double[] holder = new double[m];
        double results[] = new double[N.length];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                suma = 0;
                for (int k = 0; k < n; k++) {
                    a = data[k][i] - data[k][j];
                    suma += a * a;
                }
                holder[j] = suma;
            }
            Arrays.sort(holder);
            for (int j = 0; j < N.length; j++) {
                if (N[j] < holder.length) {
                    results[j] += Math.sqrt(holder[N[j]]);
                }
            }
        }
        for (int j = 0; j < N.length; j++) {
            results[j] /= m + 0.0;
        }
        return results;
    }

    public double[] calculateSumNNearestNeighbour(float[][] data1, float[][] data2, int[] N) {
        double suma = 0;
        double a;
        int n = data1.length;
        int m = data1[0].length;
        double[] holder = new double[m];
        double results[] = new double[N.length];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                suma = 0;
                for (int k = 0; k < n; k++) {
                    a = data1[k][i] - data2[k][j];
                    suma += a * a;
                }
                holder[j] = suma;
            }
            Arrays.sort(holder);
            for (int j = 0; j < N.length; j++) {
                results[j] += Math.sqrt(holder[N[j] - 1]);
            }
        }
        for (int j = 0; j < N.length; j++) {
            results[j] /= m + 0.0;
        }
        return results;
    }

    public double[] calculateDensity(float[][] data) {
        int n = data.length;
        int m = data[0].length;
        double[] d = new double[m * (m - 1) / 2];
        int p = 0;
        double suma = 0;
        double a;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < i; j++) {
                suma = 0;
                for (int k = 0; k < n; k++) {
                    a = data[k][i] - data[k][j];
                    suma += a * a;
                }
                d[p] = suma;
                p++;
            }
        }
        Arrays.sort(d);
        return d;
    }

    public float[][] createDistanceMatrix(float[][] data) {
        int n = data.length;
        int m = data[0].length;
        float[][] results = new float[m][];
        for (int i = 0; i < m; i++) {
            results[i] = new float[i + 1];
            for (int j = 0; j < i; j++) {
                double score = 0;
                for (int k = 0; k < n; k++) {
                    double diff = data[k][i] - data[k][j];
                    score += diff * diff;
                }
                results[i][j] = (float) Math.sqrt(score);
            }
        }
        return results;
    }
}
