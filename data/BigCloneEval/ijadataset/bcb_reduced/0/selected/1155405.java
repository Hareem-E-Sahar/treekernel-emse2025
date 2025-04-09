package rdw.robot;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * A simple wrapper class for java.awt.Robot.
 */
public class RemoteRobot implements RobotCallback {

    Robot robot;

    MouseRobot mouseRobot;

    KeyboardRobot keyboardRobot;

    public RemoteRobot() throws AWTException {
        robot = new Robot();
        mouseRobot = new MouseRobot(this.robot);
        keyboardRobot = new KeyboardRobot(this.robot);
    }

    public BufferedImage getScreenshot() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize);
        return robot.createScreenCapture(screenRect);
    }

    public void receiveClick(String button, String click, int x, int y) {
        robot.waitForIdle();
        int buttonType = 0;
        if (button.equals("left")) {
            buttonType = InputEvent.BUTTON1_MASK;
        } else if (button.equals("right")) {
            buttonType = InputEvent.BUTTON3_MASK;
        } else {
            return;
        }
        mouseRobot.move(x, y);
        if (click.equals("single")) {
            mouseRobot.click(buttonType);
        } else if (click.equals("double")) {
            mouseRobot.click(buttonType);
            mouseRobot.click(buttonType);
        }
    }

    public void receiveScrollUp() {
        mouseRobot.scrollUp();
    }

    public void receiveScrollDown() {
        mouseRobot.scrollDown();
    }

    /**
	 * Decodes string using URLDecoder using Latin-1.
	 */
    private String decodeString(String text) {
        try {
            return URLDecoder.decode(text, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
	 * escapeString replaces literal \b, \r, \t, etc. into properly escaped
	 * characters. For example, tab is received from the web interface as
	 * '\' and 't' (two characters). This function combines these two characters
	 * into a single, escaped character.
	 */
    private String escapeString(String text) {
        String source = text;
        String escaped = new String("");
        int pos = 0;
        while (pos < source.length()) {
            if (source.charAt(pos) != '\\') {
                escaped += source.charAt(pos);
                pos++;
                continue;
            } else if (pos + 1 == source.length()) {
                escaped += source.charAt(pos);
                continue;
            }
            char c = source.charAt(pos + 1);
            switch(c) {
                case 'b':
                    escaped += '\b';
                    break;
                case '\\':
                    escaped += '\\';
                    break;
                case 'r':
                    escaped += '\r';
                    break;
                case 'n':
                    escaped += '\n';
                    break;
                case 't':
                    escaped += '\t';
                    break;
            }
            pos += 2;
        }
        return escaped;
    }

    public void receiveText(String text) {
        text = decodeString(escapeString(text));
        robot.delay(500);
        for (int i = 0; i < text.length(); i++) {
            keyboardRobot.keyStroke(text.charAt(i));
            robot.waitForIdle();
        }
    }
}
