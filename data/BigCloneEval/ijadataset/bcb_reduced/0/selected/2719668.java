package tilemaster.editor.paintingtools;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

/**
 * An example painting tool plugin which draws diamond shapes.
 *
 * @author Hj. Malthaner
 */
public class DiamondTool extends PaintingToolBase {

    private int firstX, firstY;

    /**
     * @return The redraw mode to use for this tool.
     */
    public int getRefreshMode() {
        return REPAINT_REFRESH;
    }

    /**
     * @return The name to display for this tool.
     */
    public String getToolName() {
        return "Diamond";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    public void setCanvas(BufferedImage image) {
    }

    /**
     * User clicked a new location
     */
    public void firstClick(Graphics gr, int x, int y) {
        firstX = x;
        firstY = y;
        gr.fillRect(firstX, firstY, 1, 1);
    }

    /**
     * User drags mouse.
     *
     * @param gr Graphics context to use for drawing
     * @param x X coordinate
     * @param y Y coordinate
     * @param filled Paint filled shapes. Ignored by this tool.
     */
    public void paint(Graphics gr, int x, int y, boolean filled) {
        if (x != firstX || y != firstY) {
            int x1 = Math.min(x, firstX);
            int x2 = Math.max(x, firstX);
            int y1 = Math.min(y, firstY);
            int y2 = Math.max(y, firstY);
            int w2 = (x1 + x2) / 2;
            int h2 = (y1 + y2) / 2;
            gr.drawLine(x1, h2, w2, y1);
            gr.drawLine(w2 + 1, y1, x2, h2);
            gr.drawLine(x1, h2, w2, y2);
            gr.drawLine(w2 + 1, y2, x2, h2);
        }
    }

    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The color's index in the color map.
     */
    public void onColorSelected(int colorIndex) {
    }

    /** Creates a new instance of DiamondTool */
    public DiamondTool() {
    }
}
