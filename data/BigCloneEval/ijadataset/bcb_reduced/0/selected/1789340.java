package lusc.net.sourceforge;

public class CompareThread4 extends Thread {

    int start, stop, maxlength, dims, f;

    boolean weightByAmp;

    float[] scores;

    double[] validParameters, sds, seg2, point2, vec1sq;

    double[][] p, q, r, s, seg1a, seg1b, vec1;

    int[][] proute;

    double[][] dataFocal;

    int[] locsX = { -1, -1, 0 };

    int[] locsY = { 0, -1, -1 };

    double[][][] data;

    long time1, time2 = 0;

    double sdRatio = 0.5;

    double lowerCutOff = 0.02;

    public CompareThread4(int maxlength, double[][][] data, double[] sds, double sdRatio, double[] validParameters, boolean weightByAmp, float[] scores, int start, int stop, int f) {
        this.data = data;
        this.sds = sds;
        this.sdRatio = sdRatio;
        this.validParameters = validParameters;
        this.maxlength = maxlength;
        this.weightByAmp = weightByAmp;
        this.scores = scores;
        this.start = start;
        this.stop = stop;
        this.f = f;
        dataFocal = new double[data[f].length][];
        for (int i = 0; i < dataFocal.length; i++) {
            dataFocal[i] = new double[data[f][i].length];
            System.arraycopy(data[f][i], 0, dataFocal[i], 0, dataFocal[i].length);
        }
        dims = dataFocal.length - 1;
        if (weightByAmp) {
            dims--;
        }
    }

    public synchronized void run() {
        p = new double[maxlength][maxlength];
        q = new double[maxlength][maxlength];
        r = new double[maxlength][maxlength];
        s = new double[maxlength][maxlength];
        proute = new int[maxlength][maxlength];
        for (int i = start; i < stop; i++) {
            scores[i] = derTimeWarpingAsym(dataFocal, data[i]);
        }
    }

    public double[] getJointAverages(double[][] data1, double[][] data2, boolean weight) {
        double avs1[] = new double[dims];
        double avs2[] = new double[dims];
        double sum1[] = new double[dims];
        double sum2[] = new double[dims];
        for (int i = 0; i < dims; i++) {
            for (int j = 0; j < data1[0].length; j++) {
                if (weight) {
                    avs1[i] += data1[i][j] * data1[dims][j];
                    sum1[i] += data1[dims][j];
                } else {
                    avs1[i] += data1[i][j];
                    sum1[i]++;
                }
            }
            for (int j = 0; j < data2[0].length; j++) {
                if (weight) {
                    avs2[i] += data2[i][j] * data2[dims][j];
                    sum2[i] += data2[dims][j];
                } else {
                    avs2[i] += data2[i][j];
                    sum2[i]++;
                }
            }
        }
        for (int i = 0; i < dims; i++) {
            avs1[i] = 0.5 * ((avs1[i] + avs2[i]) / (sum1[i] + sum2[i]));
        }
        return (avs1);
    }

    public double[] getJointSD(double[][] data1, double[][] data2, double[] avs, boolean weight) {
        double sd1[] = new double[dims];
        double sd2[] = new double[dims];
        double sum1[] = new double[dims];
        double sum2[] = new double[dims];
        double a;
        for (int i = 0; i < dims; i++) {
            for (int j = 0; j < data1[0].length; j++) {
                if (weight) {
                    a = (data1[i][j] - avs[i]);
                    sum1[i] += data1[dims][j];
                    sd1[i] += a * a * data1[dims][j];
                } else {
                    a = data1[i][j] - avs[i];
                    sum1[i]++;
                    sd1[i] += a * a;
                }
            }
            for (int j = 0; j < data2[0].length; j++) {
                if (weight) {
                    a = (data2[i][j] - avs[i]);
                    sum2[i] += data2[dims][j];
                    sd2[i] += a * a * data2[dims][j];
                } else {
                    a = data2[i][j] - avs[i];
                    sum2[i]++;
                    sd2[i] += a * a;
                }
            }
        }
        for (int i = 0; i < dims; i++) {
            sd1[i] = Math.sqrt((sd1[i] + sd2[i]) / (sum1[i] + sum2[i]));
        }
        return (sd1);
    }

    public float derTimeWarpingAsym(double[][] dataFocal, double[][] dataComp) {
        double[] avs = getJointAverages(dataFocal, dataComp, weightByAmp);
        double[] sd = getJointSD(dataFocal, dataComp, avs, weightByAmp);
        double totweight = 0;
        for (int i = 0; i < dims; i++) {
            totweight += validParameters[i];
        }
        for (int i = 0; i < dims; i++) {
            sd[i] = sdRatio * sd[i] + (1 - sdRatio) * sds[i];
            if ((Double.isNaN(sd[i])) || (sd[i] == 0)) {
                sd[i] = sds[i];
                System.out.println("Extreme sd ratio problem!");
            }
            sd[i] = validParameters[i] / (totweight * sd[i]);
        }
        float score1 = derTimeWarpingPointInterpol(dataFocal, dataComp, sd);
        double[][] x1 = new double[dataFocal.length][dataFocal[0].length];
        for (int i = 1; i < dataFocal.length; i++) {
            System.arraycopy(dataFocal[i], 0, x1[i], 0, dataFocal[0].length);
        }
        double diff = dataFocal[0][dataFocal[0].length - 1] - dataComp[0][dataComp[0].length - 1];
        for (int i = 0; i < dataFocal[0].length; i++) {
            x1[0][i] = dataFocal[0][i] - diff;
        }
        float score3 = derTimeWarpingPointInterpol(x1, dataComp, sd);
        if (score3 < score1) {
            score1 = score3;
        }
        diff = diff * 0.5;
        for (int i = 0; i < dataFocal[0].length; i++) {
            x1[0][i] = dataFocal[0][i] - diff;
        }
        float score5 = derTimeWarpingPointInterpol(x1, dataComp, sd);
        if (score5 < score1) {
            score1 = score5;
        }
        if (Float.isNaN(score1)) {
            System.out.println("DTW Made a NaN");
            System.out.println(score1 + " " + score3 + " " + score5);
            for (int i = 0; i < sd.length; i++) {
                System.out.print(sd[i]);
            }
            System.out.println();
        }
        return score1;
    }

    public float derTimeWarpingPointInterpol(double[][] dataFocal, double[][] dataComp, double[] sdf) {
        int length1 = dataFocal[0].length;
        int length2 = dataComp[0].length;
        int length21 = dataComp.length - 1;
        double min, sc, sc2, d1, a1, b1, c1, xx1, xx2, xx3, x1, x2, x3;
        ;
        int x, y, z, id, i, j, k, locx, locy;
        x1 = 0;
        x2 = 0;
        x3 = 0;
        sc2 = 0;
        j = 0;
        for (i = 0; i < length2; i++) {
            if (dataComp[length21][i] == 0) {
                j++;
            }
        }
        int length3 = j;
        int[] w = new int[length3];
        j = 0;
        for (i = 0; i < length2; i++) {
            if (dataComp[length21][i] == 0) {
                w[j] = i;
                j++;
            }
        }
        double[][] seg1 = new double[length3][dims];
        double[][] seg2 = new double[length3][dims];
        double[] d2 = new double[length3];
        double[] d3 = new double[length3];
        j = 0;
        for (i = 0; i < length2; i++) {
            if (dataComp[length21][i] == 0) {
                for (id = 0; id < dims; id++) {
                    a1 = dataComp[id][w[j]] * sdf[id];
                    b1 = dataComp[id][w[j] + 1] * sdf[id];
                    seg1[j][id] = a1;
                    seg2[j][id] = b1;
                    c1 = b1 - a1;
                    d2[j] += c1 * c1;
                }
                d3[j] = Math.sqrt(d2[j]);
                j++;
            }
        }
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length3; j++) {
                s[i][j] = 0;
                xx1 = 0;
                xx2 = 0;
                for (id = 0; id < dims; id++) {
                    d1 = dataFocal[id][i] * sdf[id];
                    xx1 += (d1 - seg1[j][id]) * (d1 - seg1[j][id]);
                    xx2 += (d1 - seg2[j][id]) * (d1 - seg2[j][id]);
                }
                if ((xx2 - d2[j] - xx1) > 0) {
                    x1 = Math.sqrt(xx1);
                    s[i][j] = x1;
                    x = 1;
                } else if ((xx1 - d2[j] - xx2) > 0) {
                    x2 = Math.sqrt(xx2);
                    s[i][j] = x2;
                    x = 2;
                } else {
                    x1 = Math.sqrt(xx1);
                    x2 = Math.sqrt(xx2);
                    x3 = d3[j];
                    sc = 0.5 * (x3 + x1 + x2);
                    xx1 = sc - x1;
                    xx2 = sc - x2;
                    xx3 = sc - x3;
                    if (xx3 >= 0) {
                        sc2 = Math.sqrt(sc * xx1 * xx2 * xx3);
                        s[i][j] = 2 * sc2 / d3[j];
                    } else {
                        s[i][j] = 0;
                    }
                    x = 3;
                }
                if (Double.isNaN(s[i][j])) {
                    System.out.println(x + " " + x1 + " " + x2 + " " + x3 + " " + sc2);
                }
            }
        }
        r[0][0] = s[0][0];
        p[0][0] = 1;
        if (weightByAmp) {
            q[0][0] = dataFocal[dims][0];
        }
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length3; j++) {
                min = 1000000000;
                locx = 0;
                locy = 0;
                z = -1;
                for (k = 0; k < 3; k++) {
                    x = i + locsX[k];
                    y = j + locsY[k];
                    if ((x >= 0) && (y >= 0)) {
                        sc2 = r[x][y] / p[x][y];
                        if (sc2 < min) {
                            min = sc2;
                            locx = x;
                            locy = y;
                            z = k;
                            proute[i][j] = k;
                        }
                    }
                }
                if (z >= 0) {
                    r[i][j] = r[locx][locy] + s[i][j];
                    p[i][j] = p[locx][locy] + 1;
                    if (weightByAmp) {
                        q[i][j] = q[locx][locy] + dataFocal[dims][i];
                    }
                }
            }
        }
        int ba = length1 - 1;
        int bb = length3 - 1;
        double den = p[ba][bb];
        if (weightByAmp) {
            den = q[ba][bb];
        }
        j = bb;
        double finalScore = 0;
        den = 0;
        for (i = ba; i >= 0; i--) {
            sc2 = s[i][j];
            sc = 1;
            while (proute[i][j] == 2) {
                j--;
                sc2 += s[i][j];
                sc++;
            }
            sc2 /= sc;
            if (weightByAmp) {
                finalScore += sc2 * dataFocal[dims][i];
                den += dataFocal[dims][i];
            } else {
                finalScore += sc2;
            }
            if (proute[i][j] == 1) {
                j--;
            }
        }
        if (weightByAmp) {
            finalScore /= den;
        } else {
            finalScore /= length1 + 0.0;
        }
        float result = (float) finalScore;
        return result;
    }

    public float derTimeWarpingPointFast(double[][] dataFocal, double[][] dataComp, double[] sdf) {
        int length1 = dataFocal[0].length;
        int length2 = dataComp[0].length;
        double min, sc, sc2;
        int x, y, z, i, j, k, locx, locy;
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length2; j++) {
                sc = 0;
                for (k = 0; k < dims; k++) {
                    sc2 = (dataComp[k][j] - dataFocal[k][i]) * sdf[k];
                    sc += sc2 * sc2;
                }
                s[i][j] = Math.sqrt(sc);
            }
        }
        r[0][0] = s[0][0];
        p[0][0] = 1;
        if (weightByAmp) {
            q[0][0] = dataFocal[dims][0];
        }
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length2; j++) {
                min = 1000000000;
                locx = 0;
                locy = 0;
                z = -1;
                for (k = 0; k < 3; k++) {
                    x = i + locsX[k];
                    y = j + locsY[k];
                    if ((x >= 0) && (y >= 0)) {
                        sc2 = r[x][y] / p[x][y];
                        if (sc2 < min) {
                            min = sc2;
                            locx = x;
                            locy = y;
                            z = k;
                        }
                    }
                }
                if (z >= 0) {
                    r[i][j] = r[locx][locy] + s[i][j];
                    p[i][j] = p[locx][locy] + 1;
                    if (weightByAmp) {
                        q[i][j] = q[locx][locy] + dataFocal[dims][i];
                    }
                }
            }
        }
        int ba = length1 - 1;
        int bb = length2 - 1;
        double den = p[ba][bb];
        if (weightByAmp) {
            den = q[ba][bb];
        }
        float result = (float) (r[ba][bb] / den);
        return result;
    }

    public float derTimeWarpingPointOld(double[][] dataFocal, double[][] dataComp, double[] sdf) {
        int length1 = dataFocal[0].length - 1;
        int length2 = dataComp[0].length;
        double min, sc2, b, d1, d, a1, b1, c1;
        int x, y, z, id, i, j, k, locx, locy;
        double[][][] seg1 = new double[length1][dims][2];
        double[][] seg2 = new double[dims][2];
        double[] vec2 = new double[dims];
        double[] d2 = new double[length1];
        double[] point2 = new double[dims];
        double[][] vec1 = new double[length1][dims];
        for (i = 0; i < length1; i++) {
            for (id = 0; id < dims; id++) {
                a1 = dataFocal[id][i] * sdf[id];
                b1 = dataFocal[id][i + 1] * sdf[id];
                seg1[i][id][0] = a1;
                seg1[i][id][1] = b1;
                c1 = b1 - a1;
                d2[i] += c1 * c1;
                vec1[i][id] = c1;
            }
        }
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length2; j++) {
                s[i][j] = 0;
                d1 = 0;
                for (id = 0; id < dims; id++) {
                    seg2[id][1] = seg1[i][id][0];
                    seg2[id][0] = dataComp[id][j] * sdf[id];
                    vec2[id] = seg2[id][1] - seg2[id][0];
                    d1 += vec1[i][id] * vec2[id];
                }
                if (d1 <= 0) {
                    d = 0;
                    for (id = 0; id < dims; id++) {
                        d += (seg1[i][id][0] - seg2[id][0]) * (seg1[i][id][0] - seg2[id][0]);
                    }
                    s[i][j] = d;
                } else {
                    if (d2[i] <= d1) {
                        d = 0;
                        for (id = 0; id < dims; id++) {
                            d += (seg1[i][id][1] - seg2[id][0]) * (seg1[i][id][1] - seg2[id][0]);
                        }
                        s[i][j] = d;
                    } else {
                        b = d1 / d2[i];
                        for (id = 0; id < dims; id++) {
                            point2[id] = seg1[i][id][0] + b * vec1[i][id];
                        }
                        d = 0;
                        for (id = 0; id < dims; id++) {
                            d += (point2[id] - seg2[id][0]) * (point2[id] - seg2[id][0]);
                        }
                        s[i][j] = d;
                    }
                }
                if (weightByAmp) {
                    q[i][j] = 0.5 * (dataFocal[dims][i] + dataFocal[dims][i + 1]);
                } else {
                    q[i][j] = 1;
                }
            }
        }
        r[0][0] = s[0][0];
        p[0][0] = q[0][0];
        for (i = 0; i < length1; i++) {
            for (j = 0; j < length2; j++) {
                min = 1000000000;
                locx = 0;
                locy = 0;
                z = -1;
                for (k = 0; k < 3; k++) {
                    x = i + locsX[k];
                    y = j + locsY[k];
                    if ((x >= 0) && (y >= 0)) {
                        sc2 = r[x][y] / p[x][y];
                        if (sc2 < min) {
                            min = sc2;
                            locx = x;
                            locy = y;
                            z = k;
                        }
                    }
                }
                if (z >= 0) {
                    if (weightByAmp) {
                        r[i][j] = r[locx][locy] + s[i][j] * q[i][j];
                        p[i][j] = q[i][j] + p[locx][locy];
                    } else {
                        r[i][j] = r[locx][locy] + s[i][j];
                        p[i][j] = p[locx][locy] + 1;
                    }
                }
            }
        }
        return (float) Math.sqrt(r[length1 - 1][length2 - 1] / p[length1 - 1][length2 - 1]);
    }
}
