package ch.ethz.mxquery.util;

import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.util.Comparator;

/**
 * Merge Sort. Sorts an array of objects with a corresponding compare class.
 * 
 * @author David Alexander Graf
 * 
 */
public class MergeSort {

    private Object[] arr;

    private Object[] copyArr;

    private Comparator comp;

    private MergeSort(Object[] arr, Comparator comp) {
        this.arr = arr;
        this.comp = comp;
        this.copyArr = new Object[this.arr.length];
    }

    public static void sort(Object[] arr, Comparator comp) throws MXQueryException {
        if (arr == null || arr.length == 0) {
            return;
        }
        MergeSort ms = new MergeSort(arr, comp);
        ms.sort(0, arr.length - 1);
    }

    private void sort(int left, int right) throws MXQueryException {
        if (left < right) {
            int leftRight = left + (right - left) / 2;
            int rightLeft = leftRight + 1;
            this.sort(left, leftRight);
            this.sort(rightLeft, right);
            int copyPos = left;
            int curLeft = left;
            int curRight = rightLeft;
            while (curLeft <= leftRight && curRight <= right) {
                if (this.comp.compare(this.arr[curLeft], this.arr[curRight]) <= 0) {
                    this.copyArr[copyPos] = this.arr[curLeft];
                    copyPos++;
                    curLeft++;
                } else {
                    this.copyArr[copyPos] = this.arr[curRight];
                    copyPos++;
                    curRight++;
                }
            }
            while (curLeft <= leftRight) {
                this.copyArr[copyPos] = this.arr[curLeft];
                copyPos++;
                curLeft++;
            }
            while (curRight <= right) {
                this.copyArr[copyPos] = this.arr[curRight];
                copyPos++;
                curRight++;
            }
            System.arraycopy(this.copyArr, left, this.arr, left, right - left + 1);
        }
    }
}
