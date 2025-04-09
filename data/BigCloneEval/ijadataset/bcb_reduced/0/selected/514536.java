package org.openmim.icq.util.java2;

import java.io.Serializable;
import java.util.*;

/**
 * This class consists exclusively of static methods that operate on or return
 * collections.  It contains polymorphic algorithms that operate on
 * collections, "wrappers", which return a new collection backed by a
 * specified collection, and a few other odds and ends.<p>
 *
 * The documentation for the polymorphic algorithms contained in this class
 * generally includes a brief description of the <i>implementation</i>.  Such
 * descriptions should be regarded as <i>implementation notes</i>, rather than
 * parts of the <i>specification</i>.  Implementors should feel free to
 * substitute other algorithms, so long as the specification itself is adhered
 * to.  (For example, the algorithm used by <tt>sort</tt> does not have to be
 * a mergesort, but it does have to be <i>stable</i>.)
 *
 * @author  Josh Bloch
 * @version 1.45, 02/17/00
 * @see     Collection
 * @see     Set
 * @see     List
 * @see     Map
 * @since 1.2
 */
public class Collections {

    private static Random r = new Random();

    /**
   * @serial include
   */
    static class UnmodifiableCollection implements Collection, Serializable {

        private static final long serialVersionUID = 1820017752578914078L;

        Collection c;

        UnmodifiableCollection(Collection c) {
            if (c == null) throw new NullPointerException();
            this.c = c;
        }

        public int size() {
            return c.size();
        }

        public boolean isEmpty() {
            return c.isEmpty();
        }

        public boolean contains(Object o) {
            return c.contains(o);
        }

        public Object[] toArray() {
            return c.toArray();
        }

        public Object[] toArray(Object[] a) {
            return c.toArray(a);
        }

        public String toString() {
            return c.toString();
        }

        public Iterator iterator() {
            return new Iterator() {

                Iterator i = c.iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Object next() {
                    return i.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection coll) {
            return c.containsAll(coll);
        }

        public boolean addAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    /**
   * @serial include
   */
    static class UnmodifiableList extends UnmodifiableCollection implements List {

        static final long serialVersionUID = -283967356065247728L;

        private List list;

        UnmodifiableList(List list) {
            super(list);
            this.list = list;
        }

        public boolean equals(Object o) {
            return list.equals(o);
        }

        public int hashCode() {
            return list.hashCode();
        }

        public Object get(int index) {
            return list.get(index);
        }

        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException();
        }

        public ListIterator listIterator() {
            return listIterator(0);
        }

        public ListIterator listIterator(final int index) {
            return new ListIterator() {

                ListIterator i = list.listIterator(index);

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Object next() {
                    return i.next();
                }

                public boolean hasPrevious() {
                    return i.hasPrevious();
                }

                public Object previous() {
                    return i.previous();
                }

                public int nextIndex() {
                    return i.nextIndex();
                }

                public int previousIndex() {
                    return i.previousIndex();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void set(Object o) {
                    throw new UnsupportedOperationException();
                }

                public void add(Object o) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public List subList(int fromIndex, int toIndex) {
            return new UnmodifiableList(list.subList(fromIndex, toIndex));
        }
    }

    /**
   * @serial include
   */
    static class SynchronizedCollection implements Collection, Serializable {

        private static final long serialVersionUID = 3053995032091335093L;

        Collection c;

        Object mutex;

        SynchronizedCollection(Collection c) {
            if (c == null) throw new NullPointerException();
            this.c = c;
            mutex = this;
        }

        SynchronizedCollection(Collection c, Object mutex) {
            this.c = c;
            this.mutex = mutex;
        }

        public int size() {
            synchronized (mutex) {
                return c.size();
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                return c.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (mutex) {
                return c.contains(o);
            }
        }

        public Object[] toArray() {
            synchronized (mutex) {
                return c.toArray();
            }
        }

        public Object[] toArray(Object[] a) {
            synchronized (mutex) {
                return c.toArray(a);
            }
        }

        public Iterator iterator() {
            return c.iterator();
        }

        public boolean add(Object o) {
            synchronized (mutex) {
                return c.add(o);
            }
        }

        public boolean remove(Object o) {
            synchronized (mutex) {
                return c.remove(o);
            }
        }

        public boolean containsAll(Collection coll) {
            synchronized (mutex) {
                return c.containsAll(coll);
            }
        }

        public boolean addAll(Collection coll) {
            synchronized (mutex) {
                return c.addAll(coll);
            }
        }

        public boolean removeAll(Collection coll) {
            synchronized (mutex) {
                return c.removeAll(coll);
            }
        }

        public boolean retainAll(Collection coll) {
            synchronized (mutex) {
                return c.retainAll(coll);
            }
        }

        public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }

        public String toString() {
            synchronized (mutex) {
                return c.toString();
            }
        }
    }

    /**
   * @serial include
   */
    static class SynchronizedList extends SynchronizedCollection implements List {

        private List list;

        SynchronizedList(List list) {
            super(list);
            this.list = list;
        }

        SynchronizedList(List list, Object mutex) {
            super(list, mutex);
            this.list = list;
        }

        public boolean equals(Object o) {
            synchronized (mutex) {
                return list.equals(o);
            }
        }

        public int hashCode() {
            synchronized (mutex) {
                return list.hashCode();
            }
        }

        public Object get(int index) {
            synchronized (mutex) {
                return list.get(index);
            }
        }

        public Object set(int index, Object element) {
            synchronized (mutex) {
                return list.set(index, element);
            }
        }

        public void add(int index, Object element) {
            synchronized (mutex) {
                list.add(index, element);
            }
        }

        public Object remove(int index) {
            synchronized (mutex) {
                return list.remove(index);
            }
        }

        public int indexOf(Object o) {
            synchronized (mutex) {
                return list.indexOf(o);
            }
        }

        public int lastIndexOf(Object o) {
            synchronized (mutex) {
                return list.lastIndexOf(o);
            }
        }

        public boolean addAll(int index, Collection c) {
            synchronized (mutex) {
                return list.addAll(index, c);
            }
        }

        public ListIterator listIterator() {
            return list.listIterator();
        }

        public ListIterator listIterator(int index) {
            return list.listIterator(index);
        }

        public List subList(int fromIndex, int toIndex) {
            synchronized (mutex) {
                return new SynchronizedList(list.subList(fromIndex, toIndex), mutex);
            }
        }
    }

    /**
   * The empty list (immutable).  This list is serializable.
   */
    public static final List EMPTY_LIST = new EmptyList();

    /**
   * @serial include
   */
    private static class EmptyList extends AbstractList implements Serializable {

        private static final long serialVersionUID = 8842843931221139166L;

        public int size() {
            return 0;
        }

        public boolean contains(Object obj) {
            return false;
        }

        public Object get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    private static class SingletonList extends AbstractList implements Serializable {

        private final Object element;

        SingletonList(Object obj) {
            element = obj;
        }

        public int size() {
            return 1;
        }

        public boolean contains(Object obj) {
            return eq(obj, element);
        }

        public Object get(int index) {
            if (index != 0) throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
            return element;
        }
    }

    /**
   * @serial include
   */
    private static class CopiesList extends AbstractList implements Serializable {

        int n;

        Object element;

        CopiesList(int n, Object o) {
            if (n < 0) throw new IllegalArgumentException("List length = " + n);
            this.n = n;
            element = o;
        }

        public int size() {
            return n;
        }

        public boolean contains(Object obj) {
            return n != 0 && eq(obj, element);
        }

        public Object get(int index) {
            if (index < 0 || index >= n) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + n);
            return element;
        }
    }

    private static final Comparator REVERSE_ORDER = new ReverseComparator();

    /**
   * @serial include
   */
    private static class ReverseComparator implements Comparator, Serializable {

        private static final long serialVersionUID = 7207038068494060240L;

        public int compare(Object o1, Object o2) {
            Comparable c1 = (Comparable) o1;
            Comparable c2 = (Comparable) o2;
            return -c1.compareTo(c2);
        }
    }

    private Collections() {
    }

    /**
		 * Searches the specified list for the specified object using the binary
		 * search algorithm.  The list must be sorted into ascending order
		 * according to the <i>natural ordering</i> of its elements (as by the
		 * <tt>sort(List)</tt> method, above) prior to making this call.  If it is
		 * not sorted, the results are undefined.  If the list contains multiple
		 * elements equal to the specified object, there is no guarantee which one
		 * will be found.<p>
		 *
		 * This method runs in log(n) time for a "random access" list (which
		 * provides near-constant-time positional access).  It may
		 * run in n log(n) time if it is called on a "sequential access" list
		 * (which provides linear-time positional access).</p>
		 *
		 * If the specified list implements the <tt>AbstracSequentialList</tt>
		 * interface, this method will do a sequential search instead of a binary
		 * search; this offers linear performance instead of n log(n) performance
		 * if this method is called on a <tt>LinkedList</tt> object.
		 *
		 * @param  list the list to be searched.
		 * @param  key the key to be searched for.
		 * @return index of the search key, if it is contained in the list;
		 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
		 *         <i>insertion point</i> is defined as the point at which the
		 *         key would be inserted into the list: the index of the first
		 *         element greater than the key, or <tt>list.size()</tt>, if all
		 *         elements in the list are less than the specified key.  Note
		 *         that this guarantees that the return value will be &gt;= 0 if
		 *         and only if the key is found.
		 * @throws ClassCastException if the list contains elements that are not
		 *         <i>mutually comparable</i> (for example, strings and
		 *         integers), or the search key in not mutually comparable
		 *         with the elements of the list.
		 * @see    Comparable
		 * @see #sort(List)
		 */
    public static int binarySearch(List list, Object key) {
        if (list instanceof AbstractSequentialList) {
            ListIterator i = list.listIterator();
            while (i.hasNext()) {
                int cmp = ((Comparable) (i.next())).compareTo(key);
                if (cmp == 0) return i.previousIndex(); else if (cmp > 0) return -i.nextIndex();
            }
            return -i.nextIndex() - 1;
        }
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Object midVal = list.get(mid);
            int cmp = ((Comparable) midVal).compareTo(key);
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    /**
		 * Searches the specified list for the specified object using the binary
		 * search algorithm.  The list must be sorted into ascending order
		 * according to the specified comparator (as by the <tt>Sort(List,
		 * Comparator)</tt> method, above), prior to making this call.  If it is
		 * not sorted, the results are undefined.  If the list contains multiple
		 * elements equal to the specified object, there is no guarantee which one
		 * will be found.<p>
		 *
		 * This method runs in log(n) time for a "random access" list (which
		 * provides near-constant-time positional access).  It may
		 * run in n log(n) time if it is called on a "sequential access" list
		 * (which provides linear-time positional access).</p>
		 *
		 * If the specified list implements the <tt>AbstracSequentialList</tt>
		 * interface, this method will do a sequential search instead of a binary
		 * search; this offers linear performance instead of n log(n) performance
		 * if this method is called on a <tt>LinkedList</tt> object.
		 *
		 * @param  list the list to be searched.
		 * @param  key the key to be searched for.
		 * @param  c the comparator by which the list is ordered.  A
		 *        <tt>null</tt> value indicates that the elements' <i>natural
		 *        ordering</i> should be used.
		 * @return index of the search key, if it is contained in the list;
		 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
		 *         <i>insertion point</i> is defined as the point at which the
		 *         key would be inserted into the list: the index of the first
		 *         element greater than the key, or <tt>list.size()</tt>, if all
		 *         elements in the list are less than the specified key.  Note
		 *         that this guarantees that the return value will be &gt;= 0 if
		 *         and only if the key is found.
		 * @throws ClassCastException if the list contains elements that are not
		 *         <i>mutually comparable</i> using the specified comparator,
		 *         or the search key in not mutually comparable with the
		 *         elements of the list using this comparator.
		 * @see    Comparable
		 * @see #sort(List, Comparator)
		 */
    public static int binarySearch(List list, Object key, Comparator c) {
        if (c == null) return binarySearch(list, key);
        if (list instanceof AbstractSequentialList) {
            ListIterator i = list.listIterator();
            while (i.hasNext()) {
                int cmp = c.compare(i.next(), key);
                if (cmp == 0) return i.previousIndex(); else if (cmp > 0) return -i.nextIndex();
            }
            return -i.nextIndex() - 1;
        }
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Object midVal = list.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    /**
		 * Copies all of the elements from one list into another.  After the
		 * operation, the index of each copied element in the destination list
		 * will be identical to its index in the source list.  The destination
		 * list must be at least as long as the source list.  If it is longer, the
		 * remaining elements in the destination list are unaffected. <p>
		 *
		 * This method runs in linear time.
		 *
		 * @param  dest The destination list.
		 * @param  src The source list.
		 * @throws IndexOutOfBoundsException if the destination list is too small
		 *         to contain the entire source List.
		 * @throws UnsupportedOperationException if the destination list's
		 *         list-iterator does not support the <tt>set</tt> operation.
		 */
    public static void copy(List dest, List src) {
        try {
            for (ListIterator di = dest.listIterator(), si = src.listIterator(); si.hasNext(); ) {
                di.next();
                di.set(si.next());
            }
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException("Source does not fit in dest.");
        }
    }

    /**
		 * Returns an enumeration over the specified collection.  This provides
		 * interoperatbility with legacy APIs that require an enumeration
		 * as input.
		 *
		 * @param c the collection for which an enumeration is to be returned.
		 * @return an enumeration over the specified collection.
		 */
    public static Enumeration enumeration(final Collection c) {
        return new Enumeration() {

            Iterator i = c.iterator();

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public Object nextElement() {
                return i.next();
            }
        };
    }

    /**
		 * Returns true if the specified arguments are equal, or both null.
		 */
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    /**
		 * Replaces all of the elements of the specified list with the specified
		 * element. <p>
		 *
		 * This method runs in linear time.
		 *
		 * @param  list the list to be filled with the specified element.
		 * @param  o The element with which to fill the specified list.
		 * @throws UnsupportedOperationException if the specified list's
		 *         list-iterator does not support the <tt>set</tt> operation.
		 */
    public static void fill(List list, Object o) {
        for (ListIterator i = list.listIterator(); i.hasNext(); ) {
            i.next();
            i.set(o);
        }
    }

    /**
		 * Returns the maximum element of the given collection, according to the
		 * <i>natural ordering</i> of its elements.  All elements in the
		 * collection must implement the <tt>Comparable</tt> interface.
		 * Furthermore, all elements in the collection must be <i>mutually
		 * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
		 * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
		 * <tt>e2</tt> in the collection).<p>
		 *
		 * This method iterates over the entire collection, hence it requires
		 * time proportional to the size of the collection.
		 *
		 * @param  coll the collection whose maximum element is to be determined.
		 * @return the maximum element of the given collection, according
		 *         to the <i>natural ordering</i> of its elements.
		 * @throws ClassCastException if the collection contains elements that are
		 *         not <i>mutually comparable</i> (for example, strings and
		 *         integers).
		 * @throws NoSuchElementException if the collection is empty.
		 * @see Comparable
		 */
    public static Object max(Collection coll) {
        Iterator i = coll.iterator();
        Comparable candidate = (Comparable) (i.next());
        while (i.hasNext()) {
            Comparable next = (Comparable) (i.next());
            if (next.compareTo(candidate) > 0) candidate = next;
        }
        return candidate;
    }

    /**
		 * Returns the maximum element of the given collection, according to the
		 * order induced by the specified comparator.  All elements in the
		 * collection must be <i>mutually comparable</i> by the specified
		 * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
		 * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
		 * <tt>e2</tt> in the collection).<p>
		 *
		 * This method iterates over the entire collection, hence it requires
		 * time proportional to the size of the collection.
		 *
		 * @param  coll the collection whose maximum element is to be determined.
		 * @param  comp the comparator with which to determine the maximum element.
		 *         A <tt>null</tt> value indicates that the elements' <i>natural
		 *        ordering</i> should be used.
		 * @return the maximum element of the given collection, according
		 *         to the specified comparator.
		 * @throws ClassCastException if the collection contains elements that are
		 *         not <i>mutually comparable</i> using the specified comparator.
		 * @throws NoSuchElementException if the collection is empty.
		 * @see Comparable
		 */
    public static Object max(Collection coll, Comparator comp) {
        if (comp == null) return max(coll);
        Iterator i = coll.iterator();
        Object candidate = i.next();
        while (i.hasNext()) {
            Object next = i.next();
            if (comp.compare(next, candidate) > 0) candidate = next;
        }
        return candidate;
    }

    /**
		 * Returns the minimum element of the given collection, according to the
		 * <i>natural ordering</i> of its elements.  All elements in the
		 * collection must implement the <tt>Comparable</tt> interface.
		 * Furthermore, all elements in the collection must be <i>mutually
		 * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
		 * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
		 * <tt>e2</tt> in the collection).<p>
		 *
		 * This method iterates over the entire collection, hence it requires
		 * time proportional to the size of the collection.
		 *
		 * @param  coll the collection whose minimum element is to be determined.
		 * @return the minimum element of the given collection, according
		 *         to the <i>natural ordering</i> of its elements.
		 * @throws ClassCastException if the collection contains elements that are
		 *         not <i>mutually comparable</i> (for example, strings and
		 *         integers).
		 * @throws NoSuchElementException if the collection is empty.
		 * @see Comparable
		 */
    public static Object min(Collection coll) {
        Iterator i = coll.iterator();
        Comparable candidate = (Comparable) (i.next());
        while (i.hasNext()) {
            Comparable next = (Comparable) (i.next());
            if (next.compareTo(candidate) < 0) candidate = next;
        }
        return candidate;
    }

    /**
		 * Returns the minimum element of the given collection, according to the
		 * order induced by the specified comparator.  All elements in the
		 * collection must be <i>mutually comparable</i> by the specified
		 * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
		 * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
		 * <tt>e2</tt> in the collection).<p>
		 *
		 * This method iterates over the entire collection, hence it requires
		 * time proportional to the size of the collection.
		 *
		 * @param  coll the collection whose minimum element is to be determined.
		 * @param  comp the comparator with which to determine the minimum element.
		 *         A <tt>null</tt> value indicates that the elements' <i>natural
		 *         ordering</i> should be used.
		 * @return the minimum element of the given collection, according
		 *         to the specified comparator.
		 * @throws ClassCastException if the collection contains elements that are
		 *         not <i>mutually comparable</i> using the specified comparator.
		 * @throws NoSuchElementException if the collection is empty.
		 * @see Comparable
		 */
    public static Object min(Collection coll, Comparator comp) {
        if (comp == null) return min(coll);
        Iterator i = coll.iterator();
        Object candidate = i.next();
        while (i.hasNext()) {
            Object next = i.next();
            if (comp.compare(next, candidate) < 0) candidate = next;
        }
        return candidate;
    }

    /**
		 * Returns an immutable list consisting of <tt>n</tt> copies of the
		 * specified object.  The newly allocated data object is tiny (it contains
		 * a single reference to the data object).  This method is useful in
		 * combination with the <tt>List.addAll</tt> method to grow lists.
		 * The returned list is serializable.
		 *
		 * @param  n the number of elements in the returned list.
		 * @param  o the element to appear repeatedly in the returned list.
		 * @return an immutable list consisting of <tt>n</tt> copies of the
		 *          specified object.
		 * @throws IllegalArgumentException if n &lt; 0.
		 * @see    List#addAll(Collection)
		 * @see    List#addAll(int, Collection)
		 */
    public static List nCopies(int n, Object o) {
        return new CopiesList(n, o);
    }

    /**
		 * Reverses the order of the elements in the specified list.<p>
		 *
		 * This method runs in linear time.
		 *
		 * @param  l the list whose elements are to be reversed.
		 * @throws UnsupportedOperationException if the specified list's
		 *         list-iterator does not support the <tt>set</tt> operation.
		 */
    public static void reverse(List l) {
        ListIterator fwd = l.listIterator(), rev = l.listIterator(l.size());
        for (int i = 0, n = l.size() / 2; i < n; i++) {
            Object tmp = fwd.next();
            fwd.set(rev.previous());
            rev.set(tmp);
        }
    }

    /**
		 * Returns a comparator that imposes the reverse of the <i>natural
		 * ordering</i> on a collection of objects that implement the
		 * <tt>Comparable</tt> interface.  (The natural ordering is the ordering
		 * imposed by the objects' own <tt>compareTo</tt> method.)  This enables a
		 * simple idiom for sorting (or maintaining) collections (or arrays) of
		 * objects that implement the <tt>Comparable</tt> interface in
		 * reverse-natural-order.  For example, suppose a is an array of
		 * strings. Then: <pre>
		 *     Arrays.sort(a, Collections.reverseOrder());
		 * </pre> sorts the array in reverse-lexicographic (alphabetical) order.<p>
		 *
		 * The returned comparator is serializable.
		 *
		 * @return a comparator that imposes the reverse of the <i>natural
		 *          ordering</i> on a collection of objects that implement
		 *         the <tt>Comparable</tt> interface.
		 * @see Comparable
		 */
    public static Comparator reverseOrder() {
        return REVERSE_ORDER;
    }

    /**
		 * Returns an immutable list containing only the specified object.
		 * The returned list is serializable.
		 *
		 * @param o the sole object to be stored in the returned list.
		 * @return an immutable list containing only the specified object.
		 * @since 1.3
		 */
    public static List singletonList(Object o) {
        return new SingletonList(o);
    }

    /**
		 * Sorts the specified list into ascending order, according to the
		 * <i>natural ordering</i> of its elements.  All elements in the list must
		 * implement the <tt>Comparable</tt> interface.  Furthermore, all elements
		 * in the list must be <i>mutually comparable</i> (that is,
		 * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
		 * for any elements <tt>e1</tt> and <tt>e2</tt> in the list).<p>
		 *
		 * This sort is guaranteed to be <i>stable</i>:  equal elements will
		 * not be reordered as a result of the sort.<p>
		 *
		 * The specified list must be modifiable, but need not be resizable.<p>
		 *
		 * The sorting algorithm is a modified mergesort (in which the merge is
		 * omitted if the highest element in the low sublist is less than the
		 * lowest element in the high sublist).  This algorithm offers guaranteed
		 * n log(n) performance, and can approach linear performance on nearly
		 * sorted lists.<p>
		 *
		 * This implementation dumps the specified list into an array, sorts
		 * the array, and iterates over the list resetting each element
		 * from the corresponding position in the array.  This avoids the
		 * n<sup>2</sup> log(n) performance that would result from attempting
		 * to sort a linked list in place.
		 *
		 * @param  list the list to be sorted.
		 * @throws ClassCastException if the list contains elements that are not
		 *         <i>mutually comparable</i> (for example, strings and integers).
		 * @throws UnsupportedOperationException if the specified list's
		 *         list-iterator does not support the <tt>set</tt> operation.
		 * @see Comparable
		 */
    public static void sort(List list) {
        Object a[] = list.toArray();
        Arrays.sort(a);
        ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
    }

    /**
		 * Sorts the specified list according to the order induced by the
		 * specified comparator.  All elements in the list must be <i>mutually
		 * comparable</i> using the specified comparator (that is,
		 * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
		 * for any elements <tt>e1</tt> and <tt>e2</tt> in the list).<p>
		 *
		 * This sort is guaranteed to be <i>stable</i>:  equal elements will
		 * not be reordered as a result of the sort.<p>
		 *
		 * The sorting algorithm is a modified mergesort (in which the merge is
		 * omitted if the highest element in the low sublist is less than the
		 * lowest element in the high sublist).  This algorithm offers guaranteed
		 * n log(n) performance, and can approach linear performance on nearly
		 * sorted lists.<p>
		 *
		 * The specified list must be modifiable, but need not be resizable.
		 * This implementation dumps the specified list into an array, sorts
		 * the array, and iterates over the list resetting each element
		 * from the corresponding position in the array.  This avoids the
		 * n<sup>2</sup> log(n) performance that would result from attempting
		 * to sort a linked list in place.
		 *
		 * @param  list the list to be sorted.
		 * @param  c the comparator to determine the order of the list.  A
		 *        <tt>null</tt> value indicates that the elements' <i>natural
		 *        ordering</i> should be used.
		 * @throws ClassCastException if the list contains elements that are not
		 *         <i>mutually comparable</i> using the specified comparator.
		 * @throws UnsupportedOperationException if the specified list's
		 *         list-iterator does not support the <tt>set</tt> operation.
		 * @see Comparator
		 */
    public static void sort(List list, Comparator c) {
        Object a[] = list.toArray();
        Arrays.sort(a, c);
        ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
    }

    /**
		 * Swaps the two specified elements in the specified list.
		 */
    private static void swap(List a, int i, int j) {
        Object tmp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tmp);
    }

    /**
		 * Returns a synchronized (thread-safe) collection backed by the specified
		 * collection.  In order to guarantee serial access, it is critical that
		 * <strong>all</strong> access to the backing collection is accomplished
		 * through the returned collection.<p>
		 *
		 * It is imperative that the user manually synchronize on the returned
		 * collection when iterating over it:
		 * <pre>
		 *  Collection c = Collections.synchronizedCollection(myCollection);
		 *     ...
		 *  synchronized(c) {
		 *      Iterator i = c.iterator(); // Must be in the synchronized block
		 *      while (i.hasNext())
		 *         foo(i.next());
		 *  }
		 * </pre>
		 * Failure to follow this advice may result in non-deterministic behavior.
		 *
		 * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
		 * and <tt>equals</tt> operations through to the backing collection, but
		 * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
		 * necessary to preserve the contracts of these operations in the case
		 * that the backing collection is a set or a list.<p>
		 *
		 * The returned collection will be serializable if the specified collection
		 * is serializable.
		 *
		 * @param  c the collection to be "wrapped" in a synchronized collection.
		 * @return a synchronized view of the specified collection.
		 */
    public static Collection synchronizedCollection(Collection c) {
        return new SynchronizedCollection(c);
    }

    static Collection synchronizedCollection(Collection c, Object mutex) {
        return new SynchronizedCollection(c, mutex);
    }

    /**
		 * Returns a synchronized (thread-safe) list backed by the specified
		 * list.  In order to guarantee serial access, it is critical that
		 * <strong>all</strong> access to the backing list is accomplished
		 * through the returned list.<p>
		 *
		 * It is imperative that the user manually synchronize on the returned
		 * list when iterating over it:
		 * <pre>
		 *  List list = Collections.synchronizedList(new ArrayList());
		 *      ...
		 *  synchronized(list) {
		 *      Iterator i = list.iterator(); // Must be in synchronized block
		 *      while (i.hasNext())
		 *          foo(i.next());
		 *  }
		 * </pre>
		 * Failure to follow this advice may result in non-deterministic behavior.
		 *
		 * <p>The returned list will be serializable if the specified list is
		 * serializable.
		 *
		 * @param  list the list to be "wrapped" in a synchronized list.
		 * @return a synchronized view of the specified list.
		 */
    public static List synchronizedList(List list) {
        return new SynchronizedList(list);
    }

    static List synchronizedList(List list, Object mutex) {
        return new SynchronizedList(list, mutex);
    }

    /**
		 * Returns an unmodifiable view of the specified collection.  This method
		 * allows modules to provide users with "read-only" access to internal
		 * collections.  Query operations on the returned collection "read through"
		 * to the specified collection, and attempts to modify the returned
		 * collection, whether direct or via its iterator, result in an
		 * <tt>UnsupportedOperationException</tt>.<p>
		 *
		 * The returned collection does <i>not</i> pass the hashCode and equals
		 * operations through to the backing collection, but relies on
		 * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
		 * is necessary to preserve the contracts of these operations in the case
		 * that the backing collection is a set or a list.<p>
		 *
		 * The returned collection will be serializable if the specified collection
		 * is serializable.
		 *
		 * @param  c the collection for which an unmodifiable view is to be
		 *         returned.
		 * @return an unmodifiable view of the specified collection.
		 */
    public static Collection unmodifiableCollection(Collection c) {
        return new UnmodifiableCollection(c);
    }

    /**
		 * Returns an unmodifiable view of the specified list.  This method allows
		 * modules to provide users with "read-only" access to internal
		 * lists.  Query operations on the returned list "read through" to the
		 * specified list, and attempts to modify the returned list, whether
		 * direct or via its iterator, result in an
		 * <tt>UnsupportedOperationException</tt>.<p>
		 *
		 * The returned list will be serializable if the specified list
		 * is serializable.
		 *
		 * @param  list the list for which an unmodifiable view is to be returned.
		 * @return an unmodifiable view of the specified list.
		 */
    public static List unmodifiableList(List list) {
        return new UnmodifiableList(list);
    }
}
