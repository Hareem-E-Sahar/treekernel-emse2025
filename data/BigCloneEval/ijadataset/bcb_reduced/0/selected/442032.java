package org.jmol.g3d;

import java.awt.Component;
import java.awt.Image;
import java.awt.FontMetrics;
import java.util.Hashtable;
import javax.vecmath.Point3i;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix3f;

/**
 * Provides high-level graphics primitives for 3D visualization.
 *<p>
 * A pure software implementation of a 3D graphics engine.
 * No hardware required.
 * Depending upon what you are rendering ... some people say it
 * is <i>pretty fast</i>.
 *
 * @author Miguel, miguel@jmol.org
 */
public final class Graphics3D {

    Platform3D platform;

    Line3D line3d;

    Circle3D circle3d;

    Sphere3D sphere3d;

    Triangle3D triangle3d;

    Cylinder3D cylinder3d;

    Hermite3D hermite3d;

    Geodesic3D geodesic3d;

    Normix3D normix3d;

    public static final int HIGHEST_GEODESIC_LEVEL = 3;

    boolean isFullSceneAntialiasingEnabled;

    boolean antialiasThisFrame;

    boolean inGreyscaleMode;

    byte[] anaglyphChannelBytes;

    boolean tPaintingInProgress;

    int windowWidth, windowHeight;

    int width, height;

    int slab, depth;

    int xLast, yLast;

    int[] pbuf;

    int[] zbuf;

    int clipX;

    int clipY;

    int clipWidth;

    int clipHeight;

    short colixCurrent;

    int[] shadesCurrent;

    int argbCurrent;

    boolean isTranslucent;

    int argbNoisyUp, argbNoisyDn;

    Font3D font3dCurrent;

    static final int ZBUFFER_BACKGROUND = Platform3D.ZBUFFER_BACKGROUND;

    /**
   * Allocates a g3d object
   *
   * @param awtComponent the java.awt.Component where the image will be drawn
   */
    public Graphics3D(Component awtComponent) {
        platform = Platform3D.createInstance(awtComponent);
        this.line3d = new Line3D(this);
        this.circle3d = new Circle3D(this);
        this.sphere3d = new Sphere3D(this);
        this.triangle3d = new Triangle3D(this);
        this.cylinder3d = new Cylinder3D(this);
        this.hermite3d = new Hermite3D(this);
        this.geodesic3d = new Geodesic3D(this);
        this.normix3d = new Normix3D(this);
    }

    /**
   * Sets the window size. This will be smaller than the
   * rendering size if FullSceneAntialiasing is enabled
   *
   * @param windowWidth Window width
   * @param windowHeight Window height
   * @param enableFullSceneAntialiasing currently not in production
   */
    public void setWindowSize(int windowWidth, int windowHeight, boolean enableFullSceneAntialiasing) {
        if (this.windowWidth == windowWidth && this.windowHeight == windowHeight && enableFullSceneAntialiasing == isFullSceneAntialiasingEnabled) return;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        isFullSceneAntialiasingEnabled = enableFullSceneAntialiasing;
        width = -1;
        height = -1;
        pbuf = null;
        zbuf = null;
        platform.releaseBuffers();
    }

    /**
   * is full scene / oversampling antialiasing in effect
   *
   * @return the answer
   */
    public boolean fullSceneAntialiasRendering() {
        return false;
    }

    /**
   * gets g3d width
   *
   * @return width pixel count;
   */
    public int getWindowWidth() {
        return width;
    }

    /**
   * gets g3d height
   *
   * @return height pixel count
   */
    public int getWindowHeight() {
        return height;
    }

    public int getRenderWidth() {
        return width;
    }

    public int getRenderHeight() {
        return height;
    }

    /**
   * sets background color to the specified argb value
   *
   * @param argb an argb value with alpha channel
   */
    public void setBackgroundArgb(int argb) {
        platform.setBackground(argb);
    }

    /**
   * Return a greyscale rgb value 0-FF using NTSC color luminance algorithm
   *<p>
   * the alpha component is set to 0xFF. If you want a value in the
   * range 0-255 then & the result with 0xFF;
   *
   * @param rgb the rgb value
   * @return a grayscale value in the range 0 - 255 decimal
   */
    public static int calcGreyscaleRgbFromRgb(int rgb) {
        int grey = ((2989 * ((rgb >> 16) & 0xFF)) + (5870 * ((rgb >> 8) & 0xFF)) + (1140 * (rgb & 0xFF)) + 5000) / 10000;
        int greyRgb = (grey << 16) | (grey << 8) | grey | 0xFF000000;
        return greyRgb;
    }

    /**
   * controls greyscale rendering
   * @param greyscaleMode Flag for greyscale rendering
   */
    public void setGreyscaleMode(boolean greyscaleMode) {
        this.inGreyscaleMode = greyscaleMode;
    }

    /**
   * clipping from the front and the back
   *<p>
   * the plane is defined as a percentage from the back of the image
   * to the front
   *<p>
   * For slab values:
   * <ul>
   *  <li>100 means 100% is shown
   *  <li>75 means the back 75% is shown
   *  <li>50 means the back half is shown
   *  <li>0 means that nothing is shown
   * </ul>
   *<p>
   * for depth values:
   * <ul>
   *  <li>0 means 100% is shown
   *  <li>25 means the back 25% is <i>not</i> shown
   *  <li>50 means the back half is <i>not</i> shown
   *  <li>100 means that nothing is shown
   * </ul>
   *<p>
   * @param slabValue front clipping percentage [0,100]
   * @param depthValue rear clipping percentage [0,100]
   */
    public void setSlabAndDepthValues(int slabValue, int depthValue) {
        slab = slabValue < 0 ? 0 : slabValue > ZBUFFER_BACKGROUND ? ZBUFFER_BACKGROUND : slabValue;
        depth = depthValue < 0 ? 0 : depthValue > ZBUFFER_BACKGROUND ? ZBUFFER_BACKGROUND : depthValue;
    }

    /**
   * used internally when oversampling is enabled
   */
    private void downSampleFullSceneAntialiasing() {
        int[] pbuf1 = pbuf;
        int[] pbuf4 = pbuf;
        int width4 = width;
        int offset1 = 0;
        int offset4 = 0;
        for (int i = windowHeight; --i >= 0; ) {
            for (int j = windowWidth; --j >= 0; ) {
                int argb;
                argb = (pbuf4[offset4] >> 2) & 0x3F3F3F3F;
                argb += (pbuf4[offset4 + width4] >> 2) & 0x3F3F3F3F;
                ++offset4;
                argb += (pbuf4[offset4] >> 2) & 0x3F3F3F3F;
                argb += (pbuf4[offset4 + width4] >> 2) & 0x3F3F3F3F;
                argb += (argb & 0xC0C0C0C0) >> 6;
                argb |= 0xFF000000;
                pbuf1[offset1] = argb;
                ++offset1;
                ++offset4;
            }
            offset4 += width4;
        }
    }

    public boolean hasContent() {
        return platform.hasContent();
    }

    /**
   * sets current color from colix color index
   * @param colix the color index
   */
    public void setColix(short colix) {
        colixCurrent = colix;
        shadesCurrent = getShades(colix);
        argbCurrent = argbNoisyUp = argbNoisyDn = getColixArgb(colix);
        isTranslucent = (colix & TRANSLUCENT_MASK) != 0;
    }

    public void setColixIntensity(short colix, int intensity) {
        colixCurrent = colix;
        shadesCurrent = getShades(colix);
        argbCurrent = argbNoisyUp = argbNoisyDn = shadesCurrent[intensity];
        isTranslucent = (colix & TRANSLUCENT_MASK) != 0;
    }

    public void setIntensity(int intensity) {
        argbCurrent = argbNoisyUp = argbNoisyDn = shadesCurrent[intensity];
    }

    void setColorNoisy(short colix, int intensity) {
        colixCurrent = colix;
        int[] shades = getShades(colix);
        argbCurrent = shades[intensity];
        argbNoisyUp = shades[intensity < shadeLast ? intensity + 1 : shadeLast];
        argbNoisyDn = shades[intensity > 0 ? intensity - 1 : 0];
        isTranslucent = (colix & TRANSLUCENT_MASK) != 0;
    }

    int[] imageBuf = new int[0];

    /**
   * draws a circle of the specified color at the specified location
   *
   * @param colix the color index
   * @param diameter pixel diameter
   * @param x center x
   * @param y center y
   * @param z center z
   */
    public void drawCircleCentered(short colix, int diameter, int x, int y, int z) {
        if (z < slab || z > depth) return;
        int r = (diameter + 1) / 2;
        setColix(colix);
        if ((x >= r && x + r < width) && (y >= r && y + r < height)) {
            switch(diameter) {
                case 2:
                    plotPixelUnclipped(x, y - 1, z);
                    plotPixelUnclipped(x - 1, y - 1, z);
                    plotPixelUnclipped(x - 1, y, z);
                case 1:
                    plotPixelUnclipped(x, y, z);
                case 0:
                    break;
                default:
                    circle3d.plotCircleCenteredUnclipped(x, y, z, diameter);
            }
        } else {
            switch(diameter) {
                case 2:
                    plotPixelClipped(x, y - 1, z);
                    plotPixelClipped(x - 1, y - 1, z);
                    plotPixelClipped(x - 1, y, z);
                case 1:
                    plotPixelClipped(x, y, z);
                case 0:
                    break;
                default:
                    circle3d.plotCircleCenteredClipped(x, y, z, diameter);
            }
        }
    }

    /**
   * draws a screened circle ... every other dot is turned on
   *
   * @param colixFill the color index
   * @param diameter the pixel diameter
   * @param x center x
   * @param y center y
   * @param z center z
   */
    public void fillScreenedCircleCentered(short colixFill, int diameter, int x, int y, int z) {
        if (diameter == 0 || z < slab || z > depth) return;
        int r = (diameter + 1) / 2;
        setColix(colixFill);
        isTranslucent = true;
        if (x >= r && x + r < width && y >= r && y + r < height) {
            circle3d.plotFilledCircleCenteredUnclipped(x, y, z, diameter);
            isTranslucent = false;
            circle3d.plotCircleCenteredUnclipped(x, y, z, diameter);
        } else {
            circle3d.plotFilledCircleCenteredClipped(x, y, z, diameter);
            isTranslucent = false;
            circle3d.plotCircleCenteredClipped(x, y, z, diameter);
        }
    }

    /**
   * fills a solid circle
   *
   * @param colixFill the color index
   * @param diameter the pixel diameter
   * @param x center x
   * @param y center y
   * @param z center z
   */
    public void fillCircleCentered(short colixFill, int diameter, int x, int y, int z) {
        if (diameter == 0 || z < slab || z > depth) return;
        int r = (diameter + 1) / 2;
        setColix(colixFill);
        if (x >= r && x + r < width && y >= r && y + r < height) {
            circle3d.plotFilledCircleCenteredUnclipped(x, y, z, diameter);
        } else {
            circle3d.plotFilledCircleCenteredClipped(x, y, z, diameter);
        }
    }

    /**
   * fills a solid sphere
   *
   * @param colix the color index
   * @param diameter pixel count
   * @param x center x
   * @param y center y
   * @param z center z
   */
    public void fillSphereCentered(short colix, int diameter, int x, int y, int z) {
        if (diameter <= 1) {
            plotPixelClipped(colix, x, y, z);
        } else {
            sphere3d.render(getShades(colix), ((colix & TRANSLUCENT_MASK) != 0), diameter, x, y, z);
        }
    }

    /**
   * fills a solid sphere
   *
   * @param colix the color index
   * @param diameter pixel count
   * @param center javax.vecmath.Point3i defining the center
   */
    public void fillSphereCentered(short colix, int diameter, Point3i center) {
        fillSphereCentered(colix, diameter, center.x, center.y, center.z);
    }

    /**
   * fills a solid sphere
   *
   * @param colix the color index
   * @param diameter pixel count
   * @param center a javax.vecmath.Point3f ... floats are casted to ints
   */
    public void fillSphereCentered(short colix, int diameter, Point3f center) {
        fillSphereCentered(colix, diameter, (int) center.x, (int) center.y, (int) center.z);
    }

    /**
   * draws a rectangle
   *
   * @param colix the color index
   * @param x upper left x
   * @param y upper left y
   * @param z upper left z
   * @param width pixel count
   * @param height pixel count
   */
    public void drawRect(short colix, int x, int y, int z, int width, int height) {
        setColix(colix);
        int xRight = x + width - 1;
        line3d.drawHLine(argbCurrent, isTranslucent, x, y, z, width - 1, true);
        int yBottom = y + height - 1;
        line3d.drawVLine(argbCurrent, isTranslucent, x, y, z, height - 1, true);
        line3d.drawVLine(argbCurrent, isTranslucent, xRight, y, z, height - 1, true);
        line3d.drawHLine(argbCurrent, isTranslucent, x, yBottom, z, width, true);
    }

    /**
   * draws a rectangle while ignoring slab/depth clipping
   *<p>
   * could be useful for UI work
   *
   * @param colix the color index
   * @param x upper left x
   * @param y upper left y
   * @param z upper left z
   * @param width pixel count
   * @param height pixel count
   */
    public void drawRectNoSlab(short colix, int x, int y, int z, int width, int height) {
        setColix(colix);
        int xRight = x + width - 1;
        line3d.drawHLine(argbCurrent, isTranslucent, x, y, z, width - 1, false);
        int yBottom = y + height - 1;
        line3d.drawVLine(argbCurrent, isTranslucent, x, y, z, height - 1, false);
        line3d.drawVLine(argbCurrent, isTranslucent, xRight, y, z, height - 1, false);
        line3d.drawHLine(argbCurrent, isTranslucent, x, yBottom, z, width, false);
    }

    /**
   * draws the specified string in the current font.
   * no line wrapping
   *
   * @param str the string
   * @param colix the color index
   * @param xBaseline baseline x
   * @param yBaseline baseline y
   * @param z baseline z
   */
    public void drawString(String str, short colix, int xBaseline, int yBaseline, int z) {
        drawString(str, font3dCurrent, colix, (short) 0, xBaseline, yBaseline, z);
    }

    /**
   * draws the specified string in the current font.
   * no line wrapping
   *
   * @param str the String
   * @param font3d the Font3D
   * @param colix the color index
   * @param xBaseline baseline x
   * @param yBaseline baseline y
   * @param z baseline z
   */
    public void drawString(String str, Font3D font3d, short colix, int xBaseline, int yBaseline, int z) {
        drawString(str, font3d, colix, (short) 0, xBaseline, yBaseline, z);
    }

    /**
   * draws the specified string in the current font.
   * no line wrapping
   *
   * @param str the String
   * @param font3d the Font3D
   * @param colix the color index
   * @param bgcolix the color index of the background
   * @param xBaseline baseline x
   * @param yBaseline baseline y
   * @param z baseline z
   */
    public void drawString(String str, Font3D font3d, short colix, short bgcolix, int xBaseline, int yBaseline, int z) {
        font3dCurrent = font3d;
        setColix(colix);
        if (z < slab || z > depth) return;
        Text3D.plot(xBaseline, yBaseline - font3d.fontMetrics.getAscent(), z, argbCurrent, getColixArgb(bgcolix), str, font3dCurrent, this);
    }

    public void drawStringNoSlab(String str, Font3D font3d, short colix, short bgcolix, int xBaseline, int yBaseline, int z) {
        font3dCurrent = font3d;
        setColix(colix);
        Text3D.plot(xBaseline, yBaseline - font3d.fontMetrics.getAscent(), z, argbCurrent, getColixArgb(bgcolix), str, font3dCurrent, this);
    }

    public void setFontOfSize(int fontsize) {
        font3dCurrent = getFont3D(fontsize);
    }

    public void setFont(byte fid) {
        font3dCurrent = Font3D.getFont3D(fid);
    }

    public void setFont(Font3D font3d) {
        font3dCurrent = font3d;
    }

    public Font3D getFont3DCurrent() {
        return font3dCurrent;
    }

    public byte getFontFidCurrent() {
        return font3dCurrent.fid;
    }

    public FontMetrics getFontMetrics() {
        return font3dCurrent.fontMetrics;
    }

    boolean currentlyRendering;

    private void setRectClip(int x, int y, int width, int height) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > windowWidth) width = windowWidth - x;
        if (y + height > windowHeight) height = windowHeight - y;
        clipX = x;
        clipY = y;
        clipWidth = width;
        clipHeight = height;
        if (antialiasThisFrame) {
            clipX *= 2;
            clipY *= 2;
            clipWidth *= 2;
            clipHeight *= 2;
        }
    }

    public void beginRendering(int clipX, int clipY, int clipWidth, int clipHeight, Matrix3f rotationMatrix, boolean antialiasThisFrame) {
        if (currentlyRendering) endRendering();
        normix3d.setRotationMatrix(rotationMatrix);
        antialiasThisFrame &= isFullSceneAntialiasingEnabled;
        this.antialiasThisFrame = antialiasThisFrame;
        currentlyRendering = true;
        if (pbuf == null) {
            platform.allocateBuffers(windowWidth, windowHeight, isFullSceneAntialiasingEnabled);
            pbuf = platform.pBuffer;
            zbuf = platform.zBuffer;
            width = windowWidth;
            xLast = width - 1;
            height = windowHeight;
            yLast = height - 1;
        }
        width = windowWidth;
        height = windowHeight;
        if (antialiasThisFrame) {
            width *= 2;
            height *= 2;
        }
        xLast = width - 1;
        yLast = height - 1;
        setRectClip(clipX, clipY, clipWidth, clipHeight);
        platform.obtainScreenBuffer();
    }

    public void endRendering() {
        if (currentlyRendering) {
            if (antialiasThisFrame) downSampleFullSceneAntialiasing();
            platform.notifyEndOfRendering();
            currentlyRendering = false;
        }
    }

    public void snapshotAnaglyphChannelBytes() {
        if (currentlyRendering) throw new NullPointerException();
        if (anaglyphChannelBytes == null || anaglyphChannelBytes.length != pbuf.length) anaglyphChannelBytes = new byte[pbuf.length];
        for (int i = pbuf.length; --i >= 0; ) anaglyphChannelBytes[i] = (byte) pbuf[i];
    }

    public void applyBlueOrGreenAnaglyph(boolean blueChannel) {
        int shiftCount = blueChannel ? 0 : 8;
        for (int i = pbuf.length; --i >= 0; ) pbuf[i] = ((pbuf[i] & 0xFFFF0000) | ((anaglyphChannelBytes[i] & 0x000000FF) << shiftCount));
    }

    public void applyCyanAnaglyph() {
        for (int i = pbuf.length; --i >= 0; ) {
            int blueAndGreen = anaglyphChannelBytes[i] & 0x000000FF;
            int cyan = (blueAndGreen << 8) | blueAndGreen;
            pbuf[i] = pbuf[i] & 0xFFFF0000 | cyan;
        }
    }

    public Image getScreenImage() {
        return platform.imagePixelBuffer;
    }

    public void releaseScreenImage() {
        platform.clearScreenBufferThreaded();
    }

    public void drawDashedLine(short colix, int run, int rise, int x1, int y1, int z1, int x2, int y2, int z2) {
        int argb = getColixArgb(colix);
        line3d.drawDashedLine(argb, isTranslucent, argb, isTranslucent, run, rise, x1, y1, z1, x2, y2, z2);
    }

    public void drawDottedLine(short colix, int x1, int y1, int z1, int x2, int y2, int z2) {
        int argb = getColixArgb(colix);
        line3d.drawDashedLine(argb, isTranslucent, argb, isTranslucent, 2, 1, x1, y1, z1, x2, y2, z2);
    }

    public void drawDashedLine(short colix1, short colix2, int run, int rise, int x1, int y1, int z1, int x2, int y2, int z2) {
        line3d.drawDashedLine(getColixArgb(colix1), isColixTranslucent(colix1), getColixArgb(colix2), isColixTranslucent(colix2), run, rise, x1, y1, z1, x2, y2, z2);
    }

    public void drawLine(Point3i pointA, Point3i pointB) {
        line3d.drawLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public void drawLine(short colix, Point3i pointA, Point3i pointB) {
        setColix(colix);
        line3d.drawLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public void drawDottedLine(short colix, Point3i pointA, Point3i pointB) {
        drawDashedLine(colix, 2, 1, pointA, pointB);
    }

    public void drawDashedLine(short colix, int run, int rise, Point3i pointA, Point3i pointB) {
        setColix(colix);
        line3d.drawDashedLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, run, rise, pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public void drawDashedLine(int run, int rise, int x1, int y1, int z1, int x2, int y2, int z2) {
        line3d.drawDashedLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, run, rise, x1, y1, z1, x2, y2, z2);
    }

    public void drawLine(int x1, int y1, int z1, int x2, int y2, int z2) {
        line3d.drawLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, x1, y1, z1, x2, y2, z2);
    }

    public void drawLine(short colix, int x1, int y1, int z1, int x2, int y2, int z2) {
        setColix(colix);
        line3d.drawLine(argbCurrent, isTranslucent, argbCurrent, isTranslucent, x1, y1, z1, x2, y2, z2);
    }

    public void drawLine(short colix1, short colix2, int x1, int y1, int z1, int x2, int y2, int z2) {
        line3d.drawLine(getColixArgb(colix1), isColixTranslucent(colix1), getColixArgb(colix2), isColixTranslucent(colix2), x1, y1, z1, x2, y2, z2);
    }

    public void drawPolygon4(int[] ax, int[] ay, int[] az) {
        drawLine(ax[0], ay[0], az[0], ax[3], ay[3], az[3]);
        for (int i = 3; --i >= 0; ) drawLine(ax[i], ay[i], az[i], ax[i + 1], ay[i + 1], az[i + 1]);
    }

    public void fillQuadrilateral(short colix, Point3f screenA, Point3f screenB, Point3f screenC, Point3f screenD) {
        setColorNoisy(colix, calcIntensityScreen(screenA, screenB, screenC));
        fillTriangle(screenA, screenB, screenC);
        fillTriangle(screenA, screenC, screenD);
    }

    public void fillTriangle(short colix, Point3i screenA, short normixA, Point3i screenB, short normixB, Point3i screenC, short normixC) {
        int[] t;
        t = triangle3d.ax;
        t[0] = screenA.x;
        t[1] = screenB.x;
        t[2] = screenC.x;
        t = triangle3d.ay;
        t[0] = screenA.y;
        t[1] = screenB.y;
        t[2] = screenC.y;
        t = triangle3d.az;
        t[0] = screenA.z;
        t[1] = screenB.z;
        t[2] = screenC.z;
        if (normixA == normixB && normixA == normixC) {
            setColorNoisy(colix, normix3d.getIntensity(normixA));
            triangle3d.fillTriangle(false);
        } else {
            setColix(colix);
            triangle3d.setGouraud(shadesCurrent[normix3d.getIntensity(normixA)], shadesCurrent[normix3d.getIntensity(normixB)], shadesCurrent[normix3d.getIntensity(normixC)]);
            triangle3d.fillTriangle(true);
        }
    }

    public void fillTriangle(Point3i screenA, short colixA, short normixA, Point3i screenB, short colixB, short normixB, Point3i screenC, short colixC, short normixC) {
        int[] t;
        t = triangle3d.ax;
        t[0] = screenA.x;
        t[1] = screenB.x;
        t[2] = screenC.x;
        t = triangle3d.ay;
        t[0] = screenA.y;
        t[1] = screenB.y;
        t[2] = screenC.y;
        t = triangle3d.az;
        t[0] = screenA.z;
        t[1] = screenB.z;
        t[2] = screenC.z;
        if (normixA == normixB && normixA == normixC && colixA == colixB && colixA == colixC) {
            setColorNoisy(colixA, normix3d.getIntensity(normixA));
            triangle3d.fillTriangle(false);
        } else {
            triangle3d.setGouraud(getShades(colixA)[normix3d.getIntensity(normixA)], getShades(colixB)[normix3d.getIntensity(normixB)], getShades(colixC)[normix3d.getIntensity(normixC)]);
            int translucentCount = 0;
            if (isColixTranslucent(colixA)) ++translucentCount;
            if (isColixTranslucent(colixB)) ++translucentCount;
            if (isColixTranslucent(colixC)) ++translucentCount;
            isTranslucent = translucentCount >= 2;
            triangle3d.fillTriangle(true);
        }
    }

    public void fillTriangle(short colix, Point3i screenA, Point3i screenB, Point3i screenC) {
        calcSurfaceShade(colix, screenA, screenB, screenC);
        int[] t;
        t = triangle3d.ax;
        t[0] = screenA.x;
        t[1] = screenB.x;
        t[2] = screenC.x;
        t = triangle3d.ay;
        t[0] = screenA.y;
        t[1] = screenB.y;
        t[2] = screenC.y;
        t = triangle3d.az;
        t[0] = screenA.z;
        t[1] = screenB.z;
        t[2] = screenC.z;
        triangle3d.fillTriangle(false);
    }

    public void fillTriangle(short colix, short normix, int xScreenA, int yScreenA, int zScreenA, int xScreenB, int yScreenB, int zScreenB, int xScreenC, int yScreenC, int zScreenC) {
        setColorNoisy(colix, normix3d.getIntensity(normix));
        int[] t;
        t = triangle3d.ax;
        t[0] = xScreenA;
        t[1] = xScreenB;
        t[2] = xScreenC;
        t = triangle3d.ay;
        t[0] = yScreenA;
        t[1] = yScreenB;
        t[2] = yScreenC;
        t = triangle3d.az;
        t[0] = zScreenA;
        t[1] = zScreenB;
        t[2] = zScreenC;
        triangle3d.fillTriangle(false);
    }

    public void fillTriangle(short colix, Point3f screenA, Point3f screenB, Point3f screenC) {
        setColorNoisy(colix, calcIntensityScreen(screenA, screenB, screenC));
        fillTriangle(screenA, screenB, screenC);
    }

    public void fillQuadrilateral(short colix, Point3i screenA, Point3i screenB, Point3i screenC, Point3i screenD) {
        fillTriangle(colix, screenA, screenB, screenC);
        fillTriangle(colix, screenA, screenC, screenD);
    }

    public void fillQuadrilateral(short colix, Point3i screenA, short normixA, Point3i screenB, short normixB, Point3i screenC, short normixC, Point3i screenD, short normixD) {
        fillTriangle(colix, screenA, normixA, screenB, normixB, screenC, normixC);
        fillTriangle(colix, screenA, normixA, screenC, normixC, screenD, normixD);
    }

    public void fillQuadrilateral(Point3i screenA, short colixA, short normixA, Point3i screenB, short colixB, short normixB, Point3i screenC, short colixC, short normixC, Point3i screenD, short colixD, short normixD) {
        fillTriangle(screenA, colixA, normixA, screenB, colixB, normixB, screenC, colixC, normixC);
        fillTriangle(screenA, colixA, normixA, screenC, colixC, normixC, screenD, colixD, normixD);
    }

    public void fillTriangle(Point3i screenA, Point3i screenB, Point3i screenC) {
        int[] t;
        t = triangle3d.ax;
        t[0] = screenA.x;
        t[1] = screenB.x;
        t[2] = screenC.x;
        t = triangle3d.ay;
        t[0] = screenA.y;
        t[1] = screenB.y;
        t[2] = screenC.y;
        t = triangle3d.az;
        t[0] = screenA.z;
        t[1] = screenB.z;
        t[2] = screenC.z;
        triangle3d.fillTriangle(false);
    }

    public void fillTriangle(Point3f screenA, Point3f screenB, Point3f screenC) {
        int[] t;
        t = triangle3d.ax;
        t[0] = (int) screenA.x;
        t[1] = (int) screenB.x;
        t[2] = (int) screenC.x;
        t = triangle3d.ay;
        t[0] = (int) screenA.y;
        t[1] = (int) screenB.y;
        t[2] = (int) screenC.y;
        t = triangle3d.az;
        t[0] = (int) screenA.z;
        t[1] = (int) screenB.z;
        t[2] = (int) screenC.z;
        triangle3d.fillTriangle(false);
    }

    int intensity = 0;

    void diff(Vector3f v, Point3i s1, Point3i s2) {
        v.x = s1.x - s2.x;
        v.y = s1.y - s2.y;
        v.z = s1.z - s2.z;
    }

    public void calcSurfaceShade(short colix, Point3i screenA, Point3i screenB, Point3i screenC) {
        diff(vectorAB, screenB, screenA);
        diff(vectorAC, screenC, screenA);
        vectorNormal.cross(vectorAB, vectorAC);
        int intensity = vectorNormal.z >= 0 ? calcIntensity(-vectorNormal.x, -vectorNormal.y, vectorNormal.z) : calcIntensity(vectorNormal.x, vectorNormal.y, -vectorNormal.z);
        if (intensity > intensitySpecularSurfaceLimit) intensity = intensitySpecularSurfaceLimit;
        setColorNoisy(colix, intensity);
    }

    public void drawfillTriangle(short colix, int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC) {
        setColix(colix);
        int argb = argbCurrent;
        line3d.drawLine(argb, isTranslucent, argb, isTranslucent, xA, yA, zA, xB, yB, zB);
        line3d.drawLine(argb, isTranslucent, argb, isTranslucent, xA, yA, zA, xC, yC, zC);
        line3d.drawLine(argb, isTranslucent, argb, isTranslucent, xB, yB, zB, xC, yC, zC);
        int[] t;
        t = triangle3d.ax;
        t[0] = xA;
        t[1] = xB;
        t[2] = xC;
        t = triangle3d.ay;
        t[0] = yA;
        t[1] = yB;
        t[2] = yC;
        t = triangle3d.az;
        t[0] = zA;
        t[1] = zB;
        t[2] = zC;
        triangle3d.fillTriangle(false);
    }

    public void fillTriangle(short colix, boolean translucent, int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC) {
        setColix(colix);
        int[] t;
        t = triangle3d.ax;
        t[0] = xA;
        t[1] = xB;
        t[2] = xC;
        t = triangle3d.ay;
        t[0] = yA;
        t[1] = yB;
        t[2] = yC;
        t = triangle3d.az;
        t[0] = zA;
        t[1] = zB;
        t[2] = zC;
        triangle3d.fillTriangle(false);
    }

    public void drawTriangle(short colix, int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC) {
        setColix(colix);
        drawLine(xA, yA, zA, xB, yB, zB);
        drawLine(xA, yA, zA, xC, yC, zC);
        drawLine(xB, yB, zB, xC, yC, zC);
    }

    public void drawCylinderTriangle(short colix, int xA, int yA, int zA, int xB, int yB, int zB, int xC, int yC, int zC, int diameter) {
        fillCylinder(colix, colix, Graphics3D.ENDCAPS_SPHERICAL, diameter, xA, yA, zA, xB, yB, zB);
        fillCylinder(colix, colix, Graphics3D.ENDCAPS_SPHERICAL, diameter, xA, yA, zA, xC, yC, zC);
        fillCylinder(colix, colix, Graphics3D.ENDCAPS_SPHERICAL, diameter, xB, yB, zB, xC, yC, zC);
    }

    public void drawTriangle(short colix, Point3i screenA, Point3i screenB, Point3i screenC) {
        drawTriangle(colix, screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z, screenC.x, screenC.y, screenC.z);
    }

    public void drawQuadrilateral(short colix, Point3i screenA, Point3i screenB, Point3i screenC, Point3i screenD) {
        setColix(colix);
        drawLine(screenA, screenB);
        drawLine(screenB, screenC);
        drawLine(screenC, screenD);
        drawLine(screenD, screenA);
    }

    public static final byte ENDCAPS_NONE = 0;

    public static final byte ENDCAPS_OPEN = 1;

    public static final byte ENDCAPS_FLAT = 2;

    public static final byte ENDCAPS_SPHERICAL = 3;

    public void fillCylinder(short colixA, short colixB, byte endcaps, int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        cylinder3d.render(colixA, colixB, endcaps, diameter, xA, yA, zA, xB, yB, zB);
    }

    public void fillCylinder(short colix, byte endcaps, int diameter, int xA, int yA, int zA, int xB, int yB, int zB) {
        cylinder3d.render(colix, colix, endcaps, diameter, xA, yA, zA, xB, yB, zB);
    }

    public void fillCylinder(short colix, byte endcaps, int diameter, Point3i screenA, Point3i screenB) {
        cylinder3d.render(colix, colix, endcaps, diameter, screenA.x, screenA.y, screenA.z, screenB.x, screenB.y, screenB.z);
    }

    public void fillCone(short colix, byte endcap, int diameter, int xBase, int yBase, int zBase, int xTip, int yTip, int zTip) {
        cylinder3d.renderCone(colix, endcap, diameter, xBase, yBase, zBase, xTip, yTip, zTip);
    }

    public void fillCone(short colix, byte endcap, int diameter, Point3i screenBase, Point3i screenTip) {
        cylinder3d.renderCone(colix, endcap, diameter, screenBase.x, screenBase.y, screenBase.z, screenTip.x, screenTip.y, screenTip.z);
    }

    public void fillHermite(short colix, int tension, int diameterBeg, int diameterMid, int diameterEnd, Point3i s0, Point3i s1, Point3i s2, Point3i s3) {
        hermite3d.render(true, colix, tension, diameterBeg, diameterMid, diameterEnd, s0, s1, s2, s3);
    }

    public void drawHermite(short colix, int tension, Point3i s0, Point3i s1, Point3i s2, Point3i s3) {
        hermite3d.render(false, colix, tension, 0, 0, 0, s0, s1, s2, s3);
    }

    public void drawHermite(boolean fill, boolean border, short colix, int tension, Point3i s0, Point3i s1, Point3i s2, Point3i s3, Point3i s4, Point3i s5, Point3i s6, Point3i s7) {
        hermite3d.render2(fill, border, colix, tension, s0, s1, s2, s3, s4, s5, s6, s7);
    }

    public void fillRect(short colix, int x, int y, int z, int widthFill, int heightFill) {
        setColix(colix);
        if (x < 0) {
            widthFill += x;
            if (widthFill <= 0) return;
            x = 0;
        }
        if (x + widthFill > width) {
            widthFill = width - x;
            if (widthFill == 0) return;
        }
        if (y < 0) {
            heightFill += y;
            if (heightFill <= 0) return;
            y = 0;
        }
        if (y + heightFill > height) heightFill = height - y;
        while (--heightFill >= 0) plotPixelsUnclipped(widthFill, x, y++, z);
    }

    public void drawPixel(Point3i point) {
        plotPixelClipped(point);
    }

    public void drawPixel(int x, int y, int z) {
        plotPixelClipped(x, y, z);
    }

    public void drawPixel(Point3i point, int normix) {
        plotPixelClipped(shadesCurrent[normix3d.intensities[normix]], point.x, point.y, point.z);
    }

    void plotPixelClipped(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argbCurrent;
        }
    }

    void plotPixelClipped(Point3i screen) {
        int x = screen.x;
        if (x < 0 || x >= width) return;
        int y = screen.y;
        if (y < 0 || y >= height) return;
        int z = screen.z;
        if (z < slab || z > depth) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argbCurrent;
        }
    }

    void plotPixelClipped(int argb, int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argb;
        }
    }

    void plotPixelClippedNoSlab(int argb, int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argb;
        }
    }

    void plotPixelClipped(int argb, boolean isTranslucent, int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) return;
        if (isTranslucent && ((x ^ y) & 1) != 0) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argb;
        }
    }

    void plotPixelClipped(short colix, int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) return;
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = getColixArgb(colix);
        }
    }

    void plotPixelUnclipped(int x, int y, int z) {
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argbCurrent;
        }
    }

    void plotPixelUnclipped(int argb, int x, int y, int z) {
        int offset = y * width + x;
        if (z < zbuf[offset]) {
            zbuf[offset] = z;
            pbuf[offset] = argb;
        }
    }

    void plotPixelsClipped(int count, int x, int y, int z) {
        if (y < 0 || y >= height || x >= width || z < slab || z > depth) return;
        if (x < 0) {
            count += x;
            x = 0;
        }
        if (count + x > width) count = width - x;
        if (count <= 0) return;
        int offsetPbuf = y * width + x;
        int offsetMax = offsetPbuf + count;
        int step = 1;
        if (isTranslucent) {
            step = 2;
            if (((x ^ y) & 1) != 0) ++offsetPbuf;
        }
        while (offsetPbuf < offsetMax) {
            if (z < zbuf[offsetPbuf]) {
                zbuf[offsetPbuf] = z;
                pbuf[offsetPbuf] = argbCurrent;
            }
            offsetPbuf += step;
        }
    }

    void plotPixelsClipped(int count, int x, int y, int zAtLeft, int zPastRight, Rgb16 rgb16Left, Rgb16 rgb16Right) {
        if (count <= 0 || y < 0 || y >= height || x >= width || (zAtLeft < slab && zPastRight < slab) || (zAtLeft > depth && zPastRight > depth)) return;
        int seed = (x << 16) + (y << 1) ^ 0x33333333;
        int zScaled = (zAtLeft << 10) + (1 << 9);
        int dz = zPastRight - zAtLeft;
        int roundFactor = count / 2;
        int zIncrementScaled = ((dz << 10) + (dz >= 0 ? roundFactor : -roundFactor)) / count;
        if (x < 0) {
            x = -x;
            zScaled += zIncrementScaled * x;
            count -= x;
            if (count <= 0) return;
            x = 0;
        }
        if (count + x > width) count = width - x;
        boolean flipflop = ((x ^ y) & 1) != 0;
        int offsetPbuf = y * width + x;
        if (rgb16Left == null) {
            while (--count >= 0) {
                if (!isTranslucent || (flipflop = !flipflop)) {
                    int z = zScaled >> 10;
                    if (z >= slab && z <= depth && z < zbuf[offsetPbuf]) {
                        zbuf[offsetPbuf] = z;
                        seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
                        int bits = (seed >> 16) & 0x07;
                        pbuf[offsetPbuf] = (bits == 0 ? argbNoisyDn : (bits == 1 ? argbNoisyUp : argbCurrent));
                    }
                }
                ++offsetPbuf;
                zScaled += zIncrementScaled;
            }
        } else {
            int rScaled = rgb16Left.rScaled << 8;
            int rIncrement = ((rgb16Right.rScaled - rgb16Left.rScaled) << 8) / count;
            int gScaled = rgb16Left.gScaled;
            int gIncrement = (rgb16Right.gScaled - gScaled) / count;
            int bScaled = rgb16Left.bScaled;
            int bIncrement = (rgb16Right.bScaled - bScaled) / count;
            while (--count >= 0) {
                if (!isTranslucent || (flipflop = !flipflop)) {
                    int z = zScaled >> 10;
                    if (z >= slab && z <= depth && z < zbuf[offsetPbuf]) {
                        zbuf[offsetPbuf] = z;
                        pbuf[offsetPbuf] = (0xFF000000 | (rScaled & 0xFF0000) | (gScaled & 0xFF00) | ((bScaled >> 8) & 0xFF));
                    }
                }
                ++offsetPbuf;
                zScaled += zIncrementScaled;
                rScaled += rIncrement;
                gScaled += gIncrement;
                bScaled += bIncrement;
            }
        }
    }

    static final boolean ENABLE_GOURAUD_STATS = true;

    static int totalGouraud;

    static int shortCircuitGouraud;

    void plotPixelsUnclipped(int count, int x, int y, int zAtLeft, int zPastRight, Rgb16 rgb16Left, Rgb16 rgb16Right) {
        if (count <= 0) return;
        int seed = (x << 16) + (y << 1) ^ 0x33333333;
        int zScaled = (zAtLeft << 10) + (1 << 9);
        int dz = zPastRight - zAtLeft;
        int roundFactor = count / 2;
        int zIncrementScaled = ((dz << 10) + (dz >= 0 ? roundFactor : -roundFactor)) / count;
        int offsetPbuf = y * width + x;
        if (rgb16Left == null) {
            if (!isTranslucent) {
                while (--count >= 0) {
                    int z = zScaled >> 10;
                    if (z < zbuf[offsetPbuf]) {
                        zbuf[offsetPbuf] = z;
                        seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
                        int bits = (seed >> 16) & 0x07;
                        pbuf[offsetPbuf] = (bits == 0 ? argbNoisyDn : (bits == 1 ? argbNoisyUp : argbCurrent));
                    }
                    ++offsetPbuf;
                    zScaled += zIncrementScaled;
                }
            } else {
                boolean flipflop = ((x ^ y) & 1) != 0;
                while (--count >= 0) {
                    flipflop = !flipflop;
                    if (flipflop) {
                        int z = zScaled >> 10;
                        if (z < zbuf[offsetPbuf]) {
                            zbuf[offsetPbuf] = z;
                            seed = ((seed << 16) + (seed << 1) + seed) & 0x7FFFFFFF;
                            int bits = (seed >> 16) & 0x07;
                            pbuf[offsetPbuf] = (bits == 0 ? argbNoisyDn : (bits == 1 ? argbNoisyUp : argbCurrent));
                        }
                    }
                    ++offsetPbuf;
                    zScaled += zIncrementScaled;
                }
            }
        } else {
            boolean flipflop = ((x ^ y) & 1) != 0;
            if (ENABLE_GOURAUD_STATS) {
                ++totalGouraud;
                int i = count;
                int j = offsetPbuf;
                int zMin = zAtLeft < zPastRight ? zAtLeft : zPastRight;
                if (!isTranslucent) {
                    for (; zbuf[j] < zMin; ++j) if (--i == 0) {
                        if ((++shortCircuitGouraud % 100000) == 0) System.out.println("totalGouraud=" + totalGouraud + " shortCircuitGouraud=" + shortCircuitGouraud + " %=" + (100.0 * shortCircuitGouraud / totalGouraud));
                        return;
                    }
                } else {
                    if (flipflop) {
                        ++j;
                        if (--i == 0) return;
                    }
                    for (; zbuf[j] < zMin; j += 2) {
                        i -= 2;
                        if (i <= 0) {
                            if ((++shortCircuitGouraud % 100000) == 0) System.out.println("totalGouraud=" + totalGouraud + " shortCircuitGouraud=" + shortCircuitGouraud + " %=" + (100.0 * shortCircuitGouraud / totalGouraud));
                            return;
                        }
                    }
                }
            }
            int rScaled = rgb16Left.rScaled << 8;
            int rIncrement = ((rgb16Right.rScaled - rgb16Left.rScaled) << 8) / count;
            int gScaled = rgb16Left.gScaled;
            int gIncrement = (rgb16Right.gScaled - gScaled) / count;
            int bScaled = rgb16Left.bScaled;
            int bIncrement = (rgb16Right.bScaled - bScaled) / count;
            while (--count >= 0) {
                if (!isTranslucent || (flipflop = !flipflop)) {
                    int z = zScaled >> 10;
                    if (z < zbuf[offsetPbuf]) {
                        zbuf[offsetPbuf] = z;
                        pbuf[offsetPbuf] = (0xFF000000 | (rScaled & 0xFF0000) | (gScaled & 0xFF00) | ((bScaled >> 8) & 0xFF));
                    }
                }
                ++offsetPbuf;
                zScaled += zIncrementScaled;
                rScaled += rIncrement;
                gScaled += gIncrement;
                bScaled += bIncrement;
            }
        }
    }

    void plotPixelsUnclipped(int count, int x, int y, int z) {
        int offsetPbuf = y * width + x;
        if (!isTranslucent) {
            while (--count >= 0) {
                if (z < zbuf[offsetPbuf]) {
                    zbuf[offsetPbuf] = z;
                    pbuf[offsetPbuf] = argbCurrent;
                }
                ++offsetPbuf;
            }
        } else {
            int offsetMax = offsetPbuf + count;
            if (((x ^ y) & 1) != 0) if (++offsetPbuf == offsetMax) return;
            do {
                if (z < zbuf[offsetPbuf]) {
                    zbuf[offsetPbuf] = z;
                    pbuf[offsetPbuf] = argbCurrent;
                }
                offsetPbuf += 2;
            } while (offsetPbuf < offsetMax);
        }
    }

    void plotPixelsClipped(int[] pixels, int offset, int count, int x, int y, int z) {
        if (y < 0 || y >= height || x >= width || z < slab || z > depth) return;
        if (x < 0) {
            count += x;
            if (count < 0) return;
            offset -= x;
            x = 0;
        }
        if (count + x > width) count = width - x;
        int offsetPbuf = y * width + x;
        while (--count >= 0) {
            int pixel = pixels[offset++];
            int alpha = pixel & 0xFF000000;
            if (alpha >= 0x80000000) {
                if (z < zbuf[offsetPbuf]) {
                    zbuf[offsetPbuf] = z;
                    pbuf[offsetPbuf] = pixel;
                }
            }
            ++offsetPbuf;
        }
    }

    void plotPixelsUnclipped(int[] pixels, int offset, int count, int x, int y, int z) {
        int offsetPbuf = y * width + x;
        while (--count >= 0) {
            int pixel = pixels[offset++];
            int alpha = pixel & 0xFF000000;
            if ((alpha & 0x80000000) != 0) {
                if (z < zbuf[offsetPbuf]) {
                    zbuf[offsetPbuf] = z;
                    pbuf[offsetPbuf] = pixel;
                }
            }
            ++offsetPbuf;
        }
    }

    void plotLineDelta(int[] shades1, boolean isTranslucent1, int[] shades2, boolean isTranslucent2, int fp8Intensity, int x, int y, int z, int dx, int dy, int dz) {
        if (x < 0 || x >= width || x + dx < 0 || x + dx >= width || y < 0 || y >= height || y + dy < 0 || y + dy >= height || z < slab || z + dz < slab || z > depth || z + dz > depth) line3d.plotLineDeltaClipped(shades1, isTranslucent1, shades2, isTranslucent2, fp8Intensity, x, y, z, dx, dy, dz); else line3d.plotLineDeltaUnclipped(shades1, isTranslucent1, shades2, isTranslucent2, fp8Intensity, x, y, z, dx, dy, dz);
    }

    void plotLineDelta(short colixA, short colixB, int x, int y, int z, int dx, int dy, int dz) {
        if (x < 0 || x >= width || x + dx < 0 || x + dx >= width || y < 0 || y >= height || y + dy < 0 || y + dy >= height || z < slab || z + dz < slab || z > depth || z + dz > depth) line3d.plotLineDeltaClipped(getColixArgb(colixA), isColixTranslucent(colixA), getColixArgb(colixB), isColixTranslucent(colixB), x, y, z, dx, dy, dz); else line3d.plotLineDeltaUnclipped(getColixArgb(colixA), isColixTranslucent(colixA), getColixArgb(colixB), isColixTranslucent(colixB), x, y, z, dx, dy, dz);
    }

    void plotLineDelta(int argb1, boolean isTranslucent1, int argb2, boolean isTranslucent2, int x, int y, int z, int dx, int dy, int dz) {
        if (x < 0 || x >= width || x + dx < 0 || x + dx >= width || y < 0 || y >= height || y + dy < 0 || y + dy >= height || z < slab || z + dz < slab || z > depth || z + dz > depth) line3d.plotLineDeltaClipped(argb1, isTranslucent1, argb2, isTranslucent2, x, y, z, dx, dy, dz); else line3d.plotLineDeltaUnclipped(argb1, isTranslucent1, argb2, isTranslucent2, x, y, z, dx, dy, dz);
    }

    public void plotPoints(short colix, int count, int[] coordinates) {
        setColix(colix);
        int argb = argbCurrent;
        for (int i = count * 3; i > 0; ) {
            int z = coordinates[--i];
            int y = coordinates[--i];
            int x = coordinates[--i];
            if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) continue;
            int offset = y * width + x;
            if (z < zbuf[offset]) {
                zbuf[offset] = z;
                pbuf[offset] = argb;
            }
        }
    }

    public void plotPoints(int count, short colix, byte[] intensities, int[] coordinates) {
        int[] shades = getShades(colix);
        for (int i = count * 3, j = count - 1; i > 0; --j) {
            int z = coordinates[--i];
            int y = coordinates[--i];
            int x = coordinates[--i];
            if (x < 0 || x >= width || y < 0 || y >= height || z < slab || z > depth) continue;
            int offset = y * width + x;
            if (z < zbuf[offset]) {
                zbuf[offset] = z;
                pbuf[offset] = shades[intensities[j]];
            }
        }
    }

    void averageOffsetArgb(int offset, int argb) {
        pbuf[offset] = ((((pbuf[offset] >> 1) & 0x007F7F7F) + ((argb >> 1) & 0xFF7F7F7F)) | (argb & 0xFF010101));
    }

    static final short TRANSLUCENT_MASK = 0x4000;

    static final short OPAQUE_MASK = ~TRANSLUCENT_MASK;

    static final short CHANGABLE_MASK = (short) 0x8000;

    static final short UNMASK_CHANGABLE_TRANSLUCENT = 0x3FFF;

    public static final short NULL_COLIX = 0;

    public static final short TRANSLUCENT = 1;

    public static final short OPAQUE = 2;

    public static final short UNRECOGNIZED = 3;

    public static final short SPECIAL_COLIX_MAX = 4;

    public static final short BLACK = 4;

    public static final short ORANGE = 5;

    public static final short PINK = 6;

    public static final short BLUE = 7;

    public static final short WHITE = 8;

    public static final short CYAN = 9;

    public static final short RED = 10;

    public static final short GREEN = 11;

    public static final short GRAY = 12;

    public static final short SILVER = 13;

    public static final short LIME = 14;

    public static final short MAROON = 15;

    public static final short NAVY = 16;

    public static final short OLIVE = 17;

    public static final short PURPLE = 18;

    public static final short TEAL = 19;

    public static final short MAGENTA = 20;

    public static final short YELLOW = 21;

    public static final short HOTPINK = 22;

    public static final short GOLD = 23;

    static int[] predefinedArgbs = { 0xFF000000, 0xFFFFA500, 0xFFFFC0CB, 0xFF0000FF, 0xFFFFFFFF, 0xFF00FFFF, 0xFFFF0000, 0xFF008000, 0xFF808080, 0xFFC0C0C0, 0xFF00FF00, 0xFF800000, 0xFF000080, 0xFF808000, 0xFF800080, 0xFF008080, 0xFFFF00FF, 0xFFFFFF00, 0xFFFF69B4, 0xFFFFD700 };

    static {
        for (int i = 0; i < predefinedArgbs.length; ++i) if (Colix.getColix(predefinedArgbs[i]) != i + SPECIAL_COLIX_MAX) throw new NullPointerException();
    }

    public int getColixArgb(short colix) {
        if (colix < 0) colix = changableColixMap[colix & UNMASK_CHANGABLE_TRANSLUCENT];
        if (!inGreyscaleMode) return Colix.getArgb(colix);
        return Colix.getArgbGreyscale(colix);
    }

    public int[] getShades(short colix) {
        if (colix < 0) colix = changableColixMap[colix & UNMASK_CHANGABLE_TRANSLUCENT];
        if (!inGreyscaleMode) return Colix.getShades(colix);
        return Colix.getShadesGreyscale(colix);
    }

    public static final short getChangableColixIndex(short colix) {
        if (colix >= 0) return -1;
        return (short) (colix & UNMASK_CHANGABLE_TRANSLUCENT);
    }

    public static final boolean isColixTranslucent(short colix) {
        return (colix & TRANSLUCENT_MASK) != 0;
    }

    public static final short getTranslucentColix(short colix, boolean translucent) {
        return (short) (translucent ? (colix | TRANSLUCENT_MASK) : (colix & OPAQUE_MASK));
    }

    public static final short getTranslucentColix(short colix) {
        return (short) (colix | TRANSLUCENT_MASK);
    }

    public static final short getOpaqueColix(short colix) {
        return (short) (colix & OPAQUE_MASK);
    }

    public static final short getColix(int argb) {
        return Colix.getColix(argb);
    }

    public final short getColixMix(short colixA, short colixB) {
        return Colix.getColixMix(colixA >= 0 ? colixA : changableColixMap[colixA & UNMASK_CHANGABLE_TRANSLUCENT], colixB >= 0 ? colixB : changableColixMap[colixB & UNMASK_CHANGABLE_TRANSLUCENT]);
    }

    public static final short setTranslucent(short colix, boolean isTranslucent) {
        if (isTranslucent) {
            if (colix >= 0 && colix < SPECIAL_COLIX_MAX) return TRANSLUCENT;
            return (short) (colix | TRANSLUCENT_MASK);
        }
        if (colix >= 0 && colix < SPECIAL_COLIX_MAX) return OPAQUE;
        return (short) (colix & OPAQUE_MASK);
    }

    public static final short getColix(String colorName) {
        int argb = getArgbFromString(colorName);
        if (argb != 0) return getColix(argb);
        if ("none".equalsIgnoreCase(colorName)) return 0;
        if ("translucent".equalsIgnoreCase(colorName)) return TRANSLUCENT;
        if ("opaque".equalsIgnoreCase(colorName)) return OPAQUE;
        return UNRECOGNIZED;
    }

    public static final short getColix(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return getColix(((Integer) obj).intValue());
        if (obj instanceof String) return getColix((String) obj);
        System.out.println("?? getColix(" + obj + ")");
        return HOTPINK;
    }

    public static final short inheritColix(short myColix, short parentColix) {
        switch(myColix) {
            case 0:
                return parentColix;
            case TRANSLUCENT:
                return (short) (parentColix | TRANSLUCENT_MASK);
            case OPAQUE:
                return (short) (parentColix & OPAQUE_MASK);
            default:
                return myColix;
        }
    }

    public static final short inheritColix(short myColix, short parentColix, short grandParentColix) {
        if (myColix >= SPECIAL_COLIX_MAX) return myColix;
        parentColix = inheritColix(parentColix, grandParentColix);
        if (myColix == 0) return parentColix;
        return inheritColix(myColix, parentColix);
    }

    public String getHexColorFromIndex(short colix) {
        int argb = getColixArgb(colix);
        if (argb == 0) return null;
        String r = Integer.toHexString((argb >> 16) & 0xFF);
        String g = Integer.toHexString((argb >> 8) & 0xFF);
        String b = Integer.toHexString(argb & 0xFF);
        return "#" + r + g + b;
    }

    /****************************************************************
   * changable colixes
   * give me a short ID and a color, and I will give you a colix
   * later, you can reassign the color if you want
   ****************************************************************/
    short[] changableColixMap = new short[16];

    public short getChangableColix(short id, int argb) {
        if (id >= changableColixMap.length) {
            short[] t = new short[id + 16];
            System.arraycopy(changableColixMap, 0, t, 0, changableColixMap.length);
            changableColixMap = t;
        }
        if (changableColixMap[id] == 0) changableColixMap[id] = getColix(argb);
        return (short) (id | CHANGABLE_MASK);
    }

    public void changeColixArgb(short id, int argb) {
        if (id < changableColixMap.length && changableColixMap[id] != 0) changableColixMap[id] = getColix(argb);
    }

    public void flushShadesAndImageCaches() {
        Colix.flushShades();
        Sphere3D.flushImageCache();
    }

    public static final byte shadeMax = Shade3D.shadeMax;

    public static final byte shadeLast = Shade3D.shadeMax - 1;

    public static final byte shadeNormal = Shade3D.shadeNormal;

    public static final byte intensitySpecularSurfaceLimit = Shade3D.intensitySpecularSurfaceLimit;

    public void setSpecular(boolean specular) {
        Shade3D.setSpecular(specular);
    }

    public boolean getSpecular() {
        return Shade3D.getSpecular();
    }

    public void setSpecularPower(int specularPower) {
        Shade3D.setSpecularPower(specularPower);
    }

    public void setAmbientPercent(int ambientPercent) {
        Shade3D.setAmbientPercent(ambientPercent);
    }

    public void setDiffusePercent(int diffusePercent) {
        Shade3D.setDiffusePercent(diffusePercent);
    }

    public void setSpecularPercent(int specularPercent) {
        Shade3D.setSpecularPercent(specularPercent);
    }

    public void setLightsourceZ(float dist) {
        Shade3D.setLightsourceZ(dist);
    }

    private final Vector3f vectorAB = new Vector3f();

    private final Vector3f vectorAC = new Vector3f();

    private final Vector3f vectorNormal = new Vector3f();

    public int calcIntensityScreen(Point3f screenA, Point3f screenB, Point3f screenC) {
        vectorAB.sub(screenB, screenA);
        vectorAC.sub(screenC, screenA);
        vectorNormal.cross(vectorAB, vectorAC);
        return (vectorNormal.z >= 0 ? Shade3D.calcIntensity(-vectorNormal.x, -vectorNormal.y, vectorNormal.z) : Shade3D.calcIntensity(vectorNormal.x, vectorNormal.y, -vectorNormal.z));
    }

    public static int calcIntensity(float x, float y, float z) {
        return Shade3D.calcIntensity(x, y, z);
    }

    public Font3D getFont3D(int fontSize) {
        return Font3D.getFont3D(Font3D.FONT_FACE_SANS, Font3D.FONT_STYLE_PLAIN, fontSize, platform);
    }

    public Font3D getFont3D(String fontFace, int fontSize) {
        return Font3D.getFont3D(Font3D.getFontFaceID(fontFace), Font3D.FONT_STYLE_PLAIN, fontSize, platform);
    }

    public Font3D getFont3D(String fontFace, String fontStyle, int fontSize) {
        return Font3D.getFont3D(Font3D.getFontFaceID(fontFace), Font3D.getFontStyleID(fontStyle), fontSize, platform);
    }

    public byte getFontFid(int fontSize) {
        return getFont3D(fontSize).fid;
    }

    public byte getFontFid(String fontFace, int fontSize) {
        return getFont3D(fontFace, fontSize).fid;
    }

    public byte getFontFid(String fontFace, String fontStyle, int fontSize) {
        return getFont3D(fontFace, fontStyle, fontSize).fid;
    }

    public static final String[] colorNames = { "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen", "bluetint", "greenblue", "greentint", "grey", "pinktint", "redorange", "yellowtint", "pecyan", "pepurple", "pegreen", "peblue", "peviolet", "pebrown", "pepink", "peyellow", "pedarkgreen", "peorange", "pelightblue", "pedarkcyan", "pedarkgray", "pewhite" };

    public static final int[] colorArgbs = { 0xFFF0F8FF, 0xFFFAEBD7, 0xFF00FFFF, 0xFF7FFFD4, 0xFFF0FFFF, 0xFFF5F5DC, 0xFFFFE4C4, 0xFF000000, 0xFFFFEBCD, 0xFF0000FF, 0xFF8A2BE2, 0xFFA52A2A, 0xFFDEB887, 0xFF5F9EA0, 0xFF7FFF00, 0xFFD2691E, 0xFFFF7F50, 0xFF6495ED, 0xFFFFF8DC, 0xFFDC143C, 0xFF00FFFF, 0xFF00008B, 0xFF008B8B, 0xFFB8860B, 0xFFA9A9A9, 0xFF006400, 0xFFBDB76B, 0xFF8B008B, 0xFF556B2F, 0xFFFF8C00, 0xFF9932CC, 0xFF8B0000, 0xFFE9967A, 0xFF8FBC8F, 0xFF483D8B, 0xFF2F4F4F, 0xFF00CED1, 0xFF9400D3, 0xFFFF1493, 0xFF00BFFF, 0xFF696969, 0xFF1E90FF, 0xFFB22222, 0xFFFFFAF0, 0xFF228B22, 0xFFFF00FF, 0xFFDCDCDC, 0xFFF8F8FF, 0xFFFFD700, 0xFFDAA520, 0xFF808080, 0xFF008000, 0xFFADFF2F, 0xFFF0FFF0, 0xFFFF69B4, 0xFFCD5C5C, 0xFF4B0082, 0xFFFFFFF0, 0xFFF0E68C, 0xFFE6E6FA, 0xFFFFF0F5, 0xFF7CFC00, 0xFFFFFACD, 0xFFADD8E6, 0xFFF08080, 0xFFE0FFFF, 0xFFFAFAD2, 0xFF90EE90, 0xFFD3D3D3, 0xFFFFB6C1, 0xFFFFA07A, 0xFF20B2AA, 0xFF87CEFA, 0xFF778899, 0xFFB0C4DE, 0xFFFFFFE0, 0xFF00FF00, 0xFF32CD32, 0xFFFAF0E6, 0xFFFF00FF, 0xFF800000, 0xFF66CDAA, 0xFF0000CD, 0xFFBA55D3, 0xFF9370DB, 0xFF3CB371, 0xFF7B68EE, 0xFF00FA9A, 0xFF48D1CC, 0xFFC71585, 0xFF191970, 0xFFF5FFFA, 0xFFFFE4E1, 0xFFFFE4B5, 0xFFFFDEAD, 0xFF000080, 0xFFFDF5E6, 0xFF808000, 0xFF6B8E23, 0xFFFFA500, 0xFFFF4500, 0xFFDA70D6, 0xFFEEE8AA, 0xFF98FB98, 0xFFAFEEEE, 0xFFDB7093, 0xFFFFEFD5, 0xFFFFDAB9, 0xFFCD853F, 0xFFFFC0CB, 0xFFDDA0DD, 0xFFB0E0E6, 0xFF800080, 0xFFFF0000, 0xFFBC8F8F, 0xFF4169E1, 0xFF8B4513, 0xFFFA8072, 0xFFF4A460, 0xFF2E8B57, 0xFFFFF5EE, 0xFFA0522D, 0xFFC0C0C0, 0xFF87CEEB, 0xFF6A5ACD, 0xFF708090, 0xFFFFFAFA, 0xFF00FF7F, 0xFF4682B4, 0xFFD2B48C, 0xFF008080, 0xFFD8BFD8, 0xFFFF6347, 0xFF40E0D0, 0xFFEE82EE, 0xFFF5DEB3, 0xFFFFFFFF, 0xFFF5F5F5, 0xFFFFFF00, 0xFF9ACD32, 0xFFAFD7FF, 0xFF2E8B57, 0xFF98FFB3, 0xFF808080, 0xFFFFABBB, 0xFFFF4500, 0xFFF6F675, 0xFF00ffff, 0xFFd020ff, 0xFF00ff00, 0xFF6060ff, 0xFFff80c0, 0xFFa42028, 0xFFffd8d8, 0xFFffff00, 0xFF00c000, 0xFFffb000, 0xFFb0b0ff, 0xFF00a0a0, 0xFF606060, 0xFFffffff };

    private static final Hashtable mapJavaScriptColors = new Hashtable();

    static {
        for (int i = colorNames.length; --i >= 0; ) mapJavaScriptColors.put(colorNames[i], new Integer(colorArgbs[i]));
    }

    public static int getArgbFromString(String strColor) {
        if (strColor != null) {
            if (strColor.length() == 7 && strColor.charAt(0) == '#') {
                try {
                    int red = Integer.parseInt(strColor.substring(1, 3), 16);
                    int grn = Integer.parseInt(strColor.substring(3, 5), 16);
                    int blu = Integer.parseInt(strColor.substring(5, 7), 16);
                    return (0xFF000000 | (red & 0xFF) << 16 | (grn & 0xFF) << 8 | (blu & 0xFF));
                } catch (NumberFormatException e) {
                }
            } else {
                Integer boxedArgb = (Integer) mapJavaScriptColors.get(strColor.toLowerCase());
                if (boxedArgb != null) return boxedArgb.intValue();
            }
        }
        return 0;
    }

    final Vector3f vAB = new Vector3f();

    final Vector3f vAC = new Vector3f();

    public void calcNormalizedNormal(Point3f pointA, Point3f pointB, Point3f pointC, Vector3f vNormNorm) {
        vAB.sub(pointB, pointA);
        vAC.sub(pointC, pointA);
        vNormNorm.cross(vAB, vAC);
        vNormNorm.normalize();
    }

    public void calcXYNormalToLine(Point3f pointA, Point3f pointB, Vector3f vNormNorm) {
        Vector3f axis = new Vector3f(pointA);
        axis.sub(pointB);
        float phi = axis.angle(new Vector3f(0, 1, 0));
        if (phi == 0) {
            vNormNorm.set(1, 0, 0);
        } else {
            vNormNorm.cross(axis, new Vector3f(0, 1, 0));
            vNormNorm.normalize();
        }
    }

    public void calcAveragePoint(Point3f pointA, Point3f pointB, Point3f pointC) {
        Vector3f v = new Vector3f(pointB);
        v.sub(pointA);
        v.scale(1 / 2f);
        pointC.set(pointA);
        pointC.add(v);
    }

    public short getNormix(Vector3f vector) {
        return normix3d.getNormix(vector.x, vector.y, vector.z, Normix3D.NORMIX_GEODESIC_LEVEL);
    }

    public short getNormix(Vector3f vector, int geodesicLevel) {
        return normix3d.getNormix(vector.x, vector.y, vector.z, geodesicLevel);
    }

    public short getInverseNormix(Vector3f vector) {
        return normix3d.getNormix(-vector.x, -vector.y, -vector.z, Normix3D.NORMIX_GEODESIC_LEVEL);
    }

    public short getInverseNormix(short normix) {
        if (normix3d.inverseNormixes != null) return normix3d.inverseNormixes[normix];
        normix3d.calculateInverseNormixes();
        return normix3d.inverseNormixes[normix];
    }

    public short get2SidedNormix(Vector3f vector) {
        return (short) ~normix3d.getNormix(vector.x, vector.y, vector.z, Normix3D.NORMIX_GEODESIC_LEVEL);
    }

    public boolean isDirectedTowardsCamera(short normix) {
        return normix3d.isDirectedTowardsCamera(normix);
    }

    public short getClosestVisibleGeodesicVertexIndex(Vector3f vector, int[] visibilityBitmap, int level) {
        return normix3d.getVisibleNormix(vector.x, vector.y, vector.z, visibilityBitmap, level);
    }

    public boolean isNeighborVertex(short vertex1, short vertex2, int level) {
        return Geodesic3D.isNeighborVertex(vertex1, vertex2, level);
    }

    public Vector3f[] getGeodesicVertexVectors() {
        return Geodesic3D.getVertexVectors();
    }

    public int getGeodesicVertexCount(int level) {
        return Geodesic3D.getVertexCount(level);
    }

    public Vector3f[] getTransformedVertexVectors() {
        return normix3d.getTransformedVectors();
    }

    public Vector3f getNormixVector(short normix) {
        return normix3d.getVector(normix);
    }

    public int getGeodesicFaceCount(int level) {
        return Geodesic3D.getFaceCount(level);
    }

    public short[] getGeodesicFaceVertexes(int level) {
        return Geodesic3D.getFaceVertexes(level);
    }

    public short[] getGeodesicFaceNormixes(int level) {
        return normix3d.getFaceNormixes(level);
    }

    public static final int GEODESIC_START_VERTEX_COUNT = 12;

    public static final int GEODESIC_START_NEIGHBOR_COUNT = 5;

    public short[] getGeodesicNeighborVertexes(int level) {
        return Geodesic3D.getNeighborVertexes(level);
    }
}
