package programming5.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import programming5.arrays.ArrayOperations;
import programming5.math.MathOperations;

/**
 *Provides utility methods to use with/for collections classes
 *@author Andres Quiroz Hernandez
 *@version 6.08
 */
public abstract class CollectionUtils {

    /**
     * To create a vector with the elements of a given array, for which there is surprisingly no constructor
     * in the Vector class.
     * @param array the input array; the elements of the array will be put into the vector by reference
     * @return the filled vector of the type of the input array elements
     * @deprecated use of Vector class is obsolete; use listFromArray instead
     */
    @Deprecated
    public static final <T> Vector<T> vectorFromArray(T[] array) {
        Vector<T> ret = new Vector<T>();
        for (T element : array) {
            ret.add(element);
        }
        return ret;
    }

    /**
     * To create a list with the elements of a given array
     * @param array the input array; the elements of the array will be put into the list by reference
     * @return the filled list of the type of the input array elements
     */
    public static final <T> List<T> listFromArray(T[] array) {
        List<T> ret = new ArrayList<T>();
        for (T element : array) {
            ret.add(element);
        }
        return ret;
    }

    /**
     * To create an integer vector with the elements of a given array, for which there is surprisingly no constructor
     * in the Vector class.
     * @param array the input array
     * @return the filled integer vector
     * @deprecated use of Vector class is obsolete; use listFromIntArray instead
     */
    @Deprecated
    public static Vector<Integer> vectorFromIntArray(int[] array) {
        Vector<Integer> ret = new Vector<Integer>();
        for (int element : array) {
            ret.add(element);
        }
        return ret;
    }

    /**
     * To create an integer list with the elements of a given array
     * @param array the input array
     * @return the filled integer list
     */
    public static List<Integer> listFromIntArray(int[] array) {
        List<Integer> ret = new ArrayList<Integer>();
        for (int element : array) {
            ret.add(element);
        }
        return ret;
    }

    /**
     * To create a vector initialized to contain the given element, for which there is surprisingly no constructor
     * in the Vector class.
     * @param element the input element, which will be put into the vector by reference
     * @return the typed vector containing the given element
     * @deprecated use of vector is obsolete; use Collections.singletonList instead
     */
    @Deprecated
    public static final <T> Vector<T> vectorFromElement(T element) {
        Vector<T> ret = new Vector<T>();
        ret.add(element);
        return ret;
    }

    /**
     * To create a vector initialized to contain the given element of a subclass of the vector type.
     * @param element the input element, which will be put into the vector by reference
     * @param type the vector type, which is a superclass of the element type.
     * @return the typed vector containing the given element
     * @deprecated use of Vector class is obsolete; use listFromElement method instead
     */
    @Deprecated
    public static final <T, U extends T> Vector<T> vectorFromElement(U element, Class<T> type) {
        Vector<T> ret = new Vector<T>();
        ret.add(element);
        return ret;
    }

    /**
     * To create a list initialized to contain the given element of a subclass of the list type.
     * @param element the input element, which will be put into the list by reference
     * @param type the list type, which is a superclass of the element type.
     * @return the typed list containing the given element
     */
    public static final <T, U extends T> List<T> listFromElement(U element, Class<T> type) {
        List<T> ret = new ArrayList<T>();
        ret.add(element);
        return ret;
    }

    /**
     * Upcasts the given list of a derived class to a list of a base class
     * @param list the list to downcast
     * @param type the superclass that will determine the returned list class
     * @return a new list of the base type with all the elements of the input list (by
     * reference)
     */
    public static final <T, U extends T> List<T> upcastList(List<U> list, Class<T> type) {
        List<T> ret = new ArrayList<T>();
        for (U element : list) {
            ret.add(element);
        }
        return ret;
    }

    /**
     * Downcasts the given list of a base class, if possible, to a list of a derived class.
     * @param list the list to upcast, which must be composed of elements of the derived class
     * @return a new list of the derived class with all the elements of the input list
     * @throws ClassCastException if the elements of the input list are not of the derived class
     */
    public static final <T, U extends T> List<U> downcastList(List<T> list) {
        List<U> ret = new ArrayList<U>();
        for (T element : list) {
            ret.add((U) element);
        }
        return ret;
    }

    /**
     *@return true if all of the elements of the input collection are true
     */
    public static boolean tautology(Collection<Boolean> collection) {
        boolean ret = true;
        for (Boolean value : collection) {
            if (!value) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     *@return true if all of the elements of the input collection are false
     */
    public static boolean contradiction(Collection<Boolean> collection) {
        boolean ret = true;
        for (Boolean value : collection) {
            if (value) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     *@return the maximum element value of the elements of the input collection
     */
    public static <N extends Number> N max(Collection<N> numericCollection) {
        if (!numericCollection.isEmpty()) {
            N max = numericCollection.iterator().next();
            for (N value : numericCollection) {
                if (MathOperations.compare(value, max) > 0) {
                    max = value;
                }
            }
            return max;
        } else {
            throw new IllegalArgumentException("CollectionUtils: Cannot find max value: Collection is empty");
        }
    }

    /**
     *@return the maximum element value of the elements of the input array, with respect to the given comparator
     */
    public static final <T, S extends T> Object max(Collection<S> collection, Comparator<T> comp) {
        S ret = collection.iterator().next();
        for (S element : collection) {
            if (comp.compare(element, ret) > 0) {
                ret = element;
            }
        }
        return ret;
    }

    /**
     *@return the index of the element of maximum value in the input list
     */
    public static <N extends Number> int maxIndex(List<N> numericList) {
        N max = numericList.get(0);
        int maxIndex = 0;
        for (int i = 1; i < numericList.size(); i++) {
            if (MathOperations.compare(numericList.get(i), max) > 0) {
                maxIndex = i;
                max = numericList.get(i);
            }
        }
        return maxIndex;
    }

    /**
     *@return the index of the element of maximum value in the input list, with respect to the given comparator
     */
    public static final <T, S extends T> int maxIndex(List<S> collection, Comparator<T> comp) {
        int ret = 0;
        S maxval = collection.get(0);
        for (int i = 1; i < collection.size(); i++) {
            if (comp.compare(collection.get(i), maxval) > 0) {
                maxval = collection.get(i);
                ret = i;
            }
        }
        return ret;
    }

    /**
     *@return the minimum element value of the elements of the input collection
     */
    public static <N extends Number> N min(Collection<N> numericCollection) {
        if (!numericCollection.isEmpty()) {
            N min = numericCollection.iterator().next();
            for (N value : numericCollection) {
                if (MathOperations.compare(value, min) < 0) {
                    min = value;
                }
            }
            return min;
        } else {
            throw new IllegalArgumentException("CollectionUtils: Cannot find max value: Collection is empty");
        }
    }

    /**
     *@return the minimum element value of the elements of the input array, with respect to the given comparator
     */
    public static final <T, S extends T> Object min(Collection<S> collection, Comparator<T> comp) {
        S ret = collection.iterator().next();
        for (S element : collection) {
            if (comp.compare(element, ret) < 0) {
                ret = element;
            }
        }
        return ret;
    }

    /**
     *@return the index of the element of maximum value in the input list
     */
    public static <N extends Number> int minIndex(List<N> numericList) {
        N min = numericList.get(0);
        int minIndex = 0;
        for (int i = 1; i < numericList.size(); i++) {
            if (MathOperations.compare(numericList.get(i), min) < 0) {
                minIndex = i;
                min = numericList.get(i);
            }
        }
        return minIndex;
    }

    /**
     *@return the index of the element of maximum value in the input list, with respect to the given comparator
     */
    public static final <T, S extends T> int minIndex(List<S> collection, Comparator<T> comp) {
        int ret = 0;
        S maxval = collection.get(0);
        for (int i = 1; i < collection.size(); i++) {
            if (comp.compare(collection.get(i), maxval) < 0) {
                maxval = collection.get(i);
                ret = i;
            }
        }
        return ret;
    }

    /**
     *@return the sum of the elements of the input collection
     */
    public static <N extends Number> N sum(Collection<N> collection) {
        Number ret;
        double sum = 0;
        for (Number n : collection) {
            sum += MathOperations.extractValue(n);
        }
        byte byteSum = (byte) sum;
        short shortSum = (short) sum;
        int intSum = (int) sum;
        long longSum = (long) sum;
        float floatSum = (float) sum;
        if (byteSum == sum) {
            ret = new Byte(byteSum);
        } else if (shortSum == sum) {
            ret = new Short(shortSum);
        } else if (intSum == sum) {
            ret = new Integer(intSum);
        } else if (longSum == sum) {
            ret = new Long(longSum);
        } else if (floatSum == sum) {
            ret = new Float(floatSum);
        } else {
            ret = new Double(sum);
        }
        return (N) ret;
    }

    /**
     *@return the average of the elements of the input collection
     */
    public static <N extends Number> double avg(Collection<N> collection) {
        return MathOperations.extractValue(sum(collection)) / (double) collection.size();
    }

    /**
     * @return the number of true elements in the given collection
     */
    public static final int countTrue(Collection<Boolean> collection) {
        int ret = 0;
        for (boolean element : collection) {
            if (element) {
                ret++;
            }
        }
        return ret;
    }

    /**
     *@return the element of collection that is closest to the given value
     */
    public static <N extends Number, M extends Number> N findClosest(Collection<N> collection, M value) {
        double minDiff = Double.MAX_VALUE;
        double searchValue = MathOperations.extractValue(value);
        N ret = collection.iterator().next();
        for (N element : collection) {
            double diff = Math.abs(searchValue - MathOperations.extractValue(element));
            if (diff < minDiff) {
                ret = element;
                minDiff = diff;
            }
        }
        return ret;
    }

    /**
     *@return the element of collection that is closest to the given value
     */
    public static <N extends Number, M extends Number> int findClosestIndex(Collection<N> collection, M value) {
        double minDiff = Double.MAX_VALUE;
        double searchValue = MathOperations.extractValue(value);
        N closest = collection.iterator().next();
        int closestIndex = 0;
        int index = 0;
        for (N element : collection) {
            double diff = Math.abs(searchValue - MathOperations.extractValue(element));
            if (diff < minDiff) {
                closest = element;
                minDiff = diff;
                closestIndex = index;
            }
            index++;
        }
        return closestIndex;
    }

    /**
     *@return the element of a sorted array that is closest to value
     */
    public static <N extends Number, M extends Number> N findClosestInOrder(List<N> collection, M value) {
        N ret;
        if (MathOperations.compare(value, collection.get(0)) <= 0) {
            ret = collection.get(0);
        } else if (MathOperations.compare(value, collection.get(collection.size() - 1)) >= 0) {
            ret = collection.get(collection.size() - 1);
        } else {
            int a = 0;
            int b = collection.size() - 1;
            int middle;
            while (b - a > 1) {
                middle = (a + b) / 2;
                if (MathOperations.compare(value, collection.get(middle)) > 0) {
                    a = middle;
                } else {
                    b = middle;
                }
            }
            System.out.println("B = " + b);
            double searchValue = MathOperations.extractValue(value);
            double diffLeft = Math.abs(searchValue - MathOperations.extractValue(collection.get(b - 1)));
            double diffRight = Math.abs(MathOperations.extractValue(collection.get(b)) - searchValue);
            if (diffLeft <= diffRight) {
                ret = collection.get(b - 1);
            } else {
                ret = collection.get(b);
            }
        }
        return ret;
    }

    /**
     *@return the element of a sorted array that is closest to value
     */
    public static <N extends Number, M extends Number> int findClosestIndexInOrder(List<N> collection, M value) {
        int ret;
        if (MathOperations.compare(value, collection.get(0)) <= 0) {
            ret = 0;
        } else if (MathOperations.compare(value, collection.get(collection.size() - 1)) > 0) {
            ret = collection.size() - 1;
        } else {
            int a = 0;
            int b = collection.size() - 1;
            int middle;
            while (b - a > 1) {
                middle = (a + b) / 2;
                if (MathOperations.compare(value, collection.get(middle)) > 0) {
                    a = middle;
                } else {
                    b = middle;
                }
            }
            System.out.println("B = " + b);
            double searchValue = MathOperations.extractValue(value);
            double diffLeft = Math.abs(searchValue - MathOperations.extractValue(collection.get(b - 1)));
            double diffRight = Math.abs(MathOperations.extractValue(collection.get(b)) - searchValue);
            if (diffLeft <= diffRight) {
                ret = b - 1;
            } else {
                ret = b;
            }
        }
        return ret;
    }

    /**
     * Finds the smallest item in the given sorted collection that is larger or equal to the given value
     * @param collection the sorted collection
     * @param value the value for the search
     */
    public static <T extends Comparable<T>, S extends T> T findNextInOrder(List<T> collection, S value) {
        int position = Collections.binarySearch(collection, value);
        T ret = null;
        if (position < 0) {
            position = -position - 1;
        }
        if (position < collection.size()) {
            ret = collection.get(position);
        }
        return ret;
    }

    /**
     * Finds the index of the smallest item in the given sorted collection that is larger or equal to the given value
     * @param collection the sorted collection
     * @param value the value for the search
     */
    public static <T extends Comparable<T>, S extends T> int findPositionInOrder(List<T> collection, S value) {
        int position = Collections.binarySearch(collection, value);
        if (position < 0) {
            position = -position - 1;
        }
        return position;
    }

    /**
     * Finds the smallest item in the given sorted collection that is larger or equal to the given value, according to
     * the given comparator.
     * @param collection the sorted collection
     * @param value the value for the search
     * @param comp the comparator to compare values in the collection
     */
    public static <T, S extends T, U extends T> S findNextInOrder(List<S> collection, U value, Comparator<T> comp) {
        S ret;
        if (comp.compare(value, collection.get(0)) <= 0) {
            ret = collection.get(0);
        } else if (comp.compare(value, collection.get(collection.size() - 1)) > 0) {
            ret = null;
        } else {
            int a = 0;
            int b = collection.size() - 1;
            int middle;
            while (b - a > 1) {
                middle = (a + b) / 2;
                if (comp.compare(value, collection.get(middle)) > 0) {
                    a = middle;
                } else {
                    b = middle;
                }
            }
            System.out.println("B = " + b);
            ret = collection.get(b);
        }
        return ret;
    }

    /**
     * Finds the index of the smallest item in the given sorted collection that is larger or equal to the given value,
     * according to the given comparator.
     * @param collection the sorted collection
     * @param value the value for the search
     * @param comp the comparator to compare values in the collection
     */
    public static final <T, S extends T, U extends T> int findPositionInOrder(List<S> collection, U value, Comparator<T> comp) {
        int position;
        if (comp.compare(value, collection.get(0)) <= 0) {
            position = 0;
        } else if (comp.compare(value, collection.get(collection.size() - 1)) > 0) {
            position = collection.size();
        } else {
            int a = 0;
            int b = collection.size() - 1;
            int middle;
            while (b - a > 1) {
                middle = (a + b) / 2;
                if (comp.compare(value, collection.get(middle)) > 0) {
                    a = middle;
                } else {
                    b = middle;
                }
            }
            System.out.println("B = " + b);
            position = b;
        }
        return position;
    }

    /**
     * Inserts the given element in the given list at the given position
     */
    public static <T, U extends T> void insert(List<T> list, U element, int position) {
        list.add(list.get(list.size() - 1));
        for (int i = list.size() - 2; i > position; i--) {
            list.set(i, list.get(i - 1));
        }
        list.set(position, element);
    }

    /**
     * Inserts the given element in the given sorted list of comparable elements in its corresponding position
     */
    public static <T extends Comparable<T>, S extends T> void insertSorted(List<T> collection, S value) {
        int position = findPositionInOrder(collection, value);
        collection.add(value);
        for (int i = collection.size() - 1; i > position; i--) {
            T swap = collection.get(i - 1);
            collection.set(i - 1, collection.get(i));
            collection.set(i, swap);
        }
    }

    /**
     * Inserts the given element in the given sorted list in its corresponding position, according to the given comparator
     */
    public static final <T, S extends T> void insertSorted(List<T> collection, S value, Comparator<T> comp) {
        int position = findPositionInOrder(collection, value, comp);
        collection.add(value);
        for (int i = collection.size() - 1; i > position; i--) {
            T swap = collection.get(i - 1);
            collection.set(i - 1, collection.get(i));
            collection.set(i, swap);
        }
    }

    /**
     * Prints out all the elements of the given collection, one on each line
     */
    public static void printVertical(Collection c) {
        for (Object element : c) {
            System.out.println(element);
        }
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static int[] toIntArray(Collection<Integer> c) {
        int[] ret = new int[c.size()];
        int i = 0;
        for (Integer element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static float[] toFloatArray(Collection<Float> c) {
        float[] ret = new float[c.size()];
        int i = 0;
        for (Float element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static long[] toLongArray(Collection<Long> c) {
        long[] ret = new long[c.size()];
        int i = 0;
        for (Long element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static double[] toDoubleArray(Collection<Double> c) {
        double[] ret = new double[c.size()];
        int i = 0;
        for (Double element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static boolean[] toBooleanArray(Collection<Boolean> c) {
        boolean[] ret = new boolean[c.size()];
        int i = 0;
        for (Boolean element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Constructs an array from the given collection, with no guarantee of the ordering of the elements in the array
     */
    public static String[] toStringArray(Collection<String> c) {
        String[] ret = new String[c.size()];
        int i = 0;
        for (String element : c) {
            ret[i++] = element;
        }
        return ret;
    }

    /**
     * Creates a new sublist from the given list, with elements from 0, inclusive, to until, exclusive.
     */
    public static <T> List<T> prefix(List<T> list, int until) {
        if (until <= list.size()) {
            List<T> ret = new ArrayList<T>();
            for (int i = 0; i < until; i++) {
                ret.add(list.get(i));
            }
            return ret;
        } else {
            throw new IllegalArgumentException("CollectionUtils: Illegal limit for prefix: Must be less than or equal to the list size");
        }
    }

    /**
     * Creates a new sublist from the given list, with elements from from, inclusive, to the last list element.
     */
    public static <T> List<T> suffix(List<T> list, int from) {
        if (from < list.size()) {
            List<T> ret = new ArrayList<T>();
            for (int i = from; i < list.size(); i++) {
                ret.add(list.get(i));
            }
            return ret;
        } else {
            throw new IllegalArgumentException("CollectionUtils: Illegal limit for suffix: Must be less than the list size");
        }
    }

    /**
     * Creates a new sublist from the given list, with elements from from, inclusive, to until, exclusive.
     */
    public static <T> List<T> subList(List<T> list, int from, int until) {
        if (from < until) {
            return suffix(prefix(list, until), from);
        } else {
            throw new IllegalArgumentException("CollectionUtils: Illegal limits for subList: From must be less than until");
        }
    }

    public static <T, U> List<T> findMatch(List<T> list, U feature, Comparator comp) {
        List<T> matches = new ArrayList<T>();
        for (T element : list) {
            if (comp.compare(element, feature) == 0) {
                matches.add(element);
            }
        }
        return matches;
    }

    /**
     * Finds the sorted order of the given list, which must be of comparable type
     * @param <T> List element type, extends Comparable
     * @param list the list to be sorted (will not be modified)
     * @return the sorted order of the list elements, such that sortedOrder(list)[i] is the index in the original list
     * of the ith element in order
     */
    public static <T extends Comparable> int[] sortedOrder(List<T> list) {
        Comparable[] listArray = list.toArray(new Comparable[] {});
        return ArrayOperations.sortedOrder(listArray);
    }

    /**
     * Finds the sorted order of the given list, without a compile-time check that the list elements are comparable
     * @param <T> List element type, expected to be comparable, but not checked
     * @param list the list to be sorted (will not be modified)
     * @return the sorted order of the list elements, such that sortedOrder(list)[i] is the index in the original list
     * of the ith element in order
     * @throws IllegalArgumentException at runtime if the given list does not contain comparable elements
     */
    public static <T> int[] sortedOrderRT(List<T> list) throws IllegalArgumentException {
        int[] ret = null;
        try {
            Comparable[] listArray = list.toArray(new Comparable[] {});
            ret = ArrayOperations.sortedOrder(listArray);
        } catch (Exception e) {
            throw new IllegalArgumentException("CollectionUtils: Cannot return sorted order: Given list elements not comparable", e);
        }
        return ret;
    }

    public static <T extends Comparable, S extends T> int binarySearch(List<T> list, S value, int[] order) {
        int ret;
        T firstElement = list.get(order[0]);
        T lastElement = list.get(order[list.size() - 1]);
        if (value.compareTo(firstElement) <= 0) {
            ret = (value.compareTo(firstElement) == 0) ? order[0] : -1;
        } else if (value.compareTo(lastElement) > 0) {
            ret = -1;
        } else {
            int a = 0;
            int b = list.size() - 1;
            int middle;
            while (b - a > 1) {
                middle = (a + b) / 2;
                if (value.compareTo(list.get(order[middle])) > 0) {
                    a = middle;
                } else {
                    b = middle;
                }
            }
            ret = (value.compareTo(list.get(order[b])) == 0) ? order[b] : -1;
        }
        return ret;
    }

    public static <T, S extends T> int binarySearchRT(List<T> list, S value, int[] order) throws IllegalArgumentException {
        int ret;
        try {
            Comparable cvalue = (Comparable) value;
            T firstElement = list.get(order[0]);
            T lastElement = list.get(order[list.size() - 1]);
            if (cvalue.compareTo(firstElement) <= 0) {
                ret = (cvalue.compareTo(firstElement) == 0) ? order[0] : -1;
            } else if (cvalue.compareTo(lastElement) > 0) {
                ret = -1;
            } else {
                int a = 0;
                int b = list.size() - 1;
                int middle;
                while (b - a > 1) {
                    middle = (a + b) / 2;
                    if (cvalue.compareTo(list.get(order[middle])) > 0) {
                        a = middle;
                    } else {
                        b = middle;
                    }
                }
                ret = (cvalue.compareTo(list.get(order[b])) == 0) ? order[b] : -1;
            }
        } catch (Exception e) {
            ret = -1;
            throw new IllegalArgumentException("CollectionUtils: Cannot complete binary search: Given list elements not comparable", e);
        }
        return ret;
    }
}
