package uk.co.westhawk.visual;

import java.lang.*;
import java.awt.*;
import javax.swing.*;

/**
 * <p>
 * The LevelDraw class produces a lightweight component in the shape of
 * a Level. It can have a scale, and a label underneath.
 * </p>
 *
 * <p>
 * It is used by the getAllInterfacesUI application to visualize the speed.
 * </p>
 *
 * @see Level
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.3 $ $Date: 2006/01/17 17:43:54 $
 */
public class LevelDraw extends Canvas {

    private static final String version_id = "@(#)$Id: LevelDraw.java,v 1.3 2006/01/17 17:43:54 birgit Exp $ Copyright Westhawk Ltd";

    private boolean doScale = false;

    private Color fg = Color.black;

    private Color bg = Color.lightGray;

    private Color lbg = Color.white;

    private Color lfg = Color.orange;

    private double value;

    private FontMetrics fm;

    private double dmax;

    private Image image = null;

    private int width, height;

    private boolean isChanged;

    static final String xaxis[] = { "0 B/s", "10 B/s", "100 B/s", "1 KB/s", "10 KB/s", "100 KB/s", "1 MB/s" };

    static final int max = 1000000;

    static final int min = 0;

    static final String max_str = "1 MB/s";

    static final String min_str = "0";

    /**
 * Constructs a LevelDraw with no label
 */
    public LevelDraw() {
        value = 0.0;
        dmax = Math.log(max) / Math.log(10);
        width = 0;
        height = 0;
        isChanged = true;
    }

    public void setValue(double v) {
        if (v != value) {
            value = v;
            isChanged = true;
            repaint();
        }
    }

    public double getValue() {
        return (value);
    }

    public void addNotify() {
        super.addNotify();
        Font f = this.getFont();
        fm = this.getFontMetrics(f);
    }

    public void setScale(boolean b) {
        if (b != doScale) {
            doScale = b;
            isChanged = true;
            repaint();
        }
    }

    public void setForeground(Color c) {
        if (c != fg) {
            fg = c;
            isChanged = true;
            repaint();
        }
    }

    public void setBackground(Color c) {
        if (c != bg) {
            bg = c;
            isChanged = true;
            repaint();
        }
    }

    public void setLevelBackground(Color c) {
        if (c != lbg) {
            lbg = c;
            isChanged = true;
            repaint();
        }
    }

    public void setLevelForeground(Color c) {
        if (c != lfg) {
            lfg = c;
            isChanged = true;
            repaint();
        }
    }

    /**
 * Paints the LevelDraw
 */
    public void paint(Graphics g) {
        if (g != null) {
            int w = getSize().width - 1;
            int h = getSize().height - 1;
            if (w > 0 && h > 0) {
                if (image == null || width != w || height != h || isChanged) {
                    width = w;
                    height = h;
                    image = createImage(width, height);
                    drawImage(image, width, height);
                }
                g.drawImage(image, 0, 0, null);
            }
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    private void drawImage(Image image, int w, int h) {
        Graphics g = image.getGraphics();
        g.setColor(bg);
        g.fillRect(0, 0, w, h);
        int fonth = fm.getHeight();
        int scale = (int) ((h - 2 * fonth) / dmax);
        int xl;
        if (doScale) {
            xl = w / 2 - w / 8;
        } else {
            xl = w / 8;
        }
        int xr = w - w / 8;
        int yt = fonth;
        int yb = fonth + (int) (scale * dmax);
        int levelw = xr - xl;
        int levelh = yb - yt;
        g.setColor(lbg);
        g.fillRect(xl, yt, levelw, levelh);
        double dvalue = 0.0;
        if (value != 0.0) {
            dvalue = Math.log(value) / Math.log(10);
        }
        int l = (int) (scale * dvalue);
        g.setColor(lfg);
        g.fillRect(xl + 2, yb - l, levelw - 4, l);
        g.setColor(fg);
        if (doScale) {
            drawScale(g, xl, xr, yt, yb, scale);
        }
        int sw = fm.stringWidth(value + " B/s");
        int p = xl + (levelw - sw) / 2;
        g.drawString(value + " B/s", p, yb - l - 2);
    }

    private void drawScale(Graphics g, int xl, int xr, int yt, int yb, int scale) {
        int sw, p;
        int y = yb;
        int x = xl;
        for (int i = 0; i <= dmax; i++) {
            g.drawLine(x - 2, y, x + 2, y);
            sw = fm.stringWidth(xaxis[i]);
            p = (x - 2) - sw - 2;
            g.drawString(xaxis[i], p, y);
            y -= scale;
        }
        y = yt;
        g.drawLine(x - 2, y, x + 2, y);
        sw = fm.stringWidth(xaxis[6]);
        p = (x - 2) - sw - 2;
        g.drawString(xaxis[6], p, y);
    }

    /**
 * Returns the preferred size of the button. 
 */
    public Dimension getPreferredSize() {
        int w = 20;
        int h = 100;
        if (doScale) {
            w = w * 3;
        }
        return new Dimension(w, h);
    }

    /**
 * Returns the minimum size of the button. 
 */
    public Dimension getMinimumSize() {
        int w = 10;
        int h = 75;
        if (doScale) {
            w = w * 3;
        }
        return new Dimension(w, h);
    }

    public String toString() {
        return getClass().getName() + "[" + ",value=" + value + "]";
    }
}
