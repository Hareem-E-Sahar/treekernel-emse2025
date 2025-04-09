package drcube.views.graphics2D;

public class MergeSorter extends AbstractSorter {

    int comparisons;

    public void sort(CubeFacelet[] a) {
        comparisons = 0;
        mergesort(a, 0, a.length - 1);
    }

    private void mergesort(CubeFacelet[] a, int l, int r) {
        if (l < r) {
            int q = (l + r) / 2;
            mergesort(a, l, q);
            mergesort(a, q + 1, r);
            merge(a, l, q, r);
        }
    }

    private void merge(CubeFacelet[] a, int l, int q, int r) {
        q++;
        while ((l < q) && (q <= r)) {
            if (a[l].isGreater(a[q])) {
                comparisons++;
                exchange(a, q, l);
                q++;
            }
            l++;
        }
    }

    private void exchange(CubeFacelet[] a, int i, int j) {
        CubeFacelet t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
