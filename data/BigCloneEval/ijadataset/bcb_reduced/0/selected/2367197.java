package net.sourceforge.etsysync.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import net.sourceforge.etsysync.utils.event.exceptions.checked.UnableToLaunchBrowserException;

public class BrowserLauncher {

    private static final String[] browsers = { "conkeror", "epiphany", "firefox", "google-chrome", "kazehakase", "konqueror", "opera", "midori", "mozilla" };

    private static enum OperatingSystem {

        MAC, WINDOWS, LINUX
    }

    public static void openURI(URI address) throws UnableToLaunchBrowserException {
        if (Desktop.isDesktopSupported()) {
            Desktop appDesktop = getDesktop();
            try {
                appDesktop.browse(address);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            OperatingSystem system = getOperatingSystem();
            launchFallback(system, address);
        }
    }

    private static Desktop getDesktop() {
        try {
            return Desktop.getDesktop();
        } catch (UnsupportedOperationException uop) {
            uop.printStackTrace();
            return null;
        }
    }

    private static OperatingSystem getOperatingSystem() {
        String OSName = System.getProperty("os.name");
        if (OSName.startsWith("Mac OS")) {
            return OperatingSystem.MAC;
        } else if (OSName.startsWith("Windows")) {
            return OperatingSystem.WINDOWS;
        } else {
            return OperatingSystem.LINUX;
        }
    }

    private static void launchFallback(OperatingSystem os, URI address) throws UnableToLaunchBrowserException {
        if (os == OperatingSystem.LINUX) {
            String browser = findBrowser();
            launchBrowser(browser, address);
        } else {
            throw new UnableToLaunchBrowserException();
        }
    }

    private static String findBrowser() {
        for (String b : browsers) {
            if (testBrowserName(b)) {
                return b;
            }
        }
        return null;
    }

    private static boolean testBrowserName(String name) {
        try {
            return Runtime.getRuntime().exec(new String[] { "which", name }).getInputStream().read() != -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    private static void launchBrowser(String browserName, URI address) {
        try {
            if (browserName != null && address != null) {
                Runtime.getRuntime().exec(new String[] { browserName, address.toString() });
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
