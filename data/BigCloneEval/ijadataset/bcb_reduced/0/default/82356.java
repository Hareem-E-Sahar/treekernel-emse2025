import java.awt.*;
import java.util.Date;
import javax.swing.*;
import java.awt.event.*;

public class CalibrationWindow extends JComponent implements ComponentListener, WindowFocusListener, Runnable {

    private JWindow frame;

    protected Image background;

    private long lastupdate = 0;

    public boolean refreshRequested = true;

    public CalibrationWindow(JWindow frame) {
        this.frame = frame;
        updateBackground();
        frame.addComponentListener(this);
        frame.addWindowFocusListener(this);
    }

    public void updateBackground() {
        try {
            Robot rbt = new Robot();
            background = rbt.createScreenCapture(new Rectangle(frame.getX(), frame.getY(), 50, 50));
        } catch (Exception ex) {
            p(ex.toString());
            ex.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        g.drawImage(background, 0, 0, null);
        paintSymbol((Graphics2D) g, 25, 25);
    }

    private void paintSymbol(Graphics2D g, int x, int y) {
        BasicStroke s = new BasicStroke(5);
        g.setStroke(s);
        g.setColor(Color.RED);
        g.drawRoundRect(x - 15, y - 15, 30, 30, 30, 30);
        s = new BasicStroke(3);
        g.setStroke(s);
        g.drawLine(x - 25, y, x + 25, y);
        g.drawLine(x, y - 25, x, y + 25);
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
        if (this.isVisible() && frame.isVisible()) {
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
                    if (frame.isVisible()) {
                        Point location = frame.getLocation();
                        frame.setVisible(false);
                        updateBackground();
                        frame.setVisible(true);
                        frame.setLocation(location);
                        refresh();
                    }
                    lastupdate = now;
                    refreshRequested = false;
                }
            }
        } catch (Exception ex) {
            p(ex.toString());
            ex.printStackTrace();
        }
    }

    public static void p(String str) {
        System.out.println(str);
    }
}
