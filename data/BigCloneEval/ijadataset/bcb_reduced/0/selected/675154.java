package com.ibm.tuningfork.piechart;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import com.ibm.tuningfork.core.figure.FigurePainter;
import com.ibm.tuningfork.core.graphics.Area;
import com.ibm.tuningfork.core.graphics.Coord;
import com.ibm.tuningfork.core.graphics.Extent;
import com.ibm.tuningfork.core.graphics.RGBColor;
import com.ibm.tuningfork.core.graphics.TFColor;
import com.ibm.tuningfork.core.graphics.TextStyle;
import com.ibm.tuningfork.infra.data.BarredHistogramValue;
import com.ibm.tuningfork.infra.data.HistogramValue;
import com.ibm.tuningfork.infra.util.MathUtils;

public class PieChartPainter extends FigurePainter {

    protected RGBColor paperColor;

    protected TextStyle labelStyle;

    protected RGBColor borderColor;

    public int centerX;

    public int centerY;

    int[] leftOvers_HACK;

    double scaleFactor_HACK;

    Coord[] othersPosition_HACK = new Coord[3];

    protected static final double SECOND_PIE_FRACTION = 0.4;

    protected static final int EXPLOSION_POLYGON_ALPHA = 100;

    protected static final int EXPLODED_PIE_ALPHA = 65;

    private static final int FONTSIZE = 12;

    public PieChartPainter(PieChart figure, TFColor paperColor, TextStyle labelStyle, TFColor borderColor) {
        super(figure);
        this.borderColor = borderColor.toRGBColor();
        this.paperColor = paperColor.toRGBColor();
        this.labelStyle = labelStyle.withFontSize(FONTSIZE).beTransparent();
    }

    public static double cartesianToDegree(Coord coord) {
        double ratio = ((double) coord.y) / ((double) coord.x);
        double theta = Math.atan(ratio);
        double degrees = Math.toDegrees(theta);
        if (coord.x < 0 && coord.y >= 0) {
            degrees = 180 + degrees;
        } else if (coord.x < 0 && coord.y < 0) {
            degrees = 180 + degrees;
        } else if (coord.x > 0 && coord.y < 0) {
            degrees = 360 + degrees;
        }
        return degrees;
    }

    private static class Wedge {

        public int category;

        public int count;

        public String name;

        public double innerGap;

        public double outerGap;

        public Coord center;

        public int radius;

        public Coord startEdge, endEdge;

        public Coord midEdge;

        public Coord midInner;

        public Coord midOuter;

        public double fraction;

        public double startAngle;

        public double midAngle;

        public double endAngle;

        public boolean isLeft;

        public Wedge(int category, int count, double frac, String name) {
            this.category = category;
            this.count = count;
            this.name = name;
            this.fraction = frac;
        }

        public void setInfo(Coord cent, int radius, double startAng, double endAng) {
            center = cent;
            startAngle = startAng;
            endAngle = endAng;
            midAngle = (startAngle + endAngle) / 2.0;
            isLeft = ((midAngle >= (0.5 * Math.PI)) && (midAngle <= (1.5 * Math.PI)));
            innerGap = 30;
            outerGap = 20;
            if (midAngle > 1.35 * Math.PI && midAngle < 1.65 * Math.PI) {
                outerGap += 5;
            }
            if (midAngle > 1.45 * Math.PI && midAngle < 1.55 * Math.PI) {
                outerGap += 5;
            }
            startEdge = center.fromPolar(radius, startAngle);
            midEdge = center.fromPolar(radius, midAngle);
            endEdge = center.fromPolar(radius, endAngle);
            midInner = center.fromPolar(radius - innerGap, midAngle);
            midOuter = center.fromPolar(radius + outerGap, midAngle);
        }

        public boolean isLeftRotated() {
            double angle = (startAngle + endAngle) / 2;
            int rotations = 0;
            if (angle < 0) {
                rotations = 1 + (int) ((-angle) / (2 * Math.PI));
            } else {
                rotations = -((int) (angle / (2 * Math.PI)));
            }
            angle += (2 * Math.PI) * rotations;
            isLeft = ((angle >= (0.5 * Math.PI)) && (angle <= (1.5 * Math.PI)));
            return isLeft;
        }

        public static Comparator<Wedge> countComparator = new Comparator<Wedge>() {

            public int compare(Wedge a, Wedge b) {
                int ac = a.count;
                int bc = b.count;
                if (ac == bc) {
                    return 0;
                }
                return (ac < bc) ? 1 : -1;
            }

            public boolean equals(Object o) {
                return false;
            }
        };
    }

    public int[] paint(HistogramValue pieChart, Area magnifyArea, boolean displayWedgeNames, double startingAngle, int explosionLimit) {
        if (pieChart == null) {
            g.drawString(TextStyle.norm().beOpaque(), "No data available", r.center());
            return new int[0];
        }
        int maxCat = pieChart.getMaxCategory();
        int[] allCounts = new int[maxCat];
        BarredHistogramValue bhv = pieChart.getCachedBarredHistogramValue(0, 1, 1);
        double[] mainBin = bhv.getBar(0);
        if (mainBin != null) {
            for (int j = 0; j < mainBin.length; j++) {
                allCounts[j] += mainBin[j];
            }
        }
        scaleFactor_HACK = 1.0;
        othersPosition_HACK[0] = null;
        Area pieArea = r;
        int[] categoryRemapper = paintPie(pieArea, pieChart, displayWedgeNames, startingAngle, maxCat, allCounts, explosionLimit == 1);
        for (int pie = 1; pie < explosionLimit; pie++) {
            int remainingCats = leftOvers_HACK.length;
            if (remainingCats > 0) {
                int[] remainingAllCounts = new int[remainingCats];
                for (int i = 0; i < remainingCats; i++) {
                    remainingAllCounts[i] = allCounts[leftOvers_HACK[i]];
                }
                pieArea = pieArea.isWide() ? pieArea.rightPortion(SECOND_PIE_FRACTION) : pieArea.bottomPortion(SECOND_PIE_FRACTION);
                paintPie(pieArea, pieChart, displayWedgeNames, startingAngle, remainingCats, remainingAllCounts, pie == explosionLimit - 1);
            }
        }
        return categoryRemapper;
    }

    private int[] paintPie(Area pieArea, HistogramValue pieChart, boolean displayWedgeNames, double startingAngle, int maxCat, int[] allCounts, boolean lastPie) {
        int[] categoryRemapper = new int[maxCat];
        int totalCount = 0;
        for (int i = 0; i < maxCat; i++) {
            totalCount += allCounts[i];
        }
        if (totalCount == 0) {
            g.drawString(TextStyle.forColor(borderColor).beOpaque(), "No Data", pieArea.center());
            return categoryRemapper;
        }
        double cutOffFraction = 0.02;
        int cutoff = (int) (cutOffFraction * totalCount);
        int catCount = 0, leftoverCount = 0;
        for (int i = 0; i < maxCat; i++) {
            if (allCounts[i] >= cutoff) {
                catCount++;
            } else {
                leftoverCount++;
            }
        }
        leftOvers_HACK = new int[leftoverCount];
        int leftOversIndex = 0;
        if (leftoverCount > 0 && !lastPie) {
            pieArea = pieArea.isWide() ? pieArea.leftPortion(1.0 - SECOND_PIE_FRACTION) : pieArea.topPortion(1.0 - SECOND_PIE_FRACTION);
        }
        Wedge[] wedges;
        Wedge othersWedge = null;
        if (leftoverCount > 0) {
            wedges = new Wedge[catCount + 1];
            othersWedge = new Wedge(catCount, 0, 0.0, lastPie ? "Others" : "");
            wedges[catCount] = othersWedge;
        } else {
            wedges = new Wedge[catCount];
        }
        for (int i = 0, j = 0; i < maxCat; i++) {
            if (allCounts[i] >= cutoff) {
                wedges[j++] = new Wedge(i, allCounts[i], allCounts[i] / (double) totalCount, pieChart.getCategoryName(i));
            } else {
                othersWedge.category = i;
                othersWedge.count += allCounts[i];
                othersWedge.fraction = othersWedge.count / (double) totalCount;
                leftOvers_HACK[leftOversIndex++] = i;
            }
        }
        Arrays.sort(wedges, 0, catCount, Wedge.countComparator);
        final int extent = Math.min(pieArea.width, pieArea.height);
        final int radius = (extent / 2) - 40;
        centerX = pieArea.x + pieArea.width / 2;
        centerY = pieArea.y + pieArea.height / 2;
        final Coord center = new Coord(centerX, centerY);
        final int left = centerX - radius;
        final int top = centerY - radius;
        paintOthersExplosionPolygon(othersPosition_HACK, center, radius);
        int accCount = 0;
        double offsetRadians = Math.toRadians(startingAngle + ((othersWedge == null) ? 0 : (180 * othersWedge.fraction)));
        for (int i = 0; i < wedges.length; i++) {
            Wedge wedge = wedges[i];
            int curCount = accCount + wedge.count;
            double prevAngle = MathUtils.fracToRadian(((double) accCount / totalCount)) + offsetRadians;
            double angle = MathUtils.fracToRadian(((double) curCount / totalCount)) + offsetRadians;
            wedge.setInfo(center, radius, prevAngle, angle);
            accCount = curCount;
        }
        for (int i = 0; i < wedges.length; i++) {
            TFColor fillColor = appearanceRegistry.getDataColor(i);
            double angleStart = Math.toDegrees(wedges[i].startAngle);
            double angleEnd = Math.toDegrees(wedges[i].endAngle);
            Coord edge = positionOnCircle(wedges[0].center, radius, angleStart);
            if (!g.hasFractionalArc()) {
                angleStart = Math.floor(angleStart + 0.5);
                angleEnd = Math.floor(angleEnd + 0.5);
            }
            if (i == wedges.length - 1) {
                if (lastPie && leftoverCount > 0) {
                    g.fillArc(borderColor, EXPLOSION_POLYGON_ALPHA, left, top, 2 * radius, 2 * radius, angleStart, angleEnd - angleStart);
                } else {
                    othersPosition_HACK[0] = edge;
                    othersPosition_HACK[1] = positionOnCircle(wedges[0].center, radius, angleEnd);
                    othersPosition_HACK[2] = center;
                }
            } else {
                g.fillArc(fillColor.toRGBColor(), left, top, 2 * radius, 2 * radius, angleStart, angleEnd - angleStart);
            }
            g.drawLine(fillColor.asDisneyOutlineColor().toRGBColor(), wedges[0].center.x, wedges[i].center.y, edge.x, edge.y);
        }
        g.strokeArc(borderColor, left, top, 2 * radius, 2 * radius, 0, 360);
        if (displayWedgeNames) {
            paintWedgeNames(wedges);
        }
        paintWedgePercentages(wedges, scaleFactor_HACK);
        for (int i = 0; i < maxCat; i++) {
            categoryRemapper[i] = catCount;
        }
        for (int i = 0; i < wedges.length; i++) {
            categoryRemapper[wedges[i].category] = i;
        }
        if (leftoverCount > 0) {
            scaleFactor_HACK = scaleFactor_HACK * othersWedge.fraction;
        }
        return categoryRemapper;
    }

    private void paintOthersExplosionPolygon(Coord[] endPoints, Coord center, final int radius) {
        if (endPoints[0] != null) {
            Coord othersTop = endPoints[1];
            Coord othersBot = endPoints[0];
            if (endPoints[1].y > endPoints[0].y) {
                othersTop = endPoints[0];
                othersBot = endPoints[1];
            }
            Coord newTop = tangentFromExternalPoint(othersTop, center, radius, false);
            Coord newBot = tangentFromExternalPoint(othersBot, center, radius, true);
            Coord[] coords = { othersTop, newTop, newBot, othersBot, endPoints[2] };
            g.fillPolygon(borderColor, EXPLOSION_POLYGON_ALPHA, coords);
        }
    }

    private static Coord tangentFromExternalPoint(Coord point, Coord center, int radius, boolean flip) {
        int dx = center.x - point.x;
        int dy = center.y - point.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.asin(radius / distance);
        double length = Math.sqrt(distance * distance - radius * radius);
        double angleAdjustment = Math.atan(dy / (double) dx);
        if (flip) {
            angle = -angle;
        }
        Coord result = positionOnCircle(point, (int) length, Math.toDegrees(angle - angleAdjustment));
        return result;
    }

    public static Coord positionOnCircle(Coord c, int radius, double angleInDegrees) {
        int x = c.x + (int) (radius * Math.cos(Math.toRadians(angleInDegrees)));
        int y = c.y - (int) (radius * Math.sin(Math.toRadians(angleInDegrees)));
        return new Coord(x, y);
    }

    private void paintWedgeNames(Wedge[] wedges) {
        for (int i = 0; i < wedges.length; i++) {
            String name = wedges[i].name;
            if (name.equals("")) {
                continue;
            }
            Extent nameExtent = g.stringExtent(labelStyle, name);
            int xOffset = wedges[i].isLeftRotated() ? nameExtent.width + 2 : 0 - 2;
            int yOffset = nameExtent.height / 2;
            Coord labelPos = wedges[i].midOuter.minus(new Extent(xOffset, yOffset));
            g.drawString(labelStyle, name, labelPos);
            g.drawLine(labelStyle.color, wedges[i].midEdge.x, wedges[i].midEdge.y, wedges[i].midOuter.x, wedges[i].midOuter.y);
        }
    }

    private void paintWedgePercentages(Wedge[] wedges, double scaleFactor) {
        NumberFormat formatter = new DecimalFormat("0.##");
        for (int i = 0; i < wedges.length; i++) {
            double fraction = wedges[i].fraction * scaleFactor;
            String percentString = formatter.format(100.0 * fraction);
            String sizeLabel = percentString + "%";
            Extent sizeLabelExtent = g.stringExtent(labelStyle, sizeLabel);
            g.drawString(labelStyle.withContrastingColorForText(appearanceRegistry.getDataColor(i)), sizeLabel, wedges[i].midInner.minus(sizeLabelExtent.half()));
        }
    }
}
