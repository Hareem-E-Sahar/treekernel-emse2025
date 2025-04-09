package rcm.util;

/**
 * Binary search routines.
 */
public abstract class BinarySearch {

    public static Debug debug = Debug.QUIET;

    /**
     * Search a sorted array of integers.
     * @param array Array of integers
     * @param offset Starting offset of subarray to search
     * @param length Length of subarray to search
     * @param x Value to search for
     * @return largest index i in subarray (offset &lt;= i &lt;= offset+length)
     * such that all elements below i in the subarray are strictly less
     * than x.  If x is found in the subarray, then array[i] == x (and i is
     * the first occurence of x in the subarray).  If x is not found, 
     * then array[i] is where x should be inserted in the sort order.
     */
    public static int search(int[] array, int offset, int length, int x) {
        if (length <= 0) return offset;
        int low = offset;
        int high = offset + length - 1;
        if (x <= array[low]) return low;
        if (x > array[high]) return high + 1;
        while (low + 1 < high) {
            int mid = (low + high) / 2;
            if (x <= array[mid]) high = mid; else low = mid;
        }
        debug.assertion(low + 1 == high);
        return high;
    }
}
