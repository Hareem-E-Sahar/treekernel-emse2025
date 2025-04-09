package org.skycastle.scratchpad.sketch.sliders;

import org.skycastle.util.ParameterChecker;
import org.skycastle.util.applicationview.SidebarEdge;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Renders specified component of an HSBA color.
 *
 * @author Hans Haggstrom
 */
public final class ColorSliderBackgroundRenderer extends AbstractSliderBackgroundRenderer {

    private final HSBAColorComponent myColorComponent;

    private Color myColor = new Color(128, 128, 128);

    private static final Color CHECKERS_DARK = new Color(153, 153, 153);

    private static final Color CHECKERS_BRIGHT = new Color(204, 204, 204);

    public ColorSliderBackgroundRenderer(final HSBAColorComponent colorComponent) {
        ParameterChecker.checkNotNull(colorComponent, "colorComponent");
        myColorComponent = colorComponent;
    }

    /**
     * @return the edited color.
     */
    public Color getColor() {
        return myColor;
    }

    /**
     * @param aColor the edited color.
     */
    public void setColor(final Color aColor) {
        myColor = aColor;
    }

    protected void drawBackgroundSlice(final Graphics2D g2, final float pos, final int x1, final int y1, final int x2, final int y2, final int absPos, final SidebarEdge edge, final float value) {
        drawTransparentBackgroundGridPattern(g2, x1, y1, x2, y2, absPos);
        g2.setColor(myColorComponent.createColorForValue(myColor, value));
        g2.drawLine(x1, y1, x2, y2);
    }

    private void drawTransparentBackgroundGridPattern(final Graphics2D g2, final int x1, final int y1, final int x2, final int y2, final int absPos) {
        final int xm = (x1 + x2) / 2;
        final int ym = (y1 + y2) / 2;
        Color c1 = CHECKERS_BRIGHT;
        Color c2 = CHECKERS_DARK;
        if (isCheckboardPatternInverted(x1, y1, absPos, xm, ym)) {
            c1 = CHECKERS_DARK;
            c2 = CHECKERS_BRIGHT;
        }
        g2.setColor(c1);
        g2.drawLine(x1, y1, xm, ym);
        g2.setColor(c2);
        g2.drawLine(xm, ym, x2, y2);
    }

    private boolean isCheckboardPatternInverted(final int x1, final int y1, final int absPos, final int xm, final int ym) {
        int dist = Math.max(Math.abs(xm - x1), Math.abs(ym - y1));
        return (absPos / dist) % 2 == 0;
    }
}
