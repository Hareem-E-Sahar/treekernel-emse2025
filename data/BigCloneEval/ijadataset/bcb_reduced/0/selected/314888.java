package br.ujr.scorecard.gui.view.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TransparentBackground extends JPanel implements ComponentListener, WindowFocusListener, Runnable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3107020699374530737L;

    private Window window;

    private long lastupdate = 0;

    public boolean refreshRequested = true;

    private Image background;

    public TransparentBackground(Window frame) {
        this.window = frame;
        updateBackground();
        this.window.addComponentListener(this);
        this.window.addWindowFocusListener(this);
    }

    public void updateBackground() {
        try {
            Robot rbt = new Robot();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    public void paintComponent(Graphics g) {
        Point pos = this.getLocationOnScreen();
        Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
    }

    public void componentHidden(ComponentEvent arg0) {
    }

    public void componentMoved(ComponentEvent arg0) {
        repaint();
    }

    public void componentResized(ComponentEvent arg0) {
        repaint();
    }

    public void componentShown(ComponentEvent arg0) {
        repaint();
    }

    public void windowGainedFocus(WindowEvent arg0) {
        refresh();
    }

    public void windowLostFocus(WindowEvent arg0) {
        refresh();
    }

    private void refresh() {
        if (window.isVisible()) {
            repaint();
            refreshRequested = true;
            lastupdate = new Date().getTime();
        }
    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(250);
                long now = new Date().getTime();
                if (refreshRequested && ((now - lastupdate) > 1000)) {
                    if (window.isVisible()) {
                        Point location = window.getLocation();
                        window.setVisible(false);
                        updateBackground();
                        window.setVisible(true);
                        window.setLocation(location);
                        refresh();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
