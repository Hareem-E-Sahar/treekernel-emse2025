package util;

import java.util.Random;

public class Permutation {

    public static void main(String[] args) {
        int N = 20;
        int[] a = Permutation.permute(N, new Random());
        for (int i = 0; i < N; i++) System.out.print(a[i] + " ");
        System.out.println("");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (a[j] == i) System.out.print("* "); else System.out.print(". ");
            }
            System.out.println("");
        }
    }

    public static int[] permute(int N, Random rand) {
        int[] a = new int[N];
        for (int i = 0; i < N; i++) a[i] = i;
        for (int i = 0; i < N; i++) {
            int r = rand.nextInt(i + 1);
            int swap = a[r];
            a[r] = a[i];
            a[i] = swap;
        }
        return a;
    }
}
