package cody.stoutenburg.sortDetD.utilitaire;

import cody.stoutenburg.sortDetD.Sorts.Sort;

/**
 *
 * @author Stoutenburg Cody
 */
public class MergeSort {

    /**
     * Mergesort algorithm.
     * @param a an array of Comparable items.
     */
    public static void mergeSort(Sort[] a, String nomClasse) {
        Sort[] tmpArray = new Sort[a.length];
        mergeSortNiveau(a, tmpArray, 0, a.length - 1, nomClasse);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void mergeSortNiveau(Sort[] a, Sort[] tmpArray, int left, int right, String nomClasse) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSortNiveau(a, tmpArray, left, center, nomClasse);
            mergeSortNiveau(a, tmpArray, center + 1, right, nomClasse);
            mergeSortNiveauClasse(a, tmpArray, left, center + 1, right, nomClasse);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     */
    private static void mergeSortNiveauClasse(Sort[] a, Sort[] tmpArray, int leftPos, int rightPos, int rightEnd, String nomClasse) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        while (leftPos <= leftEnd && rightPos <= rightEnd) if (a[leftPos].getNiveauClasse(nomClasse) <= a[rightPos].getNiveauClasse(nomClasse)) tmpArray[tmpPos++] = a[leftPos++]; else tmpArray[tmpPos++] = a[rightPos++];
        while (leftPos <= leftEnd) tmpArray[tmpPos++] = a[leftPos++];
        while (rightPos <= rightEnd) tmpArray[tmpPos++] = a[rightPos++];
        for (int i = 0; i < numElements; i++, rightEnd--) a[rightEnd] = tmpArray[rightEnd];
    }

    /**
     * Mergesort algorithm.
     * @param a an array of Comparable items.
     */
    public static void mergeSortAlphabetique(Sort[] a) {
        Sort[] tmpArray = new Sort[a.length];
        mergeSortAlphabetique(a, tmpArray, 0, a.length - 1);
    }

    /**
     * Internal method that makes recursive calls.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param left the left-most index of the subarray.
     * @param right the right-most index of the subarray.
     */
    private static void mergeSortAlphabetique(Sort[] a, Sort[] tmpArray, int left, int right) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSortAlphabetique(a, tmpArray, left, center);
            mergeSortAlphabetique(a, tmpArray, center + 1, right);
            mergeSortAlphabetique(a, tmpArray, left, center + 1, right);
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     * @param a an array of Comparable items.
     * @param tmpArray an array to place the merged result.
     * @param leftPos the left-most index of the subarray.
     * @param rightPos the index of the start of the second half.
     * @param rightEnd the right-most index of the subarray.
     */
    private static void mergeSortAlphabetique(Sort[] a, Sort[] tmpArray, int leftPos, int rightPos, int rightEnd) {
        int leftEnd = rightPos - 1;
        int tmpPos = leftPos;
        int numElements = rightEnd - leftPos + 1;
        while (leftPos <= leftEnd && rightPos <= rightEnd) if (a[leftPos].getNom().compareTo(a[rightPos].getNom()) < 0) tmpArray[tmpPos++] = a[leftPos++]; else tmpArray[tmpPos++] = a[rightPos++];
        while (leftPos <= leftEnd) tmpArray[tmpPos++] = a[leftPos++];
        while (rightPos <= rightEnd) tmpArray[tmpPos++] = a[rightPos++];
        for (int i = 0; i < numElements; i++, rightEnd--) a[rightEnd] = tmpArray[rightEnd];
    }
}
