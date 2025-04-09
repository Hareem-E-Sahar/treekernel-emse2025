package org.systemsbiology.utils;

import org.systemsbiology.qualscore.features.MasterFeatureGenerator;
import JSci.maths.DoubleSquareMatrix;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * DOCU add javadoc comment
 * 
 * @author M. Vogelzang
 */
public class LDA {

    public static class LDAFunction {

        double[] coefficients;

        double constant;

        public double calculate(double[] values) {
            double value = constant;
            for (int i = 0; i < values.length; i++) {
                value += coefficients[i] * values[i];
            }
            return value;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("constant = ").append(constant).append("\n");
            String[] featureNames = MasterFeatureGenerator.getFeatureNames();
            for (int i = 0; i < coefficients.length; i++) {
                sb.append(featureNames[i]).append("= ").append(coefficients[i]).append("\n");
            }
            return sb.toString();
        }

        public double getCoefficient(int index) {
            return coefficients[index];
        }

        public double getConstant() {
            return constant;
        }
    }

    private static LDAFunction getFunctionFromCovarianceMatrix(double[][] covariance, double[][] groupMeans) {
        DoubleSquareMatrix matrix = new DoubleSquareMatrix(covariance);
        DoubleSquareMatrix inverse = matrix.inverse();
        int featureCount = covariance.length;
        LDAFunction function = new LDAFunction();
        function.coefficients = new double[featureCount];
        for (int j1 = 0; j1 < featureCount; j1++) {
            for (int j2 = 0; j2 < featureCount; j2++) {
                function.constant += -((groupMeans[0][j1] + groupMeans[1][j1]) / 2) * inverse.getElement(j2, j1) * (groupMeans[0][j2] - groupMeans[1][j2]);
                function.coefficients[j1] += inverse.getElement(j2, j1) * (groupMeans[0][j2] - groupMeans[1][j2]);
            }
        }
        return function;
    }

    public static LDAFunction calcLDA(double[][] features1, double[][] features2) {
        if (features1.length == 0 || features2.length == 0) throw new IllegalArgumentException("Both feature arrays must contain entries");
        int groupCount = 2;
        int featureCount = features1[0].length;
        double[] featMeans = new double[featureCount];
        double[][] groupMeans = new double[groupCount][featureCount];
        for (int i = 0; i < features1.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                groupMeans[0][j] += features1[i][j];
                featMeans[j] += features1[i][j];
            }
        }
        for (int i = 0; i < features2.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                groupMeans[1][j] += features2[i][j];
                featMeans[j] += features2[i][j];
            }
        }
        for (int j = 0; j < featureCount; j++) {
            groupMeans[0][j] /= features1.length;
            groupMeans[1][j] /= features2.length;
            featMeans[j] /= features1.length + features2.length;
        }
        double[][] covariance = new double[featureCount][featureCount];
        for (int i = 0; i < featureCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                if (j >= i) {
                    for (int k = 0; k < features1.length; k++) {
                        covariance[i][j] += (features1[k][i] - featMeans[i]) * (features1[k][j] - featMeans[j]);
                    }
                    for (int k = 0; k < features2.length; k++) {
                        covariance[i][j] += (features2[k][i] - featMeans[i]) * (features2[k][j] - featMeans[j]);
                    }
                    covariance[i][j] /= features1.length + features2.length;
                } else {
                    covariance[i][j] = covariance[j][i];
                }
            }
        }
        return getFunctionFromCovarianceMatrix(covariance, groupMeans);
    }

    public static LDAFunction calcLDA(int[] groupNo, double[][] features) {
        if (groupNo.length != features.length) throw new IllegalArgumentException("Number of items in groupNo must be same as features!");
        if (groupNo.length == 0) throw new IllegalArgumentException("groupNo.length == 0");
        for (int i = 0; i < groupNo.length; i++) {
            if (groupNo[i] != 0 && groupNo[i] != 1) throw new IllegalArgumentException("Only instances with 2 groups allowed!");
        }
        int groupCount = 2;
        int featureCount = features[0].length;
        double[] featMeans = new double[featureCount];
        double[][] groupMeans = new double[groupCount][featureCount];
        int[] groupN = new int[groupCount];
        for (int i = 0; i < groupNo.length; i++) {
            for (int j = 0; j < featureCount; j++) {
                featMeans[j] += features[i][j];
                groupMeans[groupNo[i]][j] += features[i][j];
            }
            groupN[groupNo[i]]++;
        }
        for (int j = 0; j < featureCount; j++) {
            featMeans[j] /= groupNo.length;
            for (int i = 0; i < groupCount; i++) {
                groupMeans[i][j] /= groupN[i];
            }
        }
        double[][] covariance = new double[featureCount][featureCount];
        for (int i = 0; i < featureCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                if (j >= i) {
                    for (int k = 0; k < groupNo.length; k++) {
                        covariance[i][j] += (features[k][i] - featMeans[i]) * (features[k][j] - featMeans[j]);
                    }
                    covariance[i][j] /= groupNo.length;
                } else {
                    covariance[i][j] = covariance[j][i];
                }
            }
        }
        return getFunctionFromCovarianceMatrix(covariance, groupMeans);
    }

    public static LDAFunction getClassifierFromFile(String filename) {
        LDAFunction function = new LDAFunction();
        ArrayList<BigDecimal> factors = new ArrayList<BigDecimal>();
        factors = IOUtils.parseClassifierFile(filename);
        if (factors.size() == 0) {
            System.out.println("Could not retrieve the classifier's coefficients from file.");
            System.exit(1);
        }
        function.constant = factors.get(0).doubleValue();
        int numFactors = factors.size();
        function.coefficients = new double[numFactors - 1];
        int indexCoef = 0;
        for (int i = 1; i < numFactors; i++) {
            function.coefficients[indexCoef++] = factors.get(i).doubleValue();
        }
        return function;
    }
}
