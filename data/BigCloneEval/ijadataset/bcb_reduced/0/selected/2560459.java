package fi.hip.gb.utils;

import java.util.*;
import java.lang.reflect.Array;

public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static Iterator iterate(final Object[] source) {
        return new Iterator() {

            private int _index = 0;

            public boolean hasNext() {
                return _index < source.length;
            }

            public Object next() {
                if (_index >= source.length) throw new NoSuchElementException();
                return source[_index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static boolean equals(Object[] arr1, Object[] arr2) {
        if (arr1.length != arr2.length) return false;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i].equals(arr2[i]) == false) return false;
        }
        return true;
    }

    public static int[] duplicate(int[] source) {
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    public static Object[] duplicate(Object[] source) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length);
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    public static Object[] duplicate(Object[] source, Class newComponentType) {
        Object[] copy = (Object[]) Array.newInstance(newComponentType, source.length);
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i];
        }
        return copy;
    }

    public static Object[] insert(Object[] source, int index, Object obj) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length + 1);
        System.arraycopy(source, 0, copy, 0, index);
        System.arraycopy(source, index, copy, index + 1, source.length - index);
        copy[index] = obj;
        return copy;
    }

    public static Object[] append(Object[] source, Object obj) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length + 1);
        System.arraycopy(source, 0, copy, 0, source.length);
        copy[source.length] = obj;
        return copy;
    }

    public static Object[] append(Object[] source, Object obj, Class componentType) {
        Object[] copy = (Object[]) Array.newInstance(componentType, source.length + 1);
        System.arraycopy(source, 0, copy, 0, source.length);
        copy[source.length] = obj;
        return copy;
    }

    public static Object[] concat(Object[] source, Object[] objs) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length + objs.length);
        System.arraycopy(source, 0, copy, 0, source.length);
        System.arraycopy(objs, 0, copy, source.length, objs.length);
        return copy;
    }

    public static Object[] concat(Object[] source, Object[] objs, Class componentType) {
        Object[] copy = (Object[]) Array.newInstance(componentType, source.length + objs.length);
        System.arraycopy(source, 0, copy, 0, source.length);
        System.arraycopy(objs, 0, copy, source.length, objs.length);
        return copy;
    }

    public static boolean contains(Object[] source, Object obj) {
        return getIndex(source, obj) != -1;
    }

    public static boolean containsOID(Object[] source, Object obj) {
        return getIndexOID(source, obj) != -1;
    }

    public static int getIndex(Object[] source, Object obj) {
        for (int i = 0; i < source.length; i++) if (source[i].equals(obj)) return i;
        return -1;
    }

    public static int getIndexOID(Object[] source, Object obj) {
        for (int i = 0; i < source.length; i++) if (source[i] == obj) return i;
        return -1;
    }

    public static Object[] remove(Object[] source, int index) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length - 1);
        System.arraycopy(source, 0, copy, 0, index);
        System.arraycopy(source, index + 1, copy, index, source.length - index - 1);
        return copy;
    }

    public static Object[] remove(Object[] source, Object obj) {
        int index = getIndex(source, obj);
        if (index != -1) return remove(source, index); else return source;
    }

    public static Object[] removeOID(Object[] source, Object obj) {
        int index = getIndexOID(source, obj);
        if (index != -1) return remove(source, index); else return source;
    }

    public static Object[] setLength(Object[] source, int length) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), length);
        System.arraycopy(source, 0, copy, 0, Math.min(source.length, length));
        return copy;
    }

    public static Object[] remove(Object[] source, int begin, int end) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length - (end - begin));
        System.arraycopy(source, 0, copy, 0, begin);
        System.arraycopy(source, end + 1, copy, begin, source.length - (end - begin));
        return copy;
    }

    public static Object[] replace(Object[] source, int begin, int end, Object obj) {
        Object[] copy = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length - (end - begin) + 1);
        System.arraycopy(source, 0, copy, 0, begin);
        System.arraycopy(source, end + 1, copy, begin + 1, source.length - (end - begin));
        copy[begin] = obj;
        return copy;
    }

    public static Enumeration enumerate(final Object[] source) {
        return new Enumeration() {

            public Object nextElement() {
                if (source == null || _index >= source.length) throw new NoSuchElementException();
                return source[_index++];
            }

            public boolean hasMoreElements() {
                return source != null && _index < source.length;
            }

            private int _index = 0;
        };
    }

    public static String[] stringAsArray(String longName, String delimiters) {
        StringTokenizer st = new StringTokenizer(longName, delimiters);
        String[] nameArray = new String[st.countTokens()];
        for (int i = 0; i < nameArray.length; i++) nameArray[i] = st.nextToken();
        return nameArray;
    }

    public static String asString(Object[] source, String delimiterString) {
        String s = "";
        for (int i = 0; i < source.length; i++) {
            s = s + source[i].toString();
            if (i < source.length - 1) s = s + delimiterString;
        }
        return s;
    }

    public static Object[] asArray(Object o) {
        Object[] objs = (Object[]) Array.newInstance(o.getClass(), 1);
        objs[0] = o;
        return objs;
    }

    public static Object[] asArray(Object o0, Object o1) {
        Object[] objs = (Object[]) Array.newInstance(o0.getClass(), 2);
        objs[0] = o0;
        objs[1] = o1;
        return objs;
    }

    public static Object[] asArray(Object o0, Object o1, Object o2) {
        Object[] objs = (Object[]) Array.newInstance(o0.getClass(), 1);
        objs[0] = o0;
        objs[1] = o1;
        objs[2] = o2;
        return objs;
    }

    /**
     * Print the content of an array in format [elem1, elem2...].
     * @param array content of array elements will be printed using toString() method.
     * @return string presentation of the content
     */
    public static String toString(Object[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i].toString());
            if (i + 1 < array.length) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
