package net.sourceforge.jscreengrabber.gui;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sourceforge.jscreengrabber.animations.MaximizeCompositeAnimator;
import net.sourceforge.jscreengrabber.animations.MinimizeCompositeAnimator;
import net.sourceforge.jscreengrabber.animations.RestoreCompositeAnimator;
import net.sourceforge.jscreengrabber.utils.GrabberGraphics;
import net.sourceforge.jscreengrabber.utils.Options;
import net.sourceforge.jscreengrabber.utils.OptionsEngine;
import net.sourceforge.jscreengrabber.utils.OptionsProcessor;

/**
 * This class is the panel that will be assigned as the content pane for the
 * grabber window.
 * 
 * @author Savvas Dalkitsis
 */
public class GrabberPanel extends JPanel {

    private static final long serialVersionUID = -7346558586046795959L;

    /**
	 * This reference is used to draw the grabber image. This will alternate
	 * between 2 images.
	 */
    private BufferedImage grabImg = null;

    /**
	 * This reference is used to draw the resize image. This will alternate
	 * between 2 images.
	 */
    private BufferedImage resizeImg = null;

    /**
	 * This will be true when the user clicks and drags on the window.
	 */
    private boolean drag = false;

    /**
	 * This field will hold the distance of the user click from the corner of
	 * the window. Used when resizing.
	 */
    private Point distanceFromCorner = null;

    /**
	 * This field will hold the last position of the mouse when moving the
	 * window.
	 */
    private Point last = null;

    /**
	 * This will be true when the user resizes the window.
	 */
    private boolean resize = false;

    /**
	 * This will hold the last position of the window. Used when maximizing in
	 * order to restore.
	 */
    private Point savedLocation = null;

    /**
	 * This will hold the last dimension of the window. Used when maximizing in
	 * order to restore.
	 */
    private Dimension savedDimension = null;

    /**
	 * The button used to close the program.
	 */
    private GrabberButton close = null;

    /**
	 * The button used to save the screenshot.
	 */
    private GrabberButton save = null;

    /**
	 * The last used directory to save an image.
	 */
    private String lastDir = null;

    /**
	 * Constructor to initialize the panel.
	 */
    public GrabberPanel() {
        grabImg = GrabberGraphics.grab;
        resizeImg = GrabberGraphics.resizeImage;
        setLayout(null);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                drag = true;
                distanceFromCorner = new Point(getWidth() - e.getX(), getHeight() - e.getY());
                last = e.getLocationOnScreen();
                resize = isInResize(e.getPoint()) && e.getButton() == MouseEvent.BUTTON1;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drag = false;
                distanceFromCorner = null;
                last = null;
                resize = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                grabImg = GrabberGraphics.grab;
                resizeImg = GrabberGraphics.resizeImage;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                grabImg = GrabberGraphics.grabA;
                resizeImg = GrabberGraphics.resizeImageA;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (savedLocation != null) {
                        new RestoreCompositeAnimator(savedLocation, savedDimension).start();
                        savedLocation = null;
                        savedDimension = null;
                    } else {
                        savedDimension = getSize();
                        savedLocation = getLocationOnScreen();
                        new MaximizeCompositeAnimator().start();
                    }
                }
            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isInResize(e.getPoint())) {
                    setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drag) {
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                    if (resize) {
                        if (Math.min(getWidth(), getHeight()) < 50) {
                            setCursor(Cursor.getDefaultCursor());
                            resizeImg = GrabberGraphics.resizeImageA;
                        } else setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                        int w = e.getXOnScreen() - GrabberWindow.getWindow().getX() + distanceFromCorner.x;
                        int h = e.getYOnScreen() - GrabberWindow.getWindow().getY() + distanceFromCorner.y;
                        w = (GrabberWindow.getWindow().getX() + w > screen.width) ? screen.width - GrabberWindow.getWindow().getX() : w;
                        h = (GrabberWindow.getWindow().getY() + h > screen.height) ? screen.height - GrabberWindow.getWindow().getY() : h;
                        GrabberWindow.getWindow().setSize(new Dimension(w, h));
                    } else {
                        int x = GrabberWindow.getWindow().getX() + (int) (e.getXOnScreen() - last.getX());
                        int y = GrabberWindow.getWindow().getY() + (int) (e.getYOnScreen() - last.getY());
                        x = (x < 0) ? 0 : x;
                        y = (y < 0) ? 0 : y;
                        x = (x + getWidth() > screen.width) ? screen.width - getWidth() : x;
                        y = (y + getHeight() > screen.height) ? screen.height - getHeight() : y;
                        GrabberWindow.getWindow().setLocation(x, y);
                        last = e.getLocationOnScreen();
                    }
                }
            }
        });
        close = new GrabberButton(GrabberGraphics.closeImg, GrabberGraphics.closeImgH);
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GrabberTray.setSavedLocation(GrabberWindow.getWindow().getLocation());
                GrabberTray.setSavedSize(GrabberWindow.getWindow().getSize());
                new MinimizeCompositeAnimator().start();
            }
        });
        save = new GrabberButton(GrabberGraphics.saveImg, GrabberGraphics.saveImgH);
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Robot r;
                try {
                    Rectangle bounds = new Rectangle(GrabberWindow.getWindow().getLocationOnScreen().x, GrabberWindow.getWindow().getLocationOnScreen().y, GrabberWindow.getWindow().getWidth(), GrabberWindow.getWindow().getHeight());
                    GrabberWindow.getWindow().setVisible(false);
                    r = new Robot();
                    BufferedImage tmpImg = r.createScreenCapture(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
                    String dir = OptionsEngine.getOptions().get(Options.Directory.getID()).toString();
                    if (dir.equals("")) {
                        JFileChooser fc = new JFileChooser(lastDir);
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                        fc.setFileFilter(filter);
                        int returnVal = fc.showSaveDialog(GrabberWindow.getWindow().getContentPane());
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            String path = fc.getSelectedFile().getAbsolutePath();
                            if (!path.toLowerCase().endsWith(".png")) path += ".png";
                            lastDir = path;
                            ImageIO.write(tmpImg, "png", new File(path));
                        }
                    } else {
                        String name = "img";
                        int i = 1;
                        while (new File(dir + "\\" + name + i + ".png").exists()) i++;
                        ImageIO.write(tmpImg, "png", new File(dir + "\\" + name + i + ".png"));
                    }
                } catch (AWTException e1) {
                    e1.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } finally {
                    GrabberWindow.getWindow().setVisible(true);
                }
            }
        });
        add(save);
        add(close);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                int border = (Integer) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.BorderSize.getID()), Integer.class);
                if (GrabberGraphics.closeImg != null) {
                    close.setBounds(border, (int) (getHeight() - GrabberGraphics.closeImg.getHeight() * close.getMinRatio() - border), (int) (GrabberGraphics.closeImg.getWidth() * close.getMinRatio()), (int) (GrabberGraphics.closeImg.getHeight() * close.getMinRatio()));
                    Rectangle r = close.getBounds();
                    if (r.width < 1) r.width = 1;
                    if (r.height < 1) r.height = 1;
                    close.setBounds(r);
                }
                if (getWidth() < GrabberGraphics.closeImg.getWidth() * 3) close.setBounds(0, 0, 0, 0);
                if (GrabberGraphics.saveImg != null) {
                    save.setBounds((int) (border + GrabberGraphics.saveImg.getWidth() * close.getMinRatio() * ((close.getWidth() == 0) ? 0 : 1)), (int) (getHeight() - GrabberGraphics.saveImg.getHeight() * save.getMinRatio() - border), (int) (GrabberGraphics.saveImg.getWidth() * save.getMinRatio()), (int) (GrabberGraphics.saveImg.getHeight() * save.getMinRatio()));
                    Rectangle r = save.getBounds();
                    if (r.width < 1) r.width = 1;
                    if (r.height < 1) r.height = 1;
                    save.setBounds(r);
                }
            }
        });
    }

    /**
	 * This will return true when the point provided is over the resize knob.
	 * 
	 * @param e
	 *            The point to check.
	 * @return True when the point provided is over the resize knob.
	 */
    private boolean isInResize(Point e) {
        return (e.getX() > getWidth() - resizeImg.getWidth() && e.getY() > getHeight() - resizeImg.getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {
        int border = (Integer) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.BorderSize.getID()), Integer.class);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor((Color) OptionsProcessor.process(OptionsEngine.getOptions().get(Options.Color.getID()), Color.class));
        g2.fillRect(0, 0, getWidth(), getHeight());
        Composite c = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(border, border, getWidth() - 2 * border, getHeight() - 2 * border);
        g2.setComposite(c);
        if (resizeImg != null) g2.drawImage(resizeImg, getWidth() - resizeImg.getWidth(), getHeight() - resizeImg.getHeight(), null);
        if (grabImg != null && getWidth() > grabImg.getWidth() * 2 && getHeight() > grabImg.getHeight() * 2) {
            AffineTransform t = AffineTransform.getTranslateInstance(getWidth() - grabImg.getWidth() - border, border);
            g2.transform(t);
            g2.drawImage(grabImg, 0, 0, null);
        }
    }
}
