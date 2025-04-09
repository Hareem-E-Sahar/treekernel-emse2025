public class Handler1 {

    static final int MERGE_SIZE = 2048;

    static final int QUICK_SIZE = 2048;

    static final int INSERTION_SIZE = 20;

    public Handler1() {
    }

    public void compute(int[] B, int[] C, int size) {
        if (size <= QUICK_SIZE) {
            quickSort(B, 0, size - 1);
        } else {
            int q = size / 2;
            int[] D = new int[q];
            int[] E = new int[q];
            for (int i = 0; i < q; i++) {
                E[i] = (B)[i];
                D[i] = (B)[q + i];
            }
            compute(E, D, q);
            int aLo = 0;
            int bLo = 0;
            int outLo = 0;
            while (aLo < q && bLo < q) {
                if (D[aLo] < E[bLo]) (B)[outLo++] = D[aLo++]; else (B)[outLo++] = E[bLo++];
            }
            while (aLo < q) (B)[outLo++] = D[aLo++];
            while (bLo < q) (B)[outLo++] = E[bLo++];
        }
    }

    static void quickSort(int[] A, int lo, int hi) {
        if (hi - lo + 1l <= INSERTION_SIZE) {
            for (int i = lo + 1; i <= hi; i++) {
                int t = A[i];
                int j = i - 1;
                while (j >= lo && A[j] > t) {
                    A[j + 1] = A[j];
                    --j;
                }
                A[j + 1] = t;
            }
            return;
        }
        int mid = (lo + hi) / 2;
        if (A[lo] > A[mid]) {
            int t = A[lo];
            A[lo] = A[mid];
            A[mid] = t;
        }
        if (A[mid] > A[hi]) {
            int t = A[mid];
            A[mid] = A[hi];
            A[hi] = t;
            if (A[lo] > A[mid]) {
                t = A[lo];
                A[lo] = A[mid];
                A[mid] = t;
            }
        }
        int left = lo + 1;
        int right = hi - 1;
        int partition = A[mid];
        for (; ; ) {
            while (A[right] > partition) --right;
            while (left < right && A[left] <= partition) ++left;
            if (left < right) {
                int t = A[left];
                A[left] = A[right];
                A[right] = t;
                --right;
            } else break;
        }
        quickSort(A, lo, left);
        quickSort(A, left + 1, hi);
    }
}
