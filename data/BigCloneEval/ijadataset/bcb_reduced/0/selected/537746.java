package edu.udo.scaffoldhunter.gui.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to open an {@link URL} in the default system browser
 * 
 * @author Philipp Lewe
 * 
 */
public class UrlOpener {

    private static Logger logger = LoggerFactory.getLogger(UrlOpener.class);

    /**
     * Opens the given {@link URL} in the default browser
     * 
     * @param url
     *            the url to open
     */
    public static void browse(URL url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (IOException e) {
                logfailure();
            } catch (URISyntaxException e) {
                logger.warn("Error occured while converting URL to URI");
            }
        } else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            try {
                new ProcessBuilder("xdg-open", url.toString()).start();
            } catch (IOException e) {
                logfailure();
            }
        } else {
            logger.warn("unsupported operating system: {}", System.getProperty("os.name"));
            logfailure();
        }
    }

    /**
     * Opens the given URL in the default browser
     * 
     * @param url
     *            a <code>String</code> with the url to open
     */
    public static void browse(String url) {
        try {
            browse(new URL(url));
        } catch (MalformedURLException e) {
            logger.warn(String.format("The String '%s' is no valid URL and thus not loaded in default browser", url));
        }
    }

    private static void logfailure() {
        logger.warn("Error occured while trying to open URL");
    }
}
