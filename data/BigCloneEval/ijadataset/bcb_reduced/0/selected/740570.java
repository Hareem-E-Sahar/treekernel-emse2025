package acemocr.resources;

/**
 *
 * @author Kabindra
 */
public class Recognizer {

    public float zoning(Byte image[][], float lf[][], int x1, int y1, int x2, int y2) {
        int width = x2 - x1;
        int height = y2 - y1;
        int bsum = 0;
        int count = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                bsum = 0;
                count = 0;
                for (int k = (i * width) / 5; k <= ((i + 1) * width) / 5; k++) {
                    for (int l = (j * height) / 5; l <= ((j + 1) * height) / 5; l++) {
                        count = count + 1;
                        if (image[k + x1][l + y1] == 1) {
                            bsum = bsum + 1;
                        }
                    }
                }
                lf[i][j] = (bsum * 100) / count;
            }
        }
        return (float) width / height;
    }

    public void processv(Byte Q[][], int x1, int y1, int x2, int y2, int yt[], float av[][]) {
        int width = x2 - x1;
        int sumupper = 0;
        int sumlower = 0;
        for (int j = 0; j < 10; j++) {
            av[0][j] = 0;
            av[1][j] = 0;
            for (int k = (j * width) / 10; k <= ((j + 1) * width) / 10; k++) {
                for (int l = y1; l <= yt[0]; l++) {
                    if (Q[x1 + k][l] == 1 || l == yt[0]) {
                        av[0][j] += (yt[0] - l);
                        break;
                    }
                }
                for (int l = y2; l >= yt[0]; l--) {
                    if (Q[x1 + k][l] == 1 || l == yt[0]) {
                        av[1][j] += (l - yt[0]);
                        break;
                    }
                }
            }
            sumupper += av[0][j];
            sumlower += av[1][j];
        }
        for (int i = 0; i < 10; i++) {
            av[0][i] = (av[0][i] * 100) / sumupper;
            av[1][i] = (av[1][i] * 100) / sumlower;
        }
    }

    public void processh(Byte Q[][], int x1, int y1, int x2, int y2, int xt[], float ah[][]) {
        int height = y2 - y1;
        int sumleft = 0;
        int sumright = 0;
        for (int j = 0; j < 10; j++) {
            ah[j][0] = 0;
            ah[j][1] = 0;
            for (int l = (j * height) / 10; l <= ((j + 1) * height) / 10; l++) {
                for (int k = x1; k <= xt[0]; k++) {
                    if (Q[k][y1 + l] == 1 || k == xt[0]) {
                        ah[j][0] += (xt[0] - k);
                        break;
                    }
                }
                for (int k = x2; k >= xt[0]; k--) {
                    if (Q[k][y1 + l] == 1 || k == xt[0]) {
                        ah[j][1] += (k - xt[0]);
                        break;
                    }
                }
            }
            sumleft += ah[j][0];
            sumright += ah[j][1];
        }
        for (int i = 0; i < 10; i++) {
            ah[i][0] = (ah[i][0] * 100) / sumleft;
            ah[i][1] = (ah[i][1] * 100) / sumright;
        }
    }

    public void centermass(Byte image[][], int x1, int y1, int x2, int y2, int x[], int y[]) {
        int width = x2 - x1;
        int height = y2 - y1;
        int xmean = (x1 + x2) / 2;
        int ymean = (y1 + y2) / 2;
        int sum1 = 0;
        int sum2 = 0;
        int sum = 0;
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                sum1 += image[i][j] * xmean;
                sum2 += image[i][j] * ymean;
                sum += image[i][j];
            }
        }
        x[0] = sum1 / sum;
        y[0] = sum2 / sum;
    }
}
