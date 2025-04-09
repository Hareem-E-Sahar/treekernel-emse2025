package SortingAlgorithms;

public class MergeSort implements SortAlgorithm {

    public int[] mergecall(int[] r) {
        mergesort(r, 0, r.length - 1);
        return r;
    }

    private void mergesort(int[] r, int lo, int hi) {
        if (lo < hi) {
            int m = (hi + lo) / 2;
            mergesort(r, lo, m);
            mergesort(r, m + 1, hi);
            merge(r, lo, m, hi);
        }
    }

    private void merge(int[] r, int lo, int mid, int hi) {
        int k = lo;
        int i = lo;
        int j = mid + 1;
        int[] aux = r.clone();
        while (k <= hi) {
            if (i > mid) {
                r[k] = aux[j];
                j++;
            } else if (j > hi) {
                r[k] = aux[i];
                i++;
            } else if (aux[j] < aux[i]) {
                r[k] = aux[j];
                j++;
            } else {
                r[k] = aux[i];
                i++;
            }
            k++;
        }
    }

    public void run(int[] array) {
        mergecall(array);
    }
}
