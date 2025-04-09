package gnu.ojama.util;

import java.util.*;

/**
 * Implements a sorting wrapper for List implementations. Because a binary
 * search algorithm is used, the performance is optimum with random access
 * list implementations. With sequential access lists performance is
 * remarkably slower.
 *
 * <strong>TODO: IMPLEMENT STATIC METHODS FOR MANIPULATING EXISTING LIST
 * WITHOUT WRAPPING IT INSIDE THIS CLASS.</strong>
 *
 * @author Markku Vuorenmaa
 */
public class SortingListWrapper extends AbstractList {

    public static final int INVALID_INDEX = -1;

    private List myList;

    /**
     * Constructor where specific List parameter. The specified list should
     * be accessed only through this class to make sure that elements remain
     * sorted.
     * @param list List object accessed by this wrapper
     */
    public SortingListWrapper(List list) {
        super();
        myList = list;
    }

    /**
     * Checks whether the specified element is found in the list.
     * @param searched element
     * @return always true (specified by Java2 API)
     */
    public boolean contains(Object elem) {
        return (search(elem) >= 0);
    }

    /**
     * Overridden to to keep list elements in sorted order.
     * @param o object to be added
     * @return always true (specified by Java2 API)
     */
    public boolean add(Object o) {
        addToSortedList(o);
        return true;
    }

    /**
     * Overridden to to keep list elements in sorted order.
     * @param index has no effect in this implementation
     * @param element added element
     */
    public void add(int index, Object element) {
        addToSortedList(element);
    }

    /**
     * Get the element at specified index
     * @param index element index
     * @return element from the index
     */
    public Object get(int index) {
        return myList.get(index);
    }

    /**
     * Removes an element from the list.
     * @param index index of removed element
     */
    public Object remove(int index) {
        return myList.remove(index);
    }

    /**
     * Overridden to to keep list elements in sorted order.
     * @param index element is removed from this index, location of new element
     * is not known
     * @param element added element
     */
    public Object set(int index, Object element) {
        Object oldElement = remove(index);
        addToSortedList(element);
        return oldElement;
    }

    /**
     * Return the number of elements in the list.
     * @return number of elements
     */
    public int size() {
        return myList.size();
    }

    /**
     * Search specified element from the list.
     * @param element searched element
     * @return index of the found element or INVALID_INDEX
     */
    private int search(Object element) {
        int min = 0;
        int max = size() - 1;
        boolean found = false;
        int currentIndex = 0;
        int compareResult;
        if (max >= 0) {
            do {
                currentIndex = (min + max) / 2;
                compareResult = ((Comparable) myList.get(currentIndex)).compareTo(element);
                if (compareResult < 0) {
                    min = currentIndex + 1;
                } else if (compareResult > 0) {
                    max = currentIndex - 1;
                } else {
                    found = true;
                }
            } while ((min <= max) && (found == false));
            if (found == true) {
                return currentIndex;
            } else {
                return INVALID_INDEX;
            }
        }
        return INVALID_INDEX;
    }

    /**
     * Add new element to correct location according to the sort order.
     * @param element to be added
     * @return true if addition was done, false if equal element is already
     * found in the list
     */
    private boolean addToSortedList(Object element) {
        int min = 0;
        int max = size() - 1;
        boolean found = false;
        int currentIndex = 0;
        int compareResult;
        if (max >= 0) {
            do {
                currentIndex = (min + max) / 2;
                compareResult = ((Comparable) myList.get(currentIndex)).compareTo(element);
                if (compareResult < 0) {
                    min = currentIndex + 1;
                } else if (compareResult > 0) {
                    max = currentIndex - 1;
                } else {
                    found = true;
                }
            } while ((min <= max) && (found == false));
        }
        if (found == false && size() > 0) {
            if (((Comparable) element).compareTo(get(currentIndex)) > 0) {
                myList.add(currentIndex + 1, element);
            } else {
                myList.add(currentIndex, element);
            }
            return true;
        } else if (found == false) {
            myList.add(currentIndex, element);
            return true;
        } else {
            System.out.println("Element found in vector already.");
            return false;
        }
    }
}
