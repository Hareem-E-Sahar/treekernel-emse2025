public class Test {    public static int[][] transpose(int[][] M) {
        int[][] Mt = new int[M[0].length][M.length];
        for (int i = 0; i < M.length; i++) {
            if (M[i].length != M[0].length) {
                throw new IllegalArgumentException("The array is not a matrix.");
            }
            for (int j = 0; j < M[0].length; j++) {
                Mt[j][i] = M[i][j];
            }
        }
        return (Mt);
    }
}