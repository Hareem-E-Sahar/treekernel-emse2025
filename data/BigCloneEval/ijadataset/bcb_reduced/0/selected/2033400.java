package org.jhotdraw.gui.plaf.palette;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Hashtable;
import java.util.HashMap;
import javax.swing.border.*;
import javax.swing.plaf.*;

/**
 * ToolBarUI for palette components.
 * <p>
 * This UI differs from BasicToolBarUI, in that the component holding the toolbar
 * is supposed to use BoxLayout instead of BorderLayout. This allows to have
 * multiple toolbars in the same component. The toolbars can be reorderd in the
 * component, but they are not allowed to float in their own floating window.
 * <p>
 * The JToolBar starts dragging only, if the drag starts over the insets of
 * its border.
 * 
 * @author Werner Randelshofer
 * @version $Id: PaletteToolBarUI.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class PaletteToolBarUI extends ToolBarUI implements SwingConstants {

    private static final boolean isFloatingAllowed = false;

    protected JToolBar toolBar;

    private boolean floating;

    private int floatingX;

    private int floatingY;

    private JFrame floatingFrame;

    @Nullable
    private RootPaneContainer floatingToolBar;

    @Nullable
    protected DragWindow dragWindow;

    @Nullable
    private Container dockingSource;

    private int dockingSensitivity = 0;

    protected int focusedCompIndex = -1;

    @Nullable
    protected Color dockingColor = null;

    @Nullable
    protected Color floatingColor = null;

    @Nullable
    protected Color dockingBorderColor = null;

    @Nullable
    protected Color floatingBorderColor = null;

    @Nullable
    protected MouseInputListener dockingListener;

    @Nullable
    protected PropertyChangeListener propertyListener;

    @Nullable
    protected ContainerListener toolBarContListener;

    @Nullable
    protected FocusListener toolBarFocusListener;

    @Nullable
    private Handler handler;

    protected Integer constraintBeforeFloating = 0;

    private static String IS_ROLLOVER = "JToolBar.isRollover";

    static String IS_DIVIDER_DRAWN = "Palette.ToolBar.isDividerDrawn";

    public static final String TOOLBAR_ICON_PROPERTY = "Palette.ToolBar.icon";

    public static final String TOOLBAR_TEXT_ICON_GAP_PROPERTY = "Palette.ToolBar.textIconGap";

    public static final String TOOLBAR_INSETS_OVERRIDE_PROPERTY = "Palette.ToolBar.insetsOverride";

    private static Border rolloverBorder;

    private static Border nonRolloverBorder;

    private static Border nonRolloverToggleBorder;

    private boolean rolloverBorders = false;

    private HashMap<AbstractButton, Border> borderTable = new HashMap<AbstractButton, Border>();

    private Hashtable<AbstractButton, Boolean> rolloverTable = new Hashtable<AbstractButton, Boolean>();

    /**
     * As of Java 2 platform v1.3 this previously undocumented field is no
     * longer used.
     * Key bindings are now defined by the LookAndFeel, please refer to
     * the key bindings specification for further details.
     *
     * @deprecated As of Java 2 platform v1.3.
     */
    @Deprecated
    protected KeyStroke upKey;

    /**
     * As of Java 2 platform v1.3 this previously undocumented field is no
     * longer used.
     * Key bindings are now defined by the LookAndFeel, please refer to
     * the key bindings specification for further details.
     *
     * @deprecated As of Java 2 platform v1.3.
     */
    @Deprecated
    protected KeyStroke downKey;

    /**
     * As of Java 2 platform v1.3 this previously undocumented field is no
     * longer used.
     * Key bindings are now defined by the LookAndFeel, please refer to
     * the key bindings specification for further details.
     *
     * @deprecated As of Java 2 platform v1.3.
     */
    @Deprecated
    protected KeyStroke leftKey;

    /**
     * As of Java 2 platform v1.3 this previously undocumented field is no
     * longer used.
     * Key bindings are now defined by the LookAndFeel, please refer to
     * the key bindings specification for further details.
     *
     * @deprecated As of Java 2 platform v1.3.
     */
    @Deprecated
    protected KeyStroke rightKey;

    private static String FOCUSED_COMP_INDEX = "JToolBar.focusedCompIndex";

    public static ComponentUI createUI(JComponent c) {
        return new PaletteToolBarUI();
    }

    @Override
    public void installUI(JComponent c) {
        toolBar = (JToolBar) c;
        installDefaults();
        installComponents();
        installListeners();
        installKeyboardActions();
        dockingSensitivity = 0;
        floating = false;
        floatingX = floatingY = 0;
        floatingToolBar = null;
        setOrientation(toolBar.getOrientation());
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
        if (c.getClientProperty(FOCUSED_COMP_INDEX) != null) {
            focusedCompIndex = ((Integer) (c.getClientProperty(FOCUSED_COMP_INDEX))).intValue();
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallDefaults();
        uninstallComponents();
        uninstallListeners();
        uninstallKeyboardActions();
        if (isFloating() == true) {
            setFloating(false, null);
        }
        floatingToolBar = null;
        dragWindow = null;
        dockingSource = null;
        c.putClientProperty(FOCUSED_COMP_INDEX, focusedCompIndex);
    }

    protected void installDefaults() {
        PaletteLookAndFeel.installBorder(toolBar, "ToolBar.border");
        PaletteLookAndFeel.installColorsAndFont(toolBar, "ToolBar.background", "ToolBar.foreground", "ToolBar.font");
        if (dockingColor == null || dockingColor instanceof UIResource) {
            dockingColor = UIManager.getColor("ToolBar.dockingBackground");
        }
        if (floatingColor == null || floatingColor instanceof UIResource) {
            floatingColor = UIManager.getColor("ToolBar.floatingBackground");
        }
        if (dockingBorderColor == null || dockingBorderColor instanceof UIResource) {
            dockingBorderColor = UIManager.getColor("ToolBar.dockingForeground");
        }
        if (floatingBorderColor == null || floatingBorderColor instanceof UIResource) {
            floatingBorderColor = UIManager.getColor("ToolBar.floatingForeground");
        }
        Object rolloverProp = toolBar.getClientProperty(IS_ROLLOVER);
        if (rolloverProp == null) {
            rolloverProp = UIManager.get("ToolBar.isRollover");
        }
        if (rolloverProp != null) {
            rolloverBorders = ((Boolean) rolloverProp).booleanValue();
        }
        if (rolloverBorder == null) {
            rolloverBorder = createRolloverBorder();
        }
        if (nonRolloverBorder == null) {
            nonRolloverBorder = createNonRolloverBorder();
        }
        if (nonRolloverToggleBorder == null) {
            nonRolloverToggleBorder = createNonRolloverToggleBorder();
        }
        setRolloverBorders(isRolloverBorders());
    }

    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(toolBar);
        dockingColor = null;
        floatingColor = null;
        dockingBorderColor = null;
        floatingBorderColor = null;
        installNormalBorders(toolBar);
        rolloverBorder = null;
        nonRolloverBorder = null;
        nonRolloverToggleBorder = null;
    }

    protected void installComponents() {
    }

    protected void uninstallComponents() {
    }

    protected void installListeners() {
        dockingListener = createDockingListener();
        if (dockingListener != null) {
            toolBar.addMouseMotionListener(dockingListener);
            toolBar.addMouseListener(dockingListener);
        }
        propertyListener = createPropertyListener();
        if (propertyListener != null) {
            toolBar.addPropertyChangeListener(propertyListener);
        }
        toolBarContListener = createToolBarContListener();
        if (toolBarContListener != null) {
            toolBar.addContainerListener(toolBarContListener);
        }
        toolBarFocusListener = createToolBarFocusListener();
        if (toolBarFocusListener != null) {
            Component[] components = toolBar.getComponents();
            for (int i = 0; i < components.length; ++i) {
                components[i].addFocusListener(toolBarFocusListener);
            }
        }
    }

    protected void uninstallListeners() {
        if (dockingListener != null) {
            toolBar.removeMouseMotionListener(dockingListener);
            toolBar.removeMouseListener(dockingListener);
            dockingListener = null;
        }
        if (propertyListener != null) {
            toolBar.removePropertyChangeListener(propertyListener);
            propertyListener = null;
        }
        if (toolBarContListener != null) {
            toolBar.removeContainerListener(toolBarContListener);
            toolBarContListener = null;
        }
        if (toolBarFocusListener != null) {
            Component[] components = toolBar.getComponents();
            for (int i = 0; i < components.length; ++i) {
                components[i].removeFocusListener(toolBarFocusListener);
            }
            toolBarFocusListener = null;
        }
        handler = null;
    }

    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(toolBar, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
        PaletteLazyActionMap.installLazyActionMap(toolBar, PaletteToolBarUI.class, "ToolBar.actionMap");
    }

    @Nullable
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap) PaletteLookAndFeel.getInstance().get("ToolBar.ancestorInputMap");
        }
        return null;
    }

    static void loadActionMap(PaletteLazyActionMap map) {
        map.put(new Actions(Actions.NAVIGATE_RIGHT));
        map.put(new Actions(Actions.NAVIGATE_LEFT));
        map.put(new Actions(Actions.NAVIGATE_UP));
        map.put(new Actions(Actions.NAVIGATE_DOWN));
    }

    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(toolBar, null);
        SwingUtilities.replaceUIInputMap(toolBar, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
    }

    protected void navigateFocusedComp(int direction) {
        int nComp = toolBar.getComponentCount();
        int j;
        switch(direction) {
            case EAST:
            case SOUTH:
                if (focusedCompIndex < 0 || focusedCompIndex >= nComp) {
                    break;
                }
                j = focusedCompIndex + 1;
                while (j != focusedCompIndex) {
                    if (j >= nComp) {
                        j = 0;
                    }
                    Component comp = toolBar.getComponentAtIndex(j++);
                    if (comp != null && comp.isFocusable() && comp.isEnabled()) {
                        comp.requestFocus();
                        break;
                    }
                }
                break;
            case WEST:
            case NORTH:
                if (focusedCompIndex < 0 || focusedCompIndex >= nComp) {
                    break;
                }
                j = focusedCompIndex - 1;
                while (j != focusedCompIndex) {
                    if (j < 0) {
                        j = nComp - 1;
                    }
                    Component comp = toolBar.getComponentAtIndex(j--);
                    if (comp != null && comp.isFocusable() && comp.isEnabled()) {
                        comp.requestFocus();
                        break;
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Creates a rollover border for toolbar components. The 
     * rollover border will be installed if rollover borders are 
     * enabled. 
     * <p>
     * Override this method to provide an alternate rollover border.
     *
     * @since 1.4
     */
    protected Border createRolloverBorder() {
        Object border = UIManager.get("ToolBar.rolloverBorder");
        if (border != null) {
            return (Border) border;
        }
        return new EmptyBorder(0, 0, 0, 0);
    }

    /**
     * Creates the non rollover border for toolbar components. This
     * border will be installed as the border for components added
     * to the toolbar if rollover borders are not enabled.
     * <p>
     * Override this method to provide an alternate rollover border.
     *
     * @since 1.4
     */
    protected Border createNonRolloverBorder() {
        Object border = UIManager.get("ToolBar.nonrolloverBorder");
        if (border != null) {
            return (Border) border;
        }
        return new EmptyBorder(0, 0, 0, 0);
    }

    /**
     * Creates a non rollover border for Toggle buttons in the toolbar.
     */
    private Border createNonRolloverToggleBorder() {
        return new EmptyBorder(0, 0, 0, 0);
    }

    /**
     * No longer used, use PaletteToolBarUI.createFloatingWindow(JToolBar)
     * @see #createFloatingWindow
     */
    protected JFrame createFloatingFrame(JToolBar toolbar) {
        Window window = SwingUtilities.getWindowAncestor(toolbar);
        JFrame frame = new JFrame(toolbar.getName(), (window != null) ? window.getGraphicsConfiguration() : null) {

            @Override
            protected JRootPane createRootPane() {
                JRootPane rootPane = new JRootPane() {

                    private boolean packing = false;

                    @Override
                    public void validate() {
                        super.validate();
                        if (!packing) {
                            packing = true;
                            pack();
                            packing = false;
                        }
                    }
                };
                rootPane.setOpaque(true);
                return rootPane;
            }
        };
        frame.getRootPane().setName("ToolBar.FloatingFrame");
        frame.setResizable(false);
        WindowListener wl = createFrameListener();
        frame.addWindowListener(wl);
        return frame;
    }

    /**
     * Creates a window which contains the toolbar after it has been
     * dragged out from its container
     * @return a <code>RootPaneContainer</code> object, containing the toolbar.
     */
    protected RootPaneContainer createFloatingWindow(JToolBar toolbar) {
        class ToolBarDialog extends JDialog {

            public ToolBarDialog(@Nullable Frame owner, String title, boolean modal) {
                super(owner, title, modal);
            }

            public ToolBarDialog(@Nullable Dialog owner, String title, boolean modal) {
                super(owner, title, modal);
            }

            @Override
            protected JRootPane createRootPane() {
                JRootPane rootPane = new JRootPane() {

                    private boolean packing = false;

                    @Override
                    public void validate() {
                        super.validate();
                        if (!packing) {
                            packing = true;
                            pack();
                            packing = false;
                        }
                    }
                };
                rootPane.setOpaque(true);
                return rootPane;
            }
        }
        JDialog dialog;
        Window window = SwingUtilities.getWindowAncestor(toolbar);
        if (window instanceof Frame) {
            dialog = new ToolBarDialog((Frame) window, toolbar.getName(), false);
        } else if (window instanceof Dialog) {
            dialog = new ToolBarDialog((Dialog) window, toolbar.getName(), false);
        } else {
            dialog = new ToolBarDialog((Frame) null, toolbar.getName(), false);
        }
        dialog.getRootPane().setName("ToolBar.FloatingWindow");
        dialog.setTitle(toolbar.getName());
        dialog.setResizable(false);
        WindowListener wl = createFrameListener();
        dialog.addWindowListener(wl);
        return dialog;
    }

    protected DragWindow createDragWindow(JToolBar toolbar) {
        Window frame = null;
        if (toolBar != null) {
            Container p;
            for (p = toolBar.getParent(); p != null && !(p instanceof Window); p = p.getParent()) {
            }
            frame = (Window) p;
        }
        if (floatingToolBar == null) {
            floatingToolBar = createFloatingWindow(toolBar);
        }
        if (floatingToolBar instanceof Window) {
            frame = (Window) floatingToolBar;
        }
        DragWindow w = new DragWindow(frame);
        JRootPane rp = ((RootPaneContainer) w).getRootPane();
        rp.putClientProperty("Window.alpha", new Float(0.6f));
        return w;
    }

    /**
     * Returns a flag to determine whether rollover button borders 
     * are enabled.
     *
     * @return true if rollover borders are enabled; false otherwise
     * @see #setRolloverBorders
     * @since 1.4
     */
    public boolean isRolloverBorders() {
        return rolloverBorders;
    }

    /**
     * Sets the flag for enabling rollover borders on the toolbar and it will
     * also install the apropriate border depending on the state of the flag.
     *    
     * @param rollover if true, rollover borders are installed. 
     *	      Otherwise non-rollover borders are installed
     * @see #isRolloverBorders
     * @since 1.4
     */
    public void setRolloverBorders(boolean rollover) {
        rolloverBorders = rollover;
        if (rolloverBorders) {
            installRolloverBorders(toolBar);
        } else {
            installNonRolloverBorders(toolBar);
        }
    }

    /**
     * Installs rollover borders on all the child components of the JComponent.
     * <p>
     * This is a convenience method to call <code>setBorderToRollover</code> 
     * for each child component.
     *    
     * @param c container which holds the child components (usally a JToolBar)
     * @see #setBorderToRollover
     * @since 1.4
     */
    protected void installRolloverBorders(JComponent c) {
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; ++i) {
            if (components[i] instanceof JComponent) {
                ((JComponent) components[i]).updateUI();
                setBorderToRollover(components[i]);
            }
        }
    }

    /**
     * Installs non-rollover borders on all the child components of the JComponent.
     * A non-rollover border is the border that is installed on the child component
     * while it is in the toolbar.
     * <p>
     * This is a convenience method to call <code>setBorderToNonRollover</code> 
     * for each child component.
     *    
     * @param c container which holds the child components (usally a JToolBar)
     * @see #setBorderToNonRollover
     * @since 1.4
     */
    protected void installNonRolloverBorders(JComponent c) {
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; ++i) {
            if (components[i] instanceof JComponent) {
                ((JComponent) components[i]).updateUI();
                setBorderToNonRollover(components[i]);
            }
        }
    }

    /**
     * Installs normal borders on all the child components of the JComponent.
     * A normal border is the original border that was installed on the child
     * component before it was added to the toolbar.
     * <p>
     * This is a convenience method to call <code>setBorderNormal</code> 
     * for each child component.
     *    
     * @param c container which holds the child components (usally a JToolBar)
     * @see #setBorderToNonRollover
     * @since 1.4
     */
    protected void installNormalBorders(JComponent c) {
        Component[] components = c.getComponents();
        for (int i = 0; i < components.length; ++i) {
            setBorderToNormal(components[i]);
        }
    }

    /**
     * Sets the border of the component to have a rollover border which
     * was created by <code>createRolloverBorder</code>. 
     *
     * @param c component which will have a rollover border installed 
     * @see #createRolloverBorder
     * @since 1.4
     */
    protected void setBorderToRollover(Component c) {
        if (true) {
            return;
        }
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Border border = (Border) borderTable.get(b);
            if (border == null || border instanceof UIResource) {
                borderTable.put(b, b.getBorder());
            }
            if (b.getBorder() instanceof UIResource) {
                b.setBorder(getRolloverBorder(b));
            }
            rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE : Boolean.FALSE);
            b.setRolloverEnabled(true);
        }
    }

    @Nullable
    private Border getRolloverBorder(AbstractButton b) {
        Object borderProvider = UIManager.get("ToolBar.rolloverBorderProvider");
        if (borderProvider == null) {
            return rolloverBorder;
        }
        return null;
    }

    /**
     * Sets the border of the component to have a non-rollover border which
     * was created by <code>createNonRolloverBorder</code>. 
     *
     * @param c component which will have a non-rollover border installed 
     * @see #createNonRolloverBorder
     * @since 1.4
     */
    protected void setBorderToNonRollover(Component c) {
        if (true) {
            return;
        }
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Border border = (Border) borderTable.get(b);
            if (border == null || border instanceof UIResource) {
                borderTable.put(b, b.getBorder());
            }
            if (b.getBorder() instanceof UIResource) {
                if (b instanceof JToggleButton) {
                    ((JToggleButton) b).setBorder(nonRolloverToggleBorder);
                } else {
                    b.setBorder(nonRolloverBorder);
                }
            }
            rolloverTable.put(b, b.isRolloverEnabled() ? Boolean.TRUE : Boolean.FALSE);
            b.setRolloverEnabled(false);
        }
    }

    /**
     * Sets the border of the component to have a normal border.
     * A normal border is the original border that was installed on the child
     * component before it was added to the toolbar.
     *
     * @param c component which will have a normal border re-installed 
     * @see #createNonRolloverBorder
     * @since 1.4
     */
    protected void setBorderToNormal(Component c) {
        if (true) {
            return;
        }
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            Border border = (Border) borderTable.remove(b);
            b.setBorder(border);
            Boolean value = (Boolean) rolloverTable.remove(b);
            if (value != null) {
                b.setRolloverEnabled(value.booleanValue());
            }
        }
    }

    public void setFloatingLocation(int x, int y) {
        floatingX = x;
        floatingY = y;
    }

    public boolean isFloating() {
        return floating;
    }

    public void setFloating(boolean b, @Nullable Point p) {
        if (toolBar.isFloatable() == true) {
            if (dragWindow != null) {
                dragWindow.setVisible(false);
            }
            this.floating = b;
            if (b && isFloatingAllowed) {
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                    dockingSource.remove(toolBar);
                }
                constraintBeforeFloating = calculateConstraint();
                if (propertyListener != null) {
                    UIManager.addPropertyChangeListener(propertyListener);
                }
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                floatingToolBar.getContentPane().add(toolBar, BorderLayout.CENTER);
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).pack();
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setLocation(floatingX, floatingY);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(true);
                }
            } else {
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(false);
                }
                floatingToolBar.getContentPane().remove(toolBar);
                Integer constraint = getDockingConstraint(dockingSource, p);
                if (constraint == null) {
                    constraint = 0;
                }
                int orientation = mapConstraintToOrientation(constraint);
                setOrientation(orientation);
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                if (propertyListener != null) {
                    UIManager.removePropertyChangeListener(propertyListener);
                }
                dockingSource.add(toolBar, constraint.intValue());
            }
            dockingSource.invalidate();
            Container dockingSourceParent = dockingSource.getParent();
            if (dockingSourceParent != null) {
                dockingSourceParent.validate();
            }
            dockingSource.repaint();
        }
    }

    private int mapConstraintToOrientation(Object constraint) {
        int orientation = toolBar.getOrientation();
        if (constraint != null) {
            if (constraint.equals(BorderLayout.EAST) || constraint.equals(BorderLayout.WEST)) {
                orientation = JToolBar.VERTICAL;
            } else if (constraint.equals(BorderLayout.NORTH) || constraint.equals(BorderLayout.SOUTH)) {
                orientation = JToolBar.HORIZONTAL;
            }
        }
        return orientation;
    }

    public void setOrientation(int orientation) {
        toolBar.setOrientation(orientation);
        if (dragWindow != null) {
            dragWindow.setOrientation(orientation);
        }
    }

    /**
     * Gets the color displayed when over a docking area
     */
    public Color getDockingColor() {
        return dockingColor;
    }

    /**
     * Sets the color displayed when over a docking area
     */
    public void setDockingColor(Color c) {
        this.dockingColor = c;
    }

    /**
     * Gets the color displayed when over a floating area
     */
    public Color getFloatingColor() {
        return floatingColor;
    }

    /**
     * Sets the color displayed when over a floating area
     */
    public void setFloatingColor(Color c) {
        this.floatingColor = c;
    }

    private boolean isBlocked(Component comp, Object constraint) {
        if (comp instanceof Container) {
            Container cont = (Container) comp;
            LayoutManager lm = cont.getLayout();
            if (lm instanceof BorderLayout) {
                BorderLayout blm = (BorderLayout) lm;
                Component c = blm.getLayoutComponent(cont, constraint);
                return (c != null && c != toolBar);
            }
        }
        return false;
    }

    public boolean canDock(Component c, Point p) {
        return (p != null && getDockingConstraint(c, p) != null);
    }

    private Integer calculateConstraint() {
        Integer constraint = null;
        LayoutManager lm = dockingSource.getLayout();
        if (lm instanceof BoxLayout) {
            for (int i = 0, n = dockingSource.getComponentCount(); i < n; i++) {
                if (dockingSource.getComponent(i) == toolBar) {
                    constraint = i;
                    break;
                }
            }
        }
        return (constraint != null) ? constraint : constraintBeforeFloating;
    }

    @Nullable
    private Integer getDockingConstraint(Component c, Point p) {
        if (p == null) {
            return constraintBeforeFloating;
        }
        if (c.contains(p)) {
            for (int i = 0, n = dockingSource.getComponentCount(); i < n; i++) {
                Component child = dockingSource.getComponent(i);
                Point childP = new Point(p.x - child.getX(), p.y - child.getY());
                if (child.contains(childP)) {
                    return Math.min(n - 1, (childP.x <= child.getWidth()) ? i : i + 1);
                }
            }
            if (dockingSource.getComponentCount() == 0 || p.x < dockingSource.getComponent(0).getX()) {
                return 0;
            }
            return dockingSource.getComponentCount() - 1;
        }
        return null;
    }

    protected void dragTo(Point position, Point origin) {
        if (toolBar.isFloatable() == true) {
            try {
                if (dragWindow == null) {
                    dragWindow = createDragWindow(toolBar);
                }
                Point offset = dragWindow.getOffset();
                if (offset == null) {
                    Dimension size = toolBar.getSize();
                    offset = new Point(size.width / 2, size.height / 2);
                    dragWindow.setOffset(offset);
                }
                Point global = new Point(origin.x + position.x, origin.y + position.y);
                Point dragPoint = new Point(global.x - offset.x, global.y - offset.y);
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                constraintBeforeFloating = calculateConstraint();
                Point dockingPosition = dockingSource.getLocationOnScreen();
                Point comparisonPoint = new Point(global.x - dockingPosition.x, global.y - dockingPosition.y);
                if (canDock(dockingSource, comparisonPoint)) {
                    dragWindow.setBackground(getDockingColor());
                    Object constraint = getDockingConstraint(dockingSource, comparisonPoint);
                    int orientation = mapConstraintToOrientation(constraint);
                    dragWindow.setOrientation(orientation);
                    dragWindow.setBorderColor(dockingBorderColor);
                } else {
                    dragWindow.setBackground(getFloatingColor());
                    dragWindow.setBorderColor(floatingBorderColor);
                }
                dragWindow.setLocation(dragPoint.x, dragPoint.y);
                if (dragWindow.isVisible() == false) {
                    Dimension size = toolBar.getSize();
                    dragWindow.setSize(size.width, size.height);
                    dragWindow.setVisible(true);
                }
            } catch (IllegalComponentStateException e) {
            }
        }
    }

    protected void floatAt(Point position, Point origin) {
        if (toolBar.isFloatable() == true) {
            try {
                Point offset = dragWindow.getOffset();
                if (offset == null) {
                    offset = position;
                    dragWindow.setOffset(offset);
                }
                Point global = new Point(origin.x + position.x, origin.y + position.y);
                setFloatingLocation(global.x - offset.x, global.y - offset.y);
                if (dockingSource != null) {
                    Point dockingPosition = dockingSource.getLocationOnScreen();
                    Point comparisonPoint = new Point(global.x - dockingPosition.x, global.y - dockingPosition.y);
                    if (canDock(dockingSource, comparisonPoint)) {
                        setFloating(false, comparisonPoint);
                    } else {
                        setFloating(true, null);
                    }
                } else {
                    setFloating(true, null);
                }
                dragWindow.setOffset(null);
            } catch (IllegalComponentStateException e) {
            }
        }
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    protected ContainerListener createToolBarContListener() {
        return getHandler();
    }

    protected FocusListener createToolBarFocusListener() {
        return getHandler();
    }

    protected PropertyChangeListener createPropertyListener() {
        return getHandler();
    }

    protected MouseInputListener createDockingListener() {
        getHandler().tb = toolBar;
        return getHandler();
    }

    protected WindowListener createFrameListener() {
        return new FrameListener();
    }

    /**
     * Paints the contents of the window used for dragging.
     *
     * @param g Graphics to paint to.
     * @throws NullPointerException is <code>g</code> is null
     * @since 1.5
     */
    protected void paintDragWindow(Graphics g) {
        int w = dragWindow.getWidth();
        int h = dragWindow.getHeight();
        g.setColor(dragWindow.getBackground());
        g.fillRect(0, 0, w, h);
        boolean wasDoubleBuffered = false;
        if (toolBar.isDoubleBuffered()) {
            wasDoubleBuffered = true;
            toolBar.setDoubleBuffered(false);
        }
        Graphics g2 = g.create();
        toolBar.paintAll(g2);
        g2.dispose();
        g.setColor(dragWindow.getBorderColor());
        g.drawRect(0, 0, w - 1, h - 1);
        if (wasDoubleBuffered) {
            toolBar.setDoubleBuffered(true);
        }
    }

    private static class Actions extends AbstractAction {

        private static final String NAVIGATE_RIGHT = "navigateRight";

        private static final String NAVIGATE_LEFT = "navigateLeft";

        private static final String NAVIGATE_UP = "navigateUp";

        private static final String NAVIGATE_DOWN = "navigateDown";

        public Actions(String name) {
            super(name);
        }

        public String getName() {
            return (String) getValue(Action.NAME);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String key = getName();
            JToolBar toolBar = (JToolBar) evt.getSource();
            PaletteToolBarUI ui = (PaletteToolBarUI) PaletteLookAndFeel.getUIOfType(toolBar.getUI(), PaletteToolBarUI.class);
            if (NAVIGATE_RIGHT == key) {
                ui.navigateFocusedComp(EAST);
            } else if (NAVIGATE_LEFT == key) {
                ui.navigateFocusedComp(WEST);
            } else if (NAVIGATE_UP == key) {
                ui.navigateFocusedComp(NORTH);
            } else if (NAVIGATE_DOWN == key) {
                ui.navigateFocusedComp(SOUTH);
            }
        }
    }

    private class Handler implements ContainerListener, FocusListener, MouseInputListener, PropertyChangeListener {

        @Override
        public void componentAdded(ContainerEvent evt) {
            Component c = evt.getChild();
            if (toolBarFocusListener != null) {
                c.addFocusListener(toolBarFocusListener);
            }
            if (isRolloverBorders()) {
                setBorderToRollover(c);
            } else {
                setBorderToNonRollover(c);
            }
        }

        @Override
        public void componentRemoved(ContainerEvent evt) {
            Component c = evt.getChild();
            if (toolBarFocusListener != null) {
                c.removeFocusListener(toolBarFocusListener);
            }
            setBorderToNormal(c);
        }

        @Override
        public void focusGained(FocusEvent evt) {
            Component c = evt.getComponent();
            focusedCompIndex = toolBar.getComponentIndex(c);
        }

        @Override
        public void focusLost(FocusEvent evt) {
        }

        JToolBar tb;

        boolean isDragging = false;

        @Nullable
        Point origin = null;

        boolean isArmed = false;

        @Override
        public void mousePressed(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            isDragging = false;
            if (evt.getSource() instanceof JToolBar) {
                JComponent c = (JComponent) evt.getSource();
                Insets insets;
                if (c.getBorder() instanceof PaletteToolBarBorder) {
                    insets = ((PaletteToolBarBorder) c.getBorder()).getDragInsets(c);
                } else {
                    insets = c.getInsets();
                }
                isArmed = !(evt.getX() > insets.left && evt.getX() < c.getWidth() - insets.right && evt.getY() > insets.top && evt.getY() < c.getHeight() - insets.bottom);
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            if (isDragging == true) {
                Point position = evt.getPoint();
                if (origin == null) {
                    origin = evt.getComponent().getLocationOnScreen();
                }
                floatAt(position, origin);
            }
            origin = null;
            isDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (!tb.isEnabled()) {
                return;
            }
            if (!isArmed) {
                return;
            }
            isDragging = true;
            Point position = evt.getPoint();
            if (origin == null) {
                origin = evt.getComponent().getLocationOnScreen();
            }
            dragTo(position, origin);
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
        }

        @Override
        public void mouseEntered(MouseEvent evt) {
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        }

        @Override
        public void mouseMoved(MouseEvent evt) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName == "lookAndFeel") {
                toolBar.updateUI();
            } else if (propertyName == "orientation") {
                Component[] components = toolBar.getComponents();
                int orientation = ((Integer) evt.getNewValue()).intValue();
                JToolBar.Separator separator;
                for (int i = 0; i < components.length; ++i) {
                    if (components[i] instanceof JToolBar.Separator) {
                        separator = (JToolBar.Separator) components[i];
                        if ((orientation == JToolBar.HORIZONTAL)) {
                            separator.setOrientation(JSeparator.VERTICAL);
                        } else {
                            separator.setOrientation(JSeparator.HORIZONTAL);
                        }
                        Dimension size = separator.getSeparatorSize();
                        if (size != null && size.width != size.height) {
                            Dimension newSize = new Dimension(size.height, size.width);
                            separator.setSeparatorSize(newSize);
                        }
                    }
                }
            } else if (propertyName == IS_ROLLOVER) {
                installNormalBorders(toolBar);
                setRolloverBorders(((Boolean) evt.getNewValue()).booleanValue());
            }
        }
    }

    protected class FrameListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent w) {
            if (toolBar.isFloatable() == true) {
                if (dragWindow != null) {
                    dragWindow.setVisible(false);
                }
                floating = false;
                if (floatingToolBar == null) {
                    floatingToolBar = createFloatingWindow(toolBar);
                }
                if (floatingToolBar instanceof Window) {
                    ((Window) floatingToolBar).setVisible(false);
                }
                floatingToolBar.getContentPane().remove(toolBar);
                Integer constraint = constraintBeforeFloating;
                if (dockingSource == null) {
                    dockingSource = toolBar.getParent();
                }
                if (propertyListener != null) {
                    UIManager.removePropertyChangeListener(propertyListener);
                }
                dockingSource.add(toolBar, constraint.intValue());
                dockingSource.invalidate();
                Container dockingSourceParent = dockingSource.getParent();
                if (dockingSourceParent != null) {
                    dockingSourceParent.validate();
                }
                dockingSource.repaint();
            }
        }
    }

    protected class ToolBarContListener implements ContainerListener {

        @Override
        public void componentAdded(ContainerEvent e) {
            getHandler().componentAdded(e);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
            getHandler().componentRemoved(e);
        }
    }

    protected class ToolBarFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            getHandler().focusGained(e);
        }

        @Override
        public void focusLost(FocusEvent e) {
            getHandler().focusLost(e);
        }
    }

    protected class PropertyListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            getHandler().propertyChange(e);
        }
    }

    /**
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of PaletteToolBarUI.
     */
    public class DockingListener implements MouseInputListener {

        protected JToolBar toolBar;

        protected boolean isDragging = false;

        @Nullable
        protected Point origin = null;

        public DockingListener(JToolBar t) {
            this.toolBar = t;
            getHandler().tb = t;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            getHandler().mouseClicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            getHandler().tb = toolBar;
            getHandler().mousePressed(e);
            isDragging = getHandler().isDragging;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            getHandler().tb = toolBar;
            getHandler().isDragging = isDragging;
            getHandler().origin = origin;
            getHandler().mouseReleased(e);
            isDragging = getHandler().isDragging;
            origin = getHandler().origin;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            getHandler().mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            getHandler().mouseExited(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            getHandler().tb = toolBar;
            getHandler().origin = origin;
            getHandler().mouseDragged(e);
            isDragging = getHandler().isDragging;
            origin = getHandler().origin;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            getHandler().mouseMoved(e);
        }
    }

    protected class DragWindow extends JWindow {

        Color borderColor = Color.gray;

        int orientation = toolBar.getOrientation();

        @Nullable
        Point offset;

        DragWindow(Window w) {
            super(w);
            getContentPane().add(new JPanel() {

                @Override
                public void paintComponent(Graphics g) {
                    paintDragWindow(g);
                }
            });
        }

        public void setOrientation(int o) {
            if (isShowing()) {
                if (o == this.orientation) {
                    return;
                }
                this.orientation = o;
                Dimension size = getSize();
                setSize(new Dimension(size.height, size.width));
                if (offset != null) {
                    if (toolBar.getComponentOrientation().isLeftToRight()) {
                        setOffset(new Point(offset.y, offset.x));
                    } else if (o == JToolBar.HORIZONTAL) {
                        setOffset(new Point(size.height - offset.y, offset.x));
                    } else {
                        setOffset(new Point(offset.y, size.width - offset.x));
                    }
                }
                repaint();
            }
        }

        @Nullable
        public Point getOffset() {
            return offset;
        }

        public void setOffset(@Nullable Point p) {
            this.offset = p;
        }

        public void setBorderColor(Color c) {
            if (this.borderColor == c) {
                return;
            }
            this.borderColor = c;
            repaint();
        }

        public Color getBorderColor() {
            return this.borderColor;
        }

        @Override
        public Insets getInsets() {
            return new Insets(1, 1, 1, 1);
        }
    }
}
