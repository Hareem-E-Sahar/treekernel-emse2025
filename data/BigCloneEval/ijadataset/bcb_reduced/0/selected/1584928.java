package uk.org.toot.swingui.miscui;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.UIManager;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.Rectangle;
import java.io.IOException;
import java.awt.Robot;
import java.awt.Point;
import java.awt.AWTException;
import javax.imageio.ImageIO;

public class SwingApplication {

    private final String basename;

    private final String fullname;

    private static Camera camera = new Camera();

    public SwingApplication(final String aBasename, final String aFullname) {
        basename = aBasename;
        fullname = aFullname;
        setLookAndFeel();
        UIManager.put("TabbedPane.selectedForeground", new Color(15, 50, 125));
    }

    public void setLookAndFeel() {
        setNativeLookAndFeel();
    }

    public void setContent(final Container content) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(content, basename + " - " + fullname);
            }
        });
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI(Container content, String title) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = createFrame(content, title);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                close();
            }
        });
    }

    public static JFrame createFrame(Container content, String title) {
        return createFrame(content, title, createFrameImage());
    }

    public static JFrame createFrame(final Container content, String title, Image image) {
        final JFrame frame = new JFrame(title);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                unbindKeys(content);
                frame.setIconImage(null);
                frame.removeWindowListener(this);
                frame.getContentPane().removeAll();
            }
        });
        frame.getContentPane().add(content);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(image);
        frame.pack();
        frame.setVisible(true);
        bindKeys(content);
        return frame;
    }

    protected static void bindKeys(Component comp) {
        JComponent jcomp;
        if (comp instanceof JComponent) {
            jcomp = (JComponent) comp;
            jcomp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(SHOOT_KEYS), SHOOT);
            jcomp.getActionMap().put(SHOOT, new ShootAction(jcomp));
        }
    }

    protected static void unbindKeys(Component comp) {
        JComponent jcomp;
        if (comp instanceof JComponent) {
            jcomp = (JComponent) comp;
            jcomp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(SHOOT_KEYS));
            jcomp.getActionMap().remove(SHOOT);
        }
    }

    private static final String SHOOT_KEYS = "control alt S";

    private static final String SHOOT = "shoot";

    private static class ShootAction extends AbstractAction {

        private Component subject;

        public ShootAction(JComponent sub) {
            subject = sub.getTopLevelAncestor();
            if (subject == null) {
                subject = sub;
            }
        }

        public void actionPerformed(ActionEvent e) {
            camera.shootComponent(subject);
        }
    }

    protected static Image createFrameImage() {
        SquareImage img = new SquareImage(16);
        Graphics g = img.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 15, 15);
        g.setColor(Color.RED);
        g.fillOval(5, 3, 6, 6);
        g.dispose();
        return img;
    }

    public Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    protected void close() {
        System.exit(0);
    }

    /** Tell system to use native look and feel, as in previous
   *  releases. Metal (Java) LAF is the default otherwise.
   */
    public static void setNativeLookAndFeel() {
        try {
            String syslaf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(syslaf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setJavaLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMotifLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SquareImage extends BufferedImage {

        public SquareImage() {
            this(24);
        }

        public SquareImage(int size) {
            super(size, size, BufferedImage.TYPE_INT_RGB);
        }
    }

    public static class Camera {

        private int shot = 0;

        private File path = new File(new File(System.getProperty("user.home"), "toot"), "screenshots");

        public void shoot(Rectangle screenRect) {
            try {
                Robot robot = new Robot();
                BufferedImage image = robot.createScreenCapture(screenRect);
                String outFileName = "shot" + (++shot) + ".png";
                File file = new File(path, outFileName);
                file.mkdirs();
                ImageIO.write(image, "png", file);
                System.out.println(file.getPath());
            } catch (AWTException ae) {
            } catch (IOException ioe) {
            }
        }

        public void shootScreen() {
            shoot(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        }

        public void shootComponent(Component component) {
            Point loc = component.getLocationOnScreen();
            Dimension size = component.getSize();
            shoot(new Rectangle(loc.x, loc.y, size.width, size.height));
        }
    }
}
