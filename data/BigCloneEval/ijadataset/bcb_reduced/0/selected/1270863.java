package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**
 * This class creates a RenderedImage in conformance to the one
 * defined for the feTurbulence filter of the SVG specification.  What
 * follows is my high-level description of how the noise is generated.
 * This is not contained in the SVG spec, just the algorithm for
 * doing it.  This is provided in the hope that someone will figure
 * out a clever way to accelerate parts of the function.
 *
 * gradient contains a long list of random unit vectors.  For each
 * point we are to generate noise for we do two things.  first we use
 * the latticeSelector to 'co-mingle' the integer portions of x and y
 * (this allows us to have a one-dimensional array of gradients that
 * appears 2 dimensional, by using the co-mingled index).
 *
 * We do this for [x,y], [x+1,y], [x,y+1], and [x+1, y+1], this gives
 * us the four gradient vectors that surround the point (b00, b10, ...)
 * 
 * Next we construct the four vectors from the grid points (where the
 * gradient vectors are defined) [these are rx0, rx1, ry0, ry1].
 *
 * We then take the dot product between the gradient vectors and the
 * grid point vectors (this gives the portion of the grid point vector
 * that projects along the gradient vector for each grid point).
 * These four dot projects are then combined with linear interpolation.
 * The weight factor for the linear combination is the result of applying
 * the 's' curve function to the fractional part of x and y (rx0, ry0).
 * The S curve function get's it's name because it looks a bit like as
 * 'S' from 0->1.
 *  
 * @author     <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @author     <a href="mailto:DeWeese@apache.org">Thomas DeWeese</a>
 * @version $Id: TurbulencePatternRed.java,v 1.1 2005/11/21 09:51:20 dev Exp $ */
public final class TurbulencePatternRed extends AbstractRed {

    /**
     * Inner class to store tile stitching info.
     * #see
     */
    static final class StitchInfo {

        /**
         * Width of the integer lattice tile
         */
        int width;

        /**
         * Height of the integer lattice tile
         */
        int height;

        /**
         * Value beyond which values are wrapped on
         * the x-axis.
         * @see #noise2Stitch
         */
        int wrapX;

        /**
         * Value beyond which values are wrapped on
         * the y-axis.
         * @see #noise2Stitch
         */
        int wrapY;

        /**
         * Default constructor
         */
        StitchInfo() {
        }

        /**
         * Copy constructor
         */
        StitchInfo(StitchInfo stitchInfo) {
            this.width = stitchInfo.width;
            this.height = stitchInfo.height;
            this.wrapX = stitchInfo.wrapX;
            this.wrapY = stitchInfo.wrapY;
        }

        final void assign(StitchInfo stitchInfo) {
            this.width = stitchInfo.width;
            this.height = stitchInfo.height;
            this.wrapX = stitchInfo.wrapX;
            this.wrapY = stitchInfo.wrapY;
        }

        final void doubleFrequency() {
            width *= 2;
            height *= 2;
            wrapX *= 2;
            wrapY *= 2;
            wrapX -= PerlinN;
            wrapY -= PerlinN;
        }
    }

    /**
     * Used when stitching is on
     */
    private StitchInfo stitchInfo = null;

    /**
     * Identity transform, default used when null input in generatePattern
     * @see #generatePattern
     */
    private static final AffineTransform IDENTITY = new AffineTransform();

    /**
     *  x-axis base frequency for the noise function along the x-axis
     */
    private double baseFrequencyX;

    /**
     * y-axis base frequency for the noise function along the y-axis
     */
    private double baseFrequencyY;

    /**
     * Number of octaves in the noise function
     */
    private int numOctaves;

    /**
     * Starting number for the pseudo random number generator
     */
    private int seed;

    /**
     * Defines the tile for the turbulence function, if non-null turns
     * on stitching, so frequencies are adjusted to avoid
     * discontinuities in case frequencies do not match tile
     * boundaries.  
     */
    private Rectangle2D tile;

    /**
     * Defines the tile for the turbulence function
     */
    private AffineTransform txf;

    /**
     * Defines whether the filter performs a fractal noise or a turbulence function
     */
    private boolean isFractalNoise;

    /**
     * List of channels that the generator produces.
     */
    private int channels[];

    double tx[] = { 1, 0 };

    double ty[] = { 0, 1 };

    /**
     * Produces results in the range [1, 2**31 - 2].
     * Algorithm is: r = (a * r) mod m
     * where a = 16807 and m = 2**31 - 1 = 2147483647
     * See [Park & Miller], CACM vol. 31 no. 10 p. 1195, Oct. 1988
     * To test: the algorithm should produce the result 1043618065
     * as the 10,000th generated number if the original seed is 1.
     */
    private static final int RAND_m = 2147483647;

    private static final int RAND_a = 16807;

    private static final int RAND_q = 127773;

    private static final int RAND_r = 2836;

    private static final int BSize = 0x100;

    private static final int BM = 0xff;

    private static final double PerlinN = 0x1000;

    private static final int NP = 12;

    private static final int NM = 0xfff;

    private final int latticeSelector[] = new int[BSize + 1];

    private final double gradient[] = new double[(BSize + 1) * 8];

    public double getBaseFrequencyX() {
        return baseFrequencyX;
    }

    public double getBaseFrequencyY() {
        return baseFrequencyY;
    }

    public int getNumOctaves() {
        return numOctaves;
    }

    public int getSeed() {
        return seed;
    }

    public Rectangle2D getTile() {
        return (Rectangle2D) tile.clone();
    }

    public boolean isFractalNoise() {
        return isFractalNoise;
    }

    public boolean[] getChannels() {
        boolean channels[] = new boolean[4];
        for (int i = 0; i < this.channels.length; i++) channels[this.channels[i]] = true;
        return channels;
    }

    public final int setupSeed(int seed) {
        if (seed <= 0) seed = -(seed % (RAND_m - 1)) + 1;
        if (seed > RAND_m - 1) seed = RAND_m - 1;
        return seed;
    }

    public final int random(int seed) {
        int result = RAND_a * (seed % RAND_q) - RAND_r * (seed / RAND_q);
        if (result <= 0) result += RAND_m;
        return result;
    }

    private void initLattice(int seed) {
        double u, v, s;
        int i, j, k, s1, s2;
        seed = setupSeed(seed);
        for (k = 0; k < 4; k++) {
            for (i = 0; i < BSize; i++) {
                u = (((seed = random(seed)) % (BSize + BSize)) - BSize);
                v = (((seed = random(seed)) % (BSize + BSize)) - BSize);
                s = 1 / Math.sqrt(u * u + v * v);
                gradient[i * 8 + k * 2] = u * s;
                gradient[i * 8 + k * 2 + 1] = v * s;
            }
        }
        for (i = 0; i < BSize; i++) latticeSelector[i] = i;
        while (--i > 0) {
            k = latticeSelector[i];
            j = (seed = random(seed)) % BSize;
            latticeSelector[i] = latticeSelector[j];
            latticeSelector[j] = k;
            s1 = i << 3;
            s2 = j << 3;
            for (j = 0; j < 8; j++) {
                s = gradient[s1 + j];
                gradient[s1 + j] = gradient[s2 + j];
                gradient[s2 + j] = s;
            }
        }
        latticeSelector[BSize] = latticeSelector[0];
        for (j = 0; j < 8; j++) gradient[(BSize * 8) + j] = gradient[j];
    }

    private static final double s_curve(final double t) {
        return (t * t * (3 - 2 * t));
    }

    private static final double lerp(double t, double a, double b) {
        return (a + t * (b - a));
    }

    /**
     * Generate a pixel of noise corresponding to the point vec0,vec1.
     * See class description for a high level discussion of method.
     * This handles cases where channels <= 4.  
     * @param noise The place to put the generated noise.
     * @param vec0  The X coordiate to generate noise for
     * @param vec1  The Y coordiate to generate noise for
     */
    private final void noise2(final double noise[], double vec0, double vec1) {
        int b0, b1;
        final int i, j;
        final double rx0, rx1, ry0, ry1, sx, sy;
        vec0 += PerlinN;
        b0 = ((int) vec0) & BM;
        i = latticeSelector[b0];
        j = latticeSelector[b0 + 1];
        rx0 = vec0 - (int) vec0;
        rx1 = rx0 - 1.0;
        sx = s_curve(rx0);
        vec1 += PerlinN;
        b0 = (int) vec1;
        b1 = ((j + b0) & BM) << 3;
        b0 = ((i + b0) & BM) << 3;
        ry0 = vec1 - (int) vec1;
        ry1 = ry0 - 1.0;
        sy = s_curve(ry0);
        switch(channels.length) {
            case 4:
                noise[3] = lerp(sy, lerp(sx, rx0 * gradient[b0 + 6] + ry0 * gradient[b0 + 7], rx1 * gradient[b1 + 6] + ry0 * gradient[b1 + 7]), lerp(sx, rx0 * gradient[b0 + 8 + 6] + ry1 * gradient[b0 + 8 + 7], rx1 * gradient[b1 + 8 + 6] + ry1 * gradient[b1 + 8 + 7]));
            case 3:
                noise[2] = lerp(sy, lerp(sx, rx0 * gradient[b0 + 4] + ry0 * gradient[b0 + 5], rx1 * gradient[b1 + 4] + ry0 * gradient[b1 + 5]), lerp(sx, rx0 * gradient[b0 + 8 + 4] + ry1 * gradient[b0 + 8 + 5], rx1 * gradient[b1 + 8 + 4] + ry1 * gradient[b1 + 8 + 5]));
            case 2:
                noise[1] = lerp(sy, lerp(sx, rx0 * gradient[b0 + 2] + ry0 * gradient[b0 + 3], rx1 * gradient[b1 + 2] + ry0 * gradient[b1 + 3]), lerp(sx, rx0 * gradient[b0 + 8 + 2] + ry1 * gradient[b0 + 8 + 3], rx1 * gradient[b1 + 8 + 2] + ry1 * gradient[b1 + 8 + 3]));
            case 1:
                noise[0] = lerp(sy, lerp(sx, rx0 * gradient[b0 + 0] + ry0 * gradient[b0 + 1], rx1 * gradient[b1 + 0] + ry0 * gradient[b1 + 1]), lerp(sx, rx0 * gradient[b0 + 8 + 0] + ry1 * gradient[b0 + 8 + 1], rx1 * gradient[b1 + 8 + 0] + ry1 * gradient[b1 + 8 + 1]));
        }
    }

    /**
     * This version of the noise function implements stitching.
     * If any of the lattice is on the right or bottom edge, the
     * function uses the the latice on the other side of the
     * tile, i.e., the left or right edge.
     * @param noise The place to put the generated noise.
     * @param vec0  The X coordiate to generate noise for
     * @param vec1  The Y coordiate to generate noise for
     * @param stitchInfo The stitching information for the noise function.
     */
    private final void noise2Stitch(final double noise[], final double vec0, final double vec1, final StitchInfo stitchInfo) {
        int b0, b1;
        final int i, j, b00, b10, b01, b11;
        double t;
        final double rx0, rx1, ry0, ry1, sx, sy;
        t = vec0 + PerlinN;
        b0 = ((int) t);
        b1 = b0 + 1;
        if (b1 >= stitchInfo.wrapX) {
            if (b0 >= stitchInfo.wrapX) {
                b0 -= stitchInfo.width;
                b1 -= stitchInfo.width;
            } else {
                b1 -= stitchInfo.width;
            }
        }
        i = latticeSelector[b0 & BM];
        j = latticeSelector[b1 & BM];
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0;
        sx = s_curve(rx0);
        t = vec1 + PerlinN;
        b0 = ((int) t);
        b1 = b0 + 1;
        if (b1 >= stitchInfo.wrapY) {
            if (b0 >= stitchInfo.wrapY) {
                b0 -= stitchInfo.height;
                b1 -= stitchInfo.height;
            } else {
                b1 -= stitchInfo.height;
            }
        }
        b00 = ((i + b0) & BM) << 3;
        b10 = ((j + b0) & BM) << 3;
        b01 = ((i + b1) & BM) << 3;
        b11 = ((j + b1) & BM) << 3;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0;
        sy = s_curve(ry0);
        switch(channels.length) {
            case 4:
                noise[3] = lerp(sy, lerp(sx, rx0 * gradient[b00 + 6] + ry0 * gradient[b00 + 7], rx1 * gradient[b10 + 6] + ry0 * gradient[b10 + 7]), lerp(sx, rx0 * gradient[b01 + 6] + ry1 * gradient[b01 + 7], rx1 * gradient[b11 + 6] + ry1 * gradient[b11 + 7]));
            case 3:
                noise[2] = lerp(sy, lerp(sx, rx0 * gradient[b00 + 4] + ry0 * gradient[b00 + 5], rx1 * gradient[b10 + 4] + ry0 * gradient[b10 + 5]), lerp(sx, rx0 * gradient[b01 + 4] + ry1 * gradient[b01 + 5], rx1 * gradient[b11 + 4] + ry1 * gradient[b11 + 5]));
            case 2:
                noise[1] = lerp(sy, lerp(sx, rx0 * gradient[b00 + 2] + ry0 * gradient[b00 + 3], rx1 * gradient[b10 + 2] + ry0 * gradient[b10 + 3]), lerp(sx, rx0 * gradient[b01 + 2] + ry1 * gradient[b01 + 3], rx1 * gradient[b11 + 2] + ry1 * gradient[b11 + 3]));
            case 1:
                noise[0] = lerp(sy, lerp(sx, rx0 * gradient[b00 + 0] + ry0 * gradient[b00 + 1], rx1 * gradient[b10 + 0] + ry0 * gradient[b10 + 1]), lerp(sx, rx0 * gradient[b01 + 0] + ry1 * gradient[b01 + 1], rx1 * gradient[b11 + 0] + ry1 * gradient[b11 + 1]));
        }
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.  This is
     * special case for 4 bands of output.
     *
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @return The ARGB pixel value.
     */
    private final int turbulence_4(double pointX, double pointY, final double fSum[]) {
        double n, ratio = 255;
        int i, j, b0, b1, nOctave;
        double px, py, rx0, rx1, ry0, ry1, sx, sy;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0;
        for (nOctave = numOctaves; nOctave > 0; nOctave--) {
            px = pointX + PerlinN;
            b0 = ((int) px) & BM;
            i = latticeSelector[b0];
            j = latticeSelector[b0 + 1];
            rx0 = px - (int) px;
            rx1 = rx0 - 1.0;
            sx = s_curve(rx0);
            py = pointY + PerlinN;
            b0 = ((int) py) & BM;
            b1 = (b0 + 1) & BM;
            b1 = ((j + b0) & BM) << 3;
            b0 = ((i + b0) & BM) << 3;
            ry0 = py - (int) py;
            ry1 = ry0 - 1.0;
            sy = s_curve(ry0);
            n = lerp(sy, lerp(sx, rx0 * gradient[b0 + 0] + ry0 * gradient[b0 + 1], rx1 * gradient[b1 + 0] + ry0 * gradient[b1 + 1]), lerp(sx, rx0 * gradient[b0 + 8 + 0] + ry1 * gradient[b0 + 8 + 1], rx1 * gradient[b1 + 8 + 0] + ry1 * gradient[b1 + 8 + 1]));
            if (n < 0) fSum[0] -= (n * ratio); else fSum[0] += (n * ratio);
            n = lerp(sy, lerp(sx, rx0 * gradient[b0 + 2] + ry0 * gradient[b0 + 3], rx1 * gradient[b1 + 2] + ry0 * gradient[b1 + 3]), lerp(sx, rx0 * gradient[b0 + 8 + 2] + ry1 * gradient[b0 + 8 + 3], rx1 * gradient[b1 + 8 + 2] + ry1 * gradient[b1 + 8 + 3]));
            if (n < 0) fSum[1] -= (n * ratio); else fSum[1] += (n * ratio);
            n = lerp(sy, lerp(sx, rx0 * gradient[b0 + 4] + ry0 * gradient[b0 + 5], rx1 * gradient[b1 + 4] + ry0 * gradient[b1 + 5]), lerp(sx, rx0 * gradient[b0 + 8 + 4] + ry1 * gradient[b0 + 8 + 5], rx1 * gradient[b1 + 8 + 4] + ry1 * gradient[b1 + 8 + 5]));
            if (n < 0) fSum[2] -= (n * ratio); else fSum[2] += (n * ratio);
            n = lerp(sy, lerp(sx, rx0 * gradient[b0 + 6] + ry0 * gradient[b0 + 7], rx1 * gradient[b1 + 6] + ry0 * gradient[b1 + 7]), lerp(sx, rx0 * gradient[b0 + 8 + 6] + ry1 * gradient[b0 + 8 + 7], rx1 * gradient[b1 + 8 + 6] + ry1 * gradient[b1 + 8 + 7]));
            if (n < 0) fSum[3] -= (n * ratio); else fSum[3] += (n * ratio);
            ratio *= .5;
            pointX *= 2;
            pointY *= 2;
        }
        i = (int) fSum[0];
        if ((i & 0xFFFFFF00) == 0) j = i << 16; else j = ((i & 0x80000000) != 0) ? 0 : 0xFF0000;
        i = (int) fSum[1];
        if ((i & 0xFFFFFF00) == 0) j |= i << 8; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF00;
        i = (int) fSum[2];
        if ((i & 0xFFFFFF00) == 0) j |= i; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF;
        i = (int) fSum[3];
        if ((i & 0xFFFFFF00) == 0) j |= i << 24; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF000000;
        return j;
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.
     * @param rgb array for the four color components
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @param noise array used to avoid reallocating double array for
     *        each pixel
     */
    private final void turbulence(final int rgb[], double pointX, double pointY, final double fSum[], final double noise[]) {
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0;
        double ratio = 255;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        switch(channels.length) {
            case 4:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2(noise, pointX, pointY);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[2] < 0) fSum[2] -= (noise[2] * ratio); else fSum[2] += (noise[2] * ratio);
                    if (noise[3] < 0) fSum[3] -= (noise[3] * ratio); else fSum[3] += (noise[3] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                }
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                rgb[1] = (int) fSum[1];
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[2] = (int) fSum[2];
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
                rgb[3] = (int) fSum[3];
                if ((rgb[3] & 0xFFFFFF00) != 0) rgb[3] = ((rgb[3] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 3:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2(noise, pointX, pointY);
                    if (noise[2] < 0) fSum[2] -= (noise[2] * ratio); else fSum[2] += (noise[2] * ratio);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                }
                rgb[2] = (int) fSum[2];
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
                rgb[1] = (int) fSum[1];
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 2:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2(noise, pointX, pointY);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                }
                rgb[1] = (int) fSum[1];
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 1:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2(noise, pointX, pointY);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                }
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
        }
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.
     * @param rgb array for the four color components
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @param noise array used to avoid reallocating double array for
     * each pixel
     * @param stitchInfo The stitching information for the noise function
     */
    private final void turbulenceStitch(final int rgb[], double pointX, double pointY, final double fSum[], final double noise[], StitchInfo stitchInfo) {
        double ratio = 1;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0;
        switch(channels.length) {
            case 4:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2Stitch(noise, pointX, pointY, stitchInfo);
                    if (noise[3] < 0) fSum[3] -= (noise[3] * ratio); else fSum[3] += (noise[3] * ratio);
                    if (noise[2] < 0) fSum[2] -= (noise[2] * ratio); else fSum[2] += (noise[2] * ratio);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                    stitchInfo.doubleFrequency();
                }
                rgb[3] = (int) (fSum[3] * 255);
                if ((rgb[3] & 0xFFFFFF00) != 0) rgb[3] = ((rgb[3] & 0x80000000) != 0) ? 0 : 255;
                rgb[2] = (int) (fSum[2] * 255);
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
                rgb[1] = (int) (fSum[1] * 255);
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[0] = (int) (fSum[0] * 255);
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 3:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2Stitch(noise, pointX, pointY, stitchInfo);
                    if (noise[2] < 0) fSum[2] -= (noise[2] * ratio); else fSum[2] += (noise[2] * ratio);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                    stitchInfo.doubleFrequency();
                }
                rgb[2] = (int) (fSum[2] * 255);
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
                rgb[1] = (int) (fSum[1] * 255);
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[0] = (int) (fSum[0] * 255);
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 2:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2Stitch(noise, pointX, pointY, stitchInfo);
                    if (noise[1] < 0) fSum[1] -= (noise[1] * ratio); else fSum[1] += (noise[1] * ratio);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                    stitchInfo.doubleFrequency();
                }
                rgb[1] = (int) (fSum[1] * 255);
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
                rgb[0] = (int) (fSum[0] * 255);
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
            case 1:
                for (int nOctave = 0; nOctave < numOctaves; nOctave++) {
                    noise2Stitch(noise, pointX, pointY, stitchInfo);
                    if (noise[0] < 0) fSum[0] -= (noise[0] * ratio); else fSum[0] += (noise[0] * ratio);
                    ratio *= .5;
                    pointX *= 2;
                    pointY *= 2;
                    stitchInfo.doubleFrequency();
                }
                rgb[0] = (int) (fSum[0] * 255);
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
                break;
        }
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.  This handles the
     * case where we are generating 4 channels of noise.
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @return The ARGB pixel
     */
    private final int turbulenceFractal_4(double pointX, double pointY, final double fSum[]) {
        int b0, b1, nOctave, i, j;
        double px, py, rx0, rx1, ry0, ry1, sx, sy, ratio = 127.5;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;
        for (nOctave = numOctaves; nOctave > 0; nOctave--) {
            px = pointX + PerlinN;
            b0 = ((int) px) & BM;
            i = latticeSelector[b0];
            j = latticeSelector[b0 + 1];
            rx0 = px - (int) px;
            rx1 = rx0 - 1.0;
            sx = s_curve(rx0);
            py = pointY + PerlinN;
            b0 = ((int) py) & BM;
            b1 = (b0 + 1) & BM;
            b1 = ((j + b0) & BM) << 3;
            b0 = ((i + b0) & BM) << 3;
            ry0 = py - (int) py;
            ry1 = ry0 - 1.0;
            sy = s_curve(ry0);
            fSum[0] += lerp(sy, lerp(sx, rx0 * gradient[b0 + 0] + ry0 * gradient[b0 + 1], rx1 * gradient[b1 + 0] + ry0 * gradient[b1 + 1]), lerp(sx, rx0 * gradient[b0 + 8 + 0] + ry1 * gradient[b0 + 8 + 1], rx1 * gradient[b1 + 8 + 0] + ry1 * gradient[b1 + 8 + 1])) * ratio;
            fSum[1] += lerp(sy, lerp(sx, rx0 * gradient[b0 + 2] + ry0 * gradient[b0 + 3], rx1 * gradient[b1 + 2] + ry0 * gradient[b1 + 3]), lerp(sx, rx0 * gradient[b0 + 8 + 2] + ry1 * gradient[b0 + 8 + 3], rx1 * gradient[b1 + 8 + 2] + ry1 * gradient[b1 + 8 + 3])) * ratio;
            fSum[2] += lerp(sy, lerp(sx, rx0 * gradient[b0 + 4] + ry0 * gradient[b0 + 5], rx1 * gradient[b1 + 4] + ry0 * gradient[b1 + 5]), lerp(sx, rx0 * gradient[b0 + 8 + 4] + ry1 * gradient[b0 + 8 + 5], rx1 * gradient[b1 + 8 + 4] + ry1 * gradient[b1 + 8 + 5])) * ratio;
            fSum[3] += lerp(sy, lerp(sx, rx0 * gradient[b0 + 6] + ry0 * gradient[b0 + 7], rx1 * gradient[b1 + 6] + ry0 * gradient[b1 + 7]), lerp(sx, rx0 * gradient[b0 + 8 + 6] + ry1 * gradient[b0 + 8 + 7], rx1 * gradient[b1 + 8 + 6] + ry1 * gradient[b1 + 8 + 7])) * ratio;
            ratio *= .5;
            pointX *= 2;
            pointY *= 2;
        }
        i = (int) fSum[0];
        if ((i & 0xFFFFFF00) == 0) j = i << 16; else j = ((i & 0x80000000) != 0) ? 0 : 0xFF0000;
        i = (int) fSum[1];
        if ((i & 0xFFFFFF00) == 0) j |= i << 8; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF00;
        i = (int) fSum[2];
        if ((i & 0xFFFFFF00) == 0) j |= i; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF;
        i = (int) fSum[3];
        if ((i & 0xFFFFFF00) == 0) j |= i << 24; else j |= ((i & 0x80000000) != 0) ? 0 : 0xFF000000;
        return j;
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.
     * @param rgb array for the four color components
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @param noise array used to avoid reallocating double array for
     * each pixel 
     */
    private final void turbulenceFractal(final int rgb[], double pointX, double pointY, final double fSum[], final double noise[]) {
        double ratio = 127.5;
        int nOctave;
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        for (nOctave = numOctaves; nOctave > 0; nOctave--) {
            noise2(noise, pointX, pointY);
            switch(channels.length) {
                case 4:
                    fSum[3] += (noise[3] * ratio);
                case 3:
                    fSum[2] += (noise[2] * ratio);
                case 2:
                    fSum[1] += (noise[1] * ratio);
                case 1:
                    fSum[0] += (noise[0] * ratio);
            }
            ratio *= .5;
            pointX *= 2;
            pointY *= 2;
        }
        switch(channels.length) {
            case 4:
                rgb[3] = (int) fSum[3];
                if ((rgb[3] & 0xFFFFFF00) != 0) rgb[3] = ((rgb[3] & 0x80000000) != 0) ? 0 : 255;
            case 3:
                rgb[2] = (int) fSum[2];
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
            case 2:
                rgb[1] = (int) fSum[1];
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
            case 1:
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
        }
    }

    /**
     * This is the heart of the turbulence calculation. It returns
     * 'turbFunctionResult', as defined in the spec.
     * @param rgb array for the four color components
     * @param point x and y coordinates of the point to process.
     * @param fSum array used to avoid reallocating double array for each pixel
     * @param noise array used to avoid reallocating double array for
     * each pixel
     * @param stitchInfo The stitching information for the noise function
     */
    private final void turbulenceFractalStitch(final int rgb[], double pointX, double pointY, final double fSum[], final double noise[], StitchInfo stitchInfo) {
        double ratio = 127.5;
        int nOctave;
        fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;
        pointX *= baseFrequencyX;
        pointY *= baseFrequencyY;
        for (nOctave = numOctaves; nOctave > 0; nOctave--) {
            noise2Stitch(noise, pointX, pointY, stitchInfo);
            switch(channels.length) {
                case 4:
                    fSum[3] += (noise[3] * ratio);
                case 3:
                    fSum[2] += (noise[2] * ratio);
                case 2:
                    fSum[1] += (noise[1] * ratio);
                case 1:
                    fSum[0] += (noise[0] * ratio);
            }
            ratio *= .5;
            pointX *= 2;
            pointY *= 2;
            stitchInfo.doubleFrequency();
        }
        switch(channels.length) {
            case 4:
                rgb[3] = (int) fSum[3];
                if ((rgb[3] & 0xFFFFFF00) != 0) rgb[3] = ((rgb[3] & 0x80000000) != 0) ? 0 : 255;
            case 3:
                rgb[2] = (int) fSum[2];
                if ((rgb[2] & 0xFFFFFF00) != 0) rgb[2] = ((rgb[2] & 0x80000000) != 0) ? 0 : 255;
            case 2:
                rgb[1] = (int) fSum[1];
                if ((rgb[1] & 0xFFFFFF00) != 0) rgb[1] = ((rgb[1] & 0x80000000) != 0) ? 0 : 255;
            case 1:
                rgb[0] = (int) fSum[0];
                if ((rgb[0] & 0xFFFFFF00) != 0) rgb[0] = ((rgb[0] & 0x80000000) != 0) ? 0 : 255;
        }
    }

    /**
     * Generates a Perlin noise pattern into dest Raster.
     * @param dest Raster to fill with the pattern.
     */
    public WritableRaster copyData(WritableRaster dest) {
        if (dest == null) throw new IllegalArgumentException("Cannot generate a noise pattern into a null raster");
        int w = dest.getWidth();
        int h = dest.getHeight();
        DataBufferInt dstDB = (DataBufferInt) dest.getDataBuffer();
        SinglePixelPackedSampleModel sppsm;
        int minX = dest.getMinX();
        int minY = dest.getMinY();
        sppsm = (SinglePixelPackedSampleModel) dest.getSampleModel();
        int dstOff = dstDB.getOffset() + sppsm.getOffset(minX - dest.getSampleModelTranslateX(), minY - dest.getSampleModelTranslateY());
        final int destPixels[] = dstDB.getBankData()[0];
        int dstAdjust = sppsm.getScanlineStride() - w;
        int i, end, dp = dstOff;
        final int rgb[] = new int[4];
        final double fSum[] = { 0, 0, 0, 0 };
        final double noise[] = { 0, 0, 0, 0 };
        final double tx0, tx1, ty0, ty1;
        tx0 = tx[0];
        tx1 = tx[1];
        ty0 = ty[0] - (w * tx0);
        ty1 = ty[1] - (w * tx1);
        double p[] = { minX, minY };
        txf.transform(p, 0, p, 0, 1);
        double point_0 = p[0];
        double point_1 = p[1];
        if (isFractalNoise) {
            if (stitchInfo == null) {
                if (channels.length == 4) {
                    for (i = 0; i < h; i++) {
                        for (end = dp + w; dp < end; dp++) {
                            destPixels[dp] = turbulenceFractal_4(point_0, point_1, fSum);
                            point_0 += tx0;
                            point_1 += tx1;
                        }
                        point_0 += ty0;
                        point_1 += ty1;
                        dp += dstAdjust;
                    }
                } else {
                    for (i = 0; i < h; i++) {
                        for (end = dp + w; dp < end; dp++) {
                            turbulenceFractal(rgb, point_0, point_1, fSum, noise);
                            destPixels[dp] = ((rgb[3] << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]));
                            point_0 += tx0;
                            point_1 += tx1;
                        }
                        point_0 += ty0;
                        point_1 += ty1;
                        dp += dstAdjust;
                    }
                }
            } else {
                StitchInfo si = new StitchInfo();
                for (i = 0; i < h; i++) {
                    for (end = dp + w; dp < end; dp++) {
                        si.assign(this.stitchInfo);
                        turbulenceFractalStitch(rgb, point_0, point_1, fSum, noise, si);
                        destPixels[dp] = ((rgb[3] << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]));
                        point_0 += tx0;
                        point_1 += tx1;
                    }
                    point_0 += ty0;
                    point_1 += ty1;
                    dp += dstAdjust;
                }
            }
        } else {
            if (stitchInfo == null) {
                if (channels.length == 4) {
                    for (i = 0; i < h; i++) {
                        for (end = dp + w; dp < end; dp++) {
                            destPixels[dp] = turbulence_4(point_0, point_1, fSum);
                            point_0 += tx0;
                            point_1 += tx1;
                        }
                        point_0 += ty0;
                        point_1 += ty1;
                        dp += dstAdjust;
                    }
                } else {
                    for (i = 0; i < h; i++) {
                        for (end = dp + w; dp < end; dp++) {
                            turbulence(rgb, point_0, point_1, fSum, noise);
                            destPixels[dp] = ((rgb[3] << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]));
                            point_0 += tx0;
                            point_1 += tx1;
                        }
                        point_0 += ty0;
                        point_1 += ty1;
                        dp += dstAdjust;
                    }
                }
            } else {
                StitchInfo si = new StitchInfo();
                for (i = 0; i < h; i++) {
                    for (end = dp + w; dp < end; dp++) {
                        si.assign(this.stitchInfo);
                        turbulenceStitch(rgb, point_0, point_1, fSum, noise, si);
                        destPixels[dp] = ((rgb[3] << 24) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]));
                        point_0 += tx0;
                        point_1 += tx1;
                    }
                    point_0 += ty0;
                    point_1 += ty1;
                    dp += dstAdjust;
                }
            }
        }
        return dest;
    }

    /**
     * @param baseFrequencyX x-axis base frequency for the noise
     * function along the x-axis
     * @param baseFrequencyY y-axis base frequency for the noise
     *        function along the x-axis
     * @param numOctaves number of octaves in the noise
     *        function. Positive integral value.
     * @param seed starting number for the pseudo random number generator
     * @param isFractalNoise defines whether the filter performs a
     *        fractal noise or a turbulence function.
     * @param tile defines the tile size. May be null if stitchTiles
     *        is false. Otherwise, should not be null.
     * @param txf The affine transform from device to user space.
     * @param cs The Colorspace to output. 
     * @param alpha True if the data should have an alpha channel.
     */
    public TurbulencePatternRed(double baseFrequencyX, double baseFrequencyY, int numOctaves, int seed, boolean isFractalNoise, Rectangle2D tile, AffineTransform txf, Rectangle devRect, ColorSpace cs, boolean alpha) {
        this.baseFrequencyX = baseFrequencyX;
        this.baseFrequencyY = baseFrequencyY;
        this.seed = seed;
        this.isFractalNoise = isFractalNoise;
        this.tile = tile;
        this.txf = txf;
        if (this.txf == null) this.txf = IDENTITY;
        int nChannels = cs.getNumComponents();
        if (alpha) nChannels++;
        channels = new int[nChannels];
        for (int i = 0; i < channels.length; i++) channels[i] = i;
        txf.deltaTransform(tx, 0, tx, 0, 1);
        txf.deltaTransform(ty, 0, ty, 0, 1);
        double vecX[] = { .5, 0 };
        double vecY[] = { 0, .5 };
        txf.deltaTransform(vecX, 0, vecX, 0, 1);
        txf.deltaTransform(vecY, 0, vecY, 0, 1);
        double dx = Math.max(Math.abs(vecX[0]), Math.abs(vecY[0]));
        int maxX = -(int) Math.round((Math.log(dx) + Math.log(baseFrequencyX)) / Math.log(2));
        double dy = Math.max(Math.abs(vecX[1]), Math.abs(vecY[1]));
        int maxY = -(int) Math.round((Math.log(dy) + Math.log(baseFrequencyY)) / Math.log(2));
        this.numOctaves = numOctaves > maxX ? maxX : numOctaves;
        this.numOctaves = this.numOctaves > maxY ? maxY : this.numOctaves;
        if (this.numOctaves < 1 && numOctaves > 1) this.numOctaves = 1;
        if (this.numOctaves > 8) this.numOctaves = 8;
        if (tile != null) {
            double lowFreq = Math.floor(tile.getWidth() * baseFrequencyX) / tile.getWidth();
            double highFreq = Math.ceil(tile.getWidth() * baseFrequencyX) / tile.getWidth();
            if (baseFrequencyX / lowFreq < highFreq / baseFrequencyX) this.baseFrequencyX = lowFreq; else this.baseFrequencyX = highFreq;
            lowFreq = Math.floor(tile.getHeight() * baseFrequencyY) / tile.getHeight();
            highFreq = Math.ceil(tile.getHeight() * baseFrequencyY) / tile.getHeight();
            if (baseFrequencyY / lowFreq < highFreq / baseFrequencyY) this.baseFrequencyY = lowFreq; else this.baseFrequencyY = highFreq;
            stitchInfo = new StitchInfo();
            stitchInfo.width = ((int) (tile.getWidth() * this.baseFrequencyX));
            stitchInfo.height = ((int) (tile.getHeight() * this.baseFrequencyY));
            stitchInfo.wrapX = ((int) (tile.getX() * this.baseFrequencyX + PerlinN + stitchInfo.width));
            stitchInfo.wrapY = ((int) (tile.getY() * this.baseFrequencyY + PerlinN + stitchInfo.height));
            if (stitchInfo.width == 0) stitchInfo.width = 1;
            if (stitchInfo.height == 0) stitchInfo.height = 1;
        }
        initLattice(seed);
        ColorModel cm;
        if (alpha) cm = new DirectColorModel(cs, 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000, false, DataBuffer.TYPE_INT); else cm = new DirectColorModel(cs, 24, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x0, false, DataBuffer.TYPE_INT);
        int tileSize = AbstractTiledRed.getDefaultTileSize();
        init((CachableRed) null, devRect, cm, cm.createCompatibleSampleModel(tileSize, tileSize), 0, 0, null);
    }
}
