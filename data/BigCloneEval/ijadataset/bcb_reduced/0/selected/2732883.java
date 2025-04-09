package de.cit_ec.helmerttransformation;

import de.cit_ec.helmerttransformation.tools.ConfReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 *
 * @author flier
 */
public final class Transformer {

    private float[][] originsystem;

    private float[][] targetsystem;

    private float[][] transformvals = new float[3][3];

    private float[][] rotationmatrix = new float[3][3];

    private float[][][] eigenvalues = new float[2][1][1];

    private float[] centerofgravity_origin = new float[3];

    private float[] centerofgravity_target = new float[3];

    private float scalefactor = 0.0f;

    private float[][] transformationsmatrix = new float[4][4];

    private Matrix4f transformationsmatrix4f = new Matrix4f();

    private Matrix3f rotationmatrix3f = new Matrix3f();

    private Vector4f new_point = new Vector4f();

    private float m1 = 0.0f;

    private float m2 = 0.0f;

    private float mg = 0.0f;

    private float[][] redstartg = new float[3][3];

    private float[][] redtargetg = new float[3][3];

    private ConfReader confr;

    /**
     * Transformer()
     * @param args the command line arguments == none 
     */
    public Transformer(String filename) {
        originsystem = new float[3][3];
        targetsystem = new float[3][3];
        try {
            confr = new ConfReader(filename);
        } catch (ParsingException ex) {
            Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Transformer.class.getName()).log(Level.SEVERE, null, ex);
        }
        originsystem[0] = confr.origincoord0;
        originsystem[1] = confr.origincoord1;
        originsystem[2] = confr.origincoord2;
        targetsystem[0] = confr.targetcoord0;
        targetsystem[1] = confr.targetcoord1;
        targetsystem[2] = confr.targetcoord2;
        doHelmertTransformation(originsystem, targetsystem);
        RunTests();
    }

    /**
     *   createNullMatrix()
     *	 @param <int> r == rows
     *	 @param <int> c == columns
     * 	 @return <Array> Matrix mat
     *	 @description Contructs a matrix filled with zeros (float)
     *
     **/
    public float[][] createNullMatrix(int r, int c) {
        float[][] mat = new float[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                mat[i][j] = 0.0f;
            }
        }
        return mat;
    }

    /**
     *  transposeMatrix()
     *	@param <Array> A
     * 	@return <Array> AT
     *	@description Transposes the Matrix A and returns AT
     *				 
     *
     **/
    public float[][] transposeMatrix(float[][] mat) {
        float[][] transmat = createNullMatrix(mat[0].length, mat.length);
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                transmat[j][i] = mat[i][j];
            }
        }
        return transmat;
    }

    /**
     *   multiplyMatrix()
     *	@param <Array> mat1
     *	@param <Array> mat2
     * 	@return <Array> sum = mat1*mat2
     *	@description Multiplies mat1(o,p), mat2(l,k) and return the sum(p,l)
     *
     **/
    public float[][] multiplyMatrix(float[][] mat1, float[][] mat2) {
        if (mat1[0].length == mat2.length) {
            float[][] sum = createNullMatrix(mat1.length, mat2[0].length);
            for (int i = 0; i < mat1.length; i++) {
                for (int j = 0; j < mat2[0].length; j++) {
                    sum[i][j] = 0.0f;
                    for (int k = 0; k < mat1[0].length; k++) {
                        sum[i][j] += mat1[i][k] * mat2[k][j];
                    }
                }
            }
            return sum;
        }
        return null;
    }

    public float[][][] deriveEigenwert(float[][] mat) {
        if (mat[0].length != mat.length) {
            return null;
        }
        int n = mat.length;
        float[][] eigen = mat;
        float[][] eigenvektoren = new float[n][n];
        for (int k = 0; k < n; k++) {
            for (int l = 0; l < n; l++) {
                eigenvektoren[k][l] = 0.0f;
            }
            eigenvektoren[k][k] = 1.0f;
        }
        float phi = Float.MAX_VALUE;
        int iter = 20;
        while (Math.abs(phi) > 1.0E-8 && iter-- > 0) {
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    float[][] tmp = new float[n][n];
                    for (int k = 0; k < n; k++) {
                        for (int l = 0; l < n; l++) {
                            tmp[k][l] = eigenvektoren[k][l];
                        }
                    }
                    phi = (float) (0.5f * Math.atan2(2.0 * eigen[i][j], eigen[i][i] - eigen[j][j]));
                    for (int k = 0; k < n; k++) {
                        for (int l = 0; l < n; l++) {
                            eigenvektoren[k][l] = 0.0f;
                        }
                        eigenvektoren[k][k] = 1.0f;
                    }
                    eigenvektoren[i][i] = Round((float) Math.cos(phi), 2);
                    eigenvektoren[i][j] = Round((float) -Math.sin(phi), 2);
                    eigenvektoren[j][i] = Round((float) Math.sin(phi), 2);
                    eigenvektoren[j][j] = Round((float) Math.cos(phi), 2);
                    float[][] eigentranspose = transposeMatrix(eigenvektoren);
                    eigen = multiplyMatrix(eigentranspose, (multiplyMatrix(eigen, eigenvektoren)));
                    eigenvektoren = multiplyMatrix(tmp, eigenvektoren);
                }
            }
        }
        if (iter <= 0) {
            return null;
        }
        for (int i = 0; i < eigen.length; i++) {
            for (int j = 0; j < eigen[i].length; j++) {
                if (i != j) {
                    eigen[i][j] = 0.0f;
                }
            }
        }
        float[][][] result = new float[2][1][1];
        result[0] = eigenvektoren;
        result[1] = eigen;
        this.eigenvalues = result;
        return result;
    }

    public static float[] deriveCenterOfGravity(float[][] system) {
        float x, y, z;
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
        for (int i = 0; i < system.length; i++) {
            x += system[i][0];
            y += system[i][1];
            z += system[i][2];
        }
        float[] result = new float[3];
        result[0] = x / system.length;
        result[1] = y / system.length;
        result[2] = z / system.length;
        return result;
    }

    public boolean doHelmertTransformation(float[][] originsystem, float[][] targetsystem) {
        System.out.println("Deriving Transformation...");
        final long startZeit_1CPU = System.currentTimeMillis();
        if (originsystem.length > 0 && originsystem.length == targetsystem.length) {
            float Sxx = 0.0f;
            float Sxy = 0.0f;
            float Sxz = 0.0f;
            float Syx = 0.0f;
            float Syy = 0.0f;
            float Syz = 0.0f;
            float Szx = 0.0f;
            float Szy = 0.0f;
            float Szz = 0.0f;
            float[] cs = deriveCenterOfGravity(originsystem);
            float[] ct = deriveCenterOfGravity(targetsystem);
            this.centerofgravity_origin = cs;
            this.centerofgravity_target = ct;
            float[][] redStart = new float[originsystem.length][originsystem.length];
            float[][] redTarget = new float[originsystem.length][originsystem.length];
            float N[][] = new float[4][4];
            float[][] t = new float[3][3];
            for (int i = 0; i < originsystem.length; i++) {
                float[] bufforigin = { originsystem[i][0] - cs[0], originsystem[i][1] - cs[1], originsystem[i][2] - cs[2] };
                float[] bufftarget = { targetsystem[i][0] - ct[0], targetsystem[i][1] - ct[1], targetsystem[i][2] - ct[2] };
                redStart[i] = bufforigin;
                redTarget[i] = bufftarget;
                Sxx += ((originsystem[i][0] - cs[0]) * (targetsystem[i][0] - ct[0]));
                Sxy += ((originsystem[i][0] - cs[0]) * (targetsystem[i][1] - ct[1]));
                Sxz += ((originsystem[i][0] - cs[0]) * (targetsystem[i][2] - ct[2]));
                Syx += ((originsystem[i][1] - cs[1]) * (targetsystem[i][0] - ct[0]));
                Syy += ((originsystem[i][1] - cs[1]) * (targetsystem[i][1] - ct[1]));
                Syz += ((originsystem[i][1] - cs[1]) * (targetsystem[i][2] - ct[2]));
                Szx += ((originsystem[i][2] - cs[2]) * (targetsystem[i][0] - ct[0]));
                Szy += ((originsystem[i][2] - cs[2]) * (targetsystem[i][1] - ct[1]));
                Szz += ((originsystem[i][2] - cs[2]) * (targetsystem[i][2] - ct[2]));
            }
            this.redstartg = redStart;
            this.redtargetg = redTarget;
            float sx0, sx1, sx2, sx3, sy0, sy1, sy2, sy3, sz0, sz1, sz2, sz3, sw0, sw1, sw2, sw3;
            sx0 = Sxx + Syy + Szz;
            sx1 = Syz - Szy;
            sx2 = Szx - Sxz;
            sx3 = Sxy - Syx;
            sy0 = Syz - Szy;
            sy1 = Sxx - Syy - Szz;
            sy2 = Sxy + Syx;
            sy3 = Szx + Sxz;
            sz0 = Szx - Sxz;
            sz1 = Sxy + Syx;
            sz2 = -Sxx + Syy - Szz;
            sz3 = Syz + Szy;
            sw0 = Sxy - Syx;
            sw1 = Szx + Sxz;
            sw2 = Syz + Szy;
            sw3 = -Sxx - Syy + Szz;
            float[] buffn0 = { sx0, sx1, sx2, sx3 };
            float[] buffn1 = { sy0, sy1, sy2, sy3 };
            float[] buffn2 = { sz0, sz1, sz2, sz3 };
            float[] buffn3 = { sw0, sw1, sw2, sw3 };
            N[0] = buffn0;
            N[1] = buffn1;
            N[2] = buffn2;
            N[3] = buffn3;
            float[][][] VD = deriveEigenwert(N);
            if (VD == null) {
                System.out.println("VD == Null");
                return false;
            }
            int indexOfMaxEig = 0;
            float maxEig = (VD[1][indexOfMaxEig][indexOfMaxEig]);
            for (int i = 1; i < VD[1].length; i++) {
                if (maxEig < VD[1][i][i]) {
                    indexOfMaxEig = i;
                    maxEig = VD[1][i][i];
                }
            }
            float[] q = { VD[0][0][indexOfMaxEig], VD[0][1][indexOfMaxEig], VD[0][2][indexOfMaxEig], VD[0][3][indexOfMaxEig] };
            float[][] R = new float[3][3];
            float[] buffR0 = { 2.0f * q[0] * q[0] - 1.0f + 2.0f * q[1] * q[1], 2.0f * (q[1] * q[2] - q[0] * q[3]), 2.0f * (q[1] * q[3] + q[0] * q[2]) };
            R[0] = buffR0;
            float[] buffR1 = { 2.0f * (q[1] * q[2] + q[0] * q[3]), 2.0f * q[0] * q[0] - 1.0f + 2.0f * q[2] * q[2], 2.0f * (q[2] * q[3] - q[0] * q[1]) };
            R[1] = buffR1;
            float[] buffR2 = { 2.0f * (q[1] * q[3] - q[0] * q[2]), 2.0f * (q[2] * q[3] + q[0] * q[1]), 2.0f * q[0] * q[0] - 1.0f + 2.0f * q[3] * q[3] };
            R[2] = buffR2;
            this.rotationmatrix = R;
            float[][] ps = new float[originsystem.length][originsystem.length];
            float[][] pt = new float[originsystem.length][originsystem.length];
            for (int i = 0; i < originsystem.length; i++) {
                ps[0] = this.redstartg[i];
                pt[0] = this.redtargetg[i];
                m1 += multiplyMatrix(pt, multiplyMatrix(R, transposeMatrix(ps)))[0][0];
                m2 += multiplyMatrix(ps, transposeMatrix(ps))[0][0];
            }
            float m = (m1 / m2);
            this.mg = m;
            this.scalefactor = m - 1.0f;
            float[][] tmp2 = new float[1][1];
            tmp2[0] = cs;
            t = multiplyMatrix(rotationmatrix, transposeMatrix(tmp2));
            for (int i = 0; i < 3; i++) {
                t[i][0] = ct[i] - m * t[i][0];
            }
            this.transformvals = t;
            final long endZeit_1CPU = System.currentTimeMillis();
            final long dauer_1CPU = endZeit_1CPU - startZeit_1CPU;
            System.out.println("...done");
            System.out.println("Time needed on 1 CPU: " + dauer_1CPU + " ms");
            return true;
        } else {
            System.out.println("OriginSystem is < 0 or OriginSystem.length != StartSystem.length");
            return false;
        }
    }

    public Matrix3f array2RotationMatrix3f() {
        Matrix3f temp_rotationmatrix3f = new Matrix3f(Round(rotationmatrix[0][0], 2), Round(rotationmatrix[0][1], 2), Round(rotationmatrix[0][2], 2), Round(rotationmatrix[1][0], 2), Round(rotationmatrix[1][1], 2), Round(rotationmatrix[1][2], 2), Round(rotationmatrix[2][0], 2), Round(rotationmatrix[2][1], 2), Round(rotationmatrix[2][2], 2));
        this.rotationmatrix3f = temp_rotationmatrix3f;
        return rotationmatrix3f;
    }

    public Vector3f array2TranslationVecMatrix3f() {
        Vector3f translationvector3f = new Vector3f(transformvals[0][0], transformvals[1][0], transformvals[2][0]);
        return translationvector3f;
    }

    public float getScaleFactor() {
        return this.scalefactor;
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float) tmp / p;
    }

    public Vector4f deriveTransformation(Vector4f vec) {
        new_point.x = 0.0f;
        new_point.y = 0.0f;
        new_point.z = 0.0f;
        new_point.w = 0.0f;
        Matrix3f transmatrix = new Matrix3f();
        Vector4f ultra_new = new Vector4f();
        transmatrix.m00 = 1.0f + this.scalefactor;
        transmatrix.m01 = -(float) Math.toRadians(this.rotationmatrix3f.m22);
        transmatrix.m02 = (float) Math.toRadians(this.rotationmatrix3f.m11);
        transmatrix.m10 = (float) Math.toRadians(this.rotationmatrix3f.m22);
        transmatrix.m11 = 1.0f + this.scalefactor;
        transmatrix.m12 = -((float) Math.toRadians(this.rotationmatrix3f.m00));
        transmatrix.m20 = -((float) Math.toRadians(this.rotationmatrix3f.m11));
        transmatrix.m21 = (float) Math.toRadians(this.rotationmatrix3f.m00);
        transmatrix.m22 = 1.0f + this.scalefactor;
        System.out.println("Transmatrix: \n" + transmatrix);
        new_point.x = transmatrix.m00 * vec.x + transmatrix.m01 * vec.y + transmatrix.m02 * vec.z;
        new_point.y = transmatrix.m10 * vec.x + transmatrix.m11 * vec.y + transmatrix.m12 * vec.z;
        new_point.z = transmatrix.m20 * vec.x + transmatrix.m21 * vec.y + transmatrix.m22 * vec.z;
        new_point.w = 1.0f;
        System.out.println("New PointA: \n" + new_point);
        ultra_new.x = new_point.x + transformvals[0][0];
        ultra_new.y = new_point.y + transformvals[1][0];
        ultra_new.z = new_point.z + transformvals[2][0];
        ultra_new.w = 1.0f;
        System.out.println("New PointB: \n" + ultra_new);
        return ultra_new;
    }

    public Vector4f MatrixtoAxisAngle(float[][] m) {
        float angle, x, y, z;
        float epsilon = 0.01f;
        float epsilon2 = 0.1f;
        if ((Math.abs(m[0][1] - m[1][0]) < epsilon) && (Math.abs(m[0][2] - m[2][0]) < epsilon) && (Math.abs(m[1][2] - m[2][1]) < epsilon)) {
            if ((Math.abs(m[0][1] + m[1][0]) < epsilon2) && (Math.abs(m[0][2] + m[2][0]) < epsilon2) && (Math.abs(m[1][2] + m[2][1]) < epsilon2) && (Math.abs(m[0][0] + m[1][1] + m[2][2] - 3) < epsilon2)) {
                return new Vector4f(0, 1, 0, 0);
            }
            angle = (float) Math.PI;
            float xx = (m[0][0] + 1) / 2;
            float yy = (m[1][1] + 1) / 2;
            float zz = (m[2][2] + 1) / 2;
            float xy = (m[0][1] + m[1][0]) / 4;
            float xz = (m[0][2] + m[2][0]) / 4;
            float yz = (m[1][2] + m[2][1]) / 4;
            if ((xx > yy) && (xx > zz)) {
                if (xx < epsilon) {
                    x = 0.0f;
                    y = 0.7071f;
                    z = 0.7071f;
                } else {
                    x = (float) Math.sqrt(xx);
                    y = xy / x;
                    z = xz / x;
                }
            } else if (yy > zz) {
                if (yy < epsilon) {
                    x = 0.7071f;
                    y = 0.0f;
                    z = 0.7071f;
                } else {
                    y = (float) Math.sqrt(yy);
                    x = xy / y;
                    z = yz / y;
                }
            } else {
                if (zz < epsilon) {
                    x = 0.7071f;
                    y = 0.7071f;
                    z = 0.0f;
                } else {
                    z = (float) Math.sqrt(zz);
                    x = xz / z;
                    y = yz / z;
                }
            }
            return new Vector4f(angle, x, y, z);
        }
        float s = (float) Math.sqrt((m[2][1] - m[1][2]) * (m[2][1] - m[1][2]) + (m[0][2] - m[2][0]) * (m[0][2] - m[2][0]) + (m[1][0] - m[0][1]) * (m[1][0] - m[0][1]));
        if (Math.abs(s) < 0.001) {
            s = 1;
        }
        angle = (float) Math.acos((m[0][0] + m[1][1] + m[2][2] - 1) / 2);
        x = (m[2][1] - m[1][2]) / s;
        y = (m[0][2] - m[2][0]) / s;
        z = (m[1][0] - m[0][1]) / s;
        return new Vector4f(angle, x, y, z);
    }

    public void RunTests() {
        System.out.println("---------\n---------");
        for (int i = 0; i < centerofgravity_origin.length; i++) {
            System.out.println("CoG Originsystem: " + centerofgravity_origin[i]);
        }
        System.out.println("---------\n---------");
        for (int i = 0; i < centerofgravity_target.length; i++) {
            System.out.println("CoG Targetsystem: " + centerofgravity_target[i]);
        }
        System.out.println("---------\n---------");
        for (int i = 0; i < eigenvalues[0][1].length; i++) {
            for (int j = 0; j < eigenvalues[0][1].length; j++) {
                System.out.println("Eigenvektoren: " + eigenvalues[0][i][j]);
            }
        }
        System.out.println("---------\n---------");
        for (int i = 0; i < eigenvalues[1][1].length; i++) {
            for (int j = 0; j < eigenvalues[1][1].length; j++) {
                System.out.println("Eigenwerte: " + eigenvalues[1][i][j]);
            }
        }
        System.out.println("---------\n---------");
        for (int ii = 0; ii < rotationmatrix.length; ii++) {
            for (int iii = 0; iii < rotationmatrix.length; iii++) {
                System.out.println("3x3 RotMatrix: " + rotationmatrix[ii][iii] + " " + "Zeile " + ii + " Spalte " + iii);
            }
        }
        System.out.println("---------\n---------");
        System.out.println("Scalefactor: " + scalefactor);
        System.out.println("---------\n---------");
        for (int i = 0; i < transformvals.length; i++) {
            System.out.println("|Transformation|: " + transformvals[i][0]);
        }
        System.out.println("---------\n---------");
        System.out.println("RotationMatrix3f: \n" + array2RotationMatrix3f());
        System.out.println("---------\n---------");
        System.out.println("TranslationVector3f: " + array2TranslationVecMatrix3f());
        System.out.println("---------\n---------");
        System.out.println("AxisAngles: " + MatrixtoAxisAngle(rotationmatrix));
        System.out.println("---------\n---------");
    }
}
