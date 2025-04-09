package com.qarks.util;

public class SortUtilities {

    public static int getInsertIndex(Object[] objects, Object toInsert, boolean ascending) {
        if (objects.length == 0) {
            return 0;
        }
        int low = 0;
        int hight = objects.length - 1;
        while (low != hight) {
            int middle = (low + hight) / 2;
            if (middle != low && middle != hight) {
                int compare = compare(toInsert, objects[middle], ascending);
                if (compare == 0) {
                    low = hight = middle;
                } else if (compare > 0) {
                    low = middle;
                } else {
                    hight = middle;
                }
            } else if (middle == low) {
                int compare = compare(toInsert, objects[middle], ascending);
                if (compare <= 0) {
                    hight = low;
                } else {
                    low++;
                }
            } else if (middle == hight) {
                int compare = compare(objects[middle], toInsert, ascending);
                if (compare >= 0) {
                    low = hight;
                } else {
                    hight--;
                }
            }
        }
        int result = low;
        if (compare(toInsert, objects[low], ascending) > 0) {
            result++;
        }
        return result;
    }

    public static <T> T[] sort(T[] objects, boolean ascending) {
        if (objects == null) {
            return null;
        }
        Class componentType = objects.getClass().getComponentType();
        T[] result = (T[]) java.lang.reflect.Array.newInstance(componentType, objects.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = objects[i];
        }
        if (objects.length > 0) {
            shuttlesort(objects, result, 0, objects.length, ascending);
        }
        return result;
    }

    private static void shuttlesort(Object from[], Object to[], int low, int high, boolean ascending) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle, ascending);
        shuttlesort(to, from, middle, high, ascending);
        int p = low;
        int q = middle;
        if (high - low >= 4 && compare(from[middle - 1], from[middle], ascending) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || p < middle && compare(from[p], from[q], ascending) <= 0) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    private static int compare(Object insert, Object ref, boolean ascending) {
        int result = 0;
        if (insert != null && ref != null) {
            result = insert.toString().compareToIgnoreCase(ref.toString());
            if (!ascending) {
                result = -result;
            }
        }
        return result;
    }
}
