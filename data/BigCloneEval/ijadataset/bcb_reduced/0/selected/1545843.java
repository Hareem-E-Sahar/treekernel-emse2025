package mipt.gui.graph.primitives;

import java.awt.*;
import mipt.gui.graph.GraphRegion;

/**
 * Zone with text on it. Unlike superclass, by default fills area
 *  (and print text in the center of it) and does not draw line.
 */
public class TextZone extends Zone {

    private Color textColor;

    private String text;

    public TextZone() {
    }

    /**
	 * Zone will be in fill mode! 
	 */
    public TextZone(double max, Color color, String text, Color textColor) {
        this(max, color, true, text, textColor);
    }

    /**
	 * 
	 */
    public TextZone(double max, Color color, boolean fill, String text, Color textColor) {
        super(max, color, fill);
        this.text = text;
        this.textColor = textColor;
    }

    public Zone copy() {
        return new TextZone(max, getColor(), isFill(), text, textColor);
    }

    protected void paintHorizontal(GraphRegion rgn, int y, int height, double max, double min) {
        super.paintHorizontal(rgn, y, height, max, min);
        int textW = rgn.fm.stringWidth(text), textH = rgn.fm.getHeight();
        if (rgn.width > textW + 5 && height > textH + 4) {
            rgn.gr.setColor(textColor);
            double v = (max + min) / 2;
            rgn.gr.drawString(text, rgn.x + (rgn.width - textW) / 2, rgn.y - (int) (rgn.scaleY * (v - rgn.minY)) + textH / 3);
        }
    }

    protected void paintVertical(GraphRegion rgn, int x, int width, double max, double min) {
        super.paintVertical(rgn, x, width, max, min);
        throw new UnsupportedOperationException("Text can't be painted in vertical direction yet");
    }
}
