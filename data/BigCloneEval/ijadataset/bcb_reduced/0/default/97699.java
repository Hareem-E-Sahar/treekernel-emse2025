/**
 * This class runs an in place merge sort on an array of any object type
 * @author Evan Compton
 * @version 1.4 7/13/2011
 */
public class POSort<T> {

    /**
   * Initializes a temporary array and performs a merge sort on the given array
   * @param array
   */
    public void mergeSort(T[] array) {
        T temp[] = (T[]) new Comparable[array.length];
        mergeSort(array, temp, 0, (array.length - 1));
    }

    private void mergeSort(T[] array, T[] temp, int lo, int hi) {
        if (hi <= lo) {
            return;
        }
        int mid = (hi + lo) / 2;
        mergeSort(array, temp, lo, mid);
        mergeSort(array, temp, mid + 1, hi);
        merge(array, temp, lo, mid, hi);
    }

    private void merge(T[] array, T[] temp, int lo, int mid, int hi) {
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            temp[k] = array[k];
        }
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                array[k] = temp[j++];
            } else if (j > hi) {
                array[k] = temp[i++];
            } else if (((POCommon) temp[j]).compareTo(temp[i]) < 0) {
                array[k] = temp[j++];
            } else {
                array[k] = temp[i++];
            }
        }
    }
}
