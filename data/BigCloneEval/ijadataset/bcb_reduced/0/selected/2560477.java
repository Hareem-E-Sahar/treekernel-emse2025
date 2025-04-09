package org.chartsy.main.components;

import java.awt.AWTEventMulticaster;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.Serializable;
import javax.swing.border.Border;
import org.chartsy.main.util.VersionUtil;

/**
 *
 * @author Viorel
 */
public class ImageButton extends Canvas implements Serializable {

    private static final long serialVersionUID = VersionUtil.APPVERSION;

    protected ActionListener listener = null;

    private int w = 0, h = 0;

    private Insets insets = new Insets(0, 0, 0, 0);

    private boolean clicked = false, down = false, enabled = true;

    ;

    private Image upImage, downImage, disabledImage;

    public ImageButton(Image upImage) {
        this(upImage, null);
    }

    public ImageButton(Image upImage, Image downImage) {
        this(upImage, downImage, null);
    }

    public ImageButton(Image upImage, Image downImage, Image disabledImage) {
        this.upImage = upImage;
        this.downImage = downImage != null ? downImage : upImage;
        this.disabledImage = createImage(new FilteredImageSource(upImage.getSource(), new ImageButtonDisableFilter()));
        if (upImage != null) {
            this.w = upImage.getWidth(this);
            this.h = upImage.getHeight(this);
        }
        setSize(w, h);
        addMouseListener(new ImageButtonMouseListener());
        addMouseMotionListener(new ImageButtonMouseMotionListener());
    }

    public void setBorder(Border border) {
        if (insets != null) {
            w -= (insets.left + insets.right);
            h -= (insets.top + insets.bottom);
        }
        insets = border.getBorderInsets(this);
        w += (insets.left + insets.right);
        h += (insets.top + insets.bottom);
    }

    public void paint(Graphics g) {
        if (down) {
            int iw = downImage.getWidth(this);
            int ih = downImage.getHeight(this);
            int x = (w + iw) / 2;
            int y = (h + ih) / 2;
            g.drawImage(downImage, x, y, this);
        } else {
            if (enabled) {
                int iw = upImage.getWidth(this);
                int ih = upImage.getHeight(this);
                int x = (w + iw) / 2;
                int y = (h + ih) / 2;
                g.drawImage(upImage, x, y, this);
            } else {
                int iw = disabledImage.getWidth(this);
                int ih = disabledImage.getHeight(this);
                int x = (w + iw) / 2;
                int y = (h + ih) / 2;
                g.drawImage(disabledImage, x, y, this);
            }
        }
    }

    public void setEnabled(boolean b) {
        enabled = b;
        repaint();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void addActionListener(ActionListener listener) {
        this.listener = AWTEventMulticaster.add(this.listener, listener);
    }

    public void removeActionListener(ActionListener listener) {
        this.listener = AWTEventMulticaster.remove(this.listener, listener);
    }

    public class ImageButtonMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0) && (enabled == true)) {
                clicked = true;
                down = true;
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            Point p = e.getPoint();
            if (down) {
                down = false;
                repaint();
            }
            if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0) && (clicked == true)) {
                ActionEvent event = new ActionEvent(e.getComponent(), 0, "click");
                if (listener != null) {
                    listener.actionPerformed(event);
                }
            }
            clicked = false;
        }

        public void mouseEntered(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public class ImageButtonMouseMotionListener extends MouseMotionAdapter {

        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            if ((p.x < w) && (p.y < h) && (p.x > 0) && (p.y > 0) && (clicked == true)) {
                if (!down) {
                    down = true;
                    repaint();
                }
            } else {
                if (down) {
                    down = false;
                    repaint();
                }
            }
        }
    }

    class ImageButtonDisableFilter extends RGBImageFilter {

        public ImageButtonDisableFilter() {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            return (rgb & ~0xff000000) | 0x80000000;
        }
    }
}
