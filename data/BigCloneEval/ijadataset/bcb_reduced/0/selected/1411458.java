package org.jjazz.ui.editor.barrenderer;

import org.jjazz.ui.editor.barrenderer.api.BarRenderer;
import java.awt.*;
import org.jjazz.harmony.TimeSignature;
import org.jjazz.leadsheet.item.Position;
import org.jjazz.ui.itemrenderer.api.ItemRenderer;

/**
 * This LayoutManager places ItemRenderers at their corresponding beat position in the bar.
 */
public class BeatBasedLayoutManager implements LayoutManager {

    /**
     * Return the Position that corresponds to the X-coordinate xPos in the BarRenderer.
     * @param xPos int The x position in the BarRenderer coordinates.
     * @return Position
     */
    public Position getPositionFromPoint(BarRenderer br, int xPos) {
        if (!(br instanceof BeatBasedBarRenderer)) {
            throw new IllegalArgumentException("br=" + br);
        }
        TimeSignature ts = ((BeatBasedBarRenderer) br).getTimeSignature();
        Rectangle r = br.getDrawingArea();
        xPos = Math.max(r.x, xPos);
        xPos = Math.min(r.x + r.width - 1, xPos);
        float beat = (xPos - r.x) * (ts.getNbNaturalBeats() / (float) r.width);
        Position pos = new Position(br.getBarIndex(), beat);
        return pos;
    }

    /**
     * Calculate the X position for a beat, in a bar who has width=barWidth.
     *
     * @param beat A float representing the beat position.
     * @param barWidth An integer for the width of the bar.
     *
     * @return An integer representing the X position of pos.
     */
    public int getBeatXPosition(float beat, int barWidth, TimeSignature ts) {
        if (ts == null) {
            ts = TimeSignature.FOUR_FOUR;
        }
        float nbBeats = ts.getNbNaturalBeats();
        float beatLength = barWidth / nbBeats;
        return (int) (beat * beatLength);
    }

    /**
     * Layout all children at their respective beat position.
     * @param parent Container
     */
    @Override
    public void layoutContainer(Container parent) {
        if (!(parent instanceof BarRenderer) || !(parent instanceof BeatBasedBarRenderer)) {
            throw new IllegalArgumentException("parent=" + parent);
        }
        BarRenderer br = (BarRenderer) parent;
        int barWidth = br.getDrawingArea().width;
        int barHeight = br.getDrawingArea().height;
        int barLeft = br.getDrawingArea().x;
        int barTop = br.getDrawingArea().y;
        TimeSignature ts = ((BeatBasedBarRenderer) parent).getTimeSignature();
        for (ItemRenderer ir : br.getItemRenderers()) {
            Position pos = ir.getModel().getPosition();
            int eventWidth = ir.getWidth();
            int eventHeight = ir.getHeight();
            int x = getBeatXPosition(pos.getBeat(), barWidth, ts);
            x += (barLeft - (eventWidth / 2));
            int y = barTop + (barHeight - eventHeight) / 2;
            ir.setLocation(x, y);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
