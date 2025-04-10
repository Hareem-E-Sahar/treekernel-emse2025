package ij.plugin.filter;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import java.awt.Rectangle;

/** This plug-in filter uses convolution with a Gaussian function for smoothing.
 * 'Radius' means the radius of decay to exp(-0.5) ~ 61%, i.e. the standard
 * deviation sigma of the Gaussian (this is the same as in Photoshop, but
 * different from the 'Gaussian Blur' in ImageJ versions before 1.38u, where
 * a value 2.5 times as much had to be entered.
 * - Like all convolution operations in ImageJ, it assumes that out-of-image
 * pixels have a value equal to the nearest edge pixel. This gives higher
 * weight to edge pixels than pixels inside the image, and higher weight
 * to corner pixels than non-corner pixels at the edge. Thus, when smoothing
 * with very high blur radius, the output will be dominated by the edge
 * pixels and especially the corner pixels (in the extreme case, with
 * a blur radius of e.g. 1e20, the image will be raplaced by the average
 * of the four corner pixels).
 * - For increased speed, except for small blur radii, the lines (rows or
 * columns of the image) are downscaled before convolution and upscaled
 * to their original length thereafter.
 * 
 * Version 03-Jun-2007 M. Schmid with preview, progressBar stack-aware,
 * snapshot via snapshot flag; restricted range for resetOutOfRoi
 * 
 * 20-Feb-2010 S. Saalfeld inner multi-threading
 *
 */
public class GaussianBlur implements ExtendedPlugInFilter, DialogListener {

    /** the standard deviation of the Gaussian*/
    private static double sigma = 2.0;

    /** whether sigma is given in units corresponding to the pixel scale (not pixels)*/
    private static boolean sigmaScaled = false;

    /** The flags specifying the capabilities and needs */
    private int flags = DOES_ALL | SUPPORTS_MASKING | KEEP_PREVIEW;

    private ImagePlus imp;

    private boolean hasScale = false;

    private int nPasses = 1;

    private int nChannels = 1;

    private int pass;

    /** Method to return types supported
     * @param arg unused
     * @param imp The ImagePlus, used to get the spatial calibration
     * @return Code describing supported formats etc.
     * (see ij.plugin.filter.PlugInFilter & ExtendedPlugInFilter)
     */
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        if (imp != null && imp.getRoi() != null) {
            Rectangle roiRect = imp.getRoi().getBoundingRect();
            if (roiRect.y > 0 || roiRect.y + roiRect.height < imp.getDimensions()[1]) flags |= SNAPSHOT;
        }
        return flags;
    }

    /** Ask the user for the parameters
     */
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        String options = Macro.getOptions();
        boolean oldMacro = false;
        nChannels = imp.getProcessor().getNChannels();
        if (options != null) {
            if (options.indexOf("radius=") >= 0) {
                oldMacro = true;
                Macro.setOptions(options.replaceAll("radius=", "sigma="));
            }
        }
        GenericDialog gd = new GenericDialog(command);
        sigma = Math.abs(sigma);
        gd.addNumericField("Sigma (Radius)", sigma, 2);
        if (imp.getCalibration() != null && !imp.getCalibration().getUnits().equals("pixels")) {
            hasScale = true;
            gd.addCheckbox("Scaled Units (" + imp.getCalibration().getUnits() + ")", sigmaScaled);
        } else sigmaScaled = false;
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        gd.showDialog();
        if (gd.wasCanceled()) return DONE;
        if (oldMacro) sigma /= 2.5;
        IJ.register(this.getClass());
        return IJ.setupDialog(imp, flags);
    }

    /** Listener to modifications of the input fields of the dialog */
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        sigma = gd.getNextNumber();
        if (sigma < 0 || gd.invalidNumber()) return false;
        if (hasScale) sigmaScaled = gd.getNextBoolean();
        return true;
    }

    /** Set the number of passes of the blur1Direction method. If called by the
     *  PlugInFilterRunner of ImageJ, an ImagePlus is known and conversion of RGB images
     *  to float as well as the two filter directions are taken into account.
     *  Otherwise, the caller should set nPasses to the number of 1-dimensional
     *  filter operations required.
     */
    public void setNPasses(int nPasses) {
        this.nPasses = 2 * nChannels * nPasses;
        pass = 0;
    }

    /** This method is invoked for each slice during execution
     * @param ip The image subject to filtering. It must have a valid snapshot if
     * the height of the roi is less than the full image height.
     */
    public void run(ImageProcessor ip) {
        double sigmaX = sigmaScaled ? sigma / imp.getCalibration().pixelWidth : sigma;
        double sigmaY = sigmaScaled ? sigma / imp.getCalibration().pixelHeight : sigma;
        double accuracy = (ip instanceof ByteProcessor || ip instanceof ColorProcessor) ? 0.002 : 0.0002;
        Rectangle roi = ip.getRoi();
        blurGaussian(ip, sigmaX, sigmaY, accuracy);
    }

    /** Gaussian Filtering of an ImageProcessor. This method is for compatibility with the
     *  previous code (before 1.38r) and uses a low-accuracy kernel, only slightly better
     *  than the previous ImageJ code */
    public boolean blur(ImageProcessor ip, double radius) {
        Rectangle roi = ip.getRoi();
        if (roi.height != ip.getHeight() && ip.getMask() == null) ip.snapshot();
        blurGaussian(ip, 0.4 * radius, 0.4 * radius, 0.01);
        return true;
    }

    /** Gaussian Filtering of an ImageProcessor. If filtering is not applied to the
     *  full image height, the ImageProcessor must have a valid snapshot.
     * @param ip       The ImageProcessor to be filtered.
     * @param sigmaX   Standard deviation of the Gaussian in x direction (pixels)
     * @param sigmaY   Standard deviation of the Gaussian in y direction (pixels)
     * @param accuracy Accuracy of kernel, should not be above 0.02. Better (lower)
     *                 accuracy needs slightly more computing time.
     */
    public void blurGaussian(ImageProcessor ip, double sigmaX, double sigmaY, double accuracy) {
        if (nPasses <= 1) nPasses = ip.getNChannels() * (sigmaX > 0 && sigmaY > 0 ? 2 : 1);
        FloatProcessor fp = null;
        for (int i = 0; i < ip.getNChannels(); i++) {
            fp = ip.toFloat(i, fp);
            if (Thread.currentThread().isInterrupted()) return;
            blurFloat(fp, sigmaX, sigmaY, accuracy);
            if (Thread.currentThread().isInterrupted()) return;
            ip.setPixels(i, fp);
        }
        if (ip.getRoi().height != ip.getHeight() && sigmaX > 0 && sigmaY > 0) resetOutOfRoi(ip, (int) Math.ceil(5 * sigmaY));
        return;
    }

    /** Gaussian Filtering of a FloatProcessor. This method does NOT include
     *  resetOutOfRoi(ip), i.e., pixels above and below the roi rectangle will
     *  be also subject to filtering in x direction and must be restored
     *  afterwards (unless the full image height is processed).
     * @param ip        The FloatProcessor to be filtered.
     * @param sigmaX    Standard deviation of the Gaussian in x direction (pixels)
     * @param sigmaY    Standard deviation of the Gaussian in y direction (pixels)
     * @param accuracy  Accuracy of kernel, should not be above 0.02. Better (lower)
     *                  accuracy needs slightly more computing time.
     */
    public void blurFloat(FloatProcessor ip, double sigmaX, double sigmaY, double accuracy) {
        if (sigmaX > 0) blur1Direction(ip, sigmaX, accuracy, true, (int) Math.ceil(5 * sigmaY));
        if (Thread.currentThread().isInterrupted()) return;
        if (sigmaY > 0) blur1Direction(ip, sigmaY, accuracy, false, 0);
        return;
    }

    /** Blur an image in one direction (x or y) by a Gaussian.
     * @param ip        The Image with the original data where also the result will be stored
     * @param sigma     Standard deviation of the Gaussian
     * @param accuracy  Accuracy of kernel, should not be > 0.02
     * @param xDirection True for bluring in x direction, false for y direction
     * @param extraLines Number of lines (parallel to the blurring direction) 
     *                  below and above the roi bounds that should be processed.
     */
    public void blur1Direction(final FloatProcessor ip, final double sigma, final double accuracy, final boolean xDirection, final int extraLines) {
        final int UPSCALE_K_RADIUS = 2;
        final double MIN_DOWNSCALED_SIGMA = 4.;
        final float[] pixels = (float[]) ip.getPixels();
        final int width = ip.getWidth();
        final int height = ip.getHeight();
        final Rectangle roi = ip.getRoi();
        final int length = xDirection ? width : height;
        final int pointInc = xDirection ? 1 : width;
        final int lineInc = xDirection ? width : 1;
        final int lineFromA = (xDirection ? roi.y : roi.x) - extraLines;
        final int lineFrom;
        if (lineFromA < 0) lineFrom = 0; else lineFrom = lineFromA;
        final int lineToA = (xDirection ? roi.y + roi.height : roi.x + roi.width) + extraLines;
        final int lineTo;
        if (lineToA > (xDirection ? height : width)) lineTo = (xDirection ? height : width); else lineTo = lineToA;
        final int writeFrom = xDirection ? roi.x : roi.y;
        final int writeTo = xDirection ? roi.x + roi.width : roi.y + roi.height;
        final int inc = Math.max((lineTo - lineFrom) / (100 / (nPasses > 0 ? nPasses : 1) + 1), 20);
        pass++;
        if (pass > nPasses) pass = 1;
        final int numThreads = Math.min(Prefs.getThreads(), lineTo - lineFrom);
        final Thread[] lineThreads = new Thread[numThreads];
        final boolean doDownscaling = sigma > 2 * MIN_DOWNSCALED_SIGMA + 0.5;
        final int reduceBy = doDownscaling ? Math.min((int) Math.floor(sigma / MIN_DOWNSCALED_SIGMA), length) : 1;
        final double sigmaGauss = doDownscaling ? Math.sqrt(sigma * sigma / (reduceBy * reduceBy) - 1. / 3. - 1. / 4.) : sigma;
        final int maxLength = doDownscaling ? (length + reduceBy - 1) / reduceBy + 2 * (UPSCALE_K_RADIUS + 1) : length;
        final float[][] gaussKernel = makeGaussianKernel(sigmaGauss, accuracy, maxLength);
        final int kRadius = gaussKernel[0].length * reduceBy;
        final int readFrom = (writeFrom - kRadius < 0) ? 0 : writeFrom - kRadius;
        final int readTo = (writeTo + kRadius > length) ? length : writeTo + kRadius;
        final int newLength = doDownscaling ? (readTo - readFrom + reduceBy - 1) / reduceBy + 2 * (UPSCALE_K_RADIUS + 1) : length;
        final int unscaled0 = readFrom - (UPSCALE_K_RADIUS + 1) * reduceBy;
        final float[] downscaleKernel = doDownscaling ? makeDownscaleKernel(reduceBy) : null;
        final float[] upscaleKernel = doDownscaling ? makeUpscaleKernel(reduceBy) : null;
        for (int t = 0; t < numThreads; ++t) {
            final int ti = t;
            final float[] cache1 = new float[newLength];
            final float[] cache2 = doDownscaling ? new float[newLength] : null;
            final Thread thread = new Thread(new Runnable() {

                public final void run() {
                    long lastTime = System.currentTimeMillis();
                    boolean canShowProgress = Thread.currentThread() == lineThreads[0];
                    int pixel0 = (lineFrom + ti) * lineInc;
                    for (int line = lineFrom + ti; line < lineTo; line += numThreads, pixel0 += numThreads * lineInc) {
                        long time = System.currentTimeMillis();
                        if (time - lastTime > 110) {
                            if (canShowProgress) showProgress((double) (line - lineFrom) / (lineTo - lineFrom));
                            if (Thread.currentThread().isInterrupted()) return;
                            lastTime = time;
                        }
                        if (doDownscaling) {
                            downscaleLine(pixels, cache1, downscaleKernel, reduceBy, pixel0, unscaled0, length, pointInc, newLength);
                            convolveLine(cache1, cache2, gaussKernel, 0, newLength, 1, newLength - 1, 0, 1);
                            upscaleLine(cache2, pixels, upscaleKernel, reduceBy, pixel0, unscaled0, writeFrom, writeTo, pointInc);
                        } else {
                            int p = pixel0 + readFrom * pointInc;
                            for (int i = readFrom; i < readTo; i++, p += pointInc) cache1[i] = pixels[p];
                            convolveLine(cache1, pixels, gaussKernel, readFrom, readTo, writeFrom, writeTo, pixel0, pointInc);
                        }
                    }
                }
            }, "GaussianBlur-" + t);
            thread.setPriority(Thread.currentThread().getPriority());
            lineThreads[ti] = thread;
            thread.start();
        }
        try {
            for (final Thread thread : lineThreads) if (thread != null) thread.join();
        } catch (InterruptedException e) {
            for (final Thread thread : lineThreads) thread.interrupt();
            try {
                for (final Thread thread : lineThreads) thread.join();
            } catch (InterruptedException f) {
            }
            Thread.currentThread().interrupt();
        }
        showProgress(1.0);
        return;
    }

    /** Scale a line (row or column of a FloatProcessor or part thereof)
     * down by a factor <code>reduceBy</code> and write the result into
     * <code>cache</code>.
     * Input line pixel # <code>unscaled0</code> will correspond to output
     * line pixel # 0. <code>unscaled0</code> may be negative. Out-of-line
     * pixels of the input are replaced by the edge pixels.
     * @param pixels    input array
     * @param cache     output array
     * @param kernel    downscale kernel, runs form -1.5 to +1.5 in downscaled coordinates
     * @param reduceBy  downscaling factor
     * @param pixel0    index in pixels array corresponding to start of line or column
     * @param unscaled0 index in input line corresponding to output line index 0, May be negative.
     * @param length    length of full input line or column
     * @param pointInc  spacing of values in input array (1 for lines, image width for columns)
     * @param newLength length of downscaled data
     */
    private static final void downscaleLine(final float[] pixels, final float[] cache, final float[] kernel, final int reduceBy, final int pixel0, final int unscaled0, final int length, final int pointInc, final int newLength) {
        int p = pixel0 + pointInc * (unscaled0 - reduceBy * 3 / 2);
        final int pLast = pixel0 + pointInc * (length - 1);
        for (int xout = -1; xout <= newLength; xout++) {
            float sum0 = 0, sum1 = 0, sum2 = 0;
            for (int x = 0; x < reduceBy; x++, p += pointInc) {
                float v = pixels[p < pixel0 ? pixel0 : (p > pLast ? pLast : p)];
                sum0 += v * kernel[x + 2 * reduceBy];
                sum1 += v * kernel[x + reduceBy];
                sum2 += v * kernel[x];
            }
            if (xout > 0) cache[xout - 1] += sum0;
            if (xout >= 0 && xout < newLength) cache[xout] += sum1;
            if (xout + 1 < newLength) cache[xout + 1] = sum2;
        }
    }

    private static final float[] makeDownscaleKernel(final int unitLength) {
        final int mid = unitLength * 3 / 2;
        final float[] kernel = new float[3 * unitLength];
        for (int i = 0; i <= unitLength / 2; i++) {
            final double x = i / (double) unitLength;
            final float v = (float) ((0.75 - x * x) / unitLength);
            kernel[mid - i] = v;
            kernel[mid + i] = v;
        }
        for (int i = unitLength / 2 + 1; i < (unitLength * 3 + 1) / 2; i++) {
            final double x = i / (double) unitLength;
            final float v = (float) ((0.125 + 0.5 * (x - 1) * (x - 2)) / unitLength);
            kernel[mid - i] = v;
            kernel[mid + i] = v;
        }
        return kernel;
    }

    /** Scale a line up by factor <code>reduceBy</code> and write as a row
     * or column (or part thereof) to the pixels array of a FloatProcessor.
     */
    private static final void upscaleLine(final float[] cache, final float[] pixels, final float[] kernel, final int reduceBy, final int pixel0, final int unscaled0, final int writeFrom, final int writeTo, final int pointInc) {
        int p = pixel0 + pointInc * writeFrom;
        for (int xout = writeFrom; xout < writeTo; xout++, p += pointInc) {
            final int xin = (xout - unscaled0 + reduceBy - 1) / reduceBy;
            final int x = reduceBy - 1 - (xout - unscaled0 + reduceBy - 1) % reduceBy;
            pixels[p] = cache[xin - 2] * kernel[x] + cache[xin - 1] * kernel[x + reduceBy] + cache[xin] * kernel[x + 2 * reduceBy] + cache[xin + 1] * kernel[x + 3 * reduceBy];
        }
    }

    /** Create a kernel for upscaling. The kernel function is a convolution
     *  of four unit squares, i.e., four uniform kernels with value +1
     *  from -0.5 to +0.5 (in downscaled coordinates). The second derivative
     *  of this kernel is smooth, the third is not. Its standard deviation
     *  is 1/sqrt(3) in downscaled cordinates.
     *  The kernel runs from [-2 to +2[, corresponding to array index
     *  0 ... 4*unitLength (whereby the last point is not in the array any more).
     */
    private static final float[] makeUpscaleKernel(final int unitLength) {
        final float[] kernel = new float[4 * unitLength];
        final int mid = 2 * unitLength;
        kernel[0] = 0;
        for (int i = 0; i < unitLength; i++) {
            final double x = i / (double) unitLength;
            final float v = (float) ((2. / 3. - x * x * (1 - 0.5 * x)));
            kernel[mid + i] = v;
            kernel[mid - i] = v;
        }
        for (int i = unitLength; i < 2 * unitLength; i++) {
            final double x = i / (double) unitLength;
            final float v = (float) ((2. - x) * (2. - x) * (2. - x) / 6.);
            kernel[mid + i] = v;
            kernel[mid - i] = v;
        }
        return kernel;
    }

    /** Convolve a line with a symmetric kernel and write to a separate array,
     * possibly the pixels array of a FloatProcessor (as a row or column or part thereof)
     *
     * @param input     Input array containing the line
     * @param pixels    Float array for output, can be the pixels of a FloatProcessor
     * @param kernel    "One-sided" kernel array, kernel[0][n] must contain the kernel
     *                  itself, kernel[1][n] must contain the running sum over all
     *                  kernel elements from kernel[0][n+1] to the periphery.
     *                  The kernel must be normalized, i.e. sum(kernel[0][n]) = 1
     *                  where n runs from the kernel periphery (last element) to 0 and
     *                  back. Normalization should include all kernel points, also these
     *                  not calculated because they are not needed.
     * @param readFrom  First array element of the line that must be read.
     *                  <code>writeFrom-kernel.length</code> or 0.
     * @param readTo    Last array element+1 of the line that must be read.
     *                  <code>writeTo+kernel.length</code> or <code>input.length</code>
     * @param writeFrom Index of the first point in the line that should be written
     * @param writeTo   Index+1 of the last point in the line that should be written
     * @param point0    Array index of first element of the 'line' in pixels (i.e., lineNumber * lineInc)
     * @param pointInc  Increment of the pixels array index to the next point (for an ImageProcessor,
     *                  it should be <code>1</code> for a row, <code>width</code> for a column)
     */
    private static final void convolveLine(final float[] input, final float[] pixels, final float[][] kernel, final int readFrom, final int readTo, final int writeFrom, final int writeTo, final int point0, final int pointInc) {
        final int length = input.length;
        final float first = input[0];
        final float last = input[length - 1];
        final float[] kern = kernel[0];
        final float kern0 = kern[0];
        final float[] kernSum = kernel[1];
        final int kRadius = kern.length;
        final int firstPart = kRadius < length ? kRadius : length;
        int p = point0 + writeFrom * pointInc;
        int i = writeFrom;
        for (; i < firstPart; i++, p += pointInc) {
            float result = input[i] * kern0;
            result += kernSum[i] * first;
            if (i + kRadius > length) result += kernSum[length - i - 1] * last;
            for (int k = 1; k < kRadius; k++) {
                float v = 0;
                if (i - k >= 0) v += input[i - k];
                if (i + k < length) v += input[i + k];
                result += kern[k] * v;
            }
            pixels[p] = result;
        }
        final int iEndInside = length - kRadius < writeTo ? length - kRadius : writeTo;
        for (; i < iEndInside; i++, p += pointInc) {
            float result = input[i] * kern0;
            for (int k = 1; k < kRadius; k++) result += kern[k] * (input[i - k] + input[i + k]);
            pixels[p] = result;
        }
        for (; i < writeTo; i++, p += pointInc) {
            float result = input[i] * kern0;
            if (i < kRadius) result += kernSum[i] * first;
            if (i + kRadius >= length) result += kernSum[length - i - 1] * last;
            for (int k = 1; k < kRadius; k++) {
                float v = 0;
                if (i - k >= 0) v += input[i - k];
                if (i + k < length) v += input[i + k];
                result += kern[k] * v;
            }
            pixels[p] = result;
        }
    }

    /** Create a 1-dimensional normalized Gaussian kernel with standard deviation sigma
     *  and the running sum over the kernel
     *  Note: this is one side of the kernel only, not the full kernel as used by the
     *  Convolver class of ImageJ.
     *  To avoid a step due to the cutoff at a finite value, the near-edge values are
     *  replaced by a 2nd-order polynomial with its minimum=0 at the first out-of-kernel
     *  pixel. Thus, the kernel function has a smooth 1st derivative in spite of finite
     *  length.
     *
     * @param sigma     Standard deviation, i.e. radius of decay to 1/sqrt(e), in pixels.
     * @param accuracy  Relative accuracy; for best results below 0.01 when processing
     *                  8-bit images. For short or float images, values of 1e-3 to 1e-4
     *                  are better (but increase the kernel size and thereby the
     *                  processing time). Edge smoothing will fail with very poor
     *                  accuracy (above approx. 0.02)
     * @param maxRadius Maximum radius of the kernel: Limits kernel size in case of
     *                  large sigma, should be set to image width or height. For small
     *                  values of maxRadius, the kernel returned may have a larger
     *                  radius, however.
     * @return          A 2*n array. Array[0][n] is the kernel, decaying towards zero,
     *                  which would be reached at kernel.length (unless kernel size is
     *                  limited by maxRadius). Array[1][n] holds the sum over all kernel
     *                  values > n, including non-calculated values in case the kernel
     *                  size is limited by <code>maxRadius</code>.
     */
    public float[][] makeGaussianKernel(final double sigma, final double accuracy, int maxRadius) {
        int kRadius = (int) Math.ceil(sigma * Math.sqrt(-2 * Math.log(accuracy))) + 1;
        if (maxRadius < 50) maxRadius = 50;
        if (kRadius > maxRadius) kRadius = maxRadius;
        float[][] kernel = new float[2][kRadius];
        for (int i = 0; i < kRadius; i++) kernel[0][i] = (float) (Math.exp(-0.5 * i * i / sigma / sigma));
        if (kRadius < maxRadius && kRadius > 3) {
            double sqrtSlope = Double.MAX_VALUE;
            int r = kRadius;
            while (r > kRadius / 2) {
                r--;
                double a = Math.sqrt(kernel[0][r]) / (kRadius - r);
                if (a < sqrtSlope) sqrtSlope = a; else break;
            }
            for (int r1 = r + 2; r1 < kRadius; r1++) kernel[0][r1] = (float) ((kRadius - r1) * (kRadius - r1) * sqrtSlope * sqrtSlope);
        }
        double sum;
        if (kRadius < maxRadius) {
            sum = kernel[0][0];
            for (int i = 1; i < kRadius; i++) sum += 2 * kernel[0][i];
        } else sum = sigma * Math.sqrt(2 * Math.PI);
        double rsum = 0.5 + 0.5 * kernel[0][0] / sum;
        for (int i = 0; i < kRadius; i++) {
            double v = (kernel[0][i] / sum);
            kernel[0][i] = (float) v;
            rsum -= v;
            kernel[1][i] = (float) rsum;
        }
        return kernel;
    }

    /** Set the processed pixels above and below the roi rectangle back to their
     * previous value (i.e., snapshot buffer). This is necessary since ImageJ
     * only restores out-of-roi pixels inside the enclosing rectangle of the roi
     * (If the roi is non-rectangular and the SUPPORTS_MASKING flag is set).
     * @param ip The image to be processed
     * @param radius The range above and below the roi that should be processed
     */
    public static void resetOutOfRoi(ImageProcessor ip, int radius) {
        Rectangle roi = ip.getRoi();
        int width = ip.getWidth();
        int height = ip.getHeight();
        Object pixels = ip.getPixels();
        Object snapshot = ip.getSnapshotPixels();
        int y0 = roi.y - radius;
        if (y0 < 0) y0 = 0;
        for (int y = y0, p = width * y + roi.x; y < roi.y; y++, p += width) System.arraycopy(snapshot, p, pixels, p, roi.width);
        int yEnd = roi.y + roi.height + radius;
        if (yEnd > height) yEnd = height;
        for (int y = roi.y + roi.height, p = width * y + roi.x; y < yEnd; y++, p += width) System.arraycopy(snapshot, p, pixels, p, roi.width);
    }

    void showProgress(double percent) {
        percent = (double) (pass - 1) / nPasses + percent / nPasses;
        IJ.showProgress(percent);
    }
}
