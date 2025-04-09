package algorithm.sort;

public class BinaryInsertionSort {

    public static void main(String[] args) {
        int[] a = { 9, 6, 8, 3, 6, 2, 1 };
        insertSort2(a);
        printArray(a);
    }

    private static void insertSort2(int[] a) {
        int lo = 0;
        int up = a.length;
        int tempr = 0;
        int j = 0;
        int l, h = 0;
        for (int i = lo + 1; i < up; i++) {
            tempr = a[i];
            for (l = lo - 1, h = i; h - l > 1; ) {
                j = (h + l) / 2;
                if (tempr < a[j]) h = j; else l = j;
            }
            for (j = i; j > h; j--) a[j] = a[j - 1];
            a[h] = tempr;
        }
    }

    private static void printArray(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ,");
        }
    }

    private int binarySearchRecursive(int a[], int low, int high, int key) {
        int mid;
        mid = low + ((high - low) / 2);
        if (key > a[mid]) return binarySearchRecursive(a, mid + 1, high, key); else if (key < a[mid]) return binarySearchRecursive(a, low, mid, key);
        return mid;
    }

    private static void binaryInsertionSort(int[] a) {
        int tmp = 0;
        int left = 0;
        int right = 0;
        int mid = 0;
        for (int i = 1; i < a.length; i++) {
            tmp = a[i];
            right = i;
            while (left < right) {
                mid = (left + right) / 2;
                if (tmp >= a[mid]) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            for (int j = i; j > left; j--) {
                swap(a, j);
            }
        }
    }

    private static void swap(int[] a, int j) {
        int tmp2 = a[j - 1];
        a[j - 1] = a[j];
        a[j] = tmp2;
    }

    void insertsort(int a[], int n) {
        int i, j, index;
        for (i = 0; i < n; i++) {
            index = a[i];
            j = 1;
            while ((j > 0) && (a[j - 1] > index)) {
                a[j] = a[j - 1];
                j = j - 1;
            }
            a[j] = index;
        }
    }
}
