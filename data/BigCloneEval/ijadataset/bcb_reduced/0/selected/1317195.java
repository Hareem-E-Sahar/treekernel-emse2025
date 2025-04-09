package co.edu.unal.ungrid.image.util;

public class TransposeHelper {

    private TransposeHelper() {
    }

    public static byte[] transpose(byte[] ba, int w, int h) {
        assert ba != null;
        byte[] data = new byte[ba.length];
        for (int y = 0, zi = 0; y < h; y++) {
            for (int x = 0, zo = y; x < w; x++, zi++, zo += h) {
                data[zo] = ba[zi];
            }
        }
        return data;
    }

    public static int[] transpose(int[] na, int w, int h) {
        assert na != null;
        int[] data = new int[na.length];
        for (int y = 0, zi = 0; y < h; y++) {
            for (int x = 0, zo = y; x < w; x++, zi++, zo += h) {
                data[zo] = na[zi];
            }
        }
        return data;
    }

    public static float[] transpose(float[] fa, int w, int h) {
        assert fa != null;
        float[] data = new float[fa.length];
        for (int y = 0, zi = 0; y < h; y++) {
            for (int x = 0, zo = y; x < w; x++, zi++, zo += h) {
                data[zo] = fa[zi];
            }
        }
        return data;
    }

    public static double[] transpose(double[] da, int w, int h) {
        assert da != null;
        double[] data = new double[da.length];
        for (int y = 0, zi = 0; y < h; y++) {
            for (int x = 0, zo = y; x < w; x++, zi++, zo += h) {
                data[zo] = da[zi];
            }
        }
        return data;
    }

    public static double[][] transpose(double[][] in) {
        assert in != null;
        int h = in.length;
        int w = in[0].length;
        double[][] out = new double[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out[x][y] = in[y][x];
            }
        }
        return out;
    }

    public static void inPlaceTranspose(double[][] in) {
        assert in != null;
        int h = in.length;
        int w = in[0].length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (x > y) {
                    double f = in[y][x];
                    in[y][x] = in[x][y];
                    in[x][y] = f;
                }
            }
        }
    }

    public static void inPlaceTranspose(double[] in, int h, int w) {
        assert in != null;
        for (int y = 0, za = 0; y < h; y++) {
            for (int x = 0, zb = y; x < w; x++, za++, zb += w) {
                if (x > y) {
                    double f = in[za];
                    in[za] = in[zb];
                    in[zb] = f;
                }
            }
        }
    }

    public static void print(double[][] in) {
        assert in != null;
        int h = in.length;
        int w = in[0].length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                System.out.print(String.format("%04.1f ", in[y][x]));
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void print(double[] in, int h, int w) {
        assert in != null;
        for (int y = 0, z = 0; y < h; y++) {
            for (int x = 0; x < w; x++, z++) {
                System.out.print(String.format("%04.1f ", in[z]));
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void testTranspose(String[] args) {
        int h = 4;
        int w = 4;
        double[][] in = new double[h][w];
        for (int y = 0, z = 1; y < h; y++) {
            for (int x = 0; x < w; x++, z++) {
                in[y][x] = z;
            }
        }
        print(in);
        print(transpose(in));
    }

    public static void testInPlaceTranspose2D(String[] args) {
        int h = 4;
        int w = 4;
        double[][] in = new double[h][w];
        for (int y = 0, z = 1; y < h; y++) {
            for (int x = 0; x < w; x++, z++) {
                in[y][x] = z;
            }
        }
        print(in);
        inPlaceTranspose(in);
        print(in);
    }

    public static void testInPlaceTranspose1D(String[] args) {
        int h = 3;
        int w = 4;
        double[] in = new double[h * w];
        for (int y = 0, z = 0; y < h; y++) {
            for (int x = 0; x < w; x++, z++) {
                in[z] = (z + 1);
            }
        }
        print(in, h, w);
        inPlaceTranspose(in, h, w);
        print(in, w, h);
    }

    public static void main(String[] args) {
        testInPlaceTranspose1D(args);
    }
}
