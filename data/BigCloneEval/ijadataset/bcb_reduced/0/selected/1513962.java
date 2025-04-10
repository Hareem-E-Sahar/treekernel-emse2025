package papertoolkit.actions.types;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.KeyStroke;
import papertoolkit.actions.Action;
import papertoolkit.util.ArrayUtils;
import papertoolkit.util.StringUtils;
import papertoolkit.util.graphics.ImageUtils;

/**
 * <p>
 * Makes the machine run low level keyboard and mouse actions.
 * </p>
 * <p>
 * TODO: Implement higher level calls, like drag from x1,y1 to x2,y2. Which ends up being a mouse down at
 * x1,y1, mouseMove, and mouseUp at x2,y2.
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class RobotAction implements Action {

    /**
	 * multiply by these to set a direction
	 */
    public static enum MouseWheelDirection {

        ROLL_WHEEL_DOWN(1), ROLL_WHEEL_UP(-1);

        private int value;

        private MouseWheelDirection(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    /**
	 * Embodies a robot method and its arguments.
	 */
    public static class RobotCommand {

        private Object[] arguments;

        private RobotMethod method;

        public RobotCommand(RobotMethod m, Object... args) {
            method = m;
            arguments = args;
        }

        public String toString() {
            return method.getCommand() + ":" + ArrayUtils.toString(arguments);
        }
    }

    /**
	 * Different things you can ask a robot to do.
	 */
    public static enum RobotMethod {

        CREATE_SCREEN_CAPTURE("CreateScreenCapture"), DELAY("Delay"), GET_PIXEL_COLOR("GetPixelColor"), KEY_PRESS("KPress"), KEY_RELEASE("KRelease"), KEY_TYPE("KType"), MOUSE_MOVE("MMove"), MOUSE_PRESS("MPress"), MOUSE_RELEASE("MRelease"), MOUSE_WHEEL("MWheel"), SET_AUTO_DELAY("SADelay"), SET_AUTO_WAIT_FOR_IDLE("SAWaitForIdle"), WAIT_FOR_IDLE("WaitForIdle");

        private String command;

        private RobotMethod(String commandString) {
            command = commandString;
        }

        public String getCommand() {
            return command;
        }
    }

    /**
	 * Save up a list of commands to run in order.
	 */
    private List<RobotCommand> commandsToRun = new ArrayList<RobotCommand>();

    /**
	 * default to screen device 0 (primary)
	 */
    private int deviceToControl = 0;

    /**
	 * 
	 */
    public RobotAction() {
    }

    /**
	 * @param screenRect
	 * @return
	 */
    public void createScreenCapture(Rectangle screenRect, File destFile) {
        commandsToRun.add(new RobotCommand(RobotMethod.CREATE_SCREEN_CAPTURE, screenRect, destFile));
    }

    /**
	 * @param ms
	 */
    public void delay(int ms) {
        commandsToRun.add(new RobotCommand(RobotMethod.DELAY, ms));
    }

    /**
	 * @return
	 */
    public int getNumCommands() {
        return commandsToRun.size();
    }

    /**
	 * @param x
	 * @param y
	 */
    public void getPixelColor(int x, int y) {
        commandsToRun.add(new RobotCommand(RobotMethod.GET_PIXEL_COLOR, x, y));
    }

    /**
	 * Gets the local machine's Java robot object.
	 * 
	 * @return
	 */
    private Robot getRobot() {
        Robot r = null;
        final GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (!(deviceToControl >= 0 && deviceToControl < screenDevices.length)) {
            deviceToControl = 0;
        }
        try {
            r = new Robot(screenDevices[deviceToControl]);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
	 * Run through our command list and invoke each command.
	 * 
	 * @see papertoolkit.actions.Action#invoke()
	 */
    public void invoke() {
        final Robot rob = getRobot();
        for (RobotCommand command : commandsToRun) {
            Object[] arguments = command.arguments;
            RobotMethod method = command.method;
            switch(method) {
                case CREATE_SCREEN_CAPTURE:
                    BufferedImage image = rob.createScreenCapture((Rectangle) arguments[0]);
                    ImageUtils.writeImageToJPEG(image, 100, (File) arguments[1]);
                    break;
                case DELAY:
                    rob.delay((Integer) arguments[0]);
                    break;
                case GET_PIXEL_COLOR:
                    final Color pixelColor = rob.getPixelColor((Integer) arguments[0], (Integer) arguments[1]);
                    System.out.println("RobotAction :: Pixel Color at " + arguments[0] + "," + arguments[1] + " is " + pixelColor);
                    break;
                case KEY_PRESS:
                    rob.keyPress((Integer) arguments[0]);
                    break;
                case KEY_RELEASE:
                    rob.keyRelease((Integer) arguments[0]);
                    break;
                case MOUSE_MOVE:
                    rob.mouseMove((Integer) arguments[0], (Integer) arguments[1]);
                    break;
                case MOUSE_PRESS:
                    rob.mousePress((Integer) arguments[0]);
                    break;
                case MOUSE_RELEASE:
                    rob.mouseRelease((Integer) arguments[0]);
                    break;
                case MOUSE_WHEEL:
                    rob.mouseWheel((Integer) arguments[0]);
                    break;
                case SET_AUTO_DELAY:
                    rob.setAutoDelay((Integer) arguments[0]);
                    break;
                case SET_AUTO_WAIT_FOR_IDLE:
                    rob.setAutoWaitForIdle((Boolean) arguments[0]);
                    break;
                case WAIT_FOR_IDLE:
                    rob.waitForIdle();
                    break;
            }
        }
    }

    /**
	 * @param keycode
	 *            e.g, KeyEvent.VK_A, or KeyEvent.VK_SHIFT
	 */
    public void keyPress(int keycode) {
        commandsToRun.add(new RobotCommand(RobotMethod.KEY_PRESS, keycode));
    }

    /**
	 * @param keycode
	 */
    public void keyRelease(int keycode) {
        commandsToRun.add(new RobotCommand(RobotMethod.KEY_RELEASE, keycode));
    }

    /**
	 * @param keycode
	 *            e.g, KeyEvent.VK_A, or KeyEvent.VK_SHIFT
	 */
    public void keyType(int keycode) {
        keyPress(keycode);
        keyRelease(keycode);
    }

    /**
	 * Queue up a mouseMove.
	 * 
	 * @param x
	 * @param y
	 */
    public void mouseMove(int x, int y) {
        commandsToRun.add(new RobotCommand(RobotMethod.MOUSE_MOVE, x, y));
    }

    /**
	 * InputEvent.BUTTON1_MASK, InputEvent.BUTTON2_MASK, InputEvent.BUTTON3_MASK
	 * 
	 * @param buttons
	 */
    public void mousePress(int buttons) {
        commandsToRun.add(new RobotCommand(RobotMethod.MOUSE_PRESS, buttons));
    }

    /**
	 * @param buttons
	 * @return
	 */
    public void mouseRelease(int buttons) {
        commandsToRun.add(new RobotCommand(RobotMethod.MOUSE_RELEASE, buttons));
    }

    /**
	 * @param wheelAmt
	 * @param direction
	 */
    public void mouseWheel(int wheelAmt, MouseWheelDirection direction) {
        commandsToRun.add(new RobotCommand(RobotMethod.MOUSE_WHEEL, Math.abs(wheelAmt) * direction.getValue()));
    }

    /**
	 * @param ms
	 */
    public void setAutoDelay(int ms) {
        commandsToRun.add(new RobotCommand(RobotMethod.SET_AUTO_DELAY, ms));
    }

    /**
	 * @param isOn
	 */
    public void setAutoWaitForIdle(boolean isOn) {
        commandsToRun.add(new RobotCommand(RobotMethod.SET_AUTO_WAIT_FOR_IDLE, isOn));
    }

    /**
	 * @param requestedDevice
	 */
    public void setScreenDevice(int requestedDevice) {
        deviceToControl = requestedDevice;
    }

    /**
	 * @param text
	 */
    public void typeString(String text) {
        char[] cs = text.toCharArray();
        for (char key : cs) {
            keyType(KeyStroke.getKeyStroke(key).getKeyCode());
        }
    }

    /**
	 */
    public void waitForIdle() {
        commandsToRun.add(new RobotCommand(RobotMethod.WAIT_FOR_IDLE));
    }

    public String toString() {
        return "Invoke Java Robot Commands: " + commandsToRun;
    }
}
