package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * An empty immutable sorted set with one or more elements.
 * TODO: Consider creating a separate class for a single-element sorted set.
 * 
 * @author Jared Levy
 */
@GwtCompatible(serializable = true)
@SuppressWarnings("serial")
final class RegularImmutableSortedSet<E> extends ImmutableSortedSet<E> {

    private final Object[] elements;

    /**
   * The index of the first element that's in the sorted set (inclusive
   * index).
   */
    private final int fromIndex;

    /**
   * The index after the last element that's in the sorted set (exclusive
   * index).
   */
    private final int toIndex;

    RegularImmutableSortedSet(Object[] elements, Comparator<? super E> comparator) {
        super(comparator);
        this.elements = elements;
        this.fromIndex = 0;
        this.toIndex = elements.length;
    }

    RegularImmutableSortedSet(Object[] elements, Comparator<? super E> comparator, int fromIndex, int toIndex) {
        super(comparator);
        this.elements = elements;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnmodifiableIterator<E> iterator() {
        return (UnmodifiableIterator<E>) Iterators.forArray(elements, fromIndex, size());
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int size() {
        return toIndex - fromIndex;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        try {
            return binarySearch(o) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> targets) {
        if (!hasSameComparator(targets, comparator()) || (targets.size() <= 1)) {
            return super.containsAll(targets);
        }
        int i = fromIndex;
        Iterator<?> iterator = targets.iterator();
        Object target = iterator.next();
        while (true) {
            if (i >= toIndex) {
                return false;
            }
            int cmp = unsafeCompare(elements[i], target);
            if (cmp < 0) {
                i++;
            } else if (cmp == 0) {
                if (!iterator.hasNext()) {
                    return true;
                }
                target = iterator.next();
                i++;
            } else if (cmp > 0) {
                return false;
            }
        }
    }

    private int binarySearch(Object key) {
        int lower = fromIndex;
        int upper = toIndex - 1;
        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;
            int c = unsafeCompare(key, elements[middle]);
            if (c < 0) {
                upper = middle - 1;
            } else if (c > 0) {
                lower = middle + 1;
            } else {
                return middle;
            }
        }
        return -lower - 1;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        System.arraycopy(elements, fromIndex, array, 0, size());
        return array;
    }

    @Override
    public <T> T[] toArray(T[] array) {
        int size = size();
        if (array.length < size) {
            array = ObjectArrays.newArray(array, size);
        } else if (array.length > size) {
            array[size] = null;
        }
        System.arraycopy(elements, fromIndex, array, 0, size);
        return array;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Set)) {
            return false;
        }
        Set<?> that = (Set<?>) object;
        if (size() != that.size()) {
            return false;
        }
        if (hasSameComparator(that, comparator)) {
            Iterator<?> iterator = that.iterator();
            try {
                for (int i = fromIndex; i < toIndex; i++) {
                    Object otherElement = iterator.next();
                    if (otherElement == null || unsafeCompare(elements[i], otherElement) != 0) {
                        return false;
                    }
                }
                return true;
            } catch (ClassCastException e) {
                return false;
            } catch (NoSuchElementException e) {
                return false;
            }
        }
        return this.containsAll(that);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            hash += elements[i].hashCode();
        }
        return hash;
    }

    @SuppressWarnings("unchecked")
    public E first() {
        return (E) elements[fromIndex];
    }

    @SuppressWarnings("unchecked")
    public E last() {
        return (E) elements[toIndex - 1];
    }

    @Override
    ImmutableSortedSet<E> headSetImpl(E toElement) {
        return createSubset(fromIndex, findSubsetIndex(toElement));
    }

    @Override
    ImmutableSortedSet<E> subSetImpl(E fromElement, E toElement) {
        return createSubset(findSubsetIndex(fromElement), findSubsetIndex(toElement));
    }

    @Override
    ImmutableSortedSet<E> tailSetImpl(E fromElement) {
        return createSubset(findSubsetIndex(fromElement), toIndex);
    }

    private int findSubsetIndex(E element) {
        int index = binarySearch(element);
        return (index >= 0) ? index : (-index - 1);
    }

    private ImmutableSortedSet<E> createSubset(int newFromIndex, int newToIndex) {
        if (newFromIndex < newToIndex) {
            return new RegularImmutableSortedSet<E>(elements, comparator, newFromIndex, newToIndex);
        } else {
            return emptySet(comparator);
        }
    }

    @Override
    boolean hasPartialArray() {
        return (fromIndex != 0) || (toIndex != elements.length);
    }
}
