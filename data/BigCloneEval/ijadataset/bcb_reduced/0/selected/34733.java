package org.osee.indexer.automaton;

import org.osee.indexer.automaton.DFA;

public class PreWork {

    private static int n, total;

    private static int[] first, num;

    private static int[][] second;

    private static byte[][][] keyWord = new byte[3][][];

    public static boolean isInterpunction(char c) {
        if (c == '.' || c == ',' || c == '?' || c == '!' || c == '|') {
            return true;
        }
        return false;
    }

    private static int[] sort(int[] a, int len) {
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                if (a[i] > a[j]) {
                    int tmp = a[i];
                    a[i] = a[j];
                    a[j] = tmp;
                }
            }
        }
        return a;
    }

    private static void buildBoard() {
        int cnt = 0;
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < keyWord[i].length; j++) {
                cnt += keyWord[i][j].length;
            }
        }
        int[] tmp = new int[cnt / 2];
        int len = 0;
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < keyWord[i].length; j++) {
                for (int k = 0; k < keyWord[i][j].length; k += 2) {
                    tmp[len] = keyWord[i][j][k];
                    len++;
                }
            }
        }
        sort(tmp, len);
        first = new int[len];
        n = 0;
        for (int i = 0; i < len; ) {
            int j = i + 1;
            while (j < len && tmp[i] == tmp[j]) {
                j++;
            }
            first[n] = tmp[i];
            n++;
            i = j;
        }
        second = new int[n][cnt / 2];
        num = new int[n];
        for (int i = 0; i < n; i++) {
            num[i] = 0;
        }
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < keyWord[i].length; j++) {
                for (int k = 1; k < keyWord[i][j].length; k += 2) {
                    int l = 0, r = n - 1, op = 0;
                    while (l <= r) {
                        int mid = (l + r) / 2;
                        if (first[mid] == keyWord[i][j][k - 1]) {
                            op = mid;
                            break;
                        }
                        if (first[mid] > keyWord[i][j][k - 1]) {
                            r = mid - 1;
                        } else l = mid + 1;
                    }
                    second[op][num[op]] = keyWord[i][j][k];
                    num[op]++;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            sort(second[i], num[i]);
            int m = 0;
            for (int j = 0; j < num[i]; ) {
                int k = j + 1;
                while (k < num[i] && second[i][j] == second[i][k]) {
                    k++;
                }
                second[i][m] = second[i][j];
                m++;
                j = k;
            }
            num[i] = m;
        }
    }

    private static boolean found(byte a, byte b) {
        int l = 0, r = n - 1, op = -1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (first[mid] == a) {
                op = mid;
                break;
            }
            if (first[mid] > a) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        if (op == -1) {
            return false;
        }
        l = 0;
        r = num[op];
        while (l <= r) {
            int mid = (l + r) / 2;
            if (second[op][mid] == b) {
                return true;
            }
            if (second[op][mid] > b) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return false;
    }

    public static String getNewContent(DFA[] dfa, String content) {
        total = dfa.length;
        for (int i = 0; i < dfa.length; i++) {
            keyWord[i] = dfa[i].keyWord;
        }
        buildBoard();
        String str = new String();
        boolean flag = false;
        for (int i = 0; i < content.length(); i++) {
            if (isInterpunction(content.charAt(i))) {
                str += content.charAt(i);
            }
            byte[] bt = content.substring(i, i + 1).getBytes();
            if (bt.length != 2) {
                flag = true;
                continue;
            }
            if (found(bt[0], bt[1])) {
                if (flag) {
                    str += '|';
                    flag = false;
                }
                str += content.charAt(i);
            } else {
                flag = true;
            }
        }
        return str;
    }
}
