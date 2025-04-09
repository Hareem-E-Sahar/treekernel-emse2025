package org.pqt.mr2rib.ribtranslator;

import java.util.Iterator;
import java.util.Vector;

public class Matrix extends Object implements Cloneable {

    private float[][] m_aaElement = new float[4][4];

    /** default constructor*/
    public Matrix() {
    }

    /** Basic constructor in which all the matrix values are given */
    public Matrix(float r1c1, float r1c2, float r1c3, float r1c4, float r2c1, float r2c2, float r2c3, float r2c4, float r3c1, float r3c2, float r3c3, float r3c4, float r4c1, float r4c2, float r4c3, float r4c4) {
        m_aaElement[0][0] = r1c1;
        m_aaElement[0][1] = r1c2;
        m_aaElement[0][2] = r1c3;
        m_aaElement[0][3] = r1c4;
        m_aaElement[1][0] = r2c1;
        m_aaElement[1][1] = r2c2;
        m_aaElement[1][2] = r2c3;
        m_aaElement[1][3] = r2c4;
        m_aaElement[2][0] = r3c1;
        m_aaElement[2][1] = r3c2;
        m_aaElement[2][2] = r3c3;
        m_aaElement[2][3] = r3c4;
        m_aaElement[3][0] = r4c1;
        m_aaElement[3][1] = r4c2;
        m_aaElement[3][2] = r4c3;
        m_aaElement[3][3] = r4c4;
    }

    /** Scale matrix constructor
     * @param xs Float scale in x.
     * @param ys Float scale in y.
     * @param zs Float scale in z.
     */
    public Matrix(float xs, float ys, float zs) {
        identity();
        m_aaElement[0][0] = xs;
        m_aaElement[1][1] = ys;
        m_aaElement[2][2] = zs;
    }

    /** Translate constructor. Constructs a translation matrix translating by a given vector.
     * @param trans	The vector by which to translate.
     */
    public Matrix(Point trans) {
        identity();
        m_aaElement[3][0] = trans.value[0];
        m_aaElement[3][1] = trans.value[1];
        m_aaElement[3][2] = trans.value[2];
    }

    /** Rotate matrix constructor
     * @param angle	The angle to rotate by.
     * @param axis The axis about which to rotate.
     */
    public Matrix(float angle, Point axis) {
        identity();
        if (angle != 0.0f && axis.lengthSquared() != 0.0f) rotate(angle, axis);
    }

    /** Skew matrix constructor
     * @param Angle
     * @param dx1, dy1, dz1
     * @param dx2, dy2, dz2
     *
     * For now base this on what Larry Gritz posted a while back.
     * There are some more optimizations that can be done.
     */
    public Matrix(float angle, float dx1, float dy1, float dz1, float dx2, float dy2, float dz2) {
        Point d1 = new Point(dx1, dy1, dz1);
        Point d2 = new Point(dx2, dy2, dz2);
        d1.normalize();
        d2.normalize();
        float d1d2dot = d1.dot(d2);
        float axisangle = (float) Math.acos(d1d2dot);
        if (angle >= axisangle || angle <= (axisangle - Math.PI)) {
            identity();
        } else {
            Point right = d1.cross(d2);
            right.normalize();
            Matrix Rot = new Matrix(right.value[0], d1.value[0], d2.value[0], 0, right.value[1], d1.value[1], d2.value[1], 0, right.value[2], d1.value[2], d2.value[2], 0, 0, 0, 0, 1);
            float par = d1d2dot;
            float perp = (float) Math.sqrt(1 - par * par);
            float s = (float) Math.tan(angle + Math.acos(perp)) * perp - par;
            Matrix Skw = new Matrix(1, 0, 0, 0, 0, 1, s, 0, 0, 0, 1, 0, 0, 0, 0, 1);
        }
    }

    /** Copy constructor.
     */
    public Matrix(Matrix from) {
        Matrix m = (Matrix) from.clone();
        m_aaElement = m.m_aaElement;
    }

    /** Constructor from a 2D float array.
     * @param From 2D float array to copy data from.
     */
    public Matrix(float[][] from) {
        set(from);
    }

    /** Constructor from a float array.
     * @param From 1D float array to copy data from, in row order
     */
    public Matrix(float[] from) {
        set(from);
    }

    /** Construct from a Vector containing an array of 16 float
     *@param token the token to use
     */
    public Matrix(Vector array) {
        if (array.size() != 16) {
            m_aaElement = null;
            return;
        }
        float f;
        Object o;
        Iterator i = array.iterator();
        for (int j = 0; j < 4; j++) for (int k = 0; k < 4; k++) {
            o = i.next();
            if (!(o instanceof Float)) {
                m_aaElement = null;
                return;
            }
            m_aaElement[j][k] = ((Float) o).floatValue();
        }
    }

    /** Turn the matrix into an identity.
     */
    public void identity() {
        m_aaElement[0][1] = m_aaElement[0][2] = m_aaElement[0][3] = m_aaElement[1][0] = m_aaElement[1][2] = m_aaElement[1][3] = m_aaElement[2][0] = m_aaElement[2][1] = m_aaElement[2][3] = m_aaElement[3][0] = m_aaElement[3][1] = m_aaElement[3][2] = 0.0f;
        m_aaElement[0][0] = m_aaElement[1][1] = m_aaElement[2][2] = m_aaElement[3][3] = 1.0f;
    }

    /**Create an identity matrix*/
    public static Matrix createIdentity() {
        Matrix result = new Matrix();
        result.identity();
        return result;
    }

    /** Scale matrix uniformly in all three axes.
     * @param factor The amount to scale by.
     */
    public void scale(float factor) {
        if (factor != 1.0f) scale(factor, factor, factor);
    }

    /** Scale matrix in three axes.
     * @param xs X scale factor.
     * @param ys Y scale factor.
     * @param zs Z scale factor.
     */
    public void scale(float xs, float ys, float zs) {
        Matrix scale = new Matrix(xs, ys, zs);
        preMultiply(scale);
    }

    /** Rotates this matrix by a given rotation angle about a given axis through the origin.
     * @param angle	The angle to rotate by in.
     * @param axis The axis about which to rotate.
     */
    public void rotate(double angle, Point axis) {
        Matrix r = new Matrix();
        double s = Math.cos(angle / 2);
        double sp = Math.sin(angle / 2);
        double l = axis.length();
        double a = axis.value[0] * sp / l;
        double b = axis.value[1] * sp / l;
        double c = axis.value[2] * sp / l;
        l = Math.sqrt(a * a + b * b + c * c + s * s);
        a /= l;
        b /= l;
        c /= l;
        s /= l;
        r.m_aaElement[0][0] = (float) (1 - 2 * b * b - 2 * c * c);
        r.m_aaElement[0][1] = (float) (2 * a * b - 2 * s * c);
        r.m_aaElement[0][2] = (float) (2 * a * c + 2 * s * b);
        r.m_aaElement[0][3] = 0;
        r.m_aaElement[1][0] = (float) (2 * a * b + 2 * s * c);
        r.m_aaElement[1][1] = (float) (1 - 2 * a * a - 2 * c * c);
        r.m_aaElement[1][2] = (float) (2 * b * c - 2 * s * a);
        r.m_aaElement[1][3] = 0;
        r.m_aaElement[2][0] = (float) (2 * a * c - 2 * s * b);
        r.m_aaElement[2][1] = (float) (2 * b * c + 2 * s * a);
        r.m_aaElement[2][2] = (float) (1 - 2 * a * a - 2 * b * b);
        r.m_aaElement[2][3] = 0;
        r.m_aaElement[3][0] = 0;
        r.m_aaElement[3][1] = 0;
        r.m_aaElement[3][2] = 0;
        r.m_aaElement[3][3] = 1;
        preMultiply(r);
    }

    /** Translates this matrix by a given vector.
     * @param trans	The vector by which to translate.
     */
    public void translate(Point trans) {
        Matrix matTrans = new Matrix(trans);
        preMultiply(matTrans);
    }

    /** Translates this matrix by three axis distances.
     * @param xt X distance to translate.
     * @param yt Y distance to translate.
     * @param zt Z distance to translate.
     */
    public void translate(float xt, float yt, float zt) {
        if (xt != 0.0f || yt != 0.0f || zt != 0.0f) translate(new Point(xt, yt, zt));
    }

    /** Shears this matrix's X axis according to two shear factors, yh and zh
     * @param yh Y shear factor.
     * @param zh Z shear factor.
     */
    public void shearX(float yh, float zh) {
        Matrix shear = new Matrix();
        shear.identity();
        shear.m_aaElement[0][1] = yh;
        shear.m_aaElement[0][2] = zh;
        preMultiply(shear);
    }

    /** Shears this matrix's Y axis according to two shear factors, xh and zh
     * @param xh X shear factor.
     * @param zh Z shear factor.
     */
    public void shearY(float xh, float zh) {
        Matrix shear = new Matrix();
        shear.identity();
        shear.m_aaElement[1][0] = xh;
        shear.m_aaElement[1][2] = zh;
        preMultiply(shear);
    }

    /** Shears this matrix's Z axis according to two shear factors, xh and yh
     * @param xh X shear factor.
     * @param yh Y shear factor.
     */
    public void shearZ(float xh, float yh) {
        Matrix shear = new Matrix();
        shear.identity();
        shear.m_aaElement[2][0] = xh;
        shear.m_aaElement[2][1] = yh;
        preMultiply(shear);
    }

    /** Skew matrix
     * @param xs X scale factor.
     * @param ys Y scale factor.
     * @param zs Z scale factor.
     */
    public void skew(float angle, float dx1, float dy1, float dz1, float dx2, float dy2, float dz2) {
        Matrix skew = new Matrix(angle, dx1, dy1, dz1, dx2, dy2, dz2);
        preMultiply(skew);
    }

    /** Normalise the matrix, returning the homogenous part of the matrix to 1.
     */
    public void normalise() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m_aaElement[i][j] /= m_aaElement[3][3];
            }
        }
    }

    /** Multiply two matrices together without changing either
     * @param from The matrix to multiply with this matrix.
     * @return The resultant multiplied matrix.
     */
    public static Matrix multiply(Matrix to, Matrix from) {
        Matrix temp = (Matrix) to.clone();
        temp.multiply(from);
        return temp;
    }

    /** Multiply this matrix by specified matrix. This now takes into account the types of matrices, in an attempt to speed it up.
     * @param From The matrix to multiply with this matrix.
     * @return the result
     */
    public Matrix multiply(Matrix from) {
        float[][] temp = m_aaElement;
        m_aaElement = new float[4][4];
        m_aaElement[0][0] = from.m_aaElement[0][0] * temp[0][0] + from.m_aaElement[0][1] * temp[1][0] + from.m_aaElement[0][2] * temp[2][0] + from.m_aaElement[0][3] * temp[3][0];
        m_aaElement[0][1] = from.m_aaElement[0][0] * temp[0][1] + from.m_aaElement[0][1] * temp[1][1] + from.m_aaElement[0][2] * temp[2][1] + from.m_aaElement[0][3] * temp[3][1];
        m_aaElement[0][2] = from.m_aaElement[0][0] * temp[0][2] + from.m_aaElement[0][1] * temp[1][2] + from.m_aaElement[0][2] * temp[2][2] + from.m_aaElement[0][3] * temp[3][2];
        m_aaElement[0][3] = from.m_aaElement[0][0] * temp[0][3] + from.m_aaElement[0][1] * temp[1][3] + from.m_aaElement[0][2] * temp[2][3] + from.m_aaElement[0][3] * temp[3][3];
        m_aaElement[1][0] = from.m_aaElement[1][0] * temp[0][0] + from.m_aaElement[1][1] * temp[1][0] + from.m_aaElement[1][2] * temp[2][0] + from.m_aaElement[1][3] * temp[3][0];
        m_aaElement[1][1] = from.m_aaElement[1][0] * temp[0][1] + from.m_aaElement[1][1] * temp[1][1] + from.m_aaElement[1][2] * temp[2][1] + from.m_aaElement[1][3] * temp[3][1];
        m_aaElement[1][2] = from.m_aaElement[1][0] * temp[0][2] + from.m_aaElement[1][1] * temp[1][2] + from.m_aaElement[1][2] * temp[2][2] + from.m_aaElement[1][3] * temp[3][2];
        m_aaElement[1][3] = from.m_aaElement[1][0] * temp[0][3] + from.m_aaElement[1][1] * temp[1][3] + from.m_aaElement[1][2] * temp[2][3] + from.m_aaElement[1][3] * temp[3][3];
        m_aaElement[2][0] = from.m_aaElement[2][0] * temp[0][0] + from.m_aaElement[2][1] * temp[1][0] + from.m_aaElement[2][2] * temp[2][0] + from.m_aaElement[2][3] * temp[3][0];
        m_aaElement[2][1] = from.m_aaElement[2][0] * temp[0][1] + from.m_aaElement[2][1] * temp[1][1] + from.m_aaElement[2][2] * temp[2][1] + from.m_aaElement[2][3] * temp[3][1];
        m_aaElement[2][2] = from.m_aaElement[2][0] * temp[0][2] + from.m_aaElement[2][1] * temp[1][2] + from.m_aaElement[2][2] * temp[2][2] + from.m_aaElement[2][3] * temp[3][2];
        m_aaElement[2][3] = from.m_aaElement[2][0] * temp[0][3] + from.m_aaElement[2][1] * temp[1][3] + from.m_aaElement[2][2] * temp[2][3] + from.m_aaElement[2][3] * temp[3][3];
        m_aaElement[3][0] = from.m_aaElement[3][0] * temp[0][0] + from.m_aaElement[3][1] * temp[1][0] + from.m_aaElement[3][2] * temp[2][0] + from.m_aaElement[3][3] * temp[3][0];
        m_aaElement[3][1] = from.m_aaElement[3][0] * temp[0][1] + from.m_aaElement[3][1] * temp[1][1] + from.m_aaElement[3][2] * temp[2][1] + from.m_aaElement[3][3] * temp[3][1];
        m_aaElement[3][2] = from.m_aaElement[3][0] * temp[0][2] + from.m_aaElement[3][1] * temp[1][2] + from.m_aaElement[3][2] * temp[2][2] + from.m_aaElement[3][3] * temp[3][2];
        m_aaElement[3][3] = from.m_aaElement[3][0] * temp[0][3] + from.m_aaElement[3][1] * temp[1][3] + from.m_aaElement[3][2] * temp[2][3] + from.m_aaElement[3][3] * temp[3][3];
        return this;
    }

    /** Matrix multiplication of the form a = b * a.
     * @param from The matrix to multiply with this matrix.
     */
    public Matrix preMultiply(Matrix from) {
        float[][] temp = m_aaElement;
        m_aaElement = new float[4][4];
        m_aaElement[0][0] = temp[0][0] * from.m_aaElement[0][0] + temp[0][1] * from.m_aaElement[1][0] + temp[0][2] * from.m_aaElement[2][0] + temp[0][3] * from.m_aaElement[3][0];
        m_aaElement[0][1] = temp[0][0] * from.m_aaElement[0][1] + temp[0][1] * from.m_aaElement[1][1] + temp[0][2] * from.m_aaElement[2][1] + temp[0][3] * from.m_aaElement[3][1];
        m_aaElement[0][2] = temp[0][0] * from.m_aaElement[0][2] + temp[0][1] * from.m_aaElement[1][2] + temp[0][2] * from.m_aaElement[2][2] + temp[0][3] * from.m_aaElement[3][2];
        m_aaElement[0][3] = temp[0][0] * from.m_aaElement[0][3] + temp[0][1] * from.m_aaElement[1][3] + temp[0][2] * from.m_aaElement[2][3] + temp[0][3] * from.m_aaElement[3][3];
        m_aaElement[1][0] = temp[1][0] * from.m_aaElement[0][0] + temp[1][1] * from.m_aaElement[1][0] + temp[1][2] * from.m_aaElement[2][0] + temp[1][3] * from.m_aaElement[3][0];
        m_aaElement[1][1] = temp[1][0] * from.m_aaElement[0][1] + temp[1][1] * from.m_aaElement[1][1] + temp[1][2] * from.m_aaElement[2][1] + temp[1][3] * from.m_aaElement[3][1];
        m_aaElement[1][2] = temp[1][0] * from.m_aaElement[0][2] + temp[1][1] * from.m_aaElement[1][2] + temp[1][2] * from.m_aaElement[2][2] + temp[1][3] * from.m_aaElement[3][2];
        m_aaElement[1][3] = temp[1][0] * from.m_aaElement[0][3] + temp[1][1] * from.m_aaElement[1][3] + temp[1][2] * from.m_aaElement[2][3] + temp[1][3] * from.m_aaElement[3][3];
        m_aaElement[2][0] = temp[2][0] * from.m_aaElement[0][0] + temp[2][1] * from.m_aaElement[1][0] + temp[2][2] * from.m_aaElement[2][0] + temp[2][3] * from.m_aaElement[3][0];
        m_aaElement[2][1] = temp[2][0] * from.m_aaElement[0][1] + temp[2][1] * from.m_aaElement[1][1] + temp[2][2] * from.m_aaElement[2][1] + temp[2][3] * from.m_aaElement[3][1];
        m_aaElement[2][2] = temp[2][0] * from.m_aaElement[0][2] + temp[2][1] * from.m_aaElement[1][2] + temp[2][2] * from.m_aaElement[2][2] + temp[2][3] * from.m_aaElement[3][2];
        m_aaElement[2][3] = temp[2][0] * from.m_aaElement[0][3] + temp[2][1] * from.m_aaElement[1][3] + temp[2][2] * from.m_aaElement[2][3] + temp[2][3] * from.m_aaElement[3][3];
        m_aaElement[3][0] = temp[3][0] * from.m_aaElement[0][0] + temp[3][1] * from.m_aaElement[1][0] + temp[3][2] * from.m_aaElement[2][0] + temp[3][3] * from.m_aaElement[3][0];
        m_aaElement[3][1] = temp[3][0] * from.m_aaElement[0][1] + temp[3][1] * from.m_aaElement[1][1] + temp[3][2] * from.m_aaElement[2][1] + temp[3][3] * from.m_aaElement[3][1];
        m_aaElement[3][2] = temp[3][0] * from.m_aaElement[0][2] + temp[3][1] * from.m_aaElement[1][2] + temp[3][2] * from.m_aaElement[2][2] + temp[3][3] * from.m_aaElement[3][2];
        m_aaElement[3][3] = temp[3][0] * from.m_aaElement[0][3] + temp[3][1] * from.m_aaElement[1][3] + temp[3][2] * from.m_aaElement[2][3] + temp[3][3] * from.m_aaElement[3][3];
        return this;
    }

    /** Premultiplies this matrix by a vector, returning v*m. This is the same as postmultiply the transpose of m by a vector: T(m)*v
     * @param Vector The vector to multiply.
     * @return The result of multiplying the vector by this matrix.
     */
    public Point4D preMultiply(Point4D vector) {
        Point4D result = new Point4D();
        result.value[0] = m_aaElement[0][0] * vector.value[0] + m_aaElement[0][1] * vector.value[1] + m_aaElement[0][2] * vector.value[2] + m_aaElement[0][3] * vector.value[3];
        result.value[1] = m_aaElement[1][0] * vector.value[0] + m_aaElement[1][1] * vector.value[1] + m_aaElement[1][2] * vector.value[2] + m_aaElement[1][3] * vector.value[3];
        result.value[2] = m_aaElement[2][0] * vector.value[0] + m_aaElement[2][1] * vector.value[1] + m_aaElement[2][2] * vector.value[2] + m_aaElement[2][3] * vector.value[3];
        result.value[3] = m_aaElement[3][0] * vector.value[0] + m_aaElement[3][1] * vector.value[1] + m_aaElement[3][2] * vector.value[2] + m_aaElement[3][3] * vector.value[3];
        return result;
    }

    /** Apply scale matrix uniformly in all three axes.
     * @param s The amount by which to scale matrix.
     * @return Result of scaling this matrix by S.
     */
    public static Matrix multiply(Matrix m, float s) {
        Matrix temp = new Matrix(m);
        temp.multiply(s);
        return (temp);
    }

    /** Apply scale matrix uniformly in all three axes to this matrix.
     * @param S The amount by which to scale this matrix.
     * @return The result scaling this matrix by S.
     */
    public Matrix multiply(float s) {
        Matrix scaleMatrix = new Matrix(s, s, s);
        preMultiply(scaleMatrix);
        return this;
    }

    /** Multiply a vector by this matrix.
     * @param Vector	: The vector to multiply.
     * @return The result of multiplying the vector by this matrix.
     */
    public Point4D multiply(Point4D vector) {
        Point4D result = new Point4D();
        result.value[0] = m_aaElement[0][0] * vector.value[0] + m_aaElement[1][0] * vector.value[1] + m_aaElement[2][0] * vector.value[2] + m_aaElement[3][0] * vector.value[3];
        result.value[1] = m_aaElement[0][1] * vector.value[0] + m_aaElement[1][1] * vector.value[1] + m_aaElement[2][1] * vector.value[2] + m_aaElement[3][1] * vector.value[3];
        result.value[2] = m_aaElement[0][2] * vector.value[0] + m_aaElement[1][2] * vector.value[1] + m_aaElement[2][2] * vector.value[2] + m_aaElement[3][2] * vector.value[3];
        result.value[3] = m_aaElement[0][3] * vector.value[0] + m_aaElement[1][3] * vector.value[1] + m_aaElement[2][3] * vector.value[2] + m_aaElement[3][3] * vector.value[3];
        return result;
    }

    /** premultiply a Point by this matrix
     * @param point the point to multiply
     * @return the transformed point*/
    public Point preMultiply(Point point) {
        Point4D p = new Point4D(point.value[0], point.value[1], point.value[2], 1f);
        return preMultiply(p).toPoint();
    }

    /** Multiply a vector by this matrix.
     * @param Vector The vector to multiply.
     * @return The result of multiplying the vector by this matrix.
     */
    public Point multiply(Point vector) {
        Point result = new Point();
        float h = (m_aaElement[0][3] * vector.value[0] + m_aaElement[1][3] * vector.value[1] + m_aaElement[2][3] * vector.value[2] + m_aaElement[3][3]);
        result.value[0] = (m_aaElement[0][0] * vector.value[0] + m_aaElement[1][0] * vector.value[1] + m_aaElement[2][0] * vector.value[2] + m_aaElement[3][0]) / h;
        result.value[1] = (m_aaElement[0][1] * vector.value[0] + m_aaElement[1][1] * vector.value[1] + m_aaElement[2][1] * vector.value[2] + m_aaElement[3][1]) / h;
        result.value[2] = (m_aaElement[0][2] * vector.value[0] + m_aaElement[1][2] * vector.value[1] + m_aaElement[2][2] * vector.value[2] + m_aaElement[3][2]) / h;
        return result;
    }

    /** Translate matrix by 4D Vector.
     * @param m the matrix to translate
     * @param vector The vector to translate by.
     * @return Result of translating this matrix by the vector.
     */
    public static Matrix add(Matrix m, Point4D vector) {
        Matrix temp = new Matrix(m);
        temp.add(vector);
        return temp;
    }

    /** Translate this matrix by 4D Vector.
     * @param vector The vector to translate by.
     * @return The result of translating this matrix by the specified vector.
     */
    public Matrix add(Point4D vector) {
        Matrix trans = new Matrix(new Point(vector.value[0] / vector.value[3], vector.value[1] / vector.value[3], vector.value[2] / vector.value[3]));
        preMultiply(trans);
        return this;
    }

    /** Translate matrix by 4D Vector.
     * @param Vector The vector to translate by.
     * @return Result of translating this matrix by the vector.
     */
    public static Matrix minus(Matrix m, Point4D vector) {
        Matrix temp = new Matrix(m);
        temp.minus(vector);
        return temp;
    }

    /** Translate this matrix by 4D Vector.
     * @param Vector The vector to translate by.
     */
    public Matrix minus(Point4D vector) {
        Matrix trans = new Matrix(new Point(-vector.value[0] / vector.value[3], -vector.value[1] / vector.value[3], -vector.value[2] / vector.value[3]));
        preMultiply(trans);
        return this;
    }

    /** Add two matrices.
     * @param From The matrix to add.
     * @return Result of adding From to this matrix.
     */
    public static Matrix add(Matrix a, Matrix b) {
        Matrix temp = new Matrix(a);
        temp.add(b);
        return temp;
    }

    /** Add a given matrix to this matrix.
     * @param From The matrix to add.
     */
    public Matrix add(Matrix from) {
        m_aaElement[0][0] += from.m_aaElement[0][0];
        m_aaElement[1][0] += from.m_aaElement[1][0];
        m_aaElement[2][0] += from.m_aaElement[2][0];
        m_aaElement[3][0] += from.m_aaElement[3][0];
        m_aaElement[0][1] += from.m_aaElement[0][1];
        m_aaElement[1][1] += from.m_aaElement[1][1];
        m_aaElement[2][1] += from.m_aaElement[2][1];
        m_aaElement[3][1] += from.m_aaElement[3][1];
        m_aaElement[0][2] += from.m_aaElement[0][2];
        m_aaElement[1][2] += from.m_aaElement[1][2];
        m_aaElement[2][2] += from.m_aaElement[2][2];
        m_aaElement[3][2] += from.m_aaElement[3][2];
        m_aaElement[0][3] += from.m_aaElement[0][3];
        m_aaElement[1][3] += from.m_aaElement[1][3];
        m_aaElement[2][3] += from.m_aaElement[2][3];
        m_aaElement[3][3] += from.m_aaElement[3][3];
        return this;
    }

    /** Subtract two matrices.
     * @param a,b The matrix to subtract.
     * \return Result of subtracting From from this matrix.
     */
    public static Matrix minus(Matrix a, Matrix b) {
        Matrix temp = new Matrix(a);
        temp.minus(b);
        return temp;
    }

    /** Subtract a given matrix from this matrix.
     * @param From The matrix to subtract.
     */
    public Matrix minus(Matrix from) {
        m_aaElement[0][0] -= from.m_aaElement[0][0];
        m_aaElement[1][0] -= from.m_aaElement[1][0];
        m_aaElement[2][0] -= from.m_aaElement[2][0];
        m_aaElement[3][0] -= from.m_aaElement[3][0];
        m_aaElement[0][1] -= from.m_aaElement[0][1];
        m_aaElement[1][1] -= from.m_aaElement[1][1];
        m_aaElement[2][1] -= from.m_aaElement[2][1];
        m_aaElement[3][1] -= from.m_aaElement[3][1];
        m_aaElement[0][2] -= from.m_aaElement[0][2];
        m_aaElement[1][2] -= from.m_aaElement[1][2];
        m_aaElement[2][2] -= from.m_aaElement[2][2];
        m_aaElement[3][2] -= from.m_aaElement[3][2];
        m_aaElement[0][3] -= from.m_aaElement[0][3];
        m_aaElement[1][3] -= from.m_aaElement[1][3];
        m_aaElement[2][3] -= from.m_aaElement[2][3];
        m_aaElement[3][3] -= from.m_aaElement[3][3];
        return this;
    }

    /** Copy function.
     *
     */
    public Object clone() {
        Object o;
        try {
            o = (Matrix) super.clone();
        } catch (Exception e) {
            return null;
        }
        Matrix temp = (Matrix) o;
        temp.m_aaElement = new float[4][4];
        temp.m_aaElement[0][0] = m_aaElement[0][0];
        temp.m_aaElement[1][0] = m_aaElement[1][0];
        temp.m_aaElement[2][0] = m_aaElement[2][0];
        temp.m_aaElement[3][0] = m_aaElement[3][0];
        temp.m_aaElement[0][1] = m_aaElement[0][1];
        temp.m_aaElement[1][1] = m_aaElement[1][1];
        temp.m_aaElement[2][1] = m_aaElement[2][1];
        temp.m_aaElement[3][1] = m_aaElement[3][1];
        temp.m_aaElement[0][2] = m_aaElement[0][2];
        temp.m_aaElement[1][2] = m_aaElement[1][2];
        temp.m_aaElement[2][2] = m_aaElement[2][2];
        temp.m_aaElement[3][2] = m_aaElement[3][2];
        temp.m_aaElement[0][3] = m_aaElement[0][3];
        temp.m_aaElement[1][3] = m_aaElement[1][3];
        temp.m_aaElement[2][3] = m_aaElement[2][3];
        temp.m_aaElement[3][3] = m_aaElement[3][3];
        return o;
    }

    /** Copy function.
     * @param from Renderman matrix to copy information from.
     */
    public Matrix set(float[][] from) {
        m_aaElement[0][0] = from[0][0];
        m_aaElement[1][0] = from[1][0];
        m_aaElement[2][0] = from[2][0];
        m_aaElement[3][0] = from[3][0];
        m_aaElement[0][1] = from[0][1];
        m_aaElement[1][1] = from[1][1];
        m_aaElement[2][1] = from[2][1];
        m_aaElement[3][1] = from[3][1];
        m_aaElement[0][2] = from[0][2];
        m_aaElement[1][2] = from[1][2];
        m_aaElement[2][2] = from[2][2];
        m_aaElement[3][2] = from[3][2];
        m_aaElement[0][3] = from[0][3];
        m_aaElement[1][3] = from[1][3];
        m_aaElement[2][3] = from[2][3];
        m_aaElement[3][3] = from[3][3];
        return this;
    }

    /** Copy function.
     * @param From Renderman matrix to copy information from.
     */
    public Matrix set(float[] from) {
        m_aaElement[0][0] = from[0];
        m_aaElement[0][1] = from[1];
        m_aaElement[0][2] = from[2];
        m_aaElement[0][3] = from[3];
        m_aaElement[1][0] = from[4];
        m_aaElement[1][1] = from[5];
        m_aaElement[1][2] = from[6];
        m_aaElement[1][3] = from[7];
        m_aaElement[2][0] = from[8];
        m_aaElement[2][1] = from[9];
        m_aaElement[2][2] = from[10];
        m_aaElement[2][3] = from[11];
        m_aaElement[3][0] = from[12];
        m_aaElement[3][1] = from[13];
        m_aaElement[3][2] = from[14];
        m_aaElement[3][3] = from[15];
        return this;
    }

    /** Returns the inverse of this matrix using an algorithm from Graphics
     * \param Gems IV (p554) - Gauss-Jordan elimination with partial pivoting
     */
    public static Matrix inverse(Matrix m) {
        Matrix b = new Matrix();
        Matrix a = new Matrix(m);
        b.identity();
        int i;
        int j;
        int i1;
        for (j = 0; j < 4; j++) {
            i1 = j;
            for (i = j + 1; i < 4; i++) {
                if (Math.abs(a.m_aaElement[i][j]) > Math.abs(a.m_aaElement[i1][j])) {
                    i1 = i;
                }
            }
            if (i1 != j) {
                float temp;
                temp = a.m_aaElement[i1][0];
                a.m_aaElement[i1][0] = a.m_aaElement[j][0];
                a.m_aaElement[j][0] = temp;
                temp = a.m_aaElement[i1][1];
                a.m_aaElement[i1][1] = a.m_aaElement[j][1];
                a.m_aaElement[j][1] = temp;
                temp = a.m_aaElement[i1][2];
                a.m_aaElement[i1][2] = a.m_aaElement[j][2];
                a.m_aaElement[j][2] = temp;
                temp = a.m_aaElement[i1][3];
                a.m_aaElement[i1][3] = a.m_aaElement[j][3];
                a.m_aaElement[j][3] = temp;
                temp = b.m_aaElement[i1][0];
                b.m_aaElement[i1][0] = b.m_aaElement[j][0];
                b.m_aaElement[j][0] = temp;
                temp = b.m_aaElement[i1][1];
                b.m_aaElement[i1][1] = b.m_aaElement[j][1];
                b.m_aaElement[j][1] = temp;
                temp = b.m_aaElement[i1][2];
                b.m_aaElement[i1][2] = b.m_aaElement[j][2];
                b.m_aaElement[j][2] = temp;
                temp = b.m_aaElement[i1][3];
                b.m_aaElement[i1][3] = b.m_aaElement[j][3];
                b.m_aaElement[j][3] = temp;
            }
            if (a.m_aaElement[j][j] == 0.0f) {
                return null;
            }
            float scale = 1.0f / a.m_aaElement[j][j];
            b.m_aaElement[j][0] *= scale;
            b.m_aaElement[j][1] *= scale;
            b.m_aaElement[j][2] *= scale;
            b.m_aaElement[j][3] *= scale;
            for (i1 = j + 1; i1 < 4; i1++) {
                a.m_aaElement[j][i1] *= scale;
            }
            a.m_aaElement[j][j] = 1.0f;
            for (i = 0; i < 4; i++) {
                if (i != j) {
                    scale = a.m_aaElement[i][j];
                    b.m_aaElement[i][0] -= scale * b.m_aaElement[j][0];
                    b.m_aaElement[i][1] -= scale * b.m_aaElement[j][1];
                    b.m_aaElement[i][2] -= scale * b.m_aaElement[j][2];
                    b.m_aaElement[i][3] -= scale * b.m_aaElement[j][3];
                    for (i1 = j + 1; i1 < 4; i1++) {
                        a.m_aaElement[i][i1] -= scale * a.m_aaElement[j][i1];
                    }
                    a.m_aaElement[i][j] = 0.0f;
                }
            }
        }
        return b;
    }

    /** Returns the transpose of this matrix
     */
    public static Matrix transpose(Matrix m) {
        Matrix temp = new Matrix();
        temp.m_aaElement[0][0] = m.m_aaElement[0][0];
        temp.m_aaElement[0][1] = m.m_aaElement[1][0];
        temp.m_aaElement[0][2] = m.m_aaElement[2][0];
        temp.m_aaElement[0][3] = m.m_aaElement[3][0];
        temp.m_aaElement[1][0] = m.m_aaElement[0][1];
        temp.m_aaElement[1][1] = m.m_aaElement[1][1];
        temp.m_aaElement[1][2] = m.m_aaElement[2][1];
        temp.m_aaElement[1][3] = m.m_aaElement[3][1];
        temp.m_aaElement[2][0] = m.m_aaElement[0][2];
        temp.m_aaElement[2][1] = m.m_aaElement[1][2];
        temp.m_aaElement[2][2] = m.m_aaElement[2][2];
        temp.m_aaElement[2][3] = m.m_aaElement[3][2];
        temp.m_aaElement[3][0] = m.m_aaElement[0][3];
        temp.m_aaElement[3][1] = m.m_aaElement[1][3];
        temp.m_aaElement[3][2] = m.m_aaElement[2][3];
        temp.m_aaElement[3][3] = m.m_aaElement[3][3];
        return temp;
    }

    /** A utility function, used by GetDeterminant
     * Calculates the determinant of a 2x2 matrix
     */
    protected static float det2x2(float a, float b, float c, float d) {
        return a * d - b * c;
    }

    /** A utility function, used by CqMatrix::GetDeterminant
     * Calculates the determinant of a 3x3 matrix
     */
    protected static float det3x3(float a1, float a2, float a3, float b1, float b2, float b3, float c1, float c2, float c3) {
        return a1 * det2x2(b2, b3, c2, c3) - b1 * det2x2(a2, a3, c2, c3) + c1 * det2x2(a2, a3, b2, b3);
    }

    /** Returns the determinant of this matrix using an algorithm from
     * Graphics Gems I (p768)
     */
    public float determinant() {
        float a1 = m_aaElement[0][0];
        float b1 = m_aaElement[0][1];
        float c1 = m_aaElement[0][2];
        float d1 = m_aaElement[0][3];
        float a2 = m_aaElement[1][0];
        float b2 = m_aaElement[1][1];
        float c2 = m_aaElement[1][2];
        float d2 = m_aaElement[1][3];
        float a3 = m_aaElement[2][0];
        float b3 = m_aaElement[2][1];
        float c3 = m_aaElement[2][2];
        float d3 = m_aaElement[2][3];
        float a4 = m_aaElement[3][0];
        float b4 = m_aaElement[3][1];
        float c4 = m_aaElement[3][2];
        float d4 = m_aaElement[3][3];
        return a1 * det3x3(b2, b3, b4, c2, c3, c4, d2, d3, d4) - b1 * det3x3(a2, a3, a4, c2, c3, c4, d2, d3, d4) + c1 * det3x3(a2, a3, a4, b2, b3, b4, d2, d3, d4) - d1 * det3x3(a2, a3, a4, b2, b3, b4, c2, c3, c4);
    }

    /** Converts a matrix to a string.
     */
    public String toString() {
        StringBuffer s = new StringBuffer(250);
        s.append("[ ");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                s.append(m_aaElement[i][j]);
                s.append(' ');
            }
        }
        s.append(" ]");
        return s.toString();
    }

    /** Scale each element by the specified value.
     * @param S The amount by which to scale the matrix elements.
     * @return Result of scaling this matrix by S.
     */
    public static Matrix multiply(float s, Matrix a) {
        Matrix temp = new Matrix(a);
        temp.m_aaElement[0][0] *= s;
        temp.m_aaElement[1][0] *= s;
        temp.m_aaElement[2][0] *= s;
        temp.m_aaElement[3][0] *= s;
        temp.m_aaElement[0][1] *= s;
        temp.m_aaElement[1][1] *= s;
        temp.m_aaElement[2][1] *= s;
        temp.m_aaElement[3][1] *= s;
        temp.m_aaElement[0][2] *= s;
        temp.m_aaElement[1][2] *= s;
        temp.m_aaElement[2][2] *= s;
        temp.m_aaElement[3][2] *= s;
        temp.m_aaElement[0][3] *= s;
        temp.m_aaElement[1][3] *= s;
        temp.m_aaElement[2][3] *= s;
        temp.m_aaElement[3][3] *= s;
        return temp;
    }

    /** Premultiply matrix by vector.
     */
    public static Point4D multiply(Point4D vector, Matrix m) {
        return m.preMultiply(vector);
    }

    public boolean isError() {
        return (m_aaElement == null);
    }

    public float getElement(int x, int y) {
        return m_aaElement[x][y];
    }

    /**Compare this matrix to an object with a given tolerance
     *@param o the object to compare to
     *@param diff the tolerance
     *@return true if o is a Matrix and the individual elements are equal with
     *the given tolerance*/
    public boolean equals(Object o, float diff) {
        boolean isEqual = false;
        if (o instanceof Matrix) {
            Matrix m = (Matrix) o;
            for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++) {
                if (Math.abs(m_aaElement[i][j] - m.m_aaElement[i][j]) > diff) return false;
            }
            return true;
        }
        return false;
    }

    /** Compare to an object
     *@param the object to compare to
     *@return true if the object is a matrix and its values are the same*/
    public boolean equals(Object o) {
        return equals(o, 0f);
    }

    /**Convert the matrix into a row major order one dimensional float array*/
    public float[] toArray() {
        float[] result = new float[16];
        int count = 0;
        for (int i = 0; i < this.m_aaElement.length; i++) for (int j = 0; j < this.m_aaElement[i].length; j++) {
            result[count] = m_aaElement[i][j];
            count++;
        }
        return result;
    }
}
