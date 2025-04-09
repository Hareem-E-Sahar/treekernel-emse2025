package takatuka.classreader.logic.util;

import java.util.*;

/**
 *
 * @author aslam
 */
public class BinarySearch {

    private static final int NOT_FOUND = -1;

    private static int lowGlobal = 0;

    public static int binarySearch(Vector list, Object toFind, Comparator comp) {
        return binarySearch(list.toArray(), toFind, comp);
    }

    public static int binarySearch(Comparable[] list, Comparable toFind) {
        int low = 0;
        int high = list.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (list[mid].compareTo(toFind) < 0) {
                low = mid + 1;
            } else if (list[mid].compareTo(toFind) > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        lowGlobal = low;
        return NOT_FOUND;
    }

    /**
     * 
     * @param list list from which object is to be found
     * @param toFind object to be found
     * @param comp how to compare objects
     * @return
     */
    public static int binarySearch(Object[] list, Object toFind, Comparator comp) {
        int low = 0;
        int high = list.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (comp.compare(list[mid], toFind) < 0) {
                low = mid + 1;
            } else if (comp.compare(list[mid], toFind) > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        lowGlobal = low;
        return NOT_FOUND;
    }

    private static int getLastLow() {
        return lowGlobal;
    }

    public static void main(String[] args) {
        Vector objs = new Vector();
        int range = 10;
        Random r = new Random(System.currentTimeMillis());
        for (int loop = 0; loop < 10; loop++) {
            objs.addElement(r.nextInt(range));
        }
        QuickSort.sort(objs, new IntegerComparator());
        Miscellaneous.println("Sorted Array " + objs);
        Integer search = r.nextInt(range);
        Miscellaneous.println("To find in the array = " + search);
        int ret = BinarySearch.binarySearch(objs.toArray(), search, new IntegerComparator());
        Miscellaneous.println(ret + ", " + lowGlobal);
        if (ret == -1) {
            objs.add(lowGlobal, search);
        }
        Miscellaneous.println("If not found then inserted in the array " + objs);
    }

    private static class IntegerComparator implements Comparator<Integer> {

        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    }
}
