/**
 * A merge sort demonstration algorithm using extra space
 * SortAlgorithm.java, Thu Oct 27 10:32:35 1994
 *
 * @author Jack Snoeyink@cs.ubc.ca
 * @version 	1.0, 09 Jan 97
 */
class ExtraStorageMergeSortAlgorithm extends SortAlgorithm {

    void sort(int a[], int lo, int hi, int scratch[]) throws Exception {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        sort(a, lo, mid, scratch);
        sort(a, mid + 1, hi, scratch);
        int k, t_lo = lo, t_hi = mid + 1;
        for (k = lo; k <= hi; k++) if ((t_lo <= mid) && ((t_hi > hi) || (a[t_lo] < a[t_hi]))) {
            scratch[k] = a[t_lo++];
            pause(t_lo, t_hi);
        } else {
            scratch[k] = a[t_hi++];
            pause(t_lo, t_hi);
        }
        for (k = lo; k <= hi; k++) {
            a[k] = scratch[k];
            pause(k);
        }
    }

    void sort(int a[]) throws Exception {
        int scratch[] = new int[a.length];
        sort(a, 0, a.length - 1, scratch);
    }
}
