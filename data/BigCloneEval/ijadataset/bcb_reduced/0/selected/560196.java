package de.grogra.util;

import de.grogra.xl.util.IntList;

public class Int2ObjectMap {

    private int[] keys;

    private Object[] values;

    private int size;

    public Int2ObjectMap(int capacity) {
        if (capacity < 4) {
            capacity = 4;
        }
        keys = new int[capacity];
        values = new Object[capacity];
    }

    public Int2ObjectMap() {
        this(16);
    }

    public Int2ObjectMap(int[] keys, int off, int len) {
        this(len);
        System.arraycopy(keys, off, this.keys, 0, len);
        size = len;
    }

    public Int2ObjectMap dup() {
        Int2ObjectMap d = new Int2ObjectMap(size);
        System.arraycopy(keys, 0, d.keys, 0, size);
        System.arraycopy(values, 0, d.values, 0, size);
        d.size = size;
        return d;
    }

    public int[] getKeys(int[] array) {
        if ((array == null) || (array.length < size)) {
            array = new int[size];
        }
        System.arraycopy(keys, 0, array, 0, size);
        return array;
    }

    public Object[] getValues(Object[] array) {
        if (array == null) {
            array = new Object[size];
        } else if (array.length < size) {
            array = (Object[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        System.arraycopy(values, 0, array, 0, size);
        return array;
    }

    public final synchronized Object synchronizedPut(int key, Object value) {
        return put(key, value);
    }

    public final Object put(int key, Object value) {
        int i = IntList.binarySearch(keys, key, 0, size);
        if (i >= 0) {
            Object prev = values[i];
            values[i] = value;
            return prev;
        }
        i = ~i;
        if (keys.length == size) {
            System.arraycopy(keys, 0, keys = new int[2 * size], 0, size);
            System.arraycopy(values, 0, values = new Object[2 * size], 0, size);
        }
        if (i < size) {
            System.arraycopy(keys, i, keys, i + 1, size - i);
            System.arraycopy(values, i, values, i + 1, size - i);
        }
        keys[i] = key;
        values[i] = value;
        size++;
        return null;
    }

    public final Object get(int key) {
        int i = IntList.binarySearch(keys, key, 0, size);
        return (i >= 0) ? values[i] : null;
    }

    public final synchronized Object synchronizedGet(int key) {
        return get(key);
    }

    public final Object remove(int key) {
        int i = IntList.binarySearch(keys, key, 0, size);
        if (i >= 0) {
            Object o = values[i];
            size--;
            System.arraycopy(keys, i + 1, keys, i, size - i);
            System.arraycopy(values, i + 1, values, i, size - i);
            values[size] = null;
            return o;
        }
        return null;
    }

    public final synchronized Object synchronizedRemove(int key) {
        return remove(key);
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = size - 1; i >= 0; i--) {
            values[i] = null;
        }
        size = 0;
    }

    public int getKeyAt(int index) {
        return keys[index];
    }

    public Object getValueAt(int index) {
        return values[index];
    }

    public int findIndex(int key) {
        return IntList.binarySearch(keys, key, 0, size);
    }

    public void setValueAt(int index, Object value) {
        values[index] = value;
    }
}
