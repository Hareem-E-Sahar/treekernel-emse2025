package CORE;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 *
 * @author Olivier Combe
 */
public class Dissolver extends JComponent implements Runnable {

    Frame frame;

    Window fullscreen;

    int count;

    BufferedImage frame_buffer;

    BufferedImage screen_buffer;

    public Dissolver() {
    }

    public void dissolveExit(JFrame frame) {
        try {
            this.frame = frame;
            Robot robot = new Robot();
            Rectangle frame_rect = frame.getBounds();
            frame_buffer = robot.createScreenCapture(frame_rect);
            frame.setVisible(false);
            Dimension screensize = new Dimension(frame.getWidth(), frame.getHeight());
            screen_buffer = robot.createScreenCapture(frame.getBounds());
            fullscreen = new Window(new JFrame());
            fullscreen.setBounds(frame.getBounds());
            fullscreen.add(this);
            frame.setVisible(true);
            fullscreen.setVisible(true);
            new Thread(this).start();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
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
        fullscreen.setVisible(false);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.drawImage(screen_buffer, 0, 0, null);
        Composite old_comp = g2.getComposite();
        Composite fade = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - ((float) count) / 20f);
        g2.setComposite(fade);
        g2.drawImage(frame_buffer, frame.getX(), frame.getY(), null);
        g2.setComposite(old_comp);
    }
}
