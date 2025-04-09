package it.allerj.common.utility;

import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class<? extends Object> elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    public static boolean sameContent(Object array1, Object array2) {
        if (array1 == null && array2 == null) return true;
        if ((array1 == null || array2 == null)) return false;
        Class<? extends Object> array1Type = array1.getClass().getComponentType();
        Class<? extends Object> array2Type = array2.getClass().getComponentType();
        List<? extends Object> list1 = Arrays.asList((Object[]) array1);
        List<? extends Object> list2 = Arrays.asList((Object[]) array2);
        if (!array1Type.equals(array2Type) || list1.size() != list2.size()) return false;
        for (Object key : list1) {
            if (!list2.contains(key)) return false;
        }
        return true;
    }
}
