package de.ufinke.cubaja.sort;

import java.util.Comparator;

/**
 * Mergesort algorithm.
 * Stable sort; the original sequence of equal elements will be retained.
 * @author Uwe Finke
 */
public class Mergesort implements SortAlgorithm {

    /**
   * Constructor.
   */
    public Mergesort() {
    }

    @SuppressWarnings("rawtypes")
    public void sort(final Object[] array, final int size, final Comparator comparator) {
        if (size == 0) {
            return;
        }
        Object[] temp = new Object[size];
        mergesort(array, temp, 0, size - 1, comparator);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void mergesort(final Object[] entries, final Object[] temp, final int left, final int right, final Comparator comparator) {
        if (right <= left) {
            return;
        }
        int i = left;
        boolean sorted = true;
        while (sorted && (i < right)) {
            sorted = comparator.compare(entries[i], entries[++i]) <= 0;
        }
        if (sorted) {
            return;
        }
        final int middle = (left + right) / 2;
        mergesort(entries, temp, left, middle, comparator);
        mergesort(entries, temp, middle + 1, right, comparator);
        System.arraycopy(entries, left, temp, left, right - left + 1);
        int iLeft = left;
        int iRight = middle + 1;
        int iOut = left;
        while ((iLeft <= middle) && (iRight <= right)) {
            entries[iOut++] = comparator.compare(temp[iLeft], temp[iRight]) <= 0 ? temp[iLeft++] : temp[iRight++];
        }
        if (iLeft <= middle) {
            System.arraycopy(temp, iLeft, entries, iOut, middle - iLeft + 1);
        } else {
            System.arraycopy(temp, iRight, entries, iOut, right - iRight + 1);
        }
    }
}
