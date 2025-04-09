package com.android.nb.model;

import java.util.ArrayList;
import java.util.Collections;

public class Number {

    private int[][] ss = new int[5][5];

    int[][] findSolutionGrid = new int[5][5];

    private int[] createInventoryShuffleControlArray = new int[10];

    private int[] findSolutionShuffleControlArray = new int[10];

    protected static ArrayList<NumberGroup> createInventoryControlNumberGroups = new ArrayList<NumberGroup>();

    protected static ArrayList<NumberGroup> findSolutionControlNumberGroups = new ArrayList<NumberGroup>();

    public Number() {
        initSsGrid();
        initShuffleControlArrays();
        initControlNumberGroups();
    }

    private void initSsGrid() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < i + 1; j++) {
                ss[i][j] = (int) (Math.random() * 9);
                ss[j][i] = ss[i][j];
            }
        }
    }

    private void initShuffleControlArrays() {
        for (int i = 0; i < 10; i++) {
            createInventoryShuffleControlArray[i] = i;
        }
        for (int i = 0; i < 10; i++) {
            findSolutionShuffleControlArray[i] = i;
        }
    }

    private static void exch(int[] a, int i, int j) {
        int swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    private static void shuffleControlArrays(int[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N - i));
            exch(a, i, r);
        }
    }

    public ArrayList<NumberGroup> getNumberGroups() {
        ArrayList<NumberGroup> numberGroups = new ArrayList<NumberGroup>();
        shuffleControlArrays(createInventoryShuffleControlArray);
        for (int i = 0; i < 10; i++) {
            numberGroups.add(i, createInventoryControlNumberGroups.get(createInventoryShuffleControlArray[i]));
        }
        return numberGroups;
    }

    private void initControlNumberGroups() {
        NumberGroup one = new NumberGroup();
        NumberGroup two = new NumberGroup();
        NumberGroup three = new NumberGroup();
        NumberGroup four = new NumberGroup();
        NumberGroup five = new NumberGroup();
        NumberGroup six = new NumberGroup();
        NumberGroup seven = new NumberGroup();
        NumberGroup eight = new NumberGroup();
        NumberGroup nine = new NumberGroup();
        NumberGroup ten = new NumberGroup();
        one.length = 3;
        one.numbers.add(ss[0][0]);
        one.numbers.add(ss[1][0]);
        one.numbers.add(ss[2][0]);
        one.isVertical = true;
        createInventoryControlNumberGroups.add(one);
        two.length = 3;
        two.numbers.add(ss[2][1]);
        two.numbers.add(ss[3][1]);
        two.numbers.add(ss[4][1]);
        two.isVertical = true;
        createInventoryControlNumberGroups.add(two);
        three.length = 3;
        three.numbers.add(ss[1][4]);
        three.numbers.add(ss[2][4]);
        three.numbers.add(ss[3][4]);
        three.isVertical = true;
        createInventoryControlNumberGroups.add(three);
        four.length = 3;
        four.numbers.add(ss[1][1]);
        four.numbers.add(ss[1][2]);
        four.numbers.add(ss[1][3]);
        four.isHorizontal = true;
        createInventoryControlNumberGroups.add(four);
        five.length = 3;
        five.numbers.add(ss[4][2]);
        five.numbers.add(ss[4][3]);
        five.numbers.add(ss[4][4]);
        five.isHorizontal = true;
        createInventoryControlNumberGroups.add(five);
        six.length = 2;
        six.numbers.add(ss[3][0]);
        six.numbers.add(ss[4][0]);
        six.isVertical = true;
        createInventoryControlNumberGroups.add(six);
        seven.length = 2;
        seven.numbers.add(ss[2][2]);
        seven.numbers.add(ss[3][2]);
        seven.isVertical = true;
        createInventoryControlNumberGroups.add(seven);
        eight.length = 2;
        eight.numbers.add(ss[2][3]);
        eight.numbers.add(ss[3][3]);
        eight.isVertical = true;
        createInventoryControlNumberGroups.add(eight);
        nine.length = 2;
        nine.numbers.add(ss[0][1]);
        nine.numbers.add(ss[0][2]);
        nine.isHorizontal = true;
        createInventoryControlNumberGroups.add(nine);
        ten.length = 2;
        ten.numbers.add(ss[0][3]);
        ten.numbers.add(ss[0][4]);
        ten.isHorizontal = true;
        createInventoryControlNumberGroups.add(ten);
    }

    public boolean verifyGrid(int[][] ss) {
        boolean result = true;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (result) {
                    if (ss[i][j] != ss[j][i]) result = false;
                }
            }
        }
        return result;
    }

    public int[][] findSolution(ArrayList<NumberGroup> numberGroups) {
        boolean gridControl = false;
        boolean result = false;
        while (!result) {
            while (!gridControl) {
                gridControl = true;
                findSolutionControlNumberGroups.clear();
                findSolutionControlNumberGroups.addAll(numberGroups);
                shuffleFindSolutionControlNumberGroups();
                numberGroups.clear();
                numberGroups.addAll(findSolutionControlNumberGroups);
                findSolutionGrid = initGiveUpSolution(findSolutionGrid);
                for (int i = 0; i < 10; i++) {
                    if (gridControl) {
                        if (isAvailable(numberGroups.get(i))) {
                            putBlockInFindSolutionGrid(numberGroups.get(i));
                        } else gridControl = false;
                    }
                }
            }
            result = verifyGrid(findSolutionGrid);
            gridControl = false;
        }
        return findSolutionGrid;
    }

    private void putBlockInFindSolutionGrid(NumberGroup numberGroup) {
        boolean first = false;
        if (numberGroup.isHorizontal) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (findSolutionGrid[i][j] == 11) {
                        if (!first) {
                            first = true;
                            for (int k = 0; k < numberGroup.numbers.size(); k++) {
                                findSolutionGrid[i][j + k] = numberGroup.numbers.get(k);
                            }
                        }
                    }
                }
            }
        }
        if (numberGroup.isVertical) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (findSolutionGrid[i][j] == 11) {
                        if (!first) {
                            first = true;
                            for (int k = 0; k < numberGroup.numbers.size(); k++) {
                                findSolutionGrid[i + k][j] = numberGroup.numbers.get(k);
                            }
                        }
                    }
                }
            }
        }
    }

    private void shuffleFindSolutionControlNumberGroups() {
        shuffleControlArrays(findSolutionShuffleControlArray);
        ArrayList<NumberGroup> shuffleControlNumberGroups = new ArrayList<NumberGroup>();
        for (int i = 0; i < 10; i++) {
            shuffleControlNumberGroups.add(findSolutionControlNumberGroups.get(findSolutionShuffleControlArray[i]));
        }
        findSolutionControlNumberGroups.clear();
        findSolutionControlNumberGroups.addAll(shuffleControlNumberGroups);
    }

    private int[][] initGiveUpSolution(int[][] giveUpSolution) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                giveUpSolution[i][j] = 11;
            }
        }
        return giveUpSolution;
    }

    private boolean isAvailable(NumberGroup numberGroup) {
        boolean result = true;
        boolean first = false;
        if (numberGroup.isHorizontal) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (findSolutionGrid[i][j] == 11) {
                        if (!first) {
                            first = true;
                            int k = 0;
                            while (k < numberGroup.numbers.size()) {
                                if ((j + k) < 5) {
                                    if (findSolutionGrid[i][j + k] != 11) {
                                        result = false;
                                    }
                                    k++;
                                } else {
                                    k++;
                                    result = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (numberGroup.isVertical) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (findSolutionGrid[i][j] == 11) {
                        if (!first) {
                            first = true;
                            int k = 0;
                            while (k < numberGroup.numbers.size()) {
                                if ((i + k) < 5) {
                                    if (findSolutionGrid[i + k][j] != 11) {
                                        result = false;
                                    }
                                    k++;
                                } else {
                                    k++;
                                    result = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
