package dataStructures;

public class SortArray {

    private static final int MIN_SIZE = 3;

    /** Task: Sorts the first n objects in an array into ascending order.
	 * @param a an array of comparable objects
	 * @param n an integer > 0 */
    public static void selectionSort(Comparable[] a, int n) {
        for (int index = 0; index < n - 1; index++) {
            int indexOfNextSmallest = indexOfSmallest(a, index, n - 1);
            swap(a, index, indexOfNextSmallest);
        }
    }

    /** Task: Determines the index of the smallest value in an array
	 * @param a and array of comparable objects
	 * @param first an integer > 0 that is the index of the first array element to consider
	 * @param last an integer >0 that is the index of the last array element to consider
	 * @return the index of the smallest value value amoung the array
	 */
    private static int indexOfSmallest(Comparable[] a, int first, int last) {
        Comparable min = a[first];
        int indexOfMin = first;
        for (int index = first + 1; index <= last; index++) {
            if (a[index].compareTo(min) < 0) {
                min = a[index];
                indexOfMin = index;
            }
        }
        return indexOfMin;
    }

    /** Task: Swaps the array elements a[i] and a[j].
	 * @param a an array of Comparable objects
	 * @param i an integer >=0 and <a.length
	 * @param j an integer >=0 and <a.length
	 */
    private static void swap(Comparable[] a, int i, int j) {
        Comparable temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public static void insertionSort(Comparable[] a, int first, int last) {
        for (int unsorted = first + 1; unsorted <= last; unsorted++) {
            Comparable firstUnsorted = a[unsorted];
            insertInOrder(firstUnsorted, a, first, unsorted - 1);
        }
    }

    public static void insertionSortRecursive(Comparable[] a, int first, int last) {
        if (first < last) {
            insertionSortRecursive(a, first, last - 1);
            insertInOrder(a[last], a, first, last - 1);
        }
    }

    private static void insertInOrder(Comparable element, Comparable[] a, int first, int last) {
        if (element.compareTo(a[last]) >= 0) a[last + 1] = element; else if (first < last) {
            a[last + 1] = a[last];
            insertInOrder(element, a, first, last - 1);
        } else {
            a[last + 1] = a[last];
            a[last] = element;
        }
    }

    /** Task: sorts equally spaced elements of an array into ascending order
	 * @param a an array of comparable integers
	 * @param first an integer > 0 that is the index of the first array element to consider
	 * @param last an integer >= first and < a.length that is the index of the last array element to consider
	 * @param space the difference between the indices of the elements to sort 
	 */
    private static void incrementalInsertionSort(Comparable[] a, int first, int last, int space) {
        int unsorted, index;
        for (unsorted = first + space; unsorted <= last; unsorted = unsorted + space) {
            Comparable firstUnsorted = a[unsorted];
            for (index = unsorted - space; (index >= first) && (firstUnsorted.compareTo(a[index]) < 0); index = index - space) {
                a[index + space] = a[index];
            }
            a[index + space] = firstUnsorted;
        }
    }

    public static void shellSort(Comparable[] a, int first, int last) {
        int n = last - first + 1;
        for (int space = n / 2; space > 0; space = space / 2) {
            for (int begin = first; begin < (first + space); begin++) incrementalInsertionSort(a, begin, last, space);
        }
    }

    public static void mergeSort(Comparable[] a, int first, int last) {
    }

    /** Task: Sorts the first, middle and last elements of an array  
	 * @param a an array of Comparable objects
	 * @param first the integer index of the first array elemenet; first >- 0
	 * @param mid the integer index of the middle array element 
	 * @param last the integer index of the last array element; last - first>=2, last <a.length
	 */
    private static void sortFirstMiddleLast(Comparable[] a, int first, int mid, int last) {
        order(a, first, mid);
        order(a, mid, last);
        order(a, first, mid);
    }

    /** Task: Orders two given array elements into ascending order so that a[i] <= a[j]
	 * @param a an array of Comparable objects
	 * @param i an integer >= 0 and < array.length
	 * @param j an integer >= 0 and < array.length 
	 */
    private static void order(Comparable[] a, int i, int j) {
        if (a[i].compareTo(a[j]) > 0) swap(a, i, j);
    }

    /** Task: Partitions an array as part of quick sort into two subarrays called 
	 * 	Smaller and Larger that are separated by a single element called a pivot
	 *  Elements in Smaller are left of the pivot and <= pivot
	 *  Elements in Larger are right of the pivot and >- pivot
	 *  @param a an array of Comparable objects
	 *  @param first the integer index of the first array element; first >= 0
	 *  @param last the integer index of the last array element; last - first >= 3; last < a.length
	 *  @return the index of the pivot  
	 */
    private static int partition(Comparable[] a, int first, int last) {
        int mid = (first + last) / 2;
        sortFirstMiddleLast(a, first, mid, last);
        swap(a, mid, last - 1);
        int pivotIndex = last - 1;
        Comparable pivot = a[pivotIndex];
        int indexFromLeft = first + 1;
        int indexFromRight = last - 2;
        boolean done = false;
        while (!done) {
            while (a[indexFromLeft].compareTo(pivot) < 0) indexFromLeft++;
            while (a[indexFromRight].compareTo(pivot) > 0) indexFromRight--;
            if (indexFromLeft < indexFromRight) {
                swap(a, indexFromLeft, indexFromRight);
                indexFromLeft++;
                indexFromRight++;
            } else done = true;
        }
        swap(a, pivotIndex, indexFromLeft);
        pivotIndex = indexFromLeft;
        return pivotIndex;
    }

    /** Task: Sorts an array into ascending order. Uses quick sort with media-of-three
	 * pivot selection for arrays of at least MIN_SIZE elements and uses selection sort for other arrays
	 */
    public static void quickSort(Comparable[] a, int first, int last) {
        if (last - first + 1 < MIN_SIZE) insertionSortRecursive(a, first, last); else {
            int pivotIndex = partition(a, first, last);
            quickSort(a, first, pivotIndex - 1);
            quickSort(a, pivotIndex + 1, last);
        }
    }
}
