public class Test {    public static Image[][] getFlipHorizintalImage2D(Image[][] pixels) {
        int w = pixels.length;
        int h = pixels[0].length;
        Image pixel[][] = new Image[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixel[i][j] = pixels[j][i];
            }
        }
        return pixel;
    }
}