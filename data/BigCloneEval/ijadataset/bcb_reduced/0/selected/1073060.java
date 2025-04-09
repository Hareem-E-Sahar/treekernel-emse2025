package org.columba.core.gui.base;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JWindow;

public class TransparentWindow extends JWindow {

    Robot robot;

    BufferedImage screen;

    Shape shape;

    BufferedImage buffer;

    ImageIcon splashimg;

    public TransparentWindow(ImageIcon splashimg) throws AWTException {
        this.splashimg = splashimg;
        robot = new Robot(getGraphicsConfiguration().getDevice());
        requestFocus();
        setSize(splashimg.getIconWidth(), splashimg.getIconHeight());
        buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        updateScreen();
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
    }

    protected void updateScreen() {
        screen = robot.createScreenCapture(new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize()));
    }

    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getID() == FocusEvent.FOCUS_GAINED) {
            updateScreen();
            repaint();
        }
    }

    public void paint(Graphics _g) {
        Graphics2D g = buffer.createGraphics();
        if (screen != null) {
            Point location = getLocationOnScreen();
            g.drawImage(screen, -location.x, -location.y, this);
        }
        g.drawImage(splashimg.getImage(), 0, 0, this);
        _g.drawImage(buffer, 0, 0, this);
    }
}
