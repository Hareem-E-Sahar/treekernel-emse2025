package org.tigr.seq.display;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.tigr.cloe.controller.traceManager.TraceListener;
import org.tigr.cloe.model.facade.consensusFacade.ConsensusFacadeParameters;
import org.tigr.cloe.model.facade.datastoreFacade.authentication.UserCredentials;
import org.tigr.cloe.model.facade.datastoreFacade.dao.DAOException;
import org.tigr.cloe.model.traceManager.TraceFetcherWatcher;
import org.tigr.cloe.utils.ApplicationProperties;
import org.tigr.common.Application;
import org.tigr.seq.cloe.Cloe;
import org.tigr.seq.log.*;
import org.tigr.seq.util.*;
import org.tigr.seq.seqdata.SeqdataException;
import org.tigr.seq.task.TaskListener;
import org.tigr.seq.task.ITask;

/**
 *
 * Generic utility class for GUI-related things.
 *
 * <p>
 *
 * Copyright &copy; 2001 The Institute for Genomic Research (TIGR).
 * <p>
 * All rights reserved.
 * 
 * <pre>
 * $RCSfile: AppUtil.java,v $
 * $Revision: 1.31 $
 * $Date: 2005/12/09 18:27:28 $
 * $Author: dkatzel $
 * </pre>
 * 
 * @author Miguel Covarrubias
 * @version 1.0
 */
public class AppUtil {

    /**
     * Describe variable <code>windowListActions</code> here.
     *
     *
     */
    private static Hashtable<IWindow, WindowListAction> windowListActions = new Hashtable<IWindow, WindowListAction>();

    /**
     * Describe variable <code>windowListeners</code> here.
     *
     *
     */
    private static Vector<IWindowListener> windowListeners = new Vector<IWindowListener>();

    private static Set<TraceListener> traceListeners = new HashSet<TraceListener>();

    private static Set<TaskListener> taskListeners = new HashSet<TaskListener>();

    private static ConsensusFacadeParameters sliceServiceParameters;

    /**
     * A unique digit to put on the screenshot window title.
     *
     *
     */
    private static int screenShotCounter = 0;

    static {
        AppUtil.setProperty("com.apple.macos.useRobot", "true");
        Exception ex = null;
        try {
            AppUtil.KEY_ANTIALIASING = RenderingHints.KEY_ANTIALIASING;
            AppUtil.VALUE_ANTIALIAS_ON = RenderingHints.VALUE_ANTIALIAS_ON;
            AppUtil.VALUE_ANTIALIAS_OFF = RenderingHints.VALUE_ANTIALIAS_OFF;
            Class g2Class = Class.forName("java.awt.Graphics2D");
            Method[] methods = g2Class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("setRenderingHint")) {
                    AppUtil.setRenderingHintMethod = methods[i];
                    break;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            ex = cnfe;
        }
        if (ex != null) {
            Log.log(Log.ERROR, new Throwable(), ex, ResourceUtil.getMessage(AppUtil.class, "error_getting_antialiasing_fields"));
        }
    }

    /**
     *
     * Describe class <code>LAFActionListener</code> here. 
     *
     * <p>
     * Copyright &copy; 2002 The Institute for Genomic Research (TIGR).
     * <p>
     * All rights reserved.
     * 
     * <pre>
     * $RCSfile: AppUtil.java,v $
     * $Revision: 1.31 $
     * $Date: 2005/12/09 18:27:28 $
     * $Author: dkatzel $
     * </pre>
     * 
     *
     */
    private class LAFActionListener implements ActionListener {

        /**
         * Describe variable <code>supportedLAFInfos</code> here.
         *
         *
         */
        private UIManager.LookAndFeelInfo[] supportedLAFInfos;

        /**
         * Describe variable <code>lastLafSelected</code> here.
         *
         *
         */
        private String lastLafSelected;

        /**
         * Describe variable <code>lafMenu</code> here.
         *
         *
         */
        private JMenu lafMenu;

        /**
         * Creates a new <code>LAFActionListener</code> instance.
         *
         *
         * @param lafis an <code>UIManager.LookAndFeelInfo[]</code> value
         * 
         * @param menu a <code>JMenu</code> value
         * 
         */
        public LAFActionListener(UIManager.LookAndFeelInfo[] lafis, JMenu menu) {
            this.supportedLAFInfos = lafis;
            this.lafMenu = menu;
        }

        /**
         * Describe <code>actionPerformed</code> method here.
         *
         *
         * @param event an <code>ActionEvent</code> value
         * 
         */
        public void actionPerformed(ActionEvent event) {
            JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) event.getSource();
            String sourcelabel = rbmi.getText();
            if (!sourcelabel.equals(UIManager.getLookAndFeel().getName())) {
                Exception ex = null;
                try {
                    UIManager.LookAndFeelInfo[] lafis = this.supportedLAFInfos;
                    for (int i = 0; i < lafis.length; i++) {
                        if (lafis[i].getName().equals(sourcelabel)) {
                            try {
                                UIManager.setLookAndFeel(lafis[i].getClassName());
                                System.out.println(" Set the new LAF: " + lafis[i].getClassName());
                            } catch (UnsupportedLookAndFeelException lafex) {
                                Log.log(Log.WARN, new Throwable(), ResourceUtil.getMessage(AppUtil.class, "unsupported_laf", lafis[i].getName()));
                                rbmi.setEnabled(false);
                                System.out.println(" ++++++++ Last LAF selected was " + this.lastLafSelected);
                                if (this.lastLafSelected != null) {
                                    Component[] menuitems = this.lafMenu.getMenuComponents();
                                    JRadioButtonMenuItem lastrbmi;
                                    for (int k = 0; k < menuitems.length; k++) {
                                        lastrbmi = (JRadioButtonMenuItem) menuitems[k];
                                        if (lastrbmi.getText().equals(this.lastLafSelected)) {
                                            lastrbmi.setSelected(true);
                                            break;
                                        }
                                    }
                                }
                                return;
                            }
                            System.out.println("Update visiable windows ");
                            AppUtil.updateVisibleIWindows();
                            this.lastLafSelected = rbmi.getText();
                            break;
                        }
                    }
                } catch (IllegalAccessException e2) {
                    ex = e2;
                } catch (InstantiationException e3) {
                    ex = e3;
                } catch (ClassNotFoundException e4) {
                    ex = e4;
                }
                if (ex != null) {
                    Log.log(Log.ERROR, new Throwable(), ResourceUtil.getMessage(AppUtil.class, "laf_error", ex.getClass().getName(), sourcelabel, LogUtil.stringify(ex)));
                }
                AppUtil.updateLAFMenuButtonGroups();
            }
        }
    }

    /**
     *
     * A panel representing a screen shot.
     *
     *
     */
    private static class ScreenShotPanel extends JPanel {

        /**
         * 
         */
        private static final long serialVersionUID = 1189264683558346698L;

        private Image image;

        public ScreenShotPanel(Image image) {
            this.image = image;
        }

        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(this.image, 0, 0, this);
        }
    }

    private static class ScreenShotWindow extends JFrame {

        /**
         * 
         */
        private static final long serialVersionUID = -5746289681819926597L;

        private static ScreenShotPanel panel;

        public ScreenShotWindow(Image image) {
            panel = new ScreenShotPanel(image);
            this.getContentPane().add(panel);
        }
    }

    /**
     * Describe variable <code>KEY_ANTIALIASING</code> here.
     *
     *
     */
    private static Object KEY_ANTIALIASING;

    /**
     * Describe variable <code>VALUE_ANTIALIAS_ON</code> here.
     *
     *
     */
    private static Object VALUE_ANTIALIAS_ON;

    /**
     * Describe variable <code>VALUE_ANTIALIAS_OFF</code> here.
     *
     *
     */
    private static Object VALUE_ANTIALIAS_OFF;

    /**
     * Describe variable <code>setRenderingHintMethod</code> here.
     *
     *
     */
    private static Method setRenderingHintMethod;

    /**
     * A registry of ButtonGroups that will need to by synced up
     * everytime there is a LAF change.
     *
     * */
    private static Map<ButtonGroup, Boolean> buttonGroupRegistry = new WeakHashMap<ButtonGroup, Boolean>();

    /**
     * Describe <code>setProperty</code> method here.
     *
     *
     * @param key a <code>String</code> value
     * 
     * @param value a <code>String</code> value
     * 
     */
    public static void setProperty(String key, String value) {
        AppUtil.setProperty(System.getProperties(), key, value);
    }

    /**
     * Describe <code>setProperty</code> method here.
     *
     *
     * @param properties a <code>Properties</code> value
     * 
     * @param key a <code>String</code> value
     * 
     * @param value a <code>String</code> value
     * 
     */
    public static void setProperty(Properties properties, String key, String value) {
        Exception ex = null;
        try {
            Method setPropertyMethod = Properties.class.getMethod("setProperty", new Class[] { String.class, String.class });
            setPropertyMethod.invoke(properties, new Object[] { key, value });
        } catch (NoSuchMethodException nsme) {
            ex = nsme;
        } catch (IllegalAccessException iae) {
            ex = iae;
        } catch (InvocationTargetException ite) {
            ex = ite;
        }
        if (ex != null) {
            Log.log(Log.ERROR, new Throwable(), ex, ResourceUtil.getMessage(AppUtil.class, "error_setting_property"));
        }
    }

    /**
     * Put the specified frame in the middle of the screen.
     *
     * @param component The <code>JFrame</code> to be centered
     */
    public static void center(JFrame component) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = component.getSize();
        component.setLocation((int) ((screen.width - size.width) / 2), (int) ((screen.height - size.height) / 2));
    }

    /**
     * Put the specified dialog in the middle of the screen.
     *
     * @param component the <code>JDialog</code> to be centered
     */
    public static void center(JDialog component) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = component.getSize();
        component.setLocation((int) ((screen.width - size.width) / 2), (int) ((screen.height - size.height) / 2));
    }

    /**
     * Set a monospaced font on the requested component.
     *
     * @param component the <code>Component</code> that should get the
     * monospaced font  */
    public static void setMonospacedFont(Component component) {
        Font oldFont = component.getFont();
        Font newFont = new Font("Monospaced", oldFont.getStyle(), oldFont.getSize());
        component.setFont(newFont);
    }

    /**
     * Describe <code>updateLAFMenuButtonGroups</code> method here.
     *
     *
     */
    private static void updateLAFMenuButtonGroups() {
        String lafname = UIManager.getLookAndFeel().getName();
        Iterator iter = AppUtil.buttonGroupRegistry.keySet().iterator();
        while (iter.hasNext()) {
            ButtonGroup group = (ButtonGroup) iter.next();
            Enumeration enumeration = group.getElements();
            ITERATION_OVER_BUTTONS_IN_THIS_GROUP: while (enumeration.hasMoreElements()) {
                AbstractButton button = (AbstractButton) enumeration.nextElement();
                if (button.getText().equals(lafname)) {
                    button.setSelected(true);
                    break ITERATION_OVER_BUTTONS_IN_THIS_GROUP;
                }
            }
        }
    }

    /**
     * Set the application's LAF as system's LAF
     */
    public static void setApplicationLAF() {
        String currentlook = ApplicationProperties.getInstance().getProperties().getProperty("Cloe.defaultLookAndFeel", UIManager.getLookAndFeel().getClass().getName());
        try {
            UIManager.setLookAndFeel(currentlook);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        AppUtil.updateLAFMenuButtonGroups();
    }

    /**
     * Update visible IWindows in the application
     * 
     *
     * This method is intend to fix the LAF relatied bugs.
     * The original AppUtil.updateVisibleFrames () method
     * does not work properly since it only retrieve the 
     * visible 'Frame'S. If the window is Dialog box, that
     * method can not detect. 
     *
     */
    public static void updateVisibleIWindows() {
        Iterator windows = AppUtil.getApplicationIWindows();
        while (windows != null && windows.hasNext()) {
            Object o = windows.next();
            if (o instanceof IWindow) {
                IWindow ui = (IWindow) o;
                if (ui.isWindowVisible()) {
                    Window window = (Window) ui;
                    SwingUtilities.updateComponentTreeUI(window);
                    window.repaint();
                    window.validate();
                }
            }
        }
    }

    /**
     * Update visible frames after a LAF change
     *
     *
     */
    public static void updateVisibleFrames() {
        Frame[] frames = Frame.getFrames();
        for (int f = 0; f < frames.length; f++) {
            Frame frame = frames[f];
            if (frame.isVisible()) {
                SwingUtilities.updateComponentTreeUI(frame);
                System.out.println("Update frame " + frame.getName() + " 's   LAF ");
                frame.repaint();
                frame.validate();
            }
        }
        AppUtil.updateVisibleIWindows();
    }

    /**
     * Return FontMetrics for the specified component.
     * 
     * @param component
     *            the <code>JComponent</code> whose FontMetrics we'd like to
     *            get
     * 
     * @return the retrieved <code>FontMetrics</code>
     */
    private static FontMetrics getFontMetrics(JComponent component) {
        FontMetrics ret = null;
        if (component != null) {
            Font f = component.getFont();
            ret = component.getFontMetrics(f);
        }
        return ret;
    }

    /**
     * How wide would the specified String be if drawn on the specified
     * JComponent?
     *
     * @param component the <code>JComponent</code> in whose context
     * we're doing our calculations
     *
     * @param text the <code>String</code> value whose width we're
     * calculating
     *
     * @return the calculated width */
    public static int getStringWidth(JComponent component, String text) {
        int ret = 0;
        if (component != null && text != null) {
            FontMetrics fm = AppUtil.getFontMetrics(component);
            ret = fm.stringWidth(text);
        }
        return ret;
    }

    /**
     * How tall would this multiline String be on the specified component?
     *
     * @param component the <code>JComponent</code> in whose context
     * we're doing our calculations
     *
     * @param text the <code>String</code> value whose height we're
     * calculating
     *
     * @return the calculated width 
     */
    public static int getMultilineStringHeight(JComponent component, String text) {
        int ret = 0;
        if (component != null && text != null) {
            FontMetrics fm = AppUtil.getFontMetrics(component);
            int lineHeight = fm.getAscent() + fm.getDescent();
            int lineCount = 0;
            int lastIndex = 0;
            while (lastIndex < text.length()) {
                lastIndex = text.indexOf('\n', lastIndex);
                if (lastIndex == -1) {
                    break;
                }
                lineCount++;
                lastIndex++;
            }
            lineCount++;
            ret = lineCount * lineHeight;
        }
        return ret;
    }

    /**
     * How wide would the specified multiline String be if drawn on the
     * specified JComponent?
     *
     *
     * @param component the <code>JComponent</code> in whose context
     * we're doing our calculations
     *
     * @param text the <code>String</code> value whose width we're
     * calculating
     *
     * @return the calculated width 
     */
    public static int getMultilineStringWidth(JComponent component, String text) {
        int ret = 0;
        if (component != null && text != null) {
            int lastindex = 0;
            int index = text.indexOf('\n');
            while (index != -1) {
                String car;
                car = text.substring(lastindex, index);
                ret = Math.max(ret, AppUtil.getStringWidth(component, car));
                lastindex = index;
                index = text.indexOf('\n', lastindex + 1);
            }
            ret = Math.max(ret, AppUtil.getStringWidth(component, text.substring(lastindex)));
        }
        return ret;
    }

    /**
     * Method to check if input integeris valid or not
     *
     * @param pInputNum a <code>String</code> value
     * 
     * @return  hash table that stores the key value pair, key is boolean type, value is Exception message
     */
    public static String validateInteger(String pInputNum) {
        int num = -1;
        String invalidNumber = null;
        try {
            num = java.lang.Integer.parseInt(pInputNum, 10);
            if (num <= 0) {
                invalidNumber = pInputNum;
            }
        } catch (NumberFormatException e) {
            invalidNumber = pInputNum;
        }
        return invalidNumber;
    }

    /**
     * Method to check if input integeris valid or not
     *
     * @param pInputNum a <code>String</code> value
     * 
     * @return  hash table that stores the key value pair, key is boolean type, value is Exception message
     */
    public static String validateShort(String pInputNum) {
        int num;
        String invalidNumber = null;
        try {
            num = java.lang.Short.parseShort(pInputNum, 10);
            if (num < 0) {
                invalidNumber = pInputNum;
            }
        } catch (NumberFormatException e) {
            invalidNumber = pInputNum;
        }
        return invalidNumber;
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param component a <code>Component</code> value
     * 
     */
    public static void createScreenshot(Component component) {
        Point point = component.getLocationOnScreen();
        Dimension dim = component.getSize();
        AppUtil.createScreenshot(point.x, point.y, dim.width, dim.height);
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param component a <code>JComponent</code> value
     * 
     */
    public static void createScreenshot(JComponent component) {
        Point point = component.getLocationOnScreen();
        Dimension dim = component.getSize();
        AppUtil.createScreenshot(point.x, point.y, dim.width, dim.height);
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param dialog a <code>JDialog</code> value
     * 
     */
    public static void createScreenshot(JDialog dialog) {
        Point point = dialog.getLocationOnScreen();
        Dimension dim = dialog.getSize();
        AppUtil.createScreenshot(point.x, point.y, dim.width, dim.height);
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param frame a <code>JFrame</code> value
     * 
     */
    public static void createScreenshot(JFrame frame) {
        Point point = frame.getLocationOnScreen();
        Dimension dim = frame.getSize();
        AppUtil.createScreenshot(point.x, point.y, dim.width, dim.height);
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param x an <code>int</code> value
     * 
     * @param y an <code>int</code> value
     * 
     * @param width an <code>int</code> value
     * 
     * @param height an <code>int</code> value
     * 
     */
    public static void createScreenshot(int x, int y, int width, int height) {
        AppUtil.createScreenshot(new Rectangle(x, y, width, height));
    }

    /**
     * Describe <code>createScreenshot</code> method here.
     *
     *
     * @param rect a <code>Rectangle</code> value
     * 
     */
    public static void createScreenshot(Rectangle rect) {
        Exception ex = null;
        try {
            Class jaiClass = Class.forName("javax.media.jai.JAI");
            Class renderedImageClass = Class.forName("java.awt.image.RenderedImage");
            Method createMethod = jaiClass.getMethod("create", new Class[] { String.class, renderedImageClass, Object.class, Object.class, Object.class });
            Class botClass = Class.forName("java.awt.Robot");
            Constructor botConstructor = botClass.getConstructor(new Class[] {});
            Object bot = botConstructor.newInstance(new Object[] {});
            Method screenCapMethod = botClass.getMethod("createScreenCapture", new Class[] { Rectangle.class });
            Object bimage = screenCapMethod.invoke(bot, new Object[] { rect });
            System.out.println("bimage is " + bimage);
            String outfile = "./capture.png";
            FileOutputStream outstream = new FileOutputStream(outfile);
            createMethod.invoke(null, new Object[] { "encode", bimage, outstream, "PNG", null });
            createMethod.invoke(null, new Object[] { "filestore", bimage, outfile, "PNG", null });
            outstream.close();
            Image image = (Image) bimage;
            AppUtil.screenShotCounter++;
            ScreenShotWindow screenShotWindow = new ScreenShotWindow(image);
            screenShotWindow.setSize(image.getWidth(null) + 100, image.getHeight(null) + 100);
            AppUtil.center(screenShotWindow);
            String title = ResourceUtil.getResource(AppUtil.class, "title.screenshot_window", Application.getApplicationName(), new Integer(AppUtil.screenShotCounter));
            screenShotWindow.setTitle(title);
            screenShotWindow.toFront();
            screenShotWindow.setVisible(true);
        } catch (FileNotFoundException fx) {
            ex = fx;
        } catch (IOException iox) {
            ex = iox;
        } catch (ClassNotFoundException cnfe) {
            ex = cnfe;
        } catch (NoSuchMethodException nsme) {
            ex = nsme;
        } catch (InstantiationException ie) {
            ex = ie;
        } catch (IllegalAccessException iae) {
            ex = iae;
        } catch (InvocationTargetException ite) {
            ex = ite;
        }
        if (ex != null) {
            Log.log(Log.ERROR, new Throwable(), ex, ResourceUtil.getMessage(AppUtil.class, "exception_while_screenshotting"));
        }
    }

    /**
     * Describe <code>waitCursor</code> method here.
     *
     *
     * @param comp a <code>Component</code> value
     * 
     * @return a <code>Cursor</code> value
     *
     */
    public static Cursor waitCursor(Component comp) {
        Cursor ret = comp.getCursor();
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        return ret;
    }

    /**
     * Describe <code>normalCursor</code> method here.
     *
     *
     * @param comp a <code>Component</code> value
     * 
     * @return a <code>Cursor</code> value
     *
     */
    public static Cursor normalCursor(Component comp) {
        Cursor ret = comp.getCursor();
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return ret;
    }

    /**
     * Set the anti-aliasing.  Ignored on Java 1.
     *
     *
     * @param g a <code>Graphics</code> value
     * 
     * @param flag a <code>boolean</code> value
     * 
     */
    public static void setAntiAliasing(Graphics g, boolean flag) {
        Object value;
        Exception ex = null;
        if (flag) {
            value = AppUtil.VALUE_ANTIALIAS_ON;
        } else {
            value = AppUtil.VALUE_ANTIALIAS_OFF;
        }
        try {
            AppUtil.setRenderingHintMethod.invoke(g, new Object[] { AppUtil.KEY_ANTIALIASING, value });
        } catch (InvocationTargetException ite) {
            ex = ite;
        } catch (IllegalAccessException iae) {
            ex = iae;
        }
        if (ex != null) {
            Log.log(Log.ERROR, new Throwable(), ex, ResourceUtil.getMessage(AppUtil.class, "error_setting_antialiasing"));
        }
    }

    /**
    * Method for createImageIcon
    * @param file  the file path and name the image file
    * @return ImageIcon 
    */
    public static ImageIcon createImageIcon(String file) {
        ImageIcon image = new ImageIcon();
        try {
            image = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(file)));
        } catch (Exception ex) {
            System.out.println("Image fetching exception: " + ex);
        }
        return image;
    }

    /**
     * Describe <code>buildLAFMenu</code> method here.
     *
     *
     * @param menu a <code>JMenu</code> value
     * 
     */
    public static void buildLAFMenu(JMenu menu) {
        new AppUtil().buildLAFMenu(menu, null);
    }

    /**
     * Build the Look and Feel menu.  Iterate through the laf infos.
     * Load the classes and figure out if these are supported.  The
     * ones that are supported go on the menu.
     *
     *
     * @param menu a <code>JMenu</code> value
     * 
     * @param bogus an <code>Object</code> value
     *  */
    public void buildLAFMenu(JMenu menu, Object bogus) {
        ButtonGroup group = new ButtonGroup();
        UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        Vector<UIManager.LookAndFeelInfo> tmplist = new Vector<UIManager.LookAndFeelInfo>();
        for (int i = 0; i < infos.length; i++) {
            Exception clex = null;
            try {
                Class clazz = Class.forName(infos[i].getClassName());
                LookAndFeel laf = (LookAndFeel) clazz.newInstance();
                if (laf.isSupportedLookAndFeel()) {
                    tmplist.add(infos[i]);
                }
            } catch (ClassNotFoundException cnfe) {
                clex = cnfe;
            } catch (IllegalAccessException iae) {
                clex = iae;
            } catch (InstantiationException ie) {
                clex = ie;
            }
            if (clex != null) {
                Log.log(Log.INFO, new Throwable(), ResourceUtil.getMessage(AppUtil.class, "exception_loading_laf", clex.getClass().getName(), infos[i].getName(), LogUtil.stringify(clex)));
            }
        }
        UIManager.LookAndFeelInfo[] supportedLAFInfos = new UIManager.LookAndFeelInfo[tmplist.size()];
        for (int i = 0; i < supportedLAFInfos.length; i++) {
            supportedLAFInfos[i] = (UIManager.LookAndFeelInfo) tmplist.get(i);
        }
        ActionListener al = new LAFActionListener(supportedLAFInfos, menu);
        String currentlook = UIManager.getLookAndFeel().getClass().getName();
        for (int i = 0; i < supportedLAFInfos.length; i++) {
            JMenuItem mi = new JRadioButtonMenuItem(supportedLAFInfos[i].getName());
            if (supportedLAFInfos[i].getClassName().equals(currentlook)) {
                ((JRadioButtonMenuItem) mi).setSelected(true);
            }
            mi.addActionListener(al);
            group.add(mi);
            menu.add(mi);
        }
        AppUtil.buttonGroupRegistry.put(group, Boolean.TRUE);
    }

    /**
     * Describe <code>defaultWindowCreationHandler</code> method here.
     *
     *
     * @param windowsMenu a <code>JMenu</code> value
     * 
     * @param ui an <code>IWindow</code> value
     * 
     */
    public static void defaultWindowCreationHandler(JMenu windowsMenu, IWindow ui) {
        WindowListAction action = new WindowListAction(ui);
        windowsMenu.setEnabled(true);
        windowsMenu.add(action);
    }

    /**
     * Describe <code>defaultWindowDestructionHandler</code> method here.
     *
     *
     * @param windowsMenu a <code>JMenu</code> value
     * 
     * @param ui an <code>IWindow</code> value
     * 
     */
    public static void defaultWindowDestructionHandler(JMenu windowsMenu, IWindow ui) {
        Component[] items = windowsMenu.getMenuComponents();
        WindowListAction action = null;
        JMenuItem mi;
        for (int i = 0; i < items.length; i++) {
            mi = (JMenuItem) items[i];
            action = (WindowListAction) mi.getAction();
            if (action != null && action.getUI() == ui) {
                windowsMenu.remove(mi);
                break;
            }
        }
        if (windowsMenu.getMenuComponents().length == 0) {
            windowsMenu.setEnabled(false);
        }
    }

    /**
     * Describe <code>addWindowListener</code> method here.
     *
     *
     * @param listener an <code>IWindowListener</code> value
     * 
     */
    public static void addWindowListener(IWindowListener listener) {
        AppUtil.windowListeners.add(listener);
    }

    /**
     * Describe <code>removeWindowListener</code> method here.
     *
     *
     * @param listener an <code>IWindowListener</code> value
     * 
     */
    public static void removeWindowListener(IWindowListener listener) {
        AppUtil.windowListeners.remove(listener);
    }

    public static void addTraceListener(TraceListener listener) {
        traceListeners.add(listener);
    }

    public static void removeTraceListener(TraceListener listener) {
        traceListeners.remove(listener);
    }

    public static void notifyTraceFinsih(TraceFetcherWatcher traceThatIsFinished) {
        for (TraceListener listener : traceListeners) {
            listener.traceFinish(traceThatIsFinished);
        }
    }

    public static void notifyTraceKilled(String traceName) {
        for (TraceListener listener : traceListeners) {
            listener.traceKilled(traceName);
        }
    }

    public static void addTaskListener(TaskListener listener) {
        taskListeners.add(listener);
    }

    public static void removeTaskListener(TaskListener listener) {
        taskListeners.remove(listener);
    }

    public static void notifyTaskUpdate(ITask taskThatHasUpdated) {
        for (TaskListener listener : taskListeners) {
            listener.taskUpdated(taskThatHasUpdated);
        }
    }

    /**
     * Describe <code>windowCreated</code> method here.
     *
     *
     * @param ui an <code>IWindow</code> value
     * 
     */
    public static void windowCreated(IWindow ui) {
        if (ui != null) {
            WindowListAction action = new WindowListAction(ui);
            AppUtil.windowListActions.put(ui, action);
            IWindowListener listener;
            Iterator iter = AppUtil.windowListeners.iterator();
            while (iter.hasNext()) {
                listener = (IWindowListener) iter.next();
                listener.windowCreated(ui);
            }
        }
    }

    /**
     * Describe <code>windowDestroyed</code> method here.
     *
     *
     * @param ui an <code>IWindow</code> value
     * 
     */
    public static void windowDestroyed(IWindow ui) {
        AppUtil.windowListActions.remove(ui);
        for (int i = windowListeners.size() - 1; i > 0; i--) {
            ((IWindowListener) windowListeners.get(i)).windowDestroyed(ui);
        }
    }

    /**
     * Describe <code>populateWindowMenu</code> method here.
     *
     *
     * @param menu a <code>JMenu</code> value
     * 
     */
    public static void populateWindowMenu(JMenu menu) {
        Iterator iter = AppUtil.windowListActions.values().iterator();
        while (iter.hasNext()) {
            Action action = (Action) iter.next();
            menu.add(action);
        }
    }

    /**
     * Describe <code>getApplicationIWindows</code> 
     *
     * @return  <code>Iterator</code> value
     *
     */
    public static Iterator<IWindow> getApplicationIWindows() {
        Iterator<IWindow> ret = null;
        if (!AppUtil.windowListActions.isEmpty()) {
            Hashtable clone = (Hashtable) windowListActions.clone();
            ret = (Iterator<IWindow>) clone.keys();
        }
        return ret;
    }

    public static Point rangeCheckPanelScrollPoint(JPanel pPanel, JViewport pViewport, Point pProposedPoint) {
        int x = pProposedPoint.x;
        x = Math.max(x, 0);
        x = Math.min(x, pPanel.getSize().width - pViewport.getExtentSize().width);
        int y = pProposedPoint.y;
        y = Math.max(y, 0);
        y = Math.min(y, pPanel.getSize().height - pViewport.getExtentSize().height);
        Point p = new Point(x, y);
        return p;
    }

    /**
     * @return Returns the sliceServiceParameters.
     */
    public static ConsensusFacadeParameters getSliceServiceParameters() throws DAOException {
        if (sliceServiceParameters == null) {
            synchronized (AppUtil.class) {
                sliceServiceParameters = Application.getDatastore().getDao().getConsensusFacadeParameters(UserCredentials.getProjectName());
            }
        }
        return sliceServiceParameters;
    }

    /**
     * @param sliceServiceParameters The sliceServiceParameters to set.
     */
    public static void setSliceServiceParameters(ConsensusFacadeParameters sliceServiceParameters) {
        AppUtil.sliceServiceParameters = sliceServiceParameters;
    }
}
