package gnu.javax.imageio.jpeg;

/**
 * Discrete Cosine Transformations.
 */
public class DCT {

    /**
   * Cosine matrix
   */
    public double c[][] = new double[8][8];

    /**
   * Transformed cosine matrix
   */
    public double cT[][] = new double[8][8];

    public DCT() {
        initMatrix();
    }

    /**
   * Figure A.3.3 IDCT, Cu Cv on A-5 of the ISO DIS 10918-1. Requirements and
   * Guidelines.
   * 
   * @param u
   * @return
   */
    public static double C(int u) {
        return ((u == 0) ? (double) 1 / (double) Math.sqrt((double) 2) : (double) 1);
    }

    /**
   * Initialize matrix values for the fast_idct function
   */
    private void initMatrix() {
        for (int j = 0; j < 8; j++) {
            double nn = (double) (8);
            c[0][j] = 1.0 / Math.sqrt(nn);
            cT[j][0] = c[0][j];
        }
        for (int i = 1; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                double jj = (double) j;
                double ii = (double) i;
                c[i][j] = Math.sqrt(2.0 / 8.0) * Math.cos(((2.0 * jj + 1.0) * ii * Math.PI) / (2.0 * 8.0));
                cT[j][i] = c[i][j];
            }
        }
    }

    /**
   * slow_idct - Figure A.3.3 IDCT (informative) on A-5 of the ISO DIS
   * 10918-1. Requirements and Guidelines. This is a slow IDCT, there are
   * better algorithms to use, it's fairly expensive with processor speed.
   * 
   * @param matrix
   * @return
   */
    public static double[][] slow_idct(double[][] matrix) {
        double[][] output = new double[matrix.length][matrix.length];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                double val = 0;
                for (double v = 0; v < 8; v++) {
                    double innerloop = 0;
                    for (double u = 0; u < 8; u++) innerloop += (DCT.C((int) u) / (double) 2) * matrix[(int) v][(int) u] * Math.cos((2 * x + 1) * u * Math.PI / (double) 16) * Math.cos((2 * y + 1) * v * Math.PI / (double) 16);
                    val += (DCT.C((int) v) / (double) 2) * innerloop;
                }
                output[y][x] = (val + 128);
            }
        }
        return (output);
    }

    public static float[][] slow_fdct(float[][] value) {
        float[][] buffer = new float[8][8];
        for (int u = 0; u < 8; u++) {
            for (int v = 0; v < 8; v++) {
                buffer[u][v] = (float) (1 / 4) * (float) C((int) u) * (float) C((int) v);
                float innerval = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        innerval += value[y][x] * Math.cos(((2 * x + 1) * u * Math.PI) / 16) * Math.cos(((2 * y + 1) * v * Math.PI) / 16);
                    }
                }
                buffer[u][v] *= innerval;
            }
        }
        return (buffer);
    }

    public float[][] fast_fdct(float[][] input) {
        float output[][] = new float[8][8];
        double temp[][] = new double[8][8];
        double temp1;
        int i;
        int j;
        int k;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                temp[i][j] = 0.0;
                for (k = 0; k < 8; k++) {
                    temp[i][j] += (((int) (input[i][k]) - 128) * cT[k][j]);
                }
            }
        }
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                temp1 = 0.0;
                for (k = 0; k < 8; k++) {
                    temp1 += (c[i][k] * temp[k][j]);
                }
                output[i][j] = (int) Math.round(temp1) * 8;
            }
        }
        return output;
    }

    /**
   * fast_idct - Figure A.3.3 IDCT (informative) on A-5 of the ISO DIS
   * 10918-1. Requires and Guidelines. This is a fast IDCT, it much more
   * effecient and only inaccurate at about 1/1000th of a percent of values
   * analyzed. Cannot be static because initMatrix must run before any
   * fast_idct values can be computed.
   * 
   * @param input
   * @return
   */
    public double[][] fast_idct(double[][] input) {
        double output[][] = new double[8][8];
        double temp[][] = new double[8][8];
        double temp1;
        int i, j, k;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                temp[i][j] = 0.0;
                for (k = 0; k < 8; k++) {
                    temp[i][j] += input[i][k] * c[k][j];
                }
            }
        }
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                temp1 = 0.0;
                for (k = 0; k < 8; k++) temp1 += cT[i][k] * temp[k][j];
                temp1 += 128.0;
                if (temp1 < 0) output[i][j] = 0; else if (temp1 > 255) output[i][j] = 255; else output[i][j] = (int) Math.round(temp1);
            }
        }
        return output;
    }

    public double[][] idj_fast_fdct(float input[][]) {
        double output[][] = new double[8][8];
        double tmp0, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7;
        double tmp10, tmp11, tmp12, tmp13;
        double z1, z2, z3, z4, z5, z11, z13;
        int i;
        int j;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                output[i][j] = ((double) input[i][j] - (double) 128.0);
            }
        }
        for (i = 0; i < 8; i++) {
            tmp0 = output[i][0] + output[i][7];
            tmp7 = output[i][0] - output[i][7];
            tmp1 = output[i][1] + output[i][6];
            tmp6 = output[i][1] - output[i][6];
            tmp2 = output[i][2] + output[i][5];
            tmp5 = output[i][2] - output[i][5];
            tmp3 = output[i][3] + output[i][4];
            tmp4 = output[i][3] - output[i][4];
            tmp10 = tmp0 + tmp3;
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
            output[i][0] = tmp10 + tmp11;
            output[i][4] = tmp10 - tmp11;
            z1 = (tmp12 + tmp13) * (double) 0.707106781;
            output[i][2] = tmp13 + z1;
            output[i][6] = tmp13 - z1;
            tmp10 = tmp4 + tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;
            z5 = (tmp10 - tmp12) * (double) 0.382683433;
            z2 = ((double) 0.541196100) * tmp10 + z5;
            z4 = ((double) 1.306562965) * tmp12 + z5;
            z3 = tmp11 * ((double) 0.707106781);
            z11 = tmp7 + z3;
            z13 = tmp7 - z3;
            output[i][5] = z13 + z2;
            output[i][3] = z13 - z2;
            output[i][1] = z11 + z4;
            output[i][7] = z11 - z4;
        }
        for (i = 0; i < 8; i++) {
            tmp0 = output[0][i] + output[7][i];
            tmp7 = output[0][i] - output[7][i];
            tmp1 = output[1][i] + output[6][i];
            tmp6 = output[1][i] - output[6][i];
            tmp2 = output[2][i] + output[5][i];
            tmp5 = output[2][i] - output[5][i];
            tmp3 = output[3][i] + output[4][i];
            tmp4 = output[3][i] - output[4][i];
            tmp10 = tmp0 + tmp3;
            tmp13 = tmp0 - tmp3;
            tmp11 = tmp1 + tmp2;
            tmp12 = tmp1 - tmp2;
            output[0][i] = tmp10 + tmp11;
            output[4][i] = tmp10 - tmp11;
            z1 = (tmp12 + tmp13) * (double) 0.707106781;
            output[2][i] = tmp13 + z1;
            output[6][i] = tmp13 - z1;
            tmp10 = tmp4 + tmp5;
            tmp11 = tmp5 + tmp6;
            tmp12 = tmp6 + tmp7;
            z5 = (tmp10 - tmp12) * (double) 0.382683433;
            z2 = ((double) 0.541196100) * tmp10 + z5;
            z4 = ((double) 1.306562965) * tmp12 + z5;
            z3 = tmp11 * ((double) 0.707106781);
            z11 = tmp7 + z3;
            z13 = tmp7 - z3;
            output[5][i] = z13 + z2;
            output[3][i] = z13 - z2;
            output[1][i] = z11 + z4;
            output[7][i] = z11 - z4;
        }
        return output;
    }
}
