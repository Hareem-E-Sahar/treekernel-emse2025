package com.rhythm.commons.collections;

import static com.rhythm.base.Preconditions.assertNotNull;
import static com.rhythm.base.Preconditions.assertArgument;
import com.rhythm.base.Nulls;
import com.rhythm.base.Numbers;
import com.rhythm.base.Strings;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author Michael J. Lee
 */
public class Arrays {

    public static final int INDEX_NOT_FOUND = -1;

    /** An empty object array. */
    public static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * Returns a new array of the given length with the specified component type.
     *
     * <pre>
     * Arrays.newArray(String.class, 10)   = new String[10)
     * Arrays.newArray(Foo.class, 0)         = new Foo[0]
     * </pre>
     *
     * @param <T>
     * @param type the component type
     * @param length the length of the new array
     * @throws IllegalArgumentException if lenght is less than zero.
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int length) {
        assertArgument(length >= 0, "Length of an array cannot be negative.");
        return (T[]) Array.newInstance(type, length);
    }

    /**
     * Returns a new array of the given length with the same type as a reference
     * array.
     *
     * @param reference any array of the desired type
     * @param length the length of the new array
     */
    public static <T> T[] newArray(T[] reference, int length) {
        Class<?> type = reference.getClass().getComponentType();
        @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(type, length);
        return result;
    }

    /**
     * Returns an empty array with the same component type as the specified array.
     *
     * @param array the array from which to infer the component type
     */
    public static <T> T[] emptyArray(T[] array) {
        return (array.length == 0) ? array : newArray(array, 0);
    }

    /**
     * Checks if an array of Objects is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(Object[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive longs is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(long[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive ints is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(int[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive shorts is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(short[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive chars is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(char[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive bytes is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(byte[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive doubles is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(double[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive floats is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(float[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an array of primitive booleans is empty or <code>null</code>.
     *
     * @param array  the array to test
     * @return <code>true</code> if the array is empty or <code>null</code>
     */
    public static boolean isEmpty(boolean[] array) {
        if (array == null || array.length == 0) {
            return true;
        }
        return false;
    }

    public static <T> T[] add(T[] array, T element) {
        Nulls.failIfNull(array, "Cannot add to a null array");
        T[] copy = expand(array, 1);
        copy[copy.length - 1] = element;
        return copy;
    }

    /**
     * <p>Finds the index of the given object in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param objectToFind  the object to find, may be <code>null</code>
     * @return the index of the object within the array, 
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    /**
     * <p>Finds the index of the given object in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param objectToFind  the object to find, may be <code>null</code>
     * @param startIndex  the index to start searching at
     * @return the index of the object within the array starting at the index,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given object within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param objectToFind  the object to find, may be <code>null</code>
     * @return the last index of the object within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(Object[] array, Object objectToFind) {
        return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given object in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than
     * the array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param objectToFind  the object to find, may be <code>null</code>
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the object within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i >= 0; i--) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i >= 0; i--) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the object is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param objectToFind  the object to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(long[] array, long valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(long[] array, long valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(long[] array, long valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(long[] array, long valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(int[] array, int valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(int[] array, int valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(int[] array, int valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(int[] array, int valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(short[] array, short valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(short[] array, short valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(short[] array, short valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the 
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(short[] array, short valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     * @since 2.1
     */
    public static int indexOf(char[] array, char valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     * @since 2.1
     */
    public static int indexOf(char[] array, char valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     * @since 2.1
     */
    public static int lastIndexOf(char[] array, char valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     * @since 2.1
     */
    public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     * @since 2.1
     */
    public static boolean contains(char[] array, char valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(byte[] array, byte valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(byte[] array, byte valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the 
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(byte[] array, byte valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(double[] array, double valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value within a given tolerance in the array.
     * This method will return the index of the first value which falls between the region
     * defined by valueToFind - tolerance and valueToFind + tolerance.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param tolerance tolerance of the search
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(double[] array, double valueToFind, double tolerance) {
        return indexOf(array, valueToFind, 0, tolerance);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(double[] array, double valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.
     * This method will return the index of the first value which falls between the region
     * defined by valueToFind - tolerance and valueToFind + tolerance.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @param tolerance tolerance of the search
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        double min = valueToFind - tolerance;
        double max = valueToFind + tolerance;
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(double[] array, double valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value within a given tolerance in the array.
     * This method will return the index of the last value which falls between the region
     * defined by valueToFind - tolerance and valueToFind + tolerance.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param tolerance tolerance of the search
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(double[] array, double valueToFind, double tolerance) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE, tolerance);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the 
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(double[] array, double valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.
     * This method will return the index of the last value which falls between the region
     * defined by valueToFind - tolerance and valueToFind + tolerance.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the 
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @param tolerance  search for value within plus/minus this amount
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        double min = valueToFind - tolerance;
        double max = valueToFind + tolerance;
        for (int i = startIndex; i >= 0; i--) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(double[] array, double valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if a value falling within the given tolerance is in the
     * given array.  If the array contains a value within the inclusive range 
     * defined by (value - tolerance) to (value + tolerance).</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array
     * is passed in.</p>
     *
     * @param array  the array to search
     * @param valueToFind  the value to find
     * @param tolerance  the array contains the tolerance of the search
     * @return true if value falling within tolerance is in array
     */
    public static boolean contains(double[] array, double valueToFind, double tolerance) {
        return indexOf(array, valueToFind, 0, tolerance) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(float[] array, float valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(float[] array, float valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(float[] array, float valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than the 
     * array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(float[] array, float valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the index of the given value in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int indexOf(boolean[] array, boolean valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    /**
     * <p>Finds the index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} (<code>-1</code>).</p>
     * 
     * @param array  the array to search through for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code>
     *  array input
     */
    public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Finds the last index of the given value within the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) if 
     * <code>null</code> array input.</p>
     * 
     * @param array  the array to travers backwords looking for the object, may be <code>null</code>
     * @param valueToFind  the object to find
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(boolean[] array, boolean valueToFind) {
        return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    /**
     * <p>Finds the last index of the given value in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} (<code>-1</code>) for a <code>null</code> input array.</p>
     *
     * <p>A negative startIndex will return {@link #INDEX_NOT_FOUND} (<code>-1</code>). A startIndex larger than 
     * the array length will search from the end of the array.</p>
     * 
     * @param array  the array to traverse for looking for the object, may be <code>null</code>
     * @param valueToFind  the value to find
     * @param startIndex  the start index to travers backwards from
     * @return the last index of the value within the array,
     *  {@link #INDEX_NOT_FOUND} (<code>-1</code>) if not found or <code>null</code> array input
     */
    public static int lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) {
        if (Arrays.isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; i--) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * <p>Checks if the value is in the given array.</p>
     *
     * <p>The method returns <code>false</code> if a <code>null</code> array is passed in.</p>
     * 
     * @param array  the array to search through
     * @param valueToFind  the value to find
     * @return <code>true</code> if the array contains the object
     */
    public static boolean contains(boolean[] array, boolean valueToFind) {
        return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
    }

    /**
     * Returns a copy of the given array of size 1 greater than the argument. 
     * The last value of the array is left to the default value.
     * 
     * @param <T> the type
     * @param array The array to copy, must not be <code>null</code>.
     * @param amount The amount to expend the array
     * @return A new copy of the array of size 1 greater than the input.
     */
    public static <T> T[] expand(T[] array, int amount) {
        Nulls.failIfNull(array, "Cannot expand a null array");
        int arrayLength = Array.getLength(array);
        @SuppressWarnings("unchecked") T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), arrayLength + amount);
        System.arraycopy(array, 0, newArray, 0, arrayLength);
        return newArray;
    }

    /**
     * Generifies the elements
     * @param <T>
     * @param elements
     * @return 
     */
    public static <T> T[] of(T... elements) {
        return elements;
    }

    /**
     * Returns if the given index is the last item in the given array. 
     * Throws a <code>NullPointerException</code> if the given array is
     * null.
     * 
     * @param i a valid index in the array
     * @param array array
     * @return True if the given index is the last item in the given 
     * array, otherwise false.
     * 
     * @throws NullPointerException if array is null
     */
    public static <T> boolean isLastItem(int i, T[] array) {
        Nulls.failIfNull(array, "Cannot check the size of a null array.");
        if (i == (array.length - 1)) {
            return true;
        }
        return false;
    }

    /**
     * Returns if the given index is the last item in the given array. 
     * Throws a <code>NullPointerException</code> if the given array is
     * null.
     * 
    
     * @param obj 
     * @param array array
     * @return True if the given index is the last item in the given 
     * array, otherwise false.
     * 
     * @throws NullPointerException if array is null
     */
    public static boolean isLastItem(Object obj, Object[] array) {
        Nulls.failIfNull(array, "Cannot check the size of a null array.");
        int index = Arrays.indexOf(array, obj);
        return ((index == INDEX_NOT_FOUND) ? false : isLastItem(index, array));
    }

    /**
     * Returns a <code>String</code> that contains all the given array
     * values.
     * <pre>
     * toString({"mon","tue","wed"})    = "montuewed"
     * toString(null)                   = "null"
     * </pre>
     * @param strs nullable String array
     * @return
     */
    public static String toString(String[] strs) {
        if (strs == null) {
            return Strings.valueOf(strs);
        }
        StringBuilder buffer = new StringBuilder(strs.length);
        for (String string : strs) {
            buffer.append(string);
        }
        return buffer.toString();
    }

    /**
     * Returns a <code>String</code> that contains all the given array
     * values.
     * <pre>
     * toString({"mon","tue","wed"}, ",")    = "mon,tue,wed"
     * toString(null)                   = "null"
     * </pre>
     * @param strs nullable String array
     * @param delimiter 
     * @return
     */
    public static String toString(String[] strs, String delimiter) {
        if (strs == null) {
            return Strings.valueOf(strs);
        }
        StringBuilder buffer = new StringBuilder(strs.length);
        String str = null;
        for (int i = 0; i < strs.length; i++) {
            str = strs[i];
            buffer.append(str);
            if (!Arrays.isLastItem(i, strs)) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a fixed-size list backed by the specified array.  (Changes to
     * the returned list "write through" to the array.)  This method acts
     * as bridge between array-based and collection-based APIs, in
     * combination with {@link Collection#toArray}.  The returned list is
     * serializable and implements {@link RandomAccess}.
     *
     * <p>This method also provides a convenient way to create a fixed-size
     * list initialized to contain several elements:
     * <pre>
     *     List&lt;String&gt; stooges = Arrays.asList("Larry", "Moe", "Curly");
     * </pre>
     *
     * @return a list view of the specified array
     */
    public static <T> List<T> toList(T... array) {
        return java.util.Arrays.asList(array);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param <T>
     * @param array nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized <T> T random(T[] array) {
        return ((isEmpty(array)) ? null : array[Numbers.random(0, array.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized int random(int[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized char random(char[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized boolean random(boolean[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized long random(long[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized byte random(byte[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized float random(float[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized short random(short[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Returns a random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @param ref nullable
     * @return A random entry in the given array.  If the given array is {@code null} or empty then
     * a {@code null} will be returned
     * @see Numbers.random(min, max)
     */
    public static synchronized double random(double[] ref) {
        return ((isEmpty(ref)) ? null : ref[Numbers.random(0, ref.length - 1)]);
    }

    /**
     * Concatenates to arrays into a new instance.
     *
     * @param <T>
     * @param a non-nullable
     * @param b non-nullable
     * @return
     */
    public static synchronized <T> T[] concat(T[] a, T[] b) {
        assertNotNull(a, "Cannot concatenate a null array reference");
        assertNotNull(b, "Cannot concatenate a null array reference");
        final int alen = a.length;
        final int blen = b.length;
        if (alen == 0) {
            return b;
        }
        if (blen == 0) {
            return a;
        }
        final T[] result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), alen + blen);
        System.arraycopy(a, 0, result, 0, alen);
        System.arraycopy(b, 0, result, alen, blen);
        return result;
    }

    public static synchronized char[] concat(char[] a, char[] b) {
        assertNotNull(a, "Cannot concatenate a null array reference");
        assertNotNull(b, "Cannot concatenate a null array reference");
        final int alen = a.length;
        final int blen = b.length;
        if (alen == 0) {
            return b;
        }
        if (blen == 0) {
            return a;
        }
        final char[] result = new char[alen + blen];
        System.arraycopy(a, 0, result, 0, alen);
        System.arraycopy(b, 0, result, alen, blen);
        return result;
    }

    public static synchronized int[] concat(int[] a, int[] b) {
        assertNotNull(a, "Cannot concatenate a null array reference");
        assertNotNull(b, "Cannot concatenate a null array reference");
        final int alen = a.length;
        final int blen = b.length;
        if (alen == 0) {
            return b;
        }
        if (blen == 0) {
            return a;
        }
        final int[] result = new int[alen + blen];
        System.arraycopy(a, 0, result, 0, alen);
        System.arraycopy(b, 0, result, alen, blen);
        return result;
    }

    /**
     * Copies an array using the {@code System.arraycopy(ref, 0, copy, 0, ref.lenght}
     * @param <T>
     * @param ref
     * @return
     * @see System.arraycopy();
     */
    public static synchronized <T extends Object> T[] copy(T[] ref) {
        if (ref == null) {
            return null;
        }
        T[] copy = Arrays.newArray(ref, ref.length);
        System.arraycopy(ref, 0, copy, 0, ref.length);
        return copy;
    }

    /**
     * Creates an new {@code Iterator} from a given array
     * @param <T>
     * @param ref not-nullable
     * @return
     * @throws NullPointerException if ref is null
     */
    @SuppressWarnings("unchecked")
    public static synchronized <T> Iterator<T> toIterator(final T[] ref) {
        assertNotNull(ref, "null array not allowed");
        return new Iterator() {

            private T[] ary = ref;

            private int position = 0;

            public boolean hasNext() {
                return (position < ary.length);
            }

            public T next() {
                if (hasNext()) {
                    return ary[position++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Creates an new {@code Iterator} from a given array
     * @param <T>
     * @param ref not-nullable
     * @return
     * @throws NullPointerException if ref is null
     */
    @SuppressWarnings("unchecked")
    public static synchronized Iterator toIterator(final int[] ref) {
        assertNotNull(ref, "null array not allowed");
        return new Iterator() {

            private int[] ary = ref;

            private int position = 0;

            public boolean hasNext() {
                return (position < ary.length);
            }

            public Object next() {
                if (hasNext()) {
                    return ary[position++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Inserts the order of the given {@code array} into a new {@code array}
     * @param <T>
     * @param ref
     * @return
     */
    public static synchronized <T> T[] invert(T[] ref) {
        assertNotNull("Cannot invert a null array");
        T[] copy = newArray(ref, ref.length);
        int index = 0;
        for (int i = ref.length; i > 0; i--) {
            copy[index++] = ref[i - 1];
        }
        return copy;
    }

    private Arrays() {
    }
}
