package de.muntjak.tinylookandfeel;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.text.View;
import de.muntjak.tinylookandfeel.borders.TinyPopupMenuBorder;
import de.muntjak.tinylookandfeel.util.ColorRoutines;

/**
 * Tiny MenuItemUI implementation.

 * @version 1.4.0
 * @author Hans Bickel
 */
public class TinyMenuItemUI extends MenuItemUI {

    private static final boolean TRACE = false;

    private static final boolean VERBOSE = false;

    private static final boolean DEBUG = false;

    private static final int DEFAULT_ICON_GAP = 4;

    private static final int DEFAULT_ACC_GAP = 8;

    private static final int DEFAULT_ARROW_GAP = 12;

    private static final int CHECK_WIDTH = 10 + DEFAULT_ICON_GAP;

    private static final int ARROW_WIDTH = 4 + DEFAULT_ARROW_GAP;

    protected JMenuItem menuItem = null;

    protected Color selectionBackground;

    protected Color selectionForeground;

    protected Color disabledForeground;

    protected Color acceleratorForeground;

    protected Color acceleratorSelectionForeground;

    private String acceleratorDelimiter;

    protected Font acceleratorFont;

    protected MouseInputListener mouseInputListener;

    protected MenuDragMouseListener menuDragMouseListener;

    protected MenuKeyListener menuKeyListener;

    private PropertyChangeListener propertyChangeListener;

    protected Icon arrowIcon = null;

    protected Icon checkIcon = null;

    protected boolean oldBorderPainted;

    /** Used for accelerator binding, lazily created. */
    InputMap windowInputMap;

    /** Client property keys */
    public static final String MAX_TEXT_WIDTH = "TinyMenuItemUI.maxTextWidth";

    public static final String MAX_ICON_WIDTH = "TinyMenuItemUI.maxIconWidth";

    public static final String MAX_LABEL_WIDTH = "TinyMenuItemUI.maxLabelWidth";

    public static final String MAX_ACC_WIDTH = "TinyMenuItemUI.maxAccWidth";

    public void installUI(JComponent c) {
        menuItem = (JMenuItem) c;
        installDefaults();
        installComponents(menuItem);
        installListeners();
        installKeyboardActions();
    }

    /**
	 * @since 1.3
	 */
    protected void installComponents(JMenuItem menuItem) {
        BasicHTML.updateRenderer(menuItem, menuItem.getText());
    }

    protected String getPropertyPrefix() {
        return "MenuItem";
    }

    protected void installListeners() {
        if ((mouseInputListener = createMouseInputListener(menuItem)) != null) {
            menuItem.addMouseListener(mouseInputListener);
            menuItem.addMouseMotionListener(mouseInputListener);
        }
        if ((menuDragMouseListener = createMenuDragMouseListener(menuItem)) != null) {
            menuItem.addMenuDragMouseListener(menuDragMouseListener);
        }
        if ((propertyChangeListener = createPropertyChangeListener(menuItem)) != null) {
            menuItem.addPropertyChangeListener(propertyChangeListener);
        }
    }

    protected void installKeyboardActions() {
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(menuItem, actionMap);
        updateAcceleratorBinding();
    }

    public void uninstallUI(JComponent c) {
        menuItem = (JMenuItem) c;
        uninstallDefaults();
        uninstallComponents(menuItem);
        uninstallListeners();
        uninstallKeyboardActions();
        Container parent = menuItem.getParent();
        if (parent != null && (parent instanceof JComponent)) {
            JComponent p = (JComponent) parent;
            p.putClientProperty(MAX_ACC_WIDTH, null);
            p.putClientProperty(MAX_TEXT_WIDTH, null);
            p.putClientProperty(MAX_ICON_WIDTH, null);
            p.putClientProperty(MAX_LABEL_WIDTH, null);
        }
        menuItem = null;
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(menuItem);
        menuItem.setBorderPainted(oldBorderPainted);
        if (menuItem.getMargin() instanceof UIResource) menuItem.setMargin(null);
        if (arrowIcon instanceof UIResource) arrowIcon = null;
        if (checkIcon instanceof UIResource) checkIcon = null;
    }

    /**
	 * @since 1.3
	 */
    protected void uninstallComponents(JMenuItem menuItem) {
        BasicHTML.updateRenderer(menuItem, "");
    }

    protected void uninstallListeners() {
        if (mouseInputListener != null) {
            menuItem.removeMouseListener(mouseInputListener);
            menuItem.removeMouseMotionListener(mouseInputListener);
        }
        if (menuDragMouseListener != null) {
            menuItem.removeMenuDragMouseListener(menuDragMouseListener);
        }
        if (propertyChangeListener != null) {
            menuItem.removePropertyChangeListener(propertyChangeListener);
        }
        mouseInputListener = null;
        menuDragMouseListener = null;
        menuKeyListener = null;
        propertyChangeListener = null;
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(menuItem, null);
        if (windowInputMap != null) {
            SwingUtilities.replaceUIInputMap(menuItem, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
            windowInputMap = null;
        }
    }

    protected MouseInputListener createMouseInputListener(JComponent c) {
        return new MouseInputHandler();
    }

    protected MenuDragMouseListener createMenuDragMouseListener(JComponent c) {
        return new MenuDragMouseHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener(JComponent c) {
        return new PropertyChangeHandler();
    }

    ActionMap getActionMap() {
        String propertyPrefix = getPropertyPrefix();
        String uiKey = propertyPrefix + ".actionMap";
        ActionMap am = (ActionMap) UIManager.get(uiKey);
        if (am == null) {
            am = createActionMap();
            UIManager.getLookAndFeelDefaults().put(uiKey, am);
        }
        return am;
    }

    ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put("doClick", new ClickAction());
        return map;
    }

    InputMap createInputMap(int condition) {
        if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
            return new ComponentInputMapUIResource(menuItem);
        }
        return null;
    }

    void updateAcceleratorBinding() {
        KeyStroke accelerator = menuItem.getAccelerator();
        if (windowInputMap != null) {
            windowInputMap.clear();
        }
        if (accelerator != null) {
            if (windowInputMap == null) {
                windowInputMap = createInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                SwingUtilities.replaceUIInputMap(menuItem, JComponent.WHEN_IN_FOCUSED_WINDOW, windowInputMap);
            }
            windowInputMap.put(accelerator, "doClick");
        }
    }

    public Dimension getMinimumSize(JComponent c) {
        Dimension d = null;
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d = getPreferredSize(c);
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }

    public Dimension getPreferredSize(JComponent c) {
        if (Theme.menuAllowTwoIcons.getValue()) {
            return getPreferredMenuItemSizeTwoIcons(c, checkIcon, arrowIcon);
        } else {
            return getPreferredMenuItemSizeOneIcon(c, checkIcon, arrowIcon);
        }
    }

    /**
	 * Renders the text of the current menu item.
	 * <p>
	 * @param g graphics context
	 * @param menuItem menu item to render
	 * @param textRect bounding rectangle for rendering the text
	 * @param text string to render
	 * @since 1.4
	 */
    protected void paintText(Graphics g, JMenuItem menuItem, Rectangle textRect, String text) {
        ButtonModel model = menuItem.getModel();
        FontMetrics fm = g.getFontMetrics();
        int mnemIndex = menuItem.getDisplayedMnemonicIndex();
        if (!model.isEnabled()) {
            if (isTopLevelMenu()) {
                g.setColor(Theme.menuDisabledFgColor.getColor());
            } else {
                g.setColor(Theme.menuItemDisabledFgColor.getColor());
            }
            TinyUtils.drawStringUnderlineCharAt(menuItem, g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
        } else {
            if (isTopLevelMenu()) {
                if (menuItem.getClientProperty("rollover") == Boolean.TRUE && Theme.menuRollover.getValue() && !model.isSelected()) {
                    g.setColor(Theme.menuRolloverFgColor.getColor());
                } else {
                    if (!(menuItem.getForeground() instanceof ColorUIResource)) {
                        g.setColor(menuItem.getForeground());
                    } else {
                        g.setColor(Theme.menuFontColor.getColor());
                    }
                }
            } else if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected())) {
                g.setColor(Theme.menuItemSelectedTextColor.getColor());
            } else {
                g.setColor(menuItem.getForeground());
            }
            TinyUtils.drawStringUnderlineCharAt(menuItem, g, text, mnemIndex, textRect.x, textRect.y + fm.getAscent());
        }
    }

    public Dimension getMaximumSize(JComponent c) {
        Dimension d = null;
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d = getPreferredSize(c);
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }

    protected static Rectangle zeroRect = new Rectangle(0, 0, 0, 0);

    protected static Rectangle iconRect = new Rectangle();

    protected static Rectangle textRect = new Rectangle();

    protected static Rectangle acceleratorRect = new Rectangle();

    protected static Rectangle checkIconRect = new Rectangle();

    protected static Rectangle arrowIconRect = new Rectangle();

    protected static Rectangle viewRect = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

    protected static Rectangle rect = new Rectangle();

    private void resetRects() {
        iconRect.setBounds(zeroRect);
        textRect.setBounds(zeroRect);
        acceleratorRect.setBounds(zeroRect);
        checkIconRect.setBounds(zeroRect);
        arrowIconRect.setBounds(zeroRect);
        viewRect.setBounds(0, 0, Short.MAX_VALUE, Short.MAX_VALUE);
        rect.setBounds(zeroRect);
    }

    /**
	 * We draw the background in paintMenuItem()
	 * so override update (which fills the background of opaque
	 * components by default) to just call paint().
	 *
	 */
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }

    public void paint(Graphics g, JComponent c) {
        if (Theme.menuAllowTwoIcons.getValue()) {
            paintMenuItemTwoIcons(g, c, checkIcon, arrowIcon, selectionBackground, selectionForeground, DEFAULT_ICON_GAP);
        } else {
            paintMenuItemOneIcon(g, c, checkIcon, arrowIcon, selectionBackground, selectionForeground, DEFAULT_ICON_GAP);
        }
    }

    private boolean isTopLevelMenu() {
        return (menuItem instanceof JMenu) && ((JMenu) menuItem).isTopLevelMenu();
    }

    private boolean isTopLevelMenu(Component item) {
        return (item instanceof JMenu) && ((JMenu) item).isTopLevelMenu();
    }

    public MenuElement[] getPath() {
        MenuSelectionManager m = MenuSelectionManager.defaultManager();
        MenuElement oldPath[] = m.getSelectedPath();
        MenuElement newPath[];
        int i = oldPath.length;
        if (i == 0) return new MenuElement[0];
        Component parent = menuItem.getParent();
        if (oldPath[i - 1].getComponent() == parent) {
            newPath = new MenuElement[i + 1];
            System.arraycopy(oldPath, 0, newPath, 0, i);
            newPath[i] = menuItem;
        } else {
            int j;
            for (j = oldPath.length - 1; j >= 0; j--) {
                if (oldPath[j].getComponent() == parent) break;
            }
            newPath = new MenuElement[j + 2];
            System.arraycopy(oldPath, 0, newPath, 0, j + 1);
            newPath[j + 1] = menuItem;
        }
        return newPath;
    }

    protected class MouseInputHandler implements MouseInputListener {

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
            Point p = e.getPoint();
            if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight()) {
                doClick(manager);
            } else {
                manager.processMouseEvent(e);
            }
        }

        public void mouseEntered(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
            int modifiers = e.getModifiers();
            if ((modifiers & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
                MenuSelectionManager.defaultManager().processMouseEvent(e);
            } else {
                manager.setSelectedPath(getPath());
            }
        }

        public void mouseExited(MouseEvent e) {
            MenuSelectionManager manager = MenuSelectionManager.defaultManager();
            int modifiers = e.getModifiers();
            if ((modifiers & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
                MenuSelectionManager.defaultManager().processMouseEvent(e);
            } else {
                MenuElement path[] = manager.getSelectedPath();
                if (path.length > 1) {
                    MenuElement newPath[] = new MenuElement[path.length - 1];
                    int i, c;
                    for (i = 0, c = path.length - 1; i < c; i++) newPath[i] = path[i];
                    manager.setSelectedPath(newPath);
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            MenuSelectionManager.defaultManager().processMouseEvent(e);
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private class MenuDragMouseHandler implements MenuDragMouseListener {

        public void menuDragMouseEntered(MenuDragMouseEvent e) {
        }

        public void menuDragMouseDragged(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            manager.setSelectedPath(path);
        }

        public void menuDragMouseExited(MenuDragMouseEvent e) {
        }

        public void menuDragMouseReleased(MenuDragMouseEvent e) {
            MenuSelectionManager manager = e.getMenuSelectionManager();
            MenuElement path[] = e.getPath();
            Point p = e.getPoint();
            if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight()) {
                doClick(manager);
            } else {
                manager.clearSelectedPath();
            }
        }
    }

    private class PropertyChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("labelFor") || name.equals("displayedMnemonic") || name.equals("accelerator")) {
                updateAcceleratorBinding();
            } else if (name.equals("text") || "font".equals(name) || "foreground".equals(name)) {
                JMenuItem lbl = ((JMenuItem) e.getSource());
                String text = lbl.getText();
                BasicHTML.updateRenderer(lbl, text);
            }
        }
    }

    private static class ClickAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            JMenuItem mi = (JMenuItem) e.getSource();
            MenuSelectionManager.defaultManager().clearSelectedPath();
            mi.doClick();
        }
    }

    /**
	 * Call this method when a menu item is to be activated.
	 * This method handles some of the details of menu item activation
	 * such as clearing the selected path and messaging the 
	 * JMenuItem's doClick() method.
	 *
	 * @param msm  A MenuSelectionManager. The visual feedback and 
	 *             internal bookkeeping tasks are delegated to 
	 *             this MenuSelectionManager. If <code>null</code> is
	 *             passed as this argument, the 
	 *             <code>MenuSelectionManager.defaultManager</code> is
	 *             used.
	 * @see MenuSelectionManager
	 * @see JMenuItem#doClick(int)
	 * @since 1.4
	 */
    protected void doClick(MenuSelectionManager msm) {
        if (msm == null) {
            msm = MenuSelectionManager.defaultManager();
        }
        msm.clearSelectedPath();
        menuItem.doClick(0);
    }

    /** 
	 * This is to see if the menu item in question is part of the 
	 * system menu on an internal frame.
	 * The Strings that are being checked can be found in 
	 * MetalInternalFrameTitlePaneUI.java,
	 * WindowsInternalFrameTitlePaneUI.java, and
	 * MotifInternalFrameTitlePaneUI.java.
	 *
	 * @since 1.4
	 */
    private boolean isInternalFrameSystemMenu() {
        String actionCommand = menuItem.getActionCommand();
        if ((actionCommand == "Close") || (actionCommand == "Minimize") || (actionCommand == "Restore") || (actionCommand == "Maximize")) {
            return true;
        } else {
            return false;
        }
    }

    public static ComponentUI createUI(JComponent c) {
        return new TinyMenuItemUI();
    }

    protected void installDefaults() {
        String prefix = getPropertyPrefix();
        acceleratorFont = UIManager.getFont(prefix + ".acceleratorFont");
        menuItem.setOpaque(true);
        if (menuItem.getMargin() == null || (menuItem.getMargin() instanceof UIResource)) {
            menuItem.setMargin(UIManager.getInsets(prefix + ".margin"));
        }
        LookAndFeel.installBorder(menuItem, prefix + ".border");
        oldBorderPainted = menuItem.isBorderPainted();
        menuItem.setBorderPainted(((Boolean) (UIManager.get(prefix + ".borderPainted"))).booleanValue());
        LookAndFeel.installColorsAndFont(menuItem, prefix + ".background", prefix + ".foreground", prefix + ".font");
        if (selectionBackground == null || selectionBackground instanceof UIResource) {
            selectionBackground = UIManager.getColor(prefix + ".selectionBackground");
        }
        if (selectionForeground == null || selectionForeground instanceof UIResource) {
            selectionForeground = UIManager.getColor(prefix + ".selectionForeground");
        }
        if (disabledForeground == null || disabledForeground instanceof UIResource) {
            disabledForeground = UIManager.getColor(prefix + ".disabledForeground");
        }
        if (acceleratorForeground == null || acceleratorForeground instanceof UIResource) {
            acceleratorForeground = UIManager.getColor(prefix + ".acceleratorForeground");
        }
        if (acceleratorSelectionForeground == null || acceleratorSelectionForeground instanceof UIResource) {
            acceleratorSelectionForeground = UIManager.getColor(prefix + ".acceleratorSelectionForeground");
        }
        acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
        if (acceleratorDelimiter == null) {
            acceleratorDelimiter = "+";
        }
        if (arrowIcon == null || arrowIcon instanceof UIResource) {
            arrowIcon = UIManager.getIcon(prefix + ".arrowIcon");
        }
        if (checkIcon == null || checkIcon instanceof UIResource) {
            checkIcon = UIManager.getIcon(prefix + ".checkIcon");
        }
    }

    /**
	 * 
	 * @param accelerator
	 * @return the accelerator text or an empty string if argument is null
	 */
    private String getAcceleratorText(KeyStroke accelerator) {
        if (accelerator == null) return "";
        String acceleratorText = "";
        int modifiers = accelerator.getModifiers();
        if (modifiers > 0) {
            acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
            acceleratorText += acceleratorDelimiter;
        }
        int keyCode = accelerator.getKeyCode();
        if (keyCode != 0) {
            acceleratorText += KeyEvent.getKeyText(keyCode);
        } else {
            acceleratorText += accelerator.getKeyChar();
        }
        return acceleratorText;
    }

    /**
	 * Calculate and return the preferred size of a menu item.
	 * @param c
	 * @param checkIcon
	 * @param arrowIcon
	 * @return the preferred size of the specified component
	 */
    protected Dimension getPreferredMenuItemSizeTwoIcons(JComponent c, Icon checkIcon, Icon arrowIcon) {
        JMenuItem b = (JMenuItem) c;
        String text = b.getText();
        String acceleratorText = getAcceleratorText(b.getAccelerator());
        boolean isTopLevelMenu = isTopLevelMenu(c);
        Icon icon = b.getIcon();
        Font font = b.getFont();
        FontMetrics fm = b.getFontMetrics(font);
        FontMetrics fmAccel = b.getFontMetrics(acceleratorFont);
        int horizontalAlignment = b.getHorizontalAlignment();
        int horizontalTextPosition = b.getHorizontalTextPosition();
        resetRects();
        layoutMenuItem(fm, text, fmAccel, acceleratorText, icon, checkIcon, arrowIcon, b.getVerticalAlignment(), horizontalAlignment, b.getVerticalTextPosition(), horizontalTextPosition, viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, text == null ? 0 : DEFAULT_ICON_GAP, true);
        rect.setBounds(textRect);
        rect = SwingUtilities.computeUnion(iconRect.x, iconRect.y, iconRect.width, iconRect.height, rect);
        Insets insets = b.getInsets();
        if (isTopLevelMenu) {
            if (VERBOSE) {
                System.out.println(b.getText() + ".rect=" + p(rect) + ", insets=" + p(insets));
            }
            rect.width += insets.left + insets.right;
            rect.height += insets.top + insets.bottom;
            rect.width += TinyPopupMenuBorder.SHADOW_SIZE;
            if (VERBOSE) {
                System.out.println("  Returning: " + p(rect));
            }
            return rect.getSize();
        }
        JComponent parent = (JComponent) c.getParent();
        Integer val = (Integer) parent.getClientProperty(MAX_TEXT_WIDTH);
        int maxTextWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_LABEL_WIDTH);
        int maxLabelWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ICON_WIDTH);
        int maxIconWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ACC_WIDTH);
        int maxAccWidth = (val == null ? 0 : val.intValue());
        if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING || horizontalTextPosition == SwingConstants.CENTER) {
            if (rect.width > maxTextWidth) {
                maxTextWidth = rect.width;
                parent.putClientProperty(MAX_TEXT_WIDTH, new Integer(maxTextWidth));
            } else if (maxTextWidth > 0) {
                rect.width = maxTextWidth;
            }
        } else {
            if (textRect.width > maxTextWidth) {
                maxTextWidth = textRect.width;
                parent.putClientProperty(MAX_TEXT_WIDTH, new Integer(maxTextWidth));
            } else if (maxTextWidth > 0 && iconRect.width < maxTextWidth) {
                rect.width += maxTextWidth - textRect.width;
            }
            if (horizontalAlignment == SwingConstants.LEFT || horizontalAlignment == SwingConstants.LEADING) {
                if (iconRect.width > maxIconWidth) {
                    maxIconWidth = iconRect.width;
                    parent.putClientProperty(MAX_ICON_WIDTH, new Integer(maxIconWidth));
                } else if (iconRect.width > 0) {
                    rect.width += maxIconWidth - iconRect.width;
                }
            }
        }
        if (rect.width > maxLabelWidth) {
            maxLabelWidth = rect.width;
            parent.putClientProperty(MAX_LABEL_WIDTH, new Integer(maxLabelWidth));
        } else if (maxLabelWidth > 0) {
            rect.width = maxLabelWidth;
        }
        if (acceleratorRect.width > maxAccWidth) {
            maxAccWidth = acceleratorRect.width;
            parent.putClientProperty(MAX_ACC_WIDTH, new Integer(maxAccWidth));
        }
        rect.width += Math.max(insets.left + insets.right, 11);
        rect.height += insets.top + insets.bottom;
        rect.width += CHECK_WIDTH + ARROW_WIDTH;
        if (maxAccWidth > 0) {
            rect.width += maxAccWidth + DEFAULT_ACC_GAP;
        }
        if (VERBOSE) {
            System.out.println("getPreferredMenuItemSize() \"" + b.getText() + "\" (" + b.getClass().getName() + ")");
            System.out.println("  checkIcon.width=" + checkIconRect.width + ", icon.width=" + iconRect.width + ", text.width=" + textRect.width + ", accelerator.width=" + acceleratorRect.width + ", maxTextWidth=" + maxTextWidth + ", maxLabelWidth=" + maxLabelWidth);
            System.out.println("  Returning " + p(rect.getSize()));
        }
        return rect.getSize();
    }

    /**
	 * Calculate and return the preferred size of a menu item.
	 * @param c
	 * @param checkIcon
	 * @param arrowIcon
	 * @return the preferred size of the specified component
	 */
    protected Dimension getPreferredMenuItemSizeOneIcon(JComponent c, Icon checkIcon, Icon arrowIcon) {
        JMenuItem b = (JMenuItem) c;
        String text = b.getText();
        String acceleratorText = getAcceleratorText(b.getAccelerator());
        boolean isTopLevelMenu = isTopLevelMenu(c);
        Icon icon = b.getIcon();
        Font font = b.getFont();
        FontMetrics fm = b.getFontMetrics(font);
        FontMetrics fmAccel = b.getFontMetrics(acceleratorFont);
        int horizontalAlignment = b.getHorizontalAlignment();
        int horizontalTextPosition = b.getHorizontalTextPosition();
        if (icon == null && checkIcon != null) {
            icon = checkIcon;
            checkIcon = null;
        }
        resetRects();
        layoutMenuItem(fm, text, fmAccel, acceleratorText, icon, checkIcon, arrowIcon, b.getVerticalAlignment(), horizontalAlignment, b.getVerticalTextPosition(), horizontalTextPosition, viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, text == null ? 0 : DEFAULT_ICON_GAP, true);
        rect.setBounds(textRect);
        rect = SwingUtilities.computeUnion(iconRect.x, iconRect.y, iconRect.width, iconRect.height, rect);
        Insets insets = b.getInsets();
        if (isTopLevelMenu) {
            if (VERBOSE) {
                System.out.println(b.getText() + ".rect=" + p(rect) + ", insets=" + p(insets));
            }
            rect.width += insets.left + insets.right;
            rect.height += insets.top + insets.bottom;
            rect.width += TinyPopupMenuBorder.SHADOW_SIZE;
            if (VERBOSE) {
                System.out.println("  Returning: " + p(rect));
            }
            return rect.getSize();
        }
        JComponent parent = (JComponent) c.getParent();
        Integer val = (Integer) parent.getClientProperty(MAX_TEXT_WIDTH);
        int maxTextWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_LABEL_WIDTH);
        int maxLabelWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ICON_WIDTH);
        int maxIconWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ACC_WIDTH);
        int maxAccWidth = (val == null ? 0 : val.intValue());
        if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING || horizontalTextPosition == SwingConstants.CENTER) {
            if (rect.width > maxTextWidth) {
                maxTextWidth = rect.width;
                parent.putClientProperty(MAX_TEXT_WIDTH, new Integer(maxTextWidth));
            } else if (maxTextWidth > 0) {
                rect.width = maxTextWidth;
            }
        } else {
            if (textRect.width > maxTextWidth) {
                maxTextWidth = textRect.width;
                parent.putClientProperty(MAX_TEXT_WIDTH, new Integer(maxTextWidth));
            } else if (maxTextWidth > 0 && iconRect.width < maxTextWidth) {
                rect.width += maxTextWidth - textRect.width;
            }
            if (horizontalAlignment == SwingConstants.LEFT || horizontalAlignment == SwingConstants.LEADING) {
                if (iconRect.width > maxIconWidth) {
                    maxIconWidth = iconRect.width;
                    parent.putClientProperty(MAX_ICON_WIDTH, new Integer(maxIconWidth));
                } else if (iconRect.width > 0) {
                    rect.width += maxIconWidth - iconRect.width;
                }
            }
        }
        if (rect.width > maxLabelWidth) {
            maxLabelWidth = rect.width;
            parent.putClientProperty(MAX_LABEL_WIDTH, new Integer(maxLabelWidth));
        } else if (maxLabelWidth > 0) {
            rect.width = maxLabelWidth;
        }
        if (acceleratorRect.width > maxAccWidth) {
            maxAccWidth = acceleratorRect.width;
            parent.putClientProperty(MAX_ACC_WIDTH, new Integer(maxAccWidth));
        }
        rect.width += Math.max(insets.left + insets.right, 11);
        rect.height += insets.top + insets.bottom;
        rect.width += ARROW_WIDTH;
        if (maxAccWidth > 0) {
            rect.width += maxAccWidth + DEFAULT_ACC_GAP;
        }
        if (VERBOSE) {
            System.out.println("getPreferredMenuItemSize() \"" + b.getText() + "\" (" + b.getClass().getName() + ")");
            System.out.println("  checkIcon.width=" + checkIconRect.width + ", icon.width=" + iconRect.width + ", text.width=" + textRect.width + ", accelerator.width=" + acceleratorRect.width + ", maxTextWidth=" + maxTextWidth + ", maxLabelWidth=" + maxLabelWidth);
            System.out.println("  Returning " + p(rect.getSize()));
        }
        return rect.getSize();
    }

    private String p(Rectangle r) {
        if (r == null) return "null";
        return r.x + ", " + r.y + ", " + r.width + ", " + r.height;
    }

    private String p(Insets i) {
        if (i == null) return "null";
        return i.top + ", " + i.left + ", " + i.bottom + ", " + i.right;
    }

    private String p(Dimension d) {
        if (d == null) return "null";
        return d.width + ", " + d.height;
    }

    private String getSwingConstantsString(int htp) {
        if (htp == SwingConstants.CENTER) {
            return "CENTER";
        } else if (htp == SwingConstants.LEADING) {
            return "LEADING";
        } else if (htp == SwingConstants.LEFT) {
            return "LEFT";
        } else if (htp == SwingConstants.RIGHT) {
            return "RIGHT";
        } else if (htp == SwingConstants.TRAILING) {
            return "TRAILING";
        } else {
            return "???";
        }
    }

    private String getSwingConstantsString(int htp, int ha) {
        return getSwingConstantsString(htp) + ", " + getSwingConstantsString(ha);
    }

    protected void paintMenuItemTwoIcons(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int iconGap) {
        JMenuItem b = (JMenuItem) c;
        ButtonModel model = b.getModel();
        JComponent parent = (JComponent) b.getParent();
        Integer val = (Integer) parent.getClientProperty(MAX_ACC_WIDTH);
        int maxAccWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ICON_WIDTH);
        int maxIconWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_LABEL_WIDTH);
        int maxLabelWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_TEXT_WIDTH);
        int maxTextWidth = (val == null ? 0 : val.intValue());
        int menuWidth = b.getWidth();
        int menuHeight = b.getHeight();
        Insets insets = c.getInsets();
        boolean isTopLevelMenu = isTopLevelMenu();
        boolean isLeftToRight = c.getComponentOrientation().isLeftToRight();
        int horizontalAlignment = b.getHorizontalAlignment();
        int horizontalTextPosition = b.getHorizontalTextPosition();
        resetRects();
        viewRect.setBounds(0, 0, menuWidth, menuHeight);
        viewRect.x += insets.left;
        viewRect.y += insets.top;
        viewRect.width -= (insets.right + insets.left);
        viewRect.height -= (insets.bottom + insets.top);
        Font holdf = g.getFont();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);
        FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);
        String acceleratorText = getAcceleratorText(b.getAccelerator());
        Icon ic = b.getIcon();
        Icon cIcon = null;
        Icon paintIcon = ic;
        if (b instanceof JCheckBoxMenuItem || b instanceof JRadioButtonMenuItem) {
            cIcon = checkIcon;
        }
        String text = layoutMenuItem(fm, b.getText(), fmAccel, acceleratorText, ic, cIcon, arrowIcon, b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, b.getText() == null ? 0 : iconGap, false);
        if (!isTopLevelMenu) {
            if (VERBOSE) System.out.println("*** " + getSwingConstantsString(horizontalTextPosition, horizontalAlignment) + ", menu.size=" + menuWidth + ", " + menuHeight);
            if (isLeftToRight) {
                checkIconRect.x = insets.left;
                acceleratorRect.x = insets.left + CHECK_WIDTH + maxLabelWidth + DEFAULT_ICON_GAP * 2;
                if (horizontalTextPosition == SwingConstants.RIGHT || horizontalTextPosition == SwingConstants.TRAILING) {
                    iconRect.x = insets.left + CHECK_WIDTH;
                    textRect.x = iconRect.x + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + CHECK_WIDTH + maxLabelWidth;
                        int xd = right - (textRect.x + textRect.width);
                        textRect.x = right - textRect.width;
                        iconRect.x = textRect.x - DEFAULT_ICON_GAP - iconRect.width;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        iconRect.x = insets.left + CHECK_WIDTH + (maxLabelWidth - labelWidth) / 2;
                        textRect.x = iconRect.x + iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP);
                    }
                } else if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING) {
                    textRect.x = insets.left + CHECK_WIDTH + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + CHECK_WIDTH + maxLabelWidth;
                        int xd = right - (textRect.x + textRect.width);
                        iconRect.x = right - iconRect.width;
                        textRect.x = iconRect.x - DEFAULT_ICON_GAP - textRect.width;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        textRect.x = insets.left + CHECK_WIDTH + (maxLabelWidth - labelWidth) / 2;
                        iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.CENTER) {
                    int labelWidth = (textRect.width > iconRect.width ? textRect.width : iconRect.width);
                    int labelx = insets.left + CHECK_WIDTH + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    int xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                    iconRect.x += xd;
                    textRect.x += xd;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + CHECK_WIDTH + maxLabelWidth;
                        labelx = right - labelWidth;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        labelx = insets.left + CHECK_WIDTH + (maxLabelWidth - labelWidth) / 2;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    }
                }
            } else {
                checkIconRect.x = viewRect.x + viewRect.width - CHECK_WIDTH + DEFAULT_ICON_GAP;
                acceleratorRect.x = insets.left + ARROW_WIDTH + maxAccWidth - acceleratorRect.width;
                if (horizontalTextPosition == SwingConstants.RIGHT || horizontalTextPosition == SwingConstants.TRAILING) {
                    iconRect.x = checkIconRect.x - DEFAULT_ICON_GAP - iconRect.width;
                    textRect.x = checkIconRect.x - DEFAULT_ICON_GAP - maxIconWidth - (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP) - textRect.width;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        textRect.x = left;
                        iconRect.x = left + textRect.width + DEFAULT_ICON_GAP;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        textRect.x = left + (maxLabelWidth - labelWidth) / 2;
                        iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING) {
                    textRect.x = checkIconRect.x - DEFAULT_ICON_GAP - textRect.width - maxIconWidth - (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    iconRect.x = textRect.x - DEFAULT_ICON_GAP - iconRect.width;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        iconRect.x = left;
                        textRect.x = left + iconRect.width + DEFAULT_ICON_GAP;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        iconRect.x = left + (maxLabelWidth - labelWidth) / 2;
                        textRect.x = iconRect.x + iconRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.CENTER) {
                    int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                    int labelWidth = (textRect.width > iconRect.width ? textRect.width : iconRect.width);
                    int labelx = left + maxTextWidth - labelWidth;
                    int xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                    iconRect.x += xd;
                    textRect.x += xd;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        xd = (textRect.width > iconRect.width ? left - textRect.x : left - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        labelx = left + (maxLabelWidth - labelWidth) / 2;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    }
                }
            }
        }
        if (VERBOSE && !isTopLevelMenu) {
            System.out.println("paintMenuItem() \"" + b.getText() + "\"");
            System.out.println("  checkIconRect=" + p(checkIconRect) + ", iconRect=" + p(iconRect) + ", textRect=" + p(textRect) + ", acceleratorRect=" + p(acceleratorRect) + ", arrowIconRect=" + p(arrowIconRect) + ", viewRect=" + p(viewRect));
            System.out.println("  maxIconWidth=" + maxIconWidth + ", maxTextWidth=" + maxTextWidth + ", maxAccWidth=" + maxAccWidth + ", maxLabelWidth=" + maxLabelWidth);
        }
        paintBackground(g, b, background, isLeftToRight);
        Color holdc = g.getColor();
        if (cIcon != null && !isTopLevelMenu) {
            if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            cIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
            g.setColor(holdc);
        }
        if (paintIcon != null) {
            Icon icon;
            if (!model.isEnabled()) {
                icon = b.getDisabledIcon();
            } else if (model.isPressed() && model.isArmed()) {
                icon = b.getPressedIcon();
                if (icon == null) {
                    icon = b.getIcon();
                }
            } else if (model.isSelected()) {
                icon = b.getSelectedIcon();
                if (icon == null) {
                    icon = b.getIcon();
                }
            } else if (model.isArmed()) {
                icon = b.getIcon();
            } else {
                icon = b.getIcon();
            }
            if (icon != null) {
                icon.paintIcon(c, g, iconRect.x, iconRect.y);
            }
        }
        if (text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            g.setColor(Theme.menuItemFontColor.getColor());
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
        }
        if (!"".equals(acceleratorText)) {
            g.setFont(acceleratorFont);
            if (!model.isEnabled()) {
                g.setColor(Theme.menuItemDisabledFgColor.getColor());
                BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x, acceleratorRect.y + fmAccel.getAscent());
            } else {
                if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                    g.setColor(Theme.menuItemSelectedTextColor.getColor());
                } else {
                    g.setColor(Theme.menuItemFontColor.getColor());
                }
                BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x, acceleratorRect.y + fmAccel.getAscent());
            }
        }
        if (arrowIcon != null) {
            if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                g.setColor(foreground);
            }
            if (!isTopLevelMenu) {
                arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
            }
        }
        g.setColor(holdc);
        g.setFont(holdf);
    }

    protected void paintMenuItemOneIcon(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int iconGap) {
        JMenuItem b = (JMenuItem) c;
        ButtonModel model = b.getModel();
        JComponent parent = (JComponent) b.getParent();
        Integer val = (Integer) parent.getClientProperty(MAX_ACC_WIDTH);
        int maxAccWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_ICON_WIDTH);
        int maxIconWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_LABEL_WIDTH);
        int maxLabelWidth = (val == null ? 0 : val.intValue());
        val = (Integer) parent.getClientProperty(MAX_TEXT_WIDTH);
        int maxTextWidth = (val == null ? 0 : val.intValue());
        int menuWidth = b.getWidth();
        int menuHeight = b.getHeight();
        Insets insets = c.getInsets();
        boolean isTopLevelMenu = isTopLevelMenu();
        boolean isLeftToRight = c.getComponentOrientation().isLeftToRight();
        int horizontalAlignment = b.getHorizontalAlignment();
        int horizontalTextPosition = b.getHorizontalTextPosition();
        resetRects();
        viewRect.setBounds(0, 0, menuWidth, menuHeight);
        viewRect.x += insets.left;
        viewRect.y += insets.top;
        viewRect.width -= insets.right + insets.left;
        viewRect.height -= insets.bottom + insets.top;
        Font holdf = g.getFont();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);
        FontMetrics fmAccel = g.getFontMetrics(acceleratorFont);
        String acceleratorText = getAcceleratorText(b.getAccelerator());
        Icon ic = b.getIcon();
        Icon cIcon = null;
        Icon paintIcon = ic;
        if (b instanceof JCheckBoxMenuItem || b instanceof JRadioButtonMenuItem) {
            if (ic == null) {
                cIcon = checkIcon;
            }
        }
        String text = layoutMenuItem(fm, b.getText(), fmAccel, acceleratorText, ic, cIcon, arrowIcon, b.getVerticalAlignment(), b.getHorizontalAlignment(), b.getVerticalTextPosition(), b.getHorizontalTextPosition(), viewRect, iconRect, textRect, acceleratorRect, checkIconRect, arrowIconRect, b.getText() == null ? 0 : iconGap, false);
        if (!isTopLevelMenu) {
            if (VERBOSE) System.out.println("*** " + getSwingConstantsString(horizontalTextPosition, horizontalAlignment) + ", menu.size=" + menuWidth + ", " + menuHeight);
            if (isLeftToRight) {
                checkIconRect.x = insets.left;
                acceleratorRect.x = insets.left + maxLabelWidth + DEFAULT_ICON_GAP * 2;
                if (horizontalTextPosition == SwingConstants.RIGHT || horizontalTextPosition == SwingConstants.TRAILING) {
                    iconRect.x = checkIconRect.x;
                    textRect.x = iconRect.x + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + maxLabelWidth;
                        int xd = right - (textRect.x + textRect.width);
                        textRect.x = right - textRect.width;
                        iconRect.x = textRect.x - DEFAULT_ICON_GAP - iconRect.width;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        iconRect.x = insets.left + (maxLabelWidth - labelWidth) / 2;
                        textRect.x = iconRect.x + iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP);
                    }
                } else if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING) {
                    textRect.x = insets.left + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + maxLabelWidth;
                        int xd = right - (textRect.x + textRect.width);
                        iconRect.x = right - iconRect.width;
                        textRect.x = iconRect.x - DEFAULT_ICON_GAP - textRect.width;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        textRect.x = insets.left + (maxLabelWidth - labelWidth) / 2;
                        iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.CENTER) {
                    int labelWidth = (textRect.width > iconRect.width ? textRect.width : iconRect.width);
                    int labelx = insets.left + maxIconWidth + (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    int xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                    iconRect.x += xd;
                    textRect.x += xd;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int right = insets.left + maxLabelWidth;
                        labelx = right - labelWidth;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        labelx = insets.left + (maxLabelWidth - labelWidth) / 2;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    }
                }
            } else {
                checkIconRect.x = viewRect.x + viewRect.width - CHECK_WIDTH + DEFAULT_ICON_GAP;
                acceleratorRect.x = insets.left + ARROW_WIDTH + maxAccWidth - acceleratorRect.width;
                if (horizontalTextPosition == SwingConstants.RIGHT || horizontalTextPosition == SwingConstants.TRAILING) {
                    iconRect.x = viewRect.x + viewRect.width - iconRect.width;
                    textRect.x = checkIconRect.x - DEFAULT_ICON_GAP - maxIconWidth - (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP) - textRect.width;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        textRect.x = left;
                        iconRect.x = left + textRect.width + DEFAULT_ICON_GAP;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        textRect.x = left + (maxLabelWidth - labelWidth) / 2;
                        iconRect.x = textRect.x + textRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.LEFT || horizontalTextPosition == SwingConstants.LEADING) {
                    textRect.x = checkIconRect.x - DEFAULT_ICON_GAP - textRect.width - maxIconWidth - (maxIconWidth == 0 ? 0 : DEFAULT_ICON_GAP);
                    iconRect.x = textRect.x - DEFAULT_ICON_GAP - iconRect.width;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        iconRect.x = left;
                        textRect.x = left + iconRect.width + DEFAULT_ICON_GAP;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                        int labelWidth = iconRect.width + (iconRect.width == 0 ? 0 : DEFAULT_ICON_GAP) + textRect.width;
                        iconRect.x = left + (maxLabelWidth - labelWidth) / 2;
                        textRect.x = iconRect.x + iconRect.width + DEFAULT_ICON_GAP;
                    }
                } else if (horizontalTextPosition == SwingConstants.CENTER) {
                    int left = insets.left + ARROW_WIDTH + maxAccWidth + (maxAccWidth == 0 ? 0 : DEFAULT_ACC_GAP);
                    int labelWidth = (textRect.width > iconRect.width ? textRect.width : iconRect.width);
                    int labelx = left + maxTextWidth - labelWidth;
                    int xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                    iconRect.x += xd;
                    textRect.x += xd;
                    if (horizontalAlignment == SwingConstants.RIGHT || horizontalAlignment == SwingConstants.TRAILING) {
                        xd = (textRect.width > iconRect.width ? left - textRect.x : left - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    } else if (horizontalAlignment == SwingConstants.CENTER) {
                        labelx = left + (maxLabelWidth - labelWidth) / 2;
                        xd = (textRect.width > iconRect.width ? labelx - textRect.x : labelx - iconRect.x);
                        iconRect.x += xd;
                        textRect.x += xd;
                    }
                }
            }
        }
        if (VERBOSE && !isTopLevelMenu) {
            System.out.println("paintMenuItem() \"" + b.getText() + "\"");
            System.out.println("  checkIconRect=" + p(checkIconRect) + ", iconRect=" + p(iconRect) + ", textRect=" + p(textRect) + ", acceleratorRect=" + p(acceleratorRect) + ", arrowIconRect=" + p(arrowIconRect) + ", viewRect=" + p(viewRect));
            System.out.println("  maxIconWidth=" + maxIconWidth + ", maxTextWidth=" + maxTextWidth + ", maxAccWidth=" + maxAccWidth + ", maxLabelWidth=" + maxLabelWidth);
        }
        paintBackground(g, b, background, isLeftToRight);
        Color holdc = g.getColor();
        if (cIcon != null && !isTopLevelMenu) {
            if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            cIcon.paintIcon(c, g, checkIconRect.x, checkIconRect.y);
            g.setColor(holdc);
        }
        if (paintIcon != null) {
            Icon icon = paintIcon;
            Icon selectedIcon = null;
            Icon tmpIcon = null;
            if (model.isSelected()) {
                selectedIcon = (Icon) b.getSelectedIcon();
                if (selectedIcon != null) {
                    icon = selectedIcon;
                }
            }
            if (!model.isEnabled()) {
                if (model.isSelected()) {
                    tmpIcon = b.getDisabledSelectedIcon();
                    if (tmpIcon == null) {
                        tmpIcon = selectedIcon;
                    }
                }
                if (tmpIcon == null) {
                    tmpIcon = b.getDisabledIcon();
                }
            } else if (model.isPressed() && model.isArmed()) {
                tmpIcon = b.getPressedIcon();
            } else if (model.isArmed()) {
                if (model.isSelected()) {
                    tmpIcon = b.getRolloverSelectedIcon();
                    if (tmpIcon == null) {
                        tmpIcon = selectedIcon;
                    }
                }
                if (tmpIcon == null) {
                    tmpIcon = b.getRolloverIcon();
                }
            }
            if (tmpIcon != null) {
                icon = tmpIcon;
            }
            if (icon != null) {
                icon.paintIcon(c, g, iconRect.x, iconRect.y);
                if (((b instanceof JCheckBoxMenuItem) || (b instanceof JRadioButtonMenuItem)) && b.getSelectedIcon() == null && model.isSelected()) {
                    paintSelected(g, b);
                }
            }
        }
        if (text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            g.setColor(Theme.menuItemFontColor.getColor());
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
        }
        if (!"".equals(acceleratorText)) {
            g.setFont(acceleratorFont);
            if (!model.isEnabled()) {
                g.setColor(Theme.menuItemDisabledFgColor.getColor());
                BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x, acceleratorRect.y + fmAccel.getAscent());
            } else {
                if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                    g.setColor(Theme.menuItemSelectedTextColor.getColor());
                } else {
                    g.setColor(Theme.menuItemFontColor.getColor());
                }
                BasicGraphicsUtils.drawString(g, acceleratorText, 0, acceleratorRect.x, acceleratorRect.y + fmAccel.getAscent());
            }
        }
        if (arrowIcon != null) {
            if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
                g.setColor(foreground);
            }
            if (!isTopLevelMenu) {
                arrowIcon.paintIcon(c, g, arrowIconRect.x, arrowIconRect.y);
            }
        }
        g.setColor(holdc);
        g.setFont(holdf);
    }

    private void paintSelected(Graphics g, JMenuItem b) {
        Color iconColor, bgColor;
        if (!b.isEnabled()) {
            iconColor = Theme.menuIconDisabledColor.getColor();
            bgColor = Theme.menuPopupColor.getColor();
        } else if (b.isArmed()) {
            iconColor = Theme.menuIconRolloverColor.getColor();
            bgColor = Theme.menuItemRolloverColor.getColor();
        } else {
            iconColor = Theme.menuIconColor.getColor();
            bgColor = Theme.menuPopupColor.getColor();
        }
        int x1 = iconRect.x - 3;
        int y1 = iconRect.y - 1;
        if (b.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT) {
            x1 = iconRect.x + iconRect.width - 4;
        }
        g.translate(x1, y1);
        g.setColor(iconColor);
        if (b instanceof JCheckBoxMenuItem) {
            g.drawLine(0, 2, 0, 4);
            g.drawLine(1, 3, 1, 5);
            g.drawLine(2, 4, 2, 6);
            g.drawLine(3, 3, 3, 5);
            g.drawLine(4, 2, 4, 4);
            g.drawLine(5, 1, 5, 3);
            g.drawLine(6, 0, 6, 2);
            g.setColor(bgColor);
            g.drawLine(3, 1, 4, 1);
            g.drawLine(3, 2, 3, 2);
            g.setColor(ColorRoutines.getAlphaColor(bgColor, 128));
            g.drawLine(3, 6, 3, 6);
            g.drawLine(4, 5, 4, 5);
            g.drawLine(5, 4, 5, 4);
            g.drawLine(6, 3, 6, 3);
        } else {
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 96));
            g.drawLine(1, 0, 1, 0);
            g.drawLine(5, 0, 5, 0);
            g.drawLine(0, 1, 0, 1);
            g.drawLine(6, 1, 6, 1);
            g.drawLine(0, 5, 0, 5);
            g.drawLine(6, 5, 6, 5);
            g.drawLine(1, 6, 1, 6);
            g.drawLine(5, 6, 5, 6);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 184));
            g.drawLine(2, 0, 2, 0);
            g.drawLine(4, 0, 4, 0);
            g.drawLine(0, 2, 0, 2);
            g.drawLine(6, 2, 6, 2);
            g.drawLine(0, 4, 0, 4);
            g.drawLine(6, 4, 6, 4);
            g.drawLine(2, 6, 2, 6);
            g.drawLine(4, 6, 4, 6);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 245));
            g.drawLine(3, 0, 3, 0);
            g.drawLine(3, 6, 3, 6);
            g.drawLine(0, 3, 0, 3);
            g.drawLine(6, 3, 6, 3);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 159));
            g.drawLine(1, 1, 1, 1);
            g.drawLine(5, 1, 5, 1);
            g.drawLine(1, 5, 1, 5);
            g.drawLine(5, 5, 5, 5);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 71));
            g.drawLine(2, 1, 2, 1);
            g.drawLine(4, 1, 4, 1);
            g.drawLine(1, 2, 1, 2);
            g.drawLine(5, 2, 5, 2);
            g.drawLine(1, 4, 1, 4);
            g.drawLine(5, 4, 5, 4);
            g.drawLine(2, 5, 2, 5);
            g.drawLine(4, 5, 4, 5);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 112));
            g.drawLine(2, 2, 2, 2);
            g.drawLine(4, 2, 4, 2);
            g.drawLine(2, 4, 2, 4);
            g.drawLine(4, 4, 4, 4);
            g.setColor(ColorRoutines.getAlphaColor(iconColor, 224));
            g.drawLine(3, 2, 3, 2);
            g.drawLine(3, 4, 3, 4);
            g.drawLine(2, 3, 2, 3);
            g.drawLine(4, 3, 4, 3);
            g.setColor(iconColor);
            g.drawLine(3, 3, 3, 3);
            g.setColor(ColorRoutines.getAlphaColor(bgColor, 128));
            g.drawLine(3, 1, 3, 1);
            g.drawLine(5, 3, 5, 3);
            g.drawLine(3, 5, 3, 5);
        }
        g.translate(-x1, -y1);
    }

    /**
	 * Draws the background of one menu item.
	 *
	 * @param g the paint graphics
	 * @param menuItem menu item to be painted
	 * @param bgColor unused
	 * @since 1.4
	 */
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor, boolean isLeftToRight) {
        if (!menuItem.isOpaque()) return;
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        int menuWidth = menuItem.getWidth();
        int menuHeight = menuItem.getHeight();
        boolean armed = (model.isArmed() || (menuItem instanceof JMenu && model.isSelected()));
        if (isTopLevelMenu(menuItem)) {
            Color parentBg = menuItem.getParent().getBackground();
            if (parentBg instanceof ColorUIResource) {
                parentBg = Theme.menuBarColor.getColor();
            }
            if (model.isSelected()) {
                g.setColor(parentBg);
                g.fillRect(0, 0, menuWidth, menuHeight);
                paintXpTopMenuBorder(g, 0, 0, menuWidth, menuHeight, true, isLeftToRight, parentBg);
            } else if (menuItem.getClientProperty("rollover") == Boolean.TRUE && Theme.menuRollover.getValue()) {
                g.setColor(Theme.menuRolloverBgColor.getColor());
                if (isLeftToRight) {
                    g.fillRect(0, 0, menuWidth - TinyPopupMenuBorder.SHADOW_SIZE, menuHeight);
                    g.setColor(parentBg);
                    g.fillRect(menuWidth - TinyPopupMenuBorder.SHADOW_SIZE, 0, TinyPopupMenuBorder.SHADOW_SIZE, menuHeight);
                } else {
                    g.fillRect(TinyPopupMenuBorder.SHADOW_SIZE, 0, menuWidth, menuHeight);
                    g.setColor(parentBg);
                    g.fillRect(0, 0, TinyPopupMenuBorder.SHADOW_SIZE, menuHeight);
                }
                paintXpTopMenuBorder(g, 0, 0, menuWidth, menuHeight, false, isLeftToRight, parentBg);
            } else {
                if (menuItem.getBackground() instanceof ColorUIResource) {
                    g.setColor(parentBg);
                } else {
                    g.setColor(menuItem.getBackground());
                }
                g.fillRect(0, 0, menuWidth, menuHeight);
            }
        } else if (armed) {
            g.setColor(Theme.menuItemRolloverColor.getColor());
            g.fillRect(0, 0, menuWidth, menuHeight);
        } else {
            if (menuItem.getBackground() instanceof ColorUIResource) {
                g.setColor(Theme.menuPopupColor.getColor());
            } else {
                g.setColor(menuItem.getBackground());
            }
            g.fillRect(0, 0, menuWidth, menuHeight);
        }
        g.setColor(oldColor);
    }

    private void paintXpTopMenuBorder(Graphics g, int x, int y, int w, int h, boolean selected, boolean isLeftToRight, Color bg) {
        g.setColor(Theme.menuBorderColor.getColor());
        if (selected) {
            if (isLeftToRight) {
                g.drawLine(x, y, x + w - TinyPopupMenuBorder.SHADOW_SIZE - 1, y);
                g.drawLine(x, y, x, y + h - 1);
                g.drawLine(x + w - TinyPopupMenuBorder.SHADOW_SIZE - 1, y, x + w - TinyPopupMenuBorder.SHADOW_SIZE - 1, y + h - 1);
                paintTopMenuShadow(g, x + w - TinyPopupMenuBorder.SHADOW_SIZE, y + 1, TinyPopupMenuBorder.SHADOW_SIZE, h - 1, isLeftToRight);
            } else {
                g.drawLine(x + TinyPopupMenuBorder.SHADOW_SIZE, y, x + w - 1, y);
                g.drawLine(x + TinyPopupMenuBorder.SHADOW_SIZE, y, x + TinyPopupMenuBorder.SHADOW_SIZE, y + h - 1);
                g.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
                paintTopMenuShadow(g, x, y + 1, TinyPopupMenuBorder.SHADOW_SIZE, h - 1, isLeftToRight);
            }
        } else {
            if (isLeftToRight) {
                g.drawRect(x, y, w - TinyPopupMenuBorder.SHADOW_SIZE - 1, h - 1);
            } else {
                g.drawRect(x + TinyPopupMenuBorder.SHADOW_SIZE, y, w - TinyPopupMenuBorder.SHADOW_SIZE - 1, h - 1);
            }
        }
    }

    private void paintTopMenuShadow(Graphics g, int x, int y, int w, int h, boolean isLeftToRight) {
        if (isLeftToRight) {
            Image img = TinyPopupMenuBorder.LEFT_TO_RIGHT_SHADOW_MASK;
            g.drawImage(img, x, y, x + 5, y + 4, 6, 0, 11, 4, null);
            g.drawImage(img, x, y + 4, x + 5, y + h, 6, 4, 11, 5, null);
        } else {
            Image img = TinyPopupMenuBorder.RIGHT_TO_LEFT_SHADOW_MASK;
            g.drawImage(img, x, y, x + 5, y + 4, 0, 0, 5, 4, null);
            g.drawImage(img, x, y + 4, x + 5, y + h, 0, 4, 5, 5, null);
        }
    }

    /**
	 * Compute the location of the icons origin, the
	 * location of origin of the text baseline, and a possibly clipped
	 * version of the compound labels string.  Locations are computed
	 * relative to the viewRect rectangle.
	 */
    private String layoutMenuItem(FontMetrics fm, String text, FontMetrics fmAccel, String acceleratorText, Icon icon, Icon checkIcon, Icon arrowIcon, int verticalAlignment, int horizontalAlignment, int verticalTextPosition, int horizontalTextPosition, Rectangle viewRect, Rectangle iconRect, Rectangle textRect, Rectangle acceleratorRect, Rectangle checkIconRect, Rectangle arrowIconRect, int iconGap, boolean calcPreferredSize) {
        boolean isTopLevelMenu = isTopLevelMenu();
        boolean hasIcon = (icon != null && icon.getIconWidth() > 0);
        boolean isLeftToRight = menuItem.getComponentOrientation().isLeftToRight();
        SwingUtilities.layoutCompoundLabel(menuItem, fm, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition, horizontalTextPosition, viewRect, iconRect, textRect, hasIcon ? iconGap : 0);
        if (!isTopLevelMenu) {
            if (isLeftToRight) {
                iconRect.x += CHECK_WIDTH;
                textRect.x += CHECK_WIDTH;
            } else {
                iconRect.x -= CHECK_WIDTH;
                textRect.x -= CHECK_WIDTH;
            }
        }
        if ((acceleratorText == null) || acceleratorText.equals("")) {
            acceleratorRect.width = acceleratorRect.height = 0;
            acceleratorText = "";
        } else {
            acceleratorRect.width = SwingUtilities.computeStringWidth(fmAccel, acceleratorText);
            acceleratorRect.height = fmAccel.getHeight();
        }
        if (!isTopLevelMenu) {
            if (checkIcon != null) {
                checkIconRect.height = checkIcon.getIconHeight();
                checkIconRect.width = checkIcon.getIconWidth();
            } else {
                checkIconRect.width = checkIconRect.height = 0;
            }
            if (arrowIcon != null) {
                arrowIconRect.width = arrowIcon.getIconWidth();
                arrowIconRect.height = arrowIcon.getIconHeight();
            } else {
                arrowIconRect.width = arrowIconRect.height = 0;
            }
        } else {
            checkIconRect.width = checkIconRect.height = 0;
            arrowIconRect.width = arrowIconRect.height = 0;
        }
        Rectangle labelRect = iconRect.union(textRect);
        acceleratorRect.y = labelRect.y + (labelRect.height / 2) - (acceleratorRect.height / 2);
        if (!isTopLevelMenu) {
            arrowIconRect.y = labelRect.y + (labelRect.height / 2) - (arrowIconRect.height / 2);
            checkIconRect.y = labelRect.y + (labelRect.height / 2) - (checkIconRect.height / 2);
            if (isLeftToRight) {
                arrowIconRect.x = viewRect.x + viewRect.width - arrowIconRect.width;
            } else {
                arrowIconRect.x = viewRect.x;
            }
        }
        if (VERBOSE && !isTopLevelMenu) {
            System.out.println("layoutMenuItem(" + (calcPreferredSize ? "size" : "paint") + ") \"" + menuItem.getText() + "\" icon:" + (icon == null ? "no" : "yes") + " checkIcon:" + (checkIcon == null ? "no" : "yes"));
            System.out.println("  checkIconRect=" + p(checkIconRect) + ", iconRect=" + p(iconRect) + ", textRect=" + p(textRect) + ", acceleratorRect=" + p(acceleratorRect) + ", arrowIconRect=" + p(arrowIconRect) + ", viewRect=" + p(viewRect));
        }
        return text;
    }
}
