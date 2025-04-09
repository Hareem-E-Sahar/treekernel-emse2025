package java.awt;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import sun.awt.RobotHelper;

class QtRobotHelper extends RobotHelper {

    QtGraphicsDevice qtDevice;

    Rectangle bounds;

    public static native void init();

    public native void doMouseActionNative(int x, int y, int buttons, boolean pressed);

    public void doMouseAction(int x, int y, int buttons, boolean pressed) {
        doMouseActionNative(x, y, buttons, pressed);
    }

    private static native void pCreateScreenCapture(int screenPSD, int imagePSD, int x, int y, int width, int height);

    private static native int pGetScreenPixel(int psd, int x, int y);

    public native void doKeyActionNative(int keySym, boolean pressed);

    public void doKeyAction(int keySym, boolean pressed) {
        doKeyActionNative(keySym, pressed);
    }

    public native void doKeyActionOnWidget(int KeySym, int widgetType, boolean pressed);

    static {
        init();
    }

    public QtRobotHelper(GraphicsDevice graphicsDevice) throws AWTException {
        if (graphicsDevice.getClass().getName().indexOf("QtGraphicsDevice") == -1) {
            throw new IllegalArgumentException("screen device is not on QT");
        }
        qtDevice = (QtGraphicsDevice) graphicsDevice;
    }

    public BufferedImage getScreenImage(Rectangle screenRect) {
        if (bounds == null) bounds = qtDevice.getDefaultConfiguration().getBounds();
        screenRect = screenRect.intersection(bounds);
        QtImage image = new QtImage(screenRect.width, screenRect.height, (QtGraphicsConfiguration) qtDevice.getDefaultConfiguration());
        pCreateScreenCapture(qtDevice.psd, image.psd, screenRect.x, screenRect.y, screenRect.width, screenRect.height);
        return image.getSubimage(0, 0, screenRect.width, screenRect.height);
    }

    public Color getPixelColor(int x, int y) {
        ColorModel cm = qtDevice.getDefaultConfiguration().getColorModel();
        int rgb;
        rgb = pGetScreenPixel(qtDevice.psd, x, y);
        Color color = new Color(cm.getRGB(rgb));
        return color;
    }
}
