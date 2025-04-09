package VolumeJ;

import java.awt.*;
import volume.*;
import ij.process.*;
import ij.ImagePlus;

/**
 * VJIsosurfaceRender.
 *
 * For patenting and copyrighting reasons all informative Javadoc comments have been removed.
 *
 * Copyright (c) 2001-2003, Michael Abramoff. All rights reserved.
 * @author: Michael Abramoff
 *
 * Note: this is not open source software!
 * These algorithms, source code, documentation or any derived programs ('the software')
 * are the intellectual property of Michael Abramoff.
 * Michael Abramoff asserts his right as the sole owner of the rights
 * to this software.
 * You and/or any person(s) acting with or for you may not:
 * - directly or indirectly copy, sell, lease, rent, license,
 * sublicense, redistribute, lend, give, transfer or otherwise distribute or
 * use the software
 * - modify, translate, or create derivative works from the software, assign or
 * otherwise transfer rights to the Software or use the Software for timesharing
 * or service bureau purposes
 * - reverse engineer, decompile, disassemble or otherwise attempt to discover the
 * source code or underlying ideas or algorithms of the Software or any subsequent
 * version thereof or any part thereof.
 * Commercial licensing of the software is available by contacting the author.
 * THE SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 */
public class VJIsosurfaceRender extends VJRenderer {

    private static final float EPSILON = 0.0001f;

    private VJBinaryShell shell;

    /**
	 * Create a new renderer with specified methods.
	 * @param interpolator the interpolator that will interpolate VJValues from the volume to be rendered.
	 * @param shader the VJShader that will be used for shading.
	 * @param classifier the VJClassifier that will be used for classifying.
	 * @throws IllegalArgumentException if parameters not properly defined.
	 */
    public VJIsosurfaceRender(VJInterpolator interpolator, VJShader shader, VJClassifier classifier) throws IllegalArgumentException {
        super(interpolator, shader, classifier);
    }

    /**
	 * The volume has changed. Reset the volume size and the binary shell.
	 * @param volume the new volume.
	 */
    public void setVolume(Volume v) {
        this.v = v;
        VJThresholdedVolume tv = new VJThresholdedVolume(v, classifier);
        shell = new VJBinaryShell(tv);
    }

    /**
	 *  Variables are all inherited from VJRenderer
	*/
    public synchronized void run() {
        running = true;
        float iso = (float) classifier.getThreshold();
        newViewportBuffer();
        VJValue sample0 = null;
        if (outputType == COLORINT && v instanceof VolumeRGB) sample0 = (VJValue) (new VJValueHSB()); else sample0 = new VJValue();
        VJValue sample1 = new VJValue();
        float[] istep = mi.getColumn(0);
        float[] jstep = mi.getColumn(1);
        float[] kstep = mi.getColumn(2);
        float[] cstep = mi.getColumn(3);
        float dx = (depth - 1) * kstep[0];
        float dy = (depth - 1) * kstep[1];
        float dz = (depth - 1) * kstep[2];
        VJCell cell = new VJCell(m, mi);
        float[] rayvs = VJMatrix.newVector(ioffset, joffset, koffset);
        float[] rayos = mi.mul(rayvs);
        float ckx = koffset * kstep[0] + joffset * jstep[0] + ioffset * istep[0] + cstep[0];
        float cky = koffset * kstep[1] + joffset * jstep[1] + ioffset * istep[1] + cstep[1];
        float ckz = koffset * kstep[2] + joffset * jstep[2] + ioffset * istep[2] + cstep[2];
        shell.advancePrepare(dx, dy, dz);
        long start = System.currentTimeMillis();
        for (int j = 0; j < height && running; j++) {
            float ox = ckx + j * jstep[0];
            float oy = cky + j * jstep[1];
            float oz = ckz + j * jstep[2];
            for (int i = 0; i < width; i++) {
                cell.move(ox, oy, oz);
                int max = shell.advanceInit(cell, ox, oy, oz);
                while (true) {
                    int k;
                    if (onTrace(i, j)) {
                        k = shell.advanceToSurfaceTracing(cell);
                        trace("(" + sequenceNumber + ")" + i + "," + j + ": " + shell.s);
                    } else k = shell.advanceToSurface(cell);
                    if (k >= max) break; else if (interpolator.isValidGradient(cell, v)) {
                        float[] kintersect;
                        if (onTrace(i, j)) {
                            kintersect = cell.intersectTracing(ioffset + i, joffset + j, koffset + k);
                            trace("(" + sequenceNumber + ")" + i + "," + j + ": " + cell.s);
                        } else kintersect = cell.intersect(ioffset + i, joffset + j, koffset + k);
                        if (kintersect != null) {
                            VJVoxelLoc vlk0 = new VJVoxelLoc(ox + (kintersect[0] - rayvs[2]) * kstep[0], oy + (kintersect[0] - rayvs[2]) * kstep[1], oz + (kintersect[0] - rayvs[2]) * kstep[2]);
                            VJVoxelLoc vlk1 = new VJVoxelLoc(ox + (kintersect[1] - rayvs[2]) * kstep[0], oy + (kintersect[1] - rayvs[2]) * kstep[1], oz + (kintersect[1] - rayvs[2]) * kstep[2]);
                            float k0sample = interpolator.value(sample0, v, vlk0).floatvalue;
                            float k1sample = interpolator.value(sample1, v, vlk1).floatvalue;
                            if (iso >= k0sample && iso <= k1sample || iso >= k1sample && iso <= k0sample) {
                                VJVoxelLoc vl = bisection(sample0, vlk0, iso, kintersect[0], kintersect[1], k0sample, k1sample, kstep, 3);
                                VJGradient g = interpolator.gradient(v, vl);
                                g.normalize();
                                VJShade shade = shader.shade(g);
                                int pixel;
                                if (sample0 instanceof VJValueHSB) {
                                    VJValueHSB samplehsb = (VJValueHSB) sample0;
                                    interpolator.valueHS(samplehsb, (VolumeRGB) v, iso, vl);
                                    pixel = java.awt.Color.HSBtoRGB(samplehsb.getHue(), samplehsb.getSaturation(), shade.get());
                                } else pixel = (int) (shade.get() * 255.0);
                                setPixel(pixel, i, j);
                                break;
                            } else if (onTrace(i, j)) trace("(" + sequenceNumber + ") failed iso bracket at " + vlk0 + "," + vlk1 + " samples " + k0sample + ", " + k1sample);
                        }
                    }
                }
                ox += istep[0];
                oy += istep[1];
                oz += istep[2];
            }
            yield();
        }
        VJUserInterface.progress(1f);
        traceWrite();
        pixelms = (float) (System.currentTimeMillis() - start) / (float) (width * height);
        running = false;
    }

    /**
	 * Do bisection for a number of steps.
	 * @param vlk0 the ray position at k0 in objectspace coordinates.
	 * @param iso the value of the isosurface
	 * @param k0, k1 the positions on the ray you want to bisect between.
	 * @param k1sample, k2sample the volume sample values at k1 and k2.
	 * @param kstep a float[4] with for x,y,z the changes in objectspace coordinates for a step in k-direction
	 * in viewspace.
	 * @param steps the number of bisection steps.
	 * @return a VJVoxelLoc postioned on the ray at the final bisection. This is the position
	 * on the ray where the volume sample is closest to iso.
	 */
    private VJVoxelLoc bisection(VJValue sample, VJVoxelLoc vlk0, float iso, float k0, float k1, float k0sample, float k1sample, float[] kstep, int steps) {
        float basek0 = k0;
        float basek1 = k1;
        float base0sample = k0sample;
        float base1sample = k1sample;
        VJVoxelLoc vl = null;
        for (int s = 0; s < steps; s++) {
            float m = k0 + (k1 - k0) / 2;
            float mdiff = m - basek0;
            vl = new VJVoxelLoc(vlk0.x + mdiff * (float) kstep[0], vlk0.y + mdiff * (float) kstep[1], vlk0.z + mdiff * (float) kstep[2]);
            float samplef = interpolator.value(sample, v, vl).floatvalue;
            float dk0 = k0sample - iso;
            float dk1 = k1sample - iso;
            float diso = samplef - iso;
            if (diso < EPSILON) return vl;
            if (dk0 * diso > 0.0) {
                k0sample = samplef;
                k0 = m;
            } else if (dk1 * diso > 0.0) {
                k1sample = samplef;
                k1 = m;
            } else if (Math.abs(dk0) >= Math.abs(dk1)) {
                k0sample = samplef;
                k0 = m;
            } else {
                k1sample = samplef;
                k1 = m;
            }
        }
        float sampleDiff = (k1sample - k0sample);
        if (Math.abs(sampleDiff) > 0.0) {
            float f = (iso - k0sample) / sampleDiff;
            float t = k0 + (k1 - k0) * f;
            float tshift = t - basek0;
            vl = new VJVoxelLoc(vlk0.x + tshift * (float) kstep[0], vlk0.y + tshift * (float) kstep[1], vlk0.z + tshift * (float) kstep[2]);
        }
        return vl;
    }

    public static String desc() {
        return "Isosurface";
    }
}
