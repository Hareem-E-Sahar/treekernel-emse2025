package guineu.modules.mylly.alignment.scoreAligner.functions;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class AlignmentPathQueue implements Queue<AlignmentPath> {

    private AlignmentPath[] arr;

    private int used;

    public AlignmentPathQueue(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size was too small (" + size + ")");
        }
        initalize(size);
    }

    private void initalize(int size) {
        arr = new AlignmentPath[size];
        used = 0;
    }

    public boolean add(AlignmentPath p) {
        if (!offer(p)) {
            throw new ArrayStoreException("No place for " + p.toString());
        }
        return true;
    }

    public boolean remove(AlignmentPath p) {
        int ix = 0;
        for (; ix < arr.length; ix++) {
            if (arr[ix].equals(p)) {
                if (remove(ix) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized AlignmentPath remove(int ix) {
        AlignmentPath val = arr[ix];
        if (val != null) {
            int copyLen = used - ix - 1;
            System.arraycopy(arr, ix + 1, arr, ix, copyLen);
            arr[used--] = null;
        }
        return val;
    }

    public AlignmentPath element() {
        if (used == 0) {
            throw new NoSuchElementException("No head element");
        }
        return arr[0];
    }

    public synchronized boolean offer(AlignmentPath p) {
        int ix = 0;
        boolean retVal = false;
        for (; ix < used; ix++) {
            if (p.getScore() < arr[ix].getScore()) {
                break;
            }
        }
        if (ix < arr.length) {
            if (used != 0 && ix != arr.length - 1) {
                int copyLen = used - ix - (used == arr.length ? 1 : 0);
                if (copyLen > 0) {
                    System.arraycopy(arr, ix, arr, ix + 1, copyLen);
                }
            }
            arr[ix] = p;
            retVal = true;
        }
        if (used != arr.length) {
            used++;
        }
        return retVal;
    }

    public AlignmentPath peek() {
        return arr[0];
    }

    public AlignmentPath poll() {
        return remove(0);
    }

    public AlignmentPath remove() {
        AlignmentPath head = remove(0);
        if (head == null) {
            throw new NoSuchElementException("No head element");
        }
        return head;
    }

    public boolean addAll(Collection<? extends AlignmentPath> c) {
        return false;
    }

    public void clear() {
        for (int i = 0; i < used; i++) {
            arr[i] = null;
        }
        used = 0;
    }

    public boolean contains(Object o) {
        if (o instanceof AlignmentPath) {
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return used == 0;
    }

    public Iterator<AlignmentPath> iterator() {
        return new Iterator<AlignmentPath>() {

            private int ix = 0;

            private boolean removeCalled = true;

            public boolean hasNext() {
                return (ix < used);
            }

            public AlignmentPath next() {
                synchronized (this) {
                    removeCalled = false;
                    return arr[ix++];
                }
            }

            public void remove() {
                synchronized (this) {
                    if (!removeCalled) {
                        AlignmentPathQueue.this.remove(ix);
                        removeCalled = true;
                    } else {
                        throw new IllegalStateException("next method has not yet been called, or the remove method has already been called after the last call to the next method.");
                    }
                }
            }
        };
    }

    /**
	 * Returns false as more specialized
	 * remove(AlignmentPath) will do the trick
	 * for correct items.
	 */
    public boolean remove(Object o) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Retain all is not supported");
    }

    public int size() {
        return used;
    }

    public Object[] toArray() {
        AlignmentPath[] returned = new AlignmentPath[used];
        for (int i = 0; i < used; i++) {
            returned[i] = arr[i];
        }
        return returned;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] targetArray) {
        if (targetArray.length < used) {
            targetArray = (T[]) java.lang.reflect.Array.newInstance(targetArray.getClass().getComponentType(), used);
        }
        System.arraycopy(arr, 0, targetArray, 0, used);
        if (targetArray.length > used) {
            targetArray[used] = null;
        }
        return targetArray;
    }
}
