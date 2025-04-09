package abbot.tester.swt;

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import abbot.Log;
import abbot.Platform;
import abbot.WaitTimedOutError;
import abbot.script.Condition;
import abbot.swt.DefaultWidgetFinder;
import abbot.swt.WidgetFinder;
import abbot.util.Properties;
import abbot.util.Reflector;

public class Robot {

    public static final String copyright = "Licensed Materials	-- Property of IBM\n" + "(c) Copyright International Business Machines Corporation, 2003\nUS Government " + "Users Restricted Rights - Use, duplication or disclosure restricted by GSA " + "ADP Schedule Contract with IBM Corp.";

    /** Use java.awt.Robot to generate events. */
    protected static int EM_ROBOT = 0;

    /** Post events to the AWT event queue. */
    protected static int EM_AWT = 1;

    /** Use programmatic control where possible (only partly implemented). */
    protected static int EM_PROG = 2;

    public static final int BUTTON_MASK = (SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3);

    public static int MENU_DELAY = 300;

    private static final int MENU_TIMEOUT = 2500;

    private boolean threadFlag;

    private static final boolean popupOnButton2 = Platform.isMacintosh();

    public static final int POPUP_MASK = popupOnButton2 ? SWT.BUTTON2 : SWT.BUTTON3;

    public static final String POPUP_MODIFIER = popupOnButton2 ? "BUTTON2_MASK" : "BUTTON3_MASK";

    public static final boolean POPUP_ON_PRESS = !Platform.isWindows();

    public static final int TERTIARY_MASK = popupOnButton2 ? SWT.BUTTON3 : SWT.BUTTON2;

    public static final String TERTIARY_MODIFIER = popupOnButton2 ? "BUTTON3_MASK" : "BUTTON2_MASK";

    /** TODO Add private helper function AWTToSWTCode to fill in below*/
    public static final int MENU_SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /** Return whether this is the tertiary button, considering primary to be
	 * button1 and secondary to be the popup trigger button.
	 */
    public static boolean isTertiaryButton(int mods) {
        return ((mods & BUTTON_MASK) != SWT.BUTTON1) && ((mods & POPUP_MASK) == 0);
    }

    /** OS X (as of 1.3.1, v10.1.5) has incorrect location information for the
		menu bar when it is installed at the top of the screen.  Indicate
		whether to adjust for that (no workaround yet).
		Recheck with Jaguar.
	*/
    protected static final boolean needMenuFix() {
        return Platform.isOSX() && (Boolean.getBoolean("com.apple.macos.useScreenMenuBar") || Boolean.getBoolean("apple.laf.useScreenMenuBar"));
    }

    protected boolean boolT = false;

    protected int intT;

    protected Point pointT = new Point(0, 0);

    protected Rectangle rectT = new Rectangle(0, 0, 0, 0);

    protected Control controlT;

    protected Shell shellT;

    protected Object objT;

    protected Menu menuT;

    protected MenuItem itemT;

    /** Base delay setting. */
    private static int defaultDelay = Properties.getProperty("abbot.robot.default_delay", 0, 60000, 30000);

    /** Delay before checking for idle.  This allows the system a little time
		to put a native event onto the AWT event queue. */
    private static int eventPostDelay = Properties.getProperty("abbot.robot.event_post_delay", 0, 1000, 100);

    /** Delay before failing to find a popup menu that should appear. */
    private static int popupDelay = Properties.getProperty("abbot.robot.popup_delay", 0, 60000, defaultDelay);

    /** Delay before failing to find a component that should be visible. */
    protected static int componentDelay = Properties.getProperty("abbot.robot.component_delay", 0, 60000, defaultDelay);

    /** With decreased robot auto delay, OSX popup menus don't activate
	 * properly.  Indicate the minimum delay for proper operation (determined
	 * experimentally). 
	 */
    private static final int subMenuDelay = Platform.isOSX() ? 100 : 0;

    /** How events are generated. */
    private static int eventMode = EM_ROBOT;

    /** If we started a drag, this is the source. */
    protected Widget dragSource = null;

    protected org.eclipse.swt.graphics.Point dragLocation = null;

    protected boolean inDragSource = false;

    protected boolean inDropTarget = false;

    /** The robot used to generate events. */
    private static abbot.swt.Robot robot = null;

    /** Suitable delay for most cases; tests have been run safely at this
		value.  Should definitely be less than the double-click threshold.
		(The default value, zero, causes half the tests to fail on linux).
		FIXME need to find a value between 0 and 100 (100 is kinda slow).
		30 works (almost) for w32/linux, but OSX 10.1.5 text input lags (50 is 
		minimum). <p>
		As platforms are tested at 0 delay, adjust this value.<p>
		OSX test run time was reduced from 130s to 96s.<p>
		Not sure it's worth tracking down all the robot bugs and working
		around them.
	*/
    private static final int DEFAULT_DELAY = Platform.isOSX() || Platform.isLinux() || Platform.isWindows() ? 0 : 50;

    private static final int SLEEP_INTERVAL = 10;

    private static int autoDelay = DEFAULT_DELAY;

    public static int getAutoDelay() {
        return autoDelay;
    }

    static {
        String mode = System.getProperty("abbot.robot.mode", "robot");
        autoDelay = Properties.getProperty("abbot.robot.auto_delay", -1, 60000, autoDelay);
        try {
            robot = new abbot.swt.Robot();
            if (autoDelay != -1) {
                robot.setAutoDelay(autoDelay);
            } else {
                autoDelay = robot.getAutoDelay();
                Log.warn("Using delay of " + autoDelay);
            }
        } catch (SWTException swte) {
            robot = null;
        }
        if (mode.equals("awt") || robot == null) {
            eventMode = EM_AWT;
        }
    }

    /** 
	 * Move the mouse to the given location, in screen coordinates.  
	 * NOTE: in robot mode, you may need to invokethis with a little jitter.
	 * There are some conditions where a single mouse move will not
	 * generate the necessary enter event on a component (typically a
	 * dialog with an OK button) before a mousePress.  See also click().
	 */
    public void mouseMove(int x, int y) {
        if (eventMode == EM_ROBOT) {
            Log.debug("ROBOT: Mouse move: (" + x + "," + y + ")");
            robot.mouseMove(x, y);
        } else {
            throw new AWTDependentCodeException("eventMode == EM_AWT.");
        }
    }

    /** Send a button press event. */
    public void mousePress(int buttons) {
        if (eventMode == EM_ROBOT) {
            Log.debug("ROBOT: Mouse press: " + getAcceleratorMouseString(buttons));
            robot.mousePress(buttons);
        } else {
            throw new AWTDependentCodeException("eventMode == EM_AWT.");
        }
    }

    /** Send a button release event. */
    public void mouseRelease(int buttons) {
        if (eventMode == EM_ROBOT) {
            Log.debug("ROBOT: Mouse release: " + getAcceleratorMouseString(buttons));
            robot.mouseRelease(buttons);
        } else {
            throw new AWTDependentCodeException("eventMode == EM_AWT.");
        }
    }

    public static void waitForIdle() {
        if (eventPostDelay > autoDelay) {
            delay(eventPostDelay - autoDelay);
        }
        robot.waitForIdle();
    }

    public static void waitForIdle(Display d) {
        if (eventPostDelay > autoDelay) {
            delay(eventPostDelay - autoDelay);
        }
        robot.waitForIdle(d);
    }

    public synchronized void mouseMove(final Widget w) {
        Robot.syncExec(w.getDisplay(), this, new Runnable() {

            public void run() {
                pointT = WidgetLocator.getLocation(w);
            }
        });
        mouseMove(pointT.x, pointT.y);
    }

    public synchronized void mouseMove(final Widget w, int x, int y) {
        Robot.syncExec(w.getDisplay(), this, new Runnable() {

            public void run() {
                pointT = WidgetLocator.getLocation(w);
            }
        });
        mouseMove(pointT.x + x, pointT.y + y);
    }

    public void activate(final Shell shell) {
        Robot.syncExec(shell.getDisplay(), null, new Runnable() {

            public void run() {
                shell.forceActive();
            }
        });
    }

    public synchronized Widget findFocusOwner(final Display display) {
        if (display != null) {
            Robot.syncExec(display, this, new Runnable() {

                public void run() {
                    controlT = display.getFocusControl();
                }
            });
            return controlT;
        } else return null;
    }

    /** Move keyboard focus to the given component. */
    public void focus(final Control c) {
        final Display display = c.getDisplay();
        Robot.syncExec(display, null, new Runnable() {

            public void run() {
                c.forceFocus();
            }
        });
        mouseMove(c);
    }

    /** Send a key press event. */
    public void keyPress(int keycode) {
        if (eventMode == EM_ROBOT) {
            Log.debug("ROBOT: key press " + getAcceleratorKeyString(keycode));
            robot.keyPress(keycode);
        } else {
            throw new AWTDependentCodeException("eventMode == EM_AWT.");
        }
    }

    /** Send a key release event. */
    public void keyRelease(int keycode) {
        if (eventMode == EM_ROBOT) {
            Log.debug("ROBOT: key release " + getAcceleratorKeyString(keycode));
            robot.keyRelease(keycode);
        } else {
            throw new AWTDependentCodeException("eventMode == EM_AWT.");
        }
    }

    /** Sleep for a little bit, measured in UI time. */
    public static void sleep() {
        delay(SLEEP_INTERVAL);
    }

    /** Sleep the given duration of ms. */
    public static void delay(int ms) {
        if (eventMode == EM_ROBOT) abbot.swt.Robot.delay(ms); else {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ie) {
            }
        }
    }

    /** Sample the color at the given point on the screen. */
    public static Color sample(int x, int y) {
        return robot.getPixelColor(x, y);
    }

    public static Image capture(Rectangle bounds) {
        return robot.createScreenCapture(bounds);
    }

    /** Capture the contents of the given Widget, sans any border or
	 * insets. 
	 * 
	 * [FROM abbot.tester.awt's implementation]:
	 * This should only be used on components that do
	 * not use a LAF UI, or the results will not be consistent across
	 * platforms.  
	 */
    public synchronized Image capture(final Widget w) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w, false);
            }
        });
        Log.debug("Component bounds " + rectT);
        return capture(rectT);
    }

    /** Capture the contents of the given Widget, optionally including the
	 * border and/or insets.  
	 * 
	 * [FROM abbot.tester.awt's implementation]:
	 * This should only be used on components that do
	 * not use a LAF UI, or the results will not be consistent across
	 * platforms.  
	 */
    public synchronized Image capture(final Widget w, final boolean ignoreBorder) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w, ignoreBorder);
            }
        });
        Log.debug("Component bounds " + rectT);
        return capture(rectT);
    }

    /** Returns the current event-generation mode. */
    public static int getEventMode() {
        return eventMode;
    }

    /** Set the event-generation mode. */
    static void setEventMode(int mode) {
        eventMode = mode;
    }

    public static int getEventPostDelay() {
        return eventPostDelay;
    }

    public static void setEventPostDelay(int delay) {
        eventPostDelay = Math.min(1000, Math.max(0, delay));
    }

    /** Allow this to be adjusted, mostly for testing. */
    public static void setAutoDelay(int ms) {
        ms = Math.min(60000, Math.max(0, ms));
        if (eventMode == EM_ROBOT) robot.setAutoDelay(ms);
        autoDelay = ms;
    }

    /** Run the given action on the event dispatch thread. 
	 */
    public static void invokeAction(Display display, Runnable action) {
        display.asyncExec(action);
    }

    private static final Runnable EMPTY_RUNNABLE = new Runnable() {

        public void run() {
        }
    };

    protected void jitter(Widget w, int x, int y) {
        mouseMove(w, (x > 0 ? x - 1 : x + 1), y);
    }

    private void jitter(int x, int y) {
        mouseMove((x > 0 ? x - 1 : x + 1), y);
    }

    /** Move the mouse appropriately to get from the source to the
		destination.  Enter/exit events will be generated where appropriate.
	*/
    public void dragOver(Widget dst, int x, int y) {
        mouseMove(dst, (x > 1) ? x - 1 : x + 1, y);
        mouseMove(dst, x, y);
    }

    /** Begin a drag operation. */
    public void drag(Widget src, int sx, int sy, int modifiers) {
        mousePress(src, sx, sy, modifiers);
        mouseMove(src, sx > 0 ? sx - 1 : sx + 1, sy);
        dragSource = src;
        dragLocation = new org.eclipse.swt.graphics.Point(sx, sy);
        inDragSource = true;
    }

    /** End a drag operation, releasing the mouse button over the given target
		location.
	*/
    public void drop(Widget target, int x, int y, int modifiers) {
        if (dragSource == null) throw new ActionFailedException("There is no drag source");
        inDropTarget = dragSource == target;
        dragOver(target, x, y);
        mouseRelease(modifiers);
        dragSource = null;
        dragLocation = null;
        inDragSource = inDropTarget = false;
    }

    /** 
	 * Type all the keys contained in this accelerator.
	 * 
	 * Note that uppercase characters will be typed as uppercase, which
	 * requires that the shift key be depressed.
	 *
	 *  @see abbot.swt.Robot
	 */
    public void key(int accelerator) {
        robot.keyPress(accelerator);
        robot.keyRelease(accelerator);
    }

    /**
	 * Type the given character.  Note that this sends the key to whatever
	 * component currently has the focus.
	 */
    public void keyStroke(char ch) {
        key((int) ch);
    }

    /** Type the given string. */
    public void keyString(String str) {
        char[] ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            keyStroke(ch[i]);
        }
    }

    public synchronized void mousePress(final Widget w) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w);
            }
        });
        mousePress(w, rectT.width / 2, rectT.height / 2);
    }

    public synchronized void mousePress(final Widget w, int mask) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w);
            }
        });
        mousePress(w, rectT.width / 2, rectT.height / 2, mask);
    }

    public void mousePress(Widget w, int x, int y) {
        mousePress(w, x, y, SWT.BUTTON1);
    }

    /** Mouse down in the given part of the component.  All other mousePress
		methods must eventually invoke this one.
	*/
    public void mousePress(Widget w, int x, int y, int mask) {
        if (eventMode == EM_ROBOT && hasRobotMotionBug()) jitter(w, x, y); else mouseMove(w, x, y);
        mousePress(mask);
    }

    /** Click in the center of the given component.  This is not static b/c it
	 * sometimes needs to be redefined (i.e. JComponent to scroll before
	 * clicking).
	 */
    public synchronized void click(final Widget w) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w);
                System.out.println(rectT);
            }
        });
        click(w, rectT.width / 2, rectT.height / 2);
    }

    public synchronized void click(final Widget w, int mask) {
        Display display = w.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(w);
            }
        });
        click(w, rectT.width / 2, rectT.height / 2, mask);
    }

    public void click(Widget w, int x, int y) {
        click(w, x, y, SWT.BUTTON1);
    }

    public void click(Widget w, int x, int y, int mask) {
        click(w, x, y, mask, 1);
    }

    /** Click in the given part of the component.  All other click methods
	 * must eventually invoke this one.
	 */
    public void click(Widget w, int x, int y, int mask, int count) {
        Log.debug("Click at (" + x + "," + y + ") on " + w);
        boolean shift = (mask & SWT.SHIFT) == SWT.SHIFT;
        mask &= (SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3);
        if (shift) keyPress(SWT.SHIFT);
        int oldDelay = getAutoDelay();
        if (oldDelay * 2 > 200) setAutoDelay(50);
        mousePress(w, x, y, mask);
        while (count-- > 1) {
            mouseRelease(mask);
            mousePress(mask);
        }
        mouseRelease(mask);
        setAutoDelay(oldDelay);
        if (shift) keyRelease(SWT.SHIFT);
    }

    public void selectPopupMenuItem(final MenuItem item, final int x, final int y) {
        Display display = item.getDisplay();
        Robot.syncExec(display, null, new Runnable() {

            public void run() {
                Menu root = getRootMenu(item);
                if ((root.getStyle() & SWT.POP_UP) == SWT.POP_UP) {
                    exposeMenuItem(item, MENU_DELAY, true, 0, 0);
                    waitForIdle(item.getDisplay());
                } else {
                    exposeMenuItem(item, MENU_DELAY, false, x, y);
                    int oldAccel = item.getAccelerator();
                    int accel;
                    activate(item.getParent().getShell());
                    if (oldAccel == 0) {
                        accel = findUnusedAccelerator(item.getDisplay());
                        if (accel == -1) {
                            Log.debug("selectMenuItem: could not find unused accelerator");
                            return;
                        }
                        item.setAccelerator(accel);
                        key(accel);
                        waitForIdle(item.getDisplay());
                        item.setAccelerator(0);
                    } else {
                        key(oldAccel);
                        waitForIdle(item.getDisplay());
                    }
                }
            }
        });
    }

    /** Find and select an SWT MenuItem based on its "name" property.
	 * 
	 * @param parent a Composite container that contains the MenuItem
	 * @param label the text of the MenuItem (and its name)
	 */
    public void selectMenuItemByText(Menu parent, String name) {
        Hashtable ht = hashMenuItemsByText(parent);
        org.eclipse.swt.widgets.MenuItem item = (org.eclipse.swt.widgets.MenuItem) ht.get(name);
        if (item == null) throw new WidgetMissingException("No MenuItem found with " + "the text \"" + name + "\".");
        selectMenuItem(item);
    }

    /**
	 * Select an SWT MenuItem.
	 * 
	 * NOTE: no menus can be open when this method is called
	 * 
	 * @param item The MenuItem to be selected
	 */
    public void selectMenuItem(final MenuItem item) {
        Display display = item.getDisplay();
        Robot.syncExec(display, null, new Runnable() {

            public void run() {
                Menu root = getRootMenu(item);
                if ((root.getStyle() & SWT.POP_UP) == SWT.POP_UP) {
                    exposeMenuItem(item, MENU_DELAY, true, 0, 0);
                    waitForIdle(item.getDisplay());
                } else {
                    exposeMenuItem(item, MENU_DELAY, false, 0, 0);
                    int oldAccel = item.getAccelerator();
                    int accel;
                    activate(item.getParent().getShell());
                    if (oldAccel == 0) {
                        accel = findUnusedAccelerator(item.getDisplay());
                        if (accel == -1) {
                            Log.debug("selectMenuItem: could not find unused accelerator");
                            return;
                        }
                        if (!item.isDisposed()) item.setAccelerator(accel);
                        key(accel);
                        waitForIdle(item.getDisplay());
                        item.setAccelerator(0);
                    } else {
                        key(oldAccel);
                        waitForIdle(item.getDisplay());
                    }
                }
            }
        });
    }

    private void exposeMenuItem(MenuItem item, final int delay, final boolean click, int x, int y) {
        int depth = 0;
        Stack itemStack = new Stack();
        Menu parent;
        MenuItem parentItem;
        LinkedList keyStrokes = new LinkedList();
        while (true) {
            parent = item.getParent();
            parentItem = parent.getParentItem();
            itemStack.push(item);
            if (parentItem == null) break;
            item = parentItem;
            depth++;
        }
        this.activate(parent.getShell());
        org.eclipse.swt.widgets.MenuItem[] items;
        int itemIdx = -1;
        boolean found = false;
        if (parent.getShell().getMenuBar() != null && parent.getShell().getMenuBar().equals(parent)) {
            keyStrokes.add(new Integer(SWT.ALT));
            items = parent.getItems();
            for (int i = 0; i < items.length; i++) {
                if (item.equals(items[i])) {
                    itemIdx = i;
                    found = true;
                    break;
                }
            }
            if (found) {
                for (int i = 0; i < itemIdx; i++) {
                    keyStrokes.add(new Integer(SWT.ARROW_RIGHT));
                }
                if (!itemStack.isEmpty()) {
                }
                keyStrokes.add(new Integer(SWT.ARROW_DOWN));
                itemStack.pop();
            } else {
                Log.debug("enterMenuItem(): " + item + " not found.");
                return;
            }
        } else if ((parent.getStyle() & SWT.POP_UP) == SWT.POP_UP) {
            Rectangle parentBounds = WidgetLocator.getBounds(parent.getParent());
            mouseMove(parent.getParent(), x, y);
            robot.waitForIdle(parent.getDisplay());
            parent.setVisible(true);
            keyStrokes.add(new Integer(SWT.ARROW_DOWN));
        }
        found = false;
        itemIdx = -1;
        while (!itemStack.isEmpty()) {
            found = false;
            item = (org.eclipse.swt.widgets.MenuItem) (itemStack.pop());
            parent = item.getParent();
            items = parent.getItems();
            itemIdx = 0;
            if (!parent.isEnabled()) {
                Log.debug("enterMenuItem(): " + parent + " is not enabled.");
                return;
            }
            for (int i = 0; i < items.length; i++) {
                if (item.equals(items[i])) {
                    itemIdx = i;
                    found = true;
                    break;
                }
            }
            if (found) {
                for (int i = 0; i < itemIdx; i++) {
                    keyStrokes.add(new Integer(SWT.ARROW_DOWN));
                }
                if (!itemStack.isEmpty()) {
                    keyStrokes.add(new Integer(SWT.ARROW_RIGHT));
                }
            } else {
                Log.debug("enterMenuItem(): " + item + " not found.");
                return;
            }
        }
        final int[] keyStrokeAccels = new int[keyStrokes.size()];
        for (int i = 0; i < keyStrokes.size(); i++) keyStrokeAccels[i] = ((Integer) keyStrokes.get(i)).intValue();
        synchronized (this) {
            boolT = false;
            Thread enter = new Thread() {

                public void run() {
                    for (int i = 0; i < keyStrokeAccels.length; i++) {
                        robot.keyPress(keyStrokeAccels[i]);
                        robot.keyRelease(keyStrokeAccels[i]);
                    }
                    Robot.delay(delay);
                    if (click) {
                        robot.keyPress(SWT.CR);
                        robot.keyRelease(SWT.CR);
                    } else {
                        robot.keyPress(SWT.ALT);
                        robot.keyRelease(SWT.ALT);
                    }
                    boolT = true;
                }
            };
            enter.start();
            try {
                enter.join(1);
            } catch (InterruptedException ie) {
                Log.debug("unable to wait for keystrokes to enter menuItem");
            }
            Display display = item.getDisplay();
            while (!display.isDisposed() && !boolT) display.readAndDispatch();
        }
    }

    /**
	 * returns an array of all menu items contained in this menu and its submenus
	 */
    private LinkedList traverseMenuTree(Menu menu) {
        MenuItem[] items = menu.getItems();
        LinkedList list = new LinkedList();
        list.addAll(Arrays.asList(items));
        for (int i = 0; i < items.length; i++) {
            if (items[i].getMenu() != null) list.addAll(traverseMenuTree(items[i].getMenu()));
        }
        return list;
    }

    /**
	 * returns a hashtable of all menuitems contained in this menu and its submenus 
	 */
    public synchronized Hashtable hashMenuItemsByText(final Menu menu) {
        final Hashtable ht = new Hashtable();
        Robot.syncExec(menu.getDisplay(), this, new Runnable() {

            public void run() {
                LinkedList items = traverseMenuTree(menu);
                for (int i = 0; i < items.size(); i++) {
                    itemT = (MenuItem) items.get(i);
                    ht.put(itemT.getText(), itemT);
                }
            }
        });
        return ht;
    }

    /**
	 * returns the root menu that contains a given menuitem
	 */
    public synchronized Menu getRootMenu(final MenuItem item) {
        Display display = item.getDisplay();
        Robot.syncExec(display, this, new Runnable() {

            public void run() {
                menuT = item.getParent();
                itemT = menuT.getParentItem();
            }
        });
        return (itemT == null) ? menuT : getRootMenu(itemT);
    }

    private int findUnusedAccelerator(Display display) {
        Shell active = display.getActiveShell();
        LinkedList items = new LinkedList();
        Menu popup = null;
        Menu bar = null;
        try {
            popup = (active.getMenu() != null) ? active.getMenu() : null;
            bar = active.getMenuBar();
        } catch (NullPointerException ignored) {
        }
        if (popup != null) items.addAll(traverseMenuTree(popup));
        if (bar != null) items.addAll(traverseMenuTree(bar));
        LinkedList accelerators = new LinkedList();
        int x;
        for (int i = 0; i < items.size(); i++) {
            x = ((org.eclipse.swt.widgets.MenuItem) items.get(i)).getAccelerator();
            if (x != 0) accelerators.add(new Integer(x));
        }
        int testAccel;
        int usedAccel;
        int[] testMasks = { (SWT.ALT), (SWT.CTRL), (SWT.ALT | SWT.CTRL) };
        boolean clear = false;
        for (int j = 0; j < testMasks.length; j++) {
            for (char c = 'a'; c < 'z'; c++) {
                testAccel = (testMasks[j] | c);
                clear = true;
                for (int i = 0; i < accelerators.size(); i++) {
                    usedAccel = ((Integer) accelerators.get(i)).intValue();
                    if (usedAccel == testAccel) clear = false;
                }
                if (clear) return testAccel;
            }
        }
        return -1;
    }

    /** Invoke the window close operation. */
    public void close(final Decorations window) {
        Robot.syncExec(window.getDisplay(), null, new Runnable() {

            public void run() {
                window.dispose();
            }
        });
    }

    /** Move the given Decorations to the requested location. */
    public synchronized void move(final Decorations window, int newx, int newy) {
        Robot.syncExec(window.getDisplay(), this, new Runnable() {

            public void run() {
                pointT = WidgetLocator.getLocation(window);
            }
        });
        moveBy(window, newx - pointT.x, newy - pointT.y);
    }

    /** Move the given Decorations by the given amount. */
    public synchronized void moveBy(final Decorations window, final int dx, final int dy) {
        Robot.syncExec(window.getDisplay(), this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(window);
            }
        });
        mouseMove(window, rectT.width / 2, 0);
        mouseMove(window, rectT.width / 2 + dx, dy);
        Robot.syncExec(window.getDisplay(), null, new Runnable() {

            public void run() {
                window.setLocation(new Point(rectT.x + dx, rectT.y + dy));
            }
        });
        mouseMove(window, rectT.width / 2, 0);
    }

    /** Resize the given Decorations to the given size.  */
    public synchronized void resize(final Decorations window, int width, int height) {
        Robot.syncExec(window.getDisplay(), this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(window);
            }
        });
        resizeBy(window, width - rectT.width, height - rectT.height);
    }

    /** Resize the given Decorations by the given amounts.  */
    public synchronized void resizeBy(final Decorations window, final int dx, final int dy) {
        Robot.syncExec(window.getDisplay(), this, new Runnable() {

            public void run() {
                rectT = WidgetLocator.getBounds(window);
            }
        });
        mouseMove(window, rectT.width - 1, rectT.height - 1);
        mouseMove(window, rectT.width + dx - 1, rectT.height + dy - 1);
        Robot.syncExec(window.getDisplay(), null, new Runnable() {

            public void run() {
                window.setSize(rectT.width + dx, rectT.width + dy);
            }
        });
        mouseMove(window, rectT.width - 1, rectT.height - 1);
    }

    /** Identify the coordinates of the iconify button where we can, returning
	 * null if we can't.
	 */
    public synchronized Point getIconifyLocation(final Shell shell) {
        Robot.syncExec(shell.getDisplay(), this, new Runnable() {

            public void run() {
                int style = shell.getStyle();
                if ((style & SWT.MIN) == SWT.MIN) {
                    if (Platform.isWindows()) {
                        int xOffset = 50 + shell.getBorderWidth();
                        int yOffset = 12 + shell.getBorderWidth();
                        Rectangle bounds = WidgetLocator.getBounds(shell);
                        pointT = new Point(bounds.width - xOffset, yOffset);
                    }
                }
                pointT = null;
            }
        });
        return pointT;
    }

    /** Identify the coordinates of the maximize button where possible,
	 *	returning null if not.
	 */
    public synchronized Point getMaximizeLocation(final Shell shell) {
        final Point loc = getIconifyLocation(shell);
        Robot.syncExec(shell.getDisplay(), this, new Runnable() {

            public void run() {
                intT = shell.getStyle();
            }
        });
        int style = intT;
        if (loc != null && (style & SWT.MAX) == SWT.MAX) {
            if (Platform.isWindows()) {
                return new Point(loc.x + 17, loc.y);
            }
        }
        return null;
    }

    /** Iconify the given Shell.  Don't support iconification of Dialogs at
	 * this point (although maybe should).
	 */
    public void iconify(final Shell shell) {
        Point loc = getIconifyLocation(shell);
        if (loc != null) mouseMove(shell, loc.x, loc.y);
        Robot.syncExec(shell.getDisplay(), null, new Runnable() {

            public void run() {
                shell.setMinimized(true);
            }
        });
    }

    public void deiconify(Shell shell) {
        normalize(shell);
    }

    public void normalize(final Shell shell) {
        Robot.syncExec(shell.getDisplay(), null, new Runnable() {

            public void run() {
                shell.setMinimized(false);
                shell.setMaximized(false);
            }
        });
    }

    /** Make the window full size */
    public void maximize(final Shell shell) {
        Point loc = getMaximizeLocation(shell);
        if (loc != null) mouseMove(shell, loc.x, loc.y);
        Robot.syncExec(shell.getDisplay(), null, new Runnable() {

            public void run() {
                shell.setMaximized(true);
                if (!shell.getMaximized() && (shell.getStyle() & SWT.RESIZE) == SWT.RESIZE) {
                    Rectangle screen = shell.getDisplay().getBounds();
                    shell.setLocation(screen.x, screen.y);
                    shell.setSize(screen.width, screen.height);
                }
            }
        });
    }

    public static Class getCanonicalClass(Class refClass) {
        while (refClass.getName().indexOf("$") != -1 || refClass.getName().startsWith("javax.swing.plaf") || refClass.getName().startsWith("com.apple.mrj")) refClass = refClass.getSuperclass();
        return refClass;
    }

    /** Return the numeric event ID corresponding to the given string. */
    public static int getEventID(Class cls, String id) {
        return Reflector.getFieldValue(cls, id);
    }

    /** TODO Fix this so that it will parse out chars as well */
    public static int getModifiers(String mods) {
        int value = 0;
        if (mods != null && !mods.equals("")) {
            StringTokenizer st = new StringTokenizer(mods, "| ");
            while (st.hasMoreTokens()) {
                String flag = st.nextToken();
                if (POPUP_MODIFIER.equals(flag)) value |= POPUP_MASK; else if (TERTIARY_MODIFIER.equals(flag)) value |= TERTIARY_MASK; else if (!flag.equals("0") && flag.indexOf('\'') == -1) value |= Reflector.getFieldValue(SWT.class, flag);
            }
        }
        return value;
    }

    /** 
	 * Provides a String representation of the mouse modifiers in 
	 * the given accelerator. 
	 */
    public String getAcceleratorMouseString(int accelerator) {
        return getAcceleratorString(accelerator, false, true);
    }

    /**
	 * Provides a String representation of a given accelerator.
	 */
    public String getAcceleratorString(int accelerator, boolean key, boolean mouse) {
        String res = "{ ";
        int count = 0;
        if (mouse) {
            if ((accelerator & SWT.BUTTON1) == SWT.BUTTON1) {
                if (count != 0) res += "| ";
                res += "SWT.BUTTON1 ";
                count++;
            }
            if ((accelerator & SWT.BUTTON2) == SWT.BUTTON2) {
                if (count != 0) res += "| ";
                res += "SWT.BUTTON2 ";
                count++;
            }
            if ((accelerator & SWT.BUTTON3) == SWT.BUTTON3) {
                if (count != 0) res += "| ";
                res += "SWT.BUTTON3 ";
                count++;
            }
        }
        if (key) {
            if ((accelerator & SWT.ALT) == SWT.ALT) {
                if (count != 0) res += "| ";
                res += "SWT.ALT ";
                count++;
            }
            if ((accelerator & SWT.SHIFT) == SWT.SHIFT) {
                if (count != 0) res += "| ";
                res += "SWT.SHIFT ";
                count++;
            }
            if ((accelerator & SWT.CTRL) == SWT.CTRL) {
                if (count != 0) res += "| ";
                res += "SWT.CTRL ";
                count++;
            }
            if ((accelerator & SWT.COMMAND) == SWT.COMMAND) {
                if (count != 0) res += "| ";
                res += "SWT.COMMAND ";
                count++;
            }
            int keyCode = accelerator & SWT.KEY_MASK;
            if ((SWT.KEYCODE_BIT & keyCode) != 0 && keyCode != 0) {
                switch(keyCode) {
                    case SWT.ARROW_UP:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.ARROW_UP";
                        count++;
                        break;
                    case SWT.ARROW_DOWN:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.ARROW_DOWN";
                        count++;
                        break;
                    case SWT.ARROW_LEFT:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.ARROW_LEFT";
                        count++;
                        break;
                    case SWT.ARROW_RIGHT:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.ARROW_RIGHT";
                        count++;
                        break;
                    case SWT.PAGE_UP:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.PAGE_UP";
                        count++;
                        break;
                    case SWT.PAGE_DOWN:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.PAGE_DOWN";
                        count++;
                        break;
                    case SWT.HOME:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.HOME";
                        count++;
                        break;
                    case SWT.END:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.END";
                        count++;
                        break;
                    case SWT.INSERT:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.INSERT";
                        count++;
                        break;
                    case SWT.F1:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F1";
                        count++;
                        break;
                    case SWT.F2:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F2";
                        count++;
                        break;
                    case SWT.F3:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F3";
                        count++;
                        break;
                    case SWT.F4:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F4";
                        count++;
                        break;
                    case SWT.F5:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F5";
                        count++;
                        break;
                    case SWT.F6:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F6";
                        count++;
                        break;
                    case SWT.F7:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F7";
                        count++;
                        break;
                    case SWT.F8:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F8";
                        count++;
                        break;
                    case SWT.F9:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F9";
                        count++;
                        break;
                    case SWT.F10:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F10";
                        count++;
                        break;
                    case SWT.F11:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F11";
                        count++;
                        break;
                    case SWT.F12:
                        res += (count != 0) ? "| " : "";
                        res += "SWT.F12";
                        count++;
                        break;
                    default:
                        break;
                }
            } else if (keyCode != 0) {
                if (count != 0) res += "| ";
                res += "'(char)keyCode'";
                count++;
            }
        }
        res += "}";
        return res;
    }

    /** 
	 * returns a String representation of the key modifiers 
	 * in the given accelerator 
	 */
    public String getAcceleratorKeyString(int accelerator) {
        return getAcceleratorString(accelerator, true, false);
    }

    /** Is the given MenuItem on a popup? */
    public static boolean isOnPopup(MenuItem item) {
        Menu parent = item.getParent();
        return (parent.getStyle() & SWT.POP_UP) == SWT.POP_UP || (parent.getParentItem() != null && isOnPopup(parent.getParentItem()));
    }

    /** Strip the package from the class name. */
    public static String simpleClassName(Class cls) {
        String name = cls.getName();
        int dot = name.lastIndexOf(".");
        return name.substring(dot + 1, name.length());
    }

    /** Wait for the given Condition to return true.  The default timeout may
	 * be changed by setting abbot.robot.default_delay.
	 * @throws WaitTimedOutError if the default timeout (30s) is exceeded. 
	 */
    public static void wait(Condition condition) {
        wait(condition, defaultDelay);
    }

    /** Wait for the given Condition to return true, waiting for timeout ms.
	 * @throws WaitTimedOutError if the timeout is exceeded. 
	 */
    public static void wait(Condition condition, long timeout) {
        wait(condition, timeout, SLEEP_INTERVAL);
    }

    /** Wait for the given Condition to return true, waiting for timeout ms,
	 * polling at the given interval.
	 * @throws WaitTimedOutError if the timeout is exceeded. 
	 */
    public static void wait(Condition condition, long timeout, int interval) {
        long now = System.currentTimeMillis();
        while (!condition.test()) {
            if (System.currentTimeMillis() - now > timeout) {
                String msg = "Timed out waiting for " + condition;
                throw new WaitTimedOutError(msg);
            }
            delay(interval);
        }
    }

    private static java.util.ArrayList bugList = null;

    private static boolean gotBug1Event = false;

    /** Place the pointer in the center of the display */
    public void resetPointer() {
        if (eventMode == EM_ROBOT) {
            Rectangle screen = robot.getDisplay().getBounds();
            mouseMove(screen.width / 2, screen.height / 2);
            mouseMove(screen.width / 2 - 1, screen.height / 2 - 1);
        }
    }

    /**
	 * Get the <code>Display</code> object with which this robot is
	 * synchronized.
	 * 
	 * @return the <code>Display</code> associated with this <code>Robot</code>
	 */
    public Display getDisplay() {
        return robot.getDisplay();
    }

    /**
	 * Set the <code>Display</code> object with which this robot is
	 * synchronized.
	 * 
	 * @param display the <code>Display</code> to associate with this <code>Robot</code>
	 */
    public void setDisplay(Display display) {
        robot.setDisplay(display);
    }

    public static String toString(Widget widget) {
        if (widget == null) return "(null)";
        Class cls = widget.getClass();
        WidgetFinder finder = DefaultWidgetFinder.getFinder();
        String name = finder.getWidgetName(widget);
        if (name == null) name = WidgetTester.getTag(widget);
        cls = getCanonicalClass(cls);
        String cname = simpleClassName(widget.getClass());
        if (!cls.equals(widget.getClass())) cname += "/" + simpleClassName(cls);
        if (name == null) name = cname + " instance"; else name = "'" + name + "' (" + cname + ")";
        return name;
    }

    public static class SyncFlag {

        static Hashtable flags = new Hashtable();

        public static synchronized void initFlag(Display display) {
            flags.put(display, new Boolean(false));
        }

        public static synchronized void clearFlag(Display display) {
            flags.remove(display);
        }

        public static synchronized void setFlag() {
            Display display = Display.findDisplay(Thread.currentThread());
            flags.put(display, new Boolean(true));
        }

        public static synchronized boolean getFlag(Display display) {
            return ((Boolean) flags.get(Thread.currentThread())).booleanValue();
        }
    }

    boolean locked = false;

    public static void syncExec(Display dsply, Object obj, Runnable action) {
        try {
            dsply.syncExec(action);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    public static boolean hasRobotMotionBug() {
        return Platform.isOSX() || (!Platform.isWindows() && Platform.JAVA_VERSION < Platform.JAVA_1_4) || Boolean.getBoolean("abbot.robot.need_jitter");
    }
}
