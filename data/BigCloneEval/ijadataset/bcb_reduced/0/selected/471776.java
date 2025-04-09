package nij.qrfrp.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Class implementing methods for manipulating images.
 * 
 * <p>This software is distributed under the GNU General Public License, version 3.0.
 * A copy of the license should have been distributed along with the code. If not, 
 * please visit http://www.fsf.org to view the terms of the license.</p>
 */
public class ImageUtil {

    /**
	 * Overlays two images.   
	 * <br><br>
	 * TODO: This works okay; need to look into another way to effectively accomplish this.
	 * 
	 * @param inputImage1 - The first input image.
	 * @param inputImage2 - The second input image to overlay.
	 * @return The image resulting from overlaying the two input images.
	 */
    public static BufferedImage overlayTwoGrayImages(BufferedImage inputImage1, BufferedImage inputImage2) {
        int w1 = inputImage1.getWidth();
        int w2 = inputImage2.getWidth();
        int h1 = inputImage1.getHeight();
        int h2 = inputImage2.getHeight();
        if (w1 != w2 || h1 != h2) {
            System.out.println("inputImage1 dimensions must = inputImage2 dimensions.");
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        BufferedImage outputImage = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_RGB);
        WritableRaster outputWRaster = outputImage.getRaster();
        Raster inputImage1Raster = inputImage1.getData();
        Raster inputImage2Raster = inputImage2.getData();
        for (int i = 0; i < h1; i++) {
            for (int j = 0; j < w1; j++) {
                int R = inputImage1Raster.getSample(j, i, 0);
                int G = inputImage2Raster.getSample(j, i, 0);
                outputWRaster.setSample(j, i, 0, ((R)));
                outputWRaster.setSample(j, i, 2, ((G)));
                outputWRaster.setSample(j, i, 1, (((G + R) / 2)));
            }
        }
        return outputImage;
    }

    /**
	 * Creates a histogram for a given image.
	 * 
	 * @param inputImage - The image for which a histogram will be created.
	 * @return 2D array of histogram data formatted as <i>[band][intensity]</i>.
	 */
    public static int[][] getHistogram(BufferedImage inputImage) {
        return getHistogram(inputImage, false, null);
    }

    /**
	 * Creates a histogram for a given image.
	 * 
	 * @param inputImage - The image for which a histogram will be created.
	 * @param printData - Boolean value, if true, prints the data to the console.
	 * @return 2D array of histogram data formatted as <i>[band][intensity]</i>.
	 */
    public static int[][] getHistogram(BufferedImage inputImage, boolean printData) {
        return getHistogram(inputImage, printData, null);
    }

    /**
	 * Creates a histogram for a given image.
	 * 
	 * @param inputImage - The image for which a histogram will be created.
	 * @param printData - Boolean value, if true, prints the data to the console.
	 * @param saveDir - The directory to save the diagnostic images.
	 * @return 2D array of histogram data formatted as <i>[band][intensity]</i>.
	 */
    public static int[][] getHistogram(BufferedImage inputImage, boolean printData, File saveDir) {
        Raster inputRaster = inputImage.getData();
        int numBands = inputRaster.getNumBands();
        int[][] histogram = new int[numBands][256];
        for (int i = 0; i < numBands; i++) {
            for (int j = 0; j < 256; j++) {
                histogram[i][j] = 0;
            }
        }
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                for (int b = 0; b < numBands; b++) {
                    int temp = inputRaster.getSample(j, i, b);
                    histogram[b][temp]++;
                }
            }
        }
        if (printData) {
            System.out.println("Histogram.");
            for (int j = 0; j < 256; j++) {
                System.out.print("[" + j + "] ");
                for (int i = 0; i < numBands; i++) {
                    System.out.print("B" + i + ":" + histogram[i][j] + " ");
                }
                System.out.println();
            }
        }
        if (saveDir != null && saveDir.exists()) {
            for (int b = 0; b < numBands; b++) {
                BufferedImage histogramImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
                WritableRaster histogramWr = histogramImage.getRaster();
                int max = 0;
                for (int i = 0; i < 256; i++) {
                    if (histogram[b][i] > max) max = histogram[b][i];
                }
                for (int i = 0; i < 256; i++) {
                    int upper = (histogram[b][i] * 255) / max;
                    for (int j = 255; j > (255 - upper); j--) {
                        histogramWr.setSample(i, j, b, 255);
                        if (numBands == 1) {
                            histogramWr.setSample(i, j, 1, 255);
                            histogramWr.setSample(i, j, 2, 255);
                        }
                    }
                }
                try {
                    ImageIO.write(histogramImage, "bmp", new File(saveDir + "\\histogram_" + b + ".bmp"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return histogram;
    }

    /**
	 * Converts an 8-bit gray BufferedImage to a 24-bit RGB BufferedImage
	 * 
	 * @param inputImage 8-bit gray image to be converted.
	 * @return 24-bit RGB copy of the input image.
	 */
    public static BufferedImage convertGrayToRGB(BufferedImage inputImage) {
        Raster inputRaster = inputImage.getData();
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster outputRaster = outputImage.getRaster();
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                int bandInc = 0;
                int band = 0;
                if (inputRaster.getNumBands() > 1) bandInc++;
                outputRaster.setSample(j, i, 0, inputRaster.getSample(j, i, band));
                band += bandInc;
                outputRaster.setSample(j, i, 1, inputRaster.getSample(j, i, band));
                band += bandInc;
                outputRaster.setSample(j, i, 2, inputRaster.getSample(j, i, band));
            }
        }
        return outputImage;
    }

    /**
	 * Converts a 24-bit RGB BufferedImage to an 8-bit gray BufferedImage.
	 * 
	 * @param inputImage - 24-bit RGB image to be converted.
	 * @return 8-bit gray image.
	 */
    public static BufferedImage convertRGBToGray(BufferedImage inputImage) {
        Raster inputRaster = inputImage.getData();
        if (inputRaster.getNumBands() == 1) return inputImage;
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster outputRaster = outputImage.getRaster();
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                double R = inputRaster.getSample(j, i, 0);
                double G = inputRaster.getSample(j, i, 1);
                double B = inputRaster.getSample(j, i, 2);
                double Y = 0.3 * R + 0.59 * G + 0.11 * B;
                outputRaster.setSample(j, i, 0, Y);
            }
        }
        return outputImage;
    }

    /**
	 * Selects an image from a directory.
	 * <br><br>
	 * TODO: This was created to avoid having to type in the long file names; not sure if this
	 * will be useful or necessary in the long run.
	 * 
	 * @param directory - The directory to select an image from.
	 * @param imageNumber - The image number.
	 * @return The image from the directory corresponding to the input image number.
	 * @throws IOException 
	 */
    public static BufferedImage selectImage(File directory, int imageNumber) throws IOException {
        BufferedImage selectedImage = null;
        if (!directory.isDirectory()) {
            System.out.println("Directory invalid.");
            return selectedImage;
        }
        File[] directory_contents = directory.listFiles();
        int numFiles = directory_contents.length;
        if (imageNumber > numFiles) {
            System.out.println("Invalid image number.");
            return selectedImage;
        }
        if (!directory_contents[imageNumber].getName().contains(".bmp") && !directory_contents[imageNumber].getName().contains(".jpg")) {
            System.out.println("File is not a valid image.");
            return selectedImage;
        }
        selectedImage = ImageIO.read(directory_contents[imageNumber]);
        return selectedImage;
    }

    /**
	 * Pads an image on the right and bottom so that it can be evenly broken into blocks 
	 * of size <b>blockSize</b>.
	 * 
	 * @param inputImage - The image to be padded.
	 * @param blockSize - The desired blocksize the image should be divisible by. 
	 * @return The padded image.
	 * @throws IOException
	 */
    public static BufferedImage padImage(BufferedImage inputImage, int blockSize) throws IOException {
        Raster inputRaster = inputImage.getData();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        while (width % blockSize != 0) width++;
        while (height % blockSize != 0) height++;
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster outputWRaster = outputImage.getRaster();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                try {
                    outputWRaster.setSample(j, i, 0, inputRaster.getSample(j, i, 0));
                } catch (Exception ee) {
                    outputWRaster.setSample(j, i, 0, 128);
                }
            }
        }
        return outputImage;
    }

    /**
     * Scales an image using AffineTransform.
     * 
     * @param inputImage - The image to be scaled.  
     * @param scale - The amount to scale an image as a decimal (0.5 returns an image
     * that is 1/2 the size of the original, etc.).
     * @return The scaled image.
     */
    public static BufferedImage getScaledImage(BufferedImage inputImage, double scale) {
        int w = (int) (scale * inputImage.getWidth());
        int h = (int) (scale * inputImage.getHeight());
        int type = inputImage.getType();
        BufferedImage out = new BufferedImage(w, h, type);
        Graphics2D g2 = out.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        g2.drawImage(inputImage, at, null);
        g2.dispose();
        return out;
    }

    /**
     * Combines two images.  
     *
     * @param inputImage1 - The first input image in the combination.
     * @param inputImage2 - The second input image in the combination.
     * @param method - The method to use in the combinations:  <br>
     * <li> 0 = compare corresponding pixels between images and take the least value. Corresponds to public method <b>takeMinPixelBetweenImages(...)</b></li>
     * <li> 1 = compare corresponding pixels between images and take the greatest value. Corresponds to public method <b>takeMaxPixelBetweenImages(...)</b></li>
     * <li> 2 = take logical AND of corresponding pixels. Corresponds to public method <b>logicalAND(...)</b></li>
     * <li> 3 = take logical OR of corresponding pixels. Corresponds to public method <b>logicalOR(...)</b></li>
     * <li> 4 = take logical XOR of corresponding pixels. Corresponds to public method <b>logicalNOR(...)</b></li>
     * <li> 5 = take average of corresponding pixels. Corresponds to public method <b>averageImages(...)</b></li>
     * <li> 6 = take difference between corresponding pixels. Corresponds to public method <b>subtractImages(...)</b></li>
     * @return The combined image.
     * @throws IOException
     */
    private static BufferedImage combineTwoImages(BufferedImage inputImage1, BufferedImage inputImage2, int method) throws IOException {
        BufferedImage outputImage = new BufferedImage(inputImage1.getWidth(), inputImage1.getHeight(), inputImage1.getType());
        WritableRaster outputRaster = outputImage.getRaster();
        Raster inputRaster1 = inputImage1.getData();
        Raster inputRaster2 = inputImage2.getData();
        if (inputImage1.getWidth() != inputImage2.getWidth() || inputImage1.getHeight() != inputImage2.getHeight()) throw new IOException("Images must be same size to combine.");
        if (inputRaster1.getNumBands() != inputRaster2.getNumBands()) throw new IOException("# bands in image1 must = # bands in image 2.");
        for (int i = 0; i < inputImage1.getHeight(); i++) {
            for (int j = 0; j < inputImage1.getWidth(); j++) {
                for (int b = 0; b < inputRaster1.getNumBands(); b++) {
                    int outPixel = 0;
                    int pixel1 = inputRaster1.getSample(j, i, b);
                    int pixel2 = inputRaster2.getSample(j, i, b);
                    switch(method) {
                        case 0:
                            if (pixel1 < pixel2) outPixel = pixel1; else outPixel = pixel2;
                            break;
                        case 1:
                            if (pixel1 > pixel2) outPixel = pixel1; else outPixel = pixel2;
                            break;
                        case 2:
                            outPixel = pixel1 & pixel2;
                            break;
                        case 3:
                            outPixel = pixel1 | pixel2;
                            break;
                        case 4:
                            outPixel = pixel1 ^ pixel2;
                            break;
                        case 5:
                            outPixel = (pixel1 + pixel2) / 2;
                            break;
                        case 6:
                            outPixel = Math.abs(pixel1 - pixel2);
                            break;
                    }
                    outputRaster.setSample(j, i, b, outPixel);
                }
            }
        }
        return outputImage;
    }

    public static BufferedImage takeMinPixelBetweenImages(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 0);
    }

    public static BufferedImage takeMaxPixelBetweenImages(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 1);
    }

    public static BufferedImage logicalAND(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 2);
    }

    public static BufferedImage logicalOR(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 3);
    }

    public static BufferedImage logicalXOR(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 4);
    }

    public static BufferedImage averageImages(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 5);
    }

    public static BufferedImage subtractImages(BufferedImage inputImage1, BufferedImage inputImage2) throws IOException {
        return combineTwoImages(inputImage1, inputImage2, 6);
    }

    /**
	 * This method masks a grayscale image.  Pixels in the mask with the value <b>maskValue</b>
	 * are masked with red on <b>inputImage</b>.
	 * 
	 * @param inputImage - The input image to be masked.
	 * @param mask - The mask to use.
	 * @param maskValue	- The pixel value in the mask that represent masked data.
	 * @return The image with the mask applied.
	 * @throws IOException
	 */
    public static BufferedImage mask(BufferedImage inputImage, BufferedImage mask, int maskValue) throws IOException {
        BufferedImage maskedImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster maskedImageWr = maskedImage.getRaster();
        Raster inputRaster = inputImage.getData();
        Raster maskR = mask.getData();
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                try {
                    if (maskR.getSample(j, i, 0) == maskValue) maskedImageWr.setSample(j, i, 0, 255); else {
                        maskedImageWr.setSample(j, i, 0, (inputRaster.getSample(j, i, 0)));
                        maskedImageWr.setSample(j, i, 1, (inputRaster.getSample(j, i, 0)));
                        maskedImageWr.setSample(j, i, 2, (inputRaster.getSample(j, i, 0)));
                    }
                } catch (Exception ee) {
                }
            }
        }
        return maskedImage;
    }

    /**
	 * This method masks a grayscale image.  Pixels in the mask with the value <b>maskValue</b>
	 * are masked with red on <b>inputImage</b>.
	 * 
	 * @param inputImage - The input image to be masked.
	 * @param mask - The mask to use.
	 * @param maskValue	- The pixel value in the mask that represent masked data.
	 * @return The image with the mask applied.
	 * @throws IOException
	 */
    public static BufferedImage applyIntMask(BufferedImage inputImage, int[][] mask, int maskValue) {
        Raster inputRaster = inputImage.getData();
        BufferedImage mBi = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        WritableRaster wr = mBi.getRaster();
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                int maskMask = 0;
                if (mask[j][i] == maskValue) maskMask = 0x00000000; else maskMask = 0xFFFFFFFF;
                wr.setSample(j, i, 0, (inputRaster.getSample(j, i, 0)) & maskMask);
            }
        }
        return mBi;
    }

    /**
	 * Sums the intensities of each column in an image.
	 * <br><br>
	 * TODO: Only works with a grayscale image.  
	 * 
	 * @param inputImage - The image for which the columns will be summed.
	 * @return 1D int array containing the sums of each column.  
	 */
    public static int[] sumImageColumns(BufferedImage inputImage) {
        Raster inputRaster = inputImage.getData();
        int[] sum = new int[inputImage.getWidth()];
        for (int i = 0; i < inputImage.getWidth(); i++) {
            sum[i] = 0;
            for (int j = 0; j < inputImage.getHeight(); j++) {
                sum[i] += inputRaster.getSample(i, j, 0);
            }
        }
        return sum;
    }

    /**
	 * Loads a BufferedImage into a 2D double array.
	 * 
	 * @param inputImage - The image to load into the 2D double array.
	 * @return 2D array containing the image data.
	 */
    public static double[][] loadInto2dDouble(BufferedImage inputImage) {
        double[][] out = new double[inputImage.getWidth()][inputImage.getHeight()];
        Raster inputRaster = inputImage.getData();
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                out[j][i] = inputRaster.getSample(j, i, 0);
            }
        }
        return out;
    }

    /**
	 * Loads a BufferedImage from a 2D int array.
	 * 
	 * @param d - The image data to load into the BufferedImage.
	 * @param type - The type of BufferedImage to load the data into.
	 * @param normalize - Boolean; if true, the data is normalized before being loaded into the image.
	 * @return A BufferedImage with specified data loaded. 
	 */
    public static BufferedImage loadFrom2dInt(int[][] d, int type, boolean normalize) {
        int width = d.length;
        int height = d[0].length;
        BufferedImage outputImage = new BufferedImage(width, height, type);
        WritableRaster outputWRaster = outputImage.getRaster();
        double min = 0;
        double max = 0;
        if (normalize) {
            min = MathUtil.min(d);
            max = MathUtil.max(d);
        }
        int rgb = 0;
        for (int b = 0; b < outputWRaster.getNumBands(); b++) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (normalize) rgb = (int) ((d[j][i] + Math.abs(min)) * (255 / (Math.abs(min) + max))); else rgb = (d[j][i]);
                    outputWRaster.setSample(j, i, b, rgb);
                }
            }
        }
        return outputImage;
    }

    /**
	 * Loads a BufferedImage from a 2D double array.
	 * 
	 * @param d - The image data to load into the BufferedImage.
	 * @param type - The type of BufferedImage to load the data into.
	 * @param normalize - Boolean; if true, the data is normalized before being loaded into the image.
	 * @return A BufferedImage with specified data loaded. 
	 */
    public static BufferedImage loadFrom2dDouble(double[][] d, int type, boolean normalize) {
        int width = d.length;
        int height = d[0].length;
        BufferedImage outputImage = new BufferedImage(width, height, type);
        WritableRaster outputWRaster = outputImage.getRaster();
        double min = 0;
        double max = 0;
        if (normalize) {
            min = MathUtil.min(d);
            max = MathUtil.max(d);
        }
        int rgb = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (normalize) rgb = (int) ((d[j][i] + Math.abs(min)) * (255 / (Math.abs(min) + max))); else rgb = (int) (d[j][i]);
                outputWRaster.setSample(j, i, 0, rgb);
                try {
                    outputWRaster.setSample(j, i, 1, rgb);
                    outputWRaster.setSample(j, i, 2, rgb);
                } catch (Exception ee) {
                }
            }
        }
        return outputImage;
    }

    /**
	 * Creates a copy of the specified image.
	 * 
	 * @param inputImage - The image to be copied.
	 * @return A copy of the image.
	 */
    public static BufferedImage createCopy(BufferedImage inputImage) {
        BufferedImage copy = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        Raster inputRaster = inputImage.getData();
        copy.setData(inputRaster);
        return copy;
    }

    /**
	 * Normalizes an image to have a specified mean and variance.  
	 * 
	 * @param inputImage - The image to be normalized.
	 * @param reqMean - The specified mean.
	 * @param reqVar - The specified variance.
	 * @return The normalized image.
	 */
    public static double[][] normalize(BufferedImage inputImage, double reqMean, double reqVar) {
        boolean debug = false;
        double[][] d_bi = ImageUtil.loadInto2dDouble(inputImage);
        double[][] normIm = new double[inputImage.getWidth()][inputImage.getHeight()];
        double mean = MathUtil.mean(d_bi);
        double std = MathUtil.std(d_bi);
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                normIm[j][i] = (((d_bi[j][i] - mean) / std) * Math.sqrt(reqVar)) + reqMean;
                if (debug) System.out.print(normIm[j][i] + ",");
            }
            if (debug) System.out.println();
        }
        return normIm;
    }

    /**
	 * Normalizes an image such that its intensites are in the 0-255 range.
	 * 
	 * @param inputImage - The image to be normalized.
	 * @return - Normalized image.
	 */
    public static double[][] normalize(BufferedImage inputImage) {
        boolean debug = false;
        double[][] d_bi = ImageUtil.loadInto2dDouble(inputImage);
        double[][] normIm = new double[inputImage.getWidth()][inputImage.getHeight()];
        double min = MathUtil.min(d_bi);
        double max = MathUtil.max(d_bi);
        for (int i = 0; i < inputImage.getHeight(); i++) {
            for (int j = 0; j < inputImage.getWidth(); j++) {
                normIm[j][i] = (d_bi[j][i] - min) / max;
                if (debug) System.out.print(normIm[j][i] + ",");
            }
            if (debug) System.out.println();
        }
        return normIm;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage inputImage1 = ImageIO.read(new File(".\\test\\inputImage1.bmp"));
        BufferedImage inputImage2 = ImageIO.read(new File(".\\test\\inputImage2.bmp"));
        BufferedImage outputImage = ImageUtil.overlayTwoGrayImages(inputImage1, inputImage2);
        ImageIO.write(outputImage, "bmp", new File(".\\test\\overlay.bmp"));
    }
}
