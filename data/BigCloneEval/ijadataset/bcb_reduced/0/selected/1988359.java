package algoritms;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 * @author Sergiy Doroshenko
 */
public class MergeSortAdvance extends TestCase {

    private static int[] source;

    /**
     * @param args
     */
    public void testQuickSort() {
        System.out.println("start");
        int result[] = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        source = ShuffleArray.shuffle(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        System.out.println(Arrays.toString(source));
        assertFalse(Arrays.equals(result, source));
        mergeSort(source);
        System.out.println(Arrays.toString(source));
        assertTrue(Arrays.equals(result, source));
    }

    /**
     * @param source2
     */
    public void mergeSort(int[] s) {
        source = s;
        if (source.length < 2) return;
        split(0, s.length - 1);
    }

    private static void split(final int first, final int last) {
        if (first < last) {
            int middle = (first + last) / 2;
            split(first, middle);
            split(middle + 1, last);
            merge(first, middle, middle + 1, last);
        }
    }

    /**
    * merge (leftFirst, leftLast, rightFirst, rightLast)
    *(uses a local array, tempArray)
    *
    Set saveFirst to leftFirst // To know where to copy back
    Set index to leftFirst
    while more items in left half AND more items in right half
    if values[leftFirst] < values[rightFirst]
    Set tempArray[index] to values[leftFirst]
    Increment leftFirst
    else
    Set tempArray[index] to values[rightFirst]
    Increment rightFirst
    Increment index
    Copy any remaining items from left half to tempArray
    Copy any remaining items from right half to tempArray
    Copy the sorted elements from tempArray back into values
     * 
     * @param firstLeft
     * @param lastLeft
     * @param firstRight
     * @param lastRight
     */
    private static void merge(int firstLeft, int lastLeft, int firstRight, int lastRight) {
        int[] temp = new int[source.length];
        int index = firstLeft;
        int saveF = firstLeft;
        while (firstLeft <= lastLeft && firstRight <= lastRight) {
            if (source[firstLeft] < source[firstRight]) {
                temp[index] = source[firstLeft];
                firstLeft++;
            } else {
                temp[index] = source[firstRight];
                firstRight++;
            }
            index++;
        }
        while (firstLeft <= lastLeft) {
            temp[index] = source[firstLeft];
            firstLeft++;
            index++;
        }
        while (firstRight <= lastRight) {
            temp[index] = source[firstRight];
            firstRight++;
            index++;
        }
        for (index = saveF; index <= lastRight; index++) {
            source[index] = temp[index];
        }
    }
}
