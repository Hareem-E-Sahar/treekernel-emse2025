package test;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class BGTest1 {

    public static void main(String[] args) {
        JFrame frame = new TransparentBackground("Transparent Window");
        frame.setLayout(new BorderLayout());
        JButton button = new JButton("This is a button");
        frame.add("North", button);
        JLabel label = new JLabel("This is a label");
        frame.add("South", label);
        frame.pack();
        frame.setSize(300, 300);
        frame.setVisible(true);
    }
}

class TransparentBackground extends JFrame implements ComponentListener, WindowFocusListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1871115006302477115L;

    private Image background;

    public TransparentBackground(String title) {
        super(title);
        try {
            System.out.println((GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration()).getBounds());
            background = (new Robot()).createScreenCapture(new Rectangle(0, 0, 1280, 1024));
            addComponentListener(this);
            addWindowFocusListener(this);
        } catch (AWTException e) {
        }
    }

    public void paint(Graphics g) {
        Point pos = this.getLocationOnScreen();
        g.drawImage(background, -pos.x, -pos.y, null);
    }

    public void componentShown(ComponentEvent evt) {
        repaint();
    }

    public void componentResized(ComponentEvent evt) {
        repaint();
    }

    public void componentMoved(ComponentEvent evt) {
        repaint();
    }

    public void componentHidden(ComponentEvent evt) {
    }

    public void windowGainedFocus(WindowEvent evt) {
        refresh();
    }

    public void windowLostFocus(WindowEvent evt) {
        refresh();
    }

    public void refresh() {
        if (this.isVisible()) {
            repaint();
        }
    }
}
