package org.xblackcat.rojac.gui.popup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Simple implementation to open an URL in system browser.
 *
 * @author xBlackCat
 */
class OpenUrlAction implements ActionListener {

    private static final Log log = LogFactory.getLog(OpenUrlAction.class);

    private final String messageUrl;

    public OpenUrlAction(String url) {
        this.messageUrl = url;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!Desktop.isDesktopSupported()) {
            log.warn("Desktop is not supported.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(messageUrl));
        } catch (IOException e1) {
            log.error("Can not open url " + messageUrl + " in default browser");
        } catch (URISyntaxException e1) {
            log.error("Can not obtain URI of URL: " + messageUrl);
        }
    }
}
