package com.niyue.sandbox.uclock.guiclock;

import javax.swing.JWindow;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class TransDemo extends JWindow implements MouseMotionListener {

    public TransDemo() {
        ox = 100;
        oy = 100;
        Rectangle bounds = new Rectangle(ox, oy, W, H);
        setBackground(new Color(0, 0, 0, 0));
        setForeground(Color.MAGENTA);
        setBounds(bounds);
        underneath[0] = new BufferedImage(W, H, BufferedImage.TYPE_INT_BGR);
        underneath[1] = new BufferedImage(W, H, BufferedImage.TYPE_INT_BGR);
        try {
            robot = new Robot();
            BufferedImage tmp = robot.createScreenCapture(bounds);
            underneath[0].getGraphics().drawImage(tmp, 0, 0, null);
            underneath[1].getGraphics().drawImage(tmp, 0, 0, null);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        canvas = new BufferedImage(W, H, BufferedImage.TYPE_INT_BGR);
        addMouseMotionListener(this);
        System.gc();
    }

    public static void main(String[] args) {
        TransDemo cs = new TransDemo();
        cs.show();
    }

    public void initialize() {
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Graphics2D gc = (Graphics2D) canvas.getGraphics();
        gc.drawImage(underneath[flipflop], 0, 0, null);
        gc.setColor(Color.MAGENTA);
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.drawOval(10, 10, 80, 80);
        setLocation(ox, oy);
        g.drawImage(canvas, 0, 0, null);
    }

    public void mouseDragged(MouseEvent e) {
        int nx = ox + e.getX() - W / 2;
        int ny = oy + e.getY() - H / 2;
        int dx = nx - ox;
        int dy = ny - oy;
        if (dx == 0 && dy == 0) return;
        int flop = 1 - flipflop;
        Graphics g = underneath[flop].getGraphics();
        g.drawImage(underneath[flipflop], -dx, -dy, null);
        if (dx > 0) {
            strip.x = nx + W - dx;
            strip.y = ny;
            strip.width = dx;
            strip.height = H;
            g.drawImage(robot.createScreenCapture(strip), W - dx, 0, null);
        } else if (dx < 0) {
            strip.x = nx;
            strip.y = ny;
            strip.width = -dx;
            strip.height = H;
            g.drawImage(robot.createScreenCapture(strip), 0, 0, null);
        }
        if (dy > 0) {
            strip.x = dx > 0 ? nx : nx - dx;
            strip.y = ny + H - dy;
            strip.width = W - Math.abs(dx);
            strip.height = dy;
            g.drawImage(robot.createScreenCapture(strip), dx > 0 ? 0 : -dx, H - dy, null);
        } else if (dy < 0) {
            strip.x = dx > 0 ? nx : nx - dx;
            strip.y = ny;
            strip.width = W - Math.abs(dx);
            strip.height = -dy;
            g.drawImage(robot.createScreenCapture(strip), dx > 0 ? 0 : -dx, 0, null);
        }
        flipflop = flop;
        ox = nx;
        oy = ny;
        paint(getGraphics());
    }

    public void mouseMoved(MouseEvent e) {
    }

    private int ox = 0;

    private int oy = 0;

    private int W = 100;

    private int H = 100;

    private Robot robot;

    private BufferedImage[] underneath = new BufferedImage[2];

    private int flipflop = 0;

    private BufferedImage canvas;

    private Rectangle strip = new Rectangle();
}
