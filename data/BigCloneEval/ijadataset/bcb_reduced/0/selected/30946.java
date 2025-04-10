package sun.awt;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

/**
 * This class is used to generate native system input events
 * for the purposes of test automation, self-running demos, and
 * other applications where control of the mouse and keyboard
 * is needed. The primary purpose of Robot is to facilitate
 * automated testing of Java platform implementations.
 * <p>
 * Using the class to generate input events differs from posting
 * events to the AWT event queue or AWT components in that the
 * events are generated in the platform's native input
 * queue. For example, <code>Robot.mouseMove</code> will actually move
 * the mouse cursor instead of just generating mouse move events.
 * <p>
 * Note that some platforms require special privileges or extensions
 * to access low-level input control. If the current platform configuration
 * does not allow input control, an <code>AWTException</code> will be thrown
 * when trying to construct Robot objects. For example, X-Window systems
 * will throw the exception if the XTEST 2.2 standard extension is not supported
 * (or not enabled) by the X server.
 * <p>
 * Applications that use Robot for purposes other than self-testing should
 * handle these error conditions gracefully.
 *
 * @version      1.3, 11/21/02
 * @author       Nicholas Allen
 */
public class Robot {

    private static final int MAX_DELAY = 60000;

    private boolean isAutoWaitForIdle = false;

    private int autoDelay = 0;

    private static final int LEGAL_BUTTON_MASK = InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;

    /** The location where the mouse was last moved to. As Microwindows does not support
        generating the low level events in native drivers, events are posted to the Java
        event queue. When mouseMoved is called the coordinates are remembered in these
        fields (The mouse may be physically in a different place though). */
    private int mouseX, mouseY;

    /** The graphics device useed for screen capture. */
    private GraphicsDevice screen;

    private RobotHelper robotHelper;

    /**
     * Constructs a Robot object in the coordinate system of the primary screen.
     * <p>
     *
     * @throws   AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true
     * @throws   SecurityException if <code>createRobot</code> permission is not granted
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     SecurityManager#checkPermission
     * @see      AWTPermission
     */
    public Robot() throws AWTException {
        this(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    /**
     * Creates a Robot for the given screen device. Coordinates passed
     * to Robot method calls like mouseMove and createScreenCapture will
     * be interpreted as being in the same coordinate system as the
     * specified screen. Note that depending on the platform configuration,
     * multiple screens may either:
     * <ul>
     * <li>share the same coordinate system to form a combined virtual screen</li>
     * <li>use different coordinate systems to act as independent screens</li>
     * </ul>
     * This constructor is meant for the latter case.
     * <p>
     * If screen devices are reconfigured such that the coordinate system is
     * affected, the behavior of existing Robot objects is undefined.
     *
     * @param screen     A screen GraphicsDevice indicating the coordinate
     *           system the Robot will operate in.
     * @throws   AWTException if the platform configuration does not allow
     * low-level input control.  This exception is always thrown when
     * GraphicsEnvironment.isHeadless() returns true.
     * @throws  IllegalArgumentException if <code>screen</code> is not a screen
     *   GraphicsDevice.
     * @throws   SecurityException if <code>createRobot</code> permission is not granted
     * @see     java.awt.GraphicsEnvironment#isHeadless
     * @see     GraphicsDevice
     * @see     SecurityManager#checkPermission
     * @see      AWTPermission
     */
    public Robot(GraphicsDevice screen) throws AWTException {
        robotHelper = RobotHelper.getRobotHelper(screen);
        if (robotHelper == null) {
            throw new IllegalArgumentException("not a valid screen device");
        }
        this.screen = screen;
    }

    /**
     * Moves mouse pointer to given screen coordinates.
     * @param x  X position
     * @param y  Y position
     */
    public synchronized void mouseMove(int x, int y) {
        mouseX = x;
        mouseY = y;
        robotHelper.doMouseAction(x, y, 0, false);
        afterEvent();
    }

    /**
     * Presses one or more mouse buttons.
     *
     * @param buttons    the Button mask; a combination of one or more
     * of these flags:
     * <ul>
     * <li><code>InputEvent.BUTTON1_MASK</code>
     * <li><code>InputEvent.BUTTON2_MASK</code>
     * <li><code>InputEvent.BUTTON3_MASK</code>
     * </ul>
     * @throws   IllegalArgumentException if the button mask is not a
     *   valid combination
     */
    public synchronized void mousePress(int buttons) {
        checkButtonsArgument(buttons);
        robotHelper.doMouseAction(mouseX, mouseY, buttons, true);
        afterEvent();
    }

    /**
     * Releases one or more mouse buttons.
     *
     * @param buttons    the Button mask; a combination of one or more
     * of these flags:
     * <ul>
     * <li><code>InputEvent.BUTTON1_MASK</code>
     * <li><code>InputEvent.BUTTON2_MASK</code>
     * <li><code>InputEvent.BUTTON3_MASK</code>
     * </ul>
     * @throws   IllegalArgumentException if the button mask is not a valid
     *   combination
     */
    public synchronized void mouseRelease(int buttons) {
        checkButtonsArgument(buttons);
        robotHelper.doMouseAction(mouseX, mouseY, buttons, false);
        afterEvent();
    }

    private void checkButtonsArgument(int buttons) {
        if ((buttons | LEGAL_BUTTON_MASK) != LEGAL_BUTTON_MASK) {
            throw new IllegalArgumentException("Invalid combination of button flags");
        }
    }

    /**
     * Presses a given key.
     * <p>
     * Key codes that have more than one physical key associated with them
     * (e.g. <code>KeyEvent.VK_SHIFT</code> could mean either the
     * left or right shift key) will map to the left key.
     *
     * @param    keyCode         Key to press (e.g. <code>KeyEvent.VK_A</code>)
     * @throws   IllegalArgumentException if <code>keycode</code> is not a valid key
     * @see     java.awt.event.KeyEvent
     */
    public synchronized void keyPress(int keycode) {
        checkKeycodeArgument(keycode);
        robotHelper.doKeyAction(keycode, true);
        afterEvent();
    }

    /**
     * Releases a given key.
     * <p>
     * Key codes that have more than one physical key associated with them
     * (e.g. <code>KeyEvent.VK_SHIFT</code> could mean either the
     * left or right shift key) will map to the left key.
     *
     * @param    keyCode         Key to release (e.g. <code>KeyEvent.VK_A</code>)
     * @throws   IllegalArgumentException if <code>keycode</code> is not a valid key
     * @see     java.awt.event.KeyEvent
     */
    public synchronized void keyRelease(int keycode) {
        checkKeycodeArgument(keycode);
        robotHelper.doKeyAction(keycode, false);
        afterEvent();
    }

    private void checkKeycodeArgument(int keycode) {
        if (keycode == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException("Invalid key code");
        }
    }

    /**
     * Returns the color of a pixel at the given screen coordinates.
     * @param    x       X position of pixel
     * @param    y       Y position of pixel
     * @return  Color of the pixel
     */
    public synchronized Color getPixelColor(int x, int y) {
        return robotHelper.getPixelColor(x, y);
    }

    /**
     * Creates an image containing pixels read from the screen.
     * @param    screenRect      Rect to capture in screen coordinates
     * @return   The captured image
     * @throws   IllegalArgumentException if <code>screenRect</code> width and height are not greater than zero
     * @throws   SecurityException if <code>readDisplayPixels</code> permission is not granted
     * @see     SecurityManager#checkPermission
     * @see      AWTPermission
     */
    public synchronized BufferedImage createScreenCapture(Rectangle screenRect) {
        if (screenRect.width <= 0 || screenRect.height <= 0) {
            throw new IllegalArgumentException("Rectangle width and height must be > 0");
        }
        return robotHelper.getScreenImage(screenRect);
    }

    private void afterEvent() {
        autoWaitForIdle();
        autoDelay();
    }

    /**
     * Returns whether this Robot automatically invokes <code>waitForIdle</code>
     * after generating an event.
     * @return Whether <code>waitForIdle</code> is automatically called
     */
    public synchronized boolean isAutoWaitForIdle() {
        return isAutoWaitForIdle;
    }

    /**
     * Sets whether this Robot automatically invokes <code>waitForIdle</code>
     * after generating an event.
     * @param    isOn    Whether <code>waitForIdle</code> is automatically invoked
     */
    public synchronized void setAutoWaitForIdle(boolean isOn) {
        isAutoWaitForIdle = isOn;
    }

    private void autoWaitForIdle() {
        if (isAutoWaitForIdle) {
            waitForIdle();
        }
    }

    /**
     * Returns the number of milliseconds this Robot sleeps after generating an event.
     */
    public synchronized int getAutoDelay() {
        return autoDelay;
    }

    /**
     * Sets the number of milliseconds this Robot sleeps after generating an event.
     * @throws   IllegalArgumentException If <code>ms</code> is not between 0 and 60,000 milliseconds inclusive
     */
    public synchronized void setAutoDelay(int ms) {
        checkDelayArgument(ms);
        autoDelay = ms;
    }

    private void autoDelay() {
        delay(autoDelay);
    }

    /**
     * Sleeps for the specified time.
     * To catch any <code>InterruptedException</code>s that occur,
     * <code>Thread.sleep()</code> may be used instead.
     * @param    ms      time to sleep in milliseconds
     * @throws   IllegalArgumentException if <code>ms</code> is not between 0 and 60,000 milliseconds inclusive
     * @see     java.lang.Thread#sleep()
     */
    public synchronized void delay(int ms) {
        checkDelayArgument(ms);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ite) {
            ite.printStackTrace();
        }
    }

    private void checkDelayArgument(int ms) {
        if (ms < 0 || ms > MAX_DELAY) {
            throw new IllegalArgumentException("Delay must be to 0 to 60,000ms");
        }
    }

    /**
     * Waits until all events currently on the event queue have been processed.
     * @throws   IllegalThreadStateException if called on the AWT event dispatching thread
     */
    public synchronized void waitForIdle() {
        checkNotDispatchThread();
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                }
            });
        } catch (InterruptedException ite) {
            System.err.println("Robot.waitForIdle, non-fatal exception caught:");
            ite.printStackTrace();
        } catch (InvocationTargetException ine) {
            System.err.println("Robot.waitForIdle, non-fatal exception caught:");
            ine.printStackTrace();
        }
    }

    private void checkNotDispatchThread() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalThreadStateException("Cannot call method from the event dispatcher thread");
        }
    }

    /**
     * Returns a string representation of this Robot.
     *
     * @return   the string representation.
     */
    public synchronized String toString() {
        String params = "autoDelay = " + getAutoDelay() + ", " + "autoWaitForIdle = " + isAutoWaitForIdle();
        return getClass().getName() + "[ " + params + " ]";
    }
}
