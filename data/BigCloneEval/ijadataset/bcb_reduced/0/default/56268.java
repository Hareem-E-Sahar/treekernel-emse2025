/**
 * A merge sort demonstration algorithm
 * SortAlgorithm.java, Thu Oct 27 10:32:35 1994
 *
 * @author Jason Harrison@cs.ubc.ca
 * @version 	1.1, 12 Jan 1998
 */
class MergeSortAlgorithm extends SortAlgorithm {

    void sort(int a[], int lo0, int hi0) throws Exception {
        int lo = lo0;
        int hi = hi0;
        pause(lo, hi);
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid);
        sort(a, mid + 1, hi);
        int end_lo = mid;
        int start_hi = mid + 1;
        while ((lo <= end_lo) && (start_hi <= hi)) {
            pause(lo);
            if (stopRequested) {
                return;
            }
            if (a[lo] < a[start_hi]) {
                lo++;
            } else {
                int T = a[start_hi];
                for (int k = start_hi - 1; k >= lo; k--) {
                    a[k + 1] = a[k];
                    pause(lo);
                }
                a[lo] = T;
                lo++;
                end_lo++;
                start_hi++;
            }
        }
    }

    void sort(int a[]) throws Exception {
        sort(a, 0, a.length - 1);
    }
}
