package com.solidmatrix.regxmaker.util.shared;

/**
 * <PRE>
 * Name   : com.solidmatrix.regxmaker.util.shared.SortUtils
 * Project: RegXmaker Library
 * Version: 1.1
 * Tier   : N/A (Function Class)
 * Author : Gennadiy Shafranovich
 * Purpose: To provide custom features when sortings
 * Summary: Provides the ability to sort arrays of primitives 
 *          and return an array of ints in which each subset
 *          of the array contains the original position of the value
 *          that resides in the sorted array. Methods are copied from
 *          java.util.Arrays class and then modified to provide this
 *          functionality.
 *
 * Copyright (C) 2001, 2004 SolidMatrix Technologies, Inc.
 * This file is part of RegXmaker Library.
 *
 * RegXmaker Library is is free software; you can redistribute it and/or modify
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * RegXmaker library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Comments: Full, with javadoc.
 *
 * Modification History
 *
 * 01-23-2001  GS Class created and ready for testing
 *
 * 07-05-2004  YS Added licensing information
 * </PRE>
 */
public class SortUtils {

    /**
     * Sorts the specified array of longs into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(long[] a) {
        int[] pos = getPosArray(a.length);
        sort1(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of longs into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)
     *
     * <p>The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     * <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort1(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of ints into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(int[] a) {
        int[] pos = getPosArray(a.length);
        sort1(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of ints into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort1(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of shorts into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(short[] a) {
        int[] pos = getPosArray(a.length);
        sort1(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of shorts into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort1(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of chars into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(char[] a) {
        int[] pos = getPosArray(a.length);
        sort1(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of chars into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort1(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of bytes into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(byte[] a) {
        int[] pos = getPosArray(a.length);
        sort1(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of bytes into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort1(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of doubles into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(double[] a) {
        int[] pos = getPosArray(a.length);
        sort2(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of doubles into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort2(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    /**
     * Sorts the specified array of floats into ascending numerical order.
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     */
    public static int[] sortRetain(float[] a) {
        int[] pos = getPosArray(a.length);
        sort2(a, pos, 0, a.length);
        return pos;
    }

    /**
     * Sorts the specified range of the specified array of floats into
     * ascending numerical order.  The range to be sorted extends from index
     * <tt>fromIndex</tt>, inclusive, to index <tt>toIndex</tt>, exclusive.
     * (If <tt>fromIndex==toIndex</tt>, the range to be sorted is empty.)<p>
     *
     * The sorting algorithm is a tuned quicksort, adapted from Jon
     * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
     * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993).  This algorithm offers n*log(n) performance on many data sets
     * that cause other quicksorts to degrade to quadratic performance.
     *
     * @param a the array to be sorted.
     * @param fromIndex the index of the first element (inclusive) to be
     *        sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *	       <tt>toIndex &gt; a.length</tt>
     */
    public static int[] sortRetain(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        int[] pos = getPosArray(a.length, fromIndex);
        sort2(a, pos, fromIndex, toIndex - fromIndex);
        return pos;
    }

    private static void sort2(double a[], int[] y, int fromIndex, int toIndex) {
        final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0d);
        int numNegZeros = 0;
        int i = fromIndex, n = toIndex;
        while (i < n) {
            if (a[i] != a[i]) {
                a[i] = a[--n];
                a[n] = Double.NaN;
                int o = y[i];
                y[i] = y[n];
                y[n] = o;
            } else {
                if (a[i] == 0 && Double.doubleToLongBits(a[i]) == NEG_ZERO_BITS) {
                    a[i] = 0.0d;
                    numNegZeros++;
                }
                i++;
            }
        }
        sort1(a, y, fromIndex, n - fromIndex);
        if (numNegZeros != 0) {
            int j = binarySearch(a, 0.0d, fromIndex, n - 1);
            do {
                j--;
            } while (j >= 0 && a[j] == 0.0d);
            for (int k = 0; k < numNegZeros; k++) a[++j] = -0.0d;
        }
    }

    private static void sort2(float a[], int[] y, int fromIndex, int toIndex) {
        final int NEG_ZERO_BITS = Float.floatToIntBits(-0.0f);
        int numNegZeros = 0;
        int i = fromIndex, n = toIndex;
        while (i < n) {
            if (a[i] != a[i]) {
                a[i] = a[--n];
                a[n] = Float.NaN;
                int o = y[i];
                y[i] = y[n];
                y[n] = o;
            } else {
                if (a[i] == 0 && Float.floatToIntBits(a[i]) == NEG_ZERO_BITS) {
                    a[i] = 0.0f;
                    numNegZeros++;
                }
                i++;
            }
        }
        sort1(a, y, fromIndex, n - fromIndex);
        if (numNegZeros != 0) {
            int j = binarySearch(a, 0.0f, fromIndex, n - 1);
            do {
                j--;
            } while (j >= 0 && a[j] == 0.0f);
            for (int k = 0; k < numNegZeros; k++) a[++j] = -0.0f;
        }
    }

    /**
     * Sorts the specified sub-array of longs into ascending order.
     */
    private static void sort1(long x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        long v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(long x[], int[] y, int a, int b) {
        long t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(long x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed longs.
     */
    private static int med3(long x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of integers into ascending order.
     */
    private static void sort1(int x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        int v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int x[], int[] y, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(int x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of shorts into ascending order.
     */
    private static void sort1(short x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        short v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(short x[], int[] y, int a, int b) {
        short t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(short x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed shorts.
     */
    private static int med3(short x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of chars into ascending order.
     */
    private static void sort1(char x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        char v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(char x[], int[] y, int a, int b) {
        char t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(char x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed chars.
     */
    private static int med3(char x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of bytes into ascending order.
     */
    private static void sort1(byte x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        byte v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(byte x[], int[] y, int a, int b) {
        byte t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(byte x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed bytes.
     */
    private static int med3(byte x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of doubles into ascending order.
     */
    private static void sort1(double x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        double v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(double x[], int[] y, int a, int b) {
        double t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(double x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed doubles.
     */
    private static int med3(double x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Sorts the specified sub-array of floats into ascending order.
     */
    private static void sort1(float x[], int[] y, int off, int len) {
        if (len < 7) {
            for (int i = off; i < len + off; i++) for (int j = i; j > off && x[j - 1] > x[j]; j--) swap(x, y, j, j - 1);
            return;
        }
        int m = off + len / 2;
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = med3(x, l, l + s, l + 2 * s);
                m = med3(x, m - s, m, m + s);
                n = med3(x, n - 2 * s, n - s, n);
            }
            m = med3(x, l, m, n);
        }
        float v = x[m];
        int a = off, b = a, c = off + len - 1, d = c;
        while (true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v) swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v) swap(x, y, c, d--);
                c--;
            }
            if (b > c) break;
            swap(x, y, b++, c--);
        }
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(x, y, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, y, b, n - s, s);
        if ((s = b - a) > 1) sort1(x, y, off, s);
        if ((s = d - c) > 1) sort1(x, y, n - s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(float x[], int[] y, int a, int b) {
        float t = x[a];
        x[a] = x[b];
        x[b] = t;
        int o = y[a];
        y[a] = y[b];
        y[b] = o;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(float x[], int[] y, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++) swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed floats.
     */
    private static int med3(float x[], int a, int b, int c) {
        return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }

    /**
     * Searches the specified array of doubles for the specified value using
     * the binary search algorithm.  The array <strong>must</strong> be sorted
     * (as by the <tt>sort</tt> method, above) prior to making this call.  If
     * it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(double[])
     */
    public static int binarySearch(double[] a, double key) {
        return binarySearch(a, key, 0, a.length - 1);
    }

    private static int binarySearch(double[] a, double key, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            double midVal = a[mid];
            int cmp;
            if (midVal < key) {
                cmp = -1;
            } else if (midVal > key) {
                cmp = 1;
            } else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ? 0 : (midBits < keyBits ? -1 : 1));
            }
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    /**
     * Searches the specified array of floats for the specified value using
     * the binary search algorithm.  The array <strong>must</strong> be sorted
     * (as by the <tt>sort</tt> method, above) prior to making this call.  If
     * it is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @see #sort(float[])
     */
    public static int binarySearch(float[] a, float key) {
        return binarySearch(a, key, 0, a.length - 1);
    }

    private static int binarySearch(float[] a, float key, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            float midVal = a[mid];
            int cmp;
            if (midVal < key) {
                cmp = -1;
            } else if (midVal > key) {
                cmp = 1;
            } else {
                int midBits = Float.floatToIntBits(midVal);
                int keyBits = Float.floatToIntBits(key);
                cmp = (midBits == keyBits ? 0 : (midBits < keyBits ? -1 : 1));
            }
            if (cmp < 0) low = mid + 1; else if (cmp > 0) high = mid - 1; else return mid;
        }
        return -(low + 1);
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an
     * appropriate exception if they aren't.
     */
    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        if (fromIndex < 0) throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen) throw new ArrayIndexOutOfBoundsException(toIndex);
    }

    /**
     * Creates a new int array with values from 0 to array size - 1
     */
    private static int[] getPosArray(int size) {
        int[] pos = new int[size];
        for (int i = 0; i < size; i++) pos[i] = i;
        return pos;
    }

    /**
     * Creates a new int array of a certain size and fills it with 
     * values from start to start + size - 1
     */
    private static int[] getPosArray(int size, int start) {
        int[] pos = new int[size];
        for (int i = 0; i < size; i++) pos[i] = start + i;
        return pos;
    }
}
