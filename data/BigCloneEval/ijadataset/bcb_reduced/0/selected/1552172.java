package arraySort;

import java.util.Arrays;

/**
 * provides methods to insert an element in a sorted array.
 * 
 * @author Jakob Karolus, Kevin Munk
 * @version 1.0
 * 
 */
public class ArraySort {

    /**
	 * inserts a number into a sorted array of numbers.
	 * 
	 * @param element
	 *            the element to insert.
	 * @param array
	 *            the sorted array.
	 * @return the sorted array with the element at the right position.
	 */
    public static int[] insertElement(int element, int[] array) {
        int[] tempArray = Arrays.copyOf(array, array.length + 1);
        tempArray[tempArray.length - 1] = element;
        Arrays.sort(tempArray);
        return tempArray;
    }

    /**
	 * inserts a number into a sorted array of numbers.
	 * 
	 * @param element
	 *            the element to insert.
	 * @param array
	 *            the sorted array.
	 * @return the sorted array with the element at the right position.
	 */
    public static int[] insertElementFast(int element, int[] array) {
        int indexElement;
        indexElement = binaereSuche(0, array.length - 1, element, array);
        int[] tmpArray = Arrays.copyOf(array, array.length + 1);
        for (int j = 0; j < indexElement; j++) {
            tmpArray[j] = array[j];
        }
        tmpArray[indexElement] = element;
        for (int j = indexElement + 1; j < tmpArray.length; j++) {
            tmpArray[j] = array[j - 1];
        }
        return tmpArray;
    }

    public static int binaereSuche(int LiIndex, int ReIndex, int element, int[] array) {
        if (LiIndex > ReIndex) return -1;
        if (LiIndex == ReIndex) {
            if (element < array[LiIndex]) {
                return LiIndex;
            } else {
                return LiIndex + 1;
            }
        } else {
            int NeuLi, NeuRe, PivotIndex;
            PivotIndex = (ReIndex + LiIndex) / 2;
            if (element < array[PivotIndex]) {
                NeuLi = LiIndex;
                NeuRe = PivotIndex;
                return binaereSuche(NeuLi, NeuRe, element, array);
            } else {
                NeuLi = PivotIndex + 1;
                NeuRe = ReIndex;
                return binaereSuche(NeuLi, NeuRe, element, array);
            }
        }
    }

    public static int[] insertElementFastFast(int element, int[] array) {
        int indexElement;
        indexElement = binaereSuche(0, array.length - 1, element, array);
        int[] tmpArray = new int[array.length + 1];
        System.arraycopy(array, 0, tmpArray, 0, indexElement);
        System.arraycopy(array, indexElement, tmpArray, indexElement + 1, array.length - indexElement);
        tmpArray[indexElement] = element;
        return tmpArray;
    }
}
