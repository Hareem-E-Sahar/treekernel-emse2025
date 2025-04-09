package cz.jAlgorithm.sort;

/**
 *
 * @author Pavel Micka
 */
public class MergeSort {

    public static void mergeSort(Comparable[] array) {
        Comparable[] aux = new Comparable[array.length];
        mergeSort(array, aux, 0, array.length - 1);
    }

    /** Merge sort
     * @param array array to sort
     * @param aux helper array
     * @param left first element index that we can touch
     * @param right last element index, that we can touch
     */
    private static void mergeSort(Comparable[] array, Comparable[] aux, int left, int right) {
        if (left == right) {
            return;
        }
        int middleIndex = (left + right) / 2;
        mergeSort(array, aux, left, middleIndex);
        mergeSort(array, aux, middleIndex + 1, right);
        merge(array, aux, left, right);
        for (int i = left; i <= right; i++) {
            array[i] = aux[i];
        }
    }

    /**
     * Slevani pro mergesort
     * @param array pole k serazeni
     * @param aux pomocne pole (stejne velikosti jako razene)
     * @param left prvni index, na ktery smim sahnout
     * @param right posledni index, na ktery smim sahnout
     */
    private static void merge(Comparable[] array, Comparable[] aux, int left, int right) {
        int middleIndex = (left + right) / 2;
        int leftIndex = left;
        int rightIndex = middleIndex + 1;
        int auxIndex = left;
        while (leftIndex <= middleIndex && rightIndex <= right) {
            if (array[leftIndex].compareTo(array[rightIndex]) != -1) {
                aux[auxIndex] = array[leftIndex++];
            } else {
                aux[auxIndex] = array[rightIndex++];
            }
            auxIndex++;
        }
        while (leftIndex <= middleIndex) {
            aux[auxIndex] = array[leftIndex++];
            auxIndex++;
        }
        while (rightIndex <= right) {
            aux[auxIndex] = array[rightIndex++];
            auxIndex++;
        }
    }
}
