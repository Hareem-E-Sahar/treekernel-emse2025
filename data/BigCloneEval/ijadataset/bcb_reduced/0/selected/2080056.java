package pl.edu.zut.wi.vsl.modules.steganalysis.lsb;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;
import pl.edu.zut.wi.vsl.commons.StegoImage;
import pl.edu.zut.wi.vsl.commons.steganalysis.SteganalysisException;
import pl.edu.zut.wi.vsl.commons.steganalysis.SteganalyticTechnique;
import pl.edu.zut.wi.vsl.commons.utils.ImageUtility;

/**
 * RS analysis for a stego-image.
 * <p>
 * RS analysis is a system for detecting LSB steganography proposed by
 * Dr. Fridrich at Binghamton University, NY.  You can visit her
 * webpage for more information - 
 * {@link http://www.ws.binghamton.edu/fridrich/} 
 * <p>
 * Implemented as described in "Reliable detection of LSB steganography
 * in color and grayscale images" by J. Fridrich, M. Goljan and R. Du. 
 * <p>
 * The original code of this technique was produced by Kathryn Hempstalk.
 * Visit her webpage for more information -
 * {@link http://www.cs.waikato.ac.nz/~kah18/}
 * 
 * @author Michal Wegrzyn
 */
public class RSAnalysisImpl implements SteganalyticTechnique {

    /** Denotes analysis to be done with red. */
    public static final int ANALYSIS_COLOUR_RED = 0;

    /** Denotes analysis to be done with green. */
    public static final int ANALYSIS_COLOUR_GREEN = 1;

    /** Denotes analysis to be done with blue. */
    public static final int ANALYSIS_COLOUR_BLUE = 2;

    /** Denotes analysis to be done with gray-scale images. */
    public static final int ANALYSIS_GRAY = 3;

    private static final Logger logger = Logger.getLogger(RSAnalysisImpl.class);

    /** The mask to be used for the pixel groups. */
    private int[][] mMask;

    /** The x length of the mask. */
    private int mM;

    /** The y length of the mask. */
    private int mN;

    /**
     * A small main method that will print out the message length
     * in percent of pixels.
     *
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            System.exit(1);
        }
        StegoImage si = null;
        LinkedHashMap<String, String> o = new LinkedHashMap<String, String>();
        o.put("x mask size", args[1]);
        o.put("y mask size", args[2]);
        RSAnalysisImpl rs = new RSAnalysisImpl();
        rs.initialize(2, 2);
        try {
            BufferedImage bi = ImageUtility.readImage(args[0]);
            si = new StegoImage(bi, args[0]);
        } catch (IllegalArgumentException e) {
            logger.error("Could not create stegoimage.", e);
            System.exit(1);
        } catch (NullPointerException e) {
            logger.error("Could not create stegoimage.", e);
            System.exit(1);
        } catch (IOException e) {
            logger.error("Could not create stegoimage.", e);
            System.exit(1);
        }
        try {
            rs.analyse(si, null);
        } catch (SteganalysisException e) {
            logger.error("Error occured during steganalysis", e);
            System.exit(1);
        }
    }

    /**
     * Prints usage to console.
     */
    public static void printUsage() {
        System.out.println("Usage: \n" + "vsl-module-steganalysis-lsb <path to image> <x mask size> <y mask size>\n" + "x mask size - the x mask size for RS Analysis mask\n" + "x mask size - the y mask size for RS Analysis mask");
    }

    public LinkedHashMap<String, String> analyse(StegoImage image, LinkedHashMap<String, String> options) throws SteganalysisException {
        int xsize, ysize;
        try {
            xsize = Integer.parseInt(options.get("x mask size"));
            ysize = Integer.parseInt(options.get("y mask size"));
        } catch (NumberFormatException e) {
            throw new SteganalysisException("Mask sizes should be " + "integer values", e);
        }
        initialize(xsize, ysize);
        double average = 0;
        double l = 0;
        logger.info("Starting RS Analysis");
        switch(image.getLayerCount()) {
            case 1:
                LinkedHashMap<String, String> resultsGray = doAnalysis(image, RSAnalysisImpl.ANALYSIS_GRAY, true);
                l = Double.valueOf(resultsGray.get("Gray: Estimated message length (in bytes)"));
                logger.info("Result from gray [B]: " + l);
                resultsGray.put("result", "Estimated message size [B]:" + l);
                return resultsGray;
            case 3:
                LinkedHashMap<String, String> resultsRed = doAnalysis(image, RSAnalysisImpl.ANALYSIS_COLOUR_RED, true);
                l = Double.valueOf(resultsRed.get("Red: Estimated message length (in bytes)"));
                logger.info("Result from red [B]: " + l);
                average += l;
                LinkedHashMap<String, String> resultsGreen = doAnalysis(image, RSAnalysisImpl.ANALYSIS_COLOUR_GREEN, true);
                l = Double.valueOf(resultsGreen.get("Green: Estimated message length (in bytes)"));
                logger.info("Result from green [B]: " + l);
                average += l;
                LinkedHashMap<String, String> resultsBlue = doAnalysis(image, RSAnalysisImpl.ANALYSIS_COLOUR_BLUE, true);
                l = Double.valueOf(resultsBlue.get("Blue: Estimated message length (in bytes)"));
                logger.info("Result from blue [B]: " + l);
                average += l;
                average = average / 3;
                logger.info("Average result [B]: " + average + "\n");
                resultsRed.putAll(resultsGreen);
                resultsRed.putAll(resultsBlue);
                resultsRed.put("Average result", String.valueOf(average));
                resultsRed.put("result", "Estimated message size [B]:" + average);
                return resultsRed;
            case 0:
            default:
                throw new SteganalysisException("unsupported image type");
        }
    }

    /**
     * Initializes a new RS analysis with a given mask size of m x n.
     *
     * Each alternating bit is set to 1.  Eg for a mask of size 2x2
     * the resulting mask will be {1,0;0,1}.  Two masks are used - one is
     * the inverse of the other.
     *
     * @param m The x mask size.
     * @param n The y mask size.
     */
    private void initialize(int m, int n) {
        mMask = new int[2][m * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (((j % 2) == 0 && (i % 2) == 0) || ((j % 2) == 1 && (i % 2) == 1)) {
                    mMask[0][k] = 1;
                    mMask[1][k] = 0;
                } else {
                    mMask[0][k] = 0;
                    mMask[1][k] = 1;
                }
                k++;
            }
        }
        mM = m;
        mN = n;
    }

    /**
     * Does an RS analysis of a given image.  
     * <P>
     * The analysis data returned is specified by name in
     * the getResultNames() method.
     *
     * @param image The image to analyse.
     * @param colour The color to analyse.
     * @param overlap Whether the blocks should overlap or not.
     * @return The analysis information.
     */
    public LinkedHashMap<String, String> doAnalysis(StegoImage image, int colour, boolean overlap) {
        int imgx = image.getWidth(), imgy = image.getHeight();
        int startx = 0, starty = 0;
        int block[] = new int[mM * mN];
        double numregular = 0, numsingular = 0;
        double numnegreg = 0, numnegsing = 0;
        double numunusable = 0, numnegunusable = 0;
        double variationB, variationP, variationN;
        while (startx < imgx && starty < imgy) {
            for (int m = 0; m < 2; m++) {
                int k = 0;
                if (image.getLayerCount() == 3) {
                    for (int i = 0; i < mN; i++) {
                        for (int j = 0; j < mM; j++) {
                            block[k] = image.getRGB(startx + j, starty + i);
                            k++;
                        }
                    }
                } else {
                    for (int i = 0; i < mN; i++) {
                        for (int j = 0; j < mM; j++) {
                            block[k] = image.getRaster().getSample(startx + j, starty + i, 0);
                            k++;
                        }
                    }
                }
                variationB = getVariation(block, colour);
                block = flipBlock(block, mMask[m], colour);
                variationP = getVariation(block, colour);
                block = flipBlock(block, mMask[m], colour);
                mMask[m] = this.invertMask(mMask[m]);
                variationN = getNegativeVariation(block, colour, mMask[m]);
                mMask[m] = this.invertMask(mMask[m]);
                if (variationP > variationB) {
                    numregular++;
                }
                if (variationP < variationB) {
                    numsingular++;
                }
                if (variationP == variationB) {
                    numunusable++;
                }
                if (variationN > variationB) {
                    numnegreg++;
                }
                if (variationN < variationB) {
                    numnegsing++;
                }
                if (variationN == variationB) {
                    numnegunusable++;
                }
            }
            if (overlap) {
                startx += 1;
            } else {
                startx += mM;
            }
            if (startx >= (imgx - 1)) {
                startx = 0;
                if (overlap) {
                    starty += 1;
                } else {
                    starty += mN;
                }
            }
            if (starty >= (imgy - 1)) {
                break;
            }
        }
        double totalgroups = numregular + numsingular + numunusable;
        double allpixels[] = this.getAllPixelFlips(image, colour, overlap);
        double x = getX(numregular, numnegreg, allpixels[0], allpixels[2], numsingular, numnegsing, allpixels[1], allpixels[3]);
        double epf, ml;
        if (2 * (x - 1) == 0) {
            epf = 0;
        } else {
            epf = Math.abs(x / (2 * (x - 1)));
        }
        if (x - 0.5 == 0) {
            ml = 0;
        } else {
            ml = Math.abs(x / (x - 0.5));
        }
        LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();
        String colName;
        switch(colour) {
            case RSAnalysisImpl.ANALYSIS_COLOUR_RED:
                colName = "Red: ";
                break;
            case RSAnalysisImpl.ANALYSIS_COLOUR_GREEN:
                colName = "Green: ";
                break;
            case RSAnalysisImpl.ANALYSIS_COLOUR_BLUE:
                colName = "Blue: ";
                break;
            case RSAnalysisImpl.ANALYSIS_GRAY:
                colName = "Gray: ";
                break;
            default:
                colName = "";
                break;
        }
        results.put(colName + "Number of regular groups (positive)", String.valueOf(numregular));
        results.put(colName + "Number of singular groups (positive)", String.valueOf(numsingular));
        results.put(colName + "Number of regular groups (negative)", String.valueOf(numnegreg));
        results.put(colName + "Number of singular groups (negative)", String.valueOf(numnegsing));
        results.put(colName + "Difference for regular groups", String.valueOf(Math.abs(numregular - numnegreg)));
        results.put(colName + "Difference for singular groups", String.valueOf(Math.abs(numsingular - numnegsing)));
        results.put(colName + "Percentage of regular groups (positive)", String.valueOf((numregular / totalgroups) * 100));
        results.put(colName + "Percentage of singular groups (positive)", String.valueOf((numsingular / totalgroups) * 100));
        results.put(colName + "Percentage of regular groups (negative)", String.valueOf((numnegreg / totalgroups) * 100));
        results.put(colName + "Percentage of singular groups (negative)", String.valueOf((numnegsing / totalgroups) * 100));
        results.put(colName + "Difference for regular groups %", String.valueOf((Math.abs(numregular - numnegreg) / totalgroups) * 100));
        results.put(colName + "Difference for singular groups %", String.valueOf((Math.abs(numsingular - numnegsing) / totalgroups) * 100));
        results.put(colName + "Number of regular groups (positive for all flipped)", String.valueOf(allpixels[0]));
        results.put(colName + "Number of singular groups (positive for all flipped)", String.valueOf(allpixels[1]));
        results.put(colName + "Number of regular groups (negative for all flipped)", String.valueOf(allpixels[2]));
        results.put(colName + "Number of singular groups (negative for all flipped)", String.valueOf(allpixels[3]));
        results.put(colName + "Difference for regular groups (all flipped)", String.valueOf(Math.abs(allpixels[0] - allpixels[1])));
        results.put(colName + "Difference for singular groups (all flipped)", String.valueOf(Math.abs(allpixels[2] - allpixels[3])));
        results.put(colName + "Percentage of regular groups (positive for all flipped)", String.valueOf((allpixels[0] / totalgroups) * 100));
        results.put(colName + "Percentage of singular groups (positive for all flipped)", String.valueOf((allpixels[1] / totalgroups) * 100));
        results.put(colName + "Percentage of regular groups (negative for all flipped)", String.valueOf((allpixels[2] / totalgroups) * 100));
        results.put(colName + "Percentage of singular groups (negative for all flipped)", String.valueOf((allpixels[3] / totalgroups) * 100));
        results.put(colName + "Difference for regular groups (all flipped) %", String.valueOf((Math.abs(allpixels[0] - allpixels[1]) / totalgroups) * 100));
        results.put(colName + "Difference for singular groups (all flipped) %", String.valueOf((Math.abs(allpixels[2] - allpixels[3]) / totalgroups) * 100));
        results.put(colName + "Total number of groups", String.valueOf(totalgroups));
        results.put(colName + "Estimated percent of flipped pixels", String.valueOf(epf));
        results.put(colName + "Estimated message length (in percent of pixels)(p)", String.valueOf(ml));
        results.put(colName + "Estimated message length (in bytes)", String.valueOf(((imgx * imgy * (colour == RSAnalysisImpl.ANALYSIS_GRAY ? 1 : 3)) * ml) / 8));
        return results;
    }

    /**
     * Gets the x value for the p=x(x/2) RS equation. See the paper for
     * more details.
     *
     * @param r The value of Rm(p/2).
     * @param rm The value of R-m(p/2).
     * @param r1 The value of Rm(1-p/2).
     * @param rm1 The value of R-m(1-p/2).
     * @param s The value of Sm(p/2).
     * @param sm The value of S-m(p/2).
     * @param s1 The value of Sm(1-p/2).
     * @param sm1 The value of S-m(1-p/2).
     * @return The value of x.
     */
    private double getX(double r, double rm, double r1, double rm1, double s, double sm, double s1, double sm1) {
        double x = 0;
        double dzero = r - s;
        double dminuszero = rm - sm;
        double done = r1 - s1;
        double dminusone = rm1 - sm1;
        double a = 2 * (done + dzero);
        double b = dminuszero - dminusone - done - (3 * dzero);
        double c = dzero - dminuszero;
        if (a == 0) {
            x = c / b;
        }
        double discriminant = Math.pow(b, 2) - (4 * a * c);
        if (discriminant >= 0) {
            double rootpos = ((-1 * b) + Math.sqrt(discriminant)) / (2 * a);
            double rootneg = ((-1 * b) - Math.sqrt(discriminant)) / (2 * a);
            if (Math.abs(rootpos) <= Math.abs(rootneg)) {
                x = rootpos;
            } else {
                x = rootneg;
            }
        } else {
            double cr = (rm - r) / (r1 - r + rm - rm1);
            double cs = (sm - s) / (s1 - s + sm - sm1);
            x = (cr + cs) / 2;
        }
        if (x == 0) {
            double ar = ((rm1 - r1 + r - rm) + (rm - r) / x) / (x - 1);
            double as = ((sm1 - s1 + s - sm) + (sm - s) / x) / (x - 1);
            if (as > 0 | ar < 0) {
                double cr = (rm - r) / (r1 - r + rm - rm1);
                double cs = (sm - s) / (s1 - s + sm - sm1);
                x = (cr + cs) / 2;
            }
        }
        return x;
    }

    /**
     * Gets the RS analysis results for flipping performed on all
     * pixels.
     *
     * @param image The image to analyse.
     * @param colour The colour to analyse.
     * @param overlap Whether the blocks should overlap.
     * @return The analysis information for all flipped pixels.
     */
    private double[] getAllPixelFlips(StegoImage image, int colour, boolean overlap) {
        int[] allmask = new int[mM * mN];
        for (int i = 0; i < allmask.length; i++) {
            allmask[i] = 1;
        }
        int imgx = image.getWidth(), imgy = image.getHeight();
        int startx = 0, starty = 0;
        int block[] = new int[mM * mN];
        double numregular = 0, numsingular = 0;
        double numnegreg = 0, numnegsing = 0;
        double numunusable = 0, numnegunusable = 0;
        double variationB, variationP, variationN;
        while (startx < imgx && starty < imgy) {
            for (int m = 0; m < 2; m++) {
                int k = 0;
                if (image.getLayerCount() == 3) {
                    for (int i = 0; i < mN; i++) {
                        for (int j = 0; j < mM; j++) {
                            block[k] = image.getRGB(startx + j, starty + i);
                            k++;
                        }
                    }
                } else {
                    for (int i = 0; i < mN; i++) {
                        for (int j = 0; j < mM; j++) {
                            block[k] = image.getRaster().getSample(startx + j, starty + i, 0);
                            k++;
                        }
                    }
                }
                block = flipBlock(block, allmask, colour);
                variationB = getVariation(block, colour);
                block = flipBlock(block, mMask[m], colour);
                variationP = getVariation(block, colour);
                block = flipBlock(block, mMask[m], colour);
                mMask[m] = this.invertMask(mMask[m]);
                variationN = getNegativeVariation(block, colour, mMask[m]);
                mMask[m] = this.invertMask(mMask[m]);
                if (variationP > variationB) {
                    numregular++;
                }
                if (variationP < variationB) {
                    numsingular++;
                }
                if (variationP == variationB) {
                    numunusable++;
                }
                if (variationN > variationB) {
                    numnegreg++;
                }
                if (variationN < variationB) {
                    numnegsing++;
                }
                if (variationN == variationB) {
                    numnegunusable++;
                }
            }
            if (overlap) {
                startx += 1;
            } else {
                startx += mM;
            }
            if (startx >= (imgx - 1)) {
                startx = 0;
                if (overlap) {
                    starty += 1;
                } else {
                    starty += mN;
                }
            }
            if (starty >= (imgy - 1)) {
                break;
            }
        }
        double results[] = new double[4];
        results[0] = numregular;
        results[1] = numsingular;
        results[2] = numnegreg;
        results[3] = numnegsing;
        return results;
    }

    /**
     * Gets the variation of the blocks of data. Uses
     * the formula f(x) = |x0 - x1| + |x1 + x3| + |x3 - x2| + |x2 - x0|;
     * However, if the block is not in the shape 2x2 or 4x1, this will be
     * applied as many times as the block can be broken up into 4 (without
     * overlaps).
     *
     * @param block The block of data (in 24 bit colour).
     * @param colour The colour to get the variation of.
     * @return The variation in the block.
     */
    private double getVariation(int[] block, int colour) {
        double var = 0;
        int colour1, colour2;
        for (int i = 0; i < block.length; i = i + 4) {
            colour1 = getPixelColour(block[0 + i], colour);
            colour2 = getPixelColour(block[1 + i], colour);
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[3 + i], colour);
            colour2 = getPixelColour(block[2 + i], colour);
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[1 + i], colour);
            colour2 = getPixelColour(block[3 + i], colour);
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[2 + i], colour);
            colour2 = getPixelColour(block[0 + i], colour);
            var += Math.abs(colour1 - colour2);
        }
        return var;
    }

    /**
     * Gets the negative variation of the blocks of data. Uses
     * the formula f(x) = |x0 - x1| + |x1 + x3| + |x3 - x2| + |x2 - x0|;
     * However, if the block is not in the shape 2x2 or 4x1, this will be
     * applied as many times as the block can be broken up into 4 (without
     * overlaps).
     *
     * @param block The block of data (in 24 bit colour).
     * @param colour The colour to get the variation of.
     * @param mask The negative mask.
     * @return The variation in the block.
     */
    private double getNegativeVariation(int[] block, int colour, int[] mask) {
        double var = 0;
        int colour1, colour2;
        for (int i = 0; i < block.length; i = i + 4) {
            colour1 = getPixelColour(block[0 + i], colour);
            colour2 = getPixelColour(block[1 + i], colour);
            if (mask[0 + i] == -1) {
                colour1 = invertLSB(colour1);
            }
            if (mask[1 + i] == -1) {
                colour2 = invertLSB(colour2);
            }
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[1 + i], colour);
            colour2 = getPixelColour(block[3 + i], colour);
            if (mask[1 + i] == -1) {
                colour1 = invertLSB(colour1);
            }
            if (mask[3 + i] == -1) {
                colour2 = invertLSB(colour2);
            }
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[3 + i], colour);
            colour2 = getPixelColour(block[2 + i], colour);
            if (mask[3 + i] == -1) {
                colour1 = invertLSB(colour1);
            }
            if (mask[2 + i] == -1) {
                colour2 = invertLSB(colour2);
            }
            var += Math.abs(colour1 - colour2);
            colour1 = getPixelColour(block[2 + i], colour);
            colour2 = getPixelColour(block[0 + i], colour);
            if (mask[2 + i] == -1) {
                colour1 = invertLSB(colour1);
            }
            if (mask[0 + i] == -1) {
                colour2 = invertLSB(colour2);
            }
            var += Math.abs(colour1 - colour2);
        }
        return var;
    }

    /**
     * Gets the given colour value for this pixel.
     * 
     * @param pixel The pixel to get the colour of.
     * @param colour The colour to get.
     * @return The colour value of the given colour in the given pixel.
     */
    public int getPixelColour(int pixel, int colour) {
        switch(colour) {
            case RSAnalysisImpl.ANALYSIS_COLOUR_RED:
                return ImageUtility.getRed(pixel);
            case RSAnalysisImpl.ANALYSIS_COLOUR_GREEN:
                return ImageUtility.getGreen(pixel);
            case RSAnalysisImpl.ANALYSIS_COLOUR_BLUE:
                return ImageUtility.getBlue(pixel);
            case RSAnalysisImpl.ANALYSIS_GRAY:
                return pixel;
            default:
                return 0;
        }
    }

    /**
     * Flips a block of pixels.
     *
     * @param block The block to flip.
     * @param mask The mask to use for flipping.
     * @return The flipped block.
     */
    private int[] flipBlock(int[] block, int[] mask, int colour) {
        for (int i = 0; i < block.length; i++) {
            if ((mask[i] == 1)) {
                if (colour == RSAnalysisImpl.ANALYSIS_GRAY) {
                    int gray = negateLSB(block[i]);
                    block[i] = gray;
                } else {
                    int red = ImageUtility.getRed(block[i]);
                    int green = ImageUtility.getGreen(block[i]);
                    int blue = ImageUtility.getBlue(block[i]);
                    red = negateLSB(red);
                    green = negateLSB(green);
                    blue = negateLSB(blue);
                    int newpixel = (0xff << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | ((blue & 0xff));
                    block[i] = newpixel;
                }
            } else if (mask[i] == -1) {
                if (colour == RSAnalysisImpl.ANALYSIS_GRAY) {
                    int gray = invertLSB(block[i]);
                    block[i] = gray;
                } else {
                    int red = ImageUtility.getRed(block[i]);
                    int green = ImageUtility.getGreen(block[i]);
                    int blue = ImageUtility.getBlue(block[i]);
                    red = invertLSB(red);
                    green = invertLSB(green);
                    blue = invertLSB(blue);
                    int newpixel = (0xff << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | ((blue & 0xff));
                    block[i] = newpixel;
                }
            }
        }
        return block;
    }

    /**
     * Negates the LSB of a given byte (stored in an int).
     *
     * @param abyte The byte to negate the LSB of.
     * @return The byte with negated LSB.
     */
    private int negateLSB(int abyte) {
        int temp = abyte & 0xfe;
        if (temp == abyte) {
            return abyte | 0x1;
        } else {
            return temp;
        }
    }

    /**
     * Inverts the LSB of a given byte (stored in an int).
     * 
     * @param abyte The byte to flip.
     * @return The byte with the flipped LSB.
     */
    private int invertLSB(int abyte) {
        if (abyte == 255) {
            return 256;
        }
        if (abyte == 256) {
            return 255;
        }
        return (negateLSB(abyte + 1) - 1);
    }

    /**
     * Inverts a mask.
     *
     * @param mask The mask to invert.
     * @return The flipped mask.
     */
    private int[] invertMask(int[] mask) {
        for (int i = 0; i < mask.length; i++) {
            mask[i] = mask[i] * -1;
        }
        return mask;
    }
}
