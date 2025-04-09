package com.isaac4.lgpl.witl;

/**
 * @author isaac
 * 
 * Capture a screen shot of the whole screen. I learned how to capture a
 * screenshot from rgagnon (Real's Java:)
 * http://www.rgagnon.com/javadetails/java-0489.html
 */
public class ScreenCamera implements CapturableScreen {

    private java.awt.Robot robot = null;

    private java.awt.Toolkit toolkit = null;

    private java.awt.Dimension screenSize = null;

    private java.awt.Rectangle rectangle = null;

    public java.awt.image.BufferedImage getShot() {
        try {
            if (robot == null) {
                robot = new java.awt.Robot();
            }
        } catch (java.awt.AWTException ex) {
            return null;
        }
        if (toolkit == null) {
            toolkit = java.awt.Toolkit.getDefaultToolkit();
        }
        screenSize = toolkit.getScreenSize();
        rectangle = new java.awt.Rectangle(screenSize);
        return robot.createScreenCapture(rectangle);
    }
}
