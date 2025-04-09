package calclipse.lib.math.util.graph3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.List;
import calclipse.lib.math.mtrx.DenseMatrix;
import calclipse.lib.math.mtrx.Matrix;
import calclipse.lib.math.mtrx.MatrixUtil;

/**
 * A surface bounded by a 3D polygon.
 * @author T. Sommerland
 */
public abstract class Surface3D extends Polygon3D {

    public Surface3D(final XYZ... coords) {
        super(coords);
    }

    public abstract boolean drawBounds();

    public abstract boolean fillBounds();

    public abstract Color boundsColor();

    public abstract Color fillColor();

    private static Polygon getPolygon(final Point[] points) {
        final int[] xpoints = new int[points.length];
        final int[] ypoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            xpoints[i] = points[i].x;
            ypoints[i] = points[i].y;
        }
        return new Polygon(xpoints, ypoints, points.length);
    }

    @Override
    protected void paint(final Graphics g, final Point[] points) {
        final Polygon polygon = getPolygon(points);
        if (fillBounds()) {
            g.setColor(fillColor());
            g.fillPolygon(polygon);
        }
        if (drawBounds()) {
            g.setColor(boundsColor());
            g.drawPolygon(polygon);
        }
    }

    /**
     * Calculates the shade of this surface when it is hit by light.
     * @throws calclipse.lib.math.mtrx.SizeException
     * if the light source is not a vector with three rows.
     */
    public Color getShade(final Color lightColor, final Matrix lightSource) {
        final Matrix normal = getNormal(getCoordinates());
        if (normal == null) {
            return boundsColor();
        }
        double cosang = lightSource.dot(normal) / (MatrixUtil.normF(lightSource) * MatrixUtil.normF(normal));
        if (normal.get(1, 0) < 0) {
            cosang = -cosang;
        }
        final double intensity = (cosang + 1) / 2;
        final double red = lightColor.getRed() * intensity;
        final double green = lightColor.getGreen() * intensity;
        final double blue = lightColor.getBlue() * intensity;
        final int iRed = (int) red;
        final int iGreen = (int) green;
        final int iBlue = (int) blue;
        return new Color(Math.max(Math.min(iRed, 255), 0), Math.max(Math.min(iGreen, 255), 0), Math.max(Math.min(iBlue, 255), 0), lightColor.getAlpha());
    }

    private static Matrix getNormal(final List<? extends XYZ> coords) {
        if (coords.size() < 3) {
            return null;
        }
        Matrix v1 = getVector(coords, 0);
        for (int i = 1; i < coords.size(); i++) {
            final Matrix v2 = getVector(coords, i);
            final Matrix normal = v1.cross(v2);
            if (!isZero(normal)) {
                return normal;
            }
            v1 = v2;
        }
        return null;
    }

    private static Matrix getVector(final List<? extends XYZ> coords, final int index) {
        final XYZ c1;
        final XYZ c2;
        if (index < coords.size() - 1) {
            c1 = coords.get(index);
            c2 = coords.get(index + 1);
        } else {
            c1 = coords.get(index);
            c2 = coords.get(0);
        }
        return new DenseMatrix(new double[][] { { c2.x - c1.x }, { c2.y - c1.y }, { c2.z - c1.z } });
    }

    private static boolean isZero(final Matrix v) {
        return v.get(0, 0) == 0 && v.get(1, 0) == 0 && v.get(2, 0) == 0;
    }
}
