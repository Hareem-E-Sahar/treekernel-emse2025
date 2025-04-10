package corina.index;

/**
    A collection of matrix solvers.

    <p><code>solveNxN</code> will solve any square matrix system.  In
    solving Ax=b, it will destroy A (and b?).  The
    routines were adapted from chapter 6 of <i>Introduction to
    Scientific Computing</i>, second edition, Charles van Loan.  The
    solver uses LU decomposition with pivoting.</p>

    <p>Also contains a least-squares fitter,
    <code>leastSquares()</code>, from <i>Introduction to
    Algorithms</i>, Cormen, Leiserson, and Rivest, pp. 768-771, which
    uses the matrix solvers.</p>
    
    @see Function
    @see SingularMatrixException

    @author Ken Harris &lt;kbh7 <i style="color: gray">at</i> cornell <i style="color: gray">dot</i> edu&gt;
    @version $Id: Solver.java,v 1.2 2004/01/18 18:03:08 aaron Exp $
*/
public class Solver {

    private Solver() {
    }

    /** Lower-upper decomposition of a matrix; returns the permutation
	vector.  See van Loan, p. 228.
	@param A the matrix to decompose
	@return the permutation vector
	@exception IllegalArgumentException if A is not square
	@exception SingularMatrixException if A is singular */
    private static int[] GEpiv(double A[][]) throws SingularMatrixException {
        int n = A.length;
        if (A[0].length != n) throw new IllegalArgumentException("Not square");
        int piv[] = new int[n];
        for (int i = 0; i < n; i++) piv[i] = i;
        for (int k = 0; k < n - 1; k++) {
            double maxr = 0.;
            int r = 0;
            for (int i = k; i < n; i++) if (Math.abs(A[i][k]) > maxr) {
                maxr = Math.abs(A[i][k]);
                r = i;
            }
            {
                int tmp = piv[k];
                piv[k] = piv[r];
                piv[r] = tmp;
            }
            for (int i = 0; i < n; i++) {
                double tmp = A[k][i];
                A[k][i] = A[r][i];
                A[r][i] = tmp;
            }
            if (A[k][k] != 0.) {
                for (int i = k + 1; i < n; i++) {
                    A[i][k] /= A[k][k];
                    for (int j = k + 1; j < n; j++) A[i][j] -= A[i][k] * A[k][j];
                }
            }
        }
        return piv;
    }

    /** Solves the nonsingular lower-triangular system Lx=b.  See van
	Loan, p. 211.
	@param L
	@param b
	@return x
	@exception IllegalArgumentException if L isn't square, or b
	isn't the same size */
    private static double[] LTriSol(double L[][], double b[]) {
        int n = L.length;
        if (L[0].length != n || b.length != n) throw new IllegalArgumentException("Wrong size");
        double x[] = new double[n];
        for (int j = 0; j < n - 1; j++) {
            x[j] = b[j] / L[j][j];
            for (int i = j + 1; i < n; i++) b[i] -= L[i][j] * x[j];
        }
        x[n - 1] = b[n - 1] / L[n - 1][n - 1];
        return x;
    }

    /** Solves the nonsingular upper-triangular system Ux=b.  See van
	Loan, p. 212.
	@param U
	@param b
	@return x
	@exception IllegalArgumentException if L isn't square, or b
	isn't the same size */
    private static double[] UTriSol(double U[][], double b[]) {
        int n = U.length;
        if (U[0].length != n || b.length != n) throw new IllegalArgumentException("Wrong size");
        double x[] = new double[n];
        for (int j = n - 1; j > 0; j--) {
            x[j] = b[j] / U[j][j];
            for (int i = 0; i < j; i++) b[i] -= x[j] * U[i][j];
        }
        x[0] = b[0] / U[0][0];
        return x;
    }

    /** Solve the general equation Ax=b for x, given square matrix A
	and vector b.  (Intended to be partially compatible with
	Numerical Recipes' <code>gaussj</code>, which it replaces.)
	See van Loan for derivation.
	@param A the "A" matrix in Ax=b
	@param b the "b" matrix in Ax=b; it is replaced with x
	@exception IllegalArgumentException if A is not square, or b
	is a different size
	@exception SingularMatrixException if A is singular */
    public static double[] solveNxN(double A[][], double b[]) throws SingularMatrixException {
        if (A.length == 2 && A[0].length == 2 && b.length == 2) return solve2x2(A, b);
        int n = A.length;
        if (A[0].length != n || b.length != n) throw new IllegalArgumentException("Wrong size");
        int piv[] = GEpiv(A);
        double L[][] = new double[n][n];
        double U[][] = new double[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (j >= i) U[i][j] = A[i][j]; else L[i][j] = A[i][j];
        for (int i = 0; i < n; i++) L[i][i] = 1.;
        {
            double tmp[] = new double[n];
            for (int i = 0; i < n; i++) tmp[i] = b[piv[i]];
            for (int i = 0; i < n; i++) b[i] = tmp[i];
        }
        double y[] = LTriSol(L, b);
        double x[] = UTriSol(U, y);
        return x;
    }

    /** A special case of <code>solveNxN</code> for 2x2 matrices.  In
	the equation Ax=b, given A and b, x is found; its value is
	written back into b.  Direct substitution is used, so it is
	very fast (only 10 floating-point operations).
	@param A the "A" matrix in Ax=b; it is untouched
	@param b the "b" matrix in Ax=b; it is replaced with x
	@exception IllegalArgumentException if A is any size other
	than 2x2 or b is any size other than 2
	@exception SingularMatrixException if A is singular */
    public static double[] solve2x2(double A[][], double b[]) throws SingularMatrixException {
        if (A.length != 2 || A[0].length != 2 || b.length != 2) throw new IllegalArgumentException("Wrong size");
        try {
            double x0 = (b[1] - b[0] * A[1][1] / A[0][1]) / (A[1][0] - A[1][1] * A[0][0] / A[0][1]);
            double x1 = (b[0] - A[0][0] * x0) / A[0][1];
            return new double[] { x0, x1 };
        } catch (ArithmeticException ae) {
            throw new SingularMatrixException();
        }
    }

    /** A least-squares solver.  See <i>Introduction to
        Algorithms</i>, Cormen, Leiserson, and Rivest, pp. 768-771.
        This uses <code>solveNxN</code> or <code>solve2x2</code>.
	@param s an object that can evaluate the basis functions
	@param x the x-coordinates of the data
	@param y the y-coordinates of the data
	@return the coefficients of the basis functions
	@exception SingularMatrixException (can this happen?) */
    public static double[] leastSquares(Function s, double x[], double y[]) throws SingularMatrixException {
        int n = x.length;
        int m = s.f(0.).length;
        double A[][] = new double[n][];
        for (int i = 0; i < n; i++) A[i] = s.f(x[i]);
        double S[][] = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= i; j++) {
                S[i][j] = 0.;
                for (int k = 0; k < n; k++) S[i][j] += A[k][i] * A[k][j];
                S[j][i] = S[i][j];
            }
        }
        double T[] = new double[m];
        for (int i = 0; i < m; i++) {
            T[i] = 0.;
            for (int j = 0; j < n; j++) T[i] += A[j][i] * y[j];
        }
        return solveNxN(S, T);
    }
}
