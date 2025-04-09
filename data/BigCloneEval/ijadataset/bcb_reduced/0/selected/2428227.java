package net.sf.accolorhelper.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class ScreenGrabber implements Runnable {

    public ScreenGrabber(Component pComponent, Robot pRobot, int pWidth, int pHeight) {
        super();
        aRobot = pRobot;
        aComponent = pComponent;
        aWidth = pWidth;
        aHeight = pHeight;
        aScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
            if (aComponent.isShowing() && aFilteringImageComponents.size() > 0) {
                Point lLocation;
                int lMaxXPosition, lMaxYPosition;
                Rectangle lRectangle;
                BufferedImage lImage;
                lLocation = MouseInfo.getPointerInfo().getLocation();
                lLocation.x -= aWidth / 2;
                lLocation.y -= aHeight / 2;
                lMaxXPosition = aScreenSize.width - aWidth;
                if (lLocation.x < 0) {
                    lLocation.x = 0;
                } else if (lLocation.x > lMaxXPosition) {
                    lLocation.x = lMaxXPosition;
                }
                lMaxYPosition = aScreenSize.height - aHeight;
                if (lLocation.y < 0) {
                    lLocation.y = 0;
                } else if (lLocation.y > lMaxYPosition) {
                    lLocation.y = lMaxYPosition;
                }
                lRectangle = new Rectangle(lLocation.x, lLocation.y, aWidth, aHeight);
                lImage = aRobot.createScreenCapture(lRectangle);
                for (FilteringImageComponent lComponent : aFilteringImageComponents) {
                    lComponent.setImage(lImage);
                }
            }
        }
    }

    public void addFilteringImageComponent(FilteringImageComponent pComponent) {
        aFilteringImageComponents.add(pComponent);
    }

    private Component aComponent;

    private Robot aRobot;

    private int aWidth;

    private int aHeight;

    private Dimension aScreenSize;

    private Set<FilteringImageComponent> aFilteringImageComponents = new HashSet<FilteringImageComponent>();
}
