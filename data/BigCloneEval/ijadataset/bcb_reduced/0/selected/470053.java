package ec.util;

/**
 * Implementations of various center-pivot QuickSort routines in Java,
 * and (if you really want 'em) Insertion Sort routines as well.  This code
 * is derived from the QuickSort example in the <a href="ftp://ftp.prenhall.com/pub/esm/computer_science.s-041/shaffer/ds/code/JAVA/code/javacode.zip">
 * source code </a> accompanying <i>A Practical Introduction to Data Structures 
 * and Algorithm Analysis, Java Edition</i>, by Clifford Shaffer.
 * Here's the original header:
 *
 * <p>
 * Source code example for "A Practical Introduction
 * to Data Structures and Algorithm Analysis"
 * by Clifford A. Shaffer, Prentice Hall, 1998.
 * Copyright 1998 by Clifford A. Shaffer
 *
 * <p>Sorting main function for testing correctness of sort algorithm.
 * To use: <sortname> [+/-] <size_of_array> <threshold>
 *  + means increasing values, - means decreasing value and no
 *    parameter means random values;
 * where <size_of_array> controls the number of objects to be sorted;
 * and <threshold> controls the threshold parameter for certain sorts, e.g.,
 *   cutoff point for quicksort sublists.
 *
 * @author Sean Luke
 * @version 1.0 
 */
public class QuickSort {

    static final int THRESHOLD = 0;

    static final int MAXSTACKSIZE = 1000;

    /** Non-Recursive QuickSort */
    public static void qsort(byte[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(byte[] array) {
        byte tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(byte[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        byte pivot;
        int pivotindex, l, r;
        byte tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(short[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(short[] array) {
        short tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(short[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        short pivot;
        int pivotindex, l, r;
        short tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(char[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(char[] array) {
        char tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(char[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        char pivot;
        int pivotindex, l, r;
        char tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(int[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(int[] array) {
        int tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(int[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        int pivot;
        int pivotindex, l, r;
        int tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(long[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(long[] array) {
        long tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(long[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        long pivot;
        int pivotindex, l, r;
        long tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(float[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(float[] array) {
        float tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(float[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        float pivot;
        int pivotindex, l, r;
        float tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(double[] array) {
        qsort_h(array, 0, array.length - 1);
    }

    /** Insertion Sort */
    public static void inssort(double[] array) {
        double tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (array[j] < array[j - 1]); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(double[] array, int oi, int oj) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        double pivot;
        int pivotindex, l, r;
        double tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (array[++l] < pivot) ;
                while ((r != 0) && (array[--r] > pivot)) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(Object[] array, SortComparator comp) {
        qsort_h(array, 0, array.length - 1, comp);
    }

    /** Insertion Sort */
    public static void inssort(Object[] array, SortComparator comp) {
        Object tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (comp.lt(array[j], array[j - 1])); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(Object[] array, int oi, int oj, SortComparator comp) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        Object pivot;
        int pivotindex, l, r;
        Object tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (comp.lt(array[++l], pivot)) ;
                while ((r != 0) && (comp.gt(array[--r], pivot))) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array, comp);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(long[] array, SortComparatorL comp) {
        qsort_h(array, 0, array.length - 1, comp);
    }

    /** Insertion Sort */
    public static void inssort(long[] array, SortComparatorL comp) {
        long tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (comp.lt(array[j], array[j - 1])); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(long[] array, int oi, int oj, SortComparatorL comp) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        long pivot;
        int pivotindex, l, r;
        long tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (comp.lt(array[++l], pivot)) ;
                while ((r != 0) && (comp.gt(array[--r], pivot))) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array, comp);
    }

    /** Non-Recursive QuickSort */
    public static void qsort(int[] array, SortComparatorL comp) {
        qsort_h(array, 0, array.length - 1, comp);
    }

    /** Insertion Sort */
    public static void inssort(int[] array, SortComparatorL comp) {
        int tmp;
        for (int i = 1; i < array.length; i++) for (int j = i; (j > 0) && (comp.lt(array[j], array[j - 1])); j--) {
            tmp = array[j];
            array[j] = array[j - 1];
            array[j - 1] = tmp;
        }
    }

    private static void qsort_h(int[] array, int oi, int oj, SortComparatorL comp) {
        int[] stack = new int[MAXSTACKSIZE];
        int top = -1;
        int pivot;
        int pivotindex, l, r;
        int tmp;
        stack[++top] = oi;
        stack[++top] = oj;
        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            tmp = array[pivotindex];
            array[pivotindex] = array[j];
            array[j] = tmp;
            l = i - 1;
            r = j;
            do {
                while (comp.lt(array[++l], pivot)) ;
                while ((r != 0) && (comp.gt(array[--r], pivot))) ;
                tmp = array[l];
                array[l] = array[r];
                array[r] = tmp;
            } while (l < r);
            tmp = array[l];
            array[l] = array[r];
            array[r] = tmp;
            tmp = array[l];
            array[l] = array[j];
            array[j] = tmp;
            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }
        }
        inssort(array, comp);
    }
}
