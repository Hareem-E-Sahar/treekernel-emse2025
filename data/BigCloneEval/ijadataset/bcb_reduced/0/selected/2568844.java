package eu.popeye.ui.laf;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

/*******************************************************************************
 * @class PopeyeResizablePopeyeFrame
 * @extends JFrame
 *
 * @author christian.melchiorre, Paolo Gianrossi
 *
 * This class represent the toplevel ResizablePopeyeFrame window for PopeyeUser interface.
 * The original code is by Christian Melchiorre.
 * It was ported to the JavaBeans architecture by Paolo Gianrossi.
 *
 */
public class ResizablePopeyeFrame extends JFrame implements Serializable {

    private Image transparent;

    private Image scaledBitmap;

    private ResizablePopeyeFrameBackgroundPanel jpl;

    private static Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

    private Robot r;

    private Rectangle rect = new Rectangle(0, 0, d.width, d.height);

    private RszFrameMover FrameMover;

    private PropertyChangeSupport propertySupport;

    public static final String PROP_BITMAP = "bitmap";

    private Image bitmap;

    public ResizablePopeyeFrame() {
        super();
        propertySupport = new PropertyChangeSupport(this);
        setUndecorated(true);
        setXBorderThreshold(10);
        setYBorderThreshold(10);
        try {
            r = new Robot();
            transparent = r.createScreenCapture(rect);
        } catch (AWTException awe) {
            awe.printStackTrace();
            System.out.println("error reading screen");
            System.exit(0);
        }
        jpl = new ResizablePopeyeFrameBackgroundPanel(transparent, scaledBitmap, this);
        jpl.setOpaque(false);
        setContentPane(jpl);
        requestFocus();
        addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent fe) {
                transparent = null;
            }

            public void focusGained(FocusEvent fe) {
            }
        });
        this.FrameMover = new RszFrameMover(this);
        this.addMouseListener(FrameMover);
        this.addMouseMotionListener(FrameMover);
    }

    public Image getBitmap() {
        return bitmap;
    }

    public void setBitmap(Image value) {
        Image oldValue = bitmap;
        bitmap = value;
        scaledBitmap = bitmap;
        this.jpl.setSkin(scaledBitmap);
        this.setSize(scaledBitmap.getWidth(this), scaledBitmap.getHeight(this));
        propertySupport.firePropertyChange(PROP_BITMAP, oldValue, bitmap);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public int getShadowSize() {
        return this.jpl.shadowSize;
    }

    public void setShadowSize(int s) {
        this.jpl.shadowSize = s;
    }

    public void moveBck(Point to) {
        this.setVisible(false);
        transparent = r.createScreenCapture(new Rectangle(to.x, to.y, this.getWidth(), this.getHeight()));
        this.setVisible(true);
    }

    /**
     * Holds value of property xBorderThreshold.
     */
    private int xBorderThreshold;

    /**
     * Getter for property xBorderThreshold.
     * @return Value of property xBorderThreshold.
     */
    public int getXBorderThreshold() {
        return this.xBorderThreshold;
    }

    /**
     * Setter for property xBorderThreshold.
     * @param xBorderThreshold New value of property xBorderThreshold.
     */
    public void setXBorderThreshold(int xBorderThreshold) {
        this.xBorderThreshold = xBorderThreshold;
    }

    /**
     * Holds value of property yBorderThreshold.
     */
    private int yBorderThreshold;

    /**
     * Getter for property yBorderThreshold.
     * @return Value of property yBorderThreshold.
     */
    public int getYBorderThreshold() {
        return this.yBorderThreshold;
    }

    /**
     * Setter for property yBorderThreshold.
     * @param yBorderThreshold New value of property yBorderThreshold.
     */
    public void setYBorderThreshold(int yBorderThreshold) {
        this.yBorderThreshold = yBorderThreshold;
    }
}

/*******************************************************************************
 *
 * @class ResizablePopeyeFrameBackgroundPanel
 *
 * This class contains the background panel management for irregular frame shape
 * handling through transparency.
 *
 */
class ResizablePopeyeFrameBackgroundPanel extends JPanel {

    Image image1, image2;

    Window parent;

    private static final Color popeyeDarkGreen = new Color(0x7bbd42, false);

    private static final Color popeyeMediumGreen = new Color(0x8cce5a, false);

    private static final Color popeyeLightGreen = new Color(0xb8e691, false);

    public int shadowSize = 3;

    public ResizablePopeyeFrameBackgroundPanel(Image _image1, Image _image2, Window _parent) {
        this.image1 = _image1;
        this.image2 = _image2;
        this.parent = _parent;
    }

    public void paintComponent(Graphics g) {
        g.drawImage(this.image1, 0, 0, getWidth(), getHeight(), this.parent.getX(), this.parent.getY(), this.parent.getX() + getWidth(), this.parent.getY() + getHeight(), this);
        Graphics2D g2d = (Graphics2D) g;
        Insets vInsets = this.getInsets();
        int w = this.getWidth() - (vInsets.left + vInsets.right);
        int h = this.getHeight() - (vInsets.top + vInsets.bottom);
        int x = vInsets.left;
        int y = vInsets.top;
        int arc = 16;
        Shape vButtonShape = new RoundRectangle2D.Double((double) x, (double) y + 2, (double) w, (double) h - 2, (double) arc, (double) arc);
        Shape vOldClip = g.getClip();
        Color shade = new Color(0x9e303030, true);
        g2d.setColor(shade);
        g2d.fillRoundRect(x + shadowSize, y + shadowSize, w - 4, h - 4, arc, arc);
        GradientPaint vPaint = new GradientPaint(x, y, this.popeyeMediumGreen, x + w / 2, y + h / 2, Color.WHITE);
        g2d.setPaint(vPaint);
        g2d.fillRoundRect(x, y, w - 4, h - 4, arc, arc);
        vPaint = new GradientPaint(x, y, Color.BLACK, x + w, y + h, popeyeDarkGreen);
        g2d.setPaint(vPaint);
        g2d.drawRoundRect(x, y, w - 4, h - 4, arc, arc);
        g2d.setClip(vOldClip);
        g2d.setColor(new Color(0x330000ff, true));
    }

    public void setImg(Image i) {
        this.image1 = i;
        repaint();
    }

    public void setSkin(Image img) {
        this.image2 = img;
        this.repaint();
    }
}

;

/*******************************************************************************
 *
 * @class FrameCloser
 *
 * WindowClosing event handler. Closes the window and terminate the application.
 *
 */
class RszFrameCloser extends WindowAdapter {

    ResizablePopeyeFrame parent = null;

    public RszFrameCloser(ResizablePopeyeFrame _ift) {
        this.parent = _ift;
    }

    public void windowDeactivated(WindowEvent e) {
        this.parent.setBitmap(null);
    }

    public void windowIconified(WindowEvent e) {
        this.parent.setBitmap(null);
    }

    public void windowClosing(WindowEvent e) {
        this.parent.setVisible(false);
        this.parent.dispose();
    }
}

/*******************************************************************************
 *
 * @class RszFrameMover
 *
 * Mouse event handler that allows the dragging of the frame by clicking in any
 * point of the window area.
 *
 */
class RszFrameMover extends MouseInputAdapter {

    Point point;

    ResizablePopeyeFrame frame;

    /**
     * Constructor.
     */
    public RszFrameMover(ResizablePopeyeFrame frame) {
        this.frame = frame;
        this.point = new Point(0, 0);
    }

    /**
     * Mouse Pressed.
     */
    public void mousePressed(MouseEvent e) {
        this.point.x = e.getX();
        this.point.y = e.getY();
    }

    /**
     * Mouse Dragged.
     */
    public void mouseDragged(MouseEvent e) {
        Point location = frame.getLocation();
        Point moveTo = new Point(location.x + e.getX() - this.point.x, location.y + e.getY() - this.point.y);
        int w, h;
        switch(this.resize) {
            case BOTTOM:
                w = frame.getWidth();
                h = e.getY();
                System.err.println("from bottom");
                break;
            case RIGHT:
                w = e.getX();
                h = frame.getHeight();
                break;
            case BOTTOM | RIGHT:
                w = e.getX();
                h = e.getY();
                break;
            default:
                w = frame.getWidth();
                h = frame.getHeight();
                break;
        }
        if (this.resize != CENTER && this.resize != TOP) {
            frame.setSize(w, h);
        } else {
            frame.setLocation(moveTo);
        }
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        if (this.resize != CENTER) return;
        Point location = frame.getLocation();
        Point moveTo = new Point(location.x + e.getX() - this.point.x, location.y + e.getY() - this.point.y);
        frame.moveBck(moveTo);
        frame.setLocation(moveTo);
        frame.invalidate();
        frame.repaint();
    }

    private static final byte CENTER = 0x0;

    private static final byte TOP = 0x1;

    private static final byte BOTTOM = 0x2;

    private static final byte LEFT = 0x4;

    private static final byte RIGHT = 0x8;

    byte resize = CENTER;

    public void mouseMoved(MouseEvent mouseEvent) {
        int x = frame.getWidth();
        int y = frame.getHeight();
        this.resize = CENTER;
        Cursor c = Cursor.getDefaultCursor();
        if (mouseEvent.getX() < frame.getXBorderThreshold()) {
            resize |= LEFT;
        }
        if (mouseEvent.getX() > (x - frame.getXBorderThreshold())) {
            resize |= RIGHT;
        }
        if (mouseEvent.getY() < frame.getYBorderThreshold()) {
            resize |= TOP;
        }
        if (mouseEvent.getY() > (y - frame.getYBorderThreshold())) {
            resize |= BOTTOM;
        }
        switch(resize) {
            case BOTTOM:
                c = new Cursor(Cursor.S_RESIZE_CURSOR);
                break;
            case RIGHT:
                c = new Cursor(Cursor.E_RESIZE_CURSOR);
                break;
            case BOTTOM | RIGHT:
                c = new Cursor(Cursor.SE_RESIZE_CURSOR);
                break;
            default:
                c = Cursor.getDefaultCursor();
                break;
        }
        frame.setCursor(c);
    }
}
