package org.knopflerfish.bundle.desktop.swing;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.lang.reflect.Constructor;
import org.knopflerfish.service.log.LogRef;
import org.knopflerfish.service.remotefw.RemoteFramework;
import org.knopflerfish.service.desktop.BundleFilter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.knopflerfish.util.Text;

public class Activator implements BundleActivator {

    public static LogRef log;

    private static BundleContext bc;

    private static BundleContext remoteBC;

    public static Desktop desktop;

    static Activator myself;

    public static BundleContext getBC() {
        return bc;
    }

    static BundleFilter bundleFilter = null;

    public static void setBundleFilter(BundleFilter bf) {
        bundleFilter = bf;
    }

    public static Bundle[] getBundles() {
        BundleContext bc = getBC();
        Bundle[] bl = bc.getBundles();
        if (bundleFilter != null) {
            ArrayList al = new ArrayList();
            for (int i = 0; bl != null && i < bl.length; i++) {
                if (bundleFilter.accept(bl[i])) {
                    al.add(bl[i]);
                }
            }
            Bundle[] bl2 = new Bundle[al.size()];
            al.toArray(bl2);
            bl = bl2;
        }
        return bl;
    }

    /**
   * Get target BC for bundle control.
   *
   * <p>
   * This in preparation for the event of the desktop
   * being able to control a remote framework.
   * </p>
   */
    public static BundleContext getTargetBC() {
        if (remoteBC == null) {
            return getBC();
        }
        return remoteBC;
    }

    public static Map getSystemProperties() {
        if (getTargetBC() != getBC()) {
            RemoteFramework rc = (RemoteFramework) remoteTracker.getService();
            return rc.getSystemProperties(getTargetBC());
        } else {
            Properties props = System.getProperties();
            Map map = new HashMap();
            for (Enumeration e = props.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String val = Util.getProperty(key, (String) props.get(key));
                map.put(key, val);
            }
            return map;
        }
    }

    static Vector remoteHosts = new Vector() {

        {
            addElement("http://localhost:8080");
            addElement("http://localhost:8081");
            addElement("http://localhost:80");
        }
    };

    static String remoteHost = "";

    public static BundleContext openRemote(String host) {
        if (host.equals(remoteHost)) {
            return remoteBC;
        }
        RemoteFramework rc = (RemoteFramework) remoteTracker.getService();
        if (rc != null) {
            try {
                Activator.myself.closeDesktop();
                if ("".equals(host) || "local".equals(host)) {
                    remoteBC = null;
                } else {
                    remoteBC = rc.connect(host);
                }
                remoteHost = host;
            } catch (Exception e) {
                log.error("Failed to connect to " + host);
            }
            Activator.myself.openDesktop();
        }
        return remoteBC;
    }

    static ServiceTracker remoteTracker;

    Map displayers = new HashMap();

    public void start(BundleContext _bc) {
        try {
            if (null == System.getProperty("swing.aatext")) {
                System.setProperty("swing.aatext", "true");
            }
        } catch (Exception ignored) {
        }
        Activator.bc = _bc;
        Activator.log = new LogRef(bc);
        Activator.myself = this;
        remoteTracker = new ServiceTracker(bc, RemoteFramework.class.getName(), null) {

            public Object addingService(ServiceReference sr) {
                Object obj = super.addingService(sr);
                try {
                    desktop.setRemote(true);
                } catch (Exception e) {
                }
                return obj;
            }

            public void removedService(ServiceReference sr, Object service) {
                try {
                    desktop.setRemote(false);
                } catch (Exception e) {
                }
                super.removedService(sr, service);
            }
        };
        remoteTracker.open();
        Thread t = new Thread() {

            public void run() {
                openDesktop();
            }
        };
        t.start();
    }

    void openDesktop() {
        if (desktop != null) {
            System.out.println("openDesktop: desktop already open");
            return;
        }
        desktop = new Desktop();
        desktop.start();
        DefaultSwingBundleDisplayer disp;
        ServiceRegistration reg;
        String[] dispClassNames = new String[] { LargeIconsDisplayer.class.getName(), GraphDisplayer.class.getName(), TableDisplayer.class.getName(), ManifestHTMLDisplayer.class.getName(), ClosureHTMLDisplayer.class.getName(), ServiceHTMLDisplayer.class.getName(), PackageHTMLDisplayer.class.getName(), LogDisplayer.class.getName(), EventDisplayer.class.getName(), PrefsDisplayer.class.getName() };
        String dispsS = Util.getProperty("org.knopflerfish.desktop.displays", "").trim();
        if (dispsS != null && dispsS.length() > 0) {
            dispClassNames = Text.splitwords(dispsS, "\n\t ", '\"');
        }
        for (int i = 0; i < dispClassNames.length; i++) {
            String className = dispClassNames[i];
            try {
                Class clazz = Class.forName(className);
                Constructor cons = clazz.getConstructor(new Class[] { BundleContext.class });
                disp = (DefaultSwingBundleDisplayer) cons.newInstance(new Object[] { getTargetBC() });
                disp.open();
                reg = disp.register();
                displayers.put(disp, reg);
            } catch (Exception e) {
                log.warn("Failed to create displayer " + className, e);
            }
        }
        String defDisp = Util.getProperty("org.knopflerfish.desktop.display.main", LargeIconsDisplayer.NAME);
        desktop.bundlePanelShowTab(defDisp);
        int ix = desktop.detailPanel.indexOfTab("Manifest");
        if (ix != -1) {
            desktop.detailPanel.setSelectedIndex(ix);
        }
    }

    void closeDesktop() {
        try {
            if (desktop != null) {
                desktop.stop();
                desktop.theDesktop = null;
                desktop = null;
            }
            for (Iterator it = displayers.keySet().iterator(); it.hasNext(); ) {
                DefaultSwingBundleDisplayer disp = (DefaultSwingBundleDisplayer) it.next();
                ServiceRegistration reg = (ServiceRegistration) displayers.get(disp);
                disp.unregister();
                disp.close();
            }
            displayers.clear();
            if (remoteBC != null) {
                RemoteFramework rc = (RemoteFramework) remoteTracker.getService();
                if (rc != null) {
                    rc.disconnect(remoteBC);
                }
                remoteBC = null;
            }
        } catch (Exception e) {
            log.error("Failed to close desktop", e);
        }
    }

    public void stop(BundleContext bc) {
        try {
            closeDesktop();
            if (log != null) {
                log = null;
            }
            if (remoteTracker != null) {
                remoteTracker.close();
                remoteTracker = null;
            }
            this.bc = null;
            this.myself = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
