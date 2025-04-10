import java.util.Vector;

public class binarySearchIntegerVec {

    /**
	 * This function seraches through a list of integers sorted from small to large.
	 * It returns the first position where it was found. If not found it returns the first position
	 * where it could be placed(but negated).
	 */
    public static int binarySearch(Vector<Integer> list, int value) {
        int middle;
        int valueTest;
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            middle = (low + high) / 2;
            valueTest = list.get(middle);
            if (valueTest == value) {
                return middle;
            } else if (valueTest > value) {
                high = middle - 1;
            } else {
                low = middle + 1;
            }
        }
        if (low >= (list.size() - 1)) {
            valueTest = list.get(list.size() - 1);
            if (valueTest > value) {
                return -(list.size() - 1);
            } else {
                return -list.size();
            }
        } else if (high <= 0) {
            valueTest = list.get(0);
            if (valueTest > value) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -low;
        }
    }

    /**
	 * This inserts the new value in the list in sorted order.
	 */
    public static void insertSortedList(Vector<Integer> list, int value) {
        if (list.size() == 0) {
            list.add(value);
            return;
        }
        int pos = binarySearch(list, value);
        if (pos <= 0) {
            list.add(-1 * pos, new Integer(value));
        } else {
            list.add(pos + 1, new Integer(value));
        }
    }
}
