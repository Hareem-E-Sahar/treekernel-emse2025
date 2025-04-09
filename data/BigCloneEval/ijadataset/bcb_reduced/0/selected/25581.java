package com.memoire.bu;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import com.memoire.fu.FuEmptyArrays;
import com.memoire.fu.FuFactoryInteger;
import com.memoire.fu.FuLib;
import com.memoire.fu.FuLog;
import com.memoire.fu.FuWeakCache;

/**
 * Utility class with only static methods. For small and useful services.
 */
public final class BuLib implements FuEmptyArrays {

    private static final boolean DEBUG = Bu.DEBUG && false;

    public static final Frame HELPER = new Frame("BuLib Helper");

    public static final MediaTracker TRACKER = new MediaTracker(HELPER);

    public static final Image DEFAULT_IMAGE = createDefaultImage();

    public static final Font DEFAULT_FONT = createDefaultFont();

    private static final FuWeakCache CACHE_COLOR = new FuWeakCache();

    private static final FuWeakCache CACHE_FONT = new FuWeakCache();

    private static int trackerID_ = 0;

    private static Image createDefaultImage() {
        final byte[] DB = new byte[] { (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61, (byte) 0x10, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x21, (byte) 0xF9, (byte) 0x04, (byte) 0x01, (byte) 0x0A, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x1A, (byte) 0x44, (byte) 0x8E, (byte) 0xA9, (byte) 0x61, (byte) 0xEB, (byte) 0xD7, (byte) 0xDE, (byte) 0x8A, (byte) 0x32, (byte) 0xD1, (byte) 0x0A, (byte) 0xB1, (byte) 0xBA, (byte) 0x98, (byte) 0x57, (byte) 0x2F, (byte) 0x81, (byte) 0x8F, (byte) 0xE8, (byte) 0x90, (byte) 0x93, (byte) 0x66, (byte) 0xA1, (byte) 0x88, (byte) 0xB9, (byte) 0x15, (byte) 0x00, (byte) 0x3B };
        invokeLater(new Runnable() {

            public void run() {
                try {
                    HELPER.addNotify();
                } catch (AbstractMethodError ex) {
                }
                if (DEBUG) FuLog.trace("BLB: JRE is " + FuLib.jdk() + " " + FuLib.getSystemProperty("java.vm.vendor"));
                if (swing() >= 1.2) {
                    Window w = new JWindow().getOwner();
                    if (w instanceof Frame) ((Frame) w).setTitle("Swing Shared Owner Frame");
                }
            }
        });
        return Toolkit.getDefaultToolkit().createImage(DB);
    }

    /**
   * Returns all the current windows.
   * 
   * @return the windows
   */
    public static final Window[] getAllWindows() {
        Vector v = new Vector();
        try {
            Class c = Frame.class;
            Method m = c.getMethod("getFrames", new Class[0]);
            Frame[] f = (Frame[]) m.invoke(null, new Object[0]);
            for (int i = 0; i < f.length; i++) if (!v.contains(f[i])) {
                v.addElement(f[i]);
                m = f[i].getClass().getMethod("getOwnedWindows", new Class[0]);
                Window[] w = (Window[]) m.invoke(f[i], new Object[0]);
                for (int j = 0; j < w.length; j++) if (!v.contains(w[j])) {
                    v.addElement(w[j]);
                }
            }
        } catch (Throwable th) {
            System.err.println(th);
        }
        int l = v.size();
        Window[] r = new Window[l];
        for (int i = 0; i < l; i++) r[i] = (Window) v.elementAt(i);
        return r;
    }

    private static final Font createDefaultFont() {
        Font r = new Font("SansSerif", Font.PLAIN, 12);
        return r;
    }

    /**
   * Ensures that an image is loaded.
   * 
   * @param _image the image to load
   * @param _default an image to return if there is an error
   */
    public static Image ensureImageIsLoaded(final Image _image, final Image _default) {
        if (_image == null) return _default;
        if (_image instanceof BufferedImage) return _image;
        return ensureImageIsLoaded0(_image, _default);
    }

    private static Image ensureImageIsLoaded0(Image _image, Image _default) {
        int id;
        Image r;
        synchronized (TRACKER) {
            id = trackerID_;
            trackerID_++;
        }
        TRACKER.addImage(_image, id);
        try {
            TRACKER.checkID(id, true);
            long before, after, delay;
            before = System.currentTimeMillis();
            while ((after = System.currentTimeMillis()) - before < 10000L) {
                try {
                    delay = 10000L - after + before;
                    if (delay > 100L) TRACKER.waitForID(id, delay);
                    break;
                } catch (InterruptedException ex) {
                }
            }
            if (TRACKER.isErrorID(id) || ((TRACKER.statusID(id, false) & MediaTracker.LOADING) != 0)) r = _default; else r = _image;
        } catch (Exception ex) {
            r = _default;
        }
        if (r != _image) {
            FuLog.debug("BLB: failed to load image <" + id + "> " + FuLib.codeLocation());
        }
        TRACKER.removeImage(_image, id);
        return r;
    }

    public static void invokeNowOrLater(final Runnable _runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            _runnable.run();
        } else {
            SwingUtilities.invokeLater(_runnable);
        }
    }

    public static void invokeNow(final Runnable _runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            _runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(_runnable);
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
                FuLog.error(ex);
            }
        }
    }

    public static void invokeLater(final Runnable _runnable) {
        SwingUtilities.invokeLater(_runnable);
    }

    public static void invokeOutsideNow(final Runnable _runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            _runnable.run();
        } else {
            Thread t = new Thread(_runnable);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
    }

    public static void invokeOutsideLater(final Runnable _runnable) {
        Thread t = new Thread(_runnable);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public static boolean isSingleClick(MouseEvent _evt) {
        if (_evt.getID() != MouseEvent.MOUSE_CLICKED) {
            FuLog.error("BLB: call isSingleClick() only for clicks");
        }
        return (_evt.getClickCount() == 1);
    }

    public static boolean isDoubleClick(MouseEvent _evt) {
        if (_evt.getID() != MouseEvent.MOUSE_CLICKED) {
            FuLog.error("BLB: call isDoubleClick() only for clicks");
        }
        return isLeft(_evt) && (_evt.getClickCount() == 2);
    }

    public static boolean isLeft(MouseEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) == InputEvent.BUTTON1_MASK;
    }

    public static boolean isMiddle(MouseEvent _evt) {
        if (_evt.getID() != MouseEvent.MOUSE_CLICKED) {
            FuLog.error("BLB: call isMiddle() only for clicks");
        }
        return (_evt.getModifiers() & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) == InputEvent.BUTTON2_MASK;
    }

    public static boolean isRight(MouseEvent _evt) {
        if (_evt.getID() != MouseEvent.MOUSE_CLICKED) {
            FuLog.error("BLB: call isRight() only for clicks");
        }
        return (_evt.getModifiers() & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) == InputEvent.BUTTON3_MASK;
    }

    public static boolean isNone(InputEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK)) == 0;
    }

    public static boolean isShift(InputEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK)) == InputEvent.SHIFT_MASK;
    }

    public static boolean isCtrl(InputEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK)) == InputEvent.CTRL_MASK;
    }

    public static boolean isAlt(InputEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK)) == InputEvent.ALT_MASK;
    }

    public static boolean isMeta(InputEvent _evt) {
        return (_evt.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.META_MASK)) == InputEvent.META_MASK;
    }

    /**
   * @param _comp a component
   * @return the metrics of the component font
   */
    public static FontMetrics getFontMetrics(JComponent _comp) {
        if (_comp == null) return null;
        return getFontMetrics(_comp, _comp.getFont());
    }

    /**
   * @param _comp a component if available, can be null
   * @param _font a font
   * @return the metrics of the font
   */
    public static FontMetrics getFontMetrics(JComponent _comp, Font _font) {
        Toolkit tk = null;
        if (_comp != null) tk = _comp.getToolkit();
        if (tk == null) tk = HELPER.getToolkit();
        if (tk == null) tk = Toolkit.getDefaultToolkit();
        return tk.getFontMetrics(_font);
    }

    /**
   * Load an icon from a file which the name is given by an actionCommand.
   */
    public static BuIcon loadCommandIcon(String _cmd) {
        BuIcon r = null;
        String c = _cmd.toLowerCase();
        int i = c.indexOf('_');
        if (i >= 0) c = c.substring(0, i);
        r = BuResource.BU.getIcon(c);
        if (r == null) r = BuResource.BU.getIcon("aucun");
        return r;
    }

    public static Font intern(Font _ft) {
        Font res = _ft;
        if (res != null) {
            Font ft = (Font) CACHE_FONT.get(res);
            if (ft == null) CACHE_FONT.put(res, res); else if (ft != res) res = ft;
        }
        return res;
    }

    public static Color intern(Color _c) {
        Color res = _c;
        if (res != null) {
            Color c = (Color) CACHE_COLOR.get(res);
            if (c == null) CACHE_COLOR.put(res, res); else if (c != res) res = c;
        }
        return res;
    }

    /**
   * To get a font of the same family than an other but a little differente in size.
   * 
   * @return a derivated font
   */
    public static Font deriveFont(String _key, int _deltasize) {
        Font ft = UIManager.getFont(_key + ".font");
        if (ft == null) ft = DEFAULT_FONT;
        int sz = Math.max(ft.getSize() + _deltasize, 10);
        return intern(new Font(ft.getFamily(), ft.getStyle(), sz));
    }

    /**
   * To get a font of the same family than an other but a little differente in size or style.
   * 
   * @return a derivated font
   */
    public static Font deriveFont(String _key, int _style, int _deltasize) {
        Font ft = UIManager.getFont(_key + ".font");
        if (ft == null) ft = DEFAULT_FONT;
        int sz = Math.max(ft.getSize() + _deltasize, 10);
        return intern(new Font(ft.getFamily(), _style, sz));
    }

    /**
   * To get a font of the same family than an other but a little differente in size.
   * 
   * @return a derivated font
   */
    public static Font deriveFont(Font _ft, int _deltasize) {
        Font res = _ft;
        if (res == null) res = DEFAULT_FONT;
        int sz = Math.max(res.getSize() + _deltasize, 10);
        return intern(new Font(res.getFamily(), res.getStyle(), sz));
    }

    /**
   * To get a font of the same family than an other but a little difference in size or style.
   * 
   * @return a derivated font
   */
    public static Font deriveFont(Font _ft, int _style, int _deltasize) {
        Font res = _ft;
        if (res == null) res = DEFAULT_FONT;
        int sz = Math.max(res.getSize() + _deltasize, 10);
        return intern(new Font(res.getFamily(), _style, sz));
    }

    /**
   * Set a plain font.
   */
    public static JComponent setPlainFont(JComponent _c) {
        Font ft = _c.getFont();
        if (ft == null) ft = DEFAULT_FONT;
        _c.setFont(deriveFont(ft, Font.PLAIN, 0));
        return _c;
    }

    /**
   * Set a bold font.
   */
    public static JComponent setBoldFont(JComponent _c) {
        Font ft = _c.getFont();
        if (ft == null) ft = DEFAULT_FONT;
        _c.setFont(deriveFont(ft, Font.BOLD, 0));
        return _c;
    }

    /**
   * Set a monospaced plain font.
   */
    public static JComponent setMonospacedFont(JComponent _c) {
        Font ft = _c.getFont();
        if (ft == null) ft = DEFAULT_FONT;
        _c.setFont(intern(new Font("Monospaced", Font.PLAIN, ft.getSize())));
        return _c;
    }

    /**
   * To get the tab placement customized by the user. (SwingConstants.LEFT, TOP, ...)
   * 
   * @return a tab placement
   */
    public static int getTabPlacement() {
        int r = BuPreferences.BU.getIntegerProperty("tabbedpane.tabplacement");
        if ((r < 1) || (r > 4)) r = 2;
        if (UIManager.getLookAndFeel().getClass().getName().endsWith(".MacLookAndFeel")) r = 1;
        return r;
    }

    /**
   * Set the width of a set of components to the maximal preferred one.
   * 
   * @deprecated ?
   */
    public static void adjustPreferredWidth(JComponent[] _c) {
        int l = _c.length;
        int w = 0;
        int[] h = new int[l];
        for (int i = 0; i < l; i++) {
            Dimension d = _c[i].getPreferredSize();
            w = Math.max(w, d.width);
            h[i] = d.height;
        }
        for (int i = 0; i < l; i++) _c[i].setPreferredSize(new Dimension(w, h[i]));
    }

    /**
   * Internal use.
   */
    private static void getAllSubComponents(Vector _v, Container _parent) {
        Component[] c = _parent.getComponents();
        for (int i = 0; i < c.length; i++) {
            if (!_v.contains(c[i])) {
                _v.addElement(c[i]);
                if (c[i] instanceof Container) getAllSubComponents(_v, (Container) c[i]);
            }
        }
    }

    /**
   * Internal use.
   */
    private static void getAllSubMenuElements(Vector _v, MenuElement _parent) {
        MenuElement[] c = _parent.getSubElements();
        for (int i = 0; i < c.length; i++) {
            if (!_v.contains(c[i])) {
                _v.addElement(c[i]);
                getAllSubMenuElements(_v, c[i]);
            }
        }
    }

    /**
   * Returns all subcomponents of a container.
   */
    public static Vector getAllSubComponents(Container _parent) {
        Vector r = new Vector();
        getAllSubComponents(r, _parent);
        return r;
    }

    /**
   * Returns all subelements of a menu element.
   */
    public static Vector getAllSubMenuElements(MenuElement _parent) {
        Vector r = new Vector();
        getAllSubMenuElements(r, _parent);
        return r;
    }

    /**
   * Set/unset double-buffering for a tree of components. Except JPopupMenu and Window.
   */
    public static void setDoubleBuffered(Container _parent, boolean _b) {
        if (_parent != null) {
            Enumeration e = getAllSubComponents(_parent).elements();
            while (e.hasMoreElements()) {
                Component c = (Component) e.nextElement();
                if (c instanceof JPopupMenu) continue;
                if (c instanceof Window) continue;
                if (c instanceof JComponent) ((JComponent) c).setDoubleBuffered(_b);
            }
        }
    }

    /**
   * Set scollmode for all the viewports in a tree of components.
   */
    public static void setScrollMode(Container _parent, int _mode) {
        int mode = _mode;
        if ((_parent != null)) {
            if ((mode != JViewport.SIMPLE_SCROLL_MODE) && getUIBoolean("Viewport.simpleMode")) mode = JViewport.SIMPLE_SCROLL_MODE;
            Enumeration e = getAllSubComponents(_parent).elements();
            while (e.hasMoreElements()) {
                Component c = (Component) e.nextElement();
                if (c instanceof JPopupMenu) continue;
                if (c instanceof Window) continue;
                if (c instanceof JViewport) ((JViewport) c).setScrollMode(mode);
            }
        }
    }

    /**
   * Center a component on its parent or the screen.
   */
    public static void centerComponent(Component _c) {
        Dimension sc = _c.getSize();
        Dimension sp = null;
        Container p = _c.getParent();
        if (p != null) sp = p.getSize(); else sp = _c.getToolkit().getScreenSize();
        _c.setLocation((sp.width - sc.width) / 2, (sp.height - sc.height) / 2);
    }

    /**
   * Simplifies a component name by removing any lowercase letter at the beginning. ex: btOPEN gives OPEN.
   */
    public static String simplifyComponentName(String _name) {
        if (_name == null) return "";
        int i = 0;
        while (i < _name.length()) {
            int c = _name.charAt(i);
            if ((c < 'A') || (c > 'Z')) i++; else break;
        }
        return _name.substring(i);
    }

    /**
   * Find a named component.
   */
    public static Component findNamedComponent(Container _parent, String _name) {
        Component r = null;
        Enumeration e = getAllSubComponents(_parent).elements();
        while (e.hasMoreElements()) {
            Component c = (Component) e.nextElement();
            String n = c.getName();
            if (_name.equals(n) || _name.equals(simplifyComponentName(n))) {
                r = c;
                break;
            }
        }
        return r;
    }

    private static void enableRecursively(Component _r, boolean _b) {
        _r.setEnabled(_b);
        if (_r instanceof Container) {
            Component[] c = ((Container) _r).getComponents();
            for (int i = 0; i < c.length; i++) setRecursiveEnabled(c[i], _b);
        }
    }

    /**
   * Enables a component and all his children, grand-children...
   */
    public static void setRecursiveEnabled(Component _r, boolean _b) {
        enableRecursively(_r, _b);
        _r.repaint();
    }

    /**
   * Returns the undecorated attribute of a dialog or a frame.
   */
    public static boolean isUndecorated(Window _w) {
        boolean r = false;
        try {
            Class c = _w.getClass();
            Method m = c.getMethod("isUndecorated", CLASS0);
            Object v = m.invoke(_w, OBJECT0);
            r = Boolean.TRUE.equals(v);
        } catch (Throwable th) {
        }
        return r;
    }

    /**
   * Sets undecorated attribute of a dialog or a frame.
   */
    public static void setUndecorated(Window _w, boolean _s) {
        try {
            Class c = _w.getClass();
            Method m = c.getMethod("setUndecorated", new Class[] { Boolean.TYPE });
            m.invoke(_w, new Object[] { Boolean.TRUE });
        } catch (Throwable th) {
        }
    }

    public static void setFullScreen(Window _w) {
    }

    public static Image filter(Image _img) {
        if (_img == null) return null;
        Image img = _img;
        ImageFilter f = (ImageFilter) UIManager.get("Theme.iconFilter");
        if (f != null) {
            img = HELPER.getToolkit().createImage(new FilteredImageSource(img.getSource(), f));
        }
        return img;
    }

    public static BuIcon filter(BuIcon _icon) {
        if (_icon == null) return null;
        if (_icon.isDefault()) return _icon;
        BuIcon icon = _icon;
        ImageFilter f = (ImageFilter) UIManager.get("Theme.iconFilter");
        if (f != null) {
            icon = new BuIcon(HELPER.getToolkit().createImage(new FilteredImageSource(icon.getImage().getSource(), f)));
        }
        return icon;
    }

    public static ImageIcon filter(JComponent _comp, ImageIcon _icon, ImageFilter _filter) {
        if (_icon == null) return null;
        BuRobustIcon r = null;
        try {
            r = new BuRobustIcon(ensureImageIsLoaded(_comp.createImage(new FilteredImageSource(_icon.getImage().getSource(), _filter)), DEFAULT_IMAGE));
            if (DEBUG) if (r.getImage() == DEFAULT_IMAGE) FuLog.debug("BLB: Failed to filter: " + _icon.getDescription() + " " + _filter);
        } catch (Throwable th) {
            if (DEBUG) FuLog.debug("BLB: filter1: " + _filter);
            r = new BuRobustIcon(DEFAULT_IMAGE);
        }
        return r;
    }

    public static ImageIcon addDefaultEffect(final JComponent _c, final ImageIcon _icon) {
        if (_icon == null) return null;
        String effect = UIManager.getString("Theme.iconEffect");
        if (effect == null) effect = BuPreferences.BU.getStringProperty("icons.effect", "none");
        if ((effect == null) || "".equals(effect) || "none".equals(effect) || (_c instanceof JMenuItem) || (FuLib.jdk() < 1.3) || (swing() < 1.2)) return _icon;
        int w = _icon.getIconWidth();
        int h = _icon.getIconHeight();
        GraphicsConfiguration gc = BuLib.HELPER.getGraphicsConfiguration();
        BufferedImage i = gc.createCompatibleImage(w + 7, h + 7, Transparency.TRANSLUCENT);
        Graphics g = i.getGraphics();
        _icon.paintIcon(_c, g, 3, 3);
        g.dispose();
        BuRobustIcon black = null;
        BuRobustIcon white = null;
        black = new BuRobustIcon(filter(filter(_c, new BuRobustIcon(i), BuShadowFilter.SHADOW).getImage()));
        if (!"shadow".equals(effect)) white = new BuRobustIcon(filter(filter(_c, new BuRobustIcon(i), BuShadowFilter.LIGHT).getImage()));
        i = gc.createCompatibleImage(w + 7, h + 7, Transparency.TRANSLUCENT);
        g = i.getGraphics();
        if ("engraved".equals(effect)) {
            black.paintIcon(_c, g, -2, -2);
            black.paintIcon(_c, g, -1, -1);
        }
        if ("embossed".equals(effect)) {
            black.paintIcon(_c, g, 2, 2);
            black.paintIcon(_c, g, 1, 1);
        }
        if (white != null && "engraved".equals(effect)) {
            white.paintIcon(_c, g, 2, 2);
            white.paintIcon(_c, g, 1, 1);
        }
        if (white != null && "embossed".equals(effect)) {
            white.paintIcon(_c, g, -2, -2);
            white.paintIcon(_c, g, -1, -1);
        }
        if ("shadow".equals(effect)) {
            black.paintIcon(_c, g, 2, 2);
            black.paintIcon(_c, g, 2, 2);
            _icon.paintIcon(_c, g, 2, 2);
        } else {
            _icon.paintIcon(_c, g, 3, 3);
        }
        g.dispose();
        return new BuRobustIcon(i);
    }

    /**
   * Sets icons for an abstract button (button, menuitem, ...).
   */
    public static void setIcon(final AbstractButton _button, final BuIcon _icon) {
        if (_icon == null) {
            _button.setIcon(null);
            _button.setPressedIcon(null);
            _button.setRolloverIcon(null);
            _button.setDisabledIcon(null);
            _button.setSelectedIcon(null);
            _button.setRolloverSelectedIcon(null);
            _button.setDisabledSelectedIcon(null);
        } else {
            _button.setIcon(_icon);
        }
    }

    /**
   * Sets the selected icons for a toggle button.
   */
    public static void setSelectedIcons(JToggleButton _button, ImageIcon _icon, boolean _effect) {
        ImageIcon icon = _icon;
        if (_effect) icon = addDefaultEffect(_button, icon);
        if (BuPreferences.BU.getBooleanProperty("icons.grey", false)) {
            _button.setSelectedIcon(filter(_button, filter(_button, icon, BuFilters.GREY), BuFilters.getSelected()));
            _button.setRolloverSelectedIcon(filter(_button, icon, BuFilters.getSelected()));
            _button.setDisabledSelectedIcon(filter(_button, filter(_button, icon, BuFilters.getSelected()), BuFilters.getDisabled()));
        } else {
            _button.setSelectedIcon(filter(_button, icon, BuFilters.getSelected()));
            _button.setRolloverSelectedIcon(filter(_button, icon, BuFilters.getRollover()));
            _button.setDisabledSelectedIcon(filter(_button, icon, BuFilters.getDisabled()));
        }
    }

    public static Border getEmptyBorder(int _gap) {
        Border r;
        switch(_gap) {
            case 0:
                r = BuBorders.EMPTY0000;
                break;
            case 1:
                r = BuBorders.EMPTY1111;
                break;
            case 2:
                r = BuBorders.EMPTY2222;
                break;
            case 3:
                r = BuBorders.EMPTY3333;
                break;
            case 4:
                r = BuBorders.EMPTY4444;
                break;
            default:
                r = BuBorders.EMPTY5555;
                break;
        }
        return r;
    }

    public static int computeLuminance(Color _color) {
        return computeLuminance(_color.getRGB());
    }

    public static int computeLuminance(int _rgb) {
        int r = (_rgb & 0x00ff0000) >> 16;
        int g = (_rgb & 0x0000ff00) >> 8;
        int b = (_rgb & 0x000000ff);
        return (299 * r + 587 * g + 114 * b) / 1000;
    }

    public static int inverseColor(int _rgb) {
        int a = (_rgb & 0xff000000) >> 24;
        int r = (_rgb & 0x00ff0000) >> 16;
        int g = (_rgb & 0x0000ff00) >> 8;
        int b = (_rgb & 0x000000ff);
        r = 255 - r;
        g = 255 - g;
        b = 255 - b;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Color inverseColor(Color _c) {
        int r = inverseColor(_c.getRGB());
        return intern(new Color(r, true));
    }

    public static Color adaptForeground(Color _fg, Color _bg) {
        if ((_fg != null) && (_bg != null)) {
            if (Math.abs(BuLib.computeLuminance(_bg) - BuLib.computeLuminance(_fg)) < 80) return BuLib.inverseColor(_fg);
        }
        return _fg;
    }

    public static int mixColors(int _rgb1, int _rgb2) {
        int a1 = (_rgb1 & 0xff000000) >> 24;
        int r1 = (_rgb1 & 0x00ff0000) >> 16;
        int g1 = (_rgb1 & 0x0000ff00) >> 8;
        int b1 = (_rgb1 & 0x000000ff);
        int a2 = (_rgb2 & 0xff000000) >> 24;
        int r2 = (_rgb2 & 0x00ff0000) >> 16;
        int g2 = (_rgb2 & 0x0000ff00) >> 8;
        int b2 = (_rgb2 & 0x000000ff);
        int a = (a1 + a2) / 2;
        int r = (r1 + r2) / 2;
        int g = (g1 + g2) / 2;
        int b = (b1 + b2) / 2;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Color mixColors(Color _a, Color _b) {
        int r = mixColors(_a.getRGB(), _b.getRGB());
        return intern(new Color(r, true));
    }

    public static void setColor(Graphics _g, Color _c) {
        _g.setColor(getColor(_c, true));
    }

    /**
   * Sets color to a graphics.
   */
    public static void setColor(Graphics _g, Color _c, boolean _e) {
        _g.setColor(getColor(_c, _e));
    }

    public static Color getColor(Color _c) {
        return getColor(_c, true);
    }

    public static int getColor(int _c) {
        return getColor(_c, true);
    }

    /**
   * Adapts a color according to the theme filters.
   */
    public static Color getColor(Color _c, boolean _e) {
        Color r = _c;
        Object o = null;
        if (!_e) o = UIManager.get("Theme.disabledColorFilter");
        if (o == null) o = UIManager.get("Theme.colorFilter");
        if (o instanceof RGBImageFilter) r = intern(new Color(((RGBImageFilter) o).filterRGB(0, 0, _c.getRGB())));
        return r;
    }

    /**
   * Adapts a color according to the theme filters.
   */
    public static int getColor(int _c, boolean _e) {
        int r = _c;
        Object o = null;
        if (!_e) o = UIManager.get("Theme.disabledColorFilter");
        if (o == null) o = UIManager.get("Theme.colorFilter");
        if (o instanceof RGBImageFilter) r = ((RGBImageFilter) o).filterRGB(0, 0, _c);
        return r;
    }

    /**
   * Activates antialiasing.
   */
    public static void setAntialiasing(Graphics _g) {
        setAntialiasing(_g, BuPreferences.BU.getBooleanProperty("antialias.all", false));
    }

    /**
   * Activates antialiasing.
   */
    public static void setAntialiasing(Component _c, Graphics _g) {
        setAntialiasing(_c, _g, BuPreferences.BU.getBooleanProperty("antialias.all", false));
    }

    /**
   * Enables or disables antialiasing.
   */
    public static void setAntialiasing(Graphics _g, boolean _e) {
        setAntialiasing(null, _g, _e);
    }

    /**
   * Enables or disables antialiasing.
   */
    public static void setAntialiasing(Component _c, Graphics _g, boolean _e) {
        if (_g instanceof PrintGraphics) return;
        if (_g instanceof Graphics2D) {
            ((Graphics2D) _g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, (_e ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));
            ((Graphics2D) _g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, (_e ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF));
            ((Graphics2D) _g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    private static final String NO_AUTO_MNEMONIC = "NO_AUTO_MNEMONIC";

    public static void setAutoMnemonic(JComponent _c, boolean _b) {
        _c.putClientProperty(NO_AUTO_MNEMONIC, (_b ? Boolean.TRUE : Boolean.FALSE));
    }

    public static boolean getAutoMnemonic(JComponent _c) {
        return (_c.getClientProperty(NO_AUTO_MNEMONIC) != Boolean.FALSE);
    }

    public static void computeMnemonics(Container _rootpane) {
        computeMnemonics(_rootpane, null);
    }

    public static void computeMnemonics(Container _rootpane, ActionListener _al) {
        Vector v = BuLib.getAllSubComponents(_rootpane);
        Hashtable t = new Hashtable();
        Hashtable u = new Hashtable();
        int lv = v.size();
        for (int i = 0; i + 1 < lv; i++) {
            Object c = v.elementAt(i);
            if (c instanceof JLabel) {
                JLabel lb = (JLabel) c;
                if (lb.getLabelFor() == null) {
                    Component tg = (Component) v.elementAt(i + 1);
                    if (!tg.isFocusable() && (tg instanceof Container)) {
                        Vector w = BuLib.getAllSubComponents((Container) tg);
                        int lw = w.size();
                        tg = null;
                        for (int j = 0; j < lw; j++) {
                            Component d = (Component) w.elementAt(j);
                            if (d.isFocusable()) {
                                tg = d;
                                break;
                            }
                        }
                    }
                    if (tg != null) {
                        lb.setLabelFor(tg);
                        u.put(tg, lb);
                    }
                }
            }
        }
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
            Object c = e.nextElement();
            if (u.get(c) != null) {
            } else if (c instanceof AbstractButton) {
                AbstractButton ab = (AbstractButton) c;
                boolean auto = getAutoMnemonic(ab);
                int mn = ab.getMnemonic();
                if (auto && (mn <= 0) && ab.isRequestFocusEnabled()) {
                    String tx = candidateMnemonics(ab.getText());
                    if (tx != null) for (int j = 0; j < tx.length(); j++) {
                        mn = tx.charAt(j);
                        if (t.get(FuFactoryInteger.get(mn)) == null) {
                            t.put(FuFactoryInteger.get(mn), ab);
                            ab.setMnemonic(mn);
                            if (_al != null) ab.registerKeyboardAction(_al, KeyStroke.getKeyStroke(mn, InputEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
                            break;
                        }
                    }
                }
            } else if (c instanceof JLabel) {
                JLabel lb = (JLabel) c;
                boolean auto = getAutoMnemonic(lb);
                int mn = lb.getDisplayedMnemonic();
                if (auto && (mn <= 0) && (lb.getLabelFor() != null)) {
                    String tx = candidateMnemonics(lb.getText());
                    if (tx != null) for (int j = 0; j < tx.length(); j++) {
                        mn = tx.charAt(j);
                        if (t.get(FuFactoryInteger.get(mn)) == null) {
                            t.put(FuFactoryInteger.get(mn), lb);
                            lb.setDisplayedMnemonic(mn);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static String candidateMnemonics(String _s) {
        if (_s == null) return null;
        String s = _s;
        s = s.trim();
        if (s.endsWith(":")) s = s.substring(0, s.length() - 1);
        if (s.endsWith("...")) s = s.substring(0, s.length() - 3);
        s = s.trim();
        if ("".equals(s)) return s;
        StringBuffer r = new StringBuffer(30);
        int i;
        if ((s.length() > 2) && (s.charAt(1) == ' ')) {
            char c = s.charAt(0);
            if ((c >= '0') && (c <= '9')) r.append(c);
        }
        {
            char c = s.charAt(0);
            if ((c >= 'A') && (c <= 'Z')) r.append(c);
        }
        s = s.toUpperCase();
        for (i = s.length() - 1; i >= 0; i--) if (s.charAt(i) == ' ') {
            char c = s.charAt(i + 1);
            int j = s.indexOf(' ', i + 1);
            if ((c >= 'A') && (c <= 'Z') && ((j < 0) || (j > i + 5))) r.append(c);
        }
        for (i = 0; i <= s.length() - 1; i++) if (s.charAt(i) == '-') {
            char c = s.charAt(i + 1);
            int j = s.indexOf('-', i + 1);
            if ((c >= 'A') && (c <= 'Z') && ((j < 0) || (j > i + 1))) r.append(c);
        }
        if (r.length() == 0) {
            char c = s.charAt(0);
            if ((c >= 'A') && (c <= 'Z')) r.append(c);
        }
        while (!"".equals(s)) {
            String t = s;
            int j = s.lastIndexOf(' ');
            if (j >= 0) {
                t = s.substring(j + 1);
                s = s.substring(0, j);
            } else {
                t = s;
                s = "";
            }
            if (t.length() > 4) {
                t = t.substring(0, t.length() - 1);
                for (i = 0; i < t.length(); i++) {
                    char c = t.charAt(i);
                    if ((c >= 'A') && (c <= 'Z') && ("AEIOUY".indexOf(c) < 0)) {
                        r.append(c);
                    }
                }
                for (i = 0; i < t.length(); i++) {
                    char c = t.charAt(i);
                    if ((c >= 'A') && (c <= 'Z') && ("AEIOUY".indexOf(c) >= 0)) {
                        r.append(c);
                    }
                }
            }
        }
        char c, o;
        int l = r.length();
        StringBuffer f = new StringBuffer(l);
        o = 0;
        for (i = 0; i < l; i++) {
            c = r.charAt(i);
            if ((c != '=') && (c != o)) {
                f.append(c);
                o = c;
            }
        }
        return f.toString();
    }

    public static final void focusScroll(JComponent _c, FocusEvent _evt) {
        if (_evt.getID() == FocusEvent.FOCUS_GAINED) _c.scrollRectToVisible(new Rectangle(0, 0, _c.getWidth(), _c.getHeight()));
    }

    private static boolean ok_ocean_ = false;

    private static boolean ok_metal_ = false;

    private static boolean ok_slaf_ = false;

    private static boolean is_ocean_ = false;

    private static boolean is_metal_ = false;

    private static boolean is_slaf_ = false;

    public static void forgetLnf() {
        ok_ocean_ = ok_metal_ = ok_slaf_ = false;
    }

    /**
   * @return true if lnf is metal/ocean
   */
    public static boolean isOcean() {
        if (ok_ocean_) return is_ocean_;
        is_ocean_ = (FuLib.jdk() >= 1.5) && isMetal();
        ok_ocean_ = true;
        return is_ocean_;
    }

    /**
   * @return true if lnf is metal
   */
    public static boolean isMetal() {
        if (ok_metal_) return is_metal_;
        String n = UIManager.getLookAndFeel().getClass().getName();
        is_metal_ = n.endsWith(".MetalLookAndFeel") || n.equals("xxxx.swing.XUIManager$2");
        ok_metal_ = true;
        return is_metal_;
    }

    /**
   * @return true if lnf is motif
   */
    public static boolean isMotif() {
        return UIManager.getLookAndFeel().getClass().getName().endsWith(".MotifLookAndFeel");
    }

    /**
   * @return true if lnf is slaf
   */
    public static boolean isSlaf() {
        if (ok_slaf_) return is_slaf_;
        is_slaf_ = UIManager.getLookAndFeel().getClass().getName().endsWith(".SlafLookAndFeel");
        ok_slaf_ = true;
        return is_slaf_;
    }

    /**
   * @return true if lnf is kunststoff
   */
    public static boolean isKunststoff() {
        return UIManager.getLookAndFeel().getClass().getName().endsWith(".KunststoffLookAndFeel");
    }

    /**
   * @return true if lnf is liquid
   */
    public static boolean isLiquid() {
        return UIManager.getLookAndFeel().getClass().getName().endsWith(".LiquidLookAndFeel");
    }

    /**
   * @return true if lnf is plastic
   */
    public static boolean isPlastic() {
        String s = UIManager.getLookAndFeel().getClass().getName();
        return s.endsWith(".PlasticLookAndFeel") || s.endsWith(".Plastic3DLookAndFeel") || s.endsWith(".PlasticXPLookAndFeel");
    }

    /**
   * @return true if lnf is liquid
   */
    public static boolean isSynthetica() {
        return UIManager.getLookAndFeel().getClass().getName().endsWith(".SyntheticaStandardLookAndFeel");
    }

    private static double swing_ = 0.;

    public static final double swing() {
        if (swing_ == 0.) {
            try {
                Window.class.getMethod("getOwner", new Class[0]);
                swing_ = 1.2;
            } catch (NoSuchMethodException ex) {
                swing_ = 1.1;
            }
        }
        return swing_;
    }

    /**
   * @deprecated no more support for swing 1.0
   * @return true if swing 1.0 is available
   */
    public static final boolean isSwing10() {
        return false;
    }

    /**
   * @deprecated no more support for swing 1.0
   * @return true if swing 1.1 is available
   */
    public static final boolean isSwing11() {
        return true;
    }

    public static final boolean getUIBoolean(String _key) {
        return getUIBoolean(_key, false);
    }

    public static final boolean getUIBoolean(String _key, boolean _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Boolean) ? _default : ((Boolean) o).booleanValue();
    }

    public static final Color getUIColor(String _key) {
        return getUIColor(_key, null);
    }

    public static final Color getUIColor(String _key, Color _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Color) ? _default : (Color) o;
    }

    public static final Dimension getUIDimension(String _key) {
        return getUIDimension(_key, null);
    }

    public static final Dimension getUIDimension(String _key, Dimension _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Dimension) ? _default : (Dimension) o;
    }

    public static final Font getUIFont(String _key) {
        return getUIFont(_key, null);
    }

    public static final Font getUIFont(String _key, Font _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Font) ? _default : (Font) o;
    }

    public static final Insets getUIInsets(String _key) {
        return getUIInsets(_key, null);
    }

    public static final Insets getUIInsets(String _key, Insets _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Insets) ? _default : (Insets) o;
    }

    public static final int getUIInt(String _key) {
        return getUIInt(_key, 0);
    }

    public static final int getUIInt(String _key, int _default) {
        Object o = UIManager.get(_key);
        return !(o instanceof Integer) ? _default : ((Integer) o).intValue();
    }

    public static final String[] BU_CLASSES = FuEmptyArrays.STRING0;

    public static final String[] SWING_CLASSES = FuEmptyArrays.STRING0;

    public static boolean hasAlpha(final Image _image) {
        if (_image instanceof BufferedImage) {
            final BufferedImage bimage = (BufferedImage) _image;
            return bimage.getColorModel().hasAlpha();
        }
        final PixelGrabber pg = new PixelGrabber(_image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (final InterruptedException e) {
        }
        return pg.getColorModel().hasAlpha();
    }

    public static BufferedImage convert(final Image _img) {
        if (_img == null) {
            return null;
        }
        if (_img instanceof BufferedImage) {
            return (BufferedImage) _img;
        }
        final Image image = new ImageIcon(_img).getImage();
        final boolean hasAlpha = hasAlpha(image);
        BufferedImage bimage = null;
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
            final GraphicsDevice gs = ge.getDefaultScreenDevice();
            final GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (final HeadlessException e) {
        }
        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        final Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    public static void save(Image _image, String _fichier, String _format) throws IOException {
        String fichier = _fichier;
        String format = _format;
        format = format.toLowerCase();
        if (format.equals("jpeg")) format = "jpg";
        if (!fichier.endsWith("." + format)) fichier += "." + format;
        ImageIO.write(convert(_image), format, new File(fichier));
    }

    public static boolean isPalette(JInternalFrame _internalFrame) {
        return _internalFrame.getClientProperty("JInternalFrame.isPalette") == Boolean.TRUE;
    }

    /**
   * Donne la m�me largeur pr�f�rentielle � tous les composants.
   * @param _components Les composant.
   */
    public static void giveSameWidth(JComponent[] _components) {
        int max = 0;
        for (int i = 0; i < _components.length; i++) {
            if ((i == 0) || (max < _components[i].getPreferredSize().width)) {
                max = _components[i].getPreferredSize().width;
            }
        }
        for (int i = 0; i < _components.length; i++) {
            _components[i].setPreferredSize(new Dimension(max, _components[i].getPreferredSize().height));
        }
    }

    /**
   * Donne la m�me hauteur pr�f�rentielle � tous les composants.
   * @param _components Les composant.
   */
    public static void giveSameHeight(JComponent[] _components) {
        int max = 0;
        for (int i = 0; i < _components.length; i++) {
            if ((i == 0) || (max < _components[i].getPreferredSize().height)) {
                max = _components[i].getPreferredSize().height;
            }
        }
        for (int i = 0; i < _components.length; i++) {
            _components[i].setPreferredSize(new Dimension(_components[i].getPreferredSize().width, max));
        }
    }
}
