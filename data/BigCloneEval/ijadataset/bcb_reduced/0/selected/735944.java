package cz.jAlgorithm.search.array;

/**
 *
 * @author Pavel Micka
 */
public class BinarySearch {

    public static int binarySearch(int[] array, int value) {
        return binarySearch(array, 0, array.length - 1, value);
    }

    /**
     * Binary search
     * @param array array to be searched
     * @param leftIndex first index, that can be touched
     * @param rightIndex last index, that we can touch
     * @param value value to search for
     * @return index of the value, -1 if not found
     */
    private static int binarySearch(int[] array, int leftIndex, int rightIndex, int value) {
        if (leftIndex == rightIndex && array[leftIndex] != value) {
            return -1;
        }
        int middleIndex = (leftIndex + rightIndex) / 2;
        if (array[middleIndex] == value) {
            return middleIndex;
        } else if (array[middleIndex] > value) {
            return binarySearch(array, middleIndex + 1, rightIndex, value);
        } else {
            return binarySearch(array, leftIndex, middleIndex - 1, value);
        }
    }
}
