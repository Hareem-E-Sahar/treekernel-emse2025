package org.openscience.jmol.render;

import org.openscience.jmol.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.RenderingHints;
import java.util.Enumeration;

public class AtomRenderer {

    DisplayControl control;

    public AtomRenderer(DisplayControl control) {
        this.control = control;
    }

    Graphics g;

    Rectangle clip;

    public void setGraphicsContext(Graphics g, Rectangle clip) {
        this.g = g;
        this.clip = clip;
        fastRendering = control.getFastRendering();
        useGraphics2D = control.getUseGraphics2D();
        colorSelection = control.getColorSelection();
    }

    boolean fastRendering;

    boolean useGraphics2D;

    Color colorSelection;

    ShadedSphereRenderer shadedSphereRenderer;

    Atom atom;

    int x;

    int y;

    int z;

    int diameter;

    byte styleAtom;

    Color color;

    Color colorOutline;

    int radius;

    int xUpperLeft;

    int yUpperLeft;

    public void render(AtomShape atomShape) {
        styleAtom = atomShape.styleAtom;
        atom = atomShape.atom;
        x = atomShape.x;
        y = atomShape.y;
        z = atomShape.z;
        diameter = atomShape.diameter;
        radius = (diameter + 1) / 2;
        xUpperLeft = x - radius;
        yUpperLeft = y - radius;
        color = atomShape.colorAtom;
        colorOutline = control.getColorAtomOutline(styleAtom, color);
        if (control.hasSelectionHalo(atom)) renderHalo();
        if (styleAtom != DisplayControl.NONE && styleAtom != DisplayControl.INVISIBLE) renderAtom();
    }

    private void renderHalo() {
        int halowidth = diameter / 4;
        if (halowidth < 4) halowidth = 4;
        if (halowidth > 10) halowidth = 10;
        int halodiameter = diameter + 2 * halowidth;
        int haloradius = (halodiameter + 1) / 2;
        g.setColor(colorSelection);
        g.fillOval(x - haloradius, y - haloradius, halodiameter, halodiameter);
    }

    private void renderAtom() {
        if (diameter <= 2) {
            if (diameter > 0) {
                g.setColor(styleAtom == DisplayControl.WIREFRAME ? color : colorOutline);
                g.fillRect(xUpperLeft, yUpperLeft, diameter, diameter);
            }
            return;
        }
        if (styleAtom == DisplayControl.SHADING && !fastRendering) {
            if (shadedSphereRenderer == null) shadedSphereRenderer = new ShadedSphereRenderer(control);
            shadedSphereRenderer.render(g, xUpperLeft, yUpperLeft, diameter, color, colorOutline);
            return;
        }
        int diamT = diameter - 1;
        g.setColor(color);
        if (!fastRendering && styleAtom != DisplayControl.WIREFRAME) {
            g.fillOval(xUpperLeft, yUpperLeft, diamT, diamT);
            g.setColor(colorOutline);
        }
        g.drawOval(xUpperLeft, yUpperLeft, diamT, diamT);
    }
}
