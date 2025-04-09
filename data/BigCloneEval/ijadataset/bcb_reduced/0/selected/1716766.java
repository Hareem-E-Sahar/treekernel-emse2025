package gov.llnl.text.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import com.google.common.base.Objects;

/**
 * @author David Buttler
 *
 */
public class CountedPriorityQueue<T> implements Iterable<T>, Collection<T> {

    private int mSize;

    private PriorityQueue<Entry<T>> mQueue;

    private Map<T, Entry<T>> mEntityEntryMap;

    private Comparator<Number> mComparator;

    private Comparator<Entry<T>> mEntryComparator;

    /**
 * create a queue that manages priorities using the natural comparator (i.e. bigger numbers are a higher priority)
 * @param size
 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CountedPriorityQueue(int size) {
        this(size, new NaturalComparator());
    }

    /**
 * create a queue that manages priorities with the given comparator
 * @param size
 * @param comparator
 */
    public CountedPriorityQueue(int size, Comparator<Number> comparator) {
        mSize = size;
        mComparator = comparator;
        mEntryComparator = new EntryComparator<T>(mComparator);
        mQueue = new PriorityQueue<Entry<T>>(size + 1, mEntryComparator);
        mEntityEntryMap = new HashMap<T, Entry<T>>((size + 1) * 4 / 3);
    }

    /**
 * Add item to queue with a given priority
 * @param o
 * @param count
 */
    public void add(T o, Number count) {
        Entry<T> entry = new Entry<T>(o, count);
        Entry<T> oldEntry = mEntityEntryMap.get(o);
        if (oldEntry == null) {
            mQueue.add(entry);
            mEntityEntryMap.put(o, entry);
            while (mQueue.size() > mSize) {
                Entry<T> smallEntry = mQueue.poll();
                Entry<T> val = mEntityEntryMap.remove(smallEntry.value);
                if (val == null) throw new RuntimeException("failed to remove smallest entry from hashmap portion of counted priority queue");
            }
        } else {
            if (mEntryComparator.compare(entry, oldEntry) > 0) {
                oldEntry.count = entry.count;
                mQueue.remove(oldEntry);
                mQueue.add(oldEntry);
            }
        }
        assert mQueue.size() == mEntityEntryMap.size();
    }

    /**
 *
 *  Return an iterator that iterates over the elements of the queue in order specified by the comparator
 *  Note that we are iterating over a copy of the queue, so additions, updates, or deletions to the queue
 *  won't effect the iterations.
 * @see java.lang.Iterable#iterator()
 */
    public Iterator<T> iterator() {
        List<Entry<T>> eList = new ArrayList<Entry<T>>();
        for (Entry<T> e : mQueue) {
            eList.add(e);
        }
        Collections.sort(eList, mEntryComparator);
        List<T> list = new ArrayList<T>();
        for (Entry<T> e : eList) {
            list.add(e.value);
        }
        eList = null;
        return list.iterator();
    }

    public T poll() {
        Entry<T> val = mQueue.poll();
        return val.value;
    }

    public T peek() {
        Entry<T> val = mQueue.peek();
        return val.value;
    }

    /**
 * Add an item to the queue with a priority of 1
 */
    public boolean add(T o) {
        add(o, 1);
        return true;
    }

    /**
 * Add all items from a collection with a priority of 1
 */
    public boolean addAll(Collection<? extends T> c) {
        for (T o : c) {
            add(o, 1);
        }
        return true;
    }

    public void clear() {
        mQueue.clear();
    }

    public boolean contains(Object o) {
        for (Entry<T> e : mQueue) {
            if (o.equals(e.value)) return true;
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return mQueue.isEmpty();
    }

    public boolean remove(Object o) {
        Entry<T> entry = mEntityEntryMap.get(o);
        if (entry != null) {
            mQueue.remove(entry);
            Entry<T> val = mEntityEntryMap.remove(o);
            if (val == null) throw new RuntimeException("failed to remove smallest entry from hashmap portion of counted priority queue");
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        boolean v = true;
        for (Object o : c) {
            if (!remove(o)) {
                v = false;
            }
        }
        return v;
    }

    public boolean retainAll(Collection<?> c) {
        List<Entry<T>> rs = new ArrayList<Entry<T>>();
        for (Entry<T> e : mQueue) {
            if (!c.contains(e.value)) {
                rs.add(e);
            }
        }
        return mQueue.removeAll(rs);
    }

    public int size() {
        return mQueue.size();
    }

    public Object[] toArray() {
        Object[] a = new Object[mQueue.size()];
        int i = 0;
        for (Entry<T> e : mQueue) {
            a[i++] = e.value;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    public <S> S[] toArray(S[] a) {
        a = (S[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size());
        Object[] elementData = toArray();
        System.arraycopy(elementData, 0, a, 0, size());
        if (a.length > size()) {
            a[size()] = null;
        }
        return a;
    }

    /**
   * Get the score of a given item
   * @param item
   * @return the priority score of the given item. Null if the item is not in the queue
   */
    public Number getScore(T item) {
        Entry<T> entry = mEntityEntryMap.get(item);
        if (entry == null) return null;
        return entry.count;
    }
}

class Entry<T> {

    public T value;

    public Number count;

    public Entry(T v, Number c) {
        value = v;
        count = c;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Entry)) return false;
        Entry other = (Entry) o;
        if (Objects.equal(other.value, value) && count.equals(other.count)) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, count);
    }
}

class EntryComparator<T> implements Comparator<Entry<T>>, Serializable {

    /**
   * no data needs to be kept
   */
    private static final long serialVersionUID = 1L;

    private Comparator<Number> valueComparator;

    public EntryComparator(Comparator<Number> comparator) {
        valueComparator = comparator;
    }

    public int compare(Entry<T> o1, Entry<T> o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        return valueComparator.compare(o1.count, o2.count);
    }
}
