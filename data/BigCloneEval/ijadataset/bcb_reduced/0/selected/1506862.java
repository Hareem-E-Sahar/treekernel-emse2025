package de.blitzcoder.collide.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JPanel;

/**
 *
 * @author blitzcoder
 */
public class TranscluentPanel extends JPanel {

    private BufferedImage background;

    public TranscluentPanel() {
        updateBackground();
    }

    public void updateBackground() {
        try {
            Robot rbt = new Robot();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension dim = tk.getScreenSize();
            BufferedImage img = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
            float[] matrix = new float[15];
            for (int i = 0; i < 15; i++) {
                matrix[i] = 1.0f / 15.0f;
            }
            BufferedImageOp op = new ConvolveOp(new Kernel(2, 2, matrix));
            background = op.filter(img, background);
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
}
