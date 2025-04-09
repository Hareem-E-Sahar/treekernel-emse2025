package gov.nasa.jpf.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides some useful methods for working with collections
 */
public final class CollectionsExt {

    private CollectionsExt() {
        throw new IllegalStateException("no instances");
    }

    public static final String newLine = System.getProperty("line.separator");

    /**
     * Returns the difference between the two sets.
     */
    public static <T> Set<T> diff(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> result = new LinkedHashSet<T>();
        result.addAll(s1);
        result.removeAll(s2);
        return result;
    }

    /**
     * Returns the union of the sets.
     */
    public static <T> Set<T> union(Set<? extends T>... sets) {
        Set<T> result = new LinkedHashSet<T>();
        for (Set<? extends T> s : sets) {
            result.addAll(s);
        }
        return result;
    }

    /**
     * Prints out the String.valueOf() of all elements of the collection, inserting a new line after each element.
     * The order is specified by the collection's iterator.
     */
    public static String toStringInLines(Collection<?> c) {
        if (c.isEmpty()) return "";
        return join(newLine, toStringLines(c)) + newLine;
    }

    /**
     * Prints out the elements of the collection in lines,
     * in lexicographic order of String.valueOf called on each element.
     */
    public static String toStringInSortedLines(Collection<?> c) {
        if (c.isEmpty()) return "";
        return join(newLine, sort(toStringLines(c))) + newLine;
    }

    /**
     * Prints out the elements of the collection in lines,
     * in lexicographic order of String.valueOf called on each element.
     */
    public static String toStringInSortedOneLine(Collection<?> c) {
        if (c.isEmpty()) return "";
        return join(" ", sort(toStringLines(c))) + newLine;
    }

    /**
     * List of String.valueOf() of all elements of the collection.
     * The order is specified by the collection's iterator.
     */
    public static List<String> toStringLines(Collection<?> c) {
        List<String> lines = new ArrayList<String>(c.size());
        for (Object each : c) {
            lines.add(String.valueOf(each));
        }
        return lines;
    }

    /**
     * Sort and return the list.
     * Useful for chaining the call.
     */
    public static List<String> sort(List<String> strings) {
        Collections.sort(strings);
        return strings;
    }

    /**
     * Reverse of String.split.
     * Glues together the strings and inserts the separator between each consecutive pair.
     */
    public static String join(String separator, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iter = strings.iterator(); iter.hasNext(); ) {
            String s = iter.next();
            sb.append(s);
            if (iter.hasNext()) sb.append(separator);
        }
        return sb.toString();
    }

    /**
     * Creates and returns an array that contains all elements of the array parameter and has
     * the el parameter appened.
     *
     * The runtime type of the resulting array is the same as the type of the argument array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] appendToArray(T[] array, T el) {
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = el;
        return newArray;
    }

    /**
     * Creates and returns an array that contains all elements of the array parameter and has
     * the el parameter prepended.
     *
     * The runtime type of the resulting array is the same as the type of the argument array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] prependToArray(T[] array, T el) {
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, newArray, 1, array.length);
        newArray[0] = el;
        return newArray;
    }
}
