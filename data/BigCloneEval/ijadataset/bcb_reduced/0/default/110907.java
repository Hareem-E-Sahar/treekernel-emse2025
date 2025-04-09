import java.util.*;

class MinMax {

    int findmin(int a[], int l, int r) {
        int mid, min1, min2;
        if (l < r) {
            mid = (l + r) / 2;
            min1 = findmin(a, l, mid);
            min2 = findmin(a, mid + 1, r);
            if (min1 < min2) return min1; else return min2;
        } else return a[l];
    }

    int findmax(int a[], int l, int r) {
        int mid, max1, max2;
        if (l < r) {
            mid = (l + r) / 2;
            max1 = findmax(a, l, mid);
            max2 = findmax(a, mid + 1, r);
            if (max1 < max2) return max2; else return max1;
        } else return a[r];
    }
}

class FindMinMax {

    public static void main(String args[]) {
        Scanner k = new Scanner(System.in);
        int a[];
        int n;
        System.out.println("How many element");
        n = k.nextInt();
        a = new int[n];
        System.out.println("\nEnter the numbers one by one");
        for (int i = 0; i < n; i++) a[i] = k.nextInt();
        MinMax m = new MinMax();
        int max, min;
        max = m.findmax(a, 0, n - 1);
        min = m.findmin(a, 0, n - 1);
        System.out.println("Minimum = " + min + "\nMaximum = " + max);
    }
}
