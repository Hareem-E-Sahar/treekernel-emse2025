package clavicom.gui.splashscreen;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JWindow;
import clavicom.core.message.CMessageEngine;
import clavicom.tools.TSwingUtils;

public class UISplashScreen extends JWindow implements Runnable {

    private final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 10);

    private final Color LABEL_COLOR = new Color(210, 210, 210);

    private final int LABEL_POS_X = 303;

    private final int LABEL_POS_Y = 229;

    private String imageFile;

    private BufferedImage bufImage;

    private Rectangle rect;

    private boolean isAlive;

    private String currentAction;

    private int waitTime;

    private long startTime, stopTime;

    private long minimumTime;

    private Cursor startCursor;

    /**
	 *  Construit et affiche le splash screen
	 *  
	 */
    public UISplashScreen(String imageFile, int waitTime, int minimumTime) {
        this.imageFile = imageFile;
        this.waitTime = waitTime;
        this.minimumTime = minimumTime;
        startTime = System.currentTimeMillis();
        startCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        run();
    }

    /**
	 *  Initialise le thread
	 */
    public void run() {
        isAlive = true;
        currentAction = "";
        Image image = TSwingUtils.getImage(imageFile).getImage();
        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        if (imageWidth > 0 && imageHeight > 0) {
            int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            rect = new Rectangle((screenWidth - imageWidth) / 2, (screenHeight - imageHeight) / 2, imageWidth, imageHeight);
            try {
                bufImage = new Robot().createScreenCapture(rect);
            } catch (AWTException e) {
                CMessageEngine.newFatalError("Can not render splashscreen", "Capture failed !");
            }
            Graphics2D g2D = bufImage.createGraphics();
            g2D.drawImage(image, 0, 0, this);
            setBounds(rect);
            setVisible(true);
        } else {
            CMessageEngine.newFatalError("Can not render splashscreen", "File " + imageFile + " was not found or is not an image file.");
        }
        isAlive = false;
    }

    /**
	 * Masque et détruit le splash screen
	 * 
	 * @throws IllegalStateException
	 */
    public void close() {
        if (!isAlive) {
            stopTime = System.currentTimeMillis();
            long diff = stopTime - startTime;
            if (diff < minimumTime) {
                try {
                    Thread.sleep(diff);
                } catch (Exception e) {
                    System.out.println("Sleep raté...");
                }
            }
            dispose();
        } else {
            System.out.println("Splash string détruit mais mal initialisé...");
        }
        setCursor(startCursor);
    }

    /**
	 *  Redefinie la méthode de JWindow
	 *
	 */
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(bufImage, 0, 0, this);
        g2D.setFont(LABEL_FONT);
        g2D.setColor(LABEL_COLOR);
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2D.drawString(currentAction, LABEL_POS_X, LABEL_POS_Y);
    }

    /**
	 * Met à jour la chaîne de chargement
	 * @param message
	 */
    public void newStep(String message) {
        currentAction = message;
        repaint();
        try {
            Thread.sleep(waitTime);
        } catch (Exception e) {
            System.out.println("Sleep raté...");
        }
    }
}
