public class Test {    public CMatrix transpose() {
        CMatrix X = new CMatrix(n, m);
        Complex[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return X;
    }
}