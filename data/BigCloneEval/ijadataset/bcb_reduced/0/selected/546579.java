package org.das2.graph;

import org.das2.datum.DatumRange;
import org.das2.datum.Units;
import org.das2.datum.Datum;
import org.das2.datum.UnitsUtil;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.*;
import org.das2.datum.DatumRangeUtil;
import org.virbo.dataset.DataSetOps;
import org.virbo.dataset.QDataSet;
import org.virbo.dataset.RankZeroDataSet;
import org.virbo.dataset.SemanticOps;
import org.virbo.dsops.Ops;

/**
 *
 * @author  Jeremy
 */
public class GraphUtil {

    public static DasPlot newDasPlot(DasCanvas canvas, DatumRange x, DatumRange y) {
        DasAxis xaxis = new DasAxis(x.min(), x.max(), DasAxis.HORIZONTAL);
        DasAxis yaxis = new DasAxis(y.min(), y.max(), DasAxis.VERTICAL);
        DasRow row = new DasRow(canvas, null, 0, 1, 2, -3, 0, 0);
        DasColumn col = new DasColumn(canvas, null, 0, 1, 5, -3, 0, 0);
        DasPlot result = new DasPlot(xaxis, yaxis);
        canvas.add(result, row, col);
        return result;
    }

    public static GeneralPath getPath(DasAxis xAxis, DasAxis yAxis, QDataSet ds, boolean histogram, boolean clip) {
        return getPath(xAxis, yAxis, SemanticOps.xtagsDataSet(ds), ds, histogram, clip);
    }

    /**
     *
     * @param xAxis
     * @param yAxis
     * @param xds
     * @param yds
     * @param histogram histogram (stair-step) mode
     * @param clip limit path to what's visible for each axis.
     * @return
     */
    public static GeneralPath getPath(DasAxis xAxis, DasAxis yAxis, QDataSet xds, QDataSet yds, boolean histogram, boolean clip) {
        GeneralPath newPath = new GeneralPath();
        Dimension d;
        Units xUnits = SemanticOps.getUnits(xds);
        Units yUnits = SemanticOps.getUnits(yds);
        QDataSet tagds = SemanticOps.xtagsDataSet(xds);
        double xSampleWidth = Double.MAX_VALUE;
        if (tagds.property(QDataSet.CADENCE) != null) {
        }
        double i0 = -Double.MAX_VALUE;
        double j0 = -Double.MAX_VALUE;
        boolean v0 = false;
        boolean skippedLast = true;
        int n = xds.length();
        QDataSet wds = SemanticOps.weightsDataSet(yds);
        Rectangle rclip = clip ? DasDevicePosition.toRectangle(yAxis.getRow(), xAxis.getColumn()) : null;
        for (int index = 0; index < n; index++) {
            double t = index;
            double x = xds.value(index);
            double y = yds.value(index);
            double i = xAxis.transform(x, xUnits);
            double j = yAxis.transform(y, yUnits);
            boolean v = rclip == null || rclip.contains(i, j);
            if (wds.value(index) == 0 || Double.isNaN(y)) {
                skippedLast = true;
            } else if (skippedLast) {
                newPath.moveTo((float) i, (float) j);
                skippedLast = !v;
            } else {
                if (v || v0) {
                    if (histogram) {
                        double i1 = (i0 + i) / 2;
                        newPath.lineTo((float) i1, (float) j0);
                        newPath.lineTo((float) i1, (float) j);
                        newPath.lineTo((float) i, (float) j);
                    } else {
                        newPath.lineTo((float) i, (float) j);
                    }
                }
                skippedLast = false;
            }
            i0 = i;
            j0 = j;
            v0 = v;
        }
        return newPath;
    }

    /**
     * calculates the AffineTransform between two sets of x and y axes, if possible.
     * @param xaxis0 the original reference frame x axis
     * @param yaxis0 the original reference frame y axis
     * @param xaxis1 the new reference frame x axis
     * @param yaxis1 the new reference frame y axis
     * @return an AffineTransform that transforms data positioned with xaxis0 and yaxis0 on xaxis1 and yaxis1, or null if no such transform exists.
     */
    public static AffineTransform calculateAT(DasAxis xaxis0, DasAxis yaxis0, DasAxis xaxis1, DasAxis yaxis1) {
        return calculateAT(xaxis0.getDatumRange(), yaxis0.getDatumRange(), xaxis1, yaxis1);
    }

    public static AffineTransform calculateAT(DatumRange xaxis0, DatumRange yaxis0, DasAxis xaxis1, DasAxis yaxis1) {
        AffineTransform at = new AffineTransform();
        double dmin0 = xaxis1.transform(xaxis0.min());
        double dmax0 = xaxis1.transform(xaxis0.max());
        double dmin1 = xaxis1.transform(xaxis1.getDataMinimum());
        double dmax1 = xaxis1.transform(xaxis1.getDataMaximum());
        double scalex = (dmin0 - dmax0) / (dmin1 - dmax1);
        double transx = -1 * dmin1 * scalex + dmin0;
        at.translate(transx, 0);
        at.scale(scalex, 1.);
        if (at.getDeterminant() == 0.000) {
            return null;
        }
        dmin0 = yaxis1.transform(yaxis0.min());
        dmax0 = yaxis1.transform(yaxis0.max());
        dmin1 = yaxis1.transform(yaxis1.getDataMinimum());
        dmax1 = yaxis1.transform(yaxis1.getDataMaximum());
        double scaley = (dmin0 - dmax0) / (dmin1 - dmax1);
        double transy = -1 * dmin1 * scaley + dmin0;
        at.translate(0, transy);
        at.scale(1., scaley);
        return at;
    }

    public static DasAxis guessYAxis(QDataSet dsz) {
        boolean log = false;
        if (dsz.property(QDataSet.SCALE_TYPE) != null) {
            if (dsz.property(QDataSet.SCALE_TYPE).equals("log")) {
                log = true;
            }
        }
        DasAxis result;
        if (SemanticOps.isSimpleTableDataSet(dsz)) {
            QDataSet ds = (QDataSet) dsz;
            QDataSet yds = SemanticOps.ytagsDataSet(ds);
            DatumRange yrange = org.virbo.dataset.DataSetUtil.asDatumRange(Ops.extent(yds), true);
            yrange = DatumRangeUtil.rescale(yrange, -0.1, 1.1);
            Datum dy = org.virbo.dataset.DataSetUtil.asDatum(org.virbo.dataset.DataSetUtil.guessCadenceNew(yds, null));
            if (UnitsUtil.isRatiometric(dy.getUnits())) {
                log = true;
            }
            result = new DasAxis(yrange.min(), yrange.max(), DasAxis.LEFT, log);
        } else if (!SemanticOps.isTableDataSet(dsz)) {
            QDataSet yds = dsz;
            if (SemanticOps.isBundle(dsz)) {
                yds = DataSetOps.unbundleDefaultDataSet(dsz);
                dsz = yds;
            }
            DatumRange yrange = org.virbo.dataset.DataSetUtil.asDatumRange(Ops.extent(yds), true);
            yrange = DatumRangeUtil.rescale(yrange, -0.1, 1.1);
            result = new DasAxis(yrange.min(), yrange.max(), DasAxis.LEFT, log);
        } else {
            throw new IllegalArgumentException("not supported: " + dsz);
        }
        if (dsz.property(QDataSet.LABEL) != null) {
            result.setLabel((String) dsz.property(QDataSet.LABEL));
        }
        return result;
    }

    public static DasAxis guessXAxis(QDataSet ds) {
        QDataSet xds = SemanticOps.xtagsDataSet(ds);
        DatumRange range = org.virbo.dataset.DataSetUtil.asDatumRange(Ops.extent(xds), true);
        range = DatumRangeUtil.rescale(range, -0.1, 1.1);
        return new DasAxis(range.min(), range.max(), DasAxis.BOTTOM);
    }

    public static DasAxis guessZAxis(QDataSet dsz) {
        if (!(SemanticOps.isTableDataSet(dsz))) {
            throw new IllegalArgumentException("only TableDataSet supported");
        }
        QDataSet ds = (QDataSet) dsz;
        DatumRange range = org.virbo.dataset.DataSetUtil.asDatumRange(Ops.extent(ds), true);
        boolean log = false;
        if ("log".equals(dsz.property(QDataSet.SCALE_TYPE))) {
            log = true;
            if (range.min().doubleValue(range.getUnits()) <= 0) {
                double max = range.max().doubleValue(range.getUnits());
                range = new DatumRange(max / 1000, max, range.getUnits());
            }
        }
        DasAxis result = new DasAxis(range.min(), range.max(), DasAxis.LEFT, log);
        if (dsz.property(QDataSet.LABEL) != null) {
            result.setLabel((String) dsz.property(QDataSet.LABEL));
        }
        return result;
    }

    /**
     * legacy guess that is used who-knows-where.  Autoplot has much better code
     * for guessing, refer to it.
     * @param ds
     * @return
     */
    public static Renderer guessRenderer(QDataSet ds) {
        Renderer rend = null;
        if (!SemanticOps.isTableDataSet(ds)) {
            if (ds.length() > 10000) {
                rend = new ImageVectorDataSetRenderer(null);
                rend.setDataSet(ds);
            } else {
                rend = new SeriesRenderer();
                rend.setDataSet(ds);
                ((SeriesRenderer) rend).setPsym(DefaultPlotSymbol.CIRCLES);
                ((SeriesRenderer) rend).setSymSize(2.0);
            }
        } else if (SemanticOps.isTableDataSet(ds)) {
            DasAxis zaxis = guessZAxis(ds);
            DasColorBar colorbar = new DasColorBar(zaxis.getDataMinimum(), zaxis.getDataMaximum(), zaxis.isLog());
            colorbar.setLabel(zaxis.getLabel());
            rend = new SpectrogramRenderer(null, colorbar);
            rend.setDataSet(ds);
        }
        return rend;
    }

    public static DasPlot guessPlot(QDataSet ds) {
        DasAxis xaxis = guessXAxis(ds);
        DasAxis yaxis = guessYAxis(ds);
        DasPlot plot = new DasPlot(xaxis, yaxis);
        plot.addRenderer(guessRenderer(ds));
        return plot;
    }

    public static DasPlot visualize(QDataSet ds) {
        JFrame jframe = new JFrame("DataSetUtil.visualize");
        DasCanvas canvas = new DasCanvas(400, 400);
        jframe.getContentPane().add(canvas);
        DasPlot result = guessPlot(ds);
        canvas.add(result, DasRow.create(canvas), DasColumn.create(canvas, null, "5em", "100%-10em"));
        jframe.pack();
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return result;
    }

    public static int reducePath(PathIterator it, GeneralPath result) {
        return reducePath(it, result, 1);
    }

    /**
     * Returns the input GeneralPath filled with new points which will be rendered identically to the input path,
     * but contains a minimal number of points.  We bin average the points within a cell, because descretization
     * would mess up the label orientation in contour plotting.
     *
     * a new GeneralPath which will be rendered identically to the input path,
     * but contains a minimal number of points.
     *
     * @return the number of "points" (LINE_TOs) in the result.
     * @param it A path iterator with minute details that will be lost when rendering.
     * @param result A GeneralPath to put the result into.
     */
    public static int reducePath(PathIterator it, GeneralPath result, int res) {
        float[] p = new float[6];
        float x0 = Float.MAX_VALUE;
        float y0 = Float.MAX_VALUE;
        float sx0 = 0;
        float sy0 = 0;
        int nx0 = 0;
        int ny0 = 0;
        float ax0 = Float.NaN;
        float ay0 = Float.NaN;
        int type0 = -999;
        float xres = res;
        float yres = res;
        int points = 0;
        int inCount = 0;
        while (!it.isDone()) {
            inCount++;
            int type = it.currentSegment(p);
            it.next();
            float dx = p[0] - x0;
            float dy = p[1] - y0;
            if ((type == PathIterator.SEG_MOVETO || type == type0) && Math.abs(dx) < xres && Math.abs(dy) < yres) {
                sx0 += p[0];
                sy0 += p[1];
                nx0 += 1;
                ny0 += 1;
                continue;
            } else {
                x0 = 0.5f + (int) Math.floor(p[0]);
                y0 = 0.5f + (int) Math.floor(p[1]);
                ax0 = nx0 > 0 ? sx0 / nx0 : p[0];
                ay0 = ny0 > 0 ? sy0 / ny0 : p[1];
                sx0 = p[0];
                sy0 = p[1];
                nx0 = 1;
                ny0 = 1;
            }
            switch(type0) {
                case PathIterator.SEG_LINETO:
                    result.lineTo(ax0, ay0);
                    points++;
                    break;
                case PathIterator.SEG_MOVETO:
                    result.moveTo(ax0, ay0);
                    break;
                case -999:
                    break;
                default:
                    throw new IllegalArgumentException("not supported");
            }
            type0 = type;
        }
        ax0 = nx0 > 0 ? sx0 / nx0 : p[0];
        ay0 = ny0 > 0 ? sy0 / ny0 : p[1];
        switch(type0) {
            case PathIterator.SEG_LINETO:
                result.lineTo(ax0, ay0);
                points++;
                break;
            case PathIterator.SEG_MOVETO:
                result.moveTo(ax0, ay0);
                break;
            case -999:
                break;
            default:
                throw new IllegalArgumentException("not supported");
        }
        return points;
    }

    /**
     * return the points along a curve.  Used by ContourRenderer.  The returned
     * result is the remaining path length.  Elements of pathlen that are beyond
     * the total path length are not computed, and the result points will be null.
     * @param pathlen monotonically increasing path lengths at which the position is to be located.  May be null if only the total path length is desired.
     * @param result the resultant points will be put into this array.  This array should have the same number of elements as pathlen
     * @param orientation the local orientation, in radians, of the point at will be put into this array.  This array should have the same number of elements as pathlen
     * @param it PathIterator first point is used to start the length.
     * @param stopAtMoveTo treat SEG_MOVETO as the end of the path.  The pathIterator will be left at this point.
     * @return the remaining length.  Note null may be used for pathlen, result, and orientation and this will simply return the total path length.
     */
    public static double pointsAlongCurve(PathIterator it, double[] pathlen, Point2D.Double[] result, double[] orientation, boolean stopAtMoveTo) {
        float[] point = new float[6];
        float fx0 = Float.NaN, fy0 = Float.NaN;
        double slen = 0;
        int pathlenIndex = 0;
        int type;
        if (pathlen == null) {
            pathlen = new double[0];
        }
        while (!it.isDone()) {
            type = it.currentSegment(point);
            it.next();
            if (!Float.isNaN(fx0) && type == PathIterator.SEG_MOVETO && stopAtMoveTo) {
                break;
            }
            if (PathIterator.SEG_CUBICTO == type) {
                throw new IllegalArgumentException("cubicto not supported");
            } else if (PathIterator.SEG_QUADTO == type) {
                throw new IllegalArgumentException("quadto not supported");
            } else if (PathIterator.SEG_LINETO == type) {
            }
            if (Float.isNaN(fx0)) {
                fx0 = point[0];
                fy0 = point[1];
                continue;
            }
            double thislen = (float) Point.distance(fx0, fy0, point[0], point[1]);
            if (thislen == 0) {
                continue;
            } else {
                slen += thislen;
            }
            while (pathlenIndex < pathlen.length && slen >= pathlen[pathlenIndex]) {
                double alpha = 1 - (slen - pathlen[pathlenIndex]) / thislen;
                double dx = point[0] - fx0;
                double dy = point[1] - fy0;
                if (result != null) {
                    result[pathlenIndex] = new Point2D.Double(fx0 + dx * alpha, fy0 + dy * alpha);
                }
                if (orientation != null) {
                    orientation[pathlenIndex] = Math.atan2(dy, dx);
                }
                pathlenIndex++;
            }
            fx0 = point[0];
            fy0 = point[1];
        }
        double remaining;
        if (pathlenIndex > 0) {
            remaining = slen - pathlen[pathlenIndex - 1];
        } else {
            remaining = slen;
        }
        if (result != null) {
            for (; pathlenIndex < result.length; pathlenIndex++) {
                result[pathlenIndex] = null;
            }
        }
        return remaining;
    }

    /**
     * @return a string representation of the affine transforms used in DasPlot for
     * debugging.
     */
    public static String getATScaleTranslateString(AffineTransform at) {
        String atDesc;
        NumberFormat nf = new DecimalFormat("0.00");
        if (at == null) {
            return "null";
        } else if (!at.isIdentity()) {
            atDesc = "scaleX:" + nf.format(at.getScaleX()) + " translateX:" + nf.format(at.getTranslateX());
            atDesc += "!c" + "scaleY:" + nf.format(at.getScaleY()) + " translateY:" + nf.format(at.getTranslateY());
            return atDesc;
        } else {
            return "identity";
        }
    }

    /**
     * calculates the slope and intercept of a line going through two points.
     * @return a double array with two elements [ slope, intercept ].
     */
    public static double[] getSlopeIntercept(double x0, double y0, double x1, double y1) {
        double slope = (y1 - y0) / (x1 - x0);
        double intercept = y0 - slope * x0;
        return new double[] { slope, intercept };
    }

    public static Color getRicePaperColor() {
        return new Color(255, 255, 255, 128);
    }

    public static String describe(GeneralPath path, boolean enumeratePoints) {
        PathIterator it = path.getPathIterator(null);
        int count = 0;
        int lineToCount = 0;
        double[] coords = new double[6];
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_LINETO) {
                lineToCount++;
            }
            if (enumeratePoints) {
            }
            count++;
            it.next();
        }
        return "count: " + count + "  lineToCount: " + lineToCount;
    }

    static String toString(Line2D line) {
        return "" + line.getX1() + "," + line.getY1() + " " + line.getX2() + "," + line.getY2();
    }

    /**
     * returns the point where the two line segments intersect, or null.
     * @param line1
     * @param line2
     * @param noBoundsCheck if true, then do not check the segment bounds.
     * @return
     */
    public static Point2D lineIntersection(Line2D line1, Line2D line2, boolean noBoundsCheck) {
        Point2D result = null;
        double a1, b1, c1, a2, b2, c2, denom;
        a1 = line1.getY2() - line1.getY1();
        b1 = line1.getX1() - line1.getX2();
        c1 = line1.getX2() * line1.getY1() - line1.getX1() * line1.getY2();
        a2 = line2.getY2() - line2.getY1();
        b2 = line2.getX1() - line2.getX2();
        c2 = line2.getX2() * line2.getY1() - line2.getX1() * line2.getY2();
        denom = a1 * b2 - a2 * b1;
        if (denom != 0) {
            result = new Point2D.Double((b1 * c2 - b2 * c1) / denom, (a2 * c1 - a1 * c2) / denom);
            if (noBoundsCheck || (((result.getX() - line1.getX1()) * (line1.getX2() - result.getX()) >= 0) && ((result.getY() - line1.getY1()) * (line1.getY2() - result.getY()) >= 0) && ((result.getX() - line2.getX1()) * (line2.getX2() - result.getX()) >= 0) && ((result.getY() - line2.getY1()) * (line2.getY2() - result.getY()) >= 0))) {
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Point2D lineRectangleIntersection(Point2D p0, Point2D p1, Rectangle2D r0) {
        PathIterator it = r0.getPathIterator(null);
        Line2D line = new Line2D.Double(p0, p1);
        float[] c0 = new float[6];
        float[] c1 = new float[6];
        it.currentSegment(c0);
        it.next();
        while (!it.isDone()) {
            int type = it.currentSegment(c1);
            if (type == PathIterator.SEG_LINETO) {
                Line2D seg = new Line2D.Double(c0[0], c0[1], c1[0], c1[1]);
                Point2D result = lineIntersection(line, seg, false);
                if (result != null) {
                    return result;
                }
            }
            it.next();
            c0[0] = c1[0];
            c0[1] = c1[1];
        }
        return null;
    }

    /**
     * returns pixel range of the datum range, guarenteeing that the first 
     * element will be less than or equal to the second.
     * @param axis
     * @param range
     * @return
     */
    public static double[] transformRange(DasAxis axis, DatumRange range) {
        double x1 = axis.transform(range.min());
        double x2 = axis.transform(range.max());
        if (x1 > x2) {
            double t = x2;
            x2 = x1;
            x1 = t;
        }
        return new double[] { x1, x2 };
    }

    public static DatumRange invTransformRange(DasAxis axis, double x1, double x2) {
        Datum d1 = axis.invTransform(x1);
        Datum d2 = axis.invTransform(x2);
        if (d1.gt(d2)) {
            Datum t = d2;
            d2 = d1;
            d1 = t;
        }
        return new DatumRange(d1, d2);
    }

    /**
     * return a block with the color and size.
     * @param w
     * @param h
     * @return 
     */
    public static Icon colorIcon(Color iconColor, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        if (iconColor.getAlpha() != 255) {
            for (int j = 0; j < 16 / 4; j++) {
                for (int i = 0; i < 16 / 4; i++) {
                    g.setColor((i - j) % 2 == 0 ? Color.GRAY : Color.WHITE);
                    g.fillRect(0 + i * 4, 0 + j * 4, 4, 4);
                }
            }
        }
        g.setColor(iconColor);
        g.fillRect(0, 0, w, h);
        return new ImageIcon(image);
    }
}
