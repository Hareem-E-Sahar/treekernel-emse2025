package org.quinn.sudoku.eng;

import java.util.Random;

public class Generator {

    Random random = new Random();

    /**
	 * creates a random permutation of the elements of a
	 */
    void permute(int[] a, int n) {
        for (int i = 0; i < n; i++) {
            int j = random.nextInt(i + 1);
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    /**
	 * reports which digits have been used in the same row, column or block
	 * in the form of a bitvector.
	 */
    private static int findUsedDigits(int[][] s, int x, int y) {
        int bx = x - x % 3;
        int by = y - y % 3;
        int used = 0;
        for (int i = 0; i < 9; i++) used |= (1 << s[x][i]) | (1 << s[i][y]) | (1 << s[bx + i % 3][by + i / 3]);
        return used;
    }

    private boolean create(int[][] s, int[] order, int todo) {
        if (todo == 0) return true;
        int o = order[todo - 1];
        int x = o % 9;
        int y = o / 9;
        int used = findUsedDigits(s, x, y);
        int[] poss = new int[9];
        int nposs = 0;
        for (int i = 1; i <= 9; i++) if ((used & (1 << i)) == 0) poss[nposs++] = i;
        permute(poss, nposs);
        System.out.print("create:" + todo + " ");
        for (int i = 0; i < nposs; i++) {
            System.out.print(poss[i] + " ");
        }
        System.out.println();
        for (int i = 0; i < nposs; i++) {
            s[x][y] = poss[i];
            if (create(s, order, todo - 1)) return true;
        }
        s[x][y] = 0;
        return false;
    }

    /**
	 * creates a random sudoku puzzle.
	 * s[][] should be initialised with zeroes.
	 */
    public void create(int[][] s) {
        int[] order = new int[81];
        for (int i = 0; i < 81; i++) order[i] = i;
        permute(order, 81);
        create(s, order, 81);
    }

    public int[][] create() {
        int[][] s = new int[9][9];
        create(s);
        return s;
    }
}
