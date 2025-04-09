package spdrender.raytracer.base;

/**
 * A simple 3x3 transformation matrix.
 * @author Maximiliano Monterrubio Gutierrez.
 */
public class TransformMatrix {

    private double[][] m;

    /**
     * Creates a new null transformation matrix (The one with all zeros).
     */
    public TransformMatrix() {
        m = new double[3][3];
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m.length; ++j) {
                m[i][j] = 0.0f;
            }
        }
    }

    /** Creates a new transformation matrix with the given values
     * 
     * @param m A 3x3 bidimensional single precision floating point array that represents the matrix.
     */
    public TransformMatrix(double[][] m) {
        if (m.length != 3 && m[0].length != 3) {
            throw new IllegalArgumentException("Illegal transformation matrix size");
        } else {
            this.m = m;
        }
    }

    /**
     * Creates a new Matrix using the given vectors as 'column' vectors.
     * @param col1 First vector.
     * @param col2 Second vector.
     * @param col3 Third vector.
     */
    public TransformMatrix(Vector3D col1, Vector3D col2, Vector3D col3) {
        m = new double[3][3];
        m[0][0] = col1.x;
        m[1][0] = col1.y;
        m[2][0] = col1.z;
        m[0][1] = col2.x;
        m[1][1] = col2.y;
        m[2][1] = col2.z;
        m[0][2] = col3.x;
        m[1][2] = col3.y;
        m[2][2] = col3.z;
    }

    /** Composes two matrices.
     * 
     * @param t Another transformation matrix.
     * @return The matrix product of the calling instance and the given matrix.
     */
    public TransformMatrix compose(TransformMatrix t) {
        int i, j, k;
        double[][] nm = new double[3][3];
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                nm[i][j] = 0.0f;
                for (k = 0; k < 3; ++k) {
                    nm[i][j] += m[i][k] * t.m[k][j];
                }
            }
        }
        return new TransformMatrix(nm);
    }

    /**
     * Inverts this matrix.
     * @return The inverse matrix in case the calling instance is invertible, <code>null</code> otherwise.
     */
    public TransformMatrix invert() {
        double det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
        if (det == 0) {
            return null;
        }
        TransformMatrix tm = new TransformMatrix();
        tm.m[0][0] = m[1][1] * m[2][2] - m[1][2] * m[2][1];
        tm.m[0][1] = m[0][2] * m[2][1] - m[0][1] * m[2][2];
        tm.m[0][2] = m[0][1] * m[1][2] - m[0][2] * m[1][1];
        tm.m[1][0] = m[1][2] * m[2][0] - m[1][0] * m[2][2];
        tm.m[1][1] = m[0][0] * m[2][2] - m[0][2] * m[2][0];
        tm.m[1][2] = m[0][2] * m[1][0] - m[0][0] * m[1][2];
        tm.m[2][0] = m[1][0] * m[2][1] - m[1][1] * m[2][0];
        tm.m[2][1] = m[0][1] * m[2][0] - m[0][0] * m[2][1];
        tm.m[2][2] = m[0][0] * m[1][1] - m[0][1] * m[1][0];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                tm.m[i][j] /= det;
            }
        }
        return tm;
    }

    /**
     * Applies this transformation matrix to the given vector.
     * @param v 3D vector to apply the transformation.
     * @return Transformed vector.
     */
    public Vector3D apply(Vector3D v) {
        double x, y, z;
        x = m[0][0] * v.x + m[1][0] * v.y + m[2][0] * v.z;
        y = m[0][1] * v.x + m[1][1] * v.y + m[2][1] * v.z;
        z = m[0][2] * v.x + m[1][2] * v.y + m[2][2] * v.z;
        return new Vector3D(x, y, z);
    }

    /** 
     * Evaluates if two matrices are equal comparing all its entries.
     * @param t Another matrix
     * @return <code>true</code> in case both matrices are equal.
     */
    public boolean equals(TransformMatrix t) {
        int i, j;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                if (Math.abs(m[i][j] - t.m[i][j]) > Vector3D.EPSILON) return false;
            }
        }
        return true;
    }

    /** 
     * Performs matrix transposition.
     * @return The transpose matrix of the calling instance.
     */
    public TransformMatrix transpose() {
        double nm[][] = new double[3][3];
        int i, j;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                nm[i][j] = m[j][i];
            }
        }
        return new TransformMatrix(nm);
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                s += m[i][j] + ", ";
            }
            s += "\n";
        }
        return s;
    }
}
