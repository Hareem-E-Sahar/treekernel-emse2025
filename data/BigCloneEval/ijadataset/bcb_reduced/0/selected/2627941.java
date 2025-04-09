package AGFX.F3D.Math;

import java.nio.FloatBuffer;
import javax.vecmath.Vector3f;
import AGFX.F3D.F3D;

/**
 * @author AndyGFX
 * 
 */
public class TF3D_Matrix {

    public float[][] grid = new float[4][4];

    public TF3D_Matrix() {
    }

    public void LoadIdentity() {
        this.grid[0][0] = 1.0f;
        this.grid[1][0] = 0.0f;
        this.grid[2][0] = 0.0f;
        this.grid[3][0] = 0.0f;
        this.grid[0][1] = 0.0f;
        this.grid[1][1] = 1.0f;
        this.grid[2][1] = 0.0f;
        this.grid[3][1] = 0.0f;
        this.grid[0][2] = 0.0f;
        this.grid[1][2] = 0.0f;
        this.grid[2][2] = 1.0f;
        this.grid[3][2] = 0.0f;
        this.grid[0][3] = 0.0f;
        this.grid[1][3] = 0.0f;
        this.grid[2][3] = 0.0f;
        this.grid[3][3] = 1.0f;
    }

    public TF3D_Matrix Copy() {
        TF3D_Matrix mat = new TF3D_Matrix();
        mat.grid[0][0] = grid[0][0];
        mat.grid[1][0] = grid[1][0];
        mat.grid[2][0] = grid[2][0];
        mat.grid[3][0] = grid[3][0];
        mat.grid[0][1] = grid[0][1];
        mat.grid[1][1] = grid[1][1];
        mat.grid[2][1] = grid[2][1];
        mat.grid[3][1] = grid[3][1];
        mat.grid[0][2] = grid[0][2];
        mat.grid[1][2] = grid[1][2];
        mat.grid[2][2] = grid[2][2];
        mat.grid[3][2] = grid[3][2];
        mat.grid[0][3] = grid[0][3];
        mat.grid[1][3] = grid[1][3];
        mat.grid[2][3] = grid[2][3];
        mat.grid[3][3] = grid[3][3];
        return mat;
    }

    public void Overwrite(TF3D_Matrix mat) {
        this.grid[0][0] = mat.grid[0][0];
        this.grid[1][0] = mat.grid[1][0];
        this.grid[2][0] = mat.grid[2][0];
        this.grid[3][0] = mat.grid[3][0];
        this.grid[0][1] = mat.grid[0][1];
        this.grid[1][1] = mat.grid[1][1];
        this.grid[2][1] = mat.grid[2][1];
        this.grid[3][1] = mat.grid[3][1];
        this.grid[0][2] = mat.grid[0][2];
        this.grid[1][2] = mat.grid[1][2];
        this.grid[2][2] = mat.grid[2][2];
        this.grid[3][2] = mat.grid[3][2];
        this.grid[0][3] = mat.grid[0][3];
        this.grid[1][3] = mat.grid[1][3];
        this.grid[2][3] = mat.grid[2][3];
        this.grid[3][3] = mat.grid[3][3];
    }

    public TF3D_Matrix Inverse() {
        TF3D_Matrix mat = new TF3D_Matrix();
        float tx = 0f;
        float ty = 0f;
        float tz = 0f;
        mat.grid[0][0] = grid[0][0];
        mat.grid[1][0] = grid[0][1];
        mat.grid[2][0] = grid[0][2];
        mat.grid[0][1] = grid[1][0];
        mat.grid[1][1] = grid[1][1];
        mat.grid[2][1] = grid[1][2];
        mat.grid[0][2] = grid[2][0];
        mat.grid[1][2] = grid[2][1];
        mat.grid[2][2] = grid[2][2];
        mat.grid[0][3] = 0f;
        mat.grid[1][3] = 0f;
        mat.grid[2][3] = 0f;
        mat.grid[3][3] = 1f;
        tx = grid[3][0];
        ty = grid[3][1];
        tz = grid[3][2];
        mat.grid[3][0] = -((grid[0][0] * tx) + (grid[0][1] * ty) + (grid[0][2] * tz));
        mat.grid[3][1] = -((grid[1][0] * tx) + (grid[1][1] * ty) + (grid[1][2] * tz));
        mat.grid[3][2] = -((grid[2][0] * tx) + (grid[2][1] * ty) + (grid[2][2] * tz));
        return mat;
    }

    public void Multiply(TF3D_Matrix mat) {
        float m00 = grid[0][0] * mat.grid[0][0] + grid[1][0] * mat.grid[0][1] + grid[2][0] * mat.grid[0][2] + grid[3][0] * mat.grid[0][3];
        float m01 = grid[0][1] * mat.grid[0][0] + grid[1][1] * mat.grid[0][1] + grid[2][1] * mat.grid[0][2] + grid[3][1] * mat.grid[0][3];
        float m02 = grid[0][2] * mat.grid[0][0] + grid[1][2] * mat.grid[0][1] + grid[2][2] * mat.grid[0][2] + grid[3][2] * mat.grid[0][3];
        float m10 = grid[0][0] * mat.grid[1][0] + grid[1][0] * mat.grid[1][1] + grid[2][0] * mat.grid[1][2] + grid[3][0] * mat.grid[1][3];
        float m11 = grid[0][1] * mat.grid[1][0] + grid[1][1] * mat.grid[1][1] + grid[2][1] * mat.grid[1][2] + grid[3][1] * mat.grid[1][3];
        float m12 = grid[0][2] * mat.grid[1][0] + grid[1][2] * mat.grid[1][1] + grid[2][2] * mat.grid[1][2] + grid[3][2] * mat.grid[1][3];
        float m20 = grid[0][0] * mat.grid[2][0] + grid[1][0] * mat.grid[2][1] + grid[2][0] * mat.grid[2][2] + grid[3][0] * mat.grid[2][3];
        float m21 = grid[0][1] * mat.grid[2][0] + grid[1][1] * mat.grid[2][1] + grid[2][1] * mat.grid[2][2] + grid[3][1] * mat.grid[2][3];
        float m22 = grid[0][2] * mat.grid[2][0] + grid[1][2] * mat.grid[2][1] + grid[2][2] * mat.grid[2][2] + grid[3][2] * mat.grid[2][3];
        float m30 = grid[0][0] * mat.grid[3][0] + grid[1][0] * mat.grid[3][1] + grid[2][0] * mat.grid[3][2] + grid[3][0] * mat.grid[3][3];
        float m31 = grid[0][1] * mat.grid[3][0] + grid[1][1] * mat.grid[3][1] + grid[2][1] * mat.grid[3][2] + grid[3][1] * mat.grid[3][3];
        float m32 = grid[0][2] * mat.grid[3][0] + grid[1][2] * mat.grid[3][1] + grid[2][2] * mat.grid[3][2] + grid[3][2] * mat.grid[3][3];
        grid[0][0] = m00;
        grid[0][1] = m01;
        grid[0][2] = m02;
        grid[1][0] = m10;
        grid[1][1] = m11;
        grid[1][2] = m12;
        grid[2][0] = m20;
        grid[2][1] = m21;
        grid[2][2] = m22;
        grid[3][0] = m30;
        grid[3][1] = m31;
        grid[3][2] = m32;
    }

    public void Translate(Vector3f v) {
        this.Translate(v.x, v.y, v.z);
    }

    public void Translate(float x, float y, float z) {
        grid[3][0] = grid[0][0] * x + grid[1][0] * y + grid[2][0] * z + grid[3][0];
        grid[3][1] = grid[0][1] * x + grid[1][1] * y + grid[2][1] * z + grid[3][1];
        grid[3][2] = grid[0][2] * x + grid[1][2] * y + grid[2][2] * z + grid[3][2];
    }

    public Vector3f TransformVector(Vector3f V) {
        Vector3f TV = new Vector3f();
        int X = 0;
        int Y = 1;
        int Z = 2;
        int W = 3;
        TV.x = V.x * this.grid[X][X] + V.y * this.grid[Y][X] + V.z * this.grid[Z][X] + this.grid[W][X];
        TV.y = V.x * this.grid[X][Y] + V.y * this.grid[Y][Y] + V.z * this.grid[Z][Y] + this.grid[W][Y];
        TV.z = V.x * this.grid[X][Z] + V.y * this.grid[Y][Z] + V.z * this.grid[Z][Z] + this.grid[W][Z];
        return TV;
    }

    public Vector3f RotateVector(Vector3f v) {
        return this.RotateVector(v.x, v.y, v.z);
    }

    public Vector3f RotateVector(float x, float y, float z) {
        Vector3f res = new Vector3f();
        res.x = grid[0][0] * x + grid[1][0] * y + grid[2][0] * z + grid[3][0];
        res.y = grid[0][1] * x + grid[1][1] * y + grid[2][1] * z + grid[3][1];
        res.z = grid[0][2] * x + grid[1][2] * y + grid[2][2] * z + grid[3][2];
        return res;
    }

    public void Scale(Vector3f v) {
        this.Scale(v.x, v.y, v.z);
    }

    public void Scale(float x, float y, float z) {
        grid[0][0] = grid[0][0] * x;
        grid[0][1] = grid[0][1] * x;
        grid[0][2] = grid[0][2] * x;
        grid[1][0] = grid[1][0] * y;
        grid[1][1] = grid[1][1] * y;
        grid[1][2] = grid[1][2] * y;
        grid[2][0] = grid[2][0] * z;
        grid[2][1] = grid[2][1] * z;
        grid[2][2] = grid[2][2] * z;
    }

    public void Rotate(Vector3f v) {
        this.Rotate(v.x, v.y, v.z);
    }

    public void Rotate(float rx, float ry, float rz) {
        float cos_ang;
        float sin_ang;
        cos_ang = (float) Math.cos(rx);
        sin_ang = (float) Math.sin(rx);
        float m10 = grid[1][0] * cos_ang + grid[2][0] * sin_ang;
        float m11 = grid[1][1] * cos_ang + grid[2][1] * sin_ang;
        float m12 = grid[1][2] * cos_ang + grid[2][2] * sin_ang;
        grid[2][0] = grid[1][0] * -sin_ang + grid[2][0] * cos_ang;
        grid[2][1] = grid[1][1] * -sin_ang + grid[2][1] * cos_ang;
        grid[2][2] = grid[1][2] * -sin_ang + grid[2][2] * cos_ang;
        grid[1][0] = m10;
        grid[1][1] = m11;
        grid[1][2] = m12;
        cos_ang = (float) Math.cos(ry);
        sin_ang = (float) Math.sin(ry);
        float m00 = grid[0][0] * cos_ang + grid[2][0] * -sin_ang;
        float m01 = grid[0][1] * cos_ang + grid[2][1] * -sin_ang;
        float m02 = grid[0][2] * cos_ang + grid[2][2] * -sin_ang;
        grid[2][0] = grid[0][0] * sin_ang + grid[2][0] * cos_ang;
        grid[2][1] = grid[0][1] * sin_ang + grid[2][1] * cos_ang;
        grid[2][2] = grid[0][2] * sin_ang + grid[2][2] * cos_ang;
        grid[0][0] = m00;
        grid[0][1] = m01;
        grid[0][2] = m02;
        cos_ang = (float) Math.cos(rz);
        sin_ang = (float) Math.sin(rz);
        m00 = grid[0][0] * cos_ang + grid[1][0] * sin_ang;
        m01 = grid[0][1] * cos_ang + grid[1][1] * sin_ang;
        m02 = grid[0][2] * cos_ang + grid[1][2] * sin_ang;
        grid[1][0] = grid[0][0] * -sin_ang + grid[1][0] * cos_ang;
        grid[1][1] = grid[0][1] * -sin_ang + grid[1][1] * cos_ang;
        grid[1][2] = grid[0][2] * -sin_ang + grid[1][2] * cos_ang;
        grid[0][0] = m00;
        grid[0][1] = m01;
        grid[0][2] = m02;
    }

    public void RotateX(float ang) {
        this.RotatePitch(ang);
    }

    public void RotatePitch(float ang) {
        float cos_ang = (float) Math.cos(ang);
        float sin_ang = (float) Math.sin(ang);
        float m10 = grid[1][0] * cos_ang + grid[2][0] * sin_ang;
        float m11 = grid[1][1] * cos_ang + grid[2][1] * sin_ang;
        float m12 = grid[1][2] * cos_ang + grid[2][2] * sin_ang;
        grid[2][0] = grid[1][0] * -sin_ang + grid[2][0] * cos_ang;
        grid[2][1] = grid[1][1] * -sin_ang + grid[2][1] * cos_ang;
        grid[2][2] = grid[1][2] * -sin_ang + grid[2][2] * cos_ang;
        grid[1][0] = m10;
        grid[1][1] = m11;
        grid[1][2] = m12;
    }

    public void RotateY(float ang) {
        this.RotateYaw(ang);
    }

    public void RotateYaw(float ang) {
        float cos_ang = (float) Math.cos(ang);
        float sin_ang = (float) Math.sin(ang);
        float m00 = grid[0][0] * cos_ang + grid[2][0] * -sin_ang;
        float m01 = grid[0][1] * cos_ang + grid[2][1] * -sin_ang;
        float m02 = grid[0][2] * cos_ang + grid[2][2] * -sin_ang;
        grid[2][0] = grid[0][0] * sin_ang + grid[2][0] * cos_ang;
        grid[2][1] = grid[0][1] * sin_ang + grid[2][1] * cos_ang;
        grid[2][2] = grid[0][2] * sin_ang + grid[2][2] * cos_ang;
        grid[0][0] = m00;
        grid[0][1] = m01;
        grid[0][2] = m02;
    }

    public void RotateZ(float ang) {
        this.RotateRoll(ang);
    }

    public void RotateRoll(float ang) {
        float cos_ang = (float) Math.cos(ang);
        float sin_ang = (float) Math.sin(ang);
        float m00 = grid[0][0] * cos_ang + grid[1][0] * sin_ang;
        float m01 = grid[0][1] * cos_ang + grid[1][1] * sin_ang;
        float m02 = grid[0][2] * cos_ang + grid[1][2] * sin_ang;
        grid[1][0] = grid[0][0] * -sin_ang + grid[1][0] * cos_ang;
        grid[1][1] = grid[0][1] * -sin_ang + grid[1][1] * cos_ang;
        grid[1][2] = grid[0][2] * -sin_ang + grid[1][2] * cos_ang;
        grid[0][0] = m00;
        grid[0][1] = m01;
        grid[0][2] = m02;
    }

    public String toString() {
        String Complete = "\n";
        String Row = "";
        for (int Y = 0; Y < 4; Y++) {
            for (int X = 0; X < 4; X++) {
                Row = Row + String.valueOf(grid[X][Y]) + " ";
            }
            Complete = Complete + Row + "\n";
            Row = "";
        }
        return Complete;
    }

    public float[] toFloat() {
        float[] res = new float[16];
        int i = 0;
        for (int Y = 0; Y < 4; Y++) {
            for (int X = 0; X < 4; X++) {
                res[i] = grid[X][Y];
                i++;
            }
        }
        return res;
    }

    public FloatBuffer toFloatBuffer() {
        float[] res = new float[16];
        int i = 0;
        for (int Y = 0; Y < 4; Y++) {
            for (int X = 0; X < 4; X++) {
                res[i] = grid[X][Y];
                i++;
            }
        }
        return F3D.GetBuffer.Float(res);
    }

    public void CreateRotationMatrix(Vector3f angle) {
        this.CreateRotationMatrix(angle.x, angle.y, angle.z);
    }

    public void CreateRotationMatrix(float _x, float _y, float _z) {
        float AX = _x * F3D.DEGTORAD;
        float AY = _y * F3D.DEGTORAD;
        float AZ = _z * F3D.DEGTORAD;
        float sx = (float) Math.sin(AX);
        float sy = (float) Math.sin(AY);
        float sz = (float) Math.sin(AZ);
        float cx = (float) Math.cos(AX);
        float cy = (float) Math.cos(AY);
        float cz = (float) Math.cos(AZ);
        TF3D_Matrix mx = new TF3D_Matrix();
        mx.CreateRotationMatrixX(sx, cx);
        TF3D_Matrix my = new TF3D_Matrix();
        my.CreateRotationMatrixY(sy, cy);
        TF3D_Matrix mz = new TF3D_Matrix();
        mz.CreateRotationMatrixZ(sz, cz);
        TF3D_Matrix m = new TF3D_Matrix();
        m.LoadIdentity();
        m.Multiply(mx);
        m.Multiply(my);
        m.Multiply(mz);
        this.grid = m.grid;
    }

    private void CreateRotationMatrixX(float Sine, float Cosine) {
        int X = 0;
        int Y = 1;
        int Z = 2;
        int W = 3;
        this.grid[X][X] = 1;
        this.grid[X][Y] = 0;
        this.grid[X][Z] = 0;
        this.grid[X][W] = 0;
        this.grid[Y][X] = 0;
        this.grid[Y][Y] = Cosine;
        this.grid[Y][Z] = -Sine;
        this.grid[Y][W] = 0;
        this.grid[Z][X] = 0;
        this.grid[Z][Y] = Sine;
        this.grid[Z][Z] = Cosine;
        this.grid[Z][W] = 0;
        this.grid[W][X] = 0;
        this.grid[W][Y] = 0;
        this.grid[W][Z] = 0;
        this.grid[W][W] = 1;
    }

    private void CreateRotationMatrixY(float Sine, float Cosine) {
        int X = 0;
        int Y = 1;
        int Z = 2;
        int W = 3;
        this.grid[X][X] = Cosine;
        this.grid[X][Y] = 0;
        this.grid[X][Z] = Sine;
        this.grid[X][W] = 0;
        this.grid[Y][X] = 0;
        this.grid[Y][Y] = 1;
        this.grid[Y][Z] = 0;
        this.grid[Y][W] = 0;
        this.grid[Z][X] = -Sine;
        this.grid[Z][Y] = 0;
        this.grid[Z][Z] = Cosine;
        this.grid[Z][W] = 0;
        this.grid[W][X] = 0;
        this.grid[W][Y] = 0;
        this.grid[W][Z] = 0;
        this.grid[W][W] = 1;
    }

    private void CreateRotationMatrixZ(float Sine, float Cosine) {
        int X = 0;
        int Y = 1;
        int Z = 2;
        int W = 3;
        this.grid[X][X] = Cosine;
        this.grid[X][Y] = -Sine;
        this.grid[X][Z] = 0;
        this.grid[X][W] = 0;
        this.grid[Y][X] = Sine;
        this.grid[Y][Y] = Cosine;
        this.grid[Y][Z] = 0;
        this.grid[Y][W] = 0;
        this.grid[Z][X] = 0;
        this.grid[Z][Y] = 0;
        this.grid[Z][Z] = 1;
        this.grid[Z][W] = 0;
        this.grid[W][X] = 0;
        this.grid[W][Y] = 0;
        this.grid[W][Z] = 0;
        this.grid[W][W] = 1;
    }

    public void CreateTranslationMatrix(Vector3f V) {
        this.CreateTranslationMatrix(V.x, V.y, V.z);
    }

    public void CreateTranslationMatrix(float _x, float _y, float _z) {
        this.LoadIdentity();
        int X = 0;
        int Y = 1;
        int Z = 2;
        int W = 3;
        this.grid[W][X] = _x;
        this.grid[W][Y] = _y;
        this.grid[W][Z] = _z;
    }
}
