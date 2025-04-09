package proscene;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * Utility class that complements the PVector and PMatrix classes.
 */
public class PSUtility {

    static final float EPS10 = 1E-10f;

    static final float EPS8 = 1E-8f;

    static final float EPS6 = 1E-6f;

    static final float EPS5 = 1E-5f;

    static final float PI = 3.14159265359f;

    static final float PIO2 = 1.57079632679f;

    /**
	 * Utility function that simply projects {@code src} on the axis
	 * of direction {@code direction} that passes through the origin.
	 * <p> 
	 * {@code direction} does not need to be normalized (but must be non null).
	 */
    public static PVector projectVectorOnAxis(PVector src, PVector direction) {
        float directionSquaredNorm = squaredNorm(direction);
        if (directionSquaredNorm < EPS10) return null;
        float modulation = src.dot(direction) / directionSquaredNorm;
        return PVector.mult(direction, modulation);
    }

    /**
	 * Utility function that simply projects {@code src} on the plane
	 * whose normal is {@code normal} that passes through the origin. 
	 * <p> 
	 * {@code normal} does not need to be normalized (but must be non null).
	 */
    public static PVector projectVectorOnPlane(PVector src, PVector normal) {
        float normalSquaredNorm = squaredNorm(normal);
        if (normalSquaredNorm < EPS10) return null;
        float modulation = src.dot(normal) / normalSquaredNorm;
        return PVector.sub(src, PVector.mult(normal, modulation));
    }

    /**
	 * Utility function that returns the squared norm of the PVector.
	 */
    public static float squaredNorm(PVector v) {
        return (v.x * v.x) + (v.y * v.y) + (v.z * v.z);
    }

    /**
	 * Utility function that returns a PVector orthogonal to {@code v}. Its
	 * {@code mag()} depends on the PVector, but is zero only for a {@code null}
	 * PVector. Note that the function that associates an {@code orthogonalVector()}
	 * to a PVector is not continuous.
	 */
    public static PVector orthogonalVector(PVector v) {
        if ((PApplet.abs(v.y) >= 0.9f * PApplet.abs(v.x)) && (PApplet.abs(v.z) >= 0.9f * PApplet.abs(v.x))) return new PVector(0.0f, -v.z, v.y); else if ((PApplet.abs(v.x) >= 0.9f * PApplet.abs(v.y)) && (PApplet.abs(v.z) >= 0.9f * PApplet.abs(v.y))) return new PVector(-v.z, 0.0f, v.x); else return new PVector(-v.y, v.x, 0.0f);
    }

    /**
	 * Utility function that returns the PMatrix3D representation of the 4x4
	 * {@code matrix} given in OpenGL format.
	 */
    public static final PMatrix3D fromOpenGLToPMatrix3D(float[][] matrix) {
        float[][] m = transpose4x4Matrix(matrix);
        float m00 = m[0][0];
        float m01 = m[0][1];
        float m02 = m[0][2];
        float m03 = m[0][3];
        float m10 = m[1][0];
        float m11 = m[1][1];
        float m12 = m[1][2];
        float m13 = m[1][3];
        float m20 = m[2][0];
        float m21 = m[2][1];
        float m22 = m[2][2];
        float m23 = m[2][3];
        float m30 = m[3][0];
        float m31 = m[3][1];
        float m32 = m[3][2];
        float m33 = m[3][3];
        return new PMatrix3D(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /**
	 * Utility function that returns the PMatrix3D representation of the 16
	 * {@code array} given in OpenGL format.
	 */
    public static final PMatrix3D fromOpenGLArray(float[] array) {
        float[][] matrix = new float[4][4];
        int count = 0;
        for (int i = 0; i < 4; ++i) for (int j = 0; j < 4; ++j) matrix[i][j] = array[count++];
        return fromOpenGLToPMatrix3D(matrix);
    }

    /**
	 * Utility function that returns the [4][4]float matrix representation
	 * (European format) of the given PMatrix3D.
	 */
    public static final float[][] fromPMatrix3DToMatrix(PMatrix3D pM) {
        float[][] m = new float[4][4];
        m[0][0] = pM.m00;
        m[0][1] = pM.m01;
        m[0][2] = pM.m02;
        m[0][3] = pM.m03;
        m[1][0] = pM.m10;
        m[1][1] = pM.m11;
        m[1][2] = pM.m12;
        m[1][3] = pM.m13;
        m[2][0] = pM.m20;
        m[2][1] = pM.m21;
        m[2][2] = pM.m22;
        m[2][3] = pM.m23;
        m[3][0] = pM.m30;
        m[3][1] = pM.m31;
        m[3][2] = pM.m32;
        m[3][0] = pM.m33;
        return m;
    }

    /**
	 * Utility function that returns the [4][4]float matrix representation
	 * (OpenGL format) of the given PMatrix3D.
	 */
    public static final float[][] fromPMatrix3DToOpenGL(PMatrix3D pM) {
        return transpose4x4Matrix(fromPMatrix3DToMatrix(pM));
    }

    /**
	 * Utility function that returns the transpose of the given 3X3 matrix.
	 */
    public static final float[][] transpose3x3Matrix(float[][] m) {
        float[][] matrix = new float[4][4];
        for (int i = 0; i < 3; ++i) for (int j = 0; j < 3; ++j) matrix[i][j] = m[j][i];
        return matrix;
    }

    /**
	 * Utility function that returns the transpose of the given 4X4 matrix.
	 */
    public static final float[][] transpose4x4Matrix(float[][] m) {
        float[][] matrix = new float[4][4];
        for (int i = 0; i < 4; ++i) for (int j = 0; j < 4; ++j) matrix[i][j] = m[j][i];
        return matrix;
    }

    /**
	 * Utility function that returns the [4][4] float matrix version of the given {@code m} array.
	 */
    public static final float[][] fromArray4x4ToMatrix(float[] m) {
        float[][] mat = new float[4][4];
        for (int i = 0; i < 4; ++i) for (int j = 0; j < 4; ++j) mat[i][j] = m[i * 4 + j];
        return mat;
    }

    /**
	 * Utility function that returns the [16] float array version of the given {@code mat} matrix.
	 */
    public static final float[] from4x4MatrixToArray(float[][] mat) {
        float[] m = new float[16];
        for (int i = 0; i < 4; ++i) for (int j = 0; j < 4; ++j) m[i * 4 + j] = mat[i][j];
        return m;
    }

    /**
	 * Utility function that returns the 3x3 upper left sub-matrix of the given
	 * PMatrix3D.
	 */
    public static final float[][] get3x3UpperLeftMatrixFromPMatrix3D(PMatrix3D pM) {
        float[][] m = new float[3][3];
        m[0][0] = pM.m00;
        m[0][1] = pM.m01;
        m[0][2] = pM.m02;
        m[1][0] = pM.m10;
        m[1][1] = pM.m11;
        m[1][2] = pM.m12;
        m[2][0] = pM.m20;
        m[2][1] = pM.m21;
        m[2][2] = pM.m22;
        return m;
    }
}
