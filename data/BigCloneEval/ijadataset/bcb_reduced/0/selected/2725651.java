package gui.eyedropper;

import gui.Studio;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Show an eyedropper to select a color on the screen.
 * 
 * @author Desprez Jean-Marc
 * 
 */
public class EyeDropper extends JFrame {

    private static final long serialVersionUID = 5351709043748565148L;

    private static final int MIN_ZOOM = 2;

    private static final int MAX_ZOOM = 32;

    private JPanel imagePanel;

    private Image image;

    private Robot robot;

    private Timer t;

    private int width = 382;

    private int height = width;

    private int zoom = 2;

    private int halfWidth = (width >> 1);

    private int halfHeight = (height >> 1);

    private int halfZoom = (zoom >> 1);

    private int roundSize = 10 * (zoom / MIN_ZOOM);

    private int arcSize = roundSize >> 1;

    private int halfRoundSize = (arcSize);

    private Studio studio;

    /**
   * The main color of the given studio will be set to the selected color.
   * 
   * @param studio
   *          the studio to update.
   */
    public EyeDropper(final Studio studio) {
        super("EyeDropper");
        this.studio = studio;
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(400, 400));
        try {
            robot = new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
        imagePanel = new JPanel() {

            private static final long serialVersionUID = 7793240458833917848L;

            public void paintComponent(final Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
                g.drawRoundRect(halfWidth - halfRoundSize, halfHeight - halfRoundSize, roundSize, roundSize, arcSize, arcSize);
                g.drawRect(halfWidth - halfZoom, halfWidth - halfZoom, zoom, zoom);
            }
        };
        add(imagePanel);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                char c = e.getKeyChar();
                switch(c) {
                    case '+':
                        if (zoom < MAX_ZOOM) {
                            zoom *= 2;
                        }
                        break;
                    case '-':
                        if (zoom > MIN_ZOOM) {
                            zoom /= 2;
                        }
                        break;
                    default:
                        if (t.isRunning()) {
                            t.stop();
                            Point p = MouseInfo.getPointerInfo().getLocation();
                            if (studio != null) {
                                studio.setMainColor(robot.getPixelColor(p.x, p.y));
                            }
                            dispose();
                        } else {
                            t.start();
                        }
                        break;
                }
                halfZoom = (zoom >> 1);
                roundSize = 10 * (zoom / MIN_ZOOM);
                arcSize = roundSize >> 1;
                halfRoundSize = (arcSize);
            }
        });
        t = new Timer(100, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                Point currentMousePos = MouseInfo.getPointerInfo().getLocation();
                image = robot.createScreenCapture(new Rectangle(currentMousePos.x - width / zoom / 2, currentMousePos.y - height / zoom / 2, width / zoom, height / zoom)).getScaledInstance(width, height, Image.SCALE_DEFAULT);
                repaint();
            }
        });
        t.start();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(final WindowEvent e) {
                if (studio != null) {
                    studio.setState(JFrame.NORMAL);
                }
            }
        });
        pack();
        setVisible(true);
    }
}
