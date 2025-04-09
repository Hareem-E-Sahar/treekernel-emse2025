package jemu.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class TransparentBackground extends JComponent implements ComponentListener, WindowFocusListener, Runnable {

    private JFrame _frame;

    private BufferedImage _background;

    private long _lastUpdate = 0;

    private boolean _refreshRequested = true;

    private Robot _robot;

    private Rectangle _screenRect;

    private ConvolveOp _blurOp;

    public boolean running;

    public TransparentBackground(JFrame frame) {
        _frame = frame;
        try {
            _robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            return;
        }
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        _screenRect = new Rectangle(dim.width, dim.height);
        float[] my_kernel = { 0.10f, 0.10f, 0.10f, 0.10f, 0.20f, 0.10f, 0.10f, 0.10f, 0.10f };
        _blurOp = new ConvolveOp(new Kernel(3, 3, my_kernel));
        updateBackground();
        _frame.addComponentListener(this);
        _frame.addWindowFocusListener(this);
        new Thread(this).start();
    }

    protected void updateBackground() {
        this.setVisible(false);
        _background = _robot.createScreenCapture(_screenRect);
        this.setVisible(true);
    }

    protected void refresh() {
        if (_frame.isVisible() && this.isVisible()) {
            repaint();
            _refreshRequested = true;
            _lastUpdate = System.currentTimeMillis();
        }
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        try {
            Point pos = this.getLocationOnScreen();
            BufferedImage buf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            buf.getGraphics().drawImage(_background, -pos.x, -pos.y, null);
            Image img = _blurOp.filter(buf, null);
            g2.drawImage(img, 0, 0, this);
        } catch (Exception e) {
        }
    }

    public void setQuit() {
        running = false;
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
        refresh();
    }

    public void windowLostFocus(WindowEvent e) {
        refresh();
    }

    public void run() {
        try {
            while (running) {
                Thread.sleep(100);
                long now = System.currentTimeMillis();
                if (_refreshRequested && ((now - _lastUpdate) > 1000)) {
                    if (_frame.isVisible()) {
                        Point location = _frame.getLocation();
                        _frame.setLocation(-_frame.getWidth(), -_frame.getHeight());
                        updateBackground();
                        _frame.setLocation(location);
                        refresh();
                    }
                    _lastUpdate = now;
                    _refreshRequested = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Transparent Window");
        TransparentBackground bg = new TransparentBackground(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(bg);
        frame.pack();
        frame.setSize(200, 200);
        frame.setLocation(500, 500);
        frame.setVisible(true);
    }
}
