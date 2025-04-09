package chapter1;

import java.util.Arrays;
import edu.princeton.cs.stdlib.In;
import edu.princeton.cs.stdlib.StdOut;

public class Exercise_1_1_28 {

    public static int rank(int key, int[] a) {
        int lo = 0;
        int hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (key < a[mid]) hi = mid - 1; else if (key > a[mid]) lo = mid + 1; else return mid;
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] whitelist = In.readInts(args[0]);
        Arrays.sort(whitelist);
        whitelist = removeDuplicate(whitelist);
        for (int i : whitelist) StdOut.print(" " + i + " ");
        In in = new In(args[1]);
        while (!in.isEmpty()) {
            int key = in.readInt();
            if (rank(key, whitelist) == -1) StdOut.println(key);
        }
    }

    public static int[] removeDuplicate(int[] a) {
        int[] uniqueWhitelistTmp = new int[a.length];
        uniqueWhitelistTmp[0] = a[0];
        int idx = 0;
        for (int i = 0; i < a.length; i++) {
            if (uniqueWhitelistTmp[idx] != a[i]) {
                idx++;
                uniqueWhitelistTmp[idx] = a[i];
            }
        }
        int[] uniqueWhitelist = new int[idx + 1];
        for (int i = 0; i < uniqueWhitelist.length; i++) {
            uniqueWhitelist[i] = uniqueWhitelistTmp[i];
        }
        return uniqueWhitelist;
    }
}
