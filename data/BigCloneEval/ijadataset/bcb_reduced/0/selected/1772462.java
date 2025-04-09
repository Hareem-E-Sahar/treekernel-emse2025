package eric;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class JTransparentBackground extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Image background;

    public JTransparentBackground(final JFrame frame) {
        updateBackground();
    }

    public void updateBackground() {
        try {
            final Robot rbt = new Robot();
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
        } catch (final Exception ex) {
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        final Point pos = this.getLocationOnScreen();
        final Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
    }
}
