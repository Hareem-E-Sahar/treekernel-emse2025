public class Test {    public static void quickSort(Object s[], int lo, int hi, Comparator cmp) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        if (cmp.compare(s[lo], s[mid]) > 0) {
            Object tmp = s[lo];
            s[lo] = s[mid];
            s[mid] = tmp;
        }
        if (cmp.compare(s[mid], s[hi]) > 0) {
            Object tmp = s[mid];
            s[mid] = s[hi];
            s[hi] = tmp;
            if (cmp.compare(s[lo], s[mid]) > 0) {
                Object tmp2 = s[lo];
                s[lo] = s[mid];
                s[mid] = tmp2;
            }
        }
        int left = lo + 1;
        int right = hi - 1;
        if (left >= right) return;
        Object partition = s[mid];
        for (; ; ) {
            while (cmp.compare(s[right], partition) > 0) --right;
            while (left < right && cmp.compare(s[left], partition) <= 0) ++left;
            if (left < right) {
                Object tmp = s[left];
                s[left] = s[right];
                s[right] = tmp;
                --right;
            } else break;
        }
        quickSort(s, lo, left, cmp);
        quickSort(s, left + 1, hi, cmp);
    }
}