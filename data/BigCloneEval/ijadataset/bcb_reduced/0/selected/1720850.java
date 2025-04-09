package a03.swing.widget;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * This class defines the application GUI and starts the application.
 */
public class A03ColorPicker extends JToggleButton {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2537353930655235255L;

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
        }
    }

    class ScreenshotAreaIcon implements Icon {

        public int getIconHeight() {
            return 66;
        }

        public int getIconWidth() {
            return 66;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (screenshotAreaImage != null) {
                g.drawImage(screenshotAreaImage, x, y, 66, 66, c);
                g.setColor(Color.RED);
                g.drawRect(x + 30, y + 30, 5, 5);
                g.dispose();
            }
        }
    }

    private BufferedImage screenshotAreaImage;

    private Timer timer;

    private JColorChooser chooser;

    public A03ColorPicker(JColorChooser chooser) {
        this.chooser = chooser;
        takeScreenshot(new Point());
        setIcon(new ScreenshotAreaIcon());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (isSelected()) {
                    timer = new Timer(70, new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            if (isShowing()) {
                                Rectangle bounds = new Rectangle(getLocationOnScreen(), getSize());
                                PointerInfo info = MouseInfo.getPointerInfo();
                                Point locationOnScreen = info.getLocation();
                                if (!bounds.contains(locationOnScreen)) {
                                    takeScreenshot(locationOnScreen);
                                    repaint();
                                }
                            }
                        }
                    });
                    timer.start();
                    Window window = SwingUtilities.getWindowAncestor(A03ColorPicker.this.chooser);
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            if (timer != null) {
                                timer.stop();
                            }
                        }
                    });
                } else {
                    timer.stop();
                    timer = null;
                }
            }
        });
    }

    private void takeScreenshot(Point locationOnScreen) {
        Color pickedColor = robot.getPixelColor(locationOnScreen.x, locationOnScreen.y);
        A03ColorPicker.this.chooser.setColor(pickedColor);
        locationOnScreen.x -= 5;
        locationOnScreen.y -= 5;
        screenshotAreaImage = robot.createScreenCapture(new Rectangle(locationOnScreen, new Dimension(11, 11)));
    }
}
