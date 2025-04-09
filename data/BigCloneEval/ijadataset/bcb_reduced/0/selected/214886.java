package de.ibis.permoto.solver.sim.tech.simEngine1.dataAnalysis.sorting;

/**
 * A merge sort demonstration algorithm.
 * SortAlgorithm.java, Thu Oct 27 10:32:35 1994
 *
 * @author Jason Harrison@cs.ubc.ca
 * @version     1.1, 12 Jan 1998
 */
public class MergeSort implements SortAlgorithm {

    /**
	 *   pre :  0 <= p <= r <= A.length
	 *
	 *  post :  elements of A[p..r] are rearranged so that
	 *
	 *             A[p] <= A[p + 1] <= ... <= A[r]
	 */
    private static void sort(double[] A, int p, int r) {
        int q;
        if (p < r) {
            q = (p + r) / 2;
            sort(A, p, q);
            sort(A, q + 1, r);
            merge(A, p, q, r);
        }
    }

    /**
	 *   pre :  (i)   0 <= p <= q < r <= A.length-1
	 *          (ii)  A[p] <= A[p+1] <= ... <= A[q]
	 *          (iii) A[q+1] <= A[q+2] <= ... <= A[r]
	 *
	 *  post :  elements of A[p..r] are rearranged so that
	 *
	 *             A[p] <= A[p+1] <= ... <= A[r]
	 */
    private static void merge(double[] A, int p, int q, int r) {
        int i, j, k;
        int N = r - p + 1;
        double[] copy = new double[N];
        for (i = 0; i < N; i++) copy[i] = A[p + i];
        int mid = q - p;
        int end = r - p;
        i = 0;
        j = mid + 1;
        k = p;
        while (i <= mid && j <= end) {
            if (copy[i] < copy[j]) {
                A[k] = copy[i];
                i++;
            } else {
                A[k] = copy[j];
                j++;
            }
            k++;
        }
        int a0, aN;
        if (i > mid) {
            a0 = j;
            aN = end;
        } else {
            a0 = i;
            aN = mid;
        }
        for (i = a0; i <= aN; i++) {
            A[k] = copy[i];
            k++;
        }
    }

    public void sort(double[] data) {
        sort(data, 0, data.length - 1);
    }
}
