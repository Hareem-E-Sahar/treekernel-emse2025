package com.teletalk.jserver.util.primitive;

import java.util.Arrays;

/**
 * This class implements a list of primitive float values.
 * 
 * @author Tobias L�fstrand
 * 
 * @since 1.1 Build 545
 */
public final class FloatList extends PrimitiveList {

    static final long serialVersionUID = -1282080052703873544L;

    private float[] floatArray;

    private final float defaultValue;

    /**
	 * Creates a new FloatList with an initial capacity of 10 and a capacity increment of 10. The default values of 
	 * the elements in this list will be <code>0</code>.
	 */
    public FloatList() {
        this(10, 10);
    }

    /**
	 * Creates a new FloatList with a capacity increment of 10. The default values of 
	 * the elements in this list will be <code>0</code>.
	 * 
	 * @param initialCapacity the initial capacity of the list.
	 */
    public FloatList(final int initialCapacity) {
        this(initialCapacity, 10);
    }

    /**
	 * Creates a new FloatList. The default values of 
	 * the elements in this list will be <code>0</code>.
	 * 
	 * @param initialCapacity the initial capacity of the list.
	 * @param capacityIncrement the number of cells by which the list will grow when necessary.
	 */
    public FloatList(final int initialCapacity, final int capacityIncrement) {
        this(initialCapacity, capacityIncrement, 0);
    }

    /**
	 * Creates a new FloatList. 
	 * 
	 * @param initialCapacity the initial capacity of the list.
	 * @param capacityIncrement the number of cells by which the list will grow when necessary.
	 * @param defaultValue the default value of the elements in this list.
	 */
    public FloatList(final int initialCapacity, final int capacityIncrement, final float defaultValue) {
        super(initialCapacity, capacityIncrement);
        this.defaultValue = defaultValue;
    }

    /**
	 * Creates a new FloatList initialized with the specified array and with a capacity increment of 10. The default values of 
	 * the elements in this list will be <code>0</code>.
	 * 
	 * @param array an array to fill the list with.
	 */
    public FloatList(final float[] array) {
        this(array, 10);
    }

    /**
	 * Creates a new FloatList initialized with the specified array. The default values of 
	 * the elements in this list will be <code>0</code>.
	 * 
	 * @param array an array to fill the list with.
	 * @param capacityIncrement the number of cells by which the list will grow when necessary.
	 */
    public FloatList(final float[] array, final int capacityIncrement) {
        this(array, capacityIncrement, 0);
    }

    /**
	 * Creates a new FloatList initialized with the specified array.
	 * 
	 * @param array an array to fill the list with.
	 * @param capacityIncrement the number of cells by which the list will grow when necessary.
	 * @param defaultValue the default value of the elements in this list.
	 */
    public FloatList(final float[] array, final int capacityIncrement, final float defaultValue) {
        super(array, array.length, capacityIncrement);
        this.defaultValue = defaultValue;
    }

    protected Object getPrimitiveArray() {
        return floatArray;
    }

    protected void setPrimitiveArray(Object primitiveArray) {
        this.floatArray = (float[]) primitiveArray;
    }

    /**
	 * Adds a value to the end of this list.
	 * 
	 * @param value the value to add.
	 * 
	 * @return the index at which the value was added.
	 */
    public int add(final float value) {
        final int addIndex = super.prepareAdd(1);
        this.floatArray[addIndex] = value;
        return addIndex;
    }

    /**
	 * Adds a value to this list at the specified index. The element at the specified index (if any) and all subsequent elements 
	 * will be shifted upward (one will be added to their indices).
	 * 
	 * @param index the index at which to add the value.
	 * @param value the value to add.
	 * 
	 * @exception IndexOutOfBoundsException if index is < 0 or > the current size.
	 */
    public void add(final int index, final float value) throws IndexOutOfBoundsException {
        super.prepareAdd(1, index);
        this.floatArray[index] = value;
    }

    /**
	 * Adds a value to this list at the index according to the natural ordering of the elements (the binarySearch method is used).
	 * 
	 * @param value the value to add.
	 * 
	 * @return the index at which the value was added.
	 */
    public int addSorted(final float value) {
        int addIndex = this.binarySearch(value);
        if (addIndex < 0) addIndex = (-addIndex) - 1;
        this.add(addIndex, value);
        return addIndex;
    }

    /**
	 * Adds all the values in the specified array to the end of this list.
	 * 
	 * @param values the values to add.
	 * 
	 * @return the index at which the first element of the specified array was added.
	 */
    public int addAll(final float[] values) {
        final int startIndex = super.prepareAdd(values.length);
        System.arraycopy(values, 0, this.floatArray, startIndex, values.length);
        return startIndex;
    }

    /**
	 * Adds all the values in the specified array to this list  at the specified index. The element at the 
	 * specified index (if any) and all subsequent elements  will be shifted upward (values.length will be added to their indices).
	 * 
	 * @param addIndex the index at which to add the values.
	 * @param values the values to add.
	 * 
	 * @exception IndexOutOfBoundsException if index is < 0 or > the current size.
	 */
    public void addAll(final int addIndex, final float[] values) throws IndexOutOfBoundsException {
        super.prepareAdd(values.length, addIndex);
        System.arraycopy(values, 0, this.floatArray, addIndex, values.length);
    }

    /**
	 * Removes the value at the specified index.
	 * 
	 * @param index the index at which to remove a value.
	 * 
	 * @return the removed value.
	 * 
	 * @exception IndexOutOfBoundsException if index is < 0 or >= the current size.
	 */
    public float remove(final int index) throws IndexOutOfBoundsException {
        final float oldValue = this.floatArray[index];
        super.removeValueAtIndex(index);
        return oldValue;
    }

    /**
	 * Removes the first occurrence of the specified value from this list.
	 * 
	 * @param value the value to remove.
	 * 
	 * @return <code>true</code> if the specified value was found (and removed).
	 */
    public boolean removeValue(final float value) {
        final int arrayLength = super.size();
        for (int i = 0; i < arrayLength; i++) {
            if (this.floatArray[i] == value) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
	 * Removes the first occurrence of the specified value from this list, using the binarySearch method.
	 * 
	 * @param value the value to remove.
	 * 
	 * @return <code>true</code> if the specified value was found (and removed).
	 */
    public boolean removeValueSorted(final float value) {
        final int removeIndex = this.binarySearch(value);
        if (removeIndex >= 0) {
            remove(removeIndex);
            return true;
        }
        return false;
    }

    /**
	 * Gets the value at the specified index.
	 * 
	 * @param index of the element to get the value of.
	 * 
	 * @return the value at the specified index.
	 * 
	 * @exception IndexOutOfBoundsException if index is < 0 or >= the current size.
	 */
    public float get(final int index) throws IndexOutOfBoundsException {
        super.boundsCheck(index);
        return this.floatArray[index];
    }

    /**
	 * Sets the value of the element at the specified index.
	 * 
	 * @param index the index of the value to set.
	 * @param value the new value to set.
	 * 
	 * @exception IndexOutOfBoundsException if index is < 0 or >= the current size.
	 */
    public float set(final int index, final float value) throws IndexOutOfBoundsException {
        super.boundsCheck(index);
        final float oldValue = this.floatArray[index];
        this.floatArray[index] = value;
        return oldValue;
    }

    protected void resetValues(final int fromIndex, final int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            this.floatArray[i] = defaultValue;
        }
    }

    protected Object newArrary(final int size) {
        return new float[size];
    }

    /**
	 * Returns all the elements in this list as an array.
	 * 
	 * @return this list as an array.
	 */
    public float[] toArray() {
        final int valueCount = super.size();
        final float[] all = new float[valueCount];
        System.arraycopy(this.floatArray, 0, all, 0, valueCount);
        return all;
    }

    /**
	 * Searches this list for the specified key using the binary search algorithm. 
	 * The list must be sorted into ascending order according to the natural ordering of its elements (as by the sort method) 
	 * prior to making this call. If the list contains multiple elements equal to the specified key, there is no guarantee which one will be found.
	 * 
	 * @param key the value to search for.
	 * 
	 * @return index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1). The insertion point is defined as 
	 * the point at which the key would be inserted into the list: the index of the first element greater than the key, or the current size of the list, if all elements 
	 * in the list are less than the specified key. Note that this guarantees that the return value will be >= 0 if and only if the key is found.
	 */
    public int binarySearch(final float key) {
        int low = 0;
        int middle;
        int high = super.size() - 1;
        float middleValue;
        while (low <= high) {
            middle = (low + high) / 2;
            middleValue = this.floatArray[middle];
            if (middleValue < key) {
                low = middle + 1;
            } else if (middleValue > key) {
                high = middle - 1;
            } else {
                return middle;
            }
        }
        return -(low + 1);
    }

    /**
	 * Checks if this lists contains the number specified by parameter <code>key</code>.
	 * 
	 * @param key the number to search for.
	 * 
	 * @return <code>true</code> if the specified number was found in this list, otherwise <code>false</code>.
	 * 
	 * @since 1.3
	 */
    public boolean contains(final float key) {
        for (int i = 0; i < floatArray.length; i++) {
            if (floatArray[i] == key) return true;
        }
        return false;
    }

    /**
	 * Sorts this list according to the natural ordering of the elements (using the Arrays.sort() method). 
	 */
    public void sort() {
        Arrays.sort(this.floatArray, 0, this.size());
    }

    /**
	 * Compares the specified object with this list for equality. This method 
	 * returns <code>true</code> if and only if the specified object is of the same 
	 * class as this list, has the same size as this list and all the elelemts are equal and in 
	 * the same order.
	 * 
	 * @param o an object to be compared for equality with this list.
	 * 
	 * @return <code>true</code> if the specified object is equal to this list.
	 */
    public boolean equals(Object o) {
        float[] otherArray;
        if (o instanceof FloatList) otherArray = ((FloatList) o).toArray(); else if (o instanceof float[]) otherArray = (float[]) o; else return false;
        if (super.size() != otherArray.length) return false;
        for (int i = 0; i < otherArray.length; i++) {
            if (this.floatArray[i] != otherArray[i]) return false;
        }
        return true;
    }

    /**
    * Gets a string representation of this list.
    */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        final int valueCount = super.size();
        for (int i = 0; i < valueCount; i++) {
            buf.append(String.valueOf(this.floatArray[i]));
            if (i < (valueCount - 1)) buf.append(",");
        }
        buf.append("]");
        return buf.toString();
    }
}
