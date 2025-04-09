package org.xith3d.loaders.models.impl.dae.misc;

import java.lang.reflect.Array;
import java.util.Arrays;

/*********************************************************************
     * Array manipulation.
     *
     * @version
     *   $Id: ArrayLib.java 851 2006-11-27 21:23:55 +0000 (Mo, 27 Nov 2006) Qudus $
     * @since
     *   2001-04-06
     * @author
     *   <a href="http://www.CroftSoft.com/">David Wallace Croft</a>
     *********************************************************************/
public final class ArrayLib {

    public static void main(String[] args) {
        System.out.println(test(args));
    }

    public static boolean test(String[] args) {
        try {
            insert(new int[] {}, 0, 0);
            @SuppressWarnings("unused") String[] stringArray = (String[]) append(new String[] {}, "");
            stringArray = (String[]) insert(new String[] {}, "", 0);
            stringArray = (String[]) remove(new String[] { "" }, 0);
            if (!testConvertFrom1DTo2D(args)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean testConvertFrom1DTo2D(String[] args) {
        try {
            final Object array1D = new int[] { 0, 1, 2, 3, 4, 5 };
            final int[][] array2D = (int[][]) convertFrom1DTo2D(array1D, 3, 2, 0);
            return (array2D.length == 2) && Arrays.equals(array2D[0], new int[] { 0, 1, 2 }) && Arrays.equals(array2D[1], new int[] { 3, 4, 5 });
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /*********************************************************************
     * Appends an Object to an Object array.
     *
     * <p>
     * Example:
     * <code>
     * <pre>
     * String [ ]  stringArray
     *   = ( String [ ] ) ArrayLib.append ( new String [ ] { }, "" );
     * </pre>
     * </code>
     * </p>
     *
     * @throws NullArgumentException
     *
     *   If either argument is null.
     *
     * @return a new array with the same component type as the old array.
     *********************************************************************/
    public static Object[] append(Object[] oldArray, Object o) {
        NullArgumentException.check(oldArray);
        NullArgumentException.check(o);
        Object[] newArray = (Object[]) Array.newInstance(oldArray.getClass().getComponentType(), oldArray.length + 1);
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        newArray[oldArray.length] = o;
        return newArray;
    }

    /*********************************************************************
     * Appends an integer to an integer array.
     *
     * @param  intArray
     *
     *   May be null.
     *********************************************************************/
    public static int[] append(int[] intArray, int i) {
        if (intArray == null) {
            return new int[] { i };
        }
        int intArrayLength = intArray.length;
        int[] newIntArray = new int[intArrayLength + 1];
        System.arraycopy(intArray, 0, newIntArray, 0, intArrayLength);
        newIntArray[intArrayLength] = i;
        return newIntArray;
    }

    /*********************************************************************
     * Converts data stored in a 1-dimensional array to a 2D array.
     * 
     * Example:
     * Two (x,y,z) points (0,1,2) and (3,4,5) stored in a 1D array as
     * {0,1,2,3,4,5} converted to {{0,1,2),{3,4,5}} using a stride of 3,
     * a count of 2, and an offset of 0.
     *********************************************************************/
    public static Object convertFrom1DTo2D(Object array1D, int stride, int count, int offset) {
        final Object array2D = Array.newInstance(array1D.getClass(), count);
        for (int i = 0; i < count; i++) {
            final Object array = Array.newInstance(array1D.getClass().getComponentType(), stride);
            for (int j = 0; j < stride; j++) {
                final Object value = Array.get(array1D, offset + i * stride + j);
                Array.set(array, j, value);
            }
            Array.set(array2D, i, array);
        }
        return array2D;
    }

    /*********************************************************************
     * Compares two object arrays for equivalency.
     *
     * <p>
     * A Java 1.1 version of the Java 1.2 method java.util.Arrays.equals().
     * </p>
     *********************************************************************/
    public static boolean equals(Object[] objectArray1, Object[] objectArray2) {
        if (objectArray1 == null) {
            return objectArray2 == null;
        } else if (objectArray2 == null) {
            return false;
        }
        if (objectArray1.length != objectArray2.length) {
            return false;
        }
        for (int i = 0; i < objectArray1.length; i++) {
            Object element1 = objectArray1[i];
            Object element2 = objectArray2[i];
            if (element1 == null) {
                if (element2 != null) {
                    return false;
                }
            } else if (!element1.equals(element2)) {
                return false;
            }
        }
        return true;
    }

    /*********************************************************************
     * Inserts an integer into an integer array at the index position.
     *********************************************************************/
    public static int[] insert(int[] intArray, int i, int index) {
        NullArgumentException.check(intArray);
        if ((index < 0) || (index > intArray.length)) {
            throw new IllegalArgumentException("index out of range:  " + index);
        }
        int intArrayLength = intArray.length;
        int[] newIntArray = new int[intArrayLength + 1];
        System.arraycopy(intArray, 0, newIntArray, 0, index);
        newIntArray[index] = i;
        System.arraycopy(intArray, index, newIntArray, index + 1, intArrayLength - index);
        return newIntArray;
    }

    /*********************************************************************
     * Inserts an Object into an Object array at the index position.
     *
     * <p>
     * Example:
     * <code>
     * <pre>
     * String [ ]  stringArray
     *   = ( String [ ] ) ArrayLib.insert ( new String [ ] { }, "", 0 );
     * </pre>
     * </code>
     * </p>
     *
     * @throws NullArgumentException
     *
     *   If objectArray or o is null.
     *
     * @throws IndexOutOfBoundsException
     *
     *   If index < 0 or index > objectArray.length.
     *
     * @return a new array with the same component type as the old array.
     *********************************************************************/
    public static Object[] insert(Object[] objectArray, Object o, int index) {
        NullArgumentException.check(objectArray);
        NullArgumentException.check(o);
        if ((index < 0) || (index > objectArray.length)) {
            throw new IndexOutOfBoundsException("index out of range:  " + index);
        }
        Object[] newObjectArray = (Object[]) Array.newInstance(objectArray.getClass().getComponentType(), objectArray.length + 1);
        System.arraycopy(objectArray, 0, newObjectArray, 0, index);
        newObjectArray[index] = o;
        System.arraycopy(objectArray, index, newObjectArray, index + 1, objectArray.length - index);
        return newObjectArray;
    }

    /*********************************************************************
     * Prints each array element to the standard output.
     *********************************************************************/
    public static void println(Object[] objectArray) {
        for (int i = 0; i < objectArray.length; i++) {
            System.out.println(objectArray[i]);
        }
    }

    /*********************************************************************
     * Removes an Object from an Object array.
     *
     * <p>
     * Example:
     * <code>
     * <pre>
     * String [ ]  stringArray
     *   = ( String [ ] ) remove ( new String [ ] { "" }, 0 );
     * </pre>
     * </code>
     * </p>
     *
     * @throws NullArgumentException
     *
     *   If oldArray is null.
     *
     * @throws ArrayIndexOutOfBoundsException
     *
     *   If index < 0 or index >= oldArray.length.
     *
     * @return a new array with the same component type as the old array.
     *********************************************************************/
    public static Object[] remove(Object[] oldArray, int index) {
        NullArgumentException.check(oldArray);
        if ((index < 0) || (index >= oldArray.length)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        Object[] newArray = (Object[]) Array.newInstance(oldArray.getClass().getComponentType(), oldArray.length - 1);
        System.arraycopy(oldArray, 0, newArray, 0, index);
        System.arraycopy(oldArray, index + 1, newArray, index, newArray.length - index);
        return newArray;
    }

    /*********************************************************************
     * Creates a new subarray from a larger array.
     *
     * <p>
     * To avoid unnecessary object creation, this method returns the
     * original array argument if the requested subarray length is the same
     * and the startIndex is 0.  That is to say, if the method arguments
     * are such that the algorithm would have created a shallow clone, the
     * original array is returned instead.
     * </p>
     *
     * @throws NullArgumentException
     *
     *   If objectArray is null.
     *
     * @throws ArrayIndexOutOfBoundsException
     *
     *   If startIndex, length, or startIndex + length are out of range.
     *
     * @return an array with the same component type as the old array.
     *********************************************************************/
    public static Object[] subArray(Object[] objectArray, int startIndex, int length) {
        NullArgumentException.check(objectArray);
        if ((startIndex == 0) && (length == objectArray.length)) {
            return objectArray;
        }
        Object[] newArray = (Object[]) Array.newInstance(objectArray.getClass().getComponentType(), length);
        System.arraycopy(objectArray, startIndex, newArray, 0, length);
        return newArray;
    }

    /*********************************************************************
     * Creates a new subarray from a larger array.
     *
     * <p>
     * <code>
     * <pre>
     * return subArray (
     *   objectArray, startIndex, objectArray.length - startIndex );
     * </pre>
     * </code>
     * </p>
     *********************************************************************/
    public static Object[] subArray(Object[] objectArray, int startIndex) {
        return subArray(objectArray, startIndex, objectArray.length - startIndex);
    }

    private ArrayLib() {
    }
}
