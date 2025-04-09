import java.util.StringTokenizer;

class P10474 {

    public static void main(String[] args) {
        int cases = 1;
        while (true) {
            StringTokenizer st = new StringTokenizer(readLn());
            int N = Integer.parseInt(st.nextToken());
            int Q = Integer.parseInt(st.nextToken());
            if (N == 0 && Q == 0) {
                break;
            }
            process(N, Q, cases);
            cases++;
        }
    }

    private static void process(int n, int q, int cases) {
        System.out.println("CASE# " + cases + ":");
        int[] marbles = new int[n];
        for (int k = 0; k < n; k++) {
            marbles[k] = Integer.parseInt(readLn());
        }
        sort(marbles);
        for (int k = 0; k < q; k++) {
            int query = Integer.parseInt(readLn());
            System.out.print(query + " ");
            int place = search(query, marbles);
            if (place == -1) {
                System.out.println("not found");
            } else {
                for (int j = place; j >= 0; j--) {
                    if (marbles[j] != query) {
                        j++;
                        System.out.println("found at " + (j + 1));
                        break;
                    }
                    if (j == 0) {
                        System.out.println("found at " + (j + 1));
                        break;
                    }
                }
            }
        }
    }

    private static int search(int query, int[] marbles) {
        return binarySearch(query, marbles, 0, marbles.length - 1);
    }

    private static int binarySearch(int query, int[] marbles, int i, int j) {
        if (i > j) {
            return -1;
        }
        int medium = (i + j) / 2;
        if (marbles[medium] == query) {
            return medium;
        }
        if (query < marbles[medium]) {
            return binarySearch(query, marbles, i, medium - 1);
        } else {
            return binarySearch(query, marbles, medium + 1, j);
        }
    }

    private static void sort(int[] array) {
        QuickSort(array, 0, array.length - 1);
    }

    static void QuickSort(int a[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        int mid;
        if (hi0 > lo0) {
            mid = a[(lo0 + hi0) / 2];
            while (lo <= hi) {
                while ((lo < hi0) && (a[lo] < mid)) ++lo;
                while ((hi > lo0) && (a[hi] > mid)) --hi;
                if (lo <= hi) {
                    swap(a, lo, hi);
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi) QuickSort(a, lo0, hi);
            if (lo < hi0) QuickSort(a, lo, hi0);
        }
    }

    private static void swap(int[] array, int i, int r) {
        int aux = array[i];
        array[i] = array[r];
        array[r] = aux;
    }

    static String readLn() {
        String newLine = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
        int car = -1;
        try {
            car = System.in.read();
            while ((car > 0) && (car != newLine.charAt(0))) {
                buffer.append((char) car);
                car = System.in.read();
            }
            if (car == newLine.charAt(0)) System.in.skip(newLine.length() - 1);
        } catch (java.io.IOException e) {
            return (null);
        }
        if ((car < 0) && (buffer.length() == 0)) return (null);
        return (buffer.toString()).trim();
    }
}
