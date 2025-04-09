package magicstudio.util;

/**
 * Math tools. Could be copied to other sources.
 */
public class MathOperation {

    public static double mean(double[] vector) {
        if (vector == null || vector.length == 0) throw new IllegalArgumentException();
        double result = 0;
        for (int i = 0; i < vector.length; i++) {
            result += vector[i];
        }
        return result / vector.length;
    }

    public static double covariance(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorA.length == 0 || vectorB == null || vectorB.length == 0) throw new IllegalArgumentException();
        if (vectorA.length != vectorB.length) throw new IllegalArgumentException("Two vectors need to have the same dimension");
        int n = vectorA.length;
        double meanA = mean(vectorA);
        double meanB = mean(vectorB);
        double[] variance = new double[n];
        for (int i = 0; i < n; i++) {
            variance[i] = (vectorA[i] - meanA) * (vectorB[i] - meanB);
        }
        return mean(variance);
    }

    /**
     * @param matrix Matrix[m][n] is an m by n matrix. There should be m vectors; each vector has n values.
     * @return the m*m covariance matrix.
     */
    public static double[][] covariance(double[][] matrix) {
        if (matrix == null || matrix.length == 0) throw new IllegalArgumentException();
        int m = matrix.length;
        int n = matrix[0].length;
        double[][] covMatrix = new double[m][m];
        double[] vector1;
        double[] vector2;
        double cov;
        for (int v1 = 0; v1 < m; v1++) {
            for (int v2 = v1; v2 < m; v2++) {
                vector1 = matrix[v1];
                vector2 = matrix[v2];
                if (vector1.length != n || vector2.length != n) throw new IllegalArgumentException();
                cov = covariance(vector1, vector2);
                covMatrix[v1][v2] = cov;
                covMatrix[v2][v1] = cov;
            }
        }
        return covMatrix;
    }

    /**
     * @param matrix Matrix[m][n] is an m by n matrix. There should be m vectors; each vector has n values.
     * @return The correlation matrix[m][m]
     */
    public static double[][] correlation(double matrix[][]) {
        double[][] covMatrix = covariance(matrix);
        int m = covMatrix.length;
        double[] vectorDiagonal = new double[m];
        for (int i = 0; i < m; i++) {
            if (covMatrix[i].length != m) throw new IllegalArgumentException();
            vectorDiagonal[i] = Math.sqrt(covMatrix[i][i]);
        }
        double[][] corMatrix = covMatrix;
        for (int v1 = 0; v1 < m; v1++) {
            for (int v2 = v1; v2 < m; v2++) {
                corMatrix[v1][v2] = covMatrix[v1][v2] / (vectorDiagonal[v1] * vectorDiagonal[v2]);
                corMatrix[v2][v1] = corMatrix[v1][v2];
            }
        }
        return corMatrix;
    }

    /**
     * To test whether two double numbers are approximately equal.
     * Relative error < 0.0001
     */
    public static boolean approxEquals(double a, double b) {
        final double TOLERANCE = 0.0001;
        if (a == b) return true;
        if (a == 0d || b == 0d) return false;
        double error = Math.abs(b - a);
        return Math.abs(error / a) <= TOLERANCE && Math.abs(error / b) <= TOLERANCE;
    }
}
