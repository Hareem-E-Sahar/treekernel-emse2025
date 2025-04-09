package swproxy.plugins.urlfilter.searchset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Toni Dietze
 *
 */
public class ArraySearchSet<E extends Matchable<E>> implements SearchSet<E> {

    protected ArrayList<E> list = null;

    protected Boolean isSorted = false;

    public ArraySearchSet() {
        list = new ArrayList<E>();
    }

    public ArraySearchSet(int initialCapacity) {
        list = new ArrayList<E>(initialCapacity);
    }

    public boolean add(E e) {
        if (list.add(e)) {
            isSorted = false;
            return true;
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (list.addAll(c)) {
            isSorted = false;
            return true;
        }
        return false;
    }

    public void clear() {
        list.clear();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public int size() {
        return list.size();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public String toString() {
        return list.toString();
    }

    public void sortNow() {
        if (isSorted) return;
        Collections.sort(list);
        isSorted = true;
    }

    public E getBestMatching(E e) {
        int min = -1;
        int max = list.size();
        E currentSearch = e;
        do {
            int index = getSimilarestIndex(currentSearch, true, min, max);
            if (index < 0) return null;
            if (e.matches(list.get(index))) {
                return list.get(index);
            } else {
                currentSearch = e.matchingPart(list.get(index));
                max = index;
            }
        } while (currentSearch.isMatchable());
        return null;
    }

    public E getSmallerSimilarest(E e) {
        return getSimilarest(e, true);
    }

    public E getLargerSimilarest(E e) {
        return getSimilarest(e, false);
    }

    private E getSimilarest(E e, Boolean smallerSimilarest) {
        int index = getSimilarestIndex(e, smallerSimilarest);
        if (index < 0) return null; else return list.get(index);
    }

    private int getSimilarestIndex(E e, Boolean smallerSimilarest) {
        return getSimilarestIndex(e, smallerSimilarest, -1, list.size());
    }

    private int getSimilarestIndex(E e, Boolean smallerSimilarest, int min, int max) {
        if (list.isEmpty()) return -1;
        sortNow();
        int current;
        while (min + 1 != max) {
            current = (min + max) / 2;
            int cmp = list.get(current).compareTo(e);
            if (cmp < 0) min = current; else if (cmp > 0) max = current; else return current;
        }
        if (smallerSimilarest) {
            return min;
        } else {
            if (max >= list.size()) return -1;
            return max;
        }
    }
}
