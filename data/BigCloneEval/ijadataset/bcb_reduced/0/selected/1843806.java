package esra.math;

/**
 * A collection of fitting routines for sets of points in 3D-space.
 * 
 * @version 0.3, September 2005
 * @author  Mika Kastenholz, Vincent Kraeutler
 * @since 0.1
 */
public class Fit {

    static final int DIM = 3;

    /**
	 * Shorthand for the origin of the coordinate system.
	 */
    public static final double[] origin = { 0.0, 0.0, 0.0 };

    /**
	 * A fit of coords to refCoords (weighted by weights).
	 * This uses a quaterion-based parametrization, which involves no need
	 * to consider special cases (e.g. co-planar coordinates).
	 * 
	 * @param coords		the coordinates to be fitted
	 * @param refCoords	the reference coordinates
	 * @param weights	the weights used in the fitting
	 * @return the fitted coordinates
	 */
    public static double[][] fit(final double[][] coords, final double[][] refCoords, final double[] weights) {
        if (coords.length != refCoords.length || coords.length != weights.length) throw new IllegalArgumentException("All arguments must have the same length. Found\n" + coords.length + " coordinates,\n" + refCoords.length + " reference coordinates,\n" + weights.length + " weights.\n");
        final double[] shift = BLA.subtract(Geometry.centerOfGeometry(refCoords, weights), BLA.zeroes(DIM));
        final double[][] centeredRefCoords = BLA.shift(refCoords, shift);
        double[][] fittedCoords = shiftCoG(coords, BLA.zeroes(DIM), weights);
        fittedCoords = rotationalFit(fittedCoords, centeredRefCoords, weights);
        return BLA.shift(fittedCoords, BLA.scale(shift, -1));
    }

    /**
	 * A fit of coords to refCoords (weighted by weights). 
	 * 
	 * this is a generalization of fit(double[][], double[][], double[])
	 * that allows you to superimpose selected parts of structures
	 * containing different numbers of coordinates.
	 * 
	 * @param coords		the coordinates to be fitted
	 * @param refCoords	the reference coordinates
	 * @param weights	the weights used in the fitting
	 * @return the fitted coordinates
	 * @see Fit#fit(double[][], double[][], double[])
	 */
    public static double[][] fit(final double[][] coords, final int[] coordSpecs, final double[][] refCoords, final int[] refCoordSpecs, final double[] weights) {
        if (coords.length < coordSpecs.length || refCoords.length < refCoordSpecs.length) throw new IllegalArgumentException("Coordinate arrays must be longer than coordinate specification " + "arrays.");
        if (coordSpecs.length != refCoordSpecs.length) throw new IllegalArgumentException("Must take same number of coordinates and reference coordinates " + "for fitting.");
        if (coordSpecs.length != weights.length) throw new IllegalArgumentException("Must take same number of coordinates and weights " + "for fitting.");
        double[] weights05 = new double[weights.length];
        for (int ii = 0; ii < weights.length; ii++) weights05[ii] = Math.sqrt(weights[ii]);
        weights05 = BLA.scale(weights05, 1.0 / BLA.sum(weights05));
        double[][] centeredRefCoords = BLA.select(refCoords, refCoordSpecs);
        final double[] shift = BLA.subtract(Geometry.centerOfGeometry(centeredRefCoords, weights), BLA.zeroes(DIM));
        centeredRefCoords = BLA.shift(centeredRefCoords, shift);
        final double[][] interestingCoords = BLA.select(coords, coordSpecs);
        final double[] cShift = BLA.subtract(Geometry.centerOfGeometry(interestingCoords, weights), BLA.zeroes(DIM));
        final double[][] centeredCoords = BLA.shift(coords, cShift);
        double[][] myCoords = BLA.select(centeredCoords, coordSpecs);
        centeredRefCoords = BLA.diagonalMatmul(weights05, centeredRefCoords);
        myCoords = BLA.diagonalMatmul(weights05, myCoords);
        final double[][] fittedCoords = Geometry.rotate(rotationMatrix(myCoords, centeredRefCoords), centeredCoords);
        return BLA.shift(fittedCoords, BLA.scale(shift, -1));
    }

    /**
	 * Shift the center of geometry of a set of 3D-coordinates
	 * and massses to some point in 3D-space.
	 * 
	 * @param coordinates		the coordinates to be shifted
	 * @param where			where to shift the coordinates
	 * @param weights		the weights associated with the coordinates
	 * @return the shifted coordinates
	 */
    public static double[][] shiftCoG(final double[][] coordinates, final double[] where, final double[] weights) {
        if (coordinates.length != weights.length) throw new IllegalArgumentException("Coordinates and weights must have the same length.");
        double[] shift = BLA.subtract(Geometry.centerOfGeometry(coordinates, weights), where);
        return BLA.shift(coordinates, shift);
    }

    /**
	 * Shift the center of geometry of a set of 3D-coordinates
	 * and massses to some point in 3D-space.
	 * 
	 * @param coordinates		the coordinates to be shifted
	 * @param where			where to shift the coordinates
	 * @return the shifted coordinates
	 * @see Fit#shiftCoG(double[][], double[], double[])
	 */
    public static double[][] shiftCoG(final double[][] coordinates, final double[] where) {
        return shiftCoG(coordinates, where, BLA.same(coordinates.length, 1.0));
    }

    /**
	 * Shift the center of geometry of a set of 3D-coordinates
	 * to the center of geometry of another set of 3D-coords, both weighted by
	 * the same weights.
	 * 
	 * @param coords		the coordinates to be shifted
	 * @param refCoords	the target coordinates
	 * @param weights	the weights
	 * @return			the shifted coords
	 */
    public static double[][] translationalFit(final double[][] coords, final double[][] refCoords, final double[] weights) {
        if (coords.length != refCoords.length || coords.length != weights.length) throw new IllegalArgumentException("All arguments must have the same length.");
        return shiftCoG(coords, Geometry.centerOfGeometry(refCoords, weights), weights);
    }

    /** 
	 * <h4>Quaternion-based rotational fit.</h4>
	 * 
	 * <p>
	 * see A.D. Kearsley, Acta Cryst. A45 (1989) 208-210.     
	 * </p>
	 * <p>      
	 * describes a rotation about the origin
	 * such that the rmsd(pos, refPos) is minimized.
	 * </p>
	 * <p>
	 * the block(operator, 0, 0, 3, 3) contains the rotation
	 * matrix, while operator[3] contains the rotation eigenvalues.
	 * scaled by the number of considered coordinates (i.e. 3 * pos.length
	 * or 3 * BLA.sum(weights) for weighted fits) the square root of 
	 * the smallest scaled eigenvalue (i.e. operator[3][3]) is the rmsd.
	 * </p>
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the (weighted) reference positions
	 * @return			the matrix[4][4] operator
	 */
    public static double[][] rotation(final double[][] pos, final double[][] refPos) {
        if (pos.length != refPos.length) throw new IllegalArgumentException("All arguments must have the same length.");
        double R_m[] = new double[3];
        double R_p[] = new double[3];
        double matrix[][] = new double[4][4];
        for (int k = 0; k < pos.length; ++k) {
            R_m = BLA.subtract(pos[k], refPos[k]);
            R_p = BLA.add(refPos[k], pos[k]);
            matrix[0][0] += (R_m[0] * R_m[0] + R_m[1] * R_m[1] + R_m[2] * R_m[2]);
            matrix[1][1] += (R_m[0] * R_m[0] + R_p[1] * R_p[1] + R_p[2] * R_p[2]);
            matrix[2][2] += (R_p[0] * R_p[0] + R_m[1] * R_m[1] + R_p[2] * R_p[2]);
            matrix[3][3] += (R_p[0] * R_p[0] + R_p[1] * R_p[1] + R_m[2] * R_m[2]);
            matrix[1][0] += (R_m[2] * R_p[1] - R_m[1] * R_p[2]);
            matrix[2][0] += (R_p[2] * R_m[0] - R_p[0] * R_m[2]);
            matrix[2][1] += (R_m[0] * R_m[1] - R_p[1] * R_p[0]);
            matrix[3][0] += (R_m[1] * R_p[0] - R_p[1] * R_m[0]);
            matrix[3][1] += (R_m[2] * R_m[0] - R_p[0] * R_p[2]);
            matrix[3][2] += (R_m[2] * R_m[1] - R_p[1] * R_p[2]);
        }
        for (int i = 0; i < 4; ++i) {
            for (int j = i + 1; j < 4; ++j) {
                matrix[i][j] = matrix[j][i];
            }
        }
        double eigenvals[] = new double[4];
        BLA.diagonalizeSymmetric(matrix, eigenvals);
        double q[] = new double[4];
        for (int i = 0; i < 4; ++i) q[i] = matrix[i][3];
        double operator[][] = BLA.zeroes(4, 4);
        operator[0][0] = q[0] * q[0] + q[1] * q[1] - q[2] * q[2] - q[3] * q[3];
        operator[1][1] = q[0] * q[0] + q[2] * q[2] - q[1] * q[1] - q[3] * q[3];
        operator[2][2] = q[0] * q[0] + q[3] * q[3] - q[1] * q[1] - q[2] * q[2];
        operator[1][0] = 2 * (q[1] * q[2] - q[0] * q[3]);
        operator[2][0] = 2 * (q[1] * q[3] + q[0] * q[2]);
        operator[2][1] = 2 * (q[2] * q[3] - q[0] * q[1]);
        operator[0][1] = 2 * (q[1] * q[2] + q[0] * q[3]);
        operator[0][2] = 2 * (q[1] * q[3] - q[0] * q[2]);
        operator[1][2] = 2 * (q[2] * q[3] + q[0] * q[1]);
        operator[3] = eigenvals;
        return operator;
    }

    /**
	 * @param coords
	 * @param refCoords
	 * @return the rotation matrix part of the rotation operator
	 * @see Fit#rotation
	 */
    public static double[][] rotationMatrix(final double[][] coords, final double[][] refCoords) {
        return BLA.block(rotation(coords, refCoords), 0, 0, 3, 3);
    }

    /**         
	 *       
	 * The atoms of the simulation system are rotated about the origin
	 * such that the (weighted) rmsd is minimized.  <BR>
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the reference positions
	 * @return			the fitted positions
	 */
    public static double[][] rotationalFit(final double[][] pos, final double[][] refPos) {
        if (pos.length != refPos.length) throw new IllegalArgumentException("All arguments must have the same length.");
        return Geometry.rotate(rotationMatrix(pos, refPos), pos);
    }

    /**      
	 * SEE A.D. KEARSLEY, ACTA. CRYST. A45 (1989) 208-210.    
	 *       
	 * The atoms of the simulation system are rotated about the origin
	 * based on a rotationmatrix calculated via a unit quaternion.
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the reference positions
	 * @param weights	the weights used in the fitting
	 * @return			the fitted positions
	 */
    public static double[][] rotationalFit(final double[][] pos, final double[][] refPos, final double[] weights) {
        return Geometry.rotate(rotationMatrix(pos, refPos, weights), pos);
    }

    /**      
	 * SEE A.D. KEARSLEY, ACTA. CRYST. A45 (1989) 208-210.    
	 *       
	 * The atoms of the simulation system are rotated about the origin
	 * based on a rotationmatrix calculated via a unit quaternion.
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the reference positions
	 * @param weights	the weights used in the fitting
	 * @return			the rotation matrix corresponding to an optimal weighted fit
	 */
    public static double[][] rotationMatrix(final double[][] pos, final double[][] refPos, final double[] weights) {
        if (pos.length != refPos.length || pos.length != weights.length) throw new IllegalArgumentException("All arguments must have the same length.");
        double[] weights05 = new double[weights.length];
        for (int ii = 0; ii < weights.length; ii++) weights05[ii] = Math.sqrt(weights[ii]);
        weights05 = BLA.scale(weights05, 1.0 / BLA.sum(weights05));
        final double[][] wPos = BLA.diagonalMatmul(weights05, pos);
        final double[][] wRefPos = BLA.diagonalMatmul(weights05, refPos);
        return rotationMatrix(wPos, wRefPos);
    }

    /**
	 * 
	 * SEE A.D. MCLACHLAN, J. MOL. BIOL. 128 (1979) 49.    
	 * 
	 * DEPRECATED use rotationMatrix() instead. it's both faster 
	 * and doesn't suffer from numerical instabilities. 
	 *       
	 * describes a rotation about the origin
	 * such that the rmsd(pos, refPos) is minimized.  <BR>
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the (weighted) reference positions
	 * @return			the rotation matrix
	 */
    public static double[][] rotationMatrixMcLachlan(final double[][] pos, final double[][] refPos) {
        if (pos.length != refPos.length) throw new IllegalArgumentException("All arguments must have the same length.");
        double M[][] = BLA.matmul(BLA.transpose(pos), refPos);
        final double det = BLA.det3x3(M);
        final int signU = (det > 0 ? 1 : -1);
        double Omega[][] = new double[6][6];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Omega[i][j + 3] = M[i][j];
                Omega[i + 3][j] = M[j][i];
            }
        }
        double eigenvals[] = new double[6];
        BLA.diagonalizeSymmetric(Omega, eigenvals);
        if (det < 0 && Math.abs(eigenvals[1] - eigenvals[2]) < 1.0e-5) {
            throw new RuntimeException("FIT: Rotation matrix degenerate.");
        }
        Omega = BLA.scale(Omega, Math.sqrt(2));
        double[][] k = BLA.transpose(BLA.block(Omega, 0, 0, 3, 3));
        double[][] h = BLA.transpose(BLA.block(Omega, 3, 0, 6, 3));
        if (BLA.dot(h[0], BLA.cross(h[1], h[2])) < 0) {
            h[2] = BLA.scale(h[2], -1 * signU);
            k[2] = BLA.scale(k[2], -1);
        }
        h = BLA.transpose(h);
        return BLA.matmul(h, k);
    }

    /**     
	 * 
	 * DEPRECATED use rotationalFit() instead. 
	 * it's both faster and doesn't suffer from numerical instabilities. 
	 * 
	 * SEE A.D. MCLACHLAN, ACTA. CRYST. A38 (1982) 871-873.    
	 *       
	 * The atoms of the simulation system are rotated about the origin
	 * such that the (weighted) rmsd is minimized.  <BR>
	 * 
	 * @param pos		the positions to be fitted
	 * @param refPos		the reference positions
	 * @param weights	the weights used in the fitting
	 * @return			the fitted positions
	 */
    public static double[][] iterativeRotationalFit(final double[][] pos, final double[][] refPos, final double[] weights) {
        if (pos.length != refPos.length || pos.length != weights.length) throw new IllegalArgumentException("All arguments must have the same length.");
        int iter = 1;
        int itermax = 300;
        double g_old[] = new double[3];
        double g[] = new double[3];
        double l[] = new double[3];
        double s[] = new double[3];
        double cross[] = new double[3];
        double s_old[] = new double[3];
        double delta[][] = new double[3][3];
        delta[0][0] = 1;
        delta[1][1] = 1;
        delta[2][2] = 1;
        double sin_theta = 0.0, cos_theta = 0.0, G = 0.0, H = 0.0, T_ij = 0.0, dot = 0.0;
        double H_sq_G_sq = 0.0;
        loop: {
            while (iter < itermax) {
                double V[][] = new double[3][3];
                double E = 0.0, v = 0.0;
                for (int i = 0; i < 3; ++i) for (int j = 0; j < 3; ++j) for (int n = 0; n < pos.length; ++n) V[i][j] += weights[n] * pos[n][i] * refPos[n][j];
                for (int i = 0; i < 3; ++i) v += V[i][i];
                for (int i = 0; i < 3; ++i) {
                    g_old[i] = g[i];
                    g[i] = 0.0;
                }
                for (int i = 0; i < pos.length; ++i) {
                    E += (weights[i] * (BLA.norm2(pos[i]) + BLA.norm2(refPos[i]))) - v;
                    cross = (BLA.cross(pos[i], refPos[i]));
                    for (int j = 0; j < 3; ++j) {
                        g[j] += cross[j] * weights[i];
                    }
                }
                E *= 0.5;
                if (iter == 1) {
                    for (int i = 0; i < 3; ++i) {
                        s_old[i] = s[i];
                        s[i] = g[i];
                        l[i] = (s[i]);
                    }
                } else {
                    for (int i = 0; i < 3; ++i) {
                        s_old[i] = s[i];
                        s[i] = g[i] + ((g[i] * g[i]) / (g_old[i] * g_old[i])) * s_old[i];
                        l[i] = (s[i]);
                    }
                }
                l = BLA.normalize(l);
                G = BLA.dot(g, l);
                H = 0.0;
                for (int i = 0; i < 3; ++i) {
                    for (int j = 0; j < 3; ++j) {
                        T_ij = (v * delta[i][j] - 0.5 * (V[i][j] + V[j][i]));
                        H += l[i] * T_ij * l[j];
                    }
                }
                H_sq_G_sq = Math.sqrt((G * G + H * H));
                sin_theta = G / H_sq_G_sq;
                cos_theta = H / H_sq_G_sq;
                for (int i = 0; i < pos.length; ++i) {
                    cross = BLA.cross(l, pos[i]);
                    dot = BLA.dot(l, pos[i]);
                    for (int j = 0; j < 3; ++j) {
                        pos[i][j] = pos[i][j] * cos_theta + cross[j] * sin_theta + dot * l[j] * (1 - cos_theta);
                    }
                }
                if (Math.abs(G) < 0.0000000001) break loop;
                ++iter;
            }
        }
        if (iter == itermax) {
            System.err.println("Fit.iterativeRotationalFit: Maximum iterations of " + itermax + " reached.");
            System.err.println("Rotational Fit might not be optimal.");
        }
        return pos;
    }

    /**
	 * 
	 * TODO test
	 * 
	 * Warning: likely unusable.
	 * 
	 * Align the principal axes of a set of mass points with the 
	 * coordinate system. 
	 * @param coords 	the coordinates to be shifted
	 * @param weights 	the weights associated with the coordinates
	 * @return 			the aligned coordinates
	 */
    public static double[][] alignPrincipalAxes(final double[][] coords, final double[] weights) {
        if (coords.length != weights.length) {
            throw new IllegalArgumentException("Must supply same number of coords and weights.");
        }
        double[][] centeredCoords = shiftCoG(coords, BLA.zeroes(3), weights);
        double[][] it = Geometry.inertiaTensor(centeredCoords, weights);
        double[] itE = BLA.zeroes(3);
        BLA.diagonalizeSymmetric(it, itE);
        return BLA.matmul(coords, BLA.transpose(it));
    }
}
