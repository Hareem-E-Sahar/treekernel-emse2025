package algoritms;

import junit.framework.TestCase;

public class BinarySearch extends TestCase {

    static int[] source = { 0, 2, 4, 6, 8, 10, 12, 14, 16, 17 };

    public void testSearch() {
        assertEquals(7, binarySearch(14, source));
        assertEquals(1, binarySearch(2, source));
        assertEquals(8, binarySearch(16, source));
        assertEquals(5, binarySearch(10, source));
        assertEquals(-1, binarySearch(20, source));
    }

    public void testSearchBloch() {
        assertEquals(7, binarySearchBloch(14, source));
        assertEquals(1, binarySearchBloch(2, source));
        assertEquals(8, binarySearchBloch(16, source));
        assertEquals(5, binarySearchBloch(10, source));
        assertEquals(-1, binarySearchBloch(20, source));
    }

    public void testSearchArray() {
        assertEquals(7, binarySearchArray(14, source));
        assertEquals(1, binarySearchArray(2, source));
        assertEquals(8, binarySearchArray(16, source));
        assertEquals(5, binarySearchArray(10, source));
        assertEquals(-1, binarySearchArray(20, source));
    }

    private static int binarySearch(int found, int[] s) {
        int L = s.length - 1;
        int k = 0;
        do {
            if (L % 2 == 0) {
                L >>>= 1;
            } else {
                L = (L + 1) >>> 1;
            }
            if (s[L + k] == found) return k + L; else if (s[L + k] < found) {
                k += L;
            }
        } while (L > 0 && L + k <= s.length);
        return -1;
    }

    private static int binarySearchBloch(int f, int[] s) {
        int low = 0;
        int high = s.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) >>> 1;
            if (s[mid] > f) {
                high = mid - 1;
            } else if (s[mid] < f) {
                low = mid + 1;
            } else return mid;
        }
        return -1;
    }

    private static int binarySearchArray(int f, int[] s) {
        int low = 0;
        int high = s.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (s[mid] > f) {
                high = mid - 1;
            } else if (s[mid] < f) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
}
