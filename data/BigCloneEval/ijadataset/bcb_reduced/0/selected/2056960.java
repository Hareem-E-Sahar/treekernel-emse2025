package net.sourceforge.olduvai.lrac.util;

import java.util.Arrays;
import java.util.Random;

/**
 * A quick sort demonstration algorithm
 * SortAlgorithm.java
 *
 * @author James Gosling
 * @author Kevin A. Smith
 * @version     @(#)QSortAlgorithm.java 1.3, 29 Feb 1996
 * extended with TriMedian and InsertionSort by Denis Ahrens
 * with all the tips from Robert Sedgewick (Algorithms in C++).
 * It uses TriMedian and InsertionSort for lists shorts than 4.
 * <fuhrmann@cs.tu-berlin.de>
 */
public class TwinnedDoubleQuickSort {

    /** This is a generic version of C.A.R Hoare's Quick Sort 
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.<BR>
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param a         an integer array
   * @param b 		array to be shuffled identically to a
   * @param lo0     left boundary of array partition
   * @param hi0     right boundary of array partition
   */
    private static void QuickSort(double a[], double b[], int l, int r) {
        int M = 4;
        int i;
        int j;
        double v;
        if ((r - l) > M) {
            i = (r + l) / 2;
            if (a[l] > a[i]) swap(a, b, l, i);
            if (a[l] > a[r]) swap(a, b, l, r);
            if (a[i] > a[r]) swap(a, b, i, r);
            j = r - 1;
            swap(a, b, i, j);
            i = l;
            v = a[j];
            for (; ; ) {
                while (a[++i] < v) ;
                while (a[--j] > v) ;
                if (j < i) break;
                swap(a, b, i, j);
            }
            swap(a, b, i, r - 1);
            QuickSort(a, b, l, j);
            QuickSort(a, b, i + 1, r);
        }
    }

    private static void swap(double a[], double b[], int i, int j) {
        double T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
        T = b[i];
        b[i] = b[j];
        b[j] = T;
    }

    private static void InsertionSort(double a[], double b[], int lo0, int hi0) {
        int i;
        int j;
        double v;
        double v2;
        for (i = lo0 + 1; i <= hi0; i++) {
            v = a[i];
            v2 = b[i];
            j = i;
            while ((j > lo0) && (a[j - 1] > v)) {
                a[j] = a[j - 1];
                b[j] = b[j - 1];
                j--;
            }
            a[j] = v;
            b[j] = v2;
        }
    }

    public static void sort(double[] a, double[] b) {
        QuickSort(a, b, 0, a.length - 1);
        InsertionSort(a, b, 0, a.length - 1);
    }

    public static void main(String[] argv) {
        final int testSize = 10000;
        Random rand = new Random(System.currentTimeMillis());
        double t1[] = new double[testSize];
        double t2[] = new double[testSize];
        double t3[] = t1.clone();
        for (int i = 0; i < testSize; i++) {
            t1[i] = rand.nextInt(500) + 100;
            t2[i] = (rand.nextInt(100) + 800);
        }
        final long beginQuick = System.currentTimeMillis();
        TwinnedDoubleQuickSort.sort(t1, t2);
        final long endQuick = System.currentTimeMillis();
        final long beginJavaSort = System.currentTimeMillis();
        Arrays.sort(t3);
        final long endJavaSort = System.currentTimeMillis();
        System.out.println("Quick took: " + (endQuick - beginQuick));
        System.out.println("Java took: " + (endJavaSort - beginJavaSort));
    }
}
