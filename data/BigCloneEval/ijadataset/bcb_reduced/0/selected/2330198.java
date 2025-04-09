package eu.pisolutions.lang;

import java.lang.reflect.Array;

/**
 * Utilities to work with arrays.
 * <p>
 * This class cannot be instantiated.
 * </p>
 *
 * @author Laurent Pireyn
 */
public final class Arrays extends Object {

    public static final boolean[] EMPTY_BOOLEAN_ARRAY = {};

    public static final byte[] EMPTY_BYTE_ARRAY = {};

    public static final short[] EMPTY_SHORT_ARRAY = {};

    public static final char[] EMPTY_CHAR_ARRAY = {};

    public static final int[] EMPTY_INT_ARRAY = {};

    public static final long[] EMPTY_LONG_ARRAY = {};

    public static final float[] EMPTY_FLOAT_ARRAY = {};

    public static final double[] EMPTY_DOUBLE_ARRAY = {};

    public static final Object[] EMPTY_OBJECT_ARRAY = {};

    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = {};

    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = {};

    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = {};

    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = {};

    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = {};

    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = {};

    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = {};

    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = {};

    public static final String[] EMPTY_STRING_ARRAY = {};

    public static int length(boolean[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(byte[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(short[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(char[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(int[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(long[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(float[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(double[] array) {
        return array != null ? array.length : 0;
    }

    public static int length(Object[] array) {
        return array != null ? array.length : 0;
    }

    public static boolean isEmpty(boolean[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(short[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(char[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(int[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(long[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(float[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(double[] array) {
        return Arrays.length(array) == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return Arrays.length(array) == 0;
    }

    public static int indexOf(boolean[] array, boolean value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(boolean[] array, boolean value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(byte[] array, byte value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(byte[] array, byte value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(short[] array, short value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(short[] array, short value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(char[] array, char value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(char[] array, char value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(int[] array, int value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(int[] array, int value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(long[] array, long value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(long[] array, long value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(float[] array, float value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(float[] array, float value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(double[] array, double value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(double[] array, double value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (array[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOf(Object[] array, Object value) {
        return Arrays.indexOf(array, value, 0);
    }

    public static int indexOf(Object[] array, Object value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (Objects.equals(array[i], value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int indexOfIgnoreCase(String[] array, String value) {
        return Arrays.indexOfIgnoreCase(array, value, 0);
    }

    public static int indexOfIgnoreCase(String[] array, String value, int start) {
        Arrays.validateIndex(start);
        if (array != null) {
            for (int i = start; i < array.length; ++i) {
                if (Strings.equalsIgnoreCase(array[i], value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> componentType) {
        return Arrays.createArray(componentType, 0);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(Class<T> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
    }

    public static boolean[] defaultIfNull(boolean[] array, boolean[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static byte[] defaultIfNull(byte[] array, byte[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static short[] defaultIfNull(short[] array, short[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static char[] defaultIfNull(char[] array, char[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static int[] defaultIfNull(int[] array, int[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static long[] defaultIfNull(long[] array, long[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static float[] defaultIfNull(float[] array, float[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static double[] defaultIfNull(double[] array, double[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static <T> T[] defaultIfNull(T[] array, T[] defaultArray) {
        return array != null ? array : defaultArray;
    }

    public static boolean[] emptyIfNull(boolean[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_BOOLEAN_ARRAY);
    }

    public static byte[] emptyIfNull(byte[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_BYTE_ARRAY);
    }

    public static short[] emptyIfNull(short[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_SHORT_ARRAY);
    }

    public static char[] emptyIfNull(char[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_CHAR_ARRAY);
    }

    public static int[] emptyIfNull(int[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_INT_ARRAY);
    }

    public static long[] emptyIfNull(long[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_LONG_ARRAY);
    }

    public static float[] emptyIfNull(float[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_FLOAT_ARRAY);
    }

    public static double[] emptyIfNull(double[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_DOUBLE_ARRAY);
    }

    public static Boolean[] emptyIfNull(Boolean[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    public static Byte[] emptyIfNull(Byte[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_BYTE_OBJECT_ARRAY);
    }

    public static Short[] emptyIfNull(Short[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_SHORT_OBJECT_ARRAY);
    }

    public static Character[] emptyIfNull(Character[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_CHARACTER_OBJECT_ARRAY);
    }

    public static Integer[] emptyIfNull(Integer[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_INTEGER_OBJECT_ARRAY);
    }

    public static Long[] emptyIfNull(Long[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_LONG_OBJECT_ARRAY);
    }

    public static Float[] emptyIfNull(Float[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_FLOAT_OBJECT_ARRAY);
    }

    public static Double[] emptyIfNull(Double[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_DOUBLE_OBJECT_ARRAY);
    }

    public static String[] emptyIfNull(String[] array) {
        return Arrays.defaultIfNull(array, Arrays.EMPTY_STRING_ARRAY);
    }

    public static <T> T[] emptyIfNull(T[] array, Class<T> componentType) {
        return Arrays.defaultIfNull(array, Arrays.emptyArray(componentType));
    }

    public static boolean[] defaultIfEmpty(boolean[] array, boolean[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static byte[] defaultIfEmpty(byte[] array, byte[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static short[] defaultIfEmpty(short[] array, short[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static char[] defaultIfEmpty(char[] array, char[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static int[] defaultIfEmpty(int[] array, int[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static long[] defaultIfEmpty(long[] array, long[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static float[] defaultIfEmpty(float[] array, float[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static double[] defaultIfEmpty(double[] array, double[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static <T> T[] defaultIfEmpty(T[] array, T[] defaultArray) {
        return Arrays.isEmpty(array) ? array : defaultArray;
    }

    public static boolean[] toBooleanArray(Boolean[] array) {
        return Arrays.toBooleanArray(array, false);
    }

    public static boolean[] toBooleanArray(Boolean[] array, boolean valueForNull) {
        if (array == null) {
            return null;
        }
        final boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Boolean[] toBooleanObjectArray(boolean[] array) {
        if (array == null) {
            return null;
        }
        final Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static byte[] toByteArray(Byte[] array) {
        return Arrays.toByteArray(array, (byte) 0);
    }

    public static byte[] toByteArray(Byte[] array, byte valueForNull) {
        if (array == null) {
            return null;
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Byte[] toByteObjectArray(byte[] array) {
        if (array == null) {
            return null;
        }
        final Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static short[] toShortArray(Short[] array) {
        return Arrays.toShortArray(array, (short) 0);
    }

    public static short[] toShortArray(Short[] array, short valueForNull) {
        if (array == null) {
            return null;
        }
        final short[] result = new short[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Short[] toShortObjectArray(short[] array) {
        if (array == null) {
            return null;
        }
        final Short[] result = new Short[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static char[] toCharacterArray(Character[] array) {
        return Arrays.toCharacterArray(array, (char) 0);
    }

    public static char[] toCharacterArray(Character[] array, char valueForNull) {
        if (array == null) {
            return null;
        }
        final char[] result = new char[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Character[] toCharacterObjectArray(char[] array) {
        if (array == null) {
            return null;
        }
        final Character[] result = new Character[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static int[] toIntegerArray(Integer[] array) {
        return Arrays.toIntegerArray(array, 0);
    }

    public static int[] toIntegerArray(Integer[] array, int valueForNull) {
        if (array == null) {
            return null;
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Integer[] toIntegerObjectArray(int[] array) {
        if (array == null) {
            return null;
        }
        final Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static long[] toLongArray(Long[] array) {
        return Arrays.toLongArray(array, 0);
    }

    public static long[] toLongArray(Long[] array, long valueForNull) {
        if (array == null) {
            return null;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Long[] toLongObjectArray(long[] array) {
        if (array == null) {
            return null;
        }
        final Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static float[] toFloatArray(Float[] array) {
        return Arrays.toFloatArray(array, 0);
    }

    public static float[] toFloatArray(Float[] array, float valueForNull) {
        if (array == null) {
            return null;
        }
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Float[] toFloatObjectArray(float[] array) {
        if (array == null) {
            return null;
        }
        final Float[] result = new Float[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static double[] toDoubleArray(Double[] array) {
        return Arrays.toDoubleArray(array, 0);
    }

    public static double[] toDoubleArray(Double[] array, double valueForNull) {
        if (array == null) {
            return null;
        }
        final double[] result = new double[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    public static Double[] toDoubleObjectArray(double[] array) {
        if (array == null) {
            return null;
        }
        final Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static String[] toStringArray(Object[] array) {
        return Arrays.toStringArray(array, null);
    }

    public static String[] toStringArray(Object[] array, String defaultValue) {
        if (array == null) {
            return null;
        }
        final String[] result = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = Objects.toString(array[i], defaultValue);
        }
        return result;
    }

    public static boolean[] repeat(boolean element, int length) {
        final boolean[] array = new boolean[length];
        if (element) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static byte[] repeat(byte element, int length) {
        final byte[] array = new byte[length];
        if (element != 0) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static short[] repeat(short element, int length) {
        final short[] array = new short[length];
        if (element != 0) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static char[] repeat(char element, int length) {
        final char[] array = new char[length];
        if (element != '\0') {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static int[] repeat(int element, int length) {
        final int[] array = new int[length];
        if (element != 0) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static long[] repeat(long element, int length) {
        final long[] array = new long[length];
        if (element != 0L) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static float[] repeat(float element, int length) {
        final float[] array = new float[length];
        if (element != 0.0f) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static double[] repeat(double element, int length) {
        final double[] array = new double[length];
        if (element != 0.0) {
            java.util.Arrays.fill(array, element);
        }
        return array;
    }

    public static int[] sequence(int first, int count) {
        Validations.greaterThanOrEqualTo(count, 0, "count");
        final int[] array = new int[count];
        for (int i = 0; i < count; ++i) {
            array[i] = first + i;
        }
        return array;
    }

    public static boolean[] add(boolean[] array, boolean element) {
        if (array == null) {
            return new boolean[] { element };
        }
        final boolean[] result = new boolean[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static byte[] add(byte[] array, byte element) {
        if (array == null) {
            return new byte[] { element };
        }
        final byte[] result = new byte[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static short[] add(short[] array, short element) {
        if (array == null) {
            return new short[] { element };
        }
        final short[] result = new short[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static char[] add(char[] array, char element) {
        if (array == null) {
            return new char[] { element };
        }
        final char[] result = new char[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static int[] add(int[] array, int element) {
        if (array == null) {
            return new int[] { element };
        }
        final int[] result = new int[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static long[] add(long[] array, long element) {
        if (array == null) {
            return new long[] { element };
        }
        final long[] result = new long[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static float[] add(float[] array, float element) {
        if (array == null) {
            return new float[] { element };
        }
        final float[] result = new float[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    public static double[] add(double[] array, double element) {
        if (array == null) {
            return new double[] { element };
        }
        final double[] result = new double[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = element;
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, T element) {
        final T[] result;
        if (array != null) {
            result = Arrays.createArray((Class<? extends T>) array.getClass().getComponentType(), array.length + 1);
            System.arraycopy(array, 0, result, 0, array.length);
        } else {
            Validations.isTrue(element != null, "Both array and element are null");
            assert element != null;
            result = Arrays.createArray((Class<? extends T>) element.getClass(), 1);
        }
        result[result.length - 1] = element;
        return result;
    }

    public static boolean[] remove(boolean[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final boolean[] result = new boolean[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static byte[] remove(byte[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final byte[] result = new byte[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static short[] remove(short[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final short[] result = new short[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static char[] remove(char[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final char[] result = new char[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static int[] remove(int[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final int[] result = new int[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static long[] remove(long[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final long[] result = new long[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static float[] remove(float[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final float[] result = new float[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static double[] remove(double[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        final double[] result = new double[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    public static <T> T[] remove(T[] array, int index) {
        Arrays.validateIndex(index);
        if (array == null) {
            return null;
        }
        if (index >= array.length) {
            return array;
        }
        @SuppressWarnings("unchecked") final T[] result = Arrays.createArray((Class<? extends T>) array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }

    private static void validateIndex(int index) {
        Validations.greaterThanOrEqualTo(index, 0, "index");
    }

    private Arrays() {
        super();
    }
}
