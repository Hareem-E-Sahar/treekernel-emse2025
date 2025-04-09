package co.edu.unal.ungrid.client.collaborative;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class DesktopCapture {

    private DesktopCapture() {
    }

    public static synchronized DesktopCapture getInstance() {
        if (m_this == null) {
            m_this = new DesktopCapture();
        }
        return m_this;
    }

    public BufferedImage getImage() {
        BufferedImage img = null;
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.sync();
            Rectangle screen = new Rectangle(tk.getScreenSize());
            Robot robot = new Robot();
            robot.setAutoDelay(0);
            robot.setAutoWaitForIdle(false);
            long it = System.currentTimeMillis();
            img = robot.createScreenCapture(screen);
            if (m_bDebug) System.out.println("BufferedImage::getImage(): t=" + (System.currentTimeMillis() - it));
        } catch (Exception exc) {
            System.out.println("DesktopCapture::getImage(): " + exc);
        }
        return img;
    }

    public static void main(String[] args) {
        DesktopCapture dc = DesktopCapture.getInstance();
        BufferedImage img = dc.getImage();
        if (img != null) {
            try {
                String sFormatName = args[0];
                if (!ImageIO.write(img, sFormatName, new File("desktop." + sFormatName))) {
                    System.out.println("DesktopCapture::main(): " + sFormatName + " not supported");
                }
            } catch (Exception exc) {
                System.out.println("DesktopCapture::main(): " + exc);
            }
        }
    }

    private static DesktopCapture m_this;

    private static final boolean m_bDebug = !true;
}
