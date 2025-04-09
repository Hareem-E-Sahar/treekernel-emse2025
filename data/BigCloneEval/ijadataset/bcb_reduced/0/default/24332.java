import java.lang.Math;

class Matrix3f {

    private float m[][];

    public Matrix3f() {
        m = new float[3][3];
        makeNull();
    }

    public Matrix3f(float mat[][]) {
        m = new float[3][3];
        setMat(mat);
    }

    public Matrix3f(Matrix3f mat) {
        m = new float[3][3];
        setMat(mat);
    }

    public Matrix3f(Quaternion quat) {
        setQuat(quat);
    }

    public Matrix3f(float hpr[]) {
        m = new float[3][3];
        setEulers(hpr);
    }

    public Matrix3f(float heading, float pitch, float roll) {
        m = new float[3][3];
        setEulers(heading, pitch, roll);
    }

    public void print() {
        System.out.println("m = " + m[0][0] + ", " + m[0][1] + ", " + m[0][2] + m[1][0] + ", " + m[1][1] + ", " + m[1][2] + m[2][0] + ", " + m[2][1] + ", " + m[2][2]);
    }

    public void setMatValue(int row, int col, float val) {
        if (row < 0 || row > 3 || col < 0 || col > 3) return;
        m[row][col] = val;
    }

    public float getMatValue(int row, int col) {
        if (row < 0 || row > 3 || col < 0 || col > 3) return 0.0f;
        return m[row][col];
    }

    public void setMat(float mat[][]) {
        m[0][0] = mat[0][0];
        m[0][1] = mat[0][1];
        m[0][2] = mat[0][2];
        m[1][0] = mat[1][0];
        m[1][1] = mat[1][1];
        m[1][2] = mat[1][2];
        m[2][0] = mat[2][0];
        m[2][1] = mat[2][1];
        m[2][2] = mat[2][2];
    }

    public void getMat(float mat[][]) {
        mat[0][0] = m[0][0];
        mat[0][1] = m[0][1];
        mat[0][2] = m[0][2];
        mat[1][0] = m[1][0];
        mat[1][1] = m[1][1];
        mat[1][2] = m[1][2];
        mat[2][0] = m[2][0];
        mat[2][1] = m[2][1];
        mat[2][2] = m[2][2];
    }

    public void setMat(Matrix3f mat) {
        float mat2[][] = new float[3][3];
        mat.getMat(mat2);
        setMat(mat2);
    }

    public void getMat(Matrix3f mat) {
        float mat2[][] = new float[3][3];
        getMat(mat2);
        mat.setMat(mat2);
    }

    public void setQuat(Quaternion quat) {
        quat.getMat3(m);
    }

    public void getQuat(Quaternion quat) {
        quat.setMat3(m);
    }

    public void setEulers(float hpr[]) {
        setEulers(hpr[0], hpr[1], hpr[2]);
    }

    public void getEulers(float hpr[]) {
        float cosh, sinh, cosp, sinp, cosr, sinr;
        sinp = -m[1][2];
        cosp = (float) Math.sqrt(1.0f - sinp * sinp);
        if ((float) Math.abs(cosp) > 0.0001f) {
            cosh = m[2][2] / cosp;
            sinh = m[0][2] / cosp;
            cosr = m[2][1] / cosp;
            sinr = m[1][0] / cosp;
        } else {
            cosh = 1.0f;
            sinh = 0.0f;
            cosr = m[0][0];
            sinr = -m[0][1];
        }
        hpr[0] = (float) Math.atan2(sinh, cosh);
        hpr[1] = (float) Math.atan2(sinp, cosp);
        hpr[2] = (float) Math.atan2(sinr, cosr);
    }

    public void setEulers(float h, float p, float r) {
        float cosh, sinh, cosp, sinp, cosr, sinr;
        cosh = (float) Math.cos(h);
        sinh = (float) Math.sin(h);
        cosp = (float) Math.cos(p);
        sinp = (float) Math.sin(p);
        cosr = (float) Math.cos(r);
        sinr = (float) Math.sin(r);
        m[0][0] = +cosh * cosr + sinh * sinp * sinr;
        m[0][1] = -cosh * sinr + sinh * sinp * cosr;
        m[0][2] = +sinh * cosp;
        m[1][0] = +cosp * sinr;
        m[1][1] = +sinh * sinr + cosh * sinp * cosr;
        m[1][2] = -sinp;
        m[2][0] = -sinh * cosr + cosh * sinp * sinr;
        m[2][1] = +cosp * cosr;
        m[2][2] = +cosh * cosp;
    }

    public void getEulers(float h[], float p[], float r[]) {
        float hpr[] = new float[3];
        getEulers(hpr);
        h[0] = hpr[0];
        p[0] = hpr[1];
        r[0] = hpr[2];
    }

    public void makeNull() {
        m[0][0] = 0f;
        m[0][1] = 0f;
        m[0][2] = 0f;
        m[1][0] = 0f;
        m[1][1] = 0f;
        m[1][2] = 0f;
        m[2][0] = 0f;
        m[2][1] = 0f;
        m[2][2] = 0f;
    }

    public void makeIdent() {
        m[0][0] = 1f;
        m[0][1] = 0f;
        m[0][2] = 0f;
        m[1][0] = 0f;
        m[1][1] = 1f;
        m[1][2] = 0f;
        m[2][0] = 0f;
        m[2][1] = 0f;
        m[2][2] = 1f;
    }

    public void xform(Vec3f vec) {
        float v[] = new float[3];
        vec.get(v);
        vec.set(0, v[0] * m[0][0] + v[1] * m[0][1] + v[2] * m[0][2]);
        vec.set(1, v[0] * m[1][0] + v[1] * m[1][1] + v[2] * m[1][2]);
        vec.set(2, v[0] * m[2][0] + v[1] * m[2][1] + v[2] * m[2][2]);
    }

    public void xform(float v[]) {
        float tmp_v[] = new float[3];
        tmp_v[0] = v[0] * m[0][0] + v[1] * m[0][1] + v[2] * m[0][2];
        tmp_v[1] = v[0] * m[1][0] + v[1] * m[1][1] + v[2] * m[1][2];
        tmp_v[2] = v[0] * m[2][0] + v[1] * m[2][1] + v[2] * m[2][2];
        v[0] = tmp_v[0];
        v[1] = tmp_v[1];
        v[2] = tmp_v[2];
    }
}
