package org.primordion.xholon.io;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.primordion.xholon.app.Application;
import org.primordion.xholon.base.IXholon;
import org.primordion.xholon.base.Xholon;
import org.primordion.xholon.exception.XholonConfigurationException;
import org.primordion.xholon.service.IScriptService;
import org.primordion.xholon.service.IXholonService;

/**
 * Opens a URL in a web browser.
 * @author <a href="mailto:ken@primordion.com">Ken Webb</a>
 * @see <a href="http://www.primordion.com/Xholon">Xholon Project website</a>
 * @since 0.7.1 (Created on October 22, 2007)
 */
public class BrowserLauncher {

    /**
	 * Open a URL.
	 * @param url A URL.
	 */
    public static void launch(String url) {
        if (Application.getApplication().isApplet()) {
            launchUsingBrowser(url);
        } else if (getJavaVersion() < 1.6) {
            launchUsingBl(url);
        } else {
            launchUsingDesktop(url);
        }
    }

    /**
	 * Get the version of Java currently being used.
	 * @return A version number (ex: 1.4 1.6 1.7).
	 */
    protected static double getJavaVersion() {
        double jv = 1.4;
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.charAt(0) == '1') {
            int firstDot = javaVersion.indexOf(".");
            int secondDot = javaVersion.indexOf(".", firstDot + 1);
            String jvStr = javaVersion.substring(0, secondDot);
            jv = Double.parseDouble(jvStr);
        }
        return jv;
    }

    /**
	 * Launch by opening a new browser window or tab.
	 * This can only be used if the Xholon app is running as an applet in a browser.
	 * @param url A URL.
	 */
    protected static void launchUsingBrowser(String url) {
        IXholon scriptService = Application.getApplication().getService(IXholonService.XHSRV_SCRIPT);
        if (scriptService != null) {
            try {
                String scriptContent = "window.open('" + url + "', 'Xholon BrowserLauncher Window', 'height=600,width=600,resizable,scrollbars,status');";
                ((IScriptService) scriptService).evalScript(null, "BrowserJS", scriptContent);
            } catch (XholonConfigurationException e) {
                Xholon.getLogger().error(e.toString());
            }
        }
    }

    /**
	 * Open a URL using java.awt.Desktop .
	 * @param url A URL.
	 */
    protected static void launchUsingDesktop(String url) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            Xholon.getLogger().error("unable to display url: " + url + " java.awt.Desktop.isDesktopSupported() = false");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            Xholon.getLogger().error("unable to display url: " + url + " desktop.isSupported(java.awt.Desktop.Action.BROWSE) = false");
            return;
        }
        try {
            URI uri = new java.net.URI(url);
            desktop.browse(uri);
        } catch (URISyntaxException e1) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e1.toString());
        } catch (IOException e1) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e1.toString());
        }
    }

    /**
	 * Open a URL using edu.stanford.ejalbert.BrowserLauncher .
	 * @param url A URL.
	 */
    protected static void launchUsingBl(String url) {
        String className = "edu.stanford.ejalbert.BrowserLauncher";
        Object launcher = null;
        try {
            launcher = Class.forName(className).newInstance();
        } catch (InstantiationException e1) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e1.toString());
        } catch (IllegalAccessException e1) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e1.toString());
        } catch (ClassNotFoundException e1) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e1.toString());
        }
        if (launcher == null) {
            return;
        }
        try {
            Class parameters[] = new Class[] { Class.forName("java.lang.String") };
            java.lang.reflect.Method m = launcher.getClass().getMethod("openURLinBrowser", parameters);
            m.invoke(launcher, new Object[] { url });
        } catch (SecurityException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        } catch (NoSuchMethodException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        } catch (IllegalArgumentException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        } catch (IllegalAccessException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        } catch (java.lang.reflect.InvocationTargetException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        } catch (ClassNotFoundException e) {
            Xholon.getLogger().error("unable to display url: " + url + " " + e.toString());
        }
    }
}
