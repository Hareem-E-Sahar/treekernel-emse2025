package ch.tarnet.pigment.view;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import ch.tarnet.pigment.model.PigmentData;
import ch.tarnet.pigment.model.PigmentDataListener;

public class ZoomView extends JPanel implements PigmentDataListener {

    public enum OverlayMode {

        NONE, GRID, SQUARE, CROSS
    }

    ;

    protected PigmentData data;

    private Robot picker;

    private OverlayMode overlay = OverlayMode.NONE;

    private int zoomRatio = 4;

    public ZoomView(PigmentData data) {
        this.data = data;
        setBorder(new ThinHetchBorder());
        try {
            picker = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        data.addPigmentChangeListener(this);
    }

    public Dimension getPreferredSize() {
        Dimension size = getSize();
        Dimension pref = new Dimension();
        pref.width = Math.max((size.width / zoomRatio) * zoomRatio, getMinimumSize().width);
        pref.height = Math.max((size.height / zoomRatio) * zoomRatio, getMinimumSize().height);
        return pref;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension min = new Dimension(32, 32);
        return min;
    }

    @Override
    public void paintComponent(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        g.setColor(Color.gray);
        g.fillRect(0, 0, getWidth(), getHeight());
        int col = getWidth() / zoomRatio;
        int row = getHeight() / zoomRatio;
        Point mousePos = data.getMousePosition();
        BufferedImage zone = picker.createScreenCapture(new Rectangle(mousePos.x - col / 2, mousePos.y - row / 2, col, row));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(zone, 0, 0, col * zoomRatio, row * zoomRatio, 0, 0, col, row, this);
        g.setColor(Color.black);
        g.drawLine(col * zoomRatio / 2, row * zoomRatio / 2 - 3, col * zoomRatio / 2, row * zoomRatio / 2 + 3);
        g.drawLine(col * zoomRatio / 2 - 3, row * zoomRatio / 2, col * zoomRatio / 2 + 3, row * zoomRatio / 2);
    }

    public void paintComponentOld(Graphics gg) {
        Graphics2D g = (Graphics2D) gg;
        g.setColor(Color.gray);
        g.fillRect(0, 0, getWidth(), getHeight());
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int cellDim = panelWidth < panelHeight ? panelWidth / zoomRatio : panelHeight / zoomRatio;
        if ((cellDim & 1) == 0) cellDim--;
        int gridDim = cellDim * zoomRatio;
        Point mousePos = data.getMousePosition();
        BufferedImage zone = picker.createScreenCapture(new Rectangle(mousePos.x - zoomRatio / 2, mousePos.y - zoomRatio / 2, zoomRatio, zoomRatio));
        AffineTransform transform = g.getTransform();
        g.translate((getWidth() - gridDim) / 2, (getHeight() - gridDim) / 2);
        g.drawImage(zone, 0, 0, gridDim, gridDim, 0, 0, zoomRatio, zoomRatio, this);
        g.setColor(Color.black);
        g.drawLine(gridDim / 2, gridDim / 2 - 3, gridDim / 2, gridDim / 2 + 3);
        g.drawLine(gridDim / 2 - 3, gridDim / 2, gridDim / 2 + 3, gridDim / 2);
        g.setColor(Color.black);
        switch(overlay) {
            case GRID:
                for (int x = 0; x < zoomRatio + 1; x++) {
                    g.drawLine(x * cellDim, 0, x * cellDim, gridDim);
                }
                for (int y = 0; y < zoomRatio + 1; y++) {
                    g.drawLine(0, y * cellDim, gridDim, y * cellDim);
                }
                break;
            case SQUARE:
                for (int x = 0; x < zoomRatio; x++) {
                    for (int y = 0; y < zoomRatio; y++) {
                        g.drawRect(x * cellDim + 2, y * cellDim + 2, cellDim - 5, cellDim - 5);
                    }
                }
                break;
            case CROSS:
                g.drawLine(0, 0, 2, 0);
                g.drawLine(0, 0, 0, 2);
                g.drawLine(gridDim, 0, gridDim - 2, 0);
                g.drawLine(gridDim, 0, gridDim, 2);
                g.drawLine(gridDim, gridDim, gridDim - 2, gridDim);
                g.drawLine(gridDim, gridDim, gridDim, gridDim - 2);
                g.drawLine(0, gridDim, 2, gridDim);
                g.drawLine(0, gridDim, 0, gridDim - 2);
                for (int x = 1; x < zoomRatio; x++) {
                    g.drawLine(x * cellDim, 0, x * cellDim, 2);
                    g.drawLine(x * cellDim - 2, 0, x * cellDim + 2, 0);
                    g.drawLine(x * cellDim, gridDim, x * cellDim, gridDim - 2);
                    g.drawLine(x * cellDim - 2, gridDim, x * cellDim + 2, gridDim);
                    for (int y = 1; y < zoomRatio; y++) {
                        g.drawLine(x * cellDim, y * cellDim - 2, x * cellDim, y * cellDim + 2);
                        g.drawLine(x * cellDim - 2, y * cellDim, x * cellDim + 2, y * cellDim);
                    }
                }
                for (int y = 1; y < zoomRatio; y++) {
                    g.drawLine(0, y * cellDim - 2, 0, y * cellDim + 2);
                    g.drawLine(0, y * cellDim, 2, y * cellDim);
                    g.drawLine(gridDim, y * cellDim - 2, gridDim, y * cellDim + 2);
                    g.drawLine(gridDim, y * cellDim, gridDim - 2, y * cellDim);
                }
            case NONE:
            default:
        }
        g.setColor(Color.black);
        g.drawRect(0, 0, gridDim, gridDim);
        g.setTransform(transform);
    }

    @Override
    public void pigmentChanged() {
        repaint();
    }
}
