package freestyleLearningGroup.independent.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Vector;
import javax.swing.*;
import com.sun.image.codec.jpeg.*;

public class FLGUIUtilities {

    public static boolean traceMode = true;

    public static Color BASE_COLOR1 = new Color(240, 241, 245);

    public static Color BASE_COLOR2 = new Color(214, 216, 224);

    public static Color BASE_COLOR3 = new Color(193, 196, 210);

    public static Color BASE_COLOR4 = new Color(172, 171, 190);

    public static Color DEFAULT_HTML_BACKGROUND_COLOR = new Color(255, 255, 255);

    public static Color RED = new Color(200, 0, 0);

    public static Color GREEN = new Color(0, 222, 0);

    public static Color BLUE = new Color(0, 0, 64);

    public static Color SHADE_BLUE_DARK = new Color(34, 34, 112);

    public static Color SHADE_BLUE_LIGHT = new Color(122, 122, 196);

    private static Vector configurationListeners = new Vector();

    private static JFrame mainFrame;

    private static JFrame fullScreenFrame;

    private static JFrame fullScreenWindow;

    private static JPanel contextDependentInteractionPanel;

    /**
     * This method returns the fullscreen-window. Is needed for opening the CDI-Panel in Fullscreen-Mode.
     * @return <code>JFrame<code> fullScreenWindow
     */
    public static JFrame getFullScreenWindow() {
        return fullScreenWindow;
    }

    /**
     * This method sets the fullscreen-window.
     * @param <code>JFrame</code> the frame of the fullscreen-window
     */
    public static void setFullScreenWindow(JFrame frame) {
        fullScreenWindow = frame;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }

    public static void setMainFrame(JFrame frame) {
        mainFrame = frame;
    }

    public static void setFullScreenFrame(JFrame frame) {
        fullScreenFrame = frame;
    }

    public static JFrame getFullScreenFrame() {
        return fullScreenFrame;
    }

    public static void startLongLastingOperation() {
        if (null != mainFrame) {
            mainFrame.getGlassPane().setVisible(true);
            mainFrame.getGlassPane().addMouseListener(new MouseAdapter() {
            });
            mainFrame.getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }
    }

    public static void startLongLastingOperation(Object c) {
        startLongLastingOperation();
        if (c instanceof RootPaneContainer) {
            ((RootPaneContainer) c).getGlassPane().setVisible(true);
            ((RootPaneContainer) c).getGlassPane().addMouseListener(new MouseAdapter() {
            });
            ((RootPaneContainer) c).getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }
    }

    public static void stopLongLastingOperation() {
        if (null != mainFrame) {
            mainFrame.getGlassPane().setVisible(false);
        }
    }

    public static void stopLongLastingOperation(Object c) {
        stopLongLastingOperation();
        if (c instanceof RootPaneContainer) {
            ((RootPaneContainer) c).getGlassPane().setVisible(false);
        }
    }

    public static void trace(Object obj, String s) {
        if (traceMode) {
            System.out.println(s);
            if (obj != null) System.out.println("\t[invoked by] " + obj.getClass().getName());
        }
    }

    public static ImageIcon getScaledIcon(Image image, int toWidth, int toHeight, int hints, boolean maintainAspectRatio) {
        int width = toWidth;
        int height = toHeight;
        if (maintainAspectRatio) {
            int imageWidth = image.getWidth(new ImageObserver() {

                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    return false;
                }
            });
            int imageHeight = image.getHeight(new ImageObserver() {

                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    return false;
                }
            });
            double scaleFactor = 1;
            if (imageWidth >= imageHeight) {
                scaleFactor = (double) toWidth / imageWidth;
            } else {
                scaleFactor = (double) toHeight / imageHeight;
            }
            width = (int) (imageWidth * scaleFactor);
            height = (int) (imageHeight * scaleFactor);
        }
        return new ImageIcon(image.getScaledInstance(width, height, hints));
    }

    public static BufferedImage captureScreen() {
        BufferedImage screenShotImage = null;
        try {
            Robot robot = new Robot();
            Rectangle mainFrameRectangle = mainFrame.getBounds();
            screenShotImage = robot.createScreenCapture(mainFrameRectangle);
        } catch (Exception e) {
            System.out.println("Exception creating screenshot: " + e.getMessage());
        }
        return screenShotImage;
    }

    public static Image createScaledImage(String imageAbsolutePath, int maxExtension) {
        Image newImage = new ImageIcon(imageAbsolutePath).getImage();
        ImageObserver observer = new ImageObserver() {

            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };
        int height = newImage.getHeight(observer);
        int width = newImage.getWidth(observer);
        double scaleFactor = (double) maxExtension / Math.max(height, width);
        return newImage.getScaledInstance((int) (width * scaleFactor), (int) (height * scaleFactor), Image.SCALE_SMOOTH);
    }

    public static void main(String[] args) {
        FLGUIUtilities u = new FLGUIUtilities();
        ImageIcon icon = new ImageIcon(u.getClass().getClassLoader().getResource("freestyleLearningGroup/independent/tourCreator/images/tourCreatorIcon.gif"));
        ImageIcon icon2 = new ImageIcon(u.getClass().getClassLoader().getResource("freestyleLearningGroup/independent/tourCreator/images/tourCreatorIcon.gif"));
        JFrame f = new JFrame();
        f.setSize(400, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new JLabel(icon));
        f.getContentPane().add(new JLabel(icon2));
        f.setVisible(true);
    }

    public static BufferedImage createBufferedImage(Image image) {
        ImageObserver observer = new ImageObserver() {

            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                return false;
            }
        };
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(observer), image.getHeight(observer), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        try {
            g2.drawImage(image, 0, 0, observer);
            System.out.println("done.");
        } catch (Exception e) {
            FLGOptionPane.showMessageDialog("Error creating Buffered Image: " + e, "Exeption occurred", FLGOptionPane.ERROR_MESSAGE);
        }
        return bufferedImage;
    }

    public static BufferedImage createBufferedImage(Image image, ImageObserver observer) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(observer), image.getHeight(observer), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, observer);
        return bufferedImage;
    }

    public static boolean saveImage(BufferedImage image, String absolutePath) {
        try {
            OutputStream os = new FileOutputStream(absolutePath);
            JPEGImageEncoder ie = JPEGCodec.createJPEGEncoder(os);
            JPEGEncodeParam param = ie.getDefaultJPEGEncodeParam(image);
            param.setQuality(1.0f, false);
            ie.setJPEGEncodeParam(param);
            ie.encode(image);
            os.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * This method returns a Color which is a mixture from two given colors.
     * The ratio of interpolation between color 1 and color 2 can also be set.
     * @param <code>color1</code> given color 1
     * @param <code>color2</code> given color 2
     * @param <code>ratio</code> ratio between 0 and 1 for interpolation
     */
    public static Color mixColors(Color color1, Color color2, double ratio) {
        int colValue = (int) Math.min(ratio * 255, 255);
        int differenz_rot = colValue * (color1.getRed() - color2.getRed()) / 255;
        int differenz_gruen = colValue * (color1.getGreen() - color2.getGreen()) / 255;
        int differenz_blau = colValue * (color1.getBlue() - color2.getBlue()) / 255;
        return new Color(color1.getRed() - differenz_rot, color1.getGreen() - differenz_gruen, color1.getBlue() - differenz_blau);
    }

    /**
     *  This class privides easy definition of file filters for swing filechooser dialogs.
     */
    public static class FLGFileFilter extends javax.swing.filechooser.FileFilter {

        String description;

        String[] extensions;

        /**
     *  @param <code>extension</code> array of allowed file extensions (including ".") 
     *  @param <code>description</code> verbal description of file type
     */
        public FLGFileFilter(String[] extensions, String description) {
            this.description = description;
            this.extensions = extensions;
        }

        public boolean accept(java.io.File file) {
            if (file.isDirectory()) return true;
            if (file.getName().lastIndexOf(".") > 0) {
                String extension = file.getName().substring(file.getName().lastIndexOf("."));
                for (int i = 0; i < extensions.length; i++) {
                    if (extension.equalsIgnoreCase(extensions[i])) return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return description;
        }
    }
}
