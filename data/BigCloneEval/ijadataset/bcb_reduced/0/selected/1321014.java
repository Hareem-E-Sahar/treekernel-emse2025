package edu.gsbme.geometrykernel.algorithm;

/**
 * Math Utility in support of Bezier Utilities
 * These includes
 * 
 * Berstein Polynomial
 * deCastelijau Algorithm
 * Rational deCasteljau
 * Transpose Matrices
 * 
 * TODO : Blossom Algorithm
 * 
 * @author David
 *
 */
public class MathUtility {

    public static void main(String[] arg) {
        double[] weight = { 1, 0.5, 1 };
        double[] x = { 0, 0, 8, 4 };
        double[] y = { 0, 2, 2, 0 };
        double t = 0.5f;
        double[] xstraight = { 0, 0.5, 1 };
        double[] xweight = { 1, 1, 1, 1 };
        System.out.println(deCasteljau(1, 1, t, x));
        System.out.println(RationaldeCasteljau(1, 1, 0.5, x, xweight));
        double[] newX = { 0, 1, 2, 3 };
        System.out.println(CubicBlossoms(1, 1, 1, newX));
    }

    /**
	 * To find the value at (u,v,w), set r to the degree, |i| = degree - r
	 * @param i
	 * @param j
	 * @param k
	 * @param r
	 * @param u
	 * @param v
	 * @param w
	 * @param control_points
	 * @param degree
	 * @return
	 */
    public static double deCasteljauTriangle(int i, int j, int k, int r, double u, double v, double w, double[] control_points, int degree) {
        if (r == 0) {
            int index = getTriangleToArrayIndex(i, j, degree);
            return control_points[index];
        }
        return u * deCasteljauTriangle(i + 1, j, k, r - 1, u, v, w, control_points, degree) + v * deCasteljauTriangle(i, j + 1, k, r - 1, u, v, w, control_points, degree) + w * deCasteljauTriangle(i, j, k + 1, r - 1, u, v, w, control_points, degree);
    }

    public static int getTriangleToArrayIndex(int a, int b, int n) {
        int row = Math.abs(b - (n));
        int offset = new Double(0.5 * ((row - 1) + 1) * ((row - 1) + 2)).intValue();
        return a + offset;
    }

    /**
	 * For Non rational cases c_i = b_i_0(c), so for the [0,c_3], c_0 -> b_0_0(c), c_1 -> b_1_0(c), c_2 -> b_2_0(c), c_3 -> b_3_0 
	 * @param i
	 * @param r
	 * @param t
	 * @param controlpoints
	 * @return
	 */
    public static double deCasteljau(int i, int r, double t, double[] controlpoints) {
        if (r == 0) return controlpoints[i];
        return (1 - t) * deCasteljau(i, r - 1, t, controlpoints) + t * deCasteljau(i + 1, r - 1, t, controlpoints);
    }

    public static double RationaldeCasteljau(int i, int r, double t, double[] controlpoints, double[] weights) {
        if (r == 0) return controlpoints[i];
        double wrt = deCasteljau(i, r, t, weights);
        return (1 - t) * (deCasteljau(i, r - 1, t, weights) / wrt) * RationaldeCasteljau(i, r - 1, t, controlpoints, weights) + t * (deCasteljau(i + 1, r - 1, t, weights) / wrt) * RationaldeCasteljau(i + 1, r - 1, t, controlpoints, weights);
    }

    public static double combination(double n, double i) {
        if (i == 0) {
            return 1;
        }
        if (i == 1) {
            return n;
        }
        return n / i * combination(n - 1, i - 1);
    }

    /**
	 * n!/(i!j!k!)
	 * @param n
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
    public static double combination(double n, double i, double j, double k) {
        double top = 1;
        for (double w = n; w > 1; w--) {
            top *= n;
        }
        double botI = 1, botJ = 1, botK = 1;
        for (double w = i; w > 1; w--) {
            botI *= i;
        }
        for (double w = j; w > 1; w--) {
            botJ *= j;
        }
        for (double w = k; w > 1; w--) {
            botK *= k;
        }
        return top / (botI * botJ * botK);
    }

    /**
	 * Berstein Poly
	 * @param n
	 * @param i
	 * @param t
	 * @return
	 */
    public static double bernstein(int n, int i, double t) {
        return combination(n, i) * Math.pow(t, i) * Math.pow((1 - t), (n - i));
    }

    public static double[][] transpose(double[][] matrix) {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i][j] = matrix[j][i];
            }
        }
        return result;
    }

    public static double CubicBlossoms(double[] t, double[] ctrlpts) {
        if (t.length != 3) {
            System.out.println("ERROR : t length does not equal to Cubic @ MathUtility@CubicBlossoms");
        }
        if (ctrlpts.length != 4) {
            System.out.println("ERROR : Control Points length does not equal to Cubic @ MathUtility@CubicBlossoms");
        }
        return CubicBlossoms(t[0], t[1], t[2], ctrlpts);
    }

    public static double CubicBlossoms(double t1, double t2, double t3, double[] ctrlpts) {
        if (ctrlpts.length != 4) {
            System.out.println("ERROR : Control Points length does not equal to Cubic @ MathUtility@CubicBlossoms");
        }
        double a1 = ctrlpts[0] * (1 - t1) * (1 - t2) * (1 - t3);
        double a2 = ctrlpts[1] * ((1 - t1) * (1 - t2) * t3 + (1 - t1) * (1 - t3) * t2 + (1 - t3) * (1 - t2) * t1);
        double a3 = ctrlpts[2] * ((1 - t3) * t1 * t2 + (1 - t2) * t1 * t3 + (1 - t1) * t3 * t2);
        double a4 = ctrlpts[3] * t1 * t2 * t3;
        return a1 + a2 + a3 + a4;
    }

    public static double QuadBlossoms(double[] t, double[] ctrlpts) {
        if (t.length != 2) {
            System.out.println("ERROR : t length does not equal to Dgree of 2 @ MathUtility@QuadBlossoms");
        }
        if (ctrlpts.length != 3) {
            System.out.println("ERROR : Control Points length does not equal to Dgree of 2 @ MathUtility@QuadBlossoms");
        }
        return QuadBlossoms(t[0], t[1], ctrlpts);
    }

    public static double QuadBlossoms(double t1, double t2, double[] ctrlpts) {
        if (ctrlpts.length != 3) {
            System.out.println("ERROR : Control Points length does not equal to Degree of 2 @ MathUtility@QuadBlossoms");
        }
        double a1 = ctrlpts[0] * (1 - t1) * (1 - t2);
        double a2 = ctrlpts[1] * (t2 * (1 - t1) + t1 * (1 - t2));
        double a3 = ctrlpts[2] * t1 * t2;
        return a1 + a2 + a3;
    }

    /**
	 * The algorithm for finite difference is as follows
	 * 
	 * D(0)(i) = ControlPoint(i)
	 * D(k)(i) = D(k-1)(i+1) - D(k-1)(i)   0 < i < n-k
	 * 
	 * 
	 * @param k
	 * @param i
	 * @param controlpoints
	 * @return
	 */
    public static double FiniteDifference(int k, int i, double[] controlpoints) {
        if (k == 0) return controlpoints[i];
        return FiniteDifference(k - 1, i + 1, controlpoints) - FiniteDifference(k - 1, i, controlpoints);
    }

    /**
	 * Page 256-257 CAGD
	 * @param r
	 * @param s
	 * @param i
	 * @param j
	 * @param ControlPoints
	 * @return
	 */
    public static double ForwardDifference(int r, int s, int i, int j, double[][] ControlPoints) {
        if (r == 1 && s == 0) {
            return ControlPoints[j][i + 1] - ControlPoints[j][i];
        } else if (r == 0 && s == 1) {
            return ControlPoints[j + 1][i] - ControlPoints[j][i];
        }
        if (s == 0 && r != 0) {
            return ForwardDifference(r - 1, 0, i + 1, j, ControlPoints) - ForwardDifference(r - 1, 0, i, j, ControlPoints);
        } else {
            return ForwardDifference(0, s - 1, i, j + 1, ControlPoints) - ForwardDifference(0, s - 1, i, j, ControlPoints);
        }
    }
}
