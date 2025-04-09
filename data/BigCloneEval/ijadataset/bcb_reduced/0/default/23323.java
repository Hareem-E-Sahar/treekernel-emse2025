import java.util.HashMap;
import java.util.HashSet;

public class WordsGame {

    HashMap<Character, Integer> charMap;

    public int minimumSwaps(String[] grid, String word) {
        int n = word.length();
        char[][] charGrid = new char[n][];
        for (int i = 0; i < n; i++) charGrid[i] = grid[i].toCharArray();
        charMap = new HashMap<Character, Integer>();
        for (int i = 0; i < n; i++) charMap.put(word.charAt(i), i);
        int minrow = minSwaps(charGrid);
        for (int i = 0; i < n - 1; i++) for (int j = i + 1; j < n; j++) {
            char temp = charGrid[i][j];
            charGrid[i][j] = charGrid[j][i];
            charGrid[j][i] = temp;
        }
        int mincol = minSwaps(charGrid);
        if (minrow < 0) return mincol; else if (mincol < 0) return minrow; else return Math.min(minrow, mincol);
    }

    private int minSwaps(char[][] charGrid) {
        int min = -1;
        for (int i = 0; i < charGrid.length; i++) {
            int s = minSwaps(charGrid[i]);
            if (s >= 0 && (min == -1 || min > s)) {
                min = s;
            }
        }
        return min;
    }

    private int minSwaps(char[] row) {
        int[] positions = new int[row.length];
        HashSet<Character> set = new HashSet<Character>();
        for (int i = 0; i < row.length; i++) {
            if (!charMap.containsKey(row[i])) return -1;
            positions[i] = charMap.get(row[i]);
            set.add(row[i]);
        }
        if (set.size() < row.length) return -1;
        int count = 0;
        int next = findNext(0, positions);
        while (next >= 0) {
            int p = positions[next];
            positions[next] = positions[p];
            positions[p] = p;
            count++;
            if (positions[next] == next) next = findNext((next + 1) % row.length, positions);
        }
        return count;
    }

    private int findNext(int k, int[] positions) {
        int count = 0;
        while (true) {
            if (positions[k] != k) return k;
            k = (k + 1) % positions.length;
            count++;
            if (count > positions.length) return -1;
        }
    }

    public static void main(String[] args) {
        System.out.println(new WordsGame().minimumSwaps(new String[] { "Mu", "uM" }, "Mu"));
    }
}
