package test;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;

public class JTransFrame extends JFrame {

    Robot robot;

    BufferedImage bi = null;

    int redM, greenM, blueM;

    boolean trans;

    boolean dirty = false;

    DirtFlagger dirtFlagger = new DirtFlagger();

    Thread thread;

    PrimeRun p;

    /**
     * This constructer takes in the alpha blending
     * Values for transparent windows.
     *
     * @param int r
     * @param int g
     * @param int b
     */
    public JTransFrame(int r, int g, int b) {
        super();
        setUndecorated(true);
        setContentPane(new TransRootPane());
        trans = false;
        redM = r;
        greenM = g;
        blueM = b;
    }

    public JTransFrame() {
        this(0, 0, 0);
    }

    /**
     * This method is true if the window is in transparent mode.
     *
     * @return boolean trans
     */
    public boolean isTransparent() {
        return trans;
    }

    /**
     * This method sets the window in or out of transparent mode
     *
     * @param boolean b
     */
    public void setTransparent(boolean b) {
        trans = b;
        maketrans();
    }

    /**
     * Initializes all the components for the frame.
     * Should always be called when a new instance is
     * created.
     */
    public void initTransComponents() throws Exception {
        robot = new Robot();
        p = new PrimeRun(this);
        thread = new Thread(p);
        thread.start();
    }

    /**
     * This method stops the resize/move listener threads.
     */
    public void stopThreads() {
        p.running = false;
    }

    /**
     * This method makes the window transparent when called.
     *
     */
    public void maketrans() {
        if (trans) {
            removeWindowListener(dirtFlagger);
            bi = robot.createScreenCapture(new Rectangle(getLocation().x, getLocation().y, getSize().width, getSize().height));
            for (int i = 0; i < bi.getHeight(); i++) {
                for (int j = 0; j < bi.getWidth(); j++) {
                    int rgba = bi.getRGB(j, i);
                    int red = (rgba >> 16) & 0xff;
                    int green = (rgba >> 8) & 0xff;
                    int blue = rgba & 0xff;
                    int alpha = (rgba >> 24) & 0xff;
                    if (red < redM) red = 0; else red -= redM;
                    if (green < greenM) green = 0; else green -= greenM;
                    if (blue < blueM) blue = 0; else blue -= blueM;
                    rgba = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    bi.setRGB(j, i, rgba);
                }
            }
            getContentPane().repaint();
            getRootPane().repaint();
            setVisible(true);
            dirty = false;
            addWindowListener(dirtFlagger);
        } else {
            validate();
            repaint();
        }
    }

    class DirtFlagger implements java.awt.event.WindowListener {

        public void windowStateChanged(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {
            dirty = true;
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
            dirty = true;
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
            dirty = true;
        }
    }

    class TransRootPane extends javax.swing.JComponent {

        public void paint(java.awt.Graphics g) {
            if (trans) {
                if (bi != null) {
                    g.drawImage(bi, 0, 0, null);
                }
            }
            super.paint(g);
        }
    }

    /**
     * This Class is basically a resize/move listener.
     *
     * @author James Neufeld
     *
     */
    class PrimeRun implements Runnable {

        JTransFrame tf;

        int x, y, w, h;

        public boolean running = true;

        /**
         * The constructer takes in the frame to listen too.
         *
         */
        PrimeRun(JTransFrame t) {
            tf = t;
            y = tf.getLocation().y;
            x = tf.getLocation().x;
            h = tf.getSize().height;
            w = tf.getSize().width;
        }

        /**
         * This method tells the class how to listen to resize/move
         * events.  It is automatically called when it is used in a
         * thread.
         */
        public void run() {
            while (running) {
                while ((x == tf.getLocation().x) && (y == tf.getLocation().y) && (w == tf.getSize().width) && (h == tf.getSize().height) && (!dirty) && (running)) {
                }
                x = tf.getLocation().x;
                y = tf.getLocation().y;
                h = tf.getSize().height;
                w = tf.getSize().width;
                if (tf.isTransparent()) {
                    tf.setVisible(false);
                    tf.maketrans();
                }
            }
        }
    }

    public static void main(String args[]) {
        JTransFrame jf = new JTransFrame(50, 50, 50);
        try {
            jf.initTransComponents();
            jf.setVisible(true);
        } catch (Exception e) {
        }
    }
}
