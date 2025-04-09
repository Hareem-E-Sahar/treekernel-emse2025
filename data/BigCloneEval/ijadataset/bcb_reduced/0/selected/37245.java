package com.vayoodoot.research.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;

public class DissolveHack {

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Dissolve Hack");
        JButton quit = new JButton("Quit");
        quit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                new Dissolver().dissolveExit(frame);
            }
        });
        frame.getContentPane().add(quit);
        frame.pack();
        frame.setLocation(300, 300);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}

class Dissolver extends JComponent implements Runnable {

    public Dissolver() {
    }

    public void dissolveExit(JFrame frame) {
        try {
            this.frame = frame;
            Robot robot = new Robot();
            Rectangle frame_rect = frame.getBounds();
            frame_buffer = robot.createScreenCapture(frame_rect);
            frame.setVisible(false);
            Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screen_rect = new Rectangle(0, 0, screensize.width, screensize.height);
            screen_buffer = robot.createScreenCapture(screen_rect);
            fullscreen = new Window(new JFrame());
            fullscreen.setSize(screensize);
            fullscreen.add(this);
            this.setSize(screensize);
            fullscreen.setVisible(true);
            new Thread(this).start();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    Frame frame;

    Window fullscreen;

    BufferedImage frame_buffer;

    BufferedImage screen_buffer;

    int count;

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.drawImage(screen_buffer, -fullscreen.getX(), -fullscreen.getY(), null);
        Composite old_comp = g2.getComposite();
        Composite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - ((float) count) / 20f);
        g2.setComposite(fade);
        g2.drawImage(frame_buffer, frame.getX(), frame.getY(), null);
        g2.setComposite(old_comp);
    }

    public void run() {
        try {
            count = 0;
            Thread.currentThread().sleep(100);
            for (int i = 0; i < 20; i++) {
                count = i;
                fullscreen.repaint();
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException ex) {
        }
        System.exit(0);
    }
}

class SpinDissolver extends Dissolver {

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.drawImage(screen_buffer, -fullscreen.getX(), -fullscreen.getY(), null);
        AffineTransform old_trans = g2.getTransform();
        g2.translate(frame.getX(), frame.getY());
        g2.translate(-((count + 1) * (frame.getX() + frame.getWidth()) / 20), 0);
        float scale = 1f / ((float) count + 1);
        g2.scale(scale, scale);
        g2.rotate(((float) count) / 3.14 / 1.3, frame.getWidth() / 2, frame.getHeight() / 2);
        g2.drawImage(frame_buffer, 0, 0, null);
        g2.setTransform(old_trans);
    }
}
