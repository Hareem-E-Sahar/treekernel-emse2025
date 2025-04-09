package org.jcvi.glk.elvira.report.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import org.jcvi.glk.elvira.report.AmpliconSuccess;
import org.jcvi.glk.elvira.report.CoordinatedSuccess;

public class SuccessGlyphFactory {

    private static final int TILING_THICKNESS = 8;

    private static final int DIRECTION_ARROW_LENGTH = 8;

    private static final int DASH_LINE_LENGTH = 4;

    private static final int DASH_LINE_SPACING = 4;

    private static final int DASH_WIDTH = DASH_LINE_LENGTH + DASH_LINE_SPACING;

    public static void drawCoordinatedSuccessGlyph(Graphics2D g, SuccessCoverageReportController controller, CoordinatedSuccess success, final int x, int y, final int width, final int height) {
        if (success instanceof AmpliconSuccess) {
            drawAmpliconSuccessGlyph(g, controller, (AmpliconSuccess) success, x, y, width, height);
        } else {
            drawDefaultCoordinatedSuccessGlyph(g, controller, success, x, y, width, height);
        }
    }

    /**
     * Draw a simple bar covering the coordinates specified.
     * @param g
     * @param controller the {@link SuccessCoverageReportController} which determines
     * the colors used to draw the object.
     * @param success the {@link CoordinatedSuccess}.
     * @param x the top x coordinate of the box.
     * @param y the top y coordinate of the box.
     * @param width width of box.
     * @param height height of box.
     */
    private static void drawDefaultCoordinatedSuccessGlyph(Graphics2D g, SuccessCoverageReportController controller, CoordinatedSuccess success, final int x, int y, final int width, final int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        final Color color = controller.computeColorFor(success);
        g2d.setColor(color);
        final int midY = y + (height - TILING_THICKNESS) / 2;
        g2d.fillRect(x, midY, width, TILING_THICKNESS);
        g2d.drawLine(x, y, x, y + height);
        g2d.drawLine(x + width, y, x + width, y + height);
        g2d.dispose();
    }

    /**
     * Draws the AmpliconSuccess specified as a 2 directed arrows (one for forward, one for reverse).
     * @param g
     * @param controller
     * @param ampliconSuccess
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private static void drawAmpliconSuccessGlyph(Graphics2D g, SuccessCoverageReportController controller, AmpliconSuccess ampliconSuccess, final int x, int y, final int width, final int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        final Color forwardColor = controller.computeColorFor(ampliconSuccess.getForwardSuccess());
        final Color reverseColor = controller.computeColorFor(ampliconSuccess.getReverseSuccess());
        final int arrowHeight = height / 2;
        final int coverageBarHeight = height / 4;
        final int dashedLineHeight = height / 8;
        int forwardOffset = ampliconSuccess.getAmplicons().get(0).getForwardCoverageStart();
        int forwardlength = ampliconSuccess.getAmplicons().get(0).getForwardCoverageEnd() - forwardOffset;
        int reverseOffset = ampliconSuccess.getAmplicons().get(0).getReverseCoverageStart();
        int reverselength = ampliconSuccess.getAmplicons().get(0).getReverseCoverageEnd() - reverseOffset;
        g2d.setColor(forwardColor);
        drawAmpliconSuccessCoverageArrow(g2d, x, y, width, arrowHeight, forwardOffset, forwardlength, coverageBarHeight, dashedLineHeight);
        g2d.setColor(reverseColor);
        drawReverseAmpliconSuccessCoverageArrow(g2d, x, y + arrowHeight, width, arrowHeight, reverseOffset, reverselength, coverageBarHeight, dashedLineHeight);
        g2d.dispose();
    }

    private static void drawReverseAmpliconSuccessCoverageArrow(Graphics2D g, int x, int y, int width, final int arrowHeight, final int coverageOffset, final int coverageLength, final int coverageBarHeight, final int dashedLineHeight) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.rotate(Math.toRadians(180), x + (width / 2d), y + (arrowHeight / 2d));
        drawAmpliconSuccessCoverageArrow(g2d, x, y, width, arrowHeight, coverageOffset, coverageLength, coverageBarHeight, dashedLineHeight);
        g2d.dispose();
    }

    private static void drawAmpliconSuccessCoverageArrow(Graphics2D g2d, int x, int y, int width, final int height, final int coverageOffset, final int coverageLength, final int coverageBarHeight, final int dashedLineHeight) {
        final int lengthTilArrow = coverageLength - DIRECTION_ARROW_LENGTH;
        final int beginCoverage = x + coverageOffset;
        final int topOfArrow = y + coverageBarHeight;
        final int bottomOfArrow = y + height;
        final int endCoverage = beginCoverage + coverageLength;
        final int topOfCoverageBar = y + height / 4;
        final int totalLength = x + width;
        drawCoverageArrow(y, g2d, beginCoverage, endCoverage, lengthTilArrow, topOfCoverageBar, coverageBarHeight, topOfArrow, bottomOfArrow);
        drawUncoveredAreas(x, y, g2d, dashedLineHeight, beginCoverage, topOfArrow, bottomOfArrow, endCoverage, totalLength);
    }

    private static void drawUncoveredAreas(int x, int y, Graphics2D g2d, final int dashedLineHeight, final int beginCoverage, final int topOfArrow, final int bottomOfArrow, final int endCoverage, final int totalLength) {
        int height = y + (bottomOfArrow - topOfArrow - dashedLineHeight);
        drawDashesAfterCoverage(g2d, dashedLineHeight, height, endCoverage, totalLength);
        drawDashesBeforeCoverage(x, g2d, dashedLineHeight, beginCoverage, height);
        g2d.drawLine(x, y, x, bottomOfArrow);
        g2d.drawLine(totalLength, y, totalLength, bottomOfArrow);
    }

    private static void drawDashesBeforeCoverage(int x, Graphics2D g2d, final int dashedLineHeight, final int beginCoverage, final int topOfDash) {
        for (int i = x; i < beginCoverage; i += DASH_WIDTH) {
            g2d.fillRect(i, topOfDash, DASH_LINE_LENGTH, dashedLineHeight);
        }
    }

    private static void drawDashesAfterCoverage(Graphics2D g2d, final int dashedLineHeight, final int topOfDash, final int endCoverage, final int totalLength) {
        for (int i = totalLength; i > endCoverage - DASH_WIDTH; i -= DASH_WIDTH) {
            g2d.fillRect(i - DASH_WIDTH, topOfDash, DASH_LINE_LENGTH, dashedLineHeight);
        }
    }

    private static void drawCoverageArrow(int y, Graphics2D g2d, final int beginCoverage, final int endCoverage, final int lengthTilArrow, final int topOfCoverageBar, final int coverageBarHeight, final int topOfArrow, final int bottomOfArrow) {
        final int arrowStart = beginCoverage + lengthTilArrow;
        drawArrow(y, g2d, topOfArrow, bottomOfArrow, endCoverage, arrowStart);
        drawCoverageBar(y, g2d, beginCoverage, endCoverage, lengthTilArrow, topOfCoverageBar, coverageBarHeight, bottomOfArrow);
    }

    private static void drawCoverageBar(int y, Graphics2D g2d, final int beginCoverage, final int endCoverage, final int lengthTilArrow, final int topOfCoverageBar, final int coverageBarHeight, final int bottomOfArrow) {
        g2d.fillRect(beginCoverage, topOfCoverageBar, lengthTilArrow, coverageBarHeight);
        g2d.drawLine(beginCoverage, y, beginCoverage, bottomOfArrow);
        g2d.drawLine(endCoverage, y, endCoverage, bottomOfArrow);
    }

    private static void drawArrow(int y, Graphics2D g2d, final int topOfArrow, final int bottomOfArrow, final int endCoverage, final int arrowStart) {
        g2d.fill(buildArrow(y, topOfArrow, bottomOfArrow, endCoverage, arrowStart));
    }

    private static Shape buildArrow(int y, final int topOfArrow, final int bottomOfArrow, final int endCoverage, final int arrowStart) {
        return new Polygon(new int[] { endCoverage, arrowStart, arrowStart }, new int[] { topOfArrow, y, bottomOfArrow }, 3);
    }
}
