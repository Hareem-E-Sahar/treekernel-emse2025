package org.jmol.g3d;

/**
 *<p>
 * Draws shaded cylinders in 3D.
 *</p>
 *<p>
 * Cylinders are used to draw bonds.
 *</p>
 *
 * @author Miguel, miguel@jmol.org
 */
class Cylinder3D {

    Graphics3D g3d;

    Cylinder3D(Graphics3D g3d) {
        this.g3d = g3d;
    }

    private short colixA, colixB;

    private int[] shadesA;

    private boolean isScreenedA;

    private int[] shadesB;

    private boolean isScreenedB;

    private int xA, yA, zA;

    private int dxB, dyB, dzB;

    private boolean tEvenDiameter;

    private int diameter;

    private byte endcaps;

    private boolean tEndcapOpen;

    private int xEndcap, yEndcap, zEndcap;

    private int argbEndcap;

    private short colixEndcap;

    private int intensityEndcap;

    private float radius, radius2, cosTheta, cosPhi, sinPhi;

    int sampleCount;

    void render(short colixA, short colixB, byte endcaps, int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        if (isFullyClipped(diameter, xA, yA, zA, xB, yB, zB)) return;
        this.dxB = xB - xA;
        this.dyB = yB - yA;
        this.dzB = zB - zA;
        if (diameter <= 1) {
            g3d.plotLineDelta(colixA, colixB, xA, yA, zA, dxB, dyB, dzB);
            return;
        }
        this.diameter = diameter;
        this.xA = xA;
        this.yA = yA;
        this.zA = zA;
        this.shadesA = g3d.getShades(this.colixA = colixA);
        this.shadesB = g3d.getShades(this.colixB = colixB);
        this.isScreenedA = (colixA & Graphics3D.TRANSLUCENT_MASK) != 0;
        this.isScreenedB = (colixB & Graphics3D.TRANSLUCENT_MASK) != 0;
        this.endcaps = endcaps;
        calcArgbEndcap(true);
        generateBaseEllipse();
        if (endcaps == Graphics3D.ENDCAPS_FLAT) renderFlatEndcap(true);
        for (int i = rasterCount; --i >= 0; ) plotRaster(i);
        if (endcaps == Graphics3D.ENDCAPS_SPHERICAL) renderSphericalEndcaps();
    }

    void generateBaseEllipse() {
        tEvenDiameter = (diameter & 1) == 0;
        radius = diameter / 2.0f;
        radius2 = radius * radius;
        int mag2d2 = dxB * dxB + dyB * dyB;
        if (mag2d2 == 0) {
            cosTheta = 1;
            cosPhi = 1;
            sinPhi = 0;
        } else {
            float mag2d = (float) Math.sqrt(mag2d2);
            float mag3d = (float) Math.sqrt(mag2d2 + dzB * dzB);
            cosTheta = dzB / mag3d;
            cosPhi = dxB / mag2d;
            sinPhi = dyB / mag2d;
        }
        calcRotatedPoint(0f, 0);
        calcRotatedPoint(0.5f, 1);
        calcRotatedPoint(1f, 2);
        rasterCount = 3;
        interpolate(0, 1);
        interpolate(1, 2);
    }

    boolean isFullyClipped(int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        int slab = g3d.slab;
        int depth = g3d.depth;
        int width = g3d.width;
        int height = g3d.height;
        int r = diameter / 2 + 1;
        int xMinA = xA - r, xMaxA = xA + r;
        int xMinB = xB - r, xMaxB = xB + r;
        if (xMaxA < 0 && xMaxB < 0 || xMinA >= width && xMinB >= width) return true;
        int yMinA = yA - r, yMaxA = yA + r;
        int yMinB = yB - r, yMaxB = yB + r;
        if (yMaxA < 0 && yMaxB < 0 || yMinA >= height && yMinB >= height) return true;
        int zMinA = zA - r, zMaxA = zA + r;
        int zMinB = zB - r, zMaxB = zB + r;
        if (zMaxA < slab && zMaxB < slab || zMinA >= depth && zMinB >= depth) return true;
        return false;
    }

    void interpolate(int iLower, int iUpper) {
        int dx = xRaster[iUpper] - xRaster[iLower];
        if (dx < 0) dx = -dx;
        int dy = yRaster[iUpper] - yRaster[iLower];
        if (dy < 0) dy = -dy;
        if ((dx + dy) <= 1) return;
        float tLower = tRaster[iLower];
        float tUpper = tRaster[iUpper];
        int iMid = allocRaster();
        for (int j = 4; --j >= 0; ) {
            float tMid = (tLower + tUpper) / 2;
            calcRotatedPoint(tMid, iMid);
            if ((xRaster[iMid] == xRaster[iLower]) && (yRaster[iMid] == yRaster[iLower])) {
                fp8IntensityUp[iLower] = (fp8IntensityUp[iLower] + fp8IntensityUp[iMid]) / 2;
                tLower = tMid;
            } else if ((xRaster[iMid] == xRaster[iUpper]) && (yRaster[iMid] == yRaster[iUpper])) {
                fp8IntensityUp[iUpper] = (fp8IntensityUp[iUpper] + fp8IntensityUp[iMid]) / 2;
                tUpper = tMid;
            } else {
                interpolate(iLower, iMid);
                interpolate(iMid, iUpper);
                return;
            }
        }
        xRaster[iMid] = xRaster[iLower];
        yRaster[iMid] = yRaster[iUpper];
    }

    void plotRaster(int i) {
        int fp8Up = fp8IntensityUp[i];
        int x = xRaster[i];
        int y = yRaster[i];
        int z = zRaster[i];
        if (tEndcapOpen) {
            g3d.plotPixelClipped(argbEndcap, xEndcap + x, yEndcap + y, zEndcap - z - 1);
            g3d.plotPixelClipped(argbEndcap, xEndcap - x, yEndcap - y, zEndcap + z - 1);
        }
        g3d.plotLineDelta(shadesA, isScreenedA, shadesB, isScreenedB, fp8Up, xA + x, yA + y, zA - z, dxB, dyB, dzB);
        if (endcaps == Graphics3D.ENDCAPS_OPEN) {
            g3d.plotLineDelta(shadesA[0], isScreenedA, shadesB[0], isScreenedB, xA - x, yA - y, zA + z, dxB, dyB, dzB);
        }
    }

    int[] realloc(int[] a) {
        int[] t;
        t = new int[a.length * 2];
        System.arraycopy(a, 0, t, 0, a.length);
        return t;
    }

    float[] realloc(float[] a) {
        float[] t;
        t = new float[a.length * 2];
        System.arraycopy(a, 0, t, 0, a.length);
        return t;
    }

    int allocRaster() {
        if (rasterCount == xRaster.length) {
            xRaster = realloc(xRaster);
            yRaster = realloc(yRaster);
            zRaster = realloc(zRaster);
            tRaster = realloc(tRaster);
            fp8IntensityUp = realloc(fp8IntensityUp);
        }
        return rasterCount++;
    }

    int rasterCount;

    float[] tRaster = new float[32];

    int[] xRaster = new int[32];

    int[] yRaster = new int[32];

    int[] zRaster = new int[32];

    int[] fp8IntensityUp = new int[32];

    void calcRotatedPoint(float t, int i) {
        tRaster[i] = t;
        double tPI = t * Math.PI;
        double xT = Math.sin(tPI) * cosTheta;
        double yT = Math.cos(tPI);
        double xR = radius * (xT * cosPhi - yT * sinPhi);
        double yR = radius * (xT * sinPhi + yT * cosPhi);
        double z2 = radius2 - (xR * xR + yR * yR);
        double zR = (z2 > 0 ? Math.sqrt(z2) : 0);
        if (tEvenDiameter) {
            xRaster[i] = (int) (xR - 0.5);
            yRaster[i] = (int) (yR - 0.5);
        } else {
            xRaster[i] = (int) (xR);
            yRaster[i] = (int) (yR);
        }
        zRaster[i] = (int) (zR + 0.5);
        fp8IntensityUp[i] = Shade3D.calcFp8Intensity((float) xR, (float) yR, (float) zR);
    }

    int yMin, yMax;

    int xMin, xMax;

    int zXMin, zXMax;

    void findMinMaxY() {
        yMin = yMax = yRaster[0];
        for (int i = rasterCount; --i > 0; ) {
            int y = yRaster[i];
            if (y < yMin) yMin = y; else if (y > yMax) yMax = y; else {
                y = -y;
                if (y < yMin) yMin = y; else if (y > yMax) yMax = y;
            }
        }
    }

    void findMinMaxX(int y) {
        xMin = Integer.MAX_VALUE;
        xMax = Integer.MIN_VALUE;
        for (int i = rasterCount; --i >= 0; ) {
            if (yRaster[i] == y) {
                int x = xRaster[i];
                if (x < xMin) {
                    xMin = x;
                    zXMin = zRaster[i];
                }
                if (x > xMax) {
                    xMax = x;
                    zXMax = zRaster[i];
                }
            }
            if (yRaster[i] == -y) {
                int x = -xRaster[i];
                if (x < xMin) {
                    xMin = x;
                    zXMin = -zRaster[i];
                }
                if (x > xMax) {
                    xMax = x;
                    zXMax = -zRaster[i];
                }
            }
        }
    }

    void renderFlatEndcap(boolean tCylinder) {
        if (dzB == 0 || (!tCylinder && dzB < 0)) return;
        int xT = xA, yT = yA, zT = zA;
        if (dzB < 0) {
            xT += dxB;
            yT += dyB;
            zT += dzB;
        }
        findMinMaxY();
        for (int y = yMin; y <= yMax; ++y) {
            findMinMaxX(y);
            int count = xMax - xMin + 1;
            g3d.setColorNoisy(colixEndcap, intensityEndcap);
            g3d.plotPixelsClipped(count, xT + xMin, yT + y, zT - zXMin - 1, zT - zXMax - 1, null, null);
        }
    }

    void renderSphericalEndcaps() {
        g3d.fillSphereCentered(colixA, diameter, xA, yA, zA + 1);
        g3d.fillSphereCentered(colixB, diameter, xA + dxB, yA + dyB, zA + dzB + 1);
    }

    int xTip, yTip, zTip;

    void renderCone(short colix, byte endcap, int diameter, int xA, int yA, int zA, int xTip, int yTip, int zTip) {
        dxB = (this.xTip = xTip) - (this.xA = xA);
        dyB = (this.yTip = yTip) - (this.yA = yA);
        dzB = (this.zTip = zTip) - (this.zA = zA);
        this.colixA = colix;
        this.shadesA = g3d.getShades(colix);
        this.isScreenedA = (colixA & Graphics3D.TRANSLUCENT_MASK) != 0;
        int intensityTip = Shade3D.calcIntensity(dxB, dyB, -dzB);
        g3d.plotPixelClipped(shadesA[intensityTip], isScreenedA, xTip, yTip, zTip);
        this.diameter = diameter;
        if (diameter <= 1) {
            if (diameter == 1) g3d.plotLineDelta(colixA, isScreenedA, colixA, isScreenedA, xA, yA, zA, dxB, dyB, dzB);
            return;
        }
        this.endcaps = endcap;
        calcArgbEndcap(false);
        generateBaseEllipse();
        if (endcaps == Graphics3D.ENDCAPS_FLAT) renderFlatEndcap(false);
        for (int i = rasterCount; --i >= 0; ) plotRasterCone(i);
    }

    void plotRasterCone(int i) {
        int x = xRaster[i];
        int y = yRaster[i];
        int z = zRaster[i];
        int xUp = xA + x, yUp = yA + y, zUp = zA - z;
        int xDn = xA - x, yDn = yA - y, zDn = zA + z;
        if (tEndcapOpen) {
            g3d.plotPixelClipped(argbEndcap, isScreenedA, xUp, yUp, zUp);
            g3d.plotPixelClipped(argbEndcap, isScreenedA, xDn, yDn, zDn);
        }
        int fp8Up = fp8IntensityUp[i];
        g3d.plotLineDelta(shadesA, isScreenedA, shadesA, isScreenedA, fp8Up, xUp, yUp, zUp, xTip - xUp, yTip - yUp, zTip - zUp);
        if (!(endcaps == Graphics3D.ENDCAPS_FLAT && dzB > 0)) {
            int argb = shadesA[0];
            g3d.plotLineDelta(argb, isScreenedA, argb, isScreenedA, xDn, yDn, zDn, xTip - xDn, yTip - yDn, zTip - zDn);
        }
    }

    void calcArgbEndcap(boolean tCylinder) {
        tEndcapOpen = false;
        if ((endcaps == Graphics3D.ENDCAPS_SPHERICAL) || (dzB == 0) || (!tCylinder && dzB < 0)) return;
        xEndcap = xA;
        yEndcap = yA;
        zEndcap = zA;
        int[] shadesEndcap;
        if (dzB >= 0) {
            intensityEndcap = Shade3D.calcIntensity(-dxB, -dyB, dzB);
            colixEndcap = colixA;
            shadesEndcap = shadesA;
        } else {
            intensityEndcap = Shade3D.calcIntensity(dxB, dyB, -dzB);
            colixEndcap = colixB;
            shadesEndcap = shadesB;
            xEndcap += dxB;
            yEndcap += dyB;
            zEndcap += dzB;
        }
        if (intensityEndcap > Graphics3D.intensitySpecularSurfaceLimit) intensityEndcap = Graphics3D.intensitySpecularSurfaceLimit;
        argbEndcap = shadesEndcap[intensityEndcap];
        tEndcapOpen = (endcaps == Graphics3D.ENDCAPS_OPEN);
    }
}
