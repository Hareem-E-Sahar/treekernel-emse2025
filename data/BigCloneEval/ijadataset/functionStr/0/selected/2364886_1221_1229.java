public class Test {    public static int[][] transpose(int[][] M) {
        int[][] tM = new int[M[0].length][M.length];
        for (int i = 0; i < tM.length; i++) {
            for (int j = 0; j < tM[0].length; j++) {
                tM[i][j] = M[j][i];
            }
        }
        return tM;
    }
}