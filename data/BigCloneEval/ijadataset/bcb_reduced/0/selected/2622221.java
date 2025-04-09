package santa.nice.ocr.kernel;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class OCRUtility {

    public static BufferedImage cropImage(BufferedImage orig, Rectangle rect) {
        if (rect.x < 0 || rect.y < 0 || rect.width <= 0 || rect.height <= 0 || rect.x + rect.width > orig.getWidth() || rect.y + rect.height > orig.getHeight()) {
            throw new RuntimeException("Utility.getCrop");
        }
        BufferedImage bi = orig.getSubimage(rect.x, rect.y, rect.width, rect.height);
        return bi;
    }

    public static BufferedImage readImage(String filename) throws IOException {
        BufferedImage im = ImageIO.read(new File(filename));
        if (im == null) throw new IOException("Not an image file");
        return im;
    }

    public static boolean imageEquals(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) return false;
        int[] array1 = null;
        array1 = image1.getData().getPixels(0, 0, image1.getWidth(), image1.getHeight(), array1);
        int[] array2 = null;
        array2 = image2.getData().getPixels(0, 0, image2.getWidth(), image2.getHeight(), array2);
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) return false;
        }
        return true;
    }

    public static void applyMedianBlur(BufferedImage image) {
        final int WIDTH = image.getWidth();
        final int HEIGHT = image.getHeight();
        int[] rgbArray = null;
        int[] buffer = null;
        buffer = image.getRGB(0, 0, WIDTH, HEIGHT, buffer, 0, WIDTH);
        rgbArray = image.getRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);
        for (int row = 1; row < HEIGHT - 1; row++) {
            for (int col = 1; col < WIDTH - 1; col++) {
                int r = (((buffer[row * WIDTH + col] >> 16) & 0xff) + ((buffer[row * WIDTH + col - 1] >> 16) & 0xff) + ((buffer[row * WIDTH + col + 1] >> 16) & 0xff) + ((buffer[(row + 1) * WIDTH + col] >> 16) & 0xff) + ((buffer[(row + 1) * WIDTH + col - 1] >> 16) & 0xff) + ((buffer[(row + 1) * WIDTH + col + 1] >> 16) & 0xff) + ((buffer[(row - 1) * WIDTH + col] >> 16) & 0xff) + ((buffer[(row - 1) * WIDTH + col - 1] >> 16) & 0xff) + ((buffer[(row - 1) * WIDTH + col + 1] >> 16) & 0xff)) / 9;
                int g = (((buffer[row * WIDTH + col] >> 8) & 0xff) + ((buffer[row * WIDTH + col - 1] >> 8) & 0xff) + ((buffer[row * WIDTH + col + 1] >> 8) & 0xff) + ((buffer[(row + 1) * WIDTH + col] >> 8) & 0xff) + ((buffer[(row + 1) * WIDTH + col - 1] >> 8) & 0xff) + ((buffer[(row + 1) * WIDTH + col + 1] >> 8) & 0xff) + ((buffer[(row - 1) * WIDTH + col] >> 8) & 0xff) + ((buffer[(row - 1) * WIDTH + col - 1] >> 8) & 0xff) + ((buffer[(row - 1) * WIDTH + col + 1] >> 8) & 0xff)) / 9;
                int b = ((buffer[row * WIDTH + col] & 0xff) + (buffer[row * WIDTH + col - 1] & 0xff) + (buffer[row * WIDTH + col + 1] & 0xff) + (buffer[(row + 1) * WIDTH + col] & 0xff) + (buffer[(row + 1) * WIDTH + col - 1] & 0xff) + (buffer[(row + 1) * WIDTH + col + 1] & 0xff) + (buffer[(row - 1) * WIDTH + col] & 0xff) + (buffer[(row - 1) * WIDTH + col - 1] & 0xff) + (buffer[(row - 1) * WIDTH + col + 1] & 0xff)) / 9;
                rgbArray[row * WIDTH + col] = (255 << 24) | (r << 16) | (g << 8) | b;
            }
        }
        image.setRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);
    }

    public static void applyPencilEffect(BufferedImage image, double sharpness, int brightness) {
        final int WIDTH = image.getWidth();
        final int HEIGHT = image.getHeight();
        int[] rgbArray = null;
        int[] buffer = null;
        buffer = image.getRGB(0, 0, WIDTH, HEIGHT, buffer, 0, WIDTH);
        rgbArray = image.getRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);
        for (int row = 1; row < HEIGHT - 1; row++) {
            for (int col = 1; col < WIDTH - 1; col++) {
                int r = Math.max(0, Math.min(255, (int) (255 + brightness + sharpness * (4 * ((buffer[row * WIDTH + col] >> 16) & 0xff) - ((buffer[row * WIDTH + col - 1] >> 16) & 0xff) - ((buffer[row * WIDTH + col + 1] >> 16) & 0xff) - ((buffer[(row - 1) * WIDTH + col] >> 16) & 0xff) - ((buffer[(row + 1) * WIDTH + col] >> 16) & 0xff)))));
                int g = Math.max(0, Math.min(255, (int) (255 + brightness + sharpness * (4 * ((buffer[row * WIDTH + col] >> 8) & 0xff) - ((buffer[row * WIDTH + col - 1] >> 8) & 0xff) - ((buffer[row * WIDTH + col + 1] >> 8) & 0xff) - ((buffer[(row - 1) * WIDTH + col] >> 8) & 0xff) - ((buffer[(row + 1) * WIDTH + col] >> 8) & 0xff)))));
                int b = Math.max(0, Math.min(255, (int) (255 + brightness + sharpness * (4 * (buffer[row * WIDTH + col] & 0xff) - (buffer[row * WIDTH + col - 1] & 0xff) - (buffer[row * WIDTH + col + 1] & 0xff) - (buffer[(row - 1) * WIDTH + col] & 0xff) - (buffer[(row + 1) * WIDTH + col] & 0xff)))));
                rgbArray[row * WIDTH + col] = (255 << 24) | (r << 16) | (g << 8) | b;
            }
        }
        image.setRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);
    }

    public static void loadLookAndFeel() {
        try {
            UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceBusinessBlackSteelLookAndFeel");
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
            }
        }
    }

    public static Color nearestColor(int c) {
        int r = (c >> 16) & 0xFF;
        int g = (c >> 8) & 0xFF;
        int b = c & 0xFF;
        r = r / 8 * 8;
        g = g / 8 * 8;
        b = b / 8 * 8;
        return new Color(r, g, b);
    }

    public static Color nearestCommonColor(BufferedImage image) {
        HashMap<Color, Integer> countMap = new HashMap<Color, Integer>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = nearestColor(image.getRGB(x, y));
                if (countMap.containsKey(c)) {
                    countMap.put(c, countMap.get(c) + 1);
                } else {
                    countMap.put(c, 1);
                }
            }
        }
        Color color = null;
        int count = 0;
        for (Color c : countMap.keySet()) {
            if (countMap.get(c) > count) {
                count = countMap.get(c);
                color = c;
            }
        }
        return color;
    }

    public static int colorDistance(Color c, int c_code) {
        return Math.abs(c.getRed() - ((c_code >> 16) & 0xFF)) + Math.abs(c.getGreen() - ((c_code >> 8) & 0xFF)) + Math.abs(c.getBlue() - (c_code & 0xFF));
    }

    public static double[] binImage2InputVec(int[][] binImage, int ngrid) {
        double[] vec = new double[ngrid * ngrid];
        int totalCount = 0;
        for (int i = 0; i < binImage.length; i++) {
            for (int j = 0; j < binImage[0].length; j++) {
                totalCount += binImage[i][j];
            }
        }
        for (int i = 0; i < ngrid; i++) {
            for (int j = 0; j < ngrid; j++) {
                vec[i * ngrid + j] = 0;
                double startRowD = ((double) binImage.length * i) / ngrid;
                double endRowD = ((double) binImage.length * (i + 1)) / ngrid;
                int startRow = (int) Math.floor(startRowD);
                int endRow = (int) Math.ceil(endRowD);
                double startColD = ((double) binImage[0].length * j) / ngrid;
                double endColD = ((double) binImage[0].length * (j + 1)) / ngrid;
                int startCol = (int) Math.floor(startColD);
                int endCol = (int) Math.ceil(endColD);
                for (int row = startRow; row < Math.min(endRow, binImage.length); row++) {
                    for (int col = startCol; col < Math.min(endCol, binImage[0].length); col++) {
                        double yRatio = 1;
                        if (row < startRowD) yRatio -= startRowD - row;
                        if (row > endRowD - 1) yRatio -= row - endRowD + 1;
                        double xRatio = 1;
                        if (col < startColD) xRatio -= startColD - col;
                        if (col > endColD - 1) xRatio -= col - endColD + 1;
                        if (binImage[row][col] == 1) vec[i * ngrid + j] += xRatio * yRatio;
                    }
                }
            }
        }
        double mySum = 0;
        for (int i = 0; i < vec.length; i++) {
            mySum += vec[i];
        }
        if (Math.abs(mySum - totalCount) > 0.1) {
            System.out.println("error in Utility.binImage2InputVec() " + mySum + " " + totalCount);
        }
        for (int i = 0; i < vec.length; i++) {
            vec[i] /= totalCount;
        }
        return vec;
    }

    public static int[][] getBinImage(BufferedImage image) {
        Color bgColor = OCRUtility.nearestCommonColor(image);
        int[][] distanceMap = new int[image.getHeight()][image.getWidth()];
        int[] distanceHistogram = new int[768];
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                distanceMap[row][col] = OCRUtility.colorDistance(bgColor, image.getRGB(col, row));
                distanceHistogram[distanceMap[row][col]]++;
            }
        }
        int highestHistogramPosition = 0;
        int histogramValue = 0;
        for (int i = 1; i < 768; i++) {
            if (distanceHistogram[i] > histogramValue) {
                highestHistogramPosition = i;
                histogramValue = distanceHistogram[highestHistogramPosition];
            }
        }
        int higherHistogramPosition = 0;
        histogramValue = 0;
        for (int i = highestHistogramPosition * 8; i < 768; i++) {
            if (distanceHistogram[i] > histogramValue && distanceHistogram[i] < distanceHistogram[highestHistogramPosition]) {
                higherHistogramPosition = i;
                histogramValue = distanceHistogram[higherHistogramPosition];
            }
        }
        int threshold = (highestHistogramPosition + higherHistogramPosition) / 2;
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                if (distanceMap[row][col] < threshold) {
                    distanceMap[row][col] = 0;
                } else {
                    distanceMap[row][col] = 1;
                }
            }
        }
        int yBegin = 0;
        lab1: for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (distanceMap[i][j] == 1) {
                    yBegin = i;
                    break lab1;
                }
            }
        }
        int yEnd = 0;
        lab1a: for (int i = image.getHeight() - 1; i >= 0; i--) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (distanceMap[i][j] == 1) {
                    yEnd = i;
                    break lab1a;
                }
            }
        }
        int xBegin = 0;
        lab2: for (int j = 0; j < image.getWidth(); j++) {
            for (int i = 0; i < image.getHeight(); i++) {
                if (distanceMap[i][j] == 1) {
                    xBegin = j;
                    break lab2;
                }
            }
        }
        int xEnd = 0;
        lab2a: for (int j = image.getWidth() - 1; j >= 0; j--) {
            for (int i = 0; i < image.getHeight(); i++) {
                if (distanceMap[i][j] == 1) {
                    xEnd = j;
                    break lab2a;
                }
            }
        }
        int[][] binImage = new int[yEnd - yBegin + 1][xEnd - xBegin + 1];
        for (int i = 0; i < binImage.length; i++) {
            for (int j = 0; j < binImage[0].length; j++) {
                binImage[i][j] = distanceMap[i + yBegin][j + xBegin];
            }
        }
        return binImage;
    }
}
