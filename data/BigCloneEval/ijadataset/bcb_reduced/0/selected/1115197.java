package uk.ac.ebi.intact.webapp.search.advancedSearch.powerSearch.business.graphdraw.graph;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

/**
 * LayoutNode with methods to render as a rectangle containing text and obtain an HTML image map.
 *
 * @author EGO
 * @version $Id: RectangularNode.java 6452 2006-10-16 16:09:42Z baranda $
 * @since 27.04.2005
 */
public class RectangularNode implements LayoutNode {

    int width;

    int height;

    String altText, url, target, text;

    Color fill, line;

    Stroke border;

    int x;

    int y;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Draw node
     */
    public void render(Graphics2D g2) {
        int yp;
        int xp;
        int internalYMargin = 2;
        int internalXMargin = 10;
        final Font nameFont = new Font("Arial", 0, 9);
        int left = x - width / 2;
        int right = x + width / 2;
        int top = y - height / 2;
        int bottom = y + height / 2;
        int centre = (left + right) / 2;
        Font accessionFont = new Font("Arial", 0, 8);
        FontRenderContext frc = new FontRenderContext(null, true, false);
        g2.setFont(accessionFont);
        g2.setColor(fill);
        g2.fillRect(x - width / 2, y - height / 2, width, height);
        g2.setColor(line);
        g2.setStroke(border);
        g2.drawRect(x - width / 2, y - height / 2, width, height);
        Rectangle2D r = accessionFont.getStringBounds(text, frc);
        LineMetrics lm = accessionFont.getLineMetrics("X", frc);
        g2.setColor(Color.BLACK);
        yp = top;
        g2.setColor(Color.blue);
        yp += internalYMargin;
        yp += lm.getAscent();
        g2.setFont(nameFont);
        yp += lm.getDescent() + lm.getLeading();
        g2.setColor(Color.black);
        Map attrs = new HashMap();
        attrs.put(TextAttribute.FONT, nameFont);
        LineBreakMeasurer measurer = new LineBreakMeasurer(new AttributedString(text, attrs).getIterator(), frc);
        float wrappingWidth = right - left - internalXMargin + 8;
        xp = centre;
        int limit = 4, count = 0;
        while (measurer.getPosition() < text.length()) {
            count++;
            TextLayout layout = measurer.nextLayout(wrappingWidth);
            yp += (layout.getAscent());
            layout.draw(g2, xp - layout.getAdvance() / 2, yp - 6);
            yp += layout.getDescent() + layout.getLeading();
        }
    }

    /**
     * Called by the layout algorithm
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public RectangularNode(int width, int height, String altText, String url, String target, String text, Color fill, Color line, Stroke border) {
        this.width = width;
        this.height = height;
        this.altText = altText;
        this.url = url;
        this.target = target;
        this.text = text;
        this.fill = fill;
        this.line = line;
        this.border = border;
    }

    /**
     * Get HTML image map
     */
    public String getImageMap() {
        int left = x - width / 2;
        int right = x + width / 2;
        int top = y - height / 2;
        int bottom = y + height / 2;
        return "<area alt=\"" + altText + "\" " + ((target != null) ? ("target=\"" + target + "\" ") : "") + "title=\"" + altText + "\" shape=\"Rect\" href=\"" + url + "\" coords=\"" + left + "," + top + " " + right + "," + bottom + "\"/>";
    }
}
