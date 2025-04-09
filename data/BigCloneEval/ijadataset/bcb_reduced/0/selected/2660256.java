package com.sts.webmeet.content.client.appshare;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Java2ScreenScraper extends AbstractScreenScraper {

    public byte[] doJpegCompression(int[] pixels, int iWidth, int iHeight) {
        byte[] baRet = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(baos);
            BufferedImage bi = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
            bi.setRGB(0, 0, iWidth, iHeight, pixels, 0, iWidth);
            JPEGEncodeParam parm = jpeg.getDefaultJPEGEncodeParam(bi);
            parm.setQuality(0.7f, false);
            jpeg.encode(bi, parm);
            baRet = baos.toByteArray();
            bi.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baRet;
    }

    public Java2ScreenScraper() throws Exception {
        super();
        robot = new Robot();
    }

    public void doNativeKeyboard(KeyEvent ke) throws Exception {
        if (this.robot != null) {
            if (ke.getID() == KeyEvent.KEY_PRESSED) {
                this.robot.keyPress(ke.getKeyCode());
            } else if (ke.getID() == KeyEvent.KEY_RELEASED) {
                this.robot.keyRelease(ke.getKeyCode());
            } else {
                System.out.println("only press and release key events supported: " + ke);
            }
        }
    }

    public int[] grabPixels(int[] pixels, Rectangle rect) throws Exception {
        BufferedImage bImage = grabBufferedImage(rect);
        PixelGrabber pg = new PixelGrabber(bImage, 0, 0, rect.width, rect.height, pixels, 0, rect.width);
        pg.grabPixels();
        return pixels;
    }

    public void stopScraping() {
        super.stopScraping();
        robot = null;
    }

    private BufferedImage grabBufferedImage(Rectangle rect) {
        BufferedImage bi = robot.createScreenCapture(rect);
        drawVirtualCursor(bi, rect);
        return bi;
    }

    private void drawVirtualCursor(BufferedImage bi, Rectangle rectCapture) {
        Point point = MouseLocator.getMouseLocation();
        if (null != point) {
            Polygon polygon = new Polygon(this.X_POINTS, this.Y_POINTS, this.X_POINTS.length);
            polygon.translate(point.x - rectCapture.x, point.y - rectCapture.y);
            Graphics2D graphics2d = (Graphics2D) bi.getGraphics();
            graphics2d.setXORMode(Color.GREEN);
            graphics2d.fillPolygon(polygon);
            graphics2d.setXORMode(Color.DARK_GRAY);
            graphics2d.drawPolygon(polygon);
        }
    }

    private Robot robot;

    private static final int[] X_POINTS = { 0, 0, 8, 15, 23, 16, 24 };

    private static final int[] Y_POINTS = { 0, 35, 32, 45, 42, 28, 24 };
}
