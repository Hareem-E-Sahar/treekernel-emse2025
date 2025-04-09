package org.walles.subsurface;

import javax.microedition.lcdui.Graphics;
import org.walles.subsurface.craft.Submarine;

/**
 * The universe contains all entities in the simulation.
 * 
 * @author johan
 */
public class Universe {

    private Submarine submarine;

    private int visibleCentimeters;

    private int visibleCentimetersX;

    private int visibleCentimetersY;

    private int visiblePixelsX;

    private int visiblePixelsY;

    private Graphics g;

    private int displayWidth;

    private int displayHeight;

    private int displayOrigoHorizontal;

    private int displayOrigoVertical;

    private int leftEdge;

    private int topEdge;

    /**
     * Where should the universe be rendered on the next call to render()?
     * @param g The destination Graphics
     * @param x Left edge of the rendering
     * @param y Top edge of the rendering
     * @param width  Width of the rendering
     * @param height Height of the rendering
     * @see #render()
     */
    public void setDisplayParameters(Graphics g, int x, int y, int width, int height) {
        this.g = g;
        this.leftEdge = x;
        this.topEdge = y;
        this.displayWidth = width;
        this.displayHeight = height;
        this.displayOrigoHorizontal = x + (width + 1) / 2;
        this.displayOrigoVertical = y + (height + 1) / 2;
        recalculateVisibility();
    }

    /**
     * Let there be light.
     */
    public Universe() {
        submarine = new Submarine();
        submarine.init(0, 0, 0, 188);
    }

    /**
     * Update the universe by this many milliseconds.
     * 
     * @param milliseconds The number of milliseconds to update the universe by.
     */
    public void update(int milliseconds) {
        submarine.update(milliseconds);
    }

    /**
     * Render the universe within the current clip rectangle of the given Graphics
     * object.
     */
    public void render() {
        submarine.render(g, universeXtoScreenX(submarine.getX()), universeYtoScreenY(submarine.getY()));
    }

    /**
     * Convert a universe X coordinate to an on-screen X coordinate
     * @param universeX The universe X coordinate.
     * @return The on-screen X coordinate.
     */
    public int universeXtoScreenX(int universeX) {
        int delta = (visiblePixelsX * universeX) / visibleCentimetersX;
        return displayOrigoHorizontal + delta;
    }

    /**
     * Convert a universe Y coordinate to an on-screen Y coordinate
     * @param universeY The universe Y coordinate.
     * @return The on-screen Y coordinate.
     */
    public int universeYtoScreenY(int universeY) {
        int delta = -(visiblePixelsY * universeY) / visibleCentimetersY;
        return displayOrigoHorizontal + delta;
    }

    /**
     * @return The single submarine in this universe
     */
    public Submarine getSubmarine() {
        return submarine;
    }

    /**
     * Convert a screen coordinate to a universe coordinate.
     * 
     * @param screenX The on-screen X coordinate.
     * 
     * @return The universe X coordinate.
     */
    public int screenXtoUniverseX(int screenX) {
        int pixelsEastOfOrigo = screenX - displayOrigoHorizontal;
        int universeX = (pixelsEastOfOrigo * visibleCentimetersX) / visiblePixelsY;
        return universeX;
    }

    /**
     * Convert a screen coordinate to a universe coordinate.
     * 
     * @param screenY The on-screen Y coordinate.
     * 
     * @return The universe Y coordinate.
     */
    public int screenYtoUniverseY(int screenY) {
        int pixelsNorthOfOrigo = displayOrigoVertical - screenY;
        int universeY = (pixelsNorthOfOrigo * visibleCentimetersY) / visiblePixelsX;
        return universeY;
    }

    /**
     * Compute the visibility along the X and the Y axis, based on the screen dimensions
     * and the requested visibility.
     */
    private void recalculateVisibility() {
        if (displayWidth <= 0 || displayHeight <= 0) {
            throw new IllegalStateException("Must set display with and height to > 0");
        }
        if (displayWidth < displayHeight) {
            visibleCentimetersX = visibleCentimeters;
            visibleCentimetersY = (displayHeight * visibleCentimetersX) / displayWidth;
        } else {
            visibleCentimetersY = visibleCentimeters;
            visibleCentimetersX = (displayWidth * visibleCentimetersY) / displayHeight;
        }
        visiblePixelsX = (displayWidth + 1) / 2;
        visiblePixelsY = (displayHeight + 1) / 2;
    }

    /**
     * How many centimeters of visibility there is in every direction when this
     * universe gets rendered.
     * 
     * @param visibleCentimeters How much can be seen in each direction.
     */
    public void setVisibleCentimeters(int visibleCentimeters) {
        this.visibleCentimeters = visibleCentimeters;
        recalculateVisibility();
    }
}
