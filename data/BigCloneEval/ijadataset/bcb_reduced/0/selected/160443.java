package net.java.swingfx.waitwithstyle;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * A InfiniteProgressPanel-like component, but more efficient. This is the preferred class to use unless you need the
 * total control over the appearance that InfiniteProgressPanel gives you.<br /><br />
 * An infinite progress panel displays a rotating figure and a message to notice the user of a long, duration unknown
 * task. The shape and the text are drawn upon a white veil which alpha level (or shield value) lets the underlying
 * component shine through. This panel is meant to be used asa <i>glass pane</i> in the window performing the long
 * operation. <br /><br /> Calling setVisible(true) makes the component visible and starts the animation.
 * Calling setVisible(false) halts the animation and makes the component invisible.
 * Once you've started the animation all the mouse events are intercepted by this panel, preventing them from being
 * forwared to the underlying components. <br /><br /> The panel can be controlled by the <code>setVisible()</code>,
 * method. <br /><br /> Example: <br /><br />
 * <pre>PerformanceInfiniteProgressPanel pane = new PerformanceInfiniteProgressPanel();
 * frame.setGlassPane(pane);
 * pane.setVisible(true);
 * // Do something here, presumably launch a new thread
 * // ...
 * // When the thread terminates:
 * pane.setVisible(false);
 * </pre>
 * @see InfiniteProgressPanel
 * <br /><br />
 * $Revision: 1.2 $
 *
 * @author Romain Guy
 * @author Henry Story
 * @version 1.0
 */
public class PerformanceInfiniteProgressPanel extends JComponent implements ActionListener {

    private static final int NUMBER_OF_BARS = 12;

    private double dScale = 1.2d;

    private MouseAdapter mouseAdapter = new MouseAdapter() {
    };

    private MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
    };

    private KeyAdapter keyAdapter = new KeyAdapter() {
    };

    private ComponentAdapter componentAdapter = new ComponentAdapter() {

        public void componentResized(ComponentEvent e) {
            if (useBackBuffer == true) {
                setOpaque(false);
                imageBuf = null;
                iterate = 3;
            }
        }
    };

    private BufferedImage imageBuf = null;

    private Area[] bars;

    private Rectangle barsBounds = null;

    private Rectangle barsScreenBounds = null;

    private AffineTransform centerAndScaleTransform = null;

    private Timer timer = new Timer(1000 / 4, this);

    private Color[] colors = new Color[NUMBER_OF_BARS * 2];

    private int colorOffset = 0;

    private boolean useBackBuffer;

    private boolean tempHide = false;

    public PerformanceInfiniteProgressPanel(boolean i_bUseBackBuffer) {
        useBackBuffer = i_bUseBackBuffer;
        bars = buildTicker(NUMBER_OF_BARS);
        barsBounds = new Rectangle();
        for (int i = 0; i < bars.length; i++) {
            barsBounds = barsBounds.union(bars[i].getBounds());
        }
        for (int i = 0; i < bars.length; i++) {
            int channel = 224 - 128 / (i + 1);
            colors[i] = new Color(channel, channel, channel);
            colors[NUMBER_OF_BARS + i] = colors[i];
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setOpaque(useBackBuffer);
    }

    int iterate;

    /**
     * Called to animate the rotation of the bar's colors
     */
    public void actionPerformed(ActionEvent e) {
        if (colorOffset == (NUMBER_OF_BARS - 1)) {
            colorOffset = 0;
        } else {
            colorOffset++;
        }
        if (barsScreenBounds != null) {
            repaint(barsScreenBounds);
        } else {
            repaint();
        }
        if (useBackBuffer && imageBuf == null) {
            if (iterate < 0) try {
                makeSnapshot();
                setOpaque(true);
            } catch (AWTException e1) {
                e1.printStackTrace();
            } else iterate--;
        }
    }

    /**
     * Show/Hide the pane, starting and stopping the animation as you go
     */
    public void setVisible(boolean i_bIsVisible) {
        setOpaque(false);
        if (i_bIsVisible) {
            if (useBackBuffer) {
                SwingUtilities.getWindowAncestor(this).addComponentListener(componentAdapter);
                iterate = 3;
            }
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseMotionAdapter);
            addKeyListener(keyAdapter);
            timer.start();
        } else {
            timer.stop();
            imageBuf = null;
            removeMouseListener(mouseAdapter);
            removeMouseMotionListener(mouseMotionAdapter);
            removeKeyListener(keyAdapter);
            Window oWindow = SwingUtilities.getWindowAncestor(this);
            if (oWindow != null) oWindow.removeComponentListener(componentAdapter);
        }
        super.setVisible(i_bIsVisible);
    }

    private void makeSnapshot() throws AWTException {
        Window oWindow = SwingUtilities.getWindowAncestor(this);
        Insets oInsets = oWindow.getInsets();
        Rectangle oRectangle = new Rectangle(oWindow.getBounds());
        oRectangle.x += oInsets.left;
        oRectangle.y += oInsets.top;
        oRectangle.width -= oInsets.left + oInsets.right;
        oRectangle.height -= oInsets.top + oInsets.bottom;
        imageBuf = new Robot().createScreenCapture(oRectangle);
    }

    /**
     * Recalc bars based on changes in size
     */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        centerAndScaleTransform = new AffineTransform();
        centerAndScaleTransform.translate((double) getWidth() / 2d, (double) getHeight() / 2d);
        centerAndScaleTransform.scale(dScale, dScale);
        if (barsBounds != null) {
            Area oBounds = new Area(barsBounds);
            oBounds.transform(centerAndScaleTransform);
            barsScreenBounds = oBounds.getBounds();
        }
    }

    /**
     * paint background dimed and bars over top
     */
    protected void paintComponent(Graphics g) {
        if (!tempHide) {
            Rectangle oClip = g.getClipBounds();
            if (imageBuf != null) {
            } else {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.transform(centerAndScaleTransform);
            for (int i = 0; i < bars.length; i++) {
                g2.setColor(colors[i + colorOffset]);
                g2.fill(bars[i]);
            }
        }
    }

    /**
     * Builds the circular shape and returns the result as an array of <code>Area</code>. Each <code>Area</code> is one
     * of the bars composing the shape.
     */
    private static Area[] buildTicker(int i_iBarCount) {
        Area[] ticker = new Area[i_iBarCount];
        Point2D.Double center = new Point2D.Double(0, 0);
        double fixedAngle = 2.0 * Math.PI / ((double) i_iBarCount);
        for (double i = 0.0; i < (double) i_iBarCount; i++) {
            Area primitive = buildPrimitive();
            AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
            AffineTransform toBorder = AffineTransform.getTranslateInstance(45.0, -6.0);
            AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());
            AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);
            primitive.transform(toWheel);
            primitive.transform(toCircle);
            ticker[(int) i] = primitive;
        }
        return ticker;
    }

    /**
     * Builds a bar.
     */
    private static Area buildPrimitive() {
        Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
        Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
        Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);
        Area tick = new Area(body);
        tick.add(new Area(head));
        tick.add(new Area(tail));
        return tick;
    }
}
