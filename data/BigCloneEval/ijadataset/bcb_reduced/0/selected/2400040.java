package net.sf.japi.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.sf.japi.util.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** This class provides some additional utility methods you might miss in {@link Arrays}.
 * It is named Arrays2 so you have no problems using both, this class and <code>java.util.Arrays</code> (no name conflict).
 * @author <a href="mailto:cher@riedquat.de">Christian Hujer</a>
 * @since 0.1
 * @see Arrays
 */
public final class Arrays2 {

    /** Random number generator. */
    private static final Random RND = new Random();

    /** Private constructor - no instances needed. */
    private Arrays2() {
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static double[] concat(@NotNull final double[]... a) {
        int ns = 0;
        for (final double[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final double[] na = new double[ns];
        int np = 0;
        for (final double[] anA : a) {
            if (anA != null) {
                System.arraycopy(anA, 0, na, np, anA.length);
                np += anA.length;
            }
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static float[] concat(@NotNull final float[]... a) {
        int ns = 0;
        for (final float[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final float[] na = new float[ns];
        int np = 0;
        for (final float[] anA : a) {
            if (anA != null) {
                System.arraycopy(anA, 0, na, np, anA.length);
                np += anA.length;
            }
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static long[] concat(@NotNull final long[]... a) {
        int ns = 0;
        for (final long[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final long[] na = new long[ns];
        int np = 0;
        for (final long[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static int[] concat(@NotNull final int[]... a) {
        int ns = 0;
        for (final int[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final int[] na = new int[ns];
        int np = 0;
        for (final int[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static short[] concat(@NotNull final short[]... a) {
        int ns = 0;
        for (final short[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final short[] na = new short[ns];
        int np = 0;
        for (final short[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static char[] concat(@NotNull final char[]... a) {
        int ns = 0;
        for (final char[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final char[] na = new char[ns];
        int np = 0;
        for (final char[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static byte[] concat(@NotNull final byte[]... a) {
        int ns = 0;
        for (final byte[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final byte[] na = new byte[ns];
        int np = 0;
        for (final byte[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static boolean[] concat(@NotNull final boolean[]... a) {
        int ns = 0;
        for (final boolean[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final boolean[] na = new boolean[ns];
        int np = 0;
        for (final boolean[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Concatenate Arrays together.
     * @param a Arrays to concatenate
     * @return new Array containing all elements of all arrays
     */
    @NotNull
    public static <T> T[] concat(@NotNull final T[]... a) {
        int ns = 0;
        for (final T[] anA : a) {
            if (anA != null) {
                ns += anA.length;
            }
        }
        final T[] na = (T[]) Array.newInstance(a[0].getClass().getComponentType(), ns);
        int np = 0;
        for (final T[] anA : a) {
            System.arraycopy(anA, 0, na, np, anA.length);
            np += anA.length;
        }
        assert ns == np;
        return na;
    }

    /** Returns an array only containing those elements accepted by the given filter.
     * The original array remains unmodified.
     * @param a      Elements to filter
     * @param filter Filter to use for <var>a</var>
     * @return array containing only those elements from <var>a</var> accepted by <var>filter</var>
     * @see Collections2#filter(Collection,Filter)
     */
    @NotNull
    public static <T> T[] filter(@NotNull final Filter<? super T> filter, @NotNull final T... a) {
        final List<T> tl = new ArrayList<T>();
        for (final T t : a) {
            if (filter.accept(t)) {
                tl.add(t);
            }
        }
        return tl.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), 0));
    }

    /** Count elements in an array that are accepted by the given filter.
     * @param a      Elements to count in
     * @param filter Filter to use for <var>a</var>
     * @return number of elements in <var>a</var> accepted by <var>filter</var>
     */
    public static <T> int count(@NotNull final Filter<? super T> filter, @NotNull final T... a) {
        int n = 0;
        for (final T t : a) {
            if (filter.accept(t)) {
                n++;
            }
        }
        return n;
    }

    /** Searches the specified array region of ints for the specified value using the binary search algorithm.
     * The array region <strong>must</strong> be sorted (as by the <tt>sort</tt> method, above) prior to making this call.
     * If it is not sorted, the results are undefined (even a runtime exception might occur).
     * If the array contains multiple elements with the specified value, there is no guarantee which one will be found.
     * @param a    the array to be searched.
     * @param key  the value to be searched for.
     * @param low  lower border (must be <code>&gt;= 0</code>)
     * @param high upper border (must be <code>&lt; a.length</code>)
     * @return index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the
     *         key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt>, if all
     *         elements in the list are less than the specified key.  Note
     *         that this guarantees that the return value will be &gt;= 0 if
     *         and only if the key is found.
     * @throws ArrayIndexOutOfBoundsException if <code>low &lt; 0</code> or <code>high &gt;= a.length</code>
     * @see Arrays#binarySearch(int[],int)
     * @see Arrays#sort(int[])
     */
    public static int binarySearch(@NotNull final int[] a, final int key, final int low, final int high) {
        int currentLow = low;
        int currentHigh = high;
        while (currentLow <= currentHigh) {
            final int mid = currentLow + currentHigh >> 1;
            final int midVal = a[mid];
            if (midVal < key) {
                currentLow = mid + 1;
            } else if (midVal > key) {
                currentHigh = mid - 1;
            } else {
                return mid;
            }
        }
        return -(currentLow + 1);
    }

    /** Perform a linear search on an array of booleans.
     * @param z boolean to find in <var>data</var>
     * @param data array of booleans to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final boolean z, @NotNull final boolean[] data) {
        return linearSearch(z, 0, data.length, data);
    }

    /** Perform a linear search on an array of chars.
     * @param c char to find in <var>data</var>
     * @param data array of chars to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final char c, @NotNull final char[] data) {
        return linearSearch(c, 0, data.length, data);
    }

    /** Perform a linear search on an array of doubles.
     * @param d double to find in <var>data</var>
     * @param data array of doubles to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final double d, @NotNull final double[] data) {
        return linearSearch(d, 0, data.length, data);
    }

    /** Perform a linear search on an array of floats.
     * @param f float to find in <var>data</var>
     * @param data array of floats to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final float f, @NotNull final float[] data) {
        return linearSearch(f, 0, data.length, data);
    }

    /** Perform a linear search on an array of longs.
     * @param l long to find in <var>data</var>
     * @param data array of longs to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final long l, @NotNull final long[] data) {
        return linearSearch(l, 0, data.length, data);
    }

    /** Perform a linear search on an array of ints.
     * @param n int to find in <var>data</var>
     * @param data array of ints to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final int n, @NotNull final int[] data) {
        return linearSearch(n, 0, data.length, data);
    }

    /** Perform a linear search on an array of shorts.
     * @param s short to find in <var>data</var>
     * @param data array of shorts to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final short s, @NotNull final short[] data) {
        return linearSearch(s, 0, data.length, data);
    }

    /** Perform a linear search on an array of bytes.
     * @param b byte to find in <var>data</var>
     * @param data array of bytes to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearSearch(final byte b, @NotNull final byte[] data) {
        return linearSearch(b, 0, data.length, data);
    }

    /** Perform a linear search on an array of booleans.
     * @param z boolean to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of booleans to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final boolean z, final int fromIndex, final int toIndex, @NotNull final boolean[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == z) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of chars.
     * @param c char to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of chars to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final char c, final int fromIndex, final int toIndex, @NotNull final char[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == c) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of doubles.
     * @param d double to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of doubles to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final double d, final int fromIndex, final int toIndex, @NotNull final double[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == d) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of floats.
     * @param f float to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of floats to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final float f, final int fromIndex, final int toIndex, @NotNull final float[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == f) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of longs.
     * @param l long to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of longs to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final long l, final int fromIndex, final int toIndex, @NotNull final long[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == l) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of ints.
     * @param n int to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of ints to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final int n, final int fromIndex, final int toIndex, @NotNull final int[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == n) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of shorts.
     * @param s short to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of shorts to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final short s, final int fromIndex, final int toIndex, @NotNull final short[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == s) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of bytes.
     * @param b byte to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of bytes to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearSearch(final byte b, final int fromIndex, final int toIndex, @NotNull final byte[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param data array of Objects to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearIdentitySearch(@Nullable final Object o, @NotNull final Object[] data) {
        return linearIdentitySearch(o, 0, data.length, data);
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of Objects to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    @SuppressWarnings({ "ObjectEquality" })
    public static int linearIdentitySearch(@Nullable final Object o, final int fromIndex, final int toIndex, @NotNull final Object[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (data[i] == o) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param data array of Objects to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static int linearEqualitySearch(@Nullable final Object o, @NotNull final Object[] data) {
        return linearEqualitySearch(o, 0, data.length, data);
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of Objects to perform linear search on
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static int linearEqualitySearch(@Nullable final Object o, final int fromIndex, final int toIndex, @NotNull final Object[] data) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (o == null && data[i] == null || o != null && o.equals(data[i]) || data[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param data array of Objects to perform linear search on
     * @param comp Comparator for comparing objects
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     */
    public static <T> int linearComparisonSearch(@Nullable final T o, @NotNull final T[] data, @NotNull final Comparator<? super T> comp) {
        return linearComparisonSearch(o, 0, data.length, data, comp);
    }

    /** Perform a linear search on an array of Objects.
     * @param o Object to find in <var>data</var>
     * @param fromIndex start index within <var>data</var>
     * @param toIndex end index within <var>data</var>
     * @param data array of Objects to perform linear search on
     * @param comp Comparator for comparing objects
     * @return index of <var>n</var> in <var>data</var> or -1 if not found
     * @throws ArrayIndexOutOfBoundsException in case <code><var>fromIndex</var> &lt; 0</code> or <code><var>toIndex</var> &gt;= data.length</code>
     */
    public static <T> int linearComparisonSearch(@Nullable final T o, final int fromIndex, final int toIndex, @NotNull final T[] data, @NotNull final Comparator<? super T> comp) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (comp.compare(o, data[i]) == 0) {
                return i;
            }
        }
        return -1;
    }

    /** Shuffle an array of booleans.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final boolean[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of booleans.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final boolean[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of booleans.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final boolean[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of booleans.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final boolean[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final boolean cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of chars.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final char[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of chars.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final char[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of chars.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final char[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of chars.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final char[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final char cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of bytes.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final byte[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of bytes.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final byte[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of bytes.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final byte[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of bytes.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final byte[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final byte cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of shorts.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final short[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of shorts.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final short[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of shorts.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final short[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of shorts.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final short[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final short cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of ints.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final int[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of ints.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final int[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of ints.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final int[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of ints.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final int[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final int cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of longs.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final long[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of longs.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final long[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of longs.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final long[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of longs.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final long[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final long cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of floats.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final float[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of floats.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final float[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of floats.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final float[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of floats.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final float[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final float cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of doubles.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final double[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of doubles.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final double[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of doubles.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final double[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of doubles.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final double[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final double cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Shuffle an array of Objects.
     * @param array Array to shuffle
     */
    public static void shuffle(@NotNull final Object[] array) {
        shuffle(array, 0, array.length, RND);
    }

    /** Shuffle an array of Objects.
     * @param array Array to shuffle
     * @param rnd Random Number Generator
     */
    public static void shuffle(@NotNull final Object[] array, @NotNull final Random rnd) {
        shuffle(array, 0, array.length, rnd);
    }

    /** Shuffle an array of Objects.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     */
    public static void shuffle(@NotNull final Object[] array, final int fromIndex, final int toIndex) {
        shuffle(array, fromIndex, toIndex, RND);
    }

    /** Shuffle an array of Objects.
     * @param array Array to shuffle
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param rnd Random number generator
     */
    public static void shuffle(@NotNull final Object[] array, final int fromIndex, final int toIndex, @NotNull final Random rnd) {
        for (int i = fromIndex; i < toIndex; i++) {
            final int j = rnd.nextInt(toIndex - fromIndex) + fromIndex;
            final Object cache = array[i];
            array[i] = array[j];
            array[j] = cache;
        }
    }

    /** Count the frequency of a specific boolean in an unsorted array of booleans.
     * @param array Array of booleans to count in
     * @param val boolean value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final boolean[] array, final boolean val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific boolean in an unsorted array of booleans.
     * @param array Array of booleans to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val boolean value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final boolean[] array, final int fromIndex, final int toIndex, final boolean val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific char in an unsorted array of chars.
     * @param array Array of chars to count in
     * @param val char value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final char[] array, final char val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific char in an unsorted array of chars.
     * @param array Array of chars to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val char value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final char[] array, final int fromIndex, final int toIndex, final char val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific byte in an unsorted array of bytes.
     * @param array Array of bytes to count in
     * @param val byte value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final byte[] array, final byte val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific byte in an unsorted array of bytes.
     * @param array Array of bytes to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val byte value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final byte[] array, final int fromIndex, final int toIndex, final byte val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific short in an unsorted array of shorts.
     * @param array Array of shorts to count in
     * @param val short value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final short[] array, final short val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific short in an unsorted array of shorts.
     * @param array Array of shorts to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val short value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final short[] array, final int fromIndex, final int toIndex, final short val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific int in an unsorted array of ints.
     * @param array Array of ints to count in
     * @param val int value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final int[] array, final int val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific int in an unsorted array of ints.
     * @param array Array of ints to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val int value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final int[] array, final int fromIndex, final int toIndex, final int val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific long in an unsorted array of longs.
     * @param array Array of longs to count in
     * @param val long value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final long[] array, final long val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific long in an unsorted array of longs.
     * @param array Array of longs to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val long value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final long[] array, final int fromIndex, final int toIndex, final long val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific float in an unsorted array of floats.
     * @param array Array of floats to count in
     * @param val float value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final float[] array, final float val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific float in an unsorted array of floats.
     * @param array Array of floats to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val float value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final float[] array, final int fromIndex, final int toIndex, final float val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific double in an unsorted array of doubles.
     * @param array Array of doubles to count in
     * @param val double value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freq(@NotNull final double[] array, final double val) {
        return freq(array, 0, array.length, val);
    }

    /** Count the frequency of a specific double in an unsorted array of doubles.
     * @param array Array of doubles to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val double value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freq(@NotNull final double[] array, final int fromIndex, final int toIndex, final double val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param val Object value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freqIdentity(@NotNull final Object[] array, @Nullable final Object val) {
        return freqIdentity(array, 0, array.length, val);
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val Object value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    @SuppressWarnings({ "ObjectEquality" })
    public static int freqIdentity(@NotNull final Object[] array, final int fromIndex, final int toIndex, @Nullable final Object val) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (array[i] == val) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param val Object value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static int freqEquals(@NotNull final Object[] array, @NotNull final Object val) {
        return freqEquals(array, 0, array.length, val);
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val Object value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static int freqEquals(@NotNull final Object[] array, final int fromIndex, final int toIndex, @Nullable final Object val) {
        if (val == null) {
            return freqIdentity(array, fromIndex, toIndex, null);
        }
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (val.equals(array[i])) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific Comparable in an unsorted array of Comparables.
     * @param array Array of Comparables to count in
     * @param val Comparable value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static <T extends Comparable<T>> int freqComparable(@NotNull final T[] array, @NotNull final T val) {
        return freqComparable(array, 0, array.length, val);
    }

    /** Count the frequency of a specific Comparable in an unsorted array of Comparables.
     * @param array Array of Comparables to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val Comparable value to count frequency of
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static <T extends Comparable<T>> int freqComparable(@NotNull final T[] array, final int fromIndex, final int toIndex, @Nullable final T val) {
        if (val == null) {
            return freqIdentity(array, fromIndex, toIndex, null);
        }
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (val.compareTo(array[i]) == 0) {
                count++;
            }
        }
        return count;
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param val Object value to count frequency of
     * @param comparator Comparator for comparing objects
     * @return frequency of <var>val</var> in <var>array</var>
     */
    public static <T> int freqComparator(@NotNull final T[] array, @NotNull final T val, @NotNull final Comparator<? super T> comparator) {
        return freqComparator(array, 0, array.length, val, comparator);
    }

    /** Count the frequency of a specific Object in an unsorted array of Objects.
     * @param array Array of Objects to count in
     * @param fromIndex Start index to shuffle at (inclusive)
     * @param toIndex End index to shuffle at (exclusive)
     * @param val Object value to count frequency of
     * @param comparator Comparator for comparing objects
     * @return frequency of <var>val</var> in <var>array</var> between <var>fromIndex</var> and <var>toIndex</var>
     */
    public static <T> int freqComparator(@NotNull final T[] array, final int fromIndex, final int toIndex, @NotNull final T val, @NotNull final Comparator<? super T> comparator) {
        int count = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            if (comparator.compare(array[i], val) == 0) {
                count++;
            }
        }
        return count;
    }
}
