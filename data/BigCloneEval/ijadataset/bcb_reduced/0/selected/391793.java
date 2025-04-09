package math;

public class Mathe {

    public static float[][] transpose(float[][] f) {
        int m = f.length;
        int n = f[1].length;
        float[][] ft = new float[n][m];
        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) {
            if (f[j].length > i) ft[i][j] = f[j][i];
            System.out.println(i + ":" + j + " " + ft[i][j]);
        }
        return ft;
    }

    public static float mm2m(int mm) {
        float m = (float) (mm / 1000f);
        return m;
    }
}
