package edu.vt.middleware.dictionary.sort;

import java.util.Comparator;
import edu.vt.middleware.dictionary.WordLists;

/**
 * Provides an implementation of the quick sort algorithm.
 *
 * @author  Middleware Services
 * @version  $Revision: 1252 $ $Date: 2010-04-16 17:24:23 -0400 (Fri, 16 Apr 2010) $
 */
public class QuickSort implements ArraySorter {

    /** {@inheritDoc} */
    public void sort(final String[] array) {
        this.sort(array, WordLists.CASE_SENSITIVE_COMPARATOR);
    }

    /** {@inheritDoc} */
    public void sort(final String[] array, final Comparator<String> c) {
        if (array.length > 0) {
            sort(array, c, 0, array.length - 1);
        }
    }

    /**
   * This will sort the supplied array beginning at the lo index and ending at
   * the hi index, using the quick sort algorithm.
   *
   * @param  array  <code>String[]</code> to sort
   * @param  c  <code>Comparator</code> to sort with
   * @param  lo  <code>int</code> index to beginning sorting at
   * @param  hi  <code>int</code> index to stop sorting at
   */
    public static void sort(final String[] array, final Comparator<String> c, final int lo, final int hi) {
        final int m = (lo + hi) / 2;
        final String x = array[m];
        int i = lo;
        int j = hi;
        do {
            while (c.compare(x, array[i]) > 0) {
                i++;
            }
            while (c.compare(x, array[j]) < 0) {
                j--;
            }
            if (i <= j) {
                final String s = array[i];
                array[i] = array[j];
                array[j] = s;
                i++;
                j--;
            }
        } while (i <= j);
        if (lo < j) {
            sort(array, c, lo, j);
        }
        if (i < hi) {
            sort(array, c, i, hi);
        }
    }
}
