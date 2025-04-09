package kfschmidt.geom3d;

import java.text.DecimalFormat;

public class AffineTransform3D {

    double[][] mTransform = new double[4][4];

    double[][] mTmpMat = new double[5][5];

    Point3D mTmpPoint = new Point3D();

    /**
     *  Creates a new transform initialized to
     *  an identity transformation
     *
     */
    public AffineTransform3D() {
        setToIdentity();
    }

    /**
     *  Creates a new transform initialized to
     *  an identity transformation
     *
     */
    public AffineTransform3D(AffineTransform3D trans) {
        for (int a = 0; a < 4; a++) {
            for (int b = 0; b < 4; b++) {
                mTransform[a][b] = trans.mTransform[a][b];
            }
        }
    }

    /**
     *  Sets this transformation to an identity transform
     *
     */
    public void setToIdentity() {
        for (int a = 0; a < 4; a++) {
            for (int b = 0; b < 4; b++) {
                if (a == b) mTransform[a][b] = 1d; else mTransform[a][b] = 0d;
            }
        }
    }

    /**
     *   transforms initialpt by this transform and stores 
     *   resulting point in destpt
     */
    public void transform(Point3D initialpt, Point3D destpt) {
        transformPointByMat(mTransform, initialpt, destpt);
    }

    /**
     *  returns an AffineTransform3D initialized to
     *  the inverse of this AffineTransform
     *  if such an inverse exists. Otherwise,
     *  returns null if the inverse does not exist.
     *
     *  This inverse will hold the property that
     *  AT.concatenate(ATINVERSE) = AT which is identity
     * 
     */
    public AffineTransform3D getInverse() {
        AffineTransform3D ret = null;
        double[][] inv = null;
        try {
            inv = calcInverse(mTransform);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (inv != null) {
            ret = new AffineTransform3D();
            for (int a = 0; a < inv.length; a++) {
                for (int b = 0; b < inv.length; b++) {
                    ret.mTransform[a][b] = inv[a][b];
                }
            }
        }
        return ret;
    }

    /**
     *  Transforms srcpoint by this affine transform
     *  and puts the result in dstpoint
     *
     */
    private void transformPointByMat(double[][] mat, Point3D srcpoint, Point3D dstpoint) {
        mTmpPoint.x = mat[0][0] * srcpoint.x + mat[0][1] * srcpoint.y + mat[0][2] * srcpoint.z + mat[0][3];
        mTmpPoint.y = mat[1][0] * srcpoint.x + mat[1][1] * srcpoint.y + mat[1][2] * srcpoint.z + mat[1][3];
        mTmpPoint.z = mat[2][0] * srcpoint.x + mat[2][1] * srcpoint.y + mat[2][2] * srcpoint.z + mat[2][3];
        dstpoint.x = mTmpPoint.x;
        dstpoint.y = mTmpPoint.y;
        dstpoint.z = mTmpPoint.z;
    }

    /**
     *   returns the 4x4 matrix
     */
    public double[][] getMatrix() {
        return mTransform;
    }

    /**
     *    Concatenates this transform with another transform
     *    by premultiplying this transforms matrix by the matrix in the
     *    other.
     *
     */
    public void concatenate(AffineTransform3D otherTransform) {
        multiplyMatrices(otherTransform.getMatrix(), mTransform, mTransform);
    }

    /**
     *   Translate this transformation by a set x,y,z
     *
     */
    public void translate(double x, double y, double z) {
        AffineTransform3D trans = new AffineTransform3D();
        trans.mTransform[0][3] += x;
        trans.mTransform[1][3] += y;
        trans.mTransform[2][3] += z;
        concatenate(trans);
    }

    /**
     *   Scales this transformation by the scale
     *   factors supplied
     *
     *
     */
    public void scale(double scalex, double scaley, double scalez) {
        AffineTransform3D scaler = new AffineTransform3D();
        scaler.mTransform[0][0] = scalex;
        scaler.mTransform[1][1] = scaley;
        scaler.mTransform[2][2] = scalez;
        concatenate(scaler);
    }

    /**
     *   Concatenates this transformation with a 
     *   rotation about the x axis
     *   units are in radians
     *
     */
    public void rotateAboutX(double rads) {
        AffineTransform3D rot = new AffineTransform3D();
        rot.mTransform[1][1] = Math.cos(rads);
        rot.mTransform[1][2] = Math.sin(rads);
        rot.mTransform[2][1] = -Math.sin(rads);
        rot.mTransform[2][2] = Math.cos(rads);
        concatenate(rot);
    }

    /**
     *   Concatenates this transformation with a 
     *   rotation about the x axis
     *   units are in radians
     *
     */
    public void rotateAboutY(double rads) {
        AffineTransform3D rot = new AffineTransform3D();
        rot.mTransform[0][0] = Math.cos(rads);
        rot.mTransform[0][2] = Math.sin(rads);
        rot.mTransform[2][0] = -Math.sin(rads);
        rot.mTransform[2][2] = Math.cos(rads);
        concatenate(rot);
    }

    /**
     *   Concatenates this transformation with a 
     *   rotation about the z axis
     *   units are in radians
     *
     */
    public void rotateAboutZ(double rads) {
        AffineTransform3D rot = new AffineTransform3D();
        rot.mTransform[0][0] = Math.cos(rads);
        rot.mTransform[0][1] = Math.sin(rads);
        rot.mTransform[1][0] = -Math.sin(rads);
        rot.mTransform[1][1] = Math.cos(rads);
        concatenate(rot);
    }

    public String toString() {
        String s = "\n---------------\nAFFINE TRANSFORM 3D";
        s += toString(mTransform);
        s += "\n-------------------------------\n";
        return s;
    }

    public static double[][] calcTranspose(double[][] mat) {
        double[][] transpose = new double[mat[0].length][mat.length];
        for (int a = 0; a < transpose.length; a++) {
            for (int b = 0; b < transpose[0].length; b++) {
                transpose[a][b] = mat[b][a];
            }
        }
        return transpose;
    }

    public static double[][] calcInverse(double[][] mat) throws Exception {
        int dim = mat.length;
        double[][] adj = new double[dim][dim];
        adj = calcAdjoint(mat);
        double det = calcDeterminant(mat);
        if (det == 0) throw new Exception("Matrix is not invertible");
        double[][] inv = scalarMultiply(adj, (1 / det));
        return inv;
    }

    /**
     *  returns the adjoint of the matrix 
     *  passed in
     *
     */
    public static double[][] calcCofactorMatrix(double[][] mat) {
        int dim = mat[0].length;
        double[][] cof = new double[dim][dim];
        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                cof[r][c] = getCofactor(mat, r + 1, c + 1);
            }
        }
        return cof;
    }

    /**
     *  returns the transpose of the cofactor matrix
     *  or Adjoint matrix
     *
     */
    public static double[][] calcAdjoint(double[][] mat) {
        return calcTranspose(calcCofactorMatrix(mat));
    }

    /**
     *  calculates the determinant of the matrix
     *  passed.
     *
     */
    public static double calcDeterminant(double[][] mat) {
        int dim = mat[0].length;
        double ret = 0d;
        if (dim == 2) {
            ret = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
        } else if (dim > 2) {
            for (int a = 1; a <= dim; a++) {
                ret += mat[0][a - 1] * getCofactor(mat, 1, a);
            }
        }
        return ret;
    }

    /**
     *  Returns the cofactor of the matrix at the row, col
     *
     */
    public static double getCofactor(double[][] mat, int row, int col) {
        double sign = Math.pow(-1, (row + col));
        double det = calcDeterminant(getMinor(mat, row, col));
        double ret = sign * det;
        return ret;
    }

    /**
     *  Scalar multiplies this matrix by the scalar and
     *  returns the resulting matrix
     *
     */
    public static double[][] scalarMultiply(double[][] mat, double scalar) {
        for (int r = 0; r < mat.length; r++) {
            for (int c = 0; c < mat[0].length; c++) {
                mat[r][c] *= scalar;
            }
        }
        return mat;
    }

    /**
     *   multiplies the matrices and populates destmat with
     *   the result
     */
    public static void multiplyMatrices(double[][] mat1, double[][] mat2, double[][] destmat) {
        double[][] tmpmat = new double[mat1.length][mat2[0].length];
        for (int r = 0; r < mat1.length; r++) {
            for (int c = 0; c < mat1[0].length; c++) {
                for (int n = 0; n < mat1.length; n++) {
                    tmpmat[r][c] += mat1[r][n] * mat2[n][c];
                }
            }
        }
        for (int r = 0; r < mat1.length; r++) {
            for (int c = 0; c < mat2[0].length; c++) {
                destmat[r][c] = tmpmat[r][c];
            }
        }
    }

    /**
     *   Return the minor of the matrix by excluding
     *   row and col; NOTE: row and col are ONE based, not 
     *   zero based like the underlying arrays
     *
     */
    public static double[][] getMinor(double[][] mat, int row, int col) {
        int dim = mat[0].length - 1;
        double[][] minor = new double[dim][dim];
        int minor_row = 0;
        int minor_col = 0;
        for (int a = 0; a <= dim; a++) {
            if ((row - 1) != a) {
                for (int b = 0; b <= dim; b++) {
                    if ((col - 1) != b) {
                        minor[minor_row][minor_col] = mat[a][b];
                        minor_col++;
                    }
                }
                minor_row++;
                minor_col = 0;
            }
        }
        return minor;
    }

    public static String toString(double[][] mat) {
        DecimalFormat f = new DecimalFormat(" 0.00E00 ;-0.00E00");
        StringBuffer sb = new StringBuffer(1000);
        sb.append("\n");
        for (int a = 0; a < mat.length; a++) {
            sb.append("\n");
            for (int b = 0; b < mat[0].length; b++) {
                sb.append(f.format(mat[a][b]));
                sb.append(" ");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
