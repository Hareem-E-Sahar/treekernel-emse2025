package jaguar;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * @author grootsw
 *
 */
public class JaguarBackground extends JComponent {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private JFrame frame;

    private BufferedImage background;

    public JaguarBackground(JFrame frame) {
        this.frame = frame;
        updateBackground();
    }

    public void updateBackground() {
        try {
            Robot rbt = Jaguar.getRobby();
            Toolkit tk = frame.getToolkit();
            Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        Point pos = this.getLocationOnScreen();
        Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
    }
}
