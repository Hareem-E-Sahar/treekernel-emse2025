import java.lang.Math;

class Matrix4f {

    private float m[][];

    public Matrix4f() {
        m = new float[4][4];
        makeNull();
    }

    public Matrix4f(float mat[][]) {
        m = new float[4][4];
        setMat(mat);
    }

    public Matrix4f(Matrix4f mat) {
        m = new float[4][4];
        setMat(mat);
    }

    public Matrix4f(Quaternion quat) {
        m = new float[4][4];
        setQuat(quat);
    }

    public Matrix4f(float hpr[]) {
        m = new float[4][4];
        setEulers(hpr);
    }

    public Matrix4f(float heading, float pitch, float roll) {
        m = new float[4][4];
        setEulers(heading, pitch, roll);
    }

    public void print() {
        System.out.println("m = " + m[0][0] + ", " + m[0][1] + ", " + m[0][2] + ", " + m[0][3] + m[1][0] + ", " + m[1][1] + ", " + m[1][2] + ", " + m[1][3] + m[2][0] + ", " + m[2][1] + ", " + m[2][2] + ", " + m[2][3] + m[3][0] + ", " + m[3][1] + ", " + m[3][2] + ", " + m[3][3]);
    }

    public void setMatValue(int row, int col, float val) {
        if (row < 0 || row > 4 || col < 0 || col > 4) return;
        m[row][col] = val;
    }

    public float getMatValue(int row, int col) {
        if (row < 0 || row > 4 || col < 0 || col > 4) return 0.0f;
        return m[row][col];
    }

    public void setMat(float mat[][]) {
        m[0][0] = mat[0][0];
        m[0][1] = mat[0][1];
        m[0][2] = mat[0][2];
        m[0][3] = mat[0][3];
        m[1][0] = mat[1][0];
        m[1][1] = mat[1][1];
        m[1][2] = mat[1][2];
        m[1][3] = mat[1][3];
        m[2][0] = mat[2][0];
        m[2][1] = mat[2][1];
        m[2][2] = mat[2][2];
        m[2][3] = mat[2][3];
        m[3][0] = mat[3][0];
        m[3][1] = mat[3][1];
        m[3][2] = mat[3][2];
        m[3][3] = mat[3][3];
    }

    public void getMat(float mat[][]) {
        mat[0][0] = m[0][0];
        mat[0][1] = m[0][1];
        mat[0][2] = m[0][2];
        mat[0][3] = m[0][3];
        mat[1][0] = m[1][0];
        mat[1][1] = m[1][1];
        mat[1][2] = m[1][2];
        mat[1][3] = m[1][3];
        mat[2][0] = m[2][0];
        mat[2][1] = m[2][1];
        mat[2][2] = m[2][2];
        mat[2][3] = m[2][3];
        mat[3][0] = m[3][0];
        mat[3][1] = m[3][1];
        mat[3][2] = m[3][2];
        mat[3][3] = m[3][3];
    }

    public void setMat(Matrix4f mat) {
        float mat2[][] = new float[4][4];
        mat.getMat(mat2);
        setMat(mat2);
    }

    public void getMat(Matrix4f mat) {
        float mat2[][] = new float[4][4];
        getMat(mat2);
        mat.setMat(mat2);
    }

    public void setQuat(Quaternion quat) {
        quat.getMat4(m);
    }

    public void getQuat(Quaternion quat) {
        quat.setMat4(m);
    }

    public void setEulers(float hpr[]) {
        setEulers(hpr[0], hpr[1], hpr[2]);
    }

    public void getEulers(float hpr[]) {
    }

    public void setEulers(float h, float p, float r) {
        float cosh, sinh, cosp, sinp, cosr, sinr;
        cosh = (float) Math.cos(h);
        sinh = (float) Math.sin(h);
        cosp = (float) Math.cos(p);
        sinp = (float) Math.sin(p);
        cosr = (float) Math.cos(r);
        sinr = (float) Math.sin(r);
        m[0][0] = cosh * cosp;
        m[0][1] = cosh * sinp * sinr - sinh * cosr;
        m[0][2] = cosh * sinp * cosr + sinh * sinr;
        m[0][3] = 0.0f;
        m[1][0] = sinh * cosp;
        m[1][1] = cosh * cosr + sinh * sinp * sinr;
        m[1][2] = sinh * sinp * cosr - cosh * sinr;
        m[1][3] = 0.0f;
        m[2][0] = -sinp;
        m[2][1] = cosp * sinr;
        m[2][2] = cosp * cosr;
        m[2][3] = 0.0f;
        m[3][0] = 0.0f;
        m[3][1] = 0.0f;
        m[3][2] = 0.0f;
        m[3][3] = 1.0f;
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
        m[0][3] = 0f;
        m[1][0] = 0f;
        m[1][1] = 0f;
        m[1][2] = 0f;
        m[1][3] = 0f;
        m[2][0] = 0f;
        m[2][1] = 0f;
        m[2][2] = 0f;
        m[2][3] = 0f;
        m[3][0] = 0f;
        m[3][1] = 0f;
        m[3][2] = 0f;
        m[3][3] = 0f;
    }

    public void makeIdent() {
        m[0][0] = 1f;
        m[0][1] = 0f;
        m[0][2] = 0f;
        m[0][3] = 0f;
        m[1][0] = 0f;
        m[1][1] = 1f;
        m[1][2] = 0f;
        m[1][3] = 0f;
        m[2][0] = 0f;
        m[2][1] = 0f;
        m[2][2] = 1f;
        m[2][3] = 0f;
        m[3][0] = 0f;
        m[3][1] = 0f;
        m[3][2] = 0f;
        m[3][3] = 1f;
    }

    public void xform(Vec4f vec) {
        float v[] = new float[4];
        vec.get(v);
        vec.set(0, v[0] * m[0][0] + v[1] * m[0][1] + v[2] * m[0][2] + v[3] * m[0][3]);
        vec.set(1, v[0] * m[1][0] + v[1] * m[1][1] + v[2] * m[1][2] + v[3] * m[1][3]);
        vec.set(2, v[0] * m[2][0] + v[1] * m[2][1] + v[2] * m[2][2] + v[3] * m[2][3]);
        vec.set(3, v[0] * m[3][0] + v[1] * m[3][1] + v[2] * m[3][2] + v[3] * m[3][3]);
    }

    public void xform(float v[]) {
        float tmp_v[] = new float[4];
        tmp_v[0] = v[0] * m[0][0] + v[1] * m[0][1] + v[2] * m[0][2] + v[3] * m[0][3];
        tmp_v[1] = v[0] * m[1][0] + v[1] * m[1][1] + v[2] * m[1][2] + v[3] * m[1][3];
        tmp_v[2] = v[0] * m[2][0] + v[1] * m[2][1] + v[2] * m[2][2] + v[3] * m[2][3];
        tmp_v[2] = v[0] * m[3][0] + v[1] * m[3][1] + v[2] * m[3][2] + v[3] * m[3][3];
        v[0] = tmp_v[0];
        v[1] = tmp_v[1];
        v[2] = tmp_v[2];
        v[3] = tmp_v[3];
    }
}
