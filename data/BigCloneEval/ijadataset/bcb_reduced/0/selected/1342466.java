package foucault.filter;

import foucault.utils.ImgUtils;

public class DerivateFilter implements PictureFilter {

    int width;

    int height;

    int[] arr;

    int[] sobelCoordsX;

    int[] sobelCoordsY;

    double[] sobelValues;

    int soebelEdges;

    public void transform(FilterParams p) {
        width = p.image.getWidth();
        height = p.image.getHeight();
        arr = new int[width * height];
        int[] copy = new int[width * height];
        p.image.getRGB(0, 0, width, height, arr, 0, width);
        int min = 1;
        int maxW = width - min;
        int maxH = height - min;
        for (int y = min; y < maxH; y++) {
            for (int x = min; x < maxW; x++) {
                double soebel = directionLessSobelEdgeDetectionValue(x, y);
                int fd = Math.max(0, Math.min(255, (int) (128 + soebel * 10d)));
                copy[y * width + x] = ImgUtils.brightness2rgb(fd);
            }
        }
        p.image.setRGB(0, 0, width, height, copy, 0, width);
    }

    private int f(int y, int x) {
        return ImgUtils.brightness(arr[y * width + x]);
    }

    private double directionLessSobelEdgeDetectionValue(int xPos, int yPos) {
        int[][] d = new int[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                d[x][y] = f(yPos + y - 1, xPos + x - 1);
            }
        }
        double sobelH = ((double) d[0][2] + (double) (2 * d[1][2]) + d[2][2] - d[0][0] - (2 * d[1][0]) - d[2][0]);
        double sobelV = ((double) d[2][0] + (double) (2 * d[2][1]) + d[2][2] - d[0][0] - (2 * d[0][1]) - d[0][2]);
        double sobelSum = (sobelH + sobelV) / 2;
        return sobelSum;
    }
}
