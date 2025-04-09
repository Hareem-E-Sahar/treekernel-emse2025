package search.methods;

import java.util.Comparator;
import list.AbstractList;
import list.UnsortedList;

/**
 * Class responsible for handling binary search through a sorted array
 * 
 * @author Mostafa Mahmoud Mahmoud Eweda
 * @version 1.0
 * @since JDK 1.6
 * 
 * @see AbstractSearch
 * @see AbstractList
 *
 * @param <E> the element to search for
 * @param <K> the applicable comparator of the list
 */
public class BinarySearch<E, K extends Comparator<E>> extends AbstractSearch<E, K> {

    public BinarySearch(AbstractList<E> list, K comp) {
        super(list, comp);
    }

    @Override
    public E find(E e) {
        comparisons = 0;
        long start = System.nanoTime();
        int found = findAt(e);
        runningTime = System.nanoTime() - start;
        if (found == -1) return null; else return list.get(found);
    }

    private int findAt(E e) {
        int lower = 0, upper = list.size() - 1;
        int mid, comparison;
        while (lower <= upper) {
            mid = (lower + upper) / 2;
            comparison = comp.compare(e, list.get(mid));
            comparisons++;
            if (comparison < 0) {
                upper = mid - 1;
            } else if (comparison > 0) {
                lower = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    @Override
    public Object[] findAll(E e) {
        long start = System.nanoTime();
        int found = findAt(e);
        if (found == -1) return new Object[0];
        AbstractList<E> temp = new UnsortedList<E>(10);
        temp.add(list.get(found));
        int move = found - 1;
        comparisons++;
        while (move >= 0 && comp.compare(e, list.get(move)) == 0) {
            comparisons++;
            temp.add(list.get(move--));
        }
        move = found + 1;
        int n = list.size();
        comparisons++;
        while (move < n && comp.compare(e, list.get(move)) == 0) {
            comparisons++;
            temp.add(list.get(move++));
        }
        runningTime = System.nanoTime() - start;
        Object[] result = new Object[temp.size()];
        for (int i = 0; i < result.length; i++) result[i] = temp.get(i);
        return result;
    }
}
