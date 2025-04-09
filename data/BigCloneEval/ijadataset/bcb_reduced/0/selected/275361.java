package edu.gsbme.geometrykernel.algorithm;

import java.math.BigDecimal;

/**
 * Spline related math functions
 * @author David
 *
 */
public class SplineUtility {

    /**
	 * Find knot index such that ui <= u < ui+1
	 * 
	 * m = n+p+1
	 * where number of knots = m+1
	 * number of control points n+1
	 * degree p
	 * 
	 * @param n control points -1
	 * @param p degree-1
	 * @param u
	 * @param U knot vector
	 * @return i
	 */
    public static int findSpan(int n, int p, double u, double[] U) {
        BigDecimal s_case = new BigDecimal(U[n + 1]);
        double d_s_case = s_case.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (u == d_s_case) return n;
        int high = n + 1;
        int low = p;
        int mid = (low + high) / 2;
        BigDecimal u_mid = new BigDecimal(U[mid]);
        double d_u_mid = u_mid.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue();
        BigDecimal u_mid1 = new BigDecimal(U[mid + 1]);
        double d_u_mid1 = u_mid1.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue();
        while (u < d_u_mid || u >= d_u_mid1) {
            if (u < d_u_mid) high = mid; else low = mid;
            mid = (low + high) / 2;
            u_mid = new BigDecimal(U[mid]);
            d_u_mid = u_mid.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue();
            u_mid1 = new BigDecimal(U[mid + 1]);
            d_u_mid1 = u_mid1.setScale(12, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return mid;
    }

    public static double[] basisFunction(int i, int p, double u, double[] U) {
        double[] N = new double[p + 1];
        double[] left = new double[p + 1];
        double[] right = new double[p + 1];
        N[0] = 1.0;
        for (int j = 1; j <= p; j++) {
            left[j] = u - U[i + 1 - j];
            right[j] = U[i + j] - u;
            double saved = 0.0;
            for (int r = 0; r < j; r++) {
                double temp = N[r] / (right[r + 1] + left[j - r]);
                N[r] = saved + right[r + 1] * temp;
                saved = left[j - r] * temp;
            }
            N[j] = saved;
        }
        return N;
    }
}
