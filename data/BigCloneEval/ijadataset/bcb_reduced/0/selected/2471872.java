package com.d_project.xprint.io;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import com.d_project.xprint.core.AbstractXGraphics;

/**
 * XRawGraphics
 * @author Kazuhiko Arase
 */
public class XRawGraphics extends AbstractXGraphics {

    private DataOutputStream out;

    private double lastX;

    private double lastY;

    public XRawGraphics(DataOutputStream out) {
        this.out = out;
    }

    public void translate(double x, double y) {
        try {
            out.writeByte(XRawCommand.CMD_TYPE_TRANSLATE);
            out.writeDouble(x);
            out.writeDouble(y);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fill(Shape shape) {
        try {
            PathIterator it = shape.getPathIterator(null);
            double[] coords = new double[6];
            lastX = 0;
            lastY = 0;
            out.writeByte(XRawCommand.CMD_TYPE_FILL);
            out.writeByte(it.getWindingRule());
            while (!it.isDone()) {
                int seg = it.currentSegment(coords);
                writePath(seg, coords);
                it.next();
            }
            out.writeByte(XRawCommand.PATH_TYPE_END);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePath(int seg, double[] coords) throws IOException {
        if (seg == PathIterator.SEG_CUBICTO) {
            double[][] quadTo = cubicToQuad(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            writePath(PathIterator.SEG_QUADTO, quadTo[0]);
            writePath(PathIterator.SEG_QUADTO, quadTo[1]);
            return;
        }
        out.writeByte(XRawCommand.PATH_TYPE_PATH);
        out.writeByte(seg);
        switch(seg) {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
                out.writeDouble(coords[0]);
                out.writeDouble(coords[1]);
                lastX = coords[0];
                lastY = coords[1];
                break;
            case PathIterator.SEG_QUADTO:
                out.writeDouble(coords[0]);
                out.writeDouble(coords[1]);
                out.writeDouble(coords[2]);
                out.writeDouble(coords[3]);
                lastX = coords[2];
                lastY = coords[3];
                break;
            case PathIterator.SEG_CUBICTO:
                out.writeDouble(coords[0]);
                out.writeDouble(coords[1]);
                out.writeDouble(coords[2]);
                out.writeDouble(coords[3]);
                out.writeDouble(coords[4]);
                out.writeDouble(coords[5]);
                lastX = coords[4];
                lastY = coords[5];
                break;
            case PathIterator.SEG_CLOSE:
                break;
            default:
                throw new IllegalStateException("seg:" + seg);
        }
    }

    public void setColor(Color color) {
        try {
            out.writeByte(XRawCommand.CMD_TYPE_SET_COLOR);
            out.writeInt((color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawImage(BufferedImage image, double dx1, double dy1, double dx2, double dy2) {
        try {
            out.writeByte(XRawCommand.CMD_TYPE_DRAW_IMAGE);
            out.writeDouble(dx1);
            out.writeDouble(dy1);
            out.writeDouble(dx2 - dx1);
            out.writeDouble(dy2 - dy1);
            byte[] jpegData = ImageUtil.getJpegData(image, 1.0, 1.0f);
            out.writeInt(jpegData.length);
            out.write(jpegData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Point2D subtract(Point2D p1, Point2D p2) {
        return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    private static Point2D getCrossPoint(Point2D a1, Point2D a2, Point2D b1, Point2D b2) throws NoninvertibleTransformException {
        Point2D a12 = subtract(a2, a1);
        Point2D b12 = subtract(b2, b1);
        AffineTransform mat = new AffineTransform(a12.getX(), a12.getY(), b12.getX(), b12.getY(), 0, 0);
        mat = mat.createInverse();
        Point2D st = mat.transform(subtract(b1, a1), null);
        double s = st.getX();
        return new Point2D.Double(a1.getX() + a12.getX() * s, a1.getY() + a12.getY() * s);
    }

    private static Point2D getSubPoint(Point2D p1, Point2D p2) {
        double ratio = 0.5;
        return new Point2D.Double(p1.getX() + (p2.getX() - p1.getX()) * ratio, p1.getY() + (p2.getY() - p1.getY()) * ratio);
    }

    private double[][] cubicToQuad(double controlX1, double controlY1, double controlX2, double controlY2, double anchorX, double anchorY) {
        try {
            Point2D p0 = new Point2D.Double(lastX, lastY);
            Point2D p1 = new Point2D.Double(controlX1, controlY1);
            Point2D p2 = new Point2D.Double(controlX2, controlY2);
            Point2D p3 = new Point2D.Double(anchorX, anchorY);
            Point2D p01 = getSubPoint(p0, p1);
            Point2D p12 = getSubPoint(p1, p2);
            Point2D p23 = getSubPoint(p2, p3);
            Point2D r1 = getSubPoint(p01, p12);
            Point2D r2 = getSubPoint(p12, p23);
            Point2D r12 = getSubPoint(r1, r2);
            Point2D c01 = getCrossPoint(r1, r2, p0, p1);
            Point2D c23 = getCrossPoint(r1, r2, p2, p3);
            return new double[][] { new double[] { c01.getX(), c01.getY(), r12.getX(), r12.getY() }, new double[] { c23.getX(), c23.getY(), p3.getX(), p3.getY() } };
        } catch (Exception e) {
            double cx = (controlX1 + controlX2) / 2;
            double cy = (controlY1 + controlY2) / 2;
            return new double[][] { new double[] { controlX1, controlY1, cx, cy }, new double[] { controlX2, controlY2, anchorX, anchorY } };
        }
    }
}
