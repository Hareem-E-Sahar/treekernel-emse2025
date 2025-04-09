package net.sourceforge.javautil.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Utilities/methods for dealing with array's and {@link Collection}'s.
 * 
 * @author ponder
 * @author $Author: ponderator $
 * @version $Id: CollectionUtil.java 2633 2010-11-29 03:43:00Z ponderator $
 */
public class CollectionUtil {

    /**
 	 * An empty {@link Object} array is used in many different code segments, especially when using reflection.
	 */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
	 * @param <T> The target collection type
	 * @param <I> The target item type
	 * @param target The target collection in which collections will be combined
	 * @param collections The to combine into the target
	 * @return The target collection
	 */
    public static <T extends Collection<I>, I> T combine(T target, Collection<I>... collections) {
        for (Collection<I> collection : collections) {
            target.addAll(collection);
        }
        return target;
    }

    /**
	 * @param <T> The type of instances to filter
	 * @param instances The array of the instances
	 * @return An array, possibly empty, of values that will not be null
	 */
    public static <T> T[] combineNonNull(T... instances) {
        return (T[]) FilterUtil.filter(instances, FilterUtil.NOTNULL);
    }

    /**
	 * @param <C> The comparable type
	 * @param collection1 The collection to compare from
	 * @param collection2 The collection to compare to
	 * @return 0 if both collections are equal (all items are equals and same size for both), -1 if collection1 is less than
	 * 	collection2 or 1 if collection1 is greater than collection2
	 */
    public static <C extends Comparable<C>> int compare(Collection<? extends C> collection1, Collection<? extends C> collection2) {
        Iterator<? extends C> items1 = collection1.iterator();
        Iterator<? extends C> items2 = collection2.iterator();
        while (items1.hasNext()) {
            if (!items2.hasNext()) return 1;
            int result = items1.next().compareTo(items2.next());
            if (result != 0) return result;
        }
        return items2.hasNext() ? -1 : 0;
    }

    /**
	 * @param <C> The type of collection
	 * @param set The collection to append to
	 * @param array The array from which to append
	 * @return The collection that was passed
	 */
    public static <C extends Collection> C appendTo(C collection, Object array) {
        for (int i = 0; i < Array.getLength(array); i++) {
            collection.add(Array.get(array, i));
        }
        return collection;
    }

    /**
	 * @param array1 The first array object
	 * @param array2 The second array object
	 * @return True if array1 is identical to array2 in structure and contents
	 */
    public static boolean equals(Object array1, Object array2) {
        assert array1 != null && array1.getClass().isArray() && array2 != null && array2.getClass().isArray() : "Both parameters must be arrays";
        if (Array.getLength(array1) != Array.getLength(array2)) return false;
        for (int a = 0; a < Array.getLength(array1); a++) {
            Object value1 = Array.get(array1, a);
            Object value2 = Array.get(array2, a);
            if (value1 == value2) continue;
            if (value1 == null || value2 == null) return false;
            if (value1.getClass().isArray() && value2.getClass().isArray()) {
                if (!equals(value1, value2)) return false;
            } else {
                if (!value1.equals(value2)) return false;
            }
        }
        return true;
    }

    /**
	 * @param array The array in question
	 * @param value The value to check for
	 * @return True if the array in question has the value, otherwise false
	 */
    public static <T> boolean contains(T[] array, T value) {
        for (int a = 0; a < Array.getLength(array); a++) {
            Object avalue = Array.get(array, a);
            if (avalue == value || (avalue != null && avalue.equals(value)) || (avalue instanceof Comparable && ((Comparable) avalue).compareTo(value) == 0)) return true;
        }
        return false;
    }

    /**
	 * Similar to {@link List#indexOf(Object)} this will return
	 * the index of the first element that is {@link Object#equals(Object)}
	 * to the provided value.
	 * 
	 * @param array The array in question
	 * @param value The value to search for
	 * @return The index of the first element that equals the value, or -1 if no such value could be found
	 */
    public static int getIndexOf(Object array, Object value) {
        for (int a = 0; a < Array.getLength(array); a++) {
            if ((value == null && Array.get(array, a) == null) || (value != null && value.equals(Array.get(array, a)))) return a;
        }
        return -1;
    }

    /**
	 * @param <T> The type of components of the list
	 * @param items The items to populate the list from
	 * @return A list containing all of the items
	 */
    public static <T> List<T> createList(T... items) {
        List<T> list = new ArrayList<T>();
        for (T item : items) list.add(item);
        return list;
    }

    /**
	 * @param <T> The type of components of the list
	 * @param lists One or more lists containing elements of the same type
	 * @return A list combining all of the elements in all the lists passed
	 */
    public static <T> List<T> createList(List<T>... lists) {
        List<T> list = new ArrayList<T>();
        for (List<T> l : lists) list.addAll(l);
        return list;
    }

    /**
	 * @param <T> The type of array
	 * @param array The array itself
	 * @return A copy of the array with the same elements.
	 */
    public static <T> T copy(T array) {
        Class componentType = array.getClass().getComponentType();
        int originalLength = Array.getLength(array);
        T newarray = (T) Array.newInstance(componentType, originalLength);
        System.arraycopy(array, 0, newarray, 0, originalLength);
        return newarray;
    }

    /**
	 * Remote one or more elements from an array.
	 * 
	 * @param <T> The type of array
	 * @param array The array itself
	 * @param idx The index at which to start element removal
	 * @param length The amount of elements to remove
	 * @return The new shorter array without the removed elements
	 */
    public static <T> T remove(T array, int idx, int length) {
        Class componentType = array.getClass().getComponentType();
        int originalLength = Array.getLength(array);
        T newarray = (T) Array.newInstance(componentType, originalLength - length);
        if (idx > 0) System.arraycopy(array, 0, newarray, 0, idx);
        if (idx + length < originalLength) System.arraycopy(array, idx + length, newarray, idx, originalLength - (idx + length));
        return newarray;
    }

    /**
	 * This is a facility method for inline additions.
	 *
	 * @see #insert(Object, Object, int)
	 */
    public static <T> T insert(T array, int idx, Object... additions) {
        return (T) insert(array, additions, idx);
    }

    /**
	 * @param <T> The type of array
	 * @param array The original array
	 * @param additionArray The additions to make to the array
	 * @param idx The index at which to insert the additions, use array length or -1 for appending
	 * @return The new array with the new additions
	 */
    public static <T> T insert(T array, T additionArray, int idx) {
        int additionLength = Array.getLength(additionArray);
        if (additionLength == 0) return array;
        Class componentType = array.getClass().getComponentType();
        int originalLength = Array.getLength(array);
        if (idx == -1) idx = originalLength;
        T newarray = (T) Array.newInstance(componentType, originalLength + additionLength);
        if (idx > 0) System.arraycopy(array, 0, newarray, 0, idx == originalLength ? idx : idx + 1);
        if (componentType.isPrimitive()) {
            for (int a = 0; a < additionLength; a++) {
                Array.set(newarray, idx + a, Array.get(additionArray, a));
            }
        } else {
            System.arraycopy(additionArray, 0, newarray, idx, additionLength);
        }
        if (idx < Array.getLength(array)) System.arraycopy(array, idx, newarray, idx + additionLength, originalLength - idx);
        return newarray;
    }

    /**
	 * @param array The array from which to create a list
	 * @return A list containing the elements of the array
	 */
    public static List<Object> asList(Object array) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < Array.getLength(array); i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }

    /**
	 * This will assume 1 positions to pop.
	 * 
	 * @see #pop(String[], int)
	 */
    public static String[] pop(String[] original) {
        return pop(original, 1);
    }

    /**
	 * @param original The original string array
	 * @param positions The amount of elements to pop off the end of the array
	 * @return A new shortened array without the elements that were removed
	 */
    public static String[] pop(String[] original, int positions) {
        String[] newArray = new String[original.length - positions];
        System.arraycopy(original, 0, newArray, 0, newArray.length);
        return newArray;
    }

    /**
	 * @param original The original string array
	 * @param additions The items to add to the end of the array
	 * @return A new larger array with the original and additions combined
	 */
    public static String[] push(String[] original, String... additions) {
        if (original.length == 0 && additions.length == 0) return new String[0];
        if (original.length == 0) return additions;
        if (additions.length == 0) return original;
        String[] newArray = new String[original.length + additions.length];
        System.arraycopy(original, 0, newArray, 0, original.length);
        System.arraycopy(additions, 0, newArray, original.length, additions.length);
        return newArray;
    }

    /**
	 * This assumes 1 position to shift.
	 * 
	 * @see #shift(String[], int)
	 */
    public static String[] shift(String[] original) {
        return shift(original, 1);
    }

    /**
	 * @param original The original string array
	 * @param positions The positions to remove from the beginning of the array
	 * @return A new shortened array with the positions removed from the beginning
	 */
    public static String[] shift(String[] original, int positions) {
        String[] newArray = new String[original.length - positions];
        System.arraycopy(original, positions, newArray, 0, newArray.length);
        return newArray;
    }

    /**
	 * @param original The original string array
	 * @param additions The new strings to add to the beginning of the array
	 * @return A new larger array with the new additions and the original combined
	 */
    public static String[] unshift(String[] original, String... additions) {
        if (original.length == 0 && additions.length == 0) return new String[0];
        if (original.length == 0) return additions;
        if (additions.length == 0) return original;
        String[] newArray = new String[original.length + additions.length];
        System.arraycopy(original, 0, newArray, additions.length, original.length);
        System.arraycopy(additions, 0, newArray, 0, additions.length);
        return newArray;
    }

    /**
	 * @param original The original array
	 * @param loc The location in the array to add the additions
	 * @param additions The new strings to add to the array
	 * @return A new larger array with the original and additions added
	 */
    public static String[] insert(String[] original, int loc, String... additions) {
        if (original.length == 0 && additions.length == 0) return new String[0];
        if (original.length == 0) return additions;
        if (additions.length == 0) return original;
        int total = loc + additions.length - 1;
        String[] news = new String[original.length + total - 1];
        int start = loc == 0 ? loc : 0;
        int end = loc == 0 ? original.length : loc;
        System.arraycopy(original, 0, news, start, end);
        System.arraycopy(additions, 0, news, loc, additions.length);
        if (start == 0) System.arraycopy(original, loc, news, loc + total - 1, original.length - loc);
        return news;
    }
}
