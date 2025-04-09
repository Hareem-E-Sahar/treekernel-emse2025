package PRISM.RobotCtrl;

import java.util.*;
import java.io.*;
import java.math.*;

/**
 * 
 * @author Mauro Dragone
 */
public class Matrix {

    private static int iDF = 0;

    public static void AddMatrix(float[][] a, float[][] b, float[][] matrix) {
        int rigA = a.length;
        int rigB = b.length;
        int rigM = matrix.length;
        int colA = a[0].length;
        int colB = b[0].length;
        int colM = matrix[0].length;
        if ((rigA != rigB) || (colA != colB) || (rigM != rigA) || (colM != colA)) {
            System.out.println("AddMatrix, wrong dimensions! " + rigA + "X" + colA + "+" + rigB + "X" + colB + " in " + rigM + "X" + colM + ")");
            return;
        }
        for (int i = 0; i < rigA; i++) for (int j = 0; j < colA; j++) {
            matrix[i][j] = a[i][j] + b[i][j];
        }
    }

    public static void MultiplyMatrix(float[][] a, float[][] b, float[][] matrix) {
        int rigA = a.length;
        int rigB = b.length;
        int rigM = matrix.length;
        int colA = a[0].length;
        int colB = b[0].length;
        int colM = matrix[0].length;
        if ((colA != rigB) || (rigM != rigA) || (colM != colB)) {
            System.out.println("MultiplyMatrix, wrong dimensions! " + rigA + "X" + colA + "*" + rigB + "X" + colB + " in " + rigM + "X" + colM + ")");
            return;
        }
        for (int i = 0; i < rigM; i++) for (int j = 0; j < colM; j++) matrix[i][j] = 0;
        for (int i = 0; i < rigM; i++) for (int j = 0; j < colM; j++) {
            for (int p = 0; p < colA; p++) matrix[i][j] += a[i][p] * b[p][j];
        }
    }

    public static void MultiplyMatrixVector(float[][] a, float[] b, float[] x) {
        int rigA = a.length;
        int rigB = b.length;
        int rigX = x.length;
        int colA = a[0].length;
        if ((colA != rigB) || (rigX != colA) || (rigX != rigA)) {
            System.out.println("MultiplyMatrixVector, wrong dimensions! " + rigA + "X" + colA + "*" + rigB + "X 1" + " in " + rigX + "X 1)");
            return;
        }
        for (int i = 0; i < rigX; i++) x[i] = 0;
        for (int i = 0; i < rigA; i++) for (int j = 0; j < colA; j++) x[i] += a[i][j] * b[j];
    }

    public static void MultiplyMatrixTranspose(float[][] a, float[][] b, float[][] matrix) {
        int rigA = a.length;
        int rigB = b[0].length;
        int rigM = matrix.length;
        int colA = a[0].length;
        int colB = b.length;
        int colM = matrix[0].length;
        if ((colA != rigB) || (rigM != rigA) || (colM != colB)) {
            System.out.println("MultuplyMatrixTranspose, wrong dimensions! " + rigA + "X" + colA + "*" + rigB + "X" + colB + " in " + rigM + "X" + colM + ")");
            return;
        }
        for (int i = 0; i < rigM; i++) for (int j = 0; j < colM; j++) matrix[i][j] = 0;
        for (int i = 0; i < rigM; i++) for (int j = 0; j < colM; j++) {
            for (int p = 0; p < colA; p++) matrix[i][j] += a[i][p] * b[j][p];
        }
    }

    public static float[][] Adjoint(float[][] a) {
        int tms = a.length;
        float m[][] = new float[tms][tms];
        int ii, jj, ia, ja;
        float det;
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            ia = ja = 0;
            float ap[][] = new float[tms - 1][tms - 1];
            for (ii = 0; ii < tms; ii++) {
                for (jj = 0; jj < tms; jj++) {
                    if ((ii != i) && (jj != j)) {
                        ap[ia][ja] = a[ii][jj];
                        ja++;
                    }
                }
                if ((ii != i) && (jj != j)) {
                    ia++;
                }
                ja = 0;
            }
            det = Determinant(ap);
            m[i][j] = (float) Math.pow(-1, i + j) * det;
        }
        m = Transpose(m);
        return m;
    }

    public static float[][] Transpose(float[][] a) {
        int righe = a.length;
        int colonne = a[0].length;
        float m[][] = new float[colonne][righe];
        for (int i = 0; i < colonne; i++) for (int j = 0; j < righe; j++) {
            m[i][j] = a[j][i];
        }
        return m;
    }

    public static void Inverse(float[][] a, float m[][]) {
        int tms = a.length;
        float mm[][] = Adjoint(a);
        float det = Determinant(a);
        float dd = 0;
        if (det == 0) {
            System.out.println("Determinant Equals 0, Not Invertible.");
        } else {
            dd = 1 / det;
        }
        for (int i = 0; i < tms; i++) for (int j = 0; j < tms; j++) {
            m[i][j] = dd * mm[i][j];
        }
    }

    public static float[][] UpperTriangle(float[][] m) {
        float f1 = 0;
        float temp = 0;
        int tms = m.length;
        int v = 1;
        iDF = 1;
        for (int col = 0; col < tms - 1; col++) {
            for (int row = col + 1; row < tms; row++) {
                v = 1;
                outahere: while (m[col][col] == 0) {
                    if (col + v >= tms) {
                        iDF = 0;
                        break outahere;
                    } else {
                        for (int c = 0; c < tms; c++) {
                            temp = m[col][c];
                            m[col][c] = m[col + v][c];
                            m[col + v][c] = temp;
                        }
                        v++;
                        iDF = iDF * -1;
                    }
                }
                if (m[col][col] != 0) {
                    try {
                        f1 = (-1) * m[row][col] / m[col][col];
                        for (int i = col; i < tms; i++) {
                            m[row][i] = f1 * m[col][i] + m[row][i];
                        }
                    } catch (Exception e) {
                        System.out.println("Still Here!!!");
                    }
                }
            }
        }
        return m;
    }

    public static float Determinant(float[][] matrix) {
        int tms = matrix.length;
        float det = 1;
        matrix = UpperTriangle(matrix);
        for (int i = 0; i < tms; i++) {
            det = det * matrix[i][i];
        }
        det = det * iDF;
        return det;
    }

    public static void Print(String name, float[][] m) {
        int righe = m.length;
        int colonne = m[0].length;
        System.out.println(name + ":");
        System.out.println("------------");
        for (int yr = 0; yr < righe; yr++) {
            for (int yc = 0; yc < colonne; yc++) {
                System.out.print(m[yr][yc] + " , ");
            }
            System.out.println();
        }
        System.out.println("------------");
    }
}
