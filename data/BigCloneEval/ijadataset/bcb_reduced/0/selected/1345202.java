package org.openscience.jvxl.util;

import java.lang.reflect.Array;

public class ArrayUtil {

    public static Object ensureLength(Object array, int minimumLength) {
        if (array != null && Array.getLength(array) >= minimumLength) return array;
        return setLength(array, minimumLength);
    }

    public static Object doubleLength(Object array) {
        return setLength(array, (array == null ? 16 : 2 * Array.getLength(array)));
    }

    public static Object setLength(Object array, int newLength) {
        if (array == null) {
            return null;
        }
        Object t = Array.newInstance(array.getClass().getComponentType(), newLength);
        int oldLength = Array.getLength(array);
        System.arraycopy(array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
        return t;
    }
}
