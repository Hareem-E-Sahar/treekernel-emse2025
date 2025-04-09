package net.sourceforge.mandalajar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.JComponent;

/**
 * @author  Lukas C. Faulstich
 */
public class MandalaView extends JComponent implements Printable, ActionListener {

    protected static final double SQRT2 = Math.sqrt(2.0d);

    protected static final double PI2 = 2 * Math.PI;

    protected MandalaModel model;

    protected Map<String, Image> imageMap = new HashMap<String, Image>();

    public MandalaView() {
        setBackground(Color.LIGHT_GRAY);
    }

    @Override
    public void paintComponent(Graphics g) {
        final Dimension size = getSize();
        final int width = size.width;
        final int height = size.height;
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);
        final int xCenter = width / 2;
        final int yCenter = height / 2;
        final int maxRadius = (int) Math.round(Math.min(width, height) / 2);
        paintModel(g, xCenter, yCenter, maxRadius, 0.0f);
    }

    protected void paintModel(Graphics g, final int xCenter, final int yCenter, final int maxRadius, float angle) {
        angle += model.getAngle();
        int radiusInnerRim;
        if (!model.isTransparent()) {
            MandalaComponent last = model.getLast();
            final int radius = (last == null) ? maxRadius : (int) (last.getRadius() * maxRadius);
            final int diameter = 2 * radius;
            g.setColor(Color.WHITE);
            g.fillOval(xCenter - radius, yCenter - radius, diameter, diameter);
        }
        g.setColor(Color.black);
        radiusInnerRim = 0;
        for (MandalaComponent component = model.getFirst(); component != null; component = component.getNext()) {
            final int radiusOuterRim = (int) Math.round(component.getRadius() * maxRadius);
            paintComponent(component, g, xCenter, yCenter, radiusOuterRim, radiusInnerRim, angle);
            radiusInnerRim = radiusOuterRim;
        }
    }

    protected void paintComponent(MandalaComponent component, Graphics g, final int xCenter, final int yCenter, final int radiusOuterRim, final int radiusInnerRim, final float angle) {
        if (radiusOuterRim > radiusInnerRim) {
            paintForms(component, g, xCenter, yCenter, radiusOuterRim, radiusInnerRim, angle);
        }
    }

    protected void paintForms(MandalaComponent component, Graphics g, final int xCenter, final int yCenter, int radiusOuterRim, final int radiusInnerRim, final float angle) {
        int symmetry = component.getSymmetry();
        float phase = component.getPhase();
        float twist = component.getTwist();
        if (component.isImage()) {
            String imageFile = component.getImageFile();
            if (imageFile != null) {
                Image image = imageMap.get(imageFile);
                if (image == null) {
                    image = getToolkit().createImage(imageFile);
                    imageMap.put(imageFile, image);
                }
                paintImages(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, angle, image);
            }
        }
        if (component.isCircle()) {
            paintCircle(g, xCenter, yCenter, radiusOuterRim);
        }
        if (component.isWheel()) {
            paintWheel(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, twist, angle);
        }
        if (component.isStar()) {
            paintStar(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, twist, angle);
        }
        if (component.isWave()) {
            paintWave(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, twist, angle);
        }
        if (component.isRings()) {
            paintRings(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, twist, angle);
        }
        if (component.isOrbits()) {
            paintOrbits(g, xCenter, yCenter, radiusInnerRim, radiusOuterRim, symmetry, phase, twist, angle);
        }
    }

    protected void paintFocus(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim) {
        Color color = g.getColor();
        final int diameterOuterRim = 2 * radiusOuterRim;
        final int xOffsetOuterRim = xCenter - radiusOuterRim;
        final int yOffsetOuterRim = yCenter - radiusOuterRim;
        g.setColor(Color.YELLOW);
        g.fillOval(xOffsetOuterRim, yOffsetOuterRim, diameterOuterRim, diameterOuterRim);
        final int diameterInnerRim = 2 * radiusInnerRim;
        final int xOffsetInnerRim = xCenter - radiusInnerRim;
        final int yOffsetInnerRim = yCenter - radiusInnerRim;
        g.setColor(Color.WHITE);
        g.fillOval(xOffsetInnerRim, yOffsetInnerRim, diameterInnerRim, diameterInnerRim);
        g.setColor(color);
    }

    protected void paintCircle(Graphics g, final int xCenter, final int yCenter, final int radiusOuterRim) {
        final int diameterOuterRim = 2 * radiusOuterRim;
        final int xOffset = xCenter - radiusOuterRim;
        final int yOffset = yCenter - radiusOuterRim;
        g.drawOval(xOffset, yOffset, diameterOuterRim, diameterOuterRim);
    }

    protected void paintOrbits(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim, final int symmetry, final float phase, final float twist, final float angle) {
        final int radiusMiddle = (radiusOuterRim - radiusInnerRim) / 2;
        final int diameterRing = radiusOuterRim + radiusInnerRim;
        final int radiusRing = diameterRing / 2;
        for (double i = 0; i < symmetry; i++) {
            final double phi = 2 * Math.PI * ((i + phase) / symmetry + angle);
            final int x0 = xCenter + (int) (radiusMiddle * Math.sin(phi)) - radiusRing;
            final int y0 = yCenter - (int) (radiusMiddle * Math.cos(phi)) - radiusRing;
            g.drawOval(x0, y0, diameterRing, diameterRing);
        }
    }

    protected void paintRings(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim, final int symmetry, final float phase, final float twist, final float angle) {
        final int radiusMiddle = (radiusInnerRim + radiusOuterRim) / 2;
        final int diameterRing = radiusOuterRim - radiusInnerRim;
        final int radiusRing = diameterRing / 2;
        for (double i = 0; i < symmetry; i++) {
            final double phi = 2 * Math.PI * ((i + phase) / symmetry + angle);
            final int x0 = xCenter + (int) (radiusMiddle * Math.sin(phi)) - radiusRing;
            final int y0 = yCenter - (int) (radiusMiddle * Math.cos(phi)) - radiusRing;
            g.drawOval(x0, y0, diameterRing, diameterRing);
        }
    }

    protected void paintWave(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim, final int symmetry, final float phase, final float twist, final float angle) {
        final int steps = 360;
        ;
        int x0 = 0;
        int y0 = 0;
        for (double i = 0; i <= steps; i++) {
            final double phi = 2 * Math.PI * (i / steps + angle);
            final double rho = 2 * Math.PI * (i / steps - twist / symmetry + angle);
            final double psi = 2 * Math.PI * (symmetry * i / steps - phase);
            final double xInner = xCenter + radiusInnerRim * Math.sin(rho);
            final double yInner = yCenter - radiusInnerRim * Math.cos(rho);
            final double xOuter = xCenter + radiusOuterRim * Math.sin(phi);
            final double yOuter = yCenter - radiusOuterRim * Math.cos(phi);
            final double alpha = (1.0d + Math.cos(psi)) / 2.0d;
            final int x1 = (int) ((1.0d - alpha) * xInner + alpha * xOuter);
            final int y1 = (int) ((1.0d - alpha) * yInner + alpha * yOuter);
            if (i > 0) {
                g.drawLine(x0, y0, x1, y1);
            }
            x0 = x1;
            y0 = y1;
        }
    }

    protected void paintStar(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim, final int symmetry, final float phase, final float twist, final float angle) {
        boolean odd = false;
        int x0 = 0;
        int y0 = 0;
        int steps = 2 * symmetry;
        for (double i = 0; i <= steps; i++) {
            final double phi = 2 * Math.PI * ((i + 2 * phase + (odd ? 2 * twist : 0.0f)) / steps + angle);
            int radius = odd ? radiusInnerRim : radiusOuterRim;
            final int x1 = xCenter + (int) (radius * Math.sin(phi));
            final int y1 = yCenter - (int) (radius * Math.cos(phi));
            if (i > 0) {
                g.drawLine(x0, y0, x1, y1);
            }
            x0 = x1;
            y0 = y1;
            odd = !odd;
        }
    }

    protected void paintWheel(Graphics g, final int xCenter, final int yCenter, final int radiusInnerRim, final int radiusOuterRim, final int symmetry, final float phase, final float twist, final float angle) {
        for (double i = 0; i < symmetry; i++) {
            final double phi1 = 2 * Math.PI * ((i + phase + twist) / symmetry + angle);
            final int x1 = xCenter + (int) (radiusInnerRim * Math.sin(phi1));
            final int y1 = yCenter - (int) (radiusInnerRim * Math.cos(phi1));
            final double phi2 = 2 * Math.PI * ((i + phase) / symmetry + angle);
            final int x2 = xCenter + (int) (radiusOuterRim * Math.sin(phi2));
            final int y2 = yCenter - (int) (radiusOuterRim * Math.cos(phi2));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    protected void paintImages(Graphics g, int xCenter, int yCenter, int radiusInnerRim, int radiusOuterRim, int symmetry, float phase, float angle, Image image) {
        Graphics2D g2 = null;
        AffineTransform t0 = null;
        try {
            g2 = (Graphics2D) g;
            t0 = g2.getTransform();
        } catch (ClassCastException e) {
        }
        if ((symmetry > 1) || (radiusInnerRim > 0)) {
            final int dr = (int) ((radiusOuterRim - radiusInnerRim) / (2 * SQRT2));
            for (double i = 0; i < symmetry; i++) {
                final double phi = PI2 * ((i + phase) / symmetry + angle);
                final int xc = xCenter + (int) (0.5d * (radiusInnerRim + radiusOuterRim) * Math.sin(phi));
                final int yc = yCenter - (int) (0.5d * (radiusInnerRim + radiusOuterRim) * Math.cos(phi));
                if (g2 != null) {
                    g2.rotate(phi, xc, yc);
                }
                g.drawImage(image, xc - dr, yc - dr, 2 * dr, 2 * dr, this);
                if (g2 != null) {
                    g2.setTransform(t0);
                }
            }
        } else {
            final int dr = (int) (radiusOuterRim / SQRT2);
            if (g2 != null) {
                g2.rotate(PI2 * phase, xCenter, yCenter);
            }
            final int xc = xCenter;
            final int yc = yCenter;
            g.drawImage(image, xc - dr, yc - dr, 2 * dr, 2 * dr, this);
            if (g2 != null) {
                g2.setTransform(t0);
            }
        }
    }

    public MandalaModel getModel() {
        return model;
    }

    public void setModel(MandalaModel model) {
        this.model = model;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("print".equals(command)) {
            print();
        } else {
            System.err.println("Unknown action " + command);
        }
    }

    public void print() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
        boolean print = false;
        attr_set.add(MediaSizeName.ISO_A4);
        print = pj.printDialog(attr_set);
        if (print) {
            try {
                pj.print(attr_set);
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    public int print(Graphics graphics, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        final Dimension size = getSize();
        final int width = size.width;
        final int height = size.height;
        final int xOffset = 18;
        final int yOffset = 72;
        final float safetyScalingFactor = 0.9f;
        float scalingFactor = (float) Math.min(pf.getImageableWidth() - xOffset, pf.getImageableHeight() - yOffset) / Math.min(width, height) * safetyScalingFactor;
        g2d.translate(pf.getImageableX() + xOffset, pf.getImageableY() + yOffset);
        g2d.scale(scalingFactor, scalingFactor);
        Color background = getBackground();
        setBackground(Color.WHITE);
        print(graphics);
        setBackground(background);
        return PAGE_EXISTS;
    }

    public double pointsToCm(double points) {
        return 2.54 / 72 * points;
    }
}
