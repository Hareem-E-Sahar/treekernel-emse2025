import java.util.*;
import java.io.*;

public class TestDom {

    public static float[][] mtx = new float[][] { { 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1 }, { 1, 0, 0, 0, 0, 1 } };

    public static void main(String[] args) {
        printMtx(mtx);
        printMtx(calTPDominance());
        printMtx(calSolDominance());
    }

    public static float[][] calTPDominance() {
        int ntp = mtx.length;
        float[][] dom = new float[ntp][ntp];
        for (int i = 0; i < ntp; i++) {
            for (int j = (i + 1); j < ntp; j++) {
                dom[i][j] = (float) checkDominance(mtx[i], mtx[j]);
                if (dom[i][j] == 1) {
                    dom[j][i] = -1;
                    dom[i][j] = 0;
                }
            }
        }
        return (dom);
    }

    public static float[][] calSolDominance() {
        int ntp = mtx.length;
        int nsol = mtx[0].length;
        float[][] dom = new float[nsol][nsol];
        float[][] int2 = new float[nsol][ntp];
        for (int i = 0; i < nsol; i++) {
            for (int j = 0; j < ntp; j++) {
                int2[i][j] = mtx[j][i];
            }
        }
        for (int i = 0; i < nsol; i++) {
            for (int j = (i + 1); j < nsol; j++) {
                dom[i][j] = (float) checkDominance(int2[i], int2[j]);
                if (dom[i][j] == 1) {
                    dom[j][i] = -1;
                    dom[i][j] = 0;
                }
            }
        }
        return (dom);
    }

    public static int checkDominance(final float[] a1, final float[] a2) {
        boolean A1DomA2 = ((a1[0] - a2[0]) > 0);
        boolean A2DomA1 = ((a1[0] - a2[0]) < 0);
        for (int i = 1; i < a1.length; i++) {
            if (A1DomA2) {
                if ((a1[i] - a2[i]) < 0) {
                    A1DomA2 = false;
                    break;
                }
            } else if (A2DomA1) {
                if ((a1[i] - a2[i]) > 0) {
                    A2DomA1 = false;
                    break;
                }
            } else {
                A1DomA2 = ((a1[i] - a2[i]) > 0);
                A2DomA1 = ((a1[i] - a2[i]) < 0);
            }
        }
        if (A1DomA2) return (1); else if (A2DomA1) return (-1); else return (0);
    }

    public static String arrayToString(final float[] array) {
        String output = new String();
        for (int i = 0; i < array.length; i++) {
            output = output + " " + array[i];
        }
        return (output);
    }

    public static void printMtx(final float[][] mtx) {
        int nrow = mtx.length;
        int ncol = mtx[0].length;
        System.out.print("      ");
        for (int j = 0; j < ncol; j++) System.out.print(j + "   ");
        System.out.print("\n");
        for (int i = 0; i < nrow; i++) System.out.println(i + ": " + arrayToString(mtx[i]));
        System.out.print("\n");
    }

    public static float sumArray(final float[] a1) {
        float sum = 0;
        for (int i = 0; i < a1.length; i++) {
            sum += a1[i];
        }
        return (sum);
    }

    public static float[] normalizeMax(final float[] a1) {
        float maxA = -1000000F;
        float minA = 1000000F;
        float[] a2 = new float[a1.length];
        for (int i = 0; i < a1.length; i++) {
            maxA = Math.max(maxA, a1[i]);
            minA = Math.min(minA, a1[i]);
        }
        for (int i = 0; i < a2.length; i++) {
            a2[i] = (a1[i] - minA) / (maxA - minA);
        }
        return (a2);
    }
}
