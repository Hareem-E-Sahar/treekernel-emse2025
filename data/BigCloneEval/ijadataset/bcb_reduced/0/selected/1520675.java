package monitor.layer3logic;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3151964290310442195L;

    private Robot robot;

    public MainFrame() throws HeadlessException {
        init();
    }

    private void init() {
        setSize(200, 70);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridLayout());
        getContentPane().add(new JButton("Hello 1"));
        getContentPane().add(new JButton("Hello 2"));
        getContentPane().add(new JButton("Hello 3"));
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        pack();
    }

    public BufferedImage captureScreen() {
        BufferedImage image = new BufferedImage(getContentPane().getWidth(), getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
        getContentPane().paint(image.createGraphics());
        if (isVisible()) {
            dumpAWT(getContentPane(), image);
        }
        return image;
    }

    private void dumpAWT(Container container, BufferedImage image) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (!(child instanceof JComponent)) {
                Rectangle bounds = child.getBounds();
                Point location = bounds.getLocation();
                bounds.setLocation(child.getLocationOnScreen());
                BufferedImage capture = robot.createScreenCapture(bounds);
                bounds.setLocation(location);
                SwingUtilities.convertRectangle(child, bounds, getContentPane());
                image.createGraphics().drawImage(capture, location.x, location.y, this);
                if (child instanceof Container) {
                    dumpAWT(container, image);
                }
            }
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
        BufferedImage image = frame.captureScreen();
        try {
            com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(new FileOutputStream("D:\\Fejlesztes\\GUIJSFTomcat60\\JSFT60Tutorial\\WebContent\\images\\myFile.jpg")).encode(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
