public class Test {    private void quickSort(int l, int r) {
        int M = 4;
        int i;
        int j;
        int v;
        if ((r - l) > M) {
            i = (r + l) / 2;
            if (lessThan(i, l)) {
                swap(l, i);
            }
            if (lessThan(r, l)) {
                swap(l, r);
            }
            if (lessThan(r, i)) {
                swap(i, r);
            }
            j = r - 1;
            swap(i, j);
            i = l;
            v = j;
            for (; ; ) {
                while (lessThan(++i, v)) {
                }
                while (lessThan(v, --j)) {
                }
                if (j < i) {
                    break;
                }
                swap(i, j);
            }
            swap(i, r - 1);
            quickSort(l, j);
            quickSort(i + 1, r);
        }
    }
}