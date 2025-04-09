package client;

/**
 * An implementation of MergeSort, needs to be subclassed to provide a 
 * comparator. Is used in GUI to sort files in tree view.
 *
 * @author John �sterlund
 * @version 1.0 2006-05-10
 */
public abstract class MergeSort extends Object {

    protected Object toSort[];

    protected Object swapSpace[];

    /**
     * Sort an array of objects.
     * 
     * @param array Array of objects to sort
     */
    public void sort(Object array[]) {
        if (array != null && array.length > 1) {
            int maxLength = array.length;
            swapSpace = new Object[maxLength];
            toSort = array;
            this.mergeSort(0, maxLength - 1);
            swapSpace = null;
            toSort = null;
        }
    }

    /**
     * Abstract comparator, must be implemented. 
     * 
     * @param beginLoc Begin location
     * @param endLoc End location
     * @return 0 if same, -1 if beginLoc is less than endLoc, 1 if beginLoc is greater than endLoc
     */
    public abstract int compareElementsAt(int beginLoc, int endLoc);

    protected void mergeSort(int begin, int end) {
        if (begin != end) {
            int mid = (begin + end) / 2;
            this.mergeSort(begin, mid);
            this.mergeSort(mid + 1, end);
            this.merge(begin, mid, end);
        }
    }

    protected void merge(int begin, int middle, int end) {
        int firstHalf, secondHalf, count;
        firstHalf = count = begin;
        secondHalf = middle + 1;
        while ((firstHalf <= middle) && (secondHalf <= end)) {
            if (this.compareElementsAt(secondHalf, firstHalf) < 0) swapSpace[count++] = toSort[secondHalf++]; else swapSpace[count++] = toSort[firstHalf++];
        }
        if (firstHalf <= middle) {
            while (firstHalf <= middle) swapSpace[count++] = toSort[firstHalf++];
        } else {
            while (secondHalf <= end) swapSpace[count++] = toSort[secondHalf++];
        }
        for (count = begin; count <= end; count++) toSort[count] = swapSpace[count];
    }
}
