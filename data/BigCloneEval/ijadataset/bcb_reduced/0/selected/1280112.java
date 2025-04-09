package org.jdesktop.jdic.dock.internal.impl;

import org.jdesktop.jdic.dock.internal.DockService;
import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Toolkit;
import sun.awt.EmbeddedFrame;
import java.lang.reflect.Constructor;
import org.jdesktop.jdic.dock.FloatingDock;
import java.util.HashMap;

public class UnixDockService implements DockService {

    EmbeddedFrame frame;

    int location = FloatingDock.LEFT;

    long window_id;

    native long createDockWindow();

    native long getWidget(long window, int widht, int height, int x, int y);

    native void adjustSizeAndLocation(long window, int width, int height, int location);

    native void mapWindow(long window, boolean b);

    static native boolean locateDock(String JavaHome);

    static native void eventLoop();

    static Thread display_thread;

    static HashMap winmap = new HashMap();

    static {
        Toolkit t = Toolkit.getDefaultToolkit();
        t.sync();
        System.loadLibrary("floatingdock");
        if (!locateDock(System.getProperty("java.home"))) {
            throw new Error("Dock not Found !");
        }
        display_thread = new Thread(new Runnable() {

            public void run() {
                eventLoop();
            }
        });
        display_thread.start();
    }

    public UnixDockService() {
        init();
    }

    void init() {
        window_id = createDockWindow();
        synchronized (winmap) {
            winmap.put(new Long(window_id), (Object) this);
        }
        frame = createEmbeddedFrame(window_id);
    }

    EmbeddedFrame createEmbeddedFrame(long window) {
        EmbeddedFrame ef = null;
        String version = System.getProperty("java.version");
        String os = System.getProperty("os.name");
        if ((version.indexOf("1.5") == -1) || (os.equals("SunOS"))) {
            long w = getWidget(window, 400, 400, 0, 0);
            Class clazz = null;
            try {
                clazz = Class.forName("sun.awt.motif.MEmbeddedFrame");
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Constructor constructor = null;
            try {
                constructor = clazz.getConstructor(new Class[] { int.class });
            } catch (Throwable e1) {
                try {
                    constructor = clazz.getConstructor(new Class[] { long.class });
                } catch (Throwable e2) {
                    e1.printStackTrace();
                }
            }
            Object value = null;
            try {
                value = constructor.newInstance(new Object[] { new Long(w) });
            } catch (Throwable e) {
                e.printStackTrace();
            }
            ef = (EmbeddedFrame) value;
        } else {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (toolkit instanceof sun.awt.motif.MToolkit) {
                Class clazz = null;
                try {
                    clazz = Class.forName("sun.awt.motif.MEmbeddedFrame");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Constructor constructor = null;
                try {
                    constructor = clazz.getConstructor(new Class[] { int.class });
                } catch (Throwable e1) {
                    try {
                        constructor = clazz.getConstructor(new Class[] { long.class });
                    } catch (Throwable e2) {
                        e1.printStackTrace();
                    }
                }
                Object value = null;
                try {
                    value = constructor.newInstance(new Object[] { new Long(window) });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                ef = (EmbeddedFrame) value;
            } else {
                Class clazz = null;
                try {
                    clazz = Class.forName("sun.awt.X11.XEmbeddedFrame");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Constructor constructor = null;
                try {
                    constructor = clazz.getConstructor(new Class[] { int.class });
                } catch (Throwable e1) {
                    try {
                        constructor = clazz.getConstructor(new Class[] { long.class });
                    } catch (Throwable e2) {
                        e1.printStackTrace();
                    }
                }
                Object value = null;
                try {
                    value = constructor.newInstance(new Object[] { new Long(window) });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                ef = (EmbeddedFrame) value;
            }
        }
        return ef;
    }

    public void setVisible(boolean b) {
        frame.setVisible(b);
        mapWindow(window_id, b);
    }

    public boolean getVisible() {
        return frame.isVisible();
    }

    long getWindow() {
        return window_id;
    }

    public void setSize(Dimension d) {
        adjustSizeAndLocation(getWindow(), d.width, d.height, location);
        frame.setSize(d.width, d.height);
        frame.validate();
    }

    void configureWindow(int x, int y, int w, int h) {
        frame.setSize(w, h);
        frame.validate();
    }

    static void configureNotify(long window, int x, int y, int w, int h) {
        UnixDockService uds;
        synchronized (winmap) {
            uds = (UnixDockService) winmap.get(new Long(window));
        }
        if (uds != null) {
            uds.configureWindow(x, y, w, h);
        }
    }

    public Dimension getSize() {
        return frame.getSize();
    }

    public void add(Component c) {
        frame.add(c);
        frame.pack();
        Dimension d = frame.getSize();
        adjustSizeAndLocation(getWindow(), d.width, d.height, location);
    }

    public void remove(Component c) {
        frame.remove(c);
        Dimension d = frame.getSize();
        adjustSizeAndLocation(getWindow(), d.width, d.height, location);
    }

    public void setLayout(LayoutManager l) {
        frame.setLayout(l);
        frame.validate();
        Dimension d = frame.getSize();
        adjustSizeAndLocation(getWindow(), d.width, d.height, location);
    }

    public LayoutManager getLayout() {
        return frame.getLayout();
    }

    public void setLocation(int l) {
        Dimension d = frame.getSize();
        location = l;
        adjustSizeAndLocation(getWindow(), d.width, d.height, location);
    }

    public int getLocation() {
        return location;
    }

    public void setAutoHide(boolean b) {
    }

    public boolean getAutoHide() {
        return true;
    }

    public void addWindowListener(WindowListener l) {
        frame.addWindowListener(l);
    }

    public void removeWindowListener(WindowListener l) {
        frame.removeWindowListener(l);
    }
}
