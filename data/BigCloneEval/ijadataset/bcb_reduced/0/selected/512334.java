package net.sf.heureka.experiment.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import net.sf.heureka.experiment.pixie.CallBackScript;
import net.sf.heureka.experiment.pixie.PixieLet;
import net.sf.heureka.experiment.ui.beans.ImageViewer;

public class ScreenShot extends PixieLet {

    public static final int DEFAULT_WIDTH = 800;

    public static final int DEFAULT_HEIGHT = 600;

    private int getExtendedState(BufferedImage image) {
        int state = JFrame.NORMAL;
        int width = image.getWidth();
        int height = image.getHeight();
        if (width >= DEFAULT_WIDTH) state = JFrame.MAXIMIZED_HORIZ;
        if (height >= DEFAULT_HEIGHT) if (state == JFrame.MAXIMIZED_HORIZ) state = JFrame.MAXIMIZED_BOTH; else state = JFrame.MAXIMIZED_VERT;
        return state;
    }

    private void show(BufferedImage image) {
        ImageViewer viewer = new ImageViewer();
        viewer.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        viewer.prepares(image);
        viewer.setExtendedState(getExtendedState(image));
        viewer.setLocationRelativeTo(null);
        viewer.setVisible(true);
    }

    @CallBackScript
    public Object scan() {
        Component content = this.session.content;
        BufferedImage image = halo.getImage(content);
        show(image);
        return null;
    }

    @CallBackScript
    public Object shot() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        Robot robot;
        try {
            robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            show(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
