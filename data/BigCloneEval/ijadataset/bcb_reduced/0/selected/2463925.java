package net.laubenberger.bogatyr.helper.launcher;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import net.laubenberger.bogatyr.helper.HelperLog;
import net.laubenberger.bogatyr.helper.HelperString;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsEmpty;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This launcher starts the system browser and displays an URI.
 *
 * @author Stefan Laubenberger
 * @version 0.9.6 (20110527)
 * @since 0.2.0
 */
public abstract class LauncherBrowser {

    private static final Logger log = LoggerFactory.getLogger(LauncherBrowser.class);

    /**
	 * Displays an {@link URI} in the default browser application.
	 *
	 * @param uri for the browser (e.g. "http://www.laubenberger.net/")
	 * @throws IOException
	 * @see URI
	 * @since 0.2.0
	 */
    public static void browse(final URI uri) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(uri));
        if (null == uri) {
            throw new RuntimeExceptionIsNull("uri");
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
        } else {
            throw new RuntimeException("Browser not supported by your machine");
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Displays an {@link String} in the default browser application.
	 *
	 * @param url for the browser (e.g. "www.laubenberger.net")
	 * @throws IOException
	 * @throws URISyntaxException
	 * @since 0.2.0
	 */
    public static void browse(final String url) throws IOException, URISyntaxException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(url));
        if (null == url) {
            throw new RuntimeExceptionIsNull("url");
        }
        if (!HelperString.isValid(url)) {
            throw new RuntimeExceptionIsEmpty("url");
        }
        final String prefix = "://";
        if (HelperString.contains(url, prefix)) {
            browse(new URI(url));
        } else {
            browse(new URI("http://" + url));
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Displays an {@link URL} in the default browser application.
	 *
	 * @param url for the browser (e.g. "http://www.laubenberger.net/")
	 * @throws IOException
	 * @throws URISyntaxException 
	 * @see URL
	 * @since 0.9.6
	 */
    public static void browse(final URL url) throws IOException, URISyntaxException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(url));
        browse(url.toURI());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }
}
