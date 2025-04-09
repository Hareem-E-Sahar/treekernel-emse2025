package com.hitao.codegen.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Array Util Library<br>
 * Utility methods to perform various array operations. At this point all
 * public methods are static and final.<br>
 *
 * @author zhangjun.ht
 * @created 2010-11-10
 * @version $Id: ArrayUtils.java 12 2011-02-20 10:50:23Z guest $
 */
@SuppressWarnings("rawtypes")
public final class ArrayUtils {

    private static final Logger logger_ = Logger.getLogger(ArrayUtils.class);

    /** constructor for the ArrayUtils object */
    private ArrayUtils() {
    }

    /**
   * Copies the specified array into one of the specified type.
   * 
   * @param <T> the type of array to return
   * @param argSource the array to copy
   * @param argType the type of array to return
   * @return a <code>argType</code>-typed array that is a copy of <code>argSource</code>
   */
    @SuppressWarnings("unchecked")
    public static <T> T[] copy(Object[] argSource, Class<T> argType) {
        T[] copy = (T[]) Array.newInstance(argType, argSource.length);
        System.arraycopy(argSource, 0, copy, 0, argSource.length);
        return copy;
    }

    /**
   * Inserts a new byte <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final byte[] insert(byte[] argArray, byte value, int position) {
        return (byte[]) insert(argArray, Byte.valueOf(value), position, byte.class);
    }

    /**
   * Inserts a new char <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final char[] insert(char[] argArray, char value, int position) {
        return (char[]) insert(argArray, Character.valueOf(value), position, char.class);
    }

    /**
   * Inserts a new double <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final double[] insert(double[] argArray, double value, int position) {
        return (double[]) insert(argArray, new Double(value), position, double.class);
    }

    /**
   * Inserts a new int <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final int[] insert(int[] argArray, int value, int position) {
        return (int[]) insert(argArray, Integer.valueOf(value), position, int.class);
    }

    /**
   * Inserts a new float <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final float[] insert(float[] argArray, float value, int position) {
        return (float[]) insert(argArray, new Float(value), position, float.class);
    }

    /**
   * Inserts a new long <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final long[] insert(long[] argArray, long value, int position) {
        return (long[]) insert(argArray, Long.valueOf(value), position, long.class);
    }

    /**
   * Inserts a new Object <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param <T>        type of the array elements
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] insert(T[] argArray, Object value, int position) {
        final Class clazz = argArray.getClass().getComponentType();
        return (T[]) insert(argArray, value, position, clazz);
    }

    /**
   * Inserts a new Object <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param <T>        component type of returned array
   * @param argArrayClass the class of the array contents.
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] insert(Class<T> argArrayClass, Object[] argArray, Object value, int position) {
        return (T[]) insert(argArray, value, position, argArrayClass);
    }

    /**
   * Inserts a new short <tt>value</tt> in array <tt>argArray</tt> at the
   * specified position <tt>position</tt>.  The array size is increased by one
   * and all elements after <tt>position</tt> are moved up by one.
   *
   * @param argArray      the array to insert into.
   * @param value      the value to insert.
   * @param position   the position to place the new value.
   * @return a new array that includes the new value at the specified position.
   * @throws  ArrayStoreException  if all elements in the <code>argArray</code>
   *               array are not of the same type.
   * @throws  NullPointerException if <code>argArray</code> is <code>null</code>.
   */
    public static final short[] insert(short[] argArray, short value, int position) {
        return (short[]) insert(argArray, Short.valueOf(value), position, short.class);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final byte[] remove(byte[] argArray, int position) {
        return (byte[]) remove(byte.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final char[] remove(char[] argArray, int position) {
        return (char[]) remove(char.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final double[] remove(double[] argArray, int position) {
        return (double[]) remove(double.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final float[] remove(float[] argArray, int position) {
        return (float[]) remove(float.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final int[] remove(int[] argArray, int position) {
        return (int[]) remove(int.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    public static final long[] remove(long[] argArray, int position) {
        return (long[]) remove(long.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param <T>       type of the array elements
   * @param argArray     the array to change
   * @param position  the position to delete
   * @return an array with one less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] remove(T[] argArray, int position) {
        final Class clazz = argArray.getClass().getComponentType();
        return (T[]) remove(clazz, (T) argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param <T>         type of the array elements
   * @param argArrayClass  the component type of the array to be returned
   * @param argArray       the array to change
   * @param position    the position to delete
   * @return an array with one less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] remove(Class<T> argArrayClass, T[] argArray, int position) {
        return (T[]) remove(argArrayClass, (T) argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @return an array with one less entries
   */
    public static final short[] remove(short[] argArray, int position) {
        return (short[]) remove(short.class, argArray, position, 1);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final byte[] remove(byte[] argArray, int position, int count) {
        return (byte[]) remove(byte.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final char[] remove(char[] argArray, int position, int count) {
        return (char[]) remove(char.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final double[] remove(double[] argArray, int position, int count) {
        return (double[]) remove(double.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final float[] remove(float[] argArray, int position, int count) {
        return (float[]) remove(float.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final int[] remove(int[] argArray, int position, int count) {
        return (int[]) remove(int.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final long[] remove(long[] argArray, int position, int count) {
        return (long[]) remove(long.class, argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param <T>            type of the array elements
   * @param argArray       the array to change
   * @param position       the position to delete
   * @param count          the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] remove(T[] argArray, int position, int count) {
        final Class clazz = argArray.getClass().getComponentType();
        return (T[]) remove(clazz, (T) argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param <T>            type of the array elements
   * @param argArrayClass  the component type of the array to be returned
   * @param argArray       the array to change
   * @param position       the position to delete
   * @param count          the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] remove(Class<T> argArrayClass, T[] argArray, int position, int count) {
        return (T[]) remove(argArrayClass, (T) argArray, position, count);
    }

    /**
   * Removes the item at <code>position</code> from <code>argArray</code>.
   * 
   * @param argArray     the array to change
   * @param position     the position to delete
   * @param count        the number of elements to remove
   * @return an array with <tt>count</tt> less entries
   */
    public static final short[] remove(short[] argArray, int position, int count) {
        return (short[]) remove(short.class, argArray, position, count);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final byte[] resize(byte[] argArray, int newSize) {
        return (byte[]) resize(byte.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final char[] resize(char[] argArray, int newSize) {
        return (char[]) resize(char.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final double[] resize(double[] argArray, int newSize) {
        return (double[]) resize(double.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final float[] resize(float[] argArray, int newSize) {
        return (float[]) resize(float.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final int[] resize(int[] argArray, int newSize) {
        return (int[]) resize(int.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final long[] resize(long[] argArray, int newSize) {
        return (long[]) resize(long.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    public static final short[] resize(short[] argArray, int newSize) {
        return (short[]) resize(short.class, argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param <T>       type of the array elements
   * @param argArray     the array to change
   * @param newSize   the new size for the array
   * @return an array with one less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] resize(T[] argArray, int newSize) {
        final Class clazz = argArray.getClass().getComponentType();
        return (T[]) resize(clazz, (Object) argArray, newSize);
    }

    /**
   * Changes the size of <code>argArray</code> by adding empty positions of the
   * end, or removing positions from the end.
   * 
   * @param <T>        type of the returned array
   * @param argArrayClass the component type of the array to return
   * @param argArray      the array to change
   * @param newSize    the new size for the array
   * @return an array with one less entries
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] resize(Class<T> argArrayClass, Object[] argArray, int newSize) {
        return (T[]) resize(argArrayClass, (Object) argArray, newSize);
    }

    private static final Object insert(Object argArray, Object value, int position, Class<?> argArrayClass) {
        final int oldSize = Array.getLength(argArray);
        Object newArray = Array.newInstance(argArrayClass, oldSize + 1);
        System.arraycopy(argArray, 0, newArray, 0, position);
        System.arraycopy(argArray, position, newArray, position + 1, oldSize - position);
        Array.set(newArray, position, value);
        return newArray;
    }

    private static final Object remove(Class<?> argArrayClass, Object argArray, int position, int count) {
        final int oldSize = Array.getLength(argArray);
        Object newArray = Array.newInstance(argArrayClass, oldSize - count);
        System.arraycopy(argArray, 0, newArray, 0, position);
        System.arraycopy(argArray, position + count, newArray, position, oldSize - position - count);
        return newArray;
    }

    private static final Object resize(Class<?> argArrayClass, Object argArray, int newSize) {
        final int oldSize = Array.getLength(argArray);
        Object newArray = Array.newInstance(argArrayClass, newSize);
        System.arraycopy(argArray, 0, newArray, 0, Math.min(oldSize, newSize));
        return newArray;
    }

    /**
   * Convert an <code>Object</code> array into a type-safe <code>Map</code>
   * containing the array elements as keys and values in alternating order. If
   * the array contains an odd number of elements, ignore the last element.
   * <ul>
   * <li>0: Key 1</li>
   * <li>1: Value 1</li>
   * <li>2: Key 2</li>
   * <li>3: Value 2</li>
   * <li>...</li>
   * <li>2n-2: Key <i>n</i></li>
   * <li>2n-1: Value <i>n</i></li>
   * </ul>
   * @param <K> Key type.
   * @param <V> Value type.
   * @param argKeyType <code>Class</code> instance for the key type.
   * @param argValueType <code>Class</code> instance for the value type.
   * @param argParameters <code>Object</code> array containing the parameters to
   * convert.
   * @return A <code>Map</code> containing the key/value pairs passed in. May be
   * empty but will not be <code>null</code>.
   */
    public static <K, V> Map<K, V> toMap(Class<K> argKeyType, Class<V> argValueType, Object... argParameters) {
        Map<K, V> map = new HashMap<K, V>();
        if (argParameters != null) {
            for (int i = 0; i < argParameters.length - 1; i += 2) {
                map.put(argKeyType.cast(argParameters[i]), argValueType.cast(argParameters[i + 1]));
            }
        }
        return map;
    }

    /**
   * Convert a <code>Collection</code> of <code>Object</code>s into a type-safe
   * <code>Map</code> containing the <code>Collection</code> elements as keys
   * and values in alternating order. If the <code>Collection</code> contains an
   * odd number of elements, ignore the last element.
   * <ul>
   * <li>0: Key 1</li>
   * <li>1: Value 1</li>
   * <li>2: Key 2</li>
   * <li>3: Value 2</li>
   * <li>...</li>
   * <li>2n-2: Key <i>n</i></li>
   * <li>2n-1: Value <i>n</i></li>
   * </ul>
   * @param <K> Key type.
   * @param <V> Value type.
   * @param argKeyType <code>Class</code> instance for the key type.
   * @param argValueType <code>Class</code> instance for the value type.
   * @param argParameters <code>Collection</code> of <code>Object</code>s
   * containing the parameters to convert.
   * @return A <code>Map</code> containing the key/value pairs passed in. May be
   * empty but will not be <code>null</code>.
   */
    public static <K, V> Map<K, V> toMap(Class<K> argKeyType, Class<V> argValueType, Collection<? extends Object> argParameters) {
        if (argParameters == null) {
            return new HashMap<K, V>();
        }
        return toMap(argKeyType, argValueType, argParameters.toArray());
    }

    /**
   * Convert a <code>CompositeObject.ToPiece</code> array into a type-safe
   * <code>Map</code> containing the array elements as keys and values in
   * alternating order. If the array contains an odd number of elements, ignore
   * the last element.
   * <ul>
   * <li>0: Key 1</li>
   * <li>1: Value 1</li>
   * <li>2: Key 2</li>
   * <li>3: Value 2</li>
   * <li>...</li>
   * <li>2n-2: Key <i>n</i></li>
   * <li>2n-1: Value <i>n</i></li>
   * </ul>
   * @param <K> Key type.
   * @param <V> Value type.
   * @param argParameters <code>CompositeObject.TwoPiece</code> array containing
   * the parameters to insert into the map.
   * @return A <code>Map</code> containing the key/value pairs passed in. May be
   * empty but will not be <code>null</code>.
   */
    public static <K, V> Map<K, V> toMap(CompositeObject.TwoPiece<K, V>[] argParameters) {
        Map<K, V> map = new HashMap<K, V>();
        if (argParameters != null) {
            for (CompositeObject.TwoPiece<K, V> pair : argParameters) {
                map.put(pair.a(), pair.b());
            }
        }
        return map;
    }

    /**
   * Convert a <code>Collection</code> of <code>CompositeObject.ToPiece</code>s
   * into a type-safe <code>Map</code> containing the <code>Collection</code>
   * elements as keys and values in alternating order. If the
   * <code>Collection</code> contains an odd number of elements, ignore the last
   * element.
   * <ul>
   * <li>0: Key 1</li>
   * <li>1: Value 1</li>
   * <li>2: Key 2</li>
   * <li>3: Value 2</li>
   * <li>...</li>
   * <li>2n-2: Key <i>n</i></li>
   * <li>2n-1: Value <i>n</i></li>
   * </ul>
   * @param <K> Key type.
   * @param <V> Value type.
   * @param argParameters <code>Collection</code> of
   * <code>CompositeObject.TwoPiece</code>s containing the parameters to insert
   * into the map.
   * @return A <code>Map</code> containing the key/value pairs passed in. May be
   * empty but will not be <code>null</code>.
   */
    public static <K, V> Map<K, V> toMap(Collection<CompositeObject.TwoPiece<K, V>> argParameters) {
        Map<K, V> map = new HashMap<K, V>();
        if (argParameters != null) {
            for (CompositeObject.TwoPiece<K, V> pair : argParameters) {
                map.put(pair.a(), pair.b());
            }
        }
        return map;
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argIntegers  the array to stringify
   * @return a string representation of the array
   */
    public static String toString(int[] argIntegers) {
        return toString(new StringBuffer(1024), argIntegers).toString();
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argIntegers  the array to stringify
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, int[] argIntegers) {
        if (argIntegers == null) {
            argTarget.append("null");
        } else {
            argTarget.append(ObjectUtils.getClass(argIntegers.getClass()));
            argTarget.append("{");
            if (argIntegers.length > 0) {
                for (int argInteger : argIntegers) {
                    argTarget.append(argInteger).append(",");
                }
                argTarget.setLength(argTarget.length() - 1);
            }
            argTarget.append("}");
        }
        return argTarget;
    }

    /**
   * Removed one dimension from an array by .
   * e.g. Flattens a two-dimensional array to a single dimension.
   * 
   * @param argTwoDimArray  the array to flatten
   * @return an array with one less dimension
   */
    public static Object[] toOneDimension(Object[][] argTwoDimArray) {
        if (argTwoDimArray == null) {
            return null;
        }
        Object[] src;
        int size = 0;
        for (Object[] element : argTwoDimArray) {
            src = element;
            if (src != null) {
                size += src.length;
            }
        }
        Class<?> arrayClass = argTwoDimArray.getClass().getComponentType().getComponentType();
        Object[] results = (Object[]) Array.newInstance(arrayClass, size);
        int nextElement = 0;
        int srcLength;
        for (Object[] element : argTwoDimArray) {
            src = element;
            if (src != null) {
                srcLength = src.length;
                System.arraycopy(src, 0, results, nextElement, srcLength);
                nextElement += srcLength;
            }
        }
        return results;
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argBytes   the array to stringify
   * @return a string representation of the array
   */
    public static String toString(byte[] argBytes) {
        return toString(argBytes, 10);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argBytes     the array to stringify
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, byte[] argBytes) {
        return toString(argTarget, argBytes, 10);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string builder to append to
   * @param argBytes     the array to stringify
   * @return the modified string builder
   */
    public static StringBuilder toString(StringBuilder argTarget, byte[] argBytes) {
        return toString(argTarget, argBytes, 10);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argBytes   the array to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @return a string representation of the array
   */
    public static String toString(byte[] argBytes, int argBase) {
        return toString(new StringBuilder(1024), argBytes, argBase).toString();
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argBytes   the array to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, byte[] argBytes, int argBase) {
        return toString(argTarget, argBytes, argBase, false);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string builder to append to
   * @param argBytes   the array to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @return the modified string builder
   */
    public static StringBuilder toString(StringBuilder argTarget, byte[] argBytes, int argBase) {
        return toString(argTarget, argBytes, argBase, false);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argBytes   the array to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @param argAsChar  if <code>true</code> the byte will shown as a character if less than 128
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, byte[] argBytes, int argBase, boolean argAsChar) {
        return toString(argTarget, argBytes, 0, argBytes.length, argBase, argAsChar);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string builder to append to
   * @param argBytes   the array to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @param argAsChar  if <code>true</code> the byte will shown as a character if less than 128
   * @return the modified string builder
   */
    public static StringBuilder toString(StringBuilder argTarget, byte[] argBytes, int argBase, boolean argAsChar) {
        return toString(argTarget, argBytes, 0, argBytes.length, argBase, argAsChar);
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget  the string buffer to append to
   * @param argBytes   the array to stringify
   * @param argStart   the start location to stringify
   * @param argLength  the number of bytes to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @param argAsChar  if <code>true</code> the byte will shown as a character if less than 128
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, final byte[] argBytes, final int argStart, final int argLength, final int argBase, final boolean argAsChar) {
        StringBuilder builder = new StringBuilder(argTarget.capacity());
        builder.append(argTarget);
        toString(builder, argBytes, argStart, argLength, argBase, argAsChar);
        return new StringBuffer(builder.toString());
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget  the string buffer to append to
   * @param argBytes   the array to stringify
   * @param argStart   the start location to stringify
   * @param argLength  the number of bytes to stringify
   * @param argBase    the number base used to represent the byte values (e.g. 8, 16, or 10)
   * @param argAsChar  if <code>true</code> the byte will shown as a character if less than 128
   * @return the modified string buffer
   */
    public static StringBuilder toString(StringBuilder argTarget, final byte[] argBytes, final int argStart, final int argLength, final int argBase, final boolean argAsChar) {
        if (argBytes == null) {
            argTarget.append("null");
        } else {
            argTarget.append(ObjectUtils.getClass(argBytes.getClass()));
            argTarget.append("{");
            final String numberPrefix;
            switch(argBase) {
                case 8:
                    numberPrefix = "0";
                    break;
                case 16:
                    numberPrefix = "0x";
                    break;
                default:
                    numberPrefix = "";
                    break;
            }
            if (argLength > 0) {
                int end = argStart + argLength;
                for (int i = argStart; i < end; i++) {
                    byte b = argBytes[i];
                    if (argAsChar && (32 <= b) && (b < 127)) {
                        argTarget.append("'");
                        argTarget.append((char) argBytes[i]);
                        argTarget.append("'");
                    } else if (argAsChar && (b < 32)) {
                        argTarget.append("'");
                        argTarget.append(argBytes[i]);
                        argTarget.append("'");
                    } else {
                        argTarget.append(numberPrefix);
                        argTarget.append(Integer.toString(b, argBase));
                    }
                    argTarget.append(",");
                }
                argTarget.setLength(argTarget.length() - 1);
            }
            argTarget.append("}");
        }
        return argTarget;
    }

    /**
   * Creates a string representation of an array that can will be informative
   * for logging.
   *
   * @param argObjects the array to stringify
   * @return a string representation that starts with the class name of the
   *         array, followed by an { followed by a comma seperated list of the
   *         values in the array, followed by }
   */
    public static String toString(Object[] argObjects) {
        return toString(new StringBuilder(1024), argObjects).toString();
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argObjects   the array to stringify
   * @return the modified string buffer
   */
    public static StringBuffer toString(StringBuffer argTarget, Object[] argObjects) {
        if (argObjects == null) {
            argTarget.append("null");
        } else {
            argTarget.append(ObjectUtils.getClass(argObjects.getClass())).append("{");
            if (argObjects.length > 0) {
                for (Object argObject : argObjects) {
                    argTarget.append(argObject).append(",");
                }
                argTarget.setLength(argTarget.length() - 1);
            }
            argTarget.append("}");
        }
        return argTarget;
    }

    /**
   * Creates a string representation of an array
   * 
   * @param argTarget    the string buffer to append to
   * @param argObjects   the array to stringify
   * @return the modified string buffer
   */
    public static StringBuilder toString(StringBuilder argTarget, Object[] argObjects) {
        if (argObjects == null) {
            argTarget.append("null");
        } else {
            argTarget.append(ObjectUtils.getClass(argObjects.getClass())).append("{");
            if (argObjects.length > 0) {
                for (Object argObject : argObjects) {
                    argTarget.append(argObject).append(",");
                }
                argTarget.setLength(argTarget.length() - 1);
            }
            argTarget.append("}");
        }
        return argTarget;
    }

    /**
   * Calculates a hash code for an array of <code>Object</code>s.
   * 
   * @param argArray  array of objects
   * @return  hash code
   */
    public static final int calculateHashCode(Object[] argArray) {
        if (argArray == null) {
            return 0;
        }
        int result = 17;
        for (Object element : argArray) {
            if (element != null) {
                try {
                    result = result * 37 + element.hashCode();
                } catch (Throwable ex) {
                    logger_.error("CAUGHT EXCEPTION", ex);
                }
            }
        }
        return result;
    }

    /**
   * Reverses the order of an array of objects.
   * 
   * @param <T>       type of the array elements
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] reverseOrder(T[] argArray) {
        Class clazz = argArray.getClass().getComponentType();
        return (T[]) reverseOrder(argArray, clazz);
    }

    /**
   * Reverses the order of an array of <code>byte</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final byte[] reverseOrder(byte[] argArray) {
        return (byte[]) reverseOrder(argArray, byte.class);
    }

    /**
   * Reverses the order of an array of <code>char</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final char[] reverseOrder(char[] argArray) {
        return (char[]) reverseOrder(argArray, char.class);
    }

    /**
   * Reverses the order of an array of <code>double</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final double[] reverseOrder(double[] argArray) {
        return (double[]) reverseOrder(argArray, double.class);
    }

    /**
   * Reverses the order of an array of <code>float</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final float[] reverseOrder(float[] argArray) {
        return (float[]) reverseOrder(argArray, float.class);
    }

    /**
   * Reverses the order of an array of <code>int</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final int[] reverseOrder(int[] argArray) {
        return (int[]) reverseOrder(argArray, int.class);
    }

    /**
   * Reverses the order of an array of <code>long</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final long[] reverseOrder(long[] argArray) {
        return (long[]) reverseOrder(argArray, long.class);
    }

    /**
   * Reverses the order of an array of <code>short</code> elements.
   * 
   * @param argArray  the array to reverse
   * @return the reversed array
   */
    public static final short[] reverseOrder(short[] argArray) {
        return (short[]) reverseOrder(argArray, short.class);
    }

    /**
   * Reverses the order of an array.
   * 
   * @param argArray       the array to reverse
   * @param argArrayClass  the component class of the array to be returned
   * @return the reversed array
   */
    public static final Object reverseOrder(Object argArray, Class<?> argArrayClass) {
        int length = Array.getLength(argArray);
        Object newArray = Array.newInstance(argArrayClass, length);
        int newIndex = 0;
        for (int oldIndex = length - 1; oldIndex >= 0; oldIndex--) {
            System.arraycopy(argArray, oldIndex, newArray, newIndex++, 1);
        }
        return newArray;
    }

    /**
   * Combines two arrays into a single array.  The resulting array will have
   * the same component type as the the first array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param <T>        type of the array elements
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   * @throws ArrayStoreException    if an element in the second array cannot be
   *    stored in an array of the type of the first array
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] combine(T[] argArray1, T[] argArray2) {
        final Class clazz = argArray1.getClass().getComponentType();
        return (T[]) combine(clazz, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The resulting array will have
   * the same component type as the the first array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param <T>        type of the array elements
   * @param argArrayClass the component type of the array to return
   * @param argArray1     the first array to combine
   * @param argArray2     the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   * @throws ArrayStoreException    if an element in the second array cannot be
   *    stored in an array of the type of the first array
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] combine(Class<T> argArrayClass, T[] argArray1, T[] argArray2) {
        return (T[]) combine(argArrayClass, (Object) argArray1, (Object) argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final byte[] combine(byte[] argArray1, byte[] argArray2) {
        return (byte[]) combine(byte.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final char[] combine(char[] argArray1, char[] argArray2) {
        return (char[]) combine(int.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final double[] combine(double[] argArray1, double[] argArray2) {
        return (double[]) combine(double.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final float[] combine(float[] argArray1, float[] argArray2) {
        return (float[]) combine(float.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final int[] combine(int[] argArray1, int[] argArray2) {
        return (int[]) combine(int.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final long[] combine(long[] argArray1, long[] argArray2) {
        return (long[]) combine(long.class, argArray1, argArray2);
    }

    /**
   * Combines two arrays into a single array.  The arrays will be combined
   * in order, with all elements of the first array followed by all elements of
   * the second array.
   * 
   * @param argArray1   the first array to combine
   * @param argArray2   the second array to combine
   * @return an array with the elements from argArray1 followed by the elements from argArray2
   * @throws NullPointerException  if either array is <code>null</code>
   */
    public static final short[] combine(short[] argArray1, short[] argArray2) {
        return (short[]) combine(short.class, argArray1, argArray2);
    }

    private static final Object combine(Class<?> argArrayClass, Object argArray1, Object argArray2) {
        final int size1 = Array.getLength(argArray1);
        final int size2 = Array.getLength(argArray2);
        Object newArray = Array.newInstance(argArrayClass, size1 + size2);
        System.arraycopy(argArray1, 0, newArray, 0, size1);
        System.arraycopy(argArray2, 0, newArray, size1, size2);
        return newArray;
    }

    /**
   * Wrap the source object in an array if it is not already an array.
   *
   * @param argSource the source object to wrap.
   * @return A new array containing the source object, or the source object if
   * it is an array.
   */
    public static final Object[] wrap(Object argSource) {
        if (argSource instanceof Object[]) {
            return (Object[]) argSource;
        }
        return new Object[] { argSource };
    }

    /**
   * Extracts a section of an array and returns a new array.
   *
   * @param <T>        type of the array elements
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] slice(T[] argArray, int argStart, int argEnd) {
        final Class clazz = argArray.getClass().getComponentType();
        return (T[]) slice(clazz, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param <T>        type of the array elements
   * @param argArrayClass the component type of the array to return
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    @SuppressWarnings("unchecked")
    public static final <T> T[] slice(Class<T> argArrayClass, T[] argArray, int argStart, int argEnd) {
        return (T[]) slice(argArrayClass, (Object) argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final byte[] slice(byte[] argArray, int argStart, int argEnd) {
        return (byte[]) slice(byte.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final char[] slice(char[] argArray, int argStart, int argEnd) {
        return (char[]) slice(int.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final double[] slice(double[] argArray, int argStart, int argEnd) {
        return (double[]) slice(double.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final float[] slice(float[] argArray, int argStart, int argEnd) {
        return (float[]) slice(float.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final int[] slice(int[] argArray, int argStart, int argEnd) {
        return (int[]) slice(int.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final long[] slice(long[] argArray, int argStart, int argEnd) {
        return (long[]) slice(long.class, argArray, argStart, argEnd);
    }

    /**
   * Extracts a section of an array and returns a new array.
   * 
   * @param argArray   The array to slice.
   * @param argStart   The zero-based index at which to begin extraction.
   * @param argEnd     The zero-based index at which to end extraction.
   *        As a negative index, endSlice indicates an offset from the end of the array.<br>
   * @return <code>null</code> if <code>argArray</code> is <code>null</code>. or the new array otherwise
   */
    public static final short[] slice(short[] argArray, int argStart, int argEnd) {
        return (short[]) slice(short.class, argArray, argStart, argEnd);
    }

    private static final Object slice(Class<?> argArrayClass, Object argArray, int argStart, int argEnd) {
        if (argArray == null) {
            return null;
        }
        final int origSize = Array.getLength(argArray);
        if (origSize == 0) {
            return argArray;
        }
        if (origSize < argStart) {
            return Array.newInstance(argArrayClass, 0);
        }
        if (argStart < 0) {
            argStart = origSize + argStart;
        }
        if (argEnd < 0) {
            argEnd = origSize + argEnd;
        }
        if (argEnd > origSize) {
            argEnd = origSize;
        }
        int newSize = argEnd - argStart;
        Object newArray = Array.newInstance(argArrayClass, newSize);
        System.arraycopy(argArray, argStart, newArray, 0, newSize);
        return newArray;
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(byte[] array1, int offset1, byte[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(char[] array1, int offset1, char[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(double[] array1, int offset1, double[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(float[] array1, int offset1, float[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(int[] array1, int offset1, int[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(long[] array1, int offset1, long[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   *
   * @param   array1       the first array
   * @param   offset1      the starting offset of the subset in <tt>array1</tt>
   * @param   array2       the second array
   * @param   offset2      the starting offset of the subset in <tt>array2</tt>
   * @param   len          the number of elements to compare.
   * @return  <code>true</code> if the specified subset of the first array
   *          matches the specified subset of the second array;
   *          <code>false</code> otherwise.
   */
    public static final boolean regionMatches(short[] array1, int offset1, short[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    /**
   * Tests if two array regions are equal.
   * <p>
   * A subset of <tt>array1</tt> is compared to a subset of the <tt>array2</tt>.
   * The result is <tt>true</tt> if these subsets are the same. The subset of
   * <tt>array1</tt> to be compared begins at index <tt>offset1</tt> and has
   * length <tt>len</tt>. The subset of <tt>array2</tt> to be compared begins
   * at index <tt>offset2</tt> and has length <tt>len</tt>. The result
   * is <tt>false</tt> if and only if at least one of the following is true:
   * <ul><li><tt>offset1</tt> is negative.
   * <li><tt>offset2</tt> is negative.
   * <li><tt>offset1+len</tt> is greater than the length of <tt>array1</tt>.
   * <li><tt>offset2+len</tt> is greater than the length of <tt>array2</tt>.
   * <li>there is some nonnegative integer <i>k</i> less than <tt>len</tt> such that:
   * <blockquote><pre>
   * array1[offset1+k] != array2[ooffset+k]
   * </pre></blockquote>
   * </ul>
   * 
   * @param <T>      type of the array elements
   * @param array1   the first array
   * @param offset1  the starting offset of the subset in <tt>array1</tt>
   * @param array2   the second array
   * @param offset2  the starting offset of the subset in <tt>array2</tt>
   * @param len      the number of elements to compare.
   * @return <code>true</code> if the specified subset of the first array
   *     matches the specified subset of the second array;
   *     <code>false</code> otherwise.
   */
    public static final <T> boolean regionMatches(T[] array1, int offset1, T[] array2, int offset2, int len) {
        return regionMatchesImpl(array1, offset1, array2, offset2, len);
    }

    private static final boolean regionMatchesImpl(Object array1, int offset1, Object array2, int offset2, int len) {
        long len1 = Array.getLength(array1);
        long len2 = Array.getLength(array2);
        if ((offset2 < 0) || (offset1 < 0) || (offset1 > len1 - len) || (offset2 > len2 - len)) {
            return false;
        }
        while (len-- > 0) {
            Object b1 = Array.get(array1, offset1++);
            Object b2 = Array.get(array2, offset2++);
            if (!eq(b1, b2)) {
                return false;
            }
        }
        return true;
    }

    private static final boolean eq(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        }
        return o1.equals(o2);
    }
}
