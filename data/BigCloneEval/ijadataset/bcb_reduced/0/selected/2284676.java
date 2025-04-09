package lis;

public class LIS {

    private LIS() {
        System.out.println("Can not creat the instance of LIS!");
    }

    public static int getLISLen(final int[] s, int[] lis) {
        if (null == s) {
            return -1;
        }
        c = new int[s.length + 1];
        cindex = new int[s.length + 1];
        pre = new int[s.length];
        cindex[0] = -1;
        for (int i = 0; i < s.length; ++i) {
            pre[i] = -1;
            cindex[i + 1] = -1;
        }
        c[0] = 0;
        c[1] = s[0];
        cindex[1] = 0;
        len = 1;
        int j;
        for (int i = 1; i < s.length; ++i) {
            j = binarySearch(c, len, s[i]);
            c[j] = s[i];
            cindex[j] = i;
            pre[i] = cindex[j - 1];
            if (len < j) {
                len = j;
                lastIndex = i;
            }
        }
        getSubsquence(s, lis);
        return len;
    }

    /**
	 * 二分查找。返回值表示n在数组a中的位置。如果在数组中有元素等于n
	 * 那么返回最后一个等于n的元素的下一个位置。
	 * @param a  数组a
	 * @param len 数组a中数据的个数
	 * @param n  需要查找的元素
	 * @return
	 */
    private static int binarySearch(final int[] a, int len, int n) {
        if (n < 0) {
            return -1;
        }
        int left = 0, right = len;
        int mid = (left + right) / 2;
        while (left <= right) {
            if (n >= a[mid]) {
                left = mid + 1;
            } else if (n < a[mid]) {
                right = mid - 1;
            }
            mid = (left + right) / 2;
        }
        return left;
    }

    /**
	 * 构造其中一个最长递增子列。
	 * @param s 原始序列。
	 * @param lis 最长子列
	 */
    private static void getSubsquence(final int[] s, int[] lis) {
        int pr;
        int index = len;
        pr = lastIndex;
        do {
            lis[--index] = s[pr];
            pr = pre[pr];
        } while (pr != -1);
    }

    private static int len = 0;

    private static int lastIndex = -1;

    private static int[] c;

    private static int[] cindex;

    private static int[] pre;
}
