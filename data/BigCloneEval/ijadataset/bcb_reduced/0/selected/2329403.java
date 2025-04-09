package fr.amille.animebrowser.control;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Open a site in the default OS browser.
 * 
 * @author amille
 * 
 */
public final class WebsiteOpener {

    private static WebsiteOpener instance;

    public static WebsiteOpener getInstance() {
        if (WebsiteOpener.instance == null) {
            return new WebsiteOpener();
        } else {
            return WebsiteOpener.instance;
        }
    }

    private WebsiteOpener() {
        WebsiteOpener.instance = this;
    }

    /**
	 * Open a site in the default OS browser.
	 * 
	 * @param url
	 *            the site url
	 */
    public void openSite(final URI uri) {
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
