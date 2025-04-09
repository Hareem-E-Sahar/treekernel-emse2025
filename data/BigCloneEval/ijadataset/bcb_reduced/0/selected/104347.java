package org.opensourcephysics.numerics;

/**
 * Polynomial least square fit without any error estimation.
 *
 * See Object Oriented Implementation of Numerical Methods by Didier H. Besset for fitting with error estimation.
 *
 * @author Wolfgang Christian.
 */
public class PolynomialLeastSquareFit extends Polynomial {

    double[][] systemMatrix;

    double[] systemConstants;

    /**
   * Constructs a PolynomialLeastSquareFit with the given order.
   * @param xd double[]
   * @param yd double[]
   * @param degree int the degree of the polynomial
   */
    public PolynomialLeastSquareFit(double[] xd, double[] yd, int degree) {
        super(new double[degree + 1]);
        int ncoef = degree + 1;
        systemMatrix = new double[ncoef][ncoef];
        systemConstants = new double[ncoef];
        fitData(xd, yd);
    }

    /**
   * Constructs a PolynomialLeastSquareFit with the given coefficients.
   * Added by D Brown 12/20/2005.
   * @param coeffs the coefficients
   */
    public PolynomialLeastSquareFit(double[] coeffs) {
        super(coeffs);
        int n = coeffs.length;
        systemMatrix = new double[n][n];
        systemConstants = new double[n];
    }

    /**
   * Sets the data and updates the fit coefficients. Added by D Brown 12/1/05.
   *
   * @param xd double[]
   * @param yd double[]
   */
    public void fitData(double[] xd, double[] yd) {
        if (xd.length != yd.length) {
            throw new IllegalArgumentException("Arrays must be of equal length.");
        }
        if (xd.length < degree() + 1) return;
        for (int i = 0; i < systemConstants.length; i++) {
            systemConstants[i] = 0;
            for (int j = 0; j < systemConstants.length; j++) {
                systemMatrix[i][j] = 0;
            }
        }
        for (int i = 0, n = xd.length; i < n; i++) {
            double xp1 = 1;
            for (int j = 0; j < systemConstants.length; j++) {
                systemConstants[j] += xp1 * yd[i];
                double xp2 = xp1;
                for (int k = 0; k <= j; k++) {
                    systemMatrix[j][k] += xp2;
                    xp2 *= xd[i];
                }
                xp1 *= xd[i];
            }
        }
        computeCoefficients();
    }

    /**
   * Computes the polynomial coefficients.
   */
    protected void computeCoefficients() {
        for (int i = 0; i < systemConstants.length; i++) {
            for (int j = i + 1; j < systemConstants.length; j++) {
                systemMatrix[i][j] = systemMatrix[j][i];
            }
        }
        LUPDecomposition lupSystem = new LUPDecomposition(systemMatrix);
        double[][] components = lupSystem.inverseMatrixComponents();
        LUPDecomposition.symmetrizeComponents(components);
        coefficients = lupSystem.solve(systemConstants);
    }
}
