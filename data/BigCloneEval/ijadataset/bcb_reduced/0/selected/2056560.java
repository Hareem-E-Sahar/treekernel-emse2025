package com.google.code.appengine.awt;

import java.lang.reflect.InvocationTargetException;
import org.apache.harmony.awt.internal.nls.Messages;
import org.apache.harmony.awt.wtk.NativeRobot;
import com.google.code.appengine.awt.AWTException;
import com.google.code.appengine.awt.AWTPermission;
import com.google.code.appengine.awt.Color;
import com.google.code.appengine.awt.EventQueue;
import com.google.code.appengine.awt.GraphicsDevice;
import com.google.code.appengine.awt.GraphicsEnvironment;
import com.google.code.appengine.awt.Rectangle;
import com.google.code.appengine.awt.Toolkit;
import com.google.code.appengine.awt.event.InputEvent;
import com.google.code.appengine.awt.image.BufferedImage;

public class Robot {

    private int autoDelay;

    private boolean autoWaitForIdle;

    private final NativeRobot nativeRobot;

    public Robot() throws AWTException {
        this(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    public Robot(GraphicsDevice screen) throws AWTException {
        Toolkit.checkHeadless();
        if ((screen == null) || (screen.getType() != GraphicsDevice.TYPE_RASTER_SCREEN)) {
            throw new IllegalArgumentException(Messages.getString("awt.129"));
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission("createRobot"));
        }
        Toolkit tk = Toolkit.getDefaultToolkit();
        nativeRobot = tk.getWTK().getNativeRobot(screen);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + "autoDelay = " + autoDelay + ", autoWaitForIdle = " + autoWaitForIdle + "]";
    }

    public BufferedImage createScreenCapture(Rectangle screenRect) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission("readDisplayPixels"));
        }
        if (screenRect.isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("awt.13D"));
        }
        return nativeRobot.createScreenCapture(screenRect);
    }

    public void delay(int ms) {
        checkDelay(ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    public int getAutoDelay() {
        return autoDelay;
    }

    public Color getPixelColor(int x, int y) {
        return nativeRobot.getPixel(x, y);
    }

    public boolean isAutoWaitForIdle() {
        return autoWaitForIdle;
    }

    public void keyPress(int keycode) {
        nativeRobot.keyEvent(keycode, true);
        doWait();
    }

    public void keyRelease(int keycode) {
        nativeRobot.keyEvent(keycode, false);
        doWait();
    }

    public void mouseMove(int x, int y) {
        nativeRobot.mouseMove(x, y);
        doWait();
    }

    public void mousePress(int buttons) {
        checkButtons(buttons);
        nativeRobot.mouseButton(buttons, true);
        doWait();
    }

    public void mouseRelease(int buttons) {
        checkButtons(buttons);
        nativeRobot.mouseButton(buttons, false);
        doWait();
    }

    public void mouseWheel(int wheelAmt) {
        nativeRobot.mouseWheel(wheelAmt);
        doWait();
    }

    public void setAutoDelay(int ms) {
        checkDelay(ms);
        autoDelay = ms;
    }

    public void setAutoWaitForIdle(boolean isOn) {
        autoWaitForIdle = isOn;
    }

    public void waitForIdle() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException(Messages.getString("awt.13E"));
        }
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void checkDelay(int ms) {
        if ((ms < 0) || (ms > 60000)) {
            throw new IllegalArgumentException(Messages.getString("awt.13F"));
        }
    }

    private void checkButtons(int buttons) {
        int mask = (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
        if ((buttons & mask) != buttons) {
            throw new IllegalArgumentException(Messages.getString("awt.140"));
        }
    }

    private void doWait() {
        if (isAutoWaitForIdle()) {
            waitForIdle();
        }
        int delay = getAutoDelay();
        if (delay > 0) {
            delay(delay);
        }
    }
}
