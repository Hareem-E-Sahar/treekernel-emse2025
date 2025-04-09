package com.memoire.fu;

public class FuSortint {

    public static void sort(int[] _a) {
        sort(_a, INT_COMPARATOR);
    }

    public static void sort(int[] _a, Comparator _cmp) {
        sort(_a, 0, _a.length, _cmp);
    }

    public static void sort(int[] _a, int _from, int _to) {
        sort(_a, _from, _to, INT_COMPARATOR);
    }

    public static void sort(int[] _a, int _from, int _to, Comparator _cmp) {
        int[] b = new int[_a.length];
        System.arraycopy(_a, 0, b, 0, _a.length);
        merge(b, _a, _from, _to, _cmp);
    }

    private static final void merge(int[] _src, int[] _dst, int _low, int _high, Comparator _cmp) {
        int length = _high - _low;
        if (length < 7) {
            for (int i = _low; i < _high; i++) for (int j = i; (j > _low) && (_cmp.compare(_dst[j - 1], _dst[j]) > 0); j--) swap(_dst, j, j - 1);
            return;
        }
        int mid = (_low + _high) / 2;
        merge(_dst, _src, _low, mid, _cmp);
        merge(_dst, _src, mid, _high, _cmp);
        if (_cmp.compare(_src[mid - 1], _src[mid]) <= 0) {
            System.arraycopy(_src, _low, _dst, _low, length);
            return;
        }
        int p = _low;
        int q = mid;
        for (int i = _low; i < _high; i++) {
            if ((q >= _high) || (p < mid) && (_cmp.compare(_src[p], _src[q]) <= 0)) _dst[i] = _src[p++]; else _dst[i] = _src[q++];
        }
    }

    private static final void swap(int[] _x, int _a, int _b) {
        int t = _x[_a];
        _x[_a] = _x[_b];
        _x[_b] = t;
    }

    public interface Comparator {

        int compare(int _a, int _b);
    }

    public static final class Reverse implements Comparator {

        private Comparator c_;

        public Reverse(Comparator _c) {
            c_ = _c;
        }

        public int compare(int _a, int _b) {
            return -c_.compare(_a, _b);
        }
    }

    public static final Comparator INT_COMPARATOR = new Comparator() {

        public int compare(int _a, int _b) {
            return _a < _b ? -1 : ((_a == _b) ? 0 : 1);
        }
    };

    public static final Comparator REVERSE_INT_COMPARATOR = new Comparator() {

        public int compare(int _a, int _b) {
            return _b < _a ? -1 : ((_a == _b) ? 0 : 1);
        }
    };
}
