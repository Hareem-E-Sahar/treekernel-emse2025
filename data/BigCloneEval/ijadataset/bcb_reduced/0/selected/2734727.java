package org.expasy.jpl.insilico.ms.peak;

public class JPLMSPeakQuickSorter {

    public static final void sort(JPLITheoMSPeak[] peaks) {
        if (peaks.length > 0) {
            sort(peaks, 0, peaks.length - 1);
        }
    }

    /**
	 * Implementation retrieved from {@link Java Performance Tuning, Jack Shirazi}
	 * adapted for MS peaks.
	 * 
	 * @param peaks the peaks to sort.
	 * @param from the index from where the sorting occurs.
	 * @param to the index to where the sorting occurs.
	 */
    public static final void sort(JPLITheoMSPeak[] peaks, int from, int to) {
        if (from >= to) {
            return;
        }
        int mid = (from + to) / 2;
        JPLITheoMSPeak middle = peaks[mid];
        JPLITheoMSPeak tmp;
        if (peaks[from].compareTo(middle) > 0) {
            peaks[mid] = peaks[from];
            peaks[from] = middle;
            middle = peaks[mid];
        }
        if (middle.compareTo(peaks[to]) > 0) {
            peaks[mid] = peaks[to];
            peaks[to] = middle;
            middle = peaks[mid];
            if (peaks[from].compareTo(middle) > 0) {
                peaks[mid] = peaks[from];
                peaks[from] = middle;
                middle = peaks[mid];
            }
        }
        int left = from + 1;
        int right = to - 1;
        if (left >= right) {
            return;
        }
        for (; ; ) {
            while (peaks[right].compareTo(middle) > 0) {
                right--;
            }
            while (left < right && peaks[left].compareTo(middle) <= 0) {
                left++;
            }
            if (left < right) {
                tmp = peaks[left];
                peaks[left] = peaks[right];
                peaks[right] = tmp;
                right--;
            } else {
                break;
            }
        }
        sort(peaks, from, left);
        sort(peaks, left + 1, to);
    }

    /** Sort the entire vector, if it is not empty
	 */
    public static final void quickSort(JPLITheoMSPeak[] peaks) {
        if (peaks.length > 0) {
            quickSort(peaks, 0, peaks.length - 1);
        }
    }

    /**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2005 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
    public static final void quickSort(JPLITheoMSPeak[] peaks, int lowIndex, int highIndex) {
        int lowToHighIndex;
        int highToLowIndex;
        int pivotIndex;
        JPLITheoMSPeak pivotValue;
        JPLITheoMSPeak lowToHighValue;
        JPLITheoMSPeak highToLowValue;
        JPLITheoMSPeak parking;
        int newLowIndex;
        int newHighIndex;
        int compareResult;
        lowToHighIndex = lowIndex;
        highToLowIndex = highIndex;
        pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
        pivotValue = peaks[pivotIndex];
        newLowIndex = highIndex + 1;
        newHighIndex = lowIndex - 1;
        while ((newHighIndex + 1) < newLowIndex) {
            lowToHighValue = peaks[lowToHighIndex];
            while (lowToHighIndex < newLowIndex & lowToHighValue.compareTo(pivotValue) < 0) {
                newHighIndex = lowToHighIndex;
                lowToHighIndex++;
                lowToHighValue = peaks[lowToHighIndex];
            }
            highToLowValue = peaks[highToLowIndex];
            while (newHighIndex <= highToLowIndex & (highToLowValue.compareTo(pivotValue) > 0)) {
                newLowIndex = highToLowIndex;
                highToLowIndex--;
                highToLowValue = peaks[highToLowIndex];
            }
            if (lowToHighIndex == highToLowIndex) {
                newHighIndex = lowToHighIndex;
            } else if (lowToHighIndex < highToLowIndex) {
                compareResult = lowToHighValue.compareTo(highToLowValue);
                if (compareResult >= 0) {
                    parking = lowToHighValue;
                    peaks[lowToHighIndex] = highToLowValue;
                    peaks[highToLowIndex] = parking;
                    newLowIndex = highToLowIndex;
                    newHighIndex = lowToHighIndex;
                    lowToHighIndex++;
                    highToLowIndex--;
                }
            }
        }
        if (lowIndex < newHighIndex) {
            quickSort(peaks, lowIndex, newHighIndex);
        }
        if (newLowIndex < highIndex) {
            quickSort(peaks, newLowIndex, highIndex);
        }
    }
}
