package de.peathal.util;

import com.incors.plaf.kunststoff.KunststoffLookAndFeel;
import de.peathal.gui.PTheme;
import de.peathal.resource.IIconHelper;
import de.peathal.resource.L;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PrintJob;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author Peter Karich, peat_hal 'at' users 'dot' sourceforge 'dot' net
 * @author Dr. Heinz M. Kabutz, findActiveFrame()
 */
public class SwingHelper {

    /**
     * This method sets the location of the specified component to a centered
     * location.
     */
    public static void center(Component w) {
        Rectangle re = getMaximumWindowBounds();
        Dimension dim = w.getSize();
        if (dim.height > re.height) dim.height = re.height;
        if (dim.width > re.width) dim.width = re.width;
        w.setLocation((re.width - dim.width) / 2, (re.height - dim.height) / 2);
    }

    /**
     * I hate ownerless dialogs.  With this method, we can find the
     * currently visible frame and attach the dialog to that, instead
     * of always attaching it to null.
     * @author Dr. Heinz M. Kabutz
     */
    public static Frame findActiveFrame() {
        Frame[] frames = JFrame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            Frame frame = frames[i];
            if (frame.isVisible()) {
                return frame;
            }
        }
        return null;
    }

    /**
     * This method returns the maximal usable bounds for centered components.
     * @return the maximal usable bounds.
     */
    public static Rectangle getMaximumWindowBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getMaximumWindowBounds();
    }

    private static JFrame splashContainer = null;

    private static JPanel splashWindow = null;

    private static JProgressBar progressBar;

    private static boolean alwaysHide = true;

    /**
     * This method shows the image 'splashscreen' if specified b is true.
     * This behaviour could be used in cunjunction with setProgress to
     * initialize an application if it has a long running startup.
     */
    public static synchronized void showSplash(final boolean b) {
        if (alwaysHide) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Thread() {

                public void run() {
                    _showSplash(b);
                }
            });
        } else {
            _showSplash(b);
        }
    }

    private static void _showSplash(boolean b) {
        if (b) {
            synchronized (SwingHelper.class) {
                if (splashContainer != null) {
                    return;
                }
                splashContainer = new JFrame();
            }
            splashContainer.setUndecorated(true);
            splashContainer.setLayout(new BorderLayout());
            if (splashWindow == null) {
                _initOnlyOnce();
            }
            if (splashWindow != null) {
                splashContainer.add(splashWindow, BorderLayout.CENTER);
            }
            splashContainer.add(progressBar, BorderLayout.SOUTH);
            splashContainer.pack();
            center(splashContainer);
            splashContainer.setVisible(true);
        } else if (splashContainer != null) {
            synchronized (SwingHelper.class) {
                if (splashContainer != null) {
                    splashContainer.setVisible(false);
                    splashContainer.dispose();
                    splashContainer = null;
                }
            }
        }
    }

    private static BufferedImage bi = null;

    private static void _initOnlyOnce() {
        IIconHelper ih = Lookup.get().getImplObject(IIconHelper.class);
        final ImageIcon icon = ih.get("splashscreen");
        if (icon == null) {
            return;
        }
        splashWindow = new JPanel() {

            public void paintComponent(Graphics g) {
                if (bi != null) g.drawImage(bi, 0, 0, null);
                g.drawImage(icon.getImage(), 0, 0, null);
            }
        };
        splashWindow.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        splashWindow.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                splashContainer.setVisible(false);
                alwaysHide = true;
            }
        });
        Color myYellow = new Color(255, 225, 102);
        Color myGreen = new Color(110, 170, 34);
        UIManager.put("ProgressBar.selectionBackground", myGreen.darker());
        UIManager.put("ProgressBar.selectionForeground", myYellow);
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setVisible(true);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(myYellow);
        progressBar.setForeground(myGreen);
    }

    public static synchronized void setEnableGUI(boolean b) {
        alwaysHide = !b;
    }

    /**
     * This method sets the progress of startup process. Use with showSplash.
     */
    public static void setProgress(int percent) {
        setProgress(null, percent);
    }

    /**
     * This method sets the progress counter and string of startup process.
     * Use in conjunction with showSplash.
     */
    public static void setProgress(final String text, final int percent) {
        synchronized (SwingHelper.class) {
            if (alwaysHide) {
                return;
            }
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        setProgress(text, percent);
                    }
                });
            } catch (Exception ex) {
                GLog.warn(L.tr("Couldn't set progress status."), ex);
            }
        } else {
            synchronized (progressBar) {
                if (splashContainer != null && progressBar != null) {
                    progressBar.setVisible(percent >= 0);
                    progressBar.setValue(percent);
                    if (text != null) {
                        progressBar.setString(text);
                    }
                }
            }
        }
    }

    private static JDialog aboutDialog;

    /**
     * This method shows the about dialog.
     */
    public static synchronized void showAbout(JFrame owner, File file, JEditorPane text) {
        if (aboutDialog == null) {
            aboutDialog = new JDialog(owner, L.tr("About"), false);
            Container c = aboutDialog.getContentPane();
            try {
                URL url = file.toURI().toURL();
                text = new JEditorPane(url);
                ((HTMLDocument) text.getDocument()).setBase(url);
            } catch (Exception ex) {
                GLog.log(L.tr("Can't_load_about_dialog_html_file!"), ex);
            }
            if (text == null) {
                aboutDialog = null;
                return;
            }
            text.setEditable(false);
            c.setLayout(new BorderLayout());
            c.add(new JScrollPane(text), BorderLayout.CENTER);
            aboutDialog.setSize(500, 550);
            center(aboutDialog);
        }
        if (aboutDialog != null) {
            aboutDialog.setVisible(true);
        }
    }

    /**
     * This method sets the fullscreen mode of specified frame to specified boolean.
     */
    public static void fullScreen(Frame g, boolean on) {
        g.setUndecorated(on);
        if (on) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            try {
                GraphicsDevice myDevice = ge.getDefaultScreenDevice();
                g.setLocation(0, 0);
                g.setPreferredSize(new Dimension(myDevice.getDisplayMode().getWidth(), myDevice.getDisplayMode().getHeight()));
            } catch (Exception e) {
                GLog.log("Frame will not be in full screen mode");
            }
        }
    }

    /**
     * If lfStr is null then this method will install default layout.
     * Specified component should be the main component of your project.
     */
    public static void setLookAndFeel(Component comp, String lfStr) {
        if (lfStr == null || lfStr.length() == 0) {
            lfStr = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
        }
        try {
            LookAndFeel lf = (LookAndFeel) Class.forName(lfStr).newInstance();
            if (lf instanceof KunststoffLookAndFeel) {
                ((KunststoffLookAndFeel) lf).setCurrentTheme(new PTheme());
            }
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            GLog.log(L.tr("Start_with_default_look_and_feel!"));
        }
        refreshLookAndFeel(comp);
    }

    public static void refreshLookAndFeel(Component comp) {
        Frame f = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, comp);
        if (f != null) {
            updateComponentTreeUI(f);
        } else SwingUtilities.updateComponentTreeUI(comp);
    }

    /**
     * A simple minded look and feel change: ask each node in the tree
     * to <code>updateUI()</code> -- that is, to initialize its UI property
     * with the current look and feel. Unlike SwingUtilities.updateComponentTreeUI
     * this method updates each component's children before updating the component itself,
     * making it easier for components to fine tune their children's look and feel's.
     */
    public static void updateComponentTreeUI(Component c) {
        updateComponentTreeUI0(c);
        c.invalidate();
        c.validate();
        c.repaint();
    }

    private static void updateComponentTreeUI0(Component c) {
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu) c).getMenuComponents();
        } else if (c instanceof Container) {
            children = ((Container) c).getComponents();
        }
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                updateComponentTreeUI0(children[i]);
            }
        }
        if (c instanceof JComponent) {
            ((JComponent) c).updateUI();
        }
    }

    public static void packButtonPanel(JComponent panel) {
        FlowLayout l = (FlowLayout) panel.getLayout();
        packButtonPanel(panel, new Insets(l.getVgap() / 2, l.getHgap(), l.getVgap() / 2, l.getHgap()));
    }

    /**
     * This method assumes that all components in the specified panel
     * with flowlayout are JComponent's!
     */
    public static void packButtonPanel(JComponent panel, Insets spaceAroundButtons) {
        Component comps[] = panel.getComponents();
        int maxWidth = 0;
        int height = 0;
        Dimension size;
        JComponent c;
        for (int i = 0; i < comps.length; i++) {
            c = (JComponent) comps[i];
            size = c.getPreferredSize();
            if (size.width > maxWidth) maxWidth = size.width;
            height += size.height + spaceAroundButtons.top + spaceAroundButtons.bottom;
        }
        for (int i = 0; i < comps.length; i++) {
            size = comps[i].getPreferredSize();
            ((JComponent) comps[i]).setPreferredSize(new Dimension(maxWidth, size.height));
        }
        panel.setPreferredSize(new Dimension(maxWidth + spaceAroundButtons.left + spaceAroundButtons.right, height));
        panel.setMinimumSize(panel.getPreferredSize());
    }

    public static JPanel createButtonPanel() {
        return createButtonPanel(4, 0);
    }

    public static JPanel createButtonPanel(int hgap, int vgap) {
        return new JPanel(new FlowLayout(FlowLayout.LEFT, hgap, vgap));
    }

    static boolean isCapsDown = false;

    private static boolean isCapsDown() {
        try {
            return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        } catch (UnsupportedOperationException ex) {
            Robot r;
            try {
                r = new Robot();
            } catch (AWTException ex2) {
                GLog.warn(L.tr("Robot is not supported by this platform."), ex2);
                return false;
            }
            Point old = MouseInfo.getPointerInfo().getLocation();
            JFrame frame = new JFrame();
            frame.setLocation(3000, 3000);
            frame.setSize(1, 1);
            frame.setVisible(true);
            frame.addKeyListener(new KeyListener() {

                public void keyPressed(KeyEvent e) {
                    if (e.isShiftDown()) {
                        return;
                    } else {
                        isCapsDown = e.getKeyChar() == 'A';
                    }
                }

                public void keyReleased(KeyEvent e) {
                }

                public void keyTyped(KeyEvent e) {
                }
            });
            r.mouseMove(3000, 3000);
            r.mousePress(InputEvent.BUTTON1_MASK);
            r.mouseRelease(InputEvent.BUTTON1_MASK);
            r.keyPress(KeyEvent.VK_A);
            r.keyRelease(KeyEvent.VK_A);
            r.mouseMove(old.x, old.y);
            r.mousePress(InputEvent.BUTTON1_MASK);
            r.mouseRelease(InputEvent.BUTTON1_MASK);
            return isCapsDown;
        }
    }

    public static void showUIManagerProperties() {
        UIDefaults defaults = UIManager.getDefaults();
        System.out.println("Count Item = " + defaults.size());
        String[] colName = { "Key", "Value" };
        String[][] rowData = new String[defaults.size()][2];
        int i = 0;
        for (Enumeration e = defaults.keys(); e.hasMoreElements(); i++) {
            Object key = e.nextElement();
            rowData[i][0] = key.toString();
            rowData[i][1] = "" + defaults.get(key);
            System.out.println(rowData[i][0] + " ,, " + rowData[i][1]);
        }
        JFrame f = new JFrame("UIDefaults Key-Value sheet");
        JTable t = new JTable(rowData, colName);
        t.setAutoCreateRowSorter(true);
        f.setContentPane(new JScrollPane(t));
        f.pack();
        f.setVisible(true);
    }

    public static void createScreenCapture(String jpegFile) {
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.sync();
            Rectangle ecran = new Rectangle(tk.getScreenSize());
            Robot robot = new Robot();
            robot.setAutoDelay(0);
            robot.setAutoWaitForIdle(false);
            BufferedImage image = robot.createScreenCapture(ecran);
            File file = new File(jpegFile);
            javax.imageio.ImageIO.write(image, "JPEG", file);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * This method generates a mouse move, without moving the mouse away from
     * its old place. It can be used to avoid screensavers.
     */
    public static void generateMouseMove() {
        try {
            Robot robot = new Robot();
            boolean oldB = robot.isAutoWaitForIdle();
            robot.setAutoWaitForIdle(false);
            robot.setAutoDelay(0);
            Point old = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(old.x - 1, old.y);
            robot.mouseMove(old.x, old.y);
            robot.setAutoWaitForIdle(oldB);
        } catch (AWTException ex) {
            GLog.warn(L.tr("Can't avoid screensaver!"));
        }
    }

    /**
     * This method saves the specified component into the specified file.
     */
    public static void save(Component comp, File file) throws IOException {
        Dimension d = comp.getSize();
        if (d.width == 0 || d.height == 0) {
            throw new UnsupportedOperationException("Can't save, because container is empty!");
        }
        BufferedImage img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, d.width, d.height);
        comp.printAll(g);
        FileOutputStream stream = new FileOutputStream(file);
        try {
            ImageIO.write(img, "jpeg", stream);
        } finally {
            stream.close();
            g.dispose();
        }
    }

    /**
     * This method prints the specified component and shows a printer dialog
     * with the specified title.
     */
    public static void print(Component comp, String title) {
        Dimension panelD = comp.getSize();
        if (panelD.width == 0 || panelD.height == 0) throw new UnsupportedOperationException(L.tr("Can't_print,_cause_") + L.tr("container_is_empty!"));
        Toolkit tk = Toolkit.getDefaultToolkit();
        final PrintJob pj = tk.getPrintJob(new Frame(), title, null);
        if (pj != null) {
            final Graphics printerG = pj.getGraphics();
            Dimension dim = pj.getPageDimension();
            BufferedImage img = new BufferedImage(Math.max(panelD.width, dim.width), Math.max(panelD.height, dim.height), BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2 = (Graphics2D) img.getGraphics();
            double xScale = (double) dim.width / panelD.width;
            double yScale = (double) dim.height / panelD.height;
            if ((dim.width > dim.height && panelD.width < panelD.height) || (dim.width < dim.height && panelD.width > panelD.height)) {
                int tmpWidth = Math.min(panelD.width, panelD.height) / 2;
                xScale = (double) dim.height / panelD.width;
                yScale = (double) dim.width / panelD.height;
                g2.rotate(Math.PI / 2, tmpWidth, tmpWidth);
            }
            if (xScale > 1) xScale = 1;
            if (yScale > 1) yScale = 1;
            g2.scale(xScale, yScale);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, panelD.width, panelD.height);
            comp.printAll(g2);
            printerG.drawImage(img, 0, 0, null);
            pj.end();
            printerG.dispose();
            g2.dispose();
        }
    }

    public static FileFilter getFileFilter(final String string) {
        final String[] endings = string.split(",");
        return new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                for (String s : endings) {
                    if (name.endsWith(s)) {
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return string;
            }
        };
    }

    public static void showMessageDialog(Component parent, Component messageComponent, String title, int option) {
        JOptionPane.showMessageDialog(null, getTitleComponent(title, messageComponent), title, option);
    }

    public static int showConfirmDialog(Component parent, Component messageComponent, String title, int option) {
        return JOptionPane.showConfirmDialog(null, getTitleComponent(title, messageComponent), title, option);
    }

    private static Component getTitleComponent(String title, Component messageComponent) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(title));
        panel.add(new JSeparator());
        panel.add(messageComponent);
        packButtonPanel(panel);
        return panel;
    }
}
