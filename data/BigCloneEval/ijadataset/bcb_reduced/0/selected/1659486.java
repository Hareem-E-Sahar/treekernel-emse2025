package org.happy.collections.lists.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.happy.collections.Collections_1x2;
import org.happy.collections.lists.decorators.iterators.ListIteratorDecorator_1x0;
import org.happy.commons.util.ObjectPointer_1x0;
import org.happy.commons.util.comparators.HashComparator_1x0;
import org.happy.commons.util.comparators.InvertComparator_1x0;

/**
 * SortedList_1x0 is a decorator which decorates other lists like ArrayList or
 * LinkedList, it inserts elements in the sorted order this decorator is only
 * good for list which don'T need to iterate to get element for index. A good
 * choice for decorated list is ArrayList and then compare element which must be
 * inserted. If the element is bigger the the right half will be again divided
 * if not then the left half this process must be repeated until the index to
 * insert the new element is found. use insert() method to add element on the
 * found index
 * 
 * @author Wjatscheslaw Stoljarski, Eugen Lofing, Andreas Hollmann
 * 
 * @param <E>
 */
public class SortedList_1x0<E> extends ListDecorator_1x0<E, List<E>> {

    /**
	 * decorates the list to be sorted and returns the decorator back
	 * 
	 * @param <E>
	 *            type of the element in then list
	 * @param list
	 *            the list which should be decorated
	 * @param comparator
	 *            the comparator which should be used to compare elements before
	 *            sorting them
	 * @return
	 */
    public static <E> SortedList_1x0<E> of(List<E> list, Comparator<E> comparator) {
        return new SortedList_1x0<E>(list, comparator);
    }

    /**
	 * decorates the list to be sorted and returns the decorator back
	 * 
	 * @param <E>
	 *            type of the element in then list
	 * @param list
	 *            the list which should be decorated
	 * @param comparator
	 *            the comparator which should be used to compare elements before
	 *            sorting them
	 * @param doSort
	 *            if true the the list, which is the parameter in the
	 *            constructor will be sorted, before it will be decorated
	 * @return
	 */
    public static <E> SortedList_1x0<E> of(List<E> list, Comparator<E> comparator, boolean doSort) {
        return new SortedList_1x0<E>(list, comparator, doSort);
    }

    /**
	 * decorates the list to be sorted and returns the decorator back
	 * 
	 * @param <E>
	 *            type of the element in then list
	 * @param list
	 *            the list which should be decorated
	 * @param comparator
	 *            the comparator which should be used to compare elements before
	 *            sorting them
	 * @param type
	 *            type for the sorting strategy, there are two types for the
	 *            LinkedList and for ArrayList with binary search-methods
	 */
    public static <E> SortedList_1x0<E> of(List<E> list, Comparator<E> comparator, SortType type) {
        return new SortedList_1x0<E>(list, comparator, type, false, true);
    }

    /**
	 * decorates the list to be sorted and returns the decorator back
	 * 
	 * @param <E>
	 *            type of the element in then list
	 * @param list
	 *            the list which should be decorated
	 * @param comparator
	 *            the comparator which should be used to compare elements before
	 *            sorting them
	 * @param type
	 *            type for the sorting strategy, there are two types for the
	 *            LinkedList and for ArrayList with binary search-methods
	 * @param inverted
	 *            if true the sorted order will be inverted, the list will be
	 *            sorted in descending order
	 * @param doSort
	 *            if true the the list, which is the parameter in the
	 *            constructor will be sorted, before it will be decorated
	 */
    public static <E> SortedList_1x0<E> of(List<E> list, Comparator<E> comparator, SortType type, boolean inverted, boolean doSort) {
        return new SortedList_1x0<E>(list, comparator, type, inverted, doSort);
    }

    protected final int minListSizeForArrayType = 128;

    public enum SortType {

        /**
		 * this type of sorting is very good for lists like ArrayList, where you
		 * have small overhead to get element for a index
		 */
        Array, /**
		 * this type of sorting is a better choice for list like LinkedList,
		 * where you have large overhead to get an object for index, but relative
		 * small overhead to get next element
		 */
        Linked
    }

    private boolean inverted = false;

    private Comparator<E> comparator = null;

    /**
	 * the type of the sorting algorithm
	 */
    private SortType type = null;

    /**
	 * this constructor use the Hash-Comparator to sort elements. It means you sort elements on their hash-code
	 * @param list decorated List
	 */
    public SortedList_1x0(List<E> list) {
        this(list, new HashComparator_1x0<E>());
    }

    /**
	 * constructor
	 * 
	 * @param list
	 *            decorated List
	 * @param comparator
	 *            comparator
	 */
    public SortedList_1x0(List<E> list, Comparator<E> comparator) {
        this(list, comparator, SortType.Array, false, true);
    }

    /**
	 * constructor
	 * 
	 * @param list
	 *            List which should be decorated
	 * @param comparator
	 *            comparator
	 * @param doSort
	 *            if true the the list, which is the parameter in the
	 *            constructor will be sorted, before it will be decorated
	 */
    public SortedList_1x0(List<E> list, Comparator<E> comparator, boolean doSort) {
        this(list, comparator, SortType.Array, false, doSort);
    }

    /**
	 * constructor
	 * 
	 * @param list
	 *            decorated List
	 * @param comparator
	 *            comparator
	 * @param type
	 *            type for the sorting strategy, there are two types for the
	 *            LinkedList and for ArrayList with binary search-methods
	 * @param inverted
	 *            if true the sorted order will be inverted, the list will be
	 *            sorted in descending order
	 * @param doSort
	 *            if true the the list, which is the parameter in the
	 *            constructor will be sorted, before it will be decorated
	 */
    public SortedList_1x0(List<E> list, Comparator<E> comparator, SortType type, boolean inverted, boolean doSort) {
        super(list);
        this.comparator = comparator;
        if (inverted) this.comparator = new InvertComparator_1x0<E>(comparator);
        this.inverted = inverted;
        this.type = type;
        if (doSort) {
            Collections_1x2.sort(list, this.comparator);
        }
    }

    @Override
    public boolean add(E element) {
        if ((this.size() < minListSizeForArrayType) || SortType.Linked.equals(this.type)) {
            ListIterator<E> it = this.decorated.listIterator();
            while (it.hasNext()) {
                E current = it.next();
                if (0 <= this.comparator.compare(current, element)) {
                    it.previous();
                    it.add(element);
                    return true;
                }
            }
        } else {
            int index = this.findInsertionIndex_TypeArray(this.decorated, element, 0, this.decorated.size() - 1, new ObjectPointer_1x0<Integer>(0));
            int sz = this.size();
            E last = this.decorated.get(sz - 1);
            if (index == sz || this.comparator.compare(last, element) < 0) {
                this.decorated.add(element);
            } else {
                this.decorated.add(index, element);
            }
            return true;
        }
        return this.decorated.add(element);
    }

    /**
	 * this method throws IllegalStateException if the sorted order can be
	 * destroyed through the new inserted value
	 */
    @Override
    public void add(int index, E element) {
        if (0 < index) {
            int result = this.comparator.compare(this.decorated.get(index - 1), element);
            if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + element.toString() + "is bigger then " + this.decorated.get(index - 1).toString());
        }
        if (index < this.decorated.size() - 2) {
            int result = this.comparator.compare(element, this.decorated.get(index + 1));
            if (0 <= result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + element.toString() + "is smaller then " + this.decorated.get(index + 1).toString());
        }
        this.decorated.add(index, element);
    }

    /**
	 * adds all elements to the decorated list
	 */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E element : c) changed = this.add(element) || changed;
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c.isEmpty()) return true;
        List<E> list = new ArrayList<E>(c);
        Collections.sort(list, this.comparator);
        for (E elem : c) list.add(elem);
        E firstElem = list.get(0);
        E lastElem = list.get(list.size() - 1);
        if (0 < index) {
            int result = this.comparator.compare(this.decorated.get(index - 1), firstElem);
            if (result < 0) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + firstElem.toString() + "is bigger then " + this.decorated.get(index - 1).toString());
        }
        if (index < this.decorated.size() - 2) {
            int result = this.comparator.compare(lastElem, this.decorated.get(index + 1));
            if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + lastElem.toString() + "is smaller then " + this.decorated.get(index + 1).toString());
        }
        return this.decorated.addAll(index, list);
    }

    /**
	 * sets the element to the new value, but this method proof the new input
	 * and if it destroys the sorted order of the list, this method throws the
	 * IllegalStateException
	 */
    @Override
    public E set(int index, E element) {
        if (0 < index) {
            int result = this.comparator.compare(this.decorated.get(index - 1), element);
            if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + element.toString() + "is bigger then " + this.decorated.get(index - 1).toString());
        }
        if (index < this.decorated.size() - 2) {
            int result = this.comparator.compare(element, this.decorated.get(index + 1));
            if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + element.toString() + "is smaller then " + this.decorated.get(index + 1).toString());
        }
        return this.decorated.set(index, element);
    }

    public boolean isInverted() {
        return inverted;
    }

    @Override
    protected Iterator<E> iteratorImpl() {
        return this.decorated.iterator();
    }

    @Override
    protected ListIterator<E> listIteratorImpl(int index) {
        return new ListIteratorDecorator_1x0<E>(this.decorated.listIterator(index)) {

            private E currentElem = null;

            private boolean iterationDirectionByUsingNext = true;

            @Override
            public E next() {
                iterationDirectionByUsingNext = true;
                this.currentElem = super.next();
                return this.currentElem;
            }

            @Override
            public E previous() {
                iterationDirectionByUsingNext = false;
                this.currentElem = super.previous();
                return this.currentElem;
            }

            @Override
            public void add(E e) {
                boolean iterationDirectionByUsingNextCopy = iterationDirectionByUsingNext;
                E nextElem = null;
                if (!iterationDirectionByUsingNextCopy) {
                    this.next();
                }
                if (this.hasNext()) {
                    nextElem = this.next();
                    this.previous();
                    this.previous();
                    this.next();
                }
                if (!iterationDirectionByUsingNextCopy) {
                    this.previous();
                }
                if (currentElem != null) {
                    int result = SortedList_1x0.this.comparator.compare(currentElem, e);
                    if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + currentElem.toString() + "is bigger then " + currentElem.toString());
                }
                if (nextElem != null) {
                    int result = SortedList_1x0.this.comparator.compare(e, nextElem);
                    if (0 <= result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + nextElem.toString() + "is smaller then " + nextElem.toString());
                }
                super.add(e);
            }

            @Override
            public void set(E e) {
                E prevElem = null;
                if (this.hasPrevious()) {
                    prevElem = this.previous();
                    this.next();
                }
                if (prevElem != null) {
                    int result = SortedList_1x0.this.comparator.compare(prevElem, e);
                    if (0 < result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + prevElem.toString() + "is bigger then " + prevElem.toString());
                }
                E nextElem = null;
                if (this.hasNext()) {
                    nextElem = this.previous();
                    this.previous();
                }
                if (nextElem != null) {
                    int result = SortedList_1x0.this.comparator.compare(e, nextElem);
                    if (0 <= result) throw new IllegalStateException("SortedList_1x0.set(int index, E element) caused exception, because " + nextElem.toString() + "is smaller then " + nextElem.toString());
                }
                super.set(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public int indexOf(Object element) {
        if (this.decorated.size() < minListSizeForArrayType || SortType.Linked.equals(this.type)) {
            return super.indexOf(element);
        }
        E elemE = null;
        try {
            elemE = (E) element;
        } catch (ClassCastException ex) {
            return -1;
        }
        if (elemE == null) return -1;
        ObjectPointer_1x0<Integer> rightBorder = new ObjectPointer_1x0<Integer>(0);
        int left = this.findInsertionIndex_TypeArray(this.decorated, elemE, 0, this.decorated.size(), rightBorder);
        int index = left;
        int iteratorIndex = left;
        ListIterator<E> it = this.decorated.listIterator(iteratorIndex);
        while (it.hasNext()) {
            E e = it.next();
            if (element.equals(e)) {
                return index;
            }
            index++;
            if (rightBorder.getObject() < index) break;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int lastIndexOf(Object element) {
        if (this.decorated.size() < minListSizeForArrayType || SortType.Linked.equals(this.type)) {
            return super.lastIndexOf(element);
        }
        E elemE = null;
        try {
            elemE = (E) element;
        } catch (ClassCastException ex) {
            return -1;
        }
        if (elemE == null) return -1;
        ObjectPointer_1x0<Integer> rightBorder = new ObjectPointer_1x0<Integer>(0);
        int left = this.findInsertionIndex_TypeArray(this.decorated, elemE, 0, this.decorated.size(), rightBorder);
        int index = left;
        ListIterator<E> it = this.decorated.listIterator(left);
        boolean found = false;
        while (it.hasNext()) {
            E e = it.next();
            if (rightBorder.getObject() < index) break;
            if (elemE.equals(e)) {
                found = true;
            } else {
                if (found) {
                    return index - 1;
                }
            }
            index++;
        }
        if (found) {
            return index - 1;
        }
        return -1;
    }

    /**
	 * find the position where the object should be inserted in to the list, or
	 * the area of the list which should be searched for the object
	 * 
	 * @param list
	 * @param element
	 * @param left
	 * @param right
	 * @param rightBorder
	 * @return first index of the element where the element should be inserted
	 */
    protected int findInsertionIndex_TypeArray(List<E> list, E element, int left, int right, ObjectPointer_1x0<Integer> rightBorder) {
        if (rightBorder == null) throw new IllegalArgumentException("rightBorder can't be null");
        if (right < left) throw new IllegalArgumentException("right must be bigger or equals as the left");
        if ((right - left) <= minListSizeForArrayType) {
            rightBorder.setObject(right);
            return findInsertionIndex_TypeLinked(list, element, left, right);
        }
        int midle = left + (right - left) / 2;
        E midleE = this.decorated.get(midle);
        int comparedValue = this.comparator.compare(midleE, element);
        if (0 < comparedValue) {
            return this.findInsertionIndex_TypeArray(list, element, left, midle, rightBorder);
        } else if (comparedValue == 0) {
            ListIterator<E> it = this.listIterator(midle);
            int index = midle;
            while (it.hasPrevious()) {
                E e = it.previous();
                if (0 != this.comparator.compare(e, element)) {
                    break;
                }
                index--;
            }
            rightBorder.setObject(index);
            return index;
        }
        return this.findInsertionIndex_TypeArray(list, element, midle, right, rightBorder);
    }

    /**
	 * searches for the element with the linked algorithm
	 * 
	 * @param list
	 * @param element
	 * @param left
	 * @param right
	 * @return
	 */
    protected int findInsertionIndex_TypeLinked(List<E> list, E element, int left, int right) {
        int index = left;
        ListIterator<E> it = list.listIterator(left);
        while (it.hasNext()) {
            E e = it.next();
            if (0 <= comparator.compare(e, element)) {
                return index;
            }
            index++;
        }
        return right;
    }

    @Override
    public boolean contains(Object obj) {
        return 0 <= this.indexOf(obj);
    }

    @Override
    public boolean remove(Object obj) {
        int index = indexOf(obj);
        if (0 <= index) {
            decorated.remove(index);
            return true;
        }
        return false;
    }
}
