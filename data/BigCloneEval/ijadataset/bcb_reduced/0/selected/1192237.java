package jrpc;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

class ScreenShooter {

    Rectangle screenResolution;

    BufferedImage oldScreen, currentScreen;

    PositionedImageBuffer changes;

    ScreenShooter() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        screenResolution = new Rectangle(0, 0, screenSize.width, screenSize.height);
        currentScreen = null;
    }

    BufferedImage TakeScreen() throws AWTException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        screenResolution = new Rectangle(0, 0, screenSize.width, screenSize.height);
        Robot robot = new Robot();
        oldScreen = currentScreen;
        currentScreen = robot.createScreenCapture(screenResolution);
        return currentScreen;
    }

    PositionedImageBuffer changeScreen() throws AWTException {
        int x, y, width, height, startX = 0, startY = 0, endX = 0, endY = 0;
        boolean startpoint = false, endpoint = false;
        TakeScreen();
        width = currentScreen.getWidth();
        height = currentScreen.getHeight();
        endX = width;
        endY = height;
        if ((width != oldScreen.getWidth()) || height != oldScreen.getHeight()) {
            changes = new PositionedImageBuffer(startX, startY, width, height, currentScreen);
        } else {
            for (y = currentScreen.getMinY(); y < height; y += 2) {
                for (x = currentScreen.getMinX(); x < width; x += 2) {
                    if (startpoint) {
                        if (currentScreen.getRGB(x, y) != oldScreen.getRGB(x, y)) {
                            startX = x;
                            startY = y;
                            startpoint = true;
                        }
                    }
                    if (endpoint) {
                        if (currentScreen.getRGB(width - x, height - y) != oldScreen.getRGB(width - x, height - y)) {
                            endX = width - x;
                            endY = height - y;
                            endpoint = true;
                        }
                    }
                    if (startpoint && endpoint) {
                        break;
                    }
                }
                if (startpoint && endpoint) {
                    break;
                }
            }
            if (startX > 2) startX -= 2; else startX = 0;
            if (startY > 2) startY -= 2; else startY = 0;
            if (endX < (width - 2)) endX += 2; else endX = width;
            if (endY < (height - 2)) endY += 2; else endY = height;
            changes = new PositionedImageBuffer(startX, startY, width, height, currentScreen);
        }
        return changes;
    }

    void actionCatcher(MouseOrKeybData catcher) throws AWTException {
        Robot robot = new Robot();
        if (catcher.type == 0) {
            robot.mouseMove((int) catcher.newX, (int) catcher.newY);
        } else if (catcher.type == 1) {
            robot.mousePress(catcher.button);
        } else if (catcher.type == 2) {
            robot.mouseRelease(catcher.button);
        } else if (catcher.type == 3) {
            robot.keyPress(catcher.key);
        } else if (catcher.type == 4) {
            robot.keyRelease(catcher.key);
        } else System.out.println("Error\n");
    }
}
