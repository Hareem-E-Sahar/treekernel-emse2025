package algorithms.search;

/**
 *
 * @author leeing
 * @date Mar 3, 2011
 */
public class BinarySearch {

    public static void main(String[] args) {
        int a[] = { 1, 2, 3, 4, 5, 8, 9 };
        System.out.println(find(a, 8));
        System.out.println(binarySearch(a, 9));
    }

    public static int find(int[] a, int find) {
        int left = 0;
        int right = a.length - 1;
        int mid = (left + right) / 2;
        while (left <= right) {
            if (find < a[mid]) {
                right = mid - 1;
            } else if (find > a[mid]) {
                left = mid + 1;
            } else {
                return mid;
            }
            mid = (left + right) / 2;
        }
        return -1;
    }

    public static int binarySearch(int[] a, int find) {
        return binarySearch(a, find, 0, a.length - 1);
    }

    public static int binarySearch(int[] a, int find, int start, int end) {
        if (start > end) {
            return -1;
        }
        int mid = (start + end) / 2;
        if (find > a[mid]) {
            return binarySearch(a, find, mid + 1, end);
        } else if (find < a[mid]) {
            return binarySearch(a, find, start, mid - 1);
        } else {
            return mid;
        }
    }
}
