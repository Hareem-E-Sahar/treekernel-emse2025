package org.pushingpixels.substance.internal.contrib.randelshofer.quaqua.colorchooser;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.security.AccessControlException;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.IconUIResource;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.internal.utils.SubstanceImageCreator;
import org.pushingpixels.substance.internal.utils.icon.TransitionAwareIcon;

/**
 * ColorPicker.
 * 
 * @author Werner Randelshofer
 * @version 1.1 2006-03-06 Abort picker when the user presses the Escape-Key. <br>
 *          1.0 December 18, 2005 Created.
 */
public class ColorPicker extends AbstractColorChooserPanel {

    /**
	 * This frame is constantly moved to the current location of the mouse. This
	 * ensures that we can trap mouse clicks while the picker cursor is showing.
	 * Also, by tying the picker cursor to this frame, we ensure that the picker
	 * cursor (the magnifying glass) is shown.
	 */
    private Dialog pickerFrame;

    private Timer pickerTimer;

    /**
	 * Holds the image of the picker cursor.
	 */
    private BufferedImage cursorImage;

    /**
	 * Graphics object used for drawing on the cursorImage.
	 */
    private Graphics2D cursorGraphics;

    /**
	 * The picker cursor.
	 */
    private Cursor pickerCursor;

    /**
	 * The hot spot of the cursor.
	 */
    private Point hotSpot;

    /**
	 * Offset from the hot spot of the pickerCursor to the pixel that we want to
	 * pick. We can't pick the color at the hotSpot of the cursor, because this
	 * point is obscured by the pickerFrame.
	 */
    private Point pickOffset;

    /**
	 * The magnifying glass image.
	 */
    private BufferedImage magnifierImage;

    /**
	 * The robot is used for creating screen captures.
	 */
    private Robot robot;

    private Color previousColor = Color.white;

    private Point previousLoc = new Point();

    private Point pickLoc = new Point();

    private Point captureOffset = new Point();

    private Rectangle captureRect;

    private static final Color transparentColor = new Color(0, true);

    private Rectangle zoomRect;

    private Rectangle glassRect;

    /**
	 * Creates a new instance.
	 */
    public ColorPicker() {
        try {
            robot = new Robot();
            robot.createScreenCapture(new Rectangle(0, 0, 1, 1));
        } catch (AWTException e) {
            throw new AccessControlException("Unable to capture screen");
        }
    }

    /**
	 * Gets the picker frame. If the frame does not yet exist, it is created
	 * along with all the other objects that are needed to make the picker work.
	 */
    private Dialog getPickerFrame() {
        if (pickerFrame == null) {
            Window owner = SwingUtilities.getWindowAncestor(this);
            if (owner instanceof Dialog) {
                pickerFrame = new Dialog((Dialog) owner);
            } else if (owner instanceof Frame) {
                pickerFrame = new Dialog((Frame) owner);
            } else {
                pickerFrame = new Dialog(new JFrame());
            }
            pickerFrame.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent evt) {
                    pickFinish();
                }

                @Override
                public void mouseExited(MouseEvent evt) {
                    updatePicker();
                }
            });
            pickerFrame.addMouseMotionListener(new MouseMotionAdapter() {

                @Override
                public void mouseMoved(MouseEvent evt) {
                    updatePicker();
                }
            });
            pickerFrame.setSize(3, 3);
            pickerFrame.setUndecorated(true);
            pickerFrame.setAlwaysOnTop(true);
            pickerFrame.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            pickCancel();
                            break;
                        case KeyEvent.VK_ENTER:
                            pickFinish();
                            break;
                    }
                }
            });
            magnifierImage = (BufferedImage) UIManager.get("ColorChooser.colorPickerMagnifier");
            glassRect = (Rectangle) UIManager.get("ColorChooser.colorPickerGlassRect");
            zoomRect = (Rectangle) UIManager.get("ColorChooser.colorPickerZoomRect");
            hotSpot = (Point) UIManager.get("ColorChooser.colorPickerHotSpot");
            captureRect = new Rectangle((Rectangle) UIManager.get("ColorChooser.colorPickerCaptureRect"));
            pickOffset = (Point) UIManager.get("ColorChooser.colorPickerPickOffset");
            captureOffset = new Point(captureRect.x, captureRect.y);
            cursorImage = getGraphicsConfiguration().createCompatibleImage(magnifierImage.getWidth(), magnifierImage.getHeight(), Transparency.TRANSLUCENT);
            cursorGraphics = cursorImage.createGraphics();
            cursorGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            pickerTimer = new Timer(5, new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    updatePicker();
                }
            });
        }
        return pickerFrame;
    }

    /**
	 * Updates the color picker.
	 */
    protected void updatePicker() {
        if (pickerFrame != null && pickerFrame.isShowing()) {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point mouseLoc = info.getLocation();
            pickerFrame.setLocation(mouseLoc.x - pickerFrame.getWidth() / 2, mouseLoc.y - pickerFrame.getHeight() / 2);
            pickLoc.x = mouseLoc.x + pickOffset.x;
            pickLoc.y = mouseLoc.y + pickOffset.y;
            if (pickLoc.x >= 0 && pickLoc.y >= 0) {
                Color c = robot.getPixelColor(pickLoc.x, pickLoc.y);
                if (!c.equals(previousColor) || !mouseLoc.equals(previousLoc)) {
                    previousColor = c;
                    previousLoc = mouseLoc;
                    captureRect.setLocation(mouseLoc.x + captureOffset.x, mouseLoc.y + captureOffset.y);
                    if (captureRect.x >= 0 && captureRect.y >= 0) {
                        BufferedImage capture = robot.createScreenCapture(captureRect);
                        cursorGraphics.setComposite(AlphaComposite.Src);
                        cursorGraphics.setColor(transparentColor);
                        cursorGraphics.fillRect(0, 0, cursorImage.getWidth(), cursorImage.getHeight());
                        cursorGraphics.setColor(Color.red);
                        cursorGraphics.fillOval(glassRect.x, glassRect.y, glassRect.width, glassRect.height);
                        cursorGraphics.setComposite(AlphaComposite.SrcIn);
                        cursorGraphics.drawImage(capture, zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height, this);
                        cursorGraphics.setComposite(AlphaComposite.SrcOver);
                        cursorGraphics.drawImage(magnifierImage, 0, 0, this);
                        BufferedImage subImage = cursorImage.getSubimage(0, 0, cursorImage.getWidth(), cursorImage.getHeight());
                        pickerFrame.setCursor(getToolkit().createCustomCursor(cursorImage, hotSpot, "ColorPicker"));
                    }
                }
            }
        }
    }

    /**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    private void initComponents() {
        pickerButton = new javax.swing.JButton();
        setLayout(new java.awt.BorderLayout());
        pickerButton.setBorderPainted(false);
        pickerButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pickerButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pickBegin(evt);
            }
        });
        add(pickerButton, java.awt.BorderLayout.CENTER);
    }

    private void pickBegin(java.awt.event.ActionEvent evt) {
        getPickerFrame();
        pickerTimer.start();
        getPickerFrame().setVisible(true);
    }

    protected void pickFinish() {
        pickerTimer.stop();
        pickerFrame.setVisible(false);
        PointerInfo info = MouseInfo.getPointerInfo();
        Point loc = info.getLocation();
        Color c = robot.getPixelColor(loc.x + pickOffset.x, loc.y + pickOffset.y);
        getColorSelectionModel().setSelectedColor(c);
    }

    protected void pickCancel() {
        pickerTimer.stop();
        pickerFrame.setVisible(false);
    }

    @Override
    protected void buildChooser() {
        initComponents();
        pickerButton.setIcon(new TransitionAwareIcon(pickerButton, new TransitionAwareIcon.Delegate() {

            @Override
            public Icon getColorSchemeIcon(SubstanceColorScheme scheme) {
                return new IconUIResource(SubstanceImageCreator.getSearchIcon(15, scheme, pickerButton.getComponentOrientation().isLeftToRight()));
            }
        }, "ColorChooser.colorPickerIcon"));
    }

    @Override
    public String getDisplayName() {
        return "Color Picker";
    }

    @Override
    public Icon getLargeDisplayIcon() {
        return UIManager.getIcon("ColorChooser.colorPickerIcon");
    }

    @Override
    public Icon getSmallDisplayIcon() {
        return getLargeDisplayIcon();
    }

    @Override
    public void updateChooser() {
    }

    private javax.swing.JButton pickerButton;
}
