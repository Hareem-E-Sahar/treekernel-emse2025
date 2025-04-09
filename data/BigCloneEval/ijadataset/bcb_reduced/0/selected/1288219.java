package deduced;

import java.util.*;

/**
 * class used to operate a property collection as a list of values
 * 
 * @author DDuff
 */
class PropertyCollectionAsValueList implements List {

    /** the property collection represented by this list */
    private PropertyCollection _collection;

    /**
     * constructor
     * 
     * @param collection the property collection represented by this list
     */
    public PropertyCollectionAsValueList(PropertyCollection collection) {
        _collection = collection;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#size()
     */
    public int size() {
        return _collection.getSize();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#clear()
     */
    public void clear() {
        _collection.clear();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
        return _collection.isEmpty();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#toArray()
     */
    public Object[] toArray() {
        Object[] retVal = new Object[size()];
        Iterator it = _collection.iteratorByValue();
        int i = 0;
        while (it.hasNext()) {
            Object element = it.next();
            retVal[i] = element;
            ++i;
        }
        return retVal;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#get(int)
     */
    public Object get(int index) {
        return _collection.getPropertyValue(_collection.getKeyList().toArray()[index]);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#remove(int)
     */
    public Object remove(int index) {
        return _collection.removeProperty(_collection.getKeyList().toArray()[index]);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        if (o == null) {
            return DeducedConstant.INDEX_NOT_FOUND;
        }
        int index = 0;
        Iterator it = _collection.iteratorByValue();
        while (it.hasNext()) {
            Object element = it.next();
            if (o.equals(element)) {
                return index;
            }
            ++index;
        }
        return DeducedConstant.INDEX_NOT_FOUND;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            return DeducedConstant.INDEX_NOT_FOUND;
        }
        int index = 0;
        int retVal = DeducedConstant.INDEX_NOT_FOUND;
        Iterator it = _collection.iteratorByValue();
        while (it.hasNext()) {
            Object element = it.next();
            if (o.equals(element)) {
                retVal = index;
            }
            ++index;
        }
        return retVal;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(Object o) {
        _collection.addProperty(null, null, o);
        return true;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return indexOf(o) != DeducedConstant.INDEX_NOT_FOUND;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return remove(indexOf(o)) != null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection c) {
        Iterator it = c.iterator();
        int position = index;
        boolean changed = false;
        while (it.hasNext()) {
            Object i = it.next();
            add(position, i);
            position++;
            changed = true;
        }
        return changed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        Iterator it = c.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            Object i = it.next();
            add(i);
            changed = true;
        }
        return changed;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        Iterator it = c.iterator();
        boolean retVal = false;
        while (it.hasNext()) {
            Object element = it.next();
            if (remove(element)) {
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#iterator()
     */
    public Iterator iterator() {
        return _collection.iteratorByValue();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#subList(int, int)
     */
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        return _collection.setProperty(_collection.getKeyList().toArray()[index], element);
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.List#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        int size = size();
        if (a.length < size) {
            a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
}
