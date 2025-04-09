package com.dukesoftware.utils.test.swing;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Only works windows xp?? 
 */
public class TransparentBackground extends JComponent implements ComponentListener, WindowFocusListener {

    public static void main(String[] args) throws AWTException {
        JFrame frame = new JFrame("transparent");
        TransparentBackground bg = new TransparentBackground(frame);
        bg.setLayout(new BorderLayout());
        JButton button = new JButton("button");
        bg.add(BorderLayout.NORTH, button);
        JLabel label = new JLabel("label");
        label.setOpaque(true);
        bg.add(BorderLayout.SOUTH, label);
        frame.getContentPane().add(BorderLayout.CENTER, bg);
        frame.setPreferredSize(new Dimension(150, 150));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static final long serialVersionUID = 1L;

    private final JFrame frame;

    private Image background;

    private final Robot robot;

    private static final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    private static final Rectangle bounds = new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight());

    private final Watcher watcher = new Watcher();

    public TransparentBackground(JFrame frame) throws AWTException {
        this.frame = frame;
        robot = new Robot();
        frame.addComponentListener(this);
        frame.addWindowFocusListener(this);
        reflesh();
        watcher.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Point pos = this.getLocationOnScreen();
        g.drawImage(background, -pos.x, -pos.y, this);
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
        repaint();
    }

    public void componentResized(ComponentEvent e) {
        repaint();
    }

    public void componentShown(ComponentEvent e) {
        repaint();
    }

    public void windowGainedFocus(WindowEvent e) {
        reflesh();
    }

    public void windowLostFocus(WindowEvent e) {
        reflesh();
    }

    private void reflesh() {
        if (frame.isVisible()) {
            repaint();
        }
    }

    private void copyScreen() {
        frame.setVisible(false);
        background = robot.createScreenCapture(bounds);
        frame.setVisible(true);
        System.out.println(background.getHeight(this));
        reflesh();
    }

    public boolean getIgnoreRepaint() {
        watcher.wakeup();
        return super.getIgnoreRepaint();
    }

    public void addNotify() {
        watcher.wakeup();
        super.addNotify();
    }

    private class Watcher extends Thread {

        public synchronized void wakeup() {
            notifyAll();
        }

        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        wait();
                    }
                    copyScreen();
                }
            } catch (InterruptedException ex) {
                return;
            }
        }
    }
}
