package net.esle.sinadura.gui.application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * @author zylk.net
 */
public class WebBrowser {

    /**
	 * @param url
	 */
    public static void openDefaultBrowser(String url) {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = null;
                try {
                    uri = new URI(url);
                    desktop.browse(uri);
                } catch (IOException ioe) {
                    ioe.getMessage();
                } catch (URISyntaxException use) {
                    use.getMessage();
                }
                LoggingDesktopController.printInfo(MessageFormat.format(ResourceHelper.getLanguage().getString("info.browser.open"), url));
            } else LoggingDesktopController.printError(ResourceHelper.getLanguage().getString("warning.no_desktop_support"));
        } else LoggingDesktopController.printError(ResourceHelper.getLanguage().getString("warning.no_desktop_support"));
    }
}
