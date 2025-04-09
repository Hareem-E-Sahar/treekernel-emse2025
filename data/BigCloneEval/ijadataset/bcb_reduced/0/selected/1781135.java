package services.testsuitegen.impl;

import java.util.Vector;

/**
 * This class is downloaded from the WWW. Thanks to its author, Henk Jan Nootenboom, it�s freely available.
 * This implementation of C.A.R Hoare�s quick sort algorithm is used to sort the Test.java instances and the 
 * ExecutionHost.java instances in a descending order.
 * 
 * @author Hochschule Furtwangen, University of Applied Science
 *
 */
public class QuickSort {

    /** 
 * Sort the entire vector, if it is not empty
 */
    public void quickSort(Vector elements) {
        if (!elements.isEmpty()) {
            this.quickSort(elements, 0, elements.size() - 1);
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
    private void quickSort(Vector elements, int lowIndex, int highIndex) {
        int lowToHighIndex;
        int highToLowIndex;
        int pivotIndex;
        Comparable pivotValue;
        Comparable lowToHighValue;
        Comparable highToLowValue;
        Comparable parking;
        int newLowIndex;
        int newHighIndex;
        int compareResult;
        lowToHighIndex = lowIndex;
        highToLowIndex = highIndex;
        pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
        pivotValue = (Comparable) elements.elementAt(pivotIndex);
        newLowIndex = highIndex + 1;
        newHighIndex = lowIndex - 1;
        while ((newHighIndex + 1) < newLowIndex) {
            lowToHighValue = (Comparable) elements.elementAt(lowToHighIndex);
            while (lowToHighIndex < newLowIndex & lowToHighValue.compareTo(pivotValue) < 0) {
                newHighIndex = lowToHighIndex;
                lowToHighIndex++;
                lowToHighValue = (Comparable) elements.elementAt(lowToHighIndex);
            }
            highToLowValue = (Comparable) elements.elementAt(highToLowIndex);
            while (newHighIndex <= highToLowIndex & (highToLowValue.compareTo(pivotValue) > 0)) {
                newLowIndex = highToLowIndex;
                highToLowIndex--;
                highToLowValue = (Comparable) elements.elementAt(highToLowIndex);
            }
            if (lowToHighIndex == highToLowIndex) {
                newHighIndex = lowToHighIndex;
            } else if (lowToHighIndex < highToLowIndex) {
                int result = lowToHighValue.compareTo(highToLowValue);
                boolean bCompareResult = (result >= 0);
                if (bCompareResult) {
                    parking = lowToHighValue;
                    elements.setElementAt(highToLowValue, lowToHighIndex);
                    elements.setElementAt(parking, highToLowIndex);
                    newLowIndex = highToLowIndex;
                    newHighIndex = lowToHighIndex;
                    lowToHighIndex++;
                    highToLowIndex--;
                }
            }
        }
        if (lowIndex < newHighIndex) {
            this.quickSort(elements, lowIndex, newHighIndex);
        }
        if (newLowIndex < highIndex) {
            this.quickSort(elements, newLowIndex, highIndex);
        }
    }
}
