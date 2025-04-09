package fs;

import java.util.Random;
import java.util.Vector;
import utilities.RadixSort;

class Classifier {

    Vector table;

    Vector instances;

    int[] labels;

    public Classifier() {
        table = new Vector();
        instances = new Vector();
    }

    private boolean equalInstances(int line, Vector I, char[][] A) {
        for (int i = 0; i < I.size(); i++) {
            if (A[line - 1][(Integer) I.elementAt(i)] != A[line][(Integer) I.elementAt(i)]) {
                return false;
            }
        }
        return true;
    }

    private int indexMaxValue(double[] v) {
        int indexMax = -1;
        double maximum = Integer.MIN_VALUE;
        for (int i = 0; i < v.length; i++) {
            if (maximum < v[i]) {
                indexMax = i;
                maximum = v[i];
            }
        }
        Vector ties = new Vector();
        for (int i = 0; i < v.length; i++) {
            if (maximum == v[i]) {
                ties.add(i);
            }
        }
        if (ties.size() > 1) {
            Random rn = new Random(System.nanoTime());
            int sorteio = rn.nextInt(ties.size());
            return ((Integer) ties.get(sorteio));
        } else return indexMax;
    }

    private double instanceIndex(char[] sample, Vector I, int n) {
        double instance = 0;
        int dim = I.size();
        for (int i = 0; i < I.size(); i++) {
            instance += sample[(Integer) I.elementAt(dim - i - 1)] * Math.pow(n, i);
        }
        return instance;
    }

    public void addTableLine(char[] sample, Vector I, int[] pYdX, int pX, int n, int c) {
        double instance = instanceIndex(sample, I, n);
        double[] tableLine = new double[c];
        for (int k = 0; k < c; k++) {
            tableLine[k] = pYdX[k];
            pYdX[k] = 0;
        }
        pX = 0;
        instances.add(instance);
        table.add(tableLine);
    }

    public int binarySearch(double value) {
        int start = 0;
        int end = instances.size() - 1;
        while (start <= end) {
            int v = (start + end) / 2;
            if ((Double) instances.elementAt(v) == value) {
                return v;
            } else if ((Double) instances.elementAt(v) < value) {
                start = v + 1;
            } else {
                end = v - 1;
            }
        }
        return -1;
    }

    public int[] instanceVector(double instanceIndex, int n, int d) {
        int[] V = new int[d];
        for (int i = d - 1; i >= 0; i--) {
            if (instanceIndex == 0) {
                break;
            }
            V[i] = (int) instanceIndex % n;
            instanceIndex = (double) Math.floor(instanceIndex / n);
        }
        return V;
    }

    public double euclideanDistance(int[] v1, int[] v2) {
        double quadraticSum = 0;
        for (int i = 0; i < v1.length; i++) {
            quadraticSum += Math.pow((double) v1[i] - v2[i], 2);
        }
        return Math.sqrt(quadraticSum);
    }

    public int nearestNeighbors(double instanceIndex, int n, int d, int c) {
        int[] instanceValues = instanceVector(instanceIndex, n, d);
        double[] distances = new double[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            int[] currentInstance = instanceVector((Double) instances.elementAt(i), n, d);
            distances[i] = euclideanDistance(instanceValues, currentInstance);
        }
        double[] pYdX = new double[c];
        while (true) {
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < instances.size(); i++) {
                if (distances[i] < minDist) {
                    minDist = distances[i];
                }
            }
            for (int i = 0; i < instances.size(); i++) {
                if (distances[i] == minDist) {
                    double[] temp = (double[]) table.elementAt(i);
                    for (int j = 0; j < c; j++) {
                        pYdX[j] += temp[j];
                    }
                    distances[i] = Double.MAX_VALUE;
                }
            }
            int indexMax = indexMaxValue((double[]) pYdX);
            if (indexMax > -1) {
                return indexMax;
            }
        }
    }

    public void classifierTable(char[][] A, Vector I, int n, int c) {
        int lines = A.length;
        int pX = 0;
        int[] pYdX = new int[c];
        RadixSort.radixSort(A, I, n);
        for (int j = 0; j < lines; j++) {
            if (j > 0 && !equalInstances(j, I, A)) {
                addTableLine(A[j - 1], I, pYdX, pX, n, c);
            }
            pYdX[A[j][A[j].length - 1]]++;
            pX++;
        }
        addTableLine(A[lines - 1], I, pYdX, pX, n, c);
    }

    public double[] classifyTestSamples(char[][] A, Vector I, int n, int c) {
        int lines = A.length;
        labels = new int[lines];
        double[] testInstances = new double[lines];
        for (int i = 0; i < lines; i++) {
            testInstances[i] = instanceIndex(A[i], I, n);
            int index = binarySearch(testInstances[i]);
            if (index == -1) {
                labels[i] = nearestNeighbors(testInstances[i], n, I.size(), c);
            } else {
                labels[i] = indexMaxValue((double[]) table.elementAt(index));
                if (labels[i] == -1) {
                    labels[i] = nearestNeighbors(testInstances[i], n, I.size(), c);
                }
            }
        }
        return (testInstances);
    }
}
