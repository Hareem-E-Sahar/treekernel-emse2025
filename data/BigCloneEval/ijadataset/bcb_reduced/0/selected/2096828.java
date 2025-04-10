package nuts.core.lang;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * utility class for Object. 
 */
public class ObjectUtils extends org.apache.commons.lang.ObjectUtils {

    private static final int INITIAL_HASH = 7;

    private static final int MULTIPLIER = 31;

    private static final String EMPTY_STRING = "";

    private static final String NULL_STRING = "null";

    private static final String ARRAY_START = "[";

    private static final String ARRAY_END = "]";

    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";

    /**
	 * get field value
	 * 
	 * @param object object
	 * @param name field name
	 * @return field field value
	 * @throws NoSuchFieldException if an error occurs
	 * @throws IllegalAccessException  if an error occurs
	 * @throws SecurityException  if an error occurs
	 * @throws IllegalArgumentException  if an error occurs
	 */
    public static Object getFieldValue(Object object, String name) throws NoSuchFieldException, IllegalArgumentException, SecurityException, IllegalAccessException {
        Field field = object.getClass().getField(name);
        return field.get(object);
    }

    /**
	 * set field value
	 * 
	 * @param object object
	 * @param name field name
	 * @param value field value
	 * @throws NoSuchFieldException  if an error occurs
	 * @throws IllegalArgumentException  if an error occurs
	 * @throws SecurityException  if an error occurs
	 * @throws IllegalAccessException  if an error occurs
	 */
    public static void setFieldValue(Object object, String name, Object value) throws NoSuchFieldException, IllegalArgumentException, SecurityException, IllegalAccessException {
        Field field = object.getClass().getField(name);
        field.set(object, value);
    }

    /**
	 * 
	 * get static field value
	 * 
	 * @param clazz class
	 * @param name field name
	 * @return field field value
	 * @throws NoSuchFieldException  if an error occurs
	 * @throws IllegalArgumentException  if an error occurs
	 * @throws SecurityException  if an error occurs
	 * @throws IllegalAccessException  if an error occurs
	 */
    public static Object getStaticFieldValue(Class clazz, String name) throws NoSuchFieldException, IllegalArgumentException, SecurityException, IllegalAccessException {
        Field field = clazz.getField(name);
        return field.get(null);
    }

    /**
	 * set static field value
	 * 
	 * @param clazz class
	 * @param name field name
	 * @param value field value
	 * @throws NoSuchFieldException  if an error occurs
	 * @throws IllegalArgumentException  if an error occurs
	 * @throws SecurityException  if an error occurs
	 * @throws IllegalAccessException  if an error occurs
	 */
    public static void setStaticFieldValue(Class clazz, String name, Object value) throws NoSuchFieldException, IllegalArgumentException, SecurityException, IllegalAccessException {
        Field field = clazz.getField(name);
        field.set(null, value);
    }

    /**
	 * Return whether the given throwable is a checked exception:
	 * that is, neither a RuntimeException nor an Error.
	 * @param ex the throwable to check
	 * @return whether the throwable is a checked exception
	 * @see java.lang.Exception
	 * @see java.lang.RuntimeException
	 * @see java.lang.Error
	 */
    public static boolean isCheckedException(Throwable ex) {
        return !(ex instanceof RuntimeException || ex instanceof Error);
    }

    /**
	 * Check whether the given exception is compatible with the exceptions
	 * declared in a throws clause.
	 * @param ex the exception to checked
	 * @param declaredExceptions the exceptions declared in the throws clause
	 * @return whether the given exception is compatible
	 */
    @SuppressWarnings("unchecked")
    public static boolean isCompatibleWithThrowsClause(Throwable ex, Class[] declaredExceptions) {
        if (!isCheckedException(ex)) {
            return true;
        }
        if (declaredExceptions != null) {
            for (int i = 0; i < declaredExceptions.length; i++) {
                if (declaredExceptions[i].isAssignableFrom(ex.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
	 * Check whether the given array contains the given element.
	 * @param array the array to check (may be <code>null</code>,
	 * in which case the return value will always be <code>false</code>)
	 * @param element the element to check for
	 * @return whether the element has been found in the given array
	 */
    public static boolean containsElement(Object[] array, Object element) {
        if (array == null) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (nullSafeEquals(array[i], element)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Append the given Object to the given array, returning a new array
	 * consisting of the input array contents plus the given Object.
	 * @param array the array to append to (can be <code>null</code>)
	 * @param obj the Object to append
	 * @return the new array (of the same component type; never <code>null</code>)
	 */
    public static Object[] addObjectToArray(Object[] array, Object obj) {
        Class compType = Object.class;
        if (array != null) {
            compType = array.getClass().getComponentType();
        } else if (obj != null) {
            compType = obj.getClass();
        }
        int newArrLength = (array != null ? array.length + 1 : 1);
        Object[] newArr = (Object[]) Array.newInstance(compType, newArrLength);
        if (array != null) {
            System.arraycopy(array, 0, newArr, 0, array.length);
        }
        newArr[newArr.length - 1] = obj;
        return newArr;
    }

    /**
	 * Convert the given array (which may be a primitive array) to an
	 * object array (if necessary of primitive wrapper objects).
	 * <p>A <code>null</code> source value will be converted to an
	 * empty Object array.
	 * @param source the (potentially primitive) array
	 * @return the corresponding object array (never <code>null</code>)
	 * @throws IllegalArgumentException if the parameter is not an array
	 */
    public static Object[] toObjectArray(Object source) {
        if (source instanceof Object[]) {
            return (Object[]) source;
        }
        if (source == null) {
            return new Object[0];
        }
        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }
        int length = Array.getLength(source);
        if (length == 0) {
            return new Object[0];
        }
        Class wrapperType = Array.get(source, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }
        return newArray;
    }

    /**
	 * Determine if the given objects are equal, returning <code>true</code>
	 * if both are <code>null</code> or <code>false</code> if only one is
	 * <code>null</code>.
	 * <p>Compares arrays with <code>Arrays.equals</code>, performing an equality
	 * check based on the array elements rather than the array reference.
	 * @param o1 first Object to compare
	 * @param o2 second Object to compare
	 * @return whether the given objects are equal
	 * @see java.util.Arrays#equals
	 */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            if (o1 instanceof Object[] && o2 instanceof Object[]) {
                return Arrays.equals((Object[]) o1, (Object[]) o2);
            }
            if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
                return Arrays.equals((boolean[]) o1, (boolean[]) o2);
            }
            if (o1 instanceof byte[] && o2 instanceof byte[]) {
                return Arrays.equals((byte[]) o1, (byte[]) o2);
            }
            if (o1 instanceof char[] && o2 instanceof char[]) {
                return Arrays.equals((char[]) o1, (char[]) o2);
            }
            if (o1 instanceof double[] && o2 instanceof double[]) {
                return Arrays.equals((double[]) o1, (double[]) o2);
            }
            if (o1 instanceof float[] && o2 instanceof float[]) {
                return Arrays.equals((float[]) o1, (float[]) o2);
            }
            if (o1 instanceof int[] && o2 instanceof int[]) {
                return Arrays.equals((int[]) o1, (int[]) o2);
            }
            if (o1 instanceof long[] && o2 instanceof long[]) {
                return Arrays.equals((long[]) o1, (long[]) o2);
            }
            if (o1 instanceof short[] && o2 instanceof short[]) {
                return Arrays.equals((short[]) o1, (short[]) o2);
            }
        }
        return false;
    }

    /**
	 * Return as hash code for the given object; typically the value of
	 * <code>{@link Object#hashCode()}</code>. If the object is an array,
	 * this method will delegate to any of the <code>nullSafeHashCode</code>
	 * methods for arrays in this class. If the object is <code>null</code>,
	 * this method returns 0.
	 * 
	 * @param obj object
	 * @return 0 if the object is null
	 * @see #nullSafeHashCode(Object[])
	 * @see #nullSafeHashCode(boolean[])
	 * @see #nullSafeHashCode(byte[])
	 * @see #nullSafeHashCode(char[])
	 * @see #nullSafeHashCode(double[])
	 * @see #nullSafeHashCode(float[])
	 * @see #nullSafeHashCode(int[])
	 * @see #nullSafeHashCode(long[])
	 * @see #nullSafeHashCode(short[])
	 */
    public static int nullSafeHashCode(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return nullSafeHashCode((Object[]) obj);
            }
            if (obj instanceof boolean[]) {
                return nullSafeHashCode((boolean[]) obj);
            }
            if (obj instanceof byte[]) {
                return nullSafeHashCode((byte[]) obj);
            }
            if (obj instanceof char[]) {
                return nullSafeHashCode((char[]) obj);
            }
            if (obj instanceof double[]) {
                return nullSafeHashCode((double[]) obj);
            }
            if (obj instanceof float[]) {
                return nullSafeHashCode((float[]) obj);
            }
            if (obj instanceof int[]) {
                return nullSafeHashCode((int[]) obj);
            }
            if (obj instanceof long[]) {
                return nullSafeHashCode((long[]) obj);
            }
            if (obj instanceof short[]) {
                return nullSafeHashCode((short[]) obj);
            }
        }
        return obj.hashCode();
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array  array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(Object[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + nullSafeHashCode(array[i]);
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(boolean[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + hashCode(array[i]);
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(byte[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + array[i];
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(char[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + array[i];
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(double[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + hashCode(array[i]);
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + hashCode(array[i]);
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + array[i];
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(long[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + hashCode(array[i]);
        }
        return hash;
    }

    /**
	 * Return a hash code based on the contents of the specified array.
	 * If <code>array</code> is <code>null</code>, this method returns 0.
	 * @param array array
	 * @return 0 if array is null
	 */
    public static int nullSafeHashCode(short[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        int arraySize = array.length;
        for (int i = 0; i < arraySize; i++) {
            hash = MULTIPLIER * hash + array[i];
        }
        return hash;
    }

    /**
	 * Return the same value as <code>{@link Boolean#hashCode()}</code>.
	 * @see Boolean#hashCode()
	 * @param bool boolean object
	 * @return bool ? 1231 : 1237
	 */
    public static int hashCode(boolean bool) {
        return bool ? 1231 : 1237;
    }

    /**
	 * Return the same value as <code>{@link Double#hashCode()}</code>.
	 * @param dbl double object
	 * @return hash code
	 * @see Double#hashCode()
	 */
    public static int hashCode(double dbl) {
        long bits = Double.doubleToLongBits(dbl);
        return hashCode(bits);
    }

    /**
	 * Return the same value as <code>{@link Float#hashCode()}</code>.
	 * @param flt float object
	 * @return hash code
	 * @see Float#hashCode()
	 */
    public static int hashCode(float flt) {
        return Float.floatToIntBits(flt);
    }

    /**
	 * Return the same value as <code>{@link Long#hashCode()}</code>.
	 * @param lng long object
	 * @return hash code
	 * @see Long#hashCode()
	 */
    public static int hashCode(long lng) {
        return (int) (lng ^ (lng >>> 32));
    }

    /**
	 * Return a String representation of an object's overall identity.
	 * @param obj the object (may be <code>null</code>)
	 * @return the object's identity as String representation,
	 * or an empty String if the object was <code>null</code>
	 */
    public static String identityToString(Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }

    /**
	 * Return a hex String form of an object's identity hash code.
	 * @param obj the object
	 * @return the object's identity code in hex notation
	 */
    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    /**
	 * Return a content-based String representation if <code>obj</code> is
	 * not <code>null</code>; otherwise returns an empty String.
	 * <p>Differs from {@link #toString(Object)} in that it returns
	 * an empty String rather than "null" for a <code>null</code> value.
	 * @param obj the object to build a display String for
	 * @return a display String representation of <code>obj</code>
	 * @see #toString(Object)
	 */
    public static String getDisplayString(Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return toString(obj);
    }

    /**
	 * Determine the class name for the given object.
	 * <p>Returns <code>"null"</code> if <code>obj</code> is <code>null</code>.
	 * @param obj the object to introspect (may be <code>null</code>)
	 * @return the corresponding class name
	 */
    public static String nullSafeClassName(Object obj) {
        return (obj != null ? obj.getClass().getName() : NULL_STRING);
    }

    /**
	 * Return a String representation of the specified Object.
	 * <p>Builds a String representation of the contents in case of an array.
	 * Returns <code>"null"</code> if <code>obj</code> is <code>null</code>.
	 * @param obj the object to build a String representation for
	 * @return a String representation of <code>obj</code>
	 */
    public static String toString(Object obj) {
        if (obj == null) {
            return NULL_STRING;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Object[]) {
            return ARRAY_START + StringUtils.join((Object[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof boolean[]) {
            return ARRAY_START + StringUtils.join((boolean[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof byte[]) {
            return ARRAY_START + StringUtils.join((byte[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof char[]) {
            return ARRAY_START + StringUtils.join((char[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof double[]) {
            return ARRAY_START + StringUtils.join((double[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof float[]) {
            return ARRAY_START + StringUtils.join((float[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof int[]) {
            return ARRAY_START + StringUtils.join((int[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof long[]) {
            return ARRAY_START + StringUtils.join((long[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        if (obj instanceof short[]) {
            return ARRAY_START + StringUtils.join((short[]) obj, ARRAY_ELEMENT_SEPARATOR) + ARRAY_END;
        }
        String str = obj.toString();
        return (str != null ? str : EMPTY_STRING);
    }
}
