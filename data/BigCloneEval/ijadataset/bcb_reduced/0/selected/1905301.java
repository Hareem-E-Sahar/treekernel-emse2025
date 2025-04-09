package org.formaria.swing.splash;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.formaria.aria.build.BuildProperties;

/**
 * A Splash window.
 *  <p>
 * Usage: MyApplication is your application class. Create a Splasher class which
 * opens the splash window, invokes the main method of your Application class,
 * and disposes the splash window afterwards.
 * Please note that we want to keep the Splasher class and the SplashWindow class
 * as small as possible. The less code and the less classes must be loaded into
 * the JVM to open the splash screen, the faster it will appear.
 * <pre>
 * class Splasher {
 *    public static void main(String[] args) {
 *         SplashWindow.splash(Startup.class.getResource("splash.gif"));
 *         MyApplication.main(args);
 *         SplashWindow.disposeSplash();
 *    }
 * }
 * </pre>
 * 
 * 
 * @author Werner Randelshofer
 * @version 2.2.1 2008 Abort when splash image can not be loaded.
 * @see http://www.randelshofer.ch/oop/javasplash/javasplash.html
 */
public class SplashWindow extends Window {

    /**
   * The current instance of the splash window.
   * (Singleton design pattern).
   */
    private static SplashWindow instance;

    /**
   * The splash image which is displayed on the splash window.
   */
    private Image image;

    /**
   * This attribute indicates whether the method
   * paint(Graphics) has been called at least once since the
   * construction of this window.<br>
   * This attribute is used to notify method splash(Image)
   * that the window has been drawn at least once
   * by the AWT event dispatcher thread.<br>
   * This attribute acts like a latch. Once set to true,
   * it will never be changed back to false again.
   *
   * @see #paint
   * @see #splash
   */
    private boolean paintCalled = false;

    private int x, y, imgWidth, imgHeight;

    private BufferedImage screenShot;

    /**
   * Creates a new instance.
   * @param parent the parent of the window.
   * @param image the splash image.
   */
    private SplashWindow(Frame parent, Image image) {
        super(parent);
        this.image = image;
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ie) {
        }
        if (mt.isErrorID(0)) {
            setSize(0, 0);
            System.err.println("Warning: SplashWindow couldn't load splash image.");
            synchronized (this) {
                paintCalled = true;
                notifyAll();
            }
            return;
        }
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        imgWidth = image.getWidth(this);
        imgHeight = image.getHeight(this);
        x = (screenDim.width - imgWidth) / 2;
        y = (screenDim.height - imgHeight) / 2;
        captureScreenShot();
        setSize(imgWidth, imgHeight);
        setLocation(x, y);
        MouseAdapter disposeOnClick = new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                synchronized (SplashWindow.this) {
                    SplashWindow.this.paintCalled = true;
                    SplashWindow.this.notifyAll();
                }
                dispose();
            }
        };
        addMouseListener(disposeOnClick);
        addWindowListener(new WindowAdapter() {

            public void windowDeactivated(WindowEvent we) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        captureScreenShot();
                        SplashWindow.this.toFront();
                    }
                });
            }
        });
    }

    public Graphics getGraphics() {
        return super.getGraphics();
    }

    /**
   * Updates the display area of the window.
   */
    public void update(Graphics g) {
        paint(g);
    }

    /**
   * Paints the image on the window.
   */
    public void paint(Graphics g) {
        if (screenShot != null) g.drawImage(screenShot, 0, 0, this);
        g.drawImage(image, 0, 0, this);
        if (!paintCalled) {
            paintCalled = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
   * Open's a splash window using the specified image.
   * @param image The splash image.
   */
    public static void splash(Image image) {
        if (instance == null && image != null) {
            Frame f = new Frame();
            instance = new SplashWindow(f, image);
            instance.show();
            if (!EventQueue.isDispatchThread() && Runtime.getRuntime().availableProcessors() == 1) {
                synchronized (instance) {
                    while (!instance.paintCalled) {
                        try {
                            instance.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    /**
   * Open's a splash window using the specified image.
   * @param imageURL The url of the splash image.
   */
    public static void splash(URL imageURL) {
        if (imageURL != null) {
            splash(Toolkit.getDefaultToolkit().createImage(imageURL));
        }
    }

    /**
   * Closes the splash window.
   * @param delay the dealy in milliseconds
   */
    public static void disposeSplash(int delay) {
        if (instance != null) {
            instance.repaint();
            ActionListener taskPerformer = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    instance.getOwner().dispose();
                    instance = null;
                }
            };
            Timer timer = new Timer(delay, taskPerformer);
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
   * Invokes the main method of the provided class name.
   * @param args the command line arguments
   */
    public static void invokeMain(String className, String[] args) {
        try {
            Class.forName(className.trim()).getMethod("main", new Class[] { String[].class }).invoke(null, new Object[] { args });
        } catch (Exception e) {
            if (BuildProperties.DEBUG) e.printStackTrace();
            InternalError error = new InternalError("Failed to invoke main method");
            error.initCause(e);
            throw error;
        }
    }

    /**
   * Make sure the splash screen is always on top
   */
    public void setAlwaysOnTop() {
        try {
            Class params[] = new Class[1];
            params[0] = boolean.class;
            Object target = this;
            Method method = target.getClass().getMethod("setAlwaysOnTop", params);
            if (method != null) {
                Object args[] = new Object[1];
                args[0] = Boolean.TRUE;
                if (method != null) method.invoke(target, args);
            }
        } catch (Exception ex) {
            if (BuildProperties.DEBUG) {
                Throwable t = ex.getCause();
                if (t != null) t.printStackTrace(); else ex.printStackTrace();
            }
        }
    }

    private void captureScreenShot() {
        try {
            setSize(0, 0);
            Robot robot = new Robot();
            Rectangle rect = new Rectangle(x, y, imgWidth, imgHeight);
            screenShot = robot.createScreenCapture(rect);
            setSize(imgWidth, imgHeight);
            repaint();
        } catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
    }

    /**
   * Shows the splash screen, launches the application and then disposes
   * the splash screen.
   * @param args the command line arguments
   */
    public static void main(String[] args) {
        SplashWindow.splash(SplashWindow.class.getResource("/splash.gif"));
        if (args.length == 0) SplashWindow.invokeMain("org.formaria.swing.Applet", args); else {
            String[] appArgs = new String[args.length - 1];
            for (int i = 1; i < args.length; i++) appArgs[i - 1] = args[i];
            SplashWindow.invokeMain(args[0], appArgs);
        }
        SplashWindow.disposeSplash(8000);
    }
}
