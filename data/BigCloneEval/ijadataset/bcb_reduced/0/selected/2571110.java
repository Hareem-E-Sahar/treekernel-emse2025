package abbot;

import abbot.tester.*;
import abbot.script.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;
import java.lang.reflect.*;

public class DefaultComponentFinder implements ComponentFinder {

    private WeakHashMap filteredComponents = new WeakHashMap();

    /** Allow chaining to existing filter sets. */
    private ComponentFinder parent = null;

    private boolean filter = true;

    /** Maps class names to their corresponding Tester object. */
    private HashMap testers = new HashMap();

    /** Maps a tester to the class it tests. */
    private HashMap tested = new HashMap();

    public DefaultComponentFinder(ComponentFinder parent) {
        this.parent = parent;
    }

    public DefaultComponentFinder() {
    }

    public Component findComponent(ComponentReference ref) throws ComponentNotFoundException {
        Component comp = findDescendent(null, ref);
        if (comp == null) throw new ComponentNotFoundException("Component " + ref + " not found");
        return comp;
    }

    public Window findWindowByName(String match) {
        Frame[] frames = getFrames();
        for (int i = 0; i < frames.length; i++) {
            String name = getComponentName(frames[i]);
            if ((match != null && match.equals(name)) || name == match) return frames[i];
            Window[] windows = getWindows(frames[i]);
            for (int w = 0; w < windows.length; w++) {
                name = getComponentName(windows[w]);
                if ((match != null && match.equals(name)) || name == match) return windows[w];
            }
        }
        return null;
    }

    public Window findWindowByTitle(String title) {
        Frame[] frames = getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (titlesMatch(title, frames[i].getTitle())) {
                return frames[i];
            }
            Window[] windows = getWindows(frames[i]);
            for (int w = 0; w < windows.length; w++) {
                if (windows[w] instanceof Dialog) {
                    Dialog d = (Dialog) windows[w];
                    if (titlesMatch(title, d.getTitle())) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    /** Return the window with the given name or title.  Attempts to find a
     * named window first, since that search is more restrictive. */
    public Window findWindow(String nameOrTitle) {
        Window w = findWindowByName(nameOrTitle);
        if (w == null) w = findWindowByTitle(nameOrTitle);
        return w;
    }

    public Component findActivePopupMenu(Component root, Component invoker) {
        if (root == null) {
            Window[] wins = getWindows();
            for (int i = 0; i < wins.length; i++) {
                Component comp = (Component) wins[i];
                Component menu = findActivePopupMenu(comp, invoker);
                if (menu != null) return menu;
            }
            return null;
        }
        Log.debug("Checking " + root.getClass().getName());
        if (root instanceof javax.swing.JPopupMenu && ((JPopupMenu) root).isShowing()) {
            return root;
        } else if (root instanceof Container) {
            Component[] subs = ((Container) root).getComponents();
            for (int i = 0; i < subs.length; i++) {
                Component menu = findActivePopupMenu(subs[i], invoker);
                if (menu != null) return menu;
            }
        }
        return null;
    }

    public Component findMenuItemByName(Component root, String name) {
        if (root instanceof JMenuItem && ((JMenuItem) root).getText().equals(name)) return root;
        if (root instanceof Container) {
            Component[] subs = ((Container) root).getComponents();
            for (int i = 0; i < subs.length; i++) {
                Component comp = findMenuItemByName(subs[i], name);
                if (comp != null) return comp;
            }
        }
        if (root instanceof MenuElement) {
            MenuElement[] els = ((MenuElement) root).getSubElements();
            for (int i = 0; i < els.length; i++) {
                Component comp = findMenuItemByName((Component) els[i], name);
                if (comp != null) return comp;
            }
        }
        return null;
    }

    /** Return the first component matching the reference's hierarchy.
     * Assumes the parent reference is only null if the child is a top-most
     * frame.  This would be the place to check for appropriate index as
     * well. 
     */
    protected Component findDescendent(Component ancestor, ComponentReference ref) {
        if (isFiltered(ancestor)) return null;
        if (ancestor == null) {
            Frame[] frames = getFrames();
            for (int f = 0; f < frames.length; f++) {
                Component comp = findDescendent(frames[f], ref);
                if (comp != null) return comp;
            }
            return null;
        }
        if (componentsMatch(ancestor, ref)) return ancestor;
        if (ancestor instanceof JMenu) {
            MenuElement[] els = ((MenuElement) ancestor).getSubElements();
            for (int i = 0; i < els.length; i++) {
                Component comp = findDescendent(els[i].getComponent(), ref);
                if (comp != null) return comp;
            }
        }
        if (ancestor instanceof Window) {
            Window[] win = ((Window) ancestor).getOwnedWindows();
            for (int w = 0; w < win.length; w++) {
                Component comp = findDescendent(win[w], ref);
                if (comp != null) return comp;
            }
        }
        if (ancestor instanceof Container) {
            Component[] children = ((Container) ancestor).getComponents();
            for (int i = 0; i < children.length; i++) {
                Component comp = findDescendent(children[i], ref);
                if (comp != null) return comp;
            }
        }
        return null;
    }

    /** Return whether the the given title matches the given pattern. */
    private boolean titlesMatch(String pattern, String actual) {
        return Regexp.stringMatch(pattern, actual);
    }

    /** Return the component's name, ensuring that null is returned if the
     * name appears to be auto-generated.
     */
    public String getComponentName(Component comp) {
        String name = comp.getName();
        if (name != null && (Regexp.stringMatch("win[0-9]*", name) || Regexp.stringMatch("dialog[0-9]*", name) || Regexp.stringMatch("frame[0-9]*", name) || Regexp.stringMatch("canvas[0-9]*", name))) name = null;
        return name;
    }

    /** Determine the best we can whether the component is the one referred to
     * by the reference.
     */
    public boolean componentsMatch(Component comp, ComponentReference ref) {
        if (comp == null || ref == null) return ref == null && comp == null;
        ComponentTester tester = getTester(comp);
        int weight = 0;
        boolean match = true;
        if (!ref.getRefClass().isAssignableFrom(comp.getClass())) match = false;
        String n1 = ref.getName();
        String n2 = getComponentName(comp);
        if ((n1 != null && !n1.equals(n2)) || (n1 == null && n1 != n2)) match = false; else if (n1 != null && n1.equals(n2)) ++weight;
        String t1 = ref.getTag();
        String t2 = getTag(comp);
        if (t1 != null && !t1.equals(t2)) match = false; else if (t1 != null && t1.equals(t2)) ++weight;
        if (ref.getParentID() != null) {
            if (componentsMatch(getComponentParent(comp), ref.getParentReference())) {
                if (weight > 0) ++weight;
            } else match = false;
        }
        if (ref.getWindowID() != null) {
            if (componentsMatch(getComponentWindow(comp), ref.getWindowReference())) {
                if (weight > 0) ++weight;
            } else match = false;
        }
        if (ref.getTitle() != null) {
            if (titlesMatch(ref.getTitle(), getComponentFrameTitle(comp))) {
                if (weight > 0) ++weight;
            } else match = false;
        }
        if (!match && weight > 0) Log.warn("Near match (weight=" + weight + "): " + ref + " vs. " + comp);
        return match;
    }

    /** Return an array of all available Frames. */
    public Frame[] getFrames() {
        Frame[] frames = Frame.getFrames();
        Vector unf = new Vector();
        for (int i = 0; i < frames.length; i++) {
            if (!isFiltered(frames[i])) unf.add(frames[i]);
        }
        return (Frame[]) unf.toArray(new Frame[unf.size()]);
    }

    /** Return all windows owned by the given window. */
    public Window[] getWindows(Window parent) {
        Window[] windows = parent.getOwnedWindows();
        Vector unf = new Vector();
        for (int i = 0; i < windows.length; i++) {
            if (!isFiltered(windows[i])) unf.add(windows[i]);
        }
        return (Window[]) unf.toArray(new Window[unf.size()]);
    }

    /** Returns the set of all available windows that have not been
     * filtered.
     */
    public Window[] getWindows() {
        HashSet set = new HashSet();
        Frame[] frames = Frame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            if (!isFiltered(frames[i])) {
                set.add(frames[i]);
                Window[] windows = frames[i].getOwnedWindows();
                for (int w = 0; w < windows.length; w++) {
                    if (!isFiltered(windows[w])) set.add(windows[w]);
                }
            }
        }
        return (Window[]) set.toArray(new Window[set.size()]);
    }

    /** Returns all components below the GUI hierarchy of the given Container,
     * including Windows and MenuElements.
     */
    public Component[] getComponents(Container c) {
        Component[] children = c.getComponents();
        Vector unf = new Vector();
        for (int i = 0; i < children.length; i++) {
            if (!isFiltered(children[i])) unf.add(children[i]);
        }
        if (c instanceof javax.swing.JMenu) {
            MenuElement[] els = ((javax.swing.JMenu) c).getSubElements();
            for (int i = 0; i < els.length; i++) {
                if (!isFiltered(els[i].getComponent())) unf.add(els[i].getComponent());
            }
        }
        if (c instanceof Window) {
            Window[] windows = ((Window) c).getOwnedWindows();
            for (int i = 0; i < windows.length; i++) {
                if (!isFiltered(windows[i])) unf.add(windows[i]);
            }
        }
        return (Component[]) unf.toArray(new Component[unf.size()]);
    }

    /** Look up the apparent parent of a component.  Basically makes a menu
     * item's parent be the menu it's in, and the popup menu's parent the menu
     * or component that spawned it.
     */
    public Component getComponentParent(Component comp) {
        Component parent = comp.getParent();
        if (comp instanceof MenuElement) {
            if (parent instanceof JPopupMenu) parent = ((JPopupMenu) parent).getInvoker();
        }
        return parent;
    }

    /** Return the nearest Window ancestor of the given Component. */
    public Window getComponentWindow(Component comp) {
        Component parent = comp;
        while (!(parent instanceof Window) && (comp = getComponentParent(parent)) != null) {
            parent = comp;
        }
        return (Window) comp;
    }

    /** 
     * Return the title of the Frame for the given component, if any.  Skip
     * over windows until we get to a frame.  If no title is found, return
     * null.
     */
    public String getComponentFrameTitle(Component comp) {
        Component frame = getComponentFrame(comp);
        String title = null;
        if (frame != null) {
            if (frame instanceof Frame) title = ((Frame) frame).getTitle(); else if (frame instanceof Dialog) title = ((Dialog) frame).getTitle();
        }
        return title;
    }

    /** Return the component's owning frame.   There will <b>always</b> one of
        these; even a frameless window will have a temporary frame generated
        for it. */
    public Component getComponentFrame(Component comp) {
        Component parent = comp;
        while (!(comp instanceof Frame || comp instanceof Dialog) && (comp = getComponentParent(parent)) != null) {
            parent = comp;
        }
        return (comp instanceof Frame || comp instanceof Dialog) ? comp : null;
    }

    public EventQueue getEventQueue(Component comp) {
        return getComponentWindow(comp).getToolkit().getSystemEventQueue();
    }

    /** Return a reasonable identifier for the given component. */
    public String getTag(Component comp) {
        return getTester(comp).deriveTag(comp);
    }

    /** Return the tester registered for the given component reference's
     * class. 
     */
    public ComponentTester getTester(ComponentReference ref) {
        return getTester(ref.getRefClass());
    }

    /** Return the tester registered for the given component's class. */
    public ComponentTester getTester(Component comp) {
        return comp != null ? getTester(comp.getClass()) : getTester(Component.class);
    }

    private static String simpleClassName(Class cls) {
        Package pkg = cls.getPackage();
        if (pkg == null) return cls.getName(); else return cls.getName().substring(pkg.getName().length() + 1);
    }

    public ComponentTester getTester(Class forClass) {
        if (!(Component.class.isAssignableFrom(forClass))) {
            throw new IllegalArgumentException("Class must derive from " + "Component");
        }
        ComponentTester tester = (ComponentTester) testers.get(forClass);
        if (tester == null) {
            String cname = simpleClassName(forClass);
            try {
                String pkg = ComponentTester.class.getPackage().getName();
                cname = pkg + "." + cname + "Tester";
                Class testClass = Class.forName(cname);
                Constructor ctor = testClass.getConstructor(new Class[] { ComponentFinder.class });
                tester = (ComponentTester) ctor.newInstance(new Object[] { this });
                testers.put(forClass, tester);
                tested.put(tester, forClass);
            } catch (InvocationTargetException ite) {
                Log.warn(ite);
            } catch (NoSuchMethodException nsm) {
                tester = getTester(forClass.getSuperclass());
            } catch (InstantiationException ie) {
                tester = getTester(forClass.getSuperclass());
            } catch (IllegalAccessException iae) {
                tester = getTester(forClass.getSuperclass());
            } catch (ClassNotFoundException cnf) {
                tester = getTester(forClass.getSuperclass());
            }
        }
        return tester;
    }

    /** Return the class tested by the given tester. */
    public Class getTestedClass(ComponentTester tester) {
        return (Class) tested.get(tester);
    }

    public boolean isFiltered(Component comp) {
        if (comp == null) return false;
        return filter && (filteredComponents.containsKey(comp) || (parent != null && parent.isFiltered(comp)));
    }

    public void filterComponent(Component comp) {
        Log.debug("Now filtering " + getTag(comp));
        filteredComponents.put(comp, comp);
    }

    /** Discard and no longer reference the given component. */
    public void discardComponent(Component comp) {
        filterComponent(comp);
        if (parent != null) parent.discardComponent(comp);
    }

    public void setFilterEnabled(boolean enable) {
        this.filter = enable;
    }

    /** Send an explicit window close event to all showing windows.  Note
        that this is not guaranteed to actually make the window go away.  */
    public void closeWindows() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Window[] windows = getWindows();
                for (int i = 0; i < windows.length; i++) {
                    Window win = windows[i];
                    if (win.isShowing()) {
                        WindowEvent ev = new WindowEvent(win, WindowEvent.WINDOW_CLOSING);
                        win.getToolkit().getSystemEventQueue().postEvent(ev);
                    }
                }
            }
        });
    }

    /** Dispose of all available windows, and does not return until they have
        been disposed of.  */
    public void disposeWindows() {
        Runnable runnable = new Runnable() {

            public void run() {
                Window[] windows = getWindows();
                for (int i = 0; i < windows.length; i++) {
                    Window win = windows[i];
                    win.dispose();
                    discardComponent(win);
                }
            }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) runnable.run(); else {
            try {
                javax.swing.SwingUtilities.invokeAndWait(runnable);
            } catch (Exception exc) {
                Log.warn(exc);
            }
        }
    }
}
