package imi.gui;

import com.sun.awt.AWTUtilities;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javolution.util.FastMap;

/**
 *
 * @author skendall
 */
public class CaptureFrame extends JFrame {

    /** An object related to this capture frame **/
    private Object key = null;

    private final JPanel mainPanel;

    private int frameTopHeight = 60;

    private Point frameTopPos = new Point();

    private Point frameTopSize = new Point();

    private JLabel panelSizeLabel;

    private int fontSize = 10;

    private Font mainFont = new Font("SansSerif", Font.BOLD, fontSize);

    private Robot robot;

    private boolean capture = false;

    private final FrameCaptureListener listener;

    private float normalOpacity = 0.5f;

    public interface FrameCaptureListener {

        public void onFrameCapture(BufferedImage captureImage, Object key);
    }

    public CaptureFrame(final FrameCaptureListener listener) {
        this.listener = listener;
        this.setTitle("SharedSpace Grabber" + System.getProperty("java.runtime.version"));
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                stopCapture();
            }
        });
        AWTUtilities.setWindowOpacity(this, normalOpacity);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Thread.dumpStack();
        }
        int defaultWidth = 500;
        Color frameColor = new Color(0, 0, 0, 5);
        getContentPane().setLayout(new GridBagLayout());
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GUIConstraints.constrain(getContentPane(), mainPanel, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 0, 0);
        panelSizeLabel = new JLabel("WERTYUI");
        panelSizeLabel.setFont(mainFont);
        panelSizeLabel.setForeground(Color.WHITE);
        final JPanel capturePanel = new JPanel() {

            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                int w = this.getWidth();
                int h = this.getHeight();
                Color color1 = new Color(50, 50, 50, 0);
                Color color2 = new Color(200, 200, 200, 0);
                GradientPaint gp = new GradientPaint(0, 0, color2, 0, h, color1);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                super.paint(g);
                g2d.setStroke(new BasicStroke(2f));
                g2d.setColor(Color.RED);
                g2d.drawRect(0, 0, w - 1, h - 1);
                panelSizeLabel.setText(w + " x " + h);
            }
        };
        capturePanel.setPreferredSize(new Dimension(480, 320));
        GUIConstraints.constrain(mainPanel, capturePanel, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 0, 0, 0, 0);
        frameTopSize.setLocation(defaultWidth, frameTopHeight);
        setBackground(frameColor);
        getContentPane().setBackground(frameColor);
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GUIConstraints.constrain(mainPanel, controlPanel, 0, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTHWEST, 1.0, 0.0, 0, 0, 0, 0);
        controlPanel.setBackground(Color.GRAY);
        GUIConstraints.constrain(controlPanel, panelSizeLabel, 0, 0, 1, 1, GridBagConstraints.VERTICAL, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 0, 0);
        JButton captureButton = new JButton("Snapshot");
        captureButton.setBackground(Color.GREEN);
        GUIConstraints.constrain(controlPanel, captureButton, 1, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 0);
        captureButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                takeSnapShot();
            }
        });
        captureButton = new JButton("Start Capture");
        captureButton.setBackground(Color.GREEN);
        GUIConstraints.constrain(controlPanel, captureButton, 2, 0, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1.0, 0.0, 0, 0, 0, 0);
        captureButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                if (capture) capture = false; else {
                    ((JButton) e.getSource()).setText("Stop Capture");
                    System.out.println("Capture starts");
                    capture = true;
                    Thread captureThread = new Thread(new Runnable() {

                        public void run() {
                            while (capture) {
                                takeSnapShot();
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(CaptureFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            System.out.println("Capture stopped");
                            ((JButton) e.getSource()).setText("Start Capture");
                        }
                    });
                    captureThread.start();
                }
            }
        });
        setAlwaysOnTop(true);
        pack();
        this.setLocationByPlatform(true);
        setVisible(true);
    }

    public void stopCapture() {
        capture = false;
    }

    public void takeSnapShot() {
        Point origin = mainPanel.getLocationOnScreen();
        final Rectangle rect = new Rectangle(origin.x, origin.y, mainPanel.getWidth(), mainPanel.getHeight());
        while (AWTUtilities.getWindowOpacity(this) != 0.0f) {
            try {
                AWTUtilities.setWindowOpacity(this, 0.0f);
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(CaptureFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        BufferedImage capI = robot.createScreenCapture(rect);
        AWTUtilities.setWindowOpacity(this, normalOpacity);
        if (listener != null) listener.onFrameCapture(capI, key);
    }

    /** An object related to this capture frame **/
    public Object getKey() {
        return key;
    }

    /** An object related to this capture frame **/
    public void setKey(Object key) {
        this.key = key;
    }

    public static void main(String[] args) {
        CaptureFrame cap = new CaptureFrame(null);
    }

    private static Map<Object, CaptureFrame> captureFrames = null;

    public static CaptureFrame getCaptureFrame(Object key, FrameCaptureListener listener) {
        if (captureFrames == null) {
            captureFrames = new FastMap<Object, CaptureFrame>();
        }
        CaptureFrame capture = captureFrames.get(key);
        if (capture == null) {
            capture = new CaptureFrame(listener);
            capture.setKey(key);
            captureFrames.put(key, capture);
        }
        return capture;
    }

    public static void clearAllCaptureFrames() {
        if (captureFrames != null) captureFrames.clear();
    }
}
