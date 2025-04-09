package joptima.fortran;

/**
*
*<p>
*This class contains Java translations of the MINPACK nonlinear least 
*squares routines.  As of November 2000, it does not yet contain
*the MINPACK solvers of systems of nonlinear equations.  They should
*be added in the Spring of 2001.<p>
*The original FORTRAN MINPACK package was produced by
*Burton S. Garbow, Kenneth E. Hillstrom, and Jorge J. More
*as part of the Argonne National Laboratory MINPACK project, March 1980.
*
*<p>
*<b>IMPORTANT:</b>  The "_f77" suffixes indicate that these routines use
*FORTRAN style indexing.  For example, you will see
*<pre>
*   for (i = 1; i <= n; i++)
*</pre>
*rather than
*<pre>
*   for (i = 0; i < n; i++)
*</pre>
*To use the "_f77" routines you will have to declare your vectors
*and matrices to be one element larger (e.g., v[101] rather than
*v[100], and a[101][101] rather than a[100][100]), and you will have
*to fill elements 1 through n rather than elements 0 through n - 1.
*Versions of these programs that use C/Java style indexing might
*eventually be available.  They would end with the suffix "_j".
*
*<p>
*This class was translated by a statistician from FORTRAN versions of 
*lmder and lmdif.  It is NOT an official translation.  It wastes
*memory by failing to use the first elements of vectors.  When 
*public domain Java optimization routines become available 
*from the people who produced MINPACK, then <b>THE CODE PRODUCED
*BY THE NUMERICAL ANALYSTS SHOULD BE USED</b>.
*
*<p>
*Meanwhile, if you have suggestions for improving this
*code, please contact Steve Verrill at steve@ws13.fpl.fs.fed.us.
*
*@author (translator)Steve Verrill
*@version .5 --- November 3, 2000
* 
*/
public class Minpack_f77 extends Object {

    static final double epsmch = 2.22044604926e-16;

    static final double minmag = 2.22507385852e-308;

    static final double zero = 0.0;

    static final double one = 1.0;

    static final double p0001 = .0001;

    static final double p001 = .001;

    static final double p05 = .05;

    static final double p1 = .1;

    static final double p25 = .25;

    static final double p5 = .5;

    static final double p75 = .75;

    /**
*
*<p>
*The lmder1_f77 method minimizes the sum of the squares of
*m nonlinear functions in n variables by a modification of the
*Levenberg-Marquardt algorithm.  This is done by using the more
*general least-squares solver lmder_f77.  The user must provide a
*method which calculates the functions and the Jacobian.
*<p>
*Translated by Steve Verrill on November 17, 2000 
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param nlls   A class that implements the Lmder_fcn interface
*              (see the definition in Lmder_fcn.java).  See
*              LmderTest_f77.java for an example of such a class.
*              The class must define a method, fcn, that must
*              have the form
*
*              public static void fcn(int m, int n, double x[],
*              double fvec[], double fjac[][], int iflag[])
*
*              If iflag[1] equals 1, fcn calculates the values of
*              the m functions [residuals] at x and returns this
*              vector in fvec.  If iflag[1] equals 2, fcn calculates
*              the Jacobian at x and returns this matrix in fjac
*              (and does not alter fvec).
*
*              The value of iflag[1] should not be changed by fcn unless
*              the user wants to terminate execution of lmder_f77.
*              In this case set iflag[1] to a negative integer.
*
*@param  m     A positive integer set to the number of functions
*              [number of observations]
*@param  n     A positive integer set to the number of variables
*              [number of parameters].  n must not exceed m.
*@param  x     On input, it contains the initial estimate of
*              the solution vector [the least squares parameters].
*              On output it contains the final estimate of the
*              solution vector.
*@param fvec   An output vector that contains the m functions
*              [residuals] evaluated at x.
*@param fjac   An output m by n array.  The upper n by n submatrix
*              of fjac contains an upper triangular matrix R with
*              diagonal elements of nonincreasing magnitude such that
*<pre>
*                 t    t         t
*                P (jac jac)P = R R,
*</pre>
*              where P is a permutation matrix and jac is the final
*              calculated Jacobian.  Column j of P is column ipvt[j]
*              of the identity matrix.  The lower trapezoidal
*              part of fjac contains information generated during
*              the computation of R.
*@param tol    tol is a nonnegative input variable.  Termination occurs
*              when the algorithm estimates either that the relative
*              error in the sum of squares is at most tol or that
*              the relative error between x and the solution is at
*              most tol.
*@param info   An integer output variable.  If the user has
*              terminated execution, info is set to the (negative)
*              value of iflag[1].  See description of fcn.  Otherwise,
*              info is set as follows.
*
*              info = 0  improper input parameters.
*
*              info = 1  algorithm estimates that the relative error
*                        in the sum of squares is at most tol.
*
*              info = 2  algorithm estimates that the relative error
*                        between x and the solution is at most tol.
*
*              info = 3  conditions for info = 1 and info = 2 both hold.
*
*              info = 4  fvec is orthogonal to the columns of the
*                        Jacobian to machine precision.
*
*              info = 5  number of calls to fcn with iflag[1] = 1 has
*                        reached 100*(n+1).
*
*              info = 6  tol is too small.  No further reduction in
*                        the sum of squares is possible.
*
*              info = 7  tol is too small.  No further improvement in
*                        the approximate solution x is possible.
*@param ipvt   An integer output array of length n.  ipvt
*              defines a permutation matrix P such that jac*P = QR,
*              where jac is the final calculated Jacobian, Q is
*              orthogonal (not stored), and R is upper triangular
*              with diagonal elements of nonincreasing magnitude.
*              Column j of P is column ipvt[j] of the identity matrix.
*
*/
    public static void lmder1_f77(Lmder_fcn nlls, int m, int n, double x[], double fvec[], double fjac[][], double tol, int info[], int ipvt[]) {
        int maxfev, mode, nprint;
        int nfev[] = new int[2];
        int njev[] = new int[2];
        double diag[] = new double[n + 1];
        double qtf[] = new double[n + 1];
        double factor, ftol, gtol, xtol;
        factor = 1.0e+2;
        info[1] = 0;
        if (n <= 0 || m < n || tol < zero) {
            return;
        } else {
            maxfev = 100 * (n + 1);
            ftol = tol;
            xtol = tol;
            gtol = zero;
            mode = 1;
            nprint = 0;
            Minpack_f77.lmder_f77(nlls, m, n, x, fvec, fjac, ftol, xtol, gtol, maxfev, diag, mode, factor, nprint, info, nfev, njev, ipvt, qtf);
            if (info[1] == 8) info[1] = 4;
            return;
        }
    }

    public static void lmder_f77(Lmder_fcn nlls, int m, int n, double x[], double fvec[], double fjac[][], double ftol, double xtol, double gtol, int maxfev, double diag[], int mode, double factor, int nprint, int info[], int nfev[], int njev[], int ipvt[], double qtf[]) {
        int i, iter, j, l;
        double actred, delta, dirder, fnorm, fnorm1, gnorm, pnorm, prered, ratio, sum, temp, temp1, temp2, xnorm;
        double par[] = new double[2];
        boolean doneout, donein;
        int iflag[] = new int[2];
        double wa1[] = new double[n + 1];
        double wa2[] = new double[n + 1];
        double wa3[] = new double[n + 1];
        double wa4[] = new double[m + 1];
        delta = 0.0;
        xnorm = 0.0;
        info[1] = 0;
        iflag[1] = 0;
        nfev[1] = 0;
        njev[1] = 0;
        if (n <= 0 || m < n || ftol < zero || xtol < zero || gtol < zero || maxfev <= 0 || factor <= zero) {
            if (nprint > 0) {
                nlls.fcn(m, n, x, fvec, fjac, iflag);
            }
            return;
        }
        if (mode == 2) {
            for (j = 1; j <= n; j++) {
                if (diag[j] <= zero) {
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, fjac, iflag);
                    }
                    return;
                }
            }
        }
        iflag[1] = 1;
        nlls.fcn(m, n, x, fvec, fjac, iflag);
        nfev[1] = 1;
        if (iflag[1] < 0) {
            info[1] = iflag[1];
            iflag[1] = 0;
            if (nprint > 0) {
                nlls.fcn(m, n, x, fvec, fjac, iflag);
            }
            return;
        }
        fnorm = Minpack_f77.enorm_f77(m, fvec);
        par[1] = zero;
        iter = 1;
        doneout = false;
        while (!doneout) {
            iflag[1] = 2;
            nlls.fcn(m, n, x, fvec, fjac, iflag);
            njev[1]++;
            if (iflag[1] < 0) {
                info[1] = iflag[1];
                iflag[1] = 0;
                if (nprint > 0) {
                    nlls.fcn(m, n, x, fvec, fjac, iflag);
                }
                return;
            }
            if (nprint > 0) {
                iflag[1] = 0;
                if ((iter - 1) % nprint == 0) {
                    nlls.fcn(m, n, x, fvec, fjac, iflag);
                }
                if (iflag[1] < 0) {
                    info[1] = iflag[1];
                    iflag[1] = 0;
                    nlls.fcn(m, n, x, fvec, fjac, iflag);
                    return;
                }
            }
            Minpack_f77.qrfac_f77(m, n, fjac, true, ipvt, wa1, wa2, wa3);
            if (iter == 1) {
                if (mode != 2) {
                    for (j = 1; j <= n; j++) {
                        diag[j] = wa2[j];
                        if (wa2[j] == zero) diag[j] = one;
                    }
                }
                for (j = 1; j <= n; j++) {
                    wa3[j] = diag[j] * x[j];
                }
                xnorm = Minpack_f77.enorm_f77(n, wa3);
                delta = factor * xnorm;
                if (delta == zero) delta = factor;
            }
            for (i = 1; i <= m; i++) wa4[i] = fvec[i];
            for (j = 1; j <= n; j++) {
                if (fjac[j][j] != zero) {
                    sum = zero;
                    for (i = j; i <= m; i++) sum += fjac[i][j] * wa4[i];
                    temp = -sum / fjac[j][j];
                    for (i = j; i <= m; i++) wa4[i] += fjac[i][j] * temp;
                }
                fjac[j][j] = wa1[j];
                qtf[j] = wa4[j];
            }
            gnorm = zero;
            if (fnorm != zero) {
                for (j = 1; j <= n; j++) {
                    l = ipvt[j];
                    if (wa2[l] != zero) {
                        sum = zero;
                        for (i = 1; i <= j; i++) sum += fjac[i][j] * (qtf[i] / fnorm);
                        gnorm = Math.max(gnorm, Math.abs(sum / wa2[l]));
                    }
                }
            }
            if (gnorm <= gtol) info[1] = 4;
            if (info[1] != 0) {
                if (iflag[1] < 0) info[1] = iflag[1];
                iflag[1] = 0;
                if (nprint > 0) {
                    nlls.fcn(m, n, x, fvec, fjac, iflag);
                }
                return;
            }
            if (mode != 2) {
                for (j = 1; j <= n; j++) {
                    diag[j] = Math.max(diag[j], wa2[j]);
                }
            }
            donein = false;
            while (!donein) {
                Minpack_f77.lmpar_f77(n, fjac, ipvt, diag, qtf, delta, par, wa1, wa2, wa3, wa4);
                for (j = 1; j <= n; j++) {
                    wa1[j] = -wa1[j];
                    wa2[j] = x[j] + wa1[j];
                    wa3[j] = diag[j] * wa1[j];
                }
                pnorm = Minpack_f77.enorm_f77(n, wa3);
                if (iter == 1) delta = Math.min(delta, pnorm);
                iflag[1] = 1;
                nlls.fcn(m, n, wa2, wa4, fjac, iflag);
                nfev[1]++;
                if (iflag[1] < 0) {
                    info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, fjac, iflag);
                    }
                    return;
                }
                fnorm1 = Minpack_f77.enorm_f77(m, wa4);
                actred = -one;
                if (p1 * fnorm1 < fnorm) actred = one - (fnorm1 / fnorm) * (fnorm1 / fnorm);
                for (j = 1; j <= n; j++) {
                    wa3[j] = zero;
                    l = ipvt[j];
                    temp = wa1[l];
                    for (i = 1; i <= j; i++) wa3[i] += fjac[i][j] * temp;
                }
                temp1 = Minpack_f77.enorm_f77(n, wa3) / fnorm;
                temp2 = (Math.sqrt(par[1]) * pnorm) / fnorm;
                prered = temp1 * temp1 + temp2 * temp2 / p5;
                dirder = -(temp1 * temp1 + temp2 * temp2);
                ratio = zero;
                if (prered != zero) ratio = actred / prered;
                if (ratio <= p25) {
                    if (actred >= zero) {
                        temp = p5;
                    } else {
                        temp = p5 * dirder / (dirder + p5 * actred);
                    }
                    if (p1 * fnorm1 >= fnorm || temp < p1) temp = p1;
                    delta = temp * Math.min(delta, pnorm / p1);
                    par[1] /= temp;
                } else {
                    if (par[1] == zero || ratio >= p75) {
                        delta = pnorm / p5;
                        par[1] *= p5;
                    }
                }
                if (ratio >= p0001) {
                    for (j = 1; j <= n; j++) {
                        x[j] = wa2[j];
                        wa2[j] = diag[j] * x[j];
                    }
                    for (i = 1; i <= m; i++) fvec[i] = wa4[i];
                    xnorm = Minpack_f77.enorm_f77(n, wa2);
                    fnorm = fnorm1;
                    iter++;
                }
                if (Math.abs(actred) <= ftol && prered <= ftol && p5 * ratio <= one) info[1] = 1;
                if (delta <= xtol * xnorm) info[1] = 2;
                if (Math.abs(actred) <= ftol && prered <= ftol && p5 * ratio <= one && info[1] == 2) info[1] = 3;
                if (info[1] != 0) {
                    if (iflag[1] < 0) info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, fjac, iflag);
                    }
                    return;
                }
                if (nfev[1] >= maxfev) info[1] = 5;
                if (Math.abs(actred) <= epsmch && prered <= epsmch && p5 * ratio <= one) info[1] = 6;
                if (delta <= epsmch * xnorm) info[1] = 7;
                if (gnorm <= epsmch) info[1] = 8;
                if (info[1] != 0) {
                    if (iflag[1] < 0) info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, fjac, iflag);
                    }
                    return;
                }
                if (ratio >= p0001) donein = true;
            }
        }
    }

    /**
*
*<p>
*The enorm_f77 method calculates the Euclidean norm of a vector.
*<p>
*Translated by Steve Verrill on November 14, 2000 
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param n  The length of the vector, x.
*@param x  The vector whose Euclidean norm is to be calculated.
*
*/
    public static double enorm_f77(int n, double x[]) {
        int i;
        double agiant, floatn, rdwarf, rgiant, s1, s2, s3, xabs, x1max, x3max;
        double enorm;
        rdwarf = 3.834e-20;
        rgiant = 1.304e+19;
        s1 = zero;
        s2 = zero;
        s3 = zero;
        x1max = zero;
        x3max = zero;
        floatn = n;
        agiant = rgiant / floatn;
        for (i = 1; i <= n; i++) {
            xabs = Math.abs(x[i]);
            if (xabs <= rdwarf || xabs >= agiant) {
                if (xabs > rdwarf) {
                    if (xabs > x1max) {
                        s1 = one + s1 * (x1max / xabs) * (x1max / xabs);
                        x1max = xabs;
                    } else {
                        s1 += (xabs / x1max) * (xabs / x1max);
                    }
                } else {
                    if (xabs > x3max) {
                        s3 = one + s3 * (x3max / xabs) * (x3max / xabs);
                        x3max = xabs;
                    } else {
                        if (xabs != zero) s3 += (xabs / x3max) * (xabs / x3max);
                    }
                }
            } else {
                s2 += xabs * xabs;
            }
        }
        if (s1 != zero) {
            enorm = x1max * Math.sqrt(s1 + (s2 / x1max) / x1max);
        } else {
            if (s2 != zero) {
                if (s2 >= x3max) {
                    enorm = Math.sqrt(s2 * (one + (x3max / s2) * (x3max * s3)));
                } else {
                    enorm = Math.sqrt(x3max * ((s2 / x3max) + (x3max * s3)));
                }
            } else {
                enorm = x3max * Math.sqrt(s3);
            }
        }
        return enorm;
    }

    /**
*
*<p>
*The qrfac_f77 method uses Householder transformations with column
*pivoting (optional) to compute a QR factorization of the
*m by n matrix A.  That is, qrfac_f77 determines an orthogonal
*matrix Q, a permutation matrix P, and an upper trapezoidal
*matrix R with diagonal elements of nonincreasing magnitude,
*such that AP = QR.
*<p>
*Translated by Steve Verrill on November 17, 2000 
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  m      The number of rows of A.
*@param  n      The number of columns of A.
*@param  a      A is an m by n array.  On input A contains the matrix for
*               which the QR factorization is to be computed.  On output
*               the strict upper trapezoidal part of A contains the strict
*               upper trapezoidal part of R, and the lower trapezoidal
*               part of A contains a factored form of Q.
*@param  pivot  pivot is a logical input variable.  If pivot is set true,
*               then column pivoting is enforced.  If pivot is set false,
*               then no column pivoting is done.
*@param  ipvt   ipvt is an integer output array.  ipvt
*               defines the permutation matrix P such that A*P = Q*R.
*               Column j of P is column ipvt[j] of the identity matrix.
*               If pivot is false, ipvt is not referenced.
*@param  rdiag  rdiag is an output array of length n which contains the
*               diagonal elements of R.
*@param  acnorm acnorm is an output array of length n which contains the
*               norms of the corresponding columns of the input matrix A.
*@param  wa     wa is a work array of length n.
*
*
*/
    public static void qrfac_f77(int m, int n, double a[][], boolean pivot, int ipvt[], double rdiag[], double acnorm[], double wa[]) {
        int i, j, jp1, k, kmax, minmn;
        double ajnorm, sum, temp;
        double fac;
        double tempvec[] = new double[m + 1];
        for (j = 1; j <= n; j++) {
            for (i = 1; i <= m; i++) {
                tempvec[i] = a[i][j];
            }
            acnorm[j] = Minpack_f77.enorm_f77(m, tempvec);
            rdiag[j] = acnorm[j];
            wa[j] = rdiag[j];
            if (pivot) ipvt[j] = j;
        }
        minmn = Math.min(m, n);
        for (j = 1; j <= minmn; j++) {
            if (pivot) {
                kmax = j;
                for (k = j; k <= n; k++) {
                    if (rdiag[k] > rdiag[kmax]) kmax = k;
                }
                if (kmax != j) {
                    for (i = 1; i <= m; i++) {
                        temp = a[i][j];
                        a[i][j] = a[i][kmax];
                        a[i][kmax] = temp;
                    }
                    rdiag[kmax] = rdiag[j];
                    wa[kmax] = wa[j];
                    k = ipvt[j];
                    ipvt[j] = ipvt[kmax];
                    ipvt[kmax] = k;
                }
            }
            for (i = j; i <= m; i++) {
                tempvec[i - j + 1] = a[i][j];
            }
            ajnorm = Minpack_f77.enorm_f77(m - j + 1, tempvec);
            if (ajnorm != zero) {
                if (a[j][j] < zero) ajnorm = -ajnorm;
                for (i = j; i <= m; i++) {
                    a[i][j] /= ajnorm;
                }
                a[j][j] += one;
                jp1 = j + 1;
                if (n >= jp1) {
                    for (k = jp1; k <= n; k++) {
                        sum = zero;
                        for (i = j; i <= m; i++) {
                            sum += a[i][j] * a[i][k];
                        }
                        temp = sum / a[j][j];
                        for (i = j; i <= m; i++) {
                            a[i][k] -= temp * a[i][j];
                        }
                        if (pivot && rdiag[k] != zero) {
                            temp = a[j][k] / rdiag[k];
                            rdiag[k] *= Math.sqrt(Math.max(zero, one - temp * temp));
                            fac = rdiag[k] / wa[k];
                            if (p05 * fac * fac <= epsmch) {
                                for (i = jp1; i <= m; i++) {
                                    tempvec[i - j] = a[i][k];
                                }
                                rdiag[k] = Minpack_f77.enorm_f77(m - j, tempvec);
                                wa[k] = rdiag[k];
                            }
                        }
                    }
                }
            }
            rdiag[j] = -ajnorm;
        }
        return;
    }

    /**
*
*<p>
*Given an m by n matrix A, an n by n diagonal matrix D,
*and an m-vector b, the problem is to determine an x which
*solves the system
*<pre>
*    Ax = b ,     Dx = 0 ,
*</pre>
*in the least squares sense.
*<p>
*This method completes the solution of the problem
*if it is provided with the necessary information from the
*QR factorization, with column pivoting, of A.  That is, if
*AP = QR, where P is a permutation matrix, Q has orthogonal
*columns, and R is an upper triangular matrix with diagonal
*elements of nonincreasing magnitude, then qrsolv_f77 expects
*the full upper triangle of R, the permutation matrix P,
*and the first n components of (Q transpose)b.  The system
*<pre>
*           Ax = b, Dx = 0, is then equivalent to
*
*                 t     t
*           Rz = Q b,  P DPz = 0 ,
*</pre>
*where x = Pz.  If this system does not have full rank,
*then a least squares solution is obtained.  On output qrsolv_f77
*also provides an upper triangular matrix S such that
*<pre>
*            t  t              t
*           P (A A + DD)P = S S .
*</pre>
*S is computed within qrsolv_f77 and may be of separate interest.
*<p>
*Translated by Steve Verrill on November 17, 2000
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  n       The order of r.
*@param  r       r is an n by n array.  On input the full upper triangle
*                must contain the full upper triangle of the matrix R.
*                On output the full upper triangle is unaltered, and the
*                strict lower triangle contains the strict upper triangle
*                (transposed) of the upper triangular matrix S.
*@param ipvt     ipvt is an integer input array of length n which defines the
*                permutation matrix P such that AP = QR.  Column j of P
*                is column ipvt[j] of the identity matrix.
*@param diag     diag is an input array of length n which must contain the
*                diagonal elements of the matrix D.
*@param qtb      qtb is an input array of length n which must contain the first
*                n elements of the vector (Q transpose)b.
*@param x        x is an output array of length n which contains the least
*                squares solution of the system Ax = b, Dx = 0.
*@param sdiag    sdiag is an output array of length n which contains the
*                diagonal elements of the upper triangular matrix S.
*@param wa       wa is a work array of length n.
*
*
*/
    public static void qrsolv_f77(int n, double r[][], int ipvt[], double diag[], double qtb[], double x[], double sdiag[], double wa[]) {
        int i, j, jp1, k, kp1, l, nsing;
        double cos, cotan, qtbpj, sin, sum, tan, temp;
        for (j = 1; j <= n; j++) {
            for (i = j; i <= n; i++) {
                r[i][j] = r[j][i];
            }
            x[j] = r[j][j];
            wa[j] = qtb[j];
        }
        for (j = 1; j <= n; j++) {
            l = ipvt[j];
            if (diag[l] != zero) {
                for (k = j; k <= n; k++) {
                    sdiag[k] = zero;
                }
                sdiag[j] = diag[l];
                qtbpj = zero;
                for (k = j; k <= n; k++) {
                    if (sdiag[k] != zero) {
                        if (Math.abs(r[k][k]) < Math.abs(sdiag[k])) {
                            cotan = r[k][k] / sdiag[k];
                            sin = p5 / Math.sqrt(p25 + p25 * cotan * cotan);
                            cos = sin * cotan;
                        } else {
                            tan = sdiag[k] / r[k][k];
                            cos = p5 / Math.sqrt(p25 + p25 * tan * tan);
                            sin = cos * tan;
                        }
                        r[k][k] = cos * r[k][k] + sin * sdiag[k];
                        temp = cos * wa[k] + sin * qtbpj;
                        qtbpj = -sin * wa[k] + cos * qtbpj;
                        wa[k] = temp;
                        kp1 = k + 1;
                        for (i = kp1; i <= n; i++) {
                            temp = cos * r[i][k] + sin * sdiag[i];
                            sdiag[i] = -sin * r[i][k] + cos * sdiag[i];
                            r[i][k] = temp;
                        }
                    }
                }
            }
            sdiag[j] = r[j][j];
            r[j][j] = x[j];
        }
        nsing = n;
        for (j = 1; j <= n; j++) {
            if (sdiag[j] == zero && nsing == n) nsing = j - 1;
            if (nsing < n) wa[j] = zero;
        }
        for (k = 1; k <= nsing; k++) {
            j = nsing - k + 1;
            sum = zero;
            jp1 = j + 1;
            for (i = jp1; i <= nsing; i++) {
                sum += r[i][j] * wa[i];
            }
            wa[j] = (wa[j] - sum) / sdiag[j];
        }
        for (j = 1; j <= n; j++) {
            l = ipvt[j];
            x[l] = wa[j];
        }
        return;
    }

    /**
*
*<p>
*Given an m by n matrix A, an n by n nonsingular diagonal
*matrix D, an m-vector b, and a positive number delta,
*the problem is to determine a value for the parameter
*par such that if x solves the system
*<pre>
*           A*x = b ,     sqrt(par)*D*x = 0
*</pre>
*in the least squares sense, and dxnorm is the Euclidean
*norm of D*x, then either par is zero and
*<pre>
*           (dxnorm-delta) <= 0.1*delta ,
*</pre>
*     or par is positive and
*<pre>
*           abs(dxnorm-delta) <= 0.1*delta .
*</pre>
*This method (lmpar_f77) completes the solution of the problem
*if it is provided with the necessary information from the
*QR factorization, with column pivoting, of A.  That is, if
*AP = QR, where P is a permutation matrix, Q has orthogonal
*columns, and R is an upper triangular matrix with diagonal
*elements of nonincreasing magnitude, then lmpar_f77 expects
*the full upper triangle of R, the permutation matrix P,
*and the first n components of (Q transpose)b.  On output
*lmpar_f77 also provides an upper triangular matrix S such that
*<pre>
*            t  t                t
*           P (A A + par*DD)P = S S .
*</pre>
*S is employed within lmpar_f77 and may be of separate interest.
*<p>
*Only a few iterations are generally needed for convergence
*of the algorithm.  If, however, the limit of 10 iterations
*is reached, then the output par will contain the best
*value obtained so far.
*<p>
*Translated by Steve Verrill on November 17, 2000 
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  n       The order of r.
*@param  r       r is an n by n array.  On input the full upper triangle
*                must contain the full upper triangle of the matrix R.
*                On output the full upper triangle is unaltered, and the
*                strict lower triangle contains the strict upper triangle
*                (transposed) of the upper triangular matrix S.
*@param ipvt     ipvt is an integer input array of length n which defines the
*                permutation matrix P such that AP = QR.  Column j of P
*                is column ipvt[j] of the identity matrix.
*@param diag     diag is an input array of length n which must contain the
*                diagonal elements of the matrix D.
*@param qtb      qtb is an input array of length n which must contain the first
*                n elements of the vector (Q transpose)b.
*@param delta    delta is a positive input variable which specifies an upper
*                bound on the Euclidean norm of Dx.
*@param par      par is a nonnegative variable.  On input par contains an
*                initial estimate of the Levenberg-Marquardt parameter.
*                On output par contains the final estimate.
*@param x        x is an output array of length n which contains the least
*                squares solution of the system Ax = b, sqrt(par)*Dx = 0,
*                for the output par.
*@param sdiag    sdiag is an output array of length n which contains the
*                diagonal elements of the upper triangular matrix S.
*@param wa1      wa1 is a work array of length n.
*@param wa2      wa2 is a work array of length n.
*
*
*/
    public static void lmpar_f77(int n, double r[][], int ipvt[], double diag[], double qtb[], double delta, double par[], double x[], double sdiag[], double wa1[], double wa2[]) {
        int i, iter, j, jm1, jp1, k, l, nsing;
        double dxnorm, dwarf, fp, gnorm, parc, parl, paru, sum, temp;
        boolean loop;
        dwarf = minmag;
        nsing = n;
        for (j = 1; j <= n; j++) {
            wa1[j] = qtb[j];
            if (r[j][j] == zero && nsing == n) nsing = j - 1;
            if (nsing < n) wa1[j] = zero;
        }
        for (k = 1; k <= nsing; k++) {
            j = nsing - k + 1;
            wa1[j] /= r[j][j];
            temp = wa1[j];
            jm1 = j - 1;
            for (i = 1; i <= jm1; i++) {
                wa1[i] -= r[i][j] * temp;
            }
        }
        for (j = 1; j <= n; j++) {
            l = ipvt[j];
            x[l] = wa1[j];
        }
        iter = 0;
        for (j = 1; j <= n; j++) {
            wa2[j] = diag[j] * x[j];
        }
        dxnorm = Minpack_f77.enorm_f77(n, wa2);
        fp = dxnorm - delta;
        if (fp <= p1 * delta) {
            par[1] = zero;
            return;
        }
        parl = zero;
        if (nsing >= n) {
            for (j = 1; j <= n; j++) {
                l = ipvt[j];
                wa1[j] = diag[l] * (wa2[l] / dxnorm);
            }
            for (j = 1; j <= n; j++) {
                sum = zero;
                jm1 = j - 1;
                for (i = 1; i <= jm1; i++) {
                    sum += r[i][j] * wa1[i];
                }
                wa1[j] = (wa1[j] - sum) / r[j][j];
            }
            temp = Minpack_f77.enorm_f77(n, wa1);
            parl = ((fp / delta) / temp) / temp;
        }
        for (j = 1; j <= n; j++) {
            sum = zero;
            for (i = 1; i <= j; i++) {
                sum += r[i][j] * qtb[i];
            }
            l = ipvt[j];
            wa1[j] = sum / diag[l];
        }
        gnorm = Minpack_f77.enorm_f77(n, wa1);
        paru = gnorm / delta;
        if (paru == zero) paru = dwarf / Math.min(delta, p1);
        par[1] = Math.max(par[1], parl);
        par[1] = Math.min(par[1], paru);
        if (par[1] == zero) par[1] = gnorm / dxnorm;
        loop = true;
        while (loop) {
            iter++;
            if (par[1] == zero) par[1] = Math.max(dwarf, p001 * paru);
            temp = Math.sqrt(par[1]);
            for (j = 1; j <= n; j++) {
                wa1[j] = temp * diag[j];
            }
            Minpack_f77.qrsolv_f77(n, r, ipvt, wa1, qtb, x, sdiag, wa2);
            for (j = 1; j <= n; j++) {
                wa2[j] = diag[j] * x[j];
            }
            dxnorm = Minpack_f77.enorm_f77(n, wa2);
            temp = fp;
            fp = dxnorm - delta;
            if (Math.abs(fp) <= p1 * delta || parl == zero && fp <= temp && temp < zero || iter == 10) {
                if (iter == 0) par[1] = zero;
                return;
            }
            for (j = 1; j <= n; j++) {
                l = ipvt[j];
                wa1[j] = diag[l] * (wa2[l] / dxnorm);
            }
            for (j = 1; j <= n; j++) {
                wa1[j] /= sdiag[j];
                temp = wa1[j];
                jp1 = j + 1;
                for (i = jp1; i <= n; i++) {
                    wa1[i] -= r[i][j] * temp;
                }
            }
            temp = Minpack_f77.enorm_f77(n, wa1);
            parc = ((fp / delta) / temp) / temp;
            if (fp > zero) parl = Math.max(parl, par[1]);
            if (fp < zero) paru = Math.min(paru, par[1]);
            par[1] = Math.max(parl, par[1] + parc);
        }
    }

    /**
*
*<p>
*The lmdif1_f77 method minimizes the sum of the squares of
*m nonlinear functions in n variables by a modification of the
*Levenberg-Marquardt algorithm.  This is done by using the more
*general least-squares solver lmdif.  The user must provide a
*method that calculates the functions.  The Jacobian is
*then calculated by a forward-difference approximation.
*<p>
*Translated by Steve Verrill on November 24, 2000 
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  nlls    A class that implements the Lmdif_fcn interface
*                (see the definition in Lmdif_fcn.java).  See
*                LmdifTest_f77.java for an example of such a class.
*                The class must define a method, fcn, that must
*                have the form
*
*                public static void fcn(int m, int n, double x[],
*                double fvec[], int iflag[])
*
*                The value of iflag[1] should not be changed by fcn unless
*                the user wants to terminate execution of lmdif_f77.
*                In this case set iflag[1] to a negative integer.
*
*@param  m       A positive integer set to the number of functions
*                [number of observations]
*@param  n       A positive integer set to the number of variables
*                [number of parameters].  n must not exceed m.
*@param  x       On input, it contains the initial estimate of
*                the solution vector [the least squares parameters].
*                On output it contains the final estimate of the
*                solution vector.
*@param  fvec    An output vector that contains the m functions
*                [residuals] evaluated at x.
*@param  tol     tol is a nonnegative input variable.  Termination occurs
*                when the algorithm estimates either that the relative
*                error in the sum of squares is at most tol or that
*                the relative error between x and the solution is at
*                most tol.
*@param  info    An integer output variable.  If the user has
*                terminated execution, info is set to the (negative)
*                value of iflag[1].  See description of fcn.  Otherwise,
*                info is set as follows.
*
*                info = 0  improper input parameters.
*
*                info = 1  algorithm estimates that the relative error
*                          in the sum of squares is at most tol.
*
*                info = 2  algorithm estimates that the relative error
*                          between x and the solution is at most tol.
*
*                info = 3  conditions for info = 1 and info = 2 both hold.
*
*                info = 4  fvec is orthogonal to the columns of the
*                          Jacobian to machine precision.
*
*                info = 5  number of calls to fcn has
*                          reached or exceeded 200*(n+1).
*
*                info = 6  tol is too small.  No further reduction in
*                          the sum of squares is possible.
*
*                info = 7  tol is too small.  No further improvement in
*                          the approximate solution x is possible.
*
*/
    public static void lmdif1_f77(Lmdif_fcn nlls, int m, int n, double x[], double fvec[], double tol, int info[]) {
        int maxfev, mode, nprint;
        double epsfcn, factor, ftol, gtol, xtol;
        double diag[] = new double[n + 1];
        int nfev[] = new int[2];
        double fjac[][] = new double[m + 1][n + 1];
        int ipvt[] = new int[n + 1];
        double qtf[] = new double[n + 1];
        factor = 100.0;
        info[1] = 0;
        if (n <= 0 || m < n || tol < zero) {
            return;
        }
        maxfev = 200 * (n + 1);
        ftol = tol;
        xtol = tol;
        gtol = zero;
        epsfcn = zero;
        mode = 1;
        nprint = 0;
        Minpack_f77.lmdif_f77(nlls, m, n, x, fvec, ftol, xtol, gtol, maxfev, epsfcn, diag, mode, factor, nprint, info, nfev, fjac, ipvt, qtf);
        if (info[1] == 8) info[1] = 4;
        return;
    }

    /**
*
*<p>
*The lmdif_f77 method minimizes the sum of the squares of
*m nonlinear functions in n variables by a modification of
*the Levenberg-Marquardt algorithm.  The user must provide a
*method that calculates the functions.  The Jacobian is
*then calculated by a forward-difference approximation.
*<p>
*Translated by Steve Verrill on November 20, 2000
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  nlls    A class that implements the Lmdif_fcn interface
*                (see the definition in Lmdif_fcn.java).  See
*                LmdifTest_f77.java for an example of such a class.
*                The class must define a method, fcn, that must
*                have the form
*
*                public static void fcn(int m, int n, double x[],
*                double fvec[], int iflag[])
*
*                The value of iflag[1] should not be changed by fcn unless
*                the user wants to terminate execution of lmdif_f77.
*                In this case set iflag[1] to a negative integer.
*
*@param  m       A positive integer set to the number of functions
*                [number of observations]
*@param  n       A positive integer set to the number of variables
*                [number of parameters].  n must not exceed m.
*@param  x       On input, it contains the initial estimate of
*                the solution vector [the least squares parameters].
*                On output it contains the final estimate of the
*                solution vector.
*@param  fvec    An output vector that contains the m functions
*                [residuals] evaluated at x.
*@param  ftol    A nonnegative input variable.  Termination
*                occurs when both the actual and predicted relative
*                reductions in the sum of squares are at most ftol.
*                Therefore, ftol measures the relative error desired
*                in the sum of squares.
*@param  xtol    A nonnegative input variable.  Termination
*                occurs when the relative error between two consecutive
*                iterates is at most xtol.  Therefore, xtol measures the
*                relative error desired in the approximate solution.
*@param  gtol    A nonnegative input variable.  Termination
*                occurs when the cosine of the angle between fvec and
*                any column of the Jacobian is at most gtol in absolute
*                value.  Therefore, gtol measures the orthogonality
*                desired between the function vector and the columns
*                of the Jacobian.
*@param  maxfev  A positive integer input variable.  Termination
*                occurs when the number of calls to fcn is at least
*                maxfev by the end of an iteration.
*@param  epsfcn  An input variable used in determining a suitable
*                step length for the forward-difference approximation.  This
*                approximation assumes that the relative errors in the
*                functions are of the order of epsfcn.  If epsfcn is less
*                than the machine precision, it is assumed that the relative
*                errors in the functions are of the order of the machine
*                precision.
*@param  diag    An vector of length n.  If mode = 1 (see
*                below), diag is internally set.  If mode = 2, diag
*                must contain positive entries that serve as
*                multiplicative scale factors for the variables.
*@param  mode    If mode = 1, the
*                variables will be scaled internally.  If mode = 2,
*                the scaling is specified by the input diag.  Other
*                values of mode are equivalent to mode = 1.
*@param  factor  A positive input variable used in determining the
*                initial step bound.  This bound is set to the product of
*                factor and the euclidean norm of diag*x if nonzero, or else
*                to factor itself.  In most cases factor should lie in the
*                interval (.1,100).  100 is a generally recommended value.
*@param  nprint  An integer input variable that enables controlled
*                printing of iterates if it is positive.  In this case,
*                fcn is called with iflag[1] = 0 at the beginning of the first
*                iteration and every nprint iterations thereafter and
*                immediately prior to return, with x and fvec
*                available for printing.  If nprint is not positive, 
*                no special calls of fcn with iflag[1] = 0 are made.
*@param  info    An integer output variable.  If the user has
*                terminated execution, info is set to the (negative)
*                value of iflag[1].  See description of fcn.  Otherwise,
*                info is set as follows.
*
*                info = 0  improper input parameters.
*
*                info = 1  both actual and predicted relative reductions
*                          in the sum of squares are at most ftol.
*
*                info = 2  relative error between two consecutive iterates
*                          is at most xtol.
*
*                info = 3  conditions for info = 1 and info = 2 both hold.
*
*                info = 4  the cosine of the angle between fvec and any
*                          column of the Jacobian is at most gtol in
*                          absolute value.
*
*                info = 5  number of calls to fcn with iflag[1] = 1 has
*                          reached maxfev.
*
*                info = 6  ftol is too small. no further reduction in
*                          the sum of squares is possible.
*
*                info = 7  xtol is too small. no further improvement in
*                          the approximate solution x is possible.
*
*                info = 8  gtol is too small. fvec is orthogonal to the
*                          columns of the Jacobian to machine precision.
*
*@param  nfev    An integer output variable set to the number of
*                calls to fcn.
*@param  fjac    An output m by n array.  The upper n by n submatrix
*                of fjac contains an upper triangular matrix R with
*                diagonal elements of nonincreasing magnitude such that
*
*                 t    t          t
*                P (jac *jac)P = R R,
*
*                where P is a permutation matrix and jac is the final
*                calculated Jacobian.  Column j of P is column ipvt[j]
*                (see below) of the identity matrix.  The lower trapezoidal
*                part of fjac contains information generated during
*                the computation of R.
*@param  ipvt    An integer output array of length n.  ipvt
*                defines a permutation matrix P such that jac*P = QR,
*                where jac is the final calculated Jacobian, Q is
*                orthogonal (not stored), and R is upper triangular
*                with diagonal elements of nonincreasing magnitude.
*                column j of P is column ipvt[j] of the identity matrix.
*
*@param  qtf     An output array of length n which contains
*                the first n elements of the vector (Q transpose)fvec.
*
*
*/
    public static void lmdif_f77(Lmdif_fcn nlls, int m, int n, double x[], double fvec[], double ftol, double xtol, double gtol, int maxfev, double epsfcn, double diag[], int mode, double factor, int nprint, int info[], int nfev[], double fjac[][], int ipvt[], double qtf[]) {
        int i, iter, j, l;
        double actred, delta, dirder, fnorm, fnorm1, gnorm, pnorm, prered, ratio, sum, temp, temp1, temp2, xnorm;
        double par[] = new double[2];
        boolean doneout, donein;
        int iflag[] = new int[2];
        double wa1[] = new double[n + 1];
        double wa2[] = new double[n + 1];
        double wa3[] = new double[n + 1];
        double wa4[] = new double[m + 1];
        delta = 0.0;
        xnorm = 0.0;
        info[1] = 0;
        iflag[1] = 0;
        nfev[1] = 0;
        if (n <= 0 || m < n || ftol < zero || xtol < zero || gtol < zero || maxfev <= 0 || factor <= zero) {
            if (nprint > 0) {
                nlls.fcn(m, n, x, fvec, iflag);
            }
            return;
        }
        if (mode == 2) {
            for (j = 1; j <= n; j++) {
                if (diag[j] <= zero) {
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, iflag);
                    }
                    return;
                }
            }
        }
        iflag[1] = 1;
        nlls.fcn(m, n, x, fvec, iflag);
        nfev[1] = 1;
        if (iflag[1] < 0) {
            info[1] = iflag[1];
            iflag[1] = 0;
            if (nprint > 0) {
                nlls.fcn(m, n, x, fvec, iflag);
            }
            return;
        }
        fnorm = Minpack_f77.enorm_f77(m, fvec);
        par[1] = zero;
        iter = 1;
        doneout = false;
        while (!doneout) {
            iflag[1] = 2;
            Minpack_f77.fdjac2_f77(nlls, m, n, x, fvec, fjac, iflag, epsfcn, wa4);
            nfev[1] += n;
            if (iflag[1] < 0) {
                info[1] = iflag[1];
                iflag[1] = 0;
                if (nprint > 0) {
                    nlls.fcn(m, n, x, fvec, iflag);
                }
                return;
            }
            if (nprint > 0) {
                iflag[1] = 0;
                if ((iter - 1) % nprint == 0) {
                    nlls.fcn(m, n, x, fvec, iflag);
                }
                if (iflag[1] < 0) {
                    info[1] = iflag[1];
                    iflag[1] = 0;
                    nlls.fcn(m, n, x, fvec, iflag);
                    return;
                }
            }
            Minpack_f77.qrfac_f77(m, n, fjac, true, ipvt, wa1, wa2, wa3);
            if (iter == 1) {
                if (mode != 2) {
                    for (j = 1; j <= n; j++) {
                        diag[j] = wa2[j];
                        if (wa2[j] == zero) diag[j] = one;
                    }
                }
                for (j = 1; j <= n; j++) {
                    wa3[j] = diag[j] * x[j];
                }
                xnorm = Minpack_f77.enorm_f77(n, wa3);
                delta = factor * xnorm;
                if (delta == zero) delta = factor;
            }
            for (i = 1; i <= m; i++) wa4[i] = fvec[i];
            for (j = 1; j <= n; j++) {
                if (fjac[j][j] != zero) {
                    sum = zero;
                    for (i = j; i <= m; i++) sum += fjac[i][j] * wa4[i];
                    temp = -sum / fjac[j][j];
                    for (i = j; i <= m; i++) wa4[i] += fjac[i][j] * temp;
                }
                fjac[j][j] = wa1[j];
                qtf[j] = wa4[j];
            }
            gnorm = zero;
            if (fnorm != zero) {
                for (j = 1; j <= n; j++) {
                    l = ipvt[j];
                    if (wa2[l] != zero) {
                        sum = zero;
                        for (i = 1; i <= j; i++) sum += fjac[i][j] * (qtf[i] / fnorm);
                        gnorm = Math.max(gnorm, Math.abs(sum / wa2[l]));
                    }
                }
            }
            if (gnorm <= gtol) info[1] = 4;
            if (info[1] != 0) {
                if (iflag[1] < 0) info[1] = iflag[1];
                iflag[1] = 0;
                if (nprint > 0) {
                    nlls.fcn(m, n, x, fvec, iflag);
                }
                return;
            }
            if (mode != 2) {
                for (j = 1; j <= n; j++) {
                    diag[j] = Math.max(diag[j], wa2[j]);
                }
            }
            donein = false;
            while (!donein) {
                Minpack_f77.lmpar_f77(n, fjac, ipvt, diag, qtf, delta, par, wa1, wa2, wa3, wa4);
                for (j = 1; j <= n; j++) {
                    wa1[j] = -wa1[j];
                    wa2[j] = x[j] + wa1[j];
                    wa3[j] = diag[j] * wa1[j];
                }
                pnorm = Minpack_f77.enorm_f77(n, wa3);
                if (iter == 1) delta = Math.min(delta, pnorm);
                iflag[1] = 1;
                nlls.fcn(m, n, wa2, wa4, iflag);
                nfev[1]++;
                if (iflag[1] < 0) {
                    info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, iflag);
                    }
                    return;
                }
                fnorm1 = Minpack_f77.enorm_f77(m, wa4);
                actred = -one;
                if (p1 * fnorm1 < fnorm) actred = one - (fnorm1 / fnorm) * (fnorm1 / fnorm);
                for (j = 1; j <= n; j++) {
                    wa3[j] = zero;
                    l = ipvt[j];
                    temp = wa1[l];
                    for (i = 1; i <= j; i++) wa3[i] += fjac[i][j] * temp;
                }
                temp1 = Minpack_f77.enorm_f77(n, wa3) / fnorm;
                temp2 = (Math.sqrt(par[1]) * pnorm) / fnorm;
                prered = temp1 * temp1 + temp2 * temp2 / p5;
                dirder = -(temp1 * temp1 + temp2 * temp2);
                ratio = zero;
                if (prered != zero) ratio = actred / prered;
                if (ratio <= p25) {
                    if (actred >= zero) {
                        temp = p5;
                    } else {
                        temp = p5 * dirder / (dirder + p5 * actred);
                    }
                    if (p1 * fnorm1 >= fnorm || temp < p1) temp = p1;
                    delta = temp * Math.min(delta, pnorm / p1);
                    par[1] /= temp;
                } else {
                    if (par[1] == zero || ratio >= p75) {
                        delta = pnorm / p5;
                        par[1] *= p5;
                    }
                }
                if (ratio >= p0001) {
                    for (j = 1; j <= n; j++) {
                        x[j] = wa2[j];
                        wa2[j] = diag[j] * x[j];
                    }
                    for (i = 1; i <= m; i++) fvec[i] = wa4[i];
                    xnorm = Minpack_f77.enorm_f77(n, wa2);
                    fnorm = fnorm1;
                    iter++;
                }
                if (Math.abs(actred) <= ftol && prered <= ftol && p5 * ratio <= one) info[1] = 1;
                if (delta <= xtol * xnorm) info[1] = 2;
                if (Math.abs(actred) <= ftol && prered <= ftol && p5 * ratio <= one && info[1] == 2) info[1] = 3;
                if (info[1] != 0) {
                    if (iflag[1] < 0) info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, iflag);
                    }
                    return;
                }
                if (nfev[1] >= maxfev) info[1] = 5;
                if (Math.abs(actred) <= epsmch && prered <= epsmch && p5 * ratio <= one) info[1] = 6;
                if (delta <= epsmch * xnorm) info[1] = 7;
                if (gnorm <= epsmch) info[1] = 8;
                if (info[1] != 0) {
                    if (iflag[1] < 0) info[1] = iflag[1];
                    iflag[1] = 0;
                    if (nprint > 0) {
                        nlls.fcn(m, n, x, fvec, iflag);
                    }
                    return;
                }
                if (ratio >= p0001) donein = true;
            }
        }
    }

    /**
*
*<p>
*The fdjac2 method computes a forward-difference approximation
*to the m by n Jacobian matrix associated with a specified
*problem of m functions in n variables.
*<p>
*Translated by Steve Verrill on November 24, 2000
*from the FORTRAN MINPACK source produced by Garbow, Hillstrom, and More.<p>
*
*
*@param  nlls   A class that implements the Lmdif_fcn interface
*               (see the definition in Lmdif_fcn.java).  See
*               LmdifTest_f77.java for an example of such a class.
*               The class must define a method, fcn, that must
*               have the form
*
*               public static void fcn(int m, int n, double x[],
*               double fvec[], int iflag[])
*
*               The value of iflag[1] should not be changed by fcn unless
*               the user wants to terminate execution of fdjac2_f77.
*               In this case iflag[1] should be set to a negative integer.
*@param   m     A positive integer set to the number of functions
*               [number of observations]
*@param   n     A positive integer set to the number of variables
*               [number of parameters].  n must not exceed m.
*@param   x     An input array.
*@param  fvec   An input array that contains the functions
*               evaluated at x.
*@param  fjac   An output m by n array that contains the
*               approximation to the Jacobian matrix evaluated at x.
*@param  iflag  An integer variable that can be used to terminate
*               the execution of fdjac2.  See the description of nlls.
*@param  epsfcn An input variable used in determining a suitable
*               step length for the forward-difference approximation.  This
*               approximation assumes that the relative errors in the
*               functions are of the order of epsfcn.  If epsfcn is less
*               than the machine precision, it is assumed that the relative
*               errors in the functions are of the order of the machine
*               precision.
*@param  wa     A work array.
*
*/
    public static void fdjac2_f77(Lmdif_fcn nlls, int m, int n, double x[], double fvec[], double fjac[][], int iflag[], double epsfcn, double wa[]) {
        int i, j;
        double eps, h, temp;
        eps = Math.sqrt(Math.max(epsfcn, epsmch));
        for (j = 1; j <= n; j++) {
            temp = x[j];
            h = eps * Math.abs(temp);
            if (h == zero) h = eps;
            x[j] = temp + h;
            nlls.fcn(m, n, x, wa, iflag);
            if (iflag[1] < 0) {
                return;
            }
            x[j] = temp;
            for (i = 1; i <= m; i++) {
                fjac[i][j] = (wa[i] - fvec[i]) / h;
            }
        }
        return;
    }
}
