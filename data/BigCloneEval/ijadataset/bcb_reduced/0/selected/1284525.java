package org.unitmetrics.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides static methods for ease of array handling.
 * @author Martin Kersten
 */
public class Arrays {

    /** 
	 * Returns the first index of the element or -1.
	 * @return The first index of the element or -1 
	 * @param array The array
	 * @param element The element to look for
	 */
    public static <T> int find(T[] array, T element) {
        Assert.notNull(array);
        if (element != null) {
            for (int index = 0; index < array.length; index++) if (array[index] != null && array[index].equals(element)) return index;
            return -1;
        } else return findNull(array);
    }

    /** 
	 * Returns the first index of the element or -1.
	 * @return The first index of the element or -1 
	 * @param array The array
	 * @param element The element to look for
	 */
    public static int find(int[] array, int element) {
        Assert.notNull(array);
        for (int index = 0; index < array.length; index++) if (array[index] == element) return index;
        return -1;
    }

    /**
	 * Returns true if the array contains the element.
	 * @return true if the array contains the element.
	 * @param array The array
	 * @param element The element to look for
	 * <p>Note: Method uses equals method for checking! </p>
	 */
    public static <T> boolean contains(T[] array, T element) {
        return find(array, element) != -1;
    }

    /**
	 * Returns true if the array contains the element.
	 * @return true if the array contains the element.
	 * @param array The array
	 * @param element The element to look for	 
	 */
    public static boolean contains(int[] array, int element) {
        return find(array, element) != -1;
    }

    /**
	 * Returns true if the array contains null.
	 * @return true if the array contains the element.
	 * @param array The array
	 */
    public static <T> boolean containsNull(T[] array) {
        Assert.notNull(array);
        for (int index = 0; index < array.length; index++) if (array[index] == null) return true;
        return false;
    }

    /**
	 * Returns true if the array contains the same instance.
	 * @return true if the array contains the same instance.
	 * @param array The array
	 * @param instance The instance to look for	 
	 */
    public static <T> boolean containsSame(T[] array, T instance) {
        Assert.notNull(array);
        for (int index = 0; index < array.length; index++) if (array[index] == instance) return true;
        return false;
    }

    /**
	 * Returns the index of the first element being null or -1.
	 * @return The index of the first element being null or -1.
	 * @param array The array
	 */
    public static <T> int findNull(T[] array) {
        Assert.notNull(array);
        for (int index = 0; index < array.length; index++) if (array[index] == null) return index;
        return -1;
    }

    /**
	 * Returns the content of the String array in the format 
	 * strings[0]+", "+strings[1]+", "+...+", "+string[n].
	 * @return The content of the string array
	 * @param strings The String array 
	 */
    public static String toString(String[] content) {
        Assert.notNull(content);
        return toString(content, ", ");
    }

    /**
	 * Returns the content of the String array in the format 
	 * strings[0]+seperator+strings[1]+seperator+...+seperator+string[n].
	 * @return The content of the string array
	 * @param strings The String array 
	 */
    public static String toString(String[] content, String seperator) {
        Assert.notNull(content);
        Assert.notNull(seperator);
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < content.length; index++) {
            if (index > 0) buffer.append(seperator);
            buffer.append(content[index]);
        }
        return buffer.toString();
    }

    /**
	 * Returns the String described by the char array.
	 * @return The String described by the array
	 * @param strings The char array 
	 */
    public static String toString(char[] content) {
        Assert.notNull(content);
        return new String(content);
    }

    /**
	 * Returns the content of the char array in the format 
	 * char[0]+seperator+char[1]+seperator+...+seperator+char[n].
	 * @return The content of the string array
	 * @param strings The String array 
	 */
    public static String toString(char[] content, String seperator) {
        Assert.notNull(content);
        Assert.notNull(seperator);
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < content.length; index++) {
            if (index > 0) buffer.append(seperator);
            buffer.append(content[index]);
        }
        return buffer.toString();
    }

    /**
	 * Returns true if both arrays equals by having the same type of array,
	 * the same number of elements and all elements of one array equals
	 * at least one element of the other. */
    public static boolean equals(Object[] array1, Object[] array2) {
        if (array1 == array2) return true; else if (array1 == null || array2 == null) return false; else if (array1.length != array2.length) return false; else if (!array1.getClass().equals(array2.getClass())) return false; else return compareArrays(array1, array2);
    }

    private static boolean compareArrays(Object[] array1, Object[] array2) {
        Object[] copy2 = (Object[]) array2.clone();
        for (int index = 0; index < array1.length; index++) {
            if (array1[index] != null) {
                boolean equaled = false;
                for (int index2 = 0; index2 < copy2.length; index2++) if (copy2[index2] != null && equals(array1[index], copy2[index2])) {
                    equaled = true;
                    copy2[index2] = null;
                }
                if (!equaled) return false;
            }
        }
        for (int index = 0; index < copy2.length; index++) if (copy2[index] != null) return false;
        return true;
    }

    /**
	 * Returns true if both elements equal. If both elements are arrays
	 * the arrays will be compared otherwise element.equals(element2) will
	 * be used. */
    private static boolean equals(Object element, Object element2) {
        if (element == element2) return true; else if (element == null || element2 == null) return false; else if (element instanceof Object[]) if (element2 instanceof Object[]) return equals((Object[]) element, (Object[]) element2); else return false; else return element.equals(element2);
    }

    /** 
	 * Adds the object at the end of array. Therefore a new array
	 * is created with an additional slot and of the same type.
	 * The value is added at the last.
	 */
    public static <T> T[] add(T[] array, T value) {
        return insert(array, array.length, value);
    }

    /** 
	 * Returns a new array of the same type containing the same values 
	 * plus the insert object. 
	 * @param pos The position to insert the value.
	 */
    @SuppressWarnings({ "unused", "unchecked" })
    public static <T> T[] insert(T[] array, int position, T value) {
        Assert.notNull(array);
        Assert.inRange(position, 0, array.length);
        T[] result = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        if (position > 0) System.arraycopy(array, 0, result, 0, position);
        if (position < array.length) System.arraycopy(array, position, result, position + 1, array.length - position);
        result[position] = value;
        return result;
    }

    /**
	 * Returns a new array containing the same values in the same order except
	 * the removed element. If the removed element is not part of the
	 * array the original array is returned.
	 */
    public static <T> T[] remove(T[] array, T objectToRemove) {
        int pos = Arrays.find(array, objectToRemove);
        if (pos >= 0) return Arrays.remove(array, pos); else return array;
    }

    /**
	 * Returns a new array containing the same values in the same order except
	 * the removed element at the given position.
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(T[] array, int position) {
        Assert.notNull(array);
        Assert.inRange(position, 0, array.length - 1);
        T[] result = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        if (position > 0) System.arraycopy(array, 0, result, 0, position);
        if (position < array.length - 1) System.arraycopy(array, position + 1, result, position, array.length - (position + 1));
        return result;
    }

    public static Object[] concat(Object[] array1, Object[] array2) {
        return concat(array1, array2, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] concat(Object[] array1, Object[] array2, Class<T> arrayType) {
        T[] result = (T[]) Array.newInstance(arrayType, array1.length + array2.length);
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<? super T> collection, Class<T> arrayType) {
        T[] array = (T[]) Array.newInstance(arrayType, collection.size());
        collection.toArray(array);
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] trim(T[] array, int newSize) {
        Assert.notNull(array);
        Assert.inRange(newSize, 0, Integer.MAX_VALUE, "newSize may not be smaller than zero");
        if (array.length > newSize) {
            Class<?> type = array.getClass().getComponentType();
            T[] newArray = (T[]) Array.newInstance(type, newSize);
            System.arraycopy(array, 0, newArray, 0, newSize);
            return newArray;
        } else return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] convert(Object[] array, Class<T> arrayType) {
        T[] newArray = (T[]) Array.newInstance(arrayType, array.length);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /** Returns a new array of same type being the filtered verion of the given array. */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T[] filter(T[] array, Filter<T> filter) {
        List<T> filteredVersion = new ArrayList<T>();
        for (T object : array) {
            if (filter.accept(object)) filteredVersion.add(object);
        }
        Class<T> componentType = (Class<T>) array.getClass().getComponentType();
        return Arrays.toArray(filteredVersion, componentType);
    }

    public static <T> List<T> asList(T... a) {
        return java.util.Arrays.asList(a);
    }
}
