package computational.geometry.visibilitygraph;

import computational.geometry.*;

public class MergeSort {

    public static void sort(Point[] a, Point p) {
        mergeSort(a, 0, a.length - 1, p);
    }

    /**
	 Ordina un intervallo di un array, usando l'algoritmo 
	 dell'ordinamento per fusione.
	 @param a l'array da ordinare
	 @param from il primo indice dell'intervallo da ordinare
	 @param to l'ultimo indice dell'intervallo da ordinare
	 */
    private static void mergeSort(Point[] a, int from, int to, Point p) {
        if (from == to) return;
        int mid = (from + to) / 2;
        mergeSort(a, from, mid, p);
        mergeSort(a, mid + 1, to, p);
        merge(a, from, mid, to, p);
    }

    private static void merge(Point[] a, int from, int mid, int to, Point p) {
        int n = to - from + 1;
        Point[] b = new Point[n];
        int i1 = from;
        int i2 = mid + 1;
        int j = 0;
        while (i1 <= mid && i2 <= to) {
            if (BasicTests.collinear(a[i1], p, a[i2])) {
                if (p.distance2(a[i1]) < p.distance2(a[i2])) {
                    b[j] = a[i1];
                    i1++;
                } else {
                    b[j] = a[i2];
                    i2++;
                }
            } else {
                if (Turn.LEFT == BasicTests.turns(a[i1], p, a[i2])) {
                    b[j] = a[i1];
                    i1++;
                } else {
                    b[j] = a[i2];
                    i2++;
                }
            }
            j++;
        }
        while (i1 <= mid) {
            b[j] = a[i1];
            i1++;
            j++;
        }
        while (i2 <= to) {
            b[j] = a[i2];
            i2++;
            j++;
        }
        for (j = 0; j < n; j++) a[from + j] = b[j];
    }
}
