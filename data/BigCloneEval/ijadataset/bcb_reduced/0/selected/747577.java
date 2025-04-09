package org.cake.game;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A class providing various utility methods.
 * @author Aaron Cake
 */
public class Util {

    public static final int NO_REPLACE = 0;

    public static final int ALWAYS_ADD = 1;

    public static final int REPLACE = 2;

    private Util() {
    }

    private static DecimalFormat df = new DecimalFormat("0.###");

    public static String format(float num, int minDecimals, int maxDecimals) {
        df.setMaximumFractionDigits(maxDecimals);
        df.setMinimumFractionDigits(minDecimals);
        return df.format(num);
    }

    public static String format(float num) {
        df.setMaximumFractionDigits(3);
        df.setMinimumFractionDigits(0);
        return df.format(num);
    }

    public static float[] ensureSize(float[] src, int targetSize, int increment) {
        if (src.length < targetSize) {
            float[] temp = new float[targetSize + increment];
            System.arraycopy(src, 0, temp, 0, src.length);
            return temp;
        } else {
            return src;
        }
    }

    public static double[] ensureSize(double[] src, int targetSize, int increment) {
        if (src.length < targetSize) {
            double[] temp = new double[targetSize + increment];
            System.arraycopy(src, 0, temp, 0, src.length);
            return temp;
        } else {
            return src;
        }
    }

    public static float fitRange(float num, float low, float high) {
        if (num < low) return low;
        if (num > high) return high;
        return num;
    }

    private static <T extends Comparable<? super T>> int binaryFind(List<T> list, T element, int start, int end) {
        if (end < start) return start;
        int mid = (start + end) / 2;
        T midObj = list.get(mid);
        int compare = midObj.compareTo(element);
        if (start == end) return compare < 0 ? start + 1 : start;
        if (compare == 0) return mid;
        if (compare > 0) return binaryFind(list, element, start, mid - 1); else return binaryFind(list, element, mid + 1, end);
    }

    private static <T> int binarySearch(List<T> list, T element, int start, int end, Comparator<? super T> comparer) {
        if (end < start) return -1;
        int mid = (start + end) / 2;
        T midObj = list.get(mid);
        int compare = comparer.compare(midObj, element);
        if (compare == 0) return mid;
        if (start == end) return -1;
        if (compare > 0) return binarySearch(list, element, start, mid - 1, comparer); else return binarySearch(list, element, mid + 1, end, comparer);
    }

    private static <T> int binaryFind(List<T> list, T element, int start, int end, Comparator<? super T> comparer) {
        if (end < start) return start;
        int mid = (start + end) / 2;
        T midObj = list.get(mid);
        int compare = comparer.compare(midObj, element);
        if (start == end) return compare < 0 ? start + 1 : start;
        if (compare == 0) return mid;
        if (compare > 0) return binaryFind(list, element, start, mid - 1, comparer); else return binaryFind(list, element, mid + 1, end, comparer);
    }

    private static <T extends Comparable<? super T>> int binarySearch(List<T> list, T element, int start, int end) {
        if (end < start) return -1;
        int mid = (start + end) / 2;
        T midObj = list.get(mid);
        int compare = midObj.compareTo(element);
        if (compare == 0) return mid;
        if (start == end) return -1;
        if (compare > 0) return binarySearch(list, element, start, mid - 1); else return binarySearch(list, element, mid + 1, end);
    }

    public static <T extends Comparable<? super T>> int binarySearch(List<T> list, T element) {
        return binarySearch(list, element, 0, list.size() - 1);
    }

    public static <T extends Comparable<? super T>> int binaryFind(List<T> list, T element) {
        return binaryFind(list, element, 0, list.size() - 1);
    }

    public static <T> int binarySearch(List<T> list, T element, Comparator<? super T> comparer) {
        return binarySearch(list, element, 0, list.size() - 1, comparer);
    }

    public static <T extends Comparable<? super T>> boolean addSorted(List<T> list, T element, int mode) {
        int pos = binaryFind(list, element, 0, list.size() - 1);
        if (mode != ALWAYS_ADD && pos < list.size() && list.get(pos).compareTo(element) == 0) {
            if (mode == REPLACE) {
                list.set(pos, element);
                return true;
            }
            return false;
        }
        list.add(pos, element);
        return true;
    }

    public static <T extends Comparable<? super T>> boolean removeSorted(List<T> list, T element) {
        int pos = binaryFind(list, element, 0, list.size() - 1);
        if (pos < list.size() && list.get(pos).compareTo(element) == 0) {
            list.remove(pos);
            return true;
        }
        return false;
    }

    public static <T> boolean addSorted(List<T> list, T element, int mode, Comparator<? super T> comparer) {
        int pos = binaryFind(list, element, 0, list.size() - 1, comparer);
        if (mode != ALWAYS_ADD && pos < list.size() && comparer.compare(list.get(pos), element) == 0) {
            if (mode == REPLACE) {
                list.set(pos, element);
                return true;
            }
            return false;
        }
        list.add(pos, element);
        return true;
    }

    public static <T> boolean removeSorted(List<T> list, T element, Comparator<? super T> comparer) {
        int pos = binaryFind(list, element, 0, list.size() - 1, comparer);
        if (pos < list.size() && comparer.compare(list.get(pos), element) == 0) {
            list.remove(pos);
            return true;
        }
        return false;
    }

    public static String toString(List list) {
        return Arrays.toString(list.toArray());
    }
}
