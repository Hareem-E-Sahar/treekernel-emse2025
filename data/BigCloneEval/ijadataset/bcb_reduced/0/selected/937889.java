package com.gampire.pc.swing.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import com.gampire.pc.util.image.ImageUtil;

public class TranslucentComponent extends JComponent {

    private Component parent;

    private BufferedImage background;

    public TranslucentComponent(Component parent) {
        this.parent = parent;
        updateBackground();
    }

    public void updateBackground() {
        try {
            Robot rbt = new Robot();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
            background = ImageUtil.fade(background, 3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Point pos = parent.getLocationOnScreen();
        g.drawImage(background, -pos.x, -pos.y, null);
    }
}
