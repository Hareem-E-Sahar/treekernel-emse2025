package org.jazzteam.edu.lang.swing.exempleFrames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TransparentBackground extends JComponent implements MouseMotionListener, MouseListener, ComponentListener, WindowListener {

    /**
*
*/
    private static final long serialVersionUID = 1L;

    private final JFrame frame;

    private int x, y;

    private Image background;

    public TransparentBackground(JFrame frame) {
        this.frame = frame;
        frame.addWindowListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        updateBackground();
    }

    public void updateBackground() {
        try {
            Dimension frameDim = frame.getSize();
            frame.setSize(0, 0);
            Robot rbt = new Robot();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
            frame.setSize(frameDim);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Point pos = this.getLocationOnScreen();
        Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
    }

    public void componentResized(ComponentEvent e) {
        Component[] components = ((JFrame) e.getComponent()).getContentPane().getComponents();
        if (components.length > 0) components[0].repaint();
    }

    public void componentMoved(ComponentEvent e) {
        componentResized(e);
    }

    public void componentShown(ComponentEvent e) {
        componentResized(e);
    }

    public void componentHidden(ComponentEvent e) {
        componentResized(e);
    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Transparent Window");
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton button = new JButton("This is a button");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        TransparentBackground bg = new TransparentBackground(frame);
        bg.setLayout(new BorderLayout());
        bg.add("North", button);
        JLabel label = new JLabel("This is a label");
        bg.add("South", label);
        frame.getContentPane().add("Center", bg);
        frame.setSize(200, 200);
        frame.setVisible(true);
    }

    public void mouseDragged(MouseEvent e) {
        frame.setLocation(e.getX() + frame.getX() - x, e.getY() + frame.getY() - y);
        frame.repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        updateBackground();
        frame.repaint();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
        updateBackground();
        frame.repaint();
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }
}
