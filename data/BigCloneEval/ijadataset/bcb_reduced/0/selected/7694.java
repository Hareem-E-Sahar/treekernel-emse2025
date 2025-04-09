package onepoint.express;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;
import onepoint.log.XLog;
import onepoint.log.XLogFactory;

/**
 * Class responsible for showing the splash screen of the application.
 *
 * @author horia.chiorean
 */
public class XSplashWindow extends Window {

    /**
    * This class's logger.
    */
    private static final XLog logger = XLogFactory.getLogger(XSplashWindow.class);

    /**
    * The splash image.
    */
    private Image image = null;

    /**
    * Backgroung image used for transparent window.
    */
    private Image bkImage = null;

    private Image dbImage;

    private Graphics dbg;

    /**
    * Initialized the splash window.
    *
    * @param parent      a <code>Frame</code> representing the parent frame.
    * @param image       a <code>Image</code> representing the splash image.
    * @param undecorated
    */
    private XSplashWindow(Frame parent, Image image, boolean undecorated, boolean resize, boolean onScreen) {
        super(parent);
        this.image = image;
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
            logger.error(ie);
        }
        if (undecorated) {
            parent.setUndecorated(true);
        }
        int imgWidth = image.getWidth(this);
        int imgHeight = image.getHeight(this);
        setSize(imgWidth, imgHeight);
        if (resize) {
            parent.setSize(imgWidth, imgHeight);
        }
        int x;
        int y;
        if (onScreen) {
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            x = (screenDim.width - imgWidth) / 2;
            y = (screenDim.height - imgHeight) / 2;
        } else {
            Rectangle frameBounds = parent.getBounds();
            Point location = parent.getLocationOnScreen();
            x = location.x + (frameBounds.width - imgWidth) / 2;
            y = location.y + (frameBounds.height - imgHeight) / 2;
        }
        setLocation(x, y);
        Robot robot;
        try {
            robot = new Robot();
            bkImage = robot.createScreenCapture(new Rectangle(x, y, imgWidth, imgHeight));
        } catch (AWTException e) {
            logger.debug("Unable to create screen capture for transparent window", e);
        } catch (NegativeArraySizeException e) {
            logger.debug("Unable to create screen capture for transparent window on two-monitor configurations");
        } catch (Exception e) {
            logger.debug("Unable to create screen capture for transparent window for some reason", e);
        }
    }

    /**
    * Invokes the main method of the real application class.
    *
    * @param className a <code>String</code> representing the name of the application class.
    * @param args      a <code>String[]</code> representing the runtime arguments.
    */
    public static void invokeMain(String className, String[] args) {
        try {
            Class.forName(className).getMethod("main", new Class[] { String[].class }).invoke(null, new Object[] { args });
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal("Cannot start the application", e);
        }
    }

    /**
    * Opens the splash screen.
    *
    * @param imageURL an <code>URL</code> to the splash image.
    */
    public static XSplashWindow splash(URL imageURL) {
        return splashOnFrame(imageURL, null);
    }

    public static XSplashWindow splashOnFrame(URL imageURL, Frame f) {
        XSplashWindow instance = null;
        if (imageURL != null) {
            Image image = Toolkit.getDefaultToolkit().createImage(imageURL);
            if (image != null) {
                if (f == null) {
                    f = new Frame();
                    instance = new XSplashWindow(f, image, true, true, true);
                } else {
                    instance = new XSplashWindow(f, image, false, false, false);
                }
                instance.setVisible(true);
            }
        }
        return instance;
    }

    /**
    * Closes the splash window.
    */
    public void disposeSplash() {
        this.dispose();
    }

    /**
    * @see Window#update(java.awt.Graphics)
    */
    public void update(Graphics g) {
        if (dbImage == null) {
            dbImage = createImage(this.getSize().width, this.getSize().height);
            dbg = dbImage.getGraphics();
        }
        if (bkImage != null) {
            dbg.drawImage(bkImage, 0, 0, this);
        } else {
            dbg.setColor(getBackground());
            dbg.fillRect(0, 0, this.getSize().width, this.getSize().height);
        }
        dbg.setColor(getForeground());
        paint(dbg);
        g.drawImage(dbImage, 0, 0, this);
    }

    /**
    * @see Window#paint(java.awt.Graphics)
    */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    public void dispose() {
        super.dispose();
        if (dbg != null) {
            dbg.dispose();
        }
    }
}
