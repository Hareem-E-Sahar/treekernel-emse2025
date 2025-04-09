package org.melati.util;

import java.util.Vector;
import java.util.Enumeration;

/**
 * A collection of useful operations on <code>Array</code>s.
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    public static Object[] arrayOf(Vector v) {
        Object[] arr;
        synchronized (v) {
            arr = new Object[v.size()];
            v.copyInto(arr);
        }
        return arr;
    }

    public static Object[] arrayOf(Enumeration e) {
        Vector v = EnumUtils.vectorOf(e);
        return arrayOf(v);
    }

    public static Object[] added(Object[] xs, Object y) {
        Object[] xsx = (Object[]) java.lang.reflect.Array.newInstance(xs.getClass().getComponentType(), xs.length + 1);
        System.arraycopy(xs, 0, xsx, 0, xs.length);
        xsx[xs.length] = y;
        return xsx;
    }

    public static Object[] concatenated(Object[] xs, Object[] ys) {
        Object[] xsys = (Object[]) java.lang.reflect.Array.newInstance(xs.getClass().getComponentType(), xs.length + ys.length);
        System.arraycopy(xs, 0, xsys, 0, xs.length);
        System.arraycopy(ys, 0, xsys, xs.length, ys.length);
        return xsys;
    }

    public static Object[] section(Object[] xs, int start, int limit) {
        Object[] xs_ = (Object[]) java.lang.reflect.Array.newInstance(xs.getClass().getComponentType(), limit - start);
        System.arraycopy(xs, start, xs_, 0, xs_.length);
        return xs_;
    }

    public static int indexOf(Object[] xs, Object x) {
        for (int i = 0; i < xs.length; ++i) if (xs[i].equals(x)) return i;
        return -1;
    }

    public static boolean contains(Object[] xs, Object x) {
        return indexOf(xs, x) != -1;
    }
}
