package huf.data.sort;

import huf.data.Container;
import huf.data.IContainer;
import huf.data.compare.IComparator;

/**
 * This is QuickSort sorter.
 *
 * <p>
 * This implementation is most effective on instances of {@link huf.data.Container}.
 * Currently other container types like {@link huf.data.LinkedList} are converted to
 * {@link huf.data.Container} first.
 * </p>
 */
public class QuickSorter implements ISorter {

    /**
	 * Create new sorter.
	 */
    public QuickSorter() {
    }

    /**
	 * Sort container in ascending order.
	 *
	 * @param container container to sort
 	 * @return sorted container (the same object as parameter)
	 */
    @Override
    public <T> IContainer<T> sort(IContainer<T> container) {
        return sort(container, container.getComparator(), ASCENDING);
    }

    /**
	 * Sort container in ascending order.
	 *
	 * @param container container to sort
 	 * @return sorted container (the same object as parameter)
	 */
    @Override
    public <T> IContainer<T> sort(IContainer<T> container, IComparator<T> comparator) {
        return sort(container, comparator, ASCENDING);
    }

    /**
	 * Sort container in specified order.
	 *
	 * @param container container to sort
	 * @param order sorting order
	 * @return sorted container (the same object as parameter)
	 */
    @Override
    public <T> IContainer<T> sort(IContainer<T> container, boolean order) {
        return sort(container, container.getComparator(), order);
    }

    /**
	 * Sort container in specified order.
	 *
	 * @param container container to sort
	 * @param order sorting order
	 * @return sorted container (the same object as parameter)
	 */
    @Override
    public <T> IContainer<T> sort(IContainer<T> container, IComparator<T> comparator, boolean order) {
        if (container == null) {
            throw new IllegalArgumentException("Data container may not be null");
        }
        int wanted = order == ISorter.ASCENDING ? IComparator.GREATER : IComparator.LESSER;
        Container<T> data = null;
        if (container instanceof Container<?>) {
            data = (Container<T>) container;
            qsort(data, comparator, 0, data.getSize() - 1, wanted);
        } else {
            data = new Container<T>(container.iterator());
            qsort(data, comparator, 0, data.getSize() - 1, wanted);
            container.set(data.iterator());
        }
        return container;
    }

    /**
	 * Perform a quicksort.
	 *
	 * @param <T> type of objects being sorted
	 * @param data sorted container
	 * @param comparator comparator used for sorting
	 * @param low lower bound
	 * @param high upper bound
	 * @param wanted wanted compare result -- container sort ordering
	 *        ({@link IComparator#GREATER} or {@link IComparator#LESSER})
	 */
    private <T> void qsort(Container<T> data, IComparator<T> comparator, int low, int high, int wanted) {
        if (low >= high) {
            return;
        }
        T ot = null;
        T or = null;
        T op = null;
        if (low == high - 1) {
            ot = data.getAt(low);
            if (comparator.compare(ot, data.getAt(high)) == wanted) {
                or = data.getAt(high);
                data.setAt(high, null);
                data.setAt(low, or);
                data.setAt(high, ot);
            }
            return;
        }
        int l = low;
        int h = high;
        int p = (l + h) / 2;
        op = data.getAt(p);
        ot = data.getAt(h);
        data.setAt(h, null);
        data.setAt(p, ot);
        data.setAt(h, op);
        while (l < h) {
            while (comparator.compare(op, data.getAt(l)) == wanted || comparator.compare(op, data.getAt(l)) == IComparator.EQUAL && l < h) {
                l++;
            }
            while (comparator.compare(data.getAt(h), op) == wanted || comparator.compare(op, data.getAt(l)) == IComparator.EQUAL && l < h) {
                h--;
            }
            if (l < h) {
                ot = data.getAt(l);
                or = data.getAt(h);
                data.setAt(h, null);
                data.setAt(l, or);
                data.setAt(h, ot);
            }
        }
        qsort(data, comparator, low, l - 1, wanted);
        qsort(data, comparator, h + 1, high, wanted);
    }

    /**
	 * Sort array in ascending order.
	 *
	 * @param array array to sort
	 * @return sorted array (the same object as parameter)
	 */
    @Override
    public <T> T[] sort(T[] array, IComparator<T> comparator) {
        return sort(array, comparator, ASCENDING);
    }

    /**
	 * Sort array in specified order.
	 *
	 * @param array container to sort
	 * @param order sorting order
	 * @return sorted array (the same object as parameter)
	 */
    @Override
    public <T> T[] sort(T[] array, IComparator<T> comparator, boolean order) {
        if (array == null) {
            throw new IllegalArgumentException("Array may not be null");
        }
        int wanted = order == ISorter.ASCENDING ? IComparator.GREATER : IComparator.LESSER;
        qsort(array, comparator, 0, array.length - 1, wanted);
        return array;
    }

    /**
	 * Perform a quicksort.
	 *
	 * @param <T> type of objects being sorted
	 * @param array sorted array
	 * @param comparator comparator used for sorting
	 * @param low lower bound
	 * @param high upper bound
	 * @param wanted wanted compare result -- container sort ordering
	 *        ({@link IComparator#GREATER} or {@link IComparator#LESSER})
	 */
    private <T> void qsort(T[] array, IComparator<T> comparator, int low, int high, int wanted) {
        if (low >= high) {
            return;
        }
        T ot = null;
        T or = null;
        T op = null;
        if (low == high - 1) {
            ot = array[low];
            if (comparator.compare(ot, array[high]) == wanted) {
                or = array[high];
                array[low] = or;
                array[high] = ot;
            }
            return;
        }
        int l = low;
        int h = high;
        int p = (l + h) >>> 1;
        op = array[p];
        ot = array[h];
        array[p] = ot;
        array[h] = op;
        while (l < h) {
            while (comparator.compare(op, array[l]) == wanted || comparator.compare(op, array[l]) == IComparator.EQUAL && l < h) {
                l++;
            }
            while (comparator.compare(array[h], op) == wanted || comparator.compare(op, array[l]) == IComparator.EQUAL && l < h) {
                h--;
            }
            if (l < h) {
                ot = array[l];
                or = array[h];
                array[l] = or;
                array[h] = ot;
            }
        }
        qsort(array, comparator, low, l - 1, wanted);
        qsort(array, comparator, h + 1, high, wanted);
    }
}
