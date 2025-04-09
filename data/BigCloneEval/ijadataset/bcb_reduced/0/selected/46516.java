package util;

public class Matrix44 {

    public static int count = 0;

    float[][] _m;

    public Matrix44() {
        count++;
        _m = new float[4][4];
    }

    public static Matrix44 createUnit() {
        Matrix44 result = new Matrix44();
        for (int i = 0; i < 4; i++) {
            result._m[i][i] = 1.0f;
        }
        return result;
    }

    public static Matrix44 createTranslation(Point3 t) {
        Matrix44 result = createUnit();
        for (int i = 0; i < 3; i++) {
            result._m[i][3] = t._p[i];
        }
        return result;
    }

    public static Matrix44 createZRotation(float angle) {
        Matrix44 result = createUnit();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        result._m[0][0] = c;
        result._m[0][1] = -s;
        result._m[1][0] = s;
        result._m[1][1] = c;
        return result;
    }

    public static Matrix44 createXRotation(float angle) {
        Matrix44 result = createUnit();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        result._m[1][1] = c;
        result._m[1][2] = -s;
        result._m[2][1] = s;
        result._m[2][2] = c;
        return result;
    }

    public static Matrix44 createYRotation(float angle) {
        Matrix44 result = createUnit();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        result._m[0][0] = c;
        result._m[0][2] = s;
        result._m[2][0] = -s;
        result._m[2][2] = c;
        return result;
    }

    public static Matrix44 createXYShear(float x, float y) {
        Matrix44 result = createUnit();
        result._m[0][2] = x;
        result._m[1][2] = y;
        return result;
    }

    public static Matrix44 createXYZScale(float x, float y, float z) {
        Matrix44 result = createUnit();
        result._m[0][0] = x;
        result._m[1][1] = y;
        result._m[2][2] = z;
        return result;
    }

    /**
     * adds a translation to this Matrix.
     */
    public void translate(Point3 p) {
        _m[0][3] += p._p[0];
        _m[1][3] += p._p[1];
        _m[2][3] += p._p[2];
    }

    public void setCol(int i, Point3 p) {
        _m[0][i] = p._p[0];
        _m[1][i] = p._p[1];
        _m[2][i] = p._p[2];
    }

    public void setRow(int i, Point3 p) {
        _m[i][0] = p._p[0];
        _m[i][1] = p._p[1];
        _m[i][2] = p._p[1];
    }

    public void rotateX(float angle) {
        Matrix44 temp = createXRotation(angle);
        Matrix44 temp2 = mult(this, temp);
        copy(temp2);
    }

    public void rotateY(float angle) {
        Matrix44 temp = createYRotation(angle);
        Matrix44 temp2 = mult(this, temp);
        copy(temp2);
    }

    public void rotateZ(float angle) {
        Matrix44 temp = createZRotation(angle);
        Matrix44 temp2 = mult(this, temp);
        copy(temp2);
    }

    public void copy(Matrix44 c) {
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                _m[i][j] = c._m[i][j];
            }
        }
    }

    public Matrix44(float[] flat) {
        this();
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                _m[j][i] = flat[index++];
            }
        }
    }

    public float[] getFloatArray() {
        float[] result = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[j * 4 + i] = _m[i][j];
            }
        }
        return result;
    }

    public Matrix44 transpose() {
        Matrix44 result = new Matrix44();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result._m[i][j] = _m[j][i];
            }
        }
        return result;
    }

    /**
     * only works if this matrix is affine
     * 
     */
    public Matrix44 affineInverse() {
        float Tx, Ty, Tz;
        Matrix44 result = new Matrix44();
        result._m[0][0] = _m[0][0];
        result._m[0][1] = _m[1][0];
        result._m[0][2] = _m[2][0];
        result._m[1][0] = _m[0][1];
        result._m[1][1] = _m[1][1];
        result._m[1][2] = _m[2][1];
        result._m[2][0] = _m[0][2];
        result._m[2][1] = _m[1][2];
        result._m[2][2] = _m[2][2];
        result._m[3][0] = 0.0f;
        result._m[3][1] = 0.0f;
        result._m[3][2] = 0.0f;
        result._m[3][3] = 1.0f;
        Tx = _m[0][3];
        Ty = _m[1][3];
        Tz = _m[2][3];
        result._m[0][3] = -(_m[0][0] * Tx + _m[1][0] * Ty + _m[2][0] * Tz);
        result._m[1][3] = -(_m[0][1] * Tx + _m[1][1] * Ty + _m[2][1] * Tz);
        result._m[2][3] = -(_m[0][2] * Tx + _m[1][2] * Ty + _m[2][2] * Tz);
        return result;
    }

    public String toString() {
        String result = "m44[\n";
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result += _m[i][j] + " ";
            }
            result += "\n";
        }
        result += "]";
        return result;
    }

    public static Matrix44 mult(Matrix44 a, Matrix44 b) {
        Matrix44 result = new Matrix44();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float temp = 0.0f;
                for (int k = 0; k < 4; k++) {
                    temp += a._m[i][k] * b._m[k][j];
                }
                result._m[i][j] = temp;
            }
        }
        return result;
    }

    public static boolean equals(Matrix44 a, Matrix44 b, float eps) {
        boolean result = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result &= Math.abs(a._m[i][j] - b._m[i][j]) < eps;
            }
        }
        return result;
    }

    public Point4 applyOn(Point4 p) {
        Point4 result = new Point4();
        applyOn(p, result);
        return result;
    }

    /**
     * applies the transformation to the 'in' point, 
     * storing the result in the 'out' point.
     * @param in the point to apply the transformation on.
     * @param out the point where the result is stored.
     */
    public void applyOn(Point4 in, Point4 out) {
        int i, j;
        for (j = 0; j < 4; j++) {
            for (i = 0; i < 4; i++) {
                out._p[i] += _m[i][j] * in._p[j];
            }
        }
    }

    private Point4 _temp = new Point4();

    /**
     * applies the transformation to the point.
     * This creates 2 Point4 objects and a point3 object.
     */
    public Point3 applyOn(Point3 p) {
        Point4 to4 = new Point4(p);
        Point4 result4 = new Point4();
        applyOn(to4, result4);
        Point3 result3 = result4.toPoint3();
        return result3;
    }
}
